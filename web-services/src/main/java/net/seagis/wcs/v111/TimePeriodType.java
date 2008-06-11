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


package net.seagis.wcs.v111;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.v311.TimePositionType;


/**
 * (Arliss) What does this mean? What do the TimeResolution and "frame" mean? 
 * 
 * <p>Java class for TimePeriodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TimePeriodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BeginPosition" type="{http://www.opengis.net/gml}TimePositionType"/>
 *         &lt;element name="EndPosition" type="{http://www.opengis.net/gml}TimePositionType"/>
 *         &lt;element name="TimeResolution" type="{http://www.opengis.net/wcs}TimeDurationType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="frame" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="#ISO-8601" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimePeriodType", propOrder = {
    "beginPosition",
    "endPosition",
    "timeResolution"
})
public class TimePeriodType {

    @XmlElement(name = "BeginPosition", required = true)
    protected TimePositionType beginPosition;
    @XmlElement(name = "EndPosition", required = true)
    protected TimePositionType endPosition;
    @XmlElement(name = "TimeResolution")
    protected String timeResolution;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String frame;

    /**
     * an empty constructor used by JAXB
     */
    TimePeriodType(){
    }
    
     /**
     * an empty constructor used by JAXB
     */
    public TimePeriodType(TimePositionType beginPosition, TimePositionType endPosition, 
            String timeResolution, String frame){
        this.beginPosition  = beginPosition;
        this.endPosition    = endPosition;
        this.frame          = frame;
        this.timeResolution = timeResolution;
    }
    
    /**
     * Gets the value of the beginPosition property.
     */
    public TimePositionType getBeginPosition() {
        return beginPosition;
    }

    /**
     * Gets the value of the endPosition property.
     */
    public TimePositionType getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the value of the timeResolution property.
     * 
     */
    public String getTimeResolution() {
        return timeResolution;
    }

    /**
     * Gets the value of the frame property.
     */
    public String getFrame() {
        if (frame == null) {
            return "#ISO-8601";
        } else {
            return frame;
        }
    }
}
