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
package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.capability.IdCapabilities;


/**
 * <p>Java class for Id_CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Id_CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://www.opengis.net/ogc}EID"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}FID"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Id_CapabilitiesType", propOrder = {
    "eid",
    "fid"
})
public class IdCapabilitiesType implements IdCapabilities {

    @XmlElement(name = "EID")
    private EID eid;
    
    @XmlElement(name = "FID")
    private FID fid;

    /**
     * Empty constructor used By JAXB
     */
     IdCapabilitiesType() {
        
     }
     
    /**
     * Build a new ID capabilities
     */
     public IdCapabilitiesType(boolean eid, boolean fid) {
        if (eid)
            this.eid = new EID();
        if (fid)
            this.fid = new FID();
     }
     
    /**
     * Gets the value of the eid property.
     */
    public EID getEID() {
        return eid;
    }
    
    /**
     * Gets the value of the eid property.
     */
    public FID getFID() {
        return fid;
    }

    public boolean hasEID() {
        return eid != null;
    }

    public boolean hasFID() {
        return fid != null;
    }

}
