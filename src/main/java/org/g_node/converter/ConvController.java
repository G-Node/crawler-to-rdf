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

import com.hp.hpl.jena.rdf.model.Model;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;
import org.g_node.Controller;
import org.g_node.srv.CtrlCheckService;
import org.g_node.srv.RDFService;

/**
 * Controller class for the RDF to RDF converter.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class ConvController implements Controller {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(ConvController.class.getName());
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
     * Method converting data from an input RDF file to an RDF file of a different supported RDF format.
     * @param cmd User provided {@link CommandLine} input.
     */
    public final void run(final CommandLine cmd) {

        final String inputFile = cmd.getOptionValue("i");
        if (!CtrlCheckService.existingInputFile(inputFile)) {
            return;
        }

        final List<String> checkExtension = RDFService.RDF_FORMAT_EXTENSION.values()
                .stream()
                .map(c->c.toUpperCase(Locale.ENGLISH))
                .collect(Collectors.toList());
        if (!CtrlCheckService.supportedInFileType(inputFile, checkExtension)) {
            return;
        }

        final String outputFormat = cmd.getOptionValue("f", "TTL").toUpperCase(Locale.ENGLISH);
        if (!CtrlCheckService.supportedOutputFormat(outputFormat)) {
            return;
        }

        final int i = inputFile.lastIndexOf('.');
        final String defaultOutputFile = String.join("", inputFile.substring(0, i), "_out");
        String outputFile = cmd.getOptionValue("o", defaultOutputFile);

        if (!outputFile.toLowerCase().endsWith(RDFService.RDF_FORMAT_EXTENSION.get(outputFormat))) {
            outputFile = String.join("", outputFile, ".", RDFService.RDF_FORMAT_EXTENSION.get(outputFormat));
        }

        Model convData;

        try {
            ConvController.LOGGER.info("Reading input file...");
            convData = RDFService.openModelFromFile(inputFile);

        } catch (RiotException e) {
            ConvController.LOGGER.error(e.getMessage());
            // TODO find out how to print stacktrace to log4j logfile
            e.printStackTrace();
            return;
        }

        RDFService.writeModelToFile(outputFile, convData, outputFormat);
    }

}
