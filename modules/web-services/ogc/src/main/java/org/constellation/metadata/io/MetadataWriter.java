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
package org.constellation.metadata.io;

// J2SE dependencies
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.logging.Logger;

// constellation dependencies
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.dublincore.AbstractSimpleLiteral;
import org.constellation.ebrim.v300.InternationalStringType;
import org.constellation.ebrim.v250.RegistryObjectType;
import org.constellation.metadata.index.AbstractIndexer;
import org.constellation.ws.WebServiceException;

//geotools dependencies
import org.geotools.metadata.iso.MetaDataImpl;

// geotools dependencies
import org.opengis.metadata.identification.Identification;

/**
 *
 * @author Guilhem Legal
 */
public abstract class MetadataWriter {

    /**
     * A debugging logger.
     */
    protected static Logger LOGGER = Logger.getLogger("org.constellation.metadata.io");
    
    /**
     * Record the date format in the metadata.
     */
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * An indexer lucene to add object into the index.
     */
    protected final AbstractIndexer indexer;
    
    /**
     * Build a new metadata writer.
     * 
     * @param MDReader an MDWeb database reader.
     */
    public MetadataWriter(AbstractIndexer indexer) throws SQLException {
        this.indexer        = indexer;
    }

    /**
     * This method try to find a title to this object.
     * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
     * else we try to find a getName() method.
     * 
     * @param obj the object for wich we want a title
     * 
     * @return the founded title or "Unknow title"
     */
    protected String findName(Object obj) {
        
        //here we try to get the title
        AbstractSimpleLiteral titleSL = null;
        String title = "unknow title";
        if (obj instanceof RecordType) {
            titleSL = ((RecordType) obj).getTitle();
            if (titleSL == null) {
                titleSL = ((RecordType) obj).getIdentifier();
            }
                               
            if (titleSL == null) {
                title = "unknow title";
            } else {
                if (titleSL.getContent().size() > 0)
                    title = titleSL.getContent().get(0);
            }
                            
        } else if (obj instanceof MetaDataImpl) {
            Collection<Identification> idents = ((MetaDataImpl) obj).getIdentificationInfo();
            if (idents.size() != 0) {
                Identification ident = idents.iterator().next();
                if (ident != null && ident.getCitation() != null && ident.getCitation().getTitle() != null) {
                    title = ident.getCitation().getTitle().toString();
                } 
            }
        } else if (obj instanceof org.constellation.ebrim.v300.RegistryObjectType) {
            InternationalStringType ident = ((org.constellation.ebrim.v300.RegistryObjectType) obj).getName();
            if (ident != null && ident.getLocalizedString().size() > 0) {
                title = ident.getLocalizedString().get(0).getValue();
            } else {
                title = ((RegistryObjectType) obj).getId();
            } 
        
        } else if (obj instanceof org.constellation.ebrim.v250.RegistryObjectType) {
            org.constellation.ebrim.v250.InternationalStringType ident = ((org.constellation.ebrim.v250.RegistryObjectType) obj).getName();
            if (ident != null && ident.getLocalizedString().size() > 0) {
                title = ident.getLocalizedString().get(0).getValue();
            } else {
                title = ((org.constellation.ebrim.v250.RegistryObjectType) obj).getId();
            } 
            
        } else {
            Method nameGetter = null;
            String methodName = "";
            int i = 0;
            while (i < 3) {
                try {
                    switch (i) {
                        case 0: methodName = "getTitle";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                                 
                        case 1: methodName = "getName";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                                
                        case 2: methodName = "getId";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                    }
                
                
                } catch (NoSuchMethodException ex) {
                    LOGGER.finer("not " + methodName + " method in " + obj.getClass().getSimpleName());
                } catch (SecurityException ex) {
                    LOGGER.severe(" security exception while getting the title of the object.");
                }
                if (nameGetter != null) {
                    i = 3;
                } else {
                    i++;
                }
            }
            
            if (nameGetter != null) {
                try {
                    Object objT = nameGetter.invoke(obj);
                    if (objT instanceof String) {
                        title = (String) obj;
                    
                    } else if (objT instanceof AbstractSimpleLiteral) {
                        titleSL = (AbstractSimpleLiteral) objT;
                        if (titleSL.getContent().size() > 0)
                            title = titleSL.getContent().get(0);
                        else title = "unknow title";
                    
                    } else {
                        title = "unknow title";
                    }
                    
                    if (title == null)
                        title = "unknow title";
                } catch (IllegalAccessException ex) {
                    LOGGER.severe("illegal access for method " + methodName + " in " + obj.getClass().getSimpleName() + '\n' + 
                                  "cause: " + ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    LOGGER.severe("illegal argument for method " + methodName + " in " + obj.getClass().getSimpleName()  +'\n' +
                                  "cause: " + ex.getMessage());
                } catch (InvocationTargetException ex) {
                    LOGGER.severe("invocation target exception for " + methodName + " in " + obj.getClass().getSimpleName() +'\n' +
                                  "cause: " + ex.getMessage());
                }
            }
            
            if (title.equals("unknow title"))
                LOGGER.severe("unknow type: " + obj.getClass().getName() + " unable to find a title");
        }
        return title;
    }
    
    /**
     * Record an object in the metadata database.
     * 
     * @param obj The object to store in the database.
     * @return true if the storage succeed, false else.
     */
    public abstract boolean storeMetadata(Object obj) throws SQLException, WebServiceException;
    
    /**
     * Destoy all the resource and close connection.
     */
    public abstract void destroy();
}
