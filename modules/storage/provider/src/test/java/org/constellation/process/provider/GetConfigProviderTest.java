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

import org.constellation.process.provider.GetConfigProviderDescriptor;
import java.io.File;
import java.net.MalformedURLException;
import org.constellation.process.provider.AbstractProviderTest;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.*;
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
    public void testGetConfigProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "getConfigProvider1", false, EMPTY_CSV);
        addProvider(parameters);

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
