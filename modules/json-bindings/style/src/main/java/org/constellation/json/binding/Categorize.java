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
import org.geotoolkit.style.function.ThreshholdsBelongTo;
import org.opengis.filter.expression.Expression;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class Categorize implements Function {

    private static final long serialVersionUID = 1L;

    private List<InterpolationPoint> points = new ArrayList<InterpolationPoint>();

    private Double interval;

    private String nanColor;

    public Categorize() {
    }

    @SuppressWarnings("rawtypes")
    public Categorize(final org.geotoolkit.style.function.Categorize categorize) {
        ensureNonNull("categorize", categorize);
        final Map<Expression, Expression> thresholdsMap = categorize.getThresholds();
        if (thresholdsMap != null) {
            for (final Map.Entry<Expression, Expression> entry : thresholdsMap.entrySet()) {
                final InterpolationPoint ip = new InterpolationPoint();
                final Expression expression = entry.getKey();
                final Expression colorHexExp = entry.getValue();

                if (colorHexExp instanceof DefaultLiteral) {
                    final Object colorHex = ((DefaultLiteral) colorHexExp).getValue();
                    ip.setColor(StyleUtilities.toHex((Color) colorHex));
                }

                if (expression instanceof DefaultLiteral) {
                    final Object obj = ((DefaultLiteral) expression).getValue();
                    if (obj instanceof Double) {
                        if (Double.isNaN((double) obj)) {
                            ip.setData(null);
                            nanColor = ip.getColor();
                        } else {
                            ip.setData((Number) obj);
                        }
                    } else if (StyleConstants.CATEGORIZE_LESS_INFINITY.equals(expression)) {
                        continue; //skip for infinity first key it will be restored later.
                    }
                }
                points.add(ip);
            }
            this.interval = (double) categorize.getThresholds().size();
        }
    }


    public List<InterpolationPoint> getPoints() {
        return points;
    }

    public void setPoints(List<InterpolationPoint> points) {
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

        // create first threshold map to create first categorize function.
        Map<Expression, Expression> values = new HashMap<>(0);
        if (nanColor != null) {
            values.put(new DefaultLiteral<Double>(Double.NaN),
                    new DefaultLiteral<Color>(Color.decode(nanColor)));
        }
        values.put(StyleConstants.CATEGORIZE_LESS_INFINITY,
                new DefaultLiteral<Color>(Color.GRAY));
        for (final InterpolationPoint ip : points) {
            values.put(new DefaultLiteral<Double>(ip.getData().doubleValue()),
                    new DefaultLiteral<String>(ip.getColor()));
        }
        return SF.categorizeFunction(StyleConstants.DEFAULT_CATEGORIZE_LOOKUP,
                values,
                ThreshholdsBelongTo.PRECEDING,
                StyleConstants.DEFAULT_FALLBACK);

    }
}
