// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node;

import java.util.*;

import org.apache.commons.cli.*;
import org.g_node.crawler.LKTLogbook.LKTLogbook;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 */
public class App {

    private final Map<String, Command> crawlers;

    App() {
        crawlers = new HashMap<>();
    }

    public static void main(final String[] args) {
        App currApp = new App();
        currApp.register();
        currApp.run(args);
    }

    public void register() {
        crawlers.put("lkt", new LKTCrawlerCommand(new LKTLogbook()));
    }

    public void run(final String[] args) {

        if (crawlers.containsKey(args[0])){

            final HelpFormatter printHelp = new HelpFormatter();
            final CommandLineParser parser = new DefaultParser();
            final Command currCrawlerCommand = crawlers.get(args[0]);
            final Options useOptions = crawlers.get(args[0]).options(crawlers.keySet());

            try {

                final CommandLine cmd = parser.parse(useOptions, args);
                if (cmd.hasOption("h")) {
                    printHelp.printHelp("Help", useOptions);
                    return;
                }
                currCrawlerCommand.run(cmd);

            } catch (final ParseException exp) {
                printHelp.printHelp("Help", useOptions);
                System.err.println(String.join("", "Parser error: ", exp.getMessage()));
            }

        } else {
            System.err.println(
                    String.join("", "Oh no, provided crawler '",
                            args[0], "' does not exist!",
                            "\n Please use syntax: 'java crawler-to-rdf.jar [crawler] [crawler options]'",
                            " \n Available crawlers: ", crawlers.keySet().toString()
                    )
            );
        }
    }
}
