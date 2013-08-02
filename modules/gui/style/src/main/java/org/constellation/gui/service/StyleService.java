/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.gui.binding.Style;
import org.geotoolkit.style.MutableStyle;
import org.opengis.feature.type.FeatureType;

import java.io.IOException;
import java.net.URL;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Manager for style provider operations.
 * <p>
 * This bean is request scoped.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class StyleService {

    /**
     * Constellation server URL.
     */
    private String constellationUrl;

    /**
     * Constellation server user login.
     */
    private String login;

    /**
     * Constellation server user password.
     */
    private String password;

    /**
     * Set the constellation server URL.
     */
    public void setConstellationUrl(final String constellationUrl) {
        this.constellationUrl = constellationUrl;
    }

    /**
     * Set the constellation server user login.
     *
     * @param login the user login
     */
    public void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Set the constellation server user password.
     *
     * @param password the user password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Gets a {@link MutableStyle} instance form constellation server.
     *
     * @param providerId the provider id
     * @param styleName  the style name
     * @return the {@link MutableStyle} instance
     * @throws java.io.IOException if failed to acquire or parse style for any reason
     */
    public MutableStyle getStyle(final String providerId, final String styleName) throws IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName", styleName);
        try {
            final URL url = new URL(constellationUrl.substring(0, constellationUrl.indexOf("api")) + "WS");
            final ConstellationServer server = new ConstellationServer(url, login, password);
            return server.providers.downloadStyle(providerId, styleName);
        } catch (Exception ex) {
            throw new IOException("The style named \"" + styleName + "\" for provider with id \"" + providerId + "\" doesn't exists.");
        }
    }

    /**
     * Gets a style JSON representation form constellation server.
     *
     * @param providerId the provider id
     * @param styleName  the style name
     * @return the style JSON representation
     * @throws java.io.IOException if failed to acquire or parse style for any reason
     */
    public String getStyleJSON(final String providerId, final String styleName) throws IOException {
        final MutableStyle mutableStyle = this.getStyle(providerId, styleName);
        final Style style = new Style(mutableStyle);
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(style);
    }

    /**
     * Updates a {@link MutableStyle} instance on the constellation server.
     *
     * @param providerId the provider id
     * @param styleName  the style name
     * @param style      the new style
     * @throws java.io.IOException if failed to update style for any reason
     */
    public void updateStyle(final String providerId, final String styleName, final MutableStyle style) throws IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName", styleName);
        try {
            final URL url = new URL(constellationUrl.substring(0, constellationUrl.indexOf("api")) + "WS");
            final ConstellationServer server = new ConstellationServer(url, login, password);
            server.providers.updateStyle(providerId, styleName, style);
        } catch (Exception ex) {
            throw new IOException("Failed to update the style named \"" + styleName + "\" in provider with id \"" + providerId + "\".");
        }
    }

    /**
     * Updates a {@link MutableStyle} instance on the constellation server.
     *
     * @param providerId the provider id
     * @param styleName  the style name
     * @param json       the new style JSON representation
     * @throws java.io.IOException if failed to update style for any reason
     */
    public void updateStyleJSON(final String providerId, final String styleName, final String json) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Style style = mapper.readValue(json, Style.class);
        this.updateStyle(providerId, styleName, style.toType());
    }

    /**
     * Gets a layer {@link org.opengis.feature.type.FeatureType} instance from the constellation server.
     *
     * @param providerId the provider id
     * @param layerName  the layer name
     * @throws java.io.IOException if failed to update style for any reason
     */
    public FeatureType getLayerFeatureType(final String providerId, final String layerName) throws IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName", layerName);
        try {
            final URL url = new URL(constellationUrl.substring(0, constellationUrl.indexOf("api")) + "WS");
            final ConstellationServer server = new ConstellationServer(url, login, password);
            return server.providers.getLayerFeatureType(providerId, layerName);
        } catch (Exception ex) {
            throw new IOException("Unable to get feature type for layer named \"" + layerName + "\" on the provider with id \"" + providerId + "\".");
        }
    }
}
