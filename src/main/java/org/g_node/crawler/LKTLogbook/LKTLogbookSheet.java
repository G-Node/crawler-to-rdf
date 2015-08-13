// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler.LKTLogbook;

import java.util.*;

/**
 * Object containing all information parsed from an ODS sheet
 */
public class LKTLogbookSheet {

    private String animalID;
    private String animalSex;
    private String dateOfBirth;
    private String dateOfWithdrawal;
    private String permitNumber;
    private String species;
    private String scientificName;
    private ArrayList<LKTLogbookEntry> entries;

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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfWithdrawal() {
        return dateOfWithdrawal;
    }

    public void setDateOfWithdrawal(String dateOfWithdrawal) {
        this.dateOfWithdrawal = dateOfWithdrawal;
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

    public ArrayList<String> isValidSheet() {

        ArrayList<String> validationMessage = new ArrayList<>();

        if (animalID.isEmpty() || Objects.equals(animalID, "")) {
            validationMessage.add("Invalid animalID");
        }
        if (animalSex.isEmpty() || Objects.equals(animalSex, "")) {
            validationMessage.add("Invalid animalSex");
        }
        if (dateOfBirth.isEmpty() || Objects.equals(dateOfBirth, "")) {
            validationMessage.add("Invalid dateOfBirth");
        }
        if (dateOfWithdrawal.isEmpty() || Objects.equals(dateOfWithdrawal, "")) {
            validationMessage.add("Invalid dateOfWithdrawal");
        }
        if (permitNumber.isEmpty() || Objects.equals(permitNumber, "")) {
            validationMessage.add("Invalid permitNumber");
        }

        return validationMessage;
    }

}
