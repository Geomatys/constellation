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
package net.seagis.xacml;

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
public class ExtendedAttributeFactory extends BaseAttributeFactory {

    private static ExtendedAttributeFactory instance = null;
    private static final Map<String, AttributeProxy> supportedDatatypes = new HashMap<String, AttributeProxy>();

    private ExtendedAttributeFactory() {
        super(supportedDatatypes);

        // the 1.x datatypes
        supportedDatatypes.put(BooleanAttribute.identifier,           new BooleanAttributeProxy());
        supportedDatatypes.put(StringAttribute.identifier,            new StringAttributeProxy());
        supportedDatatypes.put(DateAttribute.identifier,              new DateAttributeProxy());
        supportedDatatypes.put(TimeAttribute.identifier,              new TimeAttributeProxy());
        supportedDatatypes.put(DateTimeAttribute.identifier,          new DateTimeAttributeProxy());
        supportedDatatypes.put(DayTimeDurationAttribute.identifier,   new DayTimeDurationAttributeProxy());
        supportedDatatypes.put(YearMonthDurationAttribute.identifier, new YearMonthDurationAttributeProxy());
        supportedDatatypes.put(DoubleAttribute.identifier,            new DoubleAttributeProxy());
        supportedDatatypes.put(IntegerAttribute.identifier,           new IntegerAttributeProxy());
        supportedDatatypes.put(AnyURIAttribute.identifier,            new AnyURIAttributeProxy());
        supportedDatatypes.put(HexBinaryAttribute.identifier,         new HexBinaryAttributeProxy());
        supportedDatatypes.put(Base64BinaryAttribute.identifier,      new Base64BinaryAttributeProxy());
        supportedDatatypes.put(X500NameAttribute.identifier,          new X500NameAttributeProxy());
        supportedDatatypes.put(RFC822NameAttribute.identifier,        new RFC822NameAttributeProxy());

        // the 2.0 datatypes
        supportedDatatypes.put(DNSNameAttribute.identifier, new DNSNameAttributeProxy());
        supportedDatatypes.put(IPAddressAttribute.identifier, new IPAddressAttributeProxy());
    }

    @Override
    public void addDatatype(final String id, final AttributeProxy proxy) {
        supportedDatatypes.put(id, proxy);
    }

    @Override
    public AttributeValue createValue(final URI dataType, final String value)
            throws UnknownIdentifierException, ParsingException
    {
        try {
            return getProxy(dataType.toString()).getInstance(value);
        } catch (UnknownIdentifierException e) {
            throw e;
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
        } catch (UnknownIdentifierException e) {
            throw e;
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

    public static ExtendedAttributeFactory getFactory() {
        if (instance == null) {
            instance = new ExtendedAttributeFactory();
        }
        return instance;
    }

    private AttributeProxy getProxy(final String type) throws UnknownIdentifierException {
        final AttributeProxy proxy = (AttributeProxy) supportedDatatypes.get(type);
        if (proxy == null) {
            throw new UnknownIdentifierException("proxy null for " + type);
        }
        return proxy;
    }
}
