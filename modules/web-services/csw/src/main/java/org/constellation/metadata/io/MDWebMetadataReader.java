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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.measure.unit.Unit;
import javax.xml.namespace.QName;

// Constellation Dependencies
import org.constellation.ws.CstlServiceException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.ReflectionUtilities;
import org.constellation.util.Util;
import org.constellation.util.StringUtilities;
import static org.constellation.metadata.CSWQueryable.*;
        
// MDWeb dependencies
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.io.Reader;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.v20.Reader20;
import org.mdweb.model.thesaurus.Word;
import org.mdweb.io.sql.LocalReaderThesaurus;

// geotoolkit/GeoAPI dependencies
import org.geotoolkit.metadata.iso.MetadataEntity;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.Settable;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.internal.CodeLists;
import org.geotoolkit.io.wkt.UnformattableObjectException;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.csw.xml.TypeNames.*;

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
public class MDWebMetadataReader extends MetadataReader {

    /**
     * A reader to the MDWeb database.
     */
    private Reader mdReader;
    
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
    private Map<Value, Object> alreadyRead;
    
    /**
     * A List of the already logged Missing MDWeb Classe.
     */
    private List<String> classeNotFound;

    /**
     * A map of label - concept URI loaded from a Thesaurus.
     * They are used to make Anchor mark in the xml export.
     */
    private Map<String, URI> conceptMap;

    /**
     * A map of binding term-path for each standard.
     */
    private static final Map<Standard, Map<String, String>> DUBLINCORE_PATH_MAP;
    static {
        DUBLINCORE_PATH_MAP          = new HashMap<Standard, Map<String, String>>();
        
        final Map<String, String> isoMap = new HashMap<String, String>();
        isoMap.put("identifier",  "ISO 19115:MD_Metadata:fileIdentifier");
        isoMap.put("type",        "ISO 19115:MD_Metadata:hierarchyLevel");
        isoMap.put("date",        "ISO 19115:MD_Metadata:dateStamp");
        isoMap.put("subject",     "ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        isoMap.put("subject2",    "ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        isoMap.put("format",      "ISO 19115:MD_Metadata:identificationInfo:resourceFormat:name");
        isoMap.put("abstract",    "ISO 19115:MD_Metadata:identificationInfo:abstract");
        isoMap.put("boundingBox", "ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2");
        isoMap.put("creator",     "ISO 19115:MD_Metadata:identificationInfo:credit");
        isoMap.put("publisher",   "ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        isoMap.put("language",    "ISO 19115:MD_Metadata:language");
        isoMap.put("rights",      "ISO 19115:MD_Metadata:identificationInfo:resourceConstraint:useLimitation");
        DUBLINCORE_PATH_MAP.put(Standard.ISO_19115, isoMap);
        
        final Map<String, String> ebrimMap = new HashMap<String, String>();
        ebrimMap.put("identifier", "Ebrim v3.0:RegistryObject:id");
        ebrimMap.put("type",       "Ebrim v3.0:RegistryObject:objectType");
        ebrimMap.put("abstract",   "Ebrim v3.0:RegistryObject:description:localizedString:value");
        ebrimMap.put("format",     "Ebrim v3.0:ExtrinsicObject:mimeType");
        ebrimMap.put("subject",    "Ebrim v3.0:RegistryObject:slot:valueList:value");
        ebrimMap.put("boudingBox", "Ebrim v3.0:RegistryObject:slot:valueList:value");
        /*TODO @name = “http://purl.org/dc/elements/1.1/subject”
          TODO @slotType =“*:GM_Envelope”
          ebrimMap.put("creator",    "Ebrim v3:RegistryObject:identificationInfo:credit");
          ebrimMap.put("publisher",  "Ebrim v3:RegistryObject:distributionInfo:distributor:distributorContact:organisationName");
          ebrimMap.put("language",   "Ebrim v3:RegistryObject:language");
          TODO find ebrimMap.put("date",       "Ebrim V3:RegistryObject:dateStamp");
         */
        DUBLINCORE_PATH_MAP.put(Standard.EBRIM_V3, ebrimMap);
    }
    
    /**
     * Build a new metadata Reader.
     * 
     * @param MDReader a reader to the MDWeb database.
     */
    public MDWebMetadataReader(Automatic configuration) throws CstlServiceException {
        super(true, false);
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            final Connection mdConnection = db.getConnection();
            final boolean isPostgres = db.getClassName().equals("org.postgresql.Driver");
            this.mdReader           = new Reader20(mdConnection, isPostgres);
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the MDWeb reader:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }

        conceptMap = new HashMap<String, URI>();
        final List<BDD> thesaurusDBs = configuration.getThesaurus();
        for (BDD thesaurusDB : thesaurusDBs) {
            try {

                final Connection tConnection       = thesaurusDB.getConnection();
                final LocalReaderThesaurus tReader = new LocalReaderThesaurus(tConnection);
                final List<Word> words             = tReader.getWords();
                tReader.close();
                for (Word word : words) {
                    try {
                        final URI uri = new URI(word.getUriConcept());
                        conceptMap.put(word.getLabel(), uri);

                    } catch (URISyntaxException ex) {
                        LOGGER.warning("URI syntax exception for:" + word.getUriConcept());
                    }
                }
                
            } catch (SQLException ex) {
                LOGGER.warning("SQLException while initializing the Thesaurus reader: " + thesaurusDB.getConnectURL());
            }
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
     * Fill the package attributes with all the subPackage of the specified ones.
     */
    private void initPackage() {

        this.geotoolkitPackage  = Util.searchSubPackage("org.geotoolkit.metadata", "org.geotoolkit.referencing",
                                                        "org.geotoolkit.service", "org.geotoolkit.naming", "org.geotoolkit.feature.catalog",
                                                        "org.geotoolkit.metadata.fra", "org.geotoolkit.temporal.object");
        this.sensorMLPackage    = Util.searchSubPackage("org.geotoolkit.sml.xml.v100");
        this.swePackage         = Util.searchSubPackage("org.geotoolkit.swe.xml.v100");
        this.gmlPackage         = Util.searchSubPackage("org.geotoolkit.gml.xml.v311");

        this.opengisPackage     = Util.searchSubPackage("org.opengis.metadata", "org.opengis.referencing", "org.opengis.temporal",
                                                        "org.opengis.service", "org.opengis.feature.catalog");
        this.cswPackage         = Util.searchSubPackage("org.geotoolkit.csw.xml.v202", "org.geotoolkit.dublincore.xml.v2.elements", "org.geotoolkit.ows.xml.v100",
                                                        "org.geotoolkit.ogc.xml");
        this.ebrimV3Package     = Util.searchSubPackage("org.geotoolkit.ebrim.xml.v300", "org.geotoolkit.wrs.xml.v100");
        this.ebrimV25Package    = Util.searchSubPackage("org.geotoolkit.ebrim.xml.v250", "org.geotoolkit.wrs.xml.v090");
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

            for(Object className : prop.keySet()) {
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
     * @param identifier The form identifier with the pattern : "Form_ID:Catalog_Code"
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotoolkit metadata / ebrim registry object)
     * 
     * @throws java.sql.CstlServiceException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws CstlServiceException {
        int id;
        String catalogCode = "";
        
        //we parse the identifier (Form_ID:Catalog_Code)
        try  {
            if (identifier.indexOf(':') != -1) {
                catalogCode  = identifier.substring(identifier.indexOf(':') + 1, identifier.length());
                identifier   = identifier.substring(0, identifier.indexOf(':'));
                id           = Integer.parseInt(identifier);
            } else {
                throw new NumberFormatException();
            }
            
        } catch (NumberFormatException e) {
             throw new CstlServiceException("Unable to parse: " + identifier, NO_APPLICABLE_CODE, "id");
        }

        try {
            alreadyRead.clear();
            final Catalog catalog = mdReader.getCatalog(catalogCode);

            //we look for cached object
            Object result = getFromCache(identifier);
            if (mode == ISO_19115 || mode == EBRIM || mode == SENSORML) {

                if (result == null) {
                    final Form f = mdReader.getForm(catalog, id);
                    result = getObjectFromForm(identifier, f, mode);
                } else {
                    LOGGER.finer("getting from cache: " + identifier);
                }

                result = applyElementSet(result, type, elementName);

            } else if (mode == DUBLINCORE) {

                final Form form                   = mdReader.getForm(catalog, id);
                if (form != null) {
                    final Value top               = form.getTopValue();
                    final Standard recordStandard = top.getType().getStandard();

                    /*
                     * if the standard of the record is CSW and the record is cached we return it.
                     * if the record is not yet cached we proccess.
                     * if the record have to be transform from the orginal standard to CSW we process.
                     */
                    if (!recordStandard.equals(Standard.CSW) || result == null)
                        result = getRecordFromForm(identifier, form, type, elementName);

                    result = applyElementSet(result, type, elementName);
                } else {
                    throw new CstlServiceException("Unable to read the form: " + identifier, NO_APPLICABLE_CODE, "id");
                }

            } else {
                throw new IllegalArgumentException("Unknow standard mode: " + mode);
            }
            return result;

        } catch (MD_IOException e) {
             throw new CstlServiceException("SQL exception while reading the metadata: " + identifier, NO_APPLICABLE_CODE, "id");
        }
    }
    
    /**
     * Return a dublinCore record from a MDWeb formular
     * 
     * @param form the MDWeb formular.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType getRecordFromForm(String identifier, Form form, ElementSetType type, List<QName> elementName) throws MD_IOException {
        final Value top                   = form.getTopValue();
        final Standard  recordStandard    = top.getType().getStandard();
        
        if (recordStandard.equals(Standard.ISO_19115) || recordStandard.equals(Standard.EBRIM_V3)) {
            return transformMDFormInRecord(form, type, elementName);
        
        } else {
            final Object obj =  getObjectFromForm(identifier, form, DUBLINCORE);
            
            if (obj instanceof AbstractRecordType) {
                return (AbstractRecordType) obj;
            
            } else {
                String objType = "null";
                if (obj != null)
                    objType = obj.getClass().getName();
                
                throw new IllegalArgumentException("Unexpected type in getRecordFromForm. waiting for AbstractRecordType, got: " + objType);
            }
        }
    }
   
    /**
     * Return a dublinCore record from a ISO 19115 MDWeb formular
     * 
     * @param form the MDWeb formular.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType transformMDFormInRecord(Form form, ElementSetType type, List<QName> elementName) throws MD_IOException {
        
        final Value top                   = form.getTopValue();
        final Standard  recordStandard    = top.getType().getStandard();
        final Map<String, String> pathMap = DUBLINCORE_PATH_MAP.get(recordStandard);
        
        // we get the title of the form
        final SimpleLiteral title             = new SimpleLiteral(null, form.getTitle());
        
        // we get the file identifier(s)
        final List<Value>   identifierValues  = form.getValueFromPath(mdReader.getPath(pathMap.get("identifier")));
        final List<String>  identifiers       = new ArrayList<String>();
        for (Value v: identifierValues) {
            if (v instanceof TextValue) {
                identifiers.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral identifier = new SimpleLiteral(null, identifiers);
        
        //we get The boundingBox(es)
        final List<Value>   bboxValues     = form.getValueFromPath(mdReader.getPath(pathMap.get("boundingBox")));
        final List<BoundingBoxType> bboxes = new ArrayList<BoundingBoxType>();
        for (Value v: bboxValues) {
            bboxes.add(createBoundingBoxFromValue(v.getOrdinal(), form));
        }
        
        //we get the type of the data
        final List<Value> typeValues  = form.getValueFromPath(mdReader.getPath(pathMap.get("type")));
        String dataType               = null;
        SimpleLiteral litType         = null;
        try {
            if (typeValues.size() != 0) {
                final TextValue value = (TextValue)typeValues.get(0);
                final int code = Integer.parseInt(value.getValue());
                final org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList)value.getType();
                final CodeListElement element = codelist.getElementByCode(code);
                dataType = element.getName();
            } 
            litType = new SimpleLiteral(null, dataType);
        } catch (NumberFormatException ex) {
            LOGGER.severe("Number format exception while trying to get the DC type");
        }
        
        
        // we get the keywords
        final List<Value> keywordsValues  = form.getValueFromPath(mdReader.getPath(pathMap.get("subject")));
        final List<SimpleLiteral> keywords = new ArrayList<SimpleLiteral>();
        for (Value v: keywordsValues) {
            if (v instanceof TextValue) {
                keywords.add(new SimpleLiteral(null, ((TextValue)v).getValue()));
            }
        }

        // we get the keywords
        final List<Value> topicCategoriesValues  = form.getValueFromPath(mdReader.getPath(pathMap.get("subject2")));
        for (Value v: topicCategoriesValues) {
            if (v instanceof TextValue) {
                if (v.getType() instanceof org.mdweb.model.schemas.CodeList) {
                    final org.mdweb.model.schemas.CodeList c = (org.mdweb.model.schemas.CodeList) v.getType();
                    int code = 0;
                    try {
                        code = Integer.parseInt(((TextValue)v).getValue());
                    } catch (NumberFormatException ex) {
                        LOGGER.severe("unable to parse the codeListelement:" + ((TextValue)v).getValue());
                    }
                    final CodeListElement element = c.getElementByCode(code);
                    if (element != null) {
                        keywords.add(new SimpleLiteral(null, element.getName()));
                    } else {
                        LOGGER.severe("no such codeListElement:" + code + " for the codeList:" + c.getName());
                    }
                } else {
                    keywords.add(new SimpleLiteral(null, ((TextValue)v).getValue()));
                }
            }
        }
        // and the topicCategeoryy
        final List<Value> formatsValues  = form.getValueFromPath(mdReader.getPath(pathMap.get("format")));
        final List<String> formats = new ArrayList<String>();
        for (Value v: formatsValues) {
            if (v instanceof TextValue) {
                formats.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral format;
        if (formats.size() != 0) {
            format = new SimpleLiteral(null, formats);
        } else {
            format = null;
        }
        
        final List<Value> dateValues  = form.getValueFromPath(mdReader.getPath(pathMap.get("date")));
        final List<String> dates = new ArrayList<String>();
        for (Value v: dateValues) {
            if (v instanceof TextValue) {
                dates.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral date = new SimpleLiteral(null, dates);
        
        // the last update date
        final SimpleLiteral modified = new SimpleLiteral(null, dates);
        
        // the descriptions
        final List<Value>   descriptionValues = form.getValueFromPath(mdReader.getPath(pathMap.get("abstract")));
        final List<String>  descriptions      = new ArrayList<String>();
        for (Value v: descriptionValues) {
            if (v instanceof TextValue) {
                descriptions.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral description = new SimpleLiteral(null, descriptions);
        
        // TODO add spatial
        
        
        
        // the creator of the data
        final List<Value>   creatorValues = form.getValueFromPath(mdReader.getPath(pathMap.get("creator")));
        final List<String>  creators      = new ArrayList<String>();
        for (Value v: creatorValues) {
            if (v instanceof TextValue) {
                creators.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral creator;
        if (creators.size() > 0) {
            creator = new SimpleLiteral(null, creators);
        } else {
            creator = null;
        }
        
        // the publisher of the data
        final List<Value>   publisherValues = form.getValueFromPath(mdReader.getPath(pathMap.get("publisher")));
        final List<String>  publishers      = new ArrayList<String>();
        for (Value v: publisherValues) {
            if (v instanceof TextValue) {
                publishers.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral publisher = new SimpleLiteral(null, publishers);
        
        // TODO the contributors
        // TODO the source of the data

        // The rights
        final List<Value>   rightValues = form.getValueFromPath(mdReader.getPath(pathMap.get("rights")));
        final List<String>  rights      = new ArrayList<String>();
        for (Value v: rightValues) {
            if (v instanceof TextValue) {
                rights.add(((TextValue)v).getValue());
            }
        }
        
        final SimpleLiteral right;
        if (rights.size() >  0) {
            right = new SimpleLiteral(null, rights);
        } else {
            right = null;
        }

        // the language
        final List<Value>   languageValues = form.getValueFromPath(mdReader.getPath(pathMap.get("language")));
        final List<String>  languages      = new ArrayList<String>();
        for (Value v: languageValues) {
            if (v instanceof TextValue) {
                languages.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral language = new SimpleLiteral(null, languages);
        
        final RecordType fullResult = new RecordType(identifier, title, litType , keywords, format, modified, date, description, bboxes,
                        creator, publisher, language, null, null);
        if (right != null) {
            fullResult.setRights(right);
        }

        
        // for an ElementSetName mode
        if (elementName == null || elementName.size() == 0) {
            if (type.equals(ElementSetType.BRIEF)) {
                return new BriefRecordType(identifier, title, litType , bboxes);
            }
        
            if (type.equals(ElementSetType.SUMMARY)) {
                return new SummaryRecordType(identifier, title, litType , bboxes, keywords, format, modified, description);
                
            } else {
            
                return fullResult;
            }
        
        // for an element name mode    
        } else {
            final RecordType result = new RecordType();
            for (QName qn : elementName) {

                try {
                    final Method getter = ReflectionUtilities.getGetterFromName(qn.getLocalPart(), RecordType.class);
                    final Object param  = ReflectionUtilities.invokeMethod(fullResult, getter);

                    Method setter = null;
                    if (param != null) {
                        setter =ReflectionUtilities.getSetterFromName(qn.getLocalPart(), param.getClass(), RecordType.class);
                    }

                    if (setter != null) {
                        ReflectionUtilities.invokeMethod(setter, result, param);
                    } else {
                        String paramDesc = "null";
                        if (param != null) {
                            paramDesc = param.getClass() + "";
                        }
                        LOGGER.warning("No setter have been found for attribute " + qn.getLocalPart() +" of type " + paramDesc + " in the class RecordType");
                    }

                } catch (IllegalArgumentException ex) {
                    LOGGER.info("illegal argument exception while invoking the method get" + StringUtilities.firstToUpper(qn.getLocalPart()) + " in the RecordType class");
                }
            }
            return result;
        }
    }
    
    /**
     * Create a bounding box from a geographiqueElement Value
     */
    private BoundingBoxType createBoundingBoxFromValue(int ordinal, Form f) throws MD_IOException {
        Double  southValue  = null;
        Double eastValue    = null;
        Double  westValue   = null;
        Double northValue  = null;
        String crs  = null;
            try {
            //we get the CRS
            final List<Value> crsValues = f.getValueFromPath(mdReader.getPath("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code"));
            for (Value v: crsValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    crs = ((TextValue)v).getValue();
                }
            }
        
            //we get the east value
            final List<Value> eastValues = f.getValueFromPath(mdReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude"));
            for (Value v: eastValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    eastValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        
            //we get the east value
            final List<Value> westValues = f.getValueFromPath(mdReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude"));
            for (Value v: westValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    westValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        
            //we get the north value
            final List<Value> northValues = f.getValueFromPath(mdReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude"));
            for (Value v: northValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    northValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        
            //we get the south value
            final List<Value> southValues = f.getValueFromPath(mdReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude"));
            for (Value v: southValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    southValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        } catch (NumberFormatException ex) {
            LOGGER.severe("unable to parse a double in bounding box value:" + '\n' + ex.getMessage() ) ;
        }
        
        if (eastValue != null && westValue != null && northValue != null && southValue != null) {
            if (crs != null && crs.indexOf("EPSG:") == -1) {
                crs = "EPSG:" + crs;
            }
            final BoundingBoxType result = new BoundingBoxType(crs,
                                                               eastValue,
                                                               southValue,
                                                               westValue,
                                                               northValue);
            LOGGER.finer("boundingBox created");
            return result;
        } else {
            LOGGER.info("boundingBox null");
            return null;
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
    private Object getObjectFromForm(String identifier, Form form, int mode) {

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
    
    private Object applyElementSet(Object result, ElementSetType type, List<QName> elementName) {

         if (type == null)
            type = ElementSetType.FULL;
        
        // then we apply the elementSet/elementName filter
        
        // for an ElementSetName mode
        if (elementName == null || elementName.size() == 0) {
            
            //if the result can't be filtered by Set filter we return it.
            if (!(result instanceof Settable)) {
                return result;
            }
            // for a element set FULL we return the record directly
            if (type == null || type.equals(ElementSetType.FULL)) {
                return result;

            // Summary view
            } else if (type.equals(ElementSetType.SUMMARY)) {
                return ((Settable) result).toSummary();

            // Brief view
            } else if (type.equals(ElementSetType.BRIEF)) {
                return ((Settable) result).toBrief();

            // this case must never happen
            } else {
                return null;
            }

        // for an element name mode    
        } else {
            final Class recordClass    = result.getClass();
            final Object filtredResult = ReflectionUtilities.newInstance(recordClass);

            for (QName qn : elementName) {
                String currentMethodType = "";
                try {
                    currentMethodType   = "get";
                    final Method getter = ReflectionUtilities.getGetterFromName(qn.getLocalPart(), recordClass);
                    final Object param  = ReflectionUtilities.invokeMethod(result, getter);

                    Method setter = null;
                    if (param != null) {
                        currentMethodType   = "set";
                        Class paramClass = param.getClass();
                        if (paramClass.equals(ArrayList.class)) {
                            paramClass = List.class;
                        }
                        setter = ReflectionUtilities.getSetterFromName(qn.getLocalPart(), paramClass, recordClass);
                    }
                    if (setter != null) {
                        ReflectionUtilities.invokeMethod(setter, filtredResult, param);
                    } else {
                        String paramDesc = "null";
                        if (param != null) {
                            paramDesc = param.getClass() + "";
                        }
                        LOGGER.warning("No setter have been found for attribute " + qn.getLocalPart() +" of type " + paramDesc + " in the class " + recordClass);
                    }

                } catch (IllegalArgumentException ex) {
                    LOGGER.severe("illegal argument exception while invoking the method " + currentMethodType + StringUtilities.firstToUpper(qn.getLocalPart()) + " in the classe RecordType!");
                }
            }
            return filtredResult;
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
        String className;
        String standardName;
        if (value.getType() != null) {
            className    = value.getType().getName();
            standardName = value.getType().getStandard().getName();
        } else {
            LOGGER.severe("Error null type for value:" + value.getIdValue());
            return null;
        }
        Class classe = null;
        Object result;
        
        try {
            // we get the value's class
            classe = getClassFromName(className, standardName, mode);
            if (classe == null) {
                return null;
            }

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
                            result = ReflectionUtilities.invokeMethod(method, null, classe, element.getName());
                        } else {
                            LOGGER.severe("unknow codelist type");
                            return null;
                        }
                        
                        return result;
                    } catch (NumberFormatException e) {
                        LOGGER.severe("Format NumberException : unable to parse the code: " + textValue + " in the codelist: " + codelist.getName());
                        return null;
                    }

                // if the value is a date we call the static method parse 
                // instead of a constructor (temporary patch: createDate method)  
                } else if (classe.equals(Date.class)) {
                    return Util.createDate(textValue, formatter);

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

        } catch (ParseException e) {
            LOGGER.severe("The date cannot be parsed ");
            return null;
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

                //special case due to a bug in mdweb
                if (attribName.startsWith("geographicElement")) {
                    attribName = "geographicElements";
                } else if (attribName.equals("transformationParameterAvailability")) {
                    attribName = "transformationParameterAvailable";
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
    private Class getClassFromName(String className, String standardName, int mode) {
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
            while (nameType < 8) {
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
                        // we put Default before the className
                        case 4: {
                            name = name.substring(0, name.indexOf("Type"));
                            name = "Default" + name;
                            nameType = 5;
                            break;
                        }
                        // we put FRA before the className
                        case 5: {
                            name = "FRA" + name;
                            nameType = 6;
                            break;
                        }
                        // we put PropertyType behind the className
                        case 6: {
                            name = name.substring(10, name.length());
                            name += "PropertyType";
                            nameType = 7;
                            break;
                        }
                        default:
                            nameType = 8;
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
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws CstlServiceException {
        final List<DomainValues> responseList = new ArrayList<DomainValues>();
        final StringTokenizer tokens          = new StringTokenizer(propertyNames, ",");
        
        while (tokens.hasMoreTokens()) {
            final String token       = tokens.nextToken().trim();
            List<String> paths = null;
            if (ISO_QUERYABLE.get(token) != null) {
                paths = ISO_QUERYABLE.get(token);
            }
            
            if (paths == null && DUBLIN_CORE_QUERYABLE.get(token) != null) {
                paths = DUBLIN_CORE_QUERYABLE.get(token);
            }

            if (paths == null && INSPIRE_QUERYABLE.get(token) != null) {
                paths = INSPIRE_QUERYABLE.get(token);
            }

            if (paths == null) {
                throw new CstlServiceException("The property " + token + " is not queryable",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }
            
            if (paths.size() != 0) {
                try {
                    final List<String> values         = mdReader.getDomainOfValuesFromPaths(paths, true);
                    final ListOfValuesType listValues = new ListOfValuesType(values);
                    final DomainValuesType value      = new DomainValuesType(null, token, listValues, METADATA_QNAME);
                    responseList.add(value);

                } catch (MD_IOException e) {
                    throw new CstlServiceException(e, NO_APPLICABLE_CODE);
                }
            } else {
                throw new CstlServiceException("The property " + token + " is not queryable for now",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }
            
        }
        return responseList;
    }

    @Override
    public List<String> executeEbrimSQLQuery(String sqlQuery) throws CstlServiceException {
        try {
            return mdReader.executeFilterQuery(sqlQuery);
        } catch (MD_IOException ex) {
           throw new CstlServiceException("The service has throw an SQL exception while making eberim request:" + '\n' +
                                         "Cause: " + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
     
    @Override
    public void destroy() {
        try {
            mdReader.close();
            classBinding.clear();
            alreadyRead.clear();
        } catch (MD_IOException ex) {
            LOGGER.severe("SQL Exception while destroying MDWeb MetadataReader");
        }
    }

    @Override
    public List<? extends Object> getAllEntries() throws CstlServiceException {
        final List<Object> results = new ArrayList<Object>();
        try {
            final List<Catalog> catalogs = mdReader.getCatalogs();
            final List<Form> forms       = mdReader.getAllForm(catalogs);
            for (Form f: forms) {
                results.add(getObjectFromForm("no cache", f, -1));
            }
        } catch (MD_IOException ex) {
            throw new CstlServiceException("SQL Exception while getting all the entries: " +ex.getMessage(), NO_APPLICABLE_CODE);
        }
        return results;
    }

    @Override
    public List<String> getAllIdentifiers() throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public List<Integer> getSupportedDataTypes() {
        return Arrays.asList(ISO_19115, DUBLINCORE, EBRIM);
    }

    @Override
    public List<QName> getAdditionalQueryableQName() {
        return Arrays.asList(DEGREE_QNAME,
                             ACCESS_CONSTRAINTS_QNAME,
                             OTHER_CONSTRAINTS_QNAME,
                             INS_CLASSIFICATION_QNAME,
                             CONDITION_APPLYING_TO_ACCESS_AND_USE_QNAME,
                             METADATA_POINT_OF_CONTACT_QNAME,
                             LINEAGE_QNAME,
                             SPECIFICATION_TITLE_QNAME,
                             SPECIFICATION_DATE_QNAME,
                             SPECIFICATION_DATETYPE_QNAME);
    }

    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return INSPIRE_QUERYABLE;
    }

    @Override
    public Map<String, URI> getConceptMap() {
        return conceptMap;
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
            String catalogCode = "";

            //we parse the identifier (Form_ID:Catalog_Code)
            try {
                if (identifier.indexOf(':') != -1) {
                    catalogCode = identifier.substring(identifier.indexOf(':') + 1, identifier.length());
                    identifier  = identifier.substring(0, identifier.indexOf(':'));
                    id = Integer.parseInt(identifier);
                } else {
                    throw new NumberFormatException();
                }

                final Catalog catalog = mdReader.getCatalog(catalogCode);

                mdReader.removeFormFromCache(catalog, id);

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
