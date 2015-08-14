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
import org.g_node.crawler.*;
import org.g_node.crawler.LKTLogbook.LKTLogbook;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 */
public class App
{
    public static void main( String[] args )
    {
        // Register all available crawlers
        CrawlerRegistry cr = new CrawlerRegistry();
        cr.register(new LKTLogbook());

        HelpFormatter printHelp = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();

        // Construct the global parser options
        Options useOptions = CmdParseLib.constructGlobalOptions(cr.registeredKeys());

        try {
            String useCrawler;

            // use global parser options, to first check only if a valid parser has been selected,
            // do not stop at unrecognizable arguments for now.
            CommandLine initCMD = parser.parse(useOptions, args, true);

            if (initCMD.hasOption("c") & cr.isRegistered(initCMD.getOptionValue("c"))) {
                useCrawler = initCMD.getOptionValue("c");
            } else {
                throw new ParseException("Invalid crawler selected, please use one of the following options: "
                        + cr.registeredKeys().toString());
            }

            // add new cli parser options specific for the selected crawler
            cr.getReference(useCrawler).getCrawler().getCLIOptions(cr.registeredKeys()).getOptions().forEach(useOptions::addOption);

            try {


                // Test commiting stuff to repository

                System.out.println("Crawler: "+ useCrawler);

                CommandLine cmd = parser.parse(useOptions, args);

                if (cmd.hasOption("h")) {
                    printHelp.printHelp("Help", useOptions);
                    return;
                }

                CrawlerTemplate currCrawler = cr.getReference(cmd.getOptionValue("c")).getCrawler();
                if(currCrawler.checkCLIOptions(cmd)) {
                    currCrawler.parseFile(cmd.getOptionValue("i"));
                }
            }

            catch (ParseException exp) {
                printHelp.printHelp("Help", useOptions);
                System.err.println("Parser error: " + exp.getMessage());
            }

        } catch (ParseException exp) {
            printHelp.printHelp("Help", useOptions);
            System.err.println("Parser error: " + exp.getMessage());
        }

    }

}
