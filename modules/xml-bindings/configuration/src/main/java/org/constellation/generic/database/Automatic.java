/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.generic.database;

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AbstractConfigurationObject;
import org.constellation.configuration.DataSourceType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "automatic")
public class Automatic extends AbstractConfigurationObject {

    @XmlTransient
    public static final int DEFAULT     = 0;
    @XmlTransient
    public static final int FILESYSTEM  = 1;
    @XmlTransient
    public static final int BYID        = 2;

    private static final Logger LOGGER = Logging.getLogger("org.constellation.generic.database");

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
     *
     * @see org.constellation.configuration.DataSourceType#VALUES
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
    
    private String schemaPrefix;

    /**
     * In the case of a fileSystem implementation,
     * this attribute contains the path of the directory containing the data.
     *  -- FileSystem specific flag
     */
    private String dataDirectory;

    /**
     * Enable the paralele execution.
     * @deprecated use getParameter("enableThread")
     */
    @Deprecated
    private String enableThread;

    /**
     * Enable the cache of metadata
     * (caution in case of large amount of data)
     * @deprecated use getParameter("enableCache")
     */
    @Deprecated
    private String enableCache;

    /**
     * Enable the indexation of published/all metadata.
     * -- MDWeb specific flag
     * @deprecated use getParameter("indexOnlyPublishedMetadata")
     */
    @Deprecated
    private Boolean indexOnlyPublishedMetadata;

    /**
     * Enable the indexation of external recordSet.
     * -- MDWeb specific flag
     * @deprecated use getParameter("indexExternalRecordset")
     */
    @Deprecated
    private Boolean indexExternalRecordset;

    /**
     * Enable the indexation of internal recordSet.
     *  -- MDWeb specific flag
     * @deprecated use getParameter("indexInternalRecordset")
     */
    @Deprecated
    private Boolean indexInternalRecordset;

    /**
     * In the case of a MDWeb implementation,
     * this flag allow to send all the metadata in the specified RecordSet.
     *  -- MDWeb specific flag
     * @deprecated use getParameter("defaultRecordSet")
     */
    @Deprecated
    private String defaultRecordSet;

    /**
     * Allow to store the mapping between MDWeb classes and GEOTK classes
     * in a properties file at the shutdown of the reader.
     * @deprecated use getParameter("storeMapping")
     */
    @Deprecated
    private String storeMapping;

    /**
     * Allow to disable the indexation part in of the metadataReader,
     * In the operation Harvest and transaction.
     * @deprecated use getParameter("noIndexation")
     */
    @Deprecated
    private String noIndexation;

    /**
     * In the case of a CSW configuration,
     * you can use this flag to substitute the Default catalog harvester,
     * by a ByIdHarvester or a fileSystemHarvester.
     * @deprecated use getParameter("harvester")
     */
    @Deprecated
    private String harvester;

    /**
     * In the case of CSW with a ByIdHarvester,
     * you must set this parameter to indicate
     * to the harvester where to find the file containing the identifiers.
     * @deprecated use getParameter("identifierDirectory")
     */
    @Deprecated
    private String identifierDirectory;

    private String logLevel;

    private HashMap<String, String> customparameters = new HashMap<>();

    /**
     * In the case of a generic Implementation,
     * this object contains all the SQL queries used to retrieve and build metadata.
     */
    private Queries queries;

    /**
     * In the case of a generic Obervation filter,
     * this object contains the SQL request to filter the observations.
     */
    private Query filterQueries;

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
            thesaurus = new ArrayList<>();
        }
        return thesaurus;
    }

    /**
     * @param thesaurus the Thesaurus to set
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
     * @deprecated use getParameter("enableThread")
     */
    public String getEnableThread() {
        return enableThread;
    }

    /**
     * @param enableThread the enableThread to set
     * @deprecated use putParameter("enableThread", boolean)
     */
    public void setEnableThread(final String enableThread) {
        this.enableThread = enableThread;
    }

    /**
     * @return the enableCache flag.
     * @deprecated use getParameter("enableCache")
     */
    public String getEnableCache() {
        return enableCache;
    }

    /**
     * @param enableCache the enableCache flag to set
     * @deprecated use putParameter("enableCache", boolean)
     */
    public void setEnablecache(final String enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * @return the storeMapping
     * @deprecated use getParameter("storeMapping")
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
     * @deprecated use getParameter("noIndexation")
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
     * @deprecated use getParameter("defaultRecordSet")
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
     * @deprecated use getParameter("harvester")
     */
    public String getHarvester() {
        return harvester;
    }

    /**
     * @param harvester the Harvester type to use.
     */
    public void setHarvester(final String harvester) {
        this.harvester = harvester;
    }

    /**
     * @return the identifierDirectory
     * @deprecated use getParameter("identifierDirectory")
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
     * @deprecated use getParameter("indexOnlyPublishedMetadata")
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
     * @deprecated use getParameter("indexInternalRecordset")
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

    /**
     * @return the indexExternalRecordset
     * @deprecated use getParameter("indexExternalRecordset")
     */
    public Boolean getIndexExternalRecordset() {
        if (indexExternalRecordset == null) {
            return true;
        }
        return indexExternalRecordset;
    }

    /**
     * @param indexExternalRecordset the indexExternalRecordset to set
     */
    public void setIndexExternalRecordset(final Boolean indexExternalRecordset) {
        this.indexExternalRecordset = indexExternalRecordset;
    }

    /**
     * @return the logLevel
     */
    public Level getLogLevel() {
        if (logLevel != null) {
            try {
                final Level l = Level.parse(logLevel);
                return l;
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Unexpected value for Log level:{0}", logLevel);
            }
        }
        return Level.INFO;
    }

    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

     /**
     * @return the customparameters
     */
    public HashMap<String, String> getCustomparameters() {
        if (customparameters == null) {
            customparameters = new HashMap<>();
        }
        return customparameters;
    }

    public void putParameter(final String key, final String value) {
        if (customparameters == null) {
            customparameters = new HashMap<>();
        }
        this.customparameters.put(key, value);
    }

    public String getParameter(final String key) {
        if (customparameters == null) {
            customparameters = new HashMap<>();
        }
        return customparameters.get(key);
    }

    public boolean getBooleanParameter(final String key, final boolean defaultValue) {
        if (customparameters == null) {
            customparameters = new HashMap<>();
        }
        if (customparameters.containsKey(key)) {
            return Boolean.parseBoolean(customparameters.get(key));
        }
        return defaultValue;
    }

    public List<String> getParameterList(final String key) {
        final List<String> result = new ArrayList<>();
        if (customparameters == null) {
            customparameters = new HashMap<>();
        }
        final String value = customparameters.get(key);
        if (value != null) {
            final String[] parts = value.split(",");
            result.addAll(Arrays.asList(parts));
        }
        return result;
    }

    public void setParameterList(final String key, List<String> list) {
        if (customparameters == null) {
            customparameters = new HashMap<>();
        }
        String s = ",";
        for (String l : list) {
            s = s + ',' + l;
        }
        s = s.substring(1);
        customparameters.put(key, s);
    }

    public void removeParameter(final String key) {
        if (customparameters == null) {
            customparameters = new HashMap<>();
        }
        customparameters.remove(key);
    }

    /**
     * @param customparameters the customparameters to set
     */
    public void setCustomparameters(final HashMap<String, String> customparameters) {
        this.customparameters = customparameters;
    }

    /**
     * @return the filterQueries
     */
    public Query getFilterQueries() {
        return filterQueries;
    }

    /**
     * @param filterQueries the filterQueries to set
     */
    public void setFilterQueries(Query filterQueries) {
        this.filterQueries = filterQueries;
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
        if (profile != null) {
            s.append("profile:").append(profile).append('\n');
        }
        if (logLevel != null) {
            s.append("logLevel: ").append(logLevel).append('\n');
        }
        if (filterQueries != null) {
            s.append("Filter queries:").append(filterQueries).append('\n');
        }
        if (customparameters != null) {
            s.append("custom parameters:\n");
            for (Entry entry : customparameters.entrySet()) {
                s.append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
            }
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
     * @param object The object to compare with.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Automatic) {
            final Automatic that = (Automatic) object;

            return Objects.equals(this.bdd,              that.bdd)              &&
                   Objects.equals(this.name  ,           that.name)             &&
                   Objects.equals(this.format  ,         that.format)           &&
                   Objects.equals(this.dataDirectory,    that.dataDirectory)    &&
                   Objects.equals(this.defaultRecordSet, that.defaultRecordSet) &&
                   Objects.equals(this.enableCache,      that.enableCache)      &&
                   Objects.equals(this.enableThread,     that.enableThread)     &&
                   Objects.equals(this.profile,          that.profile)          &&
                   Objects.equals(this.storeMapping,     that.storeMapping)     &&
                   Objects.equals(this.thesaurus,        that.thesaurus)        &&
                   Objects.equals(this.noIndexation,     that.noIndexation)     &&
                   Objects.equals(this.harvester,        that.harvester)        &&
                   Objects.equals(this.noIndexation,     that.noIndexation)     &&
                   Objects.equals(this.logLevel,         that.logLevel)         &&
                   Objects.equals(this.customparameters, that.customparameters) &&
                   Objects.equals(this.filterQueries,    that.filterQueries)    &&
                   Objects.equals(this.queries,          that.queries);
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
        hash = 37 * hash + (this.filterQueries != null ? this.filterQueries.hashCode() : 0);
        return hash;
    }

    /**
     * @return the schemaPrefix
     */
    public String getSchemaPrefix() {
        return schemaPrefix;
    }

    /**
     * @param schemaPrefix the schemaPrefix to set
     */
    public void setSchemaPrefix(String schemaPrefix) {
        this.schemaPrefix = schemaPrefix;
    }

}
