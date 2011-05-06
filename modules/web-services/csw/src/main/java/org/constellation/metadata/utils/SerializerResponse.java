/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.metadata.utils;

import org.constellation.jaxb.CstlXMLSerializer;
import org.geotoolkit.csw.xml.CSWResponse;

/**
 * A utility model classe use to pass a XMLSerializer to the jersey messageBodeWriter,
 * in charge of the marshalling of the CSW response
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class SerializerResponse implements CSWResponse {

    private final CstlXMLSerializer serializer;

    private final CSWResponse response;

    public SerializerResponse(CSWResponse response, CstlXMLSerializer serializer) {
        this.response   = response;
        this.serializer = serializer;
    }

    /**
     * @return the serializer
     */
    public CstlXMLSerializer getSerializer() {
        return serializer;
    }

    /**
     * @return the response
     */
    public CSWResponse getResponse() {
        return response;
    }
}
