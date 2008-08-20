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
package net.seagis.xacml.factory;

import java.net.InetAddress;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import net.seagis.xacml.policy.AttributeDesignatorType;
import net.seagis.xacml.policy.AttributeValueType;
import net.seagis.xacml.policy.SubjectAttributeDesignatorType;
import net.seagis.xacml.XMLSchemaConstants;


/**
 *  Static class that has methods to create AttributeValueTypes
 *  for constructing policies
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 20, 2007 
 *  @version $Revision$
 */
public class PolicyAttributeFactory {

    public static AttributeValueType createAnyURIAttributeType(final URI value) {
        return getBareAttributeValueType("" + value, XMLSchemaConstants.DATATYPE_ANYURI);
    }

    public static AttributeValueType createBase64BinaryAttributeType(final byte[] value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_BASE64BINARY);
    }

    public static AttributeValueType createBooleanAttributeType(final boolean value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_BOOLEAN);
    }

    public static AttributeValueType createDateAttributeType() {
        return getBareAttributeValueType(getXMLDate(), XMLSchemaConstants.DATATYPE_DATE);
    }

    public static AttributeValueType createDateAttributeType(final XMLGregorianCalendar value) {
        return getBareAttributeValueType(value.toXMLFormat(), XMLSchemaConstants.DATATYPE_DATE);
    }

    public static AttributeValueType createDateTimeAttributeType() {
        return getBareAttributeValueType(getXMLDate(), XMLSchemaConstants.DATATYPE_DATE_TIME);
    }

    public static AttributeValueType createDateTimeAttributeType(final XMLGregorianCalendar value) {
        return getBareAttributeValueType(value.toXMLFormat(), XMLSchemaConstants.DATATYPE_DATE_TIME);
    }

    public static AttributeValueType createDNSNameAttributeType(final String hostname) {
        return getBareAttributeValueType(hostname, XMLSchemaConstants.DATATYPE_DNSNAME);
    }

    public static AttributeValueType createDoubleAttributeType(final double value) {
        return getBareAttributeValueType("" + value, XMLSchemaConstants.DATATYPE_DOUBLE);
    }

    public static AttributeValueType createEmailAttributeType(final String value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_RFC822NAME);
    }

    public static AttributeValueType createHexBinaryAttributeType(final byte[] value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_HEXBINARY);
    }

    public static AttributeValueType createIntegerAttributeType(final int value) {
        return getBareAttributeValueType("" + value, XMLSchemaConstants.DATATYPE_INTEGER);
    }

    public static AttributeValueType createIPAddressAttributeType(final InetAddress address) {
        return getBareAttributeValueType(address, XMLSchemaConstants.DATATYPE_IPADDRESS);
    }

    public static AttributeValueType createStringAttributeType(final String value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_STRING);
    }

    public static AttributeValueType createTimeAttributeType() {
        return getBareAttributeValueType(getXMLDate(), XMLSchemaConstants.DATATYPE_TIME);
    }

    public static AttributeValueType createTimeAttributeType(final XMLGregorianCalendar value) {
        return getBareAttributeValueType(value.toXMLFormat(), XMLSchemaConstants.DATATYPE_TIME);
    }

    public static AttributeValueType createX509NameAttributeType(final X500Principal value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_X500NAME);
    }

    public static AttributeValueType createDayTimeDurationAttributeType(final Duration value) {
        return getBareAttributeValueType(value.toString(), XMLSchemaConstants.DATATYPE_DAYTIMEDURATION);
    }

    public static AttributeValueType createYearMonthDurationAttributeType(final Duration value) {
        return getBareAttributeValueType(value.toString(), XMLSchemaConstants.DATATYPE_YEARMONTHDURATION);
    }

    public static AttributeDesignatorType createAttributeDesignatorType(final String id,
            final String dataType, final String issuer, final boolean mustBePresent)
    {
        final AttributeDesignatorType adt = new AttributeDesignatorType();
        adt.setAttributeId(id);
        adt.setDataType(dataType);
        if (issuer != null) {
            adt.setIssuer(issuer);
        }
        adt.setMustBePresent(mustBePresent);
        return adt;
    }

    public static SubjectAttributeDesignatorType createSubjectAttributeDesignatorType(
            final String id, final String dataType, final String issuer,
            final boolean mustBePresent, final String subjectCategory)
    {
        AttributeDesignatorType adt = createAttributeDesignatorType(id, dataType, issuer, mustBePresent);
        final SubjectAttributeDesignatorType sadt = new SubjectAttributeDesignatorType(adt, subjectCategory);
                
        return sadt;
    }

    private static AttributeValueType getBareAttributeValueType(final Object value, 
            final XMLSchemaConstants dataType)
    {
        final AttributeValueType avt = new AttributeValueType();
        avt.setDataType(dataType.key);
        avt.getContent().add(value);
        return avt;
    }

    private static String getXMLDate() {
        final DatatypeFactory dtf;
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        final XMLGregorianCalendar value = dtf.newXMLGregorianCalendar((GregorianCalendar) Calendar.getInstance());
        return (value != null) ? value.toXMLFormat() : null;
    }
}
