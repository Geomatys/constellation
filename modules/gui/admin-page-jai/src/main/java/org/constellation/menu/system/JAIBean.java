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

package org.constellation.menu.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.media.jai.JAI;
import org.geotoolkit.util.logging.Logging;

/**
 * Returns several information of available imagery readers and writers.
 *
 * @author Johann Sorel (Geomatys)
 */
public class JAIBean {

    private static final Logger LOGGER = Logging.getLogger(JAIBean.class);

    private final Map<String,String> types;

    public JAIBean(){
        types = new TreeMap<String, String>();
        for(String mime : ImageIO.getReaderMIMETypes()){
            try{
                final Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mime);
                if(readers.hasNext()){
                    types.put(mime, readers.next().getClass().getName());
                }
            }catch(Exception ex){
                //might happen, jai or other problems with native codecs
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage());
            }
        }
    }

    public String getTileCacheMemory(){
        return String.valueOf(JAI.getDefaultInstance().getTileCache().getMemoryCapacity());
    }

    public List<String> getMimeTypes(){
        return new ArrayList<String>(types.keySet());
    }

    public Map<String,String> getReaders(){
        return types;
    }

}
