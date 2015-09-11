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
 * Object containing all information parsed from an ODS sheet.
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

    public final void setAnimalID(final String animalID) {
        this.animalID = animalID;
    }

    public final String getAnimalID() {
        return animalID;
    }

    public final void setAnimalSex(final String animalSex) {
        this.animalSex = animalSex;
    }

    public final String getAnimalSex() {
        return animalSex;
    }

    public final LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public final String setDateOfBirth(final String dateOfBirth) {
        String errMsg = "";

        try {
            this.dateOfBirth = LocalDate.parse(dateOfBirth, supportedDate);
        } catch (final DateTimeParseException err) {
            if (dateOfBirth == null || dateOfBirth.isEmpty()) {
                errMsg = "Date of birth is missing";
            } else {
                errMsg = String.join(
                        "", "Invalid Date of birth format (", dateOfBirth,
                        "). Use format '", supportedDatePattern, "'"
                );
            }
        }

        return errMsg;
    }

    public final LocalDate getDateOfWithdrawal() {
        return dateOfWithdrawal;
    }

    public final String setDateOfWithdrawal(final String dateOfWithdrawal) {
        String errMsg = "";
        try {
            this.dateOfWithdrawal = LocalDate.parse(dateOfWithdrawal, supportedDate);
        } catch (final DateTimeParseException err) {
            if (dateOfWithdrawal == null || dateOfWithdrawal.isEmpty()) {
                errMsg = "Date of withdrawal is missing";
            } else {
                errMsg = String.join(
                        "", "Invalid Date of withdrawal format (", dateOfWithdrawal,
                        "). Use format '", supportedDatePattern, "'"
                );
            }
        }
        return errMsg;
    }

    public final String getPermitNumber() {
        return permitNumber;
    }

    public final void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public final String getSpecies() {
        return species;
    }

    public final void setSpecies(final String species) {
        this.species = species;
    }

    public final String getScientificName() {
        return scientificName;
    }

    public final void setScientificName(final String scientificName) {
        this.scientificName = scientificName;
    }

    public final ArrayList<LKTLogbookEntry> getEntries() {
        return entries;
    }

    public final void setEntries(final ArrayList<LKTLogbookEntry> entries) {
        this.entries = entries;
    }

    public final void addEntry(final LKTLogbookEntry entry) {
        this.entries.add(entry);
    }

    /**
     * Method to check if the current sheet contains all required information.
     * @return Validation message
     */
    public final ArrayList<String> isValidSheet() {

        final ArrayList<String> validationMessage = new ArrayList<>();

        if (animalID.isEmpty() || Objects.equals(animalID, "")) {
            validationMessage.add("Missing animal ID");
        }
        if (animalSex.isEmpty() || Objects.equals(animalSex, "")) {
            validationMessage.add("Missing animal sex");
        } else if (!Objects.equals(animalSex, "m") && !Objects.equals(animalSex, "f")) {
            validationMessage.add(
                    String.join(
                            "", "Invalid animal sex (", getAnimalSex(), ")"
                    )
            );
        }

        if (permitNumber.isEmpty() || Objects.equals(permitNumber, "")) {
            validationMessage.add("Missing permitNumber");
        }

        return validationMessage;
    }

}
