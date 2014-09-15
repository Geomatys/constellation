/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.metadata.io;

// J2SE dependencies

import org.apache.sis.internal.jaxb.LegacyNamespaces;
import org.apache.sis.io.wkt.UnformattableObjectException;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.util.Locales;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.util.iso.DefaultLocalName;
import org.apache.sis.util.iso.DefaultNameFactory;
import org.apache.sis.util.iso.Types;
import org.apache.sis.xml.IdentifiedObject;
import org.apache.sis.xml.IdentifierSpace;
import org.apache.sis.xml.XLink;
import org.apache.sis.xml.XML;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.util.ReflectionUtilities;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.UnlimitedInteger;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.MD_IOFactory;
import org.mdweb.io.Reader;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.CodeList;
import org.opengis.util.TypeName;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.imageio.spi.ServiceRegistry;
import javax.measure.unit.Unit;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;

// Constellation Dependencies
// MDWeb dependencies
// Geotoolkit dependencies
// GeoAPI dependencies


/**
 * A database Reader designed for an MDweb database.
 *
 * It read The MDweb records into the database and instantiate them into GeotoolKit object.
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
     * A map containing the mapping between the MDWeb className and java typeName
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
     * A map of standardName / List of package extract from a properties file
     */
    private final Map<String, List<String>> extraPackage = new HashMap<>();

    /**
     * A list of package containing the GML implementation (JAXB binding not referencing)
     */
    private List<String> gml32Package;
    private List<String> gml31Package;

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
     * A list of package containing various implementation
     */
    private List<String> otherPackage;

    /**
     * A List of the already see object for the current metadata read
     * (in order to avoid infinite loop)
     */
    protected Map<Value, Object> alreadyRead;

    /**
     * A List of the already logged missing MDWeb {@link Classe}.
     */
    private final List<String> classeNotFound = new ArrayList<>();

    private boolean storeMapping = false;

    private static final TimeZone tz = TimeZone.getTimeZone("GMT+2:00");

    private final boolean indexOnlyPublished;

    /**
     * Build a new metadata Reader.
     *
     * @param configuration the configuration object conatining the database informations.
     * @throws org.constellation.metadata.io.MetadataIoException
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
            if (dataSource == null) {
                throw new MetadataIoException("Unable to instanciate a dataSource.");
            }
            MD_IOFactory factory = null;
            final Iterator<MD_IOFactory> ite = ServiceRegistry.lookupProviders(MD_IOFactory.class);
            while (ite.hasNext()) {
                MD_IOFactory currentFactory = ite.next();
                if (currentFactory.matchImplementationType(dataSource, isPostgres)) {
                    factory = currentFactory;
                }
            }
            if (factory != null) {
                mdReader = factory.getPooledInstance(db.getConnectURL(), dataSource, isPostgres);
                mdReader.setProperty("readProfile", false);
            } else {
                throw new MetadataIoException("unable to find a MD_IO factory");
            }
        } catch (SQLException ex) {
            throw new MetadataIoException("SQLException while initializing the MDWeb reader:" +'\n'+
                                           "cause:" + ex.getMessage());
        } catch (MD_IOException ex) {
            throw new MetadataIoException("MD_IOException while initializing the MDWeb reader:" +'\n'+
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

        this.indexOnlyPublished = configuration.getIndexOnlyPublishedMetadata();

        initPackage();
        this.classBinding       = initClassBinding(configuration.getConfigurationDirectory());
        this.alreadyRead        = new HashMap<>();
    }

    /**
     * A constructor used in profile Test .
     *
     * @param mdReader a reader to the MDWeb database.
     */
    public MDWebMetadataReader(final Reader mdReader) {
        super(true, false);
        this.mdReader           = mdReader;
        initPackage();
        this.classBinding       = new HashMap<>();
        this.alreadyRead        = new HashMap<>();
        this.indexOnlyPublished = true;
    }

    /**
     * Fill the package attributes with all the subPackage of the specified ones.
     */
    private void initPackage() {

        this.geotoolkitPackage  = FileUtilities.searchSubPackage("org.apache.sis.metadata.iso",
                                                                 "org.geotoolkit.metadata.iso",
                                                                 "org.apache.sis.referencing",
                                                                 "org.geotoolkit.referencing",
                                                                 "org.geotoolkit.service",
                                                                 "org.apache.sis.util.iso",
                                                                 "org.geotoolkit.naming",
                                                                 "org.geotoolkit.feature.catalog",
                                                                 "org.apache.sis.internal.profile.fra", "org.geotoolkit.util", "org.geotoolkit.xml");
        this.sensorMLPackage    = FileUtilities.searchSubPackage("org.geotoolkit.sml.xml.v100");
        this.swePackage         = FileUtilities.searchSubPackage("org.geotoolkit.swe.xml.v100");
        this.gml31Package         = FileUtilities.searchSubPackage("org.geotoolkit.gml.xml.v311","org.geotoolkit.gml.xml");
        this.gml32Package         = FileUtilities.searchSubPackage("org.geotoolkit.gml.xml.v321","org.geotoolkit.gml.xml");

        this.opengisPackage     = FileUtilities.searchSubPackage("org.opengis.metadata", "org.opengis.referencing", "org.opengis.temporal", "org.opengis.feature.catalog");
        this.cswPackage         = FileUtilities.searchSubPackage("org.geotoolkit.csw.xml.v202", "org.geotoolkit.dublincore.xml.v2.elements", "org.geotoolkit.ows.xml.v100",
                                                               "org.geotoolkit.ogc.xml.v110","org.geotoolkit.csw.xml");
        this.ebrimV3Package     = FileUtilities.searchSubPackage("org.geotoolkit.ebrim.xml.v300", "org.geotoolkit.wrs.xml.v100");
        this.ebrimV25Package    = FileUtilities.searchSubPackage("org.geotoolkit.ebrim.xml.v250", "org.geotoolkit.wrs.xml.v090");
        this.geotkAcquisitionPackage = FileUtilities.searchSubPackage("org.apache.sis.internal.jaxb.gmi",
                                                                      "org.apache.sis.metadata.iso.acquisition",
                                                                      "org.geotoolkit.metadata.iso.quality",
                                                                      "org.apache.sis.metadata.iso.quality",
                                                                      "org.apache.sis.metadata.iso.spatial",
                                                                      "org.apache.sis.metadata.iso.lineage",
                                                                      "org.apache.sis.metadata.iso.content",
                                                                      "org.opengis.metadata.acquisition", "org.opengis.metadata.content");
        this.otherPackage       = FileUtilities.searchSubPackage("org.geotoolkit.gts.xml");
        // we add the extra binding
        final Iterator<ExtraMappingFactory> ite = ServiceRegistry.lookupProviders(ExtraMappingFactory.class);
        while (ite.hasNext()) {
            final ExtraMappingFactory currentFactory = ite.next();
            extraPackage.putAll(currentFactory.getExtraPackage());
        }
    }

    /**
     * Initialize the class binding between MDWeb database classes and java implementation classes.
     *
     * We give the possibility to the user to add a configuration file making the mapping.
     * @return
     */
    private Map<String, Class> initClassBinding(final File configDir) {
        final Map<String, Class> result = new HashMap<>();
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
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     *
     * @return A metadata Object (Dublin core Record / GeotoolKit metadata / EBrim registry object)
     *
     * @throws MetadataIoException
     */
    @Override
    public Node getMetadata(String identifier, final MetadataType mode) throws MetadataIoException {
        try {
            alreadyRead.clear();

            //we look for cached object
            Object result = null;
            if (isCacheEnabled()) {
                result = getFromCache(identifier);
            }

            if (result == null) {
                final FullRecord f = mdReader.getRecord(identifier);
                if (mode == MetadataType.NATIVE) {
                    result = f;
                } else {
                    result = getObjectFromRecord(identifier, f, mode);
                }
            } else {
                LOGGER.log(Level.FINER, "getting from cache: {0}", identifier);
            }

            // marshall to DOM
            if (result != null) {
                return writeObjectInNode(result, mode);
            }
            return null;
        } catch (MD_IOException e) {
             throw new MetadataIoException("MD_IO Exception while reading the metadata: " + identifier, e, null, "id");
        }
    }

    protected Node writeObjectInNode(final Object obj, final MetadataType mode) throws MetadataIoException {
        final boolean replace = mode == MetadataType.ISO_19115;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            final Marshaller marshaller = EBRIMMarshallerPool.getInstance().acquireMarshaller();
            final MarshallWarnings warnings = new MarshallWarnings();
            marshaller.setProperty(XML.CONVERTER, warnings);
            marshaller.setProperty(XML.TIMEZONE, tz);
            marshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            marshaller.setProperty(XML.GML_VERSION, LegacyNamespaces.VERSION_3_2_1);
            marshaller.marshal(obj, document);

            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }

    @Override
    public boolean existMetadata(String identifier) throws MetadataIoException {
        try {
            return mdReader.isAlreadyUsedIdentifier(identifier);
        } catch (MD_IOException ex) {
            throw  new MetadataIoException(ex);
        }
    }


    /**
     * Return an object from a MDWeb record.
     *
     * @param identifier The metadata Identifier.
     * @param record the MDWeb record.
     * @param mode The data type (EBRIM, SENSORML, ISO)
     *
     * @return a GeotoolKit/constellation object representing the metadata.
     */
    public Object getObjectFromRecord(final String identifier, final FullRecord record, final MetadataType mode) {

        if (record != null && record.getRoot() != null && record.getRoot().getType() != null) {
            final Value topValue = record.getRoot();
            final Object result  = getObjectFromValue(topValue, mode);

            //we put the full object in the already read metadatas.
            if (result != null && isCacheEnabled()) {
               addInCache(identifier, result);
            }
            return result;

        //debugging part to see why the record cannot be read.
        } else {
            if (record == null) {
                LOGGER.warning("record is null");
            } else if (record.getRoot() == null) {
                LOGGER.severe("Top value is null");
            } else {
                LOGGER.severe("Top value Type is null");
            }
            return null;
        }
    }

    /**
     * Return a GeotoolKit object from a MDWeb value (this value can be see as a tree).
     * This method build the value and all is attribute recursively.
     *
     * @param record the MDWeb record containing this value.
     * @param value The value to build.
     *
     * @return a GeotoolKit metadata object.
     */
    private Object getObjectFromValue(final Value value, final MetadataType mode) {
        final Class classe;
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
                            result = Types.forCodeName(classe, element.getName(), true);

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
                return TemporalUtilities.parseDateSafe(textValue,true, false);

            } else if (classe.equals(Boolean.class)) {
                if (textValue == null || textValue.isEmpty() || textValue.equals("null")) {
                    return null;
                }
                return Boolean.valueOf(textValue);


            } else if (classe.equals(Locale.class)) {
                return Locales.parse(textValue);

            // patch for backSlash in URI
            } else if (classe.equals(URI.class)) {
                textValue = textValue.replace("\\", "%5C");
                try {
                    return new URI(textValue);
                } catch (URISyntaxException ex) {
                    LOGGER.log(Level.WARNING, "URI syntax exception for:{0}", textValue);
                    return null;
                }

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



             }  else if ("DefaultMemberName".equals(className)) {
                TextValue child = null;
                Value typeChild = null;
                //We search the children of the MemberName (one String value, and one TypeName)
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue) {
                        child = (TextValue) childValue;
                    } else {
                        typeChild = childValue;
                    }
                }
                if (child != null) {
                    final CharSequence cs = child.getValue();
                    final DefaultNameFactory facto = new DefaultNameFactory();
                    if (typeChild != null) {
                        final TypeName tn = (TypeName) getObjectFromValue(typeChild, mode);
                        return facto.createMemberName(null, cs, tn);
                    } else {
                        LOGGER.warning("The memberName is mal-formed (no attributeType value)");
                        return null;
                    }
                } else {
                    LOGGER.warning("The memberName is mal-formed (no aName value)");
                    return null;
                }

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
                        if ("localPart".equals(childValue.getPath().getName())) {
                            localPart = ((TextValue)childValue).getValue();
                        } else if ("namespaceURI".equals(childValue.getPath().getName())) {
                            namespaceURI = ((TextValue)childValue).getValue();
                        }
                    }
                }
                if (localPart != null && namespaceURI != null) {
                    result = ReflectionUtilities.newInstance(classe, namespaceURI, localPart);
                    return result;
                } else {
                    LOGGER.warning("The QName is mal-formed");
                    return null;
                }

            /**
             * Again another special case UnlimitedInteger does not have a empty constructor.
             * and no setters so we must call the normal constructor.
             */
            } else if ("UnlimitedInteger".equals(className)) {
                String intValue    = null;
                String isInfinite  = null;

                //We search the children of the QName
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue) {
                        if (childValue.getPath().getName().equals("value")) {
                            intValue = ((TextValue)childValue).getValue();
                        } else if (childValue.getPath().getName().equals("isInfinite")) {
                            isInfinite = ((TextValue)childValue).getValue();
                        }
                    }
                }
                UnlimitedInteger u = null;
                if (intValue != null && !intValue.isEmpty()) {
                    try {
                        u = new UnlimitedInteger(Integer.parseInt(intValue));
                    } catch (NumberFormatException ex) {
                        LOGGER.log(Level.WARNING, "Unable to parse value for Unlimited Integer: {0}", intValue);
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
                final Map<Locale, String> map = new HashMap<>();
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

            /**
             * Again another special case PT_Locale has a special construction.
             */
            } else if ("Locale".equals(className)) {
                String countryValue  = null;
                String languageValue = null;
                for (Value childValue : value.getChildren()) {
                    if (childValue instanceof TextValue && "languageCode".equals(childValue.getPath().getName())) {
                        languageValue = ((TextValue)childValue).getValue();
                    } else if (childValue instanceof TextValue && "country".equals(childValue.getPath().getName())) {
                        countryValue = ((TextValue)childValue).getValue();
                    }
                }
                Locale language = Locales.parse(languageValue);
                Locale country  = Locales.unique(new Locale("", countryValue));
                if (language == null) {
                    language = country;
                } else if (country != null) {
                    // Merge the language and the country in a single Locale instance.
                    final String c = country.getCountry();
                    if (!c.equals(language.getCountry())) {
                        language = Locales.unique(new Locale(language.getLanguage(), c));
                    }
                }
                return language;
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
            if (result instanceof ISOMetadata) {
                final ISOMetadata meta = (ISOMetadata) result;
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

                if (mode != MetadataType.SENSORML) {
                    //special case due to a bug in mdweb
                    if (attribName.startsWith("geographicElement")) {
                        attribName = "geographicElements";
                    } else if ("transformationParameterAvailability".equals(attribName)) {
                        attribName = "transformationParameterAvailable";
                    } else if (attribName.equalsIgnoreCase("verticalCS")) {
                        attribName = "coordinateSystem";
                    } else if (attribName.equalsIgnoreCase("verticalDatum")) {
                        attribName = "datum";
                    } else if (attribName.equalsIgnoreCase("axisDirection")) {
                        attribName = "direction";
                    } else if (attribName.equalsIgnoreCase("axisAbbrev")) {
                        attribName = "abbreviation";
                    } else if (attribName.equalsIgnoreCase("uom")) {
                        attribName = "unit";
                    } else if (attribName.equalsIgnoreCase("axis")) {
                        attribName = "axes";
                    }
                }

                boolean putSuceed = false;
                if (isMeta) {
                    putSuceed = putMeta(metaMap, attribName, param, (ISOMetadata)result, path);
                }

                if (!putSuceed) {
                    final Method setter = ReflectionUtilities.getSetterFromName(attribName, param.getClass(), classe);

                    if (setter != null) {
                        ReflectionUtilities.invokeMethod(setter, result, param);
                    } else {

                        if (mode != MetadataType.SENSORML  && attribName.equalsIgnoreCase("identifier")) {
                            attribName = "name";
                        }
                        // special case for xlink
                        if (attribName.equals("xLink") && IdentifiedObject.class.isAssignableFrom(classe)) {
                            ((IdentifiedObject)result).getIdentifierMap().putSpecialized(IdentifierSpace.XLINK, (XLink)param);
                        } else {
                            final Field field = ReflectionUtilities.getFieldFromName(attribName, classe);

                            if (field != null) {
                                setFieldToValue(field, attribName, result, param);
                            } else {
                                LOGGER.warning("no field " + attribName + " in class:" + classe.getName() + "\ncurrentPath:" + path.getId());
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    private boolean putMeta(final Map<String, Object> metaMap, String attribName, final Object param, final ISOMetadata result, final Path path) {
        // special case for identifier
        if ("id".equals(attribName)) {
            result.getIdentifierMap().putSpecialized(IdentifierSpace.ID, (String)param);
            return true;
        }
        if ("xLink".equals(attribName)) {
            result.getIdentifierMap().putSpecialized(IdentifierSpace.XLINK, (XLink)param);
            return true;
        }
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

                        LOGGER.finer("unable to put " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                        tryAgain = false;
                }
            } catch (ClassCastException ex) {
                LOGGER.log(Level.WARNING, "Exception while putting in geotoolkit metadata.", ex);
                tryAgain = false;
            } catch (UnsupportedOperationException ex) {
                LOGGER.log(Level.WARNING, "Unsuported operation Exception while putting in geotoolkit metadata. ",  ex);
                tryAgain = false;
            }
        }
        return false;
    }

    private void setFieldToValue(final Field field, final String attribName, final Object result, final Object param) {
        field.setAccessible(true);
        try {
            if ("axes".equals(attribName)) {
                final CoordinateSystemAxis[] params = new CoordinateSystemAxis[1];
                params[0] = (CoordinateSystemAxis) param;
                field.set(result, params);
            } else if ("type".equals(attribName)) {
                final Object typeValue = Enum.valueOf(org.apache.sis.xml.XLink.Type.class, ((String)param).toUpperCase(Locale.US));
                field.set(result, typeValue);
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
            LOGGER.warning("error while setting the parameter:" + param + "\n to the field:" + field + ":" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            String objectStr = "null";
            if (param != null) {
                try {
                    objectStr = param.toString();
                } catch (UnformattableObjectException ex2) {
                    objectStr = "(unformattableObject) " + param.getClass().getSimpleName();
                }
            }
            LOGGER.warning("IllegalArgumentException:" + ex.getMessage() + '\n'
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
        } else if (className.equalsIgnoreCase("Date") || className.equalsIgnoreCase("DateTime")) {
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
        } else if (className.equalsIgnoreCase("LanguageCode") || className.equals("PT_Locale")) {
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
     * Return a set of package to explore in function of the standard of the MDweb {@link Classe} and the mode.
     *
     * @param standardName
     * @param className
     * @param mode
     * @return
     */
    private List<String> getPackageFromStandard(final String standardName, final String className, final MetadataType mode) {
        final List<String> packagesName = new ArrayList<>();

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

        } else if ("ISO 19108".equals(standardName) && (mode == MetadataType.SENSORML)) {
            packagesName.addAll(gml31Package);

        } else if ("ISO 19108".equals(standardName) && (mode != MetadataType.SENSORML && className.startsWith("Time"))) {
            packagesName.addAll(gml32Package);

        } else if ("ISO 19115-2".equals(standardName)) {
            packagesName.addAll(geotkAcquisitionPackage);

        } else if ("MDWEB".equals(standardName)) {
            packagesName.addAll(otherPackage);

        } else if (extraPackage.containsKey(standardName)) {
            packagesName.addAll(extraPackage.get(standardName));

        } else {
            if (!className.contains("Code") && !"DCPList".equals(className) && !"SV_CouplingType".equals(className) && !"CS_AxisDirection".equals(className)) {
                packagesName.addAll(geotoolkitPackage);
                packagesName.addAll(gml32Package);
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
    private Class getClassFromName(final Classe type, final MetadataType mode) {
        String className          = type.getName();
        final String standardName = type.getStandard().getName();

        final String classNameSave = mode.name() + '-' + standardName + ':' + className;

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
            className = "ImmutableIdentifier";
        } else if ("MD_ReferenceSystem".equals(className)) {
            className = "ReferenceSystemMetadata";
        }

        final List<String> packagesName = getPackageFromStandard(standardName, className, mode);

        for (String packageName : packagesName) {

            //TODO remove this special case
            /*if ("RS_Identifier".equals(className))
                packageName = "org.geotoolkit.referencing";
            else*/ if ("MD_ScopeCode".equals(className)) {
                packageName = "org.opengis.metadata.maintenance";
            } else if ("SV_ServiceIdentification".equals(className)) {
                packageName = "org.apache.sis.metadata.iso.identification";
            } else if (className.startsWith("FRA_")) {
                packageName = "org.apache.sis.internal.profile.fra";
                className = className.substring(4); // Remove "FRA_" prefix.
            } else if ("ReferenceSystemMetadata".equals(className)) {
                packageName = "org.apache.sis.internal.jaxb.metadata";
            } else if ("Anchor".equals(className)) {
                packageName = "org.apache.sis.internal.jaxb.gmx";
                className = "GMX_Anchor";
            } else if ("XLink".equals(className)) {
                packageName = "org.apache.sis.xml";
                className = "XLink";
            }
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

    /**
     * {@inheritDoc }
     */
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

    /**
     * {@inheritDoc }
     */
    @Override
    public List<? extends Object> getAllEntries() throws MetadataIoException {
        final List<Object> results = new ArrayList<>();
        try {
            final List<RecordSet> recordSets   = mdReader.getRecordSets();
            final Collection<FullRecord> records = mdReader.getAllRecord(recordSets);
            for (FullRecord f: records) {
                results.add(getObjectFromRecord("no cache", f, MetadataType.NATIVE));
            }
        } catch (MD_IOException ex) {
            throw new MetadataIoException("SQL Exception while getting all the entries: " +ex.getMessage());
        }
        return results;
    }

    /**
     * {@inheritDoc }
     *
     */
    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        final List<String> results = new ArrayList<>();
        try {
            final List<RecordSet> recordSets   = mdReader.getRecordSets();
            final Collection<String> ids = mdReader.getAllIdentifiers(recordSets, indexOnlyPublished);
            results.addAll(ids);
        } catch (MD_IOException ex) {
            throw new MetadataIoException("SQL Exception while getting all the entries: " +ex.getMessage());
        }
        return results;
    }

    /**
     * TODO add a proper count methode in mdreader.
     *
     * @return the number of record in the database.
     *
     * @throws MetadataIoException
     */
    @Override
    public int getEntryCount() throws MetadataIoException {
        try {
            final List<RecordSet> recordSets = mdReader.getRecordSets();
            final Collection<String> records = mdReader.getAllIdentifiers(recordSets, indexOnlyPublished);
            return records.size();
        } catch (MD_IOException ex) {
            throw new MetadataIoException("SQL Exception while getting all the entries: " +ex.getMessage());
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Iterator<String> getIdentifierIterator() throws MetadataIoException {
        final List<String> results = getAllIdentifiers();
        return results.iterator();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeFromCache(String identifier) {
        super.removeFromCache(identifier);
    }

    @Override
    public void clearCache() {
        super.clearCache();
        mdReader.clearStorageCache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataType> getSupportedDataTypes() {
        return Arrays.asList(MetadataType.ISO_19115, MetadataType.DUBLINCORE, MetadataType.EBRIM, MetadataType.SENSORML, MetadataType.ISO_19110);
    }
}
