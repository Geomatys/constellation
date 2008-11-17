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


package org.constellation.skos;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Concept", 
namespace = "http://www.w3.org/2004/02/skos/core#",
propOrder = {
    "externalID",
    "prefLabel",
    "altLabel",
    "definition",
    "date"
})
public class Concept {
    
    @XmlAttribute(namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private String about;
    
    @XmlElement(namespace = "http://www.w3.org/2004/02/skos/core#")
    private String externalID;
    
    @XmlElement(namespace = "http://www.w3.org/2004/02/skos/core#")
    private String prefLabel;
    
    @XmlElement(namespace = "http://www.w3.org/2004/02/skos/core#")
    private String altLabel;

    @XmlElement(namespace = "http://www.w3.org/2004/02/skos/core#")
    private String definition;
    
    @XmlElement(namespace="http://purl.org/dc/elements/1.1/")
    private Date date;
    
    public Concept() {
        
    }

    public Concept(String about, String externalID, String prefLabel, String altLabel, String definition, Date date) {
        this.about      = about;
        this.altLabel   = altLabel;
        this.date       = date;
        this.definition = definition;
        this.externalID = externalID;
        this.prefLabel  = prefLabel;
    }
    
    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(String altLabel) {
        this.altLabel = altLabel;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[Concept]:").append('\n');
        if (about != null)
            sb.append("about:").append(about).append('\n');
        if (externalID != null)
            sb.append("externalID:").append(externalID).append('\n');
        if (prefLabel != null)
            sb.append("prefLabel:").append(prefLabel).append('\n');
        if (altLabel != null)
            sb.append("altLabel:").append(altLabel).append('\n');
        if (definition != null)
            sb.append("definition:").append(definition).append('\n');
        if (date != null)
            sb.append("date:").append(date).append('\n');
        
        return sb.toString();
    }
}
