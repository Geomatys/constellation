/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.data.om2;

import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.opengis.metadata.Identifier;
import java.util.Collections;
import org.geotoolkit.parameter.Parameters;
import org.opengis.metadata.identification.Identification;
import org.opengis.parameter.ParameterDescriptor;
import org.apache.commons.dbcp.BasicDataSource;

import java.io.IOException;

import org.geotoolkit.data.AbstractFeatureStoreFactory;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.jdbc.ManageableDataSource;
import org.geotoolkit.jdbc.DBCPDataSource;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.metadata.iso.quality.DefaultConformanceResult;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;

import org.opengis.metadata.quality.ConformanceResult;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import org.apache.sis.util.iso.ResourceInternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class OM2FeatureStoreFactory extends AbstractFeatureStoreFactory {

    /** factory identification **/
    public static final String NAME = "om2";
    public static final DefaultServiceIdentification IDENTIFICATION;
    static {
        IDENTIFICATION = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(NAME);
        final DefaultCitation citation = new DefaultCitation(NAME);
        citation.setIdentifiers(Collections.singleton(id));
        IDENTIFICATION.setCitation(citation);
    }
    
    public static final ParameterDescriptor<String> IDENTIFIER = createFixedIdentifier(NAME);
    
    /**
     * Parameter for database port
     */
    public static final ParameterDescriptor<Integer> PORT =
             new DefaultParameterDescriptor<>("port","Port",Integer.class,5432, false);

    /**
     * Parameter identifying the OM datastore
     */
    public static final ParameterDescriptor<String> DBTYPE =
             new DefaultParameterDescriptor<>("dbtype","DbType",String.class, "OM2", true);

    /**
     * Parameter for database type (postgres, derby, ...)
     */
    public static final ParameterDescriptor<String> SGBDTYPE =
             new DefaultParameterDescriptor<>(Collections.singletonMap("name", "sgbdtype"),
            String.class, new String[]{"derby","postgres"},null,null,null,null,true);

    /**
     * Parameter for database url for derby database
     */
    public static final ParameterDescriptor<String> DERBYURL =
             new DefaultParameterDescriptor<>("derbyurl","DerbyURL",String.class, null,false);

    /**
     * Parameter for database host
     */
    public static final ParameterDescriptor<String> HOST =
             new DefaultParameterDescriptor<>("host","Host", String.class, "localhost",false);

    /**
     * Parameter for database name
     */
    public static final ParameterDescriptor<String> DATABASE =
             new DefaultParameterDescriptor<>("database","Database", String.class, null, false);

    /**
     * Parameter for database user name
     */
    public static final ParameterDescriptor<String> USER =
             new DefaultParameterDescriptor<>("user","User", String.class, null,false);

    /**
     * Parameter for database user password
     */
    public static final ParameterDescriptor<String> PASSWD =
             new DefaultParameterDescriptor<>("password","Password", String.class, null, false);

    public static final ParameterDescriptorGroup PARAMETERS_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("OM2Parameters",
                IDENTIFIER,DBTYPE,HOST,PORT,DATABASE,USER,PASSWD,NAMESPACE, SGBDTYPE, DERBYURL);

    @Override
    public Identification getIdentification() {
        return IDENTIFICATION;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public CharSequence getDescription() {
        return new ResourceInternationalString("org/constellation/data/om2/bundle", "datastoreDescription");
    }

    @Override
    public CharSequence getDisplayName() {
        return new ResourceInternationalString("org/constellation/data/om2/bundle", "datastoreTitle");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ParameterDescriptorGroup getParametersDescriptor() {
        return PARAMETERS_DESCRIPTOR;
    }

    @Override
    public boolean canProcess(final ParameterValueGroup params) {
        boolean valid = super.canProcess(params);
        if(valid){
            Object value = params.parameter(DBTYPE.getName().toString()).getValue();
            if("OM2".equals(value)){
                Object sgbdtype = Parameters.value(SGBDTYPE, params);

                if("derby".equals(sgbdtype)){
                    //check the url is set
                    Object derbyurl = Parameters.value(DERBYURL, params);
                    return derbyurl != null;
                }else{
                    return true;
                }

            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public FeatureStore open(final ParameterValueGroup params) throws DataStoreException {
        checkCanProcessWithError(params);
        try{
            //create a datasource
            final BasicDataSource dataSource = new BasicDataSource();

            // some default data source behaviour
            dataSource.setPoolPreparedStatements(true);

            // driver
            dataSource.setDriverClassName(getDriverClassName(params));

            // url
            dataSource.setUrl(getJDBCUrl(params));

            // username
            final String user = (String) params.parameter(USER.getName().toString()).getValue();
            dataSource.setUsername(user);

            // password
            final String passwd = (String) params.parameter(PASSWD.getName().toString()).getValue();
            if (passwd != null) {
                dataSource.setPassword(passwd);
            }

            // some datastores might need this
            dataSource.setAccessToUnderlyingConnectionAllowed(true);

            final ManageableDataSource source = new DBCPDataSource(dataSource);
            return new OM2FeatureStore(params,source);
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }
    }

    @Override
    public FeatureStore create(final ParameterValueGroup params) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String getDriverClassName(final ParameterValueGroup params){
        final String type  = (String) params.parameter(SGBDTYPE.getName().toString()).getValue();
        if (type.equals("derby")) {
            return "org.apache.derby.jdbc.EmbeddedDriver";
        } else {
            return "org.postgresql.Driver";
        }
    }

    private String getJDBCUrl(final ParameterValueGroup params) throws IOException {
        final String type  = (String) params.parameter(SGBDTYPE.getName().toString()).getValue();
        if (type.equals("derby")) {
            final String derbyURL = (String) params.parameter(DERBYURL.getName().toString()).getValue();
            return derbyURL;
        } else {
            final String host  = (String) params.parameter(HOST.getName().toString()).getValue();
            final Integer port = (Integer) params.parameter(PORT.getName().toString()).getValue();
            final String db    = (String) params.parameter(DATABASE.getName().toString()).getValue();
            return "jdbc:postgresql" + "://" + host + ":" + port + "/" + db;
        }
    }

    @Override
    public ConformanceResult availability() {
        DefaultConformanceResult result =  new DefaultConformanceResult();
        result.setPass(true);
        return result;
    }
   

}
