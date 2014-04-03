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
public final class PolygonSymbolizer implements Symbolizer {

    private Stroke stroke = new Stroke();
    private Fill fill     = new Fill();

    public PolygonSymbolizer() {
    }

    public PolygonSymbolizer(final org.opengis.style.PolygonSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        if (symbolizer.getStroke() != null) {
            stroke = new Stroke(symbolizer.getStroke());
        }
        if (symbolizer.getFill() != null) {
            fill = new Fill(symbolizer.getFill());
        }
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
    public org.opengis.style.Symbolizer toType() {
        return SF.polygonSymbolizer(type(stroke), type(fill), null);
    }
}
