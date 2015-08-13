// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler;

import java.util.*;

/**
 * Used to register all available crawlers in the main app.
 */
public class CrawlerRegistry {

    private final Map<String, CrawlerReference> registry = new HashMap<>();

    public CrawlerReference getReference(final String refName) {
        CrawlerReference result;
        if(isRegistered(refName)) {
            result = registry.get(refName);
        } else {
            result = new CrawlerReference(refName);
            registry.put(refName, result);
        }
        return result;
    }

    public boolean isRegistered(final String refName) {
        return registry.containsKey(refName);
    }

    public void register(final CrawlerTemplate obj) {
        final CrawlerReference ref = getReference(obj.getNameRegistry());
        if(!ref.hasCrawler() || ref.getCrawler() != obj) {
            ref.setCrawler(obj);
        }
    }

    public void unregister(final CrawlerTemplate obj) {
        if(isRegistered(obj.getNameRegistry())) {
            final CrawlerReference ref = getReference(obj.getNameRegistry());
            if(ref.hasCrawler()) {
                ref.setCrawler(null);
            }
        }
    }

    public Set<String> registeredKeys() {
        return registry.keySet();
    }

}
