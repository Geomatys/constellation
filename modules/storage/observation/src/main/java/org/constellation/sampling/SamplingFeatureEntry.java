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
package org.constellation.sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

// Geotools dependencies
import org.constellation.gml.v311.AbstractFeatureEntry;
import org.constellation.observation.ObservationEntry;
import org.constellation.observation.SurveyProcedureEntry;
import org.geotools.util.Utilities;

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
public class SamplingFeatureEntry extends AbstractFeatureEntry implements SamplingFeature {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 8822736167506306189L;

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
        super(id, name, description);
        this.sampledFeature         = new ArrayList<String>();
        this.sampledFeature.add(sampledFeature);
        
    }
    
    public SamplingFeatureEntry(   final String                 id,
                                   final String                 name,
                                   final String                 description,
                                   final List<SamplingFeatureRelationEntry> relatedSamplingFeature,
                                   final List<ObservationEntry> relatedObservation,
                                   final List<String>           sampledFeature,
                                   final SurveyProcedureEntry   surveyDetail)
    {
        super(id, name, description);
        this.surveyDetail           = surveyDetail;
        this.relatedSamplingFeature = relatedSamplingFeature;
        this.sampledFeature         = sampledFeature;
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
        return super.hashCode();
    }

    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        
        if (object instanceof SamplingFeatureEntry && super.equals(object)) {
            final SamplingFeatureEntry that = (SamplingFeatureEntry) object;
            return Utilities.equals(this.surveyDetail,           that.surveyDetail)   &&
                   Utilities.equals(this.relatedObservation,     that.relatedObservation) &&
                   Utilities.equals(this.relatedSamplingFeature, that.relatedSamplingFeature) &&
                   Utilities.equals(this.sampledFeature,         that.sampledFeature);
        } else {
            System.out.println("AbstractFeatureEntry.equals=false");
        }
        return false;
        
    }

   /**
     * Retourne une chaine de charactere representant la station.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        Iterator i =  sampledFeature.iterator();
        String sampledFeatures = "";
        while (i.hasNext()) {
            sampledFeatures += i.next() + " ";
        }
        s.append("sampledFeature = ").append(sampledFeatures);
        return s.toString();
    }
   
}
