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

    private BDD bdd;

    private List<BDD> thesaurus;

    @XmlTransient
    private File configurationDirectory;
    
    @XmlAttribute
    private String format;

    @XmlAttribute
    private String name;

    private String profile;

    private String enableThread;
    
    private String enableCache;

    private String storeMapping;

    private String dataDirectory;

    private String defaultRecordSet;

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
        if ("cdi".equals(format))
            return CDI;
        else if ("csr".equals(format))
            return CSR;
        else if ("edmed".equals(format))
            return EDMED;
        else if ("mdweb".equals(format))
            return MDWEB;
        else if ("filesystem".equals(format))
            return FILESYSTEM;
        else if ("serv".equals(format))
            return SERV;
        else if ("prodline".equals(format))
            return PRODLINE;
        else if ("prodspec".equals(format))
            return PRODSPEC;
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
        if (queries != null) {
            s.append("queries: ").append(queries).append('\n');
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
        return hash;
    }

}
