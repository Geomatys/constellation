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
package org.constellation.process.wmts;

import java.io.File;
import java.net.MalformedURLException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.LayerContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.process.AbstractProcessTest;
import org.constellation.wmts.ws.DefaultWMTSWorker;
import org.constellation.ws.WSEngine;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class WMTSProcessTest extends AbstractProcessTest {

    protected static File configDirectory;

    public WMTSProcessTest(final String str) {
        super(str);
    }

    @BeforeClass
    public static void initFolder() throws MalformedURLException {

        configDirectory = ConfigDirectory.getConfigDirectory();
        WSEngine.registerService("WMTS", "REST");
        createInstance("instance1");
        createInstance("instance2");
        createInstance("instance3");
        createInstance("instance4");

    }

    @AfterClass
    public static void destroyFolder() {
        deleteInstance("instance1");
        deleteInstance("instance2");
        deleteInstance("instance3");
        deleteInstance("instance4");
        WSEngine.destroyInstances("WMTS");
    }

    protected static void createInstance(String identifier) {
        final File wmts = new File(configDirectory, "WMTS");
        final File instance = new File(wmts, identifier);
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

    protected static void deleteInstance(String identifier) {
        final File wmts = new File(configDirectory, "WMTS");
        final File instance = new File(wmts, identifier);
        FileUtilities.deleteDirectory(instance);
    }

    protected static void startInstance(String identifier) {
        final File wmts = new File(configDirectory, "WMTS");
        final File instance = new File(wmts, identifier);
        final DefaultWMTSWorker worker = new DefaultWMTSWorker(identifier, instance);
        if (worker != null) {
            WSEngine.addServiceInstance("WMTS", identifier, worker);
        }
    }
}
