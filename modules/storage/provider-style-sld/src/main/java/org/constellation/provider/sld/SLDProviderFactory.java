/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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
package org.constellation.provider.sld;

import org.opengis.parameter.GeneralParameterDescriptor;
import java.util.logging.Level;

import org.constellation.provider.AbstractProviderFactory;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderFactory;
import org.constellation.provider.configuration.ProviderParameters;

import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;

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

    public static final ParameterDescriptor<String> FOLDER_DESCRIPTOR =
             new DefaultParameterDescriptor<>("path","Folder where style files can be found",String.class,null,true);
    
    public static final ParameterDescriptorGroup SOURCE_CONFIG_DESCRIPTOR = 
            new DefaultParameterDescriptorGroup("sldFolder",FOLDER_DESCRIPTOR);
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
