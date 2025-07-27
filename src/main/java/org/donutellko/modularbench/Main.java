package org.donutellko.modularbench;

import org.apache.commons.cli.*;
import org.donutellko.modularbench.dto.BenchResults;
import org.donutellko.modularbench.dto.ExecutionConfig;
import org.donutellko.modularbench.dto.TaskSource;
import org.donutellko.modularbench.service.BenchExecutorService;
import org.donutellko.modularbench.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Main implements CommandLineRunner {

    @Autowired
    private FileService fileService;

    @Autowired
    private BenchExecutorService benchExecutorService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            // No arguments - run as web server
            return;
        }

        Options options = new Options();

        OptionGroup commands = new OptionGroup();
        commands.addOption(new Option("bench", "Run benchmark"));
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
                ExecutionConfig executionConfig = fileService.readExecConfig(configPath);
                String[] taskPaths = line.getOptionValues("tasks");

                System.out.println("Running benchmark with config: " + configPath);
                List<TaskSource> taskSources = new ArrayList<>();
                System.out.println("Task files: ");
                for (String task : taskPaths) {
                    System.out.println(" - " + task);
                    TaskSource taskSource = fileService.readTaskSource(task);
                    taskSources.add(taskSource);
                }
                BenchResults results = benchExecutorService.evaluate(executionConfig, taskSources, "bench-results.yaml");
                System.out.println(results);
                fileService.writeBenchResults("bench-results.yaml", results);
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar modularbench.jar", options, true);
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed. Reason: " + exp.getMessage());
        }
    }
}
