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


package net.seagis.wcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.TimePositionType;


/**
 * List of time positions and periods. The time positions and periods should be ordered from the oldest to the newest, but this is not required. 
 * 
 * <p>Java class for TimeSequenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TimeSequenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://www.opengis.net/gml}timePosition"/>
 *         &lt;element name="TimePeriod" type="{http://www.opengis.net/wcs/1.1.1}TimePeriodType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeSequenceType", propOrder = {
    "timePositionOrTimePeriod"
})
public class TimeSequenceType {

    @XmlElements({
        @XmlElement(name = "TimePeriod", type = TimePeriodType.class),
        @XmlElement(name = "timePosition", namespace = "http://www.opengis.net/gml", type = TimePositionType.class)
    })
    private List<Object> timePositionOrTimePeriod = new ArrayList<Object>();

    /**
     * An empty constructor used by JAXB.
     */
    TimeSequenceType() {
    }
    
    /**
     * build a new time sequence.
     */
    public TimeSequenceType(List<Object> timePositionOrTimePeriod) {
        this.timePositionOrTimePeriod = timePositionOrTimePeriod;
        
    }
    
    /**
     * Gets the value of the timePositionOrTimePeriod property.
     */
    public List<Object> getTimePositionOrTimePeriod() {
        return Collections.unmodifiableList(timePositionOrTimePeriod);
    }

}
