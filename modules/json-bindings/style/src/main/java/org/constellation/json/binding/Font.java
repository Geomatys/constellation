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
import static org.constellation.json.util.StyleUtilities.literal;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Font implements StyleElement<org.opengis.style.Font> {

    private double size    = 12;
    private boolean bold   = false;
    private boolean italic = false;

    public Font() {
    }

    public Font(org.opengis.style.Font font) {
        ensureNonNull("font", font);
        if (font.getSize() != null) {
            size = Double.parseDouble(font.getSize().toString());
        }
        if (font.getWeight() != null) {
            bold = "bold".equals(font.getWeight().toString());
        }
        if (font.getStyle() != null) {
            italic = "italic".equals(font.getStyle().toString());
        }
    }

    public double getSize() {
        return size;
    }

    public void setSize(final double size) {
        this.size = size;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(final boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(final boolean italic) {
        this.italic = italic;
    }

    @Override
    public org.opengis.style.Font toType() {
        return SF.font(
                literal("Arial"),
                literal(this.italic ? "italic" : "normal"),
                literal(this.bold ? "bold" : "normal"),
                literal(this.size));
    }
}
