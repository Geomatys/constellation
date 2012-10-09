/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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

package org.constellation.security;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ActionPermissions {

    /**
     * Permission to display a service instance.
     */
    public static final String VIEW_SERVICE = "viewService";

    /**
     * Permission to create a new service instance.
     */
    public static final String NEW_SERVICE = "newService";

    /**
     * Permission to edit a service instance.
     */
    public static final String EDIT_SERVICE = "editService";

    /**
     * Permission to edit a service instance.
     */
    public static final String RELOAD_SERVICE = "reloadService";

    /**
     * Permission to start/stop a service instance.
     */
    public static final String START_STOP_SERVICE = "startStopService";

    /**
     * Permission to display a provider instance.
     */
    public static final String VIEW_PROVIDER = "viewProvider";

    /**
     * Permission to create a new provider instance.
     */
    public static final String NEW_PROVIDER = "newProvider";

    /**
     * Permission to edit a service instance.
     */
    public static final String EDIT_PROVIDER = "editProvider";

    /**
     * Permission to edit a service instance.
     */
    public static final String RELOAD_PROVIDER = "reloadProvider";

    public static final List<String> VALUES = new ArrayList<String>();
    static {
        VALUES.add(VIEW_SERVICE);
        VALUES.add(NEW_SERVICE);
        VALUES.add(EDIT_SERVICE);
        VALUES.add(RELOAD_SERVICE);
        VALUES.add(START_STOP_SERVICE);
        VALUES.add(VIEW_PROVIDER);
        VALUES.add(NEW_PROVIDER);
        VALUES.add(EDIT_PROVIDER);
        VALUES.add(RELOAD_PROVIDER);
    }
}
