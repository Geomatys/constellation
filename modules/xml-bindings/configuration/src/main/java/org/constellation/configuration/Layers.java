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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
