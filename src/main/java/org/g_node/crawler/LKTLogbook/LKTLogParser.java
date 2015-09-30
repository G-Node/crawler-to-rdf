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
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.g_node.srv.RDFService;
import org.g_node.srv.RDFUtils;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Parser for the main ODS metadata file used in the lab of Kay Thurley.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LKTLogParser {
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
    private static final String RDF_NS =  "http://g-node.org/lkt/";
    /**
     * Namespace prefix.
     */
    private static final String RDF_NS_ABR = "lkt";
    /**
     * Namespace used to identify FOAF RDF resources.
     */
    private static final String RDF_NS_FOAF = "http://xmlns.com/foaf/0.1/";
    /**
     * FOAF Namespace prefix.
     */
    private static final String RDF_NS_FOAF_ABR = "foaf";
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

            System.out.println(
                    String.join(
                            "", "[Info] File has # sheets: ",
                            String.valueOf(SpreadSheet.createFromFile(odsFile).getSheetCount()))
            );

            if (!(SpreadSheet.createFromFile(odsFile).getSheetCount() > 0)) {
                this.parserErrorMessages.add(
                        String.join(
                                "", "[Parser error] File ", inputFile,
                                " does not contain valid data sheets."
                        )
                );
            } else {
                final ArrayList<LKTLogParserSheet> allSheets = this.parseSheets(odsFile);
                allSheets.forEach(
                        s -> System.out.println(
                                String.join(
                                        "", "[Info] CurrSheet: ", s.getAnimalID(),
                                        ", number of entries: ",
                                        String.valueOf(s.getEntries().size())
                                )
                        )
                );

                if (this.parserErrorMessages.size() == 0) {

                    this.createRDFModel(allSheets, outputFile, outputFormat);

                } else {
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
     * @return ArrayList containing parsed {@link LKTLogParserSheet}.
     */
    private ArrayList<LKTLogParserSheet> parseSheets(final File odsFile) {
        final ArrayList<LKTLogParserSheet> allSheets = new ArrayList<>(0);
        Sheet currSheet;

        try {
            for (int i = 0; i < SpreadSheet.createFromFile(odsFile).getSheetCount(); i = i + 1) {

                currSheet = SpreadSheet.createFromFile(odsFile).getSheet(i);
                final String sheetName = currSheet.getName();

                LKTLogParserSheet currLKTLSheet = this.parseSheetVariables(currSheet);

                // Solution is not very robust, but coming up with a more robust solution would be wasted effort.
                final String checkHeaderCell = currSheet.getCellAt(
                                    String.join("", "A", String.valueOf(LKTLogParser.SHEET_HEADER_LINE))
                                ).getTextValue();

                if (checkHeaderCell == null || !checkHeaderCell.equals(LKTLogParser.FIRST_HEADER_ENTRY)) {
                    this.parserErrorMessages.add(
                            String.join(
                                    "", "[Parser error] sheet ", sheetName,
                                    ", HeaderEntry 'ImportID' not found at required line A.",
                                    String.valueOf(LKTLogParser.SHEET_HEADER_LINE)
                            )
                    );
                } else {
                    currLKTLSheet = this.parseSheetEntries(currSheet, currLKTLSheet);
                    allSheets.add(currLKTLSheet);
                }
            }
        } catch (final IOException exp) {
            System.err.println(String.join("", "[Error] reading from input file: ", exp.getMessage()));
            exp.printStackTrace();
        }
        return allSheets;
    }

    /**
     * Method for retrieving all sheet specific data from the current ODS sheet.
     * @param currSheet The current sheet from the ODS file.
     * @return The current {@link LKTLogParserSheet} containing all parsed values.
     */
    private LKTLogParserSheet parseSheetVariables(final Sheet currSheet) {
        final LKTLogParserSheet currLKTLSheet  = new LKTLogParserSheet();
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
                                String.join("", "[Parser error] sheet ", sheetName, ", ", m)
                        )
                );
            }
            // Check valid date of birth
            if (!checkDB.isEmpty()) {
                this.parserErrorMessages.add(String.join("", "[Parser error] sheet ", sheetName, ", ", checkDB));
            }
            // Check valid date of withdrawal
            if (!checkDW.isEmpty()) {
                this.parserErrorMessages.add(String.join("", "[Parser error] sheet ", sheetName, ", ", checkDW));
            }
        }
        return currLKTLSheet;
    }

    /**
     * Method for parsing the experiment entries of an animal sheet.
     * If parsing errors occur, the corresponding message will be added to {@link #parserErrorMessages}.
     * Parsing will continue to collect further possible parser errors.
     * @param currFileSheet The current sheet of the parsed ODS file.
     * @param currLKTSheet The current {@link LKTLogParserSheet}.
     * @return The current {@link LKTLogParserSheet} containing the parsed
     *  experiment entries.
     */
    private LKTLogParserSheet parseSheetEntries(final Sheet currFileSheet, final LKTLogParserSheet currLKTSheet) {
        String parseEntryMessage;

        for (int i = LKTLogParser.SHEET_HEADER_LINE + 1; i < currFileSheet.getRowCount(); i = i + 1) {

            final LKTLogParserEntry currEntry = this.parseSheetEntriesVariables(currFileSheet, String.valueOf(i));

            final boolean checkEmptyReqField = !currEntry.getProject().isEmpty()
                    || !currEntry.getExperiment().isEmpty()
                    || currEntry.getExperimentDate() != null
                    || !currEntry.getExperimenterName().isEmpty();

            parseEntryMessage = currEntry.isValidEntry();
            if (!currEntry.getIsEmptyLine() && parseEntryMessage.isEmpty()) {
                currLKTSheet.addEntry(currEntry);
            } else if (!currEntry.getIsEmptyLine() && checkEmptyReqField) {
                this.parserErrorMessages.add(
                        String.join(
                                "", "[Parser error] sheet ", currFileSheet.getName(), " row ",
                                String.valueOf(i), ", missing value: ", parseEntryMessage
                        )
                );
            }
        }
        return currLKTSheet;
    }

    /**
     * Method parsing all variables of a single entry of the current
     * ODS sheet.
     * @param currSheet The current ODS sheet.
     * @param currLine Number of the current line in the current ODS sheet.
     * @return The {@link LKTLogParserEntry} containing the parsed values from
     *  the current single entry.
     */
    private LKTLogParserEntry parseSheetEntriesVariables(final Sheet currSheet, final String currLine) {
        String checkExperimentDate;

        final LKTLogParserEntry currEntry = new LKTLogParserEntry();

        currEntry.setProject(currSheet.getCellAt(String.join("", "K", currLine)).getTextValue());
        currEntry.setExperiment(currSheet.getCellAt(String.join("", "L", currLine)).getTextValue());
        currEntry.setParadigm(currSheet.getCellAt(String.join("", "C", currLine)).getTextValue());
        currEntry.setParadigmSpecifics(currSheet.getCellAt(String.join("", "D", currLine)).getTextValue());

        // TODO check if the experimentDate parser error and the empty line messages all still work!
        checkExperimentDate = currEntry.setExperimentDate(currSheet.getCellAt(
                String.join("", "B", currLine)).getTextValue()
        );
        if (!checkExperimentDate.isEmpty()) {
            this.parserErrorMessages.add(
                    String.join(
                            "", "[Parser error] sheet ", currSheet.getName(), " row ",
                            currLine, "\n\t", checkExperimentDate
                    )
            );
        }

        currEntry.setExperimenterName(currSheet.getCellAt(String.join("", "M", currLine)).getTextValue());
        currEntry.setCommentExperiment(currSheet.getCellAt(String.join("", "H", currLine)).getTextValue());
        currEntry.setCommentAnimal(currSheet.getCellAt(String.join("", "I", currLine)).getTextValue());
        currEntry.setFeed(currSheet.getCellAt(String.join("", "J", currLine)).getTextValue());
        currEntry.setIsOnDiet(currSheet.getCellAt(String.join("", "E", currLine)).getTextValue());
        currEntry.setIsInitialWeight(currSheet.getCellAt(String.join("", "F", currLine)).getTextValue());

        final String msg = currEntry.setWeight(currSheet.getCellAt(String.join("", "G", currLine)).getTextValue());
        if (!"".equals(msg)) {
            this.parserErrorMessages.add(
                    String.join(
                            "", "[Parser error] sheet ", currSheet.getName(), " row ",
                            currLine, " ", msg
                    )
            );
        }

        return currEntry;
    }

    /**
     * Creates an RDF model from the parsed ODS sheet data and writes
     * the model to the designated output file.
     * @param allSheets Data from the parsed ODS sheets.
     * @param outputFile Name and path of the designated output file.
     * @param outputFormat RDF output format.
     */
    private void createRDFModel(final ArrayList<LKTLogParserSheet> allSheets,
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
    private void addAnimal(final LKTLogParserSheet currSheet) {
        final String animalID = currSheet.getAnimalID();
        if (!this.animalList.containsKey(animalID)) {
            this.animalList.put(animalID, UUID.randomUUID().toString());
        }

        // TODO Handle dates properly
        // TODO Check if namespaces should be handled somewhere else
        this.model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.model.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");
        this.model.setNsPrefix(LKTLogParser.RDF_NS_FOAF_ABR, LKTLogParser.RDF_NS_FOAF);
        this.model.setNsPrefix(LKTLogParser.RDF_NS_ABR, LKTLogParser.RDF_NS);

        final Resource permit = this.localRes(UUID.randomUUID().toString())
                .addProperty(RDF.type, this.localRes("Permit"))
                .addLiteral(this.localProp("hasNumber"), currSheet.getPermitNumber());

        final Resource animal = this.localRes(this.animalList.get(animalID))
                .addProperty(RDF.type, this.localRes("Animal"))
                .addLiteral(this.localProp("hasAnimalID"), animalID)
                .addLiteral(this.localProp("hasSex"), currSheet.getAnimalSex())
                .addLiteral(this.localProp("hasBirthDate"), currSheet.getDateOfBirth().toString())
                .addLiteral(this.localProp("hasWithdrawalDate"), currSheet.getDateOfWithdrawal().toString())
                .addProperty(this.localProp("hasPermit"), permit);

        RDFUtils.addNonEmptyLiteral(animal, this.localProp("hasSpeciesName"), currSheet.getSpecies());
        RDFUtils.addNonEmptyLiteral(animal, this.localProp("hasScientificName"), currSheet.getScientificName());

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
    private void addEntry(final LKTLogParserEntry currEntry, final Resource animal) {

        final String project = currEntry.getProject();
        // add project only once to the rdf model
        if (!this.projectList.containsKey(project)) {

            this.projectList.put(project, UUID.randomUUID().toString());
            this.localRes(this.projectList.get(project))
                    .addProperty(RDF.type, this.localRes("Project"))
                    .addLiteral(RDFS.label, project);
        }
        // Fetch project resource
        final Resource projectRes = this.model.getResource(
                String.join("", LKTLogParser.RDF_NS, this.projectList.get(project))
        );

        // Add experimenter only once to the RDF model
        final String experimenter = currEntry.getExperimenterName();
        if (!this.experimenterList.containsKey(experimenter)) {

            this.experimenterList.put(experimenter, UUID.randomUUID().toString());

            final Property name = this.model.createProperty(String.join("", LKTLogParser.RDF_NS_FOAF, "name"));
            final Resource personRes = this.model.createResource(String.join("", LKTLogParser.RDF_NS_FOAF, "Person"));

            this.localRes(this.experimenterList.get(experimenter))
                    .addProperty(RDF.type, this.localRes("Experimenter"))
                    .addLiteral(name, currEntry.getExperimenterName())
                    // TODO Check if this is actually correct or if the subclass
                    // TODO is supposed to be found only in the definition.
                    .addProperty(RDFS.subClassOf, personRes);
        }

        // Fetch experimenter resource
        final Resource experimenterRes = this.model.getResource(
                String.join("", LKTLogParser.RDF_NS, this.experimenterList.get(experimenter))
        );

        // Create current experiment resource
        final Resource exp = this.addExperimentEntry(currEntry);
        // Link current experiment to experimenter and animal log
        exp.addProperty(this.localProp("hasExperimenter"), experimenterRes)
                .addProperty(this.localProp("hasAnimal"), animal);

        projectRes.addProperty(this.localProp("hasExperiment"), exp);

        // Create current animalLog resource
        final Resource animalLogEntry = this.addAnimalLogEntry(currEntry);
        // Link animal log entry to experimenter
        animalLogEntry.addProperty(this.localProp("hasExperimenter"), experimenterRes);
        // Add animal log entry to the current animal node
        animal.addProperty(this.localProp("hasAnimalLogEntry"), animalLogEntry);
    }

    /**
     * Add Experiment node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created experiment node.
     */
    private Resource addExperimentEntry(final LKTLogParserEntry currEntry) {

        final Resource experiment = this.localRes(UUID.randomUUID().toString())
                .addProperty(RDF.type, this.localRes("Experiment"))
                .addLiteral(this.localProp("startedAt"), currEntry.getExperimentDate().toString())
                .addLiteral(RDFS.label, currEntry.getExperiment());

        RDFUtils.addNonEmptyLiteral(experiment, this.localProp("hasParadigm"), currEntry.getParadigm());
        RDFUtils.addNonEmptyLiteral(experiment, this.localProp("hasParadigmSpecifics"),
                currEntry.getParadigmSpecifics());
        RDFUtils.addNonEmptyLiteral(experiment, RDFS.comment, currEntry.getCommentExperiment());

        return experiment;
    }

    /**
     * Add AnimalLogEntry node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created AnimalLogEntryNode.
     */
    private Resource addAnimalLogEntry(final LKTLogParserEntry currEntry) {
        final Resource res = this.localRes(UUID.randomUUID().toString())
                .addProperty(RDF.type, this.localRes("AnimalLogEntry"))
                .addLiteral(this.localProp("startedAt"), currEntry.getExperimentDate().toString())
                .addLiteral(this.localProp("hasDiet"), currEntry.getIsOnDiet())
                .addLiteral(this.localProp("hasInitialWeightDate"), currEntry.getIsInitialWeight());

        if (currEntry.getWeight() != null) {
            final Resource weight = this.model.createResource()
                    .addLiteral(this.localProp("hasValue"), currEntry.getWeight())
                    .addLiteral(this.localProp("hasUnit"), "g");

            res.addProperty(this.localProp("hasWeight"), weight);
        }

        RDFUtils.addNonEmptyLiteral(res, RDFS.comment, currEntry.getCommentAnimal());
        RDFUtils.addNonEmptyLiteral(res, this.localProp("hasFeed"), currEntry.getFeed());

        return res;
    }

    /**
     * Convenience method for creating an RDF resource with the
     * Namespace used by this crawler.
     * @param resName Contains the name of the resource.
     * @return The created RDF Resource.
     */
    private Resource localRes(final String resName) {
        return this.model.createResource(
                String.join("", LKTLogParser.RDF_NS, resName)
        );
    }

    /**
     * Convenience method for creating an RDF property with the
     * Namespace used by this crawler.
     * @param propName Contains the name of the property.
     * @return The created RDF Property.
     */
    private Property localProp(final String propName) {
        return this.model.createProperty(
                String.join("", LKTLogParser.RDF_NS, propName)
        );
    }
}
