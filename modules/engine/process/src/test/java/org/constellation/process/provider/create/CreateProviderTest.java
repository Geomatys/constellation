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
package org.constellation.process.provider.create;

import java.net.MalformedURLException;
import org.constellation.process.provider.ProviderTest;

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
public class CreateProviderTest extends ProviderTest {

    public CreateProviderTest () {
        super(CreateProviderDescriptor.NAME);
    }
    
    @Test
    public void testCreateProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{
        
        final int nbProvider = LayerProviderProxy.getInstance().getProviders().size();
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderDescriptor.NAME);

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "newProvider", false, EMPTY_CSV);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue(DATASTORE_SERVICE.getName());
        in.parameter("parameters").setValue(parameters);

        final Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if ("newProvider".equals(p.getId())){
                provider = p;
            }
        }
        assertTrue(nbProvider+1 == LayerProviderProxy.getInstance().getProviders().size());
        assertNotNull(provider);
            
    }
}
