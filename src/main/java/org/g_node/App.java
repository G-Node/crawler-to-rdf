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
import org.g_node.cmdparse.CmdParseLib;
import org.g_node.crawler.variants.LKTLogbook;

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
        Options useOptions = CmdParseLib.constructOptions();

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

            LKTLogbook parseLogBook = new LKTLogbook();
            parseLogBook.parseFile(cmd.getOptionValue("i"));

        }

        catch (ParseException exp) {
            printHelp.printHelp("Help", useOptions);
            System.err.println("Parser error: " + exp.getMessage());
        }

    }

}
