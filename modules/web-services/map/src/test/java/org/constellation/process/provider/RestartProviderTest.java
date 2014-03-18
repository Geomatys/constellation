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
package org.constellation.process.provider;

import java.net.MalformedURLException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.AbstractProviderTest;
import org.constellation.process.provider.RestartProviderDescriptor;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class RestartProviderTest extends AbstractProviderTest {

    public RestartProviderTest() {
        super(RestartProviderDescriptor.NAME);
    }

    @Test
    public void testRestartProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{

        addProvider(buildCSVProvider(DATASTORE_SERVICE, "restartProvider1", true, EMPTY_CSV));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartProviderDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartProviderDescriptor.PROVIDER_ID_NAME).setValue("restartProvider1");

        final org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (LayerProvider p : DataProviders.getInstance().getProviders()) {
            if ("restartProvider1".equals(p.getId())){
                provider = p;
            }
        }

        assertNotNull(provider);

        removeProvider("restartProvider1");
    }

    @Test
    public void testRestartProviderFail() throws ProcessException, NoSuchIdentifierException, MalformedURLException{

        addProvider(buildCSVProvider(DATASTORE_SERVICE, "restartProvider2", true, EMPTY_CSV));

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
    public void testRestartProviderFail2() throws ProcessException, NoSuchIdentifierException, MalformedURLException{

        addProvider(buildCSVProvider(DATASTORE_SERVICE, "restartProvider3", true, EMPTY_CSV));

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
