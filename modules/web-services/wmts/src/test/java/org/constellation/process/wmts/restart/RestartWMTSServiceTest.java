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
package org.constellation.process.wmts.restart;

import org.constellation.process.wmts.restart.RestartWMTSServiceDescriptor;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.wmts.WMTSProcessTest;
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
 * @author Quentin Boileau (Geomatys).
 */
public class RestartWMTSServiceTest extends WMTSProcessTest {

    public RestartWMTSServiceTest() {
        super(RestartWMTSServiceDescriptor.NAME);
    }


    @Test
    public void testRestartOneWMTSNoClose() throws NoSuchIdentifierException, ProcessException {

        startInstance("instance1");

        final int initSize = WSEngine.getInstanceSize("WMTS");
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue("instance1");
        in.parameter(RestartWMTSServiceDescriptor.CLOSE_NAME).setValue(false);
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize("WMTS") == initSize);
        assertTrue(WSEngine.serviceInstanceExist("WMTS", "instance1"));

        WSEngine.destroyInstances("WMTS");
    }

    @Test
    public void testRestartOneWMTSClose() throws NoSuchIdentifierException, ProcessException {

        startInstance("instance2");

        final int initSize = WSEngine.getInstanceSize("WMTS");
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue("instance2");
        in.parameter(RestartWMTSServiceDescriptor.CLOSE_NAME).setValue(true);
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize("WMTS") == initSize);
        assertTrue(WSEngine.serviceInstanceExist("WMTS", "instance2"));

        WSEngine.destroyInstances("WMTS");
    }

    @Test
    public void testRestartAllWMTSNoClose() throws NoSuchIdentifierException, ProcessException {

        startInstance("instance1");
        startInstance("instance2");

        final int initSize = WSEngine.getInstanceSize("WMTS");
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue(null);
        in.parameter(RestartWMTSServiceDescriptor.CLOSE_NAME).setValue(false);
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize("WMTS") == initSize);
        assertTrue(WSEngine.serviceInstanceExist("WMTS", "instance1"));
        assertTrue(WSEngine.serviceInstanceExist("WMTS", "instance2"));

        WSEngine.destroyInstances("WMTS");
    }

    @Test
    public void testRestartAllWMTSClose() throws NoSuchIdentifierException, ProcessException {

        startInstance("instance1");
        startInstance("instance2");

        final int initSize = WSEngine.getInstanceSize("WMTS");
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue(null);
        in.parameter(RestartWMTSServiceDescriptor.CLOSE_NAME).setValue(true);
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize("WMTS") == initSize);
        assertTrue(WSEngine.serviceInstanceExist("WMTS", "instance1"));
        assertTrue(WSEngine.serviceInstanceExist("WMTS", "instance2"));

        WSEngine.destroyInstances("WMTS");
    }

    /**
     * Try to restart an instance that exist but no started.
     * @throws NoSuchIdentifierException
     * @throws ProcessException
     */
    @Test
    public void testFailRestartWMTS1() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue("instance4");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    /**
     * Try to restart an instance that doesn't exist.
     * @throws NoSuchIdentifierException
     * @throws ProcessException
     */
    @Test
    public void testFailRestartWMTS2() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartWMTSServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartWMTSServiceDescriptor.IDENTIFIER_NAME).setValue("instance5");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

}
