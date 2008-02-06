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

package net.seagis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;


/**
 * <p>Java class for TimePeriodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TimePeriodType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractTimeGeometricPrimitiveType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="beginPosition" type="{http://www.opengis.net/gml}TimePositionType"/>
 *           &lt;element name="begin" type="{http://www.opengis.net/gml}TimeInstantPropertyType"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="endPosition" type="{http://www.opengis.net/gml}TimePositionType"/>
 *           &lt;element name="end" type="{http://www.opengis.net/gml}TimeInstantPropertyType"/>
 *         &lt;/choice>
 *         &lt;group ref="{http://www.opengis.net/gml}timeLength" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimePeriodType", propOrder = {
    "beginPosition",
    "begin",
    "endPosition",
    "end",
    "duration",
    "timeInterval"
})
public class TimePeriodType extends AbstractTimeGeometricPrimitiveType {

    private TimePositionType beginPosition;
    private TimeInstantPropertyType begin;
    private TimePositionType endPosition;
    private TimeInstantPropertyType end;
    private Duration duration;
    private TimeIntervalLengthType timeInterval;

    /**
     * Empty constructor used by JAXB.
     */
    TimePeriodType(){
        
    }
    
    /**
     * Build a new Time period bounded by the begin and end time specified.
     */
    public TimePeriodType(TimePositionType beginPosition, TimePositionType endPosition){
        this.beginPosition = beginPosition;
        this.endPosition   = endPosition;
    }
    
    /**
     * Gets the value of the beginPosition property.
     */
    public TimePositionType getBeginPosition() {
        return beginPosition;
    }

    /**
     * Gets the value of the begin property.
     */
    public TimeInstantPropertyType getBegin() {
        return begin;
    }

    /**
     * Gets the value of the endPosition property.
     */
    public TimePositionType getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the value of the end property.
     */
    public TimeInstantPropertyType getEnd() {
        return end;
    }

    /**
     * Gets the value of the duration property.
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Gets the value of the timeInterval property.
     */
    public TimeIntervalLengthType getTimeInterval() {
        return timeInterval;
    }
}
