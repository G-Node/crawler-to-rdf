/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.crawler.LKTLogbook;

import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.g_node.crawler.Command;

/**
 * Command class for the LKT crawler.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 * @version $Id 0.01$
 */
public class LKTCrawlerCommand implements Command {

    private LKTLogbook crawler;

    /**
     * Constructor.
     * @param crl Instance of the {@LKTLogbook} crawler.
     */
    public LKTCrawlerCommand(final LKTLogbook crl) {
        this.crawler = crl;
    }

    /**
     * Method returning the commandline options of the LKT crawler.
     * @param regCrawlers Set of all registered crawlers.
     * @return Available commandline options.
     */
    public final Options options(final Set<String> regCrawlers) {
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

    /**
     * Method to parse information from an input file to an output file using
     * the LKT crawler.
     * @param cmd Commandline input provided by the user
     */
    public final void run(final CommandLine cmd) {
        crawler.parseFile(cmd.getOptionValue("i"));
        // TODO write stuff to a file in the specified format
    }

}
