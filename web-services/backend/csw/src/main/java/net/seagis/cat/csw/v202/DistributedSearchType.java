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

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Governs the behaviour of a distributed search.
 * hopCount     - the maximum number of message hops before the search is terminated. 
 * 
 * Each catalogue node decrements this value when the request is received, and must not forward the request if hopCount=0.
 * 
 * <p>Java class for DistributedSearchType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DistributedSearchType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="hopCount" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" default="2" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistributedSearchType")
public class DistributedSearchType {

    @XmlAttribute
    @XmlSchemaType(name = "positiveInteger")
    private Integer hopCount;

    /**
     * An empty constructor used by JAXB
     */
    DistributedSearchType(){
        
    }
    
    /**
     * Build a new DIstributed search
     */
    public DistributedSearchType(Integer hopCount){
        this.hopCount = hopCount;
    }
    
    /**
     */
    public Integer getHopCount() {
        if (hopCount == null) {
            return 2;
        } else {
            return hopCount;
        }
    }
}
