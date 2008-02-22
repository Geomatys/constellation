<<<<<<< .mine
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


package net.seagis.swe;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * DataArray is a data-type so usually appears "by value" rather than by reference.
 * 
 * <p>Java class for DataArrayPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataArrayPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/swe/1.0.1}DataArray"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataArrayPropertyType", propOrder = {
    "dataArray"
})
public class DataArrayPropertyType {

    @XmlElementRef(name = "DataArray", namespace = "http://www.opengis.net/swe/1.0.1", type = JAXBElement.class)
    private JAXBElement<? extends DataArrayEntry> dataArray;

    @XmlTransient
    private ObjectFactory factory = new ObjectFactory();
    
    /**
     * An empty constructor used by JAXB
     */
    DataArrayPropertyType() {
        
    }
    
    /**
     * Build a new Array Property type.
     */
    public DataArrayPropertyType(DataArrayEntry dataArray) {
        this.dataArray = factory.createDataArray(dataArray);
    }
    
    /**
     * Gets the value of the dataArray property.
     */
    public DataArrayEntry getDataArray() {
        if (dataArray != null) {
            return dataArray.getValue(); 
        }
        return null;
    }
    
    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final DataArrayPropertyType that = (DataArrayPropertyType) object;
            return Utilities.equals(this.dataArray,   that.dataArray);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.dataArray != null ? this.dataArray.hashCode() : 0);
        return hash;
    }
    
    /**
     * Return a string representing the dataArray.
     */
    @Override
    public String toString() {
        StringBuilder s    = new StringBuilder();
        char lineSeparator = '\n';
        if (dataArray != null)
            s.append("dataArray").append(dataArray.toString()).append(lineSeparator);
        return s.toString();
    }

}
=======
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


package net.seagis.swe;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * DataArray is a data-type so usually appears "by value" rather than by reference.
 * 
 * <p>Java class for DataArrayPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataArrayPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/swe/1.0.1}DataArray"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataArrayPropertyType", propOrder = {
    "dataArray"
})
public class DataArrayPropertyType {

    @XmlElementRef(name = "DataArray", namespace = "http://www.opengis.net/swe/1.0.1", type = JAXBElement.class)
    private JAXBElement<DataArrayEntry> dataArray;

    @XmlTransient
    private ObjectFactory factory = new ObjectFactory();
    
    /**
     * An empty constructor used by JAXB
     */
    DataArrayPropertyType() {
        
    }
    
    /**
     * Build a new Array Property type.
     */
    public DataArrayPropertyType(DataArrayEntry dataArray) {
        this.dataArray = factory.createDataArray(dataArray);
    }
    
    /**
     * Gets the value of the dataArray property.
     */
    public DataArrayEntry getDataArray() {
        if (dataArray != null) {
            return dataArray.getValue(); 
        }
        return null;
    }
}
>>>>>>> .r431
