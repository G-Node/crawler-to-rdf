/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.srv;

import org.apache.commons.cli.Option;
import org.g_node.micro.commons.RDFService;

/**
 * Class provides CLI {@link Option}s that are common
 * to all crawlers and converters of this service.
 */
public final class CliOptionService {

    /**
     * Returns help option.
     * @param altDesc Optional description.
     * @return Help option.
     */
    public static Option getHelpOpt(final String altDesc) {

        final String defaultDesc = "Print this message.";
        final String desc = !altDesc.isEmpty() ? altDesc : defaultDesc;

        return new Option("h", "help", false, desc);
    }

    /**
     * Returns option required to parse a given input file name from the command line.
     * Commandline option shorthand will always be "-i" and "-in-file". This
     * option will always be "required".
     * @param altDesc Alternative description replacing the default description.
     * @return Required CLI option parsing an input file.
     */
    public static Option getInFileOpt(final String altDesc) {

        final String defaultDesc = "Input file that's supposed to be parsed.";
        final String desc = !altDesc.isEmpty() ? altDesc : defaultDesc;

        return Option.builder("i")
                .longOpt("in-file")
                .desc(desc)
                .required()
                .hasArg()
                .valueSeparator()
                .build();
    }

    /**
     * Returns option required to parse a given output file name from the command line.
     * Commandline option shorthand will always be "-o" and "-out-file".
     * @param altDesc Alternative description replacing the default description.
     * @return CLI option parsing an output file.
     */
    public static Option getOutFileOpt(final String altDesc) {

        final String defaultDesc = String.join(
                "", "Optional: Path and name of the output file. ",
                "Files with the same name will be overwritten. ",
                "Default file name uses format [inputFileName]_out.");
        final String desc = !altDesc.isEmpty() ? altDesc : defaultDesc;

        return  Option.builder("o")
                .longOpt("out-file")
                .desc(desc)
                .hasArg()
                .valueSeparator()
                .build();
    }

    /**
     * Returns option required to parse a given output format from the command line.
     * Commandline option shorthand will always be "-f" and "-out-format".
     * @param altDesc Alternative description replacing the default description.
     * @return CLI option parsing an output format.
     */
    public static Option getOutFormatOpt(final String altDesc) {

        final String defaultDesc = String.join(
                "", "Optional: format of the RDF file that will be written.\n",
                "Supported file formats: ", RDFService.RDF_FORMAT_MAP.keySet().toString(),
                "\nDefault setting is the Turtle (TTL) format.");
        final String desc = !altDesc.isEmpty() ? altDesc : defaultDesc;

        return Option.builder("f")
                .longOpt("out-format")
                .desc(desc)
                .hasArg()
                .valueSeparator()
                .build();
    }
}
