// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler.LKTLogbook;

import java.time.LocalDate;
import java.time.format.*;
import java.util.*;

/**
 * Object containing all information parsed from an ODS sheet
 */
public class LKTLogbookSheet {

    private String animalID;
    private String animalSex;
    private LocalDate dateOfBirth;
    private LocalDate dateOfWithdrawal;
    private String permitNumber;
    private String species;
    private String scientificName;
    private ArrayList<LKTLogbookEntry> entries;

    private final String supportedDatePattern = "dd.MM.yyyy";
    private final DateTimeFormatter supportedDate = DateTimeFormatter.ofPattern(supportedDatePattern);

    public LKTLogbookSheet() {
        entries = new ArrayList<>();
    }

    public void setAnimalID(String animalID) {
        this.animalID = animalID;
    }

    public String getAnimalID(){
        return animalID;
    }

    public void setAnimalSex(String animalSex) {
        this.animalSex = animalSex;
    }

    public String getAnimalSex() {
        return animalSex;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String setDateOfBirth(String dateOfBirth) {
        String errMsg = "";

        try {
            this.dateOfBirth = LocalDate.parse(dateOfBirth, supportedDate);
        } catch(DateTimeParseException err) {
            if(dateOfBirth == null || dateOfBirth.isEmpty()) {
                errMsg = "Date of birth is missing";
            } else {
                errMsg = "Invalid Date of birth format (" + dateOfBirth + "). Use format '" + supportedDatePattern + "'";
            }
        }

        return errMsg;
    }

    public LocalDate getDateOfWithdrawal() {
        return dateOfWithdrawal;
    }

    public String setDateOfWithdrawal(String dateOfWithdrawal) {
        String errMsg = "";
        try {
            this.dateOfWithdrawal = LocalDate.parse(dateOfWithdrawal, supportedDate);
        } catch(DateTimeParseException err) {
            if(dateOfWithdrawal == null || dateOfWithdrawal.isEmpty()) {
                errMsg = "Date of withdrawal is missing";
            } else {
                errMsg = "Invalid Date of withdrawal format (" + dateOfWithdrawal + "). Use format '" + supportedDatePattern + "'";
            }
        }
        return errMsg;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public ArrayList<LKTLogbookEntry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<LKTLogbookEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(LKTLogbookEntry entry) {
        this.entries.add(entry);
    }

    // TODO redo validity checks
    public ArrayList<String> isValidSheet() {

        ArrayList<String> validationMessage = new ArrayList<>();

        if (animalID.isEmpty() || Objects.equals(animalID, "")) {
            validationMessage.add("Missing animal ID");
        }
        if (animalSex.isEmpty() || Objects.equals(animalSex, "")) {
            validationMessage.add("Missing animal sex");
        } else if (!Objects.equals(animalSex,"m") && !Objects.equals(animalSex,"f")) {
            validationMessage.add("Invalid animal sex ("+ getAnimalSex() +")");
        }

        if (permitNumber.isEmpty() || Objects.equals(permitNumber, "")) {
            validationMessage.add("Missing permitNumber");
        }

        return validationMessage;
    }

}
