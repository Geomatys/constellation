/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ObservationFilter {
    
    private String sensorID;
    
    private List<String> observedProperty;

    private Date start;
    
    private Date end;
    
    /**
     * @return the sensorID
     */
    public String getSensorID() {
        return sensorID;
    }

    /**
     * @param sensorID the sensorID to set
     */
    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }
    
    /**
     * @return the start
     */
    public Date getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Date start) {
        this.start = start;
    }
    
    /**
     * @return the end
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * @return the observedProperty
     */
    public List<String> getObservedProperty() {
        if (observedProperty == null) {
            observedProperty = new ArrayList<>();
        }
        return observedProperty;
    }

    /**
     * @param observedProperty the observedProperty to set
     */
    public void setObservedProperty(List<String> observedProperty) {
        this.observedProperty = observedProperty;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[Observation Filter]\n");
        sb.append("sensorID:").append(sensorID).append("\n");
        sb.append("observedProperties:\n");
        for (String op : observedProperty) {
            sb.append(op).append("\n");
        }
        return sb.toString();
    }
}
