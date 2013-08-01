/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.test;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.sis.test.XMLComparator;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CstlDOMComparator extends XMLComparator {

    public CstlDOMComparator(final Node expected, final Node actual) {
        super(expected, actual);
    }

    public CstlDOMComparator(final Object expected, final Object actual) throws IOException, ParserConfigurationException, SAXException {
        super(expected, actual);
    }

    /**
     * Compares the names and namespaces of the given node.
     *
     * Exclude the prefix from comparison
     *
     * @param expected The node having the expected name and namespace.
     * @param actual The node to compare.
     */
    @Override
    protected void compareNames(final Node expected, final Node actual) {
        assertPropertyEquals("namespace", expected.getNamespaceURI(), actual.getNamespaceURI(), expected, actual);
        String expectedNodeName = expected.getNodeName();
        int i = expectedNodeName.indexOf(':');
        if (i != -1) {
            expectedNodeName = expectedNodeName.substring(i + 1);
        }
        String actualNodeName   = actual.getNodeName();
        i = actualNodeName.indexOf(':');
        if (i != -1) {
            actualNodeName = actualNodeName.substring(i + 1);
        }
        assertPropertyEquals("name",      expectedNodeName,     actualNodeName,     expected, actual);
    }

}
