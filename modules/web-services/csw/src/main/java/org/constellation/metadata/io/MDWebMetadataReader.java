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
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;

// Constellation Dependencies
import org.constellation.ws.CstlServiceException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;
import org.constellation.util.StringUtilities;
import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.TypeNames.*;
        
// MDWeb dependencies
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.storage.LinkedValue;
import org.mdweb.sql.Reader;
import org.mdweb.sql.v20.Reader20;

// geotoolkit/GeoAPI dependencies
import org.geotoolkit.metadata.iso.MetadataEntity;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSet;
import org.geotoolkit.csw.xml.Settable;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ElementSetType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.opengis.util.CodeList;


/**
 * A database Reader designed for an MDweb database.
 * 
 * It read The mdweb forms into the database and instanciate them into geotools object.
 * When an object have been read it is stored in cache.
 * 
 * @author Guilhem legal
 */
public class MDWebMetadataReader extends MetadataReader {

    /**
     * A reader to the MDWeb database.
     */
    private Reader MDReader;
    
    /**
     * A map containing the mapping beetween the MDWeb className and java typeName
     */
    private Map<String, Class> classBinding;
    
    /**
     * Record the date format in the metadata.
     */
    private DateFormat dateFormat; 
    
    /**
     * A list of package containing the ISO 19115 interfaces (and the codelist classes)
     */
    private List<String> opengisPackage;
    
    /**
     * A list of package containing the ISO 19115 implementation.
     */
    private List<String> geotoolsPackage;
    
    /**
     * A list of package containing the CSW and dublinCore implementation
     */
    private List<String> CSWPackage;
    
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
     * A map of binding term-path for each standard.
     */
    private final static Map<Standard, Map<String, String>> DublinCorePathMap;
    static {
        DublinCorePathMap          = new HashMap<Standard, Map<String, String>>();
        
        Map<String, String> isoMap = new HashMap<String, String>();
        isoMap.put("identifier",  "ISO 19115:MD_Metadata:fileIdentifier");
        isoMap.put("type",        "ISO 19115:MD_Metadata:identificationInfo:citation:presentationForm");
        isoMap.put("date",        "ISO 19115:MD_Metadata:dateStamp");
        isoMap.put("subject",     "ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        isoMap.put("format",      "ISO 19115:MD_Metadata:identificationInfo:resourceFormat:name");
        isoMap.put("abstract",    "ISO 19115:MD_Metadata:identificationInfo:abstract");
        isoMap.put("boundingBox", "ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2");
        isoMap.put("creator",     "ISO 19115:MD_Metadata:identificationInfo:credit");
        isoMap.put("publisher",   "ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        isoMap.put("language",    "ISO 19115:MD_Metadata:language");
        DublinCorePathMap.put(Standard.ISO_19115, isoMap);
        
        Map<String, String> ebrimMap = new HashMap<String, String>();
        ebrimMap.put("identifier", "Ebrim v3.0:RegistryObject:id");
        ebrimMap.put("type",       "Ebrim v3.0:RegistryObject:objectType");
        ebrimMap.put("abstract",   "Ebrim v3.0:RegistryObject:description:localizedString:value");
        ebrimMap.put("format",     "Ebrim v3.0:ExtrinsicObject:mimeType");
        //TODO @name = “http://purl.org/dc/elements/1.1/subject”
        ebrimMap.put("subject",    "Ebrim v3.0:RegistryObject:slot:valueList:value");
        //TODO @slotType =“*:GM_Envelope”
        ebrimMap.put("boudingBox", "Ebrim v3.0:RegistryObject:slot:valueList:value");
        
        //ebrimMap.put("creator",    "Ebrim v3:RegistryObject:identificationInfo:credit");
        //ebrimMap.put("publisher",  "Ebrim v3:RegistryObject:distributionInfo:distributor:distributorContact:organisationName");
        //ebrimMap.put("language",   "Ebrim v3:RegistryObject:language");
        //TODO find ebrimMap.put("date",       "Ebrim V3:RegistryObject:dateStamp");
        DublinCorePathMap.put(Standard.EBRIM_V3, ebrimMap);
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
        BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            Connection MDConnection = db.getConnection();
            this.MDReader           = new Reader20(Standard.ISO_19115,  MDConnection);
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the MDWeb reader:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
        this.dateFormat         = new SimpleDateFormat("yyyy-MM-dd");
        
        this.geotoolsPackage    = Util.searchSubPackage("org.geotoolkit.metadata", "org.geotoolkit.referencing",
                                                        "org.geotools.service", "org.geotoolkit.naming", "org.geotools.feature.catalog",
                                                        "org.geotoolkit.metadata.fra");
        this.opengisPackage     = Util.searchSubPackage("org.opengis.metadata", "org.opengis.referencing", "org.opengis.temporal",
                                                        "org.opengis.service", "org.opengis.feature.catalog");
        this.CSWPackage         = Util.searchSubPackage("org.geotoolkit.csw.xml.v202", "org.geotoolkit.dublincore.xml.v2.elements", "org.geotoolkit.ows.xml.v100",
                                                       "org.geotoolkit.ogc.xml");
        this.ebrimV3Package     = Util.searchSubPackage("org.geotoolkit.ebrim.xml.v300", "org.geotoolkit.wrs.xml.v100");
        this.ebrimV25Package    = Util.searchSubPackage("org.geotoolkit.ebrim.xml.v250", "org.geotoolkit.wrs.xml.v090");
        
        this.classBinding       = initClassBinding(configuration.getConfigurationDirectory());
        this.alreadyRead        = new HashMap<Value, Object>();
        this.classeNotFound     = new ArrayList<String>();
    }

    /**
     * A constructor used in profile Test .
     *
     * @param MDReader a reader to the MDWeb database.
     */
    protected MDWebMetadataReader(Connection MDConnection) throws CstlServiceException {
        super(true, false);
        try  {
            this.MDReader           = new Reader20(Standard.ISO_19115,  MDConnection);
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the MDWeb reader:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
        this.dateFormat         = new SimpleDateFormat("yyyy-MM-dd");

        this.geotoolsPackage    = Util.searchSubPackage("org.geotoolkit.metadata", "org.geotoolkit.referencing",
                                                        "org.geotools.service", "org.geotoolkit.naming", "org.geotools.feature.catalog",
                                                        "org.geotoolkit.metadata.fra");
        this.opengisPackage     = Util.searchSubPackage("org.opengis.metadata", "org.opengis.referencing", "org.opengis.temporal",
                                                        "org.opengis.service", "org.opengis.feature.catalog");
        this.CSWPackage         = Util.searchSubPackage("org.geotoolkit.csw.xml.v202", "org.geotoolkit.dublincore.xml.v2.elements", "org.geotoolkit.ows.xml.v100",
                                                       "org.geotoolkit.ogc.xml");
        this.ebrimV3Package     = Util.searchSubPackage("org.geotoolkit.ebrim.xml.v300", "org.geotoolkit.wrs.xml.v100");
        this.ebrimV25Package    = Util.searchSubPackage("org.geotoolkit.ebrim.xml.v250", "org.geotoolkit.wrs.xml.v090");

        this.classBinding       = new HashMap<String, Class>();
        this.alreadyRead        = new HashMap<Value, Object>();
        this.classeNotFound     = new ArrayList<String>();
    }

    /**
     * Initialize the class binding between MDWeb database classes and java implementation classes.
     * 
     * We give the possibility to the user to add a configuration file making the mapping.
     * @return
     */
    private Map<String, Class> initClassBinding(File configDir) {
        Map<String, Class> result = new HashMap<String, Class>();
        try {
            // we get the configuration file
            File bindingFile   = new File(configDir, "classMapping.properties");
            FileInputStream in = new FileInputStream(bindingFile);
            Properties prop    = new Properties();
            prop.load(in);
            in.close();

            for(Object className : prop.keySet()) {
                try {
                    Class c = Class.forName(prop.getProperty((String)className));
                    result.put((String)className, c);
                } catch (ClassNotFoundException ex) {
                    logger.severe("error in class binding initialization for class:" + className);
                }
            }

        } catch (FileNotFoundException e) {
            logger.info("no class mapping found (optional)");
        }  catch (IOException e) {
            logger.info("no class mapping found (optional) IOException");
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
     * @return A metadata Object (dublin core Record / geotools metadata / ebrim registry object)
     * 
     * @throws java.sql.SQLException
     */
    public Object getMetadata(String identifier, int mode, ElementSet type, List<QName> elementName) throws CstlServiceException {
        int id;
        String catalogCode = "";
        
        //we parse the identifier (Form_ID:Catalog_Code)
        try  {
            if (identifier.indexOf(":") != -1) {
                catalogCode  = identifier.substring(identifier.indexOf(":") + 1, identifier.length());
                identifier   = identifier.substring(0, identifier.indexOf(":"));
                id           = Integer.parseInt(identifier);
            } else {
                throw new NumberFormatException();
            }
            
        } catch (NumberFormatException e) {
             throw new CstlServiceException("Unable to parse: " + identifier, NO_APPLICABLE_CODE, "id");
        }

        try {
            alreadyRead.clear();
            Catalog catalog = MDReader.getCatalog(catalogCode);

            //we look for cached object
            Object result = getFromCache(identifier);
            if (mode == ISO_19115 || mode == EBRIM) {

                if (result == null) {
                    Form f = MDReader.getForm(catalog, id);
                    result = getObjectFromForm(identifier, f);
                } else {
                    logger.finer("getting from cache: " + identifier);
                }

                result = applyElementSet(result, type, elementName);

            } else if (mode == DUBLINCORE) {

                Form form                  = MDReader.getForm(catalog, id);
                if (form != null) {
                    Value top                  = form.getTopValue();
                    Standard recordStandard    = top.getType().getStandard();

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

        } catch (SQLException e) {
             throw new CstlServiceException("SQL exception while reading the metadata: " + identifier, NO_APPLICABLE_CODE, "id");
        }
    }
    
    /**
     * Return a dublinCore record from a MDWeb formular
     * 
     * @param form the MDWeb formular.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType getRecordFromForm(String identifier, Form form, ElementSet type, List<QName> elementName) throws SQLException {
        Value top                   = form.getTopValue();
        Standard  recordStandard    = top.getType().getStandard();
        
        if (recordStandard.equals(Standard.ISO_19115) || recordStandard.equals(Standard.EBRIM_V3)) {
            return transformMDFormInRecord(form, type, elementName);
        
        } else {
            Object obj =  getObjectFromForm(identifier, form);
            
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
    private AbstractRecordType transformMDFormInRecord(Form form, ElementSet type, List<QName> elementName) throws SQLException {
        
        Value top                   = form.getTopValue();
        Standard  recordStandard    = top.getType().getStandard();
        Map<String, String> pathMap = DublinCorePathMap.get(recordStandard);
        
        // we get the title of the form
        SimpleLiteral title             = new SimpleLiteral(null, form.getTitle());
        
        // we get the file identifier(s)
        List<Value>   identifierValues  = form.getValueFromPath(MDReader.getPath(pathMap.get("identifier")));
        List<String>  identifiers       = new ArrayList<String>();
        for (Value v: identifierValues) {
            if (v instanceof TextValue) {
                identifiers.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral identifier = new SimpleLiteral(null, identifiers);
        
        //we get The boundingBox(es)
        List<Value>   bboxValues     = form.getValueFromPath(MDReader.getPath(pathMap.get("boundingBox")));
        List<BoundingBoxType> bboxes = new ArrayList<BoundingBoxType>();
        for (Value v: bboxValues) {
            bboxes.add(createBoundingBoxFromValue(v.getOrdinal(), form));
        }
        
        //we get the type of the data
        List<Value> typeValues  = form.getValueFromPath(MDReader.getPath(pathMap.get("type")));
        String dataType         = null;
        SimpleLiteral litType   = null;
        try {
            if (typeValues.size() != 0) {
                TextValue value = (TextValue)typeValues.get(0);
                int code = Integer.parseInt(value.getValue());
                org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList)value.getType();
                CodeListElement element = codelist.getElementByCode(code);
                dataType = element.getName();
            }
            litType = new SimpleLiteral(null, dataType);
        } catch (NumberFormatException ex) {
            logger.severe("Number format exception while trying to get the DC type");
        }
        
        
        // we get the keywords
        List<Value> keywordsValues  = form.getValueFromPath(MDReader.getPath(pathMap.get("subject")));
        List<SimpleLiteral> keywords = new ArrayList<SimpleLiteral>();
        for (Value v: keywordsValues) {
            if (v instanceof TextValue) {
                keywords.add(new SimpleLiteral(null, ((TextValue)v).getValue()));
            }
        }
        
        List<Value> formatsValues  = form.getValueFromPath(MDReader.getPath(pathMap.get("format")));
        List<String> formats = new ArrayList<String>();
        for (Value v: formatsValues) {
            if (v instanceof TextValue) {
                formats.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral format = new SimpleLiteral(null, formats);
        
        List<Value> dateValues  = form.getValueFromPath(MDReader.getPath(pathMap.get("date")));
        List<String> dates = new ArrayList<String>();
        for (Value v: dateValues) {
            if (v instanceof TextValue) {
                dates.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral date = new SimpleLiteral(null, dates);
        
        // the last update date
        Date lastUp            = form.getUpdateDate();
        SimpleLiteral modified = new SimpleLiteral(null, lastUp.toString());
        
        // the descriptions
        List<Value>   descriptionValues = form.getValueFromPath(MDReader.getPath(pathMap.get("abstract")));
        List<String>  descriptions      = new ArrayList<String>();
        for (Value v: descriptionValues) {
            if (v instanceof TextValue) {
                descriptions.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral description = new SimpleLiteral(null, descriptions);
        
        // TODO add spatial
        
        
        
        // the creator of the data
        List<Value>   creatorValues = form.getValueFromPath(MDReader.getPath(pathMap.get("creator")));
        List<String>  creators      = new ArrayList<String>();
        for (Value v: creatorValues) {
            if (v instanceof TextValue) {
                creators.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral creator = new SimpleLiteral(null, creators);
        
        // the publisher of the data
        List<Value>   publisherValues = form.getValueFromPath(MDReader.getPath(pathMap.get("publisher")));
        List<String>  publishers      = new ArrayList<String>();
        for (Value v: publisherValues) {
            if (v instanceof TextValue) {
                publishers.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral publisher = new SimpleLiteral(null, publishers);
        
        // TODO the contributors
        // TODO the source of the data
        // TODO The rights
        
        // the language
        List<Value>   languageValues = form.getValueFromPath(MDReader.getPath(pathMap.get("language")));
        List<String>  languages      = new ArrayList<String>();
        for (Value v: languageValues) {
            if (v instanceof TextValue) {
                languages.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral language = new SimpleLiteral(null, languages);
        
        if (keywords.size() != 0) {
            
        }
        
        RecordType fullResult = new RecordType(identifier, title, litType , keywords, format, modified, date, description, bboxes,
                        creator, publisher, language, null, null);
        
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
            RecordType result = new RecordType();
            for (QName qn : elementName) {

                String getterName = "get" + StringUtilities.firstToUpper(qn.getLocalPart());
                String setterName = "set" + StringUtilities.firstToUpper(qn.getLocalPart());
                try {
                    Method getter = Util.getMethod(getterName, RecordType.class);
                    Object param  = Util.invokeMethod(fullResult, getter);

                    Method setter = null;
                    if (param != null) {
                        setter = Util.getMethod(setterName, RecordType.class, param.getClass());
                    }

                    if (setter != null) {
                        Util.invokeMethod(setter, result, param);
                    }

                } catch (IllegalArgumentException ex) {
                    logger.info("illegal argument exception while invoking the method " + getterName + " in the classe RecordType");
                }
            }
            return result;
        }
    }
    
    /**
     * Create a bounding box from a geographiqueElement Value
     */
    private BoundingBoxType createBoundingBoxFromValue(int ordinal, Form f) throws SQLException {
        Double  southValue  = null;
        Double eastValue    = null;
        Double  westValue   = null;
        Double northValue  = null;
        String crs  = null;
            try {
            //we get the CRS
            List<Value> crsValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code"));
            for (Value v: crsValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    crs = ((TextValue)v).getValue();
                }
            }
        
            //we get the east value
            List<Value> eastValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude"));
            for (Value v: eastValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    eastValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        
            //we get the east value
            List<Value> westValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude"));
            for (Value v: westValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    westValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        
            //we get the north value
            List<Value> northValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude"));
            for (Value v: northValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    northValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        
            //we get the south value
            List<Value> southValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude"));
            for (Value v: southValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    southValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        } catch (NumberFormatException ex) {
            logger.severe("unable to parse a double in bounding box value:" + '\n' + ex.getMessage() ) ;
        }
        
        if (eastValue != null && westValue != null && northValue != null && southValue != null) {
            if (crs != null && crs.indexOf("EPSG:") != -1) {
                crs = "EPSG:" + crs;
            }
            BoundingBoxType result = new BoundingBoxType(crs,
                                                         eastValue,
                                                         southValue,
                                                         westValue,
                                                         northValue);
            logger.finer("boundingBox created");
            return result;
        } else {
            logger.info("boundingBox null");
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
     * @return a geotools/constellation object representing the metadata.
     */
    private Object getObjectFromForm(String identifier, Form form) {
        
        if (form != null && form.getTopValue() != null && form.getTopValue().getType() != null) {
            Value topValue = form.getTopValue();
            Object result = getObjectFromValue(form, topValue);
            
            //we put the full object in the already read metadatas.
            if (result != null) {
               addInCache(identifier, result);
            }
            return result;
        
        //debugging part to see why the form cannot be read.
        } else {
            if (form == null) {
                logger.severe("form is null");
            } else if (form.getTopValue() == null) {
                logger.severe("Top value is null");
            } else {
                logger.severe("Top value Type is null");
            }
            return null;
        }
    }
    
    private Object applyElementSet(Object result, ElementSet type, List<QName> elementName) {

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
            Class recordClass = result.getClass();
            Object filtredResult = Util.newInstance(recordClass);

            for (QName qn : elementName) {

                String getterName = "get" + StringUtilities.firstToUpper(qn.getLocalPart());
                String setterName = "set" + StringUtilities.firstToUpper(qn.getLocalPart());
                String currentMethodName = getterName + "()";
                try {
                    Method getter = Util.getMethod(getterName, recordClass);
                    Object param = Util.invokeMethod(result, getter);

                    Method setter = null;
                    if (param != null) {
                        currentMethodName = setterName + "(" + param.getClass() + ")";
                        Class paramClass = param.getClass();
                        if (paramClass.equals(ArrayList.class)) {
                            paramClass = List.class;
                        }
                        setter = Util.getMethod(setterName, recordClass, paramClass);
                    }
                    if (setter != null) {
                        Util.invokeMethod(setter, filtredResult, param);
                    }

                } catch (IllegalArgumentException ex) {
                    logger.severe("illegal argument exception while invoking the method " + currentMethodName + " in the classe RecordType!");
                }
            }
            return filtredResult;
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
        String className;
        String standardName;
        if (value.getType() != null) {
            className    = value.getType().getName();
            standardName = value.getType().getStandard().getName();
        } else {
            logger.severe("Error the type of the value is null");
            return null;
        }
        Class classe = null;
        Object result;
        //debug purpose
        String temp = "";
        
        try {
            // we get the value's class
            classe = getClassFromName(className, standardName);
            if (classe == null) {
                return null;
            }

            // if the value is a leaf => primitive type
            if (value instanceof TextValue) {
                String textValue = ((TextValue) value).getValue();
                // in some special case (Date, double) we have to format the text value.
                if (classe.equals(Double.class)) {
                    textValue = textValue.replace(',', '.');
                }

                // if the value is a codeList element we call the static method valueOf 
                // instead of a constructor
                if ((classe.getSuperclass() != null && classe.getSuperclass().equals(CodeList.class)) || classe.isEnum()) {
                    // the textValue of a codelist is the code and not the value
                    // so we must find the codeList element corrrespounding to this code.
                    org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList) value.getType();
                    try {
                        CodeListElement element = codelist.getElementByCode(Integer.parseInt(textValue));
                    
                        Method method;
                        if ((classe.getSuperclass() != null && classe.getSuperclass().equals(CodeList.class))) {
                            method = Util.getMethod("valueOf", classe, String.class);
                        } else if (classe.isEnum()) {
                            temp = "fromValue";
                            method = Util.getMethod("fromValue", classe, String.class);
                        } else {
                            logger.severe("unknow codelist type");
                            return null;
                        }
                        if (method != null && element != null) {
                            result = Util.invokeMethod(method, null, element.getName());
                        } else {
                            logger.severe("Unable to invoke the method: " + temp + " on the class : " + classe.getName() + "(#" + textValue + ")");
                            return null;
                        }
                        return result;
                    } catch (NumberFormatException e) {
                        logger.severe("Format NumberException : unable to parse the code: " + textValue + " in the codelist: " + codelist.getName());
                        return null;
                    }

                // if the value is a date we call the static method parse 
                // instead of a constructor (temporary patch: createDate method)  
                } else if (classe.equals(Date.class)) {
                    return Util.createDate(textValue, dateFormat);

                // else we use a String constructor    
                } else {
                    //we execute the constructor
                    result = Util.newInstance(classe, textValue);
                    
                    //fix a bug in MDWeb with the value attribute TODO remove
                    if (!form.asMoreChild(value)) {
                        return result;
                    } 
                }

            //if the value is a link
            } else if (value instanceof LinkedValue) {
                LinkedValue lv = (LinkedValue) value;
                Object tempobj = alreadyRead.get(lv.getLinkedValue());
                if (tempobj != null) {
                    return tempobj;
                } else {
                    return getObjectFromValue(lv.getLinkedForm(), lv.getLinkedValue()); 
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
                        CharSequence cs = child.getValue();
                        return Util.newInstance(classe, cs);
                    } else {
                        logger.severe("The localName is mal-formed");
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
                        result = Util.newInstance(classe, namespaceURI, localPart);
                        return result;
                    } else {
                        logger.severe("The QName is mal-formed");
                        return null;
                    }
                }
                /**
                 * normal case
                 * we get the empty constructor
                 */ 
                result = Util.newInstance(classe);
                alreadyRead.put(value, result);
            }

        } catch (ParseException e) {
            logger.severe("The date cannot be parsed ");
            return null;
        }

        //if the result is a subClasses of MetaDataEntity
        Map<String, Object> metaMap = null;
        boolean isMeta  = false;
        boolean wasMeta = false;
        if (result instanceof MetadataEntity) {
            MetadataEntity meta = (MetadataEntity) result;
            metaMap = meta.asMap();
            isMeta  = true;
            wasMeta = true;
        }

        // then we search the setter for all the child value
        for (Value childValue : form.getValues()) {
            
            Path path = childValue.getPath();

            if (childValue.getParent()!= null && childValue.getParent().equals(value)) {
                logger.finer("new childValue:" + path.getName());

                // we get the object from the child Value
                Object param = getObjectFromValue(form, childValue);
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

                        logger.finer("PUT " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                        if (isMeta) {
                              metaMap.put(attribName, param);
                        } else {
                            Method setter = Util.getSetterFromName(attribName, param.getClass(), classe);
                            if (setter != null)
                                Util.invokeMethod(setter, result, param);
                        }
                        tryAgain = false;
                    } catch (IllegalArgumentException e) {
                        logger.finer(e.getMessage());
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
                                
                                logger.severe("unable to put " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                                tryAgain = false;
                        }
                    } catch (ClassCastException ex) {
                        logger.severe("Exception while putting in geotools metadata: " + '\n' +
                                      "cause: " + ex.getMessage());
                        tryAgain = false;
                    }
                }
                if (wasMeta == true)
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
    private Class getPrimitiveTypeFromName(String className) {

        if (className.equalsIgnoreCase("CharacterString")) {
            return String.class;
        } else if (className.equalsIgnoreCase("Date")) {
            return Date.class;
        } else if (className.equalsIgnoreCase("Decimal")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("Real")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("Integer")) {
            return Integer.class;
        } else if (className.equalsIgnoreCase("Boolean")) {
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
    private Class getClassFromName(String className, String standardName) {
        Class result = classBinding.get(standardName + ':' + className);
        if (result == null) {
            logger.finer("search for class " + className);
        } else {
            return result;
        }
        
        String classNameSave = standardName + ':' + className;
        
        //for the primitive type we return java primitive type
        result = getPrimitiveTypeFromName(className);
        if (result != null) {
            classBinding.put(standardName + ':' + className, result);
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
        if (standardName.equals("Catalog Web Service") || standardName.equals("DublinCore") || 
            standardName.equals("OGC Web Service")     || standardName.equals("OGC Filter")) {
            packagesName = CSWPackage;
            
        } else if (standardName.equals("Ebrim v3.0") || standardName.equals("Web Registry Service v1.0")) {
            packagesName = ebrimV3Package;
            
        } else if (standardName.equals("Ebrim v2.5") || standardName.equals("Web Registry Service v0.9")) {
            packagesName = ebrimV25Package;
            
        } else {
            if (!className.contains("Code") && !className.equals("DCPList") && !className.equals("SV_CouplingType")) {
                packagesName = geotoolsPackage;
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
                packageName = "org.geotools.service";
             else if (className.startsWith("FRA_")) 
                packageName = "org.geotoolkit.metadata.fra";
            
            String name = className;
            int nameType = 0;
            while (nameType < 6) {
                try {
                    logger.finer("searching: " + packageName + '.' + name);
                    result = Class.forName(packageName + '.' + name);
                    
                    //if we found the class we store and return it
                    classBinding.put(standardName + ':' + className, result);
                    logger.finer("class found:" + packageName + '.' + name);
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
                            if (name.indexOf("Code") != -1) {
                                name = name.substring(0, name.indexOf("Code"));
                            }
                            if (name.startsWith("Time")) {
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
                        case 3: {
                            name = name.substring(0, name.indexOf("Impl"));
                            name += "Type";
                            nameType = 4;
                            break;
                        }
                        case 4: {
                            name = "FRA" + name;
                            nameType = 5;
                            break;
                        }
                        default:
                            nameType = 6;
                            break;
                    }

                }
            }
        }
        if (!classeNotFound.contains(classNameSave)) {
            logger.severe("class no found: " + classNameSave);
            classeNotFound.add(classNameSave);
        }
        return null;
    }

    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws CstlServiceException {
        List<DomainValues> responseList = new ArrayList<DomainValues>();
        final StringTokenizer tokens = new StringTokenizer(propertyNames, ",");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            List<String> paths = ISO_QUERYABLE.get(token);
            if (paths != null) {
                if (paths.size() != 0) {
                    try {
                        List<String> values = MDReader.getDomainOfValuesFromPaths(paths);
                        ListOfValuesType ListValues = new ListOfValuesType(values);
                        DomainValuesType value = new DomainValuesType(null, token, ListValues, _Metadata_QNAME);
                        responseList.add(value);
                        if (false) throw new SQLException();

                    } catch (SQLException e) {
                        throw new CstlServiceException("The service has launch an SQL exeption:" + e.getMessage(),
                                NO_APPLICABLE_CODE);
                    }
                } else {
                    throw new CstlServiceException("The property " + token + " is not queryable for now",
                            INVALID_PARAMETER_VALUE, "propertyName");
                }
            } else {
                throw new CstlServiceException("The property " + token + " is not queryable",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }
        }
        return responseList;
    }

    @Override
    public List<String> executeEbrimSQLQuery(String SQLQuery) throws CstlServiceException {
        try {
            return MDReader.executeFilterQuery(SQLQuery);
        } catch (SQLException ex) {
           throw new CstlServiceException("The service has throw an SQL exception while making eberim request:" + '\n' +
                                         "Cause: " + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
     
    @Override
    public void destroy() {
        try {
            MDReader.close();
            classBinding.clear();
            alreadyRead.clear();
        } catch (SQLException ex) {
            logger.severe("SQL Exception while destroying MDWeb MetadataReader");
        }
    }

    @Override
    public List<? extends Object> getAllEntries() throws CstlServiceException {
        List<Object> results = new ArrayList<Object>();
        try {
            List<Catalog> catalogs = MDReader.getCatalogs(); 
            List<Form> forms       = MDReader.getAllForm(catalogs);
            for (Form f: forms) {
                results.add(getObjectFromForm("no cache", f));
            }
        } catch (SQLException ex) {
            throw new CstlServiceException("SQL Exception while getting all the entries: " +ex.getMessage(), NO_APPLICABLE_CODE);
        }
        return results;
    }
    
    @Override
    public List<Integer> getSupportedDataTypes() {
        return Arrays.asList(ISO_19115, DUBLINCORE, EBRIM);
    }

    @Override
    public List<QName> getAdditionalQueryableQName() {
        return Arrays.asList(_Degree_QNAME,
                             _AccessConstraints_QNAME,
                             _OtherConstraints_QNAME,
                             _Classification_QNAME,
                             _ConditionApplyingToAccessAndUse_QNAME,
                             _MetadataPointOfContact_QNAME,
                             _Lineage_QNAME,
                             _SpecificationTitle_QNAME,
                             _SpecificationDate_QNAME,
                             _SpecificationDateType_QNAME);
    }

    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return INSPIRE_QUERYABLE;
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
                if (identifier.indexOf(":") != -1) {
                    catalogCode = identifier.substring(identifier.indexOf(":") + 1, identifier.length());
                    identifier = identifier.substring(0, identifier.indexOf(":"));
                    id = Integer.parseInt(identifier);
                } else {
                    throw new NumberFormatException();
                }

                Catalog catalog = MDReader.getCatalog(catalogCode);

                MDReader.removeFormFromCache(catalog, id);

            } catch (SQLException ex) {
                logger.severe("SQLException while removing " + identifier + " from the cache");
                return;
            } catch (NumberFormatException e) {
                logger.severe("NumberFormat while removing " + identifier + " from the cache");
                return;
            }
            super.removeFromCache(identifier);
        }
    }
}
