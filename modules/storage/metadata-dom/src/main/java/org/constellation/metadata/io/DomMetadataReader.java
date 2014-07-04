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
package org.constellation.metadata.io;

import org.apache.sis.xml.Namespaces;
import org.constellation.util.NodeUtilities;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class DomMetadataReader extends AbstractMetadataReader {

    /**
     * A date formatter used to display the Date object for Dublin core translation.
     */
    private static final DateFormat FORMATTER;
    static {
        FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+2"));
    }

    protected final DocumentBuilderFactory dbf;

    protected final XMLInputFactory xif = XMLInputFactory.newFactory();

    public DomMetadataReader(final boolean isCacheEnabled, final boolean isThreadEnabled) {
        super(isCacheEnabled, isThreadEnabled);
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }

    protected MetadataType getMetadataType(final InputStream metadataStream, final boolean reset) throws IOException, XMLStreamException {
        final String rootName;
        if (reset){
            metadataStream.mark(0);
        }
        final XMLStreamReader xsr = xif.createXMLStreamReader(metadataStream);
        xsr.nextTag();
        rootName = xsr.getLocalName();
        xsr.close();
        if (reset) {
            metadataStream.reset();
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
    
    protected MetadataType getMetadataType(final Reader metadataReader, final boolean reset) throws IOException, XMLStreamException {
        final String rootName;
        if (reset){
            metadataReader.mark(0);
        }
        final XMLStreamReader xsr = xif.createXMLStreamReader(metadataReader);
        xsr.nextTag();
        rootName = xsr.getLocalName();
        xsr.close();
        if (reset) {
            metadataReader.reset();
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

    protected Node getNodeFromFile(final File metadataFile) throws MetadataIoException {
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.parse(metadataFile);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new MetadataIoException("The metadata file : " + metadataFile.getName() + ".xml can not be read", ex, NO_APPLICABLE_CODE);
        }
    }

    protected Node getNodeFromStream(final InputStream stream) throws MetadataIoException {
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.parse(stream);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new MetadataIoException("unable to parse the metadata", ex, NO_APPLICABLE_CODE);
        }
    }
    
    protected Node getNodeFromReader(final Reader reader) throws MetadataIoException {
        try {
            final InputSource source = new InputSource(reader);
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.parse(source);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new MetadataIoException("unable to parse the metadata", ex, NO_APPLICABLE_CODE);
        }
    }

    protected String formatDate(final String modValue) {
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
     * Apply the elementSet (Brief, Summary or full) or the custom elementSetName on the specified record.
     *
     * @param record A dublinCore record.
     * @param type The ElementSetType to apply on this record.
     * @param elementName A list of QName corresponding to the requested attribute. this parameter is ignored if type is not null.
     *
     * @return A record object.
     * @throws MetadataIoException If the type and the element name are null.
     */
    protected Node applyElementSetNode(final Node record, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        final DocumentBuilder docBuilder;
        try {
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

    /**
     * Return all the String values corresponding to the specified list of path through the metadata.
     *
     * @param paths
     * @return
     * @throws MetadataIoException
     */
    protected List<String> getAllValuesFromPaths(final List<String> paths) throws MetadataIoException {
        final List<String> result = new ArrayList<>();
        final List<String> ids    = getAllIdentifiers();
        for (String metadataID : ids) {
            final Node metadata = getMetadata(metadataID, MetadataType.ISO_19115);
            final List<Object> value = NodeUtilities.extractValues(metadata, paths);
            if (value != null && !value.equals(Arrays.asList("null"))) {
                for (Object obj : value){
                    result.add(obj.toString());
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
     public abstract List<Node> getAllEntries() throws MetadataIoException;
}
