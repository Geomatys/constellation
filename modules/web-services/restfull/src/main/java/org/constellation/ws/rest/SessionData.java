package org.constellation.ws.rest;

import java.io.Serializable;

public interface SessionData extends Serializable {

    int getActiveDomainId();

    void setActiveDomain(int activeDomain);
    
}
