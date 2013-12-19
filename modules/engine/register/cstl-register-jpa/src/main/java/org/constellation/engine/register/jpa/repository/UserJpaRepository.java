package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.User;
import org.constellation.engine.register.jpa.UserEntity;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String>, UserRepository {

    @Transactional
    void save(User user);
    
    @Transactional
    void delete(String id);

}
