/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.util;

import org.apache.sis.util.Static;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableStyle;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class StyleEditionWorkspace extends Static {

    /**
     * Style XML reader/writer.
     */
    private static final StyleXmlIO STYLE_XML_IO = new StyleXmlIO();

    /**
     * Read the current edited {@link MutableStyle} from the workspace directory.
     *
     * @param providerId the provider id
     * @param styleName  the style name
     * @return the {@link MutableStyle} instance or {@code null}
     * @throws IOException if failed to parse the style from workspace directory
     */
    public static MutableStyle acquire(final String providerId, final String styleName) throws IOException {
        // Acquire or create the session workspace.
        final File workspace = new File(getPath());
        if (!workspace.exists()) {
            workspace.mkdirs();
        }

        // Try to find a style matching with specified arguments.
        final File[] files = workspace.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.equals(providerId + "_" + styleName + ".xml");
            }
        });
        if (files.length > 0) {
            try {
                return STYLE_XML_IO.readStyle(files[0], Specification.SymbologyEncoding.V_1_1_0);
            } catch (Exception ex) {
                throw new IOException("An error occurred when trying to read temporary style from workspace.", ex);
            }
        }
        return null; // should never happen
    }

    /**
     * Writes the specified {@link MutableStyle} in the workspace directory.
     *
     * @param providerId the provider id
     * @param styleName  the style name
     * @throws IOException if failed to write the style from workspace directory
     */
    public static void save(final String providerId, final String styleName, final MutableStyle style) throws IOException {
        try {
            final File file = new File(getPath() + "/" + providerId + "_" + styleName + ".xml");
            STYLE_XML_IO.writeStyle(file, style, Specification.StyledLayerDescriptor.V_1_1_0);
        } catch (JAXBException ex) {
            throw new IOException("The style marshalling has failed.", ex);
        }
    }

    /**
     * Gets the workspace directory path.
     *
     * @return the directory path
     */
    public static String getPath() {
        return System.getProperty("user.home") + "/.cstl-admin";
    }
}
