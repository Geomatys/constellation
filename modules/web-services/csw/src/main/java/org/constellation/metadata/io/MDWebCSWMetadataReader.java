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

package org.constellation.metadata.io;

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
import javax.xml.namespace.QName;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.ReflectionUtilities;
import org.constellation.util.StringUtilities;

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

import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.Value;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.LocalReaderThesaurus;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.thesaurus.Word;
import static org.geotoolkit.csw.xml.TypeNames.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
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
     * A map of label - concept URI loaded from a Thesaurus.
     * They are used to make Anchor mark in the xml export.
     */
    private Map<String, URI> conceptMap;

    
    public MDWebCSWMetadataReader(Automatic configuration) throws MetadataIoException {
        super(configuration);

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
    }

    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException {
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

            if (paths.size() != 0) {
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
     * @param identifier The form identifier with the pattern : "Form_ID:Catalog_Code"
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotoolkit metadata / ebrim registry object)
     *
     * @throws java.sql.MetadataIoException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
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
             throw new MetadataIoException("Unable to parse: " + identifier, NO_APPLICABLE_CODE, "id");
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
                    throw new MetadataIoException("Unable to read the form: " + identifier, NO_APPLICABLE_CODE, "id");
                }

            } else {
                throw new IllegalArgumentException("Unknow standard mode: " + mode);
            }
            return result;

        } catch (MD_IOException e) {
             throw new MetadataIoException("SQL exception while reading the metadata: " + identifier, NO_APPLICABLE_CODE, "id");
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

    @Override
    public List<Integer> getSupportedDataTypes() {
        return Arrays.asList(ISO_19115, DUBLINCORE, EBRIM, SENSORML);
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

    @Override
    public List<String> executeEbrimSQLQuery(String sqlQuery) throws MetadataIoException {
        try {
            return mdReader.executeFilterQuery(sqlQuery);
        } catch (MD_IOException ex) {
           throw new MetadataIoException("The service has throw an SQL exception while making eberim request:" + '\n' +
                                         "Cause: " + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
}
