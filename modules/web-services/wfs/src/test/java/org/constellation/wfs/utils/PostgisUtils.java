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

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.om.OMDataStoreFactory;
import org.geotoolkit.data.postgis.PostgisNGDataStoreFactory;
import org.geotoolkit.data.FeatureCollection;

import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.sml.SMLDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PostgisUtils {

    private static final Logger LOGGER = Logger.getLogger("org.mdweb.utils");

    private PostgisUtils() {}

    public static FeatureReader createOMLayer(String host, int port, String databaseName, String user, String pass, Name featureType) throws DataStoreException {

       final Map params = new HashMap<String, Object>();

       params.put("dbtype", "OM");
       params.put(OMDataStoreFactory.HOST.getName().toString(),     host);
       params.put(OMDataStoreFactory.PORT.getName().toString(),     port);
       params.put(OMDataStoreFactory.DATABASE.getName().toString(), databaseName);
       params.put(OMDataStoreFactory.USER.getName().toString(),     user);
       params.put(OMDataStoreFactory.PASSWD.getName().toString(),   pass);

       final DataStore store = DataStoreFinder.getDataStore(params);
       if (store != null) {
            return store.getFeatureReader(QueryBuilder.all(featureType));
       }
       return null;
   }

    public static FeatureReader createEmbeddedOMLayer(String url, Name featureType) throws DataStoreException {

       final Map params = new HashMap<String, Object>();

       params.put("dbtype", "OM");
       params.put(OMDataStoreFactory.SGBDTYPE.getName().toString(), "derby");
       params.put(OMDataStoreFactory.DERBYURL.getName().toString(), url);

       final DataStore store = DataStoreFinder.getDataStore(params);
       if (store != null) {
            return store.getFeatureReader(QueryBuilder.all(featureType));
       }
       return null;
   }

    public static FeatureReader createEmbeddedSMLLayer(String url, Name featureType) throws DataStoreException {

       final Map params = new HashMap<String, Object>();

       params.put("dbtype", "SML");
       params.put(SMLDataStoreFactory.SGBDTYPE.getName().toString(), "derby");
       params.put(SMLDataStoreFactory.DERBYURL.getName().toString(), url);

       final DataStore store = DataStoreFinder.getDataStore(params);
       if (store != null) {
            return store.getFeatureReader(QueryBuilder.all(featureType));
       }
       return null;
   }

    /*public static FeatureSource createPostGISLayer(String host, int port, String schema, String databaseName, String user, String pass, String featureSource) throws IOException{

       final Map params = new HashMap<String, Object>();

       params.put("dbtype", "postgisng");
       params.put(PostgisNGDataStoreFactory.HOST.getName().toString(),     host);
       params.put(PostgisNGDataStoreFactory.PORT.getName().toString(),     port);
       params.put(PostgisNGDataStoreFactory.SCHEMA.getName().toString(),   schema);
       params.put(PostgisNGDataStoreFactory.DATABASE.getName().toString(), databaseName);
       params.put(PostgisNGDataStoreFactory.USER.getName().toString(),     user);
       params.put(PostgisNGDataStoreFactory.PASSWD.getName().toString(),   pass);

       final DataStore store = DataStoreFinder.getDataStore(params);
       return store.getFeatureSource(featureSource);
   }*/

   public static SimpleFeature createPostGISFeature(String host, int port, String schema, String databaseName, String user, String pass, Name featureSource, String toponyme) throws DataStoreException{

       final Map params = new HashMap<String, Object>();

       params.put("dbtype", "postgisng");
       params.put(PostgisNGDataStoreFactory.HOST.getName().toString(),     host);
       params.put(PostgisNGDataStoreFactory.PORT.getName().toString(),     port);
       params.put(PostgisNGDataStoreFactory.SCHEMA.getName().toString(),   schema);
       params.put(PostgisNGDataStoreFactory.DATABASE.getName().toString(), databaseName);
       params.put(PostgisNGDataStoreFactory.USER.getName().toString(),     user);
       params.put(PostgisNGDataStoreFactory.PASSWD.getName().toString(),   pass);

       final DataStore store = DataStoreFinder.getDataStore(params);

       //fs.getBounds();
       final FeatureCollection coll = store.createSession(false).getFeatureCollection(QueryBuilder.all(featureSource));
       final org.geotoolkit.data.FeatureIterator ite = coll.iterator();
       try{
           while(ite.hasNext()) {
               final SimpleFeature f =  (SimpleFeature) ite.next();

               final String identifier = (String) f.getAttribute(featureSource);
               if (identifier.equals(toponyme)) {
                   return f;
               }
           }
       } catch(Exception ex) {
           LOGGER.log(Level.SEVERE, pass, ex);
       } finally {
           ite.close();
       }
       return null;
   }

    public static FeatureCollection createShapeLayer(File file) throws MalformedURLException, DataStoreException {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        if (!file.exists()) {
            System.out.println("the file does not exist");
            return null;
        }

        params.put("url", file.toURI().toURL());
        DataStore store = DataStoreFinder.getDataStore(params);
        if (store != null) {
            return store.createSession(false).getFeatureCollection(QueryBuilder.all(store.getNames().iterator().next()));
        } else {
            System.out.println("datastore null");
        }
        return null;
    }

    public static FeatureCollection createShapeLayer(URL url, String namespace) throws MalformedURLException, DataStoreException {
        Map<String, Serializable> params = new HashMap<String, Serializable>();

        params.put("url", url);
        params.put("namespace", namespace);
        DataStore store = DataStoreFinder.getDataStore(params);
        if (store != null) {
            return store.createSession(false).getFeatureCollection(QueryBuilder.all(store.getNames().iterator().next()));
        } else {
            System.out.println("datastore null");
        }
        return null;
    }
}
