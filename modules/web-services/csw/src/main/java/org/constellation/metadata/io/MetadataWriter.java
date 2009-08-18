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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

// constellation dependencies
import org.geotoolkit.csw.xml.Record;
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.dublincore.xml.AbstractSimpleLiteral;
import org.geotoolkit.ebrim.xml.EbrimInternationalString;
import org.geotoolkit.ebrim.xml.RegistryObject;
import org.constellation.ws.CstlServiceException;

//geotools dependencies
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.metadata.iso.DefaultMetaData;

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
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata");
    
    /**
     * Record the date format in the metadata.
     */
    protected final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * An indexer lucene to add object into the index.
     */
    protected final AbstractIndexer indexer;

    private static final String UNKNOW_TITLE = "unknow title";

    private static final String UNKNOW_IDENTIFIER = "unknow_identifier";

    /**
     * Build a new metadata writer.
     * 
     * @param MDReader an MDWeb database reader.
     */
    public MetadataWriter(AbstractIndexer indexer) throws CstlServiceException {
        this.indexer        = indexer;
    }

    /**
     * This method try to find a title to this object.
     * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
     * else we try to find a getName() method.
     * 
     * @param obj the object for wich we want a title
     * 
     * @return the founded title or UNKNOW_TITLE
     */
    protected String findTitle(Object obj) {
        
        //here we try to get the title
        AbstractSimpleLiteral titleSL = null;
        String title = UNKNOW_TITLE;
        if (obj instanceof Record) {
            titleSL = ((Record) obj).getTitle();
            if (titleSL == null) {
                titleSL = ((Record) obj).getIdentifier();
            }
                               
            if (titleSL == null) {
                title = UNKNOW_TITLE;
            } else {
                if (titleSL.getContent().size() > 0)
                    title = titleSL.getContent().get(0);
            }
                            
        } else if (obj instanceof DefaultMetaData) {
            final Collection<Identification> idents = ((DefaultMetaData) obj).getIdentificationInfo();
            if (idents.size() != 0) {
                final Identification ident = idents.iterator().next();
                if (ident != null && ident.getCitation() != null && ident.getCitation().getTitle() != null) {
                    title = ident.getCitation().getTitle().toString();
                } 
            }
        } else if (obj instanceof RegistryObject) {
            final EbrimInternationalString ident = ((RegistryObject) obj).getName();
            if (ident != null && ident.getLocalizedString().size() > 0) {
                title = ident.getLocalizedString().get(0).getValue();
            } else {
                title = ((RegistryObject) obj).getId();
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
                        default: break;
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
                    final Object objT = nameGetter.invoke(obj);
                    if (objT instanceof String) {
                        title = (String) obj;
                    
                    } else if (objT instanceof AbstractSimpleLiteral) {
                        titleSL = (AbstractSimpleLiteral) objT;
                        if (titleSL.getContent().size() > 0)
                            title = titleSL.getContent().get(0);
                        else title = UNKNOW_TITLE;
                    
                    } else {
                        title = UNKNOW_TITLE;
                    }
                    
                    if (title == null)
                        title = UNKNOW_TITLE;
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
            
            if (title.equals(UNKNOW_TITLE))
                LOGGER.severe("unknow type: " + obj.getClass().getName() + " unable to find a title");
        }
        return title;
    }


    /**
     * This method try to find a title to this object.
     * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
     * else we try to find a getName() method.
     *
     * @param obj the object for wich we want a title
     *
     * @return the founded title or UNKNOW_TITLE
     */
    protected String findIdentifier(Object obj) {

        //here we try to get the identifier
        AbstractSimpleLiteral identifierSL = null;
        String identifier = UNKNOW_IDENTIFIER;
        if (obj instanceof Record) {
            identifierSL = ((Record) obj).getIdentifier();
            
            if (identifierSL == null) {
                identifier = UNKNOW_IDENTIFIER;
            } else {
                if (identifierSL.getContent().size() > 0)
                    identifier = identifierSL.getContent().get(0);
            }

        } else if (obj instanceof DefaultMetaData) {
            identifier = ((DefaultMetaData) obj).getFileIdentifier();

        } else if (obj instanceof RegistryObject) {
            identifier = ((RegistryObject) obj).getId();

        } else {
            Method nameGetter = null;
            String methodName = "";
            int i = 0;
            while (i < 3) {
                try {
                    switch (i) {
                        case 0: methodName = "getId";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;

                        case 1: methodName = "getIdentifier";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;

                        case 2: methodName = "getFileIdentifier";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                        default: break;
                    }


                } catch (NoSuchMethodException ex) {
                    LOGGER.finer("there is no " + methodName + " method in " + obj.getClass().getSimpleName());
                } catch (SecurityException ex) {
                    LOGGER.severe(" security exception while getting the identifier of the object.");
                }
                if (nameGetter != null) {
                    i = 3;
                } else {
                    i++;
                }
            }

            if (nameGetter != null) {
                try {
                    final Object objT = nameGetter.invoke(obj);
                    if (objT instanceof String) {
                        identifier = (String) obj;

                    } else if (objT instanceof AbstractSimpleLiteral) {
                        identifierSL = (AbstractSimpleLiteral) objT;
                        if (identifierSL.getContent().size() > 0)
                            identifier = identifierSL.getContent().get(0);
                        else identifier = UNKNOW_IDENTIFIER;

                    } else {
                        identifier = UNKNOW_IDENTIFIER;
                    }

                    if (identifier == null)
                        identifier = UNKNOW_IDENTIFIER;
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

            if (identifier.equals(UNKNOW_IDENTIFIER))
                LOGGER.severe("unknow type: " + obj.getClass().getName() + " unable to find an identifier");
        }
        return identifier;
    }
    
    /**
     * Record an object in the metadata database.
     * 
     * @param obj The object to store in the database.
     * @return true if the storage succeed, false else.
     */
    public abstract boolean storeMetadata(Object obj) throws CstlServiceException;

    /**
     * Delete an object in the metadata database.
     * @param metadataID The identifier of the metadata to delete.
     */
    public abstract boolean deleteMetadata(String metadataID) throws CstlServiceException;


    /**
     * Replace an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param any The object to replace the matching metadata.
     */
    public abstract boolean replaceMetadata(String metadataID, Object any) throws CstlServiceException;

    /**
     * Update an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param properties A List of property-value to replace in the specified metadata.
     */
    public abstract boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws CstlServiceException;

    /**
     * Return true if the Writer supports the delete mecanism.
     */
    public abstract boolean deleteSupported();

    /**
     * Return true if the Writer supports the update mecanism.
     */
    public abstract boolean updateSupported();

    
    /**
     * Destoy all the resource and close connection.
     */
    public void destroy() {
        if (indexer != null)
            indexer.destroy();
    }
}
