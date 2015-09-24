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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
     * Namespace used to identify RDF resources and properties specific for the current usecase.
     */
    private static final String RDF_NAMESPACE = "http://g-node.org/lkt/";
    /**
     * Namespace prefix.
     */
    private static final String RDF_NS_ABR = "lkt";

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
     * Method for parsing the contents of a provided ODS input file.
     * This method will create a backup file of the original ODS file.
     * @param inputFile ODS file specific to Kay Thurleys usecase.
     * @param outputFile RDF output file.
     * @param outputFormat Format of the RDF output file.
     */
    public final void parseFile(final String inputFile, final String outputFile, final String outputFormat) {

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
                    this.parserErrorMessages.add(
                            "\nThere are parser errors present. Please resolve them and run the program again.");
                    this.parserErrorMessages.forEach(System.err::println);
                }
            }
        } catch (final IOException exp) {
            System.err.println(String.join(" ", "[Error] reading from input file:", exp.getMessage()));
            exp.printStackTrace();
        }
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

    /**
     * Creates an RDF model from the parsed ODS sheet data and writes
     * the model to the designated output file.
     * @param allSheets Data from the parsed ODS sheets.
     * @param outputFile Name and path of the designated output file.
     * @param outputFormat RDF output format.
     */
    private void createRDFModel(final ArrayList<LKTLogbookSheet> allSheets,
                                final String outputFile, final String outputFormat) {
        allSheets.stream().forEach(
                this::addAnimal
        );
        RDFService.writeModelToFile(outputFile, this.model, outputFormat);
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

        // TODO fix namespace issue
        this.model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.model.setNsPrefix(LKTLogbook.RDF_NS_ABR, "http://g-node.org/lkt#");

        final Property permitNr = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasNumber"));
        final Resource permit = this.model.createResource(
                String.join("", LKTLogbook.RDF_NAMESPACE, "Permit#", UUID.randomUUID().toString())
        )
                .addLiteral(permitNr, currSheet.getPermitNumber());

        // TODO handle dates properly
        final Property animalIDProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasAnimalID"));
        final Property sexProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasSex"));
        final Property birthDate = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasBirthDate"));
        final Property withdrawalDate = this.model.createProperty(
                String.join("", LKTLogbook.RDF_NS_ABR, ":hasWithdrawalDate")
        );
        final Property speciesName = this.model.createProperty(
                String.join("", LKTLogbook.RDF_NS_ABR, ":hasSpeciesName")
        );
        final Property scientificName = this.model.createProperty(
                String.join("", LKTLogbook.RDF_NS_ABR, ":hasScientificName")
        );
        final Property permitNumber = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasPermit"));

        final Resource animal = this.model.createResource(
                String.join("", LKTLogbook.RDF_NAMESPACE, "Animal#", animalUUID)
        )
                .addLiteral(animalIDProp, animalID)
                .addLiteral(sexProp, currSheet.getAnimalSex())
                .addLiteral(birthDate, currSheet.getDateOfBirth().toString())
                .addLiteral(withdrawalDate, currSheet.getDateOfWithdrawal().toString())
                .addLiteral(speciesName, currSheet.getSpecies())
                .addLiteral(scientificName, currSheet.getScientificName())
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

            final Property projName = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasName"));
            this.model.createResource(
                    String.join(
                            "", LKTLogbook.RDF_NAMESPACE,
                            "Project#", this.projectList.get(project))
            )
                    .addLiteral(projName, project);
        }
        final Resource projectRes = this.model.getResource(
                String.join("", LKTLogbook.RDF_NAMESPACE, "Project#", this.projectList.get(project))
        );

        // TODO replace this with the FOAF namespace!
        // TODO add middle name
        // add experimenter only once to the rdf model
        final String experimenter = String.join(" ", currEntry.getLastName(), currEntry.getFirstName());
        if (!this.experimenterList.containsKey(experimenter)) {
            this.experimenterList.put(experimenter, UUID.randomUUID().toString());

            final Property firstName = this.model.createProperty(
                    String.join("", LKTLogbook.RDF_NS_ABR, ":hasFirstName")
            );
            final Property lastName = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasLastName"));

            // TODO add middle name
            this.model.createResource(
                    String.join(
                            "", LKTLogbook.RDF_NAMESPACE,
                            "Experimenter#", this.experimenterList.get(experimenter))
            )
                    .addLiteral(firstName, currEntry.getFirstName())
                    .addLiteral(lastName, currEntry.getLastName());
        }

        final Resource experimenterRes = this.model.getResource(
                String.join("", LKTLogbook.RDF_NAMESPACE, "Experimenter#", this.experimenterList.get(experimenter))
        );

        // Create current experiment resource
        final Resource exp = this.addExperimentEntry(currEntry);
        // Link current experiment to experimenter
        final Property expmtr = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasExperimenter"));
        // Link current experiment to animal log
        final Property animalProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasAnimal"));

        exp.addProperty(expmtr, experimenterRes)
                .addProperty(animalProp, animal);

        final Property hasExpProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasExperiment"));
        projectRes.addProperty(hasExpProp, exp);

        // Create current animalLog resource
        final Resource animalLogEntry = this.addAnimalLogEntry(currEntry);
        // Link animal log entry to experimenter
        animalLogEntry.addProperty(expmtr, experimenterRes);
        // Add animal log entry to the current animal node
        final Property animalLogEntryProp = this.model.createProperty(
                String.join("", LKTLogbook.RDF_NS_ABR, ":hasAnimalLogEntry")
        );
        animal.addProperty(animalLogEntryProp, animalLogEntry);

    }

    /**
     * Add Experiment node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created experiment node.
     */
    private Resource addExperimentEntry(final LKTLogbookEntry currEntry) {
        final Property d = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":startedAt"));
        final Property label = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasLabel"));
        final Property paradigm = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasParadigm"));
        final Property paradigmSpec = this.model.createProperty(
                String.join("", LKTLogbook.RDF_NS_ABR, ":hasParadigmSpecifics")
        );

        final Property expCom = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasComment"));
        return this.model.createResource(
                String.join("", LKTLogbook.RDF_NAMESPACE, "Experiment#", currEntry.getImportID())
        )
                .addLiteral(d, currEntry.getExperimentDate().toString())
                .addLiteral(label, currEntry.getExperiment())
                .addLiteral(paradigm, currEntry.getParadigm())
                .addLiteral(paradigmSpec, currEntry.getParadigmSpecifics())
                .addLiteral(expCom, currEntry.getCommentExperiment());
    }

    /**
     * Add AnimalLogEntry node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created AnimalLogEntryNode.
     */
    private Resource addAnimalLogEntry(final LKTLogbookEntry currEntry) {
        final Property startedProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":startedAt"));
        final Property animalCommentProp = this.model.createProperty(
                String.join("", LKTLogbook.RDF_NS_ABR, ":hasComment")
        );
        final Property dietProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasDiet"));
        // TODO include blank node with g as unit
        final Property weightProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasWeight"));
        final Property initialWeightProp = this.model.createProperty(
                String.join("", LKTLogbook.RDF_NS_ABR, ":hasInitialWeightDate")
        );
        final Property feedProp = this.model.createProperty(String.join("", LKTLogbook.RDF_NS_ABR, ":hasFeed"));

        // TODO check if its actually correct to use the same UUID for experiment AND animalLogEntry
        return this.model.createResource(
                String.join("", LKTLogbook.RDF_NAMESPACE, "AnimalLogEntry#", currEntry.getImportID()))
                .addLiteral(startedProp, currEntry.getExperimentDate().toString())
                .addLiteral(animalCommentProp, currEntry.getCommentAnimal())
                .addLiteral(dietProp, currEntry.getIsOnDiet())
                .addLiteral(weightProp, currEntry.getWeight())
                .addLiteral(initialWeightProp, currEntry.getIsInitialWeight())
                .addLiteral(feedProp, currEntry.getFeed());
    }
}
