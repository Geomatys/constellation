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
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
            public ProviderConfig getConfiguration(String serviceName) {
                return new ProviderConfig();
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
            public ProviderConfig getConfiguration(String serviceName) {
                final ProviderConfig config = new ProviderConfig();

                if(serviceName.equals("mock")){
                    final ProviderSource source = new ProviderSource();
                    source.parameters.put("layers", "A,B,C");
                    config.sources.add(source);
                }

                return config;
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
            public ProviderConfig getConfiguration(String serviceName) {
                final ProviderConfig config = new ProviderConfig();

                if(serviceName.equals("mock")){
                    ProviderSource source = new ProviderSource();
                    source.parameters.put("layers", "A,B,C,D");
                    source.id = "id-0";
                    config.sources.add(source);

                    source = new ProviderSource();
                    source.parameters.put("layers", "E,F");
                    source.id = "id-1";
                    config.sources.add(source);

                    source = new ProviderSource();
                    source.parameters.put("layers", "G,H,I");
                    source.id = "id-2";
                    config.sources.add(source);
                }

                return config;
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

}