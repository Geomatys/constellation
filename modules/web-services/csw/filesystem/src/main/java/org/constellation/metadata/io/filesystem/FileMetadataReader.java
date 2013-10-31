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
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;

// XML dependencies
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataType;
import org.constellation.util.NodeUtilities;
import org.constellation.metadata.index.generic.NodeIndexer;

import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.CSWConstants.*;
import static org.constellation.metadata.io.filesystem.FileMetadataUtils.*;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.temporal.object.TemporalUtilities;

import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory.*;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory.*;
import static org.geotoolkit.csw.xml.TypeNames.*;

// Apache SIS dependencies
import org.apache.sis.xml.Namespaces;

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
     * Build a new CSW File Reader.
     *
     * @param configuration A generic configuration object containing a directory path
     * in the configuration.dataDirectory field.
     *
     * @throws MetadataIoException If the configuration object does
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
    public Object getMetadata(final String identifier, final MetadataType mode) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, new ArrayList<QName>());
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public Object getMetadata(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        return getOriginalMetadata(identifier, mode, type, elementName);
    }

    @Override
    public Object getOriginalMetadata(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
        final MetadataType metadataMode;
        try {
            metadataMode = getMetadataType(metadataFile);
        } catch (IOException | XMLStreamException ex) {
            throw new MetadataIoException(ex);
        }
        final Node metadataNode = getNodeFromFile(metadataFile);
            
        if (metadataMode ==  MetadataType.ISO_19115 && mode == MetadataType.DUBLINCORE) {
            return translateISOtoDCNode(metadataNode, type, elementName);
        } else if (mode == MetadataType.DUBLINCORE && metadataMode == MetadataType.DUBLINCORE) {
            return  applyElementSetNode(metadataNode, type, elementName);
        } else {
           return metadataNode;
        }
    }

    private MetadataType getMetadataType(final File metadataFile) throws IOException, XMLStreamException {
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
                return MetadataType.ISO_19115;
            case "Record":
                return MetadataType.DUBLINCORE;
            case "SensorML":
                return MetadataType.SENSORML;
            case "RegistryObject":
            case "AdhocQuery":
            case "Association":
            case "RegistryPackage":
            case "Registry":
            case "ExtrinsicObject":
            case "RegistryEntry":
                return MetadataType.EBRIM;
            default:
                return MetadataType.NATIVE;
        }
        // TODO complete other metadata type
    }

    @Override
    public boolean existMetadata(final String identifier) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
        return metadataFile != null && metadataFile.exists();
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
    private Node applyElementSetNode(final Node record, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        final DocumentBuilder docBuilder;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            docBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new MetadataIoException(ex);
        }
        final Document document = docBuilder.newDocument();
        if (type != null) {
            if (type.equals(ElementSetType.SUMMARY)) {
                final Element sumRoot = document.createElementNS(Namespaces.CSW, "SummaryRecord");
                final List<String> identifierValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:identifier");
                final List<Node> identifiers = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "identifier", identifierValues, true);
                NodeUtilities.appendChilds(sumRoot, identifiers);
                final List<String> titleValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:title");
                final List<Node> titles = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "title", titleValues, true);
                NodeUtilities.appendChilds(sumRoot, titles);
                final List<String> typeValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:type");
                final List<Node> types = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "type", typeValues, false);
                NodeUtilities.appendChilds(sumRoot, types);
                final List<String> subValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:subject");
                final List<Node> subjects = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "subject", subValues, false);
                NodeUtilities.appendChilds(sumRoot, subjects);
                final List<String> formValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:format");
                final List<Node> formats = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "format", formValues, false);
                NodeUtilities.appendChilds(sumRoot, formats);
                final List<String> modValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:modified");
                final List<Node> modifieds = NodeUtilities.buildNodes(document, "http://purl.org/dc/terms/", "modified", modValues, false);
                NodeUtilities.appendChilds(sumRoot, modifieds);
                final List<String> absValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:abstract");
                final List<Node> abstracts = NodeUtilities.buildNodes(document, "http://purl.org/dc/terms/", "abstract", absValues, false);
                NodeUtilities.appendChilds(sumRoot, abstracts);
                final List<Node> origBboxes = NodeUtilities.getNodeFromPath(record, "/ows:BoundingBox");
                for (Node origBbox : origBboxes) {
                    Node n = document.importNode(origBbox, true);
                    NodeUtilities.appendChilds(sumRoot, Arrays.asList(n));
                }
                return sumRoot;
            } else if (type.equals(ElementSetType.BRIEF)) {
                final Element briefRoot = document.createElementNS(Namespaces.CSW, "BriefRecord");
                final List<String> identifierValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:identifier");
                final List<Node> identifiers = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "identifier", identifierValues, true);
                NodeUtilities.appendChilds(briefRoot, identifiers);
                final List<String> titleValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:title");
                final List<Node> titles = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "title", titleValues, true);
                NodeUtilities.appendChilds(briefRoot, titles);
                final List<String> typeValues = NodeUtilities.getValuesFromPath(record, "/csw:Record/dc:type");
                final List<Node> types = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "type", typeValues, false);
                NodeUtilities.appendChilds(briefRoot, types);
                final List<Node> origBboxes = NodeUtilities.getNodeFromPath(record, "/csw:Record/ows:BoundingBox");
                for (Node origBbox : origBboxes) {
                    Node n = document.importNode(origBbox, true);
                    NodeUtilities.appendChilds(briefRoot, Arrays.asList(n));
                }
                return briefRoot;
            } else {
                return record;
            }
        } else if (elementName != null) {
            final Element recRoot = document.createElementNS(Namespaces.CSW, "Record");
            for (QName qn : elementName) {
                if (qn != null) {
                    final List<Node> origs = NodeUtilities.getNodeFromPath(record, "/dc:" + qn.getLocalPart());
                    for (Node orig : origs) {
                        Node n = document.importNode(orig, true);
                        NodeUtilities.appendChilds(recRoot, Arrays.asList(n));
                    }
                } else {
                    LOGGER.warning("An elementName was null.");
                }
            }
            return recRoot;
        } else {
            throw new MetadataIoException("No ElementSet or Element name specified");
        }
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
            final List<Node> identifiers = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "identifier", identifierValues, true);
            
            if (elementName != null && elementName.contains(_Identifier_QNAME)) {
                NodeUtilities.appendChilds(root, identifiers);
            }

            final List<String> titleValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Title"));
            final List<Node> titles = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "title", titleValues, true);

            if (elementName != null && elementName.contains(_Title_QNAME)) {
                NodeUtilities.appendChilds(root, titles);
            }
            
            final List<String> dataTypeValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Type"));
            final List<Node> dataTypes = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "type", dataTypeValues, false);
                
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
            final List<Node> abstracts = NodeUtilities.buildNodes(document, "http://purl.org/dc/terms/", "abstract", abstractValues, false);

            if (elementName != null && elementName.contains(_Abstract_QNAME)) {
                NodeUtilities.appendChilds(root, abstracts);
            }

            final List<String> kwValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Subject"));
            final List<Node> subjects = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "subject", kwValues, false);

            if (elementName != null && elementName.contains(_Subject_QNAME)) {
                NodeUtilities.appendChilds(root, subjects);
            }

            final List<String> formValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Format"));
            final List<Node> formats = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "format", formValues, false);

            if (elementName != null && elementName.contains(_Format_QNAME)) {
                 NodeUtilities.appendChilds(root, formats);
            }

            final List<String> modValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Modified"));
            final List<String> dateValues = new ArrayList<>();
            for (String modValue : modValues) {
                dateValues.add(formatDate(modValue));
            }
            final List<Node> modifieds = NodeUtilities.buildNodes(document, "http://purl.org/dc/terms/", "modified", dateValues, false);

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

            final List<Node> dates = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "date", dateValues, false);

            if (elementName != null && elementName.contains(_Date_QNAME)) {
                NodeUtilities.appendChilds(root, dates);
            }

            final List<String> creaValues = NodeUtilities.getValuesFromPaths(metadata, DUBLIN_CORE_QUERYABLE.get("creator"));
            final List<Node> creators = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "creator", creaValues, false);

            if (elementName != null && elementName.contains(_Creator_QNAME)) {
                NodeUtilities.appendChilds(root, creators);
            }

            final List<String> desValues = NodeUtilities.getValuesFromPath(metadata, "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gmx:FileName/@src");
            final List<Node> descriptions = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "description", desValues, false);

            if (!descriptions.isEmpty() && elementName != null && elementName.contains(_Description_QNAME)) {
                NodeUtilities.appendChilds(root, descriptions);
            }

            final List<String> paths = new ArrayList<>();
            paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
            paths.add("/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
            paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
            paths.add("/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor");
            final List<String> distValues = NodeUtilities.getValuesFromPaths(metadata, paths);
            final List<Node> distributors = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "publisher", distValues, false);

            if (elementName != null && elementName.contains(_Publisher_QNAME)) {
                NodeUtilities.appendChilds(root, distributors);
            }

            final List<String> langValues = NodeUtilities.getValuesFromPaths(metadata, ISO_QUERYABLE.get("Language"));
            final List<Node> languages = NodeUtilities.buildNodes(document, "http://purl.org/dc/elements/1.1/", "language", langValues, false);
           
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
                
                final Node metadata = getNodeFromFile(metadataFile);
                final List<Object> value = NodeIndexer.extractValues(metadata, paths);
                if (value != null && !value.equals(Arrays.asList("null"))) {
                    for (Object obj : value){
                        result.add(obj.toString());
                    }
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
    public List<Node> getAllEntries() throws MetadataIoException {
        return getAllEntries(dataDirectory);
    }
    
    private List<Node> getAllEntries(final File directory) throws MetadataIoException {
        final List<Node> results = new ArrayList<>();
        for (File f : directory.listFiles()) {
            final String fileName = f.getName();
            if (fileName.endsWith(XML_EXT)) {
                //final String identifier = fileName.substring(0, fileName.lastIndexOf(XML_EXT));
                results.add(getNodeFromFile(f));
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
        if (directory != null) {
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
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetadataType> getSupportedDataTypes() {
        return Arrays.asList(MetadataType.ISO_19115, MetadataType.DUBLINCORE, MetadataType.EBRIM, MetadataType.ISO_19110);
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

