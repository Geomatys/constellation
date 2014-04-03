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
import static org.constellation.json.util.StyleUtilities.expression;
import static org.constellation.json.util.StyleUtilities.type;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class TextSymbolizer implements Symbolizer {

    private String label = null;
    private Font font    = new Font();
    private Fill fill    = new Fill();

    public TextSymbolizer() {
    }

    public TextSymbolizer(final org.opengis.style.TextSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        if (symbolizer.getLabel() != null) {
            this.label = symbolizer.getLabel().toString();
        }
        if (symbolizer.getFont() != null) {
            this.font = new Font(symbolizer.getFont());
        }
        if (symbolizer.getFill() != null) {
            this.fill = new Fill(symbolizer.getFill());
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(final Font font) {
        this.font = font;
    }

    public Fill getFill() {
        return fill;
    }

    public void setFill(final Fill fill) {
        this.fill = fill;
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        return SF.textSymbolizer(
                type(fill),
                type(font),
                null,
                expression(label),
                null,
                null);
    }
}
