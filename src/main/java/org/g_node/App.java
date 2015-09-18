/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.g_node.crawler.Command;
import org.g_node.crawler.LKTLogbook.LKTCrawlerCommand;
import org.g_node.crawler.LKTLogbook.LKTLogbook;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 * @version $Id 0.01$
 */
public class App {

    private final Map<String, Command> crawlers;

    /**
     * Constructor.
     */
    App() {
        crawlers = new HashMap<>();
    }

    /**
     * Main method of the crawler-to-rdf framework. Registers all so far available crawlers and
     * selects and runs the appropriate crawler dependent on commandline input.
     * @param args User provided commandline arguments.
     */
    public static void main(final String[] args) {
        final App currApp = new App();
        currApp.register();
        currApp.run(args);
    }

    /**
     * Method to register all implemented crawlers with their short hand.
     * The short hand is required to select and run the intended crawler.
     */
    public final void register() {
        crawlers.put("lkt", new LKTCrawlerCommand(new LKTLogbook()));
    }

    /**
     * Method to parse the commandline arguments, provide
     * appropriate error messages if required and run the selected
     * crawler.
     * @param args User provided commandline arguments.
     */
    public final void run(final String[] args) {

        // The first argument of the command line has to be the crawler shorthand.
        if (crawlers.containsKey(args[0])) {

            final HelpFormatter printHelp = new HelpFormatter();
            final CommandLineParser parser = new DefaultParser();
            final Command currCrawlerCommand = crawlers.get(args[0]);
            final Options useOptions = crawlers.get(args[0]).options(crawlers.keySet());

            try {

                final CommandLine cmd = parser.parse(useOptions, args, false);
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
                    String.join(
                            "", "Oh no, provided crawler '", args[0], "' does not exist!",
                            "\n Please use syntax: 'java crawler-to-rdf.jar [crawler] [crawler options]'",
                            " \n Available crawlers: ", crawlers.keySet().toString()
                    )
            );
        }
    }
}
