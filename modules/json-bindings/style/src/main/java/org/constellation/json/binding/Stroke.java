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

import java.awt.*;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.opacity;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Stroke implements StyleElement<org.opengis.style.Stroke> {

    private String color   = "#000000";
    private double opacity = 1.0;
    private double width   = 1.0;
    private boolean dashed = false;
    private String lineJoin = "round";
    private String lineCap = "round";
    private float[] dashArray;
    private String dashOffset;

    public Stroke() {
    }

    public Stroke(final org.opengis.style.Stroke stroke) {
        ensureNonNull("stroke", stroke);
        final Color col = stroke.getColor().evaluate(null, Color.class);
        color = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
        opacity = Double.parseDouble(stroke.getOpacity().toString());
        width   = Double.parseDouble(stroke.getWidth().toString());
        dashed  = (stroke.getDashArray() != null);
        lineJoin = stroke.getLineJoin().evaluate(null, String.class);
        lineCap = stroke.getLineCap().evaluate(null, String.class);
        dashArray = stroke.getDashArray();
        dashOffset = stroke.getDashOffset().evaluate(null, String.class);
    }

    public String getColor() {
        return color;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(final double opacity) {
        this.opacity = opacity;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(final double width) {
        this.width = width;
    }

    public boolean getDashed() {
        return dashed;
    }

    public void setDashed(final boolean dashed) {
        this.dashed = dashed;
    }

    public float[] getDashArray() {
        return dashArray;
    }

    public void setDashArray(float[] dashArray) {
        this.dashArray = dashArray;
    }

    public String getDashOffset() {
        return dashOffset;
    }

    public void setDashOffset(String dashOffset) {
        this.dashOffset = dashOffset;
    }

    public String getLineJoin() {
        return lineJoin;
    }

    public void setLineJoin(String lineJoin) {
        this.lineJoin = lineJoin;
    }

    public String getLineCap() {
        return lineCap;
    }

    public void setLineCap(String lineCap) {
        this.lineCap = lineCap;
    }

    @Override
    public org.opengis.style.Stroke toType() {
        return SF.stroke(
                literal(this.color),
                opacity(opacity),
                literal(this.width),
                literal(this.lineJoin),
                literal(this.lineCap),
                dashArray,
                literal(this.dashOffset));
    }
}
