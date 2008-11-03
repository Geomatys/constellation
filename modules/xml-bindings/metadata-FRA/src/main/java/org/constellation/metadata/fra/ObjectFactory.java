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

package org.constellation.metadata.fra;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.constellation.referencing.ReferenceSystemImpl;
import org.geotools.metadata.iso.constraint.ConstraintsImpl;
import org.geotools.metadata.iso.constraint.LegalConstraintsImpl;
import org.geotools.metadata.iso.constraint.SecurityConstraintsImpl;
import org.geotools.metadata.iso.identification.DataIdentificationImpl;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fr.gouv.cnig._2005.fra package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LegalConstraints_QNAME           = new QName("http://www.isotc211.org/2005/gmd", "MD_LegalConstraints");
    private final static QName _FRALegalConstraints_QNAME        = new QName("http://www.cnig.gouv.fr/2005/fra", "FRA_LegalConstraints");
    private final static QName _ReferenceSystem_QNAME            = new QName("http://www.isotc211.org/2005/gmd", "MD_ReferenceSystem");
    private final static QName _FRADirectReferenceSystem_QNAME   = new QName("http://www.cnig.gouv.fr/2005/fra", "FRA_DirectReferenceSystem");
    private final static QName _Constraints_QNAME                = new QName("http://www.isotc211.org/2005/gmd", "MD_Constraints");
    private final static QName _FRAConstraints_QNAME             = new QName("http://www.cnig.gouv.fr/2005/fra", "FRA_Constraints");
    private final static QName _FRADataIdentification_QNAME      = new QName("http://www.cnig.gouv.fr/2005/fra", "FRA_DataIdentification");
    private final static QName _DataIdentification_QNAME         = new QName("http://www.isotc211.org/2005/gmd", "MD_DataIdentification");
    private final static QName _FRAIndirectReferenceSystem_QNAME = new QName("http://www.cnig.gouv.fr/2005/fra", "FRA_IndirectReferenceSystem");
    private final static QName _SecurityConstraints_QNAME        = new QName("http://www.isotc211.org/2005/gmd", "MD_SecurityConstraints");
    private final static QName _FRASecurityConstraints_QNAME     = new QName("http://www.cnig.gouv.fr/2005/fra", "FRA_SecurityConstraints");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fr.gouv.cnig._2005.fra
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FRASecurityConstraintsType }
     * 
     */
    public FRASecurityConstraintsType createFRASecurityConstraintsType() {
        return new FRASecurityConstraintsType();
    }

    /**
     * Create an instance of {@link FRALegalConstraintsType }
     * 
     */
    public FRALegalConstraintsType createFRALegalConstraintsType() {
        return new FRALegalConstraintsType();
    }

    /**
     * Create an instance of {@link FRAConstraintsType }
     * 
     */
    public FRAConstraintsType createFRAConstraintsType() {
        return new FRAConstraintsType();
    }

    /**
     * Create an instance of {@link FRAIndirectReferenceSystemType }
     * 
     */
    public FRAIndirectReferenceSystemType createFRAIndirectReferenceSystemType() {
        return new FRAIndirectReferenceSystemType();
    }

    /**
     * Create an instance of {@link FRADataIdentificationType }
     * 
     */
    public FRADataIdentificationType createFRADataIdentificationType() {
        return new FRADataIdentificationType();
    }

    /**
     * Create an instance of {@link FRADirectReferenceSystemType }
     * 
     */
    public FRADirectReferenceSystemType createFRADirectReferenceSystemType() {
        return new FRADirectReferenceSystemType();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRALegalConstraintsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.isotc211.org/2005/gmd", name = "MD_LegalConstraints")
    public JAXBElement<LegalConstraintsImpl> createLegalConstraints(LegalConstraintsImpl value) {
        return new JAXBElement<LegalConstraintsImpl>(_LegalConstraints_QNAME, LegalConstraintsImpl.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRALegalConstraintsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cnig.gouv.fr/2005/fra", name = "FRA_LegalConstraints", substitutionHeadNamespace = "http://www.isotc211.org/2005/gmd", substitutionHeadName = "MD_LegalConstraints")
    public JAXBElement<FRALegalConstraintsType> createFRALegalConstraints(FRALegalConstraintsType value) {
        return new JAXBElement<FRALegalConstraintsType>(_FRALegalConstraints_QNAME, FRALegalConstraintsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRADirectReferenceSystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.isotc211.org/2005/gmd", name = "MD_ReferenceSystem")
    public JAXBElement<ReferenceSystemImpl> createReferenceSystem(ReferenceSystemImpl value) {
        return new JAXBElement<ReferenceSystemImpl>(_ReferenceSystem_QNAME, ReferenceSystemImpl.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRADirectReferenceSystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cnig.gouv.fr/2005/fra", name = "FRA_DirectReferenceSystem", substitutionHeadNamespace = "http://www.isotc211.org/2005/gmd", substitutionHeadName = "MD_ReferenceSystem")
    public JAXBElement<FRADirectReferenceSystemType> createFRADirectReferenceSystem(FRADirectReferenceSystemType value) {
        return new JAXBElement<FRADirectReferenceSystemType>(_FRADirectReferenceSystem_QNAME, FRADirectReferenceSystemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRAConstraintsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.isotc211.org/2005/gmd", name = "MD_Constraints")
    public JAXBElement<ConstraintsImpl> createConstraints(ConstraintsImpl value) {
        return new JAXBElement<ConstraintsImpl>(_Constraints_QNAME, ConstraintsImpl.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRAConstraintsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cnig.gouv.fr/2005/fra", name = "FRA_Constraints", substitutionHeadNamespace = "http://www.isotc211.org/2005/gmd", substitutionHeadName = "MD_Constraints")
    public JAXBElement<FRAConstraintsType> createFRAConstraints(FRAConstraintsType value) {
        return new JAXBElement<FRAConstraintsType>(_FRAConstraints_QNAME, FRAConstraintsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRADataIdentificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.isotc211.org/2005/gmd", name = "MD_DataIdentification")
    public JAXBElement<DataIdentificationImpl> createDataIdentification(DataIdentificationImpl value) {
        return new JAXBElement<DataIdentificationImpl>(_DataIdentification_QNAME, DataIdentificationImpl.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRADataIdentificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cnig.gouv.fr/2005/fra", name = "FRA_DataIdentification", substitutionHeadNamespace = "http://www.isotc211.org/2005/gmd", substitutionHeadName = "MD_DataIdentification")
    public JAXBElement<FRADataIdentificationType> createFRADataIdentification(FRADataIdentificationType value) {
        return new JAXBElement<FRADataIdentificationType>(_FRADataIdentification_QNAME, FRADataIdentificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRAIndirectReferenceSystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cnig.gouv.fr/2005/fra", name = "FRA_IndirectReferenceSystem", substitutionHeadNamespace = "http://www.isotc211.org/2005/gmd", substitutionHeadName = "MD_ReferenceSystem")
    public JAXBElement<FRAIndirectReferenceSystemType> createFRAIndirectReferenceSystem(FRAIndirectReferenceSystemType value) {
        return new JAXBElement<FRAIndirectReferenceSystemType>(_FRAIndirectReferenceSystem_QNAME, FRAIndirectReferenceSystemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRASecurityConstraintsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.isotc211.org/2005/gmd", name = "MD_SecurityConstraints")
    public JAXBElement<SecurityConstraintsImpl> createSecurityConstraints(SecurityConstraintsImpl value) {
        return new JAXBElement<SecurityConstraintsImpl>(_SecurityConstraints_QNAME, SecurityConstraintsImpl.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FRASecurityConstraintsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cnig.gouv.fr/2005/fra", name = "FRA_SecurityConstraints", substitutionHeadNamespace = "http://www.isotc211.org/2005/gmd", substitutionHeadName = "MD_SecurityConstraints")
    public JAXBElement<FRASecurityConstraintsType> createFRASecurityConstraints(FRASecurityConstraintsType value) {
        return new JAXBElement<FRASecurityConstraintsType>(_FRASecurityConstraints_QNAME, FRASecurityConstraintsType.class, null, value);
    }

}
