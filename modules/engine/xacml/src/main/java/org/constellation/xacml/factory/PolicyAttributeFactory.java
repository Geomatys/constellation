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
import org.geotoolkit.xacml.xml.policy.AttributeValueType;
import org.geotoolkit.xacml.xml.policy.AttributeDesignatorType;
import org.geotoolkit.xacml.xml.policy.SubjectAttributeDesignatorType;


/**
 *  Static class that has methods to create AttributeValueTypes
 *  for constructing policies
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 20, 2007 
 *  @version $Revision$
 */
public final class PolicyAttributeFactory {

    private PolicyAttributeFactory() {}

    public static AttributeValueType createAnyURIAttributeType(final URI value) {
        return getBareAttributeValueType(value.toString(), XMLSchemaConstants.DATATYPE_ANYURI);
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
        return getBareAttributeValueType(Double.toString(value), XMLSchemaConstants.DATATYPE_DOUBLE);
    }

    public static AttributeValueType createEmailAttributeType(final String value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_RFC822NAME);
    }

    public static AttributeValueType createHexBinaryAttributeType(final byte[] value) {
        return getBareAttributeValueType(value, XMLSchemaConstants.DATATYPE_HEXBINARY);
    }

    public static AttributeValueType createIntegerAttributeType(final int value) {
        return getBareAttributeValueType(Integer.toString(value), XMLSchemaConstants.DATATYPE_INTEGER);
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
        final AttributeDesignatorType adt = createAttributeDesignatorType(id, dataType, issuer, mustBePresent);
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
