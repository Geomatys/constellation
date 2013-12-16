package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.TaskEntity;
import org.constellation.engine.register.repository.TaskRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskJpaRepository extends JpaRepository<TaskEntity, String>, TaskRepository {

}
