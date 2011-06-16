/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.sos.ws;

import javax.xml.namespace.QName;
import net.jcip.annotations.Immutable;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public final class SOSConstants {

    private SOSConstants() {}

    public static final String SOS = "SOS";
    public static final String VERSION = "1.0.0";
    public static final String SERVICE = "service";
    public static final String ALL = "All";
    public static final String OFFERING = "offering";
    public static final String EVENT_TIME = "eventTime";
    public static final String PROCEDURE = "procedure";
    public static final String OBSERVATION_TEMPLATE = "observationTemplate";
    public static final String RESPONSE_MODE = "responseMode";
    public static final String RESPONSE_FORMAT = "responseFormat";
    public static final String NOT_SUPPORTED = "This operation is not take in charge by the Web Service";
    public static final String SENSORML_100_FORMAT = "text/xml;subtype=\"sensorML/1.0.0\"";
    public static final String SENSORML_101_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";

    /**
     * The base Qname for complex observation.
     */
    public static final QName OBSERVATION_QNAME = new QName("http://www.opengis.net/om/1.0", "Observation", "om");

    /**
     * The base Qname for measurement observation.
     */
    public static final QName MEASUREMENT_QNAME = new QName("http://www.opengis.net/om/1.0", "Measurement", "om");
}

