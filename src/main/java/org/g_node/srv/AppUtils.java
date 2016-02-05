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
import java.util.Locale;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Class providing various utility methods.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class AppUtils {
    /**
     * Method converts a List of Strings to upper case, joins the individual entries by a blank space
     * and retrieves a hexadecimal String from the resulting input String using the SHA-1 hash algorithm.
     * @param valueList List of Strings.
     * @return Hexadecimal String of the SHA-1 encoded input Strings.
     */
    public static String getHashSHA(final List<String> valueList) {
        final String collectListValues = valueList.stream()
                .map(s -> s.toUpperCase(Locale.ENGLISH))
                .collect(Collectors.joining(" "));

        return DigestUtils.shaHex(collectListValues);
    }
}
