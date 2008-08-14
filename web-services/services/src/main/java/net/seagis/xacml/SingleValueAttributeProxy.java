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
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.AttributeProxy;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.StringAttribute;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Node;


/**
 *  Represents a single value attribute proxy
 *  @author Anil.Saldhana@redhat.com
 *  @since  Mar 28, 2008 
 *  @version $Revision$
 */
public class SingleValueAttributeProxy implements AttributeProxy {

    private URI type;

    public SingleValueAttributeProxy(final String type) {
        try {
            this.type = new URI(type);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public SingleValueAttributeProxy(final URI type) {
        this.type = type;
    }

    public AttributeValue getInstance(final Node root) throws ParsingException {
        if (root == null) {
            return null;
        }
        // now we get the attribute value
        if (root.getNodeName().equals("AttributeValue")) {
            // now get the value
            final Node child = root.getFirstChild();

            if (child == null) {
                return new StringAttribute("");
            }
            //get the type of the node
            final short nodetype = child.getNodeType();

            // now see if we have (effectively) a simple string value
            if ((nodetype == Node.TEXT_NODE) || (nodetype == Node.CDATA_SECTION_NODE) ||
                                                (nodetype == Node.COMMENT_NODE))
            {
                return new StringAttribute(child.getNodeValue());
            }
            try {
                return AttributeFactory.getInstance().createValue(child, type);
            } catch (UnknownIdentifierException uie) {
                throw new ParsingException("Unknown AttributeId", uie);
            }
        }
        return null;
    }

    public AttributeValue getInstance(final String value) {
        return new SingleValueAttribute(type, value);
    }
}
