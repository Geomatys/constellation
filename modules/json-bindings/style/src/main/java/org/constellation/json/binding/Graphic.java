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

import org.opengis.style.GraphicalSymbol;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.opacity;
import static org.constellation.json.util.StyleUtilities.singletonType;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Graphic implements StyleElement<org.opengis.style.Graphic> {

    private double size     = 10.0;
    private double opacity  = 1.0;
    private double rotation = 0.0;
    private Mark mark       = new Mark();

    public Graphic() {
    }

    public Graphic(final org.opengis.style.Graphic graphic) {
        ensureNonNull("graphic", graphic);
        this.size     = Double.valueOf(graphic.getSize().toString());
        this.opacity  = Double.valueOf(graphic.getOpacity().toString());
        this.rotation = Double.valueOf(graphic.getRotation().toString());
        for (final GraphicalSymbol gs : graphic.graphicalSymbols()) {
            if (gs instanceof org.opengis.style.Mark) {
                this.mark = new Mark((org.opengis.style.Mark) gs);
                break;
            }
        }
    }

    public double getSize() {
        return size;
    }

    public void setSize(final double size) {
        this.size = size;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(final double opacity) {
        this.opacity = opacity;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(final double rotation) {
        this.rotation = rotation;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(final Mark mark) {
        this.mark = mark;
    }

    @Override
    public org.opengis.style.Graphic toType() {
        return SF.graphic(
                singletonType(mark),
                opacity(opacity),
                literal(size),
                literal(rotation),
                null,
                null);
    }
}
