/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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


package net.seagis.ows.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *     &lt;extension base="{http://www.opengis.net/ows}DescriptionType">
 *       &lt;sequence>
 *         &lt;element name="ServiceType" type="{http://www.opengis.net/ows}CodeType"/>
 *         &lt;element name="ServiceTypeVersion" type="{http://www.opengis.net/ows}VersionType" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.opengis.net/ows}Fees" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows}AccessConstraints" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "serviceType",
    "serviceTypeVersion",
    "fees",
    "accessConstraints"
})
@XmlRootElement(name = "ServiceIdentification")
public class ServiceIdentification extends DescriptionType {

    @XmlElement(name = "ServiceType", required = true)
    private CodeType serviceType;
    @XmlElement(name = "ServiceTypeVersion", required = true)
    private List<String> serviceTypeVersion;
    @XmlElement(name = "Fees")
    private String fees;
    @XmlElement(name = "AccessConstraints")
    private List<String> accessConstraints;

    /**
     * Gets the value of the serviceType property.
     */
    public CodeType getServiceType() {
        return serviceType;
    }

    /**
     * Gets the value of the serviceTypeVersion property.
     */
    public List<String> getServiceTypeVersion() {
        if (serviceTypeVersion == null) {
            serviceTypeVersion = new ArrayList<String>();
        }
        return this.serviceTypeVersion;
    }

    /**
     * If this element is omitted, no meaning is implied. 
     */
    public String getFees() {
        return fees;
    }

    /**
     * Unordered list of access constraints applied to assure the protection of privacy or intellectual property, 
     * and any other restrictions on retrieving or using data from or otherwise using this server. 
     * The reserved value NONE (case insensitive) shall be used to mean no access constraints are imposed.
     * If this element is omitted, no meaning is implied. Gets the value of the accessConstraints property.
     */
    public List<String> getAccessConstraints() {
        if (accessConstraints == null) {
            accessConstraints = new ArrayList<String>();
        }
        return this.accessConstraints;
    }

}
