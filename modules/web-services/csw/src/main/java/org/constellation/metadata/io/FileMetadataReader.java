/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JAXB dependencies
import java.util.StringTokenizer;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.CSWClassesContext;
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.util.StringUtilities;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.TypeNames.*;

// geoAPI dependencies
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.InternationalString;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.distribution.Distributor;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSet;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ElementSetType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory.*;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory.*;

/**
 * A csw Metadata Reader. This reader does not require a database.
 * The csw records are stored XML file in a directory .
 *
 * This reader can be used for test purpose or in case of small amount of record.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataReader extends MetadataReader {

    /**
     * The directory containing the data XML files.
     */
    private final File dataDirectory;
    
    /**
     * A unmarshaller to get java object from metadata files.
     */
    private final MarshallerPool marshallerPool;

    /**
     * A date formatter used to display the Date object for dublin core translation.
     */
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * Build a new CSW File Reader.
     *
     * @param configuration A generic configuration object containing a directory path
     * in the configuration.dataDirectory field.
     *
     * @throws org.constellation.ws.CstlServiceException If the configuration object does
     * not contains an existing directory path in the configuration.dataDirectory field.
     * If the creation of a MarshallerPool throw a JAXBException.
     */
    public FileMetadataReader(Automatic configuration) throws CstlServiceException {
        super(true, false);
        File dataDir = configuration.getDataDirectory();
        if (dataDir == null || !dataDir.exists()) {
            final File configDir = configuration.getConfigurationDirectory();
            dataDir = new File(configDir, dataDir.getName());
        }
        dataDirectory = dataDir;
        if (dataDirectory == null || !dataDirectory.exists() || !dataDirectory.isDirectory()) {
            throw new CstlServiceException("cause: unable to find the data directory", NO_APPLICABLE_CODE);
        }
        try {
            marshallerPool = new MarshallerPool(CSWClassesContext.getAllClasses());
        } catch (JAXBException ex) {
            throw new CstlServiceException("cause: JAXB exception while creating unmarshaller", ex, NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * Return a metadata object from the specified identifier.
     * 
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * 
     * @return A marshallable metadata object.
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSet type, List<QName> elementName) throws CstlServiceException {
        Object obj = getObjectFromFile(identifier);
        if (obj instanceof DefaultMetaData && mode == DUBLINCORE) {
            obj = translateISOtoDC((DefaultMetaData)obj, type, elementName);
        } else if (obj instanceof RecordType && mode == DUBLINCORE) {
            obj = applyElementSet((RecordType)obj, type, elementName);
        }
        return obj;
    }

    /**
     * Unmarshall The file designed by the path dataDirectory/identifier.xml
     * If the file is not present or if it is impossible to unmarshall it it return an exception.
     *
     * @param identifier
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    private Object getObjectFromFile(String identifier) throws CstlServiceException {
        final File metadataFile = new File (dataDirectory,  identifier + ".xml");
        if (metadataFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object metadata = unmarshaller.unmarshal(metadataFile);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                addInCache(identifier, metadata);
                return metadata;
            } catch (JAXBException ex) {
                throw new CstlServiceException("The metadataFile : " + identifier + ".xml can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        } else {
            throw new CstlServiceException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Apply the elementSet (Brief, Summary or full) or the custom elementSetName on the specified record.
     * 
     * @param record
     * @param type
     * @param elementName
     * @return
     * @throws CstlServiceException If the type and the element name are null.
     */
    private AbstractRecordType applyElementSet(RecordType record, ElementSet type, List<QName> elementName) throws CstlServiceException {
        
        if (type != null) {
            if (type.equals(ElementSetType.SUMMARY)) {
                return record.toSummary();
            } else if (type.equals(ElementSetType.BRIEF)) {
                return record.toBrief();
            } else {
                return record;
            }
        } else if (elementName != null) {
            final RecordType customRecord = new RecordType();
            for (QName qn : elementName) {
                if (qn != null) {
                    try {
                        final Method getter = Util.getGetterFromName(qn.getLocalPart(), RecordType.class);
                        final Object param  = Util.invokeMethod(record, getter);

                        Method setter = null;
                        if (param != null) {
                            setter = Util.getSetterFromName(qn.getLocalPart(), param.getClass(), RecordType.class);
                        }

                        if (setter != null) {
                            Util.invokeMethod(setter, customRecord, param);
                        }

                    } catch (IllegalArgumentException ex) {
                        LOGGER.info("illegal argument exception while invoking the method for attribute" + qn.getLocalPart() + " in the classe RecordType");
                    }
                } else {
                    LOGGER.severe("An elementName was null.");
                }
            }
            return customRecord;
        } else {
            throw new CstlServiceException("No ElementSet or Element name specified");
        }
    }
    /**
     * Translate A ISO 19139 object into a DublinCore representation.
     * The elementSet (Brief, Summary or full) or the custom elementSetName is applied.
     *
     * @param metadata
     * @param type
     * @param elementName
     * @return
     */
    private AbstractRecordType translateISOtoDC(DefaultMetaData metadata, ElementSet type, List<QName> elementName) {
        if (metadata != null) {

            final RecordType customRecord = new RecordType();

            /*
             * BRIEF part
             */
            final SimpleLiteral identifier = new SimpleLiteral(metadata.getFileIdentifier());
            if (elementName != null && elementName.contains(_Identifier_QNAME)) {
                customRecord.setIdentifier(identifier);
            }

            SimpleLiteral title = null;
            //TODO see for multiple identification
            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification.getCitation() != null && identification.getCitation().getTitle() != null) {
                    title = new SimpleLiteral(identification.getCitation().getTitle().toString());
                }
            }
            if (elementName != null && elementName.contains(_Title_QNAME)) {
                customRecord.setTitle(title);
            }

            SimpleLiteral dataType = null;
            //TODO see for multiple hierarchyLevel
            for (ScopeCode code: metadata.getHierarchyLevels()) {
                dataType = new SimpleLiteral(code.identifier());
            }
            if (elementName != null && elementName.contains(_Type_QNAME)) {
                customRecord.setType(dataType);
            }

            final List<BoundingBoxType> bboxes = new ArrayList<BoundingBoxType>();

            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification instanceof DataIdentification) {
                    final DataIdentification dataIdentification = (DataIdentification) identification;
                    for (Extent extent : dataIdentification.getExtents()) {
                        for (GeographicExtent geoExtent :extent.getGeographicElements()) {
                            if (geoExtent instanceof GeographicBoundingBox) {
                                final GeographicBoundingBox bbox = (GeographicBoundingBox) geoExtent;
                                // TODO find CRS
                                bboxes.add(new BoundingBoxType("EPSG:4326",
                                                                bbox.getWestBoundLongitude(),
                                                                bbox.getSouthBoundLatitude(),
                                                                bbox.getEastBoundLongitude(),
                                                                bbox.getNorthBoundLatitude()));
                            }
                        }
                    }
                }
            }
            if (elementName != null && elementName.contains(_BoundingBox_QNAME)) {
                customRecord.setSimpleBoundingBox(bboxes);
            }

            if (type != null && type.equals(ElementSetType.BRIEF))
                return new BriefRecordType(identifier, title, dataType, bboxes);

            /*
             *  SUMMARY part
             */
            final List<SimpleLiteral> abstractt = new ArrayList<SimpleLiteral>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification.getAbstract() != null) {
                    abstractt.add(new SimpleLiteral(identification.getAbstract().toString()));
                }
            }
            if (elementName != null && elementName.contains(_Abstract_QNAME)) {
                customRecord.setAbstract(abstractt);
            }

            final List<SimpleLiteral> subjects = new ArrayList<SimpleLiteral>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                for (Keywords kw :identification.getDescriptiveKeywords()) {
                    for (InternationalString str : kw.getKeywords()) {
                        subjects.add(new SimpleLiteral(str.toString()));
                    }
                }
                if (identification instanceof DataIdentification) {
                    final DataIdentification dataIdentification = (DataIdentification) identification;
                    for (TopicCategory tc : dataIdentification.getTopicCategories()) {
                        subjects.add(new SimpleLiteral(tc.identifier()));
                    }
                }
            }
            if (elementName != null && elementName.contains(_Subject_QNAME)) {
                customRecord.setSubject(subjects);
            }


            List<SimpleLiteral> formats = new ArrayList<SimpleLiteral>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                for (Format f :identification.getResourceFormats()) {
                    if (f == null || f.getName() == null) {
                        continue;
                    }
                    formats.add(new SimpleLiteral(f.getName().toString()));
                }
            }
            if (formats.size() == 0);
                formats = null;
            if (elementName != null && elementName.contains(_Format_QNAME)) {
                customRecord.setFormat(formats);
            }

            final SimpleLiteral modified;
            if (metadata.getDateStamp() != null) {
                String dateValue = formatter.format(metadata.getDateStamp());
                dateValue = dateValue.substring(0, dateValue.length() - 2);
                dateValue = dateValue + ":00";
                modified = new SimpleLiteral(dateValue);
                if (elementName != null && elementName.contains(_Modified_QNAME)) {
                    customRecord.setModified(modified);
                }
            } else {
                modified = null;
            }


            if (type != null && type.equals(ElementSetType.SUMMARY))
                return new SummaryRecordType(identifier, title, dataType, bboxes, subjects, formats, modified, abstractt);

            final SimpleLiteral date    = modified;
            if (elementName != null && elementName.contains(_Date_QNAME)) {
                customRecord.setDate(date);
            }


            List<SimpleLiteral> creator = new ArrayList<SimpleLiteral>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                for (ResponsibleParty rp :identification.getPointOfContacts()) {
                    if (Role.ORIGINATOR.equals(rp.getRole())) {
                        creator.add(new SimpleLiteral(rp.getOrganisationName().toString()));
                    }

                }
            }
            if (creator.size() == 0) creator = null;
            if (elementName != null && elementName.contains(_Creator_QNAME)) {
                customRecord.setCreator(creator);
            }


            // TODO multiple
            SimpleLiteral distributor = null;
            Distribution distribution = metadata.getDistributionInfo();
            if (distribution != null) {
                for (Distributor dis :distribution.getDistributors()) {
                    ResponsibleParty disRP = dis.getDistributorContact();
                    if (disRP != null) {
                        if (disRP.getOrganisationName() != null) {
                            distributor = new SimpleLiteral(disRP.getOrganisationName().toString());
                        }
                    }
                }
            }
            if (elementName != null && elementName.contains(_Publisher_QNAME)) {
                customRecord.setPublisher(distributor);
            }


            final SimpleLiteral language = new SimpleLiteral(metadata.getLanguage().getISO3Language());
            if (elementName != null && elementName.contains(_Language_QNAME)) {
                customRecord.setLanguage(language);
            }

            // TODO
            final SimpleLiteral spatial = null;
            final SimpleLiteral references = null;
            if (type != null && type.equals(ElementSetType.FULL))
                return new RecordType(identifier, title, dataType, subjects, formats, modified, date, abstractt, bboxes, creator, distributor, language, spatial, references);

            return customRecord;
        }
        return null;
    }

    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws CstlServiceException {
        final List<DomainValues> responseList = new ArrayList<DomainValues>();
        final StringTokenizer tokens          = new StringTokenizer(propertyNames, ",");

        while (tokens.hasMoreTokens()) {
            final String token       = tokens.nextToken().trim();
            final List<String> paths = ISO_QUERYABLE.get(token);
            if (paths != null) {
                if (paths.size() != 0) {
                    
                    final List<String> values         = getAllValuesFromPaths(paths);
                    final ListOfValuesType listValues = new ListOfValuesType(values);
                    final DomainValuesType value      = new DomainValuesType(null, token, listValues, METADATA_QNAME);
                    responseList.add(value);
                    
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

    private List<String> getAllValuesFromPaths(List<String> paths) throws CstlServiceException {
        final List<String> result = new ArrayList<String>();
        Unmarshaller unmarshaller = null;
        try {
            for (File metadataFile : dataDirectory.listFiles()) {
                try {
                    unmarshaller = marshallerPool.acquireUnmarshaller();
                    Object metadata = unmarshaller.unmarshal(metadataFile);
                    if (metadata instanceof JAXBElement) {
                        metadata = ((JAXBElement) metadata).getValue();
                    }
                    result.add(GenericIndexer.getValues(metadata, paths));
                } catch (JAXBException ex) {
                    throw new CstlServiceException("The metadataFile : " + metadataFile.getName() + " can not be unmarshalled" + "\n" +
                            "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
                }
            }
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
        return StringUtilities.sortStringList(result);
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public List<String> executeEbrimSQLQuery(String sqlQuery) throws CstlServiceException {
        throw new CstlServiceException("Ebrim query are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    @Override
    public List<? extends Object> getAllEntries() throws CstlServiceException {
        final List<Object> results = new ArrayList<Object>();
        for (File f : dataDirectory.listFiles()) {
            if (f.getName().endsWith(".xml")) {
                final String identifier = f.getName().substring(0, f.getName().indexOf(".xml"));
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = marshallerPool.acquireUnmarshaller();
                    Object metadata = unmarshaller.unmarshal(f);
                    if (metadata instanceof JAXBElement) {
                        metadata = ((JAXBElement) metadata).getValue();
                    }
                    addInCache(identifier, metadata);
                    results.add(metadata);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("The metadataFile : " + f.getPath() + " can not be unmarshalled" + "\n" +
                            "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
                } finally {
                    if (unmarshaller != null) {
                        marshallerPool.release(unmarshaller);
                    }
                }
            } else {
                throw new CstlServiceException("The metadataFile : " + f.getPath() + " is not present", INVALID_PARAMETER_VALUE);
            }
        }
        return results;
    }

    @Override
    public List<String> getAllIdentifiers() throws CstlServiceException {
        final List<String> results = new ArrayList<String>();
        for (File f : dataDirectory.listFiles()) {
            if (f.getName().endsWith(".xml")) {
                final String identifier = f.getName().substring(0, f.getName().indexOf(".xml"));
                results.add(identifier);
            } else {
                throw new CstlServiceException("The metadataFile : " + f.getPath() + " is not present", INVALID_PARAMETER_VALUE);
            }
        }
        return results;
    }

    @Override
    public List<Integer> getSupportedDataTypes() {
        return Arrays.asList(ISO_19115, DUBLINCORE);
    }

    /**
     * Return the list of Additional queryable element (0 in MDWeb).
     */
    @Override
    public List<QName> getAdditionalQueryableQName() {
        return new ArrayList<QName>();
    }

    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return new HashMap<String, List<String>>();
    }

    @Override
    public Map<String, URI> getConceptMap() {
        return new HashMap<String, URI>();
    }
}

