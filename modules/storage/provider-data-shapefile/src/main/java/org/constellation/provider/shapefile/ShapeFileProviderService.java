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
package org.constellation.provider.shapefile;

import java.util.logging.Level;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderService;

import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geoamtys)
 */
public class ShapeFileProviderService extends AbstractProviderService
        <Name,LayerDetails,LayerProvider> implements LayerProviderService {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid shapefile provider config";
    
    public static final ParameterDescriptor<String> FOLDER_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("path","Folder where style files can be found",String.class,null,true);
    public static final ParameterDescriptor<String> NAMESPACE_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("namespace","Namespace used for the datastore is the given folder",String.class,null,false);
    public static final ParameterDescriptorGroup SOURCE_CONFIG_DESCRIPTOR =
            new DefaultParameterDescriptorGroup(SOURCE_DESCRIPTOR_NAME,FOLDER_DESCRIPTOR,NAMESPACE_DESCRIPTOR);
    public static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            new DefaultParameterDescriptorGroup(CONFIG_DESCRIPTOR_NAME,SOURCE_CONFIG_DESCRIPTOR);

    public ShapeFileProviderService(){
        super("shapefile");
    }

    @Override
    public ParameterDescriptorGroup getDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public LayerProvider createProvider(final ParameterValueGroup ps) {
        try {
            final ShapeFileProvider provider = new ShapeFileProvider(this,ps);
            getLogger().log(Level.INFO, "[PROVIDER]> shapefile provider created : {0}", value(FOLDER_DESCRIPTOR, ps));
            return provider;
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            getLogger().log(Level.SEVERE, ERROR_MSG, ex);
        }
        return null;
    }

}
