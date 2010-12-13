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

package org.constellation.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.logging.Logging;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class ReflectionUtilities {

    private static final Logger LOGGER = Logging.getLogger(ReflectionUtilities.class);

    private static final String INCLASS = " in the class ";

    /**
     * A map of getters to avoid to seach the same getters many times.
     */
    private static final Map<String, Method> GETTERS = new HashMap<String, Method>();

    private ReflectionUtilities() {}

    /**
     * Call the empty constructor on the specified class and return the result,
     * handling most errors which could be expected by logging the cause and
     * returning {@code null}.
     *
     * @param classe An arbitrary class, expected to be instantiable with a
     *                 {@code null} argument constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe) {
        try {
            if (classe == null)
                return null;

            final Constructor<?> constructor = classe.getDeclaredConstructor();
            constructor.setAccessible(true);

            //we execute the constructor
            return constructor.newInstance();

        } catch (InstantiationException ex) {
            LOGGER.warning("Unable to instantiate the class: " + classe.getName() + "()");
        } catch (IllegalAccessException ex) {
            LOGGER.warning("Unable to access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {//TODO: this cannot possibly happen.
            LOGGER.warning("Illegal Argument in empty constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            LOGGER.warning("Invocation Target Exception in empty constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("There is no empty constructor for class: " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }

    /**
     * Call the constructor which uses a {@code String} parameter for the given
     * class and return the result, handling most errors which could be expected
     * by logging the cause and returning {@code null}.
     *
     * @param classe    An arbitrary class, expected to be instantiable with a
     *                    single {@code String} argument.
     * @param parameter The {@code String} to use as an argument to the
     *                    constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe, final String parameter) {
        try {
            if (classe == null)
                return null;
            final Constructor<?> constructor = classe.getConstructor(String.class);

            //we execute the constructor
            return constructor.newInstance(parameter);

        } catch (InstantiationException ex) {
            LOGGER.warning("Unable to instantiate the class: " + classe.getName() + "(string)");
        } catch (IllegalAccessException ex) {
            LOGGER.warning("Unable to access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            LOGGER.warning("Illegal Argument in string constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            LOGGER.warning("Invocation target exception in string constructor for class: " + classe.getName() + " for parameter: " + parameter);
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("No single string constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }

    /**
     * Call the constructor which uses two {@code String} parameters for the
     * given class and return the result, handling most errors which could be
     * expected by logging the cause and returning {@code null}.
     *
     * @param classe     An arbitrary class, expected to be instantiable with a
     *                     single {@code String} argument.
     * @param parameter1 The first {@code String} to use as an argument to the
     *                     constructor.
     * @param parameter2 The second {@code String} to use as an argument to the
     *                     constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe, final String parameter1, final String parameter2) {
        try {
            if (classe == null)
                return null;
            final Constructor<?> constructor = classe.getConstructor(String.class, String.class);

            //we execute the constructor
            return constructor.newInstance(parameter1, parameter2);

        } catch (InstantiationException ex) {
            LOGGER.warning("The service can't instantiate the class: " + classe.getName() + "(string, string)");
        } catch (IllegalAccessException ex) {
            LOGGER.warning("The service can not access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            LOGGER.warning("Illegal Argument in double string constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            LOGGER.warning("Invocation target exception in double string constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("No double string constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }

    /**
     * Call the constructor which uses a character sequence parameter for the
     * given class and return the result, handling most errors which could be
     * expected by logging the cause and returning {@code null}.
     *
     * @param classe    An arbitrary class, expected to be instantiable with a
     *                    single {@code String} argument.
     * @param parameter The character sequence to use as an argument to the
     *                    constructor.
     * @return The instantiated instance of the given class, or {@code null}.
     */
    public static Object newInstance(final Class<?> classe, final CharSequence parameter) {
        try {
            if (classe == null)
                return null;
            final Constructor<?> constructor = classe.getConstructor(CharSequence.class);

            //we execute the constructor
            return constructor.newInstance(parameter);

        } catch (InstantiationException ex) {
            LOGGER.warning("The service can't instantiate the class: " + classe.getName() + "(CharSequence)");
        } catch (IllegalAccessException ex) {
            LOGGER.warning("The service can not access the constructor in class: " + classe.getName());
        } catch (IllegalArgumentException ex) {
            LOGGER.warning("Illegal Argument in CharSequence constructor for class: " + classe.getName());
        } catch (InvocationTargetException ex) {
            LOGGER.warning("Invocation target exception in CharSequence constructor for class: " + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("No such CharSequence constructor in class: " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while instantiating class: " + classe.getName());
        }
        return null;
    }

    /**
     * Invoke the given method on the specified object, handling most errors
     * which could be expected by logging the cause and returning {@code null}.
     *
     * <p>
     * TODO: what happens if the method returns {@code void}?
     * </p>
     *
     * @param object     The object on which the method will be invoked.
     * @param method     The method to invoke.
     * @return The {@code Object} generated by the method call, or, if the
     *           method call resulted in a primitive, the auto-boxing
     *           equivalent, or {@code null}.
     */
    public static Object invokeMethod(final Object object, final Method method) {
        Object result = null;
        final String baseMessage = "Unable to invoke the method " + method + ": ";
        try {
            if (method != null) {
                result = method.invoke(object);
            } else {
                LOGGER.warning(baseMessage + "the method reference is null.");
            }

        } catch (IllegalAccessException ex) {
            LOGGER.warning(baseMessage + "the class is not accessible.");

        } catch (IllegalArgumentException ex) {//TODO: this cannot happen
            LOGGER.warning(baseMessage + "the argument does not match with the method.");

        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.WARNING, baseMessage + "an Exception was thrown by the invoked method.", ex);
        }
        return result;
    }

    /**
     * Invoke a method with the specified parameter on the specified object,
     * handling most errors which could be expected by logging the cause and
     * returning {@code null}.
     *
     * <p>
     * TODO: what happens if the method returns {@code void}?
     * </p>
     *
     * @param object     The object on which the method will be invoked.
     * @param method     The method to invoke.
     * @param parameter  The parameter of the method.
     * @return The {@code Object} generated by the method call, or, if the
     *           method call resulted in a primitive, the auto-boxing
     *           equivalent, or {@code null}.
     */
    public static Object invokeMethod(final Method method, final Object object, final Object... parameter) {
        Object result = null;
        final String baseMessage = "Unable to invoke the method " + method + ": ";
        try {
            if (method != null) {
                if (parameter.length == 1) {
                    result = method.invoke(object, parameter[0]);
                } else {
                    result = method.invoke(object, parameter);
                }
            } else {
                LOGGER.warning(baseMessage + "the method reference is null.");
            }
        } catch (IllegalAccessException ex) {
            LOGGER.warning(baseMessage + "the class is not accessible.");

        } catch (IllegalArgumentException ex) {
            String param = "null";
            if (parameter != null)
                param = parameter.getClass().getSimpleName();
            LOGGER.warning(baseMessage + "the given argument does not match that required by the method.( argument type was " + param + ")" + '\n' +
                           "cause:" + ex.getMessage());

        } catch (InvocationTargetException ex) {
            String errorMsg = ex.getMessage();
            if (errorMsg == null && ex.getCause() != null) {
                errorMsg = ex.getCause().getMessage();
            }
            if (errorMsg == null && ex.getTargetException() != null) {
                errorMsg = ex.getTargetException().getMessage();
            }
            LOGGER.log(Level.WARNING, baseMessage + "an Exception was thrown in the invoked method:" + errorMsg, ex);
        }
        return result;
    }

    /**
     * Invoke a method with the specified parameter on the specified object,
     * the errors are throws (not like the method invokeMethod(...).
     *
     *
     * @param object     The object on which the method will be invoked.
     * @param method     The method to invoke.
     * @param parameter  The parameter of the method.
     * @return The {@code Object} generated by the method call, or, if the
     *           method call resulted in a primitive, the auto-boxing
     *           equivalent, or {@code null}.
     */
    public static Object invokeMethodEx(final Method method, final Object object, final Object parameter) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object result = null;
        if (method != null) {
            result = method.invoke(object, parameter);
        } else {
            LOGGER.warning("Unable to invoke the method reference is null.");
        }
        return result;
    }

    /**
     * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     *
     * @param propertyName The attribute name.
     * @param classe       The attribute type.
     * @param rootClass    The class whitch owe this attribute
     *
     * @return a setter to this attribute or {@code null}.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName);

        } catch (IllegalArgumentException ex) {
            LOGGER.warning("illegal argument exception while invoking the method " + propertyName + INCLASS + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while getting the method " + propertyName + INCLASS + classe.getName());
        }
        return method;
    }

    /**
      * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     *
     * @param propertyName The attribute name.
     * @param classe       The attribute type.
     * @param rootClass    The class whitch owe this attribute
     *
     * @return a setter to this attribute or {@code null}.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe, final Class<?> parameterClass) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName, parameterClass);

        } catch (IllegalArgumentException ex) {
            LOGGER.warning("illegal argument exception while invoking the method " + propertyName + INCLASS + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while getting the method " + propertyName + INCLASS + classe.getName());
        }
        return method;
    }
    /**
      * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     *
     * @param propertyName The attribute name.
     * @param classe       The attribute type.
     * @param rootClass    The class whitch owe this attribute
     *
     * @return a setter to this attribute or {@code null}.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe, final Class<?>... parameterClass) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName, parameterClass);

        } catch (IllegalArgumentException ex) {
            LOGGER.warning("illegal argument exception while invoking the method " + propertyName + INCLASS + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while getting the method " + propertyName + INCLASS + classe.getName());
        }
        return method;
    }

    /**
     * Return a getter Method for the specified attribute (propertyName)
     *
     * @param propertyName The attribute name.
     * @param rootClass    The class whitch owe this attribute
     *
     * @return a setter to this attribute or {@code null}.
     */
    public static Method getGetterFromName(String propertyName, final Class<?> rootClass) {

        final String rootClassName = rootClass.getName();
        //special case and corrections TODO remove
        if ("beginPosition".equals(propertyName) && !"org.geotoolkit.gml.xml.v311.TimePeriodType".equals(rootClassName)) {
            if ("org.geotoolkit.temporal.object.DefaultInstant".equals(rootClassName))
                return null;
            else
                propertyName = "beginning";
        } else if ("endPosition".equals(propertyName)  && !"org.geotoolkit.gml.xml.v311.TimePeriodType".equals(rootClassName)) {
            if ("org.geotoolkit.temporal.object.DefaultInstant".equals(rootClassName))
                return null;
            else
                propertyName = "ending";
        } else if ("recordType".equals(propertyName) && "DefaultRecordType".equals(rootClass.getSimpleName())) {
            return null;
        } else if ("dataSetURI".equals(propertyName)) {
            propertyName = "dataSetUri";
        } else if ("nameOfMeasure".equals(propertyName)) {
            propertyName = "namesOfMeasure";
        } else if ("dateTime".equals(propertyName) && rootClassName.startsWith("org.geotoolkit.metadata.iso.quality")) {
             propertyName = "dates";
        } else if ("extentTypeCode".equals(propertyName)) {
            propertyName = "inclusion";
        // TODO remove when this issue will be fix in MDWeb
        } else if (propertyName.indexOf("geographicElement") != -1) {
            propertyName = "geographicElement";

        // avoid unnecesary log flood
        } else if ("org.geotoolkit.metadata.iso.extent.DefaultGeographicDescription".equals(rootClassName) &&
                  ("westBoundLongitude".equals(propertyName) || "eastBoundLongitude".equals(propertyName) ||
                   "northBoundLatitude".equals(propertyName) || "southBoundLatitude".equals(propertyName))) {
            return null;
        // avoid unnecesary log flood
        } else if (("accessConstraints".equals(propertyName))
                   && "org.geotoolkit.metadata.iso.constraint.DefaultConstraints".equals(rootClassName)) {
            return null;
        // avoid unnecesary log flood
        } else if ("org.geotoolkit.service.ServiceIdentificationImpl".equals(rootClassName) &&
                  ("spatialResolution".equals(propertyName) || "language".equals(propertyName) ||
                   "topicCategory".equals(propertyName))) {
            return null;
        } else if ("geographicIdentifier".equals(propertyName) && "org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox".equals(rootClassName)) {
            return null;
        } else if ("position".equals(propertyName) && ("org.geotoolkit.temporal.object.DefaultPeriod".equals(rootClassName))) {
            return null;
        }  else if (("calendarEraName".equals(propertyName) || "frame".equals(propertyName) || "indeterminatePosition".equals(propertyName))
                && ("org.geotoolkit.temporal.object.DefaultPosition".equals(rootClassName))) {
            return null;
        } else if ("value".equals(propertyName) && ("org.geotoolkit.temporal.object.DefaultPosition".equals(rootClassName))) {
            propertyName = "date";
        }

        final String methodName  = "get" + StringUtilities.firstToUpper(propertyName);
        final String methodName2 = "is" + StringUtilities.firstToUpper(propertyName);
        final String methodName3 = propertyName;
        int occurenceType = 0;

        while (occurenceType < 7) {

            try {
                Method getter = null;
                switch (occurenceType) {

                    case 0: {
                        getter = rootClass.getMethod(methodName);
                        break;
                    }
                    case 1: {
                        getter = rootClass.getMethod(methodName + "s");
                        break;
                    }
                    case 2: {
                        getter = rootClass.getMethod(methodName + "es");
                        break;
                    }
                    case 3: {
                        String temp = methodName;
                        if (methodName.endsWith("y")) {
                            temp = methodName.substring(0, methodName.length() - 1) + 'i';
                        }
                        getter = rootClass.getMethod(temp + "es");
                        break;
                    }
                    case 4: {
                        getter = rootClass.getMethod(methodName2);
                        break;
                    }
                    case 5: {
                        getter = rootClass.getMethod(methodName3);
                        break;
                    }
                    case 6: {
                        String temp = methodName2;
                        if (methodName2.endsWith("Availability")) {
                            temp = methodName2.substring(0, methodName2.length() - 12) + "Available";
                        }
                        getter = rootClass.getMethod(temp);
                        break;
                    }
                    default: break;

                }
                return getter;

            } catch (NoSuchMethodException e) {
                occurenceType++;
            }
        }
        return null;
    }

    /**
     * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     *
     * @param propertyName The attribute name.
     * @param classe       The attribute type.
     * @param rootClass    The class whitch owe this attribute
     *
     * @return a setter to this attribute or {@code null}.
     */
    public static Method getSetterFromName(String propertyName, final Class<?> paramClass, final Class<?> rootClass) {
        LOGGER.finer("search for a setter in " + rootClass.getName() + " of type :" + paramClass.getName());

        final String methodName = "set" + StringUtilities.firstToUpper(propertyName);
        int occurenceType = 0;

        //TODO look all interfaces
        Class<?> interfacee = null;
        if (paramClass.getInterfaces().length != 0) {
            interfacee = paramClass.getInterfaces()[0];
        }

        Class<?> argumentSuperClass     = paramClass;
        Class<?> argumentSuperInterface = null;
        if (argumentSuperClass.getInterfaces().length > 0) {
            argumentSuperInterface = argumentSuperClass.getInterfaces()[0];
        }


        while (occurenceType < 7) {

            try {
                Method setter = null;
                switch (occurenceType) {

                    case 0: {
                        setter = rootClass.getMethod(methodName, paramClass);
                        break;
                    }
                    case 1: {
                        if (paramClass.equals(Integer.class)) {
                            setter = rootClass.getMethod(methodName, long.class);
                            break;
                        } else {
                            occurenceType = 2;
                        }
                    }
                    case 2: {
                        setter = rootClass.getMethod(methodName, interfacee);
                        break;
                    }
                    case 3: {
                        setter = rootClass.getMethod(methodName, Collection.class);
                        break;
                    }
                    case 4: {
                        setter = rootClass.getMethod(methodName + "s", Collection.class);
                        break;
                    }
                    case 5: {
                        setter = rootClass.getMethod(methodName , argumentSuperClass);
                        break;
                    }
                    case 6: {
                        setter = rootClass.getMethod(methodName , argumentSuperInterface);
                        break;
                    }
                    default: break;
                }
                return setter;

            } catch (NoSuchMethodException e) {

                final String msg = "The setter ";
                /**
                 * This switch is for debugging purpose
                 */
                switch (occurenceType) {

                    case 0: {
                        LOGGER.finer(msg + methodName + "(" + paramClass.getName() + ") does not exist");
                        occurenceType = 1;
                        break;
                    }
                    case 1: {
                        LOGGER.finer(msg + methodName + "(long) does not exist");
                        occurenceType = 2;
                        break;
                    }
                    case 2: {
                        if (interfacee != null) {
                            LOGGER.finer(msg + methodName + "(" + interfacee.getName() + ") does not exist");
                        }
                        occurenceType = 3;
                        break;
                    }
                    case 3: {
                        LOGGER.finer(msg + methodName + "(Collection<" + paramClass.getName() + ">) does not exist");
                        occurenceType = 4;
                        break;
                    }
                    case 4: {
                        LOGGER.finer(msg + methodName + "s(Collection<" + paramClass.getName() + ">) does not exist");
                        occurenceType = 5;
                        break;
                    }
                    case 5: {
                        if (argumentSuperClass != null) {
                            LOGGER.finer(msg + methodName + "(" + argumentSuperClass.getName() + ") does not exist");
                            argumentSuperClass     = argumentSuperClass.getSuperclass();
                            occurenceType = 5;

                        } else {
                            occurenceType = 6;
                        }
                        break;
                    }
                    case 6: {
                        if (argumentSuperInterface != null) {
                            LOGGER.finer(msg + methodName + "(" + argumentSuperInterface.getName() + ") does not exist");
                        }
                        occurenceType = 7;
                        break;
                    }
                    default:
                        occurenceType = 7;
                }
            }
        }
        return null;
    }

    /**
     * Return a java field from the specified class(or any of its super class)
     * or {@code null} if it does not exist.
     *
     * @param attribName
     * @param classe
     * @return
     */
    public static Field getFieldFromName(String attribName, Class classe) {
        Field field      = null;
        Class tempClasse = classe;
        while (field == null && tempClasse != null) {
            try {
                field = tempClasse.getDeclaredField(attribName);
            } catch (NoSuchFieldException ex) {
                field = null;
            }
            tempClasse = tempClasse.getSuperclass();
        }
        return field;
    }

    /**
     * Return an object value extract from the specified object by using the string path specified.
     * example : getValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:title", (MetatadataImpl) obj)
     *           will execute obj.getIdentificationInfo().getTitle() and return the result.
     *
     * @param pathID   A String path using MDWeb pattern.
     * @param metadata An Object.
     * @return A Object value.
     */
    public static Object getValuesFromPath(String pathID, Object metadata) {
        Object result = null;
        if (pathMatchObjectType(metadata, pathID)) {

            /*
             * we remove the prefix path part the path always start with STANDARD:TYPE:
             */
            pathID = pathID.substring(pathID.indexOf(':') + 1);
            pathID = pathID.substring(pathID.indexOf(':') + 1);

            //for each part of the path we execute a (many) getter
            while (!pathID.isEmpty()) {
                String attributeName;
                if (pathID.indexOf(':') != -1) {
                    attributeName = pathID.substring(0, pathID.indexOf(':'));
                    pathID        = pathID.substring(pathID.indexOf(':') + 1);
                } else {
                    attributeName = pathID;
                    pathID = "";
                }

                if (metadata instanceof Collection) {
                    final List<Object> tmp = new ArrayList<Object>();
                    for (Object subMeta: (Collection) metadata) {
                        final Object obj = getAttributeValue(subMeta, attributeName);
                        if (obj instanceof Collection) {
                            for (Object o : (Collection)obj) {
                                if (o != null) tmp.add(o);
                            }
                        } else {
                            if (obj != null) tmp.add(obj);
                        }
                    }
                    metadata = tmp;
                } else {
                    metadata = getAttributeValue(metadata, attributeName);
                }
            }
            result = metadata;
        }
        return result;
    }

    /**
     * Return true if the path is applyable to the specified metadata type.
     *
     * @param metadata A metadata object.
     * @param pathID A path on the form Standard:Type:attribute1:attribute2
     *
     * @return True if the specified path starts with the type of the metadata
     */
    public static boolean pathMatchObjectType(Object metadata, String pathID) {
        if (metadata == null) return false;

        return (pathID.startsWith("ISO 19115:MD_Metadata")      && "DefaultMetadata".equals(metadata.getClass().getSimpleName())) ||
               (pathID.startsWith("Catalog Web Service:Record") && "RecordType".equals(metadata.getClass().getSimpleName())) ||
               (pathID.startsWith("Ebrim v2.5:ExtrinsicObject") && "org.geotoolkit.ebrim.xml.v250.ExtrinsicObjectType".equals(metadata.getClass().getName())||
               (pathID.startsWith("Ebrim v2.5:RegistryObject")  && "org.geotoolkit.ebrim.xml.v250.RegistryObjectType".equals(metadata.getClass().getName()))||
               (pathID.startsWith("Ebrim v3.0:RegistryObject")  && "org.geotoolkit.ebrim.xml.v300.RegistryObjectType".equals(metadata.getClass().getName())));
    }

     /**
     * Call a get method on the specified object named get'AttributeName'() and return the result.
     * This method handle the attributeName on the form "attributeName[i]" when you want a specific values in a collection.
      *
     * @param object An object.
     * @param attributeName The name of the attribute that you want the value.
     * @return
     */
    public static Object getAttributeValue(Object object, String attributeName) {
        Object result = null;
        int ordinal   = -1;
        if (attributeName.indexOf('[') != -1){
            final String tmp = attributeName.substring(attributeName.indexOf('[') + 1, attributeName.length() - 1);
            attributeName    = attributeName.substring(0, attributeName.indexOf('['));
            try {
                ordinal = Integer.parseInt(tmp);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Unable to parse the ordinal {0}", tmp);
            }
        }
        if (object != null) {
            if (object instanceof JAXBElement) {
               object = ((JAXBElement)object).getValue();
            }
            final String getterId = object.getClass().getName() + ':' + attributeName;
            Method getter         = GETTERS.get(getterId);
            if (getter != null) {
                result = invokeMethod(object, getter);
            } else {
                if (attributeName.equalsIgnoreCase("referenceSystemIdentifier")) {
                    attributeName = "name";
                }
                getter = getGetterFromName(attributeName, object.getClass());
                if (getter != null) {
                    GETTERS.put(object.getClass().getName() + ':' + attributeName, getter);
                    result = invokeMethod(object, getter);
                } else {
                    LOGGER.finer("No getter have been found for attribute " + attributeName + " in the class " + object.getClass().getName());
                }
            }
        }
        if (result instanceof JAXBElement) {
            result = ((JAXBElement)result).getValue();
        }
        if (ordinal != -1 && result instanceof Collection) {
            final Collection c = (Collection) result;
            final Iterator t   = c.iterator();
            int i = 0;
            while (t.hasNext()) {
                result = t.next();
                if (i == ordinal) return result;
                i++;
            }

        }
        return result;
    }
}
