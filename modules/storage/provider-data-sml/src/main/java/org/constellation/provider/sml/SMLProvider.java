/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.provider.sml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerProvider;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.constellation.resources.ArraySet;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.FeatureSource;
import org.geotoolkit.data.sml.SMLDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.xml.sax.SAXException;



/**
 * SensorML Data provider. index and cache Datastores for the specified database (MDWeb strcuture).
 *
 * @version $Id: ShapeFileProvider.java 1870 2009-10-07 08:09:43Z eclesia $
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public class SMLProvider implements NamedLayerProvider {

    private static final Logger LOGGER         = Logger.getLogger("org.constellation.provider.postgis");
    private static final String KEY_SML_CONFIG = "sml_config";
    public  static final String KEY_DBTYPE     = SMLDataStoreFactory.DBTYPE.getName().toString();
    public  static final String KEY_HOST       = SMLDataStoreFactory.HOST.getName().toString();
    public  static final String KEY_PORT       = SMLDataStoreFactory.PORT.getName().toString();
    public  static final String KEY_DATABASE   = SMLDataStoreFactory.DATABASE.getName().toString();
    public  static final String KEY_USER       = SMLDataStoreFactory.USER.getName().toString();
    public  static final String KEY_PASSWD     = SMLDataStoreFactory.PASSWD.getName().toString();
    public  static final String KEY_NAMESPACE  = SMLDataStoreFactory.NAMESPACE.getName().toString();

    private final Map<String,Serializable> params = new HashMap<String,Serializable>();
    private final List<Name> index = new ArrayList<Name>();
    private DataStore store;
    private final ProviderSource source;


    protected SMLProvider(ProviderSource source) throws IOException {
        this.source = source;
        params.put(KEY_DBTYPE, "SML");

        // HOST ----------------------------------------------------------------
        final String host = source.parameters.get(KEY_HOST);
        params.put(KEY_HOST, (host != null) ? host : "localhost");

        // PORT ----------------------------------------------------------------
        final String port = source.parameters.get(KEY_PORT);
        if (port != null) {
            try{
                final Integer iport = Integer.valueOf(port);
                params.put(KEY_PORT, iport);
            } catch (NumberFormatException ex) {
                //just log it, use the default port
                params.put(KEY_PORT, 5432);
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            //this parameter is needed
            params.put(KEY_PORT, 5432);
        }

        // OTHERS --------------------------------------------------------------
        final String database = source.parameters.get(KEY_DATABASE);
        final String user       = source.parameters.get(KEY_USER);
        final String passwd     = source.parameters.get(KEY_PASSWD);
        params.put(KEY_DATABASE, database);
        params.put(KEY_USER, user);
        params.put(KEY_PASSWD, passwd);

        store = DataStoreFinder.getDataStore(params);
        if (store == null) {
            final StringBuilder sb = new StringBuilder("Could not connect to SML : \n");
            for(final Map.Entry<String,Serializable> entry : params.entrySet()){
                if (entry.getKey().equals(KEY_PASSWD)) {
                    sb.append(entry.getKey()).append(" : *******");
                } else {
                    sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
                }
            }
            throw new IOException(sb.toString());
        } else {
            visit();
        }

    }

    protected ProviderSource getSource(){
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Name> getKeyClass() {
        return Name.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return new ArraySet<Name>(index);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {
        return index.contains(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final Name key) {
        if (!index.contains(key)) {
            return null;
        }

        final List<String> styles;
        final String dateStartField;
        final String dateEndField;
        final String elevationStartField;
        final String elevationEndField;
        final ProviderLayer layer = source.getLayer(key.getLocalPart());
        if (layer != null) {
            styles              = layer.styles;
            dateStartField      = layer.dateStartField;
            dateEndField        = layer.dateEndField;
            elevationStartField = layer.elevationStartField;
            elevationEndField   = layer.elevationEndField;
        } else {
            styles              = new ArrayList<String>();
            dateStartField      = null;
            dateEndField        = null;
            elevationStartField = null;
            elevationEndField   = null;
        }
        final FeatureSource<SimpleFeatureType, SimpleFeature> fs;
        try {
            fs = store.getFeatureSource(key);
        } catch (IOException ex) {
            //could not create the requested featuresource
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
        if (fs == null) {
            return null;
        }
        return new SMLLayerDetails(key.getLocalPart(), fs, styles, dateStartField, dateEndField, elevationStartField, elevationEndField);
        
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            index.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
            params.clear();
            source.layers.clear();
            source.parameters.clear();
        }
    }

    private void visit() {
        try {
            for (final Name name : (List<Name>)store.getNames()) {
                index.add(name);
            }
        } catch (IOException ex) {
            //Looks like we could not connect to the postgis database, the layers won't be indexed and the getCapability
            //won't be able to find thoses layers.
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static final Collection<SMLProvider> loadProviders(){
        final Collection<SMLProvider> dps = new ArrayList<SMLProvider>();
        final ProviderConfig config;
        try {
            config = getConfig();
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (NamingException ex) {
            return Collections.emptyList();
        }

        if (config == null) {
            return dps;
        }
        for(final ProviderSource ps : config.sources) {
            try {
                dps.add(new SMLProvider(ps));
            } catch(IOException ex){
                LOGGER.log(Level.WARNING, "Invalide SML provider config", ex);
            }
        }

        final StringBuilder builder = new StringBuilder("DATA PROVIDER : SensorML ");
        for (final SMLProvider dp : dps){
            builder.append("\n["+ dp.source.parameters.get(KEY_DATABASE)+"=");
            for(final Name layer : dp.getKeys()){
                builder.append(layer + ",");
            }
            builder.append("]");
        }
        LOGGER.log(Level.INFO, builder.toString());

        return dps;
    }

    /**
     *
     * @return List of folders holding shapefiles
     */
    private static final ProviderConfig getConfig() throws ParserConfigurationException,
            SAXException, IOException, NamingException
    {

        final String configFile = ConfigDirectory.getPropertyValue(JNDI_GROUP,KEY_SML_CONFIG);

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }

        return ProviderConfig.read(new File(configFile.trim()));
    }

}
