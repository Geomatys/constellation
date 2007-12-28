/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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

package net.seagis.wcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.DescriptionType;
import net.seagis.ows.DomainMetadataType;
import net.seagis.ows.MetadataType;


/**
 * This type is largely a subset of the ows:DomainType as needed for a range field axis. 
 * 
 * <p>Java class for AxisType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AxisType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}DescriptionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wcs/1.1.1}AvailableKeys"/>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Meaning" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}DataType" minOccurs="0"/>
 *         &lt;group ref="{http://www.opengis.net/ows/1.1}ValuesUnit" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Metadata" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="identifier" use="required" type="{http://www.opengis.net/wcs/1.1.1}IdentifierType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AxisType", propOrder = {
    "availableKeys",
    "meaning",
    "dataType",
    "uom",
    "referenceSystem",
    "metadata"
})
public class AxisType
    extends DescriptionType
{

    @XmlElement(name = "AvailableKeys", required = true)
    protected AvailableKeys availableKeys;
    @XmlElement(name = "Meaning", namespace = "http://www.opengis.net/ows/1.1")
    protected DomainMetadataType meaning;
    @XmlElement(name = "DataType", namespace = "http://www.opengis.net/ows/1.1")
    protected DomainMetadataType dataType;
    @XmlElement(name = "UOM", namespace = "http://www.opengis.net/ows/1.1")
    protected DomainMetadataType uom;
    @XmlElement(name = "ReferenceSystem", namespace = "http://www.opengis.net/ows/1.1")
    protected DomainMetadataType referenceSystem;
    @XmlElement(name = "Metadata", namespace = "http://www.opengis.net/ows/1.1")
    protected List<MetadataType> metadata = new ArrayList<MetadataType>();
    @XmlAttribute(required = true)
    protected String identifier;

    /**
     * Gets the value of the availableKeys property.
     */
    public AvailableKeys getAvailableKeys() {
        return availableKeys;
    }

    /**
     * Meaning metadata, which should be referenced for this axis. 
     */
    public DomainMetadataType getMeaning() {
        return meaning;
    }

    /**
     * Data type metadata, which may be referenced for this axis. 
     */
    public DomainMetadataType getDataType() {
        return dataType;
    }

   
    /**
     * Identifier of unit of measure of this set of values. Should be included then this set of values has units (and not a more complete reference system). 
     */
    public DomainMetadataType getUOM() {
        return uom;
    }

    /**
     * Identifier of reference system used by this set of values. Should be included then this set of values has a reference system (not just units). 
     * 
     */
    public DomainMetadataType getReferenceSystem() {
        return referenceSystem;
    }


    /**
     * Optional unordered list of other metadata elements about this axis. A list of required and optional other metadata elements for this quantity can be specified in a WCS Application Profile. Gets the value of the metadata property.
     */
    public List<MetadataType> getMetadata() {
        return Collections.unmodifiableList(this.metadata);
    }

    /**
     * Gets the value of the identifier property.
     */
    public String getIdentifier() {
        return identifier;
    }
}
