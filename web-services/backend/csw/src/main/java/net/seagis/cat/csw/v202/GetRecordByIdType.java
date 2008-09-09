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
package net.seagis.cat.csw.v202;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             Convenience operation to retrieve default record representations 
 *             by identifier.
 *             Id - object identifier (a URI) that provides a reference to a 
 *                  catalogue item (or a result set if the catalogue supports 
 *                  persistent result sets).
 *             ElementSetName - one of "brief, "summary", or "full"
 *          
 * 
 * <p>Java class for GetRecordByIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetRecordByIdType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}RequestBaseType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}ElementSetName" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="outputFormat" type="{http://www.w3.org/2001/XMLSchema}string" default="application/xml" />
 *       &lt;attribute name="outputSchema" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRecordByIdType", propOrder = {
    "id",
    "elementSetName"
})
public class GetRecordByIdType extends RequestBaseType {

    @XmlElement(name = "Id", required = true)
    @XmlSchemaType(name = "anyURI")
    private List<String> id;
    @XmlElement(name = "ElementSetName", defaultValue = "summary")
    private ElementSetNameType elementSetName;
    @XmlAttribute
    private String outputFormat;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String outputSchema;

    /**
     * An empty constructor used by JAXB
     */
     GetRecordByIdType(){
         
     }
     
     /**
     * An empty constructor used by JAXB
     */
     public GetRecordByIdType(String service, String version, ElementSetNameType elementSetName,
             String outputFormat, String outputSchema, List<String> id){
         super(service, version);
         this.elementSetName = elementSetName;
         this.outputFormat   = outputFormat;
         this.outputSchema   = outputSchema;
         this.id             = id;
     }
     
    /**
     * Gets the value of the id property.
     * (unmodifiable)
     */
    public List<String> getId() {
        if (id == null) {
            id = new ArrayList<String>();
        }
        return Collections.unmodifiableList(id);
    }

    /**
     * Gets the value of the elementSetName property.
     */
    public ElementSetNameType getElementSetName() {
        return elementSetName;
    }

    /**
     * Gets the value of the outputFormat property.
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Gets the value of the outputSchema property.
     */
    public String getOutputSchema() {
        return outputSchema;
    }
}
