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

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteServiceDescriptor.NAME);

        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteServiceDescriptor.SERVICE_NAME_NAME).setValue(serviceName);
        in.parameter(DeleteServiceDescriptor.IDENTIFIER_NAME).setValue("deleteInstance1");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertFalse(checkInstanceExist("deleteInstance1"));
        assertTrue(checkInstanceExist("deleteInstance2"));

        deleteInstance("deleteInstance2");
    }

    @Test
    public void testFailDelete3() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteServiceDescriptor.NAME);

        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteServiceDescriptor.SERVICE_NAME_NAME).setValue(serviceName);
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
