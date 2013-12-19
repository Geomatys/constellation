package org.constellation.security;

import javax.imageio.spi.ServiceRegistry;

public class SecurityManagerHolder {

    /**
     * Security manager.
     */
    private static final SecurityManager INSTANCE = ServiceRegistry.lookupProviders(org.constellation.security.SecurityManager.class).next();

    public static SecurityManager getInstance() {
        return INSTANCE;
    }

    
}
