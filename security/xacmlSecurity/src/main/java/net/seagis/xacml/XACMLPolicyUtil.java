/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package net.seagis.xacml;

import com.sun.xacml.ParsingException;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.finder.PolicyFinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


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
            throws IOException, SAXException
    {
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
            throws IOException, SAXException
    {
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
