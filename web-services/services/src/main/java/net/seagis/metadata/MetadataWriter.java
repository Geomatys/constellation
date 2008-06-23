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

package net.seagis.metadata;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.mdweb.model.storage.LinkedValue;
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
     * Record the date format in the metadata.
     */
    private DateFormat dateFormat; 
    
    /**
     * A map recording the binding between java Class and MDWeb classe 
     */
    private Map<Class, Classe> classBinding;
    
    /**
     * A List of the already see object for the current metadata readed
     * (in order to avoid infinite loop)
     */
    private Map<Object, Value> alreadyWrite;
    
    /**
     * Build a new metadata writer.
     * 
     * @param MDReader an MDWeb database reader.
     */
    public MetadataWriter(Reader MDReader, Writer20 MDWriter) throws SQLException {
        
        MDCatalog         = MDReader.getCatalog("CSWCat");
        user              = MDReader.getUser("admin");
        this.MDReader     = MDReader;  
        this.MDWriter     = MDWriter;
        this.dateFormat   = new SimpleDateFormat("yyyy-mm-dd");
        this.classBinding = new HashMap<Class, Classe>();
        this.alreadyWrite = new HashMap<Object, Value>();
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
            
            // ISO 19115 types
            if (className.equals("MetaDataImpl")         || 
            
            // ISO 19110 types        
                className.equals("FeatureCatalogueImpl") ||
                className.equals("FeatureOperationImpl") ||
                className.equals("FeatureAssociationImpl")
            ) {
                mainStandard   = Standard.ISO_19115;
            
            // CSW Types    
            } else if (className.equals("RecordType")) {
                mainStandard = MDReader.getStandard("Catalog Web Service");
            
            // unkow types
            } else {
                throw new IllegalArgumentException("Can't register ths kind of object:" + className);
            }
            Classe rootClasse = getClasseFromObject(object);
            if (rootClasse != null) {
                alreadyWrite.clear();
                Path rootPath = new Path(rootClasse.getStandard(), rootClasse);
                addValueFromObject(form, object, rootPath, null);
                return form;
            } else {
                logger.severe("unable to find the root class:" + object.getClass().getSimpleName());
                return null;
            }
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
        if (object == null) {
            return;
        }             
        
        //if the object is a collection we call the method on each child
        Classe classe;
        if (object instanceof Collection) {
            Collection c = (Collection) object;
            for (Object obj: c) {
                addValueFromObject(form, obj, path, parentValue);
                
            }
            return;
            
        //if the object is a JAXBElement we desencapsulate it    
        } else {
            if (object instanceof JAXBElement) {
                JAXBElement jb = (JAXBElement) object;
                object = jb.getValue();
            } 
            classe = getClasseFromObject(object);
        }
        
        //if we don't have found the class we stop here
        if (classe == null) {
            return;
        }
        
        //we try to find the good ordinal
        int ordinal;
        if (parentValue == null) {
            ordinal = 1;
        } else {
            ordinal  = form.getNewOrdinal(parentValue.getIdValue() + ':' + path.getName());
        }
        
        //we look if the object have been already write
        Value linkedValue = alreadyWrite.get(object);
        
        // if its a primitive type we create a TextValue
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
                if (cle != null && cle instanceof org.mdweb.model.schemas.Locale) {
                    object = cle.getShortName();
                } else if (cle != null) {
                    object = cle.getCode();
                } else {
                    String values = "";
                    for (Property p: classe.getProperties()) {
                        values += p.getName() +'\n';
                    }
                    logger.severe("unable to find a codeListElement named " + codelistElement + " in the codelist " + classe.getName() + '\n' +
                                  "allowed values are: " + '\n' +  values);
                }
            }
            String value;
            if (object instanceof java.util.Date) {
                value = dateFormat.format(object);
            } else {
                value = object + "";
            }
            
            TextValue textValue = new TextValue(path, form , ordinal, value, classe, parentValue);
            logger.finer("new TextValue: " + path.toString() + " classe:" + classe.getName() + " value=" + object + " ordinal=" + ordinal);
        
        // if we have already see this object we build a Linked Value.
        } else if (linkedValue != null) {
            
            // TODO uncomment when succed deploying MDWEB jar 
            //LinkedValue value = new LinkedValue(path, form, ordinal, form, linkedValue, classe, parentValue);
            logger.finer("new LinkedValue: " + path.toString() + " classe:" + classe.getName() + " linkedValue=" + linkedValue.getIdValue() + " ordinal=" + ordinal);
        
        // else we build a Value node.
        } else {
        
            Value value = new Value(path, form, ordinal, classe, parentValue);
            logger.finer("new Value: " + path.toString() + " classe:" + classe.getName() + " ordinal=" + ordinal);
            //we add this object to the listed of already write element
            alreadyWrite.put(object, value);
            
            do {
                for (Property prop: classe.getProperties()) {
                    // TODO remove when fix in MDweb2
                    if (prop.getName().equals("geographicElement3") ||  prop.getName().equals("geographicElement4"))
                        continue;
                
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
                classe = classe.getSuperClass();
                if (classe != null) {
                    logger.finer("searching in superclasse " + classe.getName());
                }
            } while (classe != null);
        }
    }

    /**
     * Return a getter Method for the specified attribute (propertyName) 
     * 
     * @param propertyName The attribute name.
     * @param rootClass    The class whitch owe this attribute
     * 
     * @return a setter to this attribute.
     */
    private Method getGetterFromName(String propertyName, Class rootClass) {
        logger.finer("search for a getter in " + rootClass.getName() + " of name :" + propertyName);
        
        //special case and corrections
        if (propertyName.equals("beginPosition")) {
            propertyName = "begining";
        } else if (propertyName.equals("endPosition")) {
            propertyName = "ending";
        } else if (propertyName.equals("onlineResource")) {
            propertyName = "onLineResource";
        } else if (propertyName.equals("dataSetURI")) {
            propertyName = "dataSetUri";
        // TODO remove when this issue will be fix in MDWeb    
        } else if (propertyName.indexOf("geographicElement") != -1) {
            propertyName = "geographicElement";
        }
        
        String methodName = "get" + firstToUpper(propertyName);
        int occurenceType = 0;
        
        while (occurenceType < 4) {

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
                    case 2: {
                        logger.finer("The getter " + methodName + "es() does not exist");
                        occurenceType = 3;
                        break;
                    }
                    case 3: {
                        logger.finer("The getter " + methodName + "es() does not exist");
                        occurenceType = 4;
                        break;
                    }
                    default:
                        occurenceType = 5;
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
     * Return true if the MDWeb classe is primitive (i.e. if its a CodeList or if it has no properties).
     * 
     * @param classe an MDWeb classe Object
     */
    private boolean isPrimitive(Classe classe) {
        if (classe != null)
            return (classe.getProperties().size() == 0 || classe instanceof CodeList);
        return false;
    }
    
    /**
     * Return an MDWeb classe object for the specified java object.
     * 
     * @param object the object to identify
     *
     * @throws java.sql.SQLException
     */
    private Classe getClasseFromObject(Object object) throws SQLException {
        
        String className;
        String packageName;
        Classe result;
        if (object != null) {
            
            result = classBinding.get(object.getClass());
            if (result != null) {
                return result;
            }
            
            className   = object.getClass().getSimpleName();
            packageName = object.getClass().getPackage().getName();
            logger.finer("searche for classe " + className);
            
        } else {
            return null;
        }
        //for the primitive type we return ISO primitive type
        result = getPrimitiveTypeFromName(className);
        if (result != null) {
            classBinding.put(object.getClass(), result);
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
        if (className.endsWith("Type") && !className.equals("CouplingType") 
                                       && !className.equals("DateType") 
                                       && !className.equals("KeywordType")
                                       && !className.equals("FeatureType")) {
            className = className.substring(0, className.length() - 4);
        }
        
        List<Standard> availableStandards = new ArrayList<Standard>();
        
        // ISO 19115 and its sub standard (ISO 19119, 19110)
        if (mainStandard.equals(Standard.ISO_19115)) {
            availableStandards.add(mainStandard);
            availableStandards.add(Standard.ISO_19108);
            availableStandards.add(Standard.ISO_19103);
            availableStandards.add(MDReader.getStandard("ISO 19119"));
            availableStandards.add(MDReader.getStandard("ISO 19110"));
        
        // CSW standard    
        } else {
            availableStandards.add(MDReader.getStandard("Catalog Web Service"));
            availableStandards.add(MDReader.getStandard("DublinCore"));
            availableStandards.add(MDReader.getStandard("DublinCore-terms"));
            availableStandards.add(MDReader.getStandard("OGC Web Service"));
        }
        for (Standard standard : availableStandards) {
            
            /* to avoid some confusion between to classes with the same name
             * we affect the standard in some special case
             */
            if (packageName.equals("org.geotools.service")) {
                standard = MDReader.getStandard("ISO 19119");
            }       
            String name = className;
            int nameType = 0;
            while (nameType < 9) {
                
                logger.finer("searching: " + standard.getName() + ":" + name);
                result = MDReader.getClasse(name, standard);
                if (result != null) {
                    logger.finer("class found:" + standard.getName() + ":" + name);
                    classBinding.put(object.getClass(), result);
                    return result;
                } 
                
                switch (nameType) {

                        //we add the prefix MD_
                        case 0: {
                            nameType = 1;
                            name = "MD_" + className;    
                            break;
                        }
                        //we add the prefix MD_ + the suffix "Code"
                        case 1: {
                            nameType = 2;
                            name = "MD_" + className + "Code";    
                            break;
                        }
                        //we add the prefix CI_
                        case 2: {
                            nameType = 3;
                            name = "CI_" + className;    
                            break;
                        }
                        //we add the prefix CI_ + the suffix "Code"
                        case 3: {
                            nameType = 4;
                            name = "CI_" + className + "Code";    
                            break;
                        }
                        //we add the prefix EX_
                        case 4: {
                            nameType = 5;
                            name = "EX_" + className;    
                            break;
                        }
                        //we add the prefix SV_
                        case 5: {
                            nameType = 6;
                            name = "SV_" + className;    
                            break;
                        }
                        //we add the prefix FC_
                        case 6: {
                            nameType = 7;
                            name = "FC_" + className;    
                            break;
                        }
                        //for the code list we add the "code" suffix
                        case 7: {
                            if (name.indexOf("Code") != -1) {
                                name += "Code";
                            }
                            nameType = 8;
                            break;
                        }
                         //for the code list we add the "code" suffix
                        //for the temporal element we remove add prefix
                        case 8: {
                            name = "Time" + name;
                            nameType = 9;
                            break;
                        }
                        default:
                            nameType = 9;
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
