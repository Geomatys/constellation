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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ReflectionUtilities {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.util");

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
     *           equivalent, or null.
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
            LOGGER.warning(baseMessage + "an Exception was thrown by the invoked method.");
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
     *           equivalent, or null.
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
            LOGGER.warning(baseMessage + "an Exception was thrown in the invoked method:" + errorMsg);
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
     *           equivalent, or null.
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
     * @return a setter to this attribute.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName);

        } catch (IllegalArgumentException ex) {
            LOGGER.warning("illegal argument exception while invoking the method " + propertyName + " in the classe " + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while getting the method " + propertyName + " in the classe " + classe.getName());
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
     * @return a setter to this attribute.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe, final Class<?> parameterClass) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName, parameterClass);

        } catch (IllegalArgumentException ex) {
            LOGGER.warning("illegal argument exception while invoking the method " + propertyName + " in the classe " + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while getting the method " + propertyName + " in the classe " + classe.getName());
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
     * @return a setter to this attribute.
     */
    public static Method getMethod(final String propertyName, final Class<?> classe, final Class<?>... parameterClass) {
        Method method = null;
        try {
            method = classe.getMethod(propertyName, parameterClass);

        } catch (IllegalArgumentException ex) {
            LOGGER.warning("illegal argument exception while invoking the method " + propertyName + " in the classe " + classe.getName());
        } catch (NoSuchMethodException ex) {
            LOGGER.warning("The method " + propertyName + " does not exists in the classe " + classe.getName());
        } catch (SecurityException ex) {
            LOGGER.warning("Security exception while getting the method " + propertyName + " in the classe " + classe.getName());
        }
        return method;
    }

    /**
     * Return a getter Method for the specified attribute (propertyName)
     *
     * @param propertyName The attribute name.
     * @param rootClass    The class whitch owe this attribute
     *
     * @return a setter to this attribute.
     */
    public static Method getGetterFromName(String propertyName, final Class<?> rootClass) {


        //special case and corrections
        if (propertyName.equals("beginPosition") && !rootClass.getName().equals("org.geotoolkit.gml.xml.v311.TimePeriodType")) {
            if (rootClass.getName().equals("org.geotoolkit.temporal.object.DefaultInstant"))
                return null;
            else
                propertyName = "beginning";
        } else if (propertyName.equals("endPosition")  && !rootClass.getName().equals("org.geotoolkit.gml.xml.v311.TimePeriodType")) {
            if (rootClass.getName().equals("org.geotoolkit.temporal.object.DefaultInstant"))
                return null;
            else
                propertyName = "ending";
        } else if (propertyName.equals("dataSetURI")) {
            propertyName = "dataSetUri";
        } else if (propertyName.equals("extentTypeCode")) {
            propertyName = "inclusion";
        // TODO remove when this issue will be fix in MDWeb
        } else if (propertyName.indexOf("geographicElement") != -1) {
            propertyName = "geographicElement";

        // avoid unnecesary log flood
        } else if ((propertyName.equals("westBoundLongitude") || propertyName.equals("eastBoundLongitude") ||
                   propertyName.equals("northBoundLatitude") || propertyName.equals("southBoundLatitude"))
                   && rootClass.getName().equals("org.geotoolkit.metadata.iso.extent.DefaultGeographicDescription")) {
            return null;
        } else if (propertyName.equals("geographicIdentifier") && rootClass.getName().equals("org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox")) {
            return null;
        } else if (propertyName.equals("position") && (rootClass.getName().equals("org.geotoolkit.temporal.object.DefaultPeriod"))) {
            return null;
        } else if (propertyName.equals("value") && (rootClass.getName().equals("org.geotoolkit.temporal.object.DefaultPosition"))) {
            propertyName = "date";
        }

        String methodName = "get" + StringUtilities.firstToUpper(propertyName);
        String methodName2 = "is" + StringUtilities.firstToUpper(propertyName);
        int occurenceType = 0;

        while (occurenceType < 5) {

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
                        if (methodName.endsWith("y")) {
                            methodName = methodName.substring(0, methodName.length() - 1) + 'i';
                        }
                        getter = rootClass.getMethod(methodName + "es");
                        break;
                    }
                    case 4: {
                        getter = rootClass.getMethod(methodName2);
                        break;
                    }
                    default: break;

                }
                return getter;

            } catch (NoSuchMethodException e) {
                occurenceType++;
            }
        }
        LOGGER.warning("No getter have been found for attribute " + propertyName + " in the class " + rootClass.getName());
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
     * @return a setter to this attribute.
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

                /**
                 * This switch is for debugging purpose
                 */
                switch (occurenceType) {

                    case 0: {
                        LOGGER.finer("The setter " + methodName + "(" + paramClass.getName() + ") does not exist");
                        occurenceType = 1;
                        break;
                    }
                    case 1: {
                        LOGGER.finer("The setter " + methodName + "(long) does not exist");
                        occurenceType = 2;
                        break;
                    }
                    case 2: {
                        if (interfacee != null) {
                            LOGGER.finer("The setter " + methodName + "(" + interfacee.getName() + ") does not exist");
                        }
                        occurenceType = 3;
                        break;
                    }
                    case 3: {
                        LOGGER.finer("The setter " + methodName + "(Collection<" + paramClass.getName() + ">) does not exist");
                        occurenceType = 4;
                        break;
                    }
                    case 4: {
                        LOGGER.finer("The setter " + methodName + "s(Collection<" + paramClass.getName() + ">) does not exist");
                        occurenceType = 5;
                        break;
                    }
                    case 5: {
                        if (argumentSuperClass != null) {
                            LOGGER.finer("The setter " + methodName + "(" + argumentSuperClass.getName() + ") does not exist");
                            argumentSuperClass     = argumentSuperClass.getSuperclass();
                            occurenceType = 5;

                        } else {
                            occurenceType = 6;
                        }
                        break;
                    }
                    case 6: {
                        if (argumentSuperInterface != null) {
                            LOGGER.finer("The setter " + methodName + "(" + argumentSuperInterface.getName() + ") does not exist");
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
}
