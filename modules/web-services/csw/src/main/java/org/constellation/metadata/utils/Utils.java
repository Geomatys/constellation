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

package org.constellation.metadata.utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

// Constellation dependencies
import org.constellation.util.ReflectionUtilities;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.Record;
import org.geotoolkit.dublincore.xml.AbstractSimpleLiteral;
import org.geotoolkit.ebrim.xml.EbrimInternationalString;
import org.geotoolkit.ebrim.xml.RegistryObject;
import org.geotoolkit.metadata.iso.DefaultMetadata;

// GeoAPI dependencies
import org.opengis.metadata.identification.Identification;

/**
 * Utility methods used in CSW object.
 *
 * this classe will be moved to store-metadata when there is no more dependencies to geotk module
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class Utils {

    /**
     * A debugging logger.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata.utils");

    /**
     * A string constant used when we don't find an identifier on an object.
     */
    private static final String UNKNOW_IDENTIFIER = "unknow_identifier";

    /**
     * A string constant used when we don't find an identifier on an object.
     */
    private static final String UNKNOW_TITLE = "unknow title";

    /**
     * This method try to find an Identifier for this object.
     *  we try to find a getId(), getIdentifier() or getFileIdentifier() method.
     *
     * @param obj the object for wich we want a identifier.
     *
     * @return the founded identifier or UNKNOW_IDENTIFIER
     */
    public static String findIdentifier(Object obj) {
        
        /*
         * we try to find one of the thre method named :
         *  - getId()
         *  - getIdentifier()
         *  - getFileIdentifier()
         */
        
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
                LOGGER.warning(" security exception while getting the identifier of the object.");
            }

            // if we have find a method we exit the loop.
            if (nameGetter != null) {
                i = 3;
            } else {
                i++;
            }
        }

        if (nameGetter != null) {
            final Object objT = ReflectionUtilities.invokeMethod(obj, nameGetter);
            if (objT instanceof String) {
                return (String) objT;

            } else if (objT != null) {
                return objT.toString();
            }
        }
        
        LOGGER.log(Level.WARNING, "unable to find an identifier in object of type class {0}, using default then.", obj.getClass().getName());
        return UNKNOW_IDENTIFIER;
    }

    /**
     * TODO: This method will replace MetadatWriter.findTitle(..) soon
     *
     * This method try to find a title for this object.
     * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
     * else we try to find a getName(), getTitle(), or getId() method.
     *
     * @param obj the object for which we want a title.
     *
     * @return the founded title or UNKNOW_TITLE
     */
    public static String findTitle(Object obj) {

        if (obj != null) {
            // for a dublincore record
            if (obj instanceof Record) {
                AbstractSimpleLiteral titleSL = ((Record) obj).getTitle();
                if (titleSL != null && !titleSL.getContent().isEmpty()) {
                    return  titleSL.getContent().get(0);
                }

                titleSL = ((Record) obj).getIdentifier();
                if (titleSL != null && !titleSL.getContent().isEmpty()) {
                    return titleSL.getContent().get(0);
                }


            // for an iso metadata
            } else if (obj instanceof DefaultMetadata) {
                final Collection<Identification> idents = ((DefaultMetadata) obj).getIdentificationInfo();
                if (!idents.isEmpty()) {
                    final Identification ident = idents.iterator().next();
                    if (ident != null && ident.getCitation() != null && ident.getCitation().getTitle() != null) {
                        return ident.getCitation().getTitle().toString();
                    }
                }

            // for a ebrim object
            } else if (obj instanceof RegistryObject) {
                final EbrimInternationalString ident = ((RegistryObject) obj).getName();
                final String id                      = ((RegistryObject) obj).getId() ;
                if (ident != null && !ident.getLocalizedString().isEmpty()) {
                    return ident.getLocalizedString().get(0).getValue();
                } else if (id != null){
                    return id;
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
                        LOGGER.finer("There is no " + methodName + " method in " + obj.getClass().getSimpleName());
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
                    final Object objT = ReflectionUtilities.invokeMethod(obj, nameGetter);
                    if (objT instanceof String) {
                        return (String) objT;

                    } else if (objT != null) {
                        objT.toString();
                    }
                }
            }
            LOGGER.warning("unknow type: " + obj.getClass().getName() + " unable to find a title, using default then.");
        }
        return UNKNOW_TITLE;
    }

}
