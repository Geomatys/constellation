/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.metadata.io.filesystem;

import java.io.File;
import org.apache.sis.util.logging.Logging;
import static org.constellation.metadata.CSWConstants.XML_EXT;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataUtils {

    /**
     * Try to find a file named identifier.xml or identifier recursively
     * in the specified directory and its sub-directories.
     *
     * @param identifier The metadata identifier.
     * @param directory The current directory to explore.
     * @return
     */
    public static File getFileFromIdentifier(final String identifier, final File directory) {
        if (directory == null) {
            return null;
        } else if (!directory.isDirectory()) {
            Logging.getLogger(FileMetadataUtils.class).warning(directory.getPath() + " is not a valid directory");
        }
        // 1) try to find the file in the current directory
        File metadataFile = new File (directory,  identifier + XML_EXT);
        // 2) trying without the extension
        if (!metadataFile.exists()) {
            metadataFile = new File (directory,  identifier);
        }
        // 3) trying by replacing ':' by '-' (for windows platform who don't accept ':' in file name)
        if (!metadataFile.exists()) {
            final String windowsIdentifier = identifier.replace(':', '-');
            metadataFile = new File (directory,  windowsIdentifier + XML_EXT);
        }

        if (metadataFile.exists()) {
            return metadataFile;
        } else {
            for (File child : directory.listFiles()) {
                if (child.isDirectory()) {
                    final File result = getFileFromIdentifier(identifier, child);
                    if (result != null && result.exists()) {
                        return result;
                    }
                }
            }
        }
        return null;
    }
}
