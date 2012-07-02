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
package org.constellation.process.service;

import java.io.File;
import java.net.MalformedURLException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.LayerContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.process.AbstractProcessTest;
import org.constellation.ws.WSEngine;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractMapServiceTest extends AbstractProcessTest {

    protected static File configDirectory;
    public AbstractMapServiceTest (final String str) {
        super(str);
    }

     @BeforeClass
    public static void initFolder() throws MalformedURLException {

        configDirectory = new File("ProcessMapTest");

        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }

        configDirectory.mkdir();
        final File wms = new File(configDirectory, "WMS");
        final File wmts = new File(configDirectory, "WMTS");
        final File wfs = new File(configDirectory, "WFS");
        final File sos = new File(configDirectory, "SOS");

        wms.mkdir();
        wmts.mkdir();
        wfs.mkdir();
        sos.mkdir();

        ConfigDirectory.setConfigDirectory(configDirectory);

        WSEngine.registerService("WMS", "REST");
        WSEngine.registerService("WMTS", "REST");
        WSEngine.registerService("WFS", "REST");
    }

    @AfterClass
    public static void destroyFolder() {
        FileUtilities.deleteDirectory(configDirectory);
        WSEngine.destroyInstances("WMS");
        WSEngine.destroyInstances("WMTS");
        WSEngine.destroyInstances("WFS");
    }

    /**
     * Create a default instance of service.
     * @param identifier
     */
    protected void createDefaultInstance(final String serviceName, final String identifier) {
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

    /**
     * Create a custom instance.
     * @param serviceName
     * @param identifier
     * @param context 
     */
    protected void createInstance(final String serviceName, final String identifier, LayerContext context) {
        final File wms = new File(configDirectory, serviceName);
        final File instance = new File(wms, identifier);
        instance.mkdir();

        final File configFile = new File(instance, "layerContext.xml");
        final LayerContext configuration = context;
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

    /**
     * Create a default instance of service.
     * @param identifier
     */
    protected void deleteInstance(final String serviceName, final String identifier) {
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

    /**
     * Return the configuration of a service instance.
     * @param identifier
     * @return
     */
    protected  LayerContext getConfig(final String identifier) {
        final File wms = new File(configDirectory, "WMS");
        final File instance = new File(wms, identifier);
        final File configFile = new File(instance, "layerContext.xml");

        Unmarshaller unmarshaller = null;
        LayerContext  context = null;
        try {
            unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            context = (LayerContext) unmarshaller.unmarshal(configFile);
        } catch (JAXBException ex) {
            //
        } finally {
            if (unmarshaller != null) {
                GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
            }
        }
        return context;
    }

    /**
     * Check if an service instance exist.
     * @param serviceName
     * @param identifier
     * @return
     */
    protected boolean checkInsanceExist(final String serviceName, final String identifier) {

        final File instanceDir = new File(configDirectory.getAbsolutePath() + "/" + serviceName, identifier);
        if (instanceDir.exists() && instanceDir.isDirectory()) {
            final File configFile = new File(instanceDir, "layerContext.xml");
            return configFile.exists();
        } else {
            return false;
        }
    }
}
