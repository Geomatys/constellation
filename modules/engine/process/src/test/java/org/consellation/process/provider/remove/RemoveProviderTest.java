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
package org.consellation.process.provider.remove;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.consellation.process.provider.ProviderTest;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.remove.RemoveProviderDescriptor;
import org.constellation.provider.*;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.geotoolkit.process.Process;
import static org.junit.Assert.*;
import org.opengis.feature.ComplexAttribute;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class RemoveProviderTest extends ProviderTest {

    public RemoveProviderTest() {
        super(RemoveProviderDescriptor.NAME);
    }
    
    @Test
    public void testRemoveProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{
        
        final int nbProvider = LayerProviderProxy.getInstance().getProviders().size();
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RemoveProviderDescriptor.NAME);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("provider1");

        final Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if ("provider1".equals(p.getId())){
                provider = p;
            }
        }
        assertTrue(nbProvider-1 == LayerProviderProxy.getInstance().getProviders().size());
        assertNull(provider);
            
    }
    
}
