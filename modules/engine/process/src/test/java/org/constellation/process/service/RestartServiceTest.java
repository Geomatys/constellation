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

import java.util.List;
import java.util.Set;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.ws.WSEngine;
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
@RunWith(SpringTestRunner.class)
public abstract class RestartServiceTest extends ServiceProcessTest {

    public RestartServiceTest(final String serviceName, final Class workerClass) {
        super(RestartServiceDescriptor.NAME, serviceName, workerClass);
    }


    /**
     * Start all the existing  instance.
     */
    private void startAllInstance() {
        final List<String> serviceIDs = serviceBusiness.getServiceIdentifiers(serviceName.toLowerCase());
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
            deleteInstance(serviceBusiness, "restartInstance1");
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
            deleteInstance(serviceBusiness, "restartInstance2");
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
            deleteInstance(serviceBusiness, "restartInstance3");
            deleteInstance(serviceBusiness, "restartInstance4");
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
            deleteInstance(serviceBusiness, "restartInstance5");
            deleteInstance(serviceBusiness, "restartInstance6");
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
            deleteInstance(serviceBusiness, "restartInstance40");
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
