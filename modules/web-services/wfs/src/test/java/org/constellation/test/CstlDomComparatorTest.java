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

import org.constellation.test.utils.CstlDOMComparator;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CstlDomComparatorTest {

      /**
     * Tests the {@link DomComparator#ignoredAttributes} and {@link DomComparator#ignoredNodes}
     * sets.
     *
     * @throws Exception Should never happen.
     */
    @Test
    public void testIgnore() throws Exception {
        CstlDOMComparator cmp = new CstlDOMComparator(
            "<body>\n" +
            "  <form id=\"MyForm\">\n" +
            "    <table cellpading=\"1\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </table>\n" +
            "  </form>\n" +
            "</body>",
            "<body>\n" +
            "  <form id=\"MyForm\">\n" +
            "    <table cellpading=\"2\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </table>\n" +
            "  </form>\n" +
            "</body>");

        ensureFail("Should fail because the \"cellpading\" attribute value is different.", cmp);

        // Following comparison should not fail anymore.
        cmp.ignoredAttributes.add("cellpading");
        cmp.compare();

        cmp.ignoredAttributes.clear();
        cmp.ignoredAttributes.add("bgcolor");
        ensureFail("The \"cellpading\" attribute should not be ignored anymore.", cmp);

        // Ignore the table node, which contains the faulty attribute.
        cmp.ignoredNodes.add("table");
        cmp.compare();

        // Ignore the form node and all its children.
        cmp.ignoredNodes.clear();
        cmp.ignoredNodes.add("form");
        cmp.compare();

        cmp = new CstlDOMComparator(
            "<ns1:body xmlns:ns1=\"http:/test.com\">\n" +
            "  <ns1:form id=\"MyForm\">\n" +
            "    <ns1:table cellpading=\"1\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </ns1:table>\n" +
            "  </ns1:form>\n" +
            "</ns1:body>",

            "<ns2:body xmlns:ns2=\"http:/test.com\">\n" +
            "  <ns2:form id=\"MyForm\">\n" +
            "    <ns2:table cellpading=\"1\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </ns2:table>\n" +
            "  </ns2:form>\n" +
            "</ns2:body>");

        cmp.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        cmp.compare(); // should not fail

        cmp = new CstlDOMComparator(
            "<ns1:body xmlns:ns1=\"http:/test.com\">\n" +
            "  <ns1:form id=\"MyForm\">\n" +
            "    <ns1:table cellpading=\"1\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </ns1:table>\n" +
            "  </ns1:form>\n" +
            "</ns1:body>",

            "<ns2:body xmlns:ns2=\"http:/test.com\" xmlns:ns3=\"http:/test.flou.com\" >\n" +
            "  <ns2:form id=\"MyForm\">\n" +
            "    <ns2:table cellpading=\"1\">\n" +
            "      <tr><td>foo</td></tr>\n" +
            "    </ns2:table>\n" +
            "  </ns2:form>\n" +
            "</ns2:body>");

        cmp.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        cmp.compare(); // should not fail
    }

    /**
     * Ensures that the call to {@link DomComparator#compare()} fails. This method is
     * invoked in order to test that the comparator rightly detected an error that we
     * were expected to detect.
     *
     * @param message The message for JUnit if the comparison does not fail.
     * @param cmp The comparator on which to invoke {@link DomComparator#compare()}.
     */
    private static void ensureFail(final String message, final CstlDOMComparator cmp) {
        try {
            cmp.compare();
        } catch (AssertionError e) {
            return;
        }
        fail(message);
    }
}
