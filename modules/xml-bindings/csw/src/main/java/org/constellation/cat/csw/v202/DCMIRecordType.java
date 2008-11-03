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
package org.constellation.cat.csw.v202;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.constellation.cat.csw.DCMIRecord;
import org.constellation.dublincore.v2.elements.SimpleLiteral;
import org.geotools.util.Utilities;


/**
 * 
 * This type encapsulates all of the standard DCMI metadata terms,
 * including the Dublin Core refinements; these terms may be mapped
 * to the profile-specific information model.
 *          
 * 
 * <p>Java class for DCMIRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DCMIRecordType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}AbstractRecordType">
 *       &lt;sequence>
 *         &lt;group ref="{http://purl.org/dc/terms/}DCMI-terms"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DCMIRecordType", propOrder = {
    "identifier"  ,
    "title"       ,
    "type"        ,
    "subject"     ,
    "format"      ,
    "language"    ,
    "distributor" ,
    "creator"     ,
    "modified"    ,
    "date"        ,    
    "_abstract"   ,
    "references"  ,
    "spatial"     ,
    "dcElement"   
})
@XmlSeeAlso({
    RecordType.class
})
@XmlRootElement(name="DCMIRecord") 
public class DCMIRecordType extends AbstractRecordType implements DCMIRecord {

    @XmlElement(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral identifier;
    
    @XmlElement(name = "title", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral title;
    
    @XmlElement(name = "type", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral type;
    
    @XmlElement(name = "subject", namespace = "http://purl.org/dc/elements/1.1/")
    private List<SimpleLiteral> subject;
    
    @XmlElement(name = "format", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral format;
    
    @XmlElement(name = "language", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral language;
    
    @XmlElement(name = "publisher", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral distributor;
    
    @XmlElement(name = "creator", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral creator;
    
    @XmlElementRef(name = "DC-element", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> dcElement;

    @XmlElement(name = "modified", namespace = "http://purl.org/dc/terms/")
    private SimpleLiteral modified;
    
    @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral date;
    
    @XmlElement(name = "abstract", namespace = "http://purl.org/dc/terms/")
    private SimpleLiteral _abstract;
    
    @XmlElement(name = "spatial", namespace = "http://purl.org/dc/terms/")
    private SimpleLiteral spatial;
    
    @XmlElement(name = "references", namespace = "http://purl.org/dc/terms/")
    private SimpleLiteral references;
    
    /**
     * An empty constructor used by JAXB
     */
    DCMIRecordType() {
        this.dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
    }
        
    
    public DCMIRecordType(SimpleLiteral identifier, SimpleLiteral title, SimpleLiteral type, 
            List<SimpleLiteral> subjects, SimpleLiteral format, SimpleLiteral modified, SimpleLiteral date, SimpleLiteral _abstract,
            SimpleLiteral creator, SimpleLiteral distributor, SimpleLiteral language, SimpleLiteral spatial, 
            SimpleLiteral references) {
        
        this.identifier = identifier;
        this.title      = title;
        this.type       = type;
        this.format     = format;
        this.date       = date;
        
        this.dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
        
        this.subject     = subjects;
        this.creator     = creator;
        this.distributor = distributor;
        this.language    = language;
        this.modified    = modified;
        this._abstract   = _abstract;
        this.spatial     = spatial;
        this.references  = references;
        
    }
    
    /**
     * Gets the value of the dcElement property.
     * (unModifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getDCElement() {
        if (dcElement == null) {
            dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(dcElement);
    }
    
    public void setIdentifier(SimpleLiteral identifier) {
        this.identifier = identifier;
    }
    
    public SimpleLiteral getIdentifier() {
        return identifier;
    }
    
    public void setTitle(SimpleLiteral title) {
        this.title = title;
    }
    
    public SimpleLiteral getTitle() {
        return title;
    }
    
    public void setType(SimpleLiteral type) {
        this.type = type;
    }
    
    public SimpleLiteral getType() {
        return type;
    }
    
    public void setSubject(List<SimpleLiteral> subjects) {
        this.subject = subjects;
    }
    
    public void setSubject(SimpleLiteral subject) {
        if (this.subject == null) {
            this.subject = new ArrayList<SimpleLiteral>();
        }
        this.subject.add(subject);
    }
    
    public List<SimpleLiteral> getSubject() {
        if (subject == null) {
            subject = new ArrayList<SimpleLiteral>();
        }
        return subject;
    }
    
    public void setFormat(SimpleLiteral format) {
        this.format = format;
    }
    
    public SimpleLiteral getFormat() {
        return format;
    }
    
    public void setModified(SimpleLiteral modified) {
        this.modified = modified;
    }
    
    public SimpleLiteral getModified() {
        return modified;
    }
    
    public void setDate(SimpleLiteral date) {
        this.date = date;
    }
    
    public SimpleLiteral getDate() {
        return date;
    }
    
    public void setAbstract(SimpleLiteral _abstract) {
        this._abstract =_abstract;
    }
    
    public SimpleLiteral getAbstract() {
        return _abstract;
    }
    
    public void setCreator(SimpleLiteral creator) {
        this.creator = creator;
    }
    
    public SimpleLiteral getCreator() {
        return creator;
    }
    
    public void setDistributor(SimpleLiteral distributor) {
        this.distributor = distributor;
    }
    
    public SimpleLiteral getDistributor() {
        return distributor;
    }
    
    public void setLanguage(SimpleLiteral language) {
        this.language = language;
    }
    
    public SimpleLiteral getLanguage() {
        return language;
    }
    
    public void setRelation(SimpleLiteral relation) {
        this.dcElement.add(dublinFactory.createRelation(relation));
    }
    
    public SimpleLiteral getRelation() {
        return getAttributeFromDCelement("relation"); 
    }
    
    public void setSource(SimpleLiteral source) {
        this.dcElement.add(dublinFactory.createSource(source));
    }
    
    public SimpleLiteral getSource() {
       return getAttributeFromDCelement("source"); 
    }
    
    public void setCoverage(SimpleLiteral coverage) {
        this.dcElement.add(dublinFactory.createCoverage(coverage));
    }
    
    public SimpleLiteral getCoverage() {
        return getAttributeFromDCelement("coverage"); 
    }
    
    public void setRights(SimpleLiteral rights) {
        this.dcElement.add(dublinFactory.createRights(rights));
    }
    
    public SimpleLiteral getRights() {
        return getAttributeFromDCelement("rights"); 
    }
    
    public void setSpatial(SimpleLiteral spatial) {
        this.spatial = spatial;//dublinTermFactory.createSpatial(spatial);
    }
    
    public SimpleLiteral getSpatial() {
         return spatial;
    }
    
    public void setReferences(SimpleLiteral references) {
        this.references = references;
    }
    
    public SimpleLiteral getReferences() {
        return references;
    }
    
    public void setPublisher(SimpleLiteral publisher) {
        this.dcElement.add(dublinFactory.createPublisher(publisher));
    }
    
    public SimpleLiteral getPublisher() {
        return getAttributeFromDCelement("publisher"); 
    }
    
    public void setContributor(SimpleLiteral contributor) {
        this.dcElement.add(dublinFactory.createContributor(contributor));
    }
    
    public SimpleLiteral getContributor() {
        return getAttributeFromDCelement("contributor"); 
    }
    
    public void setDescription(SimpleLiteral description) {
        this.dcElement.add(dublinFactory.createDescription(description));
    }
    
    public SimpleLiteral getDescription() {
        return getAttributeFromDCelement("description"); 
    }
    
    /**
     * if the attribute have not been fill by JAXB we search in DCelement
     */
    public SimpleLiteral getAttributeFromDCelement(String name) {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals(name)) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (identifier != null) {
            s.append("identifier: ").append(identifier).append('\n');
        }
        if (title != null) {
            s.append("title: ").append(title).append('\n');
        }
        if (type != null) {
            s.append("type: ").append(type).append('\n');
        }
        if (format != null) {
            s.append("format: ").append(format).append('\n');
        }
        if (subject != null) {
            s.append("subjects: ").append('\n');
            for (SimpleLiteral sl: subject) {
                s.append(sl).append('\n');
            }
        }
        if (dcElement != null) {
            for (JAXBElement<SimpleLiteral> jb: dcElement) {
                s.append("name=").append(jb.getName()).append(" value=").append(jb.getValue().toString()).append('\n');
            }
        }
        if (language != null) {
            s.append("language: ").append(language).append('\n');
        }
        if (modified != null) {
            s.append("modified: ").append(modified).append('\n');
        }
        if (_abstract != null) {
            s.append("abstract: ").append(_abstract).append('\n');
        }
        if (spatial != null) {
            s.append("spatial: ").append(spatial).append('\n');
        }
        if (references != null) {
            s.append("references: ").append(references).append('\n');
        }
        return s.toString();
    }
    
     /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof DCMIRecordType) {
            final DCMIRecordType that = (DCMIRecordType) object;

            boolean dcelement = this.dcElement.size() == that.dcElement.size();
        
            //we verify that the two list contains the same object
            List<SimpleLiteral> obj = new ArrayList<SimpleLiteral>();
            for (JAXBElement<SimpleLiteral> jb: dcElement) {
                obj.add(jb.getValue());
            }
        
            for (JAXBElement<SimpleLiteral> jb: that.dcElement) {
                if (!obj.contains(jb.getValue())) {
                    dcelement = false;
                }
            }
            return Utilities.equals(this._abstract,   that._abstract)   &&
                   Utilities.equals(this.creator  ,   that.creator)     &&
                   Utilities.equals(this.distributor, that.distributor) &&
                   Utilities.equals(this.format,      that.format)      &&
                   Utilities.equals(this.identifier,  that.identifier)  &&
                   Utilities.equals(this.language,    that.language)    &&
                   Utilities.equals(this.modified,    that.modified)    &&
                   Utilities.equals(this.references,  that.references)  &&
                   Utilities.equals(this.spatial,     that.spatial)     &&
                   Utilities.equals(this.subject,     that.subject)     &&
                   Utilities.equals(this.title,       that.title)       &&
                   Utilities.equals(this.type,        that.type)        &&
                   dcelement;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.identifier != null ? this.identifier.hashCode() : 0);
        return hash;
    }
}
