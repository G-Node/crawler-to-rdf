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
    // required if a line was already imported before to mark it as an update rather than a new entry
    private boolean isUpdate;
    // required to check if a line is actually empty but parsed due to existing column format
    // if any of the required fields project, experiment, experimentDate or lastName are not
    // empty, then the line does not qualify as an empty line any longer
    private boolean isEmptyLine = true;

    private final String supportedDateTimePattern = "dd.MM.yyyy HH:mm";
    private final DateTimeFormatter supportedDateTime = DateTimeFormatter.ofPattern(supportedDateTimePattern);

    // -------- Getter and Setter ----------

    // -------------------------------------
    public final String getImportID() {
        return importID;
    }

    // if an ID already exists, the current entry has already been imported and has to be treated as an update
    // otherwise create a new ID
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

    public final String getProject() {
        return project;
    }

    public final void setProject(final String prj) {
        if (prj != null && !prj.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.project = prj;
    }

    public final String getExperiment() {
        return experiment;
    }

    public final void setExperiment(final String exp) {
        if (exp != null && !exp.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.experiment = exp;
    }

    public final String getParadigm() {
        return paradigm;
    }

    public final void setParadigm(final String para) {
        this.paradigm = para;
    }

    public final String getParadigmSpecifics() {
        return paradigmSpecifics;
    }

    public final void setParadigmSpecifics(final String pspec) {
        this.paradigmSpecifics = pspec;
    }

    public final LocalDateTime getExperimentDate() {
        return experimentDate;
    }

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

    public final String getFirstName() {
        return firstName;
    }

    public final void setFirstName(final String fnm) {
        this.firstName = fnm;
    }

    public final String getMiddleName() {
        return middleName;
    }

    public final void setMiddleName(final String mnm) {
        this.middleName = mnm;
    }

    public final String getLastName() {
        return lastName;
    }

    public final void setLastName(final String lnm) {
        if (lastName != null && !lastName.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.lastName = lnm;
    }

    public final String getCommentExperiment() {
        return commentExperiment;
    }

    public final void setCommentExperiment(final String cmtexp) {
        this.commentExperiment = cmtexp;
    }

    public final String getCommentAnimal() {
        return commentAnimal;
    }

    public final void setCommentAnimal(final String cmtan) {
        this.commentAnimal = cmtan;
    }

    public final String getFeed() {
        return feed;
    }

    public final void setFeed(final String onfd) {
        this.feed = onfd;
    }

    public final boolean getIsOnDiet() {
        return isOnDiet;
    }

    public final void setIsOnDiet(final String isod) {
        switch (isod) {
            case "y": this.isOnDiet = true;
            default: this.isOnDiet = false;
        }
    }

    public final boolean getIsInitialWeight() {
        return isInitialWeight;
    }

    public final void setIsInitialWeight(final String isinwght) {
        switch (isinwght) {
            case "y": this.isInitialWeight = true;
            default: this.isInitialWeight = false;
        }
    }

    public final String getWeight() {
        return weight;
    }

    public final void setWeight(final String wght) {
        this.weight = wght;
    }

    public final boolean getIsEmptyLine() {
        return isEmptyLine;
    }

    public final void setIsEmptyLine(final boolean iseline) {
        this.isEmptyLine = iseline;
    }

    public final boolean getIsUpdate() {
        return isUpdate;
    }

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
