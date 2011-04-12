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
package org.constellation.provider.go2graphic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderParameters;

import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.style.MutableStyle;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class GO2StyleProviderService extends AbstractProviderService
        <String,MutableStyle,StyleProvider> implements StyleProviderService {

    private static final Collection<GO2StyleProvider> PROVIDERS = new ArrayList<GO2StyleProvider>();
    private static final Collection<GO2StyleProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    public static final ParameterDescriptorGroup SOURCE_DESCRIPTOR = new DefaultParameterDescriptorGroup(
            Collections.singletonMap("name", ProviderParameters.SOURCE_DESCRIPTOR_NAME),
            0,Integer.MAX_VALUE);
    public static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            new DefaultParameterDescriptorGroup(ProviderParameters.CONFIG_DESCRIPTOR_NAME,SOURCE_DESCRIPTOR);

    public GO2StyleProviderService() {
        super("go2style");
        PROVIDERS.clear();
        //GO2 Style are hard coded java objects with no property configuration
        PROVIDERS.add(new GO2StyleProvider(this));
    }

    @Override
    public StyleProvider createProvider(final ParameterValueGroup config) {
        return null;
    }

    @Override
    public Collection<? extends StyleProvider> getAdditionalProviders() {
        return IMMUTABLE;
    }

    @Override
    public ParameterDescriptorGroup getDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

}
