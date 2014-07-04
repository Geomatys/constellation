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
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import java.net.MalformedURLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class UpdateProviderTest extends AbstractProviderTest {

    public UpdateProviderTest () {
        super(UpdateProviderDescriptor.NAME);
    }

    @Test
    public void testUpdateProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException{

        addProvider("updateProvider10",buildCSVProvider(DATASTORE_SERVICE, "updateProvider10", true, EMPTY_CSV));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateProviderDescriptor.NAME);

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "updateProvider10", false, EMPTY_CSV);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("updateProvider10");
        in.parameter("source").setValue(parameters);

        final Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (DataProvider p : DataProviders.getInstance().getProviders()) {
            if ("updateProvider10".equals(p.getId())){
                provider = p;
            }
        }

        assertNotNull(provider);
        assertTrue(parameters.equals(provider.getSource()));

        removeProvider("updateProvider1");
    }

    @Test
    public void testFailUpdateProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateProviderDescriptor.NAME);

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "updateProvider20", false, EMPTY_CSV);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("updateProvider20");
        in.parameter("source").setValue(parameters);

        try {
            final Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {

        }

    }
}
