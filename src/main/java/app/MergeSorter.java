package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingDeque;

import static java.util.stream.Collectors.toList;

/**
 * Receives a list of queues as input, determines which queue the required value is in,
 * and writes this value from the queue to a file
 */
public class MergeSorter implements Sorter, Subscriber {
    private List<BlockingDeque<String>> EOFList;
    private BufferedWriter writer;
    private Logger logger;


    /**
     * @param outputFileName - path to the file to write
     */
    public MergeSorter(String outputFileName) {
        EOFList = new ArrayList<>();
        logger = LogManager.getLogger(MergeSorter.class);
        try {
            writer = new BufferedWriter(new FileWriter(outputFileName));
        } catch (IOException exception) {
            logger.error(String.format("Error creating '%s' file cause %s", outputFileName, exception.getMessage()));
            System.exit(7);
        }
    }

    private void writeResult(String result) {
        try {
            writer.write(result);
            writer.newLine();
            writer.flush();
        } catch (IOException exception) {
            logger.warn(String.format("Error writing to output file. %s", exception.getMessage()));
            exception.printStackTrace();
        }
    }

    private void removeEmptyQueue(List<BlockingDeque<String>> queues) {
        for (BlockingDeque<String> queue : queues) {
            if (EOFList.contains(queue) && queue.size() == 0) {
                queues.remove(queue);
                EOFList.remove(queue);
            }
        }
    }

    private int findStringMaxValueIndex(String[] strings) {
        if (strings.length == 1) return 0;
        Optional<String> maximum = Arrays.stream(strings).max(Comparator.comparing(String::toString));
        return Arrays.stream(strings).collect(toList()).indexOf(maximum.get());
    }

    private int findStringsMinValueIndex(String[] strings) {
        if (strings.length == 1) return 0;
        Optional<String> minimum = Arrays.stream(strings).min(Comparator.comparing(String::toString));
        return Arrays.stream(strings).collect(toList()).indexOf(minimum.get());
    }

    private int findIntegersMinValueIndex(String[] numbers) throws NumberFormatException {
        if (numbers.length == 1) return 0;
        Optional<Integer> iMinimum = Arrays.stream(numbers).map(this::toInteger).min(Comparator.comparingInt(Integer::intValue));
        Optional<String> minimum = Optional.of(iMinimum.get().toString());
        return Arrays.stream(numbers).collect(toList()).indexOf(minimum.get());
    }

    private int findIntegersMaxValueIndex(String[] numbers) throws NumberFormatException {
        if (numbers.length == 1) return 0;
        Optional<Integer> iMaximum = Arrays.stream(numbers).map(this::toInteger).max(Comparator.comparingInt(Integer::intValue));
        Optional<String> maximum = Optional.of(iMaximum.get().toString());
        return Arrays.stream(numbers).collect(toList()).indexOf(maximum.get());
    }

    private Integer toInteger(String string) throws NumberFormatException {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException exception) {
            throw new NumberFormatException(
                    String.format(
                            "Incorrect number format in the file, cause %s. " +
                            "Wrong line: %s. File was removed", exception.getMessage(), string));
        }
    }

    private BlockingDeque<String> getRequiredQueue(List<BlockingDeque<String>> queues) throws IllegalArgumentException {
        if (queues.size() == 1) {
            return queues.get(0);
        }

        BlockingDeque<String>[] queuesArray = new BlockingDeque[queues.size()];
        String[] queuesArrayValues = new String[queues.size()];

        int i = 0;
        for (BlockingDeque<String> queue : queues) {
            if (queue.peekFirst() == null) {
                continue;
            }

            queuesArray[i] = queue;
            queuesArrayValues[i] = queue.peekFirst();
            i++;
        }

        if (Arrays.stream(queuesArrayValues).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Not all queues have values yet");
        }

        if (Main.isInteger) {
            if (Main.isAscending) {
                return queuesArray[findIntegersMinValueIndex(queuesArrayValues)];
            } else {
                return queuesArray[findIntegersMaxValueIndex(queuesArrayValues)];
            }
        } else {
            if (Main.isAscending) {
                return queuesArray[findStringsMinValueIndex(queuesArrayValues)];
            } else {
                return queuesArray[findStringMaxValueIndex(queuesArrayValues)];
            }
        }
    }

    private boolean isWrongSortingOrder(BlockingDeque<String> queue) {
        if (queue.peekFirst() == null || queue.peekLast() == null) {
            return false;
        }
        if (Main.isInteger) {
            if (Main.isAscending) {
                return toInteger(queue.peekFirst()) > toInteger(queue.peekLast());
            } else {
                return toInteger(queue.peekFirst()) < toInteger(queue.peekLast());
            }
        } else {
            if (Main.isAscending) {
                return (queue.peekFirst()).compareTo(queue.peekLast()) > 0;
            } else {
                return (queue.peekFirst()).compareTo(queue.peekLast()) < 0;
            }
        }
    }

    @Override
    public void sort(List<BlockingDeque<String>> queues) {
        for (;;) {
            removeEmptyQueue(queues);
            if (queues.size() == 0) {
                break;
            }

            BlockingDeque<String> requiredQueue = null;

            try {
                requiredQueue = getRequiredQueue(queues);
                if (isWrongSortingOrder(requiredQueue)) {
                    queues.remove(requiredQueue);
                    logger.error("Wrong sort order in the file. File was removed");
                }
            } catch (NumberFormatException exception) {
                queues.remove(requiredQueue);
                logger.error(exception.getMessage());
                continue;
            } catch (IllegalArgumentException exception) {
                logger.info(exception.getMessage());
                continue;
            }

            try {
                if (requiredQueue.size() > 0) {
                    writeResult(requiredQueue.takeFirst());
                }
            } catch (InterruptedException exception) {
                logger.error(String.format("Thread was interrupted when getting a value from queue. %s",
                        exception.getMessage()));
            }
        }
    }

    @Override
    public void respondToNotification(BlockingDeque<String> queue) {
        EOFList.add(queue);
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException exception) {
            System.err.printf("File closing error: %s", Main.outputFileName);
        }
    }
}
