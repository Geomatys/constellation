/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.provider.coveragesgroup;

import java.util.logging.Level;
import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderService;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.provider.coveragesgroup.CoveragesGroupProvider.*;


/**
 *
 * @author Johann Sorel
 * @author Cédric Briançon
 */
public class CoveragesGroupProviderService extends AbstractProviderService
        <Name,LayerDetails,LayerProvider> implements LayerProviderService {

    public static final ParameterDescriptor<String> FOLDER_DESCRIPTOR =
             new DefaultParameterDescriptor<String>(KEY_FOLDER_PATH, "Folder path", String.class, null, true);

    public static final ParameterDescriptor<MapContext> MAP_CONTEXT_DESCRIPTOR =
             new DefaultParameterDescriptor<MapContext>(KEY_MAP_CONTEXT, "Map context", MapContext.class, null, false);

    public static final ParameterDescriptorGroup SOURCE_CONFIG_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("coveragesgroup", FOLDER_DESCRIPTOR, MAP_CONTEXT_DESCRIPTOR);

    public static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            createDescriptor(SOURCE_CONFIG_DESCRIPTOR);

    public CoveragesGroupProviderService(){
        super("coverages-group");
    }

    @Override
    public ParameterDescriptorGroup getServiceDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public GeneralParameterDescriptor getSourceDescriptor() {
        return SOURCE_CONFIG_DESCRIPTOR;
    }

    @Override
    public LayerProvider createProvider(ParameterValueGroup ps) {
        if (!canProcess(ps)) {
            return null;
        }

        final CoveragesGroupProvider provider = new CoveragesGroupProvider(this, ps);
        ps = getOrCreate(SOURCE_CONFIG_DESCRIPTOR, ps);
        getLogger().log(Level.INFO, "[PROVIDER]> Coverages group provider created : {0}",
                    value(FOLDER_DESCRIPTOR, ps));
        return provider;
    }

}
