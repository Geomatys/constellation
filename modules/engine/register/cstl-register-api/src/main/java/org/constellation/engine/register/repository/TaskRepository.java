package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Task;

public interface TaskRepository {

    List<? extends Task> findAll();
    
}
