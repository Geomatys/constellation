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
package org.constellation.process.provider.update;

import java.net.MalformedURLException;
import org.constellation.process.provider.AbstractProviderTest;

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
public class UpdateProviderTest extends AbstractProviderTest {

    public UpdateProviderTest () {
        super(UpdateProviderDescriptor.NAME);
    }
    
    @Test
    public void testUpdateProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{
        
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "updateProvider1", true, EMPTY_CSV));
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateProviderDescriptor.NAME);

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "updateProvider1", false, EMPTY_CSV);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("updateProvider1");
        in.parameter("source").setValue(parameters);

        final Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if ("updateProvider1".equals(p.getId())){
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

        final ParameterValueGroup parameters = buildCSVProvider(DATASTORE_SERVICE, "updateProvider2", false, EMPTY_CSV);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("updateProvider2");
        in.parameter("source").setValue(parameters);
        
        try {
            final Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            
        }
            
    }
}
