package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.Task;
import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.jooq.tables.records.TaskParameterRecord;
import org.constellation.engine.register.jooq.tables.records.TaskRecord;
import org.constellation.engine.register.repository.TaskParameterRepository;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.repository.TaskRepository;
import org.jooq.util.derby.sys.Sys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author Thomas Rouby (Geomatys))
 */
@Component
public class JooqTaskRepository extends AbstractJooqRespository<TaskRecord, Task> implements TaskRepository {

    public JooqTaskRepository() {
        super(Task.class, Tables.TASK);
    }


    @Override
    public void create(Task task) {
        dsl.insertInto(Tables.TASK,
                Tables.TASK.END,
                Tables.TASK.IDENTIFIER,
                Tables.TASK.MESSAGE,
                Tables.TASK.OWNER,
                Tables.TASK.START,
                Tables.TASK.STATE,
                Tables.TASK.TASK_PARAMETER_ID,
                Tables.TASK.TYPE
        ).values(task.getEnd(),
                task.getIdentifier(),
                task.getMessage(),
                task.getOwner(),
                task.getStart(),
                task.getState(),
                task.getTaskParameterId(),
                task.getType()
        ).execute();
    }

    @Override
    public Task get(String uuid) {
        return dsl.select().from(Tables.TASK).where(Tables.TASK.IDENTIFIER.eq(uuid)).fetchOneInto(Task.class);
    }

    @Override
    public void update(Task task) {
        dsl.update(Tables.TASK)
                .set(Tables.TASK.TASK_PARAMETER_ID, task.getTaskParameterId())
                .set(Tables.TASK.TYPE, task.getType())
                .set(Tables.TASK.STATE, task.getState())
                .set(Tables.TASK.START, task.getStart())
                .set(Tables.TASK.END, task.getEnd())
                .set(Tables.TASK.MESSAGE, task.getMessage())
                .set(Tables.TASK.OWNER, task.getOwner())
                .where(Tables.TASK.IDENTIFIER.eq(task.getIdentifier())).execute();
    }

    @Override
    public List<Task> findRunningTasks() {
        return dsl.select().from(Tables.TASK).where(Tables.TASK.END.isNotNull().or(Tables.TASK.END.lt(System.currentTimeMillis()))).fetchInto(Task.class);
    }
}
