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

import java.util.Collection;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.constellation.provider.configuration.Configurator;
import org.opengis.parameter.ParameterValueGroup;

import static org.junit.Assert.*;
import static org.constellation.provider.MockLayerProviderService.*;
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
        final Collection<DataProviderFactory> services = DataProviders.getInstance().getServices();
        assertEquals(1, services.size());
        assertTrue(services.iterator().next() instanceof MockLayerProviderService);
    }

    /**
     * Ensure nothing is registered with an empty configuration.
     */
    @Test
    public void testEmptyConfig(){

        final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                return service.getServiceDescriptor().createValue();
            }

            @Override
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DataProviders.getInstance().setConfigurator(config);

        assertEquals(0, DataProviders.getInstance().getProviders().size());
    }

    /**
     * test a single layer provider
     */
    @Test
    public void testLayerConfig(){

        final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();

                if(service.getName().equals("mock")){
                    ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C");
                    System.out.println(config);
                }

                return config;
            }

            @Override
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DataProviders.getInstance().setConfigurator(config);

        final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
        assertEquals(1, providers.size());
        assertEquals(3, DataProviders.getInstance().getKeys().size());
    }

    /**
     * Test several layer provider and accessing by source id.
     */
    @Test
    public void testLayersConfig(){

        final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();

                if(service.getName().equals("mock")){
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
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DataProviders.getInstance().setConfigurator(config);

        final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
        assertEquals(3, providers.size());
        assertEquals(9, DataProviders.getInstance().getKeys().size());
        assertEquals(4, DataProviders.getInstance().getKeys("id-0").size());
        assertEquals(2, DataProviders.getInstance().getKeys("id-1").size());
        assertEquals(3, DataProviders.getInstance().getKeys("id-2").size());
    }

    /**
     * Test correct loading of all providers even if one of them
     * raise an error.
     */
    @Test
    public void testLoadingCrashConfig(){

        final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();

                if(service.getName().equals("mock")){
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
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DataProviders.getInstance().setConfigurator(config);

        final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
        assertEquals(2, providers.size());
        assertEquals(7, DataProviders.getInstance().getKeys().size());
        assertEquals(4, DataProviders.getInstance().getKeys("id-0").size());
        assertEquals(0, DataProviders.getInstance().getKeys("id-1").size());
        assertEquals(3, DataProviders.getInstance().getKeys("id-2").size());
    }

    /**
     * Test correct disposal of all providers even if one of them
     * raise an error.
     */
    @Test
    public void testDisposeCrashConfig(){

        final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();

                if(service.getName().equals("mock")){
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
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DataProviders.getInstance().setConfigurator(config);

        assertEquals(3, DataProviders.getInstance().getProviders().size());

        //second provider will crash on dispose
        DataProviders.getInstance().dispose();

        //ensure all providers even if it crashed before are properly reloaded
        assertEquals(3, DataProviders.getInstance().getProviders().size());
        assertEquals(9, DataProviders.getInstance().getKeys().size());


        //set an empty configuration and verify nothing remains
        DataProviders.getInstance().setConfigurator(new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                return null;
            }

            @Override
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        assertEquals(0, DataProviders.getInstance().getProviders().size());
        assertEquals(0, DataProviders.getInstance().getKeys().size());

    }

}