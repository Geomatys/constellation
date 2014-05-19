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
package org.constellation.ws.rest;

import javax.ws.rs.core.Response;

import org.constellation.engine.register.User;

public interface IUserService {

    /**
     * @return a {@link Response} which contains requester user name
     */
    public abstract Response findOne(String login);

    /**
     * @return a {@link Response} which contains requester user name
     */
    public abstract Response findAll(String login);

    public abstract Response delete(String id);

    public abstract Response post(User userDTO);

    Response put(User userDTO);

}