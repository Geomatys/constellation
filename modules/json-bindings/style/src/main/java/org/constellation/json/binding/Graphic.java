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

import org.opengis.style.GraphicalSymbol;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.opacity;
import static org.constellation.json.util.StyleUtilities.singletonType;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Graphic implements StyleElement<org.opengis.style.Graphic> {

    private double size     = 10.0;
    private double opacity  = 1.0;
    private double rotation = 0.0;
    private Mark mark       = new Mark();

    public Graphic() {
    }

    public Graphic(final org.opengis.style.Graphic graphic) {
        ensureNonNull("graphic", graphic);
        this.size     = Double.valueOf(graphic.getSize().toString());
        this.opacity  = Double.valueOf(graphic.getOpacity().toString());
        this.rotation = Double.valueOf(graphic.getRotation().toString());
        for (final GraphicalSymbol gs : graphic.graphicalSymbols()) {
            if (gs instanceof org.opengis.style.Mark) {
                this.mark = new Mark((org.opengis.style.Mark) gs);
                break;
            }
        }
    }

    public double getSize() {
        return size;
    }

    public void setSize(final double size) {
        this.size = size;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(final double opacity) {
        this.opacity = opacity;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(final double rotation) {
        this.rotation = rotation;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(final Mark mark) {
        this.mark = mark;
    }

    @Override
    public org.opengis.style.Graphic toType() {
        return SF.graphic(
                singletonType(mark),
                opacity(opacity),
                literal(size),
                literal(rotation),
                null,
                null);
    }
}
