/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.swe.v101;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Boolean", propOrder = {
    "value"
})
public class BooleanType extends AbstractDataComponentEntry {

    protected java.lang.Boolean value;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String axisID;
    @XmlAttribute
    protected String referenceFrame;

    /**
     * A empty contructor used by JAXB
     */
    public BooleanType() {
        
    }
    
    /**
     * Build a new TimeType
     */
    public BooleanType(String definition, java.lang.Boolean value) {
        super(null, definition, false);
        this.value = value;
        
    }
    
    /**
     * Gets the value of the value property.
     */
    public java.lang.Boolean isValue() {
        return value;
    }

    /**
     * Gets the value of the axisID property.
     */
    public String getAxisID() {
        return axisID;
    }

    /**
     * Gets the value of the referenceFrame property.
     */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
        final BooleanType that = (BooleanType) object;
        return Utilities.equals(this.referenceFrame, that.referenceFrame) &&
               Utilities.equals(this.axisID,         that.axisID)  &&
               Utilities.equals(this.value,          that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 11 * hash + (this.axisID != null ? this.axisID.hashCode() : 0);
        hash = 11 * hash + (this.referenceFrame != null ? this.referenceFrame.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("[BooleanType]").append('\n').append("super:").append(super.toString()).append('\n');
        if (axisID != null)
            s.append("axisId:").append(axisID).append('\n');
        if (referenceFrame != null)
            s.append("referenceFrame:").append(referenceFrame).append('\n');
        if (value != null)
            s.append("value:").append(value).append('\n');
        
        return s.toString();
    }

}
