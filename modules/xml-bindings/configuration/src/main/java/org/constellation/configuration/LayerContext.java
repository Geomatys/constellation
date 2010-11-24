/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.configuration;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlRootElement(name="LayerContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerContext {

    private Layers layers;

    public LayerContext() {

    }

    public LayerContext(Layers layers) {
        this.layers = layers;
    }

    /**
     * @return the layers
     */
    public List<Source> getLayers() {
        if (layers == null) {
            layers = new Layers();
            return layers.getSource();
        } else {
            return layers.getSource();
        }
    }


    /**
     * @param layers the layers to set
     */
    public void setLayers(List<Source> layers) {
        this.layers = new Layers(layers);
    }

    /**
     * @return the layers
     */
    public Layer getMainLayer() {
        if (layers == null) {
            layers = new Layers();
            return layers.getMainLayer();
        } else {
            return layers.getMainLayer();
        }
    }
}
