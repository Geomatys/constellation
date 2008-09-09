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
package net.seagis.wcs.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * XML encoded WCS GetCapabilities operation response. 
 * The Capabilities document provides clients with service metadata about a specific service instance,
 * including metadata about the coverages served. 
 *       
 * WCS version 1.0.0
 * <p>Java class for WCSCapabilityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WCSCapabilityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Request">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="GetCapabilities">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="DescribeCoverage">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="GetCoverage">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Exception">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Format" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="VendorSpecificCapabilities" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.0.0" />
 *       &lt;attribute name="updateSequence" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WCSCapabilityType", propOrder = {
    "request",
    "exception",
    "vendorSpecificCapabilities"
})
public class WCSCapabilityType {

    @XmlElement(name = "Request", required = true)
    private WCSCapabilityType.Request request;
    @XmlElement(name = "Exception", required = true)
    private WCSCapabilityType.Exception exception;
    @XmlElement(name = "VendorSpecificCapabilities")
    private WCSCapabilityType.VendorSpecificCapabilities vendorSpecificCapabilities;
    @XmlAttribute
    private String version;
    @XmlAttribute
    private String updateSequence;

    /**
     * Gets the value of the request property.
     */
    public WCSCapabilityType.Request getRequest() {
        return request;
    }

    /**
     * Gets the value of the exception property.
     * 
     */
    public WCSCapabilityType.Exception getException() {
        return exception;
    }

    /**
     * Gets the value of the vendorSpecificCapabilities property.
     * 
    */
    public WCSCapabilityType.VendorSpecificCapabilities getVendorSpecificCapabilities() {
        return vendorSpecificCapabilities;
    }

    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        if (version == null) {
            return "1.0.0";
        } else {
            return version;
        }
    }

    /**
     * Gets the value of the updateSequence property.
     */
    public String getUpdateSequence() {
        return updateSequence;
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
     *         &lt;element name="Format" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
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
        "format"
    })
    public static class Exception {

        @XmlElement(name = "Format", required = true)
        private List<String> format;

        /**
         * Gets the value of the format property.
         * 
         */
        public List<String> getFormat() {
            if (format == null) {
                format = new ArrayList<String>();
            }
            return this.format;
        }

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
     *         &lt;element name="GetCapabilities">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="DescribeCoverage">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="GetCoverage">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "getCapabilities",
        "describeCoverage",
        "getCoverage"
    })
    public static class Request {

        @XmlElement(name = "GetCapabilities", required = true)
        private WCSCapabilityType.Request.GetCapabilities getCapabilities;
        @XmlElement(name = "DescribeCoverage", required = true)
        private WCSCapabilityType.Request.DescribeCoverage describeCoverage;
        @XmlElement(name = "GetCoverage", required = true)
        private WCSCapabilityType.Request.GetCoverage getCoverage;

        /**
         * Gets the value of the getCapabilities property.
         */
        public WCSCapabilityType.Request.GetCapabilities getGetCapabilities() {
            return getCapabilities;
        }

        /**
         * Sets the value of the getCapabilities property.
         */
        public void setGetCapabilities(WCSCapabilityType.Request.GetCapabilities value) {
            this.getCapabilities = value;
        }

        /**
         * Gets the value of the describeCoverage property.
         */
        public WCSCapabilityType.Request.DescribeCoverage getDescribeCoverage() {
            return describeCoverage;
        }

        /**
         * Sets the value of the describeCoverage property.
         * 
         */
        public void setDescribeCoverage(WCSCapabilityType.Request.DescribeCoverage value) {
            this.describeCoverage = value;
        }

        /**
         * Gets the value of the getCoverage property.
         * 
         */
        public WCSCapabilityType.Request.GetCoverage getGetCoverage() {
            return getCoverage;
        }

        /**
         * Sets the value of the getCoverage property.
         * 
         */
        public void setGetCoverage(WCSCapabilityType.Request.GetCoverage value) {
            this.getCoverage = value;
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
         *         &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
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
            "dcpType"
        })
        public static class DescribeCoverage {

            @XmlElement(name = "DCPType", required = true)
            private List<DCPTypeType> dcpType;

            /**
             * Gets the value of the dcpType property.
             * 
             */
            public List<DCPTypeType> getDCPType() {
                if (dcpType == null) {
                    dcpType = new ArrayList<DCPTypeType>();
                }
                return this.dcpType;
            }

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
         *         &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
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
            "dcpType"
        })
        public static class GetCapabilities {

            @XmlElement(name = "DCPType", required = true)
            private List<DCPTypeType> dcpType;

            /**
             * Gets the value of the dcpType property.
             * 
             */
            public List<DCPTypeType> getDCPType() {
                if (dcpType == null) {
                    dcpType = new ArrayList<DCPTypeType>();
                }
                return this.dcpType;
            }

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
         *         &lt;element name="DCPType" type="{http://www.opengis.net/wcs}DCPTypeType" maxOccurs="unbounded"/>
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
            "dcpType"
        })
        public static class GetCoverage {

            @XmlElement(name = "DCPType", required = true)
            private List<DCPTypeType> dcpType;

            /**
             * Gets the value of the dcpType property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the dcpType property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getDCPType().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link DCPTypeType }
             * 
             * 
             */
            public List<DCPTypeType> getDCPType() {
                if (dcpType == null) {
                    dcpType = new ArrayList<DCPTypeType>();
                }
                return this.dcpType;
            }

        }

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
     *         &lt;any/>
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
        "any"
    })
    public static class VendorSpecificCapabilities {

        @XmlAnyElement(lax = true)
        private Object any;

        /**
         * Gets the value of the any property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getAny() {
            return any;
        }

        /**
         * Sets the value of the any property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setAny(Object value) {
            this.any = value;
        }

    }

}
