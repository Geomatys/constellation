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

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(Fill.class);

    private String color   = "#555555";
    private double opacity = 1.0;

    public Fill() {
    }

    public Fill(final org.opengis.style.Fill fill) {
        ensureNonNull("fill", fill);
        final Color col = fill.getColor().evaluate(null, Color.class);
        color = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
        final Expression opacityExp = fill.getOpacity();
        if(opacityExp != null){
            final String opacityStr = CQL.write(opacityExp);
            try {
                opacity = Double.parseDouble(opacityStr);
            }catch (NumberFormatException ex){
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
            }
        }
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
