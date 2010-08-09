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

package org.constellation.metadata.index.generic;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.CSWClassesContext;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
* @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractAnalyzerTest {

    public static List<Object> fillTestData() throws JAXBException {
        List<Object> result       = new ArrayList<Object>();
        MarshallerPool pool       = CSWClassesContext.getMarshallerPool();
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        Object obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata");
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta2.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta3.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta4.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta5.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta6.xml"));
        if (obj instanceof DefaultMetadata) {
            result.add((DefaultMetadata) obj);
        } else {
            throw new IllegalArgumentException("resource file must be DefaultMetadata:" + obj);
        }
        pool.release(unmarshaller);
        return result;
    }
}
