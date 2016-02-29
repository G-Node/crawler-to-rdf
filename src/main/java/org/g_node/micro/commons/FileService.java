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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Main service class for dealing with files.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class FileService {
    /**
     * Method for validating that a file actually exists.
     * @param checkFile Path and filename of the provided file.
     * @return True if the file exists, false otherwise.
     */
    public static boolean checkFile(final String checkFile) {

        return Files.exists(Paths.get(checkFile))
                && Files.exists(Paths.get(checkFile).toAbsolutePath());

    }

    /**
     * Method for validating that the provided file is of a supported file extension.
     * @param checkFile Filename of the provided file.
     * @param fileExtensions List containing all supported file extensions.
     * @return True if the file ends with a supported file extension, false otherwise.
     */
    public static boolean checkFileExtension(final String checkFile, final List<String> fileExtensions) {

        boolean correctFileType = false;

        final int i = checkFile.lastIndexOf('.');
        if (i > 0) {
            final String checkExtension = checkFile.substring(i + 1);
            correctFileType = fileExtensions.contains(checkExtension.toUpperCase(Locale.ENGLISH));
        }

        return correctFileType;
    }

    /**
     * Method for validating that the provided file is of a supported file extension.
     * @param checkFile Filename of the provided file.
     * @param fileExtension String containing the supported file extension.
     * @return True if the file end with the supported file extension, false otherwise.
     */
    public static boolean checkFileExtension(final String checkFile, final String fileExtension) {

        boolean correctFileType = false;

        final int i = checkFile.lastIndexOf('.');
        if (i > 0) {
            final String checkExtension = checkFile.substring(i + 1);
            correctFileType = fileExtension.equals(checkExtension.toUpperCase(Locale.ENGLISH));
        }

        return correctFileType;
    }

    /**
     * Creates a backup file with a timestamp and the string "backup" in its name.
     * @param file Name of the file that is to be copied.
     * @param dateTimeFormatPattern Format of the timestamp, use DateTimeFormatter pattern conventions.
     * @return True if the file was successfully created, false, if something failed.
     */
    public static boolean createTimeStampBackupFile(final String file, final String dateTimeFormatPattern) {
        final Path mainPath = Paths.get(file);
        final String fileName = mainPath.getFileName().toString();
        final String ts = AppUtils.getTimeStamp(dateTimeFormatPattern);
        final String backupName = String.join("", ts, "_backup_", fileName);
        final String backupPath = mainPath.toString().replaceFirst(fileName, backupName);

        try {
            Files.copy(mainPath, Paths.get(backupPath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
