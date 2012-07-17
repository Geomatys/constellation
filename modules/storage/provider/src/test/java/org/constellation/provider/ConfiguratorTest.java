/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.provider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.constellation.configuration.ConfigDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.constellation.provider.configuration.Configurator;
import org.geotoolkit.parameter.Parameters;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.junit.Assert.*;
import static org.constellation.provider.MockLayerProviderService.*;
import org.constellation.provider.configuration.ProviderParameters;
import static org.constellation.provider.configuration.ProviderParameters.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ConfiguratorTest {

    public ConfiguratorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * test that registered providers are found.
     */
    @Test
    public void testServiceAvailable(){
        final Collection<LayerProviderService> services = LayerProviderProxy.getInstance().getServices();
        assertEquals(2, services.size());
        assertTrue(services.iterator().next() instanceof MockLayerProviderService);
    }

    /**
     * Ensure nothing is registered with an empty configuration.
     */
    @Test
    public void testEmptyConfig(){

        final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(final String serviceName,
                    final ParameterDescriptorGroup desc) {
                return desc.createValue();
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(config);

        assertEquals(0, LayerProviderProxy.getInstance().getProviders().size());
    }

    /**
     * test a single layer provider
     */
    @Test
    public void testLayerConfig(){

        final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(final String serviceName, final ParameterDescriptorGroup desc) {
                final ParameterValueGroup config = desc.createValue();

                if(serviceName.equals("mock")){
                    ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C");
                    System.out.println(config);
                }

                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                final String fileName = serviceName + ".xml";
                final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

                if(configFile.exists()){
                    //make a backup
                    configFile.delete();
                }

                //write the configuration
                try {
                    ProviderParameters.write(configFile, params);
                } catch (XMLStreamException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(config);

        final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
        assertEquals(1, providers.size());
        assertEquals(3, LayerProviderProxy.getInstance().getKeys().size());
    }

    /**
     * Test several layer provider and accessing by source id.
     */
    @Test
    public void testLayersConfig(){

        final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {
                final ParameterValueGroup config = desc.createValue();

                if(serviceName.equals("mock")){
                    ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");

                    source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");

                    source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                }

                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                final String fileName = serviceName + ".xml";
                final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

                if(configFile.exists()){
                    //make a backup
                    configFile.delete();
                }
                try {
                    //write the configuration
                        ProviderParameters.write(configFile, params);
                } catch (IOException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.WARNING, null, ex);
                } catch (XMLStreamException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.WARNING, null, ex);
                }

            }
        };
        LayerProviderProxy.getInstance().setConfigurator(config);

        final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
        assertEquals(3, providers.size());
        assertEquals(9, LayerProviderProxy.getInstance().getKeys().size());
        assertEquals(4, LayerProviderProxy.getInstance().getKeys("id-0").size());
        assertEquals(2, LayerProviderProxy.getInstance().getKeys("id-1").size());
        assertEquals(3, LayerProviderProxy.getInstance().getKeys("id-2").size());
    }

    /**
     * Test correct loading of all providers even if one of them
     * raise an error.
     */
    @Test
    public void testLoadingCrashConfig(){

        final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {
                final ParameterValueGroup config = desc.createValue();

                if(serviceName.equals("mock")){
                    ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");

                    source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                    srcconfig.parameter(CRASH_CREATE.getName().getCode()).setValue(true);

                    source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                }

                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                final String fileName = serviceName + ".xml";
                final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

                if(configFile.exists()){
                    //make a backup
                    configFile.delete();
                }

                //write the configuration
                try {
                    ProviderParameters.write(configFile, params);
                } catch (XMLStreamException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(config);

        final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
        assertEquals(2, providers.size());
        assertEquals(7, LayerProviderProxy.getInstance().getKeys().size());
        assertEquals(4, LayerProviderProxy.getInstance().getKeys("id-0").size());
        assertEquals(0, LayerProviderProxy.getInstance().getKeys("id-1").size());
        assertEquals(3, LayerProviderProxy.getInstance().getKeys("id-2").size());
    }

    /**
     * Test correct disposal of all providers even if one of them
     * raise an error.
     */
    @Test
    public void testDisposeCrashConfig(){

        final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {
                final ParameterValueGroup config = desc.createValue();

                if(serviceName.equals("mock")){
                    ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");

                    source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                    srcconfig.parameter(CRASH_DISPOSE.getName().getCode()).setValue(true);

                    source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                }

                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                final String fileName = serviceName + ".xml";
                final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

                if(configFile.exists()){
                    //make a backup
                    configFile.delete();
                }

                //write the configuration
                try {
                    ProviderParameters.write(configFile, params);
                } catch (XMLStreamException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(config);

        assertEquals(3, LayerProviderProxy.getInstance().getProviders().size());

        //second provider will crash on dispose
        LayerProviderProxy.getInstance().dispose();

        //ensure all providers even if it crashed before are properly reloaded
        assertEquals(3, LayerProviderProxy.getInstance().getProviders().size());
        assertEquals(9, LayerProviderProxy.getInstance().getKeys().size());


        //set an empty configuration and verify nothing remains
        LayerProviderProxy.getInstance().setConfigurator(new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {
                return null;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                final String fileName = serviceName + ".xml";
                final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

                if(configFile.exists()){
                    //make a backup
                    configFile.delete();
                }

                //write the configuration
                try {
                    ProviderParameters.write(configFile, params);
                } catch (XMLStreamException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ConfiguratorTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        assertEquals(0, LayerProviderProxy.getInstance().getProviders().size());
        assertEquals(0, LayerProviderProxy.getInstance().getKeys().size());

    }

}