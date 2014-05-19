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
package org.constellation.gui.service;

import juzu.Mapped;

/**
 * POJO {@link org.constellation.configuration.Instance} mirror
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@Mapped
public class InstanceSummary {

    /**
     * service name
     */
    private String name;

    /**
     * service name
     */
    private String identifier;

    /**
     * service type
     */
    private String type;

    /**
     * service status
     */
    private String status;

    /**
     * service abstract
     */
    private String _abstract;

    /**
     * service layer number
     */
    private int layersNumber;

    /**
     * GetCapabilities URL
     */
    private String capabilitiesUrl;

    /**
     * Logs access URL.
     */
    private String logsURL;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public int getLayersNumber() {
        return layersNumber;
    }

    public void setLayersNumber(int layersNumber) {
        this.layersNumber = layersNumber;
    }

    public String getCapabilitiesUrl() {
        return capabilitiesUrl;
    }

    public void setCapabilitiesUrl(final String capabilitiesUrl) {
        this.capabilitiesUrl = capabilitiesUrl;
    }
    
    public String getLogsURL() {
        return logsURL;
    }
    
    public void setLogsURL(String logsURL) {
        this.logsURL = logsURL;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }
}
