/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.xacml;

import java.io.File;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class XacmlAnnotationsTest {
     private Logger logger = Logger.getLogger("org.constellation.xacml");

    @Test
    public void testPolicyAnnotation() throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance("org.constellation.xacml.policy");
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        File f = new File("/home/cedr/Bureau/GeoXACML/example/policy.xml");
        if (f.exists())  {
            final Object policy = unmarshaller.unmarshal(f);
            assertNotNull(policy);
        } else {
            logger.info("unable to find file skipping test");
        }
    }

    @Test
    public void testRequestAnnotation() throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance("org.constellation.xacml.policy:org.constellation.xacml.context");
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        File f = new File("/home/cedr/Bureau/GeoXACML/example/request.xml");
        if (f.exists()) {
            final Object request = unmarshaller.unmarshal(f);
            assertNotNull(request);
        } else {
            logger.info("unable to find file skipping test");
        }
    }

    @Test
    public void testResponseAnnotation() throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance("org.constellation.xacml.policy:org.constellation.xacml.context");
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        File f = new File("/home/cedr/Bureau/GeoXACML/example/response.xml");
        if (f.exists()) {
            final Object response = unmarshaller.unmarshal(f);
            assertNotNull(response);
        } else {
            logger.info("unable to find file skipping test");
        }
    }
}
