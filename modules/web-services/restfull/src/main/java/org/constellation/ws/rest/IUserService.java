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