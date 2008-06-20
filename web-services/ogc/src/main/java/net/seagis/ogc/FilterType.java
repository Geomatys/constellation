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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Add;


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
    private ObjectFactory factory = new ObjectFactory();
    
    /**
     * An empty constructor used by JAXB
     */
    public FilterType() {
        
    }
    
    /**
     * build a new FilterType with the specified logical operator
     */
    public FilterType(Object obj) {
        if (obj instanceof PropertyIsLessThanOrEqualToType) {
            this.comparisonOps = (factory.createPropertyIsLessThanOrEqualTo((PropertyIsLessThanOrEqualToType) obj));
        } else if (obj instanceof PropertyIsLessThanType) {
            this.comparisonOps = (factory.createPropertyIsLessThan((PropertyIsLessThanType) obj));
        } else if (obj instanceof PropertyIsGreaterThanOrEqualToType) {
            this.comparisonOps = (factory.createPropertyIsGreaterThanOrEqualTo((PropertyIsGreaterThanOrEqualToType) obj));
        } else if (obj instanceof PropertyIsNotEqualToType) {
            this.comparisonOps = (factory.createPropertyIsNotEqualTo((PropertyIsNotEqualToType) obj));
        } else if (obj instanceof PropertyIsGreaterThanType) {
            this.comparisonOps = (factory.createPropertyIsGreaterThan((PropertyIsGreaterThanType) obj));
        } else if (obj instanceof PropertyIsEqualToType) {
            this.comparisonOps = (factory.createPropertyIsEqualTo((PropertyIsEqualToType) obj));
        } else if (obj instanceof OrType) {
            this.logicOps = (factory.createOr((OrType) obj));
        } else if (obj instanceof NotType) {
            this.logicOps = (factory.createNot((NotType) obj));
        } else if (obj instanceof AndType) {
            this.logicOps = (factory.createAnd((AndType) obj));
        } else if (obj instanceof PropertyIsNullType) {
            this.comparisonOps =  (factory.createPropertyIsNull((PropertyIsNullType) obj));
        } else if (obj instanceof PropertyIsBetweenType) {
            this.comparisonOps = (factory.createPropertyIsBetween((PropertyIsBetweenType) obj));
        } else if (obj instanceof PropertyIsLikeType) {
            this.comparisonOps = (factory.createPropertyIsLike((PropertyIsLikeType) obj));
        } else if (obj instanceof ComparisonOpsType) {
            this.comparisonOps = (factory.createComparisonOps((ComparisonOpsType) obj));
        } else if (obj instanceof SpatialOpsType) {
            this.spatialOps = (factory.createSpatialOps((SpatialOpsType) obj));
        } else if (obj instanceof LogicOpsType) {
            this.logicOps = (factory.createLogicOps((LogicOpsType) obj));
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
     * Gets the value of the comparisonOps property.
     */
    public JAXBElement<? extends ComparisonOpsType> getComparisonOps() {
        return comparisonOps;
    }

    /**
     * Gets the value of the logicOps property.
     */
    public JAXBElement<? extends LogicOpsType> getLogicOps() {
        return logicOps;
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
        StringBuilder s = new StringBuilder("class:").append(this.getClass().getSimpleName()).append('\n');
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

}
