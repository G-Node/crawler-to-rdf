// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 */
public class App {

    public static void main(final String[] args) {
        Crawler.run(args);
    }

}
