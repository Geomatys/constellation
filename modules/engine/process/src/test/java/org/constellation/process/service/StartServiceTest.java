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

import org.constellation.process.ConstellationProcessFactory;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public abstract class StartServiceTest extends ServiceProcessTest {

    public StartServiceTest(final String serviceName, final Class workerClass) {
        super(StartServiceDescriptor.NAME, serviceName, workerClass);
    }


    @Test
    public void testStart() throws NoSuchIdentifierException, ProcessException {

        createInstance("startInstance1");
        try {
            final int initSize = WSEngine.getInstanceSize(serviceName);
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue("startInstance1");
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(WSEngine.getInstanceSize(serviceName) == initSize+1);
            assertTrue(WSEngine.serviceInstanceExist(serviceName, "startInstance1"));
        } finally {
            deleteInstance(serviceBusiness, "startInstance1");
        }
    }

    @Test
    public void testFailStart() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue("startInstance5");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
}
