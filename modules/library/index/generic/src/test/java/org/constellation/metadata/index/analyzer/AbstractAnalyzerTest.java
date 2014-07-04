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

package org.constellation.metadata.index.analyzer;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.lucene.index.LuceneIndexSearcher;
import org.opengis.filter.FilterFactory2;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
* @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractAnalyzerTest {

    protected static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));

    protected static final Logger logger = Logging.getLogger("org.constellation.metadata.index.generic");

    protected static LuceneIndexSearcher indexSearcher;

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
        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta7.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta14.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }
        CSWMarshallerPool.getInstance().recycle(unmarshaller);
        return result;
    }
}
