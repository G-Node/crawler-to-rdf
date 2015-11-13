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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link ConvController} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class ConvControllerTest {

    private ConvController convCont = new ConvController();
    private Set<String> convSet = new HashSet<>(Arrays.asList("one", "two"));

    /**
     * Ensure a fresh {@link ConvController} at the beginning of each test.
     */
    @Before
    public void provideStuff() {
        this.convCont = new ConvController();
    }

    /**
     * Tests for the {@link CommandLine} {@link Options} of the RDF to RDF converter tool.
     */
    @Test
    public void optionsTest() {
        Options checkOpt = this.convCont.options(this.convSet);

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

    /**
     * Tests the run method of the {@link ConvController}.
     * @throws Exception
     */
    @Test
    public void runTest() throws Exception {
        // TODO check if this should be split up into different tests.

        // Set up environment
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
        final PrintStream errout = System.err;
        final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errStream));

        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(
                new ConsoleAppender(
                        new PatternLayout("[%-5p] %m%n")
                )
        );

        // Create test files
        final String tmpRoot = System.getProperty("java.io.tmpdir");
        final String testFolderName = "fileservicetest";
        final String testFileName = "test.txt";
        final Path testFileFolder = Paths.get(tmpRoot, testFolderName);
        final File currTestFile = testFileFolder.resolve(testFileName).toFile();
        FileUtils.write(currTestFile, "This is a normal test file");

        final String testTurtleFileName = "test.ttl";
        final String testTurtleDefaultOutName = "test_out.ttl";
        final File currTurtleTestFile = testFileFolder.resolve(testTurtleFileName).toFile();
        FileUtils.write(currTurtleTestFile, "");

        final String testInvalidFileName = "test.rdf";
        final File currInvalidTestFile = testFileFolder.resolve(testInvalidFileName).toFile();
        FileUtils.write(currInvalidTestFile, "I shall crash!");

        final String outFileName = testFileFolder.resolve("out.ttl").toString();

        final CommandLineParser parser = new DefaultParser();
        final Options useOptions = this.convCont.options(this.convSet);
        String[] args;
        CommandLine cmd;

        // Test missing argument
        args = new String[1];
        args[0] = "-i";
        try {
            parser.parse(useOptions, args, false);
        } catch (MissingArgumentException e) {
            assertThat(e).hasMessage("Missing argument for option: i");
        }

        // Test non existent input file
        args = new String[2];
        args[0] = "-i";
        args[1] = "iDoNotExist";
        cmd = parser.parse(useOptions, args, false);
        this.convCont.run(cmd);

        assertThat(outStream.toString()).contains("Input file iDoNotExist does not exist");
        outStream.reset();

        // Test existing input file, unsupported file type
        args = new String[2];
        args[0] = "-i";
        args[1] = currTestFile.toString();
        cmd = parser.parse(useOptions, args, false);
        this.convCont.run(cmd);

        assertThat(outStream.toString()).contains(
                String.join("", "[ERROR] Input RDF file ", currTestFile.toString(), " cannot be read.")
        );
        outStream.reset();

        // Test provide supported input file type, unsupported output RDF format
        args = new String[4];
        args[0] = "-i";
        args[1] = currTurtleTestFile.toString();
        args[2] = "-f";
        args[3] = "iDoNotExist";
        cmd = parser.parse(useOptions, args, false);
        this.convCont.run(cmd);

        assertThat(outStream.toString()).contains("[ERROR] Unsupported output format: 'IDONOTEXIST'");
        outStream.reset();

        // Test provide supported input file type, test write output file w/o providing output filename
        args = new String[2];
        args[0] = "-i";
        args[1] = currTurtleTestFile.toString();
        cmd = parser.parse(useOptions, args, false);
        this.convCont.run(cmd);

        assertThat(outStream.toString()).contains(
                String.join("",
                "[INFO ] Writing data to RDF file, ", testFileFolder.resolve(testTurtleDefaultOutName).toString(), " using format 'TTL'")
        );
        outStream.reset();

        // Test invalid RDF input file
        args = new String[2];
        args[0] = "-i";
        args[1] = currInvalidTestFile.toString();
        cmd = parser.parse(useOptions, args, false);
        this.convCont.run(cmd);

        assertThat(outStream.toString()).contains("[line: 1, col: 1 ] Content is not allowed in prolog.");
        outStream.reset();

        // Test writing to provided output file does not match provided output format
        args = new String[6];
        args[0] = "-i";
        args[1] = currTurtleTestFile.toString();
        args[2] = "-o";
        args[3] = outFileName;
        args[4] = "-f";
        args[5] = "JSON-LD";

        cmd = parser.parse(useOptions, args, false);
        this.convCont.run(cmd);

        assertThat(outStream.toString()).contains(
                String.join("",
                        "[INFO ] Writing data to RDF file, ", outFileName, ".jsonld using format 'JSON-LD'")
        );
        outStream.reset();

        // Test writing to provided output file
        args = new String[4];
        args[0] = "-i";
        args[1] = currTurtleTestFile.toString();
        args[2] = "-o";
        args[3] = outFileName;

        cmd = parser.parse(useOptions, args, false);
        this.convCont.run(cmd);

        assertThat(outStream.toString()).contains(
                String.join("",
                        "[INFO ] Writing data to RDF file, ", outFileName, " using format 'TTL'")
        );
        outStream.reset();

        // Clean up
        if (Files.exists(testFileFolder)) {
            FileUtils.deleteDirectory(testFileFolder.toFile());
        }
        rootLogger.removeAllAppenders();
        System.setOut(stdout);
        System.setErr(errout);
    }

}
