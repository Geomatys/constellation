package org.constellation.services;

import org.constellation.ws.rest.SessionData;

public class SessionDataImpl implements SessionData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Active domainId, this domain will be linked to new resources (data,
     * layer, provider or service).
     */
    private int activeDomain = 1;

    @Override
    public int getActiveDomainId() {
        return activeDomain;
    }

    @Override
    public void setActiveDomain(int activeDomain) {
        this.activeDomain = activeDomain;
    }

}
