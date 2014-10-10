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
public final class PolygonSymbolizer implements Symbolizer {
    private String name;
    private Stroke stroke = new Stroke();
    private Fill fill     = new Fill();
    private double perpendicularOffset;

    public PolygonSymbolizer() {
    }

    public PolygonSymbolizer(final org.opengis.style.PolygonSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        name = symbolizer.getName();
        if (symbolizer.getStroke() != null) {
            stroke = new Stroke(symbolizer.getStroke());
        }
        if (symbolizer.getFill() != null) {
            fill = new Fill(symbolizer.getFill());
        };
        try{
            perpendicularOffset = Double.parseDouble(symbolizer.getPerpendicularOffset().evaluate(null, String.class));
        }catch(Exception ex){
            //do nothing
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getPerpendicularOffset() {
        return perpendicularOffset;
    }

    public void setPerpendicularOffset(String perpendicularOffset) {
        try{
            this.perpendicularOffset = Double.parseDouble(perpendicularOffset);
        }catch(Exception ex){
            //do nothing
        }
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        return SF.polygonSymbolizer(name,
                (String)null,
                null,
                null,
                type(stroke),
                type(fill),
                null,
                literal(this.perpendicularOffset));
    }
}
