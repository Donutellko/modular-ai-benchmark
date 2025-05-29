package org.donutellko.modularbench;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {

        Options options = new Options();

        OptionGroup commands = new OptionGroup();
        commands.addOption(new Option("bench", false, "Run the benchmark"));
        commands.setRequired(true);
        options.addOptionGroup(commands);

        options.addOption(Option.builder("config")
                .hasArg()
                .argName("file.yaml")
                .desc("Path to the configuration YAML file")
                .required()
                .build());

        options.addOption(Option.builder("tasks")
                .hasArgs()
                .argName("file1.yaml,file2.yaml,...")
                .desc("Comma-separated list of task YAML file paths")
                .required()
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("bench")) {
                String configPath = line.getOptionValue("config");
                String[] taskPaths = line.getOptionValues("tasks");

                System.out.println("Running benchmark with config: " + configPath);
                System.out.println("Task files: ");
                for (String task : taskPaths) {
                    System.out.println(" - " + task);
                }
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed. Reason: " + exp.getMessage());
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar modularbench.jar", options, true);
    }
}
