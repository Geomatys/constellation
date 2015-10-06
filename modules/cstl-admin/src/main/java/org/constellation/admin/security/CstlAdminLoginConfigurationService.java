package org.constellation.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("cstlAdminLoginConfigurationService")
public class CstlAdminLoginConfigurationService {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CstlAdminLoginConfigurationService.class);
    
    private String cstlLoginURL = "login.html";
    private String cstlLogoutURL = null;
    private String cstlRefreshURL = null;

    public CstlAdminLoginConfigurationService() {
         LOGGER.debug("***** CstlAdminLoginConfigurationService construct *****");
    }
    
    
    public String getCstlLoginURL() {
        return cstlLoginURL;
    }
    
    public void setCstlLoginURL(String cstlLoginURL) {
        LOGGER.info("CSTL Login page changed to " + cstlLoginURL);
        this.cstlLoginURL = cstlLoginURL;
    }

    /**
     * Logout URL
     * @return Cstl logout URL, can be null
     */
    public String getCstlLogoutURL() {
        return cstlLogoutURL;
    }

    public void setCstlLogoutURL(String cstlLogoutURL) {
        LOGGER.info("CSTL Logout page changed to " + cstlLogoutURL);
        this.cstlLogoutURL = cstlLogoutURL;
    }

    /**
     * Refresh token URL
     * @return refresh URL, can be null
     */
    public String getCstlRefreshURL() {
        return cstlRefreshURL;
    }

    public void setCstlRefreshURL(String cstlRefreshURL) {
        LOGGER.info("CSTL Refresh token page changed to " + cstlRefreshURL);
        this.cstlRefreshURL = cstlRefreshURL;
    }
}
