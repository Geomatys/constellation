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
import org.constellation.dto.DataDescription;
import org.constellation.gui.binding.Style;
import org.geotoolkit.style.MutableStyle;

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
     * Gets a {@link MutableStyle} body form constellation server.
     *
     * @param providerId the style provider id
     * @param styleName  the style name
     * @return the {@link Style} instance
     * @throws IOException if failed to acquire/parse response for any reason
     */
    public Style getStyle(final String providerId, final String styleName) throws IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName", styleName);

        // Load style from constellation server.
        final MutableStyle style;
        try {
            final URL url = new URL(constellationUrl.substring(0, constellationUrl.indexOf("api")) + "WS");
            final ConstellationServer server = new ConstellationServer(url, login, password);
            style = server.providers.downloadStyle(providerId, styleName);
        } catch (Exception ex) {
            throw new IOException("The style named \"" + styleName + "\" for provider with id \"" + providerId + "\" doesn't exists.");
        }

        // Ensure non null response.
        if (style == null) {
            throw new IOException("Null response for style named \"" + styleName + "\" for provider with id \"" + providerId + "\".");
        }
        return new Style(style);
    }

    /**
     * Updates a {@link MutableStyle} body on the constellation server.
     *
     * @param providerId the style provider id
     * @param styleName  the style name
     * @param style      the new style content
     * @throws IOException if failed to update object for any reason
     */
    public void updateStyle(final String providerId, final String styleName, final Style style) throws IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName",  styleName);
        ensureNonNull("style",      style);

        // Update the style body on the constellation server.
        try {
            final URL url = new URL(constellationUrl.substring(0, constellationUrl.indexOf("api")) + "WS");
            final ConstellationServer server = new ConstellationServer(url, login, password);
            server.providers.updateStyle(providerId, styleName, style.toType());
        } catch (Exception ex) {
            throw new IOException("Failed to update the style named \"" + styleName + "\" in provider with id \"" + providerId + "\".");
        }
    }

    /**
     * Gets a {@link org.constellation.dto.DataDescription} object from the constellation server.
     *
     * @param providerId the provider id
     * @param layerName  the layer name
     * @return the {@link DataDescription} instance
     * @throws IOException if failed to acquire/parse response for any reason
     */
    public DataDescription getLayerDataDescription(final String providerId, final String layerName) throws IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName", layerName);

        // Load data information from constellation server.
        final DataDescription dataDescription;
        try {
            final URL url = new URL(constellationUrl);
            final ConstellationServer server = new ConstellationServer(url, login, password);
            dataDescription = server.providers.getLayerDataDescription(providerId, layerName);
        } catch (Exception ex) {
            throw new IOException("Unable to get data info for layer named \"" + layerName + "\" for provider with id \"" + providerId + "\".");
        }

        // Ensure non null response.
        if (dataDescription == null) {
            throw new IOException("Null response for layer named \"" + layerName + "\" for provider with id \"" + providerId + "\".");
        }
        return dataDescription;
    }
}