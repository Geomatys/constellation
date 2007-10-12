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
package net.sicade.observation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import net.sicade.catalog.Entry;
// Geotools dependencies
import org.geotools.resources.Utilities;

// openGis dependencies
import org.opengis.observation.sampling.SamplingFeature;

/**
 * Implémentation d'une entrée représentant une {@link SamplingFeature station}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @todo L'implémentation actuelle n'est pas <cite>serializable</cite> du fait qu'elle nécessite
 *       une connexion à la base de données. Une version future devrait rétablir la connexion au
 *       moment de la <cite>deserialization</cite>.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SamplingFeature", namespace="http://www.opengis.net/sa/1.0")
@XmlSeeAlso({ SamplingPointEntry.class})
public class SamplingFeatureEntry extends Entry implements SamplingFeature {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 8822736167506306189L;

    /**
     * L'identifiant alphanumérique de la station.
     */
    @XmlAttribute(required = true)
    private String id;
    
    
    /**
     * La description de la station.
     */
    private String description;
    
    /**
     * Le nom de la station (un code alphanumerique)
     */
    private String name;
    
    /**
     * 
     */
    private Collection<SamplingFeatureRelationEntry> relatedSamplingFeature;
    
    /**
     * Les Observations
     */
    private Collection<ObservationEntry> relatedObservation;
    
    /**
     * Les features designé
     */
    private Collection<String> sampledFeature; 
    
    /**
     * Connexion vers la table des "survey details"
     * Optionnel peut etre {@code null}
     */
    private SurveyProcedureEntry surveyDetail;
    

    /**
     * Constructeur vide utilisé par JAXB.
     */
    protected SamplingFeatureEntry(){}
    
    /**
     * 
     * Construit une entrée pour l'identifiant de station spécifié.
     * adapté au modele de BRGM.
     * 
     * 
     * @param id  L'identifiant numérique de la station.
     * @param name        Le nom de la station.
     * @param description Une description de la station.
     * @param le
     */
    public SamplingFeatureEntry(   final String            id,
                                   final String            name,
                                   final String            description,
                                   final String            sampledFeature)
    {
        super(name, description);
        this.id                     = id;
        this.name                   = name;
        this.description            = description;
        this.sampledFeature         = new ArrayList<String>();
        this.sampledFeature.add(sampledFeature);
        
    }
    
    public SamplingFeatureEntry(   final String                 id,
                                   final String                 name,
                                   final String                 description,
                                   final List<SamplingFeatureRelationEntry> relatedSamplingFeature,
                                   final List<ObservationEntry> relatedObservation,
                                   final List<Object>           sampledFeature,
                                   final SurveyProcedureEntry   surveyDetail)
    {
        super(name, description);
        this.id                     = id;
        this.description            = description;
        this.surveyDetail           = surveyDetail;
        this.relatedSamplingFeature = relatedSamplingFeature;
        this.relatedObservation     = relatedObservation;
       // this.sampledFeature         = sampledFeature;
    }

    /**
     * retourne l'identifiant de la station.
     */
    public String getId() {
        return id;
    }
    
    /**
     * retourne la description de la station.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized Collection<SamplingFeatureRelationEntry> getRelatedSamplingFeatures() {
    
        return relatedSamplingFeature;
    }
  
    
    /**
     * {@inheritDoc}
     */
    public synchronized Collection<ObservationEntry> getRelatedObservations() {
       
        return relatedObservation;
    }
    
     /**
     * {@inheritDoc}
     */
    public synchronized Collection<String> getSampledFeatures() {
        
        return sampledFeature;
    }

    public SurveyProcedureEntry getSurveyDetail() {
        return this.surveyDetail;
    }
    

    /**
     * Retourne le code numérique identifiant cette entrée.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SamplingFeatureEntry that = (SamplingFeatureEntry) object;
            return Utilities.equals(this.id,             that.id) &&
                   Utilities.equals(this.surveyDetail,           that.surveyDetail)   &&
                   Utilities.equals(this.description,            that.description)   && 
                   Utilities.equals(this.relatedObservation,     that.relatedObservation) &&
                   Utilities.equals(this.relatedSamplingFeature, that.relatedSamplingFeature) &&
                   Utilities.equals(this.sampledFeature,         that.sampledFeature);
        }
        return false;
    }

   /**
     * Retourne une chaine de charactere representant la station.
     */
    @Override
    public String toString() {
        Iterator i =  sampledFeature.iterator();
        String sampledFeatures = "";
        while (i.hasNext()) {
            sampledFeatures += i.next() + " ";
        }
        return " id=" + id + " name=" + name  + " description=" + description + " sampledFeature=" +
               sampledFeatures; 
    }
   
}
