/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for simple App.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class AppTest {

    private App currApp;
    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;

    @Before
    public void setUp() throws Exception {
        this.outStream = new ByteArrayOutputStream();
        this.errStream = new ByteArrayOutputStream();

        this.currApp = new App();
        this.currApp.register();
    }

    @Test
    public void testMain() {
        PrintStream stderr = System.err;
        System.setErr(new PrintStream(this.errStream));

        final String[] emptyArgs = new String[0];
        App.main(emptyArgs);
        assertThat(this.errStream.toString()).startsWith("No crawler selected!");

        System.setErr(stderr);
    }

    @Test
    public void testRunWrongCrawler() {
        PrintStream stderr = System.err;
        System.setErr(new PrintStream(this.errStream));

        final String[] wrongCrawler = new String[1];
        wrongCrawler[0] = "iDoNotExist";
        currApp.run(wrongCrawler);
        assertThat(this.errStream.toString())
                .startsWith("Oh no, selected crawler 'iDoNotExist' does not exist!");

        System.setErr(stderr);
    }

    @Test
    public void testMissingInput() {
        PrintStream stderr = System.err;
        System.setErr(new PrintStream(this.errStream));

        final String[] missingArgs = new String[1];
        missingArgs[0] = "lkt";
        currApp.run(missingArgs);
        assertThat(this.errStream.toString()).startsWith("\n[Error] Missing required option: i");

        System.setErr(stderr);
    }

    @Test
    public void testRunHelp() {
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(this.outStream));

        final String[] helpArgs = new String[4];
        helpArgs[0] = "lkt";
        helpArgs[1] = "-h";
        helpArgs[2] = "-i";
        helpArgs[3] = "file";
        currApp.run(helpArgs);
        assertThat(this.outStream.toString()).startsWith("usage: Help");

        final String[] helpArgsLong = new String[4];
        helpArgsLong[0] = "lkt";
        helpArgsLong[1] = "--help";
        helpArgsLong[2] = "-i";
        helpArgsLong[3] = "file";
        currApp.run(helpArgsLong);
        assertThat(this.outStream.toString()).startsWith("usage: Help");

        System.setOut(stdout);
    }
}
