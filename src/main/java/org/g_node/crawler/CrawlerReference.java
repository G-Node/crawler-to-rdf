// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler;

/**
 * Wrapper Object for Crawler Objects used in by the CrawlerRegistry
 */
public class CrawlerReference {
    private CrawlerTemplate crawler;
    private final String refName;

    public CrawlerReference(final String refName) {
        this.refName = refName;
    }

    public String getRefName() {
        return refName;
    }

    public boolean hasCrawler() {
        return crawler != null;
    }

    public CrawlerTemplate getCrawler() {
        return crawler;
    }

    public void setCrawler(final CrawlerTemplate crawler) {
        this.crawler = crawler;
    }

}
