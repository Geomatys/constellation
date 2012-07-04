/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process;

import java.io.File;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.configuration.LayerContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.process.service.RestartServiceTest;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class RestartWMSServiceTest extends RestartServiceTest {

    public RestartWMSServiceTest() {
        super("WMS", DefaultWMSWorker.class);
    }

    /** {@inheritDoc} */
    @Override
    protected void createInstance(final String identifier) {
        final File wms = new File(configDirectory, serviceName);
        final File instance = new File(wms, identifier);
        instance.mkdir();

        final File configFile = new File(instance, "layerContext.xml");
        final LayerContext configuration = new LayerContext();
        Marshaller marshaller = null;
        try {
            marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(configuration, configFile);

        } catch (JAXBException ex) {
            //
        } finally {
            if (marshaller != null) {
                GenericDatabaseMarshallerPool.getInstance().release(marshaller);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkInstanceExist(final String identifier) {

        final File instanceDir = new File(configDirectory.getAbsolutePath() + "/" + serviceName, identifier);
        if (instanceDir.exists() && instanceDir.isDirectory()) {
            final File configFile = new File(instanceDir, "layerContext.xml");
            return configFile.exists();
        } else {
            return false;
        }
    }
}
