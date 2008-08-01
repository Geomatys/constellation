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

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

// Seagis Dependencies
import javax.xml.namespace.QName;
import net.seagis.cat.csw.v202.AbstractRecordType;
import net.seagis.cat.csw.v202.BriefRecordType;
import net.seagis.cat.csw.v202.ElementSetType;
import net.seagis.cat.csw.v202.SummaryRecordType;
import net.seagis.cat.csw.v202.RecordType;
import net.seagis.dublincore.v2.elements.SimpleLiteral;
import net.seagis.ows.v100.BoundingBoxType;
import net.seagis.ows.v100.OWSWebServiceException;
import net.seagis.coverage.web.ServiceVersion;
import static net.seagis.ows.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.sql.Reader;

//geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.mdweb.model.storage.LinkedValue;
import org.opengis.util.CodeList;


/**
 * Read The forms in the metadata database and instanciate them into geotools object.
 * When an object have been read it is stored in cache.
 * 
 * @author Guilhem legal
 */
public class MetadataReader {

    /**
     * A debugging logger
     */
    private Logger logger = Logger.getLogger("net.seagis.coverage.wms");
    
    /**
     * A reader to the MDWeb database.
     */
    private Reader MDReader;
    
    /**
     * A connection to the MDWeb database.
     */
    private Connection connection;
    
    /**
     * A map containing the metadata already extract from the database.
     */
    private Map<String, Object> metadatas;
    
    /**
     * A map containing the mapping beetween the MDWeb className and java typeName
     */
    private Map<String, Class> classBinding;
    
    /**
     * Record the date format in the metadata.
     */
    private DateFormat dateFormat; 
    
    /**
     * A list of MDWeb catalogs
     */
    private List<Catalog> MDCatalogs;
    
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
    private List<String> seagisPackage;
    
    /**
     * A List of the already see object for the current metadata readed
     * (in order to avoid infinite loop)
     */
    private Map<Value, Object> alreadyRead;
    

    public final static int DUBLINCORE = 0;
    public final static int ISO_19115  = 1;
    
    private ServiceVersion version;
    
    private final static Map<Standard, Map<String, String>> DublinCorePathMap;
    static {
        DublinCorePathMap          = new HashMap<Standard, Map<String, String>>();
        
        Map<String, String> isoMap = new HashMap<String, String>();
        isoMap.put("identifier", "ISO 19115:MD_Metadata:fileIdentifier");
        isoMap.put("type",       "ISO 19115:MD_Metadata:identificationInfo:citation:presentationForm");
        isoMap.put("date",       "ISO 19115:MD_Metadata:dateStamp");
        isoMap.put("subject",    "ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        isoMap.put("format",     "ISO 19115:MD_Metadata:identificationInfo:resourceFormat:name");
        isoMap.put("abstract",   "ISO 19115:MD_Metadata:identificationInfo:abstract");
        DublinCorePathMap.put(Standard.ISO_19115, isoMap);
    }
    
    /**
     * Build a new metadata Reader.
     * 
     * @param MDReader a reader to the MDWeb database.
     */
    public MetadataReader(Reader MDReader, Connection c) throws SQLException {
        this.MDReader        = MDReader;
        this.connection      = c;
        this.dateFormat      = new SimpleDateFormat("yyyy-MM-dd");
        
        this.MDCatalogs       = new ArrayList<Catalog>();
        this.MDCatalogs.add(MDReader.getCatalog("FR_SY"));
        this.MDCatalogs.add(MDReader.getCatalog("CSWCat"));
        
        this.geotoolsPackage = searchSubPackage("org.geotools.metadata", "net.seagis.referencing"  , "net.seagis.temporal", 
                                                "org.geotools.service" , "org.geotools.util"       , "org.geotools.feature.catalog");
        this.opengisPackage  = searchSubPackage("org.opengis.metadata" , "org.opengis.referencing" , "org.opengis.temporal",
                                                "org.opengis.service"  , "org.opengis.feature.catalog");
        this.seagisPackage   = searchSubPackage("net.seagis.cat.csw.v202"   , "net.seagis.dublincore.v2.elements", "net.seagis.ows.v100");
        this.metadatas       = new HashMap<String, Object>();
        this.classBinding    = new HashMap<String, Class>();
        this.alreadyRead     = new HashMap<Value, Object>();
    }

    /**
     * Return a metadata object from the specified identifier.
     * if is not already in cache it read it from the MDWeb database.
     * 
     * @param identifier The form identifier
     * 
     * @return An metadata object.
     * @throws java.sql.SQLException
     */
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws SQLException, OWSWebServiceException {
        Object result;
        int id;
        
        //if the identifier is an int we assme that is directly the Form ID
        try  {
            id = Integer.parseInt(identifier);
        
        //else we search for an Identifier or a title
        } catch (NumberFormatException e) {
            id = getIDFromFileIdentifier(identifier);
            if (id == -1) {
                id = getIDFromTitle(identifier);
                if (id == -1)
                    throw new OWSWebServiceException("This identifier " + identifier + " does not exist",
                                                     INVALID_PARAMETER_VALUE, "id", version);
            }
        }
        
        
        alreadyRead.clear();
        if (mode == ISO_19115) {
            result = metadatas.get(identifier);
            if (result == null) {
                //TODO gere plusieur catalogue proprement
                Form f = MDReader.getForm(MDCatalogs.get(0), id);
                if (f == null) {
                    f = MDReader.getForm(MDCatalogs.get(1), id);
                }
                result = getObjectFromForm(f);
                if (result != null) {
                    metadatas.put(identifier, result);
                }
            }
            
        } else {
            Form f = MDReader.getForm(MDCatalogs.get(0), id);
            if (f == null) {
                f = MDReader.getForm(MDCatalogs.get(1), id);
            }
            result = getRecordFromForm(f, type, elementName);
        }
        return result;
    }
    
    /**
     * Return the MDWeb form ID from the fileIdentifier
     */
    public int getIDFromFileIdentifier(String identifier) throws SQLException {
        
       PreparedStatement stmt = connection.prepareStatement("Select form from \"Storage\".\"TextValues\" " +
                                                            "WHERE value=? " +
                                                            "AND (path='ISO 19115:MD_Metadata:fileIdentifier' " +
                                                            "OR  path like '%Catalog Web Service:Record:identifier%')");
       stmt.setString(1, identifier);
       ResultSet queryResult = stmt.executeQuery();
       if (queryResult.next()) {
            return queryResult.getInt(1);
       }
       return -1;
    }
    
    /**
     * Return the MDWeb form ID from the fileIdentifier
     */
    public int getIDFromTitle(String title) throws SQLException {
        
       PreparedStatement stmt = connection.prepareStatement("Select identifier from \"Forms\" WHERE title=? ");
       stmt.setString(1, title);
       ResultSet queryResult = stmt.executeQuery();
       if (queryResult.next()) {
            return queryResult.getInt(1);
       }
       return -1;
    }
    
    /**
     * Return a dublinCore record from a MDWeb formular
     * 
     * @param form the MDWeb formular.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType getRecordFromForm(Form form, ElementSetType type, List<QName> elementName) throws SQLException {
        Value top                   = form.getTopValue();
        Standard  recordStandard    = top.getType().getStandard();
        
        if (recordStandard.equals(Standard.ISO_19115)) {
            return getRecordFromMDForm(form, type, elementName);
        
        } else {
            Object obj =  getObjectFromForm(form);
            RecordType record;
            if (obj instanceof RecordType)
                record =  (RecordType) obj;
            else
                return null;
            
            // for an ElementSetName mode
            if (elementName == null || elementName.size() == 0) {
                // for a element set FULL we return the record directly
                if (type == null || type.equals(ElementSetType.FULL)) {
                    return record;
            
                // Summary view
                } else if (type.equals(ElementSetType.SUMMARY)) {
                    return record.toSummaryRecord();
           
                // Brief view
                } else  if (type.equals(ElementSetType.BRIEF)) {
                    return record.toBriefRecord();
            
                // this case must never happen
                } else {
                    return null;
                }
            
            // for an element name mode    
            } else {
                RecordType result = new RecordType();
                Class recordClass = RecordType.class;
                for (QName qn: elementName) {

                    String getterName        = "get" + firstToUpper(qn.getLocalPart());
                    String setterName        = "set" + firstToUpper(qn.getLocalPart());
                    String currentMethodName = getterName + "()";
                    try {
                        Method getter = recordClass.getMethod(getterName);
                        Object param  = getter.invoke(record);
                        
                        Method setter = null;
                        if (param != null) {
                            currentMethodName = setterName + "(" + param.getClass() + ")";
                            Class paramClass = param.getClass();
                            if (paramClass.equals(ArrayList.class)) {
                                paramClass = List.class;
                            }
                            setter = recordClass.getMethod(setterName, paramClass);
                        }
                        
                        if (setter != null)
                            setter.invoke(result, param);
                        
                    } catch (IllegalAccessException ex) {
                        logger.info("Illegal Access exception while invoking the method " + currentMethodName + " in the classe RecordType");
                    } catch (IllegalArgumentException ex) {
                       logger.info("illegal argument exception while invoking the method " + currentMethodName + " in the classe RecordType");
                    } catch (InvocationTargetException ex) {
                        logger.info("Invocation Target exception while invoking the method " + currentMethodName + " in the classe RecordType");
                    } catch (NoSuchMethodException ex) {
                        logger.info("The method " + currentMethodName + " does not exists in the classe RecordType");
                    } catch (SecurityException ex) {
                        logger.info("Security exception while getting the method " + currentMethodName + " in the classe RecordType");
                    }
                    
                }
                return result;
            }
        }
        
    }
    /**
     * Return a dublinCore record from a ISO 19115 MDWeb formular
     * 
     * @param form the MDWeb formular.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType getRecordFromMDForm(Form form, ElementSetType type, List<QName> elementName) throws SQLException {
        
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
        List<Value>   bboxValues     = form.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2"));
        List<BoundingBoxType> bboxes = new ArrayList<BoundingBoxType>();
        for (Value v: bboxValues) {
            bboxes.add(createBoundingBoxFromValue(v.getOrdinal(), form));
        }
        
        //we get the type of the data
        List<Value> typeValues  = form.getValueFromPath(MDReader.getPath(pathMap.get("type")));
        String dataType         = null;
        if (typeValues.size() != 0) {
            TextValue value = (TextValue)typeValues.get(0);
            int code = Integer.parseInt(value.getValue());
            org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList)value.getType();
            CodeListElement element = codelist.getElementByCode(code);
            dataType = element.getName();        
        }
        SimpleLiteral litType = new SimpleLiteral(null, dataType);
        
        
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
        List<Value>   descriptionValues = form.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:abstract"));
        List<String>  descriptions      = new ArrayList<String>();
        for (Value v: descriptionValues) {
            if (v instanceof TextValue) {
                descriptions.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral description = new SimpleLiteral(null, descriptions);
        
        // TODO add spatial
        
        
        
        // the creator of the data
        List<Value>   creatorValues = form.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:credit"));
        List<String>  creators      = new ArrayList<String>();
        for (Value v: creatorValues) {
            if (v instanceof TextValue) {
                creators.add(((TextValue)v).getValue());
            }
        }
        SimpleLiteral creator = new SimpleLiteral(null, creators);
        
        // the publisher of the data
        List<Value>   publisherValues = form.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName"));
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
        List<Value>   languageValues = form.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:language"));
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
            Class recordClass = RecordType.class;
            for (QName qn : elementName) {

                String getterName = "get" + firstToUpper(qn.getLocalPart());
                String setterName = "set" + firstToUpper(qn.getLocalPart());
                try {
                    Method getter = recordClass.getMethod(getterName);
                    Object param = getter.invoke(fullResult);

                    Method setter = null;
                    if (param != null) {
                        setter = recordClass.getMethod(setterName, param.getClass());
                    }

                    if (setter != null) {
                        setter.invoke(result, param);
                    }

                } catch (IllegalAccessException ex) {
                    logger.info("Illegal Access exception while invoking the method " + getterName + " in the classe RecordType");
                } catch (IllegalArgumentException ex) {
                    logger.info("illegal argument exception while invoking the method " + getterName + " in the classe RecordType");
                } catch (InvocationTargetException ex) {
                    logger.info("Invocation Target exception while invoking the method " + getterName + " in the classe RecordType");
                } catch (NoSuchMethodException ex) {
                    logger.info("The method " + getterName + " does not exists in the classe RecordType");
                } catch (SecurityException ex) {
                    logger.info("Security exception while getting the method " + getterName + " in the classe RecordType");
                }

            }
            return result;
        }
    }
    
    /**
     * Create a bounding box from a geographiqueElement Value
     */
    private BoundingBoxType createBoundingBoxFromValue(int ordinal, Form f) throws SQLException {
        
        //we get the CRS
        List<Value> crsValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code"));
        String crs  = null;
        for (Value v: crsValues) {
            if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                crs = ((TextValue)v).getValue();
                
            }
        }
        
        //we get the east value
        List<Value> eastValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude"));
        Double eastValue  =null;
        for (Value v: eastValues) {
            if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                eastValue = Double.parseDouble(((TextValue)v).getValue());
                
            }
        }
        
        //we get the east value
        List<Value> westValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude"));
        Double  westValue  = null;
        for (Value v: westValues) {
            if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                westValue = Double.parseDouble(((TextValue)v).getValue());
            }
        }
        
        //we get the north value
        List<Value> northValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude"));
        Double northValue  = null;
        for (Value v: northValues) {
            if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                northValue = Double.parseDouble(((TextValue)v).getValue());
            }
        }
        
        //we get the south value
        List<Value> southValues = f.getValueFromPath(MDReader.getPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude"));
        Double  southValue  = null;
        for (Value v: southValues) {
            if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                southValue = Double.parseDouble(((TextValue)v).getValue());
            }
        }
        
        if (crs != null && eastValue != null && westValue != null && northValue != null && southValue != null) {
            if (crs.indexOf("EPSG:") != -1) {
                crs = "EPSG:" + crs;
            }
            BoundingBoxType result = new BoundingBoxType(crs,
                                                         eastValue,
                                                         southValue,
                                                         westValue,
                                                         northValue);
            return result;
        } else {
            return null;
        }
    } 
            
    /**
     * Return an object from a MDWeb formular.
     * 
     * @param form the MDWeb formular.
     * @return a geotools object representing the metadata.
     */
    private Object getObjectFromForm(Form form) {

        if (form != null && form.getTopValue() != null && form.getTopValue().getType() != null) {
            Value topValue = form.getTopValue();
            Object result = getObjectFromValue(form, topValue);
            return result;
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

        String className    = value.getType().getName();
        String standardName = value.getType().getStandard().getName();
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
                if (classe.getSuperclass() != null && classe.getSuperclass().equals(CodeList.class)) {
                    // the textValue of a codelist is the code and not the value
                    // so we must find the codeList element corrrespounding to this code.
                    org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList) value.getType();
                    try {
                        CodeListElement element = codelist.getElementByCode(Integer.parseInt(textValue));
                    
                        Method method = classe.getMethod("valueOf", String.class);
                        result = method.invoke(null, element.getName());
                        return result;
                    } catch (NumberFormatException e) {
                        logger.severe("Format NumberException : unable to parse the code: " + textValue + " in the codelist: " + codelist.getName());
                        return null;
                    }

                // if the value is a date we call the static method parse 
                // instead of a constructor (temporary patch: createDate method)  
                } else if (classe.equals(Date.class)) {
                    /*Method method = DateFormat.class.getMethod("parse", String.class);
                    result = method.invoke(dateFormat, textValue);*/
                    return createDate(textValue);

                // else we use a String constructor    
                } else {
                    temp = "String";
                    // we try to get a constructor(String)
                    Constructor constructor = classe.getConstructor(String.class);
                    logger.finer("constructor:" + '\n' + constructor.toGenericString());
                    
                    //we execute the constructor
                    result = constructor.newInstance(textValue);
                    
                    //fix a bug in MDWeb with the value attribute TODO remove
                    if (!asMoreChild(form, value)) {
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
                    logger.severe("the linked object is not already read: " + lv.getLinkedValue().getIdValue());
                    return null;
                }
                
            // else if the value is a complex object    
            } else {

                /** 
                 * Again another special case LocalName does not have a empty constructor (immutable) 
                 * and no setters so we must call the normal constructor.
                 */
                if (classe.getSimpleName().equals("LocalName")) {
                    Constructor constructor = classe.getConstructor(CharSequence.class);
                    TextValue child = null;
                    //We search the child of the localName
                    for (Value childValue : form.getValues()) {
                        if (childValue.getParent()!= null && childValue.getParent().equals(value) && childValue instanceof TextValue) {
                            child = (TextValue) childValue;
                        }
                    }
                    if (child != null) {
                        CharSequence cs = child.getValue();
                        result = constructor.newInstance(cs);
                        return result;
                    } else {
                        logger.severe("The localName is mal-formed");
                        return null;
                    }
                }
                
                /**
                 * normal case
                 * we get the empty constructor
                 */ 
                Constructor constructor = classe.getConstructor();
                logger.finer("constructor:" + '\n' + constructor.toGenericString());
                //we execute the constructor
                result = constructor.newInstance();
                alreadyRead.put(value, result);
            }

        } catch (NoSuchMethodException e) {
            if (temp.equals("valueOf")) {
                logger.severe("The class " + classe.getName() + " does not have a method valueOf(String code)");
            } else {
                logger.severe("The class " + classe.getName() + " does not have a constructor(" + temp + ")");
            }
            return null;
        } catch (InstantiationException e) {
            logger.severe("The class is abstract or is an interface");
            return null;
        } catch (IllegalAccessException e) {
            logger.severe("The class is not accessible");
            return null;
        } catch (ParseException e) {
            logger.severe("The date cannot be parsed ");
            return null;
        } catch (java.lang.reflect.InvocationTargetException e) {
            logger.severe("Exception throw in the invokated constructor");

            return null;
        }

        //if the result is a subClasses of MetaDataEntity
        Map<String, Object> metaMap = null;
        boolean isMeta = false;
        if (result instanceof MetadataEntity) {
            MetadataEntity meta = (MetadataEntity) result;
            metaMap = meta.asMap();
            isMeta = true;
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
                            Method setter = getSetterFromName(attribName, param.getClass(), classe);
                            if (setter != null)
                                invokeSetter(setter, result, param);
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
                            default:
                                logger.severe("unable to put " + attribName + " type " + param.getClass().getName() + " in class: " + result.getClass().getName());
                                tryAgain = false;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return true if the textValue have child (MDWeb bug)
     */
    private boolean asMoreChild(Form f, Value v) {
        String pathId = v.getPath().getId();
        
        for (Value childValue : f.getValues()) {

            Path path = childValue.getPath();
            Path parent = path.getParent();
            if (parent != null && parent.getId().equals(pathId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Invoke a setter with the specified parameter in the specified object.
     * If the setter is null nothing happen.
     * 
     * @param setter     The method to invoke
     * @param object     The object on witch the method is invoked.
     * @param parameter  The parameter of the method.
     */
    private void invokeSetter(Method setter, Object object, Object parameter) {
        String baseMessage = "unable to invoke setter: "; 
        try {
            if (setter != null)
                setter.invoke(object, parameter); 
            else
                logger.severe(baseMessage + "The setter is null");

        } catch (IllegalAccessException ex) {
            logger.severe(baseMessage + "The class is not accessible");

        } catch (IllegalArgumentException ex) {
            logger.severe(baseMessage + "The argument does not match with the setter");

        } catch (InvocationTargetException ex) {
            logger.severe(baseMessage + "Exception throw in the invokated setter");
        }

    }

    /**
     * Return a class (java primitive type) from a class name.
     * 
     * @param className the standard name of a class. 
     * @return a primitive class.
     */
    private Class getPrimitiveTypeFromName(String className) {

        if (className.equals("CharacterString")) {
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
        } else if (className.equals("LanguageCode")) {
            return Locale.class;
        } else if (className.equals("CountryCode")) {
            return String.class;
        } else if (className.equals("RO_SystRefCode")) {
            return String.class;
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
            logger.finer("searche for class " + className);
        } else {
            return result;
        }
        
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
        } else if (className.equals("FRA_DirectReferenceSystem")) {
            className = "MD_ReferenceSystem";
        }



        List<String> packagesName = new ArrayList<String>();
        if (standardName.equals("Catalog Web Service") ||standardName.equals("DublinCore") || 
            standardName.equals("OGC Web Service")) {
            packagesName = seagisPackage;
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
                packageName = "net.seagis.referencing";
            else if (className.equals("MD_ScopeCode"))
                packageName = "org.opengis.metadata.maintenance";
            else if (className.equals("SV_ServiceIdentification")) 
                packageName = "org.geotools.service";
            
            String name = className;
            int nameType = 0;
            while (nameType < 5) {
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
                        default:
                            nameType = 5;
                            break;
                    }

                }
            }
        }
        logger.severe("class no found: " + className);
        return null;
    }

    /**
     * Return a setter Method for the specified attribute (propertyName) of the type "classe"
     * in the class rootClass.
     * 
     * @param propertyName The attribute name.
     * @param classe       The attribute type.  
     * @param rootClass    The class whitch owe this attribute
     * 
     * @return a setter to this attribute.
     */
    private Method getSetterFromName(String propertyName, Class classe, Class rootClass) {
        logger.finer("search for a setter in " + rootClass.getName() + " of type :" + classe.getName());
        
        //special case
        if (propertyName.equals("beginPosition")) {
            propertyName = "begining";
        } else if (propertyName.equals("endPosition")) {
            propertyName = "ending";
        } 
        
        String methodName = "set" + firstToUpper(propertyName);
        int occurenceType = 0;
        
        //TODO look all interfaces
        Class interfacee = null;
        if (classe.getInterfaces().length != 0) {
            interfacee = classe.getInterfaces()[0];
        }
        
        //TODO make it recursivly
        Class argumentSuperClass     = null;
        Class argumentSuperInterface = null;
        if (classe.getSuperclass() != null) {
            argumentSuperClass     = classe.getSuperclass();
            if (argumentSuperClass.getInterfaces().length > 0) {
                argumentSuperInterface = argumentSuperClass.getInterfaces()[0];
            }
        }
        

        while (occurenceType < 7) {

            try {
                Method setter = null;
                switch (occurenceType) {

                    case 0: {
                        setter = rootClass.getMethod(methodName, classe);
                        break;
                    }
                    case 1: {
                        if (classe.equals(Integer.class)) {
                            setter = rootClass.getMethod(methodName, long.class);
                            break;
                        } else {
                            occurenceType = 2;
                        }
                    }
                    case 2: {
                        setter = rootClass.getMethod(methodName, interfacee);
                        break;
                    }
                    case 3: {
                        setter = rootClass.getMethod(methodName, Collection.class);
                        break;
                    }
                    case 4: {
                        setter = rootClass.getMethod(methodName + "s", Collection.class);
                        break;
                    }
                    case 5: {
                        setter = rootClass.getMethod(methodName , argumentSuperClass);
                        break;
                    }
                    case 6: {
                        setter = rootClass.getMethod(methodName , argumentSuperInterface);
                        break;
                    }
                }
                logger.finer("setter found: " + setter.toGenericString());
                return setter;

            } catch (NoSuchMethodException e) {

                /**
                 * This switch is for debugging purpose
                 */
                switch (occurenceType) {

                    case 0: {
                        logger.finer("The setter " + methodName + "(" + classe.getName() + ") does not exist");
                        occurenceType = 1;
                        break;
                    }

                    case 1: {
                        logger.finer("The setter " + methodName + "(long) does not exist");
                        occurenceType = 2;
                        break;
                    }
                    
                    case 2: {
                        if (interfacee != null) {
                            logger.finer("The setter " + methodName + "(" + interfacee.getName() + ") does not exist");
                        }
                        occurenceType = 3;
                        break;
                    }

                    case 3: {
                        logger.finer("The setter " + methodName + "(Collection<" + classe.getName() + ">) does not exist");
                        occurenceType = 4;
                        break;
                    }
                    case 4: {
                        logger.finer("The setter " + methodName + "s(Collection<" + classe.getName() + ">) does not exist");
                        occurenceType = 5;
                        break;
                    }
                    case 5: {
                        if (argumentSuperClass != null) {
                            logger.finer("The setter " + methodName + "(" + argumentSuperClass.getName() + ") does not exist");
                        }
                        occurenceType = 6;
                        break;
                    }
                    case 6: {
                        if (argumentSuperInterface != null) {
                            logger.finer("The setter " + methodName + "(" + argumentSuperInterface.getName() + ") does not exist");
                        }
                        occurenceType = 7;
                        break;
                    }
                    default:
                        occurenceType = 7;
                }
            }
        }
        logger.severe("No setter have been found for attribute " + propertyName + 
                      " of type " + classe.getName() + " in the class " + rootClass.getName());
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
     * Search in the librairies and the classes the child of the specified packages,
     * and return all of them.
     * 
     * @param packages the packages to scan.
     * 
     * @return a list of package names.
     */
    private List<String> searchSubPackage(String... packages) {
        List<String> result = new ArrayList<String>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        for (String p : packages) {
            try {
                String fileP = p.replace('.', '/');
                Enumeration<URL> urls = classloader.getResources(fileP);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try {
                        URI uri = url.toURI();
                        logger.finer("scanning :" + uri);
                        result.addAll(scan(uri, fileP));
                    } catch (URISyntaxException e) {
                        logger.severe("URL, " + url + "cannot be converted to a URI");
                    }
                }
            } catch (IOException ex) {
                logger.severe("The resources for the package" + p +
                              ", could not be obtained");
            }
        }

        return result;
    }

    /**
     * Scan a resource file (a JAR or a directory) to find the sub-package names of
     * the specified "filePackageName"
     * 
     * @param u The URI of the file.
     * @param filePackageName The package to scan.
     * 
     * @return a list of package names.
     * @throws java.io.IOException
     */
    private List<String> scan(URI u, String filePackageName) throws IOException {
        List<String> result = new ArrayList<String>();
        String scheme = u.getScheme();
        if (scheme.equals("file")) {
            File f = new File(u.getPath());
            if (f.isDirectory()) {
                result.addAll(scanDirectory(f, filePackageName));
            }
        } else if (scheme.equals("jar") || scheme.equals("zip")) {
            URI jarUri = URI.create(u.getSchemeSpecificPart());
            String jarFile = jarUri.getPath();
            jarFile = jarFile.substring(0, jarFile.indexOf('!'));
            result.addAll(scanJar(new File(jarFile), filePackageName));
        }
        return result; 
    }

    /**
     * Scan a directory to find the sub-package names of
     * the specified "parent" package
     * 
     * @param root The root file (directory) of the package to scan.
     * @param parent the package name.
     * 
     * @return a list of package names.
     */
    private List<String> scanDirectory(File root, String parent) {
        List<String> result = new ArrayList<String>();
        for (File child : root.listFiles()) {
            if (child.isDirectory()) {
                result.add(parent.replace('/', '.') + '.' + child.getName());
                result.addAll(scanDirectory(child, parent));
            }
        }
        return result;
    }

    /**
     * Scan a jar to find the sub-package names of
     * the specified "parent" package
     * 
     * @param file the jar file containing the package to scan
     * @param parent the package name.
     * 
     * @return a list of package names.
     * @throws java.io.IOException
     */
    private static List<String> scanJar(File file, String parent) throws IOException {
        List<String> result = new ArrayList<String>();
        final JarFile jar = new JarFile(file);
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            if (e.isDirectory() && e.getName().startsWith(parent)) {
                String s = e.getName().replace('/', '.');
                s = s.substring(0, s.length() - 1);
                result.add(s);
            }
        }
        return result;
    }
    
    /**
     * Return a Date by parsing different kind of date format.
     * 
     * @param date a date representation (example 2002, 02-2007, 2004-03-04, ...)
     * 
     * @return a formated date (example 2002 -> 01-01-2002,  2004-03-04 -> 04-03-2004, ...) 
     */
    private Date createDate(String date) throws ParseException{
        
        Map<String, String> POOL = new HashMap<String, String>();
        POOL.put("janvier",   "01");
        POOL.put("février",   "02");
        POOL.put("mars",      "03");
        POOL.put("avril",     "04");
        POOL.put("mai",       "05");
        POOL.put("juin",      "06");
        POOL.put("juillet",   "07");
        POOL.put("août",      "08");
        POOL.put("septembre", "09");
        POOL.put("octobre",   "10");
        POOL.put("novembre",  "11");
        POOL.put("décembre",  "12");
        
        Map<String, String> POOLcase = new HashMap<String, String>();
        POOLcase.put("Janvier",   "01");
        POOLcase.put("Février",   "02");
        POOLcase.put("Mars",      "03");
        POOLcase.put("Avril",     "04");
        POOLcase.put("Mai",       "05");
        POOLcase.put("Juin",      "06");
        POOLcase.put("Juillet",   "07");
        POOLcase.put("Août",      "08");
        POOLcase.put("Septembre", "09");
        POOLcase.put("Octobre",   "10");
        POOLcase.put("Novembre",  "11");
        POOLcase.put("Décembre",  "12");
        
        String year;
        String month;
        String day;
        Date tmp = dateFormat.parse("1900" + "-" + "01" + "-" + "01");
        if (date != null){
            if(date.contains("/")){
                
                day   = date.substring(0, date.indexOf("/"));
                date  = date.substring(date.indexOf("/")+1);
                month = date.substring(0, date.indexOf("/"));
                year  = date.substring(date.indexOf("/")+1);
                                
                tmp   = dateFormat.parse(year + "-" + month + "-" + day);
            } else if ( getOccurence(date, " ") == 2 ) {
                if (! date.contains("?")){
                               
                    day    = date.substring(0, date.indexOf(" "));
                    date   = date.substring(date.indexOf(" ")+1);
                    month  = POOL.get(date.substring(0, date.indexOf(" ")));
                    year   = date.substring(date.indexOf(" ")+1);

                    tmp    = dateFormat.parse(year + "-" + month + "-" + day);
                } else tmp = dateFormat.parse("01" + "-" + "01" + "-" + "2000");
                
            } else if ( getOccurence(date, " ") == 1 ) {
                
                month = POOLcase.get(date.substring(0, date.indexOf(" ")));
                year  = date.substring(date.indexOf(" ") + 1);   
                tmp   = dateFormat.parse(year + "-" + month + "-01");
                
            } else if ( getOccurence(date, "-") == 1 ) {
                
                month = date.substring(0, date.indexOf("-"));
                year  = date.substring(date.indexOf("-")+1);
                                
                tmp   = dateFormat.parse(year + "-" + month + "-01");
                
            } else if ( getOccurence(date, "-") == 2 ) {
                
                //if date is in format yyyy-mm-dd
                if (date.substring(0, date.indexOf("-")).length()==4){
                    year  = date.substring(0, date.indexOf("-"));
                    date  = date.substring(date.indexOf("-")+1);
                    month = date.substring(0, date.indexOf("-"));
                    day   = date.substring(date.indexOf("-")+1);
                    
                    tmp   = dateFormat.parse(year + "-" + month + "-" + day);
                }
                else{
                    day   = date.substring(0, date.indexOf("-"));
                    date  = date.substring(date.indexOf("-")+1);
                    month = date.substring(0, date.indexOf("-"));
                    year  = date.substring(date.indexOf("-")+1);
                    
                    tmp =  dateFormat.parse(year + "-" + month + "-" + day);
                }
                
            } else {
                year = date;
                tmp  =  dateFormat.parse(year + "-01-01");
            }
        }
        return tmp;
    }
    
    /**
     * This method returns a number of occurences occ in the string s.
     */
    private int getOccurence (String s, String occ){
        if (! s.contains(occ))
            return 0;
        else {
            int nbocc = 0;
            while(s.indexOf(occ) != -1){
                s = s.substring(s.indexOf(occ)+1);
                nbocc++;
            }
            return nbocc;
        }
    }
    
    /**
     * Set the current service version
     */
    public void setVersion(ServiceVersion version){
        this.version = version;
    }
}
