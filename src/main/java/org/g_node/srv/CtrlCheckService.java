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

import java.util.List;

import org.apache.log4j.Logger;
import org.g_node.micro.commons.FileService;
import org.g_node.micro.commons.RDFService;

/**
 * Class providing checks common to converters and crawlers implemented in this service.
 */
public final class CtrlCheckService {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(CtrlCheckService.class.getName());
    /**
     * Method checks if a provided file exists, logs the findings accordingly and returns corresponding boolean value.
     * @param inputFile Path and filename of the file that is supposed to be checked for existence.
     * @return True in case the file exists, false in case it does not.
     */
    public static boolean existingInputFile(final String inputFile) {

        boolean properFile = true;
        CtrlCheckService.LOGGER.info("Checking input file...");
        if (!FileService.checkFile(inputFile)) {
            CtrlCheckService.LOGGER.error(
                    String.join("", "Input file ", inputFile, " does not exist.")
            );
            properFile = false;
        }
        return properFile;
    }

    /**
     * Method if the provided file is within the list of provided file extensions.
     * @param inputFile Path and filename of the file that is supposed to be checked for the supported file type.
     * @param checkExtension List containing all supported file types for the provided input file.
     * @return True in case the file type is supported, false in case it is not.
     */
    public static boolean supportedInFileType(final String inputFile, final List<String> checkExtension) {

        boolean supportedType = true;
        CtrlCheckService.LOGGER.info("Checking input format...");
        if (!FileService.checkFileType(inputFile, checkExtension)) {
            CtrlCheckService.LOGGER.error(
                    String.join("",
                            "Input RDF file ", inputFile, " cannot be read.",
                            "\n\tOnly the following file formats are supported: \n\t",
                            checkExtension.toString()
                    )
            );
            supportedType = false;
        }
        return supportedType;
    }

    /**
     * Method checks with {@link RDFService#RDF_FORMAT_MAP} if a user provided
     * RDF output format is supported by the current tool.
     * @param outputFormat RDF format provided by the user.
     * @return True if the provided format is supported by the tool, false if not.
     */
    public static boolean supportedOutputFormat(final String outputFormat) {

        boolean supportedFormat = true;
        CtrlCheckService.LOGGER.info("Checking output format...");
        if (!RDFService.RDF_FORMAT_MAP.containsKey(outputFormat)) {
            CtrlCheckService.LOGGER.error(
                    String.join("",
                            "Unsupported output format: '", outputFormat, "'",
                            "\n Please use one of the following: ",
                            RDFService.RDF_FORMAT_MAP.keySet().toString()
                    )
            );
            supportedFormat = false;
        }
        return supportedFormat;
    }

}
