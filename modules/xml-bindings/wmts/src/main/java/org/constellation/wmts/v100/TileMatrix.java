/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.wmts.v100;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.constellation.gml.v311.PointType;
import org.constellation.ows.v110.CodeType;
import org.constellation.ows.v110.DescriptionType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}DescriptionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Identifier"/>
 *         &lt;element name="ScaleDenominator" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="TopLeftCorner" type="{http://www.opengis.net/ows/1.1}PositionType"/>
 *         &lt;element name="TileWidth" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="TileHeight" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="MatrixWidth" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="MatrixHeight" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TileMatrixType", propOrder = {
    "identifier",
    "scaleDenominator",
    "topLeftCorner",
    "topLeftPoint",
    "tileWidth",
    "tileHeight",
    "matrixWidth",
    "matrixHeight"
})
@XmlRootElement(name = "TileMatrix")
public class TileMatrix
    extends DescriptionType
{

    @XmlElement(name = "Identifier", namespace = "http://www.opengis.net/ows/1.1", required = true)
    protected CodeType identifier;
    @XmlElement(name = "ScaleDenominator")
    protected double scaleDenominator;
    @XmlList
    @XmlElement(name = "TopLeftCorner", type = Double.class)
    protected List<Double> topLeftCorner;
    @XmlElement(name = "TopPoint")
    private PointType topLeftPoint;
    @XmlElement(name = "TileWidth", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger tileWidth;
    @XmlElement(name = "TileHeight", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger tileHeight;
    @XmlElement(name = "MatrixWidth", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger matrixWidth;
    @XmlElement(name = "MatrixHeight", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger matrixHeight;

    /**
     * Tile matrix identifier. Typically an abreviation of the ScaleDenominator value or its equivalent pixel size
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getIdentifier() {
        return identifier;
    }

    /**
     * Tile matrix identifier. Typically an abreviation of the ScaleDenominator value or its equivalent pixel size
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setIdentifier(CodeType value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the scaleDenominator property.
     * 
     */
    public double getScaleDenominator() {
        return scaleDenominator;
    }

    /**
     * Sets the value of the scaleDenominator property.
     * 
     */
    public void setScaleDenominator(double value) {
        this.scaleDenominator = value;
    }

    /**
     * Gets the value of the topLeftCorner property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the topLeftCorner property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTopLeftCorner().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Double }
     * 
     * 
     */
    public List<Double> getTopLeftCorner() {
        if (topLeftCorner == null) {
            topLeftCorner = new ArrayList<Double>();
        }
        return this.topLeftCorner;
    }

    /**
     * Gets the value of the tileWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTileWidth() {
        return tileWidth;
    }

    /**
     * Sets the value of the tileWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTileWidth(BigInteger value) {
        this.tileWidth = value;
    }

    /**
     * Gets the value of the tileHeight property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTileHeight() {
        return tileHeight;
    }

    /**
     * Sets the value of the tileHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTileHeight(BigInteger value) {
        this.tileHeight = value;
    }

    /**
     * Gets the value of the matrixWidth property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMatrixWidth() {
        return matrixWidth;
    }

    /**
     * Sets the value of the matrixWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMatrixWidth(BigInteger value) {
        this.matrixWidth = value;
    }

    /**
     * Gets the value of the matrixHeight property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMatrixHeight() {
        return matrixHeight;
    }

    /**
     * Sets the value of the matrixHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMatrixHeight(BigInteger value) {
        this.matrixHeight = value;
    }

    /**
     * @return the topLeftPoint
     */
    public PointType getTopLeftPoint() {
        return topLeftPoint;
    }

    /**
     * @param topLeftPoint the topLeftPoint to set
     */
    public void setTopLeftPoint(PointType topLeftPoint) {
        this.topLeftPoint = topLeftPoint;
    }

}
