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

package org.constellation.sml.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="phone" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="voice" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="facsimile" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="address" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="deliveryPoint" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="city" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="administrativeArea" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="postalCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="electronicMailAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0}onlineResource" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="hoursOfService" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contactInstructions" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "phone",
    "address",
    "onlineResource",
    "hoursOfService",
    "contactInstructions"
})
@XmlRootElement(name = "contactInfo")
public class ContactInfo {

    private ContactInfo.Phone phone;
    private ContactInfo.Address address;
    private List<OnlineResource> onlineResource;
    private String hoursOfService;
    private String contactInstructions;

    /**
     * Gets the value of the phone property.
     */
    public ContactInfo.Phone getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     */
    public void setPhone(ContactInfo.Phone value) {
        this.phone = value;
    }

    /**
     * Gets the value of the address property.
     */
    public ContactInfo.Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     *     
     */
    public void setAddress(ContactInfo.Address value) {
        this.address = value;
    }

    /**
     * Gets the value of the onlineResource property.
     */
    public List<OnlineResource> getOnlineResource() {
        if (onlineResource == null) {
            onlineResource = new ArrayList<OnlineResource>();
        }
        return this.onlineResource;
    }

    /**
     * Gets the value of the hoursOfService property.
     */
    public String getHoursOfService() {
        return hoursOfService;
    }

    /**
     * Sets the value of the hoursOfService property.
     */
    public void setHoursOfService(String value) {
        this.hoursOfService = value;
    }

    /**
     * Gets the value of the contactInstructions property.
     * 
     */
    public String getContactInstructions() {
        return contactInstructions;
    }

    /**
     * Sets the value of the contactInstructions property.
     * 
     */
    public void setContactInstructions(String value) {
        this.contactInstructions = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ContactInfo]").append("\n");
        if (phone != null) {
            sb.append("phone: ").append(phone).append('\n');
        }
        if (address != null) {
            sb.append("address: ").append(address).append('\n');
        }
        if (hoursOfService != null) {
            sb.append("hoursOfService: ").append(hoursOfService).append('\n');
        }
        if (contactInstructions != null) {
            sb.append("contactInstructions: ").append(contactInstructions).append('\n');
        }
        if (onlineResource != null) {
            for (OnlineResource o : onlineResource) {
                sb.append("onlineResource: ").append(o).append('\n');
            }
        }
        return sb.toString();
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
     *         &lt;element name="deliveryPoint" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="city" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="administrativeArea" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="postalCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="electronicMailAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "deliveryPoint",
        "city",
        "administrativeArea",
        "postalCode",
        "country",
        "electronicMailAddress"
    })
    public static class Address {

        private List<String> deliveryPoint;
        private String city;
        private String administrativeArea;
        private String postalCode;
        private String country;
        private String electronicMailAddress;

        /**
         * Gets the value of the deliveryPoint property.
         * 
         */
        public List<String> getDeliveryPoint() {
            if (deliveryPoint == null) {
                deliveryPoint = new ArrayList<String>();
            }
            return this.deliveryPoint;
        }

        /**
         * Gets the value of the city property.
         * 
         */
        public String getCity() {
            return city;
        }

        /**
         * Sets the value of the city property.
         */
        public void setCity(String value) {
            this.city = value;
        }

        /**
         * Gets the value of the administrativeArea property.
         */
        public String getAdministrativeArea() {
            return administrativeArea;
        }

        /**
         * Sets the value of the administrativeArea property.
         * 
         */
        public void setAdministrativeArea(String value) {
            this.administrativeArea = value;
        }

        /**
         * Gets the value of the postalCode property.
         */
        public String getPostalCode() {
            return postalCode;
        }

        /**
         * Sets the value of the postalCode property.
         */
        public void setPostalCode(String value) {
            this.postalCode = value;
        }

        /**
         * Gets the value of the country property.
         */
        public String getCountry() {
            return country;
        }

        /**
         * Sets the value of the country property.
         */
        public void setCountry(String value) {
            this.country = value;
        }

        /**
         * Gets the value of the electronicMailAddress property.
         */
        public String getElectronicMailAddress() {
            return electronicMailAddress;
        }

        /**
         * Sets the value of the electronicMailAddress property.
         */
        public void setElectronicMailAddress(String value) {
            this.electronicMailAddress = value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[Address]").append("\n");
            if (city != null) {
                sb.append("city: ").append(city).append('\n');
            }
            if (administrativeArea != null) {
                sb.append("administrativeArea: ").append(administrativeArea).append('\n');
            }
            if (postalCode != null) {
                sb.append("postalCode: ").append(postalCode).append('\n');
            }
            if (country != null) {
                sb.append("country: ").append(country).append('\n');
            }
            if (electronicMailAddress != null) {
                sb.append("electronicMailAddress: ").append(electronicMailAddress).append('\n');
            }
            if (deliveryPoint != null) {
                for (String d : deliveryPoint) {
                    sb.append("deliveryPoint: ").append(d).append('\n');
                }
            }
            return sb.toString();
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
     *         &lt;element name="voice" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="facsimile" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
        "voice",
        "facsimile"
    })
    public static class Phone {

        private List<String> voice;
        private List<String> facsimile;

        /**
         * Gets the value of the voice property.
         */
        public List<String> getVoice() {
            if (voice == null) {
                voice = new ArrayList<String>();
            }
            return this.voice;
        }

        /**
         * Gets the value of the facsimile property.
         * 
         */
        public List<String> getFacsimile() {
            if (facsimile == null) {
                facsimile = new ArrayList<String>();
            }
            return this.facsimile;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[Phone]").append("\n");
            if (voice != null) {
                for (String d : voice) {
                    sb.append("voice: ").append(d).append('\n');
                }
            }
            if (facsimile != null) {
                for (String d : facsimile) {
                    sb.append("facsimile: ").append(d).append('\n');
                }
            }
            return sb.toString();
        }

    }

}
