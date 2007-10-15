/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it or
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

// jaxb import
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

// Sicade dependencies 
import net.sicade.catalog.Entry;
import net.sicade.coverage.model.DistributionEntry;

// openGis dependencies
import net.sicade.swe.AnyResultEntry;
import net.sicade.swe.DataBlockDefinitionEntry;
import org.opengis.observation.Process;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.Observation;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.MetaData;
import org.opengis.temporal.TemporalObject;

// geotools dependencies
import org.geotools.resources.Utilities;





/**
 * Implémentation d'une entrée représentant une {@linkplain Observation observation}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Observation", propOrder = {
    "definition",
    "observationMetadata",
    "samplingTime",
    "distribution",
    "procedure",
    "procedureParameter",
    "procedureTime",
    "observedProperty",
    "featureOfInterest",
    "result",
    "resultQuality",
    "resultDefinition"
})
@XmlRootElement(name = "Observation")
@XmlSeeAlso({ SamplingFeatureEntry.class, SamplingPointEntry.class, 
              PhenomenonEntry.class, CompositePhenomenonEntry.class,
              DataBlockDefinitionEntry.class, MeasurementEntry.class,
              MeasureEntry.class, AnyResultEntry.class})
public class ObservationEntry extends Entry implements Observation {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3269639171560208276L;
    
    /**
     * Le nom de l'observation.
     */
    @XmlAttribute(required = true)
    private String name;
    
    /**
     * La description de l'observation
     */
    private String definition;
    
    /**
     * La station à laquelle a été pris cet échantillon.
     */
    @XmlElement(required = true)
    private SamplingFeatureEntry featureOfInterest;
    
    /**
     * Référence vers le {@linkplain Phenomenon phénomène} observé.
     */
    @XmlElement(required = true)
    private PhenomenonEntry observedProperty;

    /**
     * Référence vers la {@linkplain Procedure procédure} associée à cet observable.
     */
    @XmlElement(required = true)
    private ProcessEntry procedure;
    
     /**
     * Référence vers la {@linkplain Distribution distribution} associée à cet observable.
     */
    private DistributionEntry distribution;
    
    /**
     * La qualité de la donnée. Peut être nul si cette information n'est pas disponible.
     */
    private ElementEntry resultQuality;
    
    /**
     * le resultat de l'observation de n'importe quel type 
     */
    @XmlElement(required = true)
    private Object result;
    
    /**
     *  
     */
     @XmlElement(required = true)
     private TemporalObjectEntry samplingTime;
     
     /**
      *
      */
     private MetaDataEntry observationMetadata;
     
    /**
     * Definition du resultat. 
     */
    private Object resultDefinition;
    
    /**
     * 
     */
    private TemporalObjectEntry procedureTime;
    
    /**
     *
     */
    private Object procedureParameter;
    
    /**
     * Construit une observation vide (utilisé pour la serialisation par JAXB)
     */
    public ObservationEntry() {}
    
    /**
     * Construit une observation.
     * 
     * 
     * @param featureOfInterest La station d'observation (par exemple une position de pêche).
     * @param observedProperty  Le phénomène observé.
     * @param procedure         La procédure associée.
     * @param resultQuality    La qualité de la donnée, ou {@code null} si inconnue.
     */
    public ObservationEntry(final String               name,
                            final String               definition,
                            final SamplingFeatureEntry featureOfInterest, 
                            final PhenomenonEntry      observedProperty,
                            final ProcessEntry         procedure,
                            final DistributionEntry    distribution,
                            final ElementEntry         quality,
                            final Object               result,
                            final TemporalObjectEntry  samplingTime,
                            final MetaDataEntry        observationMetadata,
                            final String               resultDefinition,
                            final TemporalObjectEntry  procedureTime,
                            final Object               procedureParameter) 
    {
        super(name);
        this.name                = name;
        this.definition          = definition;
        this.featureOfInterest   = featureOfInterest;
        this.observedProperty    = observedProperty;
        this.procedure           = procedure;
        if (distribution == null)
            this.distribution    = DistributionEntry.NORMAL;
        else
            this.distribution    = distribution;
        this.resultQuality       = quality;
        this.result              = result;
        this.samplingTime        = samplingTime;
        this.observationMetadata = observationMetadata;
        this.resultDefinition    = resultDefinition;
        this.procedureTime       = procedureTime;
        this.procedureParameter  = procedureParameter; 
    }
    
    /**
     * Construit une observation reduite adapté a BRGM.
     * 
     * 
     * @param featureOfInterest La station d'observation (par exemple une position de pêche).
     * @param observedProperty  Le phénomène observé.
     * @param procedure         La procédure associée.
     * @param resultQuality    La qualité de la donnée, ou {@code null} si inconnue.
     */
    public ObservationEntry(final String                name,
                            final String                definition,
                            final SamplingFeatureEntry  featureOfInterest, 
                            final PhenomenonEntry       observedProperty,
                            final ProcessEntry          procedure,
                            final DistributionEntry     distribution,
                         // final ElementEntry          resultQuality,
                            final Object                result,
                            final TemporalObjectEntry   samplingTime,
                            final Object                resultDefinition)
    {
        super(name);
        this.name                = name;
        this.definition          = definition;
        this.featureOfInterest   = featureOfInterest;
        this.observedProperty    = observedProperty;
        this.procedure           = procedure;
        if (distribution == null)
            this.distribution    = DistributionEntry.NORMAL;
        else
            this.distribution    = distribution;
        this.resultQuality       = null;       //= resultQuality;
        this.result              = result;
        this.samplingTime        = samplingTime;
        this.observationMetadata = null;
        this.resultDefinition    = resultDefinition;
        this.procedureTime       = null;
        this.procedureParameter  = null; 
    }

    /**
     */
    public void setName(String name) {
        super.name = name;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeature getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Phenomenon getObservedProperty() {
        return observedProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Process getProcedure() {
        return procedure;
    }
    
    /**
     * fixe le capteur qui a effectué cette observation.
     */
    public void setProcedure(ProcessEntry process) {
        this.procedure = process;
    }

    /**
     * {@inheritDoc}
     */
    public DistributionEntry getDistribution() {
        return distribution;
    }
    
    /**
     * fixe la distribution de cette observation.
     */
    public void setDistribution(DistributionEntry distrib) {
        this.distribution = distrib;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Element getQuality() {
        return resultQuality;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult() {
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResultDefinition() {
        return resultDefinition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalObject getSamplingTime() {
        return samplingTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaData getObservationMetadata() {
        return observationMetadata;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
     public TemporalObject getProcedureTime() {
        return procedureTime;
    }
    
    /**
     * {@inheritDoc}
     */ 
    @Override
    public Object getProcedureParameter() {
        return procedureParameter;
    }
    
   
    /**
     * {@inheritDoc}
     */ 
    @Override
    public String getDefinition() {
        return definition;
    }
    
    /**
     * Retourne vrai si l'observation satisfait le template specifi�
     */ 
    public boolean matchTemplate(ObservationEntry template) {
        return Utilities.equals(this.featureOfInterest,   template.featureOfInterest) &&
               Utilities.equals(this.observedProperty,    template.observedProperty) &&
               Utilities.equals(this.procedure,           template.procedure)  &&
               Utilities.equals(this.resultQuality,       template.resultQuality)    && 
               Utilities.equals(this.distribution,        template.distribution) &&
               Utilities.equals(this.observationMetadata, template.observationMetadata) &&
               Utilities.equals(this.resultDefinition,    template.resultDefinition) &&
               Utilities.equals(this.procedureTime,       template.procedureTime) &&
               Utilities.equals(this.procedureParameter,  template.procedureParameter);
        
    }
    /**
     * Retourne un code représentant cette observation.
     */
    @Override
    public final int hashCode() {
        return featureOfInterest.hashCode() ^ observedProperty.hashCode() ^ result.hashCode();
    }

    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final ObservationEntry that = (ObservationEntry) object;
            return Utilities.equals(this.featureOfInterest,   that.featureOfInterest) &&
                   Utilities.equals(this.observedProperty,    that.observedProperty) &&
                   Utilities.equals(this.procedure,           that.procedure)  &&
                   Utilities.equals(this.resultQuality,       that.resultQuality)    && 
                   Utilities.equals(this.distribution,        that.distribution) &&
                   Utilities.equals(this.result,              that.result) &&
                   Utilities.equals(this.samplingTime,        that.samplingTime) &&
                   Utilities.equals(this.observationMetadata, that.observationMetadata) &&
                   Utilities.equals(this.resultDefinition,    that.resultDefinition) &&
                   Utilities.equals(this.procedureTime,       that.procedureTime) &&
                   Utilities.equals(this.procedureParameter,  that.procedureParameter);
        }
        return false;
    }
    
    /**
     * Retourne une chaine de charactere representant l'observation.
     */
    @Override
    public String toString() {
        return "name=" + name + " definition=" + definition + " samplingTime=" + 
                samplingTime.toString() + " procedure=" + procedure.toString() + 
                " observedProperty=" + observedProperty.toString() + " featureOfInterest=" +
                featureOfInterest.toString() + " result=" + result.toString() + " resultDefinition=" +
                resultDefinition.toString(); 
                
    }

    

}
