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
package net.seagis.observation;

// jaxb import
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

// Sicade dependencies 
import net.seagis.catalog.Entry;
import net.seagis.coverage.model.DistributionEntry;

// openGis dependencies
import net.seagis.gml.AbstractTimeGeometricPrimitiveType;
import net.seagis.gml.TimePeriodType;
import net.seagis.gml.TimePositionType;
import net.seagis.metadata.MetaDataEntry;
import net.seagis.swe.AnyResultEntry;
import net.seagis.swe.CompositePhenomenonEntry;
import net.seagis.swe.CompoundPhenomenonEntry;
import net.seagis.swe.DataBlockDefinitionEntry;
import net.seagis.swe.PhenomenonEntry;
import net.seagis.swe.PhenomenonPropertyType;
import net.seagis.swe.TimeGeometricPrimitivePropertyType;
import org.opengis.observation.Process;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.Observation;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.MetaData;

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
    "name",
    "definition",
    "observationMetadata",
    "samplingTime",
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
@XmlSeeAlso({ MeasurementEntry.class})
public class ObservationEntry extends Entry implements Observation {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3269639171560208276L;
    
    /**
     * Le nom de l'observation
     */
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
    private PhenomenonPropertyType observedProperty;

    /**
     * Référence vers la {@linkplain Procedure procédure} associée à cet observable.
     */
    @XmlElement(required = true)
    private ProcessEntry procedure;
    
     /**
     * Référence vers la {@linkplain Distribution distribution} associée à cet observable.
     */
    @XmlTransient
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
     private TimeGeometricPrimitivePropertyType samplingTime;
   
     
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
    private TimeGeometricPrimitivePropertyType procedureTime;
   
    
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
                            final AbstractTimeGeometricPrimitiveType  samplingTime,
                            final MetaDataEntry        observationMetadata,
                            final String               resultDefinition,
                            final AbstractTimeGeometricPrimitiveType  procedureTime,
                            final Object               procedureParameter) 
    {
        super(name);
        this.name                = name;
        this.definition          = definition;
        this.featureOfInterest   = featureOfInterest;
        this.observedProperty    = new PhenomenonPropertyType(observedProperty);
        this.procedure           = procedure;
        if (distribution == null)
            this.distribution    = DistributionEntry.NORMAL;
        else
            this.distribution    = distribution;
        this.resultQuality       = quality;
        this.result              = result;
        this.observationMetadata = observationMetadata;
        this.resultDefinition    = resultDefinition;
        this.procedureParameter  = procedureParameter; 
        this.samplingTime        = new TimeGeometricPrimitivePropertyType(samplingTime);
        this.procedureTime       = new TimeGeometricPrimitivePropertyType(procedureTime);
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
                            final AbstractTimeGeometricPrimitiveType   samplingTime,
                            final Object                resultDefinition)
    {
        super(name);
        this.name                = name;
        this.definition          = definition;
        this.featureOfInterest   = featureOfInterest;
        this.observedProperty    = new PhenomenonPropertyType(observedProperty);
        this.procedure           = procedure;
        if (distribution == null)
            this.distribution    = DistributionEntry.NORMAL;
        else
            this.distribution    = distribution;
        this.resultQuality       = null;       //= resultQuality;
        this.result              = result;
        this.observationMetadata = null;
        this.resultDefinition    = resultDefinition;
        this.procedureTime       = null;
        this.procedureParameter  = null;
        this.samplingTime        = new TimeGeometricPrimitivePropertyType(samplingTime);
    }

    /**
     * Construit un nouveau template temporaire d'observation a partir d'un template fournit en argument.
     * On y rajoute un samplingTime et un id temporaire. 
     */
    public ObservationEntry getTemporaryTemplate(String temporaryName, AbstractTimeGeometricPrimitiveType time) {
        if (time == null) { 
            TimePositionType begin = new  TimePositionType("1900-01-01 00:00:00");
            time = new TimePeriodType(begin);
        }
        PhenomenonEntry pheno = null;
        if (this.observedProperty != null) {
            pheno = this.observedProperty.getPhenomenon();
        }
        return new ObservationEntry(temporaryName,
                                        this.definition,
                                        this.featureOfInterest, 
                                        pheno,
                                        this.procedure,
                                        this.distribution,
                                        null,
                                        time,
                                        this.resultDefinition);
        
    }
    
    /**
     */
    public void setName(String name) {
        super.name = name;
        this.name  = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    /**
     * {@inheritDoc}
     */
    public SamplingFeature getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * {@inheritDoc}
     */
    public Phenomenon getObservedProperty() {
        if (observedProperty != null) {
            return observedProperty.getPhenomenon();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
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
    public Element getQuality() {
        return resultQuality;
    }

    /**
     * {@inheritDoc}
     */
    public Object getResult() {
        return result;
    }
    
    /**
     * fixe le resultat de l'observation
     */
    public void setResult(Object result) {
        this.result = result;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getResultDefinition() {
        return resultDefinition;
    }
    
    /**
     * fixe la definition du resultat.
     */
    public void setResultDefinition(Object definition) {
        this.resultDefinition = definition;
    }

    /**
     * {@inheritDoc}
     */
    public AbstractTimeGeometricPrimitiveType getSamplingTime() {
       if (samplingTime != null && samplingTime.getTimeGeometricPrimitive() != null)
            return samplingTime.getTimeGeometricPrimitive().getValue();
        else 
            return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setSamplingTime(AbstractTimeGeometricPrimitiveType value) {
        if (samplingTime != null)
            this.samplingTime.setTimeGeometricPrimitive(value);
        else
            this.samplingTime = new TimeGeometricPrimitivePropertyType(value);
    }

    /**
     * {@inheritDoc}
     */
    public MetaData getObservationMetadata() {
        return observationMetadata;
    }
    
    /**
     * {@inheritDoc}
     */
    public AbstractTimeGeometricPrimitiveType getProcedureTime() {
        if (procedureTime != null && procedureTime.getTimeGeometricPrimitive() != null)
            return procedureTime.getTimeGeometricPrimitive().getValue();
        else 
            return null;
    }
    
    /**
     * {@inheritDoc}
     */ 
    public Object getProcedureParameter() {
        return procedureParameter;
    }
    
   
    /**
     * {@inheritDoc}
     */ 
    public String getDefinition() {
        return definition;
    }
    
    /**
     * Retourne vrai si l'observation satisfait le template specifie
     */ 
    public boolean matchTemplate(ObservationEntry template) {
        System.out.println("matchTemplate:" + Utilities.equals(this.observedProperty,    template.observedProperty));
        System.out.println("this:" + this.observedProperty.toString());
        System.out.println("template:" + template.observedProperty.toString());
        return Utilities.equals(this.featureOfInterest,   template.featureOfInterest)   &&
               Utilities.equals(this.observedProperty,    template.observedProperty)    &&
               Utilities.equals(this.procedure,           template.procedure)           &&
               Utilities.equals(this.resultQuality,       template.resultQuality)       && 
               Utilities.equals(this.observationMetadata, template.observationMetadata) &&
               Utilities.equals(this.procedureTime,       template.procedureTime)       &&
               Utilities.equals(this.procedureParameter,  template.procedureParameter);
               //TODO corriger ce pb
               //Utilities.equals(this.distribution,        template.distribution)        &&
               //Utilities.equals(this.resultDefinition,    template.resultDefinition)    && 
        
    }
    /**
     * Retourne un code représentant cette observation.
     */
    @Override
    public final int hashCode() {
        return featureOfInterest.hashCode() ^ observedProperty.hashCode() ^ result.hashCode();
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final ObservationEntry that = (ObservationEntry) object;
            return Utilities.equals(this.featureOfInterest,   that.featureOfInterest)   &&
                   Utilities.equals(this.observedProperty,    that.observedProperty)    &&
                   Utilities.equals(this.procedure,           that.procedure)           &&
                   Utilities.equals(this.resultQuality,       that.resultQuality)       && 
                   Utilities.equals(this.distribution,        that.distribution)        &&
                   Utilities.equals(this.result,              that.result)              &&
                   Utilities.equals(this.samplingTime,        that.samplingTime)        &&
                   Utilities.equals(this.observationMetadata, that.observationMetadata) &&
                   Utilities.equals(this.resultDefinition,    that.resultDefinition)    &&
                   Utilities.equals(this.procedureTime,       that.procedureTime)       &&
                   Utilities.equals(this.procedureParameter,  that.procedureParameter);
        }
        return false;
    }
    
    /**
     * Verifie si l'observation est complete.
     */
    public boolean isComplete() {
        //TODO appeler les isCOmplete des attributs
        return (procedure != null) && (observedProperty != null) && (featureOfInterest != null);
    }
    
    /**
     * Retourne une chaine de charactere representant l'observation.
     */
    @Override
    public String toString() {
        StringBuilder s    = new StringBuilder();
        char lineSeparator = '\n';
        s.append("name=").append(name).append(lineSeparator).append("definition=").append(definition);
        if (samplingTime != null)
            s.append(" samplingTime=").append(samplingTime.toString()).append(lineSeparator);
        if (distribution != null)
            s.append(" distribution:").append(distribution.toString()).append(lineSeparator);
        else 
            s.append("DISTRIBUTION IS NULL").append(lineSeparator);
        if (procedure != null)
            s.append("procedure=").append(procedure.toString()).append(lineSeparator);
        else
            s.append("PROCEDURE IS NULL").append(lineSeparator);
        
        if (observedProperty != null)
            s.append("observedProperty=").append(observedProperty.toString()).append(lineSeparator);
        else s.append("OBSERVED PROPERTY IS NULL").append(lineSeparator);
        if (featureOfInterest != null)
            s.append("featureOfInterest=").append(featureOfInterest.toString()).append(lineSeparator); 
        else
            s.append("FEATURE OF INTEREST IS NULL").append(lineSeparator);
        if (result != null)       
            s.append(" result=").append(result.toString()).append(lineSeparator);
        if (resultDefinition != null)
            s.append(" resultDefinition=").append(resultDefinition.toString()).append(lineSeparator);
        else
            s.append("RESULT DEFINITION IS NULL").append(lineSeparator);
        return s.toString();
    }

    

}
