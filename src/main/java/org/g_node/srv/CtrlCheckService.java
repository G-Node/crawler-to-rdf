/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.srv;

import java.util.Locale;
import java.util.Set;
import org.apache.jena.riot.RiotException;
import org.apache.log4j.Logger;
import org.g_node.micro.commons.FileService;
import org.g_node.micro.commons.RDFService;

/**
 * Class providing checks common to tools implemented in this service.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
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
    public static boolean isExistingFile(final String inputFile) {

        CtrlCheckService.LOGGER.info(
                String.join("", "Checking file '", inputFile, "'...")
        );
        if (!FileService.checkFile(inputFile)) {
            CtrlCheckService.LOGGER.error(
                    String.join("", "Input file ", inputFile, " does not exist.")
            );
            return false;
        }
        return true;
    }

    /**
     * Method if the provided file is within the list of provided file extensions.
     * @param inputFile Path and filename of the file that is supposed to be checked for the supported file type.
     * @param checkExtension Set containing all supported file types for the provided input file.
     * @return True in case the file type is supported, false in case it is not.
     */
    public static boolean isSupportedInFileType(final String inputFile, final Set<String> checkExtension) {

        CtrlCheckService.LOGGER.info("Checking input format...");
        if (!FileService.checkFileExtension(inputFile, checkExtension)) {
            CtrlCheckService.LOGGER.error(
                    String.join("",
                            "Input RDF file ", inputFile, " cannot be read.",
                            "\n\tOnly the following file formats are supported: \n\t",
                            checkExtension.toString()
                    )
            );
            return false;
        }
        return true;
    }

    /**
     * Method checks with the contents of a set, if a provided output file format is supported by the current tool
     * and logs the results of the check.
     * @param outputFormat Format provided by the user.
     * @param supportedOutFormats Set of supported output formats by this tool, the entries are all upper case.
     * @return True if the provided format is supported by the tool, false if not.
     */
    public static boolean isSupportedOutputFormat(final String outputFormat,
                                                  final Set<String> supportedOutFormats) {
        CtrlCheckService.LOGGER.info(String.join("", "Checking output format...\t\t(", outputFormat, ")"));
        if (!supportedOutFormats.contains(outputFormat.toUpperCase(Locale.ENGLISH))) {
            CtrlCheckService.LOGGER.error(
                    String.join("",
                            "Unsupported output format: '", outputFormat, "'",
                            "\n\t\tPlease use one of the following: ", supportedOutFormats.toString())
            );
            return false;
        }
        return true;
    }

    /**
     * Method checks if the upper case of a String value is contained within a set of String values.
     * @param cliArgValue Input value that is checked against a set of values.
     * @param argValSet Set of supported values.
     * @param cliArgDesc Description of the checked CLI argument value; required for proper logging
     *                   the details of the check.
     * @return True if the input value is contained within the set, false if not.
     */
    public static boolean isSupportedCliArgValue(final String cliArgValue,
                                                 final Set<String> argValSet,
                                                 final String cliArgDesc) {
        CtrlCheckService.LOGGER.info(String.join("", "Checking value of command line option '", cliArgDesc, "'..."));
        if (!argValSet.contains(cliArgValue.toUpperCase(Locale.ENGLISH))) {
            CtrlCheckService.LOGGER.error(
                    String.join("",
                            "'", cliArgValue, "' is not a supported value of command line option '", cliArgDesc, "'.",
                            "\n\t\t Please use one of the following: ",
                            String.join(" ", argValSet)
                    )
            );
            return false;
        }
        return true;
    }

    /**
     * Method tries to open a supported file assuming, that it is an RDF file.
     * If a RiotException occurs it most likely is not a valid RDF file and a
     * corresponding message is displayed and logged.
     * NOTE: If the input file has a file extension, that is not supported by
     * Apache Jena e.g. a file with the file ending "txt", the file will not be
     * closed properly after the RiotException has been raised. Only after the
     * program is closed, the file will be available again. Maybe this issue will be
     * resolved in a later Apache Jena version.
     * @param file Path and filename of the file that is supposed to be checked.
     * @return True if the file can be opened and the model loaded, false if not.
     */
    public static boolean isValidRdfFile(final String file) {
        try {
            RDFService.openModelFromFile(file);
        } catch (RiotException e) {
            CtrlCheckService.LOGGER.error(
                    String.join("",
                            "Failed to load file '", file, "'. Ensure it is a valid RDF file.",
                            "\n\t\tActual error message: ", e.getMessage())
            );
            return false;
        }
        return true;
    }

}
