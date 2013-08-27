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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public class FeatureDataDescription implements DataDescription {

    private double[] boundingBox;
    private PropertyDescription geometryProperty;
    private List<PropertyDescription> properties;

    public FeatureDataDescription() {
        this.boundingBox = new double[]{-180,-90,180,90};
        this.properties = new ArrayList<PropertyDescription>(0);
    }

    public PropertyDescription getGeometryProperty() {
        return geometryProperty;
    }

    public void setGeometryProperty(final PropertyDescription geometryProperty) {
        this.geometryProperty = geometryProperty;
    }

    public List<PropertyDescription> getProperties() {
        return properties;
    }

    public void setProperties(final List<PropertyDescription> properties) {
        this.properties = properties;
    }

    @Override
    public double[] getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void setBoundingBox(final double[] boundingBox) {
        this.boundingBox = boundingBox;
    }
}
