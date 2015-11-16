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

/**
 * Class provides CLI {@link Option}s that are common
 * to all crawlers and converters of this service.
 */
public class CliOptionService {

    /**
     * Returns help option.
     * @param desc Optional description.
     * @return Help option.
     */
    public final Option getHelpOpt(final String desc) {

        final String defaultDesc = "Print this message.";

        return new Option("h", "help", false, !desc.isEmpty() ? desc : defaultDesc);
    }

    /**
     * Returns option required to parse a given input file name from the command line.
     * Commandline option shorthand will always be "-i" and "-in-file". This
     * option will always be "required".
     * @param desc Alternative description replacing the default description.
     * @return Required CLI option parsing an input file.
     */
    public final Option getInFileOpt(final String desc) {

        final String defaultDesc = "Input file that's supposed to be parsed.";

        return Option.builder("i")
                .longOpt("in-file")
                .desc(!desc.isEmpty() ? desc : defaultDesc)
                .required()
                .hasArg()
                .valueSeparator()
                .build();
    }

    /**
     * Returns option required to parse a given output file name from the command line.
     * Commandline option shorthand will always be "-o" and "-out-file".
     * @param desc Alternative description replacing the default description.
     * @return CLI option parsing an input file.
     */
    public final Option getOutFileOpt(final String desc) {

        final String defaultDesc = String.join(
                "", "Optional: Path and name of the output file. ",
                "Files with the same name will be overwritten. ",
                "Default file name uses format [inputFileName]_out");

        return  Option.builder("o")
                .longOpt("out-file")
                .desc(!desc.isEmpty() ? desc : defaultDesc)
                .hasArg()
                .valueSeparator()
                .build();
    }

    /**
     * Returns option required to parse a given output format from the command line.
     * Commandline option shorthand will always be "-f" and "-out-format".
     * @param desc Alternative description replacing the default description.
     * @return CLI option parsing an input file.
     */
    public final Option getOutFormatOpt(final String desc) {

        final String defaultDesc = String.join(
                "", "Optional: format of the RDF file that will be written.\n",
                "Supported file formats: ", RDFService.RDF_FORMAT_MAP.keySet().toString(),
                "\nDefault setting is the Turtle (TTL) format.");

        return Option.builder("f")
                .longOpt("out-format")
                .desc(!desc.isEmpty() ? desc : defaultDesc)
                .hasArg()
                .valueSeparator()
                .build();
    }
}
