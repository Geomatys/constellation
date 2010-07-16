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

import java.util.Collection;

import org.geotoolkit.map.ElevationModel;
import org.opengis.feature.type.Name;

/**
 * Abstract implementation of LayerProvider which only handle the
 * getByIdentifier(String key) method.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractLayerProvider implements LayerProvider{

    protected static final String DEFAULT_NAMESPACE = "http://geotoolkit.org";

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
    public LayerDetails getByIdentifier(Name key, String service) {
        for(final Name n : getKeys(service)){
            if( (key.getNamespaceURI() == null && key.getLocalPart().equals(n.getLocalPart()))
                || key.equals(n) ){
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
