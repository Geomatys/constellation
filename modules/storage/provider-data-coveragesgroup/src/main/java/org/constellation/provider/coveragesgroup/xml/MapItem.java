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
package org.constellation.provider.coveragesgroup.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Can be assimilated as a group of layer. Can contain other {@linkplain MapItem groups}
 * or {@linkplain MapLayer layers} directly.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "mapItems"
})
@XmlRootElement(name = "MapItem")
public class MapItem {
    @XmlElements({
        @XmlElement(name = "MapItem", type = MapItem.class),
        @XmlElement(name = "MapLayer", type = MapLayer.class)
    })
    private List<MapItem> mapItems = new ArrayList<MapItem>();

    public MapItem() {
    }

    public MapItem(final List<MapItem> mapItems) {
        this.mapItems = mapItems;
    }

    public List<MapItem> getMapItems() {
        if (mapItems == null) {
            mapItems = new ArrayList<MapItem>();
        }
        return mapItems;
    }

    public void setMapItem(final List<MapItem> mapItems) {
        this.mapItems = mapItems;
    }
}
