/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.metadata;

import net.jcip.annotations.Immutable;


/**
 * CSW constants.
 *
 * @version $Id$
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public abstract class CSWConstants {

    /**
     * Request parameters.
     */
    public static final String CSW_202_VERSION = "2.0.2";
    public static final String CSW = "CSW";
    public static final String VERSION = "VERSION";
    public static final String SERVICE = "service";
    public static final String OUTPUT_SCHEMA = "outputSchema";
    public static final String TYPENAMES = "TypeNames";
    public static final String FILTER_CAPABILITIES = "Filter_Capabilities";
    public static final String PARAMETERNAME = "parameterName";
    public static final String TRANSACTION_TYPE = "TransactionType";
    public static final String SOURCE = "Source";
    public static final String ALL = "All";
    public static final String NAMESPACE = "namespace";
    public static final String XML_EXT = ".xml";

    // TODO those 3 namespace must move to geotk Namespace class
    public static final String EBRIM_25 = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5";
    public static final String EBRIM_30 = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

    /**
     * Error message
     */

    public static final String NOT_EXIST = " does not exist";

    public static final String MALFORMED = " is malformed";

    private CSWConstants() {}

}
