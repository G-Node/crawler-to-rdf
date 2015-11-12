/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.converter;

import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.g_node.Controller;
import org.g_node.srv.RDFService;

/**
 * Command class for the RDF to RDF converter.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class ConvController implements Controller {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(ConvController.class.getName());
    /**
     * ArrayList containing all messages that occurred while converting the input file.
     */
    //private ArrayList<String> parserErrorMsg = new ArrayList<>(0);

    /**
     * Method returning the commandline options of the RDF to RDF converter.
     * @param regTools Set of all registered tools.
     * @return Available commandline options.
     */
    public final Options options(final Set<String> regTools) {
        final Options options = new Options();

        final Option opHelp = new Option("h", "help", false, "Print this message");

        final Option opIn = Option.builder("i")
                .longOpt("in-file")
                .desc("Input RDF file that's supposed to be converted into a different RDF format.")
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
                                "Default file name uses format [inputFileName]_out"
                        )
                )
                .hasArg()
                .valueSeparator()
                .build();

        final Option opFormat = Option.builder("f")
                .longOpt("out-format")
                .desc(
                        String.join(
                                "", "Optional: format of the RDF file that will be written.\n",
                                "Supported file formats: ", RDFService.RDF_FORMAT_MAP.keySet().toString(),
                                "\nDefault setting is the Turtle (TTL) format."
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
     * Method to convert an input RDF file to an RDF file of a different supported RDF format.
     * @param cmd Commandline input provided by the user
     */
    public final void run(final CommandLine cmd) {

        ConvController.LOGGER.info("Checking input RDF file...");

    }
}
