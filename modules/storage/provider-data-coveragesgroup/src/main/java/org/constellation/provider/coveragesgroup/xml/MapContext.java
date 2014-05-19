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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Main element for this binding. A {@linkplain MapContext map context}
 * should contain a mandatory {@linkplain MapItem map item}.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "mapItem"
})
@XmlRootElement(name = "MapContext")
public class MapContext {

    @XmlElement
    private String name;

    @XmlElement(name = "MapItem", required = true)
    private MapItem mapItem;

    /**
     * An empty constructor used by JAXB.
     */
    public MapContext() {
    }

    public MapContext(final MapItem mapItem) {
        this(mapItem, null);
    }

    public MapContext(final MapItem mapItem, final String name) {
        this.mapItem = mapItem;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public MapItem getMapItem() {
        return mapItem;
    }

    public void setMapItem(final MapItem mapItem) {
        this.mapItem = mapItem;
    }
}
