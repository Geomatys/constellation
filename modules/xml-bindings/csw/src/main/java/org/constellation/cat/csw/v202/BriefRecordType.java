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
package org.constellation.cat.csw.v202;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.ows.v100.BoundingBoxType;
import org.constellation.dublincore.v2.elements.SimpleLiteral;
import org.constellation.ows.v100.WGS84BoundingBoxType;


/**
 * 
 * This type defines a brief representation of the common record
 * format.  It extends AbstractRecordType to include only the
 * dc:identifier and dc:type properties.
 *          
 * 
 * <p>Java class for BriefRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BriefRecordType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}AbstractRecordType">
 *       &lt;sequence>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}identifier" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}title" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}type" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows}BoundingBox" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BriefRecordType", propOrder = {
    "identifier",
    "title",
    "type",
    "boundingBox"
})
@XmlRootElement(name = "BriefRecord")
public class BriefRecordType extends AbstractRecordType {

    @XmlElementRef(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> identifier;
    @XmlElementRef(name = "title", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> title;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral type;
    @XmlElementRef(name = "BoundingBox", namespace = "http://www.opengis.net/ows", type = JAXBElement.class)
    private List<JAXBElement<? extends BoundingBoxType>> boundingBox;

    /**
     * An empty constructor used by JAXB
     */
    BriefRecordType() {
    }
    
    /**
     * Build a new brief record.
     * 
     * @param identifier
     * @param title
     * @param type
     * @param bbox
     */
    public BriefRecordType(SimpleLiteral identifier, SimpleLiteral title, SimpleLiteral type, List<BoundingBoxType> bboxes) {
        
        this.identifier = new ArrayList<JAXBElement<SimpleLiteral>>();
        if (identifier == null)
            identifier = new SimpleLiteral();
        this.identifier.add(dublinFactory.createIdentifier(identifier));
        
        this.title = new ArrayList<JAXBElement<SimpleLiteral>>();
        if (title == null)
            title = new SimpleLiteral();
        this.title.add(dublinFactory.createTitle(title));
        
        this.type = type;
        
        this.boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        for (BoundingBoxType bbox: bboxes) {
            if (bbox instanceof WGS84BoundingBoxType)
                this.boundingBox.add(owsFactory.createWGS84BoundingBox((WGS84BoundingBoxType)bbox));
            else if (bbox != null)
                this.boundingBox.add(owsFactory.createBoundingBox(bbox));
        }
    }
    
    /**
     * Gets the value of the identifier property.
     * (unmodifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getIdentifier() {
        if (identifier == null) {
            identifier = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(identifier);
    }

    /**
     * Gets the value of the title property.
     * (unmodifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getTitle() {
        if (title == null) {
            title = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(title);
    }

    /**
     * Gets the value of the type property.
     */
    public SimpleLiteral getType() {
        return type;
    }


    /**
     * Gets the value of the boundingBox property.
     */
    public List<JAXBElement<? extends BoundingBoxType>> getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        }
        return Collections.unmodifiableList(boundingBox);
    }

}
