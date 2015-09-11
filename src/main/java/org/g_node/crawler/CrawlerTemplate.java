// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler;

import java.util.*;
import org.apache.commons.cli.*;

/**
 * Interface for the main file crawler.
 */
public interface CrawlerTemplate {

    String getNameRegistry();

    String getNameVerbose();

    ArrayList<String> getParsableFileTypes();

    Options getCLIOptions(Set<String> regCrawlers);

    boolean validateCLIOptions(CommandLine cmd);

    void parseFile(String inputFile);

}
