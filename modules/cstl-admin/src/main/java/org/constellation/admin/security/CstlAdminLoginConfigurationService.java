package org.constellation.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("cstlAdminLoginConfigurationService")
public class CstlAdminLoginConfigurationService {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CstlAdminLoginConfigurationService.class);
    
    private String cstlLoginURL = "login.html";
    
    public CstlAdminLoginConfigurationService() {
         LOGGER.info("***** CstlAdminLoginConfigurationService construct *****");
    }
    
    
    public String getCstlLoginURL() {
        return cstlLoginURL;
    }
    
    public void setCstlLoginURL(String cstlLoginURL) {
        LOGGER.info("CSTL Login page changed to " + cstlLoginURL);
        this.cstlLoginURL = cstlLoginURL;
    }
    

}
