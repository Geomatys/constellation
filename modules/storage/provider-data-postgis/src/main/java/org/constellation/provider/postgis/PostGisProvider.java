/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2011, Geomatys
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
package org.constellation.provider.postgis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.provider.AbstractDataStoreProvider;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.storage.DataStoreException;
import org.opengis.feature.type.Name;
import org.xml.sax.SAXException;

import static org.geotoolkit.data.postgis.PostgisNGDataStoreFactory.*;

/**
 * PostGIS Data provider.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class PostGisProvider extends AbstractDataStoreProvider{

    private static final String KEY_POSTGIS_CONFIG  = "postgis_config";
    public static final String KEY_DBTYPE          = DBTYPE.getName().getCode();
    public static final String KEY_HOST            = HOST.getName().getCode();
    public static final String KEY_PORT            = PORT.getName().getCode();
    public static final String KEY_SCHEMA          = SCHEMA.getName().getCode();
    public static final String KEY_DATABASE        = DATABASE.getName().getCode();
    public static final String KEY_USER            = USER.getName().getCode();
    public static final String KEY_PASSWD          = PASSWD.getName().getCode();
    public static final String KEY_MAXCONN         = MAXCONN.getName().getCode();
    public static final String KEY_MINCONN         = MINCONN.getName().getCode();
    public static final String KEY_NAMESPACE       = NAMESPACE.getName().getCode();
    public static final String KEY_VALIDATECONN    = VALIDATECONN.getName().getCode();
    public static final String KEY_LOOSEBBOX       = LOOSEBBOX.getName().getCode();

    protected PostGisProvider(final PostGisProviderService service,
            final ProviderSource source) throws DataStoreException {
        super(service,source);
    }

    @Override
    public Map<String, Serializable> prepareParameters(final Map<String, String> originals) {
        final Map<String,Serializable> params = new HashMap<String, Serializable>();
        params.put(KEY_DBTYPE, "postgisng");

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
                getLogger().log(Level.SEVERE, null, ex);
            }
        } else {
            //this parameter is needed
            params.put(KEY_PORT, 5432);
        }

        // OTHERS --------------------------------------------------------------
        final String schema = source.parameters.get(KEY_SCHEMA);
        final String database = source.parameters.get(KEY_DATABASE);
        final String user       = source.parameters.get(KEY_USER);
        final String passwd     = source.parameters.get(KEY_PASSWD);
        final String namespace     = source.parameters.get(KEY_NAMESPACE);
        final String validateConnec = source.parameters.get(KEY_VALIDATECONN);
        params.put(KEY_SCHEMA, schema);
        params.put(KEY_DATABASE, database);
        params.put(KEY_USER, user);
        params.put(KEY_PASSWD, passwd);
        params.put(KEY_NAMESPACE, namespace);
        final Boolean validate = Boolean.valueOf(validateConnec);
        params.put(KEY_VALIDATECONN, validate);

        return params;
    }

    public static Collection<PostGisProvider> loadProviders(){
        final Collection<PostGisProvider> dps = new ArrayList<PostGisProvider>();
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
                dps.add(new PostGisProvider(null,ps));
            } catch(DataStoreException ex){
                getLogger().log(Level.WARNING, "Invalide postgis provider config", ex);
            }
        }

        final StringBuilder builder = new StringBuilder("DATA PROVIDER : PostGIS ");
        for(final PostGisProvider dp : dps){
            builder.append("\n[").append(dp.source.parameters.get(KEY_DATABASE)).append("=");
            for(final Name layer : dp.getKeys()){
                builder.append(layer).append(",");
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
            SAXException, IOException, NamingException
    {

        final String configFile = ConfigDirectory.getPropertyValue(JNDI_GROUP,KEY_POSTGIS_CONFIG);

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }

        return ProviderConfig.read(new File(configFile.trim()));
    }

}
