/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.wms;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.geotools.metadata.iso.MetadataEntity;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.sql.v20.Reader20;
import org.opengis.util.CodeList;

/**
 * Read The forms in the metadata database and instanciate them into geotools object.
 * When an object have been read it is stored in cache.
 * 
 * @author Guilhem legal
 */
public class MetadataReader {

    /**
     * A debugging logger
     */
    private Logger logger = Logger.getLogger("net.seagis.coverage.wms");
    
    /**
     * A reader to the MDWeb database.
     */
    private Reader20 MDReader;
    
    /**
     * A map containing the metadata already extract from the database.
     */
    private Map<String, Object> metadatas;
    
    /**
     * Record the date format in the metadata.
     */
    private DateFormat dateFormat; 
    
    /**
     * TODO remove this property to get all the Catalog (except MDATA)
     */
    private Catalog MDCatalog;

    /**
     * Build a new metadata Reader.
     * 
     * @param MDReader a reader to the MDWeb database.
     */
    public MetadataReader(Reader20 MDReader) throws SQLException {
        this.MDReader  = MDReader;
        this.dateFormat        = new SimpleDateFormat("dd-mm-yyyy");
        this.MDCatalog = MDReader.getCatalog("FR_SY");
    }

    /**
     * Return a metadata object from the specified identifier.
     * if is not already in cache it read it from the MDWeb database.
     * 
     * @param identifier The form identifier
     * 
     * @return An metadata object.
     * @throws java.sql.SQLException
     */
    public Object getMetadata(String identifier) throws SQLException {
        Object result = metadatas.get(identifier);
        if (result == null) {
            Form f = MDReader.getForm(MDCatalog, Integer.parseInt(identifier));
            result = getObjectFromForm(f);
            if (result != null) {
                metadatas.put(identifier, result);
            }
        }
        return result;
    }
    
    /**
     * Return an object an MDWeb formular.
     * 
     * @param form the MDWeb formular.
     * @return a geotools object representing the metadata.
     */
    private Object getObjectFromForm(Form form) {

        if (form != null && form.getTopValue() != null && form.getTopValue().getType() != null) {
            Value topValue = form.getTopValue();
            Object result = getObjectFromValue(form, topValue);
            return result;
        } else {
            if (form == null) {
                logger.severe("form is null");
            }
            if (form.getTopValue() == null) {
                logger.severe("Top value is null");
            } else {
                logger.severe("Top value Type is null");
            }
            return null;
        }
    }

    /**
     * Return an geotools object from a MDWeb value (this value can be see as a tree).
     * This method build the value and all is attribute recursivly.
     * 
     * @param form the MDWeb formular containg this value.
     * @param value The value to build.
     * 
     * @return a geotools metadat object.
     */
    private Object getObjectFromValue(Form form, Value value) {

        String className = value.getType().getName();
        Class classe = null;
        Object result;
        //@todo remove
        String temp = "";
        
        try {
            // we get the value's class
            classe = getClassFromName(className);

            // if the value is a leaf => primitive type
            if (value instanceof TextValue) {
                String textValue = ((TextValue) value).getValue();
                // in some special case (Date, double) we have to format the text value.
                if (classe.equals(Double.class)) {
                    textValue = textValue.replace(',', '.');
                }

                // if the value is a codeList element we call the static method valueOf 
                // instead of a constructor
                if (classe.getSuperclass().equals(CodeList.class)) {
                    Method method = classe.getMethod("valueOf", String.class);
                    result = method.invoke(null, textValue);
                    return result;


                // if the value is a codeList element we call the static method valueOf 
                // instead of a constructor   
                } else if (classe.equals(Date.class)) {
                    Method method = DateFormat.class.getMethod("parse", String.class);
                    result = method.invoke(dateFormat, textValue);
                    return result;

                // else we use a String constructor    
                } else {
                    temp = "String";
                    // we try to get a constructor(String)
                    Constructor constructor = classe.getConstructor(String.class);
                    logger.info("constructor:" + '\n' + constructor.toGenericString());
                    //we execute the constructor
                    result = constructor.newInstance(textValue);
                    return result;
                }

            } else {

                // we get the empty constructor
                Constructor constructor = classe.getConstructor();
                logger.info("constructor:" + '\n' + constructor.toGenericString());
                //we execute the constructor
                result = constructor.newInstance();
            }

        } catch (NoSuchMethodException e) {
            logger.severe("The class " + classe.getName() + " does not have a constructor(" + temp + ")");
            return null;
        } catch (InstantiationException e) {
            logger.severe("The class is abstract or is an interface");
            return null;
        } catch (IllegalAccessException e) {
            logger.severe("The class is not accessible");
            return null;
        } catch (java.lang.reflect.InvocationTargetException e) {
            logger.severe("Exception throw in the invokated constructor");

            return null;
        }

        //if the result is a subClasses of MetaDataEntity
        Map<String, Object> metaMap = null;
        boolean isMeta = false;
        if (result instanceof MetadataEntity) {
            MetadataEntity meta = (MetadataEntity) result;
            metaMap = meta.asMap();
            isMeta = true;
        }

        // then we search the setter for all the child value
        for (Value childValue : form.getValues()) {

            Path path = childValue.getPath();
            Path parent = path.getParent();

            if (parent != null && parent.equals(value.getPath())) {
                logger.info("new childValue:" + path.getName());

                // we get the object from the child Value
                Object param = getObjectFromValue(form, childValue);

                //we try to put the parameter in the parent object
                // by searching for the good attribute name
                boolean tryAgain = true;
                String attribName = path.getName();

                //special case due to a bug in mdweb
                if (attribName.startsWith("geographicElement")) {
                    attribName = "geographicElements";
                }

                int casee = 0;
                while (tryAgain) {
                    try {

                        logger.info("PUT " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                        if (isMeta) {
                            metaMap.put(attribName, param);
                        } else {
                            Method setter = getSetterFromName(attribName, param.getClass(), classe);
                            invokeSetter(setter, result, param);
                        }
                        tryAgain = false;
                    } catch (IllegalArgumentException e) {
                        logger.info(e.getMessage());
                        switch (casee) {

                            case 0:
                                if (attribName.charAt(attribName.length() - 1) == 'y') {
                                    attribName = path.getName().substring(0, attribName.length() - 1);
                                    attribName = attribName + "ies";
                                } else {
                                    attribName = path.getName() + 's';
                                }
                                casee = 1;
                                break;

                            case 1:
                                attribName = path.getName() + "es";
                                casee = 2;
                                break;
                            default:
                                throw e;
                            }
                    }
                }

                logger.info("");
            }
        }
        return result;
    }

    /**
     * Invoke a setter with the specified parameter in the specified object.
     * 
     * @param setter     The method to invoke
     * @param object     The object on witch the method is invoked.
     * @param parameter  The parameter of the method.
     */
    private void invokeSetter(Method setter, Object object, Object parameter) {
        String baseMessage = "unable to invoke setter: "; 
        try {

            setter.invoke(object, parameter);

        } catch (IllegalAccessException ex) {
            logger.severe(baseMessage + "The class is not accessible");

        } catch (IllegalArgumentException ex) {
            logger.severe(baseMessage + "The argument does not match with the setter");

        } catch (InvocationTargetException ex) {
            logger.severe(baseMessage + "Exception throw in the invokated setter");
        }

    }

    /**
     * Return a class (java primitive type) from a class name.
     * 
     * @param className the standard name of a class. 
     * @return a primitive class.
     */
    private Class getPrimitiveTypeFromName(String className) {

        if (className.equals("CharacterString")) {
            return String.class;
        } else if (className.equals("Date")) {
            return Date.class;
        } else if (className.equals("Decimal")) {
            return Double.class;
        } else if (className.equals("Real")) {
            return Double.class;
        } else if (className.equals("URL")) {
            return URI.class;
        //special case for locale codeList.
        } else if (className.equals("LanguageCode")) {
            return Locale.class;
        } else if (className.equals("CountryCode")) {
            return String.class;
        } else if (className.equals("RO_SystRefCode")) {
            return String.class;
        } else {
            return null;
        }
    }

    /**
     * Search an implementation for the specified class name.
     * 
     * @param className a standard class name.
     * 
     * @return a class object corresponding to the specified name.
     */
    private Class getClassFromName(String className) {
        logger.info("searche for class " + className);

        //for the primitive type we return java primitive type
        Class result = getPrimitiveTypeFromName(className);
        if (result != null) {
            return result;
        }

        //special case TODO delete when geotools/api will be updated.
        if (className.equals("MD_Metadata")) {
            className = "MD_MetaData";
        } else if (className.equals("CI_OnlineResource")) {
            className = "CI_OnLineResource";
        } else if (className.equals("CI_Date")) {
            className = "CitationDate";
        }



        List<String> packagesName = new ArrayList<String>();
        if (!className.contains("Code")) {
            packagesName.add("org.geotools.referencing");
            packagesName.add("org.geotools.metadata.iso");
            packagesName.add("org.geotools.metadata.iso.citation");
            packagesName.add("org.geotools.metadata.iso.constraint");
            packagesName.add("org.geotools.metadata.iso.content");
            packagesName.add("org.geotools.metadata.iso.distribution");
            packagesName.add("org.geotools.metadata.iso.extent");
            packagesName.add("org.geotools.metadata.iso.identification");
            packagesName.add("org.geotools.metadata.iso.lineage");
            packagesName.add("org.geotools.metadata.iso.maintenance");
            packagesName.add("org.geotools.metadata.iso.quality");
            packagesName.add("org.geotools.metadata.iso.spatial");

        } else {
            packagesName.add("org.opengis.metadata.citation");
            packagesName.add("org.opengis.metadata.constraint");
            packagesName.add("org.opengis.metadata.content");
            packagesName.add("org.opengis.metadata.distribution");
            packagesName.add("org.opengis.metadata.extent");
            packagesName.add("org.opengis.metadata.identification");
            packagesName.add("org.opengis.metadata.lineage");
            packagesName.add("org.opengis.metadata.maintenance");
            packagesName.add("org.opengis.metadata.quality");
            packagesName.add("org.opengis.metadata.spatial");
            packagesName.add("org.opengis.referencing");

        }



        for (String packageName : packagesName) {
            String name = className;
            int nameType = 0;
            while (nameType < 4) {
                try {
                    // Récupération de la classe java.awt.Button
                    result = Class.forName(packageName + '.' + name);
                    logger.info("class found:" + packageName + '.' + name);
                    return result;

                } catch (ClassNotFoundException e) {
                    switch (nameType) {

                        //we delete the prefix
                        case 0: {
                            nameType = 1;
                            if (name.indexOf('_') != -1) {
                                name = name.substring(name.indexOf('_') + 1);
                                break;
                            }
                        }
                        //for the code list we delete the "code" suffix
                        case 1: {
                            if (name.indexOf("Code") != -1) {
                                name = name.substring(0, name.indexOf("Code"));
                            }
                            nameType = 2;
                            break;
                        }
                        //we put "Impl" behind the className
                        case 2: {
                            name += "Impl";
                            nameType = 3;
                            break;
                        }
                        default:
                            nameType = 4;
                            break;
                    }

                }
            }
        }
        logger.severe("class no found: " + className);
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
    private Method getSetterFromName(String propertyName, Class classe, Class rootClass) {
        logger.info("search for a setter in " + rootClass.getName() + " of type :" + classe.getName());

        String methodName = "set" + firstToUpper(propertyName);
        int occurenceType = 0;
        Class interfacee = null;
        if (classe.getInterfaces().length != 0) {
            interfacee = classe.getInterfaces()[0];
        }

        while (occurenceType < 4) {

            try {
                Method setter = null;
                switch (occurenceType) {

                    case 0: {
                        setter = rootClass.getDeclaredMethod(methodName, classe);
                        break;
                    }
                    case 1: {
                        setter = rootClass.getDeclaredMethod(methodName, interfacee);
                        break;
                    }
                    case 2: {
                        setter = rootClass.getDeclaredMethod(methodName, Collection.class);
                        break;
                    }
                    case 3: {
                        setter = rootClass.getDeclaredMethod(methodName + "s", Collection.class);
                        break;
                    }
                }
                logger.info("setter found: " + setter.toGenericString());
                return setter;

            } catch (NoSuchMethodException e) {

                switch (occurenceType) {

                    case 0: {
                        logger.info("Le setter " + methodName + "(" + classe.getName() + ") n'existe pas");
                        occurenceType = 1;
                        break;
                    }

                    case 1: {
                        if (interfacee != null) {
                            logger.info("Le setter " + methodName + "(" + interfacee.getName() + ") n'existe pas");
                        }
                        occurenceType = 2;
                        break;
                    }

                    case 2: {
                        logger.info("Le setter " + methodName + "(Collection<" + classe.getName() + ">) n'existe pas");
                        occurenceType = 3;
                        break;
                    }
                    case 3: {
                        logger.info("Le setter " + methodName + "s(Collection<" + classe.getName() + ">) n'existe pas");
                        occurenceType = 4;
                        break;
                    }
                    default:
                        occurenceType = 4;
                }
            }
        }
        return null;
    }

    /**
     * Return a string with the first character to upper casse.
     * example : firstToUpper("hello") return "Hello".
     * 
     * @param s the string to modifiy
     * 
     * @return a string with the first character to upper casse.
     */
    private String firstToUpper(String s) {
        String first = s.substring(0, 1);
        String result = s.substring(1);
        result = first.toUpperCase() + result;
        return result;
    }
}
