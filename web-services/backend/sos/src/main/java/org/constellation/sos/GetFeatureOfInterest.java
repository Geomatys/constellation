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
package org.constellation.sos;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.constellation.ogc.SpatialOpsType;
import org.geotools.util.Utilities;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/sos/1.0}RequestBaseType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="FeatureOfInterestId" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded"/>
 *           &lt;element name="location">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element ref="{http://www.opengis.net/ogc}spatialOps"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *         &lt;element name="eventTime" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.opengis.net/ogc}temporalOps"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetFeatureOfInterest", propOrder = {
    "featureOfInterestId",
    "location",
    "eventTime"
})
@XmlRootElement(name = "GetFeatureOfInterest")
public class GetFeatureOfInterest extends RequestBaseType {

    /**
     * Identifier of the feature of interest, for which detailed information is requested.
     * These identifiers are usually listed in the Contents section of the service metadata (Capabilities) document.
     */
    @XmlElement(name = "FeatureOfInterestId")
    @XmlSchemaType(name = "anyURI")
    private List<String> featureOfInterestId;
    
    private GetFeatureOfInterest.Location location;
    
     /**
     * Allows a client to request targets from a specific instant, multiple instances or periods of time in the past, present and future.
     * This is useful for dynamic sensors for which the properties of the target are time-dependent.
     * Multiple time paramters may be indicated so that the client may request details of the observation target at multiple times.
     * The supported range is listed in the contents section of the service metadata.
     *
     */
    private List<EventTime> eventTime;

    /**
     * An empty constructor used by jaxB
     */
     GetFeatureOfInterest(){}
     
    /**
     * Gets the value of the featureOfInterestId property.
     */
    public List<String> getFeatureOfInterestId() {
        return Collections.unmodifiableList(featureOfInterestId);
    }
    
    /**
     * Gets the value of the eventTime property.
     * (unmodifiable)
     */
    public List<EventTime> getEventTime() {
        return Collections.unmodifiableList(eventTime);
    }
    
    /**
     * Gets the value of the featureOfInterestLocation property.
     *
     */
    public GetFeatureOfInterest.Location getLocation() {
        return location;
    }
    
    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final GetFeatureOfInterest that = (GetFeatureOfInterest) object;
            return Utilities.equals(this.eventTime,           that.eventTime)           &&
                   Utilities.equals(this.featureOfInterestId, that.featureOfInterestId) &&
                   Utilities.equals(this.location,            that.location);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.featureOfInterestId != null ? this.featureOfInterestId.hashCode() : 0);
        hash = 41 * hash + (this.eventTime != null ? this.eventTime.hashCode() : 0);
        hash = 41 * hash + (this.location != null ? this.location.hashCode() : 0);
        return hash;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{http://www.opengis.net/ogc}spatialOps"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "spatialOps"
    })
    public static class Location {

        @XmlElementRef(name = "spatialOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
        private JAXBElement<? extends SpatialOpsType> spatialOps;

        /**
         * Gets the value of the spatialOps property.
         */
        public JAXBElement<? extends SpatialOpsType> getSpatialOps() {
            return spatialOps;
        }
        
        /**
          * Verify if this entry is identical to the specified object.
          */
        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            final  Location that = ( Location) object;
            return Utilities.equals(this.spatialOps.getValue(), that.spatialOps.getValue());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.spatialOps != null ? this.spatialOps.hashCode() : 0);
            return hash;
        }
    }
}
