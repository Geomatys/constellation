/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.provider.configuration.ProviderLayer;

import org.constellation.provider.configuration.ProviderSource;
import org.geotoolkit.feature.DefaultName;

import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.util.StringUtilities;

import org.opengis.feature.type.Name;

/**
 * Abstract implementation of LayerProvider which only handle the
 * getByIdentifier(String key) method.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractLayerProvider extends AbstractProvider<Name,LayerDetails> implements LayerProvider{

    protected static final String DEFAULT_NAMESPACE = "http://geotoolkit.org";
    protected static final String NO_NAMESPACE = "no namespace";


    protected AbstractLayerProvider(final ProviderService service,
            final ProviderSource config){
        super(service,config);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Name> getKeyClass() {
        return Name.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails getByIdentifier(Name key) {
        for(final Name n : getKeys()){
            if(n.equals(key)){
                return get(n);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ElevationModel getElevationModel(Name name) {
        return null;
    }

    /**
     * Provider should pass by this method to fill there index.
     * This method will only log miss configurations, loading the index
     * is part of the child class.
     */
    protected void visit(){
        final ProviderSource config = getSource();
        final Set<Name> keys = getKeys();

        final List<String> missingLayers = new ArrayList<String>();

        loop:
        for(final ProviderLayer declaredLayer : config.layers){
            for(Name n : keys){
                if(DefaultName.match(n, declaredLayer.name)) continue loop;
            }

            missingLayers.add(declaredLayer.name);
        }

        if(!missingLayers.isEmpty()){
            //log list of missing layers
            final StringBuilder sb = new StringBuilder("Provider ");
            sb.append(source.id).append(" declares layers missing in the source\n");
            sb.append(StringUtilities.toStringTree(missingLayers));
            getLogger().log(Level.WARNING, sb.toString());
        }
    }

    public static Name containsOnlyLocalPart(Collection<Name> index, Name layerName) {
        if (layerName != null) {
            if (layerName.getNamespaceURI() == null) {
                for (Name name : index) {
                    if (name.getLocalPart().equals(layerName.getLocalPart())) {
                        return name;
                    }
                }
            }
        }
        return null;
    }

}
