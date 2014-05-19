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
package org.constellation.metadata.io.netcdf;

import java.util.StringTokenizer;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.Arrays;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.TimeZone;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.sis.internal.jaxb.LegacyNamespaces;
import org.constellation.util.ReflectionUtilities;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;

import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.CSWConstants.*;

import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.utils.Utils;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;

import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.ImageCoverageReader;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.XML;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.metadata.io.ElementSetType;
import org.constellation.metadata.io.MetadataType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;

import static org.geotoolkit.csw.xml.TypeNames.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory.*;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory.*;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;

import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.util.InternationalString;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.identification.Identification;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.8.4
 */
public class NetCDFMetadataReader extends AbstractMetadataReader implements CSWMetadataReader {

    /**
     * The directory containing the data XML files.
     */
    private final File dataDirectory;

    private static final String METAFILE_MSG = "The netcdf file : ";

    /**
     * A date formatter used to display the Date object for Dublin core translation.
     */
    private static final DateFormat FORMATTER;
    static {
        FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    }

    private final String CURRENT_EXT;

    private final boolean usePathAsIdentifier;

    private static final TimeZone tz = TimeZone.getTimeZone("GMT+2:00");
    /**
     * Build a new CSW NetCDF File Reader.
     *
     * @param configuration A generic configuration object containing a directory path
     * in the configuration.dataDirectory field.
     *
     * @throws MetadataIoException If the configuration object does
     * not contains an existing directory path in the configuration.dataDirectory field.
     * If the creation of a MarshallerPool throw a JAXBException.
     */
    public NetCDFMetadataReader(final Automatic configuration) throws MetadataIoException {
        super(true, false);
        dataDirectory = configuration.getDataDirectory();
        if (dataDirectory == null) {
            throw new MetadataIoException("cause: unable to find the data directory", NO_APPLICABLE_CODE);
        } else if (!dataDirectory.exists()) {
            boolean created = dataDirectory.mkdir();
            if (!created) {
                throw new MetadataIoException("cause: unable to create the unexisting data directory:" + dataDirectory.getPath(), NO_APPLICABLE_CODE);
            }
        }
        final String extension = configuration.getParameter("netcdfExtension");
        if (extension != null) {
            CURRENT_EXT = extension;
        } else {
            CURRENT_EXT = NETCDF_EXT;
        }
        final String usePathAsIdentifierValue = configuration.getParameter("usePathAsIdentifier");
        if (usePathAsIdentifierValue != null) {
            usePathAsIdentifier = Boolean.valueOf(usePathAsIdentifierValue);
        } else {
            usePathAsIdentifier = false;
        }
        if (configuration.getEnableThread() != null && !configuration.getEnableThread().isEmpty()) {
            final boolean t = Boolean.parseBoolean(configuration.getEnableThread());
            if (t) {
                LOGGER.info("parrallele treatment enabled");
            }
            setIsThreadEnabled(t);
        }
        if (configuration.getEnableCache() != null && !configuration.getEnableCache().isEmpty()) {
            final boolean c = Boolean.parseBoolean(configuration.getEnableCache());
            if (!c) {
                LOGGER.info("cache system have been disabled");
            }
            setIsCacheEnabled(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getMetadata(final String identifier, final MetadataType mode) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, new ArrayList<QName>());
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public Node getMetadata(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        Object obj = null;
        if (isCacheEnabled()) {
            obj = getFromCache(identifier);
        }
        if (obj == null) {
            obj = getObjectFromFile(identifier);
        }
        if (obj instanceof DefaultMetadata && mode == MetadataType.DUBLINCORE) {
            obj = translateISOtoDC((DefaultMetadata)obj, type, elementName);
        } else if (obj instanceof RecordType && mode == MetadataType.DUBLINCORE) {
            obj = applyElementSet((RecordType)obj, type, elementName);
        }

        // marshall to DOM
        if (obj != null) {
            return writeObjectInNode(obj, mode);
        }
        return null;
    }

    @Override
    public boolean existMetadata(final String identifier) throws MetadataIoException {
        final File metadataFile;
        if (usePathAsIdentifier) {
            metadataFile = getFileFromPathIdentifier(identifier, dataDirectory, CURRENT_EXT);
        } else {
            metadataFile = getFileFromIdentifier(identifier, dataDirectory, CURRENT_EXT);
        }
        return metadataFile != null && metadataFile.exists();
    }

    /**
     * Try to find a file named identifier.nc or identifier recursively
     * in the specified directory and its sub-directories.
     *
     * @param identifier The metadata identifier.
     * @param directory The current directory to explore.
     * @param ext file extension.
     * @return
     */
    public static File getFileFromIdentifier(final String identifier, final File directory, final String ext) {
        // 1) try to find the file in the current directory
        File metadataFile = new File (directory,  identifier + ext);
        // 2) trying without the extension
        if (!metadataFile.exists()) {
            metadataFile = new File (directory,  identifier);
        }
        // 3) trying by replacing ':' by '-' (for windows platform who don't accept ':' in file name)
        if (!metadataFile.exists()) {
            final String windowsIdentifier = identifier.replace(':', '-');
            metadataFile = new File (directory,  windowsIdentifier + ext);
        }

        if (metadataFile.exists()) {
            return metadataFile;
        } else {
            for (File child : directory.listFiles()) {
                if (child.isDirectory()) {
                    final File result = getFileFromIdentifier(identifier, child, ext);
                    if (result != null && result.exists()) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Try to find a file named identifier.nc or identifier recursively
     * in the specified directory and its sub-directories.
     *
     * @param identifier The metadata identifier.
     * @param directory The current directory to explore.
     * @param ext File extension.
     * @return
     */
    public static File getFileFromPathIdentifier(final String identifier, final File directory, final String ext) {

        // if where are in the final directory
        if (identifier.indexOf(':') == -1) {
            // 1) try to find the file in the current directory
            File metadataFile = new File (directory,  identifier + ext);
            // 2) trying without the extension
            if (!metadataFile.exists()) {
                metadataFile = new File (directory,  identifier);
            }
            // 3) trying by replacing ':' by '-' (for windows platform who don't accept ':' in file name)
            if (!metadataFile.exists()) {
                final String windowsIdentifier = identifier.replace(':', '-');
                metadataFile = new File (directory,  windowsIdentifier + ext);
            }

            if (metadataFile.exists()) {
                return metadataFile;
            } else {
                LOGGER.warning("unable to find the metadata:" + identifier + " in the directory:" + directory.getPath());
                return null;
            }
        } else {
            final int separator = identifier.indexOf(':');
            final String directoryName = identifier.substring(0, separator);
            final File child = new File(directory, directoryName);
            if (child.isDirectory()) {
                final String childIdentifier = identifier.substring(separator + 1);
                return getFileFromPathIdentifier(childIdentifier, child, ext);
            } else {
                LOGGER.log(Level.WARNING, "{0} is not a  directory.", child.getPath());
                return null;
            }
        }
    }

    /**
     * Unmarshall The file designed by the path dataDirectory/identifier.nc
     * If the file is not present or if it is impossible to unmarshall it it return an exception.
     *
     * @param identifier the metadata identifier
     * @return A unmarshalled metadata object.
     * @throws org.constellation.ws.MetadataIoException
     */
    private Object getObjectFromFile(final String identifier) throws MetadataIoException {
        final File metadataFile;
        if (usePathAsIdentifier) {
            metadataFile = getFileFromPathIdentifier(identifier, dataDirectory, CURRENT_EXT);
        } else {
            metadataFile = getFileFromIdentifier(identifier, dataDirectory, CURRENT_EXT);
        }
        if (metadataFile != null && metadataFile.exists()) {
            final ImageCoverageReader reader = new ImageCoverageReader();
            try {
                reader.setInput(metadataFile);
                final Object obj = reader.getMetadata();
                Utils.setIdentifier(identifier, obj);
                return obj;
            } catch (CoverageStoreException | IllegalArgumentException ex) {
                throw new MetadataIoException(METAFILE_MSG + metadataFile.getName() + " can not be read\ncause: " + ex.getMessage(), ex, INVALID_PARAMETER_VALUE);
            } finally {
                try {
                    reader.dispose();
                } catch (CoverageStoreException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        throw new MetadataIoException(METAFILE_MSG + identifier + ".nc is not present", INVALID_PARAMETER_VALUE);
    }

    /**
     * Apply the elementSet (Brief, Summary or full) or the custom elementSetName on the specified record.
     *
     * @param record A dublinCore record.
     * @param type The ElementSetType to apply on this record.
     * @param elementName A list of QName corresponding to the requested attribute. this parameter is ignored if type is not null.
     *
     * @return A record object.
     * @throws MetadataIoException If the type and the element name are null.
     */
    private AbstractRecordType applyElementSet(final RecordType record, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {

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

                        final Method setter;
                        if (param != null) {
                            setter = ReflectionUtilities.getSetterFromName(qn.getLocalPart(), param.getClass(), RecordType.class);
                        } else {
                            continue;
                        }

                        if (setter != null) {
                            ReflectionUtilities.invokeMethod(setter, customRecord, param);
                        } else {
                            final String paramDesc = param.getClass().getSimpleName();
                            LOGGER.warning("No setter have been found for attribute " + qn.getLocalPart() +" of type " + paramDesc + " in the class RecordType");
                        }

                    } catch (IllegalArgumentException ex) {
                        LOGGER.log(Level.WARNING, "illegal argument exception while invoking the method for attribute{0} in the classe RecordType", qn.getLocalPart());
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
    private AbstractRecordType translateISOtoDC(final DefaultMetadata metadata, final ElementSetType type, final List<QName> elementName) {
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

            final List<BoundingBoxType> bboxes = new ArrayList<>();

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
            final List<SimpleLiteral> abstractt = new ArrayList<>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification.getAbstract() != null) {
                    abstractt.add(new SimpleLiteral(identification.getAbstract().toString()));
                }
            }
            if (elementName != null && elementName.contains(_Abstract_QNAME)) {
                customRecord.setAbstract(abstractt);
            }

            final List<SimpleLiteral> subjects = new ArrayList<>();
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


            List<SimpleLiteral> formats = new ArrayList<>();
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


            List<SimpleLiteral> creator = new ArrayList<>();
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

            final SimpleLiteral language;
            if (metadata.getLanguage() != null) {
                language = new SimpleLiteral(metadata.getLanguage().getISO3Language());
                if (elementName != null && elementName.contains(_Language_QNAME)) {
                    customRecord.setLanguage(language);
                }
            } else {
                language = null;
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
    public List<DomainValues> getFieldDomainofValues(final String propertyNames) throws MetadataIoException {
        final List<DomainValues> responseList = new ArrayList<>();
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

                final List<String> values         = getAllValuesFromPaths(paths, dataDirectory, null);
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
     * Return all the String values corresponding to the specified list of path through the metadata.
     *
     * @param paths
     * @return
     * @throws MetadataIoException
     */
    private List<String> getAllValuesFromPaths(final List<String> paths, final File directory, final String parentIdentifierPrefix) throws MetadataIoException {
        final String identifierPrefix    = conputeIdentifierPrefix(directory, parentIdentifierPrefix);
        final List<String> result        = new ArrayList<>();
        final ImageCoverageReader reader = new ImageCoverageReader();
        try {
            for (File metadataFile : directory.listFiles()) {

                final String fileName = metadataFile.getName();
                if (fileName.endsWith(CURRENT_EXT)) {
                    try {
                        final String identifier = computeIdentifier(fileName, identifierPrefix);
                        reader.setInput(metadataFile);
                        final Object metadata = reader.getMetadata();
                        Utils.setIdentifier(identifier, metadata);

                        final List<Object> value = GenericIndexer.extractValues(metadata, paths);
                        if (value != null && !value.equals(Arrays.asList("null"))) {
                            for (Object obj : value) {
                                result.add(obj.toString());
                            }
                        }
                        //continue to the next file
                    } catch (CoverageStoreException | IllegalArgumentException ex) {
                        LOGGER.warning(METAFILE_MSG + metadataFile.getName() + " can not be read\ncause: " + ex.getMessage());
                    }

                } else if (metadataFile.isDirectory()) {
                    result.addAll(getAllValuesFromPaths(paths, metadataFile, identifierPrefix));
                } else {
                    //do not throw exception just skipping
                    //throw new MetadataIoException(METAFILE_MSG + f.getPath() + " does not ands with " + CURRENT_EXT + " or is not a directory", INVALID_PARAMETER_VALUE);
                }
            }

        } finally {
            try {
                reader.dispose();
            } catch (CoverageStoreException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        Collections.sort(result);
        return result;
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
    public String[] executeEbrimSQLQuery(final String sqlQuery) throws MetadataIoException {
        throw new MetadataIoException("Ebrim query are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends Object> getAllEntries() throws MetadataIoException {
        return getAllEntries(dataDirectory, null);
    }

    /**
     *
     */
    private List<? extends Object> getAllEntries(final File directory, final String parentIdentifierPrefix) throws MetadataIoException {
        final String identifierPrefix = conputeIdentifierPrefix(directory, parentIdentifierPrefix);
        final List<Object> results = new ArrayList<>();
        final ImageCoverageReader reader = new ImageCoverageReader();
        for (File f : directory.listFiles()) {
            final String fileName = f.getName();
            if (fileName.endsWith(CURRENT_EXT)) {
               final String identifier = computeIdentifier(fileName, identifierPrefix);
                try {
                    reader.setInput(f);
                    final Object metadata = reader.getMetadata();
                    Utils.setIdentifier(identifier, metadata);
                    if (isCacheEnabled()) {
                        addInCache(identifier, metadata);
                    }
                    results.add(metadata);
                } catch (CoverageStoreException ex) {
                    // throw or continue to the next file?
                    throw new MetadataIoException(METAFILE_MSG + f.getPath() + " can not be read\ncause: "
                            + ex.getMessage(), ex, INVALID_PARAMETER_VALUE);
                }
            } else if (f.isDirectory()) {
                results.addAll(getAllEntries(f, identifierPrefix));
            } else {
                //do not throw exception just skipping
                //throw new MetadataIoException(METAFILE_MSG + f.getPath() + " does not ands with " + CURRENT_EXT + " or is not a directory", INVALID_PARAMETER_VALUE);
            }
        }
        try {
            reader.dispose();
        } catch (CoverageStoreException ex) {
            // throw or continue to the next file?
            LOGGER.log(Level.WARNING, "Unable to close the imageCoverageReader", ex);
        }
        return results;
    }

    @Override
    public Iterator<String> getIdentifierIterator() throws MetadataIoException {
        final List<String> results = getAllIdentifiers();
        return results.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        return getAllIdentifiers(dataDirectory, null);
    }
    
    @Override
    public int getEntryCount() throws MetadataIoException {
        return getAllIdentifiers(dataDirectory, null).size();
    }

    /**
     * find recursively the files names used as record identifier.
     *
     * @param directory
     * @param parentIdentifierPrefix
     * @return
     * @throws MetadataIoException
     */
    private List<String> getAllIdentifiers(final File directory, final String parentIdentifierPrefix) throws MetadataIoException {
        final String identifierPrefix = conputeIdentifierPrefix(directory, parentIdentifierPrefix);
        final List<String> results = new ArrayList<>();
        if (directory != null && directory.exists()) {
            for (File f : directory.listFiles()) {
                final String fileName = f.getName();
                if (fileName.endsWith(CURRENT_EXT)) {
                    results.add(computeIdentifier(fileName, identifierPrefix));
                } else if (f.isDirectory()){
                    results.addAll(getAllIdentifiers(f, identifierPrefix));
                } else {
                    //do not throw exception just skipping
                    //throw new MetadataIoException(METAFILE_MSG + f.getPath() + " does not ands with " + CURRENT_EXT + " or is not a directory", INVALID_PARAMETER_VALUE);
                }
            }
        }
        return results;
    }

    private String computeIdentifier(final String fileName, final String identifierPrefix) {
        if (usePathAsIdentifier) {
            return identifierPrefix + ':' + fileName.substring(0, fileName.lastIndexOf(CURRENT_EXT));
        } else {
            return fileName.substring(0, fileName.lastIndexOf(CURRENT_EXT));
        }
    }

    private String conputeIdentifierPrefix(final File directory, final String identifierPrefix) {
        if (usePathAsIdentifier) {
            if (identifierPrefix == null) {
                return "";
            } else {
                return identifierPrefix + ':' + directory.getName();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataType> getSupportedDataTypes() {
        return Arrays.asList(
                MetadataType.ISO_19115, MetadataType.DUBLINCORE, MetadataType.EBRIM, MetadataType.ISO_19110);
    }

    /**
     * Return the list of Additional queryable element (0 in MDWeb).
     */
    @Override
    public List<QName> getAdditionalQueryableQName() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return new HashMap<>();
    }

    protected Node writeObjectInNode(final Object obj, final MetadataType mode) throws MetadataIoException {
        final boolean replace = mode == MetadataType.ISO_19115;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.newDocument();
            final Marshaller marshaller = EBRIMMarshallerPool.getInstance().acquireMarshaller();
            final MarshallWarnings warnings = new MarshallWarnings();
            marshaller.setProperty(XML.CONVERTER, warnings);
            marshaller.setProperty(XML.TIMEZONE, tz);
            marshaller.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, replace);
            marshaller.setProperty(XML.GML_VERSION, LegacyNamespaces.VERSION_3_2_1);
            marshaller.marshal(obj, document);

            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }
}
