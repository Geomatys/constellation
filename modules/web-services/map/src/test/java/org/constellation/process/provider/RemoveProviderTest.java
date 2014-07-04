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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class RemoveProviderTest extends AbstractProviderTest {

    public RemoveProviderTest() {
        super(DeleteProviderDescriptor.NAME);
    }

    @Test
    public void testRemoveProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException, ConfigurationException{

        addProvider("removeProvider1",buildCSVProvider(DATASTORE_SERVICE, "removeProvider1", true, EMPTY_CSV));

        final int nbProvider = DataProviders.getInstance().getProviders().size();

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteProviderDescriptor.NAME);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("removeProvider1");

        final Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (DataProvider p : DataProviders.getInstance().getProviders()) {
            if ("removeProvider1".equals(p.getId())){
                provider = p;
            }
        }
        assertTrue(nbProvider-1 == DataProviders.getInstance().getProviders().size());
        assertNull(provider);

        removeProvider("removeProvider1");
    }


    @Test
    public void testFailRemoveProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{


        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteProviderDescriptor.NAME);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("deleteProvider10");

        try {
            final Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {

        }

    }

}
