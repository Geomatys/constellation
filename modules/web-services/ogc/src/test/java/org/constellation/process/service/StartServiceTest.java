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
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

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
        final int initSize = WSEngine.getInstanceSize(serviceName);
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue("startInstance1");
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize(serviceName) == initSize+1);
        assertTrue(WSEngine.serviceInstanceExist(serviceName, "startInstance1"));

        deleteInstance("startInstance1");
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
