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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.imageio.ImageReader;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.geotoolkit.image.io.XImageIO;
import org.geotoolkit.util.converter.NonconvertibleObjectException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class ReferenceToRenderedImage extends AbstractInputConverter {
    
    private static ReferenceToRenderedImage INSTANCE;

    private ReferenceToRenderedImage(){}

    public static synchronized ReferenceToRenderedImage getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToRenderedImage();
        }
        return INSTANCE;
    }

    @Override
    public Object convert(final Map<String, Object> source) throws NonconvertibleObjectException {

        final String href = (String) source.get(IN_HREF);
        
        try {
            final URL url = new URL(href);
            final ImageReader imageReader =  XImageIO.getReader(url, true, true);
            //read the first image.
            return imageReader.read(0);
        } catch (MalformedURLException ex) {
            throw new NonconvertibleObjectException("Reference image invalid URL : Malformed url",ex);
        } catch (IOException ex) {
            throw new NonconvertibleObjectException("Reference image invalid input : IO",ex);
        }
    }
    
}
