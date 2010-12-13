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
import java.util.logging.Level;
import java.util.logging.Logger;

// Constellation dependencies
import org.constellation.util.ReflectionUtilities;

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

    private Utils() {}
    
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
}
