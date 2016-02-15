/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * Unit tests for the {@link AppUtils} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class AppUtilsTest {

    /**
     * Method checks that the {@link AppUtils} getHashSHA method returns
     * the proper hex hash for a List of String entries.
     */
    @Test
    public void testGetHashSHA() {
        final String checkHexSHA = "e5c055cdf43af5be8972d5a94c8d0182296de89b";

        final ArrayList<String> testHashLower = new ArrayList<>(Arrays.asList("hash", "me"));
        assertThat(checkHexSHA.equals(AppUtils.getHashSHA(testHashLower))).isTrue();

        final ArrayList<String> testHashMixed = new ArrayList<>(Arrays.asList("Hash", "Me"));
        assertThat(checkHexSHA.equals(AppUtils.getHashSHA(testHashMixed))).isTrue();

        final List<String> testHashSingleEntry = Collections.singletonList("Hash me");
        assertThat(checkHexSHA.equals(AppUtils.getHashSHA(testHashSingleEntry))).isTrue();

        final List<String> testDiff = Collections.singletonList("hashme");
        assertThat(checkHexSHA.equals(AppUtils.getHashSHA(testDiff))).isFalse();
    }

}
