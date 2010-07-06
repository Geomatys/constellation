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


package org.constellation.jaxb;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xml.serialize.ElementState;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CstlXMLSerializer extends XMLSerializer {

    private static final Logger LOGGER = Logger.getLogger("test");

    String currentPath = "";

    public CstlXMLSerializer(OutputFormat of) {
        super(of);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String rawName, Attributes attrs)
            throws SAXException {
        int i;
        if (namespaceURI == null || namespaceURI.isEmpty()) {
            currentPath = currentPath + '/' + localName;
        } else {
            currentPath = currentPath + '/' + namespaceURI + ':' + localName;
        }
        boolean preserveSpace;
        ElementState state;
        String name;
        String value;

        LOGGER.finer("==>startElement(" + namespaceURI + "," + localName + "," + rawName + ")");

        try {
            if (_printer == null) {
                final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "NoWriterSupplied", null);
                throw new IllegalStateException(msg);
            }

            state = getElementState();
            if (isDocumentState()) {
                // If this is the root element handle it differently.
                // If the first root element in the document, serialize
                // the document's DOCTYPE. Space preserving defaults
                // to that of the output format.
                if (!_started) {
                    startDocument((localName == null || localName.length() == 0) ? rawName : localName);
                }
            } else {
                // For any other element, if first in parent, then
                // close parent's opening tag and use the parnet's
                // space preserving.
                if (state.empty) {
                    _printer.printText('>');
                }
                // Must leave CData section first
                if (state.inCData) {
                    _printer.printText("]]>");
                    state.inCData = false;
                }
                // Indent this element on a new line if the first
                // content of the parent element or immediately
                // following an element or a comment
                if (_indenting && !state.preserveSpace
                        && (state.empty || state.afterElement || state.afterComment)) {
                    _printer.breakLine();
                }
            }
            preserveSpace = state.preserveSpace;

            //We remove the namespaces from the attributes list so that they will
            //be in _prefixes
            attrs = extractNamespaces(attrs);

            // Do not change the current element state yet.
            // This only happens in endElement().
            if (rawName == null || rawName.length() == 0) {
                if (localName == null) {
                    final String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "NoName", null);
                    throw new SAXException(msg);
                }
                if (namespaceURI != null && !namespaceURI.equals("")) {
                    String prefix = getPrefix(namespaceURI);
                    if (prefix != null && prefix.length() > 0) {
                        rawName = prefix + ":" + localName;
                    } else {
                        rawName = localName;
                    }
                } else {
                    rawName = localName;
                }
            }

            _printer.printText('<');
            _printer.printText(rawName);
            _printer.indent();

            // For each attribute print it's name and value as one part,
            // separated with a space so the element can be broken on
            // multiple lines.
            if (attrs != null) {
                for (i = 0; i < attrs.getLength(); ++i) {
                    _printer.printSpace();

                    name = attrs.getQName(i);
                    if (name != null && name.length() == 0) {
                        String prefix;
                        String attrURI;

                        name = attrs.getLocalName(i);
                        attrURI = attrs.getURI(i);
                        if ((attrURI != null && attrURI.length() != 0)
                                && (namespaceURI == null || namespaceURI.length() == 0
                                || !attrURI.equals(namespaceURI))) {
                            prefix = getPrefix(attrURI);
                            if (prefix != null && prefix.length() > 0) {
                                name = prefix + ":" + name;
                            }
                        }
                    }

                    value = attrs.getValue(i);
                    if (value == null) {
                        value = "";
                    }
                    _printer.printText(name);
                    _printer.printText("=\"");
                    printEscaped(value);
                    _printer.printText('"');

                    // If the attribute xml:space exists, determine whether
                    // to preserve spaces in this and child nodes based on
                    // its value.
                    if (name.equals("xml:space")) {
                        if (value.equals("preserve")) {
                            preserveSpace = true;
                        } else {
                            preserveSpace = _format.getPreserveSpace();
                        }
                    }
                }
            }

            if (_prefixes != null) {
                Enumeration keys;

                keys = _prefixes.keys();
                while (keys.hasMoreElements()) {
                    _printer.printSpace();
                    value = (String) keys.nextElement();
                    name = (String) _prefixes.get(value);
                    if (name.length() == 0) {
                        _printer.printText("xmlns=\"");
                        printEscaped(value);
                        _printer.printText('"');
                    } else {
                        _printer.printText("xmlns:");
                        _printer.printText(name);
                        _printer.printText("=\"");
                        printEscaped(value);
                        _printer.printText('"');
                    }
                }
            }

            // Now it's time to enter a new element state
            // with the tag name and space preserving.
            // We still do not change the curent element state.
            state = enterElementState(namespaceURI, localName, rawName, preserveSpace);
            name = (localName == null || localName.length() == 0) ? rawName : namespaceURI + "^" + localName;
            state.doCData = _format.isCDataElement(currentPath);
            state.unescaped = _format.isNonEscapingElement(name);
        } catch (IOException except) {
            throw new SAXException(except);
        }
    }



    /**
     * Retrieve and remove the namespaces declarations from the list of attributes.
     */
    private Attributes extractNamespaces(Attributes attrs) throws SAXException {
        AttributesImpl attrsOnly;
        String rawName;
        int i;
        int length;

        if (attrs == null) {
            return null;
        }
        length = attrs.getLength();
        attrsOnly = new AttributesImpl(attrs);

        for (i = length - 1; i >= 0; --i) {
            rawName = attrsOnly.getQName(i);

            //We have to exclude the namespaces declarations from the attributes
            //Append only when the feature http://xml.org/sax/features/namespace-prefixes"
            //is TRUE
            if (rawName.startsWith("xmlns")) {
                if (rawName.length() == 5) {
                    startPrefixMapping("", attrs.getValue(i));
                    attrsOnly.removeAttribute(i);
                } else if (rawName.charAt(5) == ':') {
                    startPrefixMapping(rawName.substring(6), attrs.getValue(i));
                    attrsOnly.removeAttribute(i);
                }
            }
        }
        return attrsOnly;
    }

    @Override
    public void endElement(String namespace, String localname, String rawname) throws SAXException {
        String element;
        if (namespace == null || namespace.isEmpty()) {
            element = localname;
        } else {
            element = namespace + ':' + localname;
        }
        currentPath = currentPath.substring(0, currentPath.lastIndexOf(element) - 1);

        super.endElement(namespace, localname, rawname);
    }


}
