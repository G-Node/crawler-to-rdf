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
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the main App. Output and Error streams are redirected
 * from the console to a different PrintStream and reset after tests are finished
 * to avoid mixing tool error messages with actual test error messages.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class AppTest {

    private App currApp;
    private ByteArrayOutputStream outStream;
    private PrintStream stdout;

    /**
     * Redirect Error and Out stream.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        this.stdout = System.out;
        this.outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(this.outStream));

        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(
                new ConsoleAppender(
                        new PatternLayout("[%-5p] %m%n")
                )
        );

        this.currApp = new App();
        this.currApp.register();
    }

    /**
     * Reset Out stream to the console after the tests are done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        System.setOut(this.stdout);
    }

    @Test
    public void testMain() throws Exception {
        final String[] emptyArgs = new String[0];
        App.main(emptyArgs);
        assertThat(this.outStream.toString()).contains("No crawler selected!");
    }

    @Test
    public void testRunWrongCrawler() throws Exception {
        final String[] wrongCrawler = new String[1];
        wrongCrawler[0] = "iDoNotExist";
        currApp.run(wrongCrawler);
        assertThat(this.outStream.toString())
                .contains("Oh no, selected crawler 'iDoNotExist' does not exist!");
    }

    @Test
    public void testMissingInput() throws Exception {
        final String[] missingArgs = new String[1];
        missingArgs[0] = "lkt";
        currApp.run(missingArgs);
        assertThat(this.outStream.toString())
                .contains("Missing required option: i");
    }

    @Test
    public void testRunHelp() throws Exception {
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
    }
}
