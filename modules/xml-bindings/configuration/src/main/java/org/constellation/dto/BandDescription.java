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

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class BandDescription implements Serializable {

    private String indice;
    private String name;
    private double minValue;
    private double maxValue;
    private double[] noDataValues;

    public BandDescription() {
    }

    public BandDescription(final String indice, final String name, final double minValue, final double maxValue, final double[] noDataValues) {
        this.indice         = indice;
        this.name           = name;
        this.minValue       = minValue;
        this.maxValue       = maxValue;
        this.noDataValues   = noDataValues;
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

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }
}
