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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the net.seagis.xacml.jaxb package. 
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. 
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding of schema 
 * type definitions, element declarations and model groups.
 * Factory methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

   private final static QName _Policies_QNAME = new QName("urn:jboss:xacml:2.0", "Policies");
   private final static QName _Locators_QNAME = new QName("urn:jboss:xacml:2.0", "Locators");
   private final static QName _Jbosspdp_QNAME = new QName("urn:jboss:xacml:2.0", "jbosspdp");

   /**
    * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.seagis.xacml.jaxb
    * 
    */
   public ObjectFactory() {
   }

   /**
    * Create an instance of {@link PDP }
    * 
    */
   public PDP createPDP() {
      return new PDP();
   }

   /**
    * Create an instance of {@link LocatorType }
    * 
    */
   public LocatorType createLocatorType() {
      return new LocatorType();
   }

   /**
    * Create an instance of {@link PolicyType }
    * 
    */
   public PolicyType createPolicyType() {
      return new PolicyType();
   }

   /**
    * Create an instance of {@link PolicySetType }
    * 
    */
   public PolicySetType createPolicySetType() {
      return new PolicySetType();
   }

   /**
    * Create an instance of {@link Option }
    * 
    */
   public Option createOption() {
      return new Option();
   }

   /**
    * Create an instance of {@link PoliciesType }
    * 
    */
   public PoliciesType createPoliciesType() {
      return new PoliciesType();
   }

   /**
    * Create an instance of {@link LocatorsType }
    * 
    */
   public LocatorsType createLocatorsType() {
      return new LocatorsType();
   }

   /**
    * Create an instance of {@link JAXBElement }{@code <}{@link PoliciesType }{@code >}}
    * 
    */
   @XmlElementDecl(namespace = "urn:jboss:xacml:2.0", name = "Policies")
   public JAXBElement<PoliciesType> createPolicies(PoliciesType value) {
      return new JAXBElement<PoliciesType>(_Policies_QNAME, PoliciesType.class, null, value);
   }

   /**
    * Create an instance of {@link JAXBElement }{@code <}{@link LocatorsType }{@code >}}
    * 
    */
   @XmlElementDecl(namespace = "urn:jboss:xacml:2.0", name = "Locators")
   public JAXBElement<LocatorsType> createLocators(LocatorsType value) {
      return new JAXBElement<LocatorsType>(_Locators_QNAME, LocatorsType.class, null, value);
   }

   /**
    * Create an instance of {@link JAXBElement }{@code <}{@link PDP }{@code >}}
    * 
    */
   @XmlElementDecl(namespace = "urn:jboss:xacml:2.0", name = "jbosspdp")
   public JAXBElement<PDP> createJbosspdp(PDP value) {
      return new JAXBElement<PDP>(_Jbosspdp_QNAME, PDP.class, null, value);
   }

}
