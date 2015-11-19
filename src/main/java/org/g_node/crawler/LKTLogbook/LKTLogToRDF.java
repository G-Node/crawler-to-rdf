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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
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
     * Namespace used to identify RDF resources and properties specific for the current use case.
     */
    private static final String RDF_NS_LKT =  "https://orcid.org/0000-0003-4857-1083#";
    /**
     * Namespace prefix for all instances of the LKT use case.
     */
    private static final String RDF_NS_LKT_ABR = "lkt";
    /**
     * Map containing all projects with their newly created UUIDs of the parsed ODS sheet.
     */
    private Map<String, String> projectList;
    /**
     * Map containing all the subjectIDs with their newly created UUIDs of the parsed ODS sheet.
     */
    private Map<String, String> subjectList;
    /**
     * Map containing all the experimenters with their newly created UUIDs contained in the parsed ODS sheet.
     */
    private Map<String, String> experimenterList;
    /**
     * Main RDF model containing all the parsed information from the ODS sheet.
     */
    private Model model;

    /**
     * Constructor.
     */
    public LKTLogToRDF() {
        this.projectList = new HashMap<>();
        this.subjectList = new HashMap<>();
        this.experimenterList = new HashMap<>();
        this.model = ModelFactory.createDefaultModel();

        this.model.setNsPrefix(RDFUtils.RDF_NS_RDF_ABR, RDFUtils.RDF_NS_RDF);
        this.model.setNsPrefix(RDFUtils.RDF_NS_RDFS_ABR, RDFUtils.RDF_NS_RDFS);
        this.model.setNsPrefix(RDFUtils.RDF_NS_XSD_ABR, RDFUtils.RDF_NS_XSD);
        this.model.setNsPrefix(RDFUtils.RDF_NS_FOAF_ABR, RDFUtils.RDF_NS_FOAF);
        this.model.setNsPrefix(RDFUtils.RDF_NS_DC_ABR, RDFUtils.RDF_NS_DC);
        this.model.setNsPrefix(RDFUtils.RDF_NS_GN_ONT_ABR, RDFUtils.RDF_NS_GN_ONT);
        this.model.setNsPrefix(LKTLogToRDF.RDF_NS_LKT_ABR, LKTLogToRDF.RDF_NS_LKT);
    }

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

        final String provUUID = UUID.randomUUID().toString();

        this.createInst(provUUID)
                .addProperty(RDF.type, this.mainRes("Provenance"))
                .addLiteral(DCTerms.source, inputFile)
                .addLiteral(DCTerms.created,
                        this.mainTypedLiteral(
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                XSDDatatype.XSDdateTime)
                )
                .addLiteral(DCTerms.subject,
                        "This RDF file was created by parsing data from the file indicated in the source literal");

        allSheets.stream().forEach(a -> this.addSubject(a, provUUID));

        RDFService.writeModelToFile(outputFile, this.model, outputFormat);
    }

    /**
     * Adds all data of a parsed ODS sheet to the main RDF model. The problem here is
     * that the ODS sheet is centered around the animal, while the RDF model is project
     * centric.
     * @param currSheet Data from the current sheet.
     * @param provUUID UUID of the provenance resource.
     */
    private void addSubject(final LKTLogParserSheet currSheet, final String provUUID) {

        final String subjectID = currSheet.getSubjectID();
        if (!this.subjectList.containsKey(subjectID)) {
            this.subjectList.put(subjectID, UUID.randomUUID().toString());
        }

        final Resource permit = this.createInst(UUID.randomUUID().toString())
                .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provUUID))
                .addProperty(RDF.type, this.mainRes("Permit"))
                .addLiteral(this.mainProp("hasNumber"), currSheet.getPermitNumber());

        final Resource subject = this.createInst(this.subjectList.get(subjectID))
                .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provUUID))
                .addProperty(RDF.type, this.mainRes("Subject"))
                .addLiteral(this.mainProp("hasSubjectID"), subjectID)
                .addLiteral(this.mainProp("hasSex"), currSheet.getSubjectSex())
                .addLiteral(
                        this.mainProp("hasBirthDate"),
                        this.mainTypedLiteral(currSheet.getDateOfBirth().toString(), XSDDatatype.XSDdate))
                .addLiteral(
                        this.mainProp("hasWithdrawalDate"),
                        this.mainTypedLiteral(currSheet.getDateOfWithdrawal().toString(), XSDDatatype.XSDdate))
                .addProperty(this.mainProp("hasPermit"), permit);

        RDFUtils.addNonEmptyLiteral(subject, this.mainProp("hasSpeciesName"), currSheet.getSpecies());
        RDFUtils.addNonEmptyLiteral(subject, this.mainProp("hasScientificName"), currSheet.getScientificName());

        currSheet.getEntries().stream().forEach(
                c -> this.addEntry(c, subject, provUUID)
        );
    }

    /**
     * Adds the data of the current ODS entry to the main RDF model.
     * @param currEntry Data from the current ODS line entry.
     * @param subject Resource from the main RDF model containing the information about
     *  the animal this entry is associated with.
     * @param provUUID UUID of the provenance resource.
     */
    private void addEntry(final LKTLogParserEntry currEntry, final Resource subject, final String provUUID) {

        final String project = currEntry.getProject();
        // add project only once to the rdf model
        if (!this.projectList.containsKey(project)) {

            this.projectList.put(project, UUID.randomUUID().toString());
            this.createInst(this.projectList.get(project))
                    .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provUUID))
                    .addProperty(RDF.type, this.mainRes("Project"))
                    .addLiteral(RDFS.label, project);
        }
        // Fetch project resource
        final Resource projectRes = this.fetchInstance(this.projectList.get(project));

        // Add experimenter only once to the RDF model
        final String experimenter = currEntry.getExperimenterName();
        if (!this.experimenterList.containsKey(experimenter)) {

            this.experimenterList.put(experimenter, UUID.randomUUID().toString());

            final Property name = this.model.createProperty(String.join("", RDFUtils.RDF_NS_FOAF, "name"));
            final Resource personRes = this.model.createResource(String.join("", RDFUtils.RDF_NS_FOAF, "Person"));

            this.createInst(this.experimenterList.get(experimenter))
                    .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provUUID))
                    .addProperty(RDF.type, this.mainRes("Experimenter"))
                    .addLiteral(name, currEntry.getExperimenterName())
                            // TODO Check if this is actually correct or if the subclass
                            // TODO is supposed to be found only in the definition.
                    .addProperty(RDFS.subClassOf, personRes);
        }

        // Fetch experimenter resource
        final Resource experimenterRes = this.fetchInstance(this.experimenterList.get(experimenter));

        // Create current experiment resource
        final Resource exp = this.addExperimentEntry(currEntry);
        // Link current experiment to experimenter and subject log
        exp.addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provUUID))
                .addProperty(this.mainProp("hasExperimenter"), experimenterRes)
                .addProperty(this.mainProp("hasSubject"), subject);

        projectRes.addProperty(this.mainProp("hasExperiment"), exp);

        // Create current subjectLog resource
        final Resource subjectLogEntry = this.addSubjectLogEntry(currEntry);
        // Link subject log entry to experimenter
        subjectLogEntry
                .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provUUID))
                .addProperty(this.mainProp("hasExperimenter"), experimenterRes);
        // Add subject log entry to the current subject node
        subject.addProperty(this.mainProp("hasSubjectLogEntry"), subjectLogEntry);
    }

    /**
     * Add Experiment node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created experiment node.
     */
    private Resource addExperimentEntry(final LKTLogParserEntry currEntry) {

        final Resource experiment = this.createInst(UUID.randomUUID().toString())
                .addProperty(RDF.type, this.mainRes("Experiment"))
                .addLiteral(
                        this.mainProp("startedAt"),
                        this.mainTypedLiteral(
                                currEntry.getExperimentDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                XSDDatatype.XSDdateTime)
                        )
                .addLiteral(RDFS.label, currEntry.getExperiment());

        RDFUtils.addNonEmptyLiteral(experiment, this.mainProp("hasParadigm"), currEntry.getParadigm());
        RDFUtils.addNonEmptyLiteral(experiment, this.mainProp("hasParadigmSpecifics"),
                currEntry.getParadigmSpecifics());
        RDFUtils.addNonEmptyLiteral(experiment, RDFS.comment, currEntry.getCommentExperiment());

        return experiment;
    }

    /**
     * Add SubjectLogEntry node to the RDF model.
     * @param currEntry Current entry line of the parsed ODS file.
     * @return Created SubjectLogEntry node.
     */
    private Resource addSubjectLogEntry(final LKTLogParserEntry currEntry) {
        final Resource res = this.createInst(UUID.randomUUID().toString())
                .addProperty(RDF.type, this.mainRes("SubjectLogEntry"))
                .addLiteral(
                        this.mainProp("startedAt"),
                        this.mainTypedLiteral(
                                currEntry.getExperimentDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                XSDDatatype.XSDdateTime)
                        )
                .addLiteral(
                        this.mainProp("hasDiet"),
                        this.mainTypedLiteral(currEntry.getIsOnDiet().toString(), XSDDatatype.XSDboolean))
                .addLiteral(
                        this.mainProp("hasInitialWeightDate"),
                        this.mainTypedLiteral(currEntry.getIsInitialWeight().toString(), XSDDatatype.XSDboolean)
                );

        if (currEntry.getWeight() != null) {
            final Resource weight = this.model.createResource()
                    .addLiteral(this.mainProp("hasValue"), currEntry.getWeight())
                    .addLiteral(this.mainProp("hasUnit"), "g");

            res.addProperty(this.mainProp("hasWeight"), weight);
        }

        RDFUtils.addNonEmptyLiteral(res, RDFS.comment, currEntry.getCommentSubject());
        RDFUtils.addNonEmptyLiteral(res, this.mainProp("hasFeed"), currEntry.getFeed());

        return res;
    }

    /**
     * Convenience method for fetching an RDF resource from
     * an existing UUID.
     * @param fetchID ID corresponding to the required Resource.
     * @return Requested Resource.
     */
    private Resource fetchInstance(final String fetchID) {
        return this.model.getResource(
                String.join("", LKTLogToRDF.RDF_NS_LKT, fetchID)
        );
    }

    /**
     * Convenience method for creating an RDF resource with the
     * Namespace used for instances used by this crawler.
     * @param resName Contains the name of the resource.
     * @return The created RDF Resource.
     */
    private Resource createInst(final String resName) {
        return this.model.createResource(
                String.join("", LKTLogToRDF.RDF_NS_LKT, resName)
        );
    }

    /**
     * Convenience method for creating an RDF resource with the
     * Namespace of the G-Node ontology to define Classes and Properties.
     * @param resName Contains the name of the resource.
     * @return The created RDF Resource.
     */
    private Resource mainRes(final String resName) {
        return this.model.createResource(
                String.join("", RDFUtils.RDF_NS_GN_ONT, resName)
        );
    }

    /**
     * Convenience method for creating an RDF property with the
     * Namespace of the G-Node ontology.
     * @param propName Contains the name of the property.
     * @return The created RDF Property.
     */
    private Property mainProp(final String propName) {
        return this.model.createProperty(
                String.join("", RDFUtils.RDF_NS_GN_ONT, propName)
        );
    }

    /**
     * Convenience method for creating a typed literal with the
     * Namespace of the G-Node ontology.
     * @param litVal Contains the value of the Literal.
     * @param litType Contains the RDFDatatype of the returned Literal.
     * @return The created typed Literal.
     */
    private Literal mainTypedLiteral(final String litVal, final RDFDatatype litType) {
        return this.model.createTypedLiteral(litVal, litType);
    }
}
