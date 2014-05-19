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
package org.constellation.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class LayerList {

    @XmlElement(name="Layer")
    private List<Layer> layer = new ArrayList<>();

    public LayerList() {

    }

    public LayerList(List<Layer> layer) {
        this.layer = layer;
    }

    /**
     * @return the layer
     */
    public List<Layer> getLayer() {
        if (layer == null) {
            layer = new ArrayList<>();
        }
        return layer;
    }

    /**
     * @param layer the layer to set
     */
    public void setLayer(List<Layer> layer) {
        this.layer = layer;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof LayerList) {
            final LayerList that = (LayerList) obj;
            return Objects.equals(this.layer, that.layer);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.layer != null ? this.layer.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LayerList\n");
        if (layer != null && !layer.isEmpty()) {
            for (Layer l: layer) {
                sb.append(l).append('\n');
            }
        }
        return sb.toString();
    }
}
