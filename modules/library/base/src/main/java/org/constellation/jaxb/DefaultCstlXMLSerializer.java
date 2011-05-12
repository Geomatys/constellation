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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultCstlXMLSerializer extends XMLFilterImpl implements CstlXMLSerializer{

    private static final String OPEN_CDATA = "<![CDATA[";
     
    private final StringBuilder currentPath = new StringBuilder();

    private boolean reading = false;
    
    private final List<String> cdataPaths;
    
    private final Map<String , String> namespaceReplacement;
    
    public DefaultCstlXMLSerializer(final List<String> cdataPaths, final Map<String , String> namespaceReplacement) {
        super();
        if (cdataPaths != null) {
            this.cdataPaths = cdataPaths;
        } else  {
            this.cdataPaths = new ArrayList<String>();
        }
        if (namespaceReplacement != null) {
            this.namespaceReplacement = namespaceReplacement;
        } else  {
            this.namespaceReplacement = new HashMap<String, String>();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (cdataPaths.contains(currentPath.toString())) {
            length = length + 12;
            String s = new StringBuilder().append(OPEN_CDATA).append(ch).toString();
            ch = s.toCharArray();
            ch[length - 3] = ']';
            ch[length - 2] = ']';
            ch[length - 1] = '>';
        }
        super.characters(ch, start, length);
    }
    
    @Override
    public void startElement(String namespaceURI, final String localName, String rawName, Attributes attrs) throws SAXException {
        currentPath.append('/').append(localName);
        namespaceURI = replaceNamespace(namespaceURI);
        super.startElement(namespaceURI, localName, rawName, attrs);
    }

    @Override
    public void endElement(String namespace, final String localname, final String rawname) throws SAXException {
        currentPath.substring(0, currentPath.lastIndexOf(localname) - 1);
        namespace = replaceNamespace(namespace);
        super.endElement(namespace, localname, rawname);
    }
    
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        uri = replaceNamespace(uri);
        super.startPrefixMapping(prefix, uri);
    }

    private String replaceNamespace(String uri) {
        if (reading) {
            if (namespaceReplacement.containsValue(uri)) {
                for (Entry<String, String> entry : namespaceReplacement.entrySet()) {
                    if (entry.getValue().equals(uri)) {
                        return entry.getKey();
                    }
                }
            }
        } else {
            if (namespaceReplacement.containsKey(uri)) {
                return namespaceReplacement.get(uri);
            }
        }
        return uri;
    }

    /**
     * @return the reading
     */
    public boolean isReading() {
        return reading;
    }

    /**
     * @param reading the reading to set
     */
    public void setReading(boolean reading) {
        this.reading = reading;
    }
}
