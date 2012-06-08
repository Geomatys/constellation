/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.wfs.ws.rs;

import java.util.Map;
import org.geotoolkit.data.FeatureCollection;
/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FeatureCollectionWrapper {
    
    private final FeatureCollection featureCollection;
    
    private final Map<String, String> schemaLocations;
    
    private final String gmlVersion;
    
    private final String wfsVersion;
    
    public FeatureCollectionWrapper(final FeatureCollection featureCollection, final Map<String, String> schemaLocations, final String gmlVersion, final String wfsVersion) {
        this.featureCollection = featureCollection;
        this.gmlVersion = gmlVersion;
        this.wfsVersion = wfsVersion;
        this.schemaLocations = schemaLocations;
    }

    /**
     * @return the featureCollection
     */
    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    /**
     * @return the schemaLocations
     */
    public Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }

    /**
     * @return the gmlVersion
     */
    public String getGmlVersion() {
        return gmlVersion;
    }

    /**
     * @return the wfsVersion
     */
    public String getWfsVersion() {
        return wfsVersion;
    }
    
    
}
