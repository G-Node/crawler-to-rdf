// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node;

import java.nio.file.*;
import java.util.Set;
import org.apache.commons.cli.*;
import org.apache.commons.cli.CommandLine;
import org.g_node.crawler.LKTLogbook.LKTLogbook;

/**
 * Command class for the LKT crawler.
 */
public class LKTCrawlerCommand implements Command {

    LKTLogbook crawler;

    LKTCrawlerCommand(LKTLogbook crawler) {
        this.crawler = crawler;
    }

    public Options options(final Set<String> regCrawlers){
        final Options options = new Options();

        final Option opHelp = new Option("h", "help", false, "Print this message");

        final Option opIn = Option.builder("i")
                .longOpt("in-file")
                .desc("Input file that's supposed to be parsed")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        final Option opOut = Option.builder("o")
                .longOpt("out-file")
                .desc(
                        String.join(
                                "", "Optional: Path and name of the output file. ",
                                "Files with the same name will be overwritten. ",
                                "Default file name uses format 'YYYYMMDDHHmm'"
                        )
                )
                .hasArg()
                .valueSeparator()
                .build();

        final Option opFormat = Option.builder("f")
                .longOpt("out-format")
                .desc(
                        String.join(
                                "", "Optional: format of the RDF file that will be written. ",
                                "Default setting is the Turtle (ttl) format"
                        )
                )
                .hasArg()
                .valueSeparator()
                .build();

        options.addOption(opHelp);
        options.addOption(opIn);
        options.addOption(opOut);
        options.addOption(opFormat);

        return options;
    }

    public void run(final CommandLine cmd) {
        if (validateCLIOptions(cmd)) {
            // TODO Not sure whether handing the filename over is a good idea.
            crawler.parseFile(cmd.getOptionValue("i"));
        } else {
            System.err.println("Error parsing commandline options");
        }
    }

    /**
     * Method for validating user provided command line options.
     * @param cmd User provided commandline options.
     * @return
     */
    private final boolean validateCLIOptions(final CommandLine cmd) {
        // TODO find better name
        boolean correctCLI = true;
        if (cmd.hasOption("i")) {
            System.out.println(String.join(" ", "Input file:", cmd.getOptionValue("i")));
            if (!Files.exists(Paths.get(cmd.getOptionValue("i")))
                    && (!Files.exists(Paths.get(cmd.getOptionValue("i")).toAbsolutePath())))  {
                System.err.println("\nProvided input is not a valid file");
                correctCLI = false;
            }
        }

        if (cmd.hasOption("o")) {
            System.out.println(String.join(" ", "Output file:", cmd.getOptionValue("o")));
        }

        if (cmd.hasOption("f")) {
            System.out.println(String.join(" ", "Use output format:", cmd.getOptionValue("f")));
        }

        return correctCLI;
    }


}
