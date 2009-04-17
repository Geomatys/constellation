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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;

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
public class KeywordList {

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
        this.keyword = keyword;
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
            for (JAXBElement<String> k : keyword) {
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
                    Utilities.equals(this.id, that.id) &&
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
