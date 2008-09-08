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
package net.seagis.xacml;


/**
 *  Defines the constants for XML Schema
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 20, 2007 
 *  @version $Revision$
 */
public enum XMLSchemaConstants {

    DATATYPE_ANYURI("http://www.w3.org/2001/XMLSchema#anyURI"),
    DATATYPE_BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary"),
    DATATYPE_BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean"),
    DATATYPE_DATE("http://www.w3.org/2001/XMLSchema#date"),
    DATATYPE_DATE_TIME("http://www.w3.org/2001/XMLSchema#dateTime"),
    DATATYPE_DOUBLE("http://www.w3.org/2001/XMLSchema#double"),
    DATATYPE_HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary"),
    DATATYPE_INTEGER("http://www.w3.org/2001/XMLSchema#integer"),
    DATATYPE_STRING("http://www.w3.org/2001/XMLSchema#string"),
    DATATYPE_TIME("http://www.w3.org/2001/XMLSchema#time"),
    DATATYPE_DAYTIMEDURATION("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"),
    DATATYPE_YEARMONTHDURATION("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"),
    DATATYPE_DNSNAME("urn:oasis:names:tc:xacml:2.0:data-type:dnsName"),
    DATATYPE_IPADDRESS("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress"),
    DATATYPE_RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name"),
    DATATYPE_X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name");
    
    
    public final String key;

    private XMLSchemaConstants(final String key) {
        this.key = key;
    }
}
