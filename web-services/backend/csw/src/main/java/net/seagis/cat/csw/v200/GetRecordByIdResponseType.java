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


/**
 * Returns a representation of the matching catalogue object.
 * If there is no matching record, the response message element is empty.
 *          
 * 
 * <p>Java class for GetRecordByIdResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetRecordByIdResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/cat/csw}AbstractRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRecordByIdResponseType", propOrder = {
    "abstractRecord"
})
public class GetRecordByIdResponseType {

    @XmlElementRef(name = "AbstractRecord", namespace = "http://www.opengis.net/cat/csw", type = JAXBElement.class)
    private JAXBElement<? extends AbstractRecordType> abstractRecord;

    /**
     * Gets the value of the abstractRecord property.
     * 
     */
    public JAXBElement<? extends AbstractRecordType> getAbstractRecord() {
        return abstractRecord;
    }

    /**
     * Sets the value of the abstractRecord property.
     * 
     */
    public void setAbstractRecord(JAXBElement<? extends AbstractRecordType> value) {
        this.abstractRecord = ((JAXBElement<? extends AbstractRecordType> ) value);
    }

}
