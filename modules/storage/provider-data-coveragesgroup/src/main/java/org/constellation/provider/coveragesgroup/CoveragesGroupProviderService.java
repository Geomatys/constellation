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
package org.constellation.provider.coveragesgroup;

import org.constellation.provider.AbstractProviderFactory;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.net.URL;
import java.util.logging.Level;

import static org.constellation.provider.configuration.ProviderParameters.createDescriptor;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.coveragesgroup.CoveragesGroupProvider.KEY_PATH;
import static org.geotoolkit.parameter.Parameters.value;
import org.opengis.util.GenericName;


/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class CoveragesGroupProviderService extends AbstractProviderFactory
        <GenericName,Data,DataProvider> implements DataProviderFactory {

    public static final String NAME = "coverages-group";

    public static final ParameterDescriptor<URL> URL =
            new DefaultParameterDescriptor<URL>(KEY_PATH, "Map context path", URL.class, null, true);
    
    public static final ParameterDescriptor<String> NAMESPACE =
            new DefaultParameterDescriptor<String>("namespace", "Provider general namespace", String.class, null, false);

    public static final ParameterDescriptorGroup SOURCE_CONFIG_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("coveragesgroup", URL, NAMESPACE);

    public static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            createDescriptor(SOURCE_CONFIG_DESCRIPTOR);

    public CoveragesGroupProviderService(){
        super(NAME);
    }

    @Override
    public ParameterDescriptorGroup getProviderDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public GeneralParameterDescriptor getStoreDescriptor() {
        return SOURCE_CONFIG_DESCRIPTOR;
    }

    @Override
    public DataProvider createProvider(String providerId, ParameterValueGroup ps) {
        if (!canProcess(ps)) {
            return null;
        }

        final CoveragesGroupProvider provider = new CoveragesGroupProvider(providerId, this, ps);
        ps = getOrCreate(SOURCE_CONFIG_DESCRIPTOR, ps);
        getLogger().log(Level.INFO, "[PROVIDER]> Coverages group provider created : {0}",
                value(URL, ps));
        return provider;
    }

}
