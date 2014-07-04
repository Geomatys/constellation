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

package org.constellation.provider;

import org.constellation.admin.SpringHelper;
import org.constellation.configuration.ConfigurationException;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.constellation.provider.MockLayerProviderFactory.CRASH_CREATE;
import static org.constellation.provider.MockLayerProviderFactory.CRASH_DISPOSE;
import static org.constellation.provider.MockLayerProviderFactory.LAYERS;
import static org.constellation.provider.MockLayerProviderFactory.PARAMETERS_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class ConfiguratorTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public ConfiguratorTest() {
    }

    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * test that registered providers are found.
     */
    @Test
    public void testServiceAvailable(){
        final Collection<DataProviderFactory> services = DataProviders.getInstance().getFactories();
        assertEquals(1, services.size());
        assertTrue(services.iterator().next() instanceof MockLayerProviderFactory);
    }

    /**
     * Ensure nothing is registered with an empty configuration.
     */
    @Test
    public void testEmptyConfig(){

        final Configurator config = new AbstractConfigurator() {
            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                return Collections.EMPTY_LIST;
            }

            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                return Collections.EMPTY_LIST;
            }
        };
        Providers.setConfigurator(config);

        assertEquals(0, DataProviders.getInstance().getProviders().size());
    }

    /**
     * test a single layer provider
     */
    @Test
    public void testLayerConfig(){

        final Configurator config = new AbstractConfigurator() {
            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("mock");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("mock",source));
                
                return lst;
            }
            
            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                final ArrayList<ProviderInformation> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("mock");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C");
                lst.add(new ProviderInformation("mock", "mock", source));
                
                return lst;
            }
        };
        Providers.setConfigurator(config);

        final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
        assertEquals(1, providers.size());
        assertEquals(3, DataProviders.getInstance().getKeys().size());
    }

    /**
     * Test several layer provider and accessing by source id.
     */
    @Test
    public void testLayersConfig(){

        final Configurator config = new AbstractConfigurator() {

            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-0",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-1",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-2",source));}
                
                return lst;
            }

            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                final ArrayList<Configurator.ProviderInformation> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");
                lst.add(new ProviderInformation("id-0", "mock", source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                lst.add(new ProviderInformation("id-1", "mock",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                lst.add(new ProviderInformation("id-2", "mock",source));}
                
                return lst;
            }
        };
        Providers.setConfigurator(config);

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

        final Configurator config = new AbstractConfigurator() {

            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-0",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                srcconfig.parameter(CRASH_CREATE.getName().getCode()).setValue(true);
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-1",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-2",source));}
                
                return lst;
            }
            
            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                final ArrayList<Configurator.ProviderInformation> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");
                lst.add(new ProviderInformation("id-0", "mock",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                srcconfig.parameter(CRASH_CREATE.getName().getCode()).setValue(true);
                lst.add(new ProviderInformation("id-1", "mock",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                lst.add(new ProviderInformation("id-2", "mock",source));}
                
                return lst;
            }
        };
        Providers.setConfigurator(config);

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

        final Configurator config = new AbstractConfigurator() {

            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-0",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                srcconfig.parameter(CRASH_DISPOSE.getName().getCode()).setValue(true);
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-1",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                lst.add(new AbstractMap.SimpleImmutableEntry<>("id-2",source));}
                
                return lst;
            }
            
            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                final ArrayList<Configurator.ProviderInformation> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("mock");
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-0");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("A,B,C,D");
                lst.add(new ProviderInformation("id-0", "mock",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-1");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("E,F");
                srcconfig.parameter(CRASH_DISPOSE.getName().getCode()).setValue(true);
                lst.add(new ProviderInformation("id-1", "mock",source));}
                
                {final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("id-2");
                ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                srcconfig.parameter(LAYERS.getName().getCode()).setValue("G,H,I");
                lst.add(new ProviderInformation("id-2", "mock",source));}
                
                return lst;
            }
        };
        Providers.setConfigurator(config);

        assertEquals(3, DataProviders.getInstance().getProviders().size());

        //second provider will crash on dispose
        DataProviders.getInstance().dispose();

        //ensure all providers even if it crashed before are properly reloaded
        assertEquals(3, DataProviders.getInstance().getProviders().size());
        assertEquals(9, DataProviders.getInstance().getKeys().size());


        //set an empty configuration and verify nothing remains
        Providers.setConfigurator(new AbstractConfigurator() {
            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                return Collections.EMPTY_LIST;
            }
            
            @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                return Collections.EMPTY_LIST;
            }
        });

        assertEquals(0, DataProviders.getInstance().getProviders().size());
        assertEquals(0, DataProviders.getInstance().getKeys().size());

    }

}