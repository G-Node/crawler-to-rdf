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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.g_node.srv.RDFService;
import org.g_node.srv.RDFUtils;

/**
 * Class converting parsed data to RDF.
 */
public class LKTLogToRDF {
    /**
     * Namespace used to identify RDF resources and properties specific for the current usecase.
     */
    private static final String RDF_NS =  "http://g-node.org/orcid/0000-0003-4857-1083/lkt/";
    /**
     * Namespace prefix.
     */
    private static final String RDF_NS_ABR = "lkt";
    /**
     * Namespace used to identify FOAF RDF resources.
     */
    private static final String RDF_NS_FOAF = "http://xmlns.com/foaf/0.1/";
    /**
     * FOAF Namespace prefix.
     */
    private static final String RDF_NS_FOAF_ABR = "foaf";
    /**
     * Namespace used to identify Dublin core RDF resources.
     */
    private static final String RDF_NS_DC = "http://purl.org/dc/terms/";
    /**
     * Dublin core Namespace prefix.
     */
    private static final String RDF_NS_DC_ABR = "dc";
    /**
     * Map containing all projects with their newly created UUIDs of the parsed ODS sheet.
     */
    private final Map<String, String> projectList = new HashMap<>();
    /**
     * Map containing all the animalIDs with their newly created UUIDs of the parsed ODS sheet.
     */
    private final Map<String, String> animalList = new HashMap<>();
    /**
     * Map containing all the experimenters with their newly created UUIDs contained in the parsed ODS sheet.
     */
    private final Map<String, String> experimenterList = new HashMap<>();
    /**
     * Main RDF model containing all the parsed information from the ODS sheet.
     */
    private final Model model = ModelFactory.createDefaultModel();
    /**
     * Creates an RDF model from the parsed ODS sheet data and writes
     * the model to the designated output file.
     * @param allSheets Data from the parsed ODS sheets.
     * @param inputFile Name and path of the input file
     * @param outputFile Name and path of the designated output file.
     * @param outputFormat RDF output format.
     */
    public final void createRDFModel(final ArrayList<LKTLogParserSheet> allSheets, final String inputFile,
                                final String outputFile, final String outputFormat) {

        // TODO Check if namespaces should be handled somewhere else
        this.model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.model.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");
        this.model.setNsPrefix(LKTLogToRDF.RDF_NS_FOAF_ABR, LKTLogToRDF.RDF_NS_FOAF);
        this.model.setNsPrefix(LKTLogToRDF.RDF_NS_DC_ABR, LKTLogToRDF.RDF_NS_DC);
        this.model.setNsPrefix(LKTLogToRDF.RDF_NS_ABR, LKTLogToRDF.RDF_NS);

        final String provUUID = UUID.randomUUID().toString();
        this.localRes(provUUID)
                .addProperty(RDF.type, this.localRes("Provenance"))
                .addProperty(this.localDCProp("source"), inputFile)
                .addProperty(this.localDCProp("created"),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm")))
                .addProperty(this.localDCProp("subject"),
                        "This RDF file was created by parsing data from the file indicated in the source literal");

        allSheets.stream().forEach(a -> this.addAnimal(a, provUUID));

        RDFService.writeModelToFile(outputFile, this.model, outputFormat);
    }

    /**
     * Adds all data of a parsed ODS sheet to the main RDF model. The problem here is
     * that the ODS sheet is centered around the animal, while the RDF model is project
     * centric.
     * @param currSheet Data from the current sheet.
     * @param provUUID UUID of the provenance resource.
     */
    private void addAnimal(final LKTLogParserSheet currSheet, final String provUUID) {

        final String animalID = currSheet.getAnimalID();
        if (!this.animalList.containsKey(animalID)) {
            this.animalList.put(animalID, UUID.randomUUID().toString());
        }

        // TODO Handle dates properly
        final Resource permit = this.localRes(UUID.randomUUID().toString())
                .addProperty(this.localProp("hasProvenance"), this.fetchLocalRes(provUUID))
                .addProperty(RDF.type, this.localRes("Permit"))
                .addLiteral(this.localProp("hasNumber"), currSheet.getPermitNumber());

        final Resource animal = this.localRes(this.animalList.get(animalID))
                .addProperty(this.localProp("hasProvenance"), this.fetchLocalRes(provUUID))
                .addProperty(RDF.type, this.localRes("Animal"))
                .addLiteral(this.localProp("hasAnimalID"), animalID)
                .addLiteral(this.localProp("hasSex"), currSheet.getAnimalSex())
                .addLiteral(this.localProp("hasBirthDate"), currSheet.getDateOfBirth().toString())
                .addLiteral(this.localProp("hasWithdrawalDate"), currSheet.getDateOfWithdrawal().toString())
                .addProperty(this.localProp("hasPermit"), permit);

        RDFUtils.addNonEmptyLiteral(animal, this.localProp("hasSpeciesName"), currSheet.getSpecies());
        RDFUtils.addNonEmptyLiteral(animal, this.localProp("hasScientificName"), currSheet.getScientificName());

        currSheet.getEntries().stream().forEach(
                c -> this.addEntry(c, animal, provUUID)
        );
    }

    /**
     * Adds the data of the current ODS entry to the main RDF model.
     * @param currEntry Data from the current ODS line entry.
     * @param animal Resource from the main RDF model containing the information about
     *  the animal this entry is associated with.
     * @param provUUID UUID of the provenance resource.
     */
    private void addEntry(final LKTLogParserEntry currEntry, final Resource animal, final String provUUID) {

        final String project = currEntry.getProject();
        // add project only once to the rdf model
        if (!this.projectList.containsKey(project)) {

            this.projectList.put(project, UUID.randomUUID().toString());
            this.localRes(this.projectList.get(project))
                    .addProperty(this.localProp("hasProvenance"), this.fetchLocalRes(provUUID))
                    .addProperty(RDF.type, this.localRes("Project"))
                    .addLiteral(RDFS.label, project);
        }
        // Fetch project resource
        final Resource projectRes = this.fetchLocalRes(this.projectList.get(project));

        // Add experimenter only once to the RDF model
        final String experimenter = currEntry.getExperimenterName();
        if (!this.experimenterList.containsKey(experimenter)) {

            this.experimenterList.put(experimenter, UUID.randomUUID().toString());

            final Property name = this.model.createProperty(String.join("", LKTLogToRDF.RDF_NS_FOAF, "name"));
            final Resource personRes = this.model.createResource(String.join("", LKTLogToRDF.RDF_NS_FOAF, "Person"));

            this.localRes(this.experimenterList.get(experimenter))
                    .addProperty(this.localProp("hasProvenance"), this.fetchLocalRes(provUUID))
                    .addProperty(RDF.type, this.localRes("Experimenter"))
                    .addLiteral(name, currEntry.getExperimenterName())
                            // TODO Check if this is actually correct or if the subclass
                            // TODO is supposed to be found only in the definition.
                    .addProperty(RDFS.subClassOf, personRes);
        }

        // Fetch experimenter resource
        final Resource experimenterRes = this.fetchLocalRes(this.experimenterList.get(experimenter));

        // Create current experiment resource
        final Resource exp = this.addExperimentEntry(currEntry);
        // Link current experiment to experimenter and animal log
        exp.addProperty(this.localProp("hasProvenance"), this.fetchLocalRes(provUUID))
                .addProperty(this.localProp("hasExperimenter"), experimenterRes)
                .addProperty(this.localProp("hasAnimal"), animal);

        projectRes.addProperty(this.localProp("hasExperiment"), exp);

        // Create current animalLog resource
        final Resource animalLogEntry = this.addAnimalLogEntry(currEntry);
        // Link animal log entry to experimenter
        animalLogEntry
                .addProperty(this.localProp("hasProvenance"), this.fetchLocalRes(provUUID))
                .addProperty(this.localProp("hasExperimenter"), experimenterRes);
        // Add animal log entry to the current animal node
        animal.addProperty(this.localProp("hasAnimalLogEntry"), animalLogEntry);
    }

    /**
     * Add Experiment node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created experiment node.
     */
    private Resource addExperimentEntry(final LKTLogParserEntry currEntry) {

        final Resource experiment = this.localRes(UUID.randomUUID().toString())
                .addProperty(RDF.type, this.localRes("Experiment"))
                .addLiteral(this.localProp("startedAt"), currEntry.getExperimentDate().toString())
                .addLiteral(RDFS.label, currEntry.getExperiment());

        RDFUtils.addNonEmptyLiteral(experiment, this.localProp("hasParadigm"), currEntry.getParadigm());
        RDFUtils.addNonEmptyLiteral(experiment, this.localProp("hasParadigmSpecifics"),
                currEntry.getParadigmSpecifics());
        RDFUtils.addNonEmptyLiteral(experiment, RDFS.comment, currEntry.getCommentExperiment());

        return experiment;
    }

    /**
     * Add AnimalLogEntry node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created AnimalLogEntryNode.
     */
    private Resource addAnimalLogEntry(final LKTLogParserEntry currEntry) {
        final Resource res = this.localRes(UUID.randomUUID().toString())
                .addProperty(RDF.type, this.localRes("AnimalLogEntry"))
                .addLiteral(this.localProp("startedAt"), currEntry.getExperimentDate().toString())
                .addLiteral(this.localProp("hasDiet"), currEntry.getIsOnDiet())
                .addLiteral(this.localProp("hasInitialWeightDate"), currEntry.getIsInitialWeight());

        if (currEntry.getWeight() != null) {
            final Resource weight = this.model.createResource()
                    .addLiteral(this.localProp("hasValue"), currEntry.getWeight())
                    .addLiteral(this.localProp("hasUnit"), "g");

            res.addProperty(this.localProp("hasWeight"), weight);
        }

        RDFUtils.addNonEmptyLiteral(res, RDFS.comment, currEntry.getCommentAnimal());
        RDFUtils.addNonEmptyLiteral(res, this.localProp("hasFeed"), currEntry.getFeed());

        return res;
    }

    /**
     * Convenience method for fetching an RDF resource from
     * an existing UUID.
     * @param fetchID ID corresponding to the required Resource.
     * @return Requested Resource.
     */
    private Resource fetchLocalRes(final String fetchID) {
        return this.model.getResource(
                String.join("", LKTLogToRDF.RDF_NS, fetchID)
        );
    }

    /**
     * Convenience method for creating an RDF resource with the
     * Namespace used by this crawler.
     * @param resName Contains the name of the resource.
     * @return The created RDF Resource.
     */
    private Resource localRes(final String resName) {
        return this.model.createResource(
                String.join("", LKTLogToRDF.RDF_NS, resName)
        );
    }

    /**
     * Convenience method for creating an RDF property with the
     * Namespace used by this crawler.
     * @param propName Contains the name of the property.
     * @return The created RDF Property.
     */
    private Property localProp(final String propName) {
        return this.model.createProperty(
                String.join("", LKTLogToRDF.RDF_NS, propName)
        );
    }

    /**
     * Convenience method for creating an RDF property with the
     * Dublin core Namespace.
     * @param propName Contains the name of the property.
     * @return The created RDF Property.
     */
    private Property localDCProp(final String propName) {
        return this.model.createProperty(
                String.join("", LKTLogToRDF.RDF_NS_DC, propName)
        );
    }

}
