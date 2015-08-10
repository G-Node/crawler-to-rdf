// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler.variants;

import java.util.*;

import org.g_node.crawler.CrawlerTemplate;

/**
 * Parser for the main ODS metadata file used in the lab of Kay Thurley
 */
public class LKTParseUserODS implements CrawlerTemplate {

    public final String nameCommandLine = "lkt";
    public final String nameVerbose = "LMU Kay Thurley main metadata crawler";
    public final ArrayList<String> parsableFileTypes = new ArrayList<String>(Arrays.asList("ODS"));

    public String returnNameCommandLine(){
        return nameCommandLine;
    }

    public String returnNameVerbose(){
        return nameVerbose;
    }

    public ArrayList<String> returnParsableFileTypes(){
        return parsableFileTypes;
    }

}
