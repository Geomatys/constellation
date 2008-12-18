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

package org.constellation.sml.v101;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="ComponentList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="component" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence minOccurs="0">
 *                             &lt;element ref="{http://www.opengis.net/sensorML/1.0.1}AbstractProcess"/>
 *                           &lt;/sequence>
 *                           &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}token" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "componentList"
})
@XmlRootElement(name = "components")
public class Components {

    @XmlElement(name = "ComponentList")
    private Components.ComponentList componentList;
    @XmlAttribute(namespace = "http://www.opengis.net/gml")
    @XmlSchemaType(name = "anyURI")
    private String remoteSchema;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String type;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String href;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String role;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String arcrole;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String title;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String show;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String actuate;

    /**
     * Gets the value of the componentList property.
     * 
     * @return
     *     possible object is
     *     {@link Components.ComponentList }
     *     
     */
    public Components.ComponentList getComponentList() {
        return componentList;
    }

    /**
     * Sets the value of the componentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link Components.ComponentList }
     *     
     */
    public void setComponentList(Components.ComponentList value) {
        this.componentList = value;
    }

    /**
     * Gets the value of the remoteSchema property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoteSchema() {
        return remoteSchema;
    }

    /**
     * Sets the value of the remoteSchema property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoteSchema(String value) {
        this.remoteSchema = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
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
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRole(String value) {
        this.role = value;
    }

    /**
     * Gets the value of the arcrole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArcrole() {
        return arcrole;
    }

    /**
     * Sets the value of the arcrole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArcrole(String value) {
        this.arcrole = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the show property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShow() {
        return show;
    }

    /**
     * Sets the value of the show property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShow(String value) {
        this.show = value;
    }

    /**
     * Gets the value of the actuate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActuate() {
        return actuate;
    }

    /**
     * Sets the value of the actuate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActuate(String value) {
        this.actuate = value;
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
     *       &lt;sequence>
     *         &lt;element name="component" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence minOccurs="0">
     *                   &lt;element ref="{http://www.opengis.net/sensorML/1.0.1}AbstractProcess"/>
     *                 &lt;/sequence>
     *                 &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}token" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "component"
    })
    public static class ComponentList {

        @XmlElement(required = true)
        private List<Components.ComponentList.Component> component;

        /**
         * Gets the value of the component property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the component property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getComponent().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Components.ComponentList.Component }
         * 
         * 
         */
        public List<Components.ComponentList.Component> getComponent() {
            if (component == null) {
                component = new ArrayList<Components.ComponentList.Component>();
            }
            return this.component;
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
         *       &lt;sequence minOccurs="0">
         *         &lt;element ref="{http://www.opengis.net/sensorML/1.0.1}AbstractProcess"/>
         *       &lt;/sequence>
         *       &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}token" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "process"
        })
        public static class Component {

            @XmlElementRef(name = "AbstractProcess", namespace = "http://www.opengis.net/sensorML/1.0.1", type = JAXBElement.class)
            private JAXBElement<? extends AbstractProcessType> process;
            @XmlAttribute(required = true)
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "token")
            private String name;
            @XmlAttribute(namespace = "http://www.opengis.net/gml")
            @XmlSchemaType(name = "anyURI")
            private String remoteSchema;
            @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
            private String type;
            @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
            @XmlSchemaType(name = "anyURI")
            private String href;
            @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
            @XmlSchemaType(name = "anyURI")
            private String role;
            @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
            @XmlSchemaType(name = "anyURI")
            private String arcrole;
            @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
            private String title;
            @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
            private String show;
            @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
            private String actuate;

            /**
             * Gets the value of the process property.
             * 
             * @return
             *     possible object is
             *     {@link JAXBElement }{@code <}{@link DataSourceType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ProcessModelType }{@code >}
             *     {@link JAXBElement }{@code <}{@link SystemType }{@code >}
             *     {@link JAXBElement }{@code <}{@link AbstractProcessType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ProcessChainType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ComponentArrayType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ComponentType }{@code >}
             *     
             */
            public JAXBElement<? extends AbstractProcessType> getProcess() {
                return process;
            }

            /**
             * Sets the value of the process property.
             * 
             * @param value
             *     allowed object is
             *     {@link JAXBElement }{@code <}{@link DataSourceType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ProcessModelType }{@code >}
             *     {@link JAXBElement }{@code <}{@link SystemType }{@code >}
             *     {@link JAXBElement }{@code <}{@link AbstractProcessType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ProcessChainType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ComponentArrayType }{@code >}
             *     {@link JAXBElement }{@code <}{@link ComponentType }{@code >}
             *     
             */
            public void setProcess(JAXBElement<? extends AbstractProcessType> value) {
                this.process = ((JAXBElement<? extends AbstractProcessType> ) value);
            }

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the remoteSchema property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRemoteSchema() {
                return remoteSchema;
            }

            /**
             * Sets the value of the remoteSchema property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRemoteSchema(String value) {
                this.remoteSchema = value;
            }

            /**
             * Gets the value of the type property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
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
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setType(String value) {
                this.type = value;
            }

            /**
             * Gets the value of the href property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHref() {
                return href;
            }

            /**
             * Sets the value of the href property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHref(String value) {
                this.href = value;
            }

            /**
             * Gets the value of the role property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRole() {
                return role;
            }

            /**
             * Sets the value of the role property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRole(String value) {
                this.role = value;
            }

            /**
             * Gets the value of the arcrole property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getArcrole() {
                return arcrole;
            }

            /**
             * Sets the value of the arcrole property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setArcrole(String value) {
                this.arcrole = value;
            }

            /**
             * Gets the value of the title property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getTitle() {
                return title;
            }

            /**
             * Sets the value of the title property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTitle(String value) {
                this.title = value;
            }

            /**
             * Gets the value of the show property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShow() {
                return show;
            }

            /**
             * Sets the value of the show property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShow(String value) {
                this.show = value;
            }

            /**
             * Gets the value of the actuate property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getActuate() {
                return actuate;
            }

            /**
             * Sets the value of the actuate property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setActuate(String value) {
                this.actuate = value;
            }

        }

    }

}
