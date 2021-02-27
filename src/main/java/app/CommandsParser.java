package app;

import org.apache.commons.cli.*;

import java.util.List;

/**
 *  Parses arguments from the command line
 */

public class CommandsParser {
    private String[] args;
    private Options options;

    private String parseFailedWarning = "Command line parsing failed";
    private String similarOptionsWarning = "It is possible to use only one parameter: ";
    private String noRequiredParameterWarning = "None of the required parameters are specified: ";
    private String noFilesWarning = "No output or input file names";
    private String helpMessage = "java -jar sorter.jar [OPTIONS] out.txt in1.txt in2.txt in(n).txt\n" +
            "\t(to sort all input file(-s) and write the result to the output file)\n\n";


    /**
     * @param args - array of arguments from the command line
     */
    public CommandsParser(String[] args) {
        this.args = args;
        options = new Options();
    }

    private void printUsage() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(helpMessage, options);
    }

    private void printUsageAndExit(int code) {
        printUsage();
        System.exit(code);
    }

    public void parse() {
        options.addOption("h", "help", false, "to print this help message");
        options.addOption("i", "integer", false, "to select integer type. " +
                "Exclude -s (--string). Required");
        options.addOption("s", "string", false, "to select string type. " +
                "Exclude -i (--integer). Required");
        options.addOption("a", "ascending", false, "to sort in ascending order. " +
                "Exclude -d (--descending). Optional. Installed by default");
        options.addOption("d", "descending", false, "to sort in ascending order. " +
                "Exclude -a (--ascending). Optional");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException exception) {
            System.out.println(parseFailedWarning);
            printUsageAndExit(1);
        }

        if (cmd == null) {
            System.out.println(parseFailedWarning);
            printUsageAndExit(2);
        } else {
            if (cmd.hasOption("h") || args.length == 0) {
                printUsageAndExit(0);
            }
            if (!(cmd.hasOption("i") || cmd.hasOption("s"))) {
                System.out.println(noRequiredParameterWarning + "-i or -s");
                printUsageAndExit(3);
            }
            if (cmd.hasOption("i") && cmd.hasOption("s")) {
                System.out.println(similarOptionsWarning + "-i or -s");
                printUsageAndExit(4);
            }
            if (cmd.hasOption("a") && cmd.hasOption("d")) {
                System.out.println(similarOptionsWarning + "-a or -d");
                printUsageAndExit(5);
            }

            List<String> files = cmd.getArgList();
            if (files.size() < 2) {
                System.out.println(noFilesWarning);
                printUsageAndExit(6);
            }
            if (cmd.hasOption("s")) {
                Main.isInteger = false;
            }
            if (cmd.hasOption("d")) {
                Main.isAscending = false;
            }

            Main.outputFileName = files.get(0);
            files.remove(0);
            Main.inputFileNames = files;
        }

    }
}
