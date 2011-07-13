/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

package org.constellation.metadata.index.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.util.logging.Logging;
import org.opengis.filter.FilterFactory2;

/**
 *
* @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractAnalyzerTest {

    protected static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));

    protected static final Logger logger = Logging.getLogger("org.constellation.metadata.index.generic");

    protected static AbstractIndexSearcher indexSearcher;

    public static List<Object> fillTestData() throws JAXBException {
        List<Object> result       = new ArrayList<Object>();
        Unmarshaller unmarshaller = CSWMarshallerPool.getInstance().acquireUnmarshaller();

        Object obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata");
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta3.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta4.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta5.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta6.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }
        CSWMarshallerPool.getInstance().release(unmarshaller);
        return result;
    }
}
