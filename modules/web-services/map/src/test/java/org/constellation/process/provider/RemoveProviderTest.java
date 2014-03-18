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

import java.io.File;
import java.net.MalformedURLException;
import org.constellation.process.provider.AbstractProviderTest;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.DeleteProviderDescriptor;
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
public class RemoveProviderTest extends AbstractProviderTest {

    public RemoveProviderTest() {
        super(DeleteProviderDescriptor.NAME);
    }

    @Test
    public void testRemoveProvider() throws ProcessException, NoSuchIdentifierException, MalformedURLException{

        addProvider(buildCSVProvider(DATASTORE_SERVICE, "removeProvider1", true, EMPTY_CSV));

        final int nbProvider = DataProviders.getInstance().getProviders().size();

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteProviderDescriptor.NAME);
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("provider_id").setValue("removeProvider1");

        final Process proc = desc.createProcess(in);
        proc.call();

        Provider provider = null;
        for (LayerProvider p : DataProviders.getInstance().getProviders()) {
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
