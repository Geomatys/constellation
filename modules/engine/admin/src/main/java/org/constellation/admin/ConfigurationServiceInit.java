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
package org.constellation.admin;

import org.constellation.engine.register.ConfigurationService;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.constellation.ws.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

/**
 * This class wires spring managed beans with legacy code.
 * {@link ConfigurationServiceInit#init()} method will inject a {@link WorkerFactory} to {@link WSEngine}
 * to allow Spring instantiation of OGC Workers.
 * 
 * @author Olivier NOUGUIER
 *
 */
public class ConfigurationServiceInit implements ApplicationContextAware {
    
    
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private ProviderRepository providerRepository;
    
    /**
     * Spring applucation context.
     */
    private ApplicationContext applicationContext;

    public void init() {
        
    	SpringHelper.setApplicationContext(applicationContext);
        ConfigurationEngine.setSecurityManager(SecurityManagerHolder.getInstance());
        ConfigurationEngine.setConfigurationService(configurationService);
        
        WSEngine.setWorkerFactory(new WorkerFactory() {

            @Override
            public Worker build(Class<? extends Worker> workerClass, String identifier) {
                String[] beanNames = applicationContext.getBeanNamesForType(workerClass);
                if(beanNames==null || beanNames.length == 0) {
                    LOGGER.info(workerClass.getName() + " is not managed by spring" );
                    return (Worker) ReflectionUtilities.newInstance(workerClass, identifier);
                }
                if(beanNames.length > 1) {
                    LOGGER.warn("There is more than one bean definition for workerClass: " + workerClass.getName());
                }
                
                return (Worker) applicationContext.getBean(beanNames[0], identifier);

            }

            
        });
        
        
        ServiceConfigurer.setApplicationContext(applicationContext);
        

    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
