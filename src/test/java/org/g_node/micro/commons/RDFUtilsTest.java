/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.commons;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * Unit tests for the {@link RDFUtils} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class RDFUtilsTest {

    /**
     * Method tests that a only a nonempty {@link Literal} is added to a Jena {@link Resource}.
     */
    @Test
    public void testAddNonEmptyLiteral() {
        final Model m = ModelFactory.createDefaultModel();
        final Resource r = m.createResource("testResource");
        final Property p = RDFS.comment;
        final String s = "testComment";

        assertThat(r.hasProperty(RDFS.comment)).isFalse();

        RDFUtils.addNonEmptyLiteral(r, p, null);
        assertThat(r.hasProperty(p)).isFalse();
        assertThat(r.listProperties().toList().size()).isEqualTo(0);

        RDFUtils.addNonEmptyLiteral(r, p, "");
        assertThat(r.hasProperty(p)).isFalse();
        assertThat(r.listProperties().toList().size()).isEqualTo(0);

        RDFUtils.addNonEmptyLiteral(r, p, s);
        assertThat(r.hasProperty(RDFS.comment)).isTrue();
        assertThat(r.listProperties().toList().size()).isEqualTo(1);
        assertThat(r.getProperty(RDFS.comment).getLiteral().toString())
                .isEqualTo(String.join("", s, "^^http://www.w3.org/2001/XMLSchema#string"));
    }

}
