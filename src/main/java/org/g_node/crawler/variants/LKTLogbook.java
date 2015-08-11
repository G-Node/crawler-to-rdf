// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler.variants;

import java.io.File;
import java.util.*;

import org.g_node.crawler.CrawlerTemplate;
import org.jopendocument.dom.spreadsheet.*;

/**
 * Parser for the main ODS metadata file used in the lab of Kay Thurley
 */
public class LKTLogbook implements CrawlerTemplate {

    public final String nameCommandLine = "lkt";
    public final String nameVerbose = "LMU Kay Thurley main metadata crawler";
    public final ArrayList<String> parsableFileTypes = new ArrayList<>(Collections.singletonList("ODS"));

    public String returnNameCommandLine() {
        return nameCommandLine;
    }

    public String returnNameVerbose() {
        return nameVerbose;
    }

    public ArrayList<String> returnParsableFileTypes() {
        return parsableFileTypes;
    }

    public void parseFile(String inputFile) {
        File ODSFile = new File(inputFile);
        try {

            System.out.println("File has # sheets: " + SpreadSheet.createFromFile(ODSFile).getSheetCount());

            if(!(SpreadSheet.createFromFile(ODSFile).getSheetCount() > 0)) {
                System.err.println("Provided labbook "+ inputFile +" does not contain valid data sheets");
                return;
            }

            ArrayList<LKTLogbookSheet> allSheets = new ArrayList<>();

            for (int i = 0; i < SpreadSheet.createFromFile(ODSFile).getSheetCount(); i++) {

                Sheet sheet = SpreadSheet.createFromFile(ODSFile).getSheet(i);
                System.out.println("Sheet name:" + sheet.getName() + ", RowCount: " + sheet.getRowCount() + ", ColCount: " + sheet.getColumnCount());

                LKTLogbookSheet currSheet = new LKTLogbookSheet();

                currSheet.setAnimalID(sheet.getCellAt("C2").getTextValue());
                currSheet.setAnimalSex(sheet.getCellAt("C3").getTextValue());
                currSheet.setDateOfBirth(sheet.getCellAt("C4").getTextValue());
                currSheet.setDateOfWithdrawal(sheet.getCellAt("C5").getTextValue());
                currSheet.setPermitNumber(sheet.getCellAt("C6").getTextValue());
                currSheet.setSpecies(sheet.getCellAt("C7").getTextValue());
                currSheet.setScientificName(sheet.getCellAt("C8").getTextValue());

                System.out.println("Remaining row number: "+ (sheet.getRowCount()-14));

                String currCell;
                Boolean invalid;
                for (int j = 15; j < sheet.getRowCount(); j++) {

                    LKTLogbookEntry currEntry = new LKTLogbookEntry();

                    currCell = sheet.getCellAt("A" + j).getTextValue();
                    currEntry.setProject(currCell);

                    currCell = sheet.getCellAt("B" + j).getTextValue();
                    currEntry.setExperiment(currCell);

                    currCell = sheet.getCellAt("C" + j).getTextValue();
                    currEntry.setParadigm(currCell);

                    currCell = sheet.getCellAt("D" + j).getTextValue();
                    currEntry.setExperimentDate(currCell);

                    currCell = sheet.getCellAt("E" + j).getTextValue();
                    currEntry.setFirstName(currCell);

                    currCell = sheet.getCellAt("F" + j).getTextValue();
                    currEntry.setLastName(currCell);

                    currCell = sheet.getCellAt("G" + j).getTextValue();
                    currEntry.setCommentExperiment(currCell);

                    currCell = sheet.getCellAt("H" + j).getTextValue();
                    currEntry.setLogEntry(currCell);

                    currCell = sheet.getCellAt("I" + j).getTextValue();
                    currEntry.setDiet(currCell);

                    currCell = sheet.getCellAt("J" + j).getTextValue();
                    currEntry.setInitialWeight(currCell);

                    currCell = sheet.getCellAt("K" + j).getTextValue();
                    currEntry.setWeight(currCell);


                    if(currEntry.checkValidEntry()) {
                        currSheet.addEntry(currEntry);
                    }
                }

                allSheets.add(currSheet);
            }

            allSheets.stream().forEach(s ->
            {
                System.out.println("AnimalID: " + s.getAnimalID() + ", AnimalSex: " + s.getAnimalSex());
                s.getEntries().stream().forEach(e -> System.out.println("CurrRow: " + e.getProject() + " " + e.getExperiment() + " " + e.getExperimentDate()));
            });

        } catch(Exception exp) {
            System.out.println("ODS parser error: "+ exp.getMessage());
        }
    }

}
