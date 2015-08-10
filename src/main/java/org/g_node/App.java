// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node;

import java.nio.file.*;

import org.apache.commons.cli.*;
import org.g_node.crawler.variants.LKTParseUserODS;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 */
public class App
{
    public static void main( String[] args )
    {
        HelpFormatter printHelp = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        Options useOptions = constructOptions();

        try {
            CommandLine cmd = parser.parse(useOptions, args);

            if(cmd.hasOption("h")) {
                System.out.println("Help: "+ cmd.getOptionValue("h"));
                return;
            }

            if(cmd.hasOption("i")) {
                System.out.println("Input file: "+ cmd.getOptionValue("i"));
                if(!Files.exists(Paths.get(cmd.getOptionValue("i"))) &&
                        (!Files.exists(Paths.get(cmd.getOptionValue("i")).toAbsolutePath())))  {
                    System.out.println("\nProvided input is not a valid file");
                    return;
                }
            }

            if(cmd.hasOption("c")) {
                System.out.println("Crawler: "+ cmd.getOptionValue("c"));
            }

            if(cmd.hasOption("o")) {
                System.out.println("Output file: "+ cmd.getOptionValue("o"));
            }

            if(cmd.hasOption("f")) {
                System.out.println("Use output format: "+ cmd.getOptionValue("f"));
            }

            if(cmd.hasOption("n")) {
                System.out.println("Ontology file: "+ cmd.getOptionValue("n"));
            }

            LKTParseUserODS x = new LKTParseUserODS();
            x.parseFile(cmd.getOptionValue("i"));

        }

        catch (ParseException exp) {
            printHelp.printHelp("Help", useOptions);
            System.err.println("Parser error: " + exp.getMessage());
        }

    }

    private static Options constructOptions(){
        final Options options = new Options();

        Option opHelp = new Option("h", "help", false, "Print this message");

        Option opCr = Option.builder("c")
                .longOpt("crawler")
                .desc("Shorthand of the required data crawler")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        Option opIn = Option.builder("i")
                .longOpt("in-file")
                .desc("Input file thats supposed to be parsed")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        Option opOut = Option.builder("o")
                .longOpt("out-file")
                .desc("Optional: Path and name of the output file. Files with the same name will be overwritten. Default file name uses format 'YYYYMMDDHHmm'")
                .hasArg()
                .valueSeparator()
                .build();

        Option opFormat = Option.builder("f")
                .longOpt("out-format")
                .desc("Optional: format of the RDF file that will be written. Default setting is the Turtle (ttl) format")
                .hasArg()
                .valueSeparator()
                .build();

        Option opOnt = Option.builder("n")
                .longOpt("ont-file")
                .desc("Optional: one or more onotology files can be provided to check if the created RDF files satisfy a required ontology")
                .hasArg()
                .valueSeparator()
                .build();

        options.addOption(opHelp);
        options.addOption(opCr);
        options.addOption(opIn);
        options.addOption(opOut);
        options.addOption(opFormat);
        options.addOption(opOnt);

        return options;
    }

}
