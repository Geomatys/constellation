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
package org.constellation.converter;

import java.util.Collection;
import java.util.List;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.util.DataReference;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.opengis.feature.type.Name;
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
            final Collection<StyleProvider> providers = StyleProviderProxy.getInstance().getProviders();
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
