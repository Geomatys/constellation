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


package net.seagis.sld.v110;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.se.CoverageStyleType;
import net.seagis.se.DescriptionType;
import net.seagis.se.OnlineResourceType;


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
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}IsDefault" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{http://www.opengis.net/se}FeatureTypeStyle"/>
 *           &lt;element ref="{http://www.opengis.net/se}CoverageStyle"/>
 *           &lt;element ref="{http://www.opengis.net/se}OnlineResource"/>
 *         &lt;/choice>
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
    "name",
    "description",
    "isDefault",
    "featureTypeStyleOrCoverageStyleOrOnlineResource"
})
@XmlRootElement(name = "UserStyle")
public class UserStyle {

    @XmlElement(name = "Name", namespace = "http://www.opengis.net/se")
    private String name;
    @XmlElement(name = "Description", namespace = "http://www.opengis.net/se")
    private DescriptionType description;
    @XmlElement(name = "IsDefault")
    private Boolean isDefault;
    @XmlElements({
        @XmlElement(name = "OnlineResource", namespace = "http://www.opengis.net/se", type = OnlineResourceType.class),
        @XmlElement(name = "CoverageStyle", namespace = "http://www.opengis.net/se", type = CoverageStyleType.class)
    })
    private List<Object> coverageStyleOrOnlineResource;

    /**
     * Empty Constructor used by JAXB.
     */
    UserStyle() {
        
    }
    
    /**
     * Build a new user style.
     */
    public UserStyle(String name, DescriptionType description, Boolean isDefault,
            List<Object> coverageStyleOrOnlineResource) {
        this.name = name;
        this.description = description;
        this.coverageStyleOrOnlineResource = coverageStyleOrOnlineResource;
        
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
     * Gets the value of the isDefault property.
     */
    public Boolean isIsDefault() {
        return isDefault;
    }

    /**
     * Gets the value of the coverageStyleOrOnlineResource property.
     * (unmodifiable)
     */
    public List<Object> getCoverageStyleOrOnlineResource() {
        if (coverageStyleOrOnlineResource == null) {
            coverageStyleOrOnlineResource = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(coverageStyleOrOnlineResource);
    }

}
