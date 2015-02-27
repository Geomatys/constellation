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


import org.geotoolkit.se.xml.v110.DescriptionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bgarcia on 26/02/15.
 */
public class DynamicRangeSymbolizer implements Symbolizer {

    /**
     * Channels lists
     */
    public List<DynamicRangeChannel> channels = new ArrayList<>(0);

    /**
     * Symbolizer description
     */
    private DescriptionType description;

    /**
     * Symbolizer name
     */
    private String name;

    public DynamicRangeSymbolizer() {
    }

    /**
     * constructor to map with geotk Object
     * @param symbolizer {@link org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer} object from geotk
     */
    public DynamicRangeSymbolizer(org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer symbolizer) {
        name = symbolizer.getName();
        description = symbolizer.getDescription();
        channels = new ArrayList<>(0);
        for (org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer.DRChannel channel : symbolizer.getChannels()) {
            channels.add(new DynamicRangeChannel(channel));
        }
    }

    /**
     * @see StyleElement#toType()
     */
    @Override
    public org.opengis.style.Symbolizer toType() {
        org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer symbolizer = new org.geotoolkit.display2d.ext.dynamicrange.DynamicRangeSymbolizer();
        if(channels!=null){
            for (DynamicRangeChannel channel : channels) {
                symbolizer.getChannels().add(channel.toType());
            }
        }
        symbolizer.setDescription(description);
        symbolizer.setName(name);
        return symbolizer;
    }


    /************************************
     *                                  *
     *          GETTER/SETTER           *
     *                                  *
     ************************************/

    public List<DynamicRangeChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<DynamicRangeChannel> channels) {
        this.channels = channels;
    }
}
