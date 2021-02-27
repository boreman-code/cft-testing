package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Passes the paths to the input files to the readers, gets the queue list and passes this list for sorting
 */
public class Handler implements Closeable {
    private Logger logger;
    private List<FileLinesReader> readers;
    private List<BlockingDeque<String>> queues;
    private Mediator mediator;
    private Sorter sorter;

    public Handler(Mediator mediator, Sorter sorter) {
        logger = LogManager.getLogger(Handler.class);
        this.mediator = mediator;
        this.sorter = sorter;

        readers = new ArrayList<>();
        queues = new CopyOnWriteArrayList<>();
    }

    public void processing() {
        for (String fileName : Main.inputFileNames) {
            FileLinesReader reader = new FileLinesReader(fileName, mediator);
            if (reader.isReadable()) {
                readers.add(reader);
            }
        }
        if (readers.size() > 0) {
            for (FileLinesReader reader : readers) {
                BlockingDeque<String> queue = reader.writeLinesToQueue();
                if (queue != null) {
                    queues.add(queue);
                }
            }
        } else {
            logger.error("No files that can be processed");
            System.exit(8);
        }
        sorter.sort(queues);
    }

    @Override
    public void close(){
        readers.forEach(FileLinesReader::close);
        System.out.println("Sorting completed");
    }
}
