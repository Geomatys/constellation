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

package org.constellation.gui.binding;

import juzu.Mapped;
import org.opengis.filter.expression.Literal;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.gui.util.StyleFactories.FF;
import static org.constellation.gui.util.StyleFactories.SF;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Mapped
public class Mark implements StyleElement<org.opengis.style.GraphicalSymbol> {

    private String geometry = "circle";
    private Stroke stroke   = new Stroke();
    private Fill fill       = new Fill();

    public Mark() {
    }

    public Mark(final org.opengis.style.Mark mark) {
        ensureNonNull("mark", mark);
        geometry = mark.getWellKnownName().toString();
        fill     = new Fill(mark.getFill());
        stroke   = new Stroke(mark.getStroke());
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(final String geometry) {
        this.geometry = geometry;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(final Stroke stroke) {
        this.stroke = stroke;
    }

    public Fill getFill() {
        return fill;
    }

    public void setFill(final Fill fill) {
        this.fill = fill;
    }

    @Override
    public org.opengis.style.GraphicalSymbol toType() {
        final Literal geometry                = FF.literal(this.geometry);
        final org.opengis.style.Fill fill     = this.fill.toType();
        final org.opengis.style.Stroke stroke = this.stroke.toType();
        return SF.mark(geometry, fill, stroke);
    }
}
