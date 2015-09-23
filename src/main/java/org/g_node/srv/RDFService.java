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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Main service class for saving data to an RDF file.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class RDFService {

    /**
     * Write an rdf model to an output file. This method will overwrite any
     * files with the same path and filename.
     * @param fileName Path and filename of the output file.
     * @param model RDF model that's supposed to be written to the file.
     * @param format Output format of the RDF file.
     */
    public static void writeModelToFile(final String fileName, final Model model, final String format) {

        final File file = new File(fileName);
        final RDFWriter writer = model.getWriter(format);

        try {
            final FileOutputStream fos = new FileOutputStream(file);
            System.out.println(
                    String.join(
                            "", "[Info] Writing data to RDF file, ",
                            fileName, " using format '", format, "'"
                    )
            );
            try {
                writer.write(model, fos, format);
                fos.close();
            } catch (IOException ioExc) {
                System.err.println("[Error] while closing file stream.");
            }
        } catch (FileNotFoundException exc) {
            System.err.println(String.join(
                    "", "[Error] Could not open output file ", fileName));
        }
    }
}
