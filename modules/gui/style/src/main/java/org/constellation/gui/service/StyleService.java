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
import org.constellation.dto.DataDescription;
import org.constellation.dto.StyleBean;
import org.constellation.dto.StyleListBean;
import org.constellation.gui.binding.Style;
import org.geotoolkit.style.MutableStyle;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

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
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * Gets a {@link MutableStyle} body form constellation server.
     *
     * @param providerId the style provider id
     * @param styleName  the style name
     * @return the {@link Style} instance
     * @throws IOException if failed to acquire/parse response for any reason
     */
    public Style getStyle(final String providerId, final String styleName) throws IOException {
        final MutableStyle style;
        try {
            style = cstl.openServer(true).providers.downloadStyle(providerId, styleName);
        } catch (Exception ex) {
            throw new IOException("The style named \"" + styleName + "\" for provider with id \"" + providerId + "\" doesn't exists.");
        }
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
     * @throws IOException if failed to update style for any reason
     */
    public void updateStyle(final String providerId, final String styleName, final Style style) throws IOException {
        try {
            cstl.openServer(true).providers.updateStyle(providerId, styleName, style.toType());
        } catch (Exception ex) {
            throw new IOException("Failed to update the style named \"" + styleName + "\" in provider with id \"" + providerId + "\".");
        }
    }

    /**
     * Create a {@link MutableStyle} on the constellation server.
     *
     * @param providerId the style provider id
     * @param styleName  the style name
     * @param style      the new style content
     * @throws IOException if failed to create style for any reason
     */
    public void createStyle(final String providerId, final String styleName, final Style style) throws IOException {
        try {
            cstl.openServer(true).providers.createStyle(providerId, styleName, style.toType());
        } catch (Exception ex) {
            throw new IOException("Failed to create the style named \"" + styleName + "\" in provider with id \"" + providerId + "\".");
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
        final DataDescription dataDescription;
        try {
            dataDescription = cstl.openServer().providers.getLayerDataDescription(providerId, layerName);
        } catch (Exception ex) {
            throw new IOException("Unable to get data info for layer named \"" + layerName + "\" for provider with id \"" + providerId + "\".");
        }
        if (dataDescription == null) {
            throw new IOException("Null response for layer named \"" + layerName + "\" for provider with id \"" + providerId + "\".");
        }
        return dataDescription;
    }

    /**
     *
     * @return
     */
    public StyleListBean getStyleList() throws IOException {
        return cstl.openClient().providers.getStyleList();
    }
}