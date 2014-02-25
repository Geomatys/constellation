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
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import static org.constellation.metadata.CSWConstants.XML_EXT;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataUtils {

    private static final Logger LOGGER = Logging.getLogger(FileMetadataUtils.class);

    /**
     * Try to find a file named identifier.xml or identifier recursively
     * in the specified directory and its sub-directories.
     *
     * @param identifier The metadata identifier.
     * @param directory The current directory to explore.
     * @return
     */
    @Deprecated
    public static File getFileFromIdentifier(final String identifier, final File directory) {
        if (directory == null) {
            return null;
        }
        final Path path = Paths.get(directory.getPath());
        final Path result = getFileFromIdentifier(identifier, path);
        if (result != null) {
            return result.toFile();
        }
        return null;
    }

    public static Path getFileFromIdentifier(final String identifier, final Path directory) {
        if (directory == null) {
            return null;
        } else if (!Files.isDirectory(directory)) {
            Logging.getLogger(FileMetadataUtils.class).log(Level.WARNING, "{0} is not a valid directory", directory.toString());
        }
        // 1) try to find the file in the current directory
        
        Path metadataFile = directory.resolve(identifier + XML_EXT);
        // 2) trying without the extension
        if (!Files.exists(metadataFile)) {
            metadataFile = directory.resolve(identifier);
        }
        // 3) trying by replacing ':' by '-' (for windows platform who don't accept ':' in file name)
        if (!Files.exists(metadataFile)) {
            final String windowsIdentifier = identifier.replace(':', '-');
            metadataFile = directory.resolve(windowsIdentifier + XML_EXT);
        }

        if (Files.exists(metadataFile)) {
            return metadataFile;
        } else {
            final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path file) throws IOException {
                    return (Files.isDirectory(file));
                }
            };

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, filter)) {
                for (Path subdir : stream) {
                    final Path result = getFileFromIdentifier(identifier, subdir);
                    if (result != null && Files.exists(result)) {
                        return result;
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Erro while searching for sub-directory", e);
            }
        }
        return null;
    }
}
