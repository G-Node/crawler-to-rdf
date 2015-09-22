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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
     * Boolean value stating, if errors during the parsing of the ODS file
     * have occurred.
     */
    private boolean hasParserError;

    /**
     * ArrayList containing all messages that occurred while parsing the ODS file.
     * All parser errors connected to missing values or incorrect value formats should
     * be collected and written to a logfile, so that users can correct these
     * mistakes ideally all at once before running the crawler again.
     */
    private final ArrayList<String> parserErrorMessages = new ArrayList<>(0);

    /**
     * Method for parsing the contents of a provided ODS input file.
     * @param inputFile ODS file specific to Kay Thurleys usecase.
     */
    public final void parseFile(final String inputFile) {

        this.hasParserError = false;

        // TODO is this a good way to end the execution of the app
        // TODO if the input file is not available?
        if (!this.checkInputFile(inputFile)) {
            return;
        }

        // TODO check if a robuster solution exists. Also check with Kay,
        // TODO if multiple backup files e.g. with a timestamp should exist.
        // TODO will fail for sure, if the file contains more than one period; should be addressed as well.
        // Create file backup
        final String backupFile = String.join("", inputFile.split("\\.")[0], "_backup.ods");
        try {
            Files.copy(Paths.get(inputFile), Paths.get(backupFile), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exc) {
            System.err.println(String.join(" ", "Error creating backup file:", exc.getMessage()));
            exc.printStackTrace();
            // TODO is this in this case a valid way to end the function?
            return;
        }

        try {
            final File odsFile = new File(inputFile);

            // TODO remove later
            System.out.println(
                    String.join(
                            " ", "File has # sheets:",
                            String.valueOf(SpreadSheet.createFromFile(odsFile).getSheetCount()))
            );

            if (!(SpreadSheet.createFromFile(odsFile).getSheetCount() > 0)) {
                this.hasParserError = true;
                this.parserErrorMessages.add(
                        String.join(
                                " ", "Provided labbook",
                                inputFile, "does not contain valid data sheets"
                        )
                );
            } else {
                final ArrayList<LKTLogbookSheet> allSheets = this.parseSheets(odsFile);
                allSheets.forEach(
                        s -> System.out.println(
                                String.join(
                                        " ", "CurrSheet:", s.getAnimalID(),
                                        ", number of entries:",
                                        String.valueOf(s.getEntries().size())
                                )
                        )
                );

                if (!this.hasParserError) {
                    // TODO remove later
                    allSheets.stream().forEach(
                        s -> {
                            System.out.println(
                                    String.join(
                                            " ", "AnimalID:", s.getAnimalID(),
                                            ", AnimalSex:", s.getAnimalSex()
                                    )
                            );
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
                    this.parserErrorMessages.forEach(System.err::println);
                }
            }
        } catch (final IOException exp) {
            System.err.println(String.join(" ", "Error reading from input file:", exp.getMessage()));
            exp.printStackTrace();
        }
    }

    /**
     * Method for validating that the input file actually exists.
     * @param inputFile Path and filename of the provided input file.
     * @return True if the file exists, false otherwise.
     */
    private boolean checkInputFile(final String inputFile) {
        boolean correctFile = true;
        if (!Files.exists(Paths.get(inputFile))
                    && !Files.exists(Paths.get(inputFile).toAbsolutePath())) {
            System.err.println(String.join(" ", "Invalid input file:", inputFile));
            correctFile = false;
        }
        return correctFile;
    }

    /**
     * Method parsing all sheets of the current ODS file.
     * If parsing errors occur, {@hasParserError} will be set to true,
     * the corresponding message will be added to {@parserErrorMessages}.
     * Parsing will continue to collect further possible parser errors.
     * @param odsFile Input file.
     * @return ArrayList containing parsed {@LKTLogbookSheet}.
     */
    private ArrayList<LKTLogbookSheet> parseSheets(final File odsFile) {
        final ArrayList<LKTLogbookSheet> allSheets = new ArrayList<>(0);
        Sheet currSheet;

        try {
            for (int i = 0; i < SpreadSheet.createFromFile(odsFile).getSheetCount(); i = i + 1) {

                currSheet = SpreadSheet.createFromFile(odsFile).getSheet(i);
                final String sheetName = currSheet.getName();

                // TODO remove later
                System.out.println(
                        String.join(
                                " ", "Sheet name:", currSheet.getName(),
                                ", RowCount:", String.valueOf(currSheet.getRowCount()),
                                ", ColCount:", String.valueOf(currSheet.getColumnCount())
                        )
                );

                LKTLogbookSheet currLKTLSheet = this.parseSheetVariables(currSheet);

                // TODO come up with a more robust solution
                // TODO check that line 23 contains the header and that the information start at line 24
                final String startCell = String.join("", "A", String.valueOf(LKTLogbook.SHEET_HEADER_LINE));

                if (currSheet.getCellAt(startCell).getTextValue() == null
                        || !currSheet.getCellAt(startCell).getTextValue().equals(LKTLogbook.FIRST_HEADER_ENTRY)) {
                    this.parserErrorMessages.add(
                            String.join(
                                    " ", "Parser error at sheet", sheetName,
                                    "HeaderEntry 'ImportID' not found at required line A",
                                    String.valueOf(LKTLogbook.SHEET_HEADER_LINE)
                            )
                    );
                } else {
                    currLKTLSheet = this.parseSheetEntries(currSheet, currLKTLSheet);
                    allSheets.add(currLKTLSheet);
                }
            }
        } catch (final IOException exp) {
            System.err.println(String.join(" ", "Error reading from input file:", exp.getMessage()));
            exp.printStackTrace();
        }
        return allSheets;
    }

    /**
     * Method for retrieving all sheet specific data from the current ODS sheet.
     * @param currSheet The current sheet from the ODS file.
     * @return The current {@LKTLogbookSheet} containing all parsed values.
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
            this.hasParserError = true;
            if (!parseSheetMessage.isEmpty()) {
                parseSheetMessage.forEach(
                        m -> this.parserErrorMessages.add(
                                String.join(" ", "Parser error sheet", sheetName, ",", m)
                        )
                );
            }
            // check valid date of birth
            if (!checkDB.isEmpty()) {
                this.parserErrorMessages.add(String.join(" ", "Parser error sheet", sheetName, ",", checkDB));
            }
            // check valid date of withdrawal
            if (!checkDW.isEmpty()) {
                this.parserErrorMessages.add(String.join(" ", "Parser error sheet", sheetName, ",", checkDW));
            }
        }
        return currLKTLSheet;
    }

    /**
     * Method for parsing the experiment entries of an animal sheet.
     * If parsing errors occur, {@hasParserError} will be set to true,
     * the corresponding message will be added to {@parserErrorMessages}.
     * Parsing will continue to collect further possible parser errors.
     * @param currSheet The current sheet of the parsed ODS file.
     * @param currLKTLSheet The current {@LKTLogbookSheet}.
     * @return The current {@LKTLogbookSheet} containing the parsed
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
                this.hasParserError = true;
                this.parserErrorMessages.add(
                        String.join(
                                " ", "Parser error sheet",
                                currSheet.getName(), "row",
                                String.valueOf(i), String.join("", ", missing value:", parseEntryMessage)
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
     * @return The {@LKTLogbookEntry} containing the parsed values from
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
            this.hasParserError = true;
            this.parserErrorMessages.add(
                    String.join(
                            " ", "Parser error sheet",
                            currSheet.getName(), "row",
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
