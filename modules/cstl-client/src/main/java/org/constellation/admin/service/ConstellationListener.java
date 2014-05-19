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

package org.constellation.admin.service;

import org.opengis.parameter.ParameterValueGroup;

import java.util.EventListener;


/**
 *
 * @author Cédric Briançon (Geomatys)
 */
public interface ConstellationListener extends EventListener{
    void providerCreated(final String serviceName, final ParameterValueGroup config);
    void providerDeleted(final String id);
    void providerUpdated(final String serviceName, final String id, final ParameterValueGroup config);
}
