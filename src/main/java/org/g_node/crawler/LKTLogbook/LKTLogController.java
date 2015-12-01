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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.g_node.Controller;
import org.g_node.srv.CliOptionService;
import org.g_node.srv.CtrlCheckService;
import org.g_node.srv.RDFService;

/**
 * Command class for the LKT crawler.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class LKTLogController implements Controller {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(LKTLogController.class.getName());
    /**
     * File types that can be processed by this crawler.
     */
    private static final List<String> SUPPORTED_INPUT_FILE_TYPES = Collections.singletonList("ODS");
    /**
     * ArrayList containing all messages that occurred while parsing the input file(s).
     * All parser errors connected to missing values or incorrect value formats should
     * be collected and written to a logfile, so that users can correct these
     * mistakes ideally all at once before running the crawler again.
     */
    private ArrayList<String> parserErrorMsg = new ArrayList<>(0);
    /**
     * The actual crawler this class handles and provides.
     */
    private LKTLogParser crawler;

    /**
     * Constructor.
     * @param crl Instance of the {@link LKTLogParser} crawler.
     */
    public LKTLogController(final LKTLogParser crl) {
        this.crawler = crl;
    }

    /**
     * Method returning the commandline options of the LKT crawler.
     * @return Available {@link CommandLine} {@link Options}.
     */
    public Options options() {
        final Options options = new Options();

        final Option opHelp = CliOptionService.getHelpOpt("");
        final Option opIn = CliOptionService.getInFileOpt("");
        final Option opOut = CliOptionService.getOutFileOpt("");
        final Option opFormat = CliOptionService.getOutFormatOpt("");

        options.addOption(opHelp);
        options.addOption(opIn);
        options.addOption(opOut);
        options.addOption(opFormat);

        return options;
    }

    /**
     * Method to parse information from an input file to an output file using
     * the LKT crawler. Handles all checks related to input file, output file and
     * file format before the parsing begins.
     * @param cmd User provided {@link CommandLine} input.
     */
    public void run(final CommandLine cmd) {

        final String inputFile = cmd.getOptionValue("i");
        if (!CtrlCheckService.existingInputFile(inputFile)) {
            return;
        }

        if (!CtrlCheckService.supportedInFileType(inputFile, LKTLogController.SUPPORTED_INPUT_FILE_TYPES)) {
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

        LKTLogController.LOGGER.info("Parsing input file...");
        final ArrayList<LKTLogParserSheet> allSheets = this.crawler.parseFile(inputFile, this.parserErrorMsg);

        if (this.parserErrorMsg.size() != 0) {
            LKTLogController.LOGGER.error("");
            this.parserErrorMsg.forEach(LKTLogController.LOGGER::error);
            return;
        }

        LKTLogController.LOGGER.info("Converting parsed data to RDF...");
        final LKTLogToRDF convRDF = new LKTLogToRDF();
        convRDF.createRDFModel(allSheets, inputFile, outputFile, outputFormat);
    }
}
