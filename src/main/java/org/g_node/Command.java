// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node;

import java.util.Set;

import org.apache.commons.cli.*;

/**
 * Interface for command classes of individual crawlers.
 */
public interface Command {

    Options options(Set<String> regCrawlers);

    void run(CommandLine cmd);
}