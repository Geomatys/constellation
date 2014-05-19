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

package org.constellation.json.binding;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.type;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ColorMap implements StyleElement<org.opengis.style.ColorMap> {

    private Function function = null;

    public ColorMap() {
    }

    public ColorMap(final org.opengis.style.ColorMap colorMap) {
        ensureNonNull("colorMap", colorMap);
        if (colorMap.getFunction() != null) {
            if (colorMap.getFunction() instanceof org.geotoolkit.style.function.Interpolate) {
                this.function = new Interpolate((org.geotoolkit.style.function.Interpolate) colorMap.getFunction());
            }
        }
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(final Function function) {
        this.function = function;
    }

    @Override
    public org.opengis.style.ColorMap toType() {
        return SF.colorMap(type(function));
    }
}
