/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.geotoolkit.gui.swing.tree.Trees;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlRootElement(name="LayerContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerContext {

    @XmlAttribute
    private DataSourceType implementation;
            
    private Layers layers;

    private String security;

    private Languages supportedLanguages;

    private final Map<String, String> customParameters = new HashMap<String, String>();

    public LayerContext() {

    }

    public LayerContext(final Layers layers) {
        this.layers = layers;
    }

    public LayerContext(final Layers layers, final String security) {
        this.layers = layers;
        this.security = security;
    }

    /**
     * @return the layers
     */
    public List<Source> getLayers() {
        if (layers == null) {
            layers = new Layers();
            return layers.getSource();
        } else {
            return layers.getSource();
        }
    }


    /**
     * @param layers the layers to set
     */
    public void setLayers(final List<Source> layers) {
        this.layers = new Layers(layers);
    }

    public boolean hasSource(final String sourceID) {
        for (Source src : getLayers()) {
            if (Objects.equals(src.getId(), sourceID)) {
                return true;
            }
        }
        return false;
    }

    public void removeSource(final String sourceID) {
        for (Source src : getLayers()) {
            if (Objects.equals(src.getId(), sourceID)) {
                layers.getSource().remove(src);
                return;
            }
        }
    }

    /**
     * @return the layers
     */
    public Layer getMainLayer() {
        if (layers == null) {
            layers = new Layers();
            return layers.getMainLayer();
        } else {
            return layers.getMainLayer();
        }
    }

    /**
     * @return the security constraint, or {@code null} if none.
     */
    public String getSecurity() {
        return security;
    }

    /**
     * Sets the security value.
     *
     * @param security the security value.
     */
    public void setSecurity(String security) {
        this.security = security;
    }

    /**
     * @return the supportedLanguages
     */
    public Languages getSupportedLanguages() {
        return supportedLanguages;
    }

    /**
     * @param supportedLanguages the supportedLanguages to set
     */
    public void setSupportedLanguages(Languages supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    /**
     * @return the customParameters
     */
    public Map<String, String> getCustomParameters() {
        return customParameters;
    }
    
    /**
     * @return the implementation
     */
    public DataSourceType getImplementation() {
        return implementation;
    }

    /**
     * @param implementation the implementation to set
     */
    public void setImplementation(DataSourceType implementation) {
        this.implementation = implementation;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(Trees.toString("LayerContext", getLayers()));
        if (security != null && !security.isEmpty()) {
            sb.append("Security=").append(security);
        }
        if (supportedLanguages != null) {
            sb.append("Supported languages:\n").append(supportedLanguages);
        }
        if (implementation != null) {
            sb.append("Implementation:\n").append(implementation);
        }
        if (customParameters != null && !customParameters.isEmpty()) {
            sb.append("Custom parameters:\n");
            for (Entry<String, String> entry : customParameters.entrySet()) {
                sb.append("key:").append(entry.getKey()).append(" value:").append(entry.getValue()).append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof LayerContext) {
            final LayerContext that = (LayerContext) obj;
            return Objects.equals(this.layers, that.layers) &&
                   Objects.equals(this.security, that.security) &&
                   Objects.equals(this.customParameters, that.customParameters) &&
                   Objects.equals(this.supportedLanguages, that.supportedLanguages);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.layers != null ? this.layers.hashCode() : 0);
        hash = 97 * hash + (this.security != null ? this.security.hashCode() : 0);
        hash = 97 * hash + (this.customParameters != null ? this.customParameters.hashCode() : 0);
        hash = 97 * hash + (this.supportedLanguages != null ? this.supportedLanguages.hashCode() : 0);
        return hash;
    }
}
