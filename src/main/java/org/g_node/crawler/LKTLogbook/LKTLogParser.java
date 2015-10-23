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
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 * Parser for the main ODS metadata file used in the lab of Kay Thurley.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LKTLogParser {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(LKTLogParser.class.getName());
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
     * Reference of the parserErrorMessages in the corresponding controller class.
     * ArrayList containing all messages that occurred while parsing the input file(s).
     * All parser errors connected to missing values or incorrect value formats should
     * be collected and written to a logfile, so that users can correct these
     * mistakes ideally all at once before running the crawler again.
     */
    private ArrayList<String> parserErrorMessages;
    /**
     * Method for parsing the contents of a provided ODS input file.
     * This method will create a backup file of the original ODS file.
     * @param inputFile ODS file specific to Kay Thurleys usecase.
     * @param parserErrMsg ArrayList collecting all parserErrorMessages in the corresponding
     *                     {@link LKTLogController}.
     * @return Array list containing all data from all parsed ODS sheets.
     */
    public final ArrayList<LKTLogParserSheet> parseFile(final String inputFile,
                                                        final ArrayList<String> parserErrMsg) {

        this.parserErrorMessages = parserErrMsg;

        ArrayList<LKTLogParserSheet> allSheets = new ArrayList<>(0);

        LKTLogParser.LOGGER.info("Starting to parse provided file...");
        try {
            final File odsFile = new File(inputFile);

            LKTLogParser.LOGGER.info(
                    String.join(
                            "", "File has # sheets: ",
                            String.valueOf(SpreadSheet.createFromFile(odsFile).getSheetCount()))
            );

            if (!(SpreadSheet.createFromFile(odsFile).getSheetCount() > 0)) {
                this.parserErrorMessages.add(String.join(
                        "", "[Parser] File ", inputFile,
                        " does not contain valid data sheets."
                ));
            } else {
                allSheets = this.parseSheets(odsFile);
                allSheets.forEach(
                        s -> LKTLogParser.LOGGER.info(
                                String.join(
                                        "", "CurrSheet: ", s.getAnimalID(),
                                        ", number of entries: ", String.valueOf(s.getEntries().size())
                                )
                        )
                );

                if (this.parserErrorMessages.size() > 0) {
                    this.parserErrorMessages.add(
                            "\n\tThere are parser errors present. Please resolve them and run the program again.");
                }
            }
        } catch (final IOException exp) {
            this.parserErrorMessages.add(String.join("", "[Error] reading from input file: ", exp.getMessage()));
            exp.printStackTrace();
        }

        return allSheets;
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
                    this.parserErrorMessages.add(String.join(
                            "", "[Parser] sheet ", sheetName,
                            ", HeaderEntry 'ImportID' not found at required line A.",
                            String.valueOf(LKTLogParser.SHEET_HEADER_LINE)
                        ));

                } else {
                    currLKTLSheet = this.parseSheetEntries(currSheet, currLKTLSheet);
                    allSheets.add(currLKTLSheet);
                }
            }
        } catch (final IOException exp) {
            this.parserErrorMessages.add(String.join("", "[Error] reading from input file: ", exp.getMessage()));

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
                        m -> this.parserErrorMessages.add(String.join("", "[Parser] sheet ", sheetName, ", ", m))
                );
            }
            // Check valid date of birth
            if (!checkDB.isEmpty()) {
                this.parserErrorMessages.add(String.join("", "[Parser] sheet ", sheetName, ", ", checkDB));
            }
            // Check valid date of withdrawal
            if (!checkDW.isEmpty()) {
                this.parserErrorMessages.add(String.join("", "[Parser] sheet ", sheetName, ", ", checkDW));
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
                this.parserErrorMessages.add(String.join(
                        "", "[Parser] sheet ", currFileSheet.getName(), " row ",
                        String.valueOf(i), ", missing value: ", parseEntryMessage
                ));
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

        // TODO Check if the experimentDate parser error and the empty line messages all still work!
        checkExperimentDate = currEntry.setExperimentDate(currSheet.getCellAt(
                String.join("", "B", currLine)).getTextValue()
        );
        if (!checkExperimentDate.isEmpty()) {
            this.parserErrorMessages.add(String.join(
                    "", "[Parser] sheet ", currSheet.getName(), " row ",
                    currLine, "\n\t", checkExperimentDate
            ));
        }

        currEntry.setExperimenterName(currSheet.getCellAt(String.join("", "M", currLine)).getTextValue());
        currEntry.setCommentExperiment(currSheet.getCellAt(String.join("", "H", currLine)).getTextValue());
        currEntry.setCommentAnimal(currSheet.getCellAt(String.join("", "I", currLine)).getTextValue());
        currEntry.setFeed(currSheet.getCellAt(String.join("", "J", currLine)).getTextValue());
        currEntry.setIsOnDiet(currSheet.getCellAt(String.join("", "E", currLine)).getTextValue());
        currEntry.setIsInitialWeight(currSheet.getCellAt(String.join("", "F", currLine)).getTextValue());

        final String currMsg = currEntry.setWeight(currSheet.getCellAt(String.join("", "G", currLine)).getTextValue());
        if (!"".equals(currMsg)) {
            this.parserErrorMessages.add(String.join(
                    "", "[Parser] sheet ", currSheet.getName(),
                    " row ", currLine, " ", currMsg
            ));
        }

        return currEntry;
    }

}
