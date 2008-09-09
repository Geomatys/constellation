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
package net.seagis.ows.v100;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * Connect point URL and any constraints for this HTTP request method for this operation request. In the OnlineResourceType, the xlink:href attribute in the xlink:simpleLink attribute group shall be used to contain this URL. The other attributes in the xlink:simpleLink attribute group should not be used. 
 * 
 * <p>Java class for RequestMethodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestMethodType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows}OnlineResourceType">
 *       &lt;sequence>
 *         &lt;element name="Constraint" type="{http://www.opengis.net/ows}DomainType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestMethodType", propOrder = {
    "constraint"
})
public class RequestMethodType extends OnlineResourceType {

    @XmlElement(name = "Constraint")
    private List<DomainType> constraint;

    /**
     * Empty constructor used by JAXB.
     */
    RequestMethodType(){
    }
    
    /**
     * Build a new Request method.
     */
    public RequestMethodType(List<DomainType> constraint){
        this.constraint = constraint;
    }
    
    /**
     * Gets the value of the constraint property.
     */
    public List<DomainType> getConstraint() {
        if(constraint != null) {
            constraint = new ArrayList<DomainType>();
        }
        return Collections.unmodifiableList(constraint);
    }
    
     /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final RequestMethodType that = (RequestMethodType) object;
        return Utilities.equals(this.constraint, that.constraint);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.constraint != null ? this.constraint.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        String s = super.toString();
        if (constraint!= null) {
            s += "constraint: " + constraint.toString();
        }
        return s;
    }

}
