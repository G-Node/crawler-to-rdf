/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link ConvController} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class ConvControllerTest {

    private ConvController convCont = new ConvController();

    /**
     * Tests for the {@link CommandLine} {@link Options} of the RDF to RDF converter tool.
     * @throws Exception
     */
    @Test
    public void optionsTest() throws Exception {
        Set<String> testRegToolsSet = new HashSet<>(Arrays.asList("one", "two"));
        Options checkOpt = this.convCont.options(testRegToolsSet);

        assertThat(checkOpt.getOptions().size()).isEqualTo(4);

        assertThat(checkOpt.hasOption("-i")).isTrue();
        assertThat(checkOpt.hasLongOption("in-file")).isTrue();
        assertThat(checkOpt.getOption("-i").isRequired()).isTrue();

        assertThat(checkOpt.hasOption("-o")).isTrue();
        assertThat(checkOpt.hasLongOption("out-file")).isTrue();
        assertThat(checkOpt.getOption("-o").isRequired()).isFalse();

        assertThat(checkOpt.hasOption("-f")).isTrue();
        assertThat(checkOpt.hasLongOption("out-format")).isTrue();
        assertThat(checkOpt.getOption("-f").isRequired()).isFalse();

        assertThat(checkOpt.hasOption("-h")).isTrue();
    }

}
