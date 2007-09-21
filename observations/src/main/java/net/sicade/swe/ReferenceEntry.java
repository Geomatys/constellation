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
package net.sicade.swe;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import net.sicade.catalog.Entry;

/**
 * Une reference decrivant un resultat pour une ressource MIME externe.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceType")
public class ReferenceEntry extends Entry implements Reference{
    
    /**
     * L'identifiant de la reference.
     */
    private String id;
    
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
    @XmlAttribute
    private java.lang.Boolean owns;
    
    
    /**
     * COnstructeur utilisé par jaxB
     */
    protected ReferenceEntry(){}
    
    /**
     * Créé une nouvelle reference. reduit pour l'instant a voir en fontion des besoins.
     */
    public ReferenceEntry(String id, String href) {
        super(id);
        this.id = id;
    }

    /**
     * retourne l'identifiant de la reference.
     */
    public String getId() {
        return id;
    }

    public List<String> getNilReason() {
        return nilReason;
    }

    public String getRemoteSchema() {
        return remoteSchema;
    }

    public String getActuate() {
        return actuate;
    }

    public String getArcrole() {
        return arcrole;
    }

    public String getHref() {
        return href;
    }

    public String getRole() {
        return role;
    }

    public String getShow() {
        return show;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public java.lang.Boolean getOwns() {
        return owns;
    }
    
}
