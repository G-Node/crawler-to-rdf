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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.g_node.converter.ConvController;
import org.g_node.crawler.LKTLogbook.LKTLogController;
import org.g_node.crawler.LKTLogbook.LKTLogParser;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 *
 * This application is a prototype, don't hate me if stuff is partially suboptimal or outright sucks.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class App {

    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    /**
     * Registry containing all crawlers and RDF to RDF converters implemented and available to this application.
     */
    private final Map<String, Controller> tools;

    /**
     * Constructor.
     */
    App() {
        this.tools = new HashMap<>();
    }

    /**
     * Main method of the crawler-to-rdf framework. Registers all so far available tools and
     * selects and runs the appropriate crawler or RDF to RDF converter dependent on commandline input.
     * @param args User provided {@link CommandLine} arguments.
     */
    public static void main(final String[] args) {

        final String currDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        App.LOGGER.info(String.join("", currDateTime, ", Starting RDF crawler logfile."));

        try {
            final App currApp = new App();
            currApp.register();
            currApp.run(args);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to register all implemented tools with their short hand.
     * The short hand is required to select and run the intended crawler or RDF to RDF converter.
     */
    public final void register() {
        this.tools.put("lkt", new LKTLogController(new LKTLogParser()));
        this.tools.put("conv", new ConvController());
    }

    /**
     * Method to parse the commandline arguments, provide
     * appropriate error messages if required and run the selected
     * crawler or RDF to RDF converter.
     * The first argument of the command line has to be the shorthand of the required crawler or RDF to RDF converter.
     * @param args User provided commandline arguments.
     */
    public final void run(final String[] args) {

        if (args.length < 1) {
            App.LOGGER.error(
                    String.join(
                            "", "No tool selected!",
                            "\n\t Please use syntax: 'java -jar crawler-to-rdf.jar [tool] [tool options]'",
                            "\n\t e.g. 'java -jar crawler-to-rdf.jar lkt -i exampleFile.ods -o out.ttl'",
                            "\n\t Currently available tools: ", this.tools.keySet().toString()
                    )
            );
        } else if (this.tools.containsKey(args[0])) {

            final HelpFormatter printHelp = new HelpFormatter();
            final CommandLineParser parser = new DefaultParser();
            final Controller currCrawlerController = this.tools.get(args[0]);
            final Options useOptions = this.tools.get(args[0]).options(this.tools.keySet());

            try {
                final CommandLine cmd = parser.parse(useOptions, args, false);
                if (cmd.hasOption("h")) {
                    printHelp.printHelp("Help", useOptions);
                    return;
                }
                currCrawlerController.run(cmd);

            } catch (final ParseException exp) {
                printHelp.printHelp("Help", useOptions);
                App.LOGGER.error(
                    String.join("", "\n", exp.getMessage(), "\n")
                );
            }

        } else {
            App.LOGGER.error(
                    String.join(
                            "", "Oh no, selected tool '", args[0], "' does not exist!",
                            "\n\t Please use syntax: 'java crawler-to-rdf.jar [tool] [tool options]'",
                            "\n\t e.g. 'java crawler-to-rdf.jar lkt -i exampleFile.ods -o out.ttl'",
                            "\n\t Currently available tools: ", this.tools.keySet().toString()
                    )
            );
        }
    }
}
