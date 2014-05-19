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
import com.sun.xacml.ctx.RequestCtx;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;
import org.geotoolkit.xacml.xml.context.ObjectFactory;
import org.geotoolkit.xacml.xml.context.RequestType;
import org.constellation.xacml.api.RequestContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *  Implementation of the RequestContext interface
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class CstlRequestContext implements RequestContext {

    private final Map<XACMLConstants, Object> map = new HashMap<XACMLConstants, Object>();
    private Node documentElement = null;

    /**
     * @see ContextMapOp#get
     */
    @Override
    public Object get(final XACMLConstants key) {
        return map.get(key);
    }

    /**
     * @see ContextMapOp#set
     */
    @Override
    public void set(final XACMLConstants key, final Object obj) {
        map.put(key, obj);
    }

    /**
     * @see RequestContext#getDocumentElement()
     */
    @Override
    public Node getDocumentElement() {
        return documentElement;
    }

    /**
     * @see RequestContext#setRequest(RequestType)
     */
    @Override
    public void setRequest(final RequestType requestType) throws IOException {
        final JAXBElement<RequestType> requestJAXB = new ObjectFactory().createRequest(requestType);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(requestJAXB, baos);
        final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        readRequest(bis);
    }

    /**
     * @see RequestContext#readRequest(InputStream)
     */
    @Override
    public void readRequest(final InputStream is) throws IOException {
        final Node root = getRequest(is);
        documentElement = root;
        if (root == null) {
            throw new IllegalStateException("Root node read from the input stream is null");
        }
        final RequestCtx request;
        try {
           request = RequestCtx.getInstance(root);
        } catch (ParsingException p) {
            throw new IOException(p.getMessage());
        }
        set(XACMLConstants.REQUEST_CTX, request);
    }

    /**
     * @see RequestContext#readRequest(Node)
     */
    @Override
    public void readRequest(final Node node) throws IOException {
        documentElement = node;
        if (node == null) {
            throw new IllegalArgumentException("Root node is null");
        }
        final RequestCtx request;
        try {
            request = RequestCtx.getInstance(node);
        } catch (ParsingException p) {
            throw new IOException(p.getMessage());
        }
        set(XACMLConstants.REQUEST_CTX, request);
    }

    /**
     * @see RequestContext#marshall(OutputStream)
     */
    @Override
    public void marshall(final OutputStream os) throws IOException {
        final RequestCtx storedRequest = (RequestCtx) get(XACMLConstants.REQUEST_CTX);
        if (storedRequest != null) {
            storedRequest.encode(os);
        }
    }

    private Node getRequest(final InputStream is) throws IOException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        final Document doc;
        try {
            doc = factory.newDocumentBuilder().parse(is);
        } catch (ParserConfigurationException p) {
            throw new IOException(p.getMessage());
        } catch (SAXException sax) {
            throw new IOException(sax.getMessage());
        }
        final NodeList nodes = doc.getElementsByTagNameNS(XACMLConstants.CONTEXT_SCHEMA.key, "Request");
        return nodes.item(0);
    }
}
