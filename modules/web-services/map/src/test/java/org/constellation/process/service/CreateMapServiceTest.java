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

import java.util.ArrayList;
import java.util.List;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
import static org.junit.Assert.*;

/**
 *
 * @author Quentin Boileau (Geometys).
 */
public abstract class CreateMapServiceTest extends AbstractMapServiceTest {

    public CreateMapServiceTest(final String serviceName, final Class workerClass) {
        super(CreateServiceDescriptor.NAME, serviceName, workerClass);
    }

    @Test
    public void testCreateWMS() throws ProcessException, NoSuchIdentifierException {

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);

            //WMS
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue("createInstance1");
            in.parameter(CreateServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);

            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(checkInstanceExist("createInstance1"));
        } finally {
            deleteInstance("createInstance1");
        }
    }

    @Test
    public void testCreateMapServiceWithContext() throws ProcessException, NoSuchIdentifierException {

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);

            final List<Source> sources = new ArrayList<>();
            sources.add(new Source("source1", Boolean.TRUE, null, null));
            final Layers layers = new Layers(sources);
            final LayerContext conf = new LayerContext(layers);

            //WMS
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue("createInstance11");
            in.parameter(CreateServiceDescriptor.CONFIG_NAME).setValue(conf);
            in.parameter(CreateServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(checkInstanceExist("createInstance11"));
            assertEquals(conf, getConfig("createInstance11"));
        } finally {
            deleteInstance("createInstance11");
        }
    }
    
    @Test
    public void testGetMapServiceWithContext() throws ProcessException, NoSuchIdentifierException {

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);

            final List<Source> sources = new ArrayList<>();
            sources.add(new Source("source1", Boolean.TRUE, null, null));
            final Layers layers = new Layers(sources);
            final LayerContext conf = new LayerContext(layers);
            conf.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());
            createCustomInstance("createInstance15", conf);
            //create
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue("createInstance15");
            in.parameter(CreateServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            ParameterValueGroup out = proc.call();

            final LayerContext outContext = (LayerContext)out.parameter(CreateServiceDescriptor.OUT_CONFIG_NAME).getValue();

            assertTrue(checkInstanceExist("createInstance15"));
            assertEquals(conf, outContext);
        } finally {
            deleteInstance("createInstance15");
        }
    }

    @Test
    public void testCreateEmptyIdentifier() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue("");
        in.parameter(CreateServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
}
