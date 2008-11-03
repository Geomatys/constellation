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
package org.constellation.wcs.v100;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import org.constellation.gml.v311.TimePositionType;


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
 *         &lt;element name="TimePeriod" type="{http://www.opengis.net/wcs}TimePeriodType"/>
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
     * 
     * @param timePositionOrTimePeriod a list of timePosition and timePeriod
     */
    public TimeSequenceType(List<Object> timePositionOrTimePeriod) {
        this.timePositionOrTimePeriod = timePositionOrTimePeriod;
        
    }
    
    /**
     * build a new time sequence with a simple timePosition.
     * 
     * @param timePosition a simple timePosition
     */
    public TimeSequenceType(TimePositionType timePosition) {
        timePositionOrTimePeriod = new ArrayList<Object>();
        timePositionOrTimePeriod.add(timePosition);
    }
    
    /**
     * Gets the value of the timePositionOrTimePeriod property.
     * (unmodifable)
     */
    public List<Object> getTimePositionOrTimePeriod() {
        if (timePositionOrTimePeriod == null) {
            timePositionOrTimePeriod = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(timePositionOrTimePeriod);
    }

}
