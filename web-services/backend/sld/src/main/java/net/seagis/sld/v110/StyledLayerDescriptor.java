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
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.se.DescriptionType;


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
 *         &lt;element ref="{http://www.opengis.net/sld}UseSLDLibrary" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}NamedLayer"/>
 *           &lt;element ref="{http://www.opengis.net/sld}UserLayer"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.opengis.net/se}VersionType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "description",
    "useSLDLibrary",
    "namedLayerOrUserLayer"
})
@XmlRootElement(name = "StyledLayerDescriptor")
public class StyledLayerDescriptor {

    @XmlElement(name = "Name", namespace = "http://www.opengis.net/se")
    private String name;
    @XmlElement(name = "Description", namespace = "http://www.opengis.net/se")
    private DescriptionType description;
    @XmlElement(name = "UseSLDLibrary")
    private List<UseSLDLibrary> useSLDLibrary;
    @XmlElements({
        @XmlElement(name = "NamedLayer", type = NamedLayer.class),
        @XmlElement(name = "UserLayer", type = UserLayer.class)
    })
    private List<Object> namedLayerOrUserLayer;
    @XmlAttribute(required = true)
    private String version;

    /**
     * Empty Constructor used by JAXB.
     */
    StyledLayerDescriptor() {
        
    }
    
    /**
     * build a new Styled layer descriptor (SLD).
     */
    public StyledLayerDescriptor(String name, DescriptionType description, List<UseSLDLibrary> useSLDLibrary,
            List<Object> namedLayerOrUserLayer, String version) {
        this.name                  = name;
        this.description           = description;
        this.useSLDLibrary         = useSLDLibrary;
        this.namedLayerOrUserLayer = namedLayerOrUserLayer;
        this.version               = version;
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
     * Gets the value of the useSLDLibrary property.
     */
    public List<UseSLDLibrary> getUseSLDLibrary() {
        if (useSLDLibrary == null) {
            useSLDLibrary = new ArrayList<UseSLDLibrary>();
        }
        return Collections.unmodifiableList(useSLDLibrary);
    }

    /**
     * Gets the value of the namedLayerOrUserLayer property.
     */
    public List<Object> getNamedLayerOrUserLayer() {
        if (namedLayerOrUserLayer == null) {
            namedLayerOrUserLayer = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(namedLayerOrUserLayer);
    }

    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        return version;
    }
}
