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
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.g_node.crawler.Controller;
import org.g_node.srv.FileService;
import org.g_node.srv.RDFService;

/**
 * Command class for the LKT crawler.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LKTLogController implements Controller {
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
     * Method to parse information from an input file to an output file using
     * the LKT crawler. Handles all checks related to input file, output file and
     * file format before the parsing begins.
     * @param cmd Commandline input provided by the user
     */
    public final void run(final CommandLine cmd) {

        System.out.println("[Info] Checking input file...");
        final String inputFile = cmd.getOptionValue("i");
        if (!FileService.checkFile(inputFile)) {
            System.err.println(String.join("", "[Error] Input file ", inputFile, " does not exist."));
            return;
        } else if (!FileService.checkFileType(inputFile, LKTLogController.SUPPORTED_INPUT_FILE_TYPES)) {
            System.err.println(
                    String.join(
                            "", "[Error] Invalid input file type: ", inputFile,
                            "\n\tOnly the following file types are supported: ",
                            LKTLogController.SUPPORTED_INPUT_FILE_TYPES.toString())
            );
            return;
        }

        System.out.println("[Info] Checking output format...");
        final String outputFormat = cmd.getOptionValue("f", "TTL").toUpperCase(Locale.ENGLISH);
        if (!RDFService.RDF_FORMAT_MAP.containsKey(outputFormat)) {
            System.err.println(
                    String.join("",
                            "[Error] Unsupported output format: '", outputFormat, "'",
                            "\n Please use one of the following: ",
                            RDFService.RDF_FORMAT_MAP.keySet().toString()
                    )
            );
            return;
        }

        final int i = inputFile.lastIndexOf('.');
        final String defaultOutputFile = String.join("", inputFile.substring(0, i), "_out");

        final String outputFile = cmd.getOptionValue("o", defaultOutputFile);

        System.out.println("[Info] Parsing input file...");
        this.crawler.parseFile(inputFile, outputFile, outputFormat, this.parserErrorMsg);

        if (this.parserErrorMsg.size() != 0) {
            this.parserErrorMsg.forEach(System.err::println);
            return;
        }

        System.out.println("Hurra, no parser errors!");

    }
}
