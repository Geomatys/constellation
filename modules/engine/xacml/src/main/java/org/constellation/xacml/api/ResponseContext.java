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

import org.geotoolkit.xacml.xml.context.ResultType;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 

/**
 *  Represents a XACML Response
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public interface ResponseContext extends ContextMapOp {

    /**
     * Decision based on the evaluation of policies
     * @return int value {@see XACMLConstants#DECISION_PERMIT}
     * @see XACMLConstants
     */
    int getDecision();

    /**
     * Get the result
     * @return
     */
    ResultType getResult();

    /**
     * Return the element of the document
     * from where the response was created if available
     * Null if no parsing was involved
     * @return
     */
    Node getDocumentElement();

    /**
     * Read a response from an input stream
     * @param is
     * @throws IOException
     */
    void readResponse(InputStream is) throws IOException;

    /**
     * Read a preparsed Node
     * @param node
     * @throws IOException
     */
    void readResponse(Node node) throws IOException;

    /**
     * Marshall the response context onto an Output Stream
     * @param os OutputStream (System.out, ByteArrayOutputStream etc)
     * @throws IOException
     */
    void marshall(OutputStream os) throws IOException;
}
