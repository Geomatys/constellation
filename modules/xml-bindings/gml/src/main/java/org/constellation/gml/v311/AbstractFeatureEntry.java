/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.gml.v311;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.util.Utilities;


/**
 * The basic feature model is given by the gml:AbstractFeatureType.
 * The content model for gml:AbstractFeatureType adds two specific properties suitable for geographic features to the content model defined in gml:AbstractGMLType. 
 * The value of the gml:boundedBy property describes an envelope that encloses the entire feature instance, and is primarily useful for supporting rapid searching for features that occur in a particular location. 
 * The value of the gml:location property describes the extent, position or relative location of the feature.
 * 
 * @author Guilhem Legal 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractFeatureType", propOrder = {
    "srsName",
    "boundedBy",
    "location"
})
public abstract class AbstractFeatureEntry extends AbstractGMLEntry {

    private List<String> srsName;
    @XmlElement(nillable = true)
    private BoundingShapeEntry boundedBy;
    @XmlElement
    LocationPropertyType location;

    /**
     *  Empty constructor used by JAXB.
     */
    public AbstractFeatureEntry() {}
    
    /**
     * Build a new light "Feature"
     */
    public AbstractFeatureEntry(String id, String name, String description) {
        super(null, name, description, null);
        this.boundedBy = new BoundingShapeEntry("not_bounded");
    }
    
    /**
     * Build a new "Feature"
     */
    public AbstractFeatureEntry(String id, String name, String description, ReferenceEntry descriptionReference,
            BoundingShapeEntry boundedBy, List<String> srsName) {
        super(id, name, description, descriptionReference);
        this.srsName = srsName;
        if (boundedBy == null) {
            this.boundedBy = new BoundingShapeEntry("not_bounded");
        } else { 
            this.boundedBy = boundedBy;
        }
    }
        
    /**
     * Gets the value of the boundedBy property.
     */
    public BoundingShapeEntry getBoundedBy() {
        return boundedBy;
    }

    /**
     * Gets the value of the location property.
     */
    public LocationPropertyType getLocation() {
        return location;
    }
    
    /**
     * Get srs name list
     */
    public List<String> getSrsName(){
        if (srsName == null) {
            srsName = new ArrayList<String>();
        }
        return srsName;
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object) && object instanceof AbstractFeatureEntry) {
            final AbstractFeatureEntry that = (AbstractFeatureEntry) object;
            
            boolean locationEquals = false;
            if (this.location != null && that.location != null) {
                locationEquals = Utilities.equals(this.location, that.location);
            } else {
                locationEquals = (this.location == null && that.location == null);
            }
            return Utilities.equals(this.boundedBy,           that.boundedBy) &&
                   locationEquals;
        } else System.out.println("abstractGML.equals=false");
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.boundedBy != null ? this.boundedBy.hashCode() : 0);
        hash = 23 * hash + (this.location != null ? this.location.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        if (boundedBy != null)
            s.append("boundedBy:").append(boundedBy.toString());
        if (location != null)
            s.append("location:").append(location);
        
        return s.toString();
    }

}
