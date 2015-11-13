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
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link LKTLogController} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LKTLogControllerTest {

    private LKTLogController logCtrl;
    private Set<String> logSet = new HashSet<>(Arrays.asList("one", "two"));
    private PrintStream stdout;
    private ByteArrayOutputStream outStream;
    private Logger rootLogger;

    private Path testFileFolder;
    private File currTestFile;

    /**
     * Ensure a fresh {@link LKTLogController} at the beginning of each test.
     * Set up Logger and tmp folder.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.logCtrl = new LKTLogController(new LKTLogParser());

        this.stdout = System.out;
        this.outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(this.outStream));

        this.rootLogger = Logger.getRootLogger();
        this.rootLogger.setLevel(Level.INFO);
        this.rootLogger.addAppender(
                new ConsoleAppender(
                        new PatternLayout("[%-5p] %m%n")
                )
        );

        final String tmpRoot = System.getProperty("java.io.tmpdir");
        final String testFolderName = "fileservicetest";
        final String testFileName = "test.txt";
        this.testFileFolder = Paths.get(tmpRoot, testFolderName);
        this.currTestFile = this.testFileFolder.resolve(testFileName).toFile();
        FileUtils.write(this.currTestFile, "This is a normal test file");
    }

    /**
     * Delete temporary test files and folders, close Logger and reset std out.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        if (Files.exists(this.testFileFolder)) {
            FileUtils.deleteDirectory(this.testFileFolder.toFile());
        }
        this.rootLogger.removeAllAppenders();
        System.setOut(this.stdout);
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
     * Tests the run method of the {@link LKTLogController}.
     * Will test LKTLogParser, LKTLogParserSheet and LKTLogParserEntry as well with an invalid ods sheet.
     * @throws Exception
     */
    @Test
    public void runTest() throws Exception {
        final String testODSFileName = "test.ods";
        final File currODSTestFile = this.testFileFolder.resolve(testODSFileName).toFile();
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

        assertThat(this.outStream.toString()).contains("Input file iDoNotExist does not exist");
        this.outStream.reset();

        // Test existing input file, unsupported file type
        args = new String[2];
        args[0] = "-i";
        args[1] = this.currTestFile.toString();
        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(this.outStream.toString()).contains(
                String.join("", "[ERROR] Input RDF file ", this.currTestFile.toString(), " cannot be read.")
        );
        this.outStream.reset();

        // Test provide supported input file type, unsupported output RDF format
        args = new String[4];
        args[0] = "-i";
        args[1] = currODSTestFile.toString();
        args[2] = "-f";
        args[3] = "iDoNotExist";
        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(this.outStream.toString()).contains("[ERROR] Unsupported output format: 'IDONOTEXIST'");
        this.outStream.reset();

        // Test provide supported input file type, w/o proper entries.
        args = new String[2];
        args[0] = "-i";
        args[1] = currODSTestFile.toString();
        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(this.outStream.toString()).contains(
                String.join("",
                        "[Parser] sheet null does not contain valid data.")
        );
        this.outStream.reset();

        // Test use custom invalid test file.
        final String invODSTestFile = this.getClass().getResource("/lkt_test_invalid.ods").getPath();

        args = new String[2];
        args[0] = "-i";
        args[1] = invODSTestFile;

        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(this.outStream.toString())
                .contains("There are parser errors present. Please resolve them and run the program again.");
        this.outStream.reset();
    }

    /**
     * Tests the run method of the {@link LKTLogController} with a valid ods file.
     * Will test LKTLogParser, LKTLogParserSheet, LKTLogParserEntry and LKTLogToRDF as well.
     * @throws Exception
     */
    @Test
    public void runTestValidFile() throws Exception {
        final File currOutFile = this.testFileFolder.resolve("out.ttl").toFile();
        FileUtils.write(currOutFile, "");

        final CommandLineParser parser = new DefaultParser();
        final Options useOptions = this.logCtrl.options(this.logSet);
        String[] args;
        CommandLine cmd;

        final String validODSTestFile = this.getClass().getResource("/lkt_test.ods").getPath();
        args = new String[4];
        args[0] = "-i";
        args[1] = validODSTestFile;
        args[2] = "-o";
        args[3] = this.testFileFolder.resolve("out.ttl").toString();

        cmd = parser.parse(useOptions, args, false);
        this.logCtrl.run(cmd);

        assertThat(this.outStream.toString())
                .contains(
                        String.join("", "Writing data to RDF file, ",
                                this.testFileFolder.resolve("out.ttl").toString(),
                                " using format 'TTL'")
                );
        this.outStream.reset();
    }
}
