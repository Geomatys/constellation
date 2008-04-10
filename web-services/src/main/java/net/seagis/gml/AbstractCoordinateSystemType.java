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

package net.seagis.gml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A coordinate system (CS) is the set of coordinate system axes that spans a given coordinate space. A CS is derived from a set of (mathematical) rules for specifying how coordinates in a given space are to be assigned to points. The coordinate values in a coordinate tuple shall be recorded in the order in which the coordinate system axes associations are recorded, whenever those coordinates use a coordinate reference system that uses this coordinate system. This abstract complexType shall not be used, extended, or restricted, in an Application Schema, to define a concrete subtype with a meaning equivalent to a concrete subtype specified in this document. 
 * 
 * <p>Java class for AbstractCoordinateSystemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractCoordinateSystemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractCoordinateSystemBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}csID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}remarks" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}usesAxis" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractCoordinateSystemType", propOrder = {
    "csID",
    "usesAxis"
})
/*@XmlSeeAlso({
    ObliqueCartesianCSType.class,
    VerticalCSType.class,
    LinearCSType.class,
    SphericalCSType.class,
    EllipsoidalCSType.class,
    CartesianCSType.class,
    CylindricalCSType.class,
    TemporalCSType.class,
    UserDefinedCSType.class,
    PolarCSType.class
})*/
public abstract class AbstractCoordinateSystemType extends AbstractCoordinateSystemBaseType {

    private List<IdentifierType> csID;
    @XmlElement(required = true)
    private List<CoordinateSystemAxisRefType> usesAxis;

    /**
     * Set of alternative identifications of this coordinate system. 
     * The first csID, if any, is normally the primary identification code, 
     * and any others are aliases. Gets the value of the csID property.
     * (unmodifiable)
     */
    public List<IdentifierType> getCsID() {
        if (csID == null) {
            csID = new ArrayList<IdentifierType>();
        }
        return Collections.unmodifiableList(csID);
    }

   /**
     * Ordered sequence of associations to the coordinate system axes included in this coordinate system.
     * Gets the value of the usesAxis property.
     * (unmodifiable)
     */
    public List<CoordinateSystemAxisRefType> getUsesAxis() {
        if (usesAxis == null) {
            usesAxis = new ArrayList<CoordinateSystemAxisRefType>();
        }
        return Collections.unmodifiableList(usesAxis);
    }

}
