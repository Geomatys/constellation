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

import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class XercesCstlXMLSerializer extends XMLFilterImpl implements CstlXMLSerializer{

    private static final String OPEN_CDATA = "<![CDATA[";
     
    private final StringBuilder currentPath = new StringBuilder();

    private List<String> cdataPaths;
    
    public XercesCstlXMLSerializer(final List<String> cdataPaths) {
        super();
        this.cdataPaths = cdataPaths;
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
    public void startElement(final String namespaceURI, final String localName, String rawName, Attributes attrs) throws SAXException {
        currentPath.append('/').append(localName);
        super.startElement(namespaceURI, localName, rawName, attrs);
    }

    @Override
    public void endElement(final String namespace, final String localname, final String rawname) throws SAXException {
        currentPath.substring(0, currentPath.lastIndexOf(localname) - 1);
        super.endElement(namespace, localname, rawname);
    }


}
