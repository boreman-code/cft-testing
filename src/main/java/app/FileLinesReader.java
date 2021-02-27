package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Reads lines from a file in a separate thread and writes the results to BlockingDeque with a capacity of two
 */
public class FileLinesReader implements Runnable, Closeable {
    private Logger logger;
    private Mediator mediator;
    private String fileName;
    private BufferedReader reader;
    private BlockingDeque<String> queue;
    private ExecutorService executorService;
    private boolean readable;

    /**
     * @param fileName - path to the file to read
     * @param mediator - mediator for notifying subscribers about the end of a file
     */
    public FileLinesReader(String fileName, Mediator mediator) {
        logger = LogManager.getLogger(FileLinesReader.class);
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException exception) {
            logger.error(String.format("File '%s' not found", fileName));
            readable = false;
            return;
        }

        this.mediator = mediator;
        this.fileName = fileName;

        readable = true;
        queue = new LinkedBlockingDeque<>(2);
        executorService = Executors.newSingleThreadExecutor();
    }

    public BlockingDeque<String> writeLinesToQueue() {
        if (readable) {
            executorService.execute(this);
            return queue;
        } else {
            return null;
        }
    }

    public boolean isReadable() {
        return readable;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                line = line.trim();
                try {
                    queue.putLast(line);
                } catch (InterruptedException e) {
                    System.err.printf("Interrupted reading of the file '%s'", fileName);
                }
            }
        } catch (IOException exception) {
            System.err.printf("Error reading '%s' cause %s", fileName, exception.getMessage());
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException exception) {
            System.err.printf("Closing file '%s' error: %s", fileName, exception.getMessage());
        }

        mediator.notifyEOF(queue);
        executorService.shutdown();

    }
}
