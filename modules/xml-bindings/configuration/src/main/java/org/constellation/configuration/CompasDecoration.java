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

import java.awt.Dimension;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.constellation.configuration.utils.WMSPortrayalUtils;
import org.geotoolkit.display2d.ext.northarrow.DefaultNorthArrowTemplate;
import org.geotoolkit.display2d.ext.northarrow.NorthArrowTemplate;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CompasDecoration extends PositionableDecoration {

    @XmlElement(name = "Source")
    private String source;
    @XmlAttribute(name = "width")
    private Integer width;
    @XmlAttribute(name = "height")
    private Integer height;

    public CompasDecoration() {
        super();
        this.height = 30;
        this.width = 30;
        this.source = CompasDecoration.class.getResource("/org/geotoolkit/icon/boussole.svg").toString();
    }

    public CompasDecoration(final String source, final Integer width, final Integer height, final Background background,
            final Integer offsetX, final Integer offsetY, final String position) {
        super(background, offsetX, offsetY, position);
        this.source = source;
        this.width = width;
        this.height = height;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public NorthArrowTemplate toNorthArrowTemplate() {
        return new DefaultNorthArrowTemplate(
                getBackground().toBackgroundTemplate(),
                WMSPortrayalUtils.parseURL(source, CompasDecoration.class.getResource("/org/geotoolkit/icon/boussole.svg")),
                new Dimension(
                width != null ? width : 100,
                height != null ? height : 100));
    }
}
