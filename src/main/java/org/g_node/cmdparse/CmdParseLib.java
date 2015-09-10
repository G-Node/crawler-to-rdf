// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.cmdparse;

import java.util.Set;
import org.apache.commons.cli.*;

/**
 * Create options for commandline parser.
 */
public class CmdParseLib {

    public static Options constructGlobalOptions(final Set<String> regCrawlers) {
        final Options options = new Options();

        final Option opHelp = new Option("h", "help", false, "Print this message");

        final Option opCr = Option.builder("c")
                .longOpt("crawler")
                .desc(
                        String.join(
                                "", "Shorthand of the required data crawler. ",
                                "\nAvailable crawlers: ", regCrawlers.toString()
                        )
                )
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        options.addOption(opHelp);
        options.addOption(opCr);

        return options;
    }

}
