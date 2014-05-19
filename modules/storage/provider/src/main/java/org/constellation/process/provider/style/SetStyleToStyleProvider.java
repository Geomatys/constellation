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
package org.constellation.process.provider.style;

import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterValueGroup;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor.OWNER;
import static org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor.PROVIDER_ID;
import static org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor.STYLE;
import static org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor.STYLE_ID;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * Add a style to an existing StyleProvider. If the style already exists, update it.
 *
 * @author Quentin Boileau (Geomatys).
 * @author Bernard Fabien (Geomatys).
 */
public class SetStyleToStyleProvider extends AbstractCstlProcess {

    public SetStyleToStyleProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * @throws ProcessException if the provider can't be found or if there is no specified style name
     */
    @Override
    protected void execute() throws ProcessException {
        final String providerID  = value(PROVIDER_ID, inputParameters); // required
        final MutableStyle style = value(STYLE,       inputParameters); // required
        final String owner       = value(OWNER,       inputParameters); // optional
        String styleName         = value(STYLE_ID,    inputParameters); // optional

        // Proceed style name.
        if (isBlank(styleName)) {
            if (isBlank(style.getName())) {
                throw new ProcessException("Unable to delete the style. No specified style name.", this, null);
            } else {
                styleName = style.getName();
            }
        } else {
            style.setName(styleName);
        }

        // Retrieve or not the provider instance.
        final StyleProvider provider = StyleProviders.getInstance().getProvider(providerID);
        if (provider == null) {
            throw new ProcessException("Unable to set the style named \"" + styleName + "\". Provider with id \"" + providerID + "\" not found.", this, null);
        }

        // Add style into provider.
        provider.set(styleName, style);
    }
}
