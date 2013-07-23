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

package org.constellation.gui.binding;

import juzu.Mapped;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Mapped
public class Stroke implements Serializable {

    private String color   = "#000000";
    private double opacity = 1.0;
    private double width   = 1.0;
    private boolean dashed = false;

    public Stroke() {
    }

    public Stroke(final org.opengis.style.Stroke stroke) {
        if (stroke != null) {
            color   = stroke.getColor().toString();
            opacity = Double.parseDouble(stroke.getOpacity().toString());
            width   = Double.parseDouble(stroke.getWidth().toString());
            dashed  = (stroke.getDashArray() != null);
        }
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
}
