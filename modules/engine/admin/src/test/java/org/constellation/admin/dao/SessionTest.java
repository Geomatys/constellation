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
            session.writeServiceMetadata("serv1", ServiceDef.Specification.WFS, sr, "fre");

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
