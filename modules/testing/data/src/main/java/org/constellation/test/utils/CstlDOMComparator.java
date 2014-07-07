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
package org.constellation.test;

import org.apache.sis.test.XMLComparator;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CstlDOMComparator extends XMLComparator {

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
