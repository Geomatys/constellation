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
package org.constellation.process.provider;

import java.net.MalformedURLException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.geotoolkit.process.Process;
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class GetConfigProviderTest extends AbstractProviderTest {

    public GetConfigProviderTest() {
        super(GetConfigProviderDescriptor.NAME);
    }

    @Test
    public void testGetConfigProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException{

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "getConfigProvider1", false, EMPTY_CSV);
        addProvider("getConfigProvider1",parameters);

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigProviderDescriptor.NAME);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(GetConfigProviderDescriptor.PROVIDER_ID_NAME).setValue("getConfigProvider1");

        final Process proc = desc.createProcess(in);
        final ParameterValueGroup outputs = proc.call();

        assertTrue(outputs.parameter(GetConfigProviderDescriptor.CONFIG_NAME).getValue().equals(parameters));

        removeProvider("getConfigProvider1");
    }


    @Test
    public void testFailGetConfigProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigProviderDescriptor.NAME);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(GetConfigProviderDescriptor.PROVIDER_ID_NAME).setValue("getConfigProvider2");

        try {
            final Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }

    }

}
