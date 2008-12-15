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

package org.constellation.sml.v100;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.constellation.swe.v101.DataBlockDefinitionEntry;
import org.constellation.swe.v101.DataStreamDefinitionType;
import org.w3c.dom.Element;


/**
 * <p>Java class for DataSourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataSourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/sensorML/1.0}AbstractProcessType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element name="dataDefinition">
 *               &lt;complexType>
 *                 &lt;complexContent>
 *                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                     &lt;choice minOccurs="0">
 *                       &lt;element ref="{http://www.opengis.net/swe/1.0}DataBlockDefinition"/>
 *                       &lt;element ref="{http://www.opengis.net/swe/1.0}DataStreamDefinition"/>
 *                     &lt;/choice>
 *                     &lt;attGroup ref="{http://www.opengis.net/gml/3.2}AssociationAttributeGroup"/>
 *                   &lt;/restriction>
 *                 &lt;/complexContent>
 *               &lt;/complexType>
 *             &lt;/element>
 *             &lt;element name="values">
 *               &lt;complexType>
 *                 &lt;complexContent>
 *                   &lt;extension base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;/extension>
 *                 &lt;/complexContent>
 *               &lt;/complexType>
 *             &lt;/element>
 *           &lt;/sequence>
 *           &lt;element name="observationReference">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;attGroup ref="{http://www.opengis.net/gml/3.2}AssociationAttributeGroup"/>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
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
@XmlType(name = "DataSourceType", propOrder = {
    "dataDefinition",
    "values",
    "observationReference"
})
public class DataSourceType extends AbstractProcessType {

    private DataSourceType.DataDefinition dataDefinition;
    private DataSourceType.Values values;
    private DataSourceType.ObservationReference observationReference;

    /**
     * Gets the value of the dataDefinition property.
     */
    public DataSourceType.DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    /**
     * Sets the value of the dataDefinition property.
     */
    public void setDataDefinition(DataSourceType.DataDefinition value) {
        this.dataDefinition = value;
    }

    /**
     * Gets the value of the values property.
     */
    public DataSourceType.Values getValues() {
        return values;
    }

    /**
     * Sets the value of the values property.
     */
    public void setValues(DataSourceType.Values value) {
        this.values = value;
    }

    /**
     * Gets the value of the observationReference property.
     */
    public DataSourceType.ObservationReference getObservationReference() {
        return observationReference;
    }

    /**
     * Sets the value of the observationReference property.
     */
    public void setObservationReference(DataSourceType.ObservationReference value) {
        this.observationReference = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice minOccurs="0">
     *         &lt;element ref="{http://www.opengis.net/swe/1.0}DataBlockDefinition"/>
     *         &lt;element ref="{http://www.opengis.net/swe/1.0}DataStreamDefinition"/>
     *       &lt;/choice>
     *       &lt;attGroup ref="{http://www.opengis.net/gml/3.2}AssociationAttributeGroup"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "dataBlockDefinition",
        "dataStreamDefinition"
    })
    public static class DataDefinition {

        @XmlElement(name = "DataBlockDefinition", namespace = "http://www.opengis.net/swe/1.0")
        private DataBlockDefinitionEntry dataBlockDefinition;
        @XmlElement(name = "DataStreamDefinition", namespace = "http://www.opengis.net/swe/1.0")
        private DataStreamDefinitionType dataStreamDefinition;
        @XmlAttribute
        private List<String> nilReason;
        @XmlAttribute(namespace = "http://www.opengis.net/gml/3.2")
        private String remoteSchema;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String actuate;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String arcrole;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String href;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String role;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String show;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String title;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String type;

        /**
         * Gets the value of the dataBlockDefinition property.
         * 
         */
        public DataBlockDefinitionEntry getDataBlockDefinition() {
            return dataBlockDefinition;
        }

        /**
         * Sets the value of the dataBlockDefinition property.
         */
        public void setDataBlockDefinition(DataBlockDefinitionEntry value) {
            this.dataBlockDefinition = value;
        }

        /**
         * Gets the value of the dataStreamDefinition property.
         */
        public DataStreamDefinitionType getDataStreamDefinition() {
            return dataStreamDefinition;
        }

        /**
         * Sets the value of the dataStreamDefinition property.
         */
        public void setDataStreamDefinition(DataStreamDefinitionType value) {
            this.dataStreamDefinition = value;
        }

        /**
         * Gets the value of the nilReason property.
         */
        public List<String> getNilReason() {
            if (nilReason == null) {
                nilReason = new ArrayList<String>();
            }
            return this.nilReason;
        }

        /**
         * Gets the value of the remoteSchema property.
         */
        public String getRemoteSchema() {
            return remoteSchema;
        }

        /**
         * Sets the value of the remoteSchema property.
         */
        public void setRemoteSchema(String value) {
            this.remoteSchema = value;
        }

        /**
         * Gets the value of the actuate property.
         */
        public String getActuate() {
            return actuate;
        }

        /**
         * Sets the value of the actuate property.
         */
        public void setActuate(String value) {
            this.actuate = value;
        }

        /**
         * Gets the value of the arcrole property.
         */
        public String getArcrole() {
            return arcrole;
        }

        /**
         * Sets the value of the arcrole property.
         */
        public void setArcrole(String value) {
            this.arcrole = value;
        }

        /**
         * Gets the value of the href property.
         */
        public String getHref() {
            return href;
        }

        /**
         * Sets the value of the href property.
         */
        public void setHref(String value) {
            this.href = value;
        }

        /**
         * Gets the value of the role property.
         */
        public String getRole() {
            return role;
        }

        /**
         * Sets the value of the role property.
         */
        public void setRole(String value) {
            this.role = value;
        }

        /**
         * Gets the value of the show property.
         */
        public String getShow() {
            return show;
        }

        /**
         * Sets the value of the show property.
         */
        public void setShow(String value) {
            this.show = value;
        }

        /**
         * Gets the value of the title property.
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets the value of the title property.
         */
        public void setTitle(String value) {
            this.title = value;
        }

        /**
         * Gets the value of the type property.
         */
        public String getType() {
            if (type == null) {
                return "simple";
            } else {
                return type;
            }
        }

        /**
         * Sets the value of the type property.
         */
        public void setType(String value) {
            this.type = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attGroup ref="{http://www.opengis.net/gml/3.2}AssociationAttributeGroup"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ObservationReference {

        @XmlAttribute
        private List<String> nilReason;
        @XmlAttribute(namespace = "http://www.opengis.net/gml/3.2")
        private String remoteSchema;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String actuate;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String arcrole;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String href;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String role;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String show;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String title;
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String type;

        /**
         * Gets the value of the nilReason property.
         */
        public List<String> getNilReason() {
            if (nilReason == null) {
                nilReason = new ArrayList<String>();
            }
            return this.nilReason;
        }

        /**
         * Gets the value of the remoteSchema property.
         */
        public String getRemoteSchema() {
            return remoteSchema;
        }

        /**
         * Sets the value of the remoteSchema property.
         */
        public void setRemoteSchema(String value) {
            this.remoteSchema = value;
        }

        /**
         * Gets the value of the actuate property.
         */
        public String getActuate() {
            return actuate;
        }

        /**
         * Sets the value of the actuate property.
         */
        public void setActuate(String value) {
            this.actuate = value;
        }

        /**
         * Gets the value of the arcrole property.
         */
        public String getArcrole() {
            return arcrole;
        }

        /**
         * Sets the value of the arcrole property.
        */
        public void setArcrole(String value) {
            this.arcrole = value;
        }

        /**
         * Gets the value of the href property.
         */
        public String getHref() {
            return href;
        }

        /**
         * Sets the value of the href property.
         */
        public void setHref(String value) {
            this.href = value;
        }

        /**
         * Gets the value of the role property.
         * 
         */
        public String getRole() {
            return role;
        }

        /**
         * Sets the value of the role property.
         */
        public void setRole(String value) {
            this.role = value;
        }

        /**
         * Gets the value of the show property.
         */
        public String getShow() {
            return show;
        }

        /**
         * Sets the value of the show property.
         */
        public void setShow(String value) {
            this.show = value;
        }

        /**
         * Gets the value of the title property.
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets the value of the title property.
         */
        public void setTitle(String value) {
            this.title = value;
        }

        /**
         * Gets the value of the type property.
         */
        public String getType() {
            if (type == null) {
                return "simple";
            } else {
                return type;
            }
        }

        /**
         * Sets the value of the type property.
         */
        public void setType(String value) {
            this.type = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.w3.org/2001/XMLSchema}anyType">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "any"
    })
    public static class Values {

        @XmlAnyElement
        private List<Element> any;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the any property.
         * 
         */
        public List<Element> getAny() {
            if (any == null) {
                any = new ArrayList<Element>();
            }
            return this.any;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed property on this class.
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }

}
