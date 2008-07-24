/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Institut de Recherche pour le Développement
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


package net.seagis.cat.csw.v200;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import net.seagis.dublincore.AbstractSimpleLiteral;
import net.seagis.dublincore.v1.elements.SimpleLiteral;
import org.geotools.util.Utilities;


/**
 * 
 * This type encapsulates all of the standard DCMI metadata terms, 
 * including the Dublin Core refinements; these terms may be mapped to the profile-specific information model.
 *       
 * 
 * <p>Java class for DCMIRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DCMIRecordType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw}AbstractRecordType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.purl.org/dc/terms/}DCMI-terms"/>
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
    "dcElement"   ,
    "modified"    ,
    "_abstract"   ,
    "references"  ,
    "spatial"
})
@XmlSeeAlso({
    RecordType.class
})
public class DCMIRecordType extends AbstractRecordType {

  
    
    @XmlElementRefs({
        @XmlElementRef(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "identifier", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> identifier;
    
    @XmlElementRefs({
        @XmlElementRef(name = "title", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "title", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> title;
    
    @XmlElementRefs({
        @XmlElementRef(name = "type", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "type", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> type;
    
     @XmlElementRefs({
        @XmlElementRef(name = "subject", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "subject", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private List<JAXBElement<SimpleLiteral>> subject;
    
    @XmlElementRefs({
        @XmlElementRef(name = "format", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "format", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> format;
    
    @XmlElementRefs({
        @XmlElementRef(name = "language", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "language", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> language;
    
    @XmlElementRefs({
        @XmlElementRef(name = "publisher", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "publisher", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> distributor;
    
    @XmlElementRefs({
        @XmlElementRef(name = "creator", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class),
        @XmlElementRef(name = "creator", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> creator;
    
    @XmlElementRefs({
        @XmlElementRef(name = "DC-element", namespace = "http://www.purl.org/dc/elements/1.1/", type = JAXBElement.class),
        @XmlElementRef(name = "DC-element", namespace = "http://purl.org/dc/elements/1.1/",     type = JAXBElement.class)
    })
    private List<JAXBElement<SimpleLiteral>> dcElement;

    @XmlElementRefs({
        @XmlElementRef(name = "modified", namespace = "http://purl.org/dc/terms/",     type = JAXBElement.class),
        @XmlElementRef(name = "modified", namespace = "http://www.purl.org/dc/terms/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> modified;
    
    @XmlElementRefs({
        @XmlElementRef(name = "abstract", namespace = "http://purl.org/dc/terms/",     type = JAXBElement.class),
        @XmlElementRef(name = "abstract", namespace = "http://www.purl.org/dc/terms/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> _abstract;
    
    @XmlElementRefs({
        @XmlElementRef(name = "spatial", namespace = "http://purl.org/dc/terms/",     type = JAXBElement.class),
        @XmlElementRef(name = "spatial", namespace = "http://www.purl.org/dc/terms/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> spatial;
    
    @XmlElementRefs({
        @XmlElementRef(name = "references", namespace = "http://purl.org/dc/terms/",     type = JAXBElement.class),
        @XmlElementRef(name = "references", namespace = "http://www.purl.org/dc/terms/", type = JAXBElement.class)
    })
    private JAXBElement<SimpleLiteral> references;
    


     /**
     * An empty constructor used by JAXB
     */
    DCMIRecordType() {
        this.dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
    }
        
    
    public DCMIRecordType(SimpleLiteral identifier, SimpleLiteral title, SimpleLiteral type, 
            List<SimpleLiteral> subjects, SimpleLiteral format, SimpleLiteral modified, SimpleLiteral _abstract,
            SimpleLiteral creator, SimpleLiteral distributor, SimpleLiteral language, SimpleLiteral spatial, 
            SimpleLiteral references) {
        
        this.identifier = dublinFactory.createIdentifier(identifier);
        this.title      = dublinFactory.createTitle(title);
        this.type       = dublinFactory.createType(type);
        this.format     = dublinFactory.createFormat(format);
        
        this.dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
        
        this.subject = new ArrayList<JAXBElement<SimpleLiteral>>();
        for (SimpleLiteral sub: subjects) {
            this.subject.add(dublinFactory.createSubject(sub));
        }
        
        this.creator     = dublinFactory.createCreator(creator);
        this.distributor = dublinFactory.createPublisher(distributor);
        this.language    = dublinFactory.createLanguage(language);
        this.modified    = dublinTermFactory.createModified(modified);
        this._abstract   = dublinTermFactory.createAbstract(_abstract);
        this.spatial     = dublinTermFactory.createSpatial(spatial);
        this.references  = dublinTermFactory.createReferences(references);
        
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
        this.identifier = dublinFactory.createIdentifier(identifier);
    }
    
    public AbstractSimpleLiteral getIdentifier() {
        if (identifier != null)
            return identifier.getValue();
        return null;
    }
    
    public void setTitle(SimpleLiteral title) {
        this.title = dublinFactory.createTitle(title);
    }
    
    public AbstractSimpleLiteral getTitle() {
        if (title != null)
            return title.getValue();
        return null;
    }
    
    public void setType(SimpleLiteral type) {
        this.type = dublinFactory.createType(type);
    }
    
    public AbstractSimpleLiteral getType() {
        if (type != null)
            return type.getValue();
        return null;
    }
    
    public void setSubject(List<SimpleLiteral> subjects) {
        this.subject = new ArrayList<JAXBElement<SimpleLiteral>>();
        for (SimpleLiteral sub: subjects) {
            this.subject.add(dublinFactory.createSubject(sub));
        }
    }
    
    public void setSubject(SimpleLiteral subject) {
        if (this.subject == null) {
            this.subject = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        this.subject.add(dublinFactory.createSubject(subject));
    }
    
    public List<AbstractSimpleLiteral> getSubject() {
        List<AbstractSimpleLiteral> response = new ArrayList<AbstractSimpleLiteral>();
        if (subject != null) {
            for(JAXBElement<SimpleLiteral> jb: subject) {
                response.add(jb.getValue());
            }
        }
        return response;
    }
    
    public void setFormat(SimpleLiteral format) {
        this.format = dublinFactory.createFormat(format);
    }
    
    public AbstractSimpleLiteral getFormat() {
        if (format != null)
            return format.getValue();
        return null;
    }
    
    public void setModified(SimpleLiteral modified) {
        this.modified = dublinTermFactory.createModified(modified);
    }
    
    public AbstractSimpleLiteral getModified() {
        if (modified != null)
            return modified.getValue();
        return null;
    }
    
    public void setAbstract(SimpleLiteral _abstract) {
        this._abstract = dublinTermFactory.createAbstract(_abstract);
    }
    
    public AbstractSimpleLiteral getAbstract() {
        if (_abstract != null)
            return _abstract.getValue();
        return null;
    }
    
    public void setCreator(SimpleLiteral creator) {
        this.creator = dublinFactory.createCreator(creator);
    }
    
    public AbstractSimpleLiteral getCreator() {
        if (creator != null)
            return creator.getValue();
        return null;
    }
    
    public void setDistributor(SimpleLiteral distributor) {
        this.distributor = dublinFactory.createPublisher(distributor);
    }
    
    public AbstractSimpleLiteral getDistributor() {
        if (distributor != null)
            return distributor.getValue();
        return null;
    }
    
    public void setLanguage(SimpleLiteral language) {
        this.language = dublinFactory.createLanguage(language);
    }
    
    public AbstractSimpleLiteral getLanguage() {
        if (language != null)
            return language.getValue();
        return null;
    }
    
    public void setRelation(SimpleLiteral relation) {
        this.dcElement.add(dublinFactory.createRelation(relation));
    }
    
    public AbstractSimpleLiteral getRelation() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("relation")) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    public void setSource(SimpleLiteral source) {
        this.dcElement.add(dublinFactory.createSource(source));
    }
    
    public AbstractSimpleLiteral getSource() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("source")) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    public void setCoverage(SimpleLiteral coverage) {
        this.dcElement.add(dublinFactory.createCoverage(coverage));
    }
    
    public AbstractSimpleLiteral getCoverage() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("coverage")) {
                return jb.getValue();
            }
            
        }
        return null;
    }
    
    public void setDate(SimpleLiteral date) {
        this.dcElement.add(dublinFactory.createDate(date));
    }
    
    public AbstractSimpleLiteral getDate() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("date")) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    public void setRights(SimpleLiteral rights) {
        this.dcElement.add(dublinFactory.createRights(rights));
    }
    
    public AbstractSimpleLiteral getRights() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("rights")) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    public void setSpatial(SimpleLiteral spatial) {
        this.spatial = dublinTermFactory.createSpatial(spatial);
    }
    
    public AbstractSimpleLiteral getSpatial() {
        if (spatial != null)
            return spatial.getValue();
        return null;
    }
    
    public void setReferences(SimpleLiteral references) {
        this.references = dublinTermFactory.createReferences(references);
    }
    
    public AbstractSimpleLiteral getReferences() {
        if (references != null)
            return references.getValue();
        return null;
    }
    
    public void setPublisher(SimpleLiteral publisher) {
        this.dcElement.add(dublinFactory.createPublisher(publisher));
    }
    
    public AbstractSimpleLiteral getPublisher() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("publisher")) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    public void setContributor(SimpleLiteral contributor) {
        this.dcElement.add(dublinFactory.createContributor(contributor));
    }
    
    public AbstractSimpleLiteral getContributor() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("contributor")) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    public void setDescription(SimpleLiteral description) {
        this.dcElement.add(dublinFactory.createDescription(description));
    }
    
    public AbstractSimpleLiteral getDescription() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("description")) {
                return jb.getValue();
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (identifier != null && identifier.getValue() != null) {
            s.append("identifier: ").append(identifier.getValue()).append('\n');
        }
        if (title != null && title.getValue() != null) {
            s.append("title: ").append(title.getValue()).append('\n');
        }
        if (type != null && type.getValue() != null) {
            s.append("type: ").append(type.getValue()).append('\n');
        }
        if (format != null && format.getValue() != null) {
            s.append("format: ").append(format.getValue()).append('\n');
        }
        if (subject != null) {
            s.append("subjects: ").append('\n');
            for (JAXBElement<SimpleLiteral> sl: subject) {
                s.append(sl.getValue()).append('\n');
            }
        }
        if (dcElement != null) {
            for (JAXBElement<SimpleLiteral> jb: dcElement) {
                s.append("name=").append(jb.getName()).append(" value=").append(jb.getValue()).append('\n');
            }
        }
        if (language != null && language.getValue() != null) {
            s.append("language: ").append(language.getValue()).append('\n');
        }
        if (modified != null && modified.getValue() != null) {
            s.append("modified: ").append(modified).append('\n');
        }
        if (_abstract != null && _abstract.getValue() != null) {
            s.append("abstract: ").append(_abstract.getValue()).append('\n');
        }
        if (spatial != null && spatial.getValue() != null) {
            s.append("spatial: ").append(spatial.getValue()).append('\n');
        }
        if (references != null && references.getValue() != null) {
            s.append("references: ").append(references.getValue()).append('\n');
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
