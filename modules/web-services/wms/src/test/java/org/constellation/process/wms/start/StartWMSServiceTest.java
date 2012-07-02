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
package org.constellation.process.wms.start;

import org.constellation.process.wmts.start.StartWMTSServiceDescriptor;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.wms.WMSProcessTest;
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
public class StartWMSServiceTest extends WMSProcessTest {

    public StartWMSServiceTest() {
        super(StartWMTSServiceDescriptor.NAME);
    }


    @Test
    public void testStartWMS() throws NoSuchIdentifierException, ProcessException {

        final int initSize = WSEngine.getInstanceSize("WMS");
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(StartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue("instance1");
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize("WMS") == initSize+1);
        assertTrue(WSEngine.serviceInstanceExist("WMS", "instance1"));

        WSEngine.destroyInstances("WMS");
    }

    @Test
    public void testFailStartWMS() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(StartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue("instance5");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
}
