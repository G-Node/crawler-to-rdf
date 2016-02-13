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
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;
import org.g_node.micro.commons.CliToolController;
import org.g_node.micro.commons.RDFService;
import org.g_node.srv.CliOptionService;
import org.g_node.srv.CtrlCheckService;

/**
 * Controller class for the RDF to RDF converter.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class ConvCliToolController implements CliToolController {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(ConvCliToolController.class.getName());
    /**
     * Method returning the commandline options of the RDF to RDF converter.
     * @return Available commandline options.
     */
    public final Options options() {
        final Options options = new Options();

        final Option opHelp = CliOptionService.getHelpOpt("");
        final Option opIn = CliOptionService.getInFileOpt(
                "Input RDF file that's supposed to be converted into a different RDF format.");
        final Option opOut = CliOptionService.getOutFileOpt("");
        final Option opFormat = CliOptionService.getOutFormatOpt("");

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
        if (!CtrlCheckService.isExistingFile(inputFile)) {
            return;
        }

        final List<String> checkExtension = RDFService.RDF_FORMAT_EXTENSION.values()
                .stream()
                .map(c->c.toUpperCase(Locale.ENGLISH))
                .collect(Collectors.toList());
        if (!CtrlCheckService.isSupportedInFileType(inputFile, checkExtension)) {
            return;
        }

        final String outputFormat = cmd.getOptionValue("f", "TTL").toUpperCase(Locale.ENGLISH);
        if (!CtrlCheckService.isSupportedOutputFormat(outputFormat, RDFService.RDF_FORMAT_MAP.keySet())) {
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
            ConvCliToolController.LOGGER.info("Reading input file...");
            convData = RDFService.openModelFromFile(inputFile);

        } catch (RiotException e) {
            ConvCliToolController.LOGGER.error(e.getMessage());
            // TODO find out how to print stacktrace to log4j logfile
            e.printStackTrace();
            return;
        }

        RDFService.saveModelToFile(outputFile, convData, outputFormat);
    }

}
