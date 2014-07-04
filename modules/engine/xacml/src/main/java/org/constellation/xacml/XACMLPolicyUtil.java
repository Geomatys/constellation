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
package org.constellation.xacml;

import com.sun.xacml.ParsingException;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.finder.PolicyFinder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * Create an XACML Policy Object from the url for the policy xml
 * @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007
 *  @version $Revision$
 */
public class XACMLPolicyUtil {
    /**
     * Create a PolicySet
     * @param location location of the policy set file
     * @param finder PolicyFinder instance
     * @return
     * @throws Exception
     */
    public PolicySet createPolicySet(final URL location, final PolicyFinder finder) 
            throws IOException, SAXException {
        return createPolicySet(location.openStream(), finder);
    }

    /**
     * Create a policyset
     * @param is
     * @param finder
     * @return
     * @throws Exception
     */
    public PolicySet createPolicySet(final InputStream is, final PolicyFinder finder)
            throws IOException, SAXException {
        if (finder == null) {
            throw new IllegalArgumentException("Policy Finder is null");
        }
        final Document doc = getDocument(is);
        try {
            return PolicySet.getInstance(doc.getFirstChild(), finder);
        } catch (ParsingException p) {
            throw new SAXException(p);
        }
    }

    /**
     * Create a Policy
     * @param location Policy File
     * @return
     * @throws Exception
     */
    public Policy createPolicy(final URL location) throws IOException, SAXException {
        return createPolicy(location.openStream());
    }

    /**
     * Create a policy
     * @param is Inputstream of the policy file
     * @return
     * @throws Exception
     */
    public Policy createPolicy(final InputStream is) throws IOException, SAXException {
        final Document doc = getDocument(is);
        try {
            return Policy.getInstance(doc.getFirstChild());
        } catch (ParsingException p) {
            throw new SAXException(p);
        }
    }

    private Document getDocument(final InputStream is) throws SAXException, IOException {
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException p) {
            throw new SAXException(p);
        }
        return docBuilder.parse(is);
    }
}
