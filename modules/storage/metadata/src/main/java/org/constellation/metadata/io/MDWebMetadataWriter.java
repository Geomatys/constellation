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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;

// constellation dependencies
import javax.xml.bind.annotation.XmlRootElement;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.ReflectionUtilities;
import org.constellation.util.Util;

// Geotoolkit dependencies
import org.geotoolkit.internal.jaxb.gco.ObjectReference;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicDescription;
import org.geotoolkit.util.DefaultInternationalString;
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
import org.mdweb.io.MD_IOFactory;
import org.mdweb.io.Writer;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.storage.FormInfo;
import org.mdweb.model.storage.RecordSet.EXPOSURE;
import org.opengis.annotation.UML;
import org.opengis.metadata.Metadata;

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
     * A map recording the binding between java Class and MDWeb {@link classe}
     */
    private Map<String, Classe> classBinding;
    
    /**
     * A List of the already see object for the current metadata read
     * (in order to avoid infinite loop)
     */
    private Map<Object, Value> alreadyWrite;

    /**
     * A List of contact record.
     */
    private Map<Object, Value> contacts;

    /**
     * A flag indicating that we don't want to write predefined values.
     */
    private boolean noLink = false;

    /**
     * A flag indicating that we don't want to add the metadata to the index.
     */
    private final boolean noIndexation;

    private final Map<Standard, List<Standard>> standardMapping = new HashMap<Standard, List<Standard>>();

    /**
     * Build a new metadata writer.
     * 
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
            mdWriter                    = MD_IOFactory.getPooledInstance(dataSource, isPostgres);
           
            mdRecordSet = getRecordSet(configuration.getDefaultRecordSet());
            defaultUser = mdWriter.getUser("admin");

            if ("true".equalsIgnoreCase(configuration.getNoIndexation())) {
                noIndexation = true;
                LOGGER.info("indexation is de-activated for Transactionnal part");
            } else {
                noIndexation = false;
            }

            this.contacts = new HashMap<Object, Value>();
            initStandardMapping();
            initContactMap();
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
    public MDWebMetadataWriter(final Writer mdWriter, final String defaultrecordSet, final String userLogin) throws MetadataIoException {
        super();
        this.mdWriter    = mdWriter;
        try {
            this.mdRecordSet = getRecordSet(defaultrecordSet);
            this.defaultUser = mdWriter.getUser(userLogin);
            this.contacts    = new HashMap<Object, Value>();
            initStandardMapping();
            initContactMap();
        } catch (MD_IOException ex) {
            throw new MetadataIoException("MD_IOException while getting the catalog and user:" +'\n'+
                                           "cause:" + ex.getMessage());
        }
        this.noIndexation = false;
        this.classBinding = new HashMap<String, Classe>();
        this.alreadyWrite = new HashMap<Object, Value>();
    }

    protected MDWebMetadataWriter() throws MetadataIoException {
        this.defaultUser = null;
        this.noIndexation = false;
    }

    private void initStandardMapping() {
        // ISO 19115 and its sub standard (ISO 19119, 19110)
        List<Standard> availableStandards = new ArrayList<Standard>();
        availableStandards.add(Standard.ISO_19115_FRA);
        availableStandards.add(Standard.ISO_19115);
        availableStandards.add(Standard.ISO_19115_2);
        availableStandards.add(Standard.ISO_19108);
        availableStandards.add(Standard.ISO_19103);
        availableStandards.add(Standard.ISO_19119);
        availableStandards.add(Standard.ISO_19110);

        standardMapping.put(Standard.ISO_19115, availableStandards);

        // CSW standard
        availableStandards = new ArrayList<Standard>();
        availableStandards.add(Standard.CSW);
        availableStandards.add(Standard.DUBLINCORE);
        availableStandards.add(Standard.DUBLINCORE_TERMS);
        availableStandards.add(Standard.OWS);
        standardMapping.put(Standard.CSW, availableStandards);

        // Ebrim v3 standard
        availableStandards = new ArrayList<Standard>();
        availableStandards.add(Standard.EBRIM_V3);
        availableStandards.add(Standard.CSW);
        availableStandards.add(Standard.OGC_FILTER);
        availableStandards.add(Standard.MDWEB);
        standardMapping.put(Standard.EBRIM_V3, availableStandards);

        // Ebrim v2.5 standard
        availableStandards = new ArrayList<Standard>();
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
            final InputStream extraIn = Util.getResourceAsStream("org/constellation/metadata/io/extra-standard.properties");
            if (extraIn != null) {
                final Properties extraProperties = new Properties();
                extraProperties.load(extraIn);
                extraIn.close();
                for (Entry<Object, Object> entry : extraProperties.entrySet()) {
                    String mainStandardName = (String) entry.getKey();
                    mainStandardName = mainStandardName.replace('_', ' ');
                    final Standard newMainStandard = mdWriter.getStandard(mainStandardName);
                    if (newMainStandard == null) {
                        LOGGER.log(Level.WARNING, "Unable to find the extra main standard:{0}", mainStandardName);
                        continue;
                    }
                    final List<String> standardList  = StringUtilities.toStringList((String) entry.getValue());
                    final List<Standard> standards   = new ArrayList<Standard>();
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
            } else {
                LOGGER.warning("Unable to find the extra-standard properties file");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IO exception while reading extra standard properties for MDW meta writer", ex);
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
        final List<Form> contactForms = mdWriter.getContacts();
        if (contactForms.size() > 0) {
            LOGGER.log(Level.INFO, "initiazing {0} contacts", contactForms.size());
            final MDWebMetadataReader reader = new MDWebMetadataReader(mdWriter);
            for (Form contactForm : contactForms) {
                Object responsibleParty = reader.getObjectFromForm(null, contactForm, AbstractMetadataReader.ISO_19115);
                contacts.put(responsibleParty, contactForm.getRoot());
            }
        }

    }

    /**
     * Return an MDWeb {@link Form} from an object.
     *
     * @param object The object to transform in form.
     * @return an MDWeb {@link Form} representing the metadata object.
     */
    protected Form getFormFromObject(final Object object) throws MD_IOException {
        final String title = Utils.findTitle(object);
        return getFormFromObject(object, defaultUser, mdRecordSet, null, title);
    }

    /**
     * Return an MDWeb {@link Form} from an object.
     *
     * @param object The object to transform in form.
     * @return an MDWeb {@link Form} representing the metadata object.
     */
    protected Form getFormFromObject(final Object object, String title) throws MD_IOException {
        if (title == null) {
            title = Utils.findTitle(object);
        }
        return getFormFromObject(object, defaultUser, mdRecordSet, null, title);
    }

    /**
     * Return an MDWeb {@link Form} from an object.
     * 
     * @param object The object to transform in form.
     * @return an MDWeb {@link Form} representing the metadata object.
     */
    protected Form getFormFromObject(final Object object, final User user, final RecordSet recordSet, Profile profile, String title) throws MD_IOException {
        
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
                final String msg = "Can't register ths kind of object:" + object.getClass().getName();
                LOGGER.severe(msg);
                throw new IllegalArgumentException(msg);
            }

            final String identifier = Utils.findIdentifier(object);
            if (mdWriter.isAlreadyUsedIdentifier(identifier)) {
                throw new MD_IOException("The identifier " + identifier + " is already used");
            }
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
    protected List<Value> addValueFromObject(final Form form, Object object, Path path, Value parentValue) throws MD_IOException {

        final List<Value> result = new ArrayList<Value>();

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
                    path = mdWriter.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3");
                }
                result.addAll(addValueFromObject(form, obj, path, parentValue));
                
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
            final Value rootValue = new Value(path, form, ordinal, classe, parentValue);
            result.add(rootValue);

            // 2. The default value
            final String defaultValue = dis.toString(null);
            final Path defaultValuePath = new Path(path, classe.getPropertyByName("value"));
            final TextValue textValue = new TextValue(defaultValuePath, form , ordinal, defaultValue, mdWriter.getClasse("CharacterString", Standard.ISO_19103), rootValue);
            result.add(textValue);

            // 3. the localised values
            final Classe localisedString = mdWriter.getClasse("LocalisedCharacterString", Standard.ISO_19103);
            for (Locale locale : dis.getLocales())  {
                if (locale == null) continue;
                
                final Path valuePath = new Path(path, classe.getPropertyByName("textGroup"));
                final Value value = new Value(valuePath, form, ordinal, localisedString, rootValue);
                result.add(value);

                final String localisedValue = dis.toString(locale);
                final Path locValuePath = new Path(valuePath, localisedString.getPropertyByName("value"));
                final TextValue locValValue = new TextValue(locValuePath, form , ordinal, localisedValue, mdWriter.getClasse("CharacterString", Standard.ISO_19103), value);
                result.add(locValValue);

                final Path localePath = new Path(valuePath, localisedString.getPropertyByName("locale"));
                final String localeDesc = "#locale-" + locale.getISO3Language();
                final TextValue localeValue = new TextValue(localePath, form , ordinal, localeDesc, mdWriter.getClasse("CharacterString", Standard.ISO_19103), value);
                result.add(localeValue);
            }

        //Special case for PT_Locale
        } else if (classe.getName().equals("PT_Locale")) {
            final Locale loc = (Locale) object;

            // 1. the root Value PT_Locale
            final Value rootValue = new Value(path, form, ordinal, classe, parentValue);
            result.add(rootValue);

            // 2. The languageCode value
            final String languageValue   = loc.getLanguage();
            final Path languageValuePath = new Path(path, classe.getPropertyByName("languageCode"));
            final TextValue lanTextValue = new TextValue(languageValuePath, form , ordinal, languageValue, mdWriter.getClasse("LanguageCode", Standard.ISO_19115), rootValue);
            result.add(lanTextValue);

            // 3. the country value
            final String countryValue    = loc.getCountry();
            final Path countryValuePath  = new Path(path, classe.getPropertyByName("country"));
            final TextValue couTextValue = new TextValue(countryValuePath, form , ordinal, countryValue, mdWriter.getClasse("CountryCode", Standard.ISO_19115), rootValue);
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
        
        // if we have already see this object we build a Linked Value.
        } else if (linkedValue != null) {

            final LinkedValue value = new LinkedValue(path, form, ordinal, linkedValue.getForm(), linkedValue, classe, parentValue);
            result.add(value);
        
        // else we build a Value node.
        } else {
        
            final Value value = new Value(path, form, ordinal, classe, parentValue);
            result.add(value);
            //we add this object to the listed of already write element
            if (!isNoLink()) {
                alreadyWrite.put(object, value);
            }
            
            do {
                for (Property prop: classe.getProperties()) {
                    // TODO remove when fix in MDweb2
                    if (prop.getName().equals("geographicElement3") ||  prop.getName().equals("geographicElement4"))
                        continue;
                    
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
                    // we get directly the field
                    } else if (!"unitOfMeasure".equals(propName) && !"verticalDatum".equals(propName)) {
                        final Class valueClass     = object.getClass();
                        final Object propertyValue = getValueFromField(valueClass, propName, object);
                        if (propertyValue != null) {
                            final Path childPath = new Path(path, prop);

                            //if the path is not already in the database we write it
                            if (mdWriter.getPath(childPath.getId()) == null) {
                                mdWriter.writePath(childPath);
                            }
                            result.addAll(addValueFromObject(form, propertyValue, childPath, value));
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
            } catch (SecurityException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (IllegalAccessException ex) {
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
        } else {
            propName = attributeName;
        }

        return propName;
    }
    

    /**
     * Return an MDWeb {@link Classe} object for the specified java object.
     * 
     * @param object the object to identify
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
            
            // special case for the sub classe of Xlink ObjectReference
            if (object instanceof ObjectReference) {
                return mdWriter.getClasse("XLink", mdWriter.getStandard("Xlink"));
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
            while (nameType < 2) {
                
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
                        default:
                            nameType = 2;
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
            LOGGER.warning("The database does not conatins the primitive type:" + mdwclassName + " in the standard:" + mdwStandard.getName());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean storeMetadata(final Object obj) throws MetadataIoException {
        return storeMetadata(obj, null);
    }

    public boolean storeMetadata(Object obj, final String title) throws MetadataIoException {
        // profiling operation
        final long start = System.currentTimeMillis();
        long transTime   = 0;
        long writeTime   = 0;
        
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }

        // we create a MDWeb form form the object
        Form form       = null;
        Profile profile = null;
        try {
            // we try to determine the profile for the Object
            if ("org.geotoolkit.csw.xml.v202.RecordType".equals(obj.getClass().getName())) {
                profile = mdWriter.getProfile("DublinCore");
            } else if (obj instanceof Metadata) {
                profile = mdWriter.getProfileByUrn(Utils.findStandardName(obj));
            }

            final long startTrans = System.currentTimeMillis();
            form                  = getFormFromObject(obj, title);
            transTime             = System.currentTimeMillis() - startTrans;
            
        } catch (IllegalArgumentException e) {
             throw new MetadataIoException("This kind of resource cannot be parsed by the service: " + obj.getClass().getSimpleName() +'\n' +
                                           "cause: " + e.getMessage(), e, null);
        } catch (MD_IOException e) {
             throw new MetadataIoException("The service has throw an MD_IOException while transforming the metadata to a MDWeb object: " + e.getMessage(), e, null);
        }
        
        // and we store it in the database
        if (form != null) {

            if (profile != null) {
	        form.setProfile(profile);
            // if the profile is null we set the level completion to complete
            } else {
                form.setInputLevelCompletion(new boolean[]{true, true, true}, new Date(System.currentTimeMillis()));
            }
            
            try {
                final long startWrite = System.currentTimeMillis();
                mdWriter.writeForm(form, false, true);
                writeTime             = System.currentTimeMillis() - startWrite;

            } catch (MD_IOException e) {
                throw new MetadataIoException("The service has throw an SQLException while writing the metadata :" + e.getMessage(), e, null);
            }
            
            final long time = System.currentTimeMillis() - start;

            final StringBuilder report = new StringBuilder("inserted new Form: ");
            report.append(form.getTitle()).append('[').append(form.getIdentifier()).append(']').append("( ID:").append(form.getId());
            report.append(" in ").append(time).append(" ms (transformation: ").append(transTime).append(" DB write: ").append(writeTime).append(")");
            LOGGER.log(logLevel, report.toString());
            if (!noIndexation) {
                indexDocument(form);
            }
            return true;

        }
        return false;
    }

    protected void indexDocument(final Form f) {
        //need to be override by child
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
    public boolean deleteMetadata(final String identifier) throws MetadataIoException {
        LOGGER.log(logLevel, "metadata to delete:{0}", identifier);

        if (identifier == null) {
            return false;
        }
        try {
            // TODO is a way more fast to know that the form exist? method  isAlreadyRecordedForm(int id) writer20
            final FormInfo f          = mdWriter.getFormInfo(identifier);
            if (f != null) {
                mdWriter.deleteForm(f);
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
    public boolean replaceMetadata(final String metadataID, final Object any) throws MetadataIoException {
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
    public boolean isAlreadyUsedIdentifier(String metadataID) throws MetadataIoException {
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
