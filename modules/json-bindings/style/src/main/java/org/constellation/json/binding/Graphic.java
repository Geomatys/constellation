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

import org.constellation.json.util.StyleUtilities;
import org.opengis.filter.expression.Expression;
import org.opengis.style.GraphicalSymbol;

import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Graphic implements StyleElement<org.opengis.style.Graphic> {

    private String size     = "10.0";
    private String opacity  = "1.0";
    private String rotation = "0.0";
    private Mark mark       = new Mark();
    private ExternalGraphic externalGraphic;

    public Graphic() {
    }

    public Graphic(final org.opengis.style.Graphic graphic) {
        ensureNonNull("graphic", graphic);
        final Expression sizeExp = graphic.getSize();
        if(sizeExp!=null){
            this.size = StyleUtilities.toCQL(sizeExp);
        }
        final Expression opacityExp = graphic.getOpacity();
        if(opacityExp!=null){
            this.opacity = StyleUtilities.toCQL(opacityExp);
        }
        final Expression rotationExp = graphic.getRotation();
        if(rotationExp!=null){
            this.rotation = StyleUtilities.toCQL(rotationExp);
        }
        for (final GraphicalSymbol gs : graphic.graphicalSymbols()) {
            if (gs instanceof org.opengis.style.Mark) {
                this.mark = new Mark((org.opengis.style.Mark) gs);
                break;
            }
            if (gs instanceof org.opengis.style.ExternalGraphic) {
                this.externalGraphic = new ExternalGraphic((org.opengis.style.ExternalGraphic) gs);
                break;
            }
        }
    }

    public String getSize() {
        return size;
    }

    public void setSize(final String size) {
        this.size = size;
    }

    public String getOpacity() {
        return opacity;
    }

    public void setOpacity(final String opacity) {
        this.opacity = opacity;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(final String rotation) {
        this.rotation = rotation;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(final Mark mark) {
        this.mark = mark;
    }

    public ExternalGraphic getExternalGraphic() {
        return externalGraphic;
    }

    public void setExternalGraphic(ExternalGraphic externalGraphic) {
        this.externalGraphic = externalGraphic;
    }

    @Override
    public org.opengis.style.Graphic toType() {
        final List<GraphicalSymbol> graphicalSymbols = new ArrayList<>();

        if(externalGraphic != null) {
            graphicalSymbols.add(externalGraphic.toType());
        }
        if(mark != null) {
            graphicalSymbols.add(mark.toType());
        }
        return SF.graphic(
                graphicalSymbols,
                StyleUtilities.parseExpression(opacity),
                StyleUtilities.parseExpression(size),
                StyleUtilities.parseExpression(rotation),
                null,
                null);
    }
}
