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
package org.constellation.services;

import org.constellation.ws.rest.SessionData;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value="session", proxyMode=ScopedProxyMode.INTERFACES)
public class SessionDataImpl implements SessionData {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Active domainId, this domain will be linked to new resources (data,
     * layer, provider or service).
     */
    private int activeDomainId = 0;

    @Override
    public int getActiveDomainId() {
        return activeDomainId;
    }

    @Override
    public void setActiveDomain(int activeDomainId) {
        this.activeDomainId = activeDomainId;
    }

}
