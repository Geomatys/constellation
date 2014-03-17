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

package org.constellation.provider;

import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.Parameters;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.sis.storage.DataStore;
import org.constellation.admin.dao.DataRecord.DataType;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MockLayerProviderService extends AbstractProviderService
        <Name,LayerDetails,LayerProvider> implements LayerProviderService {

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

    public MockLayerProviderService(){
        super("mock");
    }

    @Override
    public ParameterDescriptorGroup getServiceDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }
    
    @Override
    public LayerProvider createProvider(ParameterValueGroup config) {
        return new MockLayerProvider(config);
    }

    @Override
    public ParameterDescriptorGroup getSourceDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class MockLayerProvider extends AbstractLayerProvider{

        public MockLayerProvider(final ParameterValueGroup config){
            super(null,config);

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
        public LayerDetails get(final Name key) {
            return get(key, null);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public LayerDetails get(final Name key, Date version) {
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
    }
}
