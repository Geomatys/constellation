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
package org.constellation.provider.sml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.Map;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.provider.AbstractDataStoreProvider;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.storage.DataStoreException;
import org.opengis.feature.type.Name;
import org.xml.sax.SAXException;

import static org.geotoolkit.data.sml.SMLDataStoreFactory.*;



/**
 * SensorML Data provider. index and cache Datastores for the specified database (MDWeb strcuture).
 *
 * @version $Id: ShapeFileProvider.java 1870 2009-10-07 08:09:43Z eclesia $
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public class SMLProvider extends AbstractDataStoreProvider {

    private static final String KEY_SML_CONFIG = "sml_config";
    public  static final String KEY_DBTYPE     = DBTYPE.getName().getCode();
    public  static final String KEY_SGBDTYPE   = SGBDTYPE.getName().getCode();
    public  static final String KEY_DERBYURL   = DERBYURL.getName().getCode();
    public  static final String KEY_HOST       = HOST.getName().getCode();
    public  static final String KEY_PORT       = PORT.getName().getCode();
    public  static final String KEY_DATABASE   = DATABASE.getName().getCode();
    public  static final String KEY_USER       = USER.getName().getCode();
    public  static final String KEY_PASSWD     = PASSWD.getName().getCode();
    public  static final String KEY_NAMESPACE  = NAMESPACE.getName().getCode();

    protected SMLProvider(ProviderSource source) throws DataStoreException {
        super(source);
    }


    @Override
    public Map<String, Serializable> prepareParameters(final Map<String, String> original) {
        final Map<String,Serializable> params = new HashMap<String, Serializable>();
        params.put(KEY_DBTYPE, "SML");

        final String sgbdType = source.parameters.get(KEY_SGBDTYPE);
        if (sgbdType != null && sgbdType.equals("derby"))  {

            params.put(KEY_SGBDTYPE,"derby");

            final String derbyUrl = source.parameters.get(KEY_DERBYURL);
            params.put(KEY_DERBYURL, (derbyUrl != null) ? derbyUrl : "localhost");

        } else {
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
            final String database = source.parameters.get(KEY_DATABASE);
            final String user       = source.parameters.get(KEY_USER);
            final String passwd     = source.parameters.get(KEY_PASSWD);
            params.put(KEY_DATABASE, database);
            params.put(KEY_USER, user);
            params.put(KEY_PASSWD, passwd);
        }

        return params;
    }

    public static Collection<SMLProvider> loadProviders(){
        final Collection<SMLProvider> dps = new ArrayList<SMLProvider>();
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
                dps.add(new SMLProvider(ps));
            } catch(DataStoreException ex){
                getLogger().log(Level.WARNING, "Invalide SML provider config", ex);
            }
        }

        final StringBuilder builder = new StringBuilder("DATA PROVIDER : SensorML ");
        for (final SMLProvider dp : dps){
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
            SAXException, IOException, NamingException {

        final String configFile = ConfigDirectory.getPropertyValue(JNDI_GROUP,KEY_SML_CONFIG);

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }

        return ProviderConfig.read(new File(configFile.trim()));
    }

}
