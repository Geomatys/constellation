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
package org.constellation.process.service;

import org.constellation.configuration.LayerContext;
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

            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(checkInstanceExist("createInstance1"));
        } finally {
            deleteInstance(serviceBusiness, "createInstance1");
        }
    }

    @Test
    public void testCreateMapServiceWithContext() throws ProcessException, NoSuchIdentifierException {

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);

            final LayerContext conf = new LayerContext();

            //WMS
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue("createInstance11");
            in.parameter(CreateServiceDescriptor.CONFIG_NAME).setValue(conf);


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(checkInstanceExist("createInstance11"));
            assertEquals(conf, getConfig("createInstance11"));
        } finally {
            deleteInstance(serviceBusiness, "createInstance11");
        }
    }
    
    @Test
    public void testGetMapServiceWithContext() throws ProcessException, NoSuchIdentifierException {

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);

            final LayerContext conf = new LayerContext();
            conf.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());
            createCustomInstance("createInstance15", conf);
            
            //create
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue("createInstance15");


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            ParameterValueGroup out = proc.call();

            final LayerContext outContext = (LayerContext)out.parameter(CreateServiceDescriptor.OUT_CONFIG_NAME).getValue();

            assertTrue(checkInstanceExist("createInstance15"));
            assertEquals(conf, outContext);
        } finally {
            deleteInstance(serviceBusiness, "createInstance15");
        }
    }

    @Test
    public void testCreateEmptyIdentifier() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue("");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
}
