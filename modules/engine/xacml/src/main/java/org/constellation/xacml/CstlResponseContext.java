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

import com.sun.xacml.Indenter;
import com.sun.xacml.Obligation;
import com.sun.xacml.ParsingException;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;
import org.geotoolkit.xacml.xml.context.ObjectFactory;
import org.geotoolkit.xacml.xml.context.ResultType;
import org.geotoolkit.xacml.xml.context.StatusCodeType;
import org.geotoolkit.xacml.xml.context.StatusType;
import org.geotoolkit.xacml.xml.policy.EffectType;
import org.geotoolkit.xacml.xml.policy.ObligationType;
import org.geotoolkit.xacml.xml.policy.ObligationsType;
import org.constellation.xacml.api.ResponseContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *  Implementation of the ResponseContext interface
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007
 *  @version $Revision$
 */
public class CstlResponseContext implements ResponseContext {

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
     * @see ResponseContext#getDecision()
     */
    @Override
    public int getDecision() {
        final int decision = XACMLConstants.DECISION_DENY;
        final ResponseCtx response = (ResponseCtx) map.get(XACMLConstants.RESPONSE_CTX);
        if (response == null) {
            return decision;
        }
        final Set<Result> results = response.getResults();
        if (results == null || results.isEmpty()) {
            return decision;
        }
        final Result res = results.iterator().next();
        if (res == null) {
            return decision;
        }
        return res.getDecision();
    }

    /**
     * @see ResponseContext#getResult()
     */
    @Override
    public ResultType getResult() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final ResultType resultType = objectFactory.createResultType();
        final ResponseCtx response = (ResponseCtx) map.get(XACMLConstants.RESPONSE_CTX);
        if (response == null) {
            return resultType;
        }
        final Set<Result> results = response.getResults();
        if (results == null || results.isEmpty()) {
            return resultType;
        }

        //Resource ID
        final Result result = (Result) results.iterator().next();
        if (result == null) {
            return resultType;
        }
        resultType.setResourceId(result.getResource());

        //Status
        final Status status = result.getStatus();
        final StatusType statusType = objectFactory.createStatusType();
        final StatusCodeType statusCodeType = objectFactory.createStatusCodeType();
        statusCodeType.setValue(status.getMessage());
        statusType.setStatusCode(statusCodeType);

        //Obligations
        final Set<Obligation> obligationsSet = result.getObligations();
        if (obligationsSet == null) {
            return resultType;
        }
        for (Obligation obl : obligationsSet) {
            final ObligationType obType = new ObligationType();
            obType.setObligationId(obl.getId().toASCIIString());
            obType.setFulfillOn(EffectType.fromValue(Result.DECISIONS[obl.getFulfillOn()]));

            final ObligationsType obligationsType = new ObligationsType();
            obligationsType.getObligation().add(obType);
            resultType.setObligations(obligationsType);
        }

        return resultType;
    }

    /**
     * @see ResponseContext#getDocumentElement()
     */
    @Override
    public Node getDocumentElement() {
        return documentElement;
    }

    /**
     * @see ResponseContext#marshall(OutputStream)
     */
    @Override
    public void marshall(final OutputStream os) throws IOException {
        final ResponseCtx storedResponse = (ResponseCtx) get(XACMLConstants.RESPONSE_CTX);
        if (storedResponse != null) {
            storedResponse.encode(os, new Indenter(0));
        }
    }

    /**
     * @see ResponseContext#readResponse(InputStream)
     */
    @Override
    public void readResponse(final InputStream is) throws IOException {
        readResponse(getResponse(is));
    }

    /**
     * @see ResponseContext#readResponse(Node)
     */
    @Override
    public void readResponse(final Node node) throws IOException {
        if (node == null) {
            throw new IllegalArgumentException("node is null");
        }
        documentElement = node;

        final ResponseCtx responseCtx;
        try {
            responseCtx = ResponseCtx.getInstance(node);
            set(XACMLConstants.RESPONSE_CTX, responseCtx);
        } catch (ParsingException e) {
            throw new IOException(e.getMessage());
        }
    }

    private Node getResponse(final InputStream is) throws IOException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        final Document doc;
        try {
            doc = factory.newDocumentBuilder().parse(is);
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex.getMessage());
        } catch (SAXException ex) {
            throw new IOException(ex.getMessage());
        }
        NodeList nodes = doc.getElementsByTagNameNS(XACMLConstants.CONTEXT_SCHEMA.name(), "Response");
        if (nodes.getLength() == 0) {
            nodes = doc.getElementsByTagName("Response");
        }
        return nodes.item(0);
    }
}
