/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.provider.coveragesql;

import org.apache.sis.parameter.ParameterBuilder;
import org.constellation.provider.AbstractProviderFactory;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.coverage.sql.CoverageDatabase;
import org.geotoolkit.internal.sql.table.ConfigurationKey;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.opengis.util.GenericName;


/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLProviderService extends AbstractProviderFactory
        <GenericName,Data,DataProvider> implements DataProviderFactory {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid coverage-sql provider config";

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final ParameterDescriptor<String> NAMESPACE_DESCRIPTOR = BUILDER
            .addName("namespace")
            .setRemarks( "Provider general namespace")
            .setRequired(false)
            .create(String.class, null);

    public static final ParameterDescriptorGroup COVERAGESQL_DESCRIPTOR;
    private static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR;
    
    static {
        //add namespace parameter
        final ParameterDescriptorGroup base = CoverageDatabase.PARAMETERS;
        final List<GeneralParameterDescriptor> descs = new ArrayList<>(base.descriptors());
        descs.add(NAMESPACE_DESCRIPTOR);

        COVERAGESQL_DESCRIPTOR = BUILDER.addName(base.getName().getCode()).setRequired(true)
                .createGroup(descs.toArray(new GeneralParameterDescriptor[descs.size()]));
        SERVICE_CONFIG_DESCRIPTOR = ProviderParameters.createDescriptor(COVERAGESQL_DESCRIPTOR);
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
