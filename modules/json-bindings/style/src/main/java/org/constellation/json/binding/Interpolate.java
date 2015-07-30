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
import org.geotoolkit.filter.DefaultLiteral;
import org.geotoolkit.style.StyleConstants;
import org.geotoolkit.style.function.Method;
import org.geotoolkit.style.function.Mode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.listType;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Interpolate implements Function {

    private static final long serialVersionUID = 1L;

    private List<InterpolationPoint> points = new ArrayList<InterpolationPoint>();

    private Double interval;

    private String nanColor;

    public Interpolate() {
    }

    public Interpolate(final org.geotoolkit.style.function.Interpolate interpolate) {
        ensureNonNull("interpolate", interpolate);
        if (interpolate.getInterpolationPoints() != null) {
            for (final org.geotoolkit.style.function.InterpolationPoint point : interpolate.getInterpolationPoints()) {
                this.points.add(new InterpolationPoint(point));
                if (nanColor == null &&
                        point.getData() instanceof Double &&
                        Double.isNaN((double) point.getData())) {
                    if (point.getValue() instanceof DefaultLiteral) {
                        final Object colorHex = ((DefaultLiteral) point.getValue()).getValue();
                        nanColor = StyleUtilities.toHex(((Color) colorHex));
                    }
                }
            }
            this.interval = (double) interpolate.getInterpolationPoints().size();
        }
    }

    public List<InterpolationPoint> getPoints() {
        return points;
    }

    public void setPoints(final List<InterpolationPoint> points) {
        this.points = points;
    }


    public double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }


    public String getNanColor() {
        return nanColor;
    }

    public void setNanColor(String nanColor) {
        this.nanColor = nanColor;
    }

    public org.opengis.filter.expression.Function toType() {

        //remove nan point if exists because it is added later, and it cause error for max/min values
        final List<InterpolationPoint> nullPoints = new ArrayList<>();
        for (final InterpolationPoint ip : points) {
            if (ip.getData() == null) {
                nullPoints.add(ip);
            }
        }
        points.removeAll(nullPoints);
        if (nanColor != null) {
            final InterpolationPoint nanPoint = new InterpolationPoint();
            nanPoint.setColor(nanColor);
            nanPoint.setData(Double.NaN);
            points.add(nanPoint);
        }
        return SF.interpolateFunction(StyleConstants.DEFAULT_CATEGORIZE_LOOKUP,
                listType(points),
                Method.COLOR,
                Mode.LINEAR,
                StyleConstants.DEFAULT_FALLBACK);

    }

}
