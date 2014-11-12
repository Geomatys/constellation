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
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class SetConfigMapServiceTest  extends AbstractMapServiceTest {

    public SetConfigMapServiceTest (final String serviceName, final Class workerClass) {
        super(SetConfigServiceDescriptor.NAME,serviceName, workerClass);
    }

    @Test
    public void testUpdate() throws ProcessException, NoSuchIdentifierException {

        createInstance("updateInstance4", null);

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigServiceDescriptor.NAME);

            final LayerContext conf = new LayerContext();

            //WMS
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(SetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(SetConfigServiceDescriptor.IDENTIFIER_NAME).setValue("updateInstance4");
            in.parameter(SetConfigServiceDescriptor.CONFIG_NAME).setValue(conf);


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertEquals(conf, getConfig("updateInstance4"));
        } finally {
            deleteInstance(serviceBusiness, "updateInstance4");
        }
    }

    @Test
    public void testUpdateNoInstance() throws ProcessException, NoSuchIdentifierException {


        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigServiceDescriptor.NAME);

            final LayerContext conf = new LayerContext();

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(SetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(SetConfigServiceDescriptor.IDENTIFIER_NAME).setValue("instance10");
            in.parameter(SetConfigServiceDescriptor.CONFIG_NAME).setValue(conf);


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            assertEquals("Service " + serviceName.toLowerCase() + ":instance10 not found.", ex.getCause().getMessage());
        } 
    }
}
