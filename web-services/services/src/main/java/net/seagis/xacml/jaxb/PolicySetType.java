/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008 Geomatys
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

package net.seagis.xacml.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for PolicySetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PolicySetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Location" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="Policy" type="{urn:jboss:xacml:2.0}PolicyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PolicySet" type="{urn:jboss:xacml:2.0}PolicySetType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolicySetType", propOrder =
{"location", "policy", "policySet"})
public class PolicySetType {

   @XmlElement(name = "Location")
   @XmlSchemaType(name = "anyURI")
   private String location;

   @XmlElement(name = "Policy")
   private List<PolicyType> policy;

   @XmlElement(name = "PolicySet")
   private List<PolicySetType> policySet;

   /**
    * Gets the value of the location property.
    * 
    */
   public String getLocation() {
      return location;
   }

   /**
    * Sets the value of the location property.
    * 
    */
   public void setLocation(String value) {
      this.location = value;
   }

   /**
    * Gets the value of the policy property.
    * 
    */
   public List<PolicyType> getPolicy() {
      if (policy == null) {
         policy = new ArrayList<PolicyType>();
      }
      return this.policy;
   }

   /**
    * Gets the value of the policySet property.
    * 
    */
   public List<PolicySetType> getPolicySet() {
      if (policySet == null) {
         policySet = new ArrayList<PolicySetType>();
      }
      return this.policySet;
   }

}
