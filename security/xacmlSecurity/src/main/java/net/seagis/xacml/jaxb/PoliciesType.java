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
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for PoliciesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PoliciesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PolicySet" type="{urn:jboss:xacml:2.0}PolicySetType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Policy" type="{urn:jboss:xacml:2.0}PolicyType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PoliciesType", propOrder =
{"policySet", "policy"})
public class PoliciesType {

   @XmlElement(name = "PolicySet")
   private List<PolicySetType> policySet;

   @XmlElement(name = "Policy")
   private List<PolicyType> policy;

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

}
