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
