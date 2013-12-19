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

import java.util.List;
import java.util.Set;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.test.utils.Order;
import org.constellation.ws.WSEngine;
import org.constellation.test.utils.TestRunner;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@RunWith(TestRunner.class)
public abstract class RestartServiceTest extends ServiceProcessTest {

    public RestartServiceTest(final String serviceName, final Class workerClass) {
        super(RestartServiceDescriptor.NAME, serviceName, workerClass);
    }


    /**
     * Start all the existing  instance.
     */
    private void startAllInstance() {
        final List<String> serviceIDs = ConfigurationEngine.getServiceConfigurationIds(serviceName);
        for (String serviceID : serviceIDs) {
            startInstance(serviceID);
        }
    }

    @Test
    @Order(order = 1)
    public void testRestartOneNoClose() throws NoSuchIdentifierException, ProcessException {

        LOGGER.info("TEST Restart One no close");
        createInstance("restartInstance1");
        startInstance("restartInstance1");

        try {
            final int initSize = WSEngine.getInstanceSize(serviceName);
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("restartInstance1");
            in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(false);
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(WSEngine.getInstanceSize(serviceName) == initSize);
            assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance1"));
        } finally {
            deleteInstance("restartInstance1");
        }
    }

    @Test
    @Order(order = 2)
    public void testRestartOneClose() throws NoSuchIdentifierException, ProcessException {

        LOGGER.info("TEST Restart One close");

        createInstance("restartInstance2");
        startInstance("restartInstance2");

        try {
            final int initSize = WSEngine.getInstanceSize(serviceName);
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("restartInstance2");
            in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(true);
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(WSEngine.getInstanceSize(serviceName) == initSize);
            assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance2"));
        } finally {
            deleteInstance("restartInstance2");
        }
    }

    @Test
    @Order(order = 3)
    public void testRestartAllNoClose() throws NoSuchIdentifierException, ProcessException, InterruptedException {

        LOGGER.info("TEST Restart all no close");

        startAllInstance();
        createInstance("restartInstance3");
        createInstance("restartInstance4");
        startInstance("restartInstance3");
        startInstance("restartInstance4");

        try {
            final int initSize = WSEngine.getInstanceSize(serviceName);
            final Set<String> instancesBefore = WSEngine.getInstanceNames(serviceName);
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(null);
            in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(false);
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            Thread.sleep(1000);

            final int newSize =  WSEngine.getInstanceSize(serviceName);
            final Set<String> instances = WSEngine.getInstanceNames(serviceName);
            assertTrue("expected " + initSize + " (" +  instancesBefore + ") but was:" + newSize + "(" + instances + ")", newSize == initSize);
            assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance3"));
            assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance4"));

        } finally {
            deleteInstance("restartInstance3");
            deleteInstance("restartInstance4");
        }
    }

    @Test
    @Order(order = 4)
    public void testRestartAllClose() throws NoSuchIdentifierException, ProcessException {

        LOGGER.info("TEST Restart all close");
        
        startAllInstance();
        createInstance("restartInstance5");
        createInstance("restartInstance6");
        startInstance("restartInstance5");
        startInstance("restartInstance6");

        try {
            final int initSize = WSEngine.getInstanceSize(serviceName);
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(null);
            in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(true);

            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(WSEngine.getInstanceSize(serviceName) == initSize);
            assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance5"));
            assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance6"));
        } finally {
            deleteInstance("restartInstance5");
            deleteInstance("restartInstance6");
        }
    }

    /**
     * Restart an instance that exist but no started.
     */
    @Test
    @Order(order = 5)
    public void testStart() throws NoSuchIdentifierException, ProcessException {

        LOGGER.info("TEST start");

        createInstance("restartInstance40");
        try {
            final int initSize = WSEngine.getInstanceSize(serviceName);
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("restartInstance40");

            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance40"));
        } finally {
            deleteInstance("restartInstance40");
        }
    }

    /**
     * Try to restart an instance that doesn't exist.
     * @throws NoSuchIdentifierException
     * @throws ProcessException
     */
    @Test
    @Order(order = 6)
    public void testFailRestart2() throws NoSuchIdentifierException, ProcessException {
        LOGGER.info("TEST fail Restart 2");

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("restartInstance5");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

}
