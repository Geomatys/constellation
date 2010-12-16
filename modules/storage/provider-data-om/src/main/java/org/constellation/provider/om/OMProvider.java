/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.provider.om;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.om.OMDataStoreFactory;

import org.opengis.feature.type.Name;
import org.xml.sax.SAXException;



/**
 * Observation and Measurmement Data provider. index and cache Datastores for the specified database.
 *
 * @version $Id:
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public class OMProvider extends AbstractLayerProvider {

    private static final String KEY_OM_CONFIG  = "om_config";
    public  static final String KEY_DBTYPE     = OMDataStoreFactory.DBTYPE.getName().toString();
    public  static final String KEY_SGBDTYPE   = OMDataStoreFactory.SGBDTYPE.getName().toString();
    public  static final String KEY_DERBYURL   = OMDataStoreFactory.DERBYURL.getName().toString();
    public  static final String KEY_HOST       = OMDataStoreFactory.HOST.getName().toString();
    public  static final String KEY_PORT       = OMDataStoreFactory.PORT.getName().toString();
    public  static final String KEY_DATABASE   = OMDataStoreFactory.DATABASE.getName().toString();
    public  static final String KEY_USER       = OMDataStoreFactory.USER.getName().toString();
    public  static final String KEY_PASSWD     = OMDataStoreFactory.PASSWD.getName().toString();
    public  static final String KEY_NAMESPACE  = OMDataStoreFactory.NAMESPACE.getName().toString();

    private final Map<String,Serializable> params = new HashMap<String,Serializable>();
    private final Set<Name> index = new LinkedHashSet<Name>();
    private final DataStore store;


    protected OMProvider(ProviderSource source) throws DataStoreException {
        super(source);
        params.put(KEY_DBTYPE, "OM");

        final String sgbdType = source.parameters.get(KEY_SGBDTYPE);
        if (sgbdType != null && sgbdType.equals("derby"))  {

            params.put(KEY_SGBDTYPE,"derby");
            
            final String derbyUrl = source.parameters.get(KEY_DERBYURL);
            params.put(KEY_DERBYURL, (derbyUrl != null) ? derbyUrl : "localhost");

        } else {

            final String host = source.parameters.get(KEY_HOST);
            params.put(KEY_HOST, (host != null) ? host : "localhost");
            final String port = source.parameters.get(KEY_PORT);
            if (port != null) {
                try{
                    final Integer iport = Integer.valueOf(port);
                    params.put(KEY_PORT, iport);
                } catch (NumberFormatException ex) {
                    //just log it, use the default port
                    params.put(KEY_PORT, 5432);
                    getLogger().log(Level.SEVERE, null, ex);
                }
            } else {
                //this parameter is needed
                params.put(KEY_PORT, 5432);
            }

            final String database   = source.parameters.get(KEY_DATABASE);
            final String user       = source.parameters.get(KEY_USER);
            final String passwd     = source.parameters.get(KEY_PASSWD);
            params.put(KEY_DATABASE, database);
            params.put(KEY_USER, user);
            params.put(KEY_PASSWD, passwd);
        }

        store = DataStoreFinder.getDataStore(params);
        if (store == null) {
            final StringBuilder sb = new StringBuilder("Could not connect to O&M : \n");
            for(final Map.Entry<String,Serializable> entry : params.entrySet()){
                if (entry.getKey().equals(KEY_PASSWD)) {
                    sb.append(entry.getKey()).append(" : *******").append(entry.getValue()).append("*").append('\n');
                } else {
                    sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
                }
            }
            throw new DataStoreException(sb.toString());
        } else {
            visit();
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return Collections.unmodifiableSet(index);
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
        return new OMLayerDetails(key, store, styles, dateStartField, dateEndField, elevationStartField, elevationEndField);
        
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

    @Override
    protected void visit() {
        try {
            for (final Name name : store.getNames()) {
                index.add(name);
            }
        } catch (DataStoreException ex) {
            //Looks like we could not connect to the postgis database, the layers won't be indexed and the getCapability
            //won't be able to find thoses layers.
            getLogger().log(Level.SEVERE, null, ex);
        }
        super.visit();
    }

    public static Collection<OMProvider> loadProviders(){
        final Collection<OMProvider> dps = new ArrayList<OMProvider>();
        final ProviderConfig config;
        try {
            config = getConfig();
        } catch (ParserConfigurationException ex) {
            getLogger().log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (SAXException ex) {
            getLogger().log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (NamingException ex) {
            return Collections.emptyList();
        }

        if (config == null) {
            return dps;
        }
        for(final ProviderSource ps : config.sources) {
            try {
                dps.add(new OMProvider(ps));
            } catch(DataStoreException ex){
                getLogger().log(Level.WARNING, "Invalide O&M provider config", ex);
            }
        }

        final StringBuilder builder = new StringBuilder("DATA PROVIDER : O&M ");
        for (final OMProvider dp : dps){
            builder.append("\n[").append(dp.source.parameters.get(KEY_DATABASE)).append("=");
            for(final Name layer : dp.getKeys()){
                builder.append(layer).append( ",");
            }
            builder.append("]");
        }
        getLogger().log(Level.INFO, builder.toString());

        return dps;
    }

    /**
     *
     * @return List of folders holding shapefiles
     */
    private static ProviderConfig getConfig() throws ParserConfigurationException,
            SAXException, IOException, NamingException {

        final String configFile = ConfigDirectory.getPropertyValue(JNDI_GROUP,KEY_OM_CONFIG);

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }

        return ProviderConfig.read(new File(configFile.trim()));
    }

}
