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


package net.seagis.ows;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * This type is adapted from the EnvelopeType of GML 3.1, with modified contents and documentation for encoding a MINIMUM size box SURROUNDING all associated data. 
 * 
 * <p>Java class for BoundingBoxType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BoundingBoxType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LowerCorner" type="{http://www.opengis.net/ows/1.1}PositionType"/>
 *         &lt;element name="UpperCorner" type="{http://www.opengis.net/ows/1.1}PositionType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="crs" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="dimensions" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoundingBoxType", propOrder = {
    "lowerCorner",
    "upperCorner"
})
@XmlSeeAlso({
    WGS84BoundingBoxType.class
})
public class BoundingBoxType {

    @XmlList
    @XmlElement(name = "LowerCorner", type = Double.class)
    protected List<Double> lowerCorner  = new ArrayList<Double>();
    @XmlList
    @XmlElement(name = "UpperCorner", type = Double.class)
    protected List<Double> upperCorner = new ArrayList<Double>();
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String crs;
    @XmlAttribute
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger dimensions;

    BoundingBoxType(){
    }
    
    /**
     * Build a 2 dimension boundingBox.
     * 
     * @param crs
     * @param maxx
     * @param maxy
     * @param minx
     * @param miny
     */
    public BoundingBoxType(String crs, double maxx, double maxy, double minx, double miny){
        this.dimensions = new BigInteger("2");
        this.lowerCorner.add(minx);
        this.lowerCorner.add(maxy);
        this.upperCorner.add(maxx);
        this.upperCorner.add(miny);
        this.crs = crs;
    }
    
    /**
     * Gets the value of the lowerCorner property.
     * (unmodifiable)
     */
    public List<Double> getLowerCorner() {
        return Collections.unmodifiableList(lowerCorner);
    }

    /**
     * Gets the value of the upperCorner property.
     * (unmodifiable)
     */
    public List<Double> getUpperCorner() {
        return Collections.unmodifiableList(upperCorner);
    }

    /**
     * Gets the value of the crs property.
     * 
     */
    public String getCrs() {
        return crs;
    }

    /**
     * Gets the value of the dimensions property.
     */
    public BigInteger getDimensions() {
        return dimensions;
    }
}
