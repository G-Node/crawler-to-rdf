// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler.LKTLogbook;

import java.io.File;
import java.nio.file.*;
import java.util.*;

import org.apache.commons.cli.*;
import org.g_node.crawler.CrawlerTemplate;
import org.jopendocument.dom.spreadsheet.*;

/**
 * Parser for the main ODS metadata file used in the lab of Kay Thurley
 */
public class LKTLogbook implements CrawlerTemplate {

    public final String nameRegistry = "lkt";
    public final String nameVerbose = "LMU Kay Thurley main metadata crawler";
    public final ArrayList<String> parsableFileTypes = new ArrayList<>(Collections.singletonList("ODS"));

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

                String[] handleName;
                for (int j = 24; j < sheet.getRowCount(); j++) {

                    LKTLogbookEntry currEntry = new LKTLogbookEntry();

                    currEntry.setProject(sheet.getCellAt("B" + j).getTextValue());

                    currEntry.setExperiment(sheet.getCellAt("C" + j).getTextValue());

                    currEntry.setParadigm(sheet.getCellAt("D" + j).getTextValue());

                    currEntry.setExperimentDate(sheet.getCellAt("E" + j).getTextValue());

                    // [TODO] solve this better, add middle name
                    handleName = sheet.getCellAt("F" + j).getTextValue().trim().split("\\s+");

                    currEntry.setFirstName(handleName[0]);
                    currEntry.setLastName(handleName[handleName.length - 1]);

                    currEntry.setCommentExperiment(sheet.getCellAt("G" + j).getTextValue());

                    currEntry.setCommentAnimal(sheet.getCellAt("H" + j).getTextValue());

                    currEntry.setFeed(sheet.getCellAt("I" + j).getTextValue());

                    currEntry.setDiet(sheet.getCellAt("J" + j).getTextValue());

                    currEntry.setInitialWeight(sheet.getCellAt("K" + j).getTextValue());

                    currEntry.setWeight(sheet.getCellAt("L" + j).getTextValue());

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
