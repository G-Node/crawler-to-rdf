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

import java.time.LocalDate;
import java.time.format.*;
import java.util.*;

/**
 * Object containing all information parsed from an ODS sheet.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
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

    public final void setAnimalID(final String aid) {
        this.animalID = aid;
    }

    public final String getAnimalID() {
        return animalID;
    }

    public final void setAnimalSex(final String asx) {
        this.animalSex = asx;
    }

    public final String getAnimalSex() {
        return animalSex;
    }

    public final LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public final String setDateOfBirth(final String dob) {
        String errMsg = "";

        try {
            this.dateOfBirth = LocalDate.parse(dob, supportedDate);
        } catch (final DateTimeParseException err) {
            if (dob == null || dob.isEmpty()) {
                errMsg = "Date of birth is missing";
            } else {
                errMsg = String.join(
                        "", "Invalid Date of birth format (", dob,
                        "). Use format '", supportedDatePattern, "'"
                );
            }
        }

        return errMsg;
    }

    public final LocalDate getDateOfWithdrawal() {
        return dateOfWithdrawal;
    }

    public final String setDateOfWithdrawal(final String dow) {
        String errMsg = "";
        try {
            this.dateOfWithdrawal = LocalDate.parse(dow, supportedDate);
        } catch (final DateTimeParseException err) {
            if (dow == null || dow.isEmpty()) {
                errMsg = "Date of withdrawal is missing";
            } else {
                errMsg = String.join(
                        "", "Invalid Date of withdrawal format (", dow,
                        "). Use format '", supportedDatePattern, "'"
                );
            }
        }
        return errMsg;
    }

    public final String getPermitNumber() {
        return permitNumber;
    }

    public final void setPermitNumber(final String pnr) {
        this.permitNumber = pnr;
    }

    public final String getSpecies() {
        return species;
    }

    public final void setSpecies(final String spc) {
        this.species = spc;
    }

    public final String getScientificName() {
        return scientificName;
    }

    public final void setScientificName(final String snm) {
        this.scientificName = snm;
    }

    public final ArrayList<LKTLogbookEntry> getEntries() {
        return entries;
    }

    public final void setEntries(final ArrayList<LKTLogbookEntry> ent) {
        this.entries = ent;
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
