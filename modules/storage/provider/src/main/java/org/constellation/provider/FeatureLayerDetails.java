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

package org.constellation.provider;

import org.geotoolkit.data.DataStore;
import org.opengis.feature.type.Name;

/**
 * Layer details for Feature sources.
 * Provide extra methods to access the underlying datastore.
 * 
 * @author Johann Sorel (Geomatys)
 */
public interface FeatureLayerDetails extends LayerDetails{

    /**
     * Get the source of features used by this layer.
     *
     * @return FeatureSource<SimpleFeatureType,SimpleFeature>
     */
    DataStore getStore();

}
