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

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.cql.CQL;
import org.opengis.filter.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.parseExpression;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Font implements StyleElement<org.opengis.style.Font> {

    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(Font.class);

    private String size    = "12";
    private boolean bold   = false;
    private boolean italic = false;
    private List<String> family = new ArrayList<>();

    public Font() {
    }

    public Font(org.opengis.style.Font font) {
        ensureNonNull("font", font);

        final Expression sizeExp = font.getSize();
        if(sizeExp != null){
            size = CQL.write(sizeExp);
        }
        final Expression weightExp = font.getWeight();
        if (weightExp != null) {
            bold = "bold".equals(CQL.write(weightExp));
        }
        final Expression styleExp = font.getStyle();
        if (styleExp != null) {
            italic = "italic".equals(CQL.write(styleExp));
        }
        for (final Expression fam : font.getFamily()) {
            family.add(fam.evaluate(null, String.class));
        }
    }

    public String getSize() {
        return size;
    }

    public void setSize(final String size) {
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
                parseExpression(this.size));
    }
}
