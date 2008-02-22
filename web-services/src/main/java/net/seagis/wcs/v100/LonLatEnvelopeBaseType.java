/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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


package net.seagis.wcs.v100;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.gml.DirectPositionType;


/**
 * Envelope defines an extent using a pair of positions defining opposite corners in arbitrary dimensions. 
 * 
 * <p>Java class for LonLatEnvelopeBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LonLatEnvelopeBaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.opengis.net/gml}EnvelopeType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}pos" maxOccurs="2" minOccurs="2"/>
 *       &lt;/sequence>
 *       &lt;attribute name="srsName" type="{http://www.w3.org/2001/XMLSchema}anyURI" fixed="urn:ogc:def:crs:OGC:1.3:CRS84" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LonLatEnvelopeBaseType")
@XmlSeeAlso({
    LonLatEnvelopeType.class
})
public class LonLatEnvelopeBaseType extends EnvelopeEntry {
    
    LonLatEnvelopeBaseType(){
    }
    
    public LonLatEnvelopeBaseType(List<DirectPositionType> pos, String srsName) {
        super(pos, srsName);
    }
}
