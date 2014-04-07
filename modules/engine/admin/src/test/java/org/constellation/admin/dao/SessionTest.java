/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.admin.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.ServiceDef;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.configuration.LayerContext;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SessionTest {

    @BeforeClass
    public static void startup() throws JAXBException, IOException {
        ConfigurationEngine.setupTestEnvironement("SessionTest");
    }

    @AfterClass
    public static void shutDown() throws JAXBException {
        ConfigurationEngine.shutdownTestEnvironement("SessionTest");
    }

    @Test
    public void testWriteServiceRecord() throws Exception {

        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final LayerContext ctx = new LayerContext();
            final Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            StringWriter sw = new StringWriter();
            m.marshal(ctx, sw);
            StringReader sr = new StringReader(sw.toString());
            final ServiceRecord rec = session.writeService("serv1", ServiceDef.Specification.WFS, sr, null);
            assertNotNull(rec);

            final Service serv = new Service();
            serv.setName("spécial");
            sw = new StringWriter();
            m.marshal(serv, sw);
            sr = new StringReader(sw.toString());
            final StringReader srIso = new StringReader("<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"/>");
            session.writeServiceMetadata("serv1", ServiceDef.Specification.WFS, sr, srIso, "fre");

            GenericDatabaseMarshallerPool.getInstance().recycle(m);

            final InputStream is = session.readServiceMetadata(rec.id, "fre");
            final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final Object obj     = u.unmarshal(is);

            assertTrue(obj instanceof Service);
            
            final Service expServ =  (Service) obj;
            assertEquals(expServ.getName(), serv.getName());

            GenericDatabaseMarshallerPool.getInstance().recycle(u);

        } catch (SQLException ex) {
            fail();
        } finally {
            if (session != null) session.close();
        }
    }

}
