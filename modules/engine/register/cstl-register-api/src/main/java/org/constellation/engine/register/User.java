package org.constellation.engine.register;

import java.util.List;

public interface User {

    void setLastname(String lastname);

    String getLastname();

    void setFirstname(String firstname);

    String getFirstname();

    String getEmail();
    
    void setEmail(String email);
    
    void setPassword(String password);

    String getPassword();

    void setLogin(String login);

    String getLogin();

    List<? extends Role> getRoles();

}