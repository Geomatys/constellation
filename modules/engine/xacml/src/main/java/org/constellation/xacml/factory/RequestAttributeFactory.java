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

import java.net.InetAddress;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.constellation.xacml.XMLSchemaConstants;
import org.geotoolkit.xacml.xml.context.AttributeType;
import org.geotoolkit.xacml.xml.context.AttributeValueType;
 

/**
 *  Construct Commonly Used Attributes in Request Subject/Resource/Action
 *  and Environment sections
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 20, 2007 
 *  @version $Revision$
 */
public final class RequestAttributeFactory {

    private RequestAttributeFactory() {}
    
    public static AttributeType createAnyURIAttributeType(final String attrID,
            final String issuer, final URI value)
    {
        return getBareAttributeType(attrID, issuer, value.toString(), XMLSchemaConstants.DATATYPE_ANYURI);
    }

    public static AttributeType createBase64BinaryAttributeType(final String attrID,
            final String issuer, final byte[] value)
    {
        return getBareAttributeType(attrID, issuer, value, XMLSchemaConstants.DATATYPE_BASE64BINARY);
    }

    public static AttributeType createBooleanAttributeType(final String attrID,
            final String issuer, final boolean value)
    {
        return getBareAttributeType(attrID, issuer, value, XMLSchemaConstants.DATATYPE_BOOLEAN);
    }

    public static AttributeType createDateAttributeType(final String attrID, final String issuer) {
        return getBareAttributeType(attrID, issuer, getXMLDate(), XMLSchemaConstants.DATATYPE_DATE);
    }

    public static AttributeType createDateAttributeType(final String attrID, final String issuer,
            final XMLGregorianCalendar value)
    {
        return getBareAttributeType(attrID, issuer, value.toXMLFormat(), XMLSchemaConstants.DATATYPE_DATE);
    }

    public static AttributeType createDateTimeAttributeType(final String attrID, final String issuer) {
        return getBareAttributeType(attrID, issuer, getXMLDate(), XMLSchemaConstants.DATATYPE_DATE_TIME);
    }

    public static AttributeType createDateTimeAttributeType(final String attrID, final String issuer,
            final XMLGregorianCalendar value)
    {
        return getBareAttributeType(attrID, issuer, value.toXMLFormat(), XMLSchemaConstants.DATATYPE_DATE_TIME);
    }

    public static AttributeType createDNSNameAttributeType(final String attrID, final String issuer,
            final String hostname)
    {
        return getBareAttributeType(attrID, issuer, hostname, XMLSchemaConstants.DATATYPE_DNSNAME);
    }

    public static AttributeType createDoubleAttributeType(final String attrID, final String issuer,
            final double value)
    {
        return getBareAttributeType(attrID, issuer, Double.toString(value), XMLSchemaConstants.DATATYPE_DOUBLE);
    }

    public static AttributeType createEmailAttributeType(final String attrID, final String issuer,
            final String value)
    {
        return getBareAttributeType(attrID, issuer, value, XMLSchemaConstants.DATATYPE_RFC822NAME);
    }

    public static AttributeType createHexBinaryAttributeType(final String attrID, final String issuer,
            final byte[] value)
    {
        return getBareAttributeType(attrID, issuer, value, XMLSchemaConstants.DATATYPE_HEXBINARY);
    }

    public static AttributeType createIntegerAttributeType(final String attrID, final String issuer,
            final int value)
    {
        return getBareAttributeType(attrID, issuer, Integer.toString(value), XMLSchemaConstants.DATATYPE_INTEGER);
    }

    public static AttributeType createIPAddressAttributeType(final String attrID, final String issuer,
            final InetAddress address)
    {
        return getBareAttributeType(attrID, issuer, address, XMLSchemaConstants.DATATYPE_IPADDRESS);
    }

    public static AttributeType createStringAttributeType(final String attrID, final String issuer,
            final String value)
    {
        return getBareAttributeType(attrID, issuer, value, XMLSchemaConstants.DATATYPE_STRING);
    }

    public static AttributeType createTimeAttributeType(final String attrID, final String issuer)
    {
        return getBareAttributeType(attrID, issuer, getXMLDate(), XMLSchemaConstants.DATATYPE_TIME);
    }

    public static AttributeType createTimeAttributeType(final String attrID, final String issuer,
            final XMLGregorianCalendar value)
    {
        return getBareAttributeType(attrID, issuer, value.toXMLFormat(), XMLSchemaConstants.DATATYPE_TIME);
    }

    public static AttributeType createX509NameAttributeType(final String attrID, final String issuer,
            final X500Principal value)
    {
        return getBareAttributeType(attrID, issuer, value, XMLSchemaConstants.DATATYPE_X500NAME);
    }

    public static AttributeType createDayTimeDurationAttributeType(final String attrID, final String issuer,
            final Duration value)
    {
        return getBareAttributeType(attrID, issuer, value.toString(), XMLSchemaConstants.DATATYPE_DAYTIMEDURATION);
    }

    public static AttributeType createYearMonthDurationAttributeType(final String attrID, final String issuer,
            final Duration value)
    {
        return getBareAttributeType(attrID, issuer, value.toString(), XMLSchemaConstants.DATATYPE_YEARMONTHDURATION);
    }

    private static AttributeType getBareAttributeType(final String attrID, final String issuer,
            final Object value, final XMLSchemaConstants dataType)
    {
        final AttributeType attributeType = new AttributeType();
        attributeType.setAttributeId(attrID);
        attributeType.setDataType(dataType.key);
        if (issuer != null) {
            attributeType.setIssuer(issuer);
        }
        final AttributeValueType avt = new AttributeValueType();
        avt.getContent().add(value);
        attributeType.getAttributeValue().add(avt);
        return attributeType;
    }

    private static String getXMLDate() {
        final DatatypeFactory dtf;
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        final XMLGregorianCalendar value = dtf.newXMLGregorianCalendar((GregorianCalendar) Calendar.getInstance());
        return value.toXMLFormat();
    }
}
