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

package org.constellation.provider.observationstore;

import org.apache.sis.parameter.ParameterBuilder;
import org.constellation.provider.AbstractProviderFactory;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.geotoolkit.observation.ObservationStoreFactory;
import org.geotoolkit.observation.ObservationStoreFinder;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.GenericName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import static org.constellation.provider.configuration.ProviderParameters.createDescriptor;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ObservationStoreProviderService extends AbstractProviderFactory
        <GenericName,Data,DataProvider> implements DataProviderFactory {

    private static final ParameterBuilder BUILDER = new ParameterBuilder();
    /**
     * Service name
     */
    public static final String NAME = "observation-store";
    public static final ParameterDescriptorGroup SOURCE_CONFIG_DESCRIPTOR;

    static {
        final List<ParameterDescriptorGroup> descs = new ArrayList<>();
        final Iterator<ObservationStoreFactory> ite = ObservationStoreFinder.getAllFactories(null).iterator();
        while(ite.hasNext()){
            //copy the descriptor with a minimum number of zero
            final ParameterDescriptorGroup desc = ite.next().getParametersDescriptor();

            final ParameterDescriptorGroup mindesc = BUILDER.addName(desc.getName())
                    .createGroup(0, 1, desc.descriptors().toArray(new GeneralParameterDescriptor[0]));
            descs.add(mindesc);
        }

        SOURCE_CONFIG_DESCRIPTOR = BUILDER.addName("choice").setRequired(true)
                .createGroup(descs.toArray(new GeneralParameterDescriptor[descs.size()]));

    }

    public static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            createDescriptor(SOURCE_CONFIG_DESCRIPTOR);
    
    public ObservationStoreProviderService(){
        super(NAME);
    }
    
    @Override
    public ParameterDescriptorGroup getProviderDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public ParameterDescriptorGroup getStoreDescriptor() {
        return SOURCE_CONFIG_DESCRIPTOR;
    }

    @Override
    public DataProvider createProvider(String providerId, ParameterValueGroup ps) {
        if(!canProcess(ps)){
            return null;
        }

        final ObservationStoreProvider provider = new ObservationStoreProvider(providerId,this,ps);
        getLogger().log(Level.INFO, "[PROVIDER]> observation-store provider created.");
        return provider;
    }
    
}
