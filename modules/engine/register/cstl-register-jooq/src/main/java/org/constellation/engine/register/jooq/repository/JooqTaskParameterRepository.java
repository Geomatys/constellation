package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.Task;
import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.helper.TaskParameterHelper;
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
    public TaskParameter create(TaskParameter task ) {

                TaskParameterRecord newRecord = TaskParameterHelper.copy(task, dsl.newRecord(Tables.TASK_PARAMETER));
                newRecord.store();
                return newRecord.into(TaskParameter.class);

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

    @Override
    public List<? extends TaskParameter> findProgrammedTasks() {
        return dsl.select().from(Tables.TASK_PARAMETER).where(Tables.TASK_PARAMETER.TRIGGER.isNotNull()).fetch().into(TaskParameter.class);
    }
}
