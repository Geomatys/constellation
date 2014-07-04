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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Layers {

    @XmlElement(name="MainLayer")
    private Layer mainLayer;

    @XmlElement(name="Source")
    private List<Source> source;

    public Layers() {

    }

    public Layers(final List<Source> source) {
        this.source = source;
    }

    public Layers(final Layer mainLayer, final List<Source> source) {
        this.source    = source;
        this.mainLayer = mainLayer;
    }

    /**
     * @return the source
     */
    public List<Source> getSource() {
        if (source == null) {
            source = new ArrayList<Source>();
        }
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(final List<Source> source) {
        this.source = source;
    }

    /**
     * @return the mainLayer
     */
    public Layer getMainLayer() {
        return mainLayer;
    }

    /**
     * @param mainLayer the mainLayer to set
     */
    public void setMainLayer(final Layer mainLayer) {
        this.mainLayer = mainLayer;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Layers) {
            final Layers that = (Layers) obj;
            return Objects.equals(this.mainLayer, that.mainLayer) &&
                   Objects.equals(this.source, that.source);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.mainLayer != null ? this.mainLayer.hashCode() : 0);
        hash = 37 * hash + (this.source != null ? this.source.hashCode() : 0);
        return hash;
    }
}
