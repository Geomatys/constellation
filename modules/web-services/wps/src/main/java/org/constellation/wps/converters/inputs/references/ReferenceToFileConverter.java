/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.wps.converters.inputs.references;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.geotoolkit.util.converter.NonconvertibleObjectException;

/**
 * Implementation of ObjectConverter to convert a reference into a File.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class ReferenceToFileConverter extends AbstractInputConverter {

    private static ReferenceToFileConverter INSTANCE;

    private ReferenceToFileConverter() {
    }

    public static synchronized ReferenceToFileConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReferenceToFileConverter();
        }
        return INSTANCE;
    }

    @Override
    public Object convert(Map<String, Object> source) throws NonconvertibleObjectException {

        File file;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            final URL u = new URL((String) source.get(IN_HREF));
            final URLConnection uc = u.openConnection();
            final String contentType = uc.getContentType();
            final int contentLength = uc.getContentLength();

            final InputStream raw = uc.getInputStream();
            in = new BufferedInputStream(raw);

            // get filename from the path
            String filename = u.getFile();
            filename = filename.substring(filename.lastIndexOf('/') + 1);
            int dotPos = filename.lastIndexOf(".");
            int len = filename.length();
            String name = filename.substring(0, dotPos);
            String ext = filename.substring(dotPos + 1, len);

            //Create a temp file
            file = File.createTempFile(name, ext); //TODO create file in WPS temp directory
            out = new FileOutputStream(file);

            final byte[] data = new byte[contentLength];
            byte[] readData = new byte[1024];
            int i = in.read(readData);

            while (i != -1) {
                out.write(readData, 0, i);
                i = in.read(readData);
            }

            out.write(data);
            out.flush();

        } catch (MalformedURLException ex) {
            throw new NonconvertibleObjectException("Reference file invalid input : Malformed url", ex);
        } catch (IOException ex) {
            throw new NonconvertibleObjectException("Reference file invalid input : IO", ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ReferenceToFileConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return file;
    }
}