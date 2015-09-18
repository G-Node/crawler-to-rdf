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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * Object containing all information parsed from the individual data rows of an ODS sheet.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 * @version $Id 0.01$
 */
public class LKTLogbookEntry {

    private String importID;
    private String project;
    private String experiment;
    private String paradigm;
    private String paradigmSpecifics;
    private LocalDateTime experimentDate;
    private String firstName;
    private String middleName;
    private String lastName;
    private String commentExperiment;
    private String commentAnimal;
    private String feed;
    private boolean isOnDiet;
    private boolean isInitialWeight;
    private String weight;
    private boolean isUpdate;
    private boolean isEmptyLine = true;

    private final String supportedDateTimePattern = "dd.MM.yyyy HH:mm";
    private final DateTimeFormatter supportedDateTime = DateTimeFormatter.ofPattern(supportedDateTimePattern);

    /**
     * Return the importID of the current entry.
     * @return See description.
     */
    public final String getImportID() {
        return importID;
    }

    /**
     * If an importID already exists, the current entry has already been imported
     * and has to be treated as an update. If an ID already exists, the
     * variable isUpdate is set to true.
     * Otherwise a UUID is created and used as new ID.
     * @param impid String that is either empty or contains an already
     *  existing ID.
     */
    public final void setExistingImportID(final String impid) {
        this.importID = impid;
        if (impid != null && !impid.isEmpty()) {
            setIsUpdate(true);
            //TODO remove later
            System.out.println(String.join("", "Existing importID: ", impid));
        } else {
            this.importID = UUID.randomUUID().toString();
        }
    }

    /**
     * Return the project identifier string.
     * @return See description.
     */
    public final String getProject() {
        return project;
    }

    /**
     * Set the project identifier string. This entry is one of the required
     * fields to uniquely identify an entry. If the project identifier
     * is not available, the current line cannot be imported, the value of
     * the variable isEmptyLine is set to false.
     * @param prj String containing the project identifier.
     */
    public final void setProject(final String prj) {
        if (prj != null && !prj.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.project = prj;
    }

    /**
     * Return the experiment identifier string.
     * @return See description.
     */
    public final String getExperiment() {
        return experiment;
    }

    /**
     * Set the experiment identifier. This entry is one of the required
     * fields to uniquely identify an entry. If the experiment identifier
     * is not available, the current line cannot be imported, the value of
     * the variable isEmptyLine is set to false.
     * @param exp String containing the experiment identifier.
     */
    public final void setExperiment(final String exp) {
        if (exp != null && !exp.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.experiment = exp;
    }

    /**
     * Return the paradigm for the current entry.
     * @return See description.
     */
    public final String getParadigm() {
        return paradigm;
    }

    /**
     * Set the paradigm for the current entry.
     * @param para String containing the paradigm for the current entry.
     */
    public final void setParadigm(final String para) {
        this.paradigm = para;
    }

    /**
     * Return information about the paradigm.
     * @return See description.
     */
    public final String getParadigmSpecifics() {
        return paradigmSpecifics;
    }

    /**
     * Set specific information about the paradigm.
     * @param pspec String containing information about the paradigm.
     */
    public final void setParadigmSpecifics(final String pspec) {
        this.paradigmSpecifics = pspec;
    }

    /**
     * Return the date of the experiment using format {@supportedDateTimePattern}.
     * @return See description.
     */
    public final LocalDateTime getExperimentDate() {
        return experimentDate;
    }

    /**
     * Set the date of the experiment. Check that the date format is conform
     * to {@supportedDateTimePattern}. This entry is one of the required
     * fields to uniquely identify an entry. If the experiment date
     * is not available, the current line cannot be imported, the value of
     * the variable isEmptyLine is set to false.
     * @param expdt String containing the date of the experiment.
     * @return An error message, if the date was not conform to
     *  {@supportedDateTimePattern}
     */
    public final String setExperimentDate(final String expdt) {
        String errMsg = "";
        if (expdt != null && !expdt.isEmpty()) {
            setIsEmptyLine(false);
        }
        try {
            this.experimentDate = LocalDateTime.parse(expdt, supportedDateTime);
        } catch (final DateTimeParseException err) {
            if (expdt == null || expdt.isEmpty()) {
                errMsg = "Date of experiment is missing";
            } else {
                errMsg = String.join(
                        "", "Invalid experiment date format (", expdt,
                        "). Use format '", supportedDateTimePattern, "'"
                );
            }
        }
        return errMsg;
    }

    /**
     * Return the first name of the experimenter.
     * @return See description.
     */
    public final String getFirstName() {
        return firstName;
    }

    /**
     * Set the first name of the experimenter.
     * @param fnm First name of the experimenter.
     */
    public final void setFirstName(final String fnm) {
        this.firstName = fnm;
    }

    /**
     * Return the middle name of the experimenter.
     * @return See description.
     */
    public final String getMiddleName() {
        return middleName;
    }

    /**
     * Set the middle name of the experimenter.
     * @param mnm Middle name of the experimenter.
     */
    public final void setMiddleName(final String mnm) {
        this.middleName = mnm;
    }

    /**
     * Return the last name of the experimenter.
     * @return See description.
     */
    public final String getLastName() {
        return lastName;
    }

    /**
     * Set the last name of the experimenter.
     * @param lnm Last name of the experimenter.
     */
    public final void setLastName(final String lnm) {
        if (lastName != null && !lastName.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.lastName = lnm;
    }

    /**
     * Return comments about the experiment.
     * @return See description.
     */
    public final String getCommentExperiment() {
        return commentExperiment;
    }

    /**
     * Set comments about the experiment.
     * @param cmtexp Comment about the experiment.
     */
    public final void setCommentExperiment(final String cmtexp) {
        this.commentExperiment = cmtexp;
    }

    /**
     * Return comments about the animal at the current entry.
     * @return See description.
     */
    public final String getCommentAnimal() {
        return commentAnimal;
    }

    /**
     * Set comments about the animal.
     * @param cmtan Comment about the animal.
     */
    public final void setCommentAnimal(final String cmtan) {
        this.commentAnimal = cmtan;
    }

    /**
     * Return animal feed information.
     * @return See description.
     */
    public final String getFeed() {
        return feed;
    }

    /**
     * Set the feed for the current entry.
     * @param onfd Information about animal feed.
     */
    public final void setFeed(final String onfd) {
        this.feed = onfd;
    }

    /**
     * Return a boolean value if the animal is on diet.
     * @return See description.
     */
    public final boolean getIsOnDiet() {
        return isOnDiet;
    }

    /**
     * Set if the animal is on diet at the current entry.
     * @param isod Values y or n which are translated to true or false.
     */
    public final void setIsOnDiet(final String isod) {
        this.isOnDiet = isod != null && "y".equals(isod);
    }

    /**
     * Return a boolean value if the current entry contains the
     * initial weight for diet calculations.
     * @return See description.
     */
    public final boolean getIsInitialWeight() {
        return isInitialWeight;
    }

    /**
     * Set if the current entry is the initial weight for diet calculations.
     * @param isInWeight Values y or n which are translated to true or false.
     */
    public final void setIsInitialWeight(final String isInWeight) {
        this.isInitialWeight = isInWeight != null && "y".equals(isInWeight);
    }

    /**
     * Return the animal weight of the current entry.
     * @return See description.
     */
    public final String getWeight() {
        return weight;
    }

    /**
     * Set animal weight for the current entry.
     * @param wght Animal weight.
     */
    public final void setWeight(final String wght) {
        this.weight = wght;
    }

    /**
     * Return boolean value if the current entry contains no values.
     * @return See description.
     */
    public final boolean getIsEmptyLine() {
        return isEmptyLine;
    }

    /**
     * Set if the current entry contains no values or does not contain all
     * required fields.
     * Required to check, if an entry is actually empty, but parsed due to
     * existing column format.
     * If any of the required fields project, experiment, experimentDate or
     * lastName are not empty, then the line does not qualify as an empty
     * line any longer and has to be dealt with. If all of the required fields
     * are missing, the entry will simply not be imported.
     * TODO check if the description above is actually still completely true.
     * @param iseline If true, the current entry contains no values.
     */
    public final void setIsEmptyLine(final boolean iseline) {
        this.isEmptyLine = iseline;
    }

    /**
     * Return boolean value if the current entry is an update or a new entry.
     * @return See description.
     */
    public final boolean getIsUpdate() {
        return isUpdate;
    }

    /**
     * Set if the current entry is an update or not. This information
     * is required, if an entry was already previously imported,
     * to mark it as an update rather than a new entry.
     * @param isupd Boolean value, if true, the current entry is
     *  treated as an update.
     */
    public final void setIsUpdate(final boolean isupd) {
        this.isUpdate = isupd;
    }

    /**
     * Method to check if a data row from a sheet contains all required entries.
     * @return Empty String or a Message containing all missing entries
     */
    public final String isValidEntry() {

        // TODO maybe solve this better by using optional if there is no entry missing?
        String msg = "";

        if (getProject() == null || getProject().isEmpty()) {
            msg = msg.concat(" Project ");
        }
        if (getExperiment() == null || getExperiment().isEmpty()) {
            msg = msg.concat(" Experiment ");
        }
        if (getExperimentDate() == null) {
            msg = msg.concat(" Experiment date ");
        }
        if (getLastName() == null || getLastName().isEmpty()) {
            msg = msg.concat(" Name of experimenter ");
        }

        return msg;
    }
}
