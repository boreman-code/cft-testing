package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class Main {
    protected static boolean isInteger = true;
    protected static boolean isAscending = true;
    protected static String outputFileName;
    protected static List<String> inputFileNames;
    protected static Logger logger;

    public static void main(String[] args) {
        logger = LogManager.getLogger(Main.class);
        CommandsParser parser = new CommandsParser(args);
        parser.parse();

        Mediator mediator = new MediatorImpl();

        try(Sorter sorter = new MergeSorter(outputFileName);
        Handler handler = new Handler(mediator, sorter)) {
            mediator.subscribe((Subscriber) sorter);
            handler.processing();
        } catch (IOException exception) {
            logger.warn("Files closing error");
        }
    }
}
