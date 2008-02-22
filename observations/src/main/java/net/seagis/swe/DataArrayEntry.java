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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for DataArrayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataArrayType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/swe/1.0.1}AbstractDataArrayType">
 *       &lt;sequence>
 *         &lt;element name="elementType" type="{http://www.opengis.net/swe/1.0.1}DataComponentPropertyType"/>
 *         &lt;group ref="{http://www.opengis.net/swe/1.0.1}EncodedValuesGroup" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataArrayType", propOrder = {
    "elementType",
    "encoding",
    "values"
})
public class DataArrayEntry extends AbstractDataArrayEntry {

    @XmlTransient
    private String id;
    
    @XmlElement(required = true)
    private DataComponentPropertyType elementType;
    private AbstractEncodingPropertyType encoding;
    private String values;

    /**
     * An empty constructor used by JAXB.
     */
    DataArrayEntry() {
        
    }
    
    /**
     * Build a new data array.
     */
    public DataArrayEntry(String id, int count, AbstractDataRecordEntry elementType,
            AbstractEncodingPropertyType encoding, String values) {
        super(null, count);
        this.id          = id;
        this.elementType = new DataComponentPropertyType(elementType, name);
        this.encoding    = encoding;
        this.values      = values;
        
    }
    
    @Override
    public String getId() {
        return this.id;
    }
    
    /**
     * Gets the value of the elementType property.
     */
    public AbstractDataComponentEntry getElementType() {
        if (elementType != null) {
            return elementType.getAbstractRecord();
        }
        return null;
    }
    
    public DataComponentPropertyType getPropertyElementType(){
        return elementType;
    }

    /**
     * Gets the value of the encoding property.
     */
    public AbstractEncodingEntry getEncoding() {
        if (encoding != null) {
            return encoding.getencoding();
        }
        return null;
    }
    
    public AbstractEncodingPropertyType getPropertyEncoding(){
        return encoding;
    }
    
    public void setPropertyEncoding(AbstractEncodingPropertyType encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the value of the values property.
     */
    public String getValues() {
        return values;
    }
    
    /**
     * Sets the value of the values property.
     */
    public void setValues(String values) {
        this.values = values;
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
            final DataArrayEntry that = (DataArrayEntry) object;
            return Utilities.equals(this.elementType,   that.elementType)   &&
                   Utilities.equals(this.encoding,    that.encoding)    &&
                   Utilities.equals(this.id,           that.id)           &&
                   Utilities.equals(this.values,       that.values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 29 * hash + (this.elementType != null ? this.elementType.hashCode() : 0);
        hash = 29 * hash + (this.encoding != null ? this.encoding.hashCode() : 0);
        hash = 29 * hash + (this.values != null ? this.values.hashCode() : 0);
        return hash;
    }
    
    /**
     * Return a string representing the dataArray.
     */
    @Override
    public String toString() {
        StringBuilder s    = new StringBuilder(super.toString());
        char lineSeparator = '\n';
        s.append("id=").append(id).append(lineSeparator);
        if (elementType != null)
            s.append(" elementType=").append(elementType.toString()).append(lineSeparator);
        if (encoding != null)
            s.append(" encoding:").append(encoding.toString()).append(lineSeparator);
        if (values != null)
            s.append("values=").append(values).append(lineSeparator);
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataArrayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataArrayType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/swe/1.0.1}AbstractDataArrayType">
 *       &lt;sequence>
 *         &lt;element name="elementType" type="{http://www.opengis.net/swe/1.0.1}DataComponentPropertyType"/>
 *         &lt;group ref="{http://www.opengis.net/swe/1.0.1}EncodedValuesGroup" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataArrayType", propOrder = {
    "elementType",
    "encoding",
    "values"
})
public class DataArrayEntry extends AbstractDataArrayEntry {

    @XmlTransient
    private String id;
    
    @XmlElement(required = true)
    private DataComponentPropertyType elementType;
    private AbstractEncodingPropertyType encoding;
    private String values;

    /**
     * An empty constructor used by JAXB.
     */
    DataArrayEntry() {
        
    }
    
    /**
     * Build a new data array.
     */
    public DataArrayEntry(String id, int count, AbstractDataRecordEntry elementType,
            AbstractEncodingPropertyType encoding, String values) {
        super(null, count);
        this.id          = id;
        this.elementType = new DataComponentPropertyType(elementType, name);
        this.encoding    = encoding;
        this.values      = values;
        
    }
    
    @Override
    public String getId() {
        return this.id;
    }
    /**
     * Gets the value of the elementType property.
     */
    public AbstractDataComponentEntry getElementType() {
        if (elementType != null) {
            return elementType.getAbstractRecord();
        }
        return null;
    }

    /**
     * Gets the value of the encoding property.
     */
    public AbstractEncodingEntry getEncoding() {
        if (encoding != null) {
            return encoding.getencoding();
        }
        return null;
    }

    /**
     * Gets the value of the values property.
     */
    public String getValues() {
        return values;
    }
    
    /**
     * Sets the value of the values property.
     */
    public void setValues(String values) {
        this.values = values;
    }
}
>>>>>>> .r431
