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
public abstract class DeleteServiceTest extends ServiceProcessTest {

    public DeleteServiceTest(final String serviceName, final Class workerClass) {
        super(DeleteServiceDescriptor.NAME, serviceName, workerClass);
    }

    @Test
    public void testDelete() throws ProcessException, NoSuchIdentifierException {

        createInstance("deleteInstance1");
        createInstance("deleteInstance2");

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteServiceDescriptor.NAME);

            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(DeleteServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(DeleteServiceDescriptor.IDENTIFIER_NAME).setValue("deleteInstance1");

            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertFalse(checkInstanceExist("deleteInstance1"));
            assertTrue(checkInstanceExist("deleteInstance2"));
        } finally {
            deleteInstance(serviceBusiness, "deleteInstance2");
        }
    }

    @Test
    public void testFailDelete3() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteServiceDescriptor.NAME);

        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(DeleteServiceDescriptor.IDENTIFIER_NAME).setValue("unknowInstance");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }

    }
}
