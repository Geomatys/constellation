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

import org.opengis.filter.expression.Expression;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> family = new ArrayList<>();

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
        for (final Expression fam : font.getFamily()) {
            family.add(fam.evaluate(null, String.class));
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

    public List<String> getFamily() {
        return family;
    }

    public void setFamily(List<String> family) {
        this.family = family;
    }

    @Override
    public org.opengis.style.Font toType() {
        final List<Expression> famExp = new ArrayList<>();
        for (final String fam : family) {
            famExp.add(literal(fam));
        }
        if (famExp.isEmpty()) {
            famExp.add(literal("Arial"));
        }
        return SF.font(
                famExp,
                literal(this.italic ? "italic" : "normal"),
                literal(this.bold ? "bold" : "normal"),
                literal(this.size));
    }
}
