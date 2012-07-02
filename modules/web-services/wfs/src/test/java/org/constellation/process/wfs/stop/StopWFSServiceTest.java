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
package org.constellation.process.wfs.stop;

import java.io.File;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.wfs.WFSProcessTest;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class StopWFSServiceTest extends WFSProcessTest {

    public StopWFSServiceTest() {
        super(StopWFSServiceDescriptor.NAME);
    }


    @Test
    public void testStopWFS() throws NoSuchIdentifierException, ProcessException {

        startInstance("instance2");

        final int initSize = WSEngine.getInstanceSize("WFS");
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StopWFSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(StopWFSServiceDescriptor.IDENTIFIER_NAME).setValue("instance2");
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize("WFS") == initSize-1);
        assertFalse(WSEngine.serviceInstanceExist("WFS", "instance2"));

        WSEngine.destroyInstances("WFS");
    }

    @Test
    public void testFailStopWFS() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StopWFSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(StopWFSServiceDescriptor.IDENTIFIER_NAME).setValue("instance5");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    @Test
    public void testAlreadyStopWFS() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StopWFSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(StopWFSServiceDescriptor.IDENTIFIER_NAME).setValue("instance3");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
}
