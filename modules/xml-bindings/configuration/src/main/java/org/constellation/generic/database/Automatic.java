/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

package org.constellation.generic.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.constellation.configuration.DataSourceType;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "automatic")
public class Automatic {
    
    @XmlTransient
    public static final int DEFAULT     = 0;
    @XmlTransient
    public static final int FILESYSTEM  = 1;
    @XmlTransient
    public static final int BYID        = 2;

    /**
     * The database connection informations.
     */
    private BDD bdd;

    /**
     * A List of thesaurus database connection informations.
     */
    private List<BDD> thesaurus;

    /**
     * The directory where is stored the configuration file.
     * must be set by java, not in the xml file because it is transient.
     */
    @XmlTransient
    private File configurationDirectory;

    /**
     * The specific type of implementation.
     * could be one of the static flag declared up there.
     * DEFAULT, CSR, MDWEB, FILESYSTEM, PRODLINE, ....
     */
    @XmlAttribute
    private String format;

    /**
     * A name to the object.
     * could be used in a case of multiple automatic in the same file.
     */
    @XmlAttribute
    private String name;

    /**
     * The profile of the service (Discovery or transactional).
     */
    private String profile;

    /**
     * Enable the paralele execution.
     */
    private String enableThread;

    /**
     * Enable the cache of metadata
     * (caution in case of large amount of data)
     */
    private String enableCache;

    /**
     * Enable the indexation of published/all metadata.
     * -- MDWeb specific flag
     */
    private Boolean indexOnlyPublishedMetadata;

    /**
     * Enable the indexation of internal recordSet.
     *  -- MDWeb specific flag
     */
    private Boolean indexInternalRecordset;

    /**
     * In the case of a MDWeb implementation,
     * this flag allow to send all the metadata in the specified RecordSet.
     *  -- MDWeb specific flag
     */
    private String defaultRecordSet;
    
    /**
     * Allow to store the mapping between MDWeb classes and GEOTK classes
     * in a properties file at the shutdown of the reader.
     */
    private String storeMapping;

    /**
     * Allow to disable the indexation part in of the metadataReader,
     * In the operation Harvest and transaction.
     */
    private String noIndexation;

    /**
     * In the case of a fileSystem implementation,
     * this attribute contains the path of the directory containing the data.
     *  -- FileSystem specific flag
     */
    private String dataDirectory;

    /**
     * In the case of a CSW configuration,
     * you can use this flag to substitute the Default catalog harvester,
     * by a ByIdHarvester or a fileSystemHarvester.
     */
    private String harvester;

    /**
     * In the case of CSW with a ByIdHarvester,
     * you must set this parameter to indicate
     * to the harvester where to find the file containing the identifiers.
     */
    private String identifierDirectory;

    /**
     * In the case of a generic Implementation,
     * this object contains all the SQL queries used to retrieve and build metadata.
     */
    private Queries queries;

    /**
     * Constructor used by JAXB
     */
    public Automatic() {
    }

    /**
     * Build an configuration object for file system dataSource.
     *
     * @param format type of the implementation.
     * @param dataDirectory Directory containing the data file.
     */
    public Automatic(final String format, final String dataDirectory) {
        this.format        = format;
        this.dataDirectory = dataDirectory;
    }

    /**
     * Build an configuration object for SGBD dataSource.
     *
     * @param format format type of the implementation.
     * @param bdd A dataSource description.
     */
    public Automatic(final String format, final BDD bdd) {
        this.format = format;
        this.bdd    = bdd;
    }

    /**
     * Build an configuration object for SGBD dataSource with generic SQL queries.
     *
      * @param format format type of the implementation.
     * @param bdd A dataSource description.
     * @param queries A list of SQL queries
     */
    public Automatic(final String format, final BDD bdd, final Queries queries) {
        this.format  = format;
        this.bdd     = bdd;
        this.queries = queries;
    }

    /**
     * return The generic SQL Queries
     * @return
     */
    public Queries getQueries() {
        return queries;
    }

    /**
     * return the database connection informations.
     * @return
     */
    public BDD getBdd() {
        return bdd;
    }

    /**
     * Set the database connection informations.
     * @param bdd a database description.
     */
    public void setBdd(final BDD bdd) {
        this.bdd = bdd;
    }

    /**
     * @return the configurationDirectory
     */
    public File getConfigurationDirectory() {
        return configurationDirectory;
    }

    /**
     * @param configurationDirectory the configurationDirectory to set
     */
    public void setConfigurationDirectory(final File configurationDirectory) {
        this.configurationDirectory = configurationDirectory;
    }

    /**
     * return the type of implementation.
     * @return
     */
    public String getFormat() {
        return format;
    }

    /**
     * set the type of implementation.
     * 
     * @param format
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    /**
     * Return the directory containing the data files.
     * @return
     */
    public File getDataDirectory() {
        File result = null;
        if (dataDirectory != null) {
            result = new File(dataDirectory);
            if (!result.exists()){
                // TODO find a way for windows
                if (dataDirectory.startsWith("/")) {
                    return result;
                } else if (configurationDirectory != null && configurationDirectory.exists()){
                    result = new File(configurationDirectory, dataDirectory);
                }
            }
        }
        return result;
    }
    
    public String getDataDirectoryValue() {
        return dataDirectory;
    }

    /**
     * set the directory containing the data files.
     * @param s
     */
    public void setDataDirectory(final String s) {
        this.dataDirectory = s;
    }

    /**
     * Return the type of implementation as a flag.
     * @return
     */
    public DataSourceType getType() {
        return new DataSourceType(format);
    }

    /**
      * Return the type of harvester implementation as a flag.
     * @return
     */
    public int getHarvestType() {
        if ("filesystem".equalsIgnoreCase(harvester))
            return FILESYSTEM;
        else if ("byid".equalsIgnoreCase(harvester))
            return BYID;
        else
            return DEFAULT;
    }

    /**
     * Return the type of profile as a flag.
     * @return
     */
    public int getProfile() {
        if ("discovery".equalsIgnoreCase(profile))
            return 0;
        return 1;
    }
    
    public String getProfileValue() {
        return profile;
    }

    /**
     * Set the type of profile.
     * @param profile
     */
    public void setProfile(final String profile) {
        this.profile = profile;
    }
    
    /**
     * @return the Thesaurus database informations
     */
    public List<BDD> getThesaurus() {
        if (thesaurus == null) {
            thesaurus = new ArrayList<BDD>();
        }
        return thesaurus;
    }

    /**
     * @param Thesaurus the Thesaurus to set
     */
    public void setThesaurus(final List<BDD> thesaurus) {
        this.thesaurus = thesaurus;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the enableThread
     */
    public String getEnableThread() {
        return enableThread;
    }

    /**
     * @param enableThread the enableThread to set
     */
    public void setEnableThread(final String enableThread) {
        this.enableThread = enableThread;
    }

    /**
     * @return the enableCache flag.
     */
    public String getEnableCache() {
        return enableCache;
    }

    /**
     * @param enablecache the enableCache flag to set
     */
    public void setEnablecache(final String enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * @return the storeMapping
     */
    public String getStoreMapping() {
        return storeMapping;
    }

    /**
     * @param storeMapping the storeMapping to set
     */
    public void setStoreMapping(final String storeMapping) {
        this.storeMapping = storeMapping;
    }

    /**
     * @return the noIndexation
     */
    public String getNoIndexation() {
        return noIndexation;
    }

    /**
     * @param noIndexation the noIndexation to set
     */
    public void setNoIndexation(final String noIndexation) {
        this.noIndexation = noIndexation;
    }

    /**
     * @return the defaultRecordSet
     */
    public String getDefaultRecordSet() {
        return defaultRecordSet;
    }

    /**
     * @param defaultRecordSet the defaultRecordSet to set
     */
    public void setDefaultRecordSet(final String defaultRecordSet) {
        this.defaultRecordSet = defaultRecordSet;
    }

     /**
     * @return the byIdHarvester
     */
    public String getHarvester() {
        return harvester;
    }

    /**
     * @param byIdHarvester the byIdHarvester to set
     */
    public void setHarvester(final String harvester) {
        this.harvester = harvester;
    }

    /**
     * @return the identifierDirectory
     */
    public String getIdentifierDirectory() {
        return identifierDirectory;
    }

    /**
     * @param identifierDirectory the identifierDirectory to set
     */
    public void setIdentifierDirectory(final String identifierDirectory) {
        this.identifierDirectory = identifierDirectory;
    }

    /**
     * @return the indexOnlyPublishedMetadata
     */
    public boolean getIndexOnlyPublishedMetadata() {
        if (indexOnlyPublishedMetadata == null) {
            return true;
        }
        return indexOnlyPublishedMetadata;
    }

    /**
     * @param indexOnlyPublishedMetadata the indexOnlyPublishedMetadata to set
     */
    public void setIndexOnlyPublishedMetadata(final Boolean indexOnlyPublishedMetadata) {
        this.indexOnlyPublishedMetadata = indexOnlyPublishedMetadata;
    }

    /**
     * @return the indexInternalRecordset
     */
    public boolean getIndexInternalRecordset() {
        if (indexInternalRecordset == null) {
            return false;
        }
        return indexInternalRecordset;
    }

    /**
     * Replace all the password in this object by '****'
     */
    public void hideSensibleField() {
        final String hidden = "****";
        if (bdd != null) {
            bdd.setPassword(hidden);
        }
        for (BDD bd : getThesaurus()) {
            bd.setPassword(hidden);
        }
    }

    /**
     * @param indexInternalRecordset the indexInternalRecordset to set
     */
    public void setIndexInternalRecordset(final Boolean indexInternalRecordset) {
        this.indexInternalRecordset = indexInternalRecordset;
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[Automatic]");
        if (name != null) {
            s.append("name: ").append(name).append('\n');
        }
        if (format != null) {
            s.append("format: ").append(format).append('\n');
        }
        if (bdd != null) {
            s.append("BDD:").append(bdd).append('\n');
        }
        if (thesaurus != null) {
            s.append("thesaurus:").append(thesaurus).append('\n');
        }
        if (dataDirectory != null) {
            s.append("dataDirectory:").append(dataDirectory).append('\n');
        }
        if (configurationDirectory != null) {
            s.append("configurationDirectory:").append(configurationDirectory).append('\n');
        }
        if (defaultRecordSet != null) {
            s.append("defaultRecordSet:").append(defaultRecordSet).append('\n');
        }
        if (enableCache != null) {
            s.append("enableCache:").append(enableCache).append('\n');
        }
        if (enableThread != null) {
            s.append("enableThread:").append(enableThread).append('\n');
        }
        if (profile != null) {
            s.append("profile:").append(profile).append('\n');
        }
        if (storeMapping != null) {
            s.append("storeMapping:").append(storeMapping).append('\n');
        }
        if (noIndexation != null) {
            s.append("noIndexation:").append(noIndexation).append('\n');
        }
        if (harvester != null) {
            s.append("harvester:").append(harvester).append('\n');
        }
        if (identifierDirectory != null) {
            s.append("identifierDirectory: ").append(identifierDirectory).append('\n');
        }
        return s.toString();
    }
    
    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Automatic) {
            final Automatic that = (Automatic) object;

            return Utilities.equals(this.bdd,              that.bdd)              &&
                   Utilities.equals(this.name  ,           that.name)             &&
                   Utilities.equals(this.format  ,         that.format)           &&
                   Utilities.equals(this.dataDirectory,    that.dataDirectory)    &&
                   Utilities.equals(this.defaultRecordSet, that.defaultRecordSet) &&
                   Utilities.equals(this.enableCache,      that.enableCache)      &&
                   Utilities.equals(this.enableThread,     that.enableThread)     &&
                   Utilities.equals(this.profile,          that.profile)          &&
                   Utilities.equals(this.storeMapping,     that.storeMapping)     &&
                   Utilities.equals(this.thesaurus,        that.thesaurus)        &&
                   Utilities.equals(this.noIndexation,     that.noIndexation)     &&
                   Utilities.equals(this.harvester,        that.harvester)        &&
                   Utilities.equals(this.noIndexation,     that.noIndexation)     &&
                   Utilities.equals(this.queries,          that.queries);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.bdd != null ? this.bdd.hashCode() : 0);
        hash = 37 * hash + (this.format != null ? this.format.hashCode() : 0);
        hash = 37 * hash + (this.queries != null ? this.queries.hashCode() : 0);
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 37 * hash + (this.thesaurus != null ? this.thesaurus.hashCode() : 0);
        hash = 37 * hash + (this.profile != null ? this.profile.hashCode() : 0);
        hash = 37 * hash + (this.enableThread != null ? this.enableThread.hashCode() : 0);
        hash = 37 * hash + (this.enableCache != null ? this.enableCache.hashCode() : 0);
        hash = 37 * hash + (this.storeMapping != null ? this.storeMapping.hashCode() : 0);
        hash = 37 * hash + (this.dataDirectory != null ? this.dataDirectory.hashCode() : 0);
        hash = 37 * hash + (this.defaultRecordSet != null ? this.defaultRecordSet.hashCode() : 0);
        hash = 37 * hash + (this.noIndexation != null ? this.noIndexation.hashCode() : 0);
        hash = 37 * hash + (this.harvester != null ? this.harvester.hashCode() : 0);
        return hash;
    }
}
