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

import org.constellation.admin.service.ConstellationClient;
import org.constellation.admin.service.ConstellationServer;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Manager used to communicate with the Constellation server.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ConstellationService {

    /**
     * Constellation server URL.
     */
    private URL url;

    /**
     * Constellation configuration API version.
     */
    private String apiVersion;

    /**
     * Constellation server user login.
     */
    private String login;

    /**
     * Constellation server user password.
     */
    private String password;

    /**
     * Returns the Constellation root {@link URL}.
     *
     * @return the Constellation {@link URL}
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Returns the Constellation root {@link URL} with '/' at the end.
     *
     * @return the Constellation {@link URL}
     */
    public URL getUrlWithEndSlash() {
        if (url.toString().endsWith("/")) {
            return url;
        }
        try {
            return new URL(url + "/");
        } catch (MalformedURLException unexpected) { // should never happen
            throw new IllegalStateException("An unexpected exception occurred.", unexpected);
        }
    }

    /**
     * Sets the Constellation root {@link URL}.
     *
     * @param url the Constellation {@link URL}
     */
    public void setUrl(final URL url) {
        this.url = url;
    }

    /**
     * Returns the Constellation configuration API version.
     *
     * @return the version code
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets the Constellation configuration API version.
     *
     * @param apiVersion the version code
     */
    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Returns the Constellation user login.
     *
     * @return the user login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the Constellation user login.
     *
     * @param login the user login
     */
    public void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Returns the Constellation user password.
     *
     * @return the user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the Constellation user password.
     *
     * @param password the user password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Open the communication with the Constellation server.
     *
     * @return the opened {@link ConstellationServer} instance
     */
    public ConstellationServer openServer() {
        return openServer(false);
    }

    /**
     * Open the communication with the Constellation server.
     *
     * @param oldApiSupport indicates if the communication should be established
     *                      with the old API or with the new RestFull API
     * @return the opened {@link ConstellationServer} instance
     */
    public ConstellationServer openServer(final boolean oldApiSupport) {
        try {
            if (oldApiSupport) {
                return new ConstellationServer(new URL(getUrlWithEndSlash() + "WS"), login, password);
            } else {
                return new ConstellationServer(new URL(getUrlWithEndSlash() + "api/" + apiVersion + "/"), login, password);
            }
        } catch (MalformedURLException unexpected) { // should never happen
            throw new IllegalStateException("An unexpected exception occurred.", unexpected);
        }
    }

    /**
     *
     * @return
     */
    public ConstellationClient openClient(){
        return new ConstellationClient(getUrlWithEndSlash().toString(), apiVersion).auth(login, password);
    }
}
