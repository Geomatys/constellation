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
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotools.util.Utilities;


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
 *         &lt;element name="KeywordList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="keyword" type="{http://www.w3.org/2001/XMLSchema}token" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="codeSpace" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
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
    "keywordList"
})
@XmlRootElement(name = "keywords")
public class Keywords {

    @XmlElement(name = "KeywordList")
    private Keywords.KeywordList keywordList;
    @XmlAttribute
    private List<String> nilReason;
    @XmlAttribute(namespace = "http://www.opengis.net/gml")
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

    public Keywords() {

    }

    /**
     *
     */
    public Keywords(Keywords.KeywordList keywordList) {
        this.keywordList = keywordList;
    }

    /**
     * Gets the value of the keywordList property.
     */
    public Keywords.KeywordList getKeywordList() {
        return keywordList;
    }

    /**
     * Sets the value of the keywordList property.
     */
    public void setKeywordList(Keywords.KeywordList value) {
        this.keywordList = value;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[Keywords]").append("\n");
        if (keywordList != null) {
            sb.append("keywordsList: ").append(keywordList).append('\n');
        }

        if (nilReason != null) {
            sb.append("nilReason:").append('\n');
            for (String k : nilReason) {
                sb.append("nilReason: ").append(k).append('\n');
            }
        }
        if (remoteSchema != null) {
            sb.append("remoteSchema: ").append(remoteSchema).append('\n');
        }
        if (actuate != null) {
            sb.append("actuate: ").append(actuate).append('\n');
        }
        if (arcrole != null) {
            sb.append("actuate: ").append(arcrole).append('\n');
        }
        if (href != null) {
            sb.append("href: ").append(href).append('\n');
        }
        if (role != null) {
            sb.append("role: ").append(role).append('\n');
        }
        if (show != null) {
            sb.append("show: ").append(show).append('\n');
        }
        if (title != null) {
            sb.append("title: ").append(title).append('\n');
        }
        if (type != null) {
            sb.append("type: ").append(type).append('\n');
        }
        return sb.toString();
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof Keywords) {
            final Keywords that = (Keywords) object;
            return Utilities.equals(this.actuate,      that.actuate)      &&
                   Utilities.equals(this.arcrole,      that.arcrole)      &&
                   Utilities.equals(this.href,         that.href)         &&
                   Utilities.equals(this.keywordList,  that.keywordList)  &&
                   Utilities.equals(this.nilReason,    that.nilReason)    &&
                   Utilities.equals(this.remoteSchema, that.remoteSchema) &&
                   Utilities.equals(this.role,         that.role)         &&
                   Utilities.equals(this.show,         that.show)         &&
                   Utilities.equals(this.title,        that.title)        &&
                   Utilities.equals(this.type,         that.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.keywordList != null ? this.keywordList.hashCode() : 0);
        hash = 61 * hash + (this.nilReason != null ? this.nilReason.hashCode() : 0);
        hash = 61 * hash + (this.remoteSchema != null ? this.remoteSchema.hashCode() : 0);
        hash = 61 * hash + (this.actuate != null ? this.actuate.hashCode() : 0);
        hash = 61 * hash + (this.arcrole != null ? this.arcrole.hashCode() : 0);
        hash = 61 * hash + (this.href != null ? this.href.hashCode() : 0);
        hash = 61 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 61 * hash + (this.show != null ? this.show.hashCode() : 0);
        hash = 61 * hash + (this.title != null ? this.title.hashCode() : 0);
        return hash;
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
     *         &lt;element name="keyword" type="{http://www.w3.org/2001/XMLSchema}token" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *       &lt;attribute name="codeSpace" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
     *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "keyword"
    })
    public static class KeywordList {

        @XmlElementRef(name = "keyword", namespace = "http://www.opengis.net/sensorML/1.0", type = JAXBElement.class)
        private List<JAXBElement<String>> keyword;
        @XmlAttribute
        private String codeSpace;
        @XmlAttribute
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        private String id;

        public KeywordList() {

        }

        public KeywordList(String codeSpace, List<JAXBElement<String>> keyword) {
            this.codeSpace = codeSpace;
            this.keyword   = keyword;
        }

        /**
         * Gets the value of the keyword property.
         */
        public List<JAXBElement<String>> getKeyword() {
            if (keyword == null) {
                keyword = new ArrayList<JAXBElement<String>>();
            }
            return this.keyword;
        }

        /**
         * Gets the value of the codeSpace property.
         */
        public String getCodeSpace() {
            return codeSpace;
        }

        /**
         * Sets the value of the codeSpace property.
         */
        public void setCodeSpace(String value) {
            this.codeSpace = value;
        }

        /**
         * Gets the value of the id property.
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         */
        public void setId(String value) {
            this.id = value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[KeywordsList]").append("\n");
            if (keyword != null) {
                for (JAXBElement<String> k: keyword) {
                    sb.append("keyword:").append(k.getValue()).append('\n');
                }
            }
            if (codeSpace != null) {
                sb.append("codeSpace: ").append(codeSpace).append('\n');
            }
            if (id != null) {
                sb.append("id: ").append(id).append('\n');
            }
            return sb.toString();
        }

        /**
         * Verify if this entry is identical to specified object.
         */
        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }

            if (object instanceof KeywordList) {
                final KeywordList that = (KeywordList) object;
                boolean kw = false;
                if (this.getKeyword().size() == that.getKeyword().size()) {
                    kw = true;
                    for (int i = 0; i < this.getKeyword().size(); i++) {
                        JAXBElement<String> jb1 = this.getKeyword().get(i);
                        JAXBElement<String> jb2 = that.getKeyword().get(i);
                        if (!Utilities.equals(jb1.getValue(), jb2.getValue())) {
                            kw = false;
                        }
                    }
                }
                return Utilities.equals(this.codeSpace, that.codeSpace) &&
                       Utilities.equals(this.id,        that.id)        &&
                       kw;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + (this.keyword != null ? this.keyword.hashCode() : 0);
            hash = 43 * hash + (this.codeSpace != null ? this.codeSpace.hashCode() : 0);
            hash = 43 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }


    }

}
