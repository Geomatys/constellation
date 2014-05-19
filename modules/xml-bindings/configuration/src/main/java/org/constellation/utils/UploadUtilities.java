/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
