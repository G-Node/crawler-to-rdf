/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.crawler.LKTLogbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.DefaultTableModel;

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
import org.g_node.converter.ConvController;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link LKTLogController} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LKTLogControllerTest {

    private LKTLogController logCtrl = new LKTLogController(new LKTLogParser());
    private Set<String> logSet = new HashSet<>(Arrays.asList("one", "two"));

    /**
     * Ensure a fresh {@link LKTLogController} at the beginning of each test.
     */
    @Before
    public void provideStuff() {
        this.logCtrl = new LKTLogController(new LKTLogParser());
    }

    /**
     * Tests for the {@link CommandLine} {@link Options} of the RDF to RDF converter tool.
     * @throws Exception
     */
    @Test
    public void testOptions() throws Exception {
        Options checkOpt = this.logCtrl.options(this.logSet);

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

        final Logger rootLogger = Logger.getRootLogger();
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

        final String testODSFileName = "test.ods";
        final File currODSTestFile = testFileFolder.resolve(testODSFileName).toFile();
        final File ods = new File(currODSTestFile.toString());
        SpreadSheet.createEmpty(new DefaultTableModel()).saveAs(ods);


        final CommandLineParser parser = new DefaultParser();
        final Options useOptions = this.logCtrl.options(this.logSet);
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
        this.logCtrl.run(cmd);

        assertThat(outStream.toString()).contains("Input file iDoNotExist does not exist");
        outStream.reset();

        // Test existing input file, unsupported file type
        args = new String[2];
        args[0] = "-i";
        args[1] = currTestFile.toString();
        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(outStream.toString()).contains(
                String.join("", "[ERROR] Input RDF file ", currTestFile.toString(), " cannot be read.")
        );
        outStream.reset();

        // Test provide supported input file type, unsupported output RDF format
        args = new String[4];
        args[0] = "-i";
        args[1] = currODSTestFile.toString();
        args[2] = "-f";
        args[3] = "iDoNotExist";
        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(outStream.toString()).contains("[ERROR] Unsupported output format: 'IDONOTEXIST'");
        outStream.reset();

        // Test provide supported input file type, w/o proper entries.
        args = new String[2];
        args[0] = "-i";
        args[1] = currODSTestFile.toString();
        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(outStream.toString()).contains(
                String.join("",
                        "[Parser] sheet null does not contain valid data.")
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
