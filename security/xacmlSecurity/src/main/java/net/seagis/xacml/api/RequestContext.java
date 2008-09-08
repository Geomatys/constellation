/*
 *    Constellation - An open source and standard compliant SDI
 *    http://constellation.codehaus.org
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
package net.seagis.xacml.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.seagis.xacml.context.RequestType;
import org.w3c.dom.Node;
 

/**
 *  Represents a Request
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public interface RequestContext extends ContextMapOp {

    /**
     * Return the element of the document
     * from where the request was created if available
     * Null if no parsing was involved
     * @return
     */
    Node getDocumentElement();

    /**
     * Place the Request instance on the context
     * @param requestType An instance of RequestType 
     * @throws IOException
     */
    void setRequest(RequestType requestType) throws IOException;

    /**
     * Read the Request from a stream
     * @param is InputStream for the request 
     * @throws IOException
     */
    void readRequest(InputStream is) throws IOException;

    /**
     * Read a preparsed Node
     * @param node
     * @throws IOException
     */
    void readRequest(Node node) throws IOException;

    /**
     * Marshall the request context onto an Output Stream
     * @param os OutputStream (System.out, ByteArrayOutputStream etc)
     * @throws IOException
     */
    void marshall(OutputStream os) throws IOException;
}
