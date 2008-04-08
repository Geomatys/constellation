/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
package net.seagis.metadata;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.opengis.metadata.ApplicationSchemaInformation;
import org.opengis.metadata.MetaData;
import org.opengis.metadata.MetadataExtensionInformation;
import org.opengis.metadata.PortrayalCatalogueReference;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.content.ContentInformation;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.spatial.SpatialRepresentation;
import org.opengis.referencing.ReferenceSystem;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MetaData")
public class MetaDataEntry extends Entry implements MetaData{
    
     /**
     * Unique identifier for this metadata file, or {@code null} if none.
     */
    private String fileIdentifier;

    /**
     * Language used for documenting metadata.
     
JAXB issue    private Locale language;
     */
    
    /**
     * Full name of the character coding standard used for the metadata set.
    
JAXB issue    private CharacterSet characterSet;
     */
    
    /**
     * File identifier of the metadata to which this metadata is a subset (child).
     */
    private String parentIdentifier;

    /**
     * Scope to which the metadata applies.
     
JAXB issue     private Collection<ScopeCode> hierarchyLevels;
     */
    
    /**
     * Name of the hierarchy levels for which the metadata is provided.
     */
    private Collection<String> hierarchyLevelNames;

    /**
     * Party responsible for the metadata information.
     *
     * @deprecated Replaced by {@link #Contacts}.
     */
    private ResponsiblePartyEntry contact;

    /**
     * Party responsible for the metadata information.
     *
     * @since GeoAPI 2.1
     */
    private Collection<? extends ResponsiblePartyEntry> contacts;

    /**
     * Date that the metadata was created.
     */
    private Date dateStamp;

    /**
     * Name of the metadata standard (including profile name) used.
     */
    private String metadataStandardName;

    /**
     * Version (profile) of the metadata standard used.
     */
    private String metadataStandardVersion;

    /**
     * Uniformed Resource Identifier (URI) of the dataset to which the metadata applies.
     *
     * @since GeoAPI 2.1
     */
    private String dataSetUri;

    /**
     * Provides information about an alternatively used localized character
     * string for a linguistic extension
     *
     * @since GeoAPI 2.1
     
    JAXB issue private Collection<Locale> locales;    
     */
    
    /**
     * Digital representation of spatial information in the dataset.
     */
    private Collection<? extends SpatialRepresentationEntry> spatialRepresentationInfo;

    /**
     * Description of the spatial and temporal reference systems used in the dataset.
     
    JAXB issue  private Collection<? extends ReferenceSystem> referenceSystemInfo;
     */
    
    /**
     * Information describing metadata extensions.
     */
    private Collection<? extends MetadataExtensionInformationEntry> metadataExtensionInfo;
     
    
    /**
     * Basic information about the resource(s) to which the metadata applies.
     */
    private Collection<? extends IdentificationEntry> identificationInfo;

    /**
     * Provides information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    private Collection<? extends ContentInformationEntry> contentInfo;

    /**
     * Provides information about the distributor of and options for obtaining the resource(s).
     */
    @XmlTransient
    private Distribution distributionInfo;

    /**
     * Provides overall assessment of quality of a resource(s).
     */
    private Collection<? extends DataQualityEntry> dataQualityInfo;

    /**
     * Provides information about the catalogue of rules defined for the portrayal of a resource(s).
     */
    private Collection<? extends PortrayalCatalogueReferenceEntry> portrayalCatalogueInfo;

    /**
     * Provides restrictions on the access and use of data.
     */
    private Collection<? extends ConstraintsEntry> metadataConstraints;

    /**
     * Provides information about the conceptual schema of a dataset.
     
JAXB issue private Collection<? extends ApplicationSchemaInformation> applicationSchemaInfo;
     */
    
    /**
     * Provides information about the frequency of metadata updates, and the scope of those updates.
     */
    public MaintenanceInformationEntry metadataMaintenance;
    
    
    /**
     * Constructeur vide utilisé par JAXB.
     */
    private MetaDataEntry() {}
    
     /**
     * Unique identifier for this metadata file, or {@code null} if none.
     */
    public String getFileIdentifier(){
        return fileIdentifier;
    }

    /**
     * Language used for documenting metadata.
     */
    public Locale getLanguage(){
        throw new UnsupportedOperationException("Not supported yet.");
        //return language;
    }

    /**
     * Full name of the character coding standard used for the metadata set.
     */
    public CharacterSet getCharacterSet(){
        throw new UnsupportedOperationException("Not supported yet.");
        //return characterSet;
    }

    /**
     * File identifier of the metadata to which this metadata is a subset (child).
     */
    public String getParentIdentifier(){         
        return parentIdentifier;     
    }

    /**
     * Scope to which the metadata applies.
     */
    public Collection<ScopeCode> getHierarchyLevels(){ 
        throw new UnsupportedOperationException("Not supported yet.");
        //return hierarchyLevels;
    }

    /**
     * Name of the hierarchy levels for which the metadata is provided.
     */
    public Collection<String> getHierarchyLevelNames(){         
        return hierarchyLevelNames; 
    }

    /**
     * Party responsible for the metadata information.
     *
     * @deprecated Replaced by {@link #getContacts}.
     */
    public ResponsibleParty getContact(){         
        return contact;
    }

    /**
     * Party responsible for the metadata information.
     *
     * @since GeoAPI 2.1
     */
    public Collection<? extends ResponsibleParty> getContacts(){         
        return contacts;     
    }

    /**
     * Date that the metadata was created.
     */
    public Date getDateStamp(){         
        return dateStamp;     
    }

    /**
     * Name of the metadata standard (including profile name) used.
     */
    public String getMetadataStandardName(){         
        return metadataStandardName;     
    }

    /**
     * Version (profile) of the metadata standard used.
     */
    public String getMetadataStandardVersion(){        
        return metadataStandardVersion;    
    }

    /**
     * Uniformed Resource Identifier (URI) of the dataset to which the metadata applies.
     *
     * @since GeoAPI 2.1
     */
    public String getDataSetUri(){         
        return dataSetUri;
    }

    /**
     * Provides information about an alternatively used localized character
     * string for a linguistic extension
     *
     * @since GeoAPI 2.1
     */
    public Collection<Locale> getLocales(){ 
        throw new UnsupportedOperationException("Not supported yet.");
       //return locales;
    }    

    /**
     * Digital representation of spatial information in the dataset.
     */
    public Collection<? extends SpatialRepresentation> getSpatialRepresentationInfo(){         
        return spatialRepresentationInfo;
    }

    /**
     * Description of the spatial and temporal reference systems used in the dataset.
     */
    public Collection<? extends ReferenceSystem> getReferenceSystemInfo(){ 
        throw new UnsupportedOperationException("Not supported yet.");
        //return referenceSystemInfo; 
    }

    /**
     * Information describing metadata extensions.
     */
    public Collection<? extends MetadataExtensionInformation> getMetadataExtensionInfo(){         
        return metadataExtensionInfo;
    }

    /**
     * Basic information about the resource(s) to which the metadata applies.
     */
    public Collection<? extends Identification> getIdentificationInfo(){         
        return identificationInfo;
    }

    /**
     * Provides information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    public Collection<? extends ContentInformation> getContentInfo(){         
        return contentInfo;
    }

    /**
     * Provides information about the distributor of and options for obtaining the resource(s).
     */
    public Distribution getDistributionInfo(){ 
        throw new UnsupportedOperationException("Not supported yet.");
        //return distributionInfo;
    }

    /**
     * Provides overall assessment of quality of a resource(s).
     */
    public Collection<? extends DataQuality> getDataQualityInfo(){         
        return dataQualityInfo;
    }

    /**
     * Provides information about the catalogue of rules defined for the portrayal of a resource(s).
     */
    public Collection<? extends PortrayalCatalogueReference> getPortrayalCatalogueInfo(){         
        return portrayalCatalogueInfo;
    }

    /**
     * Provides restrictions on the access and use of data.
     */
    public Collection<? extends Constraints> getMetadataConstraints(){         
        return metadataConstraints;
    }

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    public Collection<? extends ApplicationSchemaInformation> getApplicationSchemaInfo(){  
        throw new UnsupportedOperationException("Not supported yet.");
        //return applicationSchemaInfo;
    }
     
    /**
     * Provides information about the frequency of metadata updates, and the scope of those updates.
     */
    public MaintenanceInformation getMetadataMaintenance(){         
        return metadataMaintenance;
    }
}

