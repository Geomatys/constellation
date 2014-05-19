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
package org.constellation.xacml;


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
