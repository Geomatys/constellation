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
package org.constellation.xacml.api;

import org.geotoolkit.xacml.xml.context.RequestType;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 

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
