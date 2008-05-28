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

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.CodeList;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.users.User;
import org.mdweb.sql.Reader;
import org.mdweb.sql.v20.Writer20;

/**
 *
 * @author Guilhem Legal
 */
public class MetadataWriter {

    /**
     * A debugging logger.
     */
    private Logger logger = Logger.getLogger("net.seagis.coverage.wms");
    
    /**
     * A MDWeb catalogs where write the form.
     */
    private Catalog MDCatalog;
    
    /**
     * The MDWeb user who owe the inserted form.
     */
    private final User user;
    
    /**
     * A reader to the MDWeb database.
     */
    private Reader MDReader;
    
    /**
     * A writer to the MDWeb database.
     */
    private Writer20 MDWriter;
    
    /**
     * The current main standard of the Object to create
     */
    private Standard mainStandard;
    
    /**
     * Build a new metadata writer.
     * 
     * @param MDReader an MDWeb database reader.
     */
    public MetadataWriter(Reader MDReader, Writer20 MDWriter) throws SQLException {
        
        MDCatalog      = MDReader.getCatalog("CSWCat");
        user           = MDReader.getUser("admin");
        this.MDReader  = MDReader;  
        this.MDWriter  = MDWriter;
    }

    /**
     * Return an MDWeb formular from an object.
     * 
     * @param object The object to transform in form.
     * @return an MDWeb form representing the metadata object.
     */
    public Form getFormFromObject(Object object, String title) throws SQLException {

        if (object != null) {
            Date creationDate = new Date(System.currentTimeMillis());
            Form form = new Form(-1, MDCatalog, title, user, null, null, creationDate);

            String className = object.getClass().getSimpleName();
            if (className.equals("MetaDataImpl")) {
                mainStandard   = Standard.ISO_19115;
            } else if (className.equals("RecordType")) {
                mainStandard = MDReader.getStandard("Catalog Web Service");
            } else {
                throw new IllegalArgumentException("Can't register ths kind of object:" + className);
            }
            Classe rootClasse = getClasseFromObject(object);
            Path rootPath = new Path(mainStandard, rootClasse);
            
            addValueFromObject(form, object, rootPath, null);
            return form;
        } else {
            logger.severe("unable to create form object is null");
            return null;
        }
    }
    
    /**
     * Add a MDWeb value (and his children)to the specified form.
     * 
     * @param form The created form.
     * 
     */
    private void addValueFromObject(Form form, Object object, Path path, Value parentValue) throws SQLException {
        //if the path is not already in the database we write it
        if (MDReader.getPath(path.getId()) == null) {
           MDWriter.writePath(path);
        } 
                            
        Classe classe;
        if (object instanceof Collection) {
            Collection c = (Collection) object;
            for (Object obj: c) {
                addValueFromObject(form, obj, path, parentValue);
                
            }
            return;
        } else {
            if (object instanceof JAXBElement) {
                JAXBElement jb = (JAXBElement) object;
                object = jb.getValue();
            } else if (object instanceof Double ){
                
            }
            classe = getClasseFromObject(object);
        }
        
        //we try to find the good ordinal
        int ordinal;
        if (parentValue == null) {
            ordinal = 1;
        } else {
            ordinal  = form.getNewOrdinal(parentValue.getIdValue() + ':' + path.getName());
        }
        
        if (isPrimitive(classe)) {
            if (classe instanceof CodeList) {
                CodeList cl = (CodeList) classe;
                String codelistElement;
                if (classe.getName().equals("LanguageCode")) {
                    codelistElement =  ((Locale) object).getISO3Language();
                } else {
                    codelistElement =  ((org.opengis.util.CodeList) object).identifier();
                }
                CodeListElement cle = (CodeListElement) cl.getPropertyByName(codelistElement);
                if (cle != null) {
                    object = cle.getCode();
                } else {
                    logger.severe("unable to find a codeListElement named " + codelistElement + " in the codelist " + classe.getName());
                }
            }
            TextValue textValue = new TextValue(path, form , ordinal, object + "", classe, parentValue);
            logger.finer("new TextValue: " + path.toString() + " classe:" + classe.getName() + " value=" + object + " ordinal=" + ordinal);
        } else {
            
            Value value = new Value(path, form, ordinal, classe, parentValue);
            logger.finer("new Value: " + path.toString() + " classe:" + classe.getName() + " ordinal=" + ordinal);
            for (Property prop: classe.getProperties()) {
                Method getter = getGetterFromName(prop.getName(), object.getClass());
                if (getter != null) {
                    try {
                        Object propertyValue = getter.invoke(object);
                        if (propertyValue != null) {
                            Path childPath = new Path(path, prop); 
                            
                            //if the path is not already in the database we write it
                            if (MDReader.getPath(childPath.getId()) == null) {
                                MDWriter.writePath(childPath);
                            }
                            addValueFromObject(form, propertyValue, childPath, value);
                        } 
                    
                    } catch (IllegalAccessException e) {
                        logger.severe("The class is not accessible");
                        return;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        logger.severe("Exception throw in the invokated constructor");
                        return;
                    }   
                }
            }
        }
    }

    /**
     * Return a getter Method for the specified attribute (propertyName) 
     * 
     * @param propertyName The attribute name.
     * @param classe       The attribute type.  
     * @param rootClass    The class whitch owe this attribute
     * 
     * @return a setter to this attribute.
     */
    private Method getGetterFromName(String propertyName, Class rootClass) {
        logger.finer("search for a getter in " + rootClass.getName() + " of name :" + propertyName);
        
        //special case
        if (propertyName.equals("beginPosition")) {
            propertyName = "begining";
        } else if (propertyName.equals("endPosition")) {
            propertyName = "ending";
        } 
        
        String methodName = "get" + firstToUpper(propertyName);
        int occurenceType = 0;
        
        while (occurenceType < 2) {

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
                }
                if (getter != null) {
                    logger.finer("getter found: " + getter.toGenericString());
                }
                return getter;

            } catch (NoSuchMethodException e) {

                switch (occurenceType) {

                    case 0: {
                        logger.finer("The getter " + methodName + "() does not exist");
                        occurenceType = 1;
                        break;
                    }

                    case 1: {
                        logger.finer("The getter " + methodName + "s() does not exist");
                        occurenceType = 2;
                        break;
                    }
                    default:
                        occurenceType = 3;
                }
            }
        }
        logger.severe("No getter have been found for attribute " + propertyName + " in the class " + rootClass.getName());
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
    
    /**
     * Return true if the MDWeb classe is primitive.
     * 
     * @param classe an MDWeb classe Object
     */
    private boolean isPrimitive(Classe classe) {
        return (classe.getProperties().size() == 0 || classe instanceof CodeList);
    }
    
    /**
     * Return an MDWeb classe object for the specified java object.
     * 
     * @param object the object to identify
     * @param mainStandard A standard indicating in witch specification search.
     *
     * @throws java.sql.SQLException
     */
    private Classe getClasseFromObject(Object object) throws SQLException {
        Classe result;
        String className;
        if (object != null) {
            className = object.getClass().getSimpleName();
            logger.finer("searche for classe " + className);
            
        } else {
            return null;
        }
        //for the primitive type we return ISO primitive type
        result = getPrimitiveTypeFromName(className);
        if (result != null) {
            return result;
        }

        //special case TODO delete when geotools/api will be updated.
        if (className.equals("MetaDataImpl")) {
            className = "Metadata";
        } else if (className.equals("OnLineResourceImpl")) {
            className = "OnlineResource";
        } else if (className.equals("CitationDate") || className.equals("CitationDateImpl")) {
            className = "CI_Date";
        } else if (className.equals("FRA_DirectReferenceSystem")) {
            className = "MD_ReferenceSystem";
        } 
        
        //we remove the Impl suffix
        int i = className.indexOf("Impl");
        if (i != -1) {
            className = className.substring(0, i);
        }
        
        //we remove the Type suffix
        if (className.endsWith("Type")) {
            className = className.substring(0, className.length() - 4);
        }
        
        List<Standard> availableStandards = new ArrayList<Standard>();
        if (mainStandard.equals(Standard.ISO_19115)) {
            availableStandards.add(mainStandard);
            availableStandards.add(Standard.ISO_19108);
            availableStandards.add(Standard.ISO_19103);
        } else {
            availableStandards.add(MDReader.getStandard("Catalog Web Service"));
            availableStandards.add(MDReader.getStandard("DublinCore"));
            availableStandards.add(MDReader.getStandard("DublinCore-terms"));
            availableStandards.add(MDReader.getStandard("OGC Web Service"));
        }
        for (Standard standard : availableStandards) {
            
                
            String name = className;
            int nameType = 0;
            while (nameType < 5) {
                
                logger.finer("searching: " + standard.getName() + ":" + name);
                result = MDReader.getClasse(name, standard);
                if (result != null) {
                    logger.finer("class found:" + standard.getName() + ":" + name);
                    return result;
                } 
                
                switch (nameType) {

                        //we add the prefix MD_
                        case 0: {
                            nameType = 1;
                            name = "MD_" + className;    
                            break;
                        }
                        //we add the prefix CI_
                        case 1: {
                            nameType = 2;
                            name = "CI_" + className;    
                            break;
                        }
                        //we add the prefix EX_
                        case 2: {
                            nameType = 3;
                            name = "EX_" + className;    
                            break;
                        }
                        //for the code list we add the "code" suffix
                        case 3: {
                            if (name.indexOf("Code") != -1) {
                                name += "Code";
                            }
                            nameType = 4;
                            break;
                        }
                         //for the code list we add the "code" suffix
                        //for the temporal element we remove add prefix
                        case 4: {
                            name = "Time" + name;
                            nameType = 5;
                            break;
                        }
                        default:
                            nameType = 5;
                            break;
                    }

                }
            }
        
        logger.severe("class no found: " + className);
        return null;
    }
    
    /**
     * Return a class (java primitive type) from a class name.
     * 
     * @param className the standard name of a class. 
     * @return a primitive class.
     */
    private Classe getPrimitiveTypeFromName(String className) throws SQLException {
        
        if (className.equals("String") || className.equals("SimpleInternationalString")) {
            return MDReader.getClasse("CharacterString", Standard.ISO_19103);
        } else if (className.equalsIgnoreCase("Date")) {
            return MDReader.getClasse(className, Standard.ISO_19103);
        }  else if (className.equalsIgnoreCase("Integer")) {
            return MDReader.getClasse(className, Standard.ISO_19103);
        } else if (className.equalsIgnoreCase("Boolean")) {
            return MDReader.getClasse(className, Standard.ISO_19103);
        }  else if (className.equalsIgnoreCase("URL")) {
            return MDReader.getClasse(className, Standard.ISO_19115);
        //special case for locale codeList.
        } else if (className.equals("Locale")) {
            return MDReader.getClasse("LanguageCode", Standard.ISO_19115);
        //special case for Role codeList.
        } else if (className.equals("Role")) {
            return MDReader.getClasse("CI_RoleCode", Standard.ISO_19115);
        } else if (className.equals("Double")) {
            return MDReader.getClasse("Real", Standard.ISO_19103);
        } else {
            return null;
        }
    }
}
