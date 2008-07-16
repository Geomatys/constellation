/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Institut de Recherche pour le Développement
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


package net.seagis.cat.csw.v200;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.v100.BoundingBoxType;


/**
 * This type extends DCMIRecordType to add ows:BoundingBox; 
 * it may be used to specify a bounding envelope for the catalogued resource.
 *       
 * 
 * <p>Java class for RecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecordType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw}DCMIRecordType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ows}BoundingBox" minOccurs="0"/>
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
    "boundingBox"
})
public class RecordType extends DCMIRecordType {

    @XmlElementRef(name = "BoundingBox", namespace = "http://www.opengis.net/ows", type = JAXBElement.class)
    private JAXBElement<? extends BoundingBoxType> boundingBox;

    /**
     * Gets the value of the boundingBox property.
     * 
     */
    public JAXBElement<? extends BoundingBoxType> getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the value of the boundingBox property.
     * 
     */
    public void setBoundingBox(JAXBElement<? extends BoundingBoxType> value) {
        this.boundingBox = ((JAXBElement<? extends BoundingBoxType> ) value);
    }

}
