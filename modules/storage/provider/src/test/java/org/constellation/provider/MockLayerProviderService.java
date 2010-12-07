/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import java.util.HashSet;
import java.util.Set;
import org.constellation.provider.configuration.ProviderSource;
import org.geotoolkit.feature.DefaultName;
import org.opengis.feature.type.Name;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MockLayerProviderService extends AbstractProviderService
        <Name,LayerDetails,LayerProvider> implements LayerProviderService {

    public MockLayerProviderService(){
        super("mock");
    }

    @Override
    public LayerProvider createProvider(ProviderSource config) {

        if(config.parameters.containsKey("crashOnCreate")){
            throw new RuntimeException("Some error while loading.");
        }

        return new MockLayerProvider(config);
    }

    private static class MockLayerProvider extends AbstractLayerProvider{

        public MockLayerProvider(ProviderSource config){
            super(config);
        }

        @Override
        public Set<Name> getKeys() {
            final String[] str = getSource().parameters.get("layers").split(",");
            final Set<Name> names = new HashSet<Name>();
            for(final String st : str){
                names.add(DefaultName.valueOf(st));
            }
            return names;
        }

        @Override
        public LayerDetails get(Name key) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void dispose() {
            super.dispose();

            if(source.parameters.containsKey("crashOnDispose")){
                throw new RuntimeException("Some error while dispose.");
            }
        }

    }
}
