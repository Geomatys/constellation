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

import org.apache.sis.util.Static;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FileFeatureStoreFactory;

import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * utility class to know files extensions availables on Geotoolkit.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public final class GeotoolkitFileExtensionAvailable extends Static {


    /**
     * Give a extension list managed by geotoolkit
     * @return an {@link String} {@link List} which contain file extension.
     */
    public static Map<String, String> getAvailableFileExtension() {
        final Map<String, String> extensions = new HashMap<>(0);

        //get coverage file extension and add on list
        final String[] coverageExtension = ImageIO.getReaderFileSuffixes();
        for (String extension : coverageExtension) {
            extensions.put(extension.toLowerCase(), "raster");
        }

        //access to features file factories
        final Iterator<FileFeatureStoreFactory> ite = FeatureStoreFinder.getAllFactories(FileFeatureStoreFactory.class).iterator();
        while (ite.hasNext()) {

            final FileFeatureStoreFactory factory = ite.next();
            //display general informations about this factory
            String[] tempExtensions = factory.getFileExtensions();
            for (int i = 0; i < tempExtensions.length; i++) {
                String extension = tempExtensions[i];

                //remove point before extension
                extensions.put(extension.replace(".", "").toLowerCase(), "vector");
            }
        }
        
        // for O&M files
        extensions.put("xml", "observation");
        
        return extensions;
    }

}
