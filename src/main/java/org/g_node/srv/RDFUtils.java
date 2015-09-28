/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.srv;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Class contains small helper functions when dealing with Jena RDF models.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class RDFUtils {

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
