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


package net.seagis.cat.csw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.v100.BoundingBoxType;
import net.seagis.dublincore.elements.SimpleLiteral;


/**
 * 
 *             This type defines a summary representation of the common record
 *             format.  It extends AbstractRecordType to include the core
 *             properties.
 *          
 * 
 * <p>Java class for SummaryRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SummaryRecordType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}AbstractRecordType">
 *       &lt;sequence>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}identifier" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}title" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}type" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}subject" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}format" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/elements/1.1/}relation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/terms/}modified" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/terms/}abstract" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://purl.org/dc/terms/}spatial" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows}BoundingBox" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummaryRecordType", propOrder = {
    "identifier",
    "title",
    "type",
    "subject",
    "format",
    "relation",
    "modified",
    "_abstract",
    "spatial",
    "boundingBox"
})
public class SummaryRecordType extends AbstractRecordType {

    @XmlElementRef(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> identifier;
    @XmlElementRef(name = "title", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> title;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private SimpleLiteral type;
    @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
    private List<SimpleLiteral> subject;
    @XmlElementRef(name = "format", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> format;
    @XmlElementRef(name = "relation", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> relation;
    @XmlElement(namespace = "http://purl.org/dc/terms/")
    private List<SimpleLiteral> modified;
    @XmlElement(name = "abstract", namespace = "http://purl.org/dc/terms/")
    private List<SimpleLiteral> _abstract;
    @XmlElement(namespace = "http://purl.org/dc/terms/")
    private List<SimpleLiteral> spatial;
    @XmlElementRef(name = "BoundingBox", namespace = "http://www.opengis.net/ows", type = JAXBElement.class)
    private List<JAXBElement<? extends BoundingBoxType>> boundingBox;

    
    /**
     * An empty constructor used by JAXB
     */
    SummaryRecordType(){
        
    }
    
    /**
     * Build a new Summary record TODO add relation and spatial
     */
    public SummaryRecordType(SimpleLiteral identifier, SimpleLiteral title, SimpleLiteral type, List<BoundingBoxType> bboxes,
            List<SimpleLiteral> subject, SimpleLiteral format, SimpleLiteral modified, SimpleLiteral _abstract){
        
        this.identifier = new ArrayList<JAXBElement<SimpleLiteral>>();
        this.identifier.add(dublinFactory.createIdentifier(identifier));
        
        this.title = new ArrayList<JAXBElement<SimpleLiteral>>();
        this.title.add(dublinFactory.createTitle(title));
        
        this.type = type;
        
        this.boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        for (BoundingBoxType bbox: bboxes) {
            this.boundingBox.add(owsFactory.createBoundingBox(bbox));
        }
        this.subject = subject;
        
        this.format = new ArrayList<JAXBElement<SimpleLiteral>>();
        this.format.add(dublinFactory.createFormat(format));
        
        this.modified = new ArrayList<SimpleLiteral>();
        this.modified.add(modified);
        
        this._abstract = new ArrayList<SimpleLiteral>();
        this._abstract.add(_abstract);
        
        
    }
    
    
    /**
     * Gets the value of the identifier property.
     * (unmodifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getIdentifier() {
        if (identifier == null) {
            identifier = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(identifier);
    }

    /**
     * Gets the value of the title property.
     * (unmodifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getTitle() {
        if (title == null) {
            title = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(title);
    }

    /**
     * Gets the value of the type property.
     */
    public SimpleLiteral getType() {
        return type;
    }

    /**
     * Gets the value of the subject property.
     * (unmodifiable) 
     */
    public List<SimpleLiteral> getSubject() {
        if (subject == null) {
            subject = new ArrayList<SimpleLiteral>();
        }
        return Collections.unmodifiableList(subject);
    }

    /**
     * Gets the value of the format property.
     * (unmodifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getFormat() {
        if (format == null) {
            format = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(format);
    }

    /**
     * Gets the value of the relation property.
     * (unmodifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getRelation() {
        if (relation == null) {
            relation = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(relation);
    }

    /**
     * Gets the value of the modified property.
     * (unmodifiable)
     */
    public List<SimpleLiteral> getModified() {
        if (modified == null) {
            modified = new ArrayList<SimpleLiteral>();
        }
        return Collections.unmodifiableList(modified);
    }

    /**
     * Gets the value of the abstract property.
     * (unmodifiable)
     */
    public List<SimpleLiteral> getAbstract() {
        if (_abstract == null) {
            _abstract = new ArrayList<SimpleLiteral>();
        }
        return Collections.unmodifiableList(_abstract);
    }

    /**
     * Gets the value of the spatial property.
     * (unmodifiable)
     */
    public List<SimpleLiteral> getSpatial() {
        if (spatial == null) {
            spatial = new ArrayList<SimpleLiteral>();
        }
        return Collections.unmodifiableList(spatial);
    }

    /**
     * Gets the value of the boundingBox property.
     * (unmodifiable)
     */
    public List<JAXBElement<? extends BoundingBoxType>> getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
        }
        return Collections.unmodifiableList(boundingBox);
    }

}
