package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.User;


public interface UserRepository {

    List<User> all();

    void insert(User user);

    void update(User user);
    
    void delete(String string);
    
    User findOneWithRolesAndDomains(String login);
    
}
