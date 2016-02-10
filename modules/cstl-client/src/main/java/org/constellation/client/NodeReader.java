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

package org.constellation.client;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
public class NodeReader implements MessageBodyReader<Node>{

    @Override
    public boolean isReadable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return type.isAssignableFrom(Node.class);
    }

    @Override
    public Node readFrom(Class<Node> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, String> mm, InputStream in) throws IOException, WebApplicationException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document document = docBuilder.parse(in);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new IOException(ex);
        }
    }

}
