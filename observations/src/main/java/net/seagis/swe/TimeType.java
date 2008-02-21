/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.swe;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Time", propOrder = {
    "uom",
    "value"
})
public class TimeType extends AbstractDataComponentEntry {

    private UomPropertyType uom;
    //private AllowedTimesPropertyType constraint;
    //private QualityPropertyType quality;
    @XmlList
    private List<String> value;
    @XmlAttribute
    private String localFrame;
    @XmlAttribute
    private String referenceFrame;
    @XmlAttribute
    private String referenceTime;

    /**
     * A empty contructor used by JAXB
     */
    public TimeType() {
        
    }
    
    /**
     * Build a new TimeType
     */
    public TimeType(String definition, String uomCode, String uomHref) {
        super(null, definition, false);
        this.uom = new UomPropertyType(uomCode, uomHref);
        
    }
    
    /**
     * Gets the value of the uom property.
     */
    public UomPropertyType getUom() {
        return uom;
    }

    /**
     * Gets the value of the value property.
     * 
     */
    public List<String> getValue() {
        if (value == null) {
            value = new ArrayList<String>();
        }
        return this.value;
    }

    /**
     * Gets the value of the localFrame property.
     */
    public String getLocalFrame() {
        return localFrame;
    }

    /**
     * Gets the value of the referenceFrame property.
     */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * Gets the value of the referenceTime property.
     */
    public String getReferenceTime() {
        return referenceTime;
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
        final TimeType that = (TimeType) object;
        return Utilities.equals(this.localFrame,     that.localFrame)     &&
               Utilities.equals(this.referenceFrame, that.referenceFrame) &&
               Utilities.equals(this.referenceTime,  that.referenceTime)  &&
               Utilities.equals(this.uom,            that.uom)            &&
               Utilities.equals(this.value,          that.value);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.uom != null ? this.uom.hashCode() : 0);
        hash = 61 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 61 * hash + (this.localFrame != null ? this.localFrame.hashCode() : 0);
        hash = 61 * hash + (this.referenceFrame != null ? this.referenceFrame.hashCode() : 0);
        hash = 61 * hash + (this.referenceTime != null ? this.referenceTime.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[TimeType]").append('\n').append("super:").append(super.toString()).append('\n');
        s.append("localFrame:").append(localFrame).append('\n');
        s.append("referenceFrame:").append(referenceFrame).append('\n');
        s.append("referenceTime:").append(referenceTime).append('\n');
        s.append("value:").append('\n');
        for (String ss:value){
            s.append(ss).append('\n');
        }
        if (uom != null) {
            s.append("uom: ").append(uom.toString());
        }
        return s.toString();
    }
    
}
