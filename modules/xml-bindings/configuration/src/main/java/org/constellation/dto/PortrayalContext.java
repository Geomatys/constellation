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
import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public final class PortrayalContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String providerId;
    private String dataName;
    private String projection;
    private double west;
    private double south;
    private double east;
    private double north;
    private int height;
    private int width;
    private String format;
    private String styleBody;
    private String sldVersion;
    private boolean lonFirstOutput;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(final String dataName) {
        this.dataName = dataName;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(final String projection) {
        this.projection = projection;
    }

    public double getWest() {
        return west;
    }

    public void setWest(final double west) {
        this.west = west;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(final double south) {
        this.south = south;
    }

    public double getEast() {
        return east;
    }

    public void setEast(final double east) {
        this.east = east;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(final double north) {
        this.north = north;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public String getStyleBody() {
        return styleBody;
    }

    public void setStyleBody(final String styleBody) {
        this.styleBody = styleBody;
    }

    public String getSldVersion() {
        return sldVersion;
    }

    public void setSldVersion(final String sldVersion) {
        this.sldVersion = sldVersion;
    }

    public boolean isLonFirstOutput() {
        return lonFirstOutput;
    }

    public void setLonFirstOutput(final boolean lonFirstOutput) {
        this.lonFirstOutput = lonFirstOutput;
    }
}
