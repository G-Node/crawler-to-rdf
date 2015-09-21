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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Object containing all information parsed from an ODS sheet.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LKTLogbookSheet {

    /**
     * Pattern that all Date values have to be formatted in
     * to be accepted by this parser.
     */
    private static final String SUPPORTED_DATE_PATTERN = "dd.MM.yyyy";
    /**
     * Formatter used to test Date values
     * for the pattern {@supportedDatePattern}.
     */
    private final DateTimeFormatter supportedDate = DateTimeFormatter.ofPattern(LKTLogbookSheet.SUPPORTED_DATE_PATTERN);

    /**
     * Animal ID of the current ODS sheet. Required value.
     */
    private String animalID;
    /**
     * Sex of the animal of the current ODS sheet. Required value.
     */
    private String animalSex;
    /**
     * Date of birth of the animal of the current ODS sheet.
     */
    private LocalDate dateOfBirth;
    /**
     * Date of withdrawal of the animal of the current ODS sheet.
     */
    private LocalDate dateOfWithdrawal;
    /**
     * Permit number for the animal of the current ODS sheet. Required value.
     */
    private String permitNumber;
    /**
     * Common species name of the animal of the current ODS sheet.
     */
    private String species;
    /**
     * Scientific species name of the animal of the current ODS sheet.
     */
    private String scientificName;

    /**
     * ArrayList containing all parsed experiment entries of the current ODS.
     */
    private ArrayList<LKTLogbookEntry> entries;

    /**
     * Constructor.
     */
    public LKTLogbookSheet() {
        this.entries = new ArrayList<>(0);
    }

    /**
     * Return the animalID of the animal of the current ODS sheet.
     * @return See description.
     */
    public final String getAnimalID() {
        return this.animalID;
    }

    /**
     * Set the animalID of the animal of the current ODS sheet.
     * This entry is required for a sheet to be complete and valid.
     * @param aid ID of the current animal.
     */
    public final void setAnimalID(final String aid) {
        this.animalID = aid;
    }

    /**
     * Return the sex of the animal of the current ODS sheet.
     * @return See description.
     */
    public final String getAnimalSex() {
        return this.animalSex;
    }

    /**
     * Set the sex of the animal of the current ODS sheet.
     * This entry is required for a sheet to be complete and valid.
     * @param asx Sex of the animal, has to be in format "f" or "m".
     */
    public final void setAnimalSex(final String asx) {
        this.animalSex = asx;
    }

    /**
     * Return the date of birth of the animal of the current ODS sheet.
     * @return Return the date of birth in format {@supportedDatePattern}.
     */
    public final LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    /**
     * Set the date of birth of the animal of the current ODS sheet.
     * This entry has to be conform to the date format {@supportedDatePattern}.
     * @param dob String containing the date of birth of the animal.
     * @return Error message, if the date does not conform to
     *  format {@supportedDatePattern}
     */
    public final String setDateOfBirth(final String dob) {
        String errMsg = "";
        try {
            this.dateOfBirth = LocalDate.parse(dob, this.supportedDate);
        } catch (final DateTimeParseException err) {
            if (dob == null || dob.isEmpty()) {
                errMsg = "Date of birth is missing";
            } else {
                errMsg = String.join(
                        "", "Invalid Date of birth format (", dob,
                        "). Use format '", this.SUPPORTED_DATE_PATTERN, "'"
                );
            }
        }
        return errMsg;
    }

    /**
     * Return the withdrawal date of the the animal of the current ODS sheet.
     * @return Animal withdrawal date in format {@supportedDatePattern}.
     */
    public final LocalDate getDateOfWithdrawal() {
        return this.dateOfWithdrawal;
    }

    /**
     * Set the withdrawal date of the animal of the current ODS sheet.
     * This entry has to be conform to the date format {@supportedDatePattern}.
     * @param dow String containing the withdrawal date of the animal.
     * @return Error message, if the date does not conform to
     *  format {@supportedDatePattern}
     */
    public final String setDateOfWithdrawal(final String dow) {
        String errMsg = "";
        try {
            this.dateOfWithdrawal = LocalDate.parse(dow, this.supportedDate);
        } catch (final DateTimeParseException err) {
            if (dow == null || dow.isEmpty()) {
                errMsg = "Date of withdrawal is missing";
            } else {
                errMsg = String.join(
                        "", "Invalid Date of withdrawal format (", dow,
                        "). Use format '", this.SUPPORTED_DATE_PATTERN, "'"
                );
            }
        }
        return errMsg;
    }

    /**
     * Return the permit number of the animal of the current ODS sheet.
     * @return See description.
     */
    public final String getPermitNumber() {
        return this.permitNumber;
    }

    /**
     * Set the permit number of the animal of the current ODS sheet.
     * This entry is required for a sheet to be complete and valid.
     * @param pnr Animal permit number.
     */
    public final void setPermitNumber(final String pnr) {
        this.permitNumber = pnr;
    }

    /**
     * Returns the common species name of the animal of the current ODS sheet.
     * @return See description.
     */
    public final String getSpecies() {
        return this.species;
    }

    /**
     * Sets the common species name of the animal of the current ODS sheet.
     * @param spc Common species animal name.
     */
    public final void setSpecies(final String spc) {
        this.species = spc;
    }

    /**
     * Returns the scientific species name of the animal of the current ODS sheet.
     * @return See description.
     */
    public final String getScientificName() {
        return this.scientificName;
    }

    /**
     * Sets the scientific species name of the animal of the current ODS sheet.
     * @param snm Scientific species animal name.
     */
    public final void setScientificName(final String snm) {
        this.scientificName = snm;
    }

    /**
     * Returns ArrayList of parsed {@LKTLogbookEntry} of the current ODS sheet.
     * @return Returns the {@entries} array.
     */
    public final ArrayList<LKTLogbookEntry> getEntries() {
        return this.entries;
    }

    /**
     * Method adds an ArrayList of {@LKTLogbookEntry} to the {@entries} array.
     * @param ent ArrayList of parsed {@LKTLogbookEntry}.
     */
    public final void setEntries(final ArrayList<LKTLogbookEntry> ent) {
        this.entries = ent;
    }

    /**
     * Method adds a single logbook entry to the {@entries} array.
     * @param entry Parsed {@LKTLogbookEntry}.
     */
    public final void addEntry(final LKTLogbookEntry entry) {
        this.entries.add(entry);
    }

    /**
     * Method to check if the current sheet contains all required information.
     * @return Validation message
     *  TODO maybe move all these checks directly to the setter methods
     *  TODO and come up with a good method how to pass the error messages to
     *  TODO the calling instance.
     */
    public final ArrayList<String> isValidSheet() {

        final ArrayList<String> validationMessage = new ArrayList<>(0);

        if (this.animalID.isEmpty() || Objects.equals(this.animalID, "")) {
            validationMessage.add("Missing animal ID");
        }
        if (this.animalSex.isEmpty() || Objects.equals(this.animalSex, "")) {
            validationMessage.add("Missing animal sex");
        } else if (!Objects.equals(this.animalSex, "m") && !Objects.equals(this.animalSex, "f")) {
            validationMessage.add(
                    String.join(
                            "", "Invalid animal sex (", this.getAnimalSex(), ")"
                    )
            );
        }

        if (this.permitNumber.isEmpty() || Objects.equals(this.permitNumber, "")) {
            validationMessage.add("Missing permitNumber");
        }
        return validationMessage;
    }
}
