/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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



package net.seagis.se;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CoverageStyleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoverageStyleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}CoverageName" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}SemanticTypeIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{http://www.opengis.net/se}Rule"/>
 *           &lt;element ref="{http://www.opengis.net/se}OnlineResource"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.opengis.net/se}VersionType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoverageStyleType", propOrder = {
    "name",
    "description",
    "coverageName",
    "semanticTypeIdentifier",
    "ruleOrOnlineResource"
})
public class CoverageStyleType {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Description")
    private DescriptionType description;
    @XmlElement(name = "CoverageName")
    private String coverageName;
    @XmlElement(name = "SemanticTypeIdentifier")
    private List<String> semanticTypeIdentifier;
    @XmlElements({
        @XmlElement(name = "OnlineResource", type = OnlineResourceType.class),
        @XmlElement(name = "Rule", type = RuleType.class)
    })
    private List<Object> ruleOrOnlineResource;
    @XmlAttribute
    private String version;

    /**
     * Empty Constructor used by JAXB.
     */
    CoverageStyleType() {
        
    }
    
    /**
     * Build a new Coverage Style.
     */
    public CoverageStyleType(String name, DescriptionType description, String coverageName, 
            List<String> semanticTypeIdentifier, List<Object> ruleOrOnlineResource, String version) {
        this.name                   = name;
        this.description            = description;
        this.coverageName           = coverageName;
        this.semanticTypeIdentifier = semanticTypeIdentifier;
        this.ruleOrOnlineResource   = ruleOrOnlineResource;
        this.version                = version;
    }
    
    
    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the description property.
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Gets the value of the coverageName property.
     */
    public String getCoverageName() {
        return coverageName;
    }

    /**
     * Gets the value of the semanticTypeIdentifier property.
     */
    public List<String> getSemanticTypeIdentifier() {
        if (semanticTypeIdentifier == null) {
            semanticTypeIdentifier = new ArrayList<String>();
        }
        return Collections.unmodifiableList(semanticTypeIdentifier);
    }

    /**
     * Gets the value of the ruleOrOnlineResource property.
     */
    public List<Object> getRuleOrOnlineResource() {
        if (ruleOrOnlineResource == null) {
            ruleOrOnlineResource = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(ruleOrOnlineResource);
    }

    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        return version;
    }
}
