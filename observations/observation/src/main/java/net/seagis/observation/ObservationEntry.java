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
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

// Sicade dependencies 
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Entry;
import net.seagis.coverage.model.Distribution;

// openGis dependencies
import net.seagis.gml.v311.AbstractTimeGeometricPrimitiveType;
import net.seagis.gml.v311.FeaturePropertyType;
import net.seagis.gml.v311.ReferenceEntry;
import net.seagis.gml.v311.TimePeriodType;
import net.seagis.gml.v311.TimePositionType;
import net.seagis.metadata.MetaDataEntry;
import net.seagis.sampling.SamplingFeatureEntry;
import net.seagis.swe.AnyResultEntry;
import net.seagis.swe.DataArrayEntry;
import net.seagis.swe.DataArrayPropertyType;
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
    "samplingTime",
    "procedure",
    "procedureParameter",
    "procedureTime",
    "observedProperty",
    "featureOfInterest",
    "result"
})
@XmlRootElement(name = "Observation")
@XmlSeeAlso({ MeasurementEntry.class})
public class ObservationEntry extends Entry implements Observation {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3269639171560208276L;
    
    /**
     * A logger (debugging purpose)
     */
    @XmlTransient
    Logger logger = Logger.getLogger("observationEntry");
    
    /**
     *The observation name
     */
    @XmlElement(namespace = "http://www.opengis.net/gml")
    private String name;
    
    /**
     * La description de l'observation
     */
    private String definition;
    
    /**
     * La station à laquelle a été pris cet échantillon.
     */
    @XmlElement(required = true)
    private FeaturePropertyType featureOfInterest;
    
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
    private Distribution distribution;
    
    /**
     * La qualité de la donnée. Peut être nul si cette information n'est pas disponible.
     */
    @XmlTransient
    private ElementEntry resultQuality;
    
    /**
     * le resultat de l'observation de n'importe quel type 
     */
    @XmlElement(required = true, type=DataArrayPropertyType.class)
    private Object result;
    
    /**
     *  
     */
     private TimeGeometricPrimitivePropertyType samplingTime;
   
     
     /**
      *
      */
     @XmlTransient
     private MetaDataEntry observationMetadata;
     
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
                            final Distribution         distribution,
                            final ElementEntry         quality,
                            final Object               result,
                            final AbstractTimeGeometricPrimitiveType  samplingTime,
                            final MetaDataEntry        observationMetadata,
                            final AbstractTimeGeometricPrimitiveType  procedureTime,
                            final Object               procedureParameter) 
    {
        super(name);
        this.name                = name;
        this.definition          = definition;
        this.featureOfInterest   = new FeaturePropertyType(featureOfInterest);
        this.observedProperty    = new PhenomenonPropertyType(observedProperty);
        this.procedure           = procedure;
        if (distribution == null)
            this.distribution    = Distribution.NORMAL;
        else
            this.distribution    = distribution;
        this.resultQuality       = quality;
        this.result              = result;
        this.observationMetadata = observationMetadata;
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
                            final Distribution          distribution,
                         // final ElementEntry          resultQuality,
                            final Object                result,
                            final AbstractTimeGeometricPrimitiveType   samplingTime)
    {
        super(name);
        this.name                = name;
        this.definition          = definition;
        this.featureOfInterest   = new FeaturePropertyType(featureOfInterest);
        this.observedProperty    = new PhenomenonPropertyType(observedProperty);
        this.procedure           = procedure;
        if (distribution == null)
            this.distribution    = Distribution.NORMAL;
        else
            this.distribution    = distribution;
        this.resultQuality       = null;       //= resultQuality;
        this.result              = result;
        this.observationMetadata = null;
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
        SamplingFeatureEntry foi = null;
        if (this.featureOfInterest != null) {
            foi = (SamplingFeatureEntry) this.featureOfInterest.getFeature();
        }
        //debugging purpose
        if (this.result instanceof DataArrayPropertyType) {
            DataArrayEntry d = ((DataArrayPropertyType)this.result).getDataArray();
            
            logger.info("encoding sent:" + d.getEncoding());     
        }
        return new ObservationEntry(temporaryName,
                                    this.definition,
                                    foi, 
                                    pheno,
                                    this.procedure,
                                    this.distribution,
                                    this.result,
                                    time);
        
    }
    
    /**
     */
    public void setName(String name) {
        this.name  = name;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * {@inheritDoc}
     */
    public SamplingFeature getFeatureOfInterest() {
        if (featureOfInterest != null) {
            return (SamplingFeature)featureOfInterest.getFeature();
        }
        return null;
    }
    
    public FeaturePropertyType getPropertyFeatureOfInterest(){
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
    
    public PhenomenonPropertyType getPropertyObservedProperty() {
       return observedProperty;
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
    public Distribution getDistribution() {
        return distribution;
    }
    
    /**
     * fixe la distribution de cette observation.
     */
    public void setDistribution(Distribution distrib) {
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
    public void setResult(Object result) throws CatalogException {
        if (!(result instanceof ReferenceEntry) && !(result instanceof AnyResultEntry)) {
            throw new CatalogException("this type " + result.getClass().getSimpleName() +
                                           " is not allowed in result");
        }
        this.result = result;
    }
    
    /**
     * {@inheritDoc}
     */
    public AbstractTimeGeometricPrimitiveType getSamplingTime() {
        return samplingTime.getTimeGeometricPrimitive();
        
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
        return procedureTime.getTimeGeometricPrimitive();
        
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
        
        boolean obsProperty = false;
        if (this.observedProperty != null && template.observedProperty != null) {
            obsProperty = Utilities.equals(this.observedProperty.getPhenomenon(),    template.observedProperty.getPhenomenon());
            if (!obsProperty) {
                System.out.println('\n' + "comparing observed property:" + '\n' + "THIS     => "+  this.observedProperty.getPhenomenon() + '\n' + "TEMPLATE => " + template.observedProperty.getPhenomenon() + '\n');
            }
        } else {
            obsProperty = this.observedProperty == null && template.observedProperty == null;
        }
        
        boolean obsFoi = false;
        if (this.featureOfInterest != null && template.featureOfInterest != null) {
            obsFoi = Utilities.equals(this.featureOfInterest.getFeature(),    template.featureOfInterest.getFeature());
            if (!obsFoi) {
                System.out.println('\n' + "comparing feature of interest:" + '\n' + "THIS    => "+  this.featureOfInterest.getFeature() + '\n' + "TEMPLATE => " + template.featureOfInterest.getFeature() + '\n');
            }
        } else {
            obsFoi = this.featureOfInterest == null && template.featureOfInterest == null;
        }
        
        boolean match = obsFoi                                                                   &&
                        Utilities.equals(this.procedure,           template.procedure)           &&
                        Utilities.equals(this.resultQuality,       template.resultQuality)       && 
                        Utilities.equals(this.observationMetadata, template.observationMetadata) &&
                        Utilities.equals(this.procedureTime,       template.procedureTime)       &&
                        Utilities.equals(this.procedureParameter,  template.procedureParameter)  &&
                        obsProperty;
        if (!match) {
            logger.severe("error matching template report:" + '\n' +
                   "FOI  =>" + obsFoi                                                                   + '\n' +
                   "PROC =>" + Utilities.equals(this.procedure,           template.procedure)           + '\n' +
                   "QUAL =>" + Utilities.equals(this.resultQuality,       template.resultQuality)       + '\n' + 
                   "META =>" + Utilities.equals(this.observationMetadata, template.observationMetadata) + '\n' +
                   "PTI  =>" + Utilities.equals(this.procedureTime,       template.procedureTime)       + '\n' +
                   "PPAM =>" + Utilities.equals(this.procedureParameter,  template.procedureParameter)  + '\n' +
                   "PHEN =>" + obsProperty);
        }
        return match;
        
             
               
        
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
        return (procedure != null) && (observedProperty != null) && (featureOfInterest != null) && (result != null);
    }
    
    /**
     * Retourne une chaine de charactere representant l'observation.
     */
    @Override
    public String toString() {
        StringBuilder s    = new StringBuilder();
        char lineSeparator = '\n';
        s.append("name = ").append(name);
        if (definition != null)
            s.append("definition = ").append(definition);
        s.append(lineSeparator);
        if (samplingTime != null)
            s.append("samplingTime = ").append(samplingTime.toString()).append(lineSeparator);
        if (distribution != null)
            s.append(" distribution = ").append(distribution.toString()).append(lineSeparator);
       if (procedure != null)
            s.append("procedure = ").append(procedure.toString()).append(lineSeparator);
        else
            s.append("procedure is null!").append(lineSeparator);
        
        if (observedProperty != null)
            s.append("observedProperty = ").append(observedProperty.toString()).append(lineSeparator);
        else s.append("observed property is null!").append(lineSeparator);
        if (featureOfInterest != null)
            s.append("feature Of Interest = ").append(featureOfInterest.toString()).append(lineSeparator); 
        else
            s.append("feature Of Interest is null!").append(lineSeparator);
        if (result != null)       
            s.append(" result = ").append(result.toString()).append(lineSeparator);
        return s.toString();
    }

    

}
