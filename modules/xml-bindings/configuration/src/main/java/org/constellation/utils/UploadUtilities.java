/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class used for Upload
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public class UploadUtilities {

    /**
     * Write file with http input stream reveived
     *
     * @param uploadedInputStream  stream from client
     * @param uploadedFileLocation folder path to save file
     * @param uploadedFileName     file name
     * @return {@link java.io.File} saved
     * @throws java.io.IOException
     */
    public static File writeToFile(final InputStream uploadedInputStream,
                                   final String uploadedFileLocation, String uploadedFileName) throws IOException {

        if (uploadedInputStream != null) {

            // create spécific directory for data
            File folder = new File(uploadedFileLocation);
            if (!folder.exists()) {
                folder.mkdir();
            }

            int read;
            byte[] bytes = new byte[4096];

            File file = new File(uploadedFileName);
            final OutputStream out = new FileOutputStream(file);
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();

            return file;
        }
        return null;
    }
}
