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
import static org.assertj.core.api.Assertions.catchThrowable;
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

    /**
     * Test checks via regular expression that the method returns a valid timestamp.
     * Further checks that a proper exception is thrown if a Non-DateTimeFormatter pattern is used.
     */
    @Test
    public void testGetTimeStamp() throws Exception {
        final String testFormat = "dd.MM.yyyy HH:mm";
        final String regEx = String.join("",
                "([0][1-9]|[1-2][0-9]|[3][0-1]).([0][1-9]|[1][0-2]).(20\\d\\d) ",
                "([0-1][0-9]|[2][0-3]):[0-5][0-9]");

        final String getFormattedTimeStamp = AppUtils.getTimeStamp(testFormat);
        assertThat(getFormattedTimeStamp).matches(regEx);

        final String testInvalidFormat = "something definitely not dateTime";
        Throwable thrown = catchThrowable(() -> AppUtils.getTimeStamp(testInvalidFormat));
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown pattern letter: o");
    }

}
