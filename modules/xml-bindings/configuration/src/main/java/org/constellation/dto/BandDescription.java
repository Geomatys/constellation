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

package org.constellation.dto;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class BandDescription implements Serializable {

    private String name;
    private double minValue;
    private double maxValue;
    private double[] noDataValues;

    public BandDescription() {
    }

    public BandDescription(final String name, final double minValue, final double maxValue, final double[] noDataValues) {
        this.name         = name;
        this.minValue     = minValue;
        this.maxValue     = maxValue;
        this.noDataValues = noDataValues;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(final double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(final double maxValue) {
        this.maxValue = maxValue;
    }

    public double[] getNoDataValues() {
        return noDataValues;
    }

    public void setNoDataValues(final double[] noDataValues) {
        this.noDataValues = noDataValues;
    }
}
