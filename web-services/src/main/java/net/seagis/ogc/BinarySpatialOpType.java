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


package net.seagis.ogc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.AbstractGeometryType;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;


/**
 * <p>Java class for BinarySpatialOpType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BinarySpatialOpType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}SpatialOpsType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/gml}AbstractGeometry"/>
 *           &lt;element ref="{http://www.opengis.net/gml}AbstractGeometricPrimitive"/>
 *           &lt;element ref="{http://www.opengis.net/gml}Point"/>
 *           &lt;element ref="{http://www.opengis.net/gml}AbstractImplicitGeometry"/>
 *           &lt;element ref="{http://www.opengis.net/gml}Envelope"/>
 *           &lt;element ref="{http://www.opengis.net/gml}EnvelopeWithTimePeriod"/>
 *           &lt;element ref="{http://www.opengis.net/ogc}PropertyName"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinarySpatialOpType", propOrder = {
    "rest"
})
@XmlSeeAlso({PropertyNameType.class})
public class BinarySpatialOpType extends SpatialOpsType {

    @XmlElementRefs({
        @XmlElementRef(name = "Envelope", namespace = "http://www.opengis.net/gml", type = JAXBElement.class),
        @XmlElementRef(name = "AbstractGeometry", namespace = "http://www.opengis.net/gml", type = JAXBElement.class),
        @XmlElementRef(name = "PropertyName", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    })
    private List<JAXBElement<?>> rest;

    @XmlTransient
    private ObjectFactory ogcFactory = new ObjectFactory();
    
    @XmlTransient
    private net.seagis.gml.ObjectFactory gmlFactory = new net.seagis.gml.ObjectFactory();
    
    /**
     * An empty constructor used by JAXB
     */
    BinarySpatialOpType() {
        
    }
    
    /**
     * Build a new Binary spatial operator
     */
    public BinarySpatialOpType(String propertyName, AbstractGeometryType geometry) {
        rest = new ArrayList<JAXBElement<?>>();
        rest.add(ogcFactory.createPropertyName(new PropertyNameType(propertyName)));
        //TODO specialize
        rest.add(gmlFactory.createAbstractGeometry(geometry));
        
    }
    
    /**
     * Gets the value of the abstractGeometryOrAbstractGeometricPrimitiveOrPoint property.
     */
    public List<JAXBElement<?>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<?>>();
        }
        return Collections.unmodifiableList(rest);
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        if (rest != null) {
            int i = 0;
            for (JAXBElement<?> jb: rest) {
                s.append(i).append(": class:").append(jb.getValue().getClass().getSimpleName()).append('\n');
                s.append(jb.getValue().toString());
                i++;        
            }
        }
        return s.toString();
    }

    public Expression getExpression1() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Expression getExpression2() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean evaluate(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object accept(FilterVisitor visitor, Object extraData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
