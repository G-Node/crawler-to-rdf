/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.commons;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Class contains small helper functions when dealing with Jena RDF models.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class RDFUtils {
    /**
     * Namespace used to identify RDF resources.
     */
    public static final String RDF_NS_RDF = RDF.getURI();
    /**
     * RDF namespace prefix.
     */
    public static final String RDF_NS_RDF_ABR = "rdf";
    /**
     * Namespace used to identify RDFS resources.
     */
    public static final String RDF_NS_RDFS = RDFS.getURI();
    /**
     * RDFS namespace prefix.
     */
    public static final String RDF_NS_RDFS_ABR = "rdfs";
    /**
     * Namespace used to identify XSD resources.
     */
    public static final String RDF_NS_XSD = XSD.getURI();
    /**
     * XSD namespace prefix.
     */
    public static final String RDF_NS_XSD_ABR = "xs";
    /**
     * Namespace used to identify FOAF RDF resources.
     */
    public static final String RDF_NS_FOAF = "http://xmlns.com/foaf/0.1/";
    /**
     * FOAF Namespace prefix.
     */
    public static final String RDF_NS_FOAF_ABR = "foaf";
    /**
     * Namespace used to identify Dublin core resources.
     */
    public static final String RDF_NS_DC = DCTerms.NS;
    /**
     * Dublin core Namespace prefix.
     */
    public static final String RDF_NS_DC_ABR = "dc";
    /**
     * Core Ontology for neuroscientific metadata defined by the G-Node.
      */
    public static final String RDF_NS_GN_ONT = "https://github.com/G-Node/neuro-ontology/";
    /**
     * G-Node ontology namespace prefix.
     */
    public static final String RDF_NS_GN_ONT_ABR = "gn";
    /**
     * Method adds an RDF Literal only to a Jena Resource, if the Literal String
     * actually contains a value.
     * @param res Jena Resource.
     * @param p Jena Property.
     * @param litStr String containing the value of the Literal.
     */
    public static void addNonEmptyLiteral(final Resource res, final Property p, final String litStr) {
        if (litStr != null && !litStr.isEmpty()) {
            res.addLiteral(p, litStr);
        }
    }
}
