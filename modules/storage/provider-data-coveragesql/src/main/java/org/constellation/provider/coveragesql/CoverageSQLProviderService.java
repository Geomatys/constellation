/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2011, Geomatys
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
package org.constellation.provider.coveragesql;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.constellation.provider.AbstractProviderFactory;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.configuration.ProviderParameters;

import org.geotoolkit.internal.sql.table.ConfigurationKey;
import org.geotoolkit.coverage.sql.CoverageDatabase;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.DefaultParameterDescriptor;

import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;


/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLProviderService extends AbstractProviderFactory
        <Name,Data,DataProvider> implements DataProviderFactory {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid coverage-sql provider config";

    public static final ParameterDescriptor<String> NAMESPACE_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("namespace","",String.class,null,false);
    
    public static final ParameterDescriptorGroup COVERAGESQL_DESCRIPTOR;
    private static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR;
    
    static {
        //add namespace parameter
        final ParameterDescriptorGroup base = CoverageDatabase.PARAMETERS;
        final List<GeneralParameterDescriptor> descs = new ArrayList<GeneralParameterDescriptor>(base.descriptors());
        descs.add(NAMESPACE_DESCRIPTOR);
        
        COVERAGESQL_DESCRIPTOR = new DefaultParameterDescriptorGroup(
                base.getName().getCode(),descs.toArray(new GeneralParameterDescriptor[descs.size()]));
        
        SERVICE_CONFIG_DESCRIPTOR =
            ProviderParameters.createDescriptor(COVERAGESQL_DESCRIPTOR);
    }
    
    public static final GeneralParameterDescriptor PASSWORD_DESCRIPTOR = COVERAGESQL_DESCRIPTOR.descriptor(ConfigurationKey.PASSWORD.key);
    public static final GeneralParameterDescriptor CATALOG_DESCRIPTOR = COVERAGESQL_DESCRIPTOR.descriptor(ConfigurationKey.CATALOG.key);
    public static final GeneralParameterDescriptor ROOT_DIRECTORY_DESCRIPTOR = COVERAGESQL_DESCRIPTOR.descriptor(ConfigurationKey.ROOT_DIRECTORY.key);
    public static final GeneralParameterDescriptor SCHEMA_DESCRIPTOR = COVERAGESQL_DESCRIPTOR.descriptor(ConfigurationKey.SCHEMA.key);
    public static final GeneralParameterDescriptor USER_DESCRIPTOR = COVERAGESQL_DESCRIPTOR.descriptor(ConfigurationKey.USER.key);
    public static final GeneralParameterDescriptor URL_DESCRIPTOR = COVERAGESQL_DESCRIPTOR.descriptor(ConfigurationKey.URL.key);
    
    public CoverageSQLProviderService(){
        super("coverage-sql");
    }

    @Override
    public ParameterDescriptorGroup getProviderDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public ParameterDescriptorGroup getStoreDescriptor() {
        return COVERAGESQL_DESCRIPTOR;
    }

    @Override
    public DataProvider createProvider(String providerId, ParameterValueGroup ps) {
        if(!canProcess(ps)){
            return null;
        }
        
        final CoverageSQLProvider provider = new CoverageSQLProvider(providerId,this,ps);
        ps = ProviderParameters.getOrCreate(COVERAGESQL_DESCRIPTOR, ps);
        final ParameterValue<?> val = ps.parameter(ConfigurationKey.URL.name());
        getLogger().log(Level.INFO, "[PROVIDER]> coverage-sql provider created : {0}"
                , new Object[]{(val==null)?"null":val.getValue()});
        return provider;
    }

}
