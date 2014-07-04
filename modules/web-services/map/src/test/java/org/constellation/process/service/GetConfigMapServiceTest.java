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
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geoamtys)
 */
public abstract class GetConfigMapServiceTest extends AbstractMapServiceTest {

    public GetConfigMapServiceTest(final String serviceName, final Class workerClass) {
        super(GetConfigServiceDescriptor.NAME, serviceName, workerClass);
    }

     @Test
    public void testGetConfigWMS() throws ProcessException, NoSuchIdentifierException {

        try {
            final LayerContext conf = new LayerContext();

            createCustomInstance("getConfInstance5", conf);

            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigServiceDescriptor.NAME);

            //WMS
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(GetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(GetConfigServiceDescriptor.IDENTIFIER_NAME).setValue("getConfInstance5");

            org.geotoolkit.process.Process proc = desc.createProcess(in);
            ParameterValueGroup ouptuts = proc.call();

            assertEquals(conf, ouptuts.parameter(GetConfigServiceDescriptor.CONFIG_NAME).getValue());
        } finally {
            deleteInstance(serviceBusiness,  "getConfInstance5");
        }
    }

    @Test
    public void testGetConfigNoInstanceWMS() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(GetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(GetConfigServiceDescriptor.IDENTIFIER_NAME).setValue("getConfInstance10");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex ) {
            //do nohing
        }
    }

}
