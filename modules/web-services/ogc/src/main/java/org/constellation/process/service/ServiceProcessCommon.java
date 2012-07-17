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
package org.constellation.process.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.constellation.ws.WSEngine;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class ServiceProcessCommon {

    private ServiceProcessCommon() {
    }

    public static String[] servicesAvaible() {

        final List<String> validValues = new ArrayList <String>();
        final Map<String, List<String>> regisredService = WSEngine.getRegisteredServices();
        for (Map.Entry<String, List<String>> service : regisredService.entrySet()) {
            validValues.add(service.getKey());
        }

        return validValues.toArray(new String[validValues.size()]);
    }

}
