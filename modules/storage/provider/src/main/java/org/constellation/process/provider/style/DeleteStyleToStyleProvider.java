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
import org.opengis.parameter.ParameterValueGroup;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.constellation.process.provider.style.DeleteStyleToStyleProviderDescriptor.*;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * Remove a style from an existing style provider.
 *
 * @author Quentin Boileau (Geomatys).
 * @author Bernard Fabien (Geomatys).
 */
public class DeleteStyleToStyleProvider extends AbstractCstlProcess {

    public DeleteStyleToStyleProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * @throws ProcessException if the provider or the style can't be found
     */
    @Override
    protected void execute() throws ProcessException {
        final String providerID = value(PROVIDER_ID, inputParameters); // required
        final String styleName  = value(STYLE_ID,    inputParameters); // required

        if (isBlank(styleName)) {
            throw new ProcessException("Unable to delete the style named \"" + styleName + "\". Style name can't be empty/blank.", this, null);
        }

        // Retrieve or not the provider instance.
        final StyleProvider provider = StyleProviders.getInstance().getProvider(providerID);
        if (provider == null) {
            throw new ProcessException("Unable to delete the style named \"" + styleName + "\". Provider with id \"" + providerID + "\" not found.", this, null);
        }

        // Remove style from provider.
        provider.remove(styleName);
    }
}
