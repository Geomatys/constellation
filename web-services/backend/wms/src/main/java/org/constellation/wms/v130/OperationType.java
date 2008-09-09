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
package net.seagis.wms.v130;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.wms.AbstractOperation;


/**
 * 
 *         For each operation offered by the server, list the available output
 *         formats and the online resource.
 *       
 * 
 * <p>Java class for OperationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OperationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wms}Format" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.opengis.net/wms}DCPType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OperationType", propOrder = {
    "format",
    "dcpType"
})
public class OperationType extends AbstractOperation {

    @XmlElement(name = "Format", required = true)
    private List<String> format   = new ArrayList<String>();
    @XmlElement(name = "DCPType", required = true)
    private List<DCPType> dcpType = new ArrayList<DCPType>();

    /**
     * An empty constructor used by JAXB.
     */
     OperationType() {
     }

    /**
     * Build a new Contact person primary object.
     */
    public OperationType(final List<String> format, DCPType... dcpTypes) {
        this.format  = format;
        for (final DCPType element : dcpTypes) {
            this.dcpType.add(element);
        }
    }
    /**
     * Gets the value of the format property.
     * 
     */
    public List<String> getFormat() {
        return Collections.unmodifiableList(format);
    }

    /**
     * Gets the value of the dcpType property.
     */
    public List<DCPType> getDCPType() {
        return Collections.unmodifiableList(dcpType);
    }

}
