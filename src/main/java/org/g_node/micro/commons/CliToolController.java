/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.commons;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Interface for command classes of individual g-node RDF microservice commandline tools.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public interface CliToolController {
    /**
     * Method returning available commandline options of the tool corresponding to the command class.
     * @return Constructed set of commandline options.
     */
    Options options();

    /**
     * Method running the tool corresponding to the command class.
     * @param cmd User commandline input.
     */
    void run(CommandLine cmd);
}
