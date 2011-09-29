/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.metadata.io.mdweb;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MDWebMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.util.ReflectionUtilities;

import static org.constellation.metadata.CSWQueryable.*;

import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.Settable;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.util.StringUtilities;

import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.Value;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.thesaurus.Word;
import org.mdweb.model.thesaurus.Thesaurus;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.LocalThesaurusHandler;
import org.mdweb.io.sql.ThesaurusDatabase;
import static org.geotoolkit.csw.xml.TypeNames.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * A CSW Metadata reader specific for MDweb data source.
 * It allows to read metadata from the dataSource.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebCSWMetadataReader extends MDWebMetadataReader implements CSWMetadataReader {

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
        isoMap.put("subject3",    "ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        isoMap.put("format",      "ISO 19115:MD_Metadata:identificationInfo:resourceFormat:name");
        isoMap.put("abstract",    "ISO 19115:MD_Metadata:identificationInfo:abstract");
        isoMap.put("boundingBox", "ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2");
        isoMap.put("creator",     "ISO 19115:MD_Metadata:identificationInfo:credit");
        isoMap.put("publisher",   "ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        isoMap.put("language",    "ISO 19115:MD_Metadata:language");
        isoMap.put("rights",      "ISO 19115:MD_Metadata:identificationInfo:resourceConstraint:useLimitation");
        DUBLINCORE_PATH_MAP.put(Standard.ISO_19115, isoMap);
        
        final Map<String, String> iso2Map = new HashMap<String, String>();
        iso2Map.put("identifier",  "ISO 19115-2:MI_Metadata:fileIdentifier");
        iso2Map.put("type",        "ISO 19115-2:MI_Metadata:hierarchyLevel");
        iso2Map.put("date",        "ISO 19115-2:MI_Metadata:dateStamp");
        iso2Map.put("subject",     "ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword");
        iso2Map.put("subject2",    "ISO 19115-2:MI_Metadata:identificationInfo:topicCategory");
        iso2Map.put("subject3",    "ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        iso2Map.put("format",      "ISO 19115-2:MI_Metadata:identificationInfo:resourceFormat:name");
        iso2Map.put("abstract",    "ISO 19115-2:MI_Metadata:identificationInfo:abstract");
        iso2Map.put("boundingBox", "ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2");
        iso2Map.put("creator",     "ISO 19115-2:MI_Metadata:identificationInfo:credit");
        iso2Map.put("publisher",   "ISO 19115-2:MI_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        iso2Map.put("language",    "ISO 19115-2:MI_Metadata:language");
        iso2Map.put("rights",      "ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraint:useLimitation");
        DUBLINCORE_PATH_MAP.put(Standard.ISO_19115_2, iso2Map);

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
     * A map of label - concept URI loaded from a Thesaurus.
     * They are used to make Anchor mark in the xml export.
     */
    private final Map<String, URI> conceptMap = new HashMap<String, URI>();

    
    public MDWebCSWMetadataReader(final Automatic configuration) throws MetadataIoException {
        super(configuration);

        final List<BDD> thesaurusDBs = configuration.getThesaurus();
        final List<Thesaurus> thesaurusList = new ArrayList<Thesaurus>();
        for (BDD thesaurusDB : thesaurusDBs) {
            try {
                final DataSource source    =    thesaurusDB.getPooledDataSource();
                final String schema        = thesaurusDB.getSchema();
                final boolean derby        = !thesaurusDB.isPostgres();
                final ThesaurusDatabase th = new ThesaurusDatabase(source, schema, null, "", derby);
                thesaurusList.add(th);

            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "SQLException while initializing the Thesaurus reader: {0}", thesaurusDB.getConnectURL());
            }
        }
        if (thesaurusList.size() > 0) {
            final LocalThesaurusHandler tReader = new LocalThesaurusHandler(thesaurusList);
            final List<Word> words = tReader.getWords(null);
            tReader.close();
            for (final Word word : words) {
                try {
                    final URI uri = new URI(word.getUriConcept());
                    conceptMap.put(word.getLabel(), uri);

                } catch (URISyntaxException ex) {
                    LOGGER.log(Level.WARNING, "URI syntax exception for:{0}", word.getUriConcept());
                }
            }
        }
    }

    @Override
    public List<DomainValues> getFieldDomainofValues(final String propertyNames) throws MetadataIoException {
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
                throw new MetadataIoException("The property " + token + " is not queryable",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }

            if (!paths.isEmpty()) {
                try {
                    final List<String> values         = mdReader.getDomainOfValuesFromPaths(paths, true);
                    final ListOfValuesType listValues = new ListOfValuesType(values);
                    final DomainValuesType value      = new DomainValuesType(null, token, listValues, METADATA_QNAME);
                    responseList.add(value);

                } catch (MD_IOException e) {
                    throw new MetadataIoException(e, NO_APPLICABLE_CODE);
                }
            } else {
                throw new MetadataIoException("The property " + token + " is not queryable for now",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }

        }
        return responseList;
    }

    /**
     * Return a metadata object from the specified identifier.
     * if is not already in cache it read it from the MDWeb database.
     *
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (Dublin core Record / GeotoolKit metadata / EBrim registry object)
     *
     * @throws java.sql.MetadataIoException
     */
    @Override
    public Object getMetadata(final String identifier, final int mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {

        try {
            alreadyRead.clear();

            //we look for cached object
            Object result = getFromCache(identifier);
            if (mode == ISO_19115 || mode == EBRIM || mode == SENSORML) {

                if (result == null) {
                    final Form f = mdReader.getForm(identifier);
                    result = getObjectFromForm(identifier, f, mode);
                } else {
                    LOGGER.log(Level.FINER, "getting from cache: {0}", identifier);
                }

                /*
                 if (mode == ISO_19115 && !(result instanceof DefaultMetadata)) {
                    LOGGER.info("The metadata:" + identifier + " is not a iso type");
                    return null;
                }

                if (mode == SENSORML && !(result instanceof AbstractSensorML)) {
                    LOGGER.info("The metadata:" + identifier + " is not a SML type");
                    return null;
                }*/

                result = applyElementSet(result, type, elementName);

            } else if (mode == DUBLINCORE) {

                final Form form                   = mdReader.getForm(identifier);
                if (form != null) {
                    final Value top               = form.getRoot();
                    final Standard recordStandard = top.getType().getStandard();

                    /*
                     * if the standard of the record is CSW and the record is cached we return it.
                     * if the record is not yet cached we proccess.
                     * if the record have to be transform from the orginal standard to CSW we process.
                     */
                    if (!recordStandard.equals(Standard.CSW) || result == null) {
                        try {
                            result = getRecordFromForm(identifier, form, type, elementName);
                        }  catch (IllegalArgumentException ex) {
                            LOGGER.warning(ex.getMessage());
                            // the metadata is not convertible to DublinCore
                            return null;
                        }
                    }
                    result = applyElementSet(result, type, elementName);
                } else {
                    throw new MetadataIoException("Unable to read the form: " + identifier, NO_APPLICABLE_CODE, "id");
                }

            } else {
                throw new IllegalArgumentException("Unknow standard mode: " + mode);
            }
            return result;

        } catch (MD_IOException e) {
             throw new MetadataIoException("SQL exception while reading the metadata: " + identifier, e, NO_APPLICABLE_CODE, "id");
        }
    }

    private Object applyElementSet(final Object result, ElementSetType type, final List<QName> elementName) {

         if (type == null)
            type = ElementSetType.FULL;

        // then we apply the elementSet/elementName filter

        // for an ElementSetName mode
        if (elementName == null || elementName.isEmpty()) {

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

            for (final QName qn : elementName) {
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
                    
                        if (setter != null) {
                            ReflectionUtilities.invokeMethod(setter, filtredResult, param);
                        } else {
                            final String paramDesc = param.getClass().getName();
                            LOGGER.warning("No setter have been found for attribute " + qn.getLocalPart() +" of type " + paramDesc + " in the class " + recordClass);
                        }
                    }

                } catch (IllegalArgumentException ex) {
                    LOGGER.severe("illegal argument exception while invoking the method " + currentMethodType + StringUtilities.firstToUpper(qn.getLocalPart()) + " in the classe RecordType!");
                }
            }
            return filtredResult;
        }
    }

    /**
     * Return a dublinCore record from a MDWeb record.
     *
     * @param form the MDWeb record.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType getRecordFromForm(final String identifier, final Form form, final ElementSetType type, final List<QName> elementName) throws MD_IOException {
        final Value top                   = form.getRoot();
        final Standard  recordStandard    = top.getType().getStandard();

        if (recordStandard.equals(Standard.ISO_19115)   || 
            recordStandard.equals(Standard.ISO_19115_2) || 
            recordStandard.equals(Standard.ISO_19115_FRA) || 
            recordStandard.equals(Standard.EBRIM_V3)) {
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
     * Return a dublinCore record from a ISO 19115 MDWeb record
     *
     * @Todo (improvement) return Brief, Summary record before getting all the property.
     * 
     * @param form the MDWeb record.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType transformMDFormInRecord(final Form form, final ElementSetType type, final List<QName> elementName) throws MD_IOException {
        
        final Value top                   = form.getRoot();
        final Standard  recordStandard    = top.getType().getStandard();
        final Map<String, String> pathMap = DUBLINCORE_PATH_MAP.get(recordStandard);
        
        if (pathMap == null) {
            LOGGER.warning("No dublin core path_mapping for standard:" + recordStandard.getName());
            return null;
        }
        // we get the title of the form
        final SimpleLiteral title             = new SimpleLiteral(null, form.getTitle());

        // we get the file identifier(s)
        final List<Value>   identifierValues  = form.getValueFromPath(pathMap.get("identifier"));
        final List<String>  identifiers       = new ArrayList<String>();
        for (Value v: identifierValues) {
            if (v instanceof TextValue) {
                identifiers.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral identifier = new SimpleLiteral(null, identifiers);

        //we get The boundingBox(es)
        final List<Value>   bboxValues     = form.getValueFromPath(pathMap.get("boundingBox"));
        final List<BoundingBoxType> bboxes = new ArrayList<BoundingBoxType>();
        for (Value v: bboxValues) {
            bboxes.add(createBoundingBoxFromValue(v.getOrdinal(), form));
        }

        //we get the type of the data
        final List<Value> typeValues  = form.getValueFromPath(pathMap.get("type"));
        String dataType               = null;
        SimpleLiteral litType         = null;
        try {
            if (!typeValues.isEmpty()) {
                final TextValue value = (TextValue)typeValues.get(0);
                final int code        = Integer.parseInt(value.getValue());
                final org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList)value.getType();
                final CodeListElement element = codelist.getElementByCode(code);
                if (element != null) {
                    dataType = element.getName();
                } else {
                    LOGGER.warning("No codeListElement found for code:" + code + " in codelist:" + codelist.getName());
                }
            }
            litType = new SimpleLiteral(null, dataType);
        } catch (NumberFormatException ex) {
            LOGGER.warning("Number format exception while trying to get the DC type");
        }


        // we get the keywords
        final List<Value> keywordsValues  = form.getValueFromPath(pathMap.get("subject3"));
        keywordsValues.addAll(form.getValueFromPath(pathMap.get("subject")));
        final List<SimpleLiteral> keywords = new ArrayList<SimpleLiteral>();
        for (Value v: keywordsValues) {
            if (v instanceof TextValue) {
                keywords.add(new SimpleLiteral(null, ((TextValue)v).getValue()));
            }
        }

        // we get the topic category
        final List<Value> topicCategoriesValues  = form.getValueFromPath(pathMap.get("subject2"));
        for (Value v: topicCategoriesValues) {
            if (v instanceof TextValue) {
                final String value = ((TextValue)v).getValue();
                if (value == null || value.isEmpty()) {
                    continue;
                }
                if (v.getType() instanceof org.mdweb.model.schemas.CodeList) {
                    final org.mdweb.model.schemas.CodeList c = (org.mdweb.model.schemas.CodeList) v.getType();
                    int code = 0;
                    try {
                        code = Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        LOGGER.log(Level.WARNING, "unable to parse the codeListelement:{0}", value);
                    }
                    final CodeListElement element = c.getElementByCode(code);
                    if (element != null) {
                        keywords.add(new SimpleLiteral(null, element.getName()));
                    } else {
                        LOGGER.warning("no such codeListElement:" + code + " for the codeList:" + c.getName());
                    }
                } else {
                    keywords.add(new SimpleLiteral(null, value));
                }
            }
        }
        // and the topicCategeoryy
        final List<Value> formatsValues  = form.getValueFromPath(pathMap.get("format"));
        final List<String> formats = new ArrayList<String>();
        for (Value v: formatsValues) {
            if (v instanceof TextValue) {
                formats.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral format;
        if (!formats.isEmpty()) {
            format = new SimpleLiteral(null, formats);
        } else {
            format = null;
        }

        final List<Value> dateValues  = form.getValueFromPath(pathMap.get("date"));
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
        final List<Value>   descriptionValues = form.getValueFromPath(pathMap.get("abstract"));
        final List<String>  descriptions      = new ArrayList<String>();
        for (Value v: descriptionValues) {
            if (v instanceof TextValue) {
                descriptions.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral description = new SimpleLiteral(null, descriptions);

        // TODO add spatial



        // the creator of the data
        final List<Value>   creatorValues = form.getValueFromPath(pathMap.get("creator"));
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
        final List<Value>   publisherValues = form.getValueFromPath(pathMap.get("publisher"));
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
        final List<Value>   rightValues = form.getValueFromPath(pathMap.get("rights"));
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
        final List<Value>   languageValues = form.getValueFromPath(pathMap.get("language"));
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
        if (elementName == null || elementName.isEmpty()) {
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
                        if (param != null) {
                            LOGGER.warning("No setter have been found for attribute " + qn.getLocalPart() +" of type " + param.getClass() + " in the class RecordType");
                        }
                    }

                } catch (IllegalArgumentException ex) {
                    LOGGER.warning("illegal argument exception while invoking the method get" + StringUtilities.firstToUpper(qn.getLocalPart()) + " in the RecordType class");
                }
            }
            return result;
        }
    }

    /**
     * Create a bounding box from a geographiqueElement Value
     */
    private BoundingBoxType createBoundingBoxFromValue(final int ordinal, final Form f) throws MD_IOException {
        Double  southValue  = null;
        Double eastValue    = null;
        Double  westValue   = null;
        Double northValue  = null;
        String crs  = null;
            try {
            //we get the CRS
            final List<Value> crsValues = f.getValueFromPath("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
            for (Value v: crsValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    crs = ((TextValue)v).getValue();
                }
            }

            //we get the east value
            final List<Value> eastValues = f.getValueFromPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
            for (Value v: eastValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    eastValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }

            //we get the east value
            final List<Value> westValues = f.getValueFromPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
            for (Value v: westValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    westValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }

            //we get the north value
            final List<Value> northValues = f.getValueFromPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
            for (Value v: northValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    northValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }

            //we get the south value
            final List<Value> southValues = f.getValueFromPath("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
            for (Value v: southValues) {
                if (v instanceof TextValue && v.getOrdinal() == ordinal) {
                    southValue = Double.parseDouble(((TextValue)v).getValue());
                }
            }
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "unable to parse a double in bounding box value:\n{0}", ex.getMessage()) ;
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
            LOGGER.warning("boundingBox null");
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getSupportedDataTypes() {
        return Arrays.asList(ISO_19115, DUBLINCORE, EBRIM, SENSORML);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return INSPIRE_QUERYABLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, URI> getConceptMap() {
        return conceptMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> executeEbrimSQLQuery(final String sqlQuery) throws MetadataIoException {
        try {
            return mdReader.executeFilterQuery(sqlQuery);
        } catch (MD_IOException ex) {
           throw new MetadataIoException("The service has throw an SQL exception while making ebrim request:" + '\n' +
                                         "Cause: " + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
}
