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
package org.constellation.process.provider.style;

import java.util.Collection;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.provider.style.AddStyleToStyleProviderDescriptor.*;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.style.MutableStyle;

/**
 * Add a style to an exising StyleProvider. If style name already exist, process will throw a ProcessException.
 *
 * @author Quentin Boileau (Geomatys).
 */
public class AddStyleToStyleProvider extends AbstractCstlProcess {

    public AddStyleToStyleProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Add a style to an existing style provider.
     * @throws ProcessException if :
     * - provider identifier is null/empty or not found in LayerProvider list.
     * - style name is null/empty.
     * - style is null.
     */
    @Override
    protected void execute() throws ProcessException {

        final String providerId = value(PROVIDER_ID, inputParameters);
        String styleName = value(STYLE_ID, inputParameters);
        final MutableStyle style = value(STYLE, inputParameters);

        if (providerId == null || "".equals(providerId.trim())) {
            throw new ProcessException("Provider identifier can't be null or empty.", this, null);
        }

        //use MutableStyle name if style_name parame is null.
        if ( styleName == null || "".equals(styleName.trim()) ) {
            if ( style.getName() == null || "".equals(style.getName().trim()) ) {
                throw new ProcessException("Style name can't be null or empty. Please set a name in style or use style_name input parameter.", this, null);
            } else {
                styleName = style.getName();
            }
        } else {
            style.setName(styleName);
        }

        if (style != null) {
            final Collection<StyleProvider> providers = StyleProviderProxy.getInstance().getProviders();

            boolean found = false;
            for (final StyleProvider p : providers) {
                if (p.getId().equals(providerId)) {
                    p.set(styleName, style);
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new ProcessException("Provider with id "+providerId+" not found.", this, null);
            }

        } else {
            throw new ProcessException("Style can't be null.", this, null);
        }
    }
}
