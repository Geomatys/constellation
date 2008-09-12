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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;


/**
 * <p>Java class for FilterType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/ogc}spatialOps"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}comparisonOps"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}logicOps"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}_Id" maxOccurs="unbounded"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterType", propOrder = {
    "spatialOps",
    "comparisonOps",
    "logicOps",
    "id"
})
@XmlRootElement(name = "Filter")
public class FilterType implements Filter {

    @XmlElementRef(name = "spatialOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    private JAXBElement<? extends SpatialOpsType> spatialOps;
    @XmlElementRef(name = "comparisonOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    private JAXBElement<? extends ComparisonOpsType> comparisonOps;
    @XmlElementRef(name = "logicOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    private JAXBElement<? extends LogicOpsType> logicOps;
    @XmlElementRef(name = "_Id", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    private List<JAXBElement<? extends AbstractIdType>> id;

    /**
     * a transient factory to build JAXBelement
     */
    @XmlTransient
    private static ObjectFactory FACTORY = new ObjectFactory();
    
    /**
     * An empty constructor used by JAXB
     */
    public FilterType() {
        
    }
    
    /**
     * build a new FilterType with the specified logical operator
     */
    public FilterType(Object obj) {
        
        // comparison operator
        if (obj instanceof ComparisonOpsType) {
            this.comparisonOps = createComparisonOps((ComparisonOpsType) obj);
            
        // logical operator    
        } else if (obj instanceof LogicOpsType) {
            this.logicOps = createLogicOps((LogicOpsType) obj);
            
        // spatial operator    
        } else if (obj instanceof SpatialOpsType) {
            this.spatialOps = createSpatialOps((SpatialOpsType) obj);
        
        } else {
            throw new IllegalArgumentException("This kind of object is not allowed:" + obj.getClass().getSimpleName());
        }
    }
    
    /**
     * Gets the value of the spatialOps property.
     */
    public JAXBElement<? extends SpatialOpsType> getSpatialOps() {
        return spatialOps;
    }
    
    /**
     * Sets the value of the spatialOps property.
     */
    public void setSpatialOps(JAXBElement<? extends SpatialOpsType> spatialOps) {
        this.spatialOps = spatialOps;
    }
    
    /**
     * Sets the value of the spatialOps property.
     */
    public void setSpatialOps(SpatialOpsType spatialOps) {
        this.spatialOps = createSpatialOps(spatialOps);
    }

    /**
     * Gets the value of the comparisonOps property.
     */
    public JAXBElement<? extends ComparisonOpsType> getComparisonOps() {
        return comparisonOps;
    }
    
    /**
     * Sets the value of the comparisonOps property.
     */
    public void setComparisonOps(JAXBElement<? extends ComparisonOpsType> comparisonOps) {
        this.comparisonOps = comparisonOps;
    }
    
    /**
     * Sets the value of the comparisonOps property.
     */
    public void setComparisonOps(ComparisonOpsType comparisonOps) {
        this.comparisonOps = createComparisonOps(comparisonOps);
    }

    /**
     * Gets the value of the logicOps property.
     */
    public JAXBElement<? extends LogicOpsType> getLogicOps() {
        return logicOps;
    }
    
    /**
     * Sets the value of the logicOps property.
     */
    public void setLogicOps(JAXBElement<? extends LogicOpsType> logicOps) {
        this.logicOps = logicOps;
    }
    
    /**
     * Sets the value of the logicOps property.
     */
    public void setLogicOps(LogicOpsType logicOps) {
        this.logicOps = createLogicOps(logicOps);
    }

    /**
     * Gets the value of the id property.
     * (unmodifiable) 
     */
    public List<JAXBElement<? extends AbstractIdType>> getId() {
        if (id == null) {
            id = new ArrayList<JAXBElement<? extends AbstractIdType>>();
        }
        return Collections.unmodifiableList(id);
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[").append(this.getClass().getSimpleName()).append(']').append('\n');
        if (spatialOps != null) {
            s.append("SpatialOps: ").append(spatialOps.getValue().toString()).append('\n');
        }
        if (comparisonOps != null) {
            s.append("ComparisonOps: ").append(comparisonOps.getValue().toString()).append('\n');
        }
        if (logicOps != null) {
            s.append("LogicOps: ").append(logicOps.getValue().toString()).append('\n');
        }
        if (id != null) {
            s.append("id:").append('\n');
            int i = 0;
            for (JAXBElement<? extends AbstractIdType> jb: id) {
                s.append("id " + i + ": ").append(jb.getValue().toString()).append('\n');
                i++;
            }
        }
        return s.toString();
    }

    public boolean evaluate(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object accept(FilterVisitor visitor, Object extraData) {
        return extraData;
    }
    
    public static JAXBElement<? extends ComparisonOpsType> createComparisonOps(ComparisonOpsType operator) {
        
        if (operator instanceof PropertyIsLessThanOrEqualToType) {
            return FACTORY.createPropertyIsLessThanOrEqualTo((PropertyIsLessThanOrEqualToType) operator);
        } else if (operator instanceof PropertyIsLessThanType) {
            return FACTORY.createPropertyIsLessThan((PropertyIsLessThanType) operator);
        } else if (operator instanceof PropertyIsGreaterThanOrEqualToType) {
            return FACTORY.createPropertyIsGreaterThanOrEqualTo((PropertyIsGreaterThanOrEqualToType) operator);
        } else if (operator instanceof PropertyIsNotEqualToType) {
            return FACTORY.createPropertyIsNotEqualTo((PropertyIsNotEqualToType) operator);
        } else if (operator instanceof PropertyIsGreaterThanType) {
            return FACTORY.createPropertyIsGreaterThan((PropertyIsGreaterThanType) operator);
        } else if (operator instanceof PropertyIsEqualToType) {
            return FACTORY.createPropertyIsEqualTo((PropertyIsEqualToType) operator);
        } else if (operator instanceof PropertyIsNullType) {
            return FACTORY.createPropertyIsNull((PropertyIsNullType) operator);
        } else if (operator instanceof PropertyIsBetweenType) {
            return FACTORY.createPropertyIsBetween((PropertyIsBetweenType) operator);
        } else if (operator instanceof PropertyIsLikeType) {
            return FACTORY.createPropertyIsLike((PropertyIsLikeType) operator);
        } else if (operator instanceof ComparisonOpsType) {
            return FACTORY.createComparisonOps((ComparisonOpsType) operator);
        } else return null;
    }
    
    public static JAXBElement<? extends LogicOpsType> createLogicOps(LogicOpsType operator) {
        
        if (operator instanceof OrType) {
            return FACTORY.createOr((OrType) operator);
        } else if (operator instanceof NotType) {
            return FACTORY.createNot((NotType) operator);
        } else if (operator instanceof AndType) {
            return FACTORY.createAnd((AndType) operator);
        } else if (operator instanceof LogicOpsType) {
            return FACTORY.createLogicOps((LogicOpsType) operator);
        } else return null;
    }
    
    public static JAXBElement<? extends SpatialOpsType> createSpatialOps(SpatialOpsType operator) {
        
        if (operator instanceof BeyondType) {
            return FACTORY.createBeyond((BeyondType) operator);
        } else if (operator instanceof DWithinType) {
            return FACTORY.createDWithin((DWithinType) operator);
        } else if (operator instanceof BBOXType) {
            return FACTORY.createBBOX((BBOXType) operator);
        } else if (operator instanceof ContainsType) {
            return FACTORY.createContains((ContainsType) operator);
        } else if (operator instanceof CrossesType) {
            return FACTORY.createCrosses((CrossesType) operator);
        } else if (operator instanceof DisjointType) {
            return FACTORY.createDisjoint((DisjointType) operator);
        } else if (operator instanceof EqualsType) {
            return FACTORY.createEquals((EqualsType) operator);
        } else if (operator instanceof IntersectsType) {
            return FACTORY.createIntersects((IntersectsType) operator);
        } else if (operator instanceof OverlapsType) {
            return FACTORY.createOverlaps((OverlapsType) operator);
        } else if (operator instanceof TouchesType) {
            return FACTORY.createTouches((TouchesType) operator);
        } else if (operator instanceof WithinType) {
            return FACTORY.createWithin((WithinType) operator);
        } else if (operator instanceof SpatialOpsType) {
            return FACTORY.createSpatialOps((SpatialOpsType) operator);
        } else {
            return null;
        }
    }

}
