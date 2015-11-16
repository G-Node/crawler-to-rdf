/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Interface for command classes of individual crawlers and converters.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public interface Controller {
    /**
     * Method returning available commandline options of the crawler
     * or converter corresponding to the command class.
     * @return Constructed set of commandline options.
     */
    Options options();

    /**
     * Method running the crawler corresponding to the command class.
     * @param cmd User commandline input.
     */
    void run(CommandLine cmd);
}
