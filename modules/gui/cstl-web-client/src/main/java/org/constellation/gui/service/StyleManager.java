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

import org.constellation.admin.service.ConstellationServer;
import org.constellation.gui.util.StyleEditionUtilities;
import org.constellation.gui.util.StyleEditionWorkspace;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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
public final class StyleManager {

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
     * Gets a {@link MutableStyle} instance form constellation.
     *
     * @param providerId the provider id
     * @param styleName  the style name
     * @return the {@link MutableStyle} instance
     * @throws IOException if failed to acquire or parse style for any reason
     */
    public MutableStyle getStyle(final String providerId, final String styleName) throws IOException {
        try {
            final URL url = new URL(constellationUrl.substring(0, constellationUrl.indexOf("api")) + "WS");
            final ConstellationServer server = new ConstellationServer(url, login, password);
            return server.providers.downloadStyle(providerId, styleName);
        } catch (Exception ex) {
            throw new IOException("The style named \"" + styleName + "\" for provider with id \"" + providerId +
                    "\" doesn't exists.");
        }
    }
}
