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


package org.constellation.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static org.constellation.metadata.CSWQueryable.DUBLIN_CORE_QUERYABLE;
import org.constellation.util.Util;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FindIdentifierTest {

    @Test
    public void finIdentifierTest() throws Exception {
        InputStream in = Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml");
        String result = getMetadataIdentifier(in, false);
        Assert.assertEquals("42292_5p_19900609195600", result);

        in = Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml");
        result = getMetadataIdentifier(in, false);
        Assert.assertEquals("000068C3-3B49-C671-89CF-10A39BB1B652", result);

        in = Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml");
        result = getMetadataIdentifier(in, false);
        Assert.assertEquals("urn:uuid:3e195454-42e8-11dd-8329-00e08157d076", result);
    }


    public static String getMetadataIdentifier(final InputStream metadataStream, final boolean reset) throws IOException, XMLStreamException {
        final List<String> identifierPaths = DUBLIN_CORE_QUERYABLE.get("identifier");
        final List<String[]> paths = new ArrayList<>();
        for (String identifierPath : identifierPaths) {
            identifierPath = identifierPath.substring(1); // remove the first '/'
            final String[] path = identifierPath.split("/");
            for (int i = 0; i < path.length; i++) {
                int sep = path[i].indexOf(':');
                if (sep != -1) {
                    path[i] = path[i].substring(sep + 1);
                }
            }
            paths.add(path);
        }

        if (reset) {
            metadataStream.mark(0);
        }
        final XMLInputFactory xif = XMLInputFactory.newFactory();
        final XMLStreamReader xsr = xif.createXMLStreamReader(metadataStream);
        int i = 0;
        while (xsr.hasNext()) {
            xsr.next();
            if (xsr.isStartElement()) {
                String nodeName = xsr.getLocalName();
                final List<String[]> toRemove = new ArrayList<>();
                for (String [] path : paths) {
                    String currentName = path[i];
                    if (i == path.length -2 && path[i + 1].startsWith("@")) {
                        final String value = xsr.getAttributeValue(null, path[i + 1].substring(1));
                        if (value != null) {
                            return value;
                        } else {
                            toRemove.add(path);
                        }
                    } else if (!currentName.equals("*") && !currentName.equals(nodeName)) {
                        toRemove.add(path);
                    } else if (i  == path.length -1) {
                        return xsr.getElementText();
                    }
                }
                paths.removeAll(toRemove);
                i++;
            }
        }

        xsr.close();
        if (reset) {
            metadataStream.reset();
        }
        return null;
    }
}
