package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.User;


public interface UserRepository {

    List<? extends User> findAll();

    void save(User user);

    void saveAndFlush(User entity);

    void delete(String string);
    
}
