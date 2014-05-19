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
