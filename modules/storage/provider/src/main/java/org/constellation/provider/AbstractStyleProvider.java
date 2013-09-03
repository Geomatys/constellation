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

package org.constellation.provider;

import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Abstract implementation of StyleProvider which only handle the
 * getByIdentifier(String key) method.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractStyleProvider extends AbstractProvider<String,MutableStyle> implements StyleProvider{

    protected AbstractStyleProvider(final ProviderService service, 
            final ParameterValueGroup source){
        super(service, source);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<MutableStyle> getValueClass() {
        return MutableStyle.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MutableStyle getByIdentifier(String key) {
        return get(key);
    }
}
