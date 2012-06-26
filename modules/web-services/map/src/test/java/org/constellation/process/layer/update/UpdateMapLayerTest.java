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
package org.constellation.process.layer.update;

import java.net.MalformedURLException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.layer.AbstractMapLayerTest;
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
public class UpdateMapLayerTest extends AbstractMapLayerTest {

    public UpdateMapLayerTest() {
        super(UpdateMapLayerDescriptor.NAME);
    }

    @Test
    public void testUpdateLayer() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildCSVProvider(DATASTORE_SERVICE, "updateProvider1", true, EMPTY_CSV, "provider2Layer"));

        final ParameterValueGroup expectedProvider = buildCSVProvider(DATASTORE_SERVICE, "updateProvider1", true, EMPTY_CSV, "newLayer");

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapLayerDescriptor.NAME);

        final ParameterValueGroup layer = buildLayer(DATASTORE_SERVICE, "newLayer");
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapLayerDescriptor.PROVIDER_ID_NAME).setValue("updateProvider1");
        in.parameter(UpdateMapLayerDescriptor.LAYER_NAME_NAME).setValue( "provider2Layer");
        in.parameter(UpdateMapLayerDescriptor.UPDATE_LAYER_NAME).setValue(layer);

        desc.createProcess(in).call();

        Provider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if ("updateProvider1".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        assertEquals(expectedProvider, provider.getSource());
        assertTrue(provider.getSource().groups("Layer").size() == 1);

        removeProvider("updateProvider1");
    }


    /**
     * Layer doesn't exist.
     */
    @Test
    public void testFailUpdateLayer1() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildCSVProvider(DATASTORE_SERVICE, "updateProvider2", true, EMPTY_CSV, "provider2Layer"));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapLayerDescriptor.NAME);

        final ParameterValueGroup layer = buildLayer(DATASTORE_SERVICE, "newLayer");
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapLayerDescriptor.PROVIDER_ID_NAME).setValue("updateProvider2");
        in.parameter(UpdateMapLayerDescriptor.LAYER_NAME_NAME).setValue("layer22");
        in.parameter(UpdateMapLayerDescriptor.UPDATE_LAYER_NAME).setValue(layer);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }

        removeProvider("updateProvider2");
    }


    /**
     * Provider doesn't exist.
     */
    @Test
    public void testFailUpdateLayer2() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapLayerDescriptor.NAME);

        final ParameterValueGroup layer = buildLayer(DATASTORE_SERVICE, "newLayer");
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapLayerDescriptor.PROVIDER_ID_NAME).setValue("updateProvider3");
        in.parameter(UpdateMapLayerDescriptor.LAYER_NAME_NAME).setValue("layer2");
        in.parameter(UpdateMapLayerDescriptor.UPDATE_LAYER_NAME).setValue(layer);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }


    /**
     * Empty provider identifier.
     */
    @Test
    public void testFailUpdateLayer3() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapLayerDescriptor.NAME);

        final ParameterValueGroup layer = buildLayer(DATASTORE_SERVICE, "newLayer");
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapLayerDescriptor.PROVIDER_ID_NAME).setValue("");
        in.parameter(UpdateMapLayerDescriptor.LAYER_NAME_NAME).setValue("layer2");
        in.parameter(UpdateMapLayerDescriptor.UPDATE_LAYER_NAME).setValue(layer);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

     /**
     * Empty layer name.
     */
    @Test
    public void testFailUpdateLayer4() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapLayerDescriptor.NAME);

        final ParameterValueGroup layer = buildLayer(DATASTORE_SERVICE, "newLayer");
        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapLayerDescriptor.PROVIDER_ID_NAME).setValue("updateProvider4");
        in.parameter(UpdateMapLayerDescriptor.LAYER_NAME_NAME).setValue("");
        in.parameter(UpdateMapLayerDescriptor.UPDATE_LAYER_NAME).setValue(layer);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

}
