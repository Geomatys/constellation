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
import org.constellation.provider.*;

import org.geotoolkit.process.Process;
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
public class CreateProviderTest extends AbstractProviderTest {

    public CreateProviderTest () {
        super(CreateProviderDescriptor.NAME);
    }

    @Test
    public void testCreateProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException{

        final int nbProvider = DataProviders.getInstance().getProviders().size();
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderDescriptor.NAME);

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "newProvider", false, EMPTY_CSV);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_type").setValue(DATASTORE_SERVICE.getName());
        in.parameter("parameters").setValue(parameters);

        final Process proc = desc.createProcess(in);
        proc.call();

        DataProvider provider = null;
        for (DataProvider p : DataProviders.getInstance().getProviders()) {
            if ("newProvider".equals(p.getId())){
                provider = p;
            }
        }
        assertTrue(nbProvider+1 == DataProviders.getInstance().getProviders().size());
        assertNotNull(provider);

        DataProviders.getInstance().removeProvider(provider);
        removeProvider("newProvider");

    }
}
