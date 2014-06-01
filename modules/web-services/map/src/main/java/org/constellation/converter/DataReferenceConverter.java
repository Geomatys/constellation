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
package org.constellation.converter;

import java.util.Collection;
import java.util.List;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;
import org.constellation.util.DataReference;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.feature.type.Name;
import org.opengis.style.Description;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Style;
import org.opengis.style.StyleVisitor;
import org.opengis.style.Symbolizer;


/**
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public final class DataReferenceConverter {

    /**
     * Prevents instantiation.
     */
    private DataReferenceConverter() {}

    public static Style convertDataReferenceToStyle(final DataReference source) throws NonconvertibleObjectException {
        if (source == null) {
            throw new NonconvertibleObjectException("Null data reference given.");
        }

        final String dataType = source.getDataType();

        Style style = null;
        final Name layerName = source.getLayerId();

        /*
         * Search in Provider layers
         */
        if (dataType.equals(DataReference.PROVIDER_STYLE_TYPE)) {
            final String providerID = source.getProviderOrServiceId();

            boolean providerFound = false;

            //find provider
            final Collection<StyleProvider> providers = StyleProviders.getInstance().getProviders();
            for (StyleProvider provider : providers) {
                if (provider.getId().equals(providerID)) {

                    providerFound = true;
                    style = provider.get(layerName.toString());
                    break;
                }
            }

            if (!providerFound) {
                throw new NonconvertibleObjectException("Provider id " + providerID + " not found.");
            }
            if (style == null) {
                throw new NonconvertibleObjectException("Layer name " + layerName + " not found.");
            }
        } else {
            throw new NonconvertibleObjectException("Layer provider and service are not supported.");
        }

        return new ReferenceStyleWrapper(style, source.getReference());
    }


    /**
     * Private internal class that wrap a Style into another with a specified identifier.
     */
    private static class ReferenceStyleWrapper implements Style {

        private final Style style;
        private final String name;

        public ReferenceStyleWrapper(final Style style, final String name) {
            this.style = style;
            this.name = name;
        }


        @Override
        public String getName() {
            return name;
        }

        @Override
        public Description getDescription() {
            return style.getDescription();
        }

        @Override
        public boolean isDefault() {
            return style.isDefault();
        }

        @Override
        public List<? extends FeatureTypeStyle> featureTypeStyles() {
             return style.featureTypeStyles();
        }

        @Override
        public Symbolizer getDefaultSpecification() {
             return style.getDefaultSpecification();
        }

        @Override
        public Object accept(final StyleVisitor visitor, final Object extraData) {
             return style.accept(visitor, extraData);
        }

    }
}
