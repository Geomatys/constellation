
package net.sicade.observation;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import net.sicade.catalog.Entry;
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
public class MetaDataEntry extends Entry implements MetaData{
    
     /**
     * Unique identifier for this metadata file, or {@code null} if none.
     */
    private String fileIdentifier;

    /**
     * Language used for documenting metadata.
     */
    private Locale language;

    /**
     * Full name of the character coding standard used for the metadata set.
     */
    private CharacterSet characterSet;

    /**
     * File identifier of the metadata to which this metadata is a subset (child).
     */
    private String parentIdentifier;

    /**
     * Scope to which the metadata applies.
     */
    private Collection<ScopeCode> hierarchyLevels;

    /**
     * Name of the hierarchy levels for which the metadata is provided.
     */
    private Collection<String> hierarchyLevelNames;

    /**
     * Party responsible for the metadata information.
     *
     * @deprecated Replaced by {@link #Contacts}.
     */
    private ResponsibleParty contact;

    /**
     * Party responsible for the metadata information.
     *
     * @since GeoAPI 2.1
     */
    private Collection<? extends ResponsibleParty> contacts;

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
     */
    private Collection<Locale> locales;    

    /**
     * Digital representation of spatial information in the dataset.
     */
    private Collection<? extends SpatialRepresentation> spatialRepresentationInfo;

    /**
     * Description of the spatial and temporal reference systems used in the dataset.
     */
    private Collection<? extends ReferenceSystem> referenceSystemInfo;

    /**
     * Information describing metadata extensions.
     */
    private Collection<? extends MetadataExtensionInformation> metadataExtensionInfo;

    /**
     * Basic information about the resource(s) to which the metadata applies.
     */
    private Collection<? extends Identification> identificationInfo;

    /**
     * Provides information about the feature catalogue and describes the coverage and
     * image data characteristics.
     */
    private Collection<? extends ContentInformation> contentInfo;

    /**
     * Provides information about the distributor of and options for obtaining the resource(s).
     */
    private Distribution distributionInfo;

    /**
     * Provides overall assessment of quality of a resource(s).
     */
    private Collection<? extends DataQuality> dataQualityInfo;

    /**
     * Provides information about the catalogue of rules defined for the portrayal of a resource(s).
     */
    private Collection<? extends PortrayalCatalogueReference> portrayalCatalogueInfo;

    /**
     * Provides restrictions on the access and use of data.
     */
    private Collection<? extends Constraints> metadataConstraints;

    /**
     * Provides information about the conceptual schema of a dataset.
     */
    private Collection<? extends ApplicationSchemaInformation> applicationSchemaInfo;
     
    /**
     * Provides information about the frequency of metadata updates, and the scope of those updates.
     
    public MaintenanceInformation metadataMaintenance;
    */
    
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
        return language;
    }

    /**
     * Full name of the character coding standard used for the metadata set.
     */
    public CharacterSet getCharacterSet(){         
        return characterSet;
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
        return hierarchyLevels;
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
        return locales;
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
        return referenceSystemInfo; 
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
        return distributionInfo;
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
        return applicationSchemaInfo;
    }
     
    /**
     * Provides information about the frequency of metadata updates, and the scope of those updates.
     */
    public MaintenanceInformation getMetadataMaintenance(){         
        return null;//metadataMaintenance;
    }
}

