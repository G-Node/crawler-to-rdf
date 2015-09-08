// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler.LKTLogbook;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.apache.commons.cli.*;
import org.g_node.crawler.CrawlerTemplate;
import org.jopendocument.dom.spreadsheet.*;

/**
 * Parser for the main ODS metadata file used in the lab of Kay Thurley
 */
public class LKTLogbook implements CrawlerTemplate {

    private final String nameRegistry = "lkt";
    private final String nameVerbose = "LMU Kay Thurley main metadata crawler";
    private final ArrayList<String> parsableFileTypes = new ArrayList<>(Collections.singletonList("ODS"));

    public boolean hasParserError = false;
    public ArrayList<String> parserErrorMessages = new ArrayList<>();

    public String getNameRegistry() {
        return nameRegistry;
    }

    public String getNameVerbose() {
        return nameVerbose;
    }

    public ArrayList<String> getParsableFileTypes() {
        return parsableFileTypes;
    }

    public Options getCLIOptions(final Set<String> regCrawlers) {
        Options options = new Options();

        Option opHelp = new Option("h", "help", false, "Print this message");

        Option opCr = Option.builder("c")
                .longOpt("crawler")
                .desc("Shorthand of the required data crawler. \nAvailable crawlers: "+ regCrawlers.toString())
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        Option opIn = Option.builder("i")
                .longOpt("in-file")
                .desc("Input file that's supposed to be parsed")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        Option opOut = Option.builder("o")
                .longOpt("out-file")
                .desc("Optional: Path and name of the output file. Files with the same name will be overwritten. Default file name uses format 'YYYYMMDDHHmm'")
                .hasArg()
                .valueSeparator()
                .build();

        Option opFormat = Option.builder("f")
                .longOpt("out-format")
                .desc("Optional: format of the RDF file that will be written. Default setting is the Turtle (ttl) format")
                .hasArg()
                .valueSeparator()
                .build();

        Option opOnt = Option.builder("n")
                .longOpt("ont-file")
                .desc("Optional: one or more onotology files can be provided to check if the created RDF files satisfy a required ontology")
                .hasArg()
                .valueSeparator()
                .build();

        options.addOption(opHelp);
        options.addOption(opCr);
        options.addOption(opIn);
        options.addOption(opOut);
        options.addOption(opFormat);
        options.addOption(opOnt);

        return options;
    }

    public boolean checkCLIOptions(final CommandLine cmd) {
        // TODO find better name
        boolean correctCLI = true;
        if(cmd.hasOption("i")) {
            System.out.println("Input file: "+ cmd.getOptionValue("i"));
            if(!Files.exists(Paths.get(cmd.getOptionValue("i"))) &&
                    (!Files.exists(Paths.get(cmd.getOptionValue("i")).toAbsolutePath())))  {
                System.out.println("\nProvided input is not a valid file");
                correctCLI = false;
            }
        }

        if(cmd.hasOption("o")) {
            System.out.println("Output file: "+ cmd.getOptionValue("o"));
        }

        if(cmd.hasOption("f")) {
            System.out.println("Use output format: "+ cmd.getOptionValue("f"));
        }

        if(cmd.hasOption("n")) {
            System.out.println("Ontology file: "+ cmd.getOptionValue("n"));
        }
        return correctCLI;
    }

    public void parseFile(String inputFile) {

        // TODO check if a robuster solution exists. Also check with Kay, if multiple backup files e.g. with a timestamp should exist.
        // TODO will fail for sure, if the file contains more than one period; should be addressed as well.
        // Create file backup
        String backupFile = inputFile.split("\\.")[0]+"_backup.ods";
        try {
            Files.copy(Paths.get(inputFile), Paths.get(backupFile), StandardCopyOption.REPLACE_EXISTING);


            File ODSFile = new File(inputFile);
            try {

                // TODO remove later
                System.out.println("File has # sheets: " + SpreadSheet.createFromFile(ODSFile).getSheetCount());

                if(!(SpreadSheet.createFromFile(ODSFile).getSheetCount() > 0)) {
                    hasParserError = true;
                    parserErrorMessages.add("Provided labbook " + inputFile + " does not contain valid data sheets");
                } else {

                    ArrayList<LKTLogbookSheet> allSheets = new ArrayList<>();
                    Sheet sheet;
                    LKTLogbookSheet currLKTLSheet;
                    ArrayList<String> parseSheetMessage;
                    String parseEntryMessage;

                    String checkDB;
                    String checkDW;

                    for (int i = 0; i < SpreadSheet.createFromFile(ODSFile).getSheetCount(); i++) {

                        sheet = SpreadSheet.createFromFile(ODSFile).getSheet(i);
                        final String sheetName = sheet.getName();

                        // TODO remove later
                        System.out.println("Sheet name:" + sheet.getName() + ", RowCount: " + sheet.getRowCount() + ", ColCount: " + sheet.getColumnCount());

                        currLKTLSheet = new LKTLogbookSheet();

                        currLKTLSheet.setAnimalID(sheet.getCellAt("C2").getTextValue());
                        currLKTLSheet.setAnimalSex(sheet.getCellAt("C3").getTextValue());
                        checkDB = currLKTLSheet.setDateOfBirth(sheet.getCellAt("C4").getTextValue());
                        checkDW = currLKTLSheet.setDateOfWithdrawal(sheet.getCellAt("C5").getTextValue());
                        currLKTLSheet.setPermitNumber(sheet.getCellAt("C6").getTextValue());
                        currLKTLSheet.setSpecies(sheet.getCellAt("C7").getTextValue());
                        currLKTLSheet.setScientificName(sheet.getCellAt("C8").getTextValue());

                        // TODO come up with a better way to deal with date errors
                        parseSheetMessage = currLKTLSheet.isValidSheet();
                        if (!parseSheetMessage.isEmpty() || !checkDB.isEmpty() || !checkDW.isEmpty()) {
                            hasParserError = true;
                            if (!parseSheetMessage.isEmpty()) {
                                parseSheetMessage.forEach(m -> parserErrorMessages.add("Parser error sheet " + sheetName + ", " + m));
                            }
                            // check valid date of brith
                            if (!checkDB.isEmpty()) {
                                parserErrorMessages.add("Parser error sheet " + sheetName + ", " + checkDB);
                            }
                            // check valid date of withdrawal
                            if (!checkDW.isEmpty()) {
                                parserErrorMessages.add("Parser error sheet " + sheetName + ", " + checkDW);
                            }
                        }

                        // TODO come up with a more robust solution
                        // TODO check that line 23 contains the header and that the information start at line 24
                        String[] handleName;
                        String checkExperimentDate;

                        if (sheet.getCellAt("A23").getTextValue() == null || !sheet.getCellAt("A23").getTextValue().equals("ImportID")) {
                            parserErrorMessages.add("Parser error sheet " + sheetName + " HeaderEntry ImportID does not start at required line 23");
                        } else {

                            for (int j = 24; j < sheet.getRowCount(); j++) {

                                LKTLogbookEntry currEntry = new LKTLogbookEntry();

                                currEntry.setExistingImportID(sheet.getCellAt("A" + j).getTextValue());
                                currEntry.setProject(sheet.getCellAt("K" + j).getTextValue());
                                currEntry.setExperiment(sheet.getCellAt("L" + j).getTextValue());
                                currEntry.setParadigm(sheet.getCellAt("C" + j).getTextValue());
                                currEntry.setParadigmSpecifics(sheet.getCellAt("D" + j).getTextValue());

                                checkExperimentDate = currEntry.setExperimentDate(sheet.getCellAt("B" + j).getTextValue());

                                // TODO solve this better, add middle name
                                handleName = sheet.getCellAt("M" + j).getTextValue().trim().split("\\s+");

                                currEntry.setFirstName(handleName[0]);
                                currEntry.setLastName(handleName[handleName.length - 1]);
                                currEntry.setCommentExperiment(sheet.getCellAt("H" + j).getTextValue());
                                currEntry.setCommentAnimal(sheet.getCellAt("I" + j).getTextValue());
                                currEntry.setFeed(sheet.getCellAt("J" + j).getTextValue());
                                currEntry.setIsOnDiet(sheet.getCellAt("E" + j).getTextValue());
                                currEntry.setIsInitialWeight(sheet.getCellAt("F" + j).getTextValue());
                                currEntry.setWeight(sheet.getCellAt("G" + j).getTextValue());

                                parseEntryMessage = currEntry.isValidEntry();
                                if (!currEntry.isEmptyLine() && parseEntryMessage.isEmpty()) {
                                    currLKTLSheet.addEntry(currEntry);
                                } else if (!currEntry.isEmptyLine() &&
                                        (!currEntry.getProject().isEmpty() || !currEntry.getExperiment().isEmpty() ||
                                                currEntry.getExperimentDate() != null || !currEntry.getLastName().isEmpty())) {
                                    hasParserError = true;
                                    // decide whether an experiment date parser error has happened or if a required value is missing at this line
                                    String msg = !checkExperimentDate.isEmpty() ? " " + checkExperimentDate : ", missing value:" + parseEntryMessage;
                                    // Parser errors should be collected and written to a logfile. If a sheet contains multiple errors
                                    // the user should get them all at once and not with every run just the latest one.
                                    parserErrorMessages.add("Parser error sheet " + sheet.getName() + " row " + j + msg);
                                }
                            }

                            allSheets.add(currLKTLSheet);
                        }
                    }

                    allSheets.forEach(s -> System.out.println("CurrSheet: " + s.getAnimalID() + " number of entries: " + s.getEntries().size()));

                    if (!hasParserError) {
                        // TODO remove later
                        allSheets.stream().forEach(s ->
                        {
                            System.out.println("AnimalID: " + s.getAnimalID() + ", AnimalSex: " + s.getAnimalSex());
                            s.getEntries().stream().forEach(e -> System.out.println("CurrRow: " + e.getProject() + " " + e.getExperiment() + " " + e.getExperimentDate()));
                        });
                    } else {
                        // TODO write to logfile
                        parserErrorMessages.forEach(System.err::println);
                    }
                }

            } catch(Exception exp) {
                System.err.println("ODS parser error: "+ exp.getMessage());
                exp.printStackTrace();
            }

        } catch(IOException exc) {
            System.err.println("Error creating backup file: "+ exc.getMessage());
            exc.printStackTrace();
        }

    }

}
