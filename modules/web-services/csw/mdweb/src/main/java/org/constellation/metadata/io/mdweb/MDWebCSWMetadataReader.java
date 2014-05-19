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

package org.constellation.metadata.io.mdweb;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MDWebMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.util.ReflectionUtilities;

import static org.constellation.metadata.CSWQueryable.*;
import org.constellation.metadata.io.ElementSetType;
import org.constellation.metadata.io.MetadataType;
import org.constellation.util.XpathUtils;

import org.geotoolkit.csw.xml.DomainValues;
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
import org.mdweb.model.storage.FullRecord;
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
import org.w3c.dom.Node;

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
    protected static final Map<Standard, Map<String, String>> DUBLINCORE_PATH_MAP = new HashMap<>();
    static {
        final Map<String, String> isoMap = new HashMap<>();
        isoMap.put("identifier",  "ISO 19115:MD_Metadata:fileIdentifier");
        isoMap.put("type",        "ISO 19115:MD_Metadata:hierarchyLevel");
        isoMap.put("date",        "ISO 19115:MD_Metadata:dateStamp");
        isoMap.put("subject",     "ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        isoMap.put("subject2",    "ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        isoMap.put("subject3",    "ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        isoMap.put("format",      "ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        isoMap.put("format2",     "ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name:value");
        isoMap.put("abstract",    "ISO 19115:MD_Metadata:identificationInfo:abstract");
        isoMap.put("boundingBox", "ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2");
        isoMap.put("creator",     "ISO 19115:MD_Metadata:identificationInfo:credit");
        isoMap.put("publisher",   "ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        isoMap.put("language",    "ISO 19115:MD_Metadata:language");
        isoMap.put("rights",      "ISO 19115:MD_Metadata:identificationInfo:resourceConstraint:useLimitation");
        isoMap.put("description", "ISO 19115:MD_Metadata:identificationInfo:graphicOverview:fileName");
        DUBLINCORE_PATH_MAP.put(Standard.ISO_19115, isoMap);

        final Map<String, String> iso2Map = new HashMap<>();
        iso2Map.put("identifier",  "ISO 19115-2:MI_Metadata:fileIdentifier");
        iso2Map.put("type",        "ISO 19115-2:MI_Metadata:hierarchyLevel");
        iso2Map.put("date",        "ISO 19115-2:MI_Metadata:dateStamp");
        iso2Map.put("subject",     "ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword");
        iso2Map.put("subject2",    "ISO 19115-2:MI_Metadata:identificationInfo:topicCategory");
        iso2Map.put("subject3",    "ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        iso2Map.put("format",      "ISO 19115-2:MI_Metadata:distributionInfo:distributionFormat:name");
        iso2Map.put("format2",     "ISO 19115-2:MI_Metadata:distributionInfo:distributionFormat:name:value");
        iso2Map.put("abstract",    "ISO 19115-2:MI_Metadata:identificationInfo:abstract");
        iso2Map.put("boundingBox", "ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2");
        iso2Map.put("creator",     "ISO 19115-2:MI_Metadata:identificationInfo:credit");
        iso2Map.put("publisher",   "ISO 19115-2:MI_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        iso2Map.put("language",    "ISO 19115-2:MI_Metadata:language");
        iso2Map.put("rights",      "ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraint:useLimitation");
        iso2Map.put("description", "ISO 19115-2:MI_Metadata:identificationInfo:graphicOverview:fileName");
        DUBLINCORE_PATH_MAP.put(Standard.ISO_19115_2, iso2Map);

        final Map<String, String> ebrimMap = new HashMap<>();
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
    private final Map<String, URI> conceptMap = new HashMap<>();


    public MDWebCSWMetadataReader(final Automatic configuration) throws MetadataIoException {
        super(configuration);

        final List<BDD> thesaurusDBs = configuration.getThesaurus();
        final List<Thesaurus> thesaurusList = new ArrayList<>();
        for (BDD thesaurusDB : thesaurusDBs) {
            final DataSource source    = thesaurusDB.getPooledDataSource();
            final String schema        = thesaurusDB.getSchema();
            final boolean derby        = !thesaurusDB.isPostgres();
            final ThesaurusDatabase th = new ThesaurusDatabase(source, schema, derby);
            thesaurusList.add(th);
        }
        if (thesaurusList.size() > 0) {
            final LocalThesaurusHandler tReader = new LocalThesaurusHandler(thesaurusList);
            final List<Word> words = tReader.getWords(null, true);
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
        final List<DomainValues> responseList = new ArrayList<>();
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
                paths = XpathUtils.xpathToMDPath(paths);
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
     * @throws MetadataIoException
     */
    @Override
    public Node getMetadata(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {

        try {
            alreadyRead.clear();

            //we look for cached object
            Object result = getFromCache(identifier);
            if (mode == MetadataType.ISO_19115 || mode == MetadataType.EBRIM || mode == MetadataType.SENSORML || mode == MetadataType.ISO_19110) {

                if (result == null) {
                    final FullRecord f = mdReader.getRecord(identifier);
                    result = getObjectFromRecord(identifier, f, mode);
                } else {
                    LOGGER.log(Level.FINER, "getting from cache: {0}", identifier);
                }

                result = applyElementSet(result, type, elementName);

            } else if (mode == MetadataType.DUBLINCORE) {

                final FullRecord record             = mdReader.getRecord(identifier);
                if (record != null) {
                    final Value top               = record.getRoot();
                    final Standard recordStandard = top.getType().getStandard();

                    /*
                     * if the standard of the record is CSW and the record is cached we return it.
                     * if the record is not yet cached we proccess.
                     * if the record have to be transform from the orginal standard to CSW we process.
                     */
                    if (!recordStandard.equals(Standard.CSW) || result == null) {
                        try {
                            result = getRecordFromMDRecord(identifier, record, type, elementName);
                        }  catch (IllegalArgumentException ex) {
                            LOGGER.warning(ex.getMessage());
                            // the metadata is not convertible to DublinCore
                            return null;
                        }
                    }
                    result = applyElementSet(result, type, elementName);
                } else {
                    throw new MetadataIoException("Unable to read the record: " + identifier, NO_APPLICABLE_CODE, "id");
                }

            } else {
                throw new IllegalArgumentException("Unknow standard mode: " + mode);
            }
            // marshall to DOM
            if (result != null) {
                return writeObjectInNode(result, mode);
            }
            return null;

        } catch (MD_IOException e) {
             throw new MetadataIoException("SQL exception while reading the metadata: " + identifier, e, NO_APPLICABLE_CODE, "id");
        }
    }

    private Object applyElementSet(final Object result, ElementSetType type, final List<QName> elementName) {

         if (type == null) {
            type = ElementSetType.FULL;
         }

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

                    if (param != null) {
                        currentMethodType   = "set";
                        Class paramClass = param.getClass();
                        if (paramClass.equals(ArrayList.class)) {
                            paramClass = List.class;
                        }
                        final Method setter = ReflectionUtilities.getSetterFromName(qn.getLocalPart(), paramClass, recordClass);

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
     * @param record the MDWeb record.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType getRecordFromMDRecord(final String identifier, final FullRecord record, final ElementSetType type, final List<QName> elementName) throws MD_IOException {
        final Value top                   = record.getRoot();
        final Standard  recordStandard    = top.getType().getStandard();

        if (recordStandard.equals(Standard.ISO_19115)   ||
            recordStandard.equals(Standard.ISO_19115_2) ||
            recordStandard.equals(Standard.ISO_19115_FRA) ||
            recordStandard.equals(Standard.EBRIM_V3)) {
            return transformMDRecordInRecord(record, type, elementName);

        } else {
            final Object obj =  getObjectFromRecord(identifier, record, MetadataType.DUBLINCORE);

            if (obj instanceof AbstractRecordType) {
                return (AbstractRecordType) obj;

            } else {
                String objType = "null";
                if (obj != null) {
                    objType = obj.getClass().getName();
                }

                throw new IllegalArgumentException("Unexpected type in getRecordFromMDRecord. waiting for AbstractRecordType, got: " + objType);
            }
        }
    }

    /**
     * Return a dublinCore record from a ISO 19115 MDWeb record
     *
     * @Todo (improvement) return Brief, Summary record before getting all the property.
     *
     * @param record the MDWeb record.
     * @return a CSW object representing the metadata.
     */
    private AbstractRecordType transformMDRecordInRecord(final FullRecord record, final ElementSetType type, final List<QName> elementName) throws MD_IOException {

        final Value top                   = record.getRoot();
        final Standard  recordStandard    = top.getType().getStandard();
        final Map<String, String> pathMap = DUBLINCORE_PATH_MAP.get(recordStandard);

        if (pathMap == null) {
            LOGGER.log(Level.WARNING, "No dublin core path_mapping for standard:{0}", recordStandard.getName());
            return null;
        }
        // we get the title of the record
        final SimpleLiteral title             = new SimpleLiteral(null, record.getTitle());

        // we get the file identifier(s)
        final List<Value>   identifierValues  = record.getValueFromPath(pathMap.get("identifier"));
        final List<String>  identifiers       = new ArrayList<>();
        for (Value v: identifierValues) {
            if (v instanceof TextValue) {
                identifiers.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral identifier = new SimpleLiteral(null, identifiers);

        //we get The boundingBox(es)
        final List<Value>   bboxValues     = record.getValueFromPath(pathMap.get("boundingBox"));
        final List<BoundingBoxType> bboxes = new ArrayList<>();
        for (Value v: bboxValues) {
            bboxes.add(createBoundingBoxFromValue(v.getIdValue(), record, recordStandard));
        }

        //we get the type of the data
        final List<Value> typeValues  = record.getValueFromPath(pathMap.get("type"));
        String dataType               = null;
        SimpleLiteral litType         = null;
        try {
            if (!typeValues.isEmpty()) {
                final TextValue value = (TextValue)typeValues.get(0);
                final String stringValue = value.getValue();
                if (stringValue != null && !stringValue.isEmpty()) {
                    final int code        = Integer.parseInt(stringValue);
                    final org.mdweb.model.schemas.CodeList codelist = (org.mdweb.model.schemas.CodeList)value.getType();
                    final CodeListElement element = codelist.getElementByCode(code);
                    if (element != null) {
                        dataType = element.getName();
                    } else {
                        LOGGER.warning("No codeListElement found for code:" + code + " in codelist:" + codelist.getName());
                    }
                }
            }
            litType = new SimpleLiteral(null, dataType);
        } catch (NumberFormatException ex) {
            LOGGER.finer("Number format exception while trying to get the DC type using the value as it is in the database");
            final TextValue value = (TextValue)typeValues.get(0);
            final String stringValue = value.getValue();
            if (stringValue != null && !stringValue.isEmpty()) {
                litType = new SimpleLiteral(null, stringValue);
            }
        }


        // we get the keywords
        final List<Value> keywordsValues  = record.getValueFromPath(pathMap.get("subject3"));
        keywordsValues.addAll(record.getValueFromPath(pathMap.get("subject")));
        final List<SimpleLiteral> keywords = new ArrayList<>();
        for (Value v: keywordsValues) {
            if (v instanceof TextValue) {
                keywords.add(new SimpleLiteral(null, ((TextValue)v).getValue()));
            }
        }

        // we get the topic category
        final List<Value> topicCategoriesValues  = record.getValueFromPath(pathMap.get("subject2"));
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
        
        final List<Value> formatsValues  = record.getValueFromPath(pathMap.get("format"));
        formatsValues.addAll(record.getValueFromPath(pathMap.get("format2")));
        final List<String> formats = new ArrayList<>();
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

        final List<Value> dateValues  = record.getValueFromPath(pathMap.get("date"));
        final List<String> dates = new ArrayList<>();
        for (Value v: dateValues) {
            if (v instanceof TextValue) {
                dates.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral date = new SimpleLiteral(null, dates);

        // the last update date
        final SimpleLiteral modified = new SimpleLiteral(null, dates);

        // the abstracts
        final List<Value>   abstractValues = record.getValueFromPath(pathMap.get("abstract"));
        final List<String>  abstracts      = new ArrayList<>();
        for (Value v: abstractValues) {
            if (v instanceof TextValue) {
                abstracts.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral _abstract = new SimpleLiteral(null, abstracts);

        // the description
        final List<Value>   descriptionValues   = record.getValueFromPath(pathMap.get("description"));
        final List<SimpleLiteral>  descriptions = new ArrayList<>();
        for (Value v: descriptionValues) {
            if (v instanceof TextValue) {
                descriptions.add(new SimpleLiteral(((TextValue)v).getValue()));
            }
        }

        // TODO add spatial



        // the creator of the data
        final List<Value>   creatorValues = record.getValueFromPath(pathMap.get("creator"));
        final List<String>  creators      = new ArrayList<>();
        for (Value v: creatorValues) {
            if (v instanceof TextValue) {
                creators.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral creator;
        if (!creators.isEmpty()) {
            creator = new SimpleLiteral(null, creators);
        } else {
            creator = null;
        }

        // the publisher of the data
        final List<Value>   publisherValues = record.getValueFromPath(pathMap.get("publisher"));
        final List<String>  publishers      = new ArrayList<>();
        for (Value v: publisherValues) {
            if (v instanceof TextValue) {
                publishers.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral publisher = new SimpleLiteral(null, publishers);

        // TODO the contributors
        // TODO the source of the data

        // The rights
        final List<Value>   rightValues = record.getValueFromPath(pathMap.get("rights"));
        final List<String>  rights      = new ArrayList<>();
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
        final List<Value>   languageValues = record.getValueFromPath(pathMap.get("language"));
        final List<String>  languages      = new ArrayList<>();
        for (Value v: languageValues) {
            if (v instanceof TextValue) {
                languages.add(((TextValue)v).getValue());
            }
        }
        final SimpleLiteral language = new SimpleLiteral(null, languages);

        final RecordType fullResult = new RecordType(identifier, title, litType , keywords, format, modified, date, _abstract, bboxes,
                        creator, publisher, language, null, null);
        if (right != null) {
            fullResult.setRights(right);
        }
        if (!descriptions.isEmpty()) {
            fullResult.setDescription(descriptions);
        }


        // for an ElementSetName mode
        if (elementName == null || elementName.isEmpty()) {
            if (type.equals(ElementSetType.BRIEF)) {
                return new BriefRecordType(identifier, title, litType , bboxes);
            }

            if (type.equals(ElementSetType.SUMMARY)) {
                return new SummaryRecordType(identifier, title, litType , bboxes, keywords, format, modified, _abstract);

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
    private BoundingBoxType createBoundingBoxFromValue(final String idValue, final FullRecord f, final Standard mainStandard) throws MD_IOException {
        Double  southValue  = null;
        Double eastValue    = null;
        Double  westValue   = null;
        Double northValue  = null;
        String crs  = null;
        final String typePrefix;
        if (Standard.ISO_19115.equals(mainStandard)) {
            typePrefix = "ISO 19115:MD_Metadata:";
        } else if (Standard.ISO_19115_2.equals(mainStandard)) {
            typePrefix = "ISO 19115-2:MI_Metadata:";
        } else {
            throw new MD_IOException("unexpected main standard:" + mainStandard);
        }
        String currentParsed = null;
        try {

            //we get the east value
            final List<Value> eastValues = f.getValueFromPath(typePrefix + "identificationInfo:extent:geographicElement2:eastBoundLongitude");
            for (Value v: eastValues) {
                final Value parentValue = v.getParent();
                if (v instanceof TextValue && parentValue.getIdValue().equals(idValue)) {
                    currentParsed = ((TextValue)v).getValue();
                    eastValue = Double.parseDouble(currentParsed);
                }
            }

            //we get the east value
            final List<Value> westValues = f.getValueFromPath(typePrefix + "identificationInfo:extent:geographicElement2:westBoundLongitude");
            for (Value v: westValues) {
                final Value parentValue = v.getParent();
                if (v instanceof TextValue && parentValue.getIdValue().equals(idValue)) {
                    currentParsed = ((TextValue)v).getValue();
                    westValue = Double.parseDouble(currentParsed);
                }
            }

            //we get the north value
            final List<Value> northValues = f.getValueFromPath(typePrefix + "identificationInfo:extent:geographicElement2:northBoundLatitude");
            for (Value v: northValues) {
                final Value parentValue = v.getParent();
                if (v instanceof TextValue && parentValue.getIdValue().equals(idValue)) {
                    currentParsed = ((TextValue)v).getValue();
                    northValue = Double.parseDouble(currentParsed);
                }
            }

            //we get the south value
            final List<Value> southValues = f.getValueFromPath(typePrefix + "identificationInfo:extent:geographicElement2:southBoundLatitude");
            for (Value v: southValues) {
                final Value parentValue = v.getParent();
                if (v instanceof TextValue && parentValue.getIdValue().equals(idValue)) {
                    currentParsed = ((TextValue)v).getValue();
                    southValue = Double.parseDouble(currentParsed);
                }
            }
        } catch (NumberFormatException ex) {
            if (currentParsed != null && !currentParsed.isEmpty()) {
                LOGGER.log(Level.WARNING, "unable to parse a double in bounding box value:\n{0}", ex.getMessage()) ;
            }
        }

        if (eastValue != null && westValue != null && northValue != null && southValue != null) {
            final BoundingBoxType result = new BoundingBoxType("EPSG:4326",
                                                               eastValue,
                                                               southValue,
                                                               westValue,
                                                               northValue);
            LOGGER.finer("boundingBox created");
            return result;
        } else {
            LOGGER.finer("boundingBox null");
            return null;
        }
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
    public String[] executeEbrimSQLQuery(final String sqlQuery) throws MetadataIoException {
        try {
            final Set<String> results = mdReader.executeFilterQuery(sqlQuery);
            return results.toArray(new String[results.size()]);
        } catch (MD_IOException ex) {
           throw new MetadataIoException("The service has throw an SQL exception while making ebrim request:" + '\n' +
                                         "Cause: " + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
}
