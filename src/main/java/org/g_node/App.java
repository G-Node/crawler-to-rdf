// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node;

import org.apache.commons.cli.*;
import org.g_node.cmdparse.CmdParseLib;
import org.g_node.crawler.*;
import org.g_node.crawler.LKTLogbook.LKTLogbook;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 */
public class App {

    public static void main(final String[] args) {
        // Register all available crawlers
        final CrawlerRegistry cr = new CrawlerRegistry();
        cr.register(new LKTLogbook());

        final HelpFormatter printHelp = new HelpFormatter();
        final CommandLineParser parser = new DefaultParser();

        // Construct the global parser options
        final Options useOptions = CmdParseLib.constructGlobalOptions(cr.registeredKeys());

        try {
            String useCrawler;

            // TODO not really happy with the split commandline parser, maybe there is a more sophisticated way
            // TODO to do this
            // Use global parser options, to first check only if a valid parser has been selected,
            // do not stop at unrecognizable arguments for now.
            final CommandLine initCMD = parser.parse(useOptions, args, true);

            if (initCMD.hasOption("c") && cr.isRegistered(initCMD.getOptionValue("c"))) {
                useCrawler = initCMD.getOptionValue("c");
            } else {
                throw new ParseException(
                        String.join(
                                " ", "Invalid crawler selected, please use one of the following options:",
                                cr.registeredKeys().toString()
                        )
                );
            }

            // Add new CLI parser options specific for the selected crawler
            cr.getReference(useCrawler).getCrawler().getCLIOptions(cr.registeredKeys()).getOptions().forEach(useOptions::addOption);

            try {
                // TODO Test commit stuff to repository
                System.out.println(String.join("", "Crawler: ", useCrawler));

                final CommandLine cmd = parser.parse(useOptions, args);
                if (cmd.hasOption("h")) {
                    printHelp.printHelp("Help", useOptions);
                    return;
                }

                final CrawlerTemplate currCrawler = cr.getReference(cmd.getOptionValue("c")).getCrawler();
                if (currCrawler.validateCLIOptions(cmd)) {
                    // TODO Not sure whether handing the filename over is a good idea.
                    currCrawler.parseFile(cmd.getOptionValue("i"));
                }

            } catch (final ParseException exp) {
                printHelp.printHelp("Help", useOptions);
                System.err.println(String.join("", "Parser error: ", exp.getMessage()));
            }

        } catch (final ParseException exp) {
            printHelp.printHelp("Help", useOptions);
            System.err.println(String.join("", "Parser error: ", exp.getMessage()));
        }
    }

}
