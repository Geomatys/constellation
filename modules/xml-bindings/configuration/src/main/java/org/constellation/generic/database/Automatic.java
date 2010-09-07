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
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "automatic")
public class Automatic {
    
    @XmlTransient
    public static final int DEFAULT     = 0;
    @XmlTransient
    public static final int CSR         = 1;
    @XmlTransient
    public static final int CDI         = 2;
    @XmlTransient
    public static final int EDMED      = 3;
    @XmlTransient
    public static final int MDWEB      = 4;
    @XmlTransient
    public static final int FILESYSTEM  = 5;
    @XmlTransient
    public static final int PRODLINE    = 6;
    @XmlTransient
    public static final int PRODSPEC   = 7;
    @XmlTransient
    public static final int SERV       = 8;
    @XmlTransient
    public static final int BYID       = 9;

    /**
     * The database connection informations.
     */
    private BDD bdd;

    /**
     * A List of thesaurus database connection informations.
     */
    private List<BDD> thesaurus;

    /**
     * The directory whe is stored the configuration file.
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
     */
    private String dataDirectory;

    /**
     * In the case of a MDWeb implementation,
     * this flag allow to send all the metadata in the specified RecordSet.
     */
    private String defaultRecordSet;

    /**
     * In the case of a CSW configuration,
     * you can use this flag to substitute the Default catalogue harvester,
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
     * this object contains all the sql queries used to retrieve and build metadata.
     */
    private Queries queries;

    public Automatic() {
    }

    public Automatic(String format, String dataDirectory) {
        this.format        = format;
        this.dataDirectory = dataDirectory;
    }

    public Automatic(String format, BDD bdd) {
        this.format = format;
        this.bdd    = bdd;
    }

    public Automatic(String format, BDD bdd, Queries queries) {
        this.format  = format;
        this.bdd     = bdd;
        this.queries = queries;
    }

    public Queries getQueries() {
        return queries;
    }

    public BDD getBdd() {
        return bdd;
    }

    public void setBdd(BDD bdd) {
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
    public void setConfigurationDirectory(File configurationDirectory) {
        this.configurationDirectory = configurationDirectory;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
    
    public File getDataDirectory() {
        File result = null;
        if (dataDirectory != null) {
            result = new File(dataDirectory);
            if (!result.exists() && configurationDirectory.exists()) {
                result = new File(configurationDirectory, dataDirectory);
            }
        }
        return result;
    }

    public void setDataDirectory(String s) {
        this.dataDirectory = s;
    }

    public int getType() {
        if ("cdi".equalsIgnoreCase(format))
            return CDI;
        else if ("csr".equalsIgnoreCase(format))
            return CSR;
        else if ("edmed".equalsIgnoreCase(format))
            return EDMED;
        else if ("mdweb".equalsIgnoreCase(format))
            return MDWEB;
        else if ("filesystem".equalsIgnoreCase(format))
            return FILESYSTEM;
        else if ("serv".equalsIgnoreCase(format))
            return SERV;
        else if ("prodline".equalsIgnoreCase(format))
            return PRODLINE;
        else if ("prodspec".equalsIgnoreCase(format))
            return PRODSPEC;
        else
            return DEFAULT;
    }

    public int getHarvestType() {
        if ("filesystem".equalsIgnoreCase(harvester))
            return FILESYSTEM;
        else if ("byid".equalsIgnoreCase(harvester))
            return BYID;
        else
            return DEFAULT;
    }

    public int getProfile() {
        if ("discovery".equalsIgnoreCase(profile))
            return 0;
        return 1;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
    
    /**
     * @return the Thesaurus
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
    public void setThesaurus(List<BDD> thesaurus) {
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
    public void setName(String name) {
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
    public void setEnableThread(String enableThread) {
        this.enableThread = enableThread;
    }

    /**
     * @return the enablecache
     */
    public String getEnableCache() {
        return enableCache;
    }

    /**
     * @param enablecache the enablecache to set
     */
    public void setEnablecache(String enableCache) {
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
    public void setStoreMapping(String storeMapping) {
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
    public void setNoIndexation(String noIndexation) {
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
    public void setDefaultRecordSet(String defaultRecordSet) {
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
    public void setHarvester(String harvester) {
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
    public void setIdentifierDirectory(String identifierDirectory) {
        this.identifierDirectory = identifierDirectory;
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
