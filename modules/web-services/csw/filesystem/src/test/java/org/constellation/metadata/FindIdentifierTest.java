/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
