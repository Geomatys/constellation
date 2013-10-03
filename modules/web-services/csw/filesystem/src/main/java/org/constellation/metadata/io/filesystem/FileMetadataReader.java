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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JAXB dependencies
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.util.ReflectionUtilities;

import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.CSWConstants.*;
import static org.constellation.metadata.io.filesystem.FileMetadataUtils.*;

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
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.identification.BrowseGraphic;
import org.opengis.util.InternationalString;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;

import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory.*;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory.*;
import static org.geotoolkit.csw.xml.TypeNames.*;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.NullArgumentException;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.Namespaces;
import org.constellation.metadata.index.XpathUtils;
import org.constellation.util.NodeUtilities;
import org.geotoolkit.temporal.object.TemporalUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A CSW Metadata Reader. This reader does not require a database.
 * The CSW records are stored XML file in a directory .
 *
 * This reader can be used for test purpose or in case of small amount of record.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataReader extends AbstractMetadataReader implements CSWMetadataReader {

    private static final String METAFILE_MSG = "The metadata file : ";
    
    /**
     * A date formatter used to display the Date object for Dublin core translation.
     */
    private static final DateFormat FORMATTER;
    static {
        FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+2"));
    }

    /**
     * The directory containing the data XML files.
     */
    private final File dataDirectory;
    
    /**
     * A unmarshaller to get java object from metadata files.
     */
    private static final MarshallerPool marshallerPool = EBRIMMarshallerPool.getInstance();

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
    public FileMetadataReader(final Automatic configuration) throws MetadataIoException {
        super(true, false);
        dataDirectory = configuration.getDataDirectory();
        if (dataDirectory == null) {
            throw new MetadataIoException("cause: unable to find the data directory", NO_APPLICABLE_CODE);
        } else if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
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
    public Object getMetadata(final String identifier, final int mode) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, new ArrayList<QName>());
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public Object getMetadata(final String identifier, final int mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        return getOriginalMetadata(identifier, mode, type, elementName);
    }

    public Object getMetadataObj(final String identifier, final int mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        //we look for cached object
        Object obj = null;
        if (isCacheEnabled()) {
            obj = getFromCache(identifier);
        }
        if (obj == null) {
            obj = getObjectFromFile(identifier);
        }
        if (obj instanceof DefaultMetadata && mode == DUBLINCORE) {
            obj = translateISOtoDC((DefaultMetadata)obj, type, elementName);
        } else if (obj instanceof RecordType && mode == DUBLINCORE) {
            obj = applyElementSet((RecordType)obj, type, elementName);
        }
        return obj;
    }

    @Override
    public Object getOriginalMetadata(final String identifier, final int mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
        final int metadataMode;
        try {
            metadataMode = getMetadataType(metadataFile);
        } catch (IOException | XMLStreamException ex) {
            throw new MetadataIoException(ex);
        }
        if ((elementName == null || elementName.isEmpty()) && (ElementSetType.FULL.equals(type) || type == null) && (metadataMode == mode || mode == NATIVE)) {
            if (metadataFile != null && metadataFile.exists()) {
                return getNodeFromFile(metadataFile);
            }
            throw new MetadataIoException(METAFILE_MSG + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        } else if (metadataMode ==  ISO_19115 && mode == DUBLINCORE) {
            return translateISOtoDCNode(getNodeFromFile(metadataFile), type, elementName);
        } else {
            return getMetadataObj(identifier, mode, type, elementName);
        }
    }

    private int getMetadataType(final File metadataFile) throws IOException, XMLStreamException {
        final XMLInputFactory xif = XMLInputFactory.newFactory();
        final String rootName;
        try (FileInputStream fos = new FileInputStream(metadataFile)) {
            XMLStreamReader xsr = xif.createXMLStreamReader(fos, "UTF-8");
            xsr.nextTag();
            rootName = xsr.getLocalName();
            xsr.close();
        }
        switch (rootName) {
            case "MD_Metadata":
            case "MI_Metadata":
                return ISO_19115;
            case "Record":
                return DUBLINCORE;
            case "SensorML":
                return SENSORML;
            case "RegistryObject":
            case "AdhocQuery":
            case "Association":
            case "RegistryPackage":
            case "Registry":
            case "ExtrinsicObject":
            case "RegistryEntry":
                return EBRIM;
            default:
                return NATIVE;
        }
        // TODO complete other metadata type
    }

    @Override
    public boolean existMetadata(final String identifier) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
        return metadataFile != null && metadataFile.exists();
    }
    
    /**
     * Unmarshall The file designed by the path dataDirectory/identifier.xml
     * If the file is not present or if it is impossible to unmarshall it it return an exception.
     *
     * @param identifier the metadata identifier
     * @return A unmarshalled metadata object.
     * @throws org.constellation.ws.MetadataIoException
     */
    private Object getObjectFromFile(final String identifier) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
        if (metadataFile != null && metadataFile.exists()) {
            try {
                final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                Object metadata = unmarshaller.unmarshal(metadataFile);
                marshallerPool.recycle(unmarshaller);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                if (isCacheEnabled()) {
                    addInCache(identifier, metadata);
                }
                return metadata;
            } catch (JAXBException | IllegalArgumentException | NullArgumentException ex) {
                throw new MetadataIoException(METAFILE_MSG + metadataFile.getName() + " can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            }
        } 
        throw new MetadataIoException(METAFILE_MSG + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
    }

    private Node getNodeFromFile(final File metadataFile) throws MetadataIoException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document document = docBuilder.parse(metadataFile);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new MetadataIoException(METAFILE_MSG + metadataFile.getName() + ".xml can not be read", ex, NO_APPLICABLE_CODE);
        }
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

            if (type != null && type.equals(ElementSetType.BRIEF)) {
                return new BriefRecordType(identifier, title, dataType, bboxes);
            }

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


            if (type != null && type.equals(ElementSetType.SUMMARY)) {
                return new SummaryRecordType(identifier, title, dataType, bboxes, subjects, formats, modified, abstractt);
            }

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
            if (creator.isEmpty()) {creator = null;}
            
            if (elementName != null && elementName.contains(_Creator_QNAME)) {
                customRecord.setCreator(creator);
            }
            
            List<String> descriptions = new ArrayList<>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                for (BrowseGraphic go :identification.getGraphicOverviews()) {
                    if (go.getFileName() != null) {
                        descriptions.add(go.getFileName().toString());
                    }
                }
            }
            
            if (!descriptions.isEmpty() && elementName != null && elementName.contains(_Description_QNAME)) {
                customRecord.setDescription(new SimpleLiteral(null, descriptions));
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
            if (type != null && type.equals(ElementSetType.FULL)) {
                final RecordType r = new RecordType(identifier, title, dataType, subjects, formats, modified, date, abstractt, bboxes, creator, distributor, language, spatial, references);
                 if (!descriptions.isEmpty()) {
                    r.setDescription(new SimpleLiteral(null, descriptions));
                 }
                return r;
            }

            return customRecord;
        }
        return null;
    }

    private Node translateISOtoDCNode(final Node metadata, final ElementSetType type, final List<QName> elementName) throws MetadataIoException  {
        if (metadata != null) {

            final DocumentBuilder docBuilder;
            try {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                docBuilder = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new MetadataIoException(ex);
            }
            final Document document = docBuilder.newDocument();

            final Element root = document.createElementNS(Namespaces.CSW, "Record");

            /*
             * BRIEF part
             */
            final List<String> identifierValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Identifier"));
            final List<Node> identifiers = new ArrayList<>();
            for (String id : identifierValues) {
                final Node identifier = document.createElementNS("http://purl.org/dc/elements/1.1/", "identifier");
                identifier.setTextContent(id);
                identifiers.add(identifier);
            }
            
            if (elementName != null && elementName.contains(_Identifier_QNAME)) {
                NodeUtilities.appendChilds(root, identifiers);
            }

            final List<String> titleValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Title"));
            final List<Node> titles = new ArrayList<>();
            for (String titleValue : titleValues) {
                final Node title = document.createElementNS("http://purl.org/dc/elements/1.1/", "title");
                title.setTextContent(titleValue);
                titles.add(title);
            }

            if (elementName != null && elementName.contains(_Title_QNAME)) {
                NodeUtilities.appendChilds(root, titles);
            }
            
            final List<String> dataTypeValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Type"));
            final List<Node> dataTypes = new ArrayList<>();
            for (String dataTypeValue : dataTypeValues) {
                final Node dataType = document.createElementNS("http://purl.org/dc/elements/1.1/", "type");
                dataType.setTextContent(dataTypeValue);
                dataTypes.add(dataType);
            }

            if (elementName != null && elementName.contains(_Type_QNAME)) {
                NodeUtilities.appendChilds(root, dataTypes);
            }

            final List<String> westValues  = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("WestBoundLongitude"));
            final List<String> eastValues  = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("EastBoundLongitude"));
            final List<String> northValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("NorthBoundLatitude"));
            final List<String> southValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("SouthBoundLatitude"));

            final List<Node> bboxes = new ArrayList<>();
            if (westValues.size()  == eastValues.size()  &&
                eastValues.size()  == northValues.size() &&
                northValues.size() == southValues.size()) {

                for (int i = 0; i < westValues.size(); i++) {
                    final Node bboxNode = document.createElementNS("http://www.opengis.net/ows", "BoundingBox");
                    final Node crsAtt   = document.createAttribute("crs");
                    crsAtt.setTextContent("EPSG:4326");
                    bboxNode.getAttributes().setNamedItem(crsAtt);
                    final Node dimAtt   = document.createAttribute("dimensions");
                    dimAtt.setTextContent("2");
                    bboxNode.getAttributes().setNamedItem(dimAtt);
                    final Node lower    = document.createElementNS("http://www.opengis.net/ows", "LowerCorner");
                    lower.setTextContent(westValues.get(i) + " " + southValues.get(i));
                    bboxNode.appendChild(lower);
                    final Node upper    = document.createElementNS("http://www.opengis.net/ows", "UpperCorner");
                    upper.setTextContent(eastValues.get(i) + " " + northValues.get(i));
                    bboxNode.appendChild(upper);
                    bboxes.add(bboxNode);
                }
            } else {
                LOGGER.warning("incoherent bboxes coordinate");
            }

            if (ElementSetType.BRIEF.equals(type)) {
                final Element briefRoot = document.createElementNS(Namespaces.CSW, "BriefRecord");
                NodeUtilities.appendChilds(briefRoot, identifiers);
                NodeUtilities.appendChilds(briefRoot, titles);
                NodeUtilities.appendChilds(briefRoot, dataTypes);
                NodeUtilities.appendChilds(briefRoot, bboxes);
                return briefRoot;
            }

            /*
             *  SUMMARY part
             */
            final List<String> abstractValues = NodeUtilities.getValuesFromPath(metadata, "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString");
            final List<Node> abstracts = new ArrayList<>();
            for (String abstractValue : abstractValues) {
                final Node _abstract = document.createElementNS("http://purl.org/dc/terms/", "abstract");
                _abstract.setTextContent(abstractValue);
                abstracts.add(_abstract);
            }

            if (elementName != null && elementName.contains(_Abstract_QNAME)) {
                NodeUtilities.appendChilds(root, abstracts);
            }

            final List<Node> subjects = new ArrayList<>();
            final List<String> kwValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Subject"));
            for (String kwValue : kwValues) {
                final Node subject = document.createElementNS("http://purl.org/dc/elements/1.1/", "subject");
                subject.setTextContent(kwValue);
                subjects.add(subject);
            }
            if (elementName != null && elementName.contains(_Subject_QNAME)) {
                NodeUtilities.appendChilds(root, subjects);
            }


            List<Node> formats = new ArrayList<>();
            final List<String> formValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Format"));
            for (String formValue : formValues) {
                final Node format = document.createElementNS("http://purl.org/dc/elements/1.1/", "format");
                format.setTextContent(formValue);
                formats.add(format);
            }

            if (elementName != null && elementName.contains(_Format_QNAME)) {
                 NodeUtilities.appendChilds(root, formats);
            }

            final List<Node> modifieds = new ArrayList<>();
            final List<String> modValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Modified"));
            for (String modValue : modValues) {
                final String dateValue = formatDate(modValue);
                final Node modified = document.createElementNS("http://purl.org/dc/terms/", "modified");
                modified.setTextContent(dateValue);
                modifieds.add(modified);
            }

            if (elementName != null && elementName.contains(_Modified_QNAME)) {
                NodeUtilities.appendChilds(root, modifieds);
            }

            if (ElementSetType.SUMMARY.equals(type)) {
                final Element sumRoot = document.createElementNS(Namespaces.CSW, "SummaryRecord");
                NodeUtilities.appendChilds(sumRoot, identifiers);
                NodeUtilities.appendChilds(sumRoot, titles);
                NodeUtilities.appendChilds(sumRoot, dataTypes);
                NodeUtilities.appendChilds(sumRoot, subjects);
                NodeUtilities.appendChilds(sumRoot, formats);
                NodeUtilities.appendChilds(sumRoot, modifieds);
                NodeUtilities.appendChilds(sumRoot, abstracts);
                NodeUtilities.appendChilds(sumRoot, bboxes);
                return sumRoot;
            }

            final List<Node> dates = new ArrayList<>();
            for (String modValue : modValues) {
                final String dateValue = formatDate(modValue);
                final Node date = document.createElementNS("http://purl.org/dc/elements/1.1/", "date");
                date.setTextContent(dateValue);
                dates.add(date);
            }

            if (elementName != null && elementName.contains(_Date_QNAME)) {
                NodeUtilities.appendChilds(root, dates);
            }


            final List<Node> creators = new ArrayList<>();
            final List<String> creaValues = NodeUtilities.getValuesFromPaths(metadata, DUBLIN_CORE_QUERYABLE.get("creator"));
            for (String creaValue : creaValues) {
                final Node creator = document.createElementNS("http://purl.org/dc/elements/1.1/", "creator");
                creator.setTextContent(creaValue);
                creators.add(creator);
            }

            if (elementName != null && elementName.contains(_Creator_QNAME)) {
                NodeUtilities.appendChilds(root, creators);
            }

            final List<Node> descriptions = new ArrayList<>();
            final List<String> desValues = NodeUtilities.getValuesFromPath(metadata, "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gmx:FileName/@src");
            for (String descValue : desValues) {
                final Node desc = document.createElementNS("http://purl.org/dc/elements/1.1/", "description");
                desc.setTextContent(descValue);
                descriptions.add(desc);
            }

            if (!descriptions.isEmpty() && elementName != null && elementName.contains(_Description_QNAME)) {
                NodeUtilities.appendChilds(root, descriptions);
            }

            final List<Node> distributors = new ArrayList<>();
            final List<String> paths = new ArrayList<>();
            paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
            paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
            paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
            paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
            final List<String> distValues = NodeUtilities.getValuesFromPaths(metadata, paths);
            for (String distValue : distValues) {
                final Node dist = document.createElementNS("http://purl.org/dc/elements/1.1/", "publisher");
                dist.setTextContent(distValue);
                distributors.add(dist);
            }

            if (elementName != null && elementName.contains(_Publisher_QNAME)) {
                NodeUtilities.appendChilds(root, distributors);
            }

            final List<Node> languages = new ArrayList<>();
            final List<String> langValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Language"));
            for (String langValue : langValues) {
                final Node lang = document.createElementNS("http://purl.org/dc/elements/1.1/", "language");
                lang.setTextContent(langValue);
                languages.add(lang);
            }
           
            if (elementName != null && elementName.contains(_Language_QNAME)) {
                NodeUtilities.appendChilds(root, languages);
            }

            if (elementName != null && elementName.contains(_BoundingBox_QNAME)) {
                NodeUtilities.appendChilds(root, bboxes);
            }
            
            /* TODO
            final SimpleLiteral spatial = null;
            final SimpleLiteral references = null;*/

            if (ElementSetType.FULL.equals(type)) {
                final Element recRoot = document.createElementNS(Namespaces.CSW, "Record");
                NodeUtilities.appendChilds(recRoot, identifiers);
                NodeUtilities.appendChilds(recRoot, titles);
                NodeUtilities.appendChilds(recRoot, dataTypes);
                NodeUtilities.appendChilds(recRoot, subjects);
                NodeUtilities.appendChilds(recRoot, formats);
                NodeUtilities.appendChilds(recRoot, languages);
                NodeUtilities.appendChilds(recRoot, creators);
                NodeUtilities.appendChilds(recRoot, modifieds);
                NodeUtilities.appendChilds(recRoot, descriptions);
                NodeUtilities.appendChilds(recRoot, dates);
                NodeUtilities.appendChilds(recRoot, abstracts);
                NodeUtilities.appendChilds(recRoot, distributors);
                NodeUtilities.appendChilds(recRoot, bboxes);
                //NodeUtilities.appendChilds(recRoot, spatials);
                //NodeUtilities.appendChilds(recRoot, references);
                return recRoot;
            }

            document.appendChild(root);
            return root;
        }
        return null;
    }

    private String formatDate(final String modValue) {
        try {
            final Date d = TemporalUtilities.parseDate(modValue);
            String dateValue;
            synchronized (FORMATTER) {
                dateValue = FORMATTER.format(d);
            }
            dateValue = dateValue.substring(0, dateValue.length() - 2);
            dateValue = dateValue + ":00";
            return dateValue;
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "unable to parse date: {0}", modValue);
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
            List<String> paths;
            if (ISO_QUERYABLE.get(token) != null) {
                paths = ISO_QUERYABLE.get(token);
            } else if (DUBLIN_CORE_QUERYABLE.get(token) != null) {
                paths = DUBLIN_CORE_QUERYABLE.get(token);
            } else {
                throw new MetadataIoException("The property " + token + " is not queryable",
                        INVALID_PARAMETER_VALUE, "propertyName");
            }

            if (!paths.isEmpty()) {
                paths = XpathUtils.xpathToMDPath(paths);
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
     * Return all the String values corresponding to the specified list of path through the metadata.
     * 
     * @param paths
     * @return
     * @throws MetadataIoException
     */
    private List<String> getAllValuesFromPaths(final List<String> paths, final File directory) throws MetadataIoException {
        final List<String> result = new ArrayList<>();
        for (File metadataFile : directory.listFiles()) {
            if (!metadataFile.isDirectory()) {
                try {
                    final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                    Object metadata = unmarshaller.unmarshal(metadataFile);
                    marshallerPool.recycle(unmarshaller);
                    if (metadata instanceof JAXBElement) {
                        metadata = ((JAXBElement) metadata).getValue();
                    }
                    final List<Object> value = GenericIndexer.extractValues(metadata, paths);
                    if (value != null && !value.equals(Arrays.asList("null"))) {
                        for (Object obj : value){
                            result.add(obj.toString());
                        }
                    }

                 //continue to the next file   
                } catch (JAXBException | IllegalArgumentException ex) {
                    LOGGER.warning(METAFILE_MSG + metadataFile.getName() + " can not be unmarshalled\ncause: " + ex.getMessage());
                }
            } else {
                result.addAll(getAllValuesFromPaths(paths, metadataFile));
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
        return getAllEntries(dataDirectory);
    }
    
    /**
     *
     */
    public List<? extends Object> getAllEntries(final File directory) throws MetadataIoException {
        final List<Object> results = new ArrayList<>();
        for (File f : directory.listFiles()) {
            final String fileName = f.getName();
            if (fileName.endsWith(XML_EXT)) {
                final String identifier = fileName.substring(0, fileName.lastIndexOf(XML_EXT));
                try {
                    final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                    Object metadata = unmarshaller.unmarshal(f);
                    marshallerPool.recycle(unmarshaller);
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
                }
            } else if (f.isDirectory()) {
                results.addAll(getAllEntries(f));
            } else {
                // throw or continue to the next file?
                //throw new MetadataIoException(METAFILE_MSG + f.getPath() + " does not ands with .xml or is not a directory", INVALID_PARAMETER_VALUE);
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
    public List<String> getAllIdentifiers(final File directory) throws MetadataIoException {
        final List<String> results = new ArrayList<>();
        for (File f : directory.listFiles()) {
            final String fileName = f.getName();
            if (fileName.endsWith(XML_EXT)) {
                final String identifier = fileName.substring(0, fileName.lastIndexOf(XML_EXT));
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
        return Arrays.asList(ISO_19115, DUBLINCORE, EBRIM, ISO_19110);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, URI> getConceptMap() {
        return new HashMap<>();
    }
}

