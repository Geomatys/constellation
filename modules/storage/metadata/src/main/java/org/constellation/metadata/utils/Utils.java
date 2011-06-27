/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import org.constellation.util.ReflectionUtilities;
import org.geotoolkit.util.SimpleInternationalString;
import org.geotoolkit.util.logging.Logging;
import org.opengis.util.InternationalString;

/**
 * Utility methods used in CSW object.
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class Utils {

    /**
     * A debugging logger.
     */
    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata.Utils");

    /**
     * A string constant used when we don't find a title on an object.
     */
    public static final String UNKNOW_TITLE = "unknow title";

    /**
     * A string constant used when we don't find an identifier on an object.
     */
    public static final String UNKNOW_IDENTIFIER = "unknow_identifier";

    private Utils() {}
    
    /**
      * This method try to find a title for this object.
      * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
      * else we try to find a getName(), getTitle(), or getId() method.
      *
      * @param obj the object for which we want a title.
      *
      * @return the founded title or UNKNOW_TITLE
      */
    public static String findTitle(final Object obj) {

        //here we try to get the title
        String title = UNKNOW_TITLE;

        final List<String> paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:title");
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("ISO 19115-2:MI_Metadata:fileIdentifier");
        paths.add("ISO 19115:CI_ResponsibleParty:organisationName");
        paths.add("ISO 19110:FC_FeatureCatalogue:name");
        paths.add("Catalog Web Service:Record:title:content");
        paths.add("Catalog Web Service:Record:identifier:content");
        paths.add("Ebrim v3.0:*:name:localizedString:value");
        paths.add("Ebrim v3.0:*:id");
        paths.add("Ebrim v2.5:*:name:localizedString:value");
        paths.add("Ebrim v2.5:*:id");
        paths.add("SensorML:SensorML:member:process:id");

        for (String path : paths) {
            Object value = ReflectionUtilities.getValuesFromPath(path, obj);
            if (value instanceof String) {
                title = (String) value;
                // we stop when we have found a response
                break;
            } else if (value instanceof Collection) {
                Collection c = (Collection) value;
                Iterator it = c.iterator();
                if (it.hasNext()) {
                    Object cValue = it.next();
                    if (cValue instanceof String) {
                        title = (String) cValue;
                         break;
                    } else if (cValue != null) {
                        title = cValue.toString();
                         break;
                    }
                }
            } else if (value != null) {
                LOGGER.finer("FIND TITLE => unexpected String type: " + value.getClass().getName() + "\ncurrentPath:" + path);
            }
        }
        return title;
    }

    /**
      * This method try to find a standard name for this object.
      * if the object is a ISO19115:Metadata we know where to search,
      *
      * @param obj the object for which we want a title.
      *
      * @return the founded standard name or {@code null}
      */
    public static String findStandardName(final Object obj) {

        //here we try to get the title
        String standardName = null;

        final List<String> paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:metadataStandardName");
        
        for (String path : paths) {
            Object value = ReflectionUtilities.getValuesFromPath(path, obj);
            if (value instanceof String) {
                standardName = (String) value;
                // we stop when we have found a response
                break;
            } else if (value != null){
                LOGGER.finer("FIND Standard name => unexpected String type: " + value.getClass().getName() + "\ncurrentPath:" + path);
            }
        }
        return standardName;
    }


    /**
     * This method try to find an identifier for this object.
     *
     * @param obj the object for which we want a identifier.
     *
     * @return the founded identifier or UNKNOW_IDENTIFIER
     */
    public static String findIdentifier(final Object obj) {

        String identifier = UNKNOW_IDENTIFIER;
        
        final List<String> paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("ISO 19115-2:MI_Metadata:fileIdentifier");
        paths.add("ISO 19115:CI_ResponsibleParty:organisationName");
        paths.add("Catalog Web Service:Record:identifier:content");
        paths.add("Ebrim v3.0:*:id");
        paths.add("Ebrim v2.5:*:id"); 
        paths.add("ISO 19110:FC_FeatureCatalogue:id");
        paths.add("SensorML:SensorML:member:process:id");

        for (String path : paths) {
            Object value = ReflectionUtilities.getValuesFromPath(path, obj);
            // we stop when we have found a response
            if (value instanceof String) {
                identifier = (String) value;
                break;
            } if (value instanceof InternationalString) {
                identifier = value.toString();
                break;
            } else if (value instanceof Collection) {
                Collection c = (Collection) value;
                Iterator it = c.iterator();
                if (it.hasNext()) {
                    Object cValue = it.next();
                    if (cValue instanceof String) {
                        identifier = (String) cValue;
                         break;
                    } else if (cValue != null) {
                        identifier = cValue.toString();
                         break;
                    }
                }
            } else if (value != null) {
                LOGGER.finer("FIND IDENTIFIER => unexpected String type: " + value.getClass().getName() + "\ncurrentPath:" + path);
            }
        }
        return identifier;
    }

    /**
     * This method try to set an identifier for this object.
     *
     * @param obj the object for which we want a identifier.
     *
     * @return the founded identifier or UNKNOW_IDENTIFIER
     */
    public static void setIdentifier(final String identifier, final Object object) {

        final List<String> paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("ISO 19115-2:MI_Metadata:fileIdentifier");
        paths.add("ISO 19115:CI_ResponsibleParty:organisationName");
        paths.add("Catalog Web Service:Record:identifier:content");
        paths.add("Ebrim v3.0:*:id");
        paths.add("Ebrim v2.5:*:id");
        paths.add("ISO 19110:FC_FeatureCatalogue:id");
        paths.add("SensorML:SensorML:member:process:id");

        for (String pathID : paths) {

            if (ReflectionUtilities.pathMatchObjectType(object, pathID)) {
                Object currentObject = object;
                /*
                 * we remove the prefix path part the path always start with STANDARD:TYPE:
                 */
                pathID = pathID.substring(pathID.indexOf(':') + 1);
                pathID = pathID.substring(pathID.indexOf(':') + 1);
                while (!pathID.isEmpty()) {

                    //we extract the current attributeName
                    String attributeName;
                    if (pathID.indexOf(':') != -1) {
                        attributeName = pathID.substring(0, pathID.indexOf(':'));
                        pathID = pathID.substring(pathID.indexOf(':') + 1);
                    } else {
                        attributeName = pathID;
                        pathID = "";
                    }

                    if (!pathID.isEmpty()) {
                        // we get the temporary object when navigating through the object.
                        Object temp = currentObject;
                        Method getter = ReflectionUtilities.getGetterFromName(attributeName, currentObject.getClass());
                        if (getter != null) {
                            currentObject = ReflectionUtilities.invokeMethod(currentObject, getter);
                            // if the object is not yet instantiate, we build it
                            if (currentObject == null) {
                                currentObject = ReflectionUtilities.newInstance(getter.getReturnType());
                                // if the build succeed we set it to the global previous object
                                if (currentObject != null) {
                                    Method setter = ReflectionUtilities.getSetterFromName(attributeName, currentObject.getClass(), temp.getClass());
                                    if (setter != null) {
                                        ReflectionUtilities.invokeMethod(setter, temp, currentObject);
                                    }
                                } 
                            } else if (currentObject instanceof Collection) {
                                if (((Collection) currentObject).size() > 0) {
                                    currentObject = ((Collection) currentObject).iterator().next();
                                    if (currentObject instanceof JAXBElement) {
                                        currentObject = ((JAXBElement)currentObject).getValue();
                                    }
                                } else {
                                    LOGGER.warning("TODO collection is empty");
                                }
                            } else if (currentObject instanceof JAXBElement) {
                                currentObject = ((JAXBElement)currentObject).getValue();
                            }
                        }

                    /* 
                     * when we are at the end of the path we call the set Method.
                     *  TODO make it more generic
                     */  
                    } else {
                        Class parameterClass;
                        if (attributeName.equals("content")) {
                            parameterClass = List.class;
                        } else if (attributeName.equals("organisationName")) {
                            parameterClass = InternationalString.class;
                        } else {
                            parameterClass = String.class;
                        }
                        Method setter = ReflectionUtilities.getSetterFromName(attributeName, parameterClass, currentObject.getClass());
                        if (setter != null) {
                            // if the parameter is a string collection
                            if (parameterClass.equals(List.class)) {
                                ReflectionUtilities.invokeMethod(setter, currentObject, Arrays.asList(identifier));
                            } else if (parameterClass.equals(InternationalString.class)){
                                ReflectionUtilities.invokeMethod(setter, currentObject, new SimpleInternationalString(identifier));
                            } else {
                                ReflectionUtilities.invokeMethod(setter, currentObject, identifier);
                            }
                        }
                    }
                }
            }
        }
        
    }
}
