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
