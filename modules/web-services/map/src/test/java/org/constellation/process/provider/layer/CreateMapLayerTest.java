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
package org.constellation.process.provider.layer;

import java.net.MalformedURLException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
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
 * @author Quentin Boileau (Geomatys).
 */
public class CreateMapLayerTest extends AbstractMapLayerTest {

    public CreateMapLayerTest() {
        super(CreateProviderLayerStyleDescriptor.NAME);
    }

    @Test
    public void testCreateLayer() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildCSVProvider(DATASTORE_SERVICE, "createProvider1", true, EMPTY_CSV, null));

        ParameterValueGroup expectedProvider = buildCSVProvider(DATASTORE_SERVICE, "createProvider1", true, EMPTY_CSV, "layer1");

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderLayerStyleDescriptor.NAME);

        final ParameterValueGroup layer = buildLayer(DATASTORE_SERVICE, "layer1");
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateProviderLayerStyleDescriptor.PROVIDER_ID_NAME).setValue("createProvider1");
        in.parameter(CreateProviderLayerStyleDescriptor.LAYER_NAME).setValue(layer);

        desc.createProcess(in).call();

        Provider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if ("createProvider1".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        assertEquals(expectedProvider, provider.getSource());
        assertFalse(provider.getSource().groups("Layer").isEmpty());

        removeProvider("createProvider1");
    }

    /**
     * Provider doesn't exist.
     */
    @Test
    public void testFailCreateLayer() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderLayerStyleDescriptor.NAME);

        final ParameterValueGroup layer = buildLayer(DATASTORE_SERVICE, "layer2");
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateProviderLayerStyleDescriptor.PROVIDER_ID_NAME).setValue("createProvider2");
        in.parameter(CreateProviderLayerStyleDescriptor.LAYER_NAME).setValue(layer);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }

    }
}
