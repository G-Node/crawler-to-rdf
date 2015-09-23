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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import org.g_node.srv.RDFService;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Parser for the main ODS metadata file used in the lab of Kay Thurley.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LKTLogbook {

    /**
     * Line within the ODS file where the header of the
     * experiment description section is found.
     */
    private static final Integer SHEET_HEADER_LINE = 23;

    /**
     * String value of the first field of the header
     * of the experiment description section. This string is
     * required to check, if the header line and subsequently
     * the actual data entry lines are properly aligned for
     * the next parsing steps.
     */
    private static final String FIRST_HEADER_ENTRY = "ImportID";

    /**
     * File types that can be processed by this crawler.
     */
    private static final List<String> SUPPORTED_INPUT_FILE_TYPES = Collections.singletonList("ODS");

    /**
     * ArrayList containing all messages that occurred while parsing the ODS file.
     * All parser errors connected to missing values or incorrect value formats should
     * be collected and written to a logfile, so that users can correct these
     * mistakes ideally all at once before running the crawler again.
     */
    private final ArrayList<String> parserErrorMessages = new ArrayList<>(0);

    /**
     * Map containing all projects with their newly created UUIDs of the parsed ODS sheet.
     */
    private final Map<String, String> projectList = new HashMap<>();
    /**
     * Map containing all the animalIDs with their newly created UUIDs of the parsed ODS sheet.
     */
    private final Map<String, String> animalList = new HashMap<>();
    /**
     * Map containing all the experimenters with their newly created UUIDs contained in the parsed ODS sheet.
     */
    private final Map<String, String> experimenterList = new HashMap<>();
    /**
     * Main RDF model containing all the parsed information from the ODS sheet.
     */
    private final Model model = ModelFactory.createDefaultModel();
    /**
     * Namespace used to identify RDF resources and properties specific for the current usecase.
     */
    private final String rdfNamespace = "http://g-node.org/lkt/";

    /**
     * Method for parsing the contents of a provided ODS input file.
     * This method will create a backup file of the original ODS file.
     * @param inputFile ODS file specific to Kay Thurleys usecase.
     */
    public final void parseFile(final String inputFile, final String outputFile, final String outputFormat) {

        System.out.println("[Info] Checking provided file...");

        // TODO is this a good way to end the execution of the app
        // TODO if the input file is not available?
        if (!this.checkInputFile(inputFile)) {
            return;
        }

        System.out.println("[Info] Creating backup file...");
        // TODO check if a robuster solution exists. Also check with Kay,
        // TODO if multiple backup files e.g. with a timestamp should exist.
        // TODO will fail for sure, if the file contains more than one period; should be addressed as well.
        final String backupFile = String.join("", inputFile.split("\\.")[0], "_backup.ods");
        try {
            Files.copy(Paths.get(inputFile), Paths.get(backupFile), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exc) {
            System.err.println(String.join(" ", "[Error] creating backup file:", exc.getMessage()));
            exc.printStackTrace();
            return;
        }

        System.out.println("[Info] Starting to parse provided file...");
        try {
            final File odsFile = new File(inputFile);

            // TODO remove later
            System.out.println(
                    String.join(
                            " ", "[Info] File has # sheets:",
                            String.valueOf(SpreadSheet.createFromFile(odsFile).getSheetCount()))
            );

            if (!(SpreadSheet.createFromFile(odsFile).getSheetCount() > 0)) {
                this.parserErrorMessages.add(
                        String.join(
                                " ", "[Parser error] File", inputFile,
                                "does not contain valid data sheets"
                        )
                );
            } else {
                final ArrayList<LKTLogbookSheet> allSheets = this.parseSheets(odsFile);
                allSheets.forEach(
                        s -> System.out.println(
                                String.join(
                                        " ", "[Info] CurrSheet:", s.getAnimalID(),
                                        ", number of entries:",
                                        String.valueOf(s.getEntries().size())
                                )
                        )
                );

                if (this.parserErrorMessages.size() == 0) {

                    this.createRDFModel(allSheets, outputFile, outputFormat);

                } else {
                    // TODO write to logfile
                    this.parserErrorMessages.forEach(System.err::println);
                }
            }
        } catch (final IOException exp) {
            System.err.println(String.join(" ", "[Error] reading from input file:", exp.getMessage()));
            exp.printStackTrace();
        }
    }

    /**
     * Creates an RDF model from the parsed ODS sheet data and writes
     * the model to the designated output file.
     * @param allSheets Data from the parsed ODS sheets.
     * @param outputFile Name and path of the designated output file.
     * @param outputFormat RDF output format.
     */
    private void createRDFModel(final ArrayList<LKTLogbookSheet> allSheets, final String outputFile, final String outputFormat) {
        allSheets.stream().forEach(
                this::addAnimal
        );
        RDFService.writeModelToFile(outputFile, model, outputFormat);
    }

    /**
     * Adds all data of a parsed ODS sheet to the main RDF model. The problem here is
     * that the ODS sheet is centered around the animal, while the RDF model is project
     * centric.
     * @param currSheet Data from the current sheet.
     */
    private void addAnimal(final LKTLogbookSheet currSheet) {
        final String animalID = currSheet.getAnimalID();
        if (!this.animalList.containsKey(animalID)) {
            this.animalList.put(animalID, UUID.randomUUID().toString());
        }
        final String animalUUID = this.animalList.get(animalID);

        Property permitNr = model.createProperty(String.join("", this.rdfNamespace, "hasNumber"));
        Resource permit = model.createResource(String.join("", this.rdfNamespace, "Permit#", UUID.randomUUID().toString()))
                .addProperty(permitNr, currSheet.getPermitNumber());

        Property animalIDProp = model.createProperty(String.join("", this.rdfNamespace, "hasAnimalID"));
        Property sexProp = model.createProperty(String.join("", this.rdfNamespace, "hasSex"));
        Property birthDate = model.createProperty(String.join("", this.rdfNamespace, "hasBirthDate"));
        Property withdrawalDate = model.createProperty(String.join("", this.rdfNamespace, "hasWithdrawalDate"));
        Property speciesName = model.createProperty(String.join("", this.rdfNamespace, "hasSpeciesName"));
        Property scientificName = model.createProperty(String.join("", this.rdfNamespace, "hasScientificName"));
        Property permitNumber = model.createProperty(String.join("", this.rdfNamespace, "hasPermit"));

        Resource animal = model.createResource(String.join("", this.rdfNamespace, "Animal#", animalUUID))
                                .addProperty(animalIDProp, animalID)
                                .addProperty(sexProp, currSheet.getAnimalSex())
                .addProperty(birthDate, currSheet.getDateOfBirth().toString())
                .addProperty(withdrawalDate, currSheet.getDateOfWithdrawal().toString())
                .addProperty(speciesName, currSheet.getSpecies())
                .addProperty(scientificName, currSheet.getScientificName())
                .addProperty(permitNumber, permit);

        currSheet.getEntries().stream().forEach(
                c -> this.addEntry(c, animal)
        );
    }

    /**
     * Adds the data of the current ODS entry to the main RDF model.
     * @param currEntry Data from the current ODS line entry.
     * @param animal Resource from the main RDF model containing the information about
     *  the animal this entry is associated with.
     */
    private void addEntry(final LKTLogbookEntry currEntry, final Resource animal) {

        final String project = currEntry.getProject();
        // add project only once to the rdf model
        if (!this.projectList.containsKey(project)) {
            this.projectList.put(project, UUID.randomUUID().toString());

            Property projName = model.createProperty(String.join("", this.rdfNamespace, "hasName"));
            Resource proj = model.createResource(String.join("", this.rdfNamespace, "Project#", this.projectList.get(project)))
                    .addProperty(projName, project);
        }
        Resource projectRes = model.getResource(String.join("", this.rdfNamespace, "Project#", this.projectList.get(project)));

        // TODO replace this with the FOAF namespace!
        // TODO add middle name!
        // add experimenter only once to the rdf model
        final String experimenter = String.join(" ", currEntry.getLastName(), currEntry.getFirstName());
        if (!this.experimenterList.containsKey(experimenter)) {
            this.experimenterList.put(experimenter, UUID.randomUUID().toString());

            Property firstName = model.createProperty(String.join("", this.rdfNamespace, "hasFirstName"));
            Property lastName = model.createProperty(String.join("", this.rdfNamespace, "hasLastName"));

            Resource name = model.createResource(String.join("", this.rdfNamespace, "Experimenter#", this.experimenterList.get(experimenter)))
                    .addProperty(firstName, currEntry.getFirstName())
                    .addProperty(lastName, currEntry.getLastName());
        }

        Resource experimenterRes = model.getResource(String.join("", this.rdfNamespace, "Experimenter#", this.experimenterList.get(experimenter)));
        Property expmtr = model.createProperty(String.join("", this.rdfNamespace, "hasExperimenter"));

        Property d = model.createProperty(String.join("", this.rdfNamespace, "startedAt"));
        Property label = model.createProperty(String.join("", this.rdfNamespace, "hasLabel"));
        Property paradigm = model.createProperty(String.join("", this.rdfNamespace, "hasParadigm"));
        Property paradigmSpec = model.createProperty(String.join("", this.rdfNamespace, "hasParadigmSpecifics"));
        Property expCom = model.createProperty(String.join("", this.rdfNamespace, "hasComment"));
        Property animalProp = model.createProperty(String.join("", this.rdfNamespace, "hasAnimal"));

        Resource exp = model.createResource(String.join("", this.rdfNamespace, "Experiment#", currEntry.getImportID()))
                .addProperty(d, currEntry.getExperimentDate().toString())
                .addProperty(label, currEntry.getExperiment())
                .addProperty(paradigm, currEntry.getParadigm())
                .addProperty(paradigmSpec, currEntry.getParadigmSpecifics())
                .addProperty(expCom, currEntry.getCommentExperiment())
                .addProperty(expmtr, experimenterRes)
                .addProperty(animalProp, animal);

        Property hasExpProp = model.createProperty(String.join("", this.rdfNamespace, "hasExperiment"));
        projectRes.addProperty(hasExpProp, exp);
    }

    /**
     * Method for validating that the input file actually exists and if it is of
     * a supported file type defined in {@link #SUPPORTED_INPUT_FILE_TYPES}.
     * @param inputFile Path and filename of the provided input file.
     * @return True if the file exists, false otherwise.
     */
    private boolean checkInputFile(final String inputFile) {
        final boolean correctFile = Files.exists(Paths.get(inputFile))
                                    && Files.exists(Paths.get(inputFile).toAbsolutePath());
        if (!correctFile) {
            System.err.println(String.join(" ", "[Error] Invalid input file:", inputFile));
        }

        boolean correctFileType;
        final int i = inputFile.lastIndexOf('.');
        if (i > 0) {
            final String checkExtension = inputFile.substring(i + 1);
            correctFileType = LKTLogbook.SUPPORTED_INPUT_FILE_TYPES
                                    .contains(checkExtension.toUpperCase(Locale.ENGLISH));
        } else {
            correctFileType = false;
        }

        if (!correctFileType) {
            System.err.println(String.join(" ", "[Error] Invalid input file type:", inputFile));
        }

        return correctFile && correctFileType;
    }

    /**
     * Method parsing all sheets of the current ODS file.
     * If parsing errors occur,  the corresponding message will be added to {@link #parserErrorMessages}.
     * Parsing will continue to collect further possible parser errors.
     * @param odsFile Input file.
     * @return ArrayList containing parsed {@link org.g_node.crawler.LKTLogbook.LKTLogbookSheet}.
     */
    private ArrayList<LKTLogbookSheet> parseSheets(final File odsFile) {
        final ArrayList<LKTLogbookSheet> allSheets = new ArrayList<>(0);
        Sheet currSheet;

        try {
            for (int i = 0; i < SpreadSheet.createFromFile(odsFile).getSheetCount(); i = i + 1) {

                currSheet = SpreadSheet.createFromFile(odsFile).getSheet(i);
                final String sheetName = currSheet.getName();

                LKTLogbookSheet currLKTLSheet = this.parseSheetVariables(currSheet);

                // TODO come up with a more robust solution
                // TODO check that line 23 contains the header and that the information start at line 24
                final String startCell = String.join("", "A", String.valueOf(LKTLogbook.SHEET_HEADER_LINE));

                if (currSheet.getCellAt(startCell).getTextValue() == null
                        || !currSheet.getCellAt(startCell).getTextValue().equals(LKTLogbook.FIRST_HEADER_ENTRY)) {
                    this.parserErrorMessages.add(
                            String.join(
                                    " ", "[Parser error] sheet", sheetName,
                                    ", HeaderEntry 'ImportID' not found at required line A",
                                    String.valueOf(LKTLogbook.SHEET_HEADER_LINE)
                            )
                    );
                } else {
                    currLKTLSheet = this.parseSheetEntries(currSheet, currLKTLSheet);
                    allSheets.add(currLKTLSheet);
                }
            }
        } catch (final IOException exp) {
            System.err.println(String.join(" ", "[Error] reading from input file:", exp.getMessage()));
            exp.printStackTrace();
        }
        return allSheets;
    }

    /**
     * Method for retrieving all sheet specific data from the current ODS sheet.
     * @param currSheet The current sheet from the ODS file.
     * @return The current {@link org.g_node.crawler.LKTLogbook.LKTLogbookSheet} containing all parsed values.
     */
    private LKTLogbookSheet parseSheetVariables(final Sheet currSheet) {
        final LKTLogbookSheet currLKTLSheet  = new LKTLogbookSheet();
        final String sheetName = currSheet.getName();
        ArrayList<String> parseSheetMessage;
        String checkDB;
        String checkDW;

        currLKTLSheet.setAnimalID(currSheet.getCellAt("C2").getTextValue());
        currLKTLSheet.setAnimalSex(currSheet.getCellAt("C3").getTextValue());
        checkDB = currLKTLSheet.setDateOfBirth(currSheet.getCellAt("C4").getTextValue());
        checkDW = currLKTLSheet.setDateOfWithdrawal(currSheet.getCellAt("C5").getTextValue());
        currLKTLSheet.setPermitNumber(currSheet.getCellAt("C6").getTextValue());
        currLKTLSheet.setSpecies(currSheet.getCellAt("C7").getTextValue());
        currLKTLSheet.setScientificName(currSheet.getCellAt("C8").getTextValue());

        // TODO come up with a better way to deal with date errors
        parseSheetMessage = currLKTLSheet.isValidSheet();
        if (!parseSheetMessage.isEmpty() || !checkDB.isEmpty() || !checkDW.isEmpty()) {
            if (!parseSheetMessage.isEmpty()) {
                parseSheetMessage.forEach(
                        m -> this.parserErrorMessages.add(
                                String.join(" ", "[Parser error] sheet", sheetName, ",", m)
                        )
                );
            }
            // check valid date of birth
            if (!checkDB.isEmpty()) {
                this.parserErrorMessages.add(String.join(" ", "[Parser error] sheet", sheetName, ",", checkDB));
            }
            // check valid date of withdrawal
            if (!checkDW.isEmpty()) {
                this.parserErrorMessages.add(String.join(" ", "[Parser error] sheet", sheetName, ",", checkDW));
            }
        }
        return currLKTLSheet;
    }

    /**
     * Method for parsing the experiment entries of an animal sheet.
     * If parsing errors occur, the corresponding message will be added to {@link #parserErrorMessages}.
     * Parsing will continue to collect further possible parser errors.
     * @param currSheet The current sheet of the parsed ODS file.
     * @param currLKTLSheet The current {@link org.g_node.crawler.LKTLogbook.LKTLogbookSheet}.
     * @return The current {@link org.g_node.crawler.LKTLogbook.LKTLogbookSheet} containing the parsed
     *  experiment entries.
     */
    private LKTLogbookSheet parseSheetEntries(final Sheet currSheet, final LKTLogbookSheet currLKTLSheet) {
        String parseEntryMessage;

        for (int i = LKTLogbook.SHEET_HEADER_LINE + 1; i < currSheet.getRowCount(); i = i + 1) {

            final LKTLogbookEntry currEntry = this.parseSheetEntriesVariables(currSheet, i);

            final boolean checkEmptyReqField = !currEntry.getProject().isEmpty()
                    || !currEntry.getExperiment().isEmpty()
                    || currEntry.getExperimentDate() != null
                    || !currEntry.getLastName().isEmpty();

            parseEntryMessage = currEntry.isValidEntry();
            if (!currEntry.getIsEmptyLine() && parseEntryMessage.isEmpty()) {
                currLKTLSheet.addEntry(currEntry);
            } else if (!currEntry.getIsEmptyLine() && checkEmptyReqField) {
                this.parserErrorMessages.add(
                        String.join(
                                " ", "[Parser error] sheet", currSheet.getName(), "row",
                                String.valueOf(i), ", missing value:", parseEntryMessage
                        )
                );
            }
        }
        return currLKTLSheet;
    }

    /**
     * Method parsing all variables of a single entry of the current
     * ODS sheet.
     * @param currSheet The current ODS sheet.
     * @param currLine Number of the current line in the current ODS sheet.
     * @return The {@link org.g_node.crawler.LKTLogbook.LKTLogbookEntry} containing the parsed values from
     *  the current single entry.
     */
    private LKTLogbookEntry parseSheetEntriesVariables(final Sheet currSheet, final int currLine) {
        String[] handleName;
        String checkExperimentDate;

        final LKTLogbookEntry currEntry = new LKTLogbookEntry();

        currEntry.setExistingImportID(currSheet.getCellAt(
                String.join("", "A", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setProject(currSheet.getCellAt(
                String.join("", "K", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setExperiment(currSheet.getCellAt(
                String.join("", "L", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setParadigm(currSheet.getCellAt(
                String.join("", "C", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setParadigmSpecifics(currSheet.getCellAt(
                String.join("", "D", String.valueOf(currLine))).getTextValue()
        );

        // TODO check if the experimentDate parser error and the empty line messages all still work!
        checkExperimentDate = currEntry.setExperimentDate(currSheet.getCellAt(
                String.join("", "B", String.valueOf(currLine))).getTextValue()
        );
        if (!checkExperimentDate.isEmpty()) {
            this.parserErrorMessages.add(
                    String.join(
                            " ", "[Parser error] sheet", currSheet.getName(), "row",
                            String.valueOf(currLine), checkExperimentDate
                    )
            );
        }

        // TODO solve this better, add middle name
        handleName = currSheet.getCellAt(
                String.join("", "M", String.valueOf(currLine))
        ).getTextValue().trim().split("\\s+");

        currEntry.setFirstName(handleName[0]);
        currEntry.setLastName(handleName[handleName.length - 1]);
        currEntry.setCommentExperiment(currSheet.getCellAt(
                String.join("", "H", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setCommentAnimal(currSheet.getCellAt(
                String.join("", "I", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setFeed(currSheet.getCellAt(
                String.join("", "J", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setIsOnDiet(currSheet.getCellAt(
                String.join("", "E", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setIsInitialWeight(currSheet.getCellAt(
                String.join("", "F", String.valueOf(currLine))).getTextValue()
        );
        currEntry.setWeight(currSheet.getCellAt(
                String.join("", "G", String.valueOf(currLine))).getTextValue()
        );

        return currEntry;
    }

}
