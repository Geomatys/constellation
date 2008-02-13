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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.DirectPositionType;
import net.seagis.gml.TimePositionType;


/**
 * Defines spatial extent by extending LonLatEnvelope with an optional time position pair. 
 * 
 * <p>Java class for LonLatEnvelopeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LonLatEnvelopeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wcs}LonLatEnvelopeBaseType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element ref="{http://www.opengis.net/gml}timePosition" maxOccurs="2" minOccurs="2"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LonLatEnvelopeType", propOrder = {
    "timePosition"
})
public class LonLatEnvelopeType extends LonLatEnvelopeBaseType {

    @XmlElement(namespace = "http://www.opengis.net/gml")
    private List<TimePositionType> timePosition;

    LonLatEnvelopeType(){
        
    }
    
    public LonLatEnvelopeType(List<DirectPositionType> pos, String srsName){
        super (pos, srsName);
    }
    
    /**
     * Gets the value of the timePosition property.
     * 
     */
    public List<TimePositionType> getTimePosition() {
        if (timePosition == null) {
            timePosition = new ArrayList<TimePositionType>();
        }
        return this.timePosition;
    }

}
