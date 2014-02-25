package org.constellation.engine.register.jpa.repository;

import java.util.List;

import org.constellation.engine.register.User;
import org.constellation.engine.register.jpa.UserEntity;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String>, UserRepository {

    @Transactional
    void save(User user);
    
    @Transactional
    void delete(String id);
    
    @Query("select u from UserEntity u where u.login = :login")
    User findOneWithRole(@Param("login") String login);

    
    @Query("select u from UserEntity u")
    List<? extends User> all();

    
}
