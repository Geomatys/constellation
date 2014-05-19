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
