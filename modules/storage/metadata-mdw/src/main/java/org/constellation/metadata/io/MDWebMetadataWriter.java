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
import org.apache.sis.metadata.iso.extent.DefaultGeographicDescription;
import org.apache.sis.referencing.NamedIdentifier;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.xml.IdentifiedObject;
import org.apache.sis.xml.IdentifierSpace;
import org.apache.sis.xml.XLink;
import org.apache.sis.xml.XLink.Type;
import org.apache.sis.xml.XML;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.ReflectionUtilities;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.util.StringUtilities;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.MD_IOFactory;
import org.mdweb.io.Writer;
import org.mdweb.model.profiles.Profile;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.CodeList;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.model.storage.RecordInfo;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.RecordSet.EXPOSURE;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.users.User;
import org.opengis.annotation.UML;
import org.opengis.metadata.Metadata;
import org.w3c.dom.Node;

import javax.imageio.spi.ServiceRegistry;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.TimeZone;
import java.util.logging.Level;

// constellation dependencies
// Geotoolkit dependencies
// Apache dependencies
// MDWeb dependencies
// GeoAPI

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataWriter extends AbstractMetadataWriter {

    /**
     * A MDWeb RecordSets where write the record.
     */
    private RecordSet mdRecordSet;

    /**
     * The MDWeb user who owe the inserted record.
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
     * A map recording the binding between java Class and MDWeb {@link classe}
     */
    private final Map<String, Classe> classBinding = new HashMap<>();

    /**
     * A List of contact record.
     */
    private final Map<Object, Value> contacts = new HashMap<>();

    /**
     * A flag indicating that we don't want to write predefined values.
     */
    private boolean noLink = false;

    /**
     * A flag indicating that we don't want to add the metadata to the index.
     */
    protected final boolean noIndexation;

    private final Map<Standard, List<Standard>> standardMapping = new HashMap<>();

    private static final TimeZone tz = TimeZone.getTimeZone("GMT+2:00");
    
    /**
     * Record the date format in the metadata.
     */
    protected static final List<DateFormat> DATE_FORMAT = new ArrayList<>();
    static {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        DATE_FORMAT.add(df);

        df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(tz);
        DATE_FORMAT.add(df);
    }

    /**
     * Build a new metadata writer.
     *
     * @param configuration The configuration object.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public MDWebMetadataWriter(final Automatic configuration) throws MetadataIoException {
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
            MD_IOFactory factory = null;
            final Iterator<MD_IOFactory> ite = ServiceRegistry.lookupProviders(MD_IOFactory.class);
            while (ite.hasNext()) {
                MD_IOFactory currentFactory = ite.next();
                if (currentFactory.matchImplementationType(dataSource, isPostgres)) {
                    factory = currentFactory;
                }
            }
            if (factory != null) {
                mdWriter    = factory.getPooledInstance(db.getConnectURL(), dataSource, isPostgres);
                mdRecordSet = getRecordSet(configuration.getDefaultRecordSet());
                defaultUser = mdWriter.getUser("admin");

                if ("true".equalsIgnoreCase(configuration.getNoIndexation())) {
                    noIndexation = true;
                    LOGGER.info("indexation is de-activated for Transactionnal part");
                } else {
                    noIndexation = false;
                }
                initStandardMapping();
                initContactMap();
            } else {
                throw new MetadataIoException("unable to find a MD_IO factory");
            }
        } catch (MD_IOException ex) {
            throw new MetadataIoException("MD_IOException while initializing the MDWeb writer:" +'\n'+
                                           "cause:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new MetadataIoException("SQLException while initializing the MDWeb writer:" +'\n'+
                                           "cause:" + ex.getMessage());
        }
    }

    /**
     * Build a new metadata writer.
     *
     * @param mdWriter
     * @param defaultrecordSet
     * @param userLogin
     * 
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public MDWebMetadataWriter(final Writer mdWriter, final String defaultrecordSet, final String userLogin) throws MetadataIoException {
        super();
        this.mdWriter    = mdWriter;
        try {
            this.mdRecordSet = getRecordSet(defaultrecordSet);
            this.defaultUser = mdWriter.getUser(userLogin);
            initStandardMapping();
            initContactMap();
        } catch (MD_IOException ex) {
            throw new MetadataIoException("MD_IOException while getting the catalog and user:" +'\n'+
                                           "cause:" + ex.getMessage());
        }
        this.noIndexation = false;
    }

    public MDWebMetadataWriter(final Writer mdWriter) throws MetadataIoException {
        super();
        this.mdWriter    = mdWriter;
        try {
            this.mdRecordSet = RecordSet.DATA_RECORDSET;
            this.defaultUser = User.INTERNAL_USER;
            initStandardMapping();
            initContactMap();
        } catch (MD_IOException ex) {
            throw new MetadataIoException("MD_IOException while getting the catalog and user:" +'\n'+
                                           "cause:" + ex.getMessage());
        }
        this.noIndexation = false;
    }

    protected MDWebMetadataWriter() throws MetadataIoException {
        this.defaultUser = null;
        this.noIndexation = false;
    }

    private void initStandardMapping() {
        // ISO 19115 and its sub standard (ISO 19119, 19110)
        List<Standard> availableStandards = new ArrayList<>();
        availableStandards.add(Standard.ISO_19115_FRA);
        availableStandards.add(Standard.ISO_19115);
        availableStandards.add(Standard.ISO_19115_2);
        availableStandards.add(Standard.ISO_19108);
        availableStandards.add(Standard.ISO_19103);
        availableStandards.add(Standard.ISO_19119);
        availableStandards.add(Standard.ISO_19110);
        availableStandards.add(Standard.MDWEB);

        standardMapping.put(Standard.ISO_19115, availableStandards);

        // CSW standard
        availableStandards = new ArrayList<>();
        availableStandards.add(Standard.CSW);
        availableStandards.add(Standard.DUBLINCORE);
        availableStandards.add(Standard.DUBLINCORE_TERMS);
        availableStandards.add(Standard.OWS);
        standardMapping.put(Standard.CSW, availableStandards);

        // Ebrim v3 standard
        availableStandards = new ArrayList<>();
        availableStandards.add(Standard.EBRIM_V3);
        availableStandards.add(Standard.CSW);
        availableStandards.add(Standard.OGC_FILTER);
        availableStandards.add(Standard.MDWEB);
        standardMapping.put(Standard.EBRIM_V3, availableStandards);

        // Ebrim v2.5 standard
        availableStandards = new ArrayList<>();
        availableStandards.add(Standard.EBRIM_V2_5);
        availableStandards.add(Standard.CSW);
        availableStandards.add(Standard.OGC_FILTER);
        availableStandards.add(Standard.MDWEB);
        standardMapping.put(Standard.EBRIM_V2_5, availableStandards);

        // SensorML standard
        availableStandards.add(Standard.SENSORML);
        availableStandards.add(Standard.SENSOR_WEB_ENABLEMENT);
        availableStandards.add(Standard.ISO_19108);
        standardMapping.put(Standard.SENSORML, availableStandards);

        // we add the extra binding extracted from a properties file
        try {
            final Map<String, List<String>> extraStandard = new HashMap<>();
            final Iterator<ExtraMappingFactory> ite = ServiceRegistry.lookupProviders(ExtraMappingFactory.class);
            while (ite.hasNext()) {
                final ExtraMappingFactory currentFactory = ite.next();
                extraStandard.putAll(currentFactory.getExtraStandard());
            }

            for (Entry<String, List<String>> entry : extraStandard.entrySet()) {
                final String mainStandardName = entry.getKey();
                final Standard newMainStandard = mdWriter.getStandard(mainStandardName);
                if (newMainStandard == null) {
                    LOGGER.log(Level.WARNING, "Unable to find the extra main standard:{0}", mainStandardName);
                    continue;
                }
                final List<String> standardList  = entry.getValue();
                final List<Standard> standards   = new ArrayList<>();
                for (String standardName : standardList) {
                    Standard standard = mdWriter.getStandard(standardName);
                    if (standard == null) {
                        LOGGER.log(Level.FINER, "Unable to find the extra standard:{0}", standardName);
                    } else {
                        standards.add(standard);
                    }
                }
                if (standardMapping.containsKey(newMainStandard)) {
                    final List<Standard> previousStandards = standardMapping.get(newMainStandard);
                    previousStandards.addAll(standards);
                    standardMapping.put(newMainStandard, previousStandards);
                } else {
                    standardMapping.put(newMainStandard, standards);
                }
            }

        } catch (MD_IOException ex) {
            LOGGER.log(Level.WARNING, "MD_IO exception while reading extra standard properties for MDW meta writer", ex);
        }
    }

    // TODO move this to CSW implementation
    public RecordSet getRecordSet(final String defaultRecordSet) throws MD_IOException {
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
     * Load the contact from MDweb database.
     *
     * @throws MD_IOException
     */
    private void initContactMap() throws MD_IOException {
        final Collection<FullRecord> contactRecords = mdWriter.getContacts();
        if (contactRecords.size() > 0) {
            LOGGER.log(Level.INFO, "initiazing {0} contacts", contactRecords.size());
            final MDWebMetadataReader reader = new MDWebMetadataReader(mdWriter);
            for (FullRecord contactRecord : contactRecords) {
                Object responsibleParty = reader.getObjectFromRecord(null, contactRecord, MetadataType.ISO_19115);
                contacts.put(responsibleParty, contactRecord.getRoot());
            }
        }

    }

    /**
     * Return an MDWeb {@link FullRecord} from an object.
     *
     * @param object The object to transform in record.
     * @return an MDWeb {@link FullRecord} representing the metadata object.
     * @throws org.mdweb.io.MD_IOException
     */
    protected FullRecord getRecordFromObject(final Object object) throws MD_IOException {
        final String title = Utils.findTitle(object);
        return getRecordFromObject(object, defaultUser, mdRecordSet, null, title);
    }

    /**
     * Return an MDWeb {@link FullRecord} from an object.
     *
     * @param object The object to transform in record.
     * @param title
     *
     * @return an MDWeb {@link FullRecord} representing the metadata object.
     * @throws org.mdweb.io.MD_IOException
     */
    protected FullRecord getRecordFromObject(final Object object, String title) throws MD_IOException {
        if (title == null) {
            title = Utils.findTitle(object);
        }
        return getRecordFromObject(object, defaultUser, mdRecordSet, null, title);
    }

    /**
     * Return an MDWeb {@link FullRecord} from an object.
     *
     * @param object The object to transform in record.
     * @param user
     * @param recordSet
     * @param title
     * @param profile
     * @return an MDWeb {@link FullRecord} representing the metadata object.
     * @throws org.mdweb.io.MD_IOException
     */
    public FullRecord getRecordFromObject(final Object object, final User user, final RecordSet recordSet, Profile profile, String title) throws MD_IOException {

        if (user == null) {
            throw new MD_IOException("The User must not be null");
        }
        if (object != null) {
            //we try to find a title for the from
            if ("unknow title".equals(title)) {
                title = mdWriter.getAvailableTitle();
            }

            final Date creationDate = new Date(System.currentTimeMillis());
            final String className  = object.getClass().getSimpleName();
            // ISO 19115 types
            if ("DefaultMetadata".equals(className)      ||

           // ISO 19115-2 types
                "MI_Metadata".equals(className)          ||

            // ISO 19110 types
                "FeatureCatalogueImpl".equals(className) ||
                "FeatureOperationImpl".equals(className) ||
                "FeatureAssociationImpl".equals(className)
            ) {
                mainStandard   = Standard.ISO_19115;

            // CSW Types
            } else if ("RecordType".equals(className)) {
                mainStandard = Standard.CSW;

            // SML Types
            } else if ("SensorML".equals(className)) {
                mainStandard = Standard.SENSORML;

            // Ebrim Types
            } else if (object.getClass().getName().startsWith("org.geotoolkit.ebrim.xml.v300")) {
                mainStandard = Standard.EBRIM_V3;

            } else if (object.getClass().getName().startsWith("org.geotoolkit.ebrim.xml.v250")) {
                mainStandard = Standard.EBRIM_V2_5;

            // unkow types
            } else {
                mainStandard   = Standard.ISO_19115;
                LOGGER.log(Level.WARNING, "Unknow object type:{0}, it may can''t be registered.", object.getClass().getName());
            }

            final String identifier = Utils.findIdentifier(object);
            final FullRecord record = new FullRecord(-1, identifier, recordSet, title, user, null, profile, creationDate, creationDate, null, false, false, FullRecord.TYPE.NORMALRECORD);

            final Classe rootClasse = getClasseFromObject(object);
            if (rootClasse != null) {
               /**
                * A List of the already see object for the current metadata read
                * (in order to avoid infinite loop)
                */
                final Map<Object, Value> alreadyWrite = new HashMap<>();
                final Path rootPath = new Path(rootClasse.getStandard(), rootClasse);
                final List<Value> collection = addValueFromObject(record, object, rootPath, null, alreadyWrite);
                collection.clear();
                alreadyWrite.clear();
                return record;
            } else {
                LOGGER.log(Level.SEVERE, "unable to find the root class:{0}", object.getClass().getSimpleName());
                return null;
            }
        } else {
            LOGGER.severe("unable to create record object is null");
            return null;
        }
    }

    /**
     * Add a MDWeb value (and his children)to the specified record.
     *
     * @param record The created record.
     * @param object
     * @param path
     * @param parentValue
     * @param alreadyWrite
     * @return
     * @throws org.mdweb.io.MD_IOException
     *
     */
    protected List<Value> addValueFromObject(final FullRecord record, Object object, Path path, final Value parentValue, final Map<Object, Value> alreadyWrite) throws MD_IOException {

        final List<Value> result = new ArrayList<>();

        //if the path is not already in the database we write it
        if (mdWriter.getPath(path.getId()) == null) {
           mdWriter.writePath(path);
        }
        if (object == null) {
            return result;
        }

        //if the object is a JAXBElement we desencapsulate it
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
                    final String parentID = path.getParent().getId();
                    path = mdWriter.getPath(parentID + ":geographicElement3");
                }
                result.addAll(addValueFromObject(record, obj, path, parentValue, alreadyWrite));

            }
            return result;
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
        if (contacts.get(object) != null) {
            linkedValue = contacts.get(object);
        } else if (isNoLink()) {
            linkedValue = null;
        } else {
            linkedValue = alreadyWrite.get(object);
        }

        //Special case for PT_FreeText
        if (classe.getName().equals("PT_FreeText")) {
            final DefaultInternationalString dis = (DefaultInternationalString) object;

            // 1. the root Value PT_FreeText
            final Value rootValue = new Value(path, record, ordinal, classe, parentValue, null);
            result.add(rootValue);

            // 2. The default value
            final String defaultValue = dis.toString(null);
            final Path defaultValuePath = new Path(path, classe.getPropertyByName("value"));
            final TextValue textValue = new TextValue(defaultValuePath, record , 1, defaultValue, mdWriter.getClasse("CharacterString", Standard.ISO_19103), rootValue, null);
            result.add(textValue);

            // 3. the localised values
            final Classe localisedString = mdWriter.getClasse("LocalisedCharacterString", Standard.ISO_19103);
            int localeOrdinal = 1;
            for (Locale locale : dis.getLocales())  {
                if (locale == null) {continue;}

                final Path valuePath = new Path(path, classe.getPropertyByName("textGroup"));
                final Value value = new Value(valuePath, record, localeOrdinal, localisedString, rootValue, null);
                result.add(value);

                final String localisedValue = dis.toString(locale);
                final Path locValuePath = new Path(valuePath, localisedString.getPropertyByName("value"));
                final TextValue locValValue = new TextValue(locValuePath, record , localeOrdinal, localisedValue, mdWriter.getClasse("CharacterString", Standard.ISO_19103), value, null);
                result.add(locValValue);

                final Path localePath = new Path(valuePath, localisedString.getPropertyByName("locale"));
                final String localeDesc = "#locale-" + locale.getISO3Language();
                final TextValue localeValue = new TextValue(localePath, record , localeOrdinal, localeDesc, mdWriter.getClasse("CharacterString", Standard.ISO_19103), value, null);
                result.add(localeValue);
                localeOrdinal++;
            }

        //Special case for PT_Locale
        } else if (classe.getName().equals("PT_Locale")) {
            final Locale loc = (Locale) object;

            // 1. the root Value PT_Locale
            final Value rootValue = new Value(path, record, ordinal, classe, parentValue, null);
            result.add(rootValue);

            // 2. The languageCode value
            final String languageValue   = loc.getLanguage();
            final Path languageValuePath = new Path(path, classe.getPropertyByName("languageCode"));
            final TextValue lanTextValue = new TextValue(languageValuePath, record , ordinal, languageValue, mdWriter.getClasse("LanguageCode", Standard.ISO_19115), rootValue, null);
            result.add(lanTextValue);

            // 3. the country value
            final String countryValue    = loc.getCountry();
            final Path countryValuePath  = new Path(path, classe.getPropertyByName("country"));
            final TextValue couTextValue = new TextValue(countryValuePath, record , ordinal, countryValue, mdWriter.getClasse("CountryCode", Standard.ISO_19115), rootValue, null);
            result.add(couTextValue);

            // 4. the encoding value "LOST for now" TODO


        // if its a primitive type we create a TextValue
        } else if (classe.isPrimitive() || classe.getName().equals("LocalName")) {
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

                        codelistElement = ReflectionUtilities.getElementNameFromEnum(object);

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
                final java.util.Date d = (java.util.Date) object;
                Calendar c = new GregorianCalendar();
                c.setTime(d);
                if (c.get(Calendar.HOUR) == 0 && c.get(Calendar.MINUTE) == 0 && c.get(Calendar.SECOND) == 0 && c.get(Calendar.MILLISECOND) == 0) {
                    synchronized (DATE_FORMAT) {
                        value = DATE_FORMAT.get(1).format(object) + "T00:00:00" + tz.getDisplayName().substring(3);
                    }
                } else {
                    synchronized (DATE_FORMAT) {
                        value = DATE_FORMAT.get(0).format(object);
                    }
                }
            } else if (object.getClass().isEnum()){
                value =  object.toString().toLowerCase(Locale.US);

            } else if (object instanceof URI){
                 value = object.toString();
                 value = value.replace("%5C", "\\");
            } else {
                value = object.toString();
            }

            final TextValue textValue = new TextValue(path, record , ordinal, value, classe, parentValue, null);
            result.add(textValue);

        // if we have already see this object we build a Linked Value.
        } else if (linkedValue != null) {

            final LinkedValue value = new LinkedValue(path, record, ordinal, linkedValue.getRecord(), linkedValue, classe, parentValue, null);
            result.add(value);

        // else we build a Value node.
        } else {

            final Value value = new Value(path, record, ordinal, classe, parentValue, null);
            result.add(value);
            //we add this object to the listed of already write element
            if (!isNoLink()) {
                alreadyWrite.put(object, value);
            }

            do {
                for (Property prop: classe.getProperties()) {
                    // TODO remove when fix in MDweb2
                    if (prop.getName().equals("geographicElement3") ||  prop.getName().equals("geographicElement4")) {
                        continue;
                    }

                    final String propName = specialCorrectionName(prop.getName(), object.getClass());

                    Method getter;
                    if ("axis".equals(propName)) {
                        getter = ReflectionUtilities.getMethod("get" + StringUtilities.firstToUpper(propName), object.getClass(), int.class);
                    } else {
                        getter = ReflectionUtilities.getGetterFromName(propName, object.getClass());
                    }

                    if (getter != null) {
                        try {
                            final Object propertyValue;
                            if ("axis".equals(propName)) {
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
                                result.addAll(addValueFromObject(record, propertyValue, childPath, value, alreadyWrite));
                            }

                        } catch (IllegalAccessException e) {
                            LOGGER.severe("The class is not accessible");
                            return result;
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            LOGGER.severe("Exception throw in the invokated getter: " + getter.toGenericString() +
                                          "\nCause: " + e.getMessage());
                            return result;
                        }

                    // special case for id
                    } else if ("id".equals(propName) && object instanceof IdentifiedObject) {
                        final Object propertyValue = ((IdentifiedObject)object).getIdentifierMap().getSpecialized(IdentifierSpace.ID);
                        if (propertyValue != null) {
                            final Path childPath = new Path(path, prop);

                            //if the path is not already in the database we write it
                            if (mdWriter.getPath(childPath.getId()) == null) {
                                mdWriter.writePath(childPath);
                            }
                            result.addAll(addValueFromObject(record, propertyValue, childPath, value, alreadyWrite));
                        }
                    } else if ("xLink".equals(propName) && object instanceof IdentifiedObject) {
                        final Object propertyValue = ((IdentifiedObject)object).getIdentifierMap().getSpecialized(IdentifierSpace.XLINK);
                        if (propertyValue != null) {
                            final Path childPath = new Path(path, prop);

                            //if the path is not already in the database we write it
                            if (mdWriter.getPath(childPath.getId()) == null) {
                                mdWriter.writePath(childPath);
                            }
                            result.addAll(addValueFromObject(record, propertyValue, childPath, value, alreadyWrite));
                        }
                    } else if (!"unitOfMeasure".equals(propName) && !"verticalDatum".equals(propName)) {
                        final Class valueClass     = object.getClass();
                        final Object propertyValue = getValueFromField(valueClass, propName, object);
                        if (propertyValue != null) {
                            final Path childPath = new Path(path, prop);

                            //if the path is not already in the database we write it
                            if (mdWriter.getPath(childPath.getId()) == null) {
                                mdWriter.writePath(childPath);
                            }
                            result.addAll(addValueFromObject(record, propertyValue, childPath, value, alreadyWrite));
                        }
                    } else {
                        LOGGER.warning("no getter found for:" + propName + " class: " + object.getClass().getName());
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
     * Try to extract the value of a field named propName in the specified class (or any of its super class)
     *
     * @param valueClass A class.
     * @param propName The name of the searched field.
     * @param object the object on which we want to extract the field value.
     *
     * @return The value of the specified field or {@code null}
     */
    private Object getValueFromField(Class valueClass, final String propName, final Object object) {
        final Class origClass = valueClass;
        do {
            try {
                final Field field = valueClass.getDeclaredField(propName);
                final Object propertyValue;
                if (field != null) {
                    field.setAccessible(true);
                    propertyValue = field.get(object);
                } else {
                    propertyValue = null;
                }
                return propertyValue;

            } catch (NoSuchFieldException ex) {
                LOGGER.log(Level.FINER, "no such Field:" + propName + " in class:" + valueClass.getName());
            } catch (SecurityException | IllegalAccessException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            valueClass = valueClass.getSuperclass();
        } while (valueClass != null);
        LOGGER.log(Level.WARNING, "no such Field:" + propName + " in class:" + origClass.getName());
        return null;
    }

    /**
     * apply special fix on the property name.
     *
     * @param attributeName
     * @param objectClass
     * @return
     */
    public String specialCorrectionName(final String attributeName, final Class objectClass) {
        final String propName;
        // special case
        if (attributeName.equalsIgnoreCase("referenceSystemIdentifier") ||
           (attributeName.equalsIgnoreCase("identifier") && objectClass.getSimpleName().equals("DefaultCoordinateSystemAxis")) ||
           (attributeName.equalsIgnoreCase("identifier") && objectClass.getSimpleName().equals("DefaultVerticalCS")) ||
           (attributeName.equalsIgnoreCase("identifier") && objectClass.getSimpleName().equals("DefaultVerticalDatum")) ||
           (attributeName.equalsIgnoreCase("identifier") && objectClass.getSimpleName().equals("DefaultVerticalCRS"))) {
            propName = "name";
        } else if (attributeName.equalsIgnoreCase("uom") && !objectClass.getSimpleName().equals("QuantityType")
                                                         && !objectClass.getSimpleName().equals("QuantityRange")
                                                         && !objectClass.getSimpleName().equals("TimeRange")
                                                         && !objectClass.getSimpleName().equals("TimeType")) {
            propName = "unit";
        } else if (attributeName.equalsIgnoreCase("verticalDatum") && objectClass.getSimpleName().equals("DefaultVerticalCRS")) {
            propName = "datum";
        } else {
            propName = attributeName;
        }

        return propName;
    }


    /**
     * Return an MDWeb {@link Classe} object for the specified java object.
     *
     * @param object the object to identify
     * @return
     *
     * @throws org.mdweb.io.MD_IOException
     */
    protected Classe getClasseFromObject(final Object object) throws MD_IOException {

        String className;
        String packageName;
        Classe result;
        if (object != null) {

            // special case variant (we don't want to use cache) for PT_Locale
            if (object instanceof Locale && ((Locale)object).getCountry() != null && !((Locale)object).getCountry().isEmpty()) {
                return mdWriter.getClasse("PT_Locale", Standard.ISO_19115);
            }

            // look for previously cached result
            result = classBinding.get(object.getClass().getName());
            if (result != null) {
                return result;
            }

            // special case for the sub classe of Xlink
            if (object.getClass().equals(XLink.class)) {
                return mdWriter.getClasse("XLink", mdWriter.getStandard("Xlink"));
            }

            // special case for Xlink.Type enum
            if (object.getClass().equals(Type.class)) {
                return PrimitiveType.STRING;
            }

            // special case for NamedIdentifier
            if (object.getClass().equals(NamedIdentifier.class)) {
                return mdWriter.getClasse("RS_Identifier", Standard.ISO_19115);
            }

            //special case for Proxy: we extract the GeoAPI interface, then we get the UML annotation for className
            if (object.getClass().getSimpleName().startsWith("$Proxy")) {
                final Class apiInterface =  object.getClass().getInterfaces()[0];
                final UML a = (UML) apiInterface.getAnnotation(UML.class);
                className =  a.identifier();
                packageName = "";

            } else {
                className   = object.getClass().getSimpleName();
                packageName = object.getClass().getPackage().getName();
            }
            LOGGER.log(Level.FINER, "search for classe {0}", className);

        } else {
            return null;
        }

        //for the primitive type we return ISO primitive type
        result = getPrimitiveTypeFromName(className);
        if (result != null) {
            classBinding.put(object.getClass().getName(), result);
            return result;
        }

        final String annotationName = getNameFromAnnotation(object);
        if (annotationName != null) {
            className =  annotationName;
        } else {

            //we remove the Default prefix
            if (className.startsWith("Default")) {
                className = className.substring(7, className.length());
            }

            //we remove the Type suffix
            if (className.endsWith("Type") && !"CodeType".equals(className)){
                className = className.substring(0, className.length() - 4);
            }
        }
        if (className.isEmpty()) {
            return null;
        }

        final List<Standard> availableStandards = standardMapping.get(mainStandard);
        if (availableStandards == null) {
            throw new IllegalArgumentException("Unexpected Main standard: " + mainStandard);
        }

        String availableStandardLabel = "";
        for (Standard standard : availableStandards) {

            availableStandardLabel = availableStandardLabel + standard.getName() + ',';
            /* to avoid some confusion between to classes with the same name
             * we affect the standard in some special case
             */
            if (packageName.startsWith("org.geotoolkit.sml.xml")) {
                standard = Standard.SENSORML;
            } else if (packageName.startsWith("org.geotoolkit.swe.xml")) {
                standard = Standard.SENSOR_WEB_ENABLEMENT;
            } else if ("org.geotoolkit.gml.xml.v311".equals(packageName)) {
                standard = Standard.ISO_19108;
            }

            String name = className;
            int nameType = 0;
            while (nameType < 3) {

                LOGGER.finer("searching: " + standard.getName() + ':' + name);
                result = mdWriter.getClasse(name, standard);
                if (result != null) {
                    LOGGER.finer("class found:" + standard.getName() + ':' + name);
                    classBinding.put(object.getClass().getName(), result);
                    return result;
                }

                switch (nameType) {

                    case 0: {
                        name = "Time" + className;
                        nameType = 1;
                        break;
                    }
                    case 1: {
                        name = "TM_" + className;
                        nameType = 2;
                        break;
                    }
                    default:
                        nameType = 3;
                        break;
                }
            }
        }

        availableStandardLabel = availableStandardLabel.substring(0, availableStandardLabel.length() - 1);
        LOGGER.warning("class not found: " + className + " in the following standards: " + availableStandardLabel + "\n (" + object.getClass().getName() + ')');
        return null;
    }

    /**
     * Find The class name by extracting the {@link XmlRootElement} annotation.
     * For the instance of {@link org.opengis.util.CodeList},
     * we extract the name from the {@link UML} annotation
     *
     * @param object A GeotoolKit object
     * @return the name parameter in the XmlElementRoot annotation or identifier parameter in UM annotation.
     *
     */
    private String getNameFromAnnotation(final Object object) {

        if (object instanceof org.opengis.util.CodeList) {
            UML a = (UML)object.getClass().getAnnotation(UML.class);
            if (a != null) {
                return a.identifier();
            }
        } else {
            XmlRootElement a = (XmlRootElement) object.getClass().getAnnotation(XmlRootElement.class);
            if (a != null) {
                return a.name();
            }
        }
        return null;
   }

    /**
     * Return a {@link Classe} (java primitive type) from a class name.
     *
     * @param className the standard name of a class.
     * @return a primitive class.
     */
    private Classe getPrimitiveTypeFromName(final String className) throws MD_IOException {
        final String mdwclassName;
        final Standard mdwStandard;
        if ("String".equals(className) || "SimpleInternationalString".equals(className) || "BaseUnit".equals(className)) {
            return PrimitiveType.STRING;
        } else if ("DefaultInternationalString".equalsIgnoreCase(className)) {
            mdwclassName = "PT_FreeText";
            mdwStandard  = Standard.ISO_19115;
        } else if ("Date".equalsIgnoreCase(className) || "URI".equalsIgnoreCase(className) || "Integer".equalsIgnoreCase(className)
                || "Boolean".equalsIgnoreCase(className)) {
            mdwclassName = className;
            mdwStandard  = Standard.ISO_19103;

        }  else if ("Long".equalsIgnoreCase(className)) {
            mdwclassName = "Integer";
            mdwStandard = Standard.ISO_19103;
        }  else if ("URL".equalsIgnoreCase(className)) {
            mdwclassName = className;
            mdwStandard  = Standard.ISO_19115;
        //special case for locale codeList.
        } else if ("Locale".equals(className)) {
            mdwclassName = "LanguageCode";
            mdwStandard = Standard.ISO_19115;
        //special case for Role codeList.
        } else if ("Double".equals(className)) {
            mdwclassName = "Real";
            mdwStandard = Standard.ISO_19103;
        } else {
            return null;
        }
        final Classe candidate = PrimitiveType.getPrimitiveTypeFromName(mdwclassName, mdwStandard);
        final Classe result;
        if (candidate != null) {
            result = candidate;
        } else {
            result = mdWriter.getClasse(mdwclassName, mdwStandard);
        }
        if (result == null) {
            LOGGER.warning("The database does not contains the primitive type:" + mdwclassName + " in the standard:" + mdwStandard.getName());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean storeMetadata(final Node obj) throws MetadataIoException {
        return storeMetadata(obj, null);
    }

    public boolean storeMetadata(final Node node, final String title) throws MetadataIoException {
        // profiling operation
        final long start = System.currentTimeMillis();
        long transTime   = 0;
        long writeTime   = 0;

        // unmarshall the objet
        final Object obj = unmarshallObject(node);

        // we create a MDWeb record the object
        FullRecord record = null;
        Profile profile   = null;
        try {
            // we try to determine the profile for the Object
            if ("org.geotoolkit.csw.xml.v202.RecordType".equals(obj.getClass().getName())) {
                profile = mdWriter.getProfile("DublinCore");
            } else if (obj instanceof Metadata) {
                profile = mdWriter.getProfileByUrn(Utils.findStandardName(obj));
            }

            final long startTrans = System.currentTimeMillis();
            record                = getRecordFromObject(obj, title);
            transTime             = System.currentTimeMillis() - startTrans;

            if (record != null && mdWriter.isAlreadyUsedIdentifier(record.getIdentifier())) {
                throw new MD_IOException("The identifier " + record.getIdentifier() + " is already used");
            }

        } catch (IllegalArgumentException e) {
             throw new MetadataIoException("This kind of resource cannot be parsed by the service: " + obj.getClass().getSimpleName() +'\n' +
                                           "cause: " + e.getMessage(), e, null);
        } catch (MD_IOException e) {
             throw new MetadataIoException("The service has throw an MD_IOException while transforming the metadata to a MDWeb object: " + e.getMessage(), e, null);
        }

        // and we store it in the database
        if (record != null) {

            if (profile != null) {
	        record.setProfile(profile);
            // if the profile is null we set the level completion to complete
            } else {
                record.setInputLevelCompletion(new boolean[]{true, true, true}, new Date(System.currentTimeMillis()));
            }

            try {
                final long startWrite = System.currentTimeMillis();
                mdWriter.writeRecord(record, false, true);
                writeTime             = System.currentTimeMillis() - startWrite;

            } catch (MD_IOException e) {
                throw new MetadataIoException("The service has throw an SQLException while writing the metadata :" + e.getMessage(), e, null);
            }

            final long time = System.currentTimeMillis() - start;

            final StringBuilder report = new StringBuilder("inserted new FullRecord: ");
            report.append(record.getTitle()).append('[').append(record.getIdentifier()).append(']').append("( ID:").append(record.getId());
            report.append(" in ").append(time).append(" ms (transformation: ").append(transTime).append(" DB write: ").append(writeTime).append(")");
            LOGGER.log(logLevel, report.toString());
            if (!noIndexation) {
                indexDocument(record);
            }
            return true;

        }
        return false;
    }

    protected void indexDocument(final FullRecord f) {
        //need to be override by child
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        classBinding.clear();
        try {
            if (mdWriter != null) {
                mdWriter.close();
            }
            classBinding.clear();
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
    public boolean deleteMetadata(final String identifier) throws MetadataIoException {
        LOGGER.log(logLevel, "metadata to delete:{0}", identifier);

        if (identifier == null) {
            return false;
        }
        try {
            // TODO is a way more fast to know that the record exist? method  isAlreadyRecordedRecord(int id) writer20
            final RecordInfo f          = mdWriter.getRecordInfo(identifier);
            if (f != null) {
                mdWriter.deleteRecord(f);
            } else {
                LOGGER.log(logLevel, "The metadata is not registered, nothing to delete");
                return false;
            }
        } catch (MD_IOException ex) {
            throw new MetadataIoException("The service has throw an MD_IOException while deleting the metadata: " + ex.getMessage());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replaceMetadata(final String metadataID, final Node any) throws MetadataIoException {
        final boolean succeed = deleteMetadata(metadataID);
        if (!succeed) {
            return false;
        }
        return storeMetadata(any);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlreadyUsedIdentifier(final String metadataID) throws MetadataIoException {
        try {
            return mdWriter.isAlreadyUsedIdentifier(metadataID);
        } catch (MD_IOException ex) {
            throw new MetadataIoException(ex);
        }
    }

    /**
     * Return an MDWeb path from a XPath.
     *
     * @param xpath An XPath
     *
     * @return An MDWeb path
     * @throws org.mdweb.io.MD_IOException
     * @throws org.constellation.metadata.io.MetadataIoException
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
        if ("MD_Metadata".equals(typeName)) {
            mainStandard = Standard.ISO_19115;
            type = mdWriter.getClasse("MD_Metadata", mainStandard);
        } else if ("Record".equals(typeName)) {
            mainStandard = Standard.CSW;
            type = mdWriter.getClasse("Record", mainStandard);
        } else {
            throw new MetadataIoException("This metadata type is not allowed:" + typeName + "\n Allowed ones are: MD_Metadata or Record");//, INVALID_PARAMETER_VALUE);
        }

        Path p  = new Path(mainStandard, type);
        final StringBuilder idValue = new StringBuilder(mainStandard.getName()).append(':').append(type.getName()).append(".*");
        int ordinal = -1;
        while (xpath.indexOf('/') != -1) {
            //Then we get the next Property name
            String propertyName = xpath.substring(0, xpath.indexOf('/'));
            //remove namespace on propertyName
            final int separatorIndex = propertyName.indexOf(':');
            if (separatorIndex != -1) {
                propertyName = propertyName.substring(separatorIndex + 1);
            }

            //we look for an ordinal
            ordinal = -1;
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

            LOGGER.log(Level.FINER, "propertyName:{0} ordinal:{1}", new Object[]{propertyName, ordinal});
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

            // remove type node
            if (xpath.indexOf('/') != -1) {
                xpath = xpath.substring(xpath.indexOf('/') + 1);
            } else {
                xpath = "";
            }
        }

        if (!xpath.isEmpty()) {
            //we look for an ordinal
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
            //remove namespace on propertyName
            final int separatorIndex = xpath.indexOf(':');
            if (separatorIndex != -1) {
                xpath = xpath.substring(separatorIndex + 1);
            }

            idValue.append(':').append(xpath).append('.');
            if (ordinal == -1) {
                idValue.append('*');
            } else {
                idValue.append(ordinal);
            }
            LOGGER.log(Level.FINER, "last propertyName:{0} ordinal:{1}", new Object[]{xpath, ordinal});
            final Property property = getProperty(type, xpath);
            p = new Path(p, property);
        }
        return new MixedPath(p, idValue.toString(), ordinal);
    }

    private Property getProperty(final Classe type, String propertyName) throws MD_IOException, MetadataIoException {
        // Special case for a bug in MDWeb
        if ("geographicElement".equals(propertyName)) {
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


    private Object unmarshallObject(final Node n) throws MetadataIoException {
        final MetadataType mode;
        switch (n.getLocalName()) {
            case "MD_Metadata":
            case "MI_Metadata":
                mode = MetadataType.ISO_19115;
                break;
            case "Record":
                mode = MetadataType.DUBLINCORE;
                break;
            case "SensorML":
                mode = MetadataType.SENSORML;
                break;
            case "RegistryObject":
            case "AdhocQuery":
            case "Association":
            case "RegistryPackage":
            case "Registry":
            case "ExtrinsicObject":
            case "RegistryEntry":
                mode = MetadataType.EBRIM;
                break;
            default:
                mode = MetadataType.NATIVE;
                break;
        }
        // TODO complete other metadata type

        try {
            final boolean replace = mode == MetadataType.ISO_19115;
            final Unmarshaller um = EBRIMMarshallerPool.getInstance().acquireUnmarshaller();
            final MarshallWarnings warnings = new MarshallWarnings();
            um.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            um.setProperty(XML.TIMEZONE, tz);
            um.setProperty(XML.CONVERTER, warnings);
            final String xml = getStringFromNode(n);
            Object obj = um.unmarshal(new StringReader(xml));
            EBRIMMarshallerPool.getInstance().recycle(um);

            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement)obj).getValue();
            }
            return obj;
        } catch (JAXBException | TransformerException ex) {
            throw new MetadataIoException(ex);
        }
    }

    private static String getStringFromNode(final Node n) throws TransformerException  {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(n), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }

    /**
     * Must be overridden by subClasses
     * 
     * @param metadataID
     * @param properties
     * @return
     * @throws MetadataIoException
     */
    @Override
    public boolean updateMetadata(final String metadataID, final Map<String, Object> properties) throws MetadataIoException {
        return false;
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
    public void setNoLink(final boolean noLink) {
        this.noLink = noLink;
    }

    /**
     * @return the mdRecordSet
     */
    public RecordSet getMdRecordSet() {
        return mdRecordSet;
    }

    /**
     * @return the defaultUser
     */
    public User getDefaultUser() {
        return defaultUser;
    }

    protected static final class MixedPath {

        public Path path;

        public String idValue;

        public int ordinal;

        public MixedPath(Path path, String idValue, int ordinal) {
            this.path    = path;
            this.idValue = idValue;
            this.ordinal = ordinal;
        }

    }
}
