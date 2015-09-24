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
import org.g_node.crawler.Controller;
import org.g_node.crawler.LKTLogbook.LKTCrawlerController;
import org.g_node.crawler.LKTLogbook.LKTLogbook;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class App {

    /**
     * Registry containing all crawlers implemented
     * and available to this application.
     */
    private final Map<String, Controller> crawlers;

    /**
     * Constructor.
     */
    App() {
        this.crawlers = new HashMap<>();
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
        this.crawlers.put("lkt", new LKTCrawlerController(new LKTLogbook()));
    }

    /**
     * Method to parse the commandline arguments, provide
     * appropriate error messages if required and run the selected
     * crawler.
     * The first argument of the command line has to be the shorthand of the required crawler.
     * @param args User provided commandline arguments.
     */
    public final void run(final String[] args) {

        if (args.length < 1) {
            System.err.println(
                    String.join(
                            "", "No crawler selected!",
                            "\n Please use syntax: 'java crawler-to-rdf.jar [crawler] [crawler options]'",
                            "\n e.g. 'java crawler-to-rdf.jar lkt -i labbook.ods -o out.ttl'",
                            "\n Currently available crawlers: ", this.crawlers.keySet().toString()
                    )
            );
        } else if (this.crawlers.containsKey(args[0])) {

            final HelpFormatter printHelp = new HelpFormatter();
            final CommandLineParser parser = new DefaultParser();
            final Controller currCrawlerController = this.crawlers.get(args[0]);
            final Options useOptions = this.crawlers.get(args[0]).options(this.crawlers.keySet());

            try {

                final CommandLine cmd = parser.parse(useOptions, args, false);
                if (cmd.hasOption("h")) {
                    printHelp.printHelp("Help", useOptions);
                    return;
                }
                currCrawlerController.run(cmd);

            } catch (final ParseException exp) {
                printHelp.printHelp("Help", useOptions);
                System.err.println(
                        String.join("", "\n[Error] ", exp.getMessage(), "\n")
                );
            }

        } else {
            System.err.println(
                    String.join(
                            "", "Oh no, selected crawler '", args[0], "' does not exist!",
                            "\n Please use syntax: 'java crawler-to-rdf.jar [crawler] [crawler options]'",
                            "\n e.g. 'java crawler-to-rdf.jar lkt -i labbook.ods -o out.ttl'",
                            "\n Currently available crawlers: ", this.crawlers.keySet().toString()
                    )
            );
        }
    }
}
