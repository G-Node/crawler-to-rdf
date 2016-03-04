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

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

/**
 * Main service class for opening data from and saving data to an RDF file.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class RDFService {
    /**
     * Map returning the RDF formats supported by this service.
     */
    public static final Map<String, RDFFormat> RDF_FORMAT_MAP =
            Collections.unmodifiableMap(new HashMap<String, RDFFormat>(3) {
                {
                    put("TTL", RDFFormat.TURTLE_PRETTY);
                    put("RDF/XML", RDFFormat.RDFXML);
                    put("NTRIPLES", RDFFormat.NTRIPLES);
                    put("JSON-LD", RDFFormat.JSONLD);
                }
            });

    /**
     * Map RDF formats to the correct file ending.
     */
    public static final Map<String, String> RDF_FORMAT_EXTENSION =
            Collections.unmodifiableMap(new HashMap<String, String>(3) {
                {
                    put("TTL", "ttl");
                    put("RDF/XML", "rdf");
                    put("NTRIPLES", "nt");
                    put("JSON-LD", "jsonld");
                }
            });

    /**
     * Query results of this RDFService can be saved to these file formats.
     * Map keys should always be upper case.
     * Map values correspond to the file extensions that will be used to save the query results.
     */
    public static final Map<String, String> QUERY_RESULT_FILE_FORMATS =
            Collections.unmodifiableMap(new HashMap<String, String>(0) {
                {
                    put("CSV", "csv");
                }
            });

    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(RDFService.class.getName());

    /**
     * Open an RDF file, load the data and return the RDF model. Method will not check,
     * if the file is actually a valid RDF file or if the file extension matches
     * the content of the file.
     * @param fileName Path and filename of a valid RDF file.
     * @return Model created from the data within the provided RDF file.
     */
    public static Model openModelFromFile(final String fileName) {
        return RDFDataMgr.loadModel(fileName);
    }

    /**
     * Write an RDF model to an output file using an RDF file format supported by this tool, specified
     * in {@link RDFService#RDF_FORMAT_MAP}.
     * This method will overwrite any files with the same path and filename.
     * @param fileName Path and filename of the output file.
     * @param model RDF model that's supposed to be written to the file.
     * @param format Output format of the RDF file.
     */
    public static void saveModelToFile(final String fileName, final Model model, final String format) {

        final File file = new File(fileName);

        try {
            final FileOutputStream fos = new FileOutputStream(file);
            RDFService.LOGGER.info(
                    String.join(
                            "", "Writing data to RDF file '", fileName, "' using format '", format, "'"
                    )
            );
            if (RDFService.RDF_FORMAT_MAP.containsKey(format)) {
                try {
                    RDFDataMgr.write(fos, model, RDFService.RDF_FORMAT_MAP.get(format));
                    fos.close();
                } catch (IOException ioExc) {
                    RDFService.LOGGER.error("Error closing file stream.");
                }
            } else {
                RDFService.LOGGER.error(
                        String.join("", "Error when saving output file: output format '",
                                format, "' is not supported.")
                );
            }
        } catch (FileNotFoundException exc) {
            RDFService.LOGGER.error(String.join("", "Could not open output file ", fileName));
        }
    }
    /**
     * Helper method saving an RDF model to a file in a specified RDF format.
     * This method will overwrite any files with the same path and filename.
     * @param m Model that's supposed to be saved.
     * @param fileName Path and Name of the output file.
     * @param format Specified {@link RDFFormat} of the output file.
     */
    public static void plainSaveModelToFile(final Model m, final String fileName, final RDFFormat format) {
        final File file = new File(fileName);

        try {
            final FileOutputStream fos = new FileOutputStream(file);
            try {
                RDFDataMgr.write(fos, m, format);
                fos.close();
            } catch (IOException ioExc) {
                ioExc.printStackTrace();
            }
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Helper method saving a JENA RDF {@link ResultSet} to an output file in a specified output format.
     * @param result JENA RDF {@link ResultSet} that will be saved.
     * @param resultFileFormat String containing a {@link #QUERY_RESULT_FILE_FORMATS} entry.
     * @param fileName String containing Path and Name of the file the results are written to.
     */
    public static void saveResultsToSupportedFile(final ResultSet result,
                                                  final String resultFileFormat,
                                                  final String fileName) {

        final String resFileFormat = resultFileFormat.toUpperCase(Locale.ENGLISH);

        if (QUERY_RESULT_FILE_FORMATS.containsKey(resFileFormat)) {

            final String fileExt = QUERY_RESULT_FILE_FORMATS.get(resFileFormat);
            final String outFile = !FileService.checkFileExtension(fileName, fileExt.toUpperCase(Locale.ENGLISH))
                    ? String.join("", fileName, ".", fileExt) : fileName;

            try {
                final File file = new File(outFile);

                if (!file.exists()) {
                    file.createNewFile();
                }

                final FileOutputStream fop = new FileOutputStream(file);

                RDFService.LOGGER.info(String.join("", "Write query to file...\t\t(", outFile, ")"));

                if ("CSV".equals(resFileFormat)) {
                    ResultSetFormatter.outputAsCSV(fop, result);
                }

                fop.flush();
                fop.close();

            } catch (IOException e) {
                RDFService.LOGGER.error(String.join("", "Cannot write to file...\t\t(", outFile, ")"));
                RDFService.LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        } else {
            RDFService.LOGGER.error(
                    String.join("", "Output file format ", resultFileFormat, " is not supported by this service.")
            );
        }
    }

}
