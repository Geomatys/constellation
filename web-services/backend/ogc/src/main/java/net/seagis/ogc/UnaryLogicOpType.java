/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;


/**
 * <p>Java class for UnaryLogicOpType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UnaryLogicOpType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}LogicOpsType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/ogc}comparisonOps"/>
 *           &lt;element ref="{http://www.opengis.net/ogc}spatialOps"/>
 *           &lt;element ref="{http://www.opengis.net/ogc}logicOps"/>
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
@XmlType(name = "UnaryLogicOpType", propOrder = {
    "comparisonOps",
    "spatialOps",
    "logicOps"
})
public class UnaryLogicOpType extends LogicOpsType {

    @XmlElementRef(name = "comparisonOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    private JAXBElement<? extends ComparisonOpsType> comparisonOps;
    @XmlElementRef(name = "spatialOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    private JAXBElement<? extends SpatialOpsType> spatialOps;
    @XmlElementRef(name = "logicOps", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    private JAXBElement<? extends LogicOpsType> logicOps;

    /**
     * an transient ogc factory to build JAXBelement
     */
    @XmlTransient
    private ObjectFactory factory = new ObjectFactory();
    
    /**
     * An empty constructor used by JAXB
     */
     UnaryLogicOpType() {
         
     }
     
     /**
      * Build a new Binary logic operator 
      */
     public UnaryLogicOpType(Object obj) {
        if (obj instanceof PropertyIsLessThanOrEqualToType) {
                 this.comparisonOps = factory.createPropertyIsLessThanOrEqualTo((PropertyIsLessThanOrEqualToType)obj);
             } else if (obj instanceof PropertyIsLessThanType) {
                 this.comparisonOps = factory.createPropertyIsLessThan((PropertyIsLessThanType)obj);
             } else if (obj instanceof PropertyIsGreaterThanOrEqualToType) {
                 this.comparisonOps = factory.createPropertyIsGreaterThanOrEqualTo((PropertyIsGreaterThanOrEqualToType)obj);
             } else if (obj instanceof PropertyIsNotEqualToType) {
                 this.comparisonOps = factory.createPropertyIsNotEqualTo((PropertyIsNotEqualToType)obj);
             } else if (obj instanceof PropertyIsGreaterThanType) {
                 this.comparisonOps = factory.createPropertyIsGreaterThan((PropertyIsGreaterThanType)obj);
             } else if (obj instanceof PropertyIsEqualToType) {
                this.comparisonOps = factory.createPropertyIsEqualTo((PropertyIsEqualToType)obj);
             } else if (obj instanceof OrType) {
                 this.logicOps = factory.createOr((OrType)obj);
             } else if (obj instanceof NotType) {
                 this.logicOps = factory.createNot((NotType)obj);
             } else if (obj instanceof AndType) {
                 this.logicOps = factory.createAnd((AndType)obj);
             } else if (obj instanceof PropertyIsNullType) {
                 this.comparisonOps = factory.createPropertyIsNull((PropertyIsNullType)obj);
             } else if (obj instanceof PropertyIsBetweenType) {
                 this.comparisonOps = factory.createPropertyIsBetween((PropertyIsBetweenType)obj);
             } else if (obj instanceof PropertyIsLikeType) {
                 this.comparisonOps = factory.createPropertyIsLike((PropertyIsLikeType)obj);
             } else if (obj instanceof ComparisonOpsType) {
                this.comparisonOps = factory.createComparisonOps((ComparisonOpsType)obj);
             } else if (obj instanceof SpatialOpsType) {
                 this.spatialOps = factory.createSpatialOps((SpatialOpsType)obj);
             } else if (obj instanceof LogicOpsType) {
                 this.logicOps = factory.createLogicOps((LogicOpsType)obj);
             } else {
                 throw new IllegalArgumentException("This kind of object is not allowed:" + obj.getClass().getSimpleName());
             }
         
     }
    /**
     * Gets the value of the comparisonOps property.
     */
    public JAXBElement<? extends ComparisonOpsType> getComparisonOps() {
        return comparisonOps;
    }


    /**
     * Gets the value of the spatialOps property.
     */
    public JAXBElement<? extends SpatialOpsType> getSpatialOps() {
        return spatialOps;
    }

    
    /**
     * Gets the value of the logicOps property.
     */
    public JAXBElement<? extends LogicOpsType> getLogicOps() {
        return logicOps;
    }
    
    /**
     * implements geoAPI interface
     * 
     * @return 
     */
    public Filter getFilter() {
        if (comparisonOps != null)
            return comparisonOps.getValue();
        else if (logicOps != null)
            return logicOps.getValue();
        else if (spatialOps != null)
            return spatialOps.getValue();
        else return null;
        
    }
    
     @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString()).append('\n');
        if (spatialOps != null) {
            s.append("SpatialOps: ").append(spatialOps.getValue().toString()).append('\n');
        }
        if (comparisonOps != null) {
            s.append("ComparisonOps: ").append(comparisonOps.getValue().toString()).append('\n');
        }
        if (logicOps != null) {
            s.append("LogicOps: ").append(logicOps.getValue().toString()).append('\n');
        }
        return s.toString();
    }

    public boolean evaluate(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object accept(FilterVisitor visitor, Object extraData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
