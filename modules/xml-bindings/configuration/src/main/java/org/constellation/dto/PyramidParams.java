/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Cédric Briançon (Geomatys)
 */
@XmlRootElement
public class PyramidParams {
    private String tileFormat;
    private String crs;
    private double[] scales;
    private Double upperCornerX;
    private Double upperCornerY;

    public PyramidParams() {}

    public String getTileFormat() {
        return tileFormat;
    }

    public void setTileFormat(String tileFormat) {
        this.tileFormat = tileFormat;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public double[] getScales() {
        return scales;
    }

    public void setScales(double[] scales) {
        this.scales = scales;
    }

    public Double getUpperCornerX() {
        return upperCornerX;
    }

    public void setUpperCornerX(Double upperCornerX) {
        this.upperCornerX = upperCornerX;
    }

    public Double getUpperCornerY() {
        return upperCornerY;
    }

    public void setUpperCornerY(Double upperCornerY) {
        this.upperCornerY = upperCornerY;
    }
}
