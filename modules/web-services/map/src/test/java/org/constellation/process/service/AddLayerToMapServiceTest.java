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
package org.constellation.process.service;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.ProviderService;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public abstract class AddLayerToMapServiceTest extends AbstractMapServiceTest {
    
    private static final String PROCESS_NAME = "service.add_layer";
    private static final DataReference COUNTRIES_DATA_REF = DataReference.createProviderDataReference(DataReference.PROVIDER_LAYER_TYPE, "shapeProvider", "{http://custom-namespace/}Countries");
    private static final DataReference STYLE_DATA_REF = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "styleProvider", "redBlue");
    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);
    
    @BeforeClass
    public static void createProvider(){
        
        ParameterDescriptorGroup sourceDesc = null;
        ProviderService service = null;
        final Collection<LayerProviderService> availableLayerServices = LayerProviderProxy.getInstance().getServices();
        for (LayerProviderService tmpService: availableLayerServices) {
            if ("feature-store".equals(tmpService.getName())) {
                service = tmpService;
            }
        }
        sourceDesc = (ParameterDescriptorGroup) service.getServiceDescriptor().descriptor(ProviderParameters.SOURCE_DESCRIPTOR_NAME);


        final ParameterValueGroup sourceValue = sourceDesc.createValue();
        sourceValue.parameter(ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("shapeProvider");
        sourceValue.parameter(ProviderParameters.SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(true);

        final ParameterValueGroup choiceValue = sourceValue.groups("choice").get(0);
        final ParameterValueGroup shapefileValue = (ParameterValueGroup) choiceValue.addGroup("ShapefileParametersFolder");
        final File shpFolder = FileUtilities.getDirectoryFromResource("data/shapefiles/");
        try {
            shapefileValue.parameter("url").setValue(shpFolder.toURI().toURL());
            shapefileValue.parameter("recursive").setValue(true);
            shapefileValue.parameter("namespace").setValue("http://custom-namespace/");
            shapefileValue.parameter("memory mapped buffer").setValue(true);
            shapefileValue.parameter("create spatial index").setValue(true);
            shapefileValue.parameter("charset").setValue(Charset.forName("UTF-8"));
            shapefileValue.parameter("load qix").setValue(true);
        } catch (MalformedURLException ex) {
            Logger.getLogger(AddLayerToMapServiceTest.class.getName()).log(Level.WARNING, null, ex);
        }         
        
        LayerProviderProxy.getInstance().createProvider((LayerProviderService) service, sourceValue);
    }
    
    @AfterClass
    public static void destroyProvider() {
        LayerProvider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if (p.getId().equals("shapeProvider")) {
                provider = p;
            }
        }
        LayerProviderProxy.getInstance().removeProvider(provider);
    }
    
    public AddLayerToMapServiceTest(final String serviceName, final Class workerClass) {
        super(AddLayerToMapServiceDescriptor.NAME, serviceName, workerClass);
    }

    @Test
    public void testAddSFLayerToConfiguration() throws NoSuchIdentifierException, ProcessException, MalformedURLException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        final Filter bbox = FF.bbox("geom", 10, 0, 30, 50, null);

        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(COUNTRIES_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue("Europe-costlines");
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(STYLE_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_FILTER_PARAM_NAME).setValue(bbox);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(serviceName);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue("addLayer1");

        final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
        final ParameterValueGroup outputs = process.call();
        final LayerContext outputContext = (LayerContext) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_CTX_PARAM_NAME).getValue();

        assertNotNull(outputContext);
        assertFalse(outputContext.getLayers().isEmpty());
        assertTrue(outputContext.getLayers().size() == 1);

        final Source outSource = outputContext.getLayers().get(0);
        assertEquals(COUNTRIES_DATA_REF.getProviderOrServiceId() ,outSource.getId());
        assertFalse(outSource.getLoadAll());
        assertTrue(outSource.getInclude().size() == 1);

        final Layer outLayer = outSource.getInclude().get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));
        
        assertTrue(checkInstanceExist("addLayer1"));
        deleteInstance("addLayer1");
    }


     /**
     * Source exist
     */
    @Test
    public void testAddSFLayerToConfiguration2() throws NoSuchIdentifierException, ProcessException, MalformedURLException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        final LayerContext inputContext = new LayerContext();
        inputContext.getLayers().add(new Source(COUNTRIES_DATA_REF.getProviderOrServiceId(), false, null, null));
        createCustomInstance("addLayer2", inputContext);
        startInstance("addLayer2");
        
        final Filter bbox = FF.bbox("geom", 10, 0, 30, 50, null);

        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(COUNTRIES_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue("Europe-costlines");
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(STYLE_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_FILTER_PARAM_NAME).setValue(bbox);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(serviceName);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue("addLayer2");

        final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
        final ParameterValueGroup outputs = process.call();
        final LayerContext outputContext = (LayerContext) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_CTX_PARAM_NAME).getValue();

        assertNotNull(outputContext);
        assertFalse(outputContext.getLayers().isEmpty());
        assertTrue(outputContext.getLayers().size() == 1);

        final Source outSource = outputContext.getLayers().get(0);
        assertEquals(COUNTRIES_DATA_REF.getProviderOrServiceId() ,outSource.getId());
        assertFalse(outSource.getLoadAll());
        assertTrue(outSource.getInclude().size() == 1);

        final Layer outLayer = outSource.getInclude().get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));
        
        assertTrue(checkInstanceExist("addLayer2"));
        deleteInstance("addLayer2");
    }

     /**
     * Layer already exist -> replacement
     */
    @Test
    public void testAddSFLayerToConfiguration3() throws NoSuchIdentifierException, ProcessException, MalformedURLException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        //init
        final LayerContext inputContext = new LayerContext();
        final List<Layer> layers = new ArrayList<Layer>();
        layers.add(new Layer(new QName(COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), COUNTRIES_DATA_REF.getLayerId().getLocalPart())));
        inputContext.getLayers().add(new Source(COUNTRIES_DATA_REF.getProviderOrServiceId(), false, layers, null));
        final Filter bbox = FF.bbox("geom", 10, 0, 30, 50, null);

        createCustomInstance("addLayer3", inputContext);
        startInstance("addLayer3");
        
        //exec process
        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(COUNTRIES_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue("Europe-costlines");
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(STYLE_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_FILTER_PARAM_NAME).setValue(bbox);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(serviceName);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue("addLayer3");

        final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
        final ParameterValueGroup outputs = process.call();
        final LayerContext outputContext = (LayerContext) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_CTX_PARAM_NAME).getValue();

        assertNotNull(outputContext);
        assertFalse(outputContext.getLayers().isEmpty());
        assertTrue(outputContext.getLayers().size() == 1);

        final Source outSource = outputContext.getLayers().get(0);
        assertEquals(COUNTRIES_DATA_REF.getProviderOrServiceId() ,outSource.getId());
        assertFalse(outSource.getLoadAll());
        assertTrue(outSource.getInclude().size() == 1);

        final Layer outLayer = outSource.getInclude().get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));
        
        assertTrue(checkInstanceExist("addLayer3"));
        deleteInstance("addLayer3");
    }


    /**
     *  Source in loadAllMode and layer already exist in exclude list
     */
     @Test
    public void testAddSFLayerToConfiguration5() throws NoSuchIdentifierException, ProcessException, MalformedURLException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        //init
        final LayerContext inputContext = new LayerContext();
        final List<Layer> layers = new ArrayList<Layer>();
        layers.add(new Layer(new QName(COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), COUNTRIES_DATA_REF.getLayerId().getLocalPart())));
        layers.add(new Layer(new QName(COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), "city")));
        inputContext.getLayers().add(new Source(COUNTRIES_DATA_REF.getProviderOrServiceId(), true, null, layers));
        createCustomInstance("addLayer5", inputContext);
        startInstance("addLayer5");
        
        final Filter bbox = FF.bbox("geom", 10, 0, 30, 50, null);

        //exec process
        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(COUNTRIES_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue("Europe-costlines");
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(STYLE_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_FILTER_PARAM_NAME).setValue(bbox);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(serviceName);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue("addLayer5");


        final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
        final ParameterValueGroup outputs = process.call();
        final LayerContext outputContext = (LayerContext) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_CTX_PARAM_NAME).getValue();

        assertNotNull(outputContext);
        assertFalse(outputContext.getLayers().isEmpty());
        assertTrue(outputContext.getLayers().size() == 1);

        final Source outSource = outputContext.getLayers().get(0);
        assertEquals(COUNTRIES_DATA_REF.getProviderOrServiceId() ,outSource.getId());
        assertFalse(outSource.getLoadAll());
        assertTrue(outSource.getInclude().size() == 1);
        assertTrue(outSource.getExclude().size() == 1);

        final Layer outLayer = outSource.isIncludedLayer(new QName("http://custom-namespace/", "Countries"));
        assertNotNull(outLayer);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));

        assertTrue(outSource.isExcludedLayer(new QName("http://custom-namespace/", "city")));
        
        
        assertTrue(checkInstanceExist("addLayer5"));
        deleteInstance("addLayer5");

    }

    /**
     * No style, no filter, no alias
     */
     @Test
    public void testAddSFLayerToConfiguration6() throws NoSuchIdentifierException, ProcessException, MalformedURLException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(COUNTRIES_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(serviceName);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue("addLayer6");

        final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
        final ParameterValueGroup outputs = process.call();
        final LayerContext outputContext = (LayerContext) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_CTX_PARAM_NAME).getValue();

        assertNotNull(outputContext);
        assertFalse(outputContext.getLayers().isEmpty());
        assertTrue(outputContext.getLayers().size() == 1);

        final Source outSource = outputContext.getLayers().get(0);
        assertEquals(COUNTRIES_DATA_REF.getProviderOrServiceId() ,outSource.getId());
        assertFalse(outSource.getLoadAll());
        assertTrue(outSource.getInclude().size() == 1);

        final Layer outLayer = outSource.getInclude().get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertNull(outLayer.getAlias());
        assertNull(outLayer.getFilter());
        assertTrue(outLayer.getStyles().isEmpty());
        
        assertTrue(checkInstanceExist("addLayer6"));
        deleteInstance("addLayer6");

    }
    
}
