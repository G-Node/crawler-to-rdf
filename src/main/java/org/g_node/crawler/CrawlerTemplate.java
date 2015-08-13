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
 * Interface for the main file crawler
 */
public interface CrawlerTemplate {

    public String getNameRegistry();

    public String getNameVerbose();

    public ArrayList<String> getParsableFileTypes();

    public Options getCLIOptions(Set<String> regCrawlers);

    public boolean checkCLIOptions(CommandLine cmd);

    public void parseFile(String inputFile);

}


