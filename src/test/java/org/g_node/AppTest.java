/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 * Michael Sonntag (sonntag@bio.lmu.de)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class AppTest
    extends TestCase {
    /**
     * Create the test case.
     * @param testName Name of the test case.
     */
    public AppTest(final String testName) {
        super(testName);
    }

    /**
     * Get the test suite.
     * @return The suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-).
     */
    public final void testApp() {
        assertTrue(true);
    }
}
