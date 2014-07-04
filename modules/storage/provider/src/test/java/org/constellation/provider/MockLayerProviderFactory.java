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

package org.constellation.provider;

import org.apache.sis.storage.DataStore;
import org.constellation.admin.dao.DataRecord.DataType;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.Parameters;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MockLayerProviderFactory extends AbstractProviderFactory
        <Name,Data,DataProvider> implements DataProviderFactory {

    public static final ParameterDescriptor<String> LAYERS =
             new DefaultParameterDescriptor<>("layers","", String.class, null, false);
    public static final ParameterDescriptor<Boolean> CRASH_CREATE =
             new DefaultParameterDescriptor<>("crashOnCreate","", Boolean.class, false, false);
    public static final ParameterDescriptor<Boolean> CRASH_DISPOSE =
             new DefaultParameterDescriptor<>("crashOnDispose","", Boolean.class, false, false);
    public static final ParameterDescriptorGroup PARAMETERS_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("MockParameters",LAYERS,CRASH_CREATE,CRASH_DISPOSE);
    private static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            ProviderParameters.createDescriptor(PARAMETERS_DESCRIPTOR);

    public MockLayerProviderFactory(){
        super("mock");
    }

    @Override
    public ParameterDescriptorGroup getProviderDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }
    
    @Override
    public DataProvider createProvider(String id,ParameterValueGroup config) {
        return new MockLayerProvider(id,config);
    }

    @Override
    public ParameterDescriptorGroup getStoreDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class MockLayerProvider extends AbstractDataProvider{

        public MockLayerProvider(String id,final ParameterValueGroup config){
            super(id,null,config);

            if(Boolean.TRUE.equals(Parameters.value(CRASH_CREATE, getConfig()))){
                throw new RuntimeException("Some error while loading.");
            }

        }

        @Override
        public DataStore getMainStore() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        private ParameterValueGroup getConfig(){
            final ParameterValueGroup params = getSource();

            final List<ParameterValueGroup> groups = params.groups(PARAMETERS_DESCRIPTOR.getName().getCode());
            if(!groups.isEmpty()){
                return groups.get(0);
            }
            return null;
        }

        @Override
        public Set<Name> getKeys() {
            final String ctr = Parameters.value(LAYERS, getConfig());
            if(ctr == null){
                return Collections.emptySet();
            }

            final String[] str = ctr.split(",");
            final Set<Name> names = new HashSet<>();
            for(final String st : str){
                names.add(DefaultName.valueOf(st));
            }
            return names;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public Data get(final Name key) {
            return get(key, null);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public Data get(final Name key, Date version) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void dispose() {
            super.dispose();

            if(Boolean.TRUE.equals(Parameters.value(CRASH_DISPOSE, getSource()))){
                throw new RuntimeException("Some error while dispose.");
            }
        }

        @Override
        public DataType getDataType() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isSensorAffectable() {
            throw new UnsupportedOperationException("Not supported."); 
        }
    }
}
