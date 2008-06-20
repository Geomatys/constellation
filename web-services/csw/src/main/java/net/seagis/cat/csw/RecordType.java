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


package net.seagis.cat.csw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.v100.BoundingBoxType;
import net.seagis.dublincore.elements.SimpleLiteral;
import net.seagis.ows.v100.WGS84BoundingBoxType;


/**
 * 
 * This type extends DCMIRecordType to add ows:BoundingBox;
 * it may be used to specify a spatial envelope for the
 * catalogued resource.
 *          
 * 
 * <p>Java class for RecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecordType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}DCMIRecordType">
 *       &lt;sequence>
 *         &lt;element name="AnyText" type="{http://www.opengis.net/cat/csw/2.0.2}EmptyType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "RecordType", propOrder = {
    "anyText",
    "boundingBox"
})
public class RecordType extends DCMIRecordType {

    @XmlElement(name = "AnyText")
    private List<EmptyType> anyText;
    @XmlElementRef(name = "BoundingBox", namespace = "http://www.opengis.net/ows", type = JAXBElement.class)
    private List<JAXBElement<? extends BoundingBoxType>> boundingBox;

    /**
     * An empty constructor used by JAXB
     */
    public RecordType() {
        
    }
    
    /**
     * Build a new Record TODO add contributor, source , spatial, right, relation
     */
    public RecordType(SimpleLiteral identifier, SimpleLiteral title, SimpleLiteral type, 
            List<SimpleLiteral> subject, SimpleLiteral format, SimpleLiteral modified, SimpleLiteral _abstract,
            List<BoundingBoxType> bboxes, SimpleLiteral creator, SimpleLiteral distributor, SimpleLiteral language) {
        
        super(identifier, title,type, subject, format, modified, _abstract, creator, distributor, language);
        
        this.boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        for (BoundingBoxType bbox: bboxes) {
            this.boundingBox.add(owsFactory.createBoundingBox(bbox));
        }
        
    }
    
    /**
     * Gets the value of the anyText property.
     * (unmodifiable)
     */
    public List<EmptyType> getAnyText() {
        if (anyText == null) {
            anyText = new ArrayList<EmptyType>();
        }
        return Collections.unmodifiableList(anyText);
    }

    /**
     * Gets the value of the boundingBox property.
     * (unmodifiable)
     */
    public List<JAXBElement<? extends BoundingBoxType>> getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        }
        return Collections.unmodifiableList(boundingBox);
    }
    
    public void setBoundingBox(BoundingBoxType bbox) {
        if (boundingBox == null) {
            this.boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        }
        if (bbox instanceof WGS84BoundingBoxType) {
            this.boundingBox.add(owsFactory.createWGS84BoundingBox((WGS84BoundingBoxType)bbox));
        } else {
            this.boundingBox.add(owsFactory.createBoundingBox(bbox));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        if (anyText != null && anyText.size() != 0) {
            s.append("anyText:");
            for (EmptyType e :anyText) {
                s.append(e.toString()).append('\n');
            }
        }
        
        if (boundingBox != null && boundingBox.size() != 0) {
            s.append("bounding boxes:");
            for (JAXBElement<? extends BoundingBoxType> bb: boundingBox) {
                s.append(bb.getValue().toString()).append('\n');
            }
        }
        
        return s.toString();
    }

}
