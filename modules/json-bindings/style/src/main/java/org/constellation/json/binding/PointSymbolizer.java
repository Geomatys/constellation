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
public final class PointSymbolizer implements Symbolizer {

    private Graphic graphic = new Graphic();

    public PointSymbolizer() {
    }

    public PointSymbolizer(org.opengis.style.PointSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        if (symbolizer.getGraphic() != null) {
            graphic = new Graphic(symbolizer.getGraphic());
        }
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public void setGraphic(final Graphic graphic) {
        this.graphic = graphic;
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        return SF.pointSymbolizer(type(graphic), null);
    }
}
