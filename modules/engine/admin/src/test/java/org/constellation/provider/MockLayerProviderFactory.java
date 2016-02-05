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

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.storage.DataStore;
import org.constellation.api.DataType;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.util.NamesExt;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.GenericName;

import java.util.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MockLayerProviderFactory extends AbstractProviderFactory
        <GenericName,Data,DataProvider> implements DataProviderFactory {


    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final ParameterDescriptor<String> LAYERS =  BUILDER
            .addName("layers")
            .setRemarks("")
            .setRequired(false)
            .create(String.class, null);
    public static final ParameterDescriptor<Boolean> CRASH_CREATE = BUILDER
            .addName("crashOnCreate")
            .setRemarks("")
            .setRequired(false)
            .create(Boolean.class, Boolean.FALSE);
    public static final ParameterDescriptor<Boolean> CRASH_DISPOSE = BUILDER
            .addName("crashOnDispose")
            .setRemarks("")
            .setRequired(false)
            .create(Boolean.class, Boolean.FALSE);
    public static final ParameterDescriptorGroup PARAMETERS_DESCRIPTOR = BUILDER.addName("MockParameters").setRequired(true)
            .createGroup(LAYERS,CRASH_CREATE,CRASH_DISPOSE);
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
        public Set<GenericName> getKeys() {
            final String ctr = Parameters.value(LAYERS, getConfig());
            if(ctr == null){
                return Collections.emptySet();
            }

            final String[] str = ctr.split(",");
            final Set<GenericName> names = new HashSet<>();
            for(final String st : str){
                names.add(NamesExt.valueOf(st));
            }
            return names;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public Data get(final GenericName key) {
            return get(key, null);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public Data get(final GenericName key, Date version) {
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
