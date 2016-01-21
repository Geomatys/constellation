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
package org.constellation.provider.sld;

import org.apache.sis.parameter.ParameterBuilder;
import org.constellation.provider.AbstractProviderFactory;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderFactory;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.util.logging.Level;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class SLDProviderFactory extends AbstractProviderFactory
        <String,MutableStyle,StyleProvider> implements StyleProviderFactory {

    /**
     * Service name
     */
    private static final String NAME = "sld";
    private static final String ERROR_MSG = "[PROVIDER]> Invalid SLD provider config";

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final ParameterDescriptor<String> FOLDER_DESCRIPTOR = BUILDER
            .addName("path")
            .setRemarks("Folder where style files can be found")
            .setRequired(true)
            .create(String.class, null);

    public static final ParameterDescriptorGroup SOURCE_CONFIG_DESCRIPTOR = BUILDER.addName("sldFolder").setRequired(true)
            .createGroup(FOLDER_DESCRIPTOR);
    public static final ParameterDescriptorGroup SOURCE_DESCRIPTOR =
            ProviderParameters.createDescriptor(SOURCE_CONFIG_DESCRIPTOR);

    @Override
    public ParameterDescriptorGroup getProviderDescriptor() {
        return SOURCE_DESCRIPTOR;
    }

    @Override
    public GeneralParameterDescriptor getStoreDescriptor() {
        return SOURCE_CONFIG_DESCRIPTOR;
    }

    public SLDProviderFactory(){
        super(NAME);
    }

    @Override
    public StyleProvider createProvider(String providerId, final ParameterValueGroup ps) {
        if(!canProcess(ps)){
            return null;
        }
        
        try {
            final SLDProvider provider = new SLDProvider(providerId,this,ps);
            getLogger().log(Level.INFO, "[PROVIDER]> SLD provider created : {0}", 
                    (provider.getFolder()==null) ? "no path" : provider.getFolder().getAbsolutePath());
            return provider;
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            getLogger().log(Level.SEVERE, ERROR_MSG, ex);
        }
        return null;
    }

}
