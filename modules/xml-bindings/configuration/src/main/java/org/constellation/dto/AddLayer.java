/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO on add layer request
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public class AddLayer {

    private String layerAlias;

    private String serviceType;

    private String serviceId;

    private String providerId;

    private String layerId;
    
    private String layerNamespace;

    public AddLayer() {
    }

    public AddLayer(final String layerAlias, final String serviceType, final String serviceId, 
            final String providerId, final String layerId, final String layerNamespace) {
        this.layerAlias = layerAlias;
        this.serviceType = serviceType;
        this.serviceId = serviceId;
        this.providerId = providerId;
        this.layerId = layerId;
        this.layerNamespace = layerNamespace;
    }

    public String getLayerAlias() {
        return layerAlias;
    }

    public void setLayerAlias(final String layerAlias) {
        this.layerAlias = layerAlias;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(final String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(final String serviceId) {
        this.serviceId = serviceId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(final String layerId) {
        this.layerId = layerId;
    }

    /**
     * @return the layerNamespace
     */
    public String getLayerNamespace() {
        return layerNamespace;
    }

    /**
     * @param layerNamespace the layerNamespace to set
     */
    public void setLayerNamespace(String layerNamespace) {
        this.layerNamespace = layerNamespace;
    }
    
    @Override
    public String toString() {
        return "addLayer{" +
                "layerAlias='" + layerAlias + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", layerId='" + layerId + '\'' +
                ", layerNamespace='" + layerNamespace + '\'' +
                '}';
    }
}
