/*
 * Constellation - An open source and standard compliant SDI
 * http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.json.binding;


import org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer;

import java.io.Serializable;

/**
 * Created by bgarcia on 27/02/15.
 */
public class DynamicRangeChannel implements Serializable {

    private String band;
    private String colorSpaceComponent;
    private DynamicRangeBounds lower;
    private DynamicRangeBounds upper;

    public DynamicRangeChannel() {
    }

    public DynamicRangeChannel(DynamicRangeSymbolizer.DRChannel channel) {
        band = channel.getBand();
        colorSpaceComponent = channel.getBand();
        lower = new DynamicRangeBounds(channel.getLower());
        upper = new DynamicRangeBounds(channel.getUpper());
    }


    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public String getColorSpaceComponent() {
        return colorSpaceComponent;
    }

    public void setColorSpaceComponent(String colorSpaceComponent) {
        this.colorSpaceComponent = colorSpaceComponent;
    }

    public DynamicRangeBounds getLower() {
        return lower;
    }

    public void setLower(DynamicRangeBounds lower) {
        this.lower = lower;
    }

    public DynamicRangeBounds getUpper() {
        return upper;
    }

    public void setUpper(DynamicRangeBounds upper) {
        this.upper = upper;
    }

    public DynamicRangeSymbolizer.DRChannel toType(){
        DynamicRangeSymbolizer.DRChannel channel = new DynamicRangeSymbolizer.DRChannel();
        channel.setBand(band);
        channel.setColorSpaceComponent(colorSpaceComponent);
        channel.setLower(lower.toType());
        channel.setUpper(upper.toType());
        return channel;
    }
}
