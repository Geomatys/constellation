package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.jooq.tables.records.TaskParameterRecord;
import org.constellation.engine.register.repository.TaskParameterRepository;
import org.constellation.engine.register.jooq.Tables;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author Thomas Rouby (Geomatys))
 */
@Component
public class JooqTaskParameterRepository extends AbstractJooqRespository<TaskParameterRecord, TaskParameter> implements TaskParameterRepository {

    public JooqTaskParameterRepository() {
        super(TaskParameter.class, Tables.TASK_PARAMETER);
    }

    @Override
    public void create(TaskParameter task) {
        dsl.insertInto(Tables.TASK_PARAMETER, Tables.TASK_PARAMETER.NAME, Tables.TASK_PARAMETER.OWNER, Tables.TASK_PARAMETER.DATE, Tables.TASK_PARAMETER.PROCESS_AUTHORITY, Tables.TASK_PARAMETER.PROCESS_CODE, Tables.TASK_PARAMETER.INPUTS)
                .values(task.getName(), task.getOwner(), new Date().getTime(), task.getProcessAuthority(), task.getProcessCode(), task.getInputs())
                .execute();
    }

    @Override
    public TaskParameter get(Integer uuid) {
        return dsl.select().from(Tables.TASK_PARAMETER).where(Tables.TASK_PARAMETER.ID.eq(uuid)).fetchOne().into(TaskParameter.class);
    }

    @Override
    public void delete(TaskParameter task) {
        dsl.delete(Tables.TASK_PARAMETER).where(Tables.TASK_PARAMETER.ID.eq(task.getId())).execute();
    }

    @Override
    public void update(TaskParameter task) {
        dsl.update(Tables.TASK_PARAMETER)
                .set(Tables.TASK_PARAMETER.NAME, task.getName())
                .set(Tables.TASK_PARAMETER.DATE, new Date().getTime())
                .set(Tables.TASK_PARAMETER.PROCESS_AUTHORITY, task.getProcessAuthority())
                .set(Tables.TASK_PARAMETER.PROCESS_CODE, task.getProcessCode())
                .set(Tables.TASK_PARAMETER.INPUTS, task.getInputs())
                .where(Tables.TASK_PARAMETER.ID.eq(task.getId()))
                .execute();
    }
}
