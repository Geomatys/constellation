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

package org.constellation.provider;

import org.constellation.api.DataType;
import org.constellation.api.ProviderType;
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Abstract implementation of StyleProvider which only handle the
 * getByIdentifier(String key) method.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractStyleProvider extends AbstractProvider<String,MutableStyle> implements StyleProvider{

    protected AbstractStyleProvider(final String id, final ProviderFactory service, 
            final ParameterValueGroup source){
        super(id, service, source);
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
    public MutableStyle getByIdentifier(final String key) {
        return get(key);
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.STYLE;
    }

    @Override
    public DataType getDataType() {
        throw new UnsupportedOperationException("Not supported for style provider.");
    }
}
