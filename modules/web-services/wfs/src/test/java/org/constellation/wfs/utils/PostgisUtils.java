/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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


package org.constellation.wfs.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.FeatureCollection;

import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.sml.SMLDataStoreFactory;
import org.opengis.feature.type.Name;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PostgisUtils {

    private PostgisUtils() {}

    public static FeatureCollection createEmbeddedSMLLayer(String url, Name featureType) throws DataStoreException {

       final Map params = new HashMap<String, Object>();

       params.put("dbtype", "SML");
       params.put(SMLDataStoreFactory.SGBDTYPE.getName().toString(), "derby");
       params.put(SMLDataStoreFactory.DERBYURL.getName().toString(), url);

       final DataStore store = DataStoreFinder.getDataStore(params);
       return store.createSession(false).getFeatureCollection(QueryBuilder.all(featureType));
   }

}
