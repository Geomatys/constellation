/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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
