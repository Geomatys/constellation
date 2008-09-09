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
package org.constellation.ows.v100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * This type is adapted from the general BoundingBoxType, 
 * with modified contents and documentation for use with the 2D WGS 84 coordinate reference system. 
 * 
 * <p>Java class for WGS84BoundingBoxType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WGS84BoundingBoxType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.opengis.net/ows}BoundingBoxType">
 *       &lt;sequence>
 *         &lt;element name="LowerCorner" type="{http://www.opengis.net/ows}PositionType2D"/>
 *         &lt;element name="UpperCorner" type="{http://www.opengis.net/ows}PositionType2D"/>
 *       &lt;/sequence>
 *       &lt;attribute name="crs" type="{http://www.w3.org/2001/XMLSchema}anyURI" fixed="urn:ogc:def:crs:OGC:2:84" />
 *       &lt;attribute name="dimensions" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" fixed="2" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WGS84BoundingBoxType")
public class WGS84BoundingBoxType extends BoundingBoxType {
    
    /**
     * An empty constructor used by JAXB.
     */
    public WGS84BoundingBoxType() {
        
    }
    
    /**
     * Build a 2 dimension boundingBox.
     * 
     */
    public WGS84BoundingBoxType(double minx, double miny, double maxx, double maxy){
        super(null, minx, miny, maxx, maxy);
    }

}
