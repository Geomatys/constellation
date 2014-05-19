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

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.type;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Mark implements StyleElement<org.opengis.style.GraphicalSymbol> {

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
        return SF.mark(literal(geometry), type(fill), type(stroke));
    }
}
