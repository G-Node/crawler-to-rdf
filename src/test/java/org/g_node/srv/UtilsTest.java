/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.srv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link Utils} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class UtilsTest {

    /**
     * Method checks that the {@link Utils} getHashSHA256 method returns
     * the proper hex hash for a List of String entries.
     */
    @Test
    public void testGetHashSHA256() {
        final String checkHexSHA256 = "c82b1b9a0ef4f87e13050b542881d1d158014e6d575f8efa7c4e2043b8c0cdf1";

        final ArrayList<String> testHashLower = new ArrayList<>(Arrays.asList("hash", "me"));
        assertThat(checkHexSHA256.equals(Utils.getHashSHA256(testHashLower))).isTrue();

        final ArrayList<String> testHashMixed = new ArrayList<>(Arrays.asList("Hash", "Me"));
        assertThat(checkHexSHA256.equals(Utils.getHashSHA256(testHashMixed))).isTrue();

        final List<String> testHashSingleEntry = Collections.singletonList("Hash me");
        assertThat(checkHexSHA256.equals(Utils.getHashSHA256(testHashSingleEntry))).isTrue();

        final List<String> testDiff = Collections.singletonList("hashme");
        assertThat(checkHexSHA256.equals(Utils.getHashSHA256(testDiff))).isFalse();
    }
}
