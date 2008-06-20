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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         A "SymbolizerType" is an abstract type for encoding the graphical
 *         properties used to portray geographic information.  Concrete Symbolizer
 *         types are derived from this base type.
 *       
 * 
 * <p>Java class for SymbolizerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SymbolizerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}BaseSymbolizer" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.opengis.net/se}VersionType" />
 *       &lt;attribute name="uom" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SymbolizerType", propOrder = {
    "name",
    "description",
    "baseSymbolizer"
})
/*@XmlSeeAlso({
    LineSymbolizerType.class,
    PointSymbolizerType.class,
    PolygonSymbolizerType.class,
    TextSymbolizerType.class,
    RasterSymbolizerType.class
})*/
public abstract class SymbolizerType {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Description")
    private DescriptionType description;
    @XmlElement(name = "BaseSymbolizer")
    private BaseSymbolizerType baseSymbolizer;
    @XmlAttribute
    private String version;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String uom;

    /**
     * Empty Constructor used by JAXB.
     */
    SymbolizerType() {
        
    }
    
    /**
     * Build a new Symbolizer.
     */
    public SymbolizerType(String name, DescriptionType description, BaseSymbolizerType baseSymbolizer,
            String version, String uom) {
        this.name           = name;
        this.description    = description;
        this.baseSymbolizer = baseSymbolizer;
        this.version        = version;
        this.uom            = uom;
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
     * Gets the value of the baseSymbolizer property.
     */
    public BaseSymbolizerType getBaseSymbolizer() {
        return baseSymbolizer;
    }

    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the value of the uom property.
     */
    public String getUom() {
        return uom;
    }
}
