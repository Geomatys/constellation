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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.media.jai.JAI;

/**
 * Returns several information of available imagery readers and writers.
 *
 * @author Johann Sorel (Geomatys)
 */
public class JAIBean {

    public String getTileCacheMemory(){
        return String.valueOf(JAI.getDefaultInstance().getTileCache().getMemoryCapacity());
    }

    public List<String> getMimeTypes(){
        return Arrays.asList(ImageIO.getReaderMIMETypes());
    }

    public Map<String,String> getReaders(){
        final Map<String,String> map = new HashMap<String, String>();
        for(String mime : ImageIO.getReaderMIMETypes()){
            final Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mime);
            if(readers.hasNext()){
                map.put(mime, readers.next().getClass().getName());
            }
        }
        return map;
    }

}
