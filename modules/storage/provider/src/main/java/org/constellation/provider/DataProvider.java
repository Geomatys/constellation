/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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

import org.geotoolkit.map.ElevationModel;
import org.opengis.feature.type.Name;

import java.util.Date;
import org.apache.sis.storage.DataStore;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public interface DataProvider extends Provider<Name,Data>{

    /**
     * Original data store.
     * @return 
     */
    DataStore getMainStore();
    
    
    ElevationModel getElevationModel(Name name);

    Data get(String key);
    
    /**
     * Get the data related to the given key in given version.
     * @return LayerDetails if it is in the data provider, or null if not.
     */
    Data get(Name key, Date version);
}
