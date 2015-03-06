package org.constellation.services.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base implementation. Downstream project should extends this class and expose
 * it to spring application context.
 * 
 * @author olivier.nouguier@geomatys.com
 *
 */
public class CstlSDIAuthorizationService {

    static final Logger LOGGER = LoggerFactory.getLogger(CstlSDIAuthorizationService.class);

    Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Will be call on action
     * 
     * @param serviceType
     * @param idendifier
     * @param action
     *            (start, stop, ...)
     * @return
     */
    public boolean hasAccessToService(String serviceType, String idendifier, String action) {
        return true;
    }

}
