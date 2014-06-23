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
package org.constellation.process.service;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.constellation.configuration.ConfigurationException;

import org.constellation.configuration.GetFeatureInfoCfg;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.map.featureinfo.CSVFeatureInfoFormat;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.ProviderFactory;
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
    public static void createProvider() throws ConfigurationException{
        
        ParameterDescriptorGroup sourceDesc = null;
        ProviderFactory service = null;
        final Collection<DataProviderFactory> availableLayerServices = DataProviders.getInstance().getFactories();
        for (DataProviderFactory tmpService: availableLayerServices) {
            if ("feature-store".equals(tmpService.getName())) {
                service = tmpService;
            }
        }
        sourceDesc = (ParameterDescriptorGroup) service.getProviderDescriptor();


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
        
        DataProviders.getInstance().createProvider("shapeProvider",(DataProviderFactory) service, sourceValue);
    }
    
    @AfterClass
    public static void destroyProvider() throws ConfigurationException {
        DataProvider provider = null;
        for (DataProvider p : DataProviders.getInstance().getProviders()) {
            if (p.getId().equals("shapeProvider")) {
                provider = p;
            }
        }
        DataProviders.getInstance().removeProvider(provider);
    }
    
    public AddLayerToMapServiceTest(final String serviceName, final Class workerClass) {
        super(AddLayerToMapServiceDescriptor.NAME, serviceName, workerClass);
    }

    @Test
    public void testAddSFLayerToConfiguration() throws NoSuchIdentifierException, ProcessException, MalformedURLException, ConfigurationException {
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
        final Layer outputLayer = (Layer) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_PARAM_NAME).getValue();

        assertNotNull(outputLayer);
        final List<Layer> layers = layerBusiness.getLayers(serviceName, "addLayer1", null);
        assertTrue(layers.size() == 1);
        
        final Layer outLayer = layers.get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));
        
        assertTrue(checkInstanceExist("addLayer1"));
        deleteInstance(serviceBusiness, "addLayer1");
    }


     /**
     * Source exist
     */
    @Test
    public void testAddSFLayerToConfiguration2() throws NoSuchIdentifierException, ProcessException, MalformedURLException, ConfigurationException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        final LayerContext inputContext = new LayerContext();
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
        final Layer layer = (Layer) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_PARAM_NAME).getValue();

        assertNotNull(layer);
        
        final List<Layer> layers = layerBusiness.getLayers(serviceName, "addLayer2", null);
        assertFalse(layers.isEmpty());
        assertTrue(layers.size() == 1);
        assertTrue(layer.getGetFeatureInfoCfgs().isEmpty()); //default generic GetFeatureInfo


        final Layer outLayer = layers.get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));
        
        assertTrue(checkInstanceExist("addLayer2"));
        deleteInstance(serviceBusiness, "addLayer2");
    }

     /**
     * Layer already exist -> replacement
     */
    @Test
    public void testAddSFLayerToConfiguration3() throws NoSuchIdentifierException, ProcessException, MalformedURLException, ConfigurationException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        //init
        final LayerContext inputContext = new LayerContext();
        inputContext.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());
        
        createCustomInstance("addLayer3", inputContext);
        startInstance("addLayer3");
        
        Layer layer = new Layer(new QName(COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), COUNTRIES_DATA_REF.getLayerId().getLocalPart()));
        layerBusiness.add(COUNTRIES_DATA_REF.getLayerId().getLocalPart(), 
                          COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), 
                          COUNTRIES_DATA_REF.getProviderOrServiceId(),
                          null, 
                          "addLayer3", 
                          serviceName.toLowerCase(), 
                          layer);
        

        final Filter bbox = FF.bbox("geom", 10, 0, 30, 50, null);
        
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
        final Layer outputLayer = (Layer) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_PARAM_NAME).getValue();

        assertNotNull(outputLayer);
        
        final List<Layer> layers = layerBusiness.getLayers(serviceName.toLowerCase(), "addLayer3", null);
        assertFalse(layers.isEmpty());
        assertTrue(layers.size() == 1);
        assertTrue(outputLayer.getGetFeatureInfoCfgs().size() > 0); //default generic GetFeatureInfo

        final Layer outLayer = layers.get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));
        
        assertTrue(checkInstanceExist("addLayer3"));
        deleteInstance(serviceBusiness, "addLayer3");
    }


    /**
     *  Source in loadAllMode and layer already exist in exclude list
     */
     @Test
    public void testAddSFLayerToConfiguration5() throws NoSuchIdentifierException, ProcessException, MalformedURLException, ConfigurationException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        //init
        final LayerContext inputContext = new LayerContext();
        inputContext.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());
        
        createCustomInstance("addLayer5", inputContext);
        startInstance("addLayer5");
        
        Layer layer1 = new Layer(new QName(COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), COUNTRIES_DATA_REF.getLayerId().getLocalPart()));
        layerBusiness.add(COUNTRIES_DATA_REF.getLayerId().getLocalPart(), 
                          COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), 
                          COUNTRIES_DATA_REF.getProviderOrServiceId(),
                          null, 
                          "addLayer5", 
                          serviceName.toLowerCase(), 
                          layer1);
        
        Layer layer2 = new Layer(new QName(COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), "city"));
        layerBusiness.add("city", 
                          COUNTRIES_DATA_REF.getLayerId().getNamespaceURI(), 
                          COUNTRIES_DATA_REF.getProviderOrServiceId(),
                          null, 
                          "addLayer5", 
                          serviceName.toLowerCase(), 
                          layer2);
        
        
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
        final Layer outputLayer = (Layer) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_PARAM_NAME).getValue();

        assertNotNull(outputLayer);
        
        final List<Layer> layers = layerBusiness.getLayers(serviceName, "addLayer5", null);
        assertFalse(layers.isEmpty());
        assertTrue(layers.size() == 1);
        assertTrue(outputLayer.getGetFeatureInfoCfgs().size() > 0); //default generic GetFeatureInfo


        final Layer outLayer = layers.get(0);
        assertNotNull(outLayer);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF ,outLayer.getStyles().get(0));

        //assertTrue(outSource.isExcludedLayer(new QName("http://custom-namespace/", "city")));
        
        
        assertTrue(checkInstanceExist("addLayer5"));
        deleteInstance(serviceBusiness, "addLayer5");

    }

    /**
     * No style, no filter, no alias
     */
     @Test
    public void testAddSFLayerToConfiguration6() throws NoSuchIdentifierException, ProcessException, MalformedURLException, ConfigurationException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(COUNTRIES_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(serviceName);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue("addLayer6");

        final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
        final ParameterValueGroup outputs = process.call();
        final Layer outputLayer = (Layer) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_PARAM_NAME).getValue();

        assertNotNull(outputLayer);
        
        final List<Layer> layers = layerBusiness.getLayers(serviceName, "addLayer6", null);
        assertFalse(layers.isEmpty());
        assertTrue(layers.size() == 1);
        assertTrue(outputLayer.getGetFeatureInfoCfgs().isEmpty()); //default generic GetFeatureInfo


        final Layer outLayer = layers.get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertNull(outLayer.getAlias());
        assertNull(outLayer.getFilter());
        assertTrue(outLayer.getStyles().isEmpty());
        
        assertTrue(checkInstanceExist("addLayer6"));
        deleteInstance(serviceBusiness, "addLayer6");

    }

    /**
     * Test custom GetFeatureInfo
     */
    @Test
    public void testAddSFLayerToConfiguration7() throws NoSuchIdentifierException, ProcessException, MalformedURLException, ConfigurationException {
        final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, PROCESS_NAME);

        final Filter bbox = FF.bbox("geom", 10, 0, 30, 50, null);
        final GetFeatureInfoCfg[] customGFI = new GetFeatureInfoCfg[1];
        customGFI[0] = new GetFeatureInfoCfg("text/plain", CSVFeatureInfoFormat.class.getCanonicalName());

        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(COUNTRIES_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue("Europe-costlines");
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(STYLE_DATA_REF);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_FILTER_PARAM_NAME).setValue(bbox);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(serviceName);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue("addLayer7");
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_CUSTOM_GFI_PARAM_NAME).setValue(customGFI);

        final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
        final ParameterValueGroup outputs = process.call();
        final Layer output = (Layer) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_PARAM_NAME).getValue();

        assertNotNull(output);
        
        final List<Layer> layers = layerBusiness.getLayers(serviceName, "addLayer7", null);
        assertFalse(layers.isEmpty());
        assertTrue(layers.size() == 1);
        assertTrue(output.getGetFeatureInfoCfgs().isEmpty()); //default generic GetFeatureInfo


        final Layer outLayer = layers.get(0);
        assertEquals(COUNTRIES_DATA_REF.getLayerId().getLocalPart() ,outLayer.getName().getLocalPart());
        assertEquals("Europe-costlines" ,outLayer.getAlias());
        assertNotNull(outLayer.getFilter());
        assertEquals(STYLE_DATA_REF, outLayer.getStyles().get(0));
        assertTrue(outLayer.getGetFeatureInfoCfgs().size() == 1);

        final GetFeatureInfoCfg outGFI = outLayer.getGetFeatureInfoCfgs().get(0);
        assertEquals("text/plain", outGFI.getMimeType());
        assertEquals(CSVFeatureInfoFormat.class.getCanonicalName(), outGFI.getBinding());

        assertTrue(checkInstanceExist("addLayer7"));
        deleteInstance(serviceBusiness, "addLayer7");
    }
    
}
