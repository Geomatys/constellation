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

import org.constellation.configuration.ConfigurationException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import java.net.MalformedURLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class RestartProviderTest extends AbstractProviderTest {

    public RestartProviderTest() {
        super(RestartProviderDescriptor.NAME);
    }

    @Test
    public void testRestartProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException{

        addProvider("restartProvider1",buildCSVProvider(DATASTORE_SERVICE, "restartProvider1", true, EMPTY_CSV));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartProviderDescriptor.PROVIDER_ID_NAME).setValue("restartProvider1");

        final org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (DataProvider p : DataProviders.getInstance().getProviders()) {
            if ("restartProvider1".equals(p.getId())){
                provider = p;
            }
        }

        assertNotNull(provider);

        removeProvider("restartProvider1");
    }

    @Test
    public void testRestartProviderFail() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException{

        addProvider("restartProvider2",buildCSVProvider(DATASTORE_SERVICE, "restartProvider2", true, EMPTY_CSV));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartProviderDescriptor.PROVIDER_ID_NAME).setValue("unknowProvider");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nohing
        }

        removeProvider("restartProvider2");
    }

    @Test
    public void testRestartProviderFail2() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException{

        addProvider("restartProvider3",buildCSVProvider(DATASTORE_SERVICE, "restartProvider3", true, EMPTY_CSV));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartProviderDescriptor.PROVIDER_ID_NAME).setValue("");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nohing
        }

        removeProvider("restartProvider3");
    }

}
