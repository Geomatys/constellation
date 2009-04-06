/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.data;

// J2SE dependencies
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// JUnit dependencies
import java.io.OutputStream;
import org.junit.*;


/**
 * Initialize the data set of images into the temporary directory of the user.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public abstract class PostgridTestCase {
    /**
     * The temporary directory on a computer, according to the environment.
     */
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Initialize the image folder into the temporary directory, and copy in it the
     * image in resources, if it is not already present.
     *
     * @throws IOException
     */
    @BeforeClass
    public static void init() throws IOException {
        // Creation of the temporary directory for Constellation data if not already created.
        final File sstDir = new File(tmpDir, "Constellation/images/Monde/SST");
        final boolean fileNeedsToBeWritten = createDirectory(sstDir);

        // Write the image in resources into the temporary directory previously created.
        if (fileNeedsToBeWritten) {
            final String imageName = "SSTMDE200305.png";
            final InputStream in = PostgridTestCase.class.getResourceAsStream(imageName);
            final OutputStream os = new FileOutputStream(new File(sstDir, imageName));
            final byte buffer[] = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.close();
            in.close();
        }
    }

    /**
     * Creates a directory in the temporary folder if it is allowed by the writing permissions.
     *
     * @param dir One or several subdirectories to generate in the temporary folder.
     * @return True if it has created something, false if nothing has been written.
     *
     * @throws IOException if the writing permissions do not allow to create that folder.
     */
    private static boolean createDirectory(final File dir) throws IOException {
        if (!dir.exists()) {
            if (!tmpDir.canWrite()) {
                throw new IOException("Unable to write into the directory: "+ tmpDir.getPath() +
                                      ". Please check writing permissions.");
            } else {
                dir.mkdirs();
                return true;
            }
        }
        return false;
    }
}
