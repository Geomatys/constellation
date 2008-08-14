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
package net.seagis.xacml.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.seagis.xacml.context.ResultType;
import org.w3c.dom.Node;
 

/**
 *  Represents a XACML Response
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public interface ResponseContext extends ContextMapOp, ElementMappingType {

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
