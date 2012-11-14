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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.LayerContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractMapServiceTest extends ServiceProcessTest {


    public AbstractMapServiceTest (final String str, final String serviceName, final Class workerClass) {
        super(str, serviceName, workerClass);
    }

    /** {@inheritDoc} */
    @Override
    protected void createInstance(String identifier) {
        createCustomInstance(identifier, null);
    }

    protected void createInstance(final String identifier, LayerContext context) {
        final File wms = new File(configDirectory, serviceName);
        final File instance = new File(wms, identifier);
        instance.mkdir();

        final File configFile = new File(instance, "layerContext.xml");
        final LayerContext configuration = context != null ? context : new LayerContext();
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

    /**
     * Create a custom instance.
     * @param serviceName
     * @param identifier
     * @param context
     */
    protected void createCustomInstance(final String identifier, LayerContext context) {
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
     * Return the configuration of a service instance.
     * @param identifier
     * @return
     */
    protected  LayerContext getConfig(final String identifier) {
        final File wms = new File(configDirectory, serviceName);
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

}
