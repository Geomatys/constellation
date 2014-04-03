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

import java.awt.*;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.opacity;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Fill implements StyleElement<org.opengis.style.Fill> {

    private String color   = "#555555";
    private double opacity = 1.0;

    public Fill() {
    }

    public Fill(final org.opengis.style.Fill fill) {
        ensureNonNull("fill", fill);
        final Color col = fill.getColor().evaluate(null, Color.class);
        color = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
        opacity = Double.parseDouble(fill.getOpacity().toString());
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(final double opacity) {
        this.opacity = opacity;
    }

    @Override
    public org.opengis.style.Fill toType() {
        return SF.fill(literal(color), opacity(opacity));
    }
}
