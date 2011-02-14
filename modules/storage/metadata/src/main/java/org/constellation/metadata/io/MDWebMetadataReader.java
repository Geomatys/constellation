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
import java.io.InputStream;
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
import javax.sql.DataSource;
import javax.xml.namespace.QName;

// Constellation Dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.ReflectionUtilities;
        
// MDWeb dependencies
import org.constellation.util.Util;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.v20.Reader20;
import org.mdweb.io.Reader;
import org.mdweb.io.sql.v21.Reader21;

// Geotoolkit dependencies
import org.geotoolkit.metadata.iso.MetadataEntity;
import org.geotoolkit.internal.CodeLists;
import org.geotoolkit.io.wkt.UnformattableObjectException;
import org.geotoolkit.naming.DefaultLocalName;
import org.geotoolkit.naming.DefaultNameFactory;
import org.geotoolkit.resources.Locales;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.geotoolkit.util.DefaultInternationalString;
import org.geotoolkit.util.FileUtilities;

// GeoAPI dependencies
import org.geotoolkit.util.StringUtilities;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.CodeList;
import org.opengis.util.UnlimitedInteger;


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
    private final Map<String, Class> classBinding;
    
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
     * A map of standardName / List of package axtract from a properties file
     */
    private final Map<String, List<String>> extraPackage = new HashMap<String, List<String>>();

    /**
     * A list of package containing the GML implementation (JAXB binding not referencing)
     */
    private List<String> gmlPackage;

    /**
     * A list of package containing the ISO 19115-2 implementation
     */
    private List<String> geotkAcquisitionPackage;

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
     * A List of the already logged missing MDWeb Classe.
     */
    private final List<String> classeNotFound = new ArrayList<String>();

    private boolean storeMapping = false;

    /**
     * Build a new metadata Reader.
     * 
     * @param MDReader a reader to the MDWeb database.
     */
    public MDWebMetadataReader(final Automatic configuration) throws MetadataIoException {
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
            final DataSource dataSource   = db.getDataSource();
            final boolean isPostgres      = db.getClassName().equals("org.postgresql.Driver");
            String version                = null;
            if (dataSource == null) {
                throw new MetadataIoException("Unable to instanciate a dataSource.");
            }
            final Connection mdConnection = dataSource.getConnection();
            final Statement versionStmt   = mdConnection.createStatement();
            final ResultSet result        = versionStmt.executeQuery("Select * FROM \"version\"");
            if (result.next()) {
                version = result.getString(1);
            }
            result.close();
            versionStmt.close();
            mdConnection.close();

            if (version != null && version.startsWith("2.0")) {
                mdReader = new Reader20(dataSource, isPostgres);
            } else if (version != null && (version.startsWith("2.1") || version.startsWith("2.2"))) {
                mdReader = new Reader21(dataSource, isPostgres);
            } else {
                throw new MetadataIoException("unexpected database version:" + version);
            }
            mdReader.setProperty("readProfile", false);
        } catch (SQLException ex) {
            throw new MetadataIoException("SQLException while initializing the MDWeb reader:" +'\n'+
                                           "cause:" + ex.getMessage());
        }

        if (configuration.getEnableThread() != null && !configuration.getEnableThread().isEmpty()) {
            final boolean t = Boolean.parseBoolean(configuration.getEnableThread());
            if (t) {
                LOGGER.info("parrallele treatment enabled");
            }
            setIsThreadEnabled(t);
        }

        if (configuration.getEnableCache() != null && !configuration.getEnableCache().isEmpty()) {
            final boolean c = Boolean.parseBoolean(configuration.getEnableCache());
            if (!c) {
                LOGGER.info("cache system have been disabled");
            }
            setIsCacheEnabled(c);
            mdReader.setProperty("cacheStorage", c);
        }

        if (configuration.getStoreMapping() != null && !configuration.getStoreMapping().isEmpty()) {
            final boolean m = Boolean.parseBoolean(configuration.getStoreMapping());
            if (m) {
                LOGGER.info("mapping storage enabled");
            }
            storeMapping = m;
        }

        initPackage();
        this.classBinding       = initClassBinding(configuration.getConfigurationDirectory());
        this.alreadyRead        = new HashMap<Value, Object>();
    }

    /**
     * A constructor used in profile Test .
     *
     * @param MDReader a reader to the MDWeb database.
     */
    protected MDWebMetadataReader(final DataSource mdConnection) {
        super(true, false);
        this.mdReader           = new Reader20(mdConnection);
        initPackage();
        this.classBinding       = new HashMap<String, Class>();
        this.alreadyRead        = new HashMap<Value, Object>();
    }

    /**
     * A constructor used in profile Test .
     *
     * @param MDReader a reader to the MDWeb database.
     */
    public MDWebMetadataReader(final Reader mdReader) {
        super(true, false);
        this.mdReader           = mdReader;
        initPackage();
        this.classBinding       = new HashMap<String, Class>();
        this.alreadyRead        = new HashMap<Value, Object>();
    }

    /**
     * Fill the package attributes with all the subPackage of the specified ones.
     */
    private void initPackage() {

        this.geotoolkitPackage  = FileUtilities.searchSubPackage("org.geotoolkit.metadata.iso", "org.geotoolkit.referencing",
                                                                 "org.geotoolkit.service", "org.geotoolkit.naming", "org.geotoolkit.feature.catalog",
                                                                 "org.geotoolkit.metadata.fra", "org.geotoolkit.temporal.object",
                                                                 "org.geotoolkit.util");
        this.sensorMLPackage    = FileUtilities.searchSubPackage("org.geotoolkit.sml.xml.v100");
        this.swePackage         = FileUtilities.searchSubPackage("org.geotoolkit.swe.xml.v100");
        this.gmlPackage         = FileUtilities.searchSubPackage("org.geotoolkit.gml.xml.v311");

        this.opengisPackage     = FileUtilities.searchSubPackage("org.opengis.metadata", "org.opengis.referencing", "org.opengis.temporal",
                                                               "org.opengis.service", "org.opengis.feature.catalog");
        this.cswPackage         = FileUtilities.searchSubPackage("org.geotoolkit.csw.xml.v202", "org.geotoolkit.dublincore.xml.v2.elements", "org.geotoolkit.ows.xml.v100",
                                                               "org.geotoolkit.ogc.xml");
        this.ebrimV3Package     = FileUtilities.searchSubPackage("org.geotoolkit.ebrim.xml.v300", "org.geotoolkit.wrs.xml.v100");
        this.ebrimV25Package    = FileUtilities.searchSubPackage("org.geotoolkit.ebrim.xml.v250", "org.geotoolkit.wrs.xml.v090");
        this.geotkAcquisitionPackage = FileUtilities.searchSubPackage("org.geotoolkit.metadata.imagery", "org.geotoolkit.metadata.iso.acquisition",
                                                                      "org.geotoolkit.metadata.iso.quality", "org.geotoolkit.metadata.iso.spatial",
                                                                      "org.geotoolkit.metadata.iso.lineage", "org.geotoolkit.metadata.iso.content",
                                                                      "org.opengis.metadata.acquisition", "org.opengis.metadata.content");
        // we add the extra binding extracted from a properties file
        try {
            final InputStream extraIn = Util.getResourceAsStream("org/constellation/metadata/io/extra-package.properties");
            if (extraIn != null) {
                final Properties extraProperties = new Properties();
                extraProperties.load(extraIn);
                extraIn.close();
                for (Entry<Object, Object> entry : extraProperties.entrySet()) {
                    final String standardName = (String) entry.getKey();
                    List<String> packageList  = StringUtilities.toStringList((String) entry.getValue());
                    packageList               = FileUtilities.searchSubPackage(packageList.toArray(new String[packageList.size()]));
                    extraPackage.put(standardName, packageList);
                }
            } else {
                LOGGER.warning("Unable to find the extra-package properties file");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IO exception while reading extra package properties for MDW meta reader", ex);
        }
    }

    /**
     * Initialize the class binding between MDWeb database classes and java implementation classes.
     * 
     * We give the possibility to the user to add a configuration file making the mapping.
     * @return
     */
    private Map<String, Class> initClassBinding(final File configDir) {
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
                    LOGGER.log(Level.WARNING, "error in class binding initialization for class:{0}", className);
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
    public Object getMetadata(String identifier, final int mode, final List<QName> elementName) throws MetadataIoException {
        int id;
        String recordSetCode = "";
        
        //we parse the identifier (Form_ID:RecordSet_Code)
        try  {
            final int semiColonIndex = identifier.indexOf(':');
            if (semiColonIndex != -1) {

                recordSetCode = identifier.substring(semiColonIndex + 1);
                identifier    = identifier.substring(0, semiColonIndex);
                id            = Integer.parseInt(identifier);
            } else {
                throw new NumberFormatException("The identifer must follow the pattern Form_ID:RecordSet_Code");
            }
            
        } catch (NumberFormatException e) {
             throw new MetadataIoException("Unable to parse: " + identifier, null, "id");
        }

        try {
            alreadyRead.clear();
            final RecordSet recordSet = mdReader.getRecordSet(recordSetCode);

            //we look for cached object
            Object result = null;
            if (isCacheEnabled()) {
                result = getFromCache(identifier);
            }

            if (result == null) {
                final Form f = mdReader.getForm(recordSet, id);
                result       = getObjectFromForm(identifier, f, mode);
            } else {
                LOGGER.log(Level.FINER, "getting from cache: {0}", identifier);
            }
            return result;

        } catch (MD_IOException e) {
             throw new MetadataIoException("MD_IO Exception while reading the metadata: " + identifier, e, null, "id");
        }
    }
    
    /**
     * Return an object from a MDWeb formular.
     * 
     * @param form the MDWeb formular.
     * @param type An elementSet : BRIEF, SUMMARY, FULL. (default is FULL);
     * @param mode
     * 
     * @return a geotoolkit/constellation object representing the metadata.
     */
    protected Object getObjectFromForm(final String identifier, final Form form, final int mode) {

        if (form != null && form.getRoot() != null && form.getRoot().getType() != null) {
            final Value topValue = form.getRoot();
            final Object result  = getObjectFromValue(topValue, mode);
            
            //we put the full object in the already read metadatas.
            if (result != null && isCacheEnabled()) {
               addInCache(identifier, result);
            }
            return result;
        
        //debugging part to see why the form cannot be read.
        } else {
            if (form == null) {
                LOGGER.severe("form is null");
            } else if (form.getRoot() == null) {
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
    private Object getObjectFromValue(final Value value, final int mode) {
        Class classe = null;
        // we get the value's class
        if (value.getType() != null) {
            classe = getClassFromName(value.getType(), mode);
        } else {
            LOGGER.log(Level.WARNING, "Error null type for value:{0}", value.getIdValue());
            return null;
        }
        
        if (classe == null) {
            return null;
        }
        
        Object result;
        /*
         * 
         * if the value is a leaf => primitive type
         *
         */
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
                    if (element == null) {
                        LOGGER.warning("unable to find a codelist element for code:" + textValue + " in the codelist:" + codelist.getName());
                        return null;
                    } else {
                        Method method;
                        if (classe.getSuperclass() != null && classe.getSuperclass().equals(CodeList.class)) {
                            result = CodeLists.valueOf(classe, element.getName());

                        } else if (classe.isEnum()) {
                            method = ReflectionUtilities.getMethod("fromValue", classe, String.class);
                            result = ReflectionUtilities.invokeMethod(method, classe, element.getName());
                        } else {
                            LOGGER.log(Level.WARNING, "unknow codelist type:{0}", value.getType());
                            return null;
                        }
                    }
                    return result;
                } catch (NumberFormatException e) {
                    if (textValue != null && !textValue.isEmpty()) {
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
                return TemporalUtilities.parseDateSafe(textValue,true);

            } else if (classe.equals(Boolean.class)) {
                if (textValue == null || textValue.isEmpty() || textValue.equals("null")) {
                    return null;
                }
                return Boolean.valueOf(textValue);


            } else if (classe.equals(Locale.class)) {
                for (Locale candidate : Locale.getAvailableLocales()) {
                    if (candidate.getISO3Language().equalsIgnoreCase(textValue)) {
                        return candidate;
                    }
                }
                 return new Locale(textValue);

            // Patch for LocalName Class
            } else if (classe.equals(DefaultLocalName.class)) {
                final DefaultNameFactory facto = new DefaultNameFactory();
                return facto.createLocalName(null, textValue);

            // else we use a String constructor
            } else {
                //we execute the constructor
                result = ReflectionUtilities.newInstance(classe, textValue);

            }

        //if the value is a link
        } else if (value instanceof LinkedValue) {
            final LinkedValue lv = (LinkedValue) value;
            final Object tempobj = alreadyRead.get(lv.getLinkedValue());
            if (tempobj != null) {
                return tempobj;
            } else {
                return getObjectFromValue(lv.getLinkedValue(), mode);
            }

        // else if the value is a complex object
        } else {

            /**
             * Again another special case TypeName does not have a empty constructor (immutable)
             * and no setters so we must call the normal constructor.
             */
            final String className = classe.getSimpleName();
            if ("DefaultTypeName".equals(className)) {
                TextValue child = null;

                //We search the child of the TypeName
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue) {
                        child = (TextValue) childValue;
                        break;
                    }
                }
                if (child != null) {
                    final CharSequence cs = child.getValue();
                    final DefaultNameFactory facto = new DefaultNameFactory();
                    return facto.createTypeName(null, cs);
                } else {
                    LOGGER.severe("The typeName is mal-formed");
                    return null;
                }

            /*  WAIT FOR GEOTK PATCH
               
             }  else if ("DefaultMemberName".equals(className)) {
                TextValue child = null;

                //We search the child of the TypeName
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue) {
                        child = (TextValue) childValue;
                        break;
                    }
                }
                if (child != null) {
                    final CharSequence cs = child.getValue();
                    return new DefaultMemberName(null, cs, null);
                } else {
                    LOGGER.severe("The typeName is mal-formed");
                    return null;
                }*/

            /**
             * Again another special case QNAME does not have a empty constructor.
             * and no setters so we must call the normal constructor.
             */
            } else if ("QName".equals(className)) {
                String localPart    = null;
                String namespaceURI = null;

                //We search the children of the QName
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue) {
                        if ("localPart".equals(childValue.getPath().getName()))
                            localPart = ((TextValue)childValue).getValue();
                        else  if ("namespaceURI".equals(childValue.getPath().getName()))
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

            /**
             * Again another special case UnlimitedInteger does not have a empty constructor.
             * and no setters so we must call the normal constructor.
             */
            } else if (className.equals("UnlimitedInteger")) {
                String intValue    = null;
                String isInfinite  = null;

                //We search the children of the QName
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue) {
                        if (childValue.getPath().getName().equals("value"))
                            intValue = ((TextValue)childValue).getValue();
                        else  if (childValue.getPath().getName().equals("isInfinite"))
                            isInfinite = ((TextValue)childValue).getValue();
                    }
                }
                UnlimitedInteger u = null;
                if (intValue != null && !intValue.isEmpty()) {
                    try {
                        u = new UnlimitedInteger(Integer.parseInt(intValue));
                    } catch (NumberFormatException ex) {
                        LOGGER.warning("Unable to parse value for Unlimited Integer: " + intValue);
                    }
                }

                // if the flag isInfinite is set to true we overwrite the int value.
                if (isInfinite != null && !isInfinite.isEmpty()) {
                    boolean inf = Boolean.parseBoolean(isInfinite);
                    if (inf) {
                        u = UnlimitedInteger.POSITIVE_INFINITY;
                    }
                }
                return u;

            /**
             * Again another special case PT_FreeText has a special construction.
             */
            } else if ("DefaultInternationalString".equals(className)) {

                String ptvalue = null;
                final Map<Locale, String> map = new HashMap<Locale, String>();
                //We search the children of the value
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue) {
                        ptvalue = ((TextValue)childValue).getValue();
                    } else {
                        if (childValue.getType() != null && "LocalisedCharacterString".equals(childValue.getType().getName())) {
                            String lvalue = null;
                            String locale = null;
                            for (Value subchildValue : childValue.getChildren()) {
                                if (subchildValue instanceof TextValue && "value".equals(subchildValue.getPath().getName())) {
                                    lvalue = ((TextValue) subchildValue).getValue();
                                } else if (subchildValue instanceof TextValue && "locale".equals(subchildValue.getPath().getName())) {
                                    locale = ((TextValue) subchildValue).getValue();
                                }
                            }
                            if (lvalue != null && locale != null) {
                                if (locale.startsWith("#locale-")) {
                                    locale = locale.substring(locale.indexOf('-') + 1);
                                    final Locale loc = Locales.parse(locale);
                                    map.put(loc, lvalue);
                                } else {
                                    LOGGER.warning("Malformed values: child of LocalisedCharacterString `\"locale\"does not starts with '#locale-'");
                                }
                            } else {
                                LOGGER.warning("Malformed values: child of LocalisedCharacterString does not contains a value and a locale");
                            }
                        } else {
                            LOGGER.warning("Malformed values: Child of PT_FreeText is not a LocalisedCharacterString");
                        }
                    }
                }
                final DefaultInternationalString resultIS = new DefaultInternationalString(ptvalue);
                for (Entry<Locale, String> entry : map.entrySet()) {
                    resultIS.add(entry.getKey(), entry.getValue());
                }
                return resultIS;
            }

            /**
             * normal case
             * we get the empty constructor
             */
            result = ReflectionUtilities.newInstance(classe);
            alreadyRead.put(value, result);
        }

        if (result != null) {
            //if the result is a subClasses of MetaDataEntity
            Map<String, Object> metaMap = null;
            boolean isMeta  = false;
            if (result instanceof MetadataEntity) {
                final MetadataEntity meta = (MetadataEntity) result;
                metaMap = meta.asMap();
                isMeta  = true;
            }

            // then we search the setter for all the child value
            for (Value childValue : value.getChildren()) {

                final Path path = childValue.getPath();
                

                // we get the object from the child Value
                final Object param = getObjectFromValue(childValue, mode);
                if (param == null) {
                    continue;
                }
                //we try to put the parameter in the parent object
                // by searching for the good attribute name
                String attribName = path.getName();

                if (mode != SENSORML) {
                    //special case due to a bug in mdweb
                    if (attribName.startsWith("geographicElement")) {
                        attribName = "geographicElements";
                    } else if ("transformationParameterAvailability".equals(attribName)) {
                        attribName = "transformationParameterAvailable";
                    } else if ("beginPosition".equals(attribName)) {
                        attribName = "begining";
                    } else if ("endPosition".equals(attribName)) {
                        attribName = "ending";
                    } else if ("value".equals(attribName) && "DefaultPosition".equals(classe.getSimpleName())) {
                        attribName = "position";
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
                    } /*else if (attribName.equalsIgnoreCase("codeSpace") && !(result instanceof DefaultIdentifier)) {
                        attribName = "codespace";
                    }*/
                }

                boolean putSuceed = false;
                if (isMeta) {
                      putSuceed = putMeta(metaMap, attribName, param, result, path);
                }

                if (!putSuceed) {
                    final Method setter = ReflectionUtilities.getSetterFromName(attribName, param.getClass(), classe);

                    if (setter != null) {
                        ReflectionUtilities.invokeMethod(setter, result, param);
                    } else {

                        if (mode != SENSORML  && attribName.equalsIgnoreCase("identifier")) {
                            attribName = "name";
                        }
                        final Field field = ReflectionUtilities.getFieldFromName(attribName, classe);

                        if (field != null) {
                            setFieldToValue(field, attribName, result, param);
                        } else {
                            LOGGER.warning("no field " + attribName + " in class:" + classe.getName() + '\n'
                                         + "currentPath:" + path.getId());
                        }
                    }
                }
            }
        }
        return result;
    }


    private boolean putMeta(final Map<String, Object> metaMap, String attribName, final Object param, final Object result, final Path path) {
        boolean tryAgain = true;
        int casee = 0;
        while (tryAgain) {
            try {
                metaMap.put(attribName, param);
                return true;
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.FINER, e.getMessage(), e);
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
                        casee = 3;
                        break;
                    default:

                        LOGGER.severe("unable to put " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                        tryAgain = false;
                }
            } catch (ClassCastException ex) {
                LOGGER.severe("Exception while putting in geotoolkit metadata: " + '\n'
                        + "cause: " + ex.getMessage());
                tryAgain = false;
            }
        }
        return false;
    }

    private void setFieldToValue(final Field field, final String attribName, final Object result, final Object param) {
        field.setAccessible(true);
        try {
            if ("axis".equals(attribName)) {
                final CoordinateSystemAxis[] params = new CoordinateSystemAxis[1];
                params[0] = (CoordinateSystemAxis) param;
                field.set(result, params);
            } else if (field.getType().isArray()) {
                // todo find how to build a typed array
                final Object[] params = new Object[1];
                params[0] = param;
                field.set(result, params);

            } else if (field.getType().equals(Unit.class)) {

                final Unit<?> unit = Unit.valueOf((String) param);
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
            LOGGER.severe("IllegalArgumentException:" + ex.getMessage() + '\n'
                        + "while setting the parameter: " + objectStr + '\n'
                        + "to the field: " + field + ".");
        }
    }

    /**
     * Return a class (java primitive type) from a class name.
     * 
     * @param className the standard name of a class. 
     * @return a primitive class.
     */
    private static Class getPrimitiveTypeFromName(final String className, final String standardName) {

        if (className.equalsIgnoreCase("CharacterString")) {
            return String.class;
        } else if (className.equalsIgnoreCase("Date")) {
            return Date.class;
        } else if (className.equalsIgnoreCase("PT_FreeText")) {
            return DefaultInternationalString.class;
        } else if (className.equalsIgnoreCase("Decimal") || className.equalsIgnoreCase("Double")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("Real")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("Integer")) {
            return Integer.class;
        } else if (className.equalsIgnoreCase("Boolean") && !"Sensor Web Enablement".equals(standardName)) {
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
        } else if (className.equalsIgnoreCase("UnlimitedInteger")) {
            return UnlimitedInteger.class;
        } else {
            return null;
        }
    }

    /**
     * Return a set of package to explore in function on the standard of the mdweb Classe and the mode.
     *
     * @param standardName
     * @param className
     * @param mode
     * @return
     */
    private List<String> getPackageFromStandard(final String standardName, final String className, final int mode) {
        final List<String> packagesName = new ArrayList<String>();
        
        if ("Catalog Web Service".equals(standardName) || "DublinCore".equals(standardName) ||
            "OGC Web Service".equals(standardName)     || "OGC Filter".equals(standardName)) {
            packagesName.addAll(cswPackage);

        } else if ("Ebrim v3.0".equals(standardName) || "Web Registry Service v1.0".equals(standardName)) {
            packagesName.addAll(ebrimV3Package);

        } else if ("Ebrim v2.5".equals(standardName) || "Web Registry Service v0.9".equals(standardName)) {
            packagesName.addAll(ebrimV25Package);

        } else if ("SensorML".equals(standardName)) {
            packagesName.addAll(sensorMLPackage);

        } else if ("Sensor Web Enablement".equals(standardName)) {
            packagesName.addAll(swePackage);

        } else if ("ISO 19108".equals(standardName) && mode == SENSORML) {
            packagesName.addAll(gmlPackage);

        } else if ("ISO 19115-2".equals(standardName)) {
            packagesName.addAll(geotkAcquisitionPackage);

        } else if (extraPackage.containsKey(standardName)) {
            packagesName.addAll(extraPackage.get(standardName));

        } else {
            if (!className.contains("Code") && !"DCPList".equals(className) && !"SV_CouplingType".equals(className) && !"AxisDirection".equals(className)) {
                packagesName.addAll(geotoolkitPackage);
                packagesName.addAll(gmlPackage);
            } else {
                packagesName.addAll(opengisPackage);
            }
        }
        return packagesName;
    }

    /**
     * Search an implementation for the specified class name.
     * 
     * @param className a standard class name.
     * 
     * @return a class object corresponding to the specified name.
     */
    private Class getClassFromName(final Classe type, final int mode) {
        String className          = type.getName();
        final String standardName = type.getStandard().getName();

        final String classNameSave = standardName + ':' + className;

        Class result = classBinding.get(classNameSave);
        if (result == null) {
            LOGGER.log(Level.FINER, "search for class {0}", classNameSave);
        } else {
            return result;
        }
        
        //for the primitive type we return java primitive type
        result = getPrimitiveTypeFromName(className, standardName);
        if (result != null) {
            classBinding.put(classNameSave, result);
            return result;
        }

        //special case TODO delete when geotoolkit/api will be updated.
        if ("CI_Date".equals(className)) {
            className = "CitationDate";
        } else if ("RS_Identifier".equals(className)) {
            className = "ReferenceIdentifier";
        } else if ("MD_ReferenceSystem".equals(className)) {
            className = "ReferenceSystemMetadata";
        }

        final List<String> packagesName = getPackageFromStandard(standardName, className, mode);

        for (String packageName : packagesName) {
            
            //TODO remove this special case
            /*if ("RS_Identifier".equals(className))
                packageName = "org.geotoolkit.referencing";
            else*/ if ("MD_ScopeCode".equals(className))
                packageName = "org.opengis.metadata.maintenance";
            else if ("SV_ServiceIdentification".equals(className))
                packageName = "org.geotoolkit.service";
            else if (className.startsWith("FRA_")) 
                packageName = "org.geotoolkit.metadata.fra";
            else if ("ReferenceSystemMetadata".equals(className))
                packageName = "org.geotoolkit.internal.jaxb.metadata";
            else if ("Anchor".equals(className))
                packageName = "org.geotoolkit.internal.jaxb.text";
            String name  = className;
            int nameType = 0;
            while (nameType < 10) {
                try {
                    LOGGER.finer("searching: " + packageName + '.' + name);
                    result = Class.forName(packageName + '.' + name);
                    
                    //if we found the class we store and return it
                    classBinding.put(classNameSave, result);
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
                            name = name.substring(0, name.lastIndexOf("Type"));
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
            LOGGER.log(Level.WARNING, "class not found: {0}", classNameSave);
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
            LOGGER.log(Level.INFO, "stored {0} classes in classMapping.properties", classBinding.size());
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
                LOGGER.log(Level.SEVERE, "SQLException while removing {0} from the cache", identifier);
                return;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, "NumberFormat while removing {0} from the cache", identifier);
                return;
            }
            super.removeFromCache(identifier);
        }
    }
}
