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
package org.constellation.metadata.io.filesystem;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
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
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.util.ReflectionUtilities;
import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.CSWConstants.*;

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
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.util.StringUtilities;
import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory.*;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory.*;
import static org.geotoolkit.csw.xml.TypeNames.*;

/**
 * A csw Metadata Reader. This reader does not require a database.
 * The csw records are stored XML file in a directory .
 *
 * This reader can be used for test purpose or in case of small amount of record.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataReader extends AbstractMetadataReader implements CSWMetadataReader {

    private static final String METAFILE_MSG = "The metadata file : ";
    
    /**
     * A date formatter used to display the Date object for dublin core translation.
     */
    private static final DateFormat FORMATTER;
    static {
        FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        //FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

    /**
     * The directory containing the data XML files.
     */
    private final File dataDirectory;
    
    /**
     * A unmarshaller to get java object from metadata files.
     */
    private final MarshallerPool marshallerPool;

    /**
     * Build a new CSW File Reader.
     *
     * @param configuration A generic configuration object containing a directory path
     * in the configuration.dataDirectory field.
     *
     * @throws org.constellation.ws.MetadataIoException If the configuration object does
     * not contains an existing directory path in the configuration.dataDirectory field.
     * If the creation of a MarshallerPool throw a JAXBException.
     */
    public FileMetadataReader(Automatic configuration) throws MetadataIoException {
        super(true, false);
        File dataDir = configuration.getDataDirectory();
        if (dataDir == null || !dataDir.exists()) {
            final File configDir = configuration.getConfigurationDirectory();
            dataDir = new File(configDir, dataDir.getName());
        }
        dataDirectory = dataDir;
        if (dataDirectory == null || !dataDirectory.exists() || !dataDirectory.isDirectory()) {
            throw new MetadataIoException("cause: unable to find the data directory", NO_APPLICABLE_CODE);
        }
        marshallerPool = EBRIMMarshallerPool.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getMetadata(String identifier, int mode, List<QName> elementName) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, elementName);
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        Object obj = getObjectFromFile(identifier);
        if (obj instanceof DefaultMetadata && mode == DUBLINCORE) {
            obj = translateISOtoDC((DefaultMetadata)obj, type, elementName);
        } else if (obj instanceof RecordType && mode == DUBLINCORE) {
            obj = applyElementSet((RecordType)obj, type, elementName);
        }
        return obj;
    }

    /**
     * Try to find a file named identifier.xml or identifier
     *
     * @param identifier
     * @param directory
     * @return
     */
    private File getFileFromIdentifier(String identifier, File directory) {
        // try to find the file in the current directory
        File metadataFile = new File (directory,  identifier + XML_EXT);
        if (!metadataFile.exists()) {
            metadataFile = new File (directory,  identifier);
        }
        if (metadataFile.exists()) {
            return metadataFile;
        } else {
            for (File child : directory.listFiles()) {
                if (child.isDirectory()) {
                    File result = getFileFromIdentifier(identifier, child);
                    if (result != null && result.exists()) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Unmarshall The file designed by the path dataDirectory/identifier.xml
     * If the file is not present or if it is impossible to unmarshall it it return an exception.
     *
     * @param identifier the metadata identifier
     * @return A unmarshalled metadata object.
     * @throws org.constellation.ws.MetadataIoException
     */
    private Object getObjectFromFile(String identifier) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
        if (metadataFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object metadata = unmarshaller.unmarshal(metadataFile);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                if (isCacheEnabled()) {
                    addInCache(identifier, metadata);
                }
                return metadata;
            } catch (JAXBException ex) {
                throw new MetadataIoException(METAFILE_MSG + metadataFile.getName() + " can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        } 
        throw new MetadataIoException(METAFILE_MSG + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
    }

    /**
     * Apply the elementSet (Brief, Summary or full) or the custom elementSetName on the specified record.
     * 
     * @param record A dublinCore record.
     * @param type The ElementSetType to apply ont this record.
     * @param elementName A list of QName correspunding to the requested attribute. this parameter is ignored if type is not null.
     *
     * @return A record object.
     * @throws MetadataIoException If the type and the element name are null.
     */
    private AbstractRecordType applyElementSet(RecordType record, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        
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
                        final Method getter = ReflectionUtilities.getGetterFromName(qn.getLocalPart(), RecordType.class);
                        final Object param  = ReflectionUtilities.invokeMethod(record, getter);

                        Method setter = null;
                        if (param != null) {
                            setter = ReflectionUtilities.getSetterFromName(qn.getLocalPart(), param.getClass(), RecordType.class);
                        } else {
                            continue;
                        }

                        if (setter != null) {
                            ReflectionUtilities.invokeMethod(setter, customRecord, param);
                        } else {
                            final String paramDesc = param.getClass().getSimpleName();
                            final String localPart = qn.getLocalPart();
                            LOGGER.warning("No setter have been found for attribute " + localPart +" of type " + paramDesc + " in the class RecordType");
                        }

                    } catch (IllegalArgumentException ex) {
                        String localPart = "null";
                        if (qn != null) {
                            localPart = qn.getLocalPart();
                        }
                        LOGGER.warning("illegal argument exception while invoking the method for attribute" + localPart + " in the classe RecordType");
                    }
                } else {
                    LOGGER.warning("An elementName was null.");
                }
            }
            return customRecord;
        } else {
            throw new MetadataIoException("No ElementSet or Element name specified");
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
    private AbstractRecordType translateISOtoDC(DefaultMetadata metadata, ElementSetType type, List<QName> elementName) {
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
            if (formats.isEmpty()) {
                formats = null;
            }
            if (elementName != null && elementName.contains(_Format_QNAME)) {
                customRecord.setFormat(formats);
            }

            final SimpleLiteral modified;
            if (metadata.getDateStamp() != null) {
                String dateValue;
                synchronized (FORMATTER) {
                    dateValue = FORMATTER.format(metadata.getDateStamp());
                }
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
            if (creator.isEmpty()) creator = null;
            
            if (elementName != null && elementName.contains(_Creator_QNAME)) {
                customRecord.setCreator(creator);
            }


            // TODO multiple
            SimpleLiteral distributor = null;
            final Distribution distribution = metadata.getDistributionInfo();
            if (distribution != null) {
                for (Distributor dis :distribution.getDistributors()) {
                    final ResponsibleParty disRP = dis.getDistributorContact();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException {
        final List<DomainValues> responseList = new ArrayList<DomainValues>();
        final StringTokenizer tokens          = new StringTokenizer(propertyNames, ",");

        while (tokens.hasMoreTokens()) {
            final String token       = tokens.nextToken().trim();
            final List<String> paths;
            if (ISO_QUERYABLE.get(token) != null) {
                paths = ISO_QUERYABLE.get(token);
            } else if (DUBLIN_CORE_QUERYABLE.get(token) != null) {
                paths = DUBLIN_CORE_QUERYABLE.get(token);
            } else {
                throw new MetadataIoException("The property " + token + " is not queryable",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }

            if (!paths.isEmpty()) {

                final List<String> values         = getAllValuesFromPaths(paths, dataDirectory);
                final ListOfValuesType listValues = new ListOfValuesType(values);
                final DomainValuesType value      = new DomainValuesType(null, token, listValues, METADATA_QNAME);
                responseList.add(value);

            } else {
                throw new MetadataIoException("The property " + token + " is not queryable for now",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }
            
        }
        return responseList;
    }

    /**
     * Return all the String values correspounding to the specified list of path through the metadata.
     * 
     * @param paths
     * @return
     * @throws MetadataIoException
     */
    private List<String> getAllValuesFromPaths(List<String> paths, File directory) throws MetadataIoException {
        final List<String> result = new ArrayList<String>();
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller    = marshallerPool.acquireUnmarshaller();
            for (File metadataFile : directory.listFiles()) {
                if (!metadataFile.isDirectory()) {
                    try {
                        Object metadata = unmarshaller.unmarshal(metadataFile);
                        if (metadata instanceof JAXBElement) {
                            metadata = ((JAXBElement) metadata).getValue();
                        }
                        final String value = GenericIndexer.extractValues(metadata, paths);
                        if (value != null && !value.equals("null")) {
                            result.add(value);
                        }
                    } catch (JAXBException ex) {
                        // throw or continue to the next file?
                        throw new MetadataIoException(METAFILE_MSG + metadataFile.getName() + " can not be unmarshalled" + "\n" +
                                "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
                    }
                } else {
                    result.addAll(getAllValuesFromPaths(paths, metadataFile));
                }
            }
        } catch (JAXBException ex) {
            throw new MetadataIoException("Error while getting unmarshaller from pool\ncause: " + ex.getMessage(), NO_APPLICABLE_CODE);

        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
        return StringUtilities.sortStringList(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> executeEbrimSQLQuery(String sqlQuery) throws MetadataIoException {
        throw new MetadataIoException("Ebrim query are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends Object> getAllEntries() throws MetadataIoException {
        return getAllEntries(dataDirectory);
    }
    
    /**
     *
     */
    public List<? extends Object> getAllEntries(File directory) throws MetadataIoException {
        final List<Object> results = new ArrayList<Object>();
        for (File f : directory.listFiles()) {
            final String fileName = f.getName();
            if (fileName.endsWith(XML_EXT)) {
                final String identifier = fileName.substring(0, fileName.lastIndexOf(XML_EXT));
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = marshallerPool.acquireUnmarshaller();
                    Object metadata = unmarshaller.unmarshal(f);
                    if (metadata instanceof JAXBElement) {
                        metadata = ((JAXBElement) metadata).getValue();
                    }
                    if (isCacheEnabled()) {
                        addInCache(identifier, metadata);
                    }
                    results.add(metadata);
                } catch (JAXBException ex) {
                    // throw or continue to the next file?
                    throw new MetadataIoException(METAFILE_MSG + f.getPath() + " can not be unmarshalled\ncause: "
                            + ex.getMessage(), ex, INVALID_PARAMETER_VALUE);
                } finally {
                    if (unmarshaller != null) {
                        marshallerPool.release(unmarshaller);
                    }
                }
            } else if (f.isDirectory()) {
                results.addAll(getAllEntries(f));
            } else {
                // throw or continue to the next file?
                throw new MetadataIoException(METAFILE_MSG + f.getPath() + " does not ands with .xml or is not a directory", INVALID_PARAMETER_VALUE);
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        return getAllIdentifiers(dataDirectory);
    }

    /**
     * 
     */
    public List<String> getAllIdentifiers(File directory) throws MetadataIoException {
        final List<String> results = new ArrayList<String>();
        for (File f : directory.listFiles()) {
            final String fileName = f.getName();
            if (fileName.endsWith(XML_EXT)) {
                final String identifier = fileName.substring(0, fileName.lastIndexOf(".xml"));
                results.add(identifier);
            } else if (f.isDirectory()){
                results.addAll(getAllIdentifiers(f));
            } else {
                throw new MetadataIoException(METAFILE_MSG + f.getPath() + " does not ands with .xml or is not a directory", INVALID_PARAMETER_VALUE);
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return new HashMap<String, List<String>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, URI> getConceptMap() {
        return new HashMap<String, URI>();
    }
}

