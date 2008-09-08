/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package net.seagis.observation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.opengis.observation.Process;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;


/**
 * Implémentation d'une entrée représentant une {@linkplain Procedure procédure}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Process")
public class ProcessEntry extends Entry implements Process {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -1370011712794916454L;
    
    /**
     * Le nom/identifiant du capteur.
     */
    @XmlAttribute(required= true, namespace = "http://www.w3.org/1999/xlink")
    private String href;
    
     /**
     * Constructeur vide utilisé par JAXB.
     */
    private ProcessEntry(){}
    
    /**
     * Construit une nouvelle procédure du nom spécifié.
     *
     * @param name Le nom de la procédure.
     */
    public ProcessEntry(final String name) {
        super(name);
        this.href = name;
        
    }

    /** 
     * Construit une nouvelle procédure du nom spécifié avec les remarques spécifiées.
     *
     * @param name    Le nom de la procédure.
     * @param remarks Remarques s'appliquant à cette procédure, ou {@code null}.
     */
    public ProcessEntry(final String name, final String remarks) {
        super(name, remarks);
        this.href = name;
    }
    
     /**
     * surcharge la methode createName() de Entry pour accepter les valeurs nulles.
     */
    @Override
    protected String createName() {
        return null;
    }
    
    /**
     * Retourne la reference du capteur.
     */
    public String getHref() {
        return href;
    }
    
    
     /**
     * Verifie si cette entree est identique a l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final ProcessEntry that = (ProcessEntry) object;

        return Utilities.equals(this.href, that.href);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.href != null ? this.href.hashCode() : 0);
        return hash;
    }
     /**
     * Retourne une chaine de charactere representant la procedure.
     */
    @Override
    public String toString() {
        return  super.toString() + " href=" + this.getHref();
    }
}
