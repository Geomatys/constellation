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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import javax.measure.unit.Unit;
import javax.xml.namespace.QName;

// Constellation Dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.ReflectionUtilities;
        
// MDWeb dependencies
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.v20.Reader20;
import org.mdweb.io.Reader;

// Geotoolkit dependencies
import org.geotoolkit.metadata.iso.MetadataEntity;
import org.geotoolkit.internal.CodeLists;
import org.geotoolkit.io.wkt.UnformattableObjectException;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.geotoolkit.util.FileUtilities;

// GeoAPI dependencies
import org.mdweb.io.sql.v21.Reader21;
import org.mdweb.model.schemas.Classe;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.CodeList;


/**
 * A database Reader designed for an MDweb database.
 * 
 * It read The mdweb forms into the database and instanciate them into geotoolkit object.
 * When an object have been read it is stored in cache.
 * 
 * @author Guilhem legal
 */
public class MDWebMetadataReader extends AbstractMetadataReader {

    /**
     * A reader to the MDWeb database.
     */
    protected Reader mdReader;
    
    /**
     * A map containing the mapping beetween the MDWeb className and java typeName
     */
    private Map<String, Class> classBinding;
    
    /**
     * A list of package containing the ISO 19115 interfaces (and the codelist classes)
     */
    private List<String> opengisPackage;
    
    /**
     * A list of package containing the ISO 19115 implementation.
     */
    private List<String> geotoolkitPackage;
    
    /**
     * A list of package containing the CSW and dublinCore implementation
     */
    private List<String> cswPackage;

     /**
     * A list of package containing the SensorML implementation
     */
    private List<String> sensorMLPackage;

    /**
     * A list of package containing the SWE implementation
     */
    private List<String> swePackage;

    /**
     * A list of package containing the GML implementation (JAXB binding not referencing)
     */
    private List<String> gmlPackage;

    /**
     * A list of package containing the Ebrim V3.0 implementation
     */
    private List<String> ebrimV3Package;
    
     /**
     * A list of package containing the Ebrim V2.5 implementation
     */
    private List<String> ebrimV25Package;
    
    /**
     * A List of the already see object for the current metadata readed
     * (in order to avoid infinite loop)
     */
    protected Map<Value, Object> alreadyRead;
    
    /**
     * A List of the already logged Missing MDWeb Classe.
     */
    private List<String> classeNotFound;

    private boolean storeMapping;

    /**
     * Build a new metadata Reader.
     * 
     * @param MDReader a reader to the MDWeb database.
     */
    public MDWebMetadataReader(Automatic configuration) throws MetadataIoException {
        super(true, false);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null");
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new MetadataIoException("The configuration file does not contains a BDD object");
        }
        try {
            final Connection mdConnection = db.getConnection();
            final boolean isPostgres      = db.getClassName().equals("org.postgresql.Driver");
            String version                = null;
            Statement versionStmt         = mdConnection.createStatement();
            ResultSet result              = versionStmt.executeQuery("Select * FROM \"version\"");
            if (result.next()) {
                version = result.getString(1);
            }
            result.close();
            versionStmt.close();

            if (version.startsWith("2.0")) {
                mdReader = new Reader20(mdConnection, isPostgres);
            } else if (version.startsWith("2.1")) {
                mdReader = new Reader21(mdConnection, isPostgres);
            } else {
                throw new MetadataIoException("unexpected database version:" + version);
            }
        } catch (SQLException ex) {
            throw new MetadataIoException("SQLException while initializing the MDWeb reader:" +'\n'+
                                           "cause:" + ex.getMessage());
        }

        if (configuration.getEnableThread() != null && !configuration.getEnableThread().isEmpty()) {
            boolean t = Boolean.parseBoolean(configuration.getEnableThread());
            if (t) {
                LOGGER.info("parrallele treatment enabled");
            }
            setIsThreadEnabled(t);
        }

        if (configuration.getEnableCache() != null && !configuration.getEnableCache().isEmpty()) {
            boolean c = Boolean.parseBoolean(configuration.getEnableCache());
            if (!c) {
                LOGGER.info("cache system have been disabled");
            }
            setIsCacheEnabled(c);
        }

        if (configuration.getStoreMapping() != null && !configuration.getStoreMapping().isEmpty()) {
            boolean m = Boolean.parseBoolean(configuration.getStoreMapping());
            if (m) {
                LOGGER.info("mapping storage enabled");
            }
            storeMapping = m;
        }

        initPackage();
        this.classBinding       = initClassBinding(configuration.getConfigurationDirectory());
        this.alreadyRead        = new HashMap<Value, Object>();
        this.classeNotFound     = new ArrayList<String>();
    }

    /**
     * A constructor used in profile Test .
     *
     * @param MDReader a reader to the MDWeb database.
     */
    protected MDWebMetadataReader(Connection mdConnection) {
        super(true, false);
        this.mdReader           = new Reader20(mdConnection);
        initPackage();
        this.classBinding       = new HashMap<String, Class>();
        this.alreadyRead        = new HashMap<Value, Object>();
        this.classeNotFound     = new ArrayList<String>();
    }

    /**
     * A constructor used in profile Test .
     *
     * @param MDReader a reader to the MDWeb database.
     */
    public MDWebMetadataReader(Reader mdReader) {
        super(true, false);
        this.mdReader           = mdReader;
        initPackage();
        this.classBinding       = new HashMap<String, Class>();
        this.alreadyRead        = new HashMap<Value, Object>();
        this.classeNotFound     = new ArrayList<String>();
    }

    /**
     * Fill the package attributes with all the subPackage of the specified ones.
     */
    private void initPackage() {

        this.geotoolkitPackage  = FileUtilities.searchSubPackage("org.geotoolkit.metadata.iso", "org.geotoolkit.referencing",
                                                               "org.geotoolkit.service", "org.geotoolkit.naming", "org.geotoolkit.feature.catalog",
                                                               "org.geotoolkit.metadata.fra", "org.geotoolkit.temporal.object");
        this.sensorMLPackage    = FileUtilities.searchSubPackage("org.geotoolkit.sml.xml.v100");
        this.swePackage         = FileUtilities.searchSubPackage("org.geotoolkit.swe.xml.v100");
        this.gmlPackage         = FileUtilities.searchSubPackage("org.geotoolkit.gml.xml.v311");

        this.opengisPackage     = FileUtilities.searchSubPackage("org.opengis.metadata", "org.opengis.referencing", "org.opengis.temporal",
                                                        "org.opengis.service", "org.opengis.feature.catalog");
        this.cswPackage         = FileUtilities.searchSubPackage("org.geotoolkit.csw.xml.v202", "org.geotoolkit.dublincore.xml.v2.elements", "org.geotoolkit.ows.xml.v100",
                                                        "org.geotoolkit.ogc.xml");
        this.ebrimV3Package     = FileUtilities.searchSubPackage("org.geotoolkit.ebrim.xml.v300", "org.geotoolkit.wrs.xml.v100");
        this.ebrimV25Package    = FileUtilities.searchSubPackage("org.geotoolkit.ebrim.xml.v250", "org.geotoolkit.wrs.xml.v090");
    }

    /**
     * Initialize the class binding between MDWeb database classes and java implementation classes.
     * 
     * We give the possibility to the user to add a configuration file making the mapping.
     * @return
     */
    private Map<String, Class> initClassBinding(File configDir) {
        final Map<String, Class> result = new HashMap<String, Class>();
        try {
            // we get the configuration file
            final File bindingFile   = new File(configDir, "classMapping.properties");
            final FileInputStream in = new FileInputStream(bindingFile);
            final Properties prop    = new Properties();
            prop.load(in);
            in.close();

            LOGGER.info("loading classe mapping");
            for (Object className : prop.keySet()) {
                try {
                    final Class c = Class.forName(prop.getProperty((String)className));
                    result.put((String)className, c);
                } catch (ClassNotFoundException ex) {
                    LOGGER.severe("error in class binding initialization for class:" + className);
                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.finer("no class mapping found (optional)");
        }  catch (IOException e) {
            LOGGER.warning("no class mapping found (optional) IOException");
        }
        return result;
    }

    /**
     * Return a metadata object from the specified identifier.
     * if is not already in cache it read it from the MDWeb database.
     * 
     * @param identifier The form identifier with the pattern : "Form_ID:RecordSet_Code"
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotoolkit metadata / ebrim registry object)
     * 
     * @throws java.sql.MetadataIoException
     */
    @Override
    public Object getMetadata(String identifier, int mode, List<QName> elementName) throws MetadataIoException {
        int id;
        String recordSetCode = "";
        
        //we parse the identifier (Form_ID:RecordSet_Code)
        try  {
            if (identifier.indexOf(':') != -1) {
                recordSetCode  = identifier.substring(identifier.indexOf(':') + 1, identifier.length());
                identifier   = identifier.substring(0, identifier.indexOf(':'));
                id           = Integer.parseInt(identifier);
            } else {
                throw new NumberFormatException();
            }
            
        } catch (NumberFormatException e) {
             throw new MetadataIoException("Unable to parse: " + identifier, null, "id");
        }

        try {
            alreadyRead.clear();
            final RecordSet recordSet = mdReader.getRecordSet(recordSetCode);

            //we look for cached object
            Object result = getFromCache(identifier);

            if (result == null) {
                final Form f = mdReader.getForm(recordSet, id);
                result = getObjectFromForm(identifier, f, mode);
            } else {
                LOGGER.finer("getting from cache: " + identifier);
            }
            return result;

        } catch (MD_IOException e) {
             throw new MetadataIoException("SQL exception while reading the metadata: " + identifier, null, "id");
        }
    }
    
    /**
     * Return an object from a MDWeb formular.
     * 
     * @param form the MDWeb formular.
     * @param type An elementSet : BRIEF, SUMMARY, FULL. (default is FULL);
     * @param elementName 
     * 
     * @return a geotoolkit/constellation object representing the metadata.
     */
    protected Object getObjectFromForm(String identifier, Form form, int mode) {

        if (form != null && form.getTopValue() != null && form.getTopValue().getType() != null) {
            final Value topValue = form.getTopValue();
            final Object result  = getObjectFromValue(form, topValue, mode);
            
            //we put the full object in the already read metadatas.
            if (result != null) {
               addInCache(identifier, result);
            }
            return result;
        
        //debugging part to see why the form cannot be read.
        } else {
            if (form == null) {
                LOGGER.severe("form is null");
            } else if (form.getTopValue() == null) {
                LOGGER.severe("Top value is null");
            } else {
                LOGGER.severe("Top value Type is null");
            }
            return null;
        }
    }
    
    /**
     * Return an geotoolkit object from a MDWeb value (this value can be see as a tree).
     * This method build the value and all is attribute recursivly.
     * 
     * @param form the MDWeb formular containg this value.
     * @param value The value to build.
     * 
     * @return a geotoolkit metadat object.
     */
    private Object getObjectFromValue(Form form, Value value, int mode) {
        Class classe = null;
        // we get the value's class
        if (value.getType() != null) {
            classe = getClassFromName(value.getType(), mode);
        } else {
            LOGGER.severe("Error null type for value:" + value.getIdValue());
            return null;
        }
        
        if (classe == null) {
            return null;
        }
        
        Object result;
        // if the value is a leaf => primitive type
        if (value instanceof TextValue) {
            String textValue = ((TextValue) value).getValue();
            // in some special case (Date, double) we have to format the text value.
            if (classe.equals(Double.class) && textValue != null) {
                textValue = textValue.replace(',', '.');
            }

            // if the value is a codeList element we call the static method valueOf
            // instead of a constructor
            if ((classe.getSuperclass() != null && classe.getSuperclass().equals(CodeList.class)) || classe.isEnum()) {
                // the textValue of a codelist is the code and not the value
                // so we must find the codeList element corrrespounding to this code.
                final org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList) value.getType();
                try {
                    final CodeListElement element = codelist.getElementByCode(Integer.parseInt(textValue));

                    Method method;
                    if (classe.getSuperclass() != null && classe.getSuperclass().equals(CodeList.class)) {
                        result = CodeLists.valueOf(classe, element.getName());

                    } else if (classe.isEnum()) {
                        method = ReflectionUtilities.getMethod("fromValue", classe, String.class);
                        result = ReflectionUtilities.invokeMethod(method, classe, element.getName());
                    } else {
                        LOGGER.severe("unknow codelist type");
                        return null;
                    }

                    return result;
                } catch (NumberFormatException e) {
                    if (textValue != null && !textValue.equals("")) {
                        LOGGER.severe("Format NumberException : unable to parse the code: " + textValue + " in the codelist: " + codelist.getName());
                    }
                    return null;
                }

            // if the value is a date we call the static method parse
            // instead of a constructor (temporary patch: createDate method)
            } else if (classe.equals(Date.class)) {
                if (textValue == null || textValue.isEmpty()) {
                    return null;
                }
                return TemporalUtilities.createDate(textValue);

            } else if (classe.equals(Locale.class)) {
                for (Locale candidate : Locale.getAvailableLocales()) {
                    if (candidate.getISO3Language().equalsIgnoreCase(textValue)) {
                        return candidate;
                    }
                }
                 return new Locale(textValue);

            // else we use a String constructor
            } else {
                //we execute the constructor
                result = ReflectionUtilities.newInstance(classe, textValue);

                //fix a bug in MDWeb with the value attribute TODO remove
                if (!form.asMoreChild(value)) {
                    return result;
                }
            }

        //if the value is a link
        } else if (value instanceof LinkedValue) {
            final LinkedValue lv = (LinkedValue) value;
            final Object tempobj = alreadyRead.get(lv.getLinkedValue());
            if (tempobj != null) {
                return tempobj;
            } else {
                return getObjectFromValue(lv.getLinkedForm(), lv.getLinkedValue(), mode);
            }

        // else if the value is a complex object
        } else {

            /**
             * Again another special case LocalName does not have a empty constructor (immutable)
             * and no setters so we must call the normal constructor.
             */
            if (classe.getSimpleName().equals("LocalName")) {
                TextValue child = null;

                //We search the child of the localName
                for (Value childValue : form.getValues()) {
                    if (childValue.getParent() != null && childValue.getParent().equals(value) && childValue instanceof TextValue) {
                        child = (TextValue) childValue;
                    }
                }
                if (child != null) {
                    final CharSequence cs = child.getValue();
                    return ReflectionUtilities.newInstance(classe, cs);
                } else {
                    LOGGER.severe("The localName is mal-formed");
                    return null;
                }

            /**
             * Again another special case QNAME does not have a empty constructor.
             * and no setters so we must call the normal constructor.
             */
            } else if (classe.getSimpleName().equals("QName")) {
                String localPart    = null;
                String namespaceURI = null;

                //We search the children of the QName
                for (Value childValue : form.getValues()) {
                    if (childValue.getParent() != null && childValue.getParent().equals(value) && childValue instanceof TextValue) {
                        if (childValue.getPath().getName().equals("localPart"))
                            localPart = ((TextValue)childValue).getValue();
                        else  if (childValue.getPath().getName().equals("namespaceURI"))
                            namespaceURI = ((TextValue)childValue).getValue();
                    }
                }
                if (localPart != null && namespaceURI != null) {
                    result = ReflectionUtilities.newInstance(classe, namespaceURI, localPart);
                    return result;
                } else {
                    LOGGER.severe("The QName is mal-formed");
                    return null;
                }
            }
            /**
             * normal case
             * we get the empty constructor
             */
            result = ReflectionUtilities.newInstance(classe);
            alreadyRead.put(value, result);
        }

        //if the result is a subClasses of MetaDataEntity
        Map<String, Object> metaMap = null;
        boolean isMeta  = false;
        boolean wasMeta = false;
        if (result instanceof MetadataEntity) {
            final MetadataEntity meta = (MetadataEntity) result;
            metaMap = meta.asMap();
            isMeta  = true;
            wasMeta = true;
        }

        // then we search the setter for all the child value
        for (Value childValue : form.getValues()) {
            
            final Path path = childValue.getPath();

            if (childValue.getParent()!= null && childValue.getParent().equals(value)) {
                LOGGER.finer("new childValue:" + path.getName());

                // we get the object from the child Value
                final Object param = getObjectFromValue(form, childValue, mode);
                if (param == null) {
                    continue;
                }
                //we try to put the parameter in the parent object
                // by searching for the good attribute name
                boolean tryAgain = true;
                String attribName = path.getName();

                if (mode != SENSORML) {
                    //special case due to a bug in mdweb
                    if (attribName.startsWith("geographicElement")) {
                        attribName = "geographicElements";
                    } else if (attribName.equals("transformationParameterAvailability")) {
                        attribName = "transformationParameterAvailable";
                    } else if (attribName.equals("beginPosition")) {
                        attribName = "begining";
                    } else if (attribName.equals("endPosition")) {
                        attribName = "ending";
                    } else if (attribName.equals("value") && classe.getSimpleName().equals("DefaultPosition")) {
                        attribName = "position";
                    }
                }

                int casee = 0;
                while (tryAgain) {
                    try {

                        //LOGGER.finer("PUT " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                        if (isMeta) {
                              metaMap.put(attribName, param);
                        } else {
                            final Method setter = ReflectionUtilities.getSetterFromName(attribName, param.getClass(), classe);
                            if (setter != null && result != null) {
                                ReflectionUtilities.invokeMethod(setter, result, param);
                            } else {
                                
                                if (mode != SENSORML) {
                                     // special case for geootoolkit referencing
                                    if (attribName.equalsIgnoreCase("identifier")) {
                                        attribName = "name";
                                    } else if (attribName.equalsIgnoreCase("verticalCSProperty")) {
                                        attribName = "coordinateSystem";
                                    } else if (attribName.equalsIgnoreCase("verticalDatumProperty")) {
                                        attribName = "datum";
                                    } else if (attribName.equalsIgnoreCase("axisDirection")) {
                                        attribName = "direction";
                                    } else if (attribName.equalsIgnoreCase("axisAbbrev")) {
                                        attribName = "abbreviation";
                                    } else if (attribName.equalsIgnoreCase("uom")) {
                                        attribName = "unit";
                                    } else if (attribName.equalsIgnoreCase("codeSpace")) {
                                        attribName = "codespace";
                                    }
                                }

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
                                if (field != null && result != null) {
                                    field.setAccessible(true);
                                    try {
                                        if (attribName.equals("axis")) {
                                            final CoordinateSystemAxis[] params = new CoordinateSystemAxis[1];
                                            params[0] = (CoordinateSystemAxis) param;
                                            field.set(result, params);
                                        } else if (field.getType().isArray()) {
                                          // todo find how to build a typed array
                                            final Object[] params = new Object[1];
                                            params[0] = param;
                                            field.set(result, params);
                                        
                                        } else if (field.getType().equals(Unit.class)) {

                                            final Unit<?> unit = Unit.valueOf((String)param);
                                            field.set(result, unit);
                                        } else {
                                            field.set(result, param);
                                        }
                                    } catch (IllegalAccessException ex) {
                                        LOGGER.severe("error while setting the parameter:" + param + "\n to the field:" + field + ":" + ex.getMessage());
                                    } catch (IllegalArgumentException ex) {
                                        String objectStr = "null";
                                        if (param != null) {
                                            try {
                                                objectStr = param.toString();
                                            } catch (UnformattableObjectException ex2) {
                                                objectStr = "(unformattableObject) " + param.getClass().getSimpleName();
                                            }
                                        }
                                        LOGGER.severe("IllegalArgumentException:" + ex.getMessage() + '\n' +
                                                      "while setting the parameter: " + objectStr   + '\n' +
                                                      "to the field: " + field + ".");
                                    }
                                } else {
                                    LOGGER.warning("no field " + attribName + " in class:" + classe.getName());
                                }
                            }
                        }
                        tryAgain = false;
                    } catch (IllegalArgumentException e) {
                        LOGGER.finer(e.getMessage());
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
                            case 2:
                                attribName = path.getName();
                                casee      = 3;
                                isMeta = false;
                                break;
                            default:
                                
                                LOGGER.severe("unable to put " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                                tryAgain = false;
                        }
                    } catch (ClassCastException ex) {
                        LOGGER.severe("Exception while putting in geotoolkit metadata: " + '\n' +
                                      "cause: " + ex.getMessage());
                        tryAgain = false;
                    }
                }
                if (wasMeta)
                    isMeta = true;
            }
        }
        return result;
    }

    /**
     * Return a class (java primitive type) from a class name.
     * 
     * @param className the standard name of a class. 
     * @return a primitive class.
     */
    private Class getPrimitiveTypeFromName(String className, String standardName) {

        if (className.equalsIgnoreCase("CharacterString")) {
            return String.class;
        } else if (className.equalsIgnoreCase("Date")) {
            return Date.class;
        } else if (className.equalsIgnoreCase("Decimal") || className.equalsIgnoreCase("Double")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("Real")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("Integer")) {
            return Integer.class;
        } else if (className.equalsIgnoreCase("Boolean") && !standardName.equals("Sensor Web Enablement")) {
            return Boolean.class;
        } else if (className.equalsIgnoreCase("Distance")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("URL") || className.equalsIgnoreCase("URI")) {
            return URI.class;
        //special case for locale codeList.
        } else if (className.equalsIgnoreCase("LanguageCode")) {
            return Locale.class;
        } else if (className.equalsIgnoreCase("CountryCode")) {
            return String.class;
        } else if (className.equalsIgnoreCase("RO_SystRefCode")) {
            return String.class;
        } else if (className.equalsIgnoreCase("QName")) {
            return QName.class;
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
    private Class getClassFromName(Classe type, int mode) {
        String className    = type.getName();
        String standardName = type.getStandard().getName();

        Class result = classBinding.get(standardName + ':' + className);
        if (result == null) {
            LOGGER.finer("search for class " + className);
        } else {
            return result;
        }
        
        final String classNameSave = standardName + ':' + className;
        
        //for the primitive type we return java primitive type
        result = getPrimitiveTypeFromName(className, standardName);
        if (result != null) {
            classBinding.put(standardName + ':' + className, result);
            return result;
        }

        //special case TODO delete when geotoolkit/api will be updated.
        if (className.equals("CI_Date")) {
            className = "CitationDate";
        } else if (className.equals("RS_Identifier")) {
            className = "ReferenceIdentifier";
        } else if (className.equals("MD_ReferenceSystem")) {
            className = "ReferenceSystemMetadata";
        }

        List<String> packagesName;
        if (standardName.equals("Catalog Web Service") || standardName.equals("DublinCore") || 
            standardName.equals("OGC Web Service")     || standardName.equals("OGC Filter")) {
            packagesName = cswPackage;
            
        } else if (standardName.equals("Ebrim v3.0") || standardName.equals("Web Registry Service v1.0")) {
            packagesName = ebrimV3Package;
            
        } else if (standardName.equals("Ebrim v2.5") || standardName.equals("Web Registry Service v0.9")) {
            packagesName = ebrimV25Package;
        
        } else if (standardName.equals("SensorML")) {
            packagesName = sensorMLPackage;

        } else if (standardName.equals("Sensor Web Enablement")) {
            packagesName = swePackage;

        } else if (standardName.equals("ISO 19108") && mode == SENSORML) {
            packagesName = gmlPackage;

        } else {
            if (!className.contains("Code") && !className.equals("DCPList") && !className.equals("SV_CouplingType") && !className.equals("AxisDirection")) {
                packagesName = geotoolkitPackage;
            } else {
                packagesName = opengisPackage;
            }
        }


        for (String packageName : packagesName) {
            
            //TODO remove this special case
            if (className.equals("RS_Identifier"))
                packageName = "org.geotoolkit.referencing";
            else if (className.equals("MD_ScopeCode"))
                packageName = "org.opengis.metadata.maintenance";
            else if (className.equals("SV_ServiceIdentification")) 
                packageName = "org.geotoolkit.service";
            else if (className.startsWith("FRA_")) 
                packageName = "org.geotoolkit.metadata.fra";
            else if (className.equals("ReferenceSystemMetadata"))
                packageName = "org.geotoolkit.internal.jaxb.metadata";
            
            String name = className;
            int nameType = 0;
            while (nameType < 10) {
                try {
                    LOGGER.finer("searching: " + packageName + '.' + name);
                    result = Class.forName(packageName + '.' + name);
                    
                    //if we found the class we store and return it
                    classBinding.put(standardName + ':' + className, result);
                    LOGGER.finer("class found:" + packageName + '.' + name);
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
                        //for the temporal element we remove "Time" prefix
                        case 1: {
                            if (name.indexOf("Code") != -1 && name.indexOf("CodeSpace") == -1) {
                                name = name.substring(0, name.indexOf("Code"));
                            }
                            if (name.startsWith("Time") && mode != SENSORML) {
                                name = name.substring(4);
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
                        // we put Type behind the className
                        case 3: {
                            name = name.substring(0, name.indexOf("Impl"));
                            name += "Type";
                            nameType = 4;
                            break;
                        }
                         // we put Entry behind the className
                        case 4: {
                            name = name.substring(0, name.indexOf("Type"));
                            name += "Entry";
                            nameType = 5;
                            break;
                        }
                        // we put Default before the className
                        case 5: {
                            name = name.substring(0, name.indexOf("Entry"));
                            name = "Default" + name;
                            nameType = 6;
                            break;
                        }
                        // we put FRA before the className
                        case 6: {
                            name = "FRA" + name;
                            nameType = 7;
                            break;
                        }
                        // we put PropertyType behind the className
                        case 7: {
                            name = name.substring(10, name.length());
                            name += "PropertyType";
                            nameType = 8;
                            break;
                        }
                        // we put Abstract before the className
                        case 8: {
                            name = name.substring(0, name.indexOf("PropertyType"));
                            name = "Abstract" + name;
                            nameType = 9;
                            break;
                        }
                        default:
                            nameType = 10;
                            break;
                    }

                }
            }
        }
        if (!classeNotFound.contains(classNameSave)) {
            LOGGER.severe("class not found: " + classNameSave);
            classeNotFound.add(classNameSave);
        }
        return null;
    }

    @Override
    public void destroy() {
        try {
            mdReader.close();
            if (storeMapping) {
                storeClassBinding();
            }
            classBinding.clear();
            alreadyRead.clear();
        } catch (MD_IOException ex) {
            LOGGER.severe("SQL Exception while destroying MDWeb MetadataReader");
        }
    }

    private void storeClassBinding() {
        FileOutputStream out = null;
        try {
            // todo find where to write this file
            final File bindingFile = new File("classMapping.properties");
            out = new FileOutputStream(bindingFile);
            final Properties prop = new Properties();
            for (Entry<String, Class> entry : classBinding.entrySet()) {
                prop.put(entry.getKey(), entry.getValue().getName());
            }
            LOGGER.info("stored " + classBinding.size() + " classes in classMapping.properties");
            prop.store(out, "save at reader destroy");

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public List<? extends Object> getAllEntries() throws MetadataIoException {
        final List<Object> results = new ArrayList<Object>();
        try {
            final List<RecordSet> recordSets = mdReader.getRecordSets();
            final List<Form> forms       = mdReader.getAllForm(recordSets);
            for (Form f: forms) {
                results.add(getObjectFromForm("no cache", f, -1));
            }
        } catch (MD_IOException ex) {
            throw new MetadataIoException("SQL Exception while getting all the entries: " +ex.getMessage());
        }
        return results;
    }

    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Add a metadata to the cache.
     * @param identifier The metadata identifier.
     * @param metadata The object to put in cache.
     */
    @Override
    public void removeFromCache(String identifier) {
        if (super.isCacheEnabled()) {
            int id;
            String recordSetCode = "";

            //we parse the identifier (Form_ID:RecordSet_Code)
            try {
                if (identifier.indexOf(':') != -1) {
                    recordSetCode = identifier.substring(identifier.indexOf(':') + 1, identifier.length());
                    identifier  = identifier.substring(0, identifier.indexOf(':'));
                    id = Integer.parseInt(identifier);
                } else {
                    throw new NumberFormatException();
                }

                final RecordSet recordSet = mdReader.getRecordSet(recordSetCode);

                mdReader.removeFormFromCache(recordSet, id);

            } catch (MD_IOException ex) {
                LOGGER.severe("SQLException while removing " + identifier + " from the cache");
                return;
            } catch (NumberFormatException e) {
                LOGGER.severe("NumberFormat while removing " + identifier + " from the cache");
                return;
            }
            super.removeFromCache(identifier);
        }
    }
}
