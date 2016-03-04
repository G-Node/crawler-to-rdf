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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link RDFService} class. Output and Error streams are redirected
 * from the console to a different PrintStream and reset after tests are finished
 * to avoid mixing tool error messages with actual test error messages.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class RDFServiceTest {

    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private PrintStream stdout;

    private final String tmpRoot = System.getProperty("java.io.tmpdir");
    private final String testFolderName = "rdfServiceTest";
    private final Path testFileFolder = Paths.get(tmpRoot, testFolderName);

    /**
     * Redirect Out stream and create a test folder and the main
     * test file in the java temp directory.
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

        final String testFileName = "test.txt";
        final File currTestFile = this.testFileFolder.resolve(testFileName).toFile();
        FileUtils.write(currTestFile, "This is a normal test file");
    }

    /**
     * Reset Out stream to the console and remove all created
     * folders and files after the tests are done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {

        System.setOut(this.stdout);

        if (Files.exists(this.testFileFolder)) {
            FileUtils.deleteDirectory(this.testFileFolder.toFile());
        }
    }

    /**
     * Check, that a Jena RDF model is written to file.
     * @throws Exception
     */
    @Test
    public void testSaveModelToFile() throws Exception {

        final String outFileName = "testRdf.ttl";
        final String outFilePath = this.testFileFolder.resolve(outFileName).toString();
        final String outFileFormat = "TTL";
        final String testString = String.join(
                "", "[INFO ] Writing data to RDF file '",
                outFilePath, "' using format '", outFileFormat, "'"
        );

        final Model model = ModelFactory.createDefaultModel();

        RDFService.saveModelToFile("", model, outFileFormat);
        assertThat(this.outStream.toString()).startsWith("[ERROR] Could not open output file");

        RDFService.saveModelToFile(outFilePath, model, outFileFormat);
        assertThat(this.outStream.toString()).contains(testString);
    }

    /**
     * Test that an empty and a non empty RDF file can be opened.
     * @throws Exception
     */
    @Test
    public void testOpenModelFromFile() throws Exception {
        final File currEmptyTestFile = this.testFileFolder.resolve("testEmpty.ttl").toFile();
        FileUtils.write(currEmptyTestFile, "");

        Model emptyModel = RDFService.openModelFromFile(currEmptyTestFile.toString());
        assertThat(emptyModel.isEmpty()).isTrue();

        final String miniTTL = "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n\n_:a foaf:name\t\"TestName\"";
        final File currTestFile = this.testFileFolder.resolve("test.ttl").toFile();
        FileUtils.write(currTestFile, miniTTL);

        Model m = RDFService.openModelFromFile(currTestFile.toString());
        assertThat(m.isEmpty()).isFalse();
    }

}
