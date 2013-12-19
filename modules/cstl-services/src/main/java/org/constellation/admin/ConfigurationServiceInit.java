package org.constellation.admin;

import org.constellation.engine.register.ConfigurationService;
import org.constellation.security.SecurityManagerHolder;
import org.springframework.beans.factory.annotation.Autowired;


public class ConfigurationServiceInit {

    @Autowired
    private ConfigurationService configurationService;
    
    public void init() {
        ConfigurationEngine.setConfigurationService(configurationService);
        ConfigurationEngine.setSecurityManager(SecurityManagerHolder.getInstance());
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    
}
