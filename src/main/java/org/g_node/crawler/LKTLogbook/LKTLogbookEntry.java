// Copyright (c) 2015, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package org.g_node.crawler.LKTLogbook;

import java.time.LocalDateTime;
import java.time.format.*;
import java.util.UUID;

/**
 * Object containing all information parsed from the individual data rows of an ODS sheet
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
    private boolean isUpdate = false;
    // required to check if a line is actually empty but parsed due to existing column format
    // if any of the required fields project, experiment, experimentDate or lastName are not
    // empty, then the line does not qualify as an empty line any longer
    private boolean isEmptyLine = true;

    private final String supportedDateTimePattern = "dd.MM.yyyy HH:mm";
    private final DateTimeFormatter supportedDateTime = DateTimeFormatter.ofPattern(supportedDateTimePattern);

    // -------- Getter and Setter ----------

    // -------------------------------------
    public String getImportID() {
        return importID;
    }

    // if an ID already exists, the current entry has already been imported and has to be treated as an update
    // otherwise create a new ID
    public void setExistingImportID(String importID) {
        this.importID = importID;
        if(importID != null && !importID.isEmpty()){
            setIsUpdate(true);
            //TODO remove later
            System.out.println("Existing importID: "+ importID);
        } else {
            this.importID = UUID.randomUUID().toString();
        }
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        if(project != null && !project.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.project = project;
    }

    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        if(experiment != null && !experiment.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.experiment = experiment;
    }

    public String getParadigm() {
        return paradigm;
    }

    public void setParadigm(String paradigm) {
        this.paradigm = paradigm;
    }

    public String getParadigmSpecifics() {
        return paradigmSpecifics;
    }

    public void setParadigmSpecifics(String paradigmSpecifics) {
        this.paradigmSpecifics = paradigmSpecifics;
    }

    public LocalDateTime getExperimentDate() {
        return experimentDate;
    }

    public String setExperimentDate(String experimentDate) {
        String errMsg = "";
        if(experimentDate != null && !experimentDate.isEmpty()) {
            setIsEmptyLine(false);
        }
        try {
            this.experimentDate = LocalDateTime.parse(experimentDate, supportedDateTime);
        } catch(DateTimeParseException err) {
            if(experimentDate == null || experimentDate.isEmpty()) {
                errMsg = "Date of experiment is missing";
            } else {
                errMsg = "Invalid experiment date format (" + experimentDate + "). Use format '" + supportedDateTimePattern + "'";
            }
        }
        return errMsg;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if(lastName != null && !lastName.isEmpty()) {
            setIsEmptyLine(false);
        }
        this.lastName = lastName;
    }

    public String getCommentExperiment() {
        return commentExperiment;
    }

    public void setCommentExperiment(String commentExperiment) {
        this.commentExperiment = commentExperiment;
    }

    public String getCommentAnimal() {
        return commentAnimal;
    }

    public void setCommentAnimal(String commentAnimal) {
        this.commentAnimal = commentAnimal;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public boolean getIsOnDiet() {
        return isOnDiet;
    }

    public void setIsOnDiet(String isOnDiet) {
        switch (isOnDiet) {
            case "y": this.isOnDiet = true;
            case "n": this.isOnDiet = false;
        }
    }

    public boolean getIsInitialWeight() {
        return isInitialWeight;
    }

    public void setIsInitialWeight(String isInitialWeight) {
        switch (isInitialWeight) {
            case "y": this.isInitialWeight = true;
            case "n": this.isInitialWeight = false;
        }
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public boolean isEmptyLine() {
        return isEmptyLine;
    }

    public void setIsEmptyLine(boolean isEmptyLine) {
        this.isEmptyLine = isEmptyLine;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    // -------- Custom methods --------

    // --------------------------------
    public String isValidEntry() {

        String msg = "";

        if(getProject() == null || getProject().equals("")){
            msg = msg.concat(" Project ");
        }
        if(getExperiment() == null || getExperiment().equals("")){
            msg = msg.concat(" Experiment ");
        }
        if(getExperimentDate() == null){
            msg = msg.concat(" Experiment date ");
        }
        if(getLastName() == null || getLastName().equals("")){
            msg = msg.concat(" Name of experimenter ");
        }

        return msg;
    }

}
