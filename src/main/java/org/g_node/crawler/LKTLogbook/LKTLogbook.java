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
 * Parser for the main ODS metadata file used in the lab of Kay Thurley.
 */
public class LKTLogbook implements CrawlerTemplate {

    private final String nameRegistry = "lkt";
    private final String nameVerbose = "LMU Kay Thurley main metadata crawler";
    private final ArrayList<String> parsableFileTypes = new ArrayList<>(Collections.singletonList("ODS"));

    // Line within the ODS file where the header of the animal entry lines is found
    private final Integer sheetHeaderLine = 23;
    private final String firstHeaderEntry = "ImportID";

    private boolean hasParserError;
    private final ArrayList<String> parserErrorMessages = new ArrayList<>();

    public final String getNameRegistry() {
        return nameRegistry;
    }

    public final String getNameVerbose() {
        return nameVerbose;
    }

    public final ArrayList<String> getParsableFileTypes() {
        return parsableFileTypes;
    }

    /**
     * Method setting and returning command line options for this crawler.
     * @param regCrawlers Set of registered and available crawlers.
     * @return Defined commandline options.
     */
    public final Options getCLIOptions(final Set<String> regCrawlers) {
        final Options options = new Options();

        final Option opHelp = new Option("h", "help", false, "Print this message");

        final Option opCr = Option.builder("c")
                .longOpt("crawler")
                .desc(
                        String.join(
                                " ", "Shorthand of the required data crawler.",
                                " \nAvailable crawlers:", regCrawlers.toString()
                        )
                )
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        final Option opIn = Option.builder("i")
                .longOpt("in-file")
                .desc("Input file that's supposed to be parsed")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        final Option opOut = Option.builder("o")
                .longOpt("out-file")
                .desc(
                        String.join(
                                "", "Optional: Path and name of the output file. ",
                                "Files with the same name will be overwritten. ",
                                "Default file name uses format 'YYYYMMDDHHmm'"
                        )
                )
                .hasArg()
                .valueSeparator()
                .build();

        final Option opFormat = Option.builder("f")
                .longOpt("out-format")
                .desc(
                        String.join(
                            "", "Optional: format of the RDF file that will be written. ",
                            "Default setting is the Turtle (ttl) format"
                        )
                    )
                .hasArg()
                .valueSeparator()
                .build();

        final Option opOnt = Option.builder("n")
                .longOpt("ont-file")
                .desc(
                        String.join(
                                "", "Optional: one or more Onotology files can be provided ",
                                "to check if the created RDF files satisfy a required ontology"
                        )
                )
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

    /**
     * Method for validating user provided command line options.
     * @param cmd User provided commandline options.
     * @return
     */
    public final boolean validateCLIOptions(final CommandLine cmd) {
        // TODO find better name
        boolean correctCLI = true;
        if (cmd.hasOption("i")) {
            System.out.println(String.join(" ", "Input file:", cmd.getOptionValue("i")));
            if (!Files.exists(Paths.get(cmd.getOptionValue("i")))
                    && (!Files.exists(Paths.get(cmd.getOptionValue("i")).toAbsolutePath())))  {
                System.out.println("\nProvided input is not a valid file");
                correctCLI = false;
            }
        }

        if (cmd.hasOption("o")) {
            System.out.println(String.join(" ", "Output file:", cmd.getOptionValue("o")));
        }

        if (cmd.hasOption("f")) {
            System.out.println(String.join(" ", "Use output format:", cmd.getOptionValue("f")));
        }

        if (cmd.hasOption("n")) {
            System.out.println(String.join(" ", "Ontology file:", cmd.getOptionValue("n")));
        }
        return correctCLI;
    }

    /**
     * Method for parsing the contents of a provided ODS input file.
     * @param inputFile ODS file specific to Kay Thurleys usecase.
     */
    public final void parseFile(final String inputFile) {

        hasParserError = false;

        // TODO check if a robuster solution exists. Also check with Kay, if multiple backup files e.g. with a timestamp should exist.
        // TODO will fail for sure, if the file contains more than one period; should be addressed as well.
        // Create file backup
        final String backupFile = String.join("", inputFile.split("\\.")[0], "_backup.ods");
        try {
            Files.copy(Paths.get(inputFile), Paths.get(backupFile), StandardCopyOption.REPLACE_EXISTING);

            final File ODSFile = new File(inputFile);
            try {

                // TODO remove later
                System.out.println(String.join(" ", "File has # sheets:", String.valueOf(SpreadSheet.createFromFile(ODSFile).getSheetCount())));

                if (!(SpreadSheet.createFromFile(ODSFile).getSheetCount() > 0)) {
                    hasParserError = true;
                    parserErrorMessages.add(String.join(" ", "Provided labbook", inputFile, "does not contain valid data sheets"));
                } else {

                    final ArrayList<LKTLogbookSheet> allSheets = new ArrayList<>();
                    Sheet currSheet;
                    LKTLogbookSheet currLKTLSheet;
                    ArrayList<String> parseSheetMessage;
                    String parseEntryMessage;

                    String checkDB;
                    String checkDW;

                    for (int i = 0; i < SpreadSheet.createFromFile(ODSFile).getSheetCount(); i++) {

                        currSheet = SpreadSheet.createFromFile(ODSFile).getSheet(i);
                        final String sheetName = currSheet.getName();

                        // TODO remove later
                        System.out.println(
                                String.join(" ",
                                        "Sheet name:", currSheet.getName(), ", RowCount:",
                                        String.valueOf(currSheet.getRowCount()),
                                        ", ColCount:", String.valueOf(currSheet.getColumnCount())
                                )
                        );

                        currLKTLSheet = new LKTLogbookSheet();

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
                            hasParserError = true;
                            if (!parseSheetMessage.isEmpty()) {
                                parseSheetMessage.forEach(m -> parserErrorMessages.add(String.join(" ", "Parser error sheet", sheetName, ",", m)));
                            }
                            // check valid date of birth
                            if (!checkDB.isEmpty()) {
                                parserErrorMessages.add(String.join(" ", "Parser error sheet", sheetName, ",", checkDB));
                            }
                            // check valid date of withdrawal
                            if (!checkDW.isEmpty()) {
                                parserErrorMessages.add(String.join(" ", "Parser error sheet", sheetName, ",", checkDW));
                            }
                        }

                        // TODO come up with a more robust solution
                        // TODO check that line 23 contains the header and that the information start at line 24
                        String[] handleName;
                        String checkExperimentDate;
                        final String startCell = String.join("", "A", String.valueOf(sheetHeaderLine));

                        if (currSheet.getCellAt(startCell).getTextValue() == null || !currSheet.getCellAt(startCell).getTextValue().equals(firstHeaderEntry)) {
                            parserErrorMessages.add(
                                    String.join(" ", "Parser error sheet", sheetName,
                                            "HeaderEntry ImportID does not start at required line",
                                            String.valueOf(sheetHeaderLine)));
                        } else {

                            for (int j = sheetHeaderLine + 1; j < currSheet.getRowCount(); j++) {

                                final LKTLogbookEntry currEntry = new LKTLogbookEntry();

                                currEntry.setExistingImportID(currSheet.getCellAt(String.join("", "A", String.valueOf(j))).getTextValue());
                                currEntry.setProject(currSheet.getCellAt(String.join("", "K", String.valueOf(j))).getTextValue());
                                currEntry.setExperiment(currSheet.getCellAt(String.join("", "L", String.valueOf(j))).getTextValue());
                                currEntry.setParadigm(currSheet.getCellAt(String.join("", "C", String.valueOf(j))).getTextValue());
                                currEntry.setParadigmSpecifics(currSheet.getCellAt(String.join("", "D", String.valueOf(j))).getTextValue());

                                checkExperimentDate = currEntry.setExperimentDate(currSheet.getCellAt(String.join("", "B", String.valueOf(j))).getTextValue());

                                // TODO solve this better, add middle name
                                handleName = currSheet.getCellAt(String.join("", "M", String.valueOf(j))).getTextValue().trim().split("\\s+");

                                currEntry.setFirstName(handleName[0]);
                                currEntry.setLastName(handleName[handleName.length - 1]);
                                currEntry.setCommentExperiment(currSheet.getCellAt(String.join("", "H", String.valueOf(j))).getTextValue());
                                currEntry.setCommentAnimal(currSheet.getCellAt(String.join("", "I", String.valueOf(j))).getTextValue());
                                currEntry.setFeed(currSheet.getCellAt(String.join("", "J", String.valueOf(j))).getTextValue());
                                currEntry.setIsOnDiet(currSheet.getCellAt(String.join("", "E", String.valueOf(j))).getTextValue());
                                currEntry.setIsInitialWeight(currSheet.getCellAt(String.join("", "F", String.valueOf(j))).getTextValue());
                                currEntry.setWeight(currSheet.getCellAt(String.join("", "G", String.valueOf(j))).getTextValue());

                                parseEntryMessage = currEntry.isValidEntry();
                                if (!currEntry.getIsEmptyLine() && parseEntryMessage.isEmpty()) {
                                    currLKTLSheet.addEntry(currEntry);
                                } else if (!currEntry.getIsEmptyLine()
                                        && (!currEntry.getProject().isEmpty()
                                            || !currEntry.getExperiment().isEmpty()
                                            || currEntry.getExperimentDate() != null
                                            || !currEntry.getLastName().isEmpty())) {
                                    hasParserError = true;
                                    // decide whether an experiment date parser error has happened or if a required value is missing at this line
                                    String msg;
                                    msg = !checkExperimentDate.isEmpty() ? checkExperimentDate : String.join("", ", missing value:", parseEntryMessage);
                                    // Parser errors should be collected and written to a logfile. If a sheet contains multiple errors
                                    // the user should get them all at once and not with every run just the latest one.
                                    parserErrorMessages.add(String.join(" ", "Parser error sheet", currSheet.getName(), "row", String.valueOf(j), msg));
                                }
                            }

                            allSheets.add(currLKTLSheet);
                        }
                    }

                    allSheets.forEach(
                            s -> System.out.println(
                                    String.join(" ", "CurrSheet:", s.getAnimalID(), ", number of entries:", String.valueOf(s.getEntries().size()))
                            )
                    );

                    if (!hasParserError) {
                        // TODO remove later
                        allSheets.stream().forEach(
                                s -> {
                                    System.out.println(String.join(" ", "AnimalID:", s.getAnimalID(), ", AnimalSex:", s.getAnimalSex()));
                                    s.getEntries().stream().forEach(
                                            e -> System.out.println(
                                                String.join(
                                                        " ", "CurrRow:", e.getProject(),
                                                        e.getExperiment(), e.getExperimentDate().toString()
                                                )
                                            )
                                    );
                                });
                    } else {
                        // TODO write to logfile
                        parserErrorMessages.forEach(System.err::println);
                    }
                }

            } catch (final Exception exp) {
                System.err.println(String.join(" ", "ODS parser error:", exp.getMessage()));
                exp.printStackTrace();
            }

        } catch (final IOException exc) {
            System.err.println(String.join(" ", "Error creating backup file:", exc.getMessage()));
            exc.printStackTrace();
        }
    }

}
