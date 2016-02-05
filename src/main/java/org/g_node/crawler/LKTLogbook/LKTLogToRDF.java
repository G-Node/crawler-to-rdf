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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.g_node.srv.AppUtils;
import org.g_node.srv.RDFService;
import org.g_node.srv.RDFUtils;

/**
 * Class converting parsed data to RDF.
 */
public final class LKTLogToRDF {
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
     * Adds all data from a parsed ODS sheet to an RDF model and writes the results to a designated output file.
     * Data specific notes:
     * This method creates an RDF Provenance class instance, using a hash ID as identifier. This hash ID is
     * created using the filename of the input file and the current date (XSDDateTime format).
     * The hash ID is created using the {@link AppUtils#getHashSHA} method.
     * @param allSheets Data from the parsed ODS sheets.
     * @param inputFile Name and path of the input file
     * @param outputFile Name and path of the designated output file.
     * @param outputFormat RDF output format.
     */
    public void createRDFModel(final ArrayList<LKTLogParserSheet> allSheets, final String inputFile,
                                final String outputFile, final String outputFormat) {

        final String provDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final String provID = AppUtils.getHashSHA(new ArrayList<>(Arrays.asList(inputFile, provDateTime)));

        this.createInst(provID)
                .addProperty(RDF.type, this.mainRes("Provenance"))
                .addLiteral(DCTerms.source, inputFile)
                .addLiteral(DCTerms.created,
                        this.mainTypedLiteral(provDateTime,
                                XSDDatatype.XSDdateTime)
                )
                .addLiteral(DCTerms.subject,
                        "This RDF file was created by parsing data from the file indicated in the source literal");

        allSheets.stream().forEach(a -> this.addSubject(a, provID));

        RDFService.writeModelToFile(outputFile, this.model, outputFormat);
    }

    /**
     * The method adds data of a parsed ODS sheet to the main RDF model.
     * Data specific notes:
     * This method creates an RDF Subject class instance, using a hash ID as identifier. This hash ID is
     * created using the current sheets values (in this order) of scientific name, subject ID,
     * date of birth (XSDDate format).
     * The method creates an RDF Permit class instance, using a hash ID as identifier. The hash ID is created
     * using the permit number parsed from the current sheet.
     * The hash IDs are created using the {@link AppUtils#getHashSHA} method.
     * @param currSheet Data from the current sheet.
     * @param provID ID of the current provenance resource.
     */
    private void addSubject(final LKTLogParserSheet currSheet, final String provID) {

        final String subjectID = currSheet.getSubjectID();
        if (!this.subjectList.containsKey(subjectID)) {

            // TODO A problem with this key could be different writing styles of the scientific name.
            final ArrayList<String> subjListID = new ArrayList<>(
                    Arrays.asList(currSheet.getScientificName(),
                            currSheet.getSubjectID(),
                            currSheet.getDateOfBirth().toString()));

            this.subjectList.put(subjectID, AppUtils.getHashSHA(subjListID));
        }

        final String permitHashID = AppUtils.getHashSHA(Collections.singletonList(currSheet.getPermitNumber()));

        final Resource permit = this.createInst(permitHashID)
                .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provID))
                .addProperty(RDF.type, this.mainRes("Permit"))
                .addLiteral(this.mainProp("hasNumber"), currSheet.getPermitNumber());

        final Resource subject = this.createInst(this.subjectList.get(subjectID))
                .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provID))
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
                c -> this.addEntry(c, subject, provID, subjectID)
        );
    }

    /**
     * The method adds the data of a parsed ODS row to the main RDF model.
     * Data specific notes:
     * This method creates an RDF Project class instance, using a hash ID as identifier. This hash ID is
     * created using the provided project name value of the current entry.
     * The method creates an RDF Experimenter class instance, using a hash ID as identifier. The hash ID is created
     * using the full name of a person parsed from the current entry.
     * The hash IDs are created using the {@link AppUtils#getHashSHA} method.
     * @param currEntry Data from the current ODS line entry.
     * @param subject Resource from the main RDF model containing the information about
     *  the test subject this entry is associated with.
     * @param provID ID of the current provenance resource.
     * @param subjectID Plain ID of the current test subject the experiment is connected to. This ID is required
     *                  to create the Hash IDs for Experiment and SubjectLogEntry instances.
     */
    private void addEntry(final LKTLogParserEntry currEntry, final Resource subject,
                          final String provID, final String subjectID) {

        final String project = currEntry.getProject();
        // Add RDF Project instance only once to the RDF model.
        if (!this.projectList.containsKey(project)) {

            final String projectHashID = AppUtils.getHashSHA(Collections.singletonList(project));
            this.projectList.put(project, projectHashID);

            this.createInst(projectHashID)
                    .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provID))
                    .addProperty(RDF.type, this.mainRes("Project"))
                    .addLiteral(RDFS.label, project);
        }
        // Fetch RDF Project instance.
        final Resource projectRes = this.fetchInstance(this.projectList.get(project));

        // Add new RDF Experimenter instance only once to the RDF model.
        final String experimenter = currEntry.getExperimenterName();
        if (!this.experimenterList.containsKey(experimenter)) {

            // TODO Problem: This will be the name as entered,
            // TODO there is no control over the order of first and last name.
            this.experimenterList.put(experimenter, AppUtils.getHashSHA(
                                            Collections.singletonList(experimenter)));

            final Property name = this.model.createProperty(String.join("", RDFUtils.RDF_NS_FOAF, "name"));
            final Resource personRes = this.model.createResource(String.join("", RDFUtils.RDF_NS_FOAF, "Person"));

            this.createInst(this.experimenterList.get(experimenter))
                    .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provID))
                    .addProperty(RDF.type, this.mainRes("Experimenter"))
                    .addLiteral(name, experimenter)
                            // TODO Check if this is actually correct or if the subclass
                            // TODO is supposed to be found only in the definition.
                    .addProperty(RDFS.subClassOf, personRes);
        }

        // Fetch RDF Experimenter instance.
        final Resource experimenterRes = this.fetchInstance(this.experimenterList.get(experimenter));

        // Create current RDF Experiment instance.
        final Resource exp = this.addExperimentEntry(currEntry, subjectID);
        // Link current RDF Experiment instance to RDF Experimenter instance and RDF SubjectLogEntry instance.
        exp.addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provID))
                .addProperty(this.mainProp("hasExperimenter"), experimenterRes)
                .addProperty(this.mainProp("hasSubject"), subject);

        projectRes.addProperty(this.mainProp("hasExperiment"), exp);

        // Create current RDF SubjectLogEntry instance.
        final Resource subjectLogEntry = this.addSubjectLogEntry(currEntry, subjectID);
        // Link RDF SubjectLogEntry instance to RDF Experimenter instance.
        subjectLogEntry
                .addProperty(this.mainProp("hasProvenance"), this.fetchInstance(provID))
                .addProperty(this.mainProp("hasExperimenter"), experimenterRes);
        // Add RDF SubjectLogEntry instance to the current RDF Subject instance.
        subject.addProperty(this.mainProp("hasSubjectLogEntry"), subjectLogEntry);
    }

    /**
     * Create RDF Experiment instance. A hash ID is created as identifier for this instance
     * using the {@link AppUtils#getHashSHA} method.
     * The Hash ID of an RDF Experiment instance uses primary key: SubjectID, Paradigm, Experiment,
     *      Experimenter, DateTime (XSDDateTime format)
     * Rational: an experimenter could potentially run two different experiments at the same time with the same
     * subject, but not within the same paradigm.
     * @param currEntry Current entry line of the parsed ODS file.
     * @param subjectID Plain ID of the current test subject the experiment is connected to.
     * @return Created Experiment instance.
     */
    private Resource addExperimentEntry(final LKTLogParserEntry currEntry, final String subjectID) {

        final String expDate = currEntry.getExperimentDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final ArrayList<String> experimentList = new ArrayList<>(Arrays.asList(
                subjectID, currEntry.getParadigm(), currEntry.getExperiment(),
                currEntry.getExperimenterName(), expDate));

        final Resource experiment = this.createInst(AppUtils.getHashSHA(experimentList))
                .addProperty(RDF.type, this.mainRes("Experiment"))
                .addLiteral(
                        this.mainProp("startedAt"),
                        this.mainTypedLiteral(expDate, XSDDatatype.XSDdateTime)
                )
                .addLiteral(RDFS.label, currEntry.getExperiment());

        RDFUtils.addNonEmptyLiteral(experiment, this.mainProp("hasParadigm"), currEntry.getParadigm());
        RDFUtils.addNonEmptyLiteral(experiment, this.mainProp("hasParadigmSpecifics"),
                currEntry.getParadigmSpecifics());
        RDFUtils.addNonEmptyLiteral(experiment, RDFS.comment, currEntry.getCommentExperiment());

        return experiment;
    }

    /**
     * Create RDF SubjectLogEntry instance. A hash ID is created as identifier for this instance
     * using the {@link AppUtils#getHashSHA} method.
     * Hash ID of an RDF SubjectLogEntry instance uses primary key:
     *      SubjectID, Experimenter, DateTime (XSDDateTime format)
     * Problem: same as with the Hash ID created for Experimenter, the name used here has to be in the
     * proper order and of the same spelling to create the same Hash ID.
     * Rational: there can only be one SubjectLogEntry at a particular point in time.
     * @param currEntry Current entry line of the parsed ODS file.
     * @param subjectID Plain ID of the current test subject the experiment is connected to.
     * @return Created SubjectLogEntry instance.
     */
    private Resource addSubjectLogEntry(final LKTLogParserEntry currEntry, final String subjectID) {

        final String logDate = currEntry.getExperimentDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final ArrayList<String> subjLogList =
                new ArrayList<>(Arrays.asList(subjectID, currEntry.getExperimenterName(), logDate));

        final Resource res = this.createInst(AppUtils.getHashSHA(subjLogList))
                .addProperty(RDF.type, this.mainRes("SubjectLogEntry"))
                .addLiteral(
                        this.mainProp("startedAt"),
                        this.mainTypedLiteral(logDate, XSDDatatype.XSDdateTime)
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
