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
package org.constellation.xacml.factory;

import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeProxy;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.Base64BinaryAttribute;
import com.sun.xacml.attr.BaseAttributeFactory;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.DNSNameAttribute;
import com.sun.xacml.attr.DateAttribute;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.DayTimeDurationAttribute;
import com.sun.xacml.attr.DoubleAttribute;
import com.sun.xacml.attr.HexBinaryAttribute;
import com.sun.xacml.attr.IPAddressAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.TimeAttribute;
import com.sun.xacml.attr.X500NameAttribute;
import com.sun.xacml.attr.YearMonthDurationAttribute;
import com.sun.xacml.attr.proxy.AnyURIAttributeProxy;
import com.sun.xacml.attr.proxy.Base64BinaryAttributeProxy;
import com.sun.xacml.attr.proxy.BooleanAttributeProxy;
import com.sun.xacml.attr.proxy.DNSNameAttributeProxy;
import com.sun.xacml.attr.proxy.DateAttributeProxy;
import com.sun.xacml.attr.proxy.DateTimeAttributeProxy;
import com.sun.xacml.attr.proxy.DayTimeDurationAttributeProxy;
import com.sun.xacml.attr.proxy.DoubleAttributeProxy;
import com.sun.xacml.attr.proxy.HexBinaryAttributeProxy;
import com.sun.xacml.attr.proxy.IPAddressAttributeProxy;
import com.sun.xacml.attr.proxy.IntegerAttributeProxy;
import com.sun.xacml.attr.proxy.RFC822NameAttributeProxy;
import com.sun.xacml.attr.proxy.StringAttributeProxy;
import com.sun.xacml.attr.proxy.TimeAttributeProxy;
import com.sun.xacml.attr.proxy.X500NameAttributeProxy;
import com.sun.xacml.attr.proxy.YearMonthDurationAttributeProxy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;


/**
 *  Extendible Attribute factory
 *  @author Anil.Saldhana@redhat.com
 *  @since  Mar 28, 2008 
 *  @version $Revision$
 */
public final class ExtendedAttributeFactory extends BaseAttributeFactory {

    private static ExtendedAttributeFactory instance = null;
    private static final Map<String, AttributeProxy> SUPPORTED_DATA_TYPES = new HashMap<String, AttributeProxy>();

    private ExtendedAttributeFactory() {
        super(SUPPORTED_DATA_TYPES);

        // the 1.x datatypes
        SUPPORTED_DATA_TYPES.put(BooleanAttribute.identifier,           new BooleanAttributeProxy());
        SUPPORTED_DATA_TYPES.put(StringAttribute.identifier,            new StringAttributeProxy());
        SUPPORTED_DATA_TYPES.put(DateAttribute.identifier,              new DateAttributeProxy());
        SUPPORTED_DATA_TYPES.put(TimeAttribute.identifier,              new TimeAttributeProxy());
        SUPPORTED_DATA_TYPES.put(DateTimeAttribute.identifier,          new DateTimeAttributeProxy());
        SUPPORTED_DATA_TYPES.put(DayTimeDurationAttribute.identifier,   new DayTimeDurationAttributeProxy());
        SUPPORTED_DATA_TYPES.put(YearMonthDurationAttribute.identifier, new YearMonthDurationAttributeProxy());
        SUPPORTED_DATA_TYPES.put(DoubleAttribute.identifier,            new DoubleAttributeProxy());
        SUPPORTED_DATA_TYPES.put(IntegerAttribute.identifier,           new IntegerAttributeProxy());
        SUPPORTED_DATA_TYPES.put(AnyURIAttribute.identifier,            new AnyURIAttributeProxy());
        SUPPORTED_DATA_TYPES.put(HexBinaryAttribute.identifier,         new HexBinaryAttributeProxy());
        SUPPORTED_DATA_TYPES.put(Base64BinaryAttribute.identifier,      new Base64BinaryAttributeProxy());
        SUPPORTED_DATA_TYPES.put(X500NameAttribute.identifier,          new X500NameAttributeProxy());
        SUPPORTED_DATA_TYPES.put(RFC822NameAttribute.identifier,        new RFC822NameAttributeProxy());

        // the 2.0 datatypes
        SUPPORTED_DATA_TYPES.put(DNSNameAttribute.identifier, new DNSNameAttributeProxy());
        SUPPORTED_DATA_TYPES.put(IPAddressAttribute.identifier, new IPAddressAttributeProxy());
    }

    @Override
    public void addDatatype(final String id, final AttributeProxy proxy) {
        SUPPORTED_DATA_TYPES.put(id, proxy);
    }

    @Override
    public AttributeValue createValue(final URI dataType, final String value)
            throws UnknownIdentifierException, ParsingException
    {
        try {
            return getProxy(dataType.toString()).getInstance(value);
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }

    @Override
    public AttributeValue createValue(final Node root, final String type)
            throws UnknownIdentifierException, ParsingException
    {
        try {
            return getProxy(type).getInstance(root);
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }

    @Override
    public AttributeValue createValue(final Node root, final URI dataType)
            throws UnknownIdentifierException, ParsingException
    {
        return createValue(root, dataType.toString());
    }

    public static synchronized ExtendedAttributeFactory getFactory() {
        if (instance == null) {
            instance = new ExtendedAttributeFactory();
        }
        return instance;
    }

    private AttributeProxy getProxy(final String type) throws UnknownIdentifierException {
        final AttributeProxy proxy = (AttributeProxy) SUPPORTED_DATA_TYPES.get(type);
        if (proxy == null) {
            throw new UnknownIdentifierException("proxy null for " + type);
        }
        return proxy;
    }
}
