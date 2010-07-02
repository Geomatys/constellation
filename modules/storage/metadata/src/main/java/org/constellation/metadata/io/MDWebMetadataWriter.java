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
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// JAXB dependencies
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.logging.Level;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.ReflectionUtilities;
import org.constellation.util.Util;

// Geotoolkit dependencies
import org.geotoolkit.metadata.iso.extent.DefaultGeographicDescription;
import org.geotoolkit.util.StringUtilities;

// MDWeb dependencies
import org.mdweb.model.profiles.Profile;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.CodeList;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.users.User;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.v20.Writer20;
import org.mdweb.io.Writer;
import org.mdweb.io.sql.v21.Writer21;
import org.mdweb.model.storage.FormInfo;
import org.mdweb.model.storage.RecordSet.EXPOSURE;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataWriter extends AbstractMetadataWriter {
    
    /**
     * A MDWeb RecordSets where write the form.
     */
    private RecordSet mdRecordSet;
    
    /**
     * The MDWeb user who owe the inserted form.
     */
    private final User defaultUser;
    
    /**
     * A writer to the MDWeb database.
     */
    protected Writer mdWriter;
    
    /**
     * The current main standard of the Object to create
     */
    private Standard mainStandard;
    
    /**
     * A map recording the binding between java Class and MDWeb classe 
     */
    private Map<String, Classe> classBinding;
    
    /**
     * A List of the already see object for the current metadata readed
     * (in order to avoid infinite loop)
     */
    private Map<Object, Value> alreadyWrite;

    protected static final String UNKNOW_TITLE = "unknow title";

    private boolean noLink = false;
    
    /**
     * Build a new metadata writer.
     * 
     */
    public MDWebMetadataWriter(Automatic configuration) throws MetadataIoException {
        super();
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null");
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new MetadataIoException("The configuration file does not contains a BDD object");
        }
        try {

            final DataSource dataSource = db.getDataSource();
            final boolean isPostgres    = db.getClassName().equals("org.postgresql.Driver");
            String version              = null;
            final Connection mdCon      = dataSource.getConnection();
            final Statement versionStmt = mdCon.createStatement();
            final ResultSet result      = versionStmt.executeQuery("Select * FROM \"version\"");
            if (result.next()) {
                version = result.getString(1);
            }
            result.close();
            versionStmt.close();
            mdCon.close();
            
            if (version != null && version.startsWith("2.0")) {
                mdWriter = new Writer20(dataSource, isPostgres);
            } else if (version != null &&  version.startsWith("2.1")) {
                mdWriter = new Writer21(dataSource, isPostgres);
            } else {
                throw new MetadataIoException("unexpected database version:" + version);
            }
           
            mdRecordSet = getRecordSet(configuration.getDefaultRecordSet());
            defaultUser = mdWriter.getUser("admin");

        } catch (MD_IOException ex) {
            throw new MetadataIoException("MD_IOException while initializing the MDWeb writer:" +'\n'+
                                           "cause:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new MetadataIoException("SQLException while initializing the MDWeb writer:" +'\n'+
                                           "cause:" + ex.getMessage());
        }
        
        this.classBinding = new HashMap<String, Classe>();
        this.alreadyWrite = new HashMap<Object, Value>();
    }

    /**
     * Build a new metadata writer.
     *
     */
    public MDWebMetadataWriter(Writer mdWriter, String defaultrecordSet) throws MetadataIoException {
        super();
        this.mdWriter    = mdWriter;
        try {
            this.mdRecordSet = getRecordSet(defaultrecordSet);
            this.defaultUser = mdWriter.getUser("admin");
        } catch (MD_IOException ex) {
            throw new MetadataIoException("MD_IOException while getting the catalog and user:" +'\n'+
                                           "cause:" + ex.getMessage());
        }
        this.classBinding = new HashMap<String, Classe>();
        this.alreadyWrite = new HashMap<Object, Value>();
    }

    protected MDWebMetadataWriter() throws MetadataIoException {
        defaultUser = null;
    }
    
    // TODO move this to CSW implementation
    public RecordSet getRecordSet(String defaultRecordSet) throws MD_IOException {
        RecordSet cat = null;
        if (defaultRecordSet != null) {
            cat = mdWriter.getRecordSet(defaultRecordSet);
        }
        if (cat == null) {
            cat = mdWriter.getRecordSet("CSWCat");
            if (cat == null) {
                cat = new RecordSet("CSWCat", "CSW Data RecordSet", null, null, EXPOSURE.EXTERNAL, 0, new Date(System.currentTimeMillis()), true);
                mdWriter.writeRecordSet(cat);
                LOGGER.info("writing CSWCat");
            }
        }
        return cat;
    }

     /**
     * This method try to find a title for this object.
     * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
     * else we try to find a getName(), getTitle(), or getId() method.
     *
     * @param obj the object for which we want a title.
     *
     * @return the founded title or UNKNOW_TITLE
     */
    protected String findTitle(Object obj) {

        //here we try to get the title
        String title = UNKNOW_TITLE;
        
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
            try {
                final Object objT = nameGetter.invoke(obj);
                if (objT instanceof String) {
                    title = (String) obj;
                } else if (objT != null) {
                    title = objT.toString();
                } else {
                    title = UNKNOW_TITLE;
                }

            } catch (IllegalAccessException ex) {
                LOGGER.warning("illegal access for method " + methodName + " in " + obj.getClass().getSimpleName() + "\ncause: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                LOGGER.warning("illegal argument for method " + methodName + " in " + obj.getClass().getSimpleName()  +"\ncause: " + ex.getMessage());
            } catch (InvocationTargetException ex) {
                LOGGER.warning("invocation target exception for " + methodName + " in " + obj.getClass().getSimpleName() +"\ncause: " + ex.getMessage());
            }
        }
        if (title == null)
            title = UNKNOW_TITLE;

        if (title.equals(UNKNOW_TITLE))
            LOGGER.warning("unknow type: " + obj.getClass().getName() + " unable to find a title, using default then.");
        
        return title;
    }

    /**
     * Return an MDWeb formular from an object.
     *
     * @param object The object to transform in form.
     * @return an MDWeb form representing the metadata object.
     */
    protected Form getFormFromObject(Object object) throws MD_IOException {
        final String title = findTitle(object);
        return getFormFromObject(object, defaultUser, mdRecordSet, null, title);
    }

    /**
     * Return an MDWeb formular from an object.
     * 
     * @param object The object to transform in form.
     * @return an MDWeb form representing the metadata object.
     */
    protected Form getFormFromObject(Object object, User user, RecordSet recordSet, Profile profile, String title) throws MD_IOException {
        
        if (object != null) {
            //we try to find a title for the from
            if (title.equals("unknow title")) {
                title = mdWriter.getAvailableTitle();
            }
            
            final Date creationDate = new Date(System.currentTimeMillis());
            final String className  = object.getClass().getSimpleName();
            // ISO 19115 types
            if (className.equals("DefaultMetadata")      ||
            
            // ISO 19110 types        
                className.equals("FeatureCatalogueImpl") ||
                className.equals("FeatureOperationImpl") ||
                className.equals("FeatureAssociationImpl")
            ) {
                mainStandard   = Standard.ISO_19115;
            
            // Ebrim Types    
            } else if (className.equals("IdentifiableType")) {
                mainStandard = Standard.EBRIM_V3;
           
            } else if (className.equals("RegistryObjectType")) {
                mainStandard = Standard.EBRIM_V2_5;
            
            // CSW Types    
            } else if (className.equals("RecordType")) {
                mainStandard = Standard.CSW;
            
            // SML Types
            } else if (className.equals("SensorML")) {
                mainStandard = Standard.SENSORML;

            // unkow types

            // unkow types
            } else {
                final String msg = "Can't register ths kind of object:" + object.getClass().getName();
                LOGGER.severe(msg);
                throw new IllegalArgumentException(msg);
            }

            if (profile == null) {
                if  (className.equals("DefaultMetadata")) {
                    profile = mdWriter.getProfile("ISO_19115");
                }
            }
            final UUID identifier = UUID.randomUUID();
            final Form form = new Form(-1, identifier, recordSet, title, user, null, profile, creationDate, creationDate, null, false, false, Form.TYPE.NORMALFORM);
            
            final Classe rootClasse = getClasseFromObject(object);
            if (rootClasse != null) {
                alreadyWrite.clear();
                final Path rootPath = new Path(rootClasse.getStandard(), rootClasse);
                final List<Value> collection = addValueFromObject(form, object, rootPath, null);
                collection.clear();
                return form;
            } else {
                LOGGER.log(Level.SEVERE, "unable to find the root class:{0}", object.getClass().getSimpleName());
                return null;
            }
        } else {
            LOGGER.severe("unable to create form object is null");
            return null;
        }
    }
    
    /**
     * Add a MDWeb value (and his children)to the specified form.
     * 
     * @param form The created form.
     * 
     */
    protected List<Value> addValueFromObject(Form form, Object object, Path path, Value parentValue) throws MD_IOException {

        final List<Value> result = new ArrayList<Value>();

        //if the path is not already in the database we write it
        if (mdWriter.getPath(path.getId()) == null) {
           mdWriter.writePath(path);
        } 
        if (object == null) {
            return result;
        }             
        if (object instanceof JAXBElement) {
            final JAXBElement jb = (JAXBElement) object;
            object = jb.getValue();
        }

        //if the object is a collection we call the method on each child
        Classe classe;
        if (object instanceof Collection) {
            final Collection c = (Collection) object;
            for (Object obj: c) {
                if (path.getName().equals("geographicElement2") && obj instanceof DefaultGeographicDescription) {
                    path = mdWriter.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3");
                }
                result.addAll(addValueFromObject(form, obj, path, parentValue));
                
            }
            return result;
            
        //if the object is a JAXBElement we desencapsulate it    
        } else {
            
            classe = getClasseFromObject(object);
        }
        
        //if we don't have found the class we stop here
        if (classe == null) {
            return result;
        }
        
        //we try to find the good ordinal
        int ordinal;
        if (parentValue == null) {
            ordinal = 1;
        } else {
            ordinal  = parentValue.getNewOrdinalForChild(path.getName());
        }
        
        //we look if the object have been already write
        final Value linkedValue;
        if (isNoLink()) {
            linkedValue = null;
        } else {
            linkedValue = alreadyWrite.get(object);
        }
        
        // if its a primitive type we create a TextValue
        if (classe.isPrimitive() || classe.getName().equals("LocalName")) {
            if (classe instanceof CodeList) {
                final CodeList cl = (CodeList) classe;
                String codelistElement;
                if (classe.getName().equals("LanguageCode")) {
                    try {
                        codelistElement =  ((Locale) object).getISO3Language();
                    } catch (MissingResourceException ex) {
                       codelistElement = ((Locale) object).getLanguage();
                    }
                } else {
                    if (object instanceof org.opengis.util.CodeList) {
                        codelistElement =  ((org.opengis.util.CodeList) object).identifier();
                        if (codelistElement == null) {
                            codelistElement = ((org.opengis.util.CodeList) object).name();
                        }
                        
                    } else if (object.getClass().isEnum()) {
                        
                        codelistElement = Util.getElementNameFromEnum(object);
                        
                    } else {
                        LOGGER.log (Level.SEVERE, "{0} is not a codelist!", object.getClass().getName());
                        codelistElement = null;
                    }
                }
                CodeListElement cle = (CodeListElement) cl.getPropertyByName(codelistElement);
                if (cle == null) {
                    cle = (CodeListElement) cl.getPropertyByShortName(codelistElement);
                }
                if (cle instanceof org.mdweb.model.schemas.Locale) {
                    object = cle.getShortName();
                } else if (cle != null) {
                    object = cle.getCode();
                } else {
                    final StringBuilder values = new StringBuilder();
                    for (Property p: classe.getProperties()) {
                        values.append(p.getName()).append('\n');
                    }
                    LOGGER.warning("unable to find a codeListElement named " + codelistElement + " in the codelist " + classe.getName() + 
                            "\nallowed values are:\n" +  values);
                }
            }
            String value;
            if (object instanceof java.util.Date) {
                synchronized (DATE_FORMAT) {
                    value = DATE_FORMAT.get(1).format(object);
                }
            } else {
                value = object.toString();
            }
            
            final TextValue textValue = new TextValue(path, form , ordinal, value, classe, parentValue);
            result.add(textValue);
            //LOGGER.finer("new TextValue: " + path.getId() + " classe:" + classe.getName() + " value=" + object + " ordinal=" + ordinal);
        
        // if we have already see this object we build a Linked Value.
        } else if (linkedValue != null) {
            
            final LinkedValue value = new LinkedValue(path, form, ordinal, form, linkedValue, classe, parentValue);
            result.add(value);
            //LOGGER.finer("new LinkedValue: " + path.getId() + " classe:" + classe.getName() + " linkedValue=" + linkedValue.getIdValue() + " ordinal=" + ordinal);
        
        // else we build a Value node.
        } else {
        
            final Value value = new Value(path, form, ordinal, classe, parentValue);
            result.add(value);
            //LOGGER.finer("new Value: " + path.getId() + " classe:" + classe.getName() + " ordinal=" + ordinal);
            //we add this object to the listed of already write element
            if (!isNoLink()) {
                alreadyWrite.put(object, value);
            }
            
            do {
                for (Property prop: classe.getProperties()) {
                    // TODO remove when fix in MDweb2
                    if (prop.getName().equals("geographicElement3") ||  prop.getName().equals("geographicElement4"))
                        continue;
                    final String propName;
                    // special case
                    if (prop.getName().equalsIgnoreCase("referenceSystemIdentifier") ||
                       (prop.getName().equalsIgnoreCase("identifier") && object.getClass().getSimpleName().equals("DefaultCoordinateSystemAxis")) ||
                       (prop.getName().equalsIgnoreCase("identifier") && object.getClass().getSimpleName().equals("DefaultVerticalCS")) ||
                       (prop.getName().equalsIgnoreCase("identifier") && object.getClass().getSimpleName().equals("DefaultVerticalDatum")) ||
                       (prop.getName().equalsIgnoreCase("identifier") && object.getClass().getSimpleName().equals("DefaultVerticalCRS"))) {
                        propName = "name";
                    } else if (prop.getName().equalsIgnoreCase("verticalCSProperty")) {
                        propName = "coordinateSystem";
                    } else if (prop.getName().equalsIgnoreCase("verticalDatumProperty")) {
                        propName = "datum";
                    } else if (prop.getName().equalsIgnoreCase("axisDirection")) {
                        propName = "direction";
                    } else if (prop.getName().equalsIgnoreCase("axisAbbrev")) {
                        propName = "abbreviation";
                    } else if (prop.getName().equalsIgnoreCase("uom") && !object.getClass().getSimpleName().equals("QuantityType")
                                                                      && !object.getClass().getSimpleName().equals("QuantityRange")
                                                                      && !object.getClass().getSimpleName().equals("TimeRange")
                                                                      && !object.getClass().getSimpleName().equals("TimeType")) {
                        propName = "unit";
                    } else if (prop.getName().equalsIgnoreCase("position") && object.getClass().getSimpleName().equals("DefaultPosition")) {
                        propName = "date";
                    
                    } else if (prop.getName().equalsIgnoreCase("transformationParameterAvailability")) {
                         propName = "transformationParameterAvailable";
                    } else {
                        propName = prop.getName();
                    }

                    final Method getter;
                    if (propName.equals("axis")) {
                        getter = ReflectionUtilities.getMethod("get" + StringUtilities.firstToUpper(propName), object.getClass(), int.class);
                    } else {
                        getter = ReflectionUtilities.getGetterFromName(propName, object.getClass());
                    }
                    if (getter != null) {
                        try {
                            final Object propertyValue;
                            if (propName.equals("axis")) {
                                propertyValue = getter.invoke(object, 0);
                            } else {
                                propertyValue = getter.invoke(object);
                            }
                            if (propertyValue != null) {
                                final Path childPath = new Path(path, prop);
                            
                                //if the path is not already in the database we write it
                                if (mdWriter.getPath(childPath.getId()) == null) {
                                    mdWriter.writePath(childPath);
                                }
                                result.addAll(addValueFromObject(form, propertyValue, childPath, value));
                            } 
                    
                        } catch (IllegalAccessException e) {
                            LOGGER.severe("The class is not accessible");
                            return result;
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            LOGGER.severe("Exception throw in the invokated getter: " + getter.toGenericString() +
                                          "\nCause: " + e.getMessage());
                            return result;
                        }   
                    }
                }
                classe = classe.getSuperClass();
                if (classe != null) {
                    LOGGER.log(Level.FINER, "searching in superclasse {0}", classe.getName());
                }
            } while (classe != null);
        }
        return result;
    }
    
    /**
     * Return an MDWeb classe object for the specified java object.
     * 
     * @param object the object to identify
     *
     * @throws java.sql.SQLException
     */
    protected Classe getClasseFromObject(Object object) throws MD_IOException {
        
        String className;
        String packageName;
        Classe result;
        if (object != null) {
            
            result = classBinding.get(object.getClass().getName());
            if (result != null) {
                return result;
            }
            
            className   = object.getClass().getSimpleName();
            packageName = object.getClass().getPackage().getName();
            LOGGER.log(Level.FINER, "searche for classe {0}", className);
            
        } else {
            return null;
        }
        //for the primitive type we return ISO primitive type
        result = getPrimitiveTypeFromName(className);
        if (result != null) {
            classBinding.put(object.getClass().getName(), result);
            return result;
        }

        //special case TODO delete when geotoolkit/api will be updated.
        if (className.equals("DefaultMetadata")) {
            className = "Metadata";
        } else if (className.equals("DefaultOnlineResource")) {
            className = "OnlineResource";
        } else if (className.equals("CitationDate") || className.equals("DefaultCitationDate")) {
            className = "CI_Date";
        } else if (className.equals("DefaultScope")) {
            className = "DQ_Scope";
        } else if (className.equals("ReferenceSystemMetadata")) {
            className = "ReferenceSystem";
        } else if (className.equals("DefaultReferenceIdentifier")) {
            className = "RS_Identifier";
        }
        
        //we remove the Impl suffix
        final int i = className.indexOf("Impl");
        if (i != -1) {
            className = className.substring(0, i);
        }

        //we remove the Default prefix
        if (className.startsWith("Default")) {
            className = className.substring(7, className.length());
        }

        //we remove the Abstract prefix
        if (className.startsWith("Abstract") && packageName.startsWith("org.geotoolkit.metadata.iso")) {
            className = className.substring(8, className.length());
        }
        
        //we remove the Type suffix
        if (className.endsWith("Type") && !className.equals("CouplingType") 
                                       && !className.equals("DateType") 
                                       && !className.equals("KeywordType")
                                       && !className.equals("FeatureType")
                                       && !className.equals("GeometricObjectType")
                                       && !className.equals("SpatialRepresentationType")
                                       && !className.equals("AssociationType")
                                       && !className.equals("InitiativeType")
                                       && !className.equals("DimensionNameType")
                                       && !className.equals("CodeType")) {
            className = className.substring(0, className.length() - 4);
        }

        if (className.endsWith("Entry") ) {
            className = className.substring(0, className.length() - 5);
        }
        
        final List<Standard> availableStandards = new ArrayList<Standard>();
        
        // ISO 19115 and its sub standard (ISO 19119, 19110)
        if (Standard.ISO_19115.equals(mainStandard)) {
            availableStandards.add(Standard.ISO_19115_FRA);
            availableStandards.add(mainStandard);
            availableStandards.add(Standard.ISO_19108);
            availableStandards.add(Standard.ISO_19103);
            availableStandards.add(Standard.ISO_19119);
            availableStandards.add(Standard.ISO_19110);
            final Standard nsdi = mdWriter.getStandard("NATSDI");
            if (nsdi != null)  {
                availableStandards.add(nsdi);
            }
        
        // CSW standard    
        } else if (Standard.CSW.equals(mainStandard)) {
            availableStandards.add(Standard.CSW);
            availableStandards.add(Standard.DUBLINCORE);
            availableStandards.add(Standard.DUBLINCORE_TERMS);
            availableStandards.add(Standard.OWS);
        
        // Ebrim v3 standard    
        } else if (Standard.EBRIM_V3.equals(mainStandard)) {
            availableStandards.add(Standard.EBRIM_V3);
            availableStandards.add(Standard.CSW);
            availableStandards.add(Standard.OGC_FILTER);
            availableStandards.add(Standard.MDWEB);
            
        // Ebrim v2.5 standard
        } else if (Standard.EBRIM_V2_5.equals(mainStandard)) {
            availableStandards.add(Standard.EBRIM_V2_5);
            availableStandards.add(Standard.CSW);
            availableStandards.add(Standard.OGC_FILTER);
            availableStandards.add(Standard.MDWEB);
        
        // Ebrim v2.5 tandard
        } else if (Standard.SENSORML.equals(mainStandard)) {
            availableStandards.add(Standard.SENSORML);
            availableStandards.add(Standard.SENSOR_WEB_ENABLEMENT);
            availableStandards.add(Standard.ISO_19108);
            

        } else {
            throw new IllegalArgumentException("Unexpected Main standard: " + mainStandard);
        }
        
        String availableStandardLabel = "";
        for (Standard standard : availableStandards) {
            
            availableStandardLabel = availableStandardLabel + standard.getName() + ',';
            /* to avoid some confusion between to classes with the same name
             * we affect the standard in some special case
             */
            if (packageName.equals("org.geotoolkit.service")) {
                standard = Standard.ISO_19119;
            } else if (packageName.startsWith("org.geotoolkit.sml.xml")) {
                standard = Standard.SENSORML;
            } else if (packageName.startsWith("org.geotoolkit.swe.xml")) {
                standard = mdWriter.getStandard("Sensor Web Enablement");
            } else if (packageName.equals("org.geotoolkit.gml.xml.v311")) {
                standard = Standard.ISO_19108;
            }
                
            String name = className;
            int nameType = 0;
            final String codeSuffix = "Code";
            while (nameType < 12) {
                
                LOGGER.finer("searching: " + standard.getName() + ":" + name);
                result = mdWriter.getClasse(name, standard);
                if (result != null) {
                    LOGGER.finer("class found:" + standard.getName() + ":" + name);
                    classBinding.put(object.getClass().getName(), result);
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
                            name = "MD_" + className + codeSuffix;
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
                            name = "CI_" + className + codeSuffix;
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
                        //we add the prefix DQ_
                        case 7: {
                            nameType = 8;
                            name = "DQ_" + className;    
                            break;
                        }
                        //we add the prefix LI_
                        case 8: {
                            nameType = 9;
                            name = "LI_" + className;    
                            break;
                        }
                        //we add the prefix DS_ + the suffix "Code"
                        case 9: {
                            nameType = 10;
                            name = "DS_" + className + codeSuffix;
                            break;
                        }
                        //for the temporal element we remove add prefix
                        case 10: {
                            name = "Time" + className;
                            nameType = 11;
                            break;
                        }
                        //for the code list we add the "code" suffix
                        case 11: {
                            if (name.indexOf(codeSuffix) != -1) {
                                name += codeSuffix;
                            }
                            nameType = 12;
                            break;
                        }
                        default:
                            nameType = 12;
                            break;
                    }

                }
            }
        
        availableStandardLabel = availableStandardLabel.substring(0, availableStandardLabel.length() - 1);
        LOGGER.warning("class no found: " + className + " in the following standards: " + availableStandardLabel + "\n (" + object.getClass().getName() + ')');
        return null;
    }
    
    /**
     * Return a class (java primitive type) from a class name.
     * 
     * @param className the standard name of a class. 
     * @return a primitive class.
     */
    private Classe getPrimitiveTypeFromName(String className) throws MD_IOException {
        
        if (className.equals("String") || className.equals("SimpleInternationalString") || className.equals("DefaultInternationalString") || className.equals("BaseUnit")) {
            return mdWriter.getClasse("CharacterString", Standard.ISO_19103);
        } else if (className.equalsIgnoreCase("Date")) {
            return mdWriter.getClasse(className, Standard.ISO_19103);
        } else if (className.equalsIgnoreCase("URI")) {
            return mdWriter.getClasse(className, Standard.ISO_19103);
        }  else if (className.equalsIgnoreCase("Integer")) {
            return mdWriter.getClasse(className, Standard.ISO_19103);
        }  else if (className.equalsIgnoreCase("Long")) {
            return mdWriter.getClasse("Integer", Standard.ISO_19103);
        } else if (className.equalsIgnoreCase("Boolean")) {
            return mdWriter.getClasse(className, Standard.ISO_19103);
        }  else if (className.equalsIgnoreCase("URL")) {
            return mdWriter.getClasse(className, Standard.ISO_19115);
        //special case for locale codeList.
        } else if (className.equals("Locale")) {
            return mdWriter.getClasse("LanguageCode", Standard.ISO_19115);
        //special case for Role codeList.
        } else if (className.equals("Role")) {
            return mdWriter.getClasse("CI_RoleCode", Standard.ISO_19115);
        } else if (className.equals("Double")) {
            return mdWriter.getClasse("Real", Standard.ISO_19103);
        } else {
            return null;
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean storeMetadata(Object obj) throws MetadataIoException {
        // profiling operation
        final long start = System.currentTimeMillis();
        long transTime   = 0;
        long writeTime   = 0;
        
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        
        // we create a MDWeb form form the object
        Form form = null;
        try {
            final long startTrans = System.currentTimeMillis();
            form                  = getFormFromObject(obj);
            transTime             = System.currentTimeMillis() - startTrans;
            
        } catch (IllegalArgumentException e) {
             throw new MetadataIoException("This kind of resource cannot be parsed by the service: " + obj.getClass().getSimpleName() +'\n' +
                                           "cause: " + e.getMessage());
        } catch (MD_IOException e) {
             throw new MetadataIoException("The service has throw an SQLException while writing the metadata: " + e.getMessage());
        }
        
        // and we store it in the database
        if (form != null) {
            try {
                final long startWrite = System.currentTimeMillis();
                final int result      = mdWriter.writeForm(form, false, true);
                writeTime             = System.currentTimeMillis() - startWrite;
                if (result == 1) {
                    LOGGER.log(logLevel, "The record have been skipped:{0}", form.getTitle());
                    return false;
                }

            } catch (MD_IOException e) {
                throw new MetadataIoException("The service has throw an SQLException while writing the metadata :" + e.getMessage(), e, null);
            }
            
            final long time = System.currentTimeMillis() - start;

            LOGGER.log(logLevel, "inserted new Form: " + form.getTitle() + " in " + time + " ms (transformation: " + transTime + " DB write: " + writeTime + ")");
            indexDocument(form);
            return true;

        }
        return false;
    }

    protected void indexDocument(Form f) {
        //need to be override by child
    }

    private void logMapSize() {
        LOGGER.info("\nclasses      :" + classBinding.size() +
                    "\nvalues       :" + alreadyWrite.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        classBinding.clear();
        try {
            if (mdWriter != null)
                mdWriter.close();
            classBinding.clear();
            alreadyWrite.clear();
            
        } catch (MD_IOException ex) {
            LOGGER.info("SQL Exception while destroying Metadata writer");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMetadata(String identifier) throws MetadataIoException {
        LOGGER.log(logLevel, "metadata to delete:{0}", identifier);

        int id;
        String recordSetCode = "";
        //we parse the identifier (Form_ID:RecordSet_Code)
        try  {
            if (identifier.indexOf(':') != -1) {
                recordSetCode = identifier.substring(identifier.indexOf(':') + 1, identifier.length());
                identifier    = identifier.substring(0, identifier.indexOf(':'));
                id            = Integer.parseInt(identifier);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
             throw new MetadataIoException("Unable to parse: " + identifier, null, "id");
        }
        try {
            // TODO is a way more fast to know that the form exist? method  isAlreadyRecordedForm(int id) writer20
            final RecordSet recordSet = mdWriter.getRecordSet(recordSetCode);
            final FormInfo f          = mdWriter.getFormInfo(recordSet, id);
            if (f != null) {
                mdWriter.deleteForm(id);
            } else {
                LOGGER.log(logLevel, "The sensor is not registered, nothing to delete");
                return false;
            }
        } catch (MD_IOException ex) {
            throw new MetadataIoException("The service has throw an SQLException while deleting the metadata: " + ex.getMessage());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replaceMetadata(String metadataID, Object any) throws MetadataIoException {
        final boolean succeed = deleteMetadata(metadataID);
        if (!succeed)
            return false;
        return storeMetadata(any);
    }

    /**
     * Return an MDWeb path from a Xpath.
     *
     * @param xpath An XPath
     *
     * @return An MDWeb path
     * @throws java.sql.SQLException
     * @throws org.constellation.ws.MetadataIoException
     */
    protected MixedPath getMDWPathFromXPath(String xpath) throws MD_IOException, MetadataIoException {
        //we remove the first '/'
        if (xpath.startsWith("/")) {
            xpath = xpath.substring(1);
        }

        String typeName = xpath.substring(0, xpath.indexOf('/'));
        if (typeName.contains(":")) {
            typeName = typeName.substring(typeName.indexOf(':') + 1);
        }
        xpath = xpath.substring(xpath.indexOf(typeName) + typeName.length() + 1);
        
        Classe type;
        // we look for a know metadata type
        if (typeName.equals("MD_Metadata")) {
            mainStandard = Standard.ISO_19115;
            type = mdWriter.getClasse("MD_Metadata", mainStandard);
        } else if (typeName.equals("Record")) {
            mainStandard = Standard.CSW;
            type = mdWriter.getClasse("Record", mainStandard);
        } else {
            throw new MetadataIoException("This metadata type is not allowed:" + typeName + "\n Allowed ones are: MD_Metadata or Record");//, INVALID_PARAMETER_VALUE);
        }

        Path p  = new Path(mainStandard, type);
        final StringBuilder idValue = new StringBuilder(mainStandard.getName()).append(':').append(type.getName()).append(".*");
        while (xpath.indexOf('/') != -1) {
            //Then we get the next Property name
            String propertyName = xpath.substring(0, xpath.indexOf('/'));

            //we look for an ordinal
            int ordinal = -1;
            if (propertyName.indexOf('[') != -1) {
                if (propertyName.indexOf(']') != -1) {
                    try {
                        final String ordinalValue = propertyName.substring(propertyName.indexOf('[') + 1, propertyName.indexOf(']'));
                        ordinal = Integer.parseInt(ordinalValue);
                    } catch (NumberFormatException ex) {
                        throw new MetadataIoException("The xpath is malformed, the brackets value is not an integer");
                    }
                    propertyName = propertyName.substring(0, propertyName.indexOf('['));
                } else {
                    throw new MetadataIoException("The xpath is malformed, unclosed bracket");
                }
            }

            LOGGER.finer("propertyName:" + propertyName + " ordinal:" + ordinal);
            idValue.append(':').append(propertyName).append('.');
            if (ordinal == -1) {
                idValue.append('*');
            } else {
                idValue.append(ordinal);
            }
            final Property property = getProperty(type, propertyName);
            p = new Path(p, property);
            type = property.getType();
            xpath = xpath.substring(xpath.indexOf('/') + 1);
        }

        //we look for an ordinal
        int ordinal = -1;
        if (xpath.indexOf('[') != -1) {
            if (xpath.indexOf(']') != -1) {
                try {
                    final String ordinalValue = xpath.substring(xpath.indexOf('[') + 1, xpath.indexOf(']'));
                    ordinal = Integer.parseInt(ordinalValue);
                } catch (NumberFormatException ex) {
                    throw new MetadataIoException("The xpath is malformed, the brackets value is not an integer");
                }
                xpath = xpath.substring(0, xpath.indexOf('['));
            } else {
                throw new MetadataIoException("The xpath is malformed, unclosed bracket");
            }
        }
        idValue.append(':').append(xpath).append('.');
        if (ordinal == -1) {
            idValue.append('*');
        } else {
            idValue.append(ordinal);
        }
        LOGGER.finer("last propertyName:" + xpath + " ordinal:" + ordinal);
        final Property property = getProperty(type, xpath);
        p = new Path(p, property);
        return new MixedPath(p, idValue.toString());
    }

    private Property getProperty(final Classe type, String propertyName) throws MD_IOException, MetadataIoException {
        // Special case for a bug in MDWeb
        if (propertyName.equals("geographicElement")) {
            propertyName = "geographicElement2";
        }
        Property property = type.getPropertyByName(propertyName);
        if (property == null) {
            // if the property is null we search in the sub-classes
            final List<Classe> subclasses = mdWriter.getSubClasses(type);
            for (Classe subClasse : subclasses) {
                property = subClasse.getPropertyByName(propertyName);
                if (property != null) {
                    break;
                }
            }
            if (property == null) {
                throw new MetadataIoException("There is no property:" + propertyName + " in the class " + type.getName());//, INVALID_PARAMETER_VALUE);
            }
        }
        return property;
    }

    /**
     * @return the noLink
     */
    public boolean isNoLink() {
        return noLink;
    }

    /**
     * @param noLink the noLink to set
     */
    public void setNoLink(boolean noLink) {
        this.noLink = noLink;
    }

    protected static final class MixedPath {

        public Path path;

        public String idValue;

        public MixedPath(Path path, String idValue) {
            this.path    = path;
            this.idValue = idValue;
        }

    }
}
