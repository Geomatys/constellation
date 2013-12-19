package org.constellation.engine.register;

public interface User {

    public abstract void setName(String name);

    public abstract String getName();

    public abstract void setPassword(String password);

    public abstract String getPassword();

    public abstract void setLogin(String login);

    public abstract String getLogin();

}