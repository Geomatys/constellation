/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import javax.measure.unit.Unit;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.geotoolkit.display2d.ext.scalebar.DefaultScaleBarTemplate;
import org.geotoolkit.display2d.ext.scalebar.ScaleBarTemplate;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ScaleBarDecoration extends PositionableDecoration {

    @XmlAttribute(name = "width")
    private Integer width;
    @XmlAttribute(name = "height")
    private Integer height;
    @XmlAttribute(name = "units")
    private String units;

    public ScaleBarDecoration() {
        super();
        this.width = 100;
        this.height = 20;
        this.units = "km";
    }

    public ScaleBarDecoration(final Integer width, final Integer height, final String units, final Background background,
            final Integer offsetX, final Integer offsetY, final String position) {
        super(background, offsetX, offsetY, position);
        this.width = width;
        this.height = height;
        this.units = units;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public ScaleBarTemplate toScaleBarTemplate() {
        String unit = units;
        if (unit == null || unit.isEmpty()) {
            unit = "km";
        }
        final Dimension dim = new Dimension(width != null ? width : 250, height != null ? height : 250);
        return new DefaultScaleBarTemplate(
                getBackground().toBackgroundTemplate(),
                dim,
                10,
                false,
                5,
                NumberFormat.getNumberInstance(),
                Color.BLACK,
                Color.BLACK,
                Color.WHITE,
                3,
                true,
                false,
                new Font("Serial", Font.PLAIN, 12),
                true,
                Unit.valueOf(unit));
    }
}