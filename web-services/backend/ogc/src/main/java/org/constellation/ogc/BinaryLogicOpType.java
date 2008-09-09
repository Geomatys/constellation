/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.ogc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Or;


/**
 * <p>Java class for BinaryLogicOpType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BinaryLogicOpType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}LogicOpsType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="2">
 *         &lt;element ref="{http://www.opengis.net/ogc}comparisonOps"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}spatialOps"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}logicOps"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryLogicOpType", propOrder = {
    "operators"
})
public class BinaryLogicOpType extends LogicOpsType {

    @XmlElementRefs({
        @XmlElementRef(name = "comparisonOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class),
        @XmlElementRef(name = "spatialOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class),
        @XmlElementRef(name = "logicOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    })
    private List<JAXBElement<?>> operators;

    /**
     * an transient ogc factory to build JAXBelement
     */
    @XmlTransient
    private ObjectFactory factory = new ObjectFactory();
    
    /**
     * An empty constructor used by JAXB
     */
     BinaryLogicOpType() {
         
     }
     
     /**
      * Build a new Binary logic operator 
      */
     public BinaryLogicOpType(Object... operators) {
         this.operators = new ArrayList<JAXBElement<?>>();
         for (Object obj: operators) {
             
             // comparison operator
             if (obj instanceof PropertyIsLessThanOrEqualToType) {
                 this.operators.add(factory.createPropertyIsLessThanOrEqualTo((PropertyIsLessThanOrEqualToType)obj));
             } else if (obj instanceof PropertyIsLessThanType) {
                 this.operators.add(factory.createPropertyIsLessThan((PropertyIsLessThanType)obj));
             } else if (obj instanceof PropertyIsGreaterThanOrEqualToType) {
                 this.operators.add(factory.createPropertyIsGreaterThanOrEqualTo((PropertyIsGreaterThanOrEqualToType)obj));
             } else if (obj instanceof PropertyIsNotEqualToType) {
                 this.operators.add(factory.createPropertyIsNotEqualTo((PropertyIsNotEqualToType)obj));
             } else if (obj instanceof PropertyIsGreaterThanType) {
                 this.operators.add(factory.createPropertyIsGreaterThan((PropertyIsGreaterThanType)obj));
             } else if (obj instanceof PropertyIsEqualToType) {
                 this.operators.add(factory.createPropertyIsEqualTo((PropertyIsEqualToType)obj));
             } else if (obj instanceof PropertyIsNullType) {
                 this.operators.add(factory.createPropertyIsNull((PropertyIsNullType)obj));
             } else if (obj instanceof PropertyIsBetweenType) {
                 this.operators.add(factory.createPropertyIsBetween((PropertyIsBetweenType)obj));
             } else if (obj instanceof PropertyIsLikeType) {
                 this.operators.add(factory.createPropertyIsLike((PropertyIsLikeType)obj));
             } else if (obj instanceof ComparisonOpsType) {
                 this.operators.add(factory.createComparisonOps((ComparisonOpsType)obj));
                 
             // logical operator    
             } else if (obj instanceof OrType) {
                 this.operators.add(factory.createOr((OrType)obj));
             } else if (obj instanceof NotType) {
                 this.operators.add(factory.createNot((NotType)obj));
             } else if (obj instanceof AndType) {
                 this.operators.add(factory.createAnd((AndType)obj));
             } else if (obj instanceof LogicOpsType) {
                 this.operators.add(factory.createLogicOps((LogicOpsType)obj));
             
             // spatial operator    
             } else if (obj instanceof BeyondType) {
                 this.operators.add(factory.createBeyond((DistanceBufferType) obj));
             } else if (obj instanceof DWithinType) {
                 this.operators.add(factory.createDWithin((DistanceBufferType) obj));
             } else if (obj instanceof BBOXType) {
                 this.operators.add(factory.createBBOX((BBOXType) obj));
             } else if (obj instanceof ContainsType) {
                 this.operators.add(factory.createContains((BinarySpatialOpType) obj));
             } else if (obj instanceof CrossesType) {
                 this.operators.add(factory.createCrosses((BinarySpatialOpType) obj));
             } else if (obj instanceof DisjointType) {
                 this.operators.add(factory.createDisjoint((BinarySpatialOpType) obj));
             } else if (obj instanceof EqualsType) {
                 this.operators.add(factory.createEquals((BinarySpatialOpType) obj));
             } else if (obj instanceof IntersectsType) {
                 this.operators.add(factory.createIntersects((BinarySpatialOpType) obj));
             } else if (obj instanceof OverlapsType) {
                 this.operators.add(factory.createOverlaps((BinarySpatialOpType) obj));
             } else if (obj instanceof TouchesType) {
                 this.operators.add(factory.createTouches((BinarySpatialOpType) obj));
             } else if (obj instanceof WithinType) {
                 this.operators.add(factory.createWithin((BinarySpatialOpType) obj));
             } else if (obj instanceof SpatialOpsType) {
                 this.operators.add(factory.createSpatialOps((SpatialOpsType) obj));
             
             } else {
                 throw new IllegalArgumentException("This kind of object is not allowed:" + obj.getClass().getSimpleName());
             }
         }
         
     }
     
    /**
     * Gets the value of the comparisonOpsOrSpatialOpsOrLogicOps property.
     * (unmodifable)
     */
    public List<JAXBElement<?>> getOperators() {
        if (operators == null) {
            operators = new ArrayList<JAXBElement<?>>();
        }
        return Collections.unmodifiableList(operators);
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        if (operators != null) {
            int i = 0; 
            for (JAXBElement<?> jb: operators) {
                s.append(i).append(": ").append(jb.getValue().toString()).append('\n');
                i++;        
            }
        }
        return s.toString();
    }

    public List<Filter> getChildren() {
        List<Filter> result = new ArrayList<Filter>();
        for (JAXBElement jb: operators) {
            result.add((Filter)jb.getValue());
        }
        return result;
    }

    public boolean evaluate(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object accept(FilterVisitor visitor, Object extraData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
