/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007, JBoss Inc.
 *    (C) 2007 - 2008, Geomatys
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
import org.constellation.xacml.context.ObjectFactory;
import org.constellation.xacml.context.RequestType;
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
    public Object get(final XACMLConstants key) {
        return map.get(key);
    }

    /**
     * @see ContextMapOp#set
     */
    public void set(final XACMLConstants key, final Object obj) {
        map.put(key, obj);
    }

    /**
     * @see RequestContext#getDocumentElement()
     */
    public Node getDocumentElement() {
        return documentElement;
    }

    /**
     * @see RequestContext#setRequest(RequestType)
     */
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
