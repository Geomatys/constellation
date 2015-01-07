/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.Task;
import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.helper.TaskHelper;
import org.constellation.engine.register.helper.TaskParameterHelper;
import org.constellation.engine.register.jooq.tables.records.TaskParameterRecord;
import org.constellation.engine.register.jooq.tables.records.TaskRecord;
import org.constellation.engine.register.repository.TaskParameterRepository;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.repository.TaskRepository;
import org.jooq.util.derby.sys.Sys;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.constellation.engine.register.jooq.Tables.DOMAIN;

/**
 * @author Thomas Rouby (Geomatys)
 * @author Christophe Mourette (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
@Component
public class JooqTaskRepository extends AbstractJooqRespository<TaskRecord, Task> implements TaskRepository {

    public JooqTaskRepository() {
        super(Task.class, Tables.TASK);
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Task create(Task task) {
        TaskRecord newRecord = TaskHelper.copy(task, dsl.newRecord(Tables.TASK));
        newRecord.store();
        return newRecord.into(Task.class);
    }

    @Override
    public Task get(String uuid) {
        return dsl.select().from(Tables.TASK).where(Tables.TASK.IDENTIFIER.eq(uuid)).fetchOneInto(Task.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(Task task) {
        dsl.update(Tables.TASK)
                .set(Tables.TASK.TASK_PARAMETER_ID, task.getTaskParameterId())
                .set(Tables.TASK.TYPE, task.getType())
                .set(Tables.TASK.STATE, task.getState())
                .set(Tables.TASK.DATE_START, task.getDateStart())
                .set(Tables.TASK.DATE_END, task.getDateEnd())
                .set(Tables.TASK.MESSAGE, task.getMessage())
                .set(Tables.TASK.OWNER, task.getOwner())
                .set(Tables.TASK.PROGRESS, task.getProgress())
                .set(Tables.TASK.TASK_OUTPUT, task.getTaskOutput())
                .where(Tables.TASK.IDENTIFIER.eq(task.getIdentifier())).execute();
    }

    @Override
    public List<Task> findRunningTasks() {
        return dsl.select().from(Tables.TASK)
                .where(Tables.TASK.DATE_END.isNull())
                .fetchInto(Task.class);
    }

    @Override
    public List<Task> findRunningTasks(Integer id, Integer offset, Integer limit) {
        return dsl.select().from(Tables.TASK)
                .where(Tables.TASK.DATE_END.isNull().and(Tables.TASK.TASK_PARAMETER_ID.eq(id)))
                .orderBy(Tables.TASK.DATE_END.desc())
                .limit(limit).offset(offset)
                .fetchInto(Task.class);
    }

    @Override
    public List<Task> taskHistory(Integer id, Integer offset, Integer limit) {
        return dsl.select().from(Tables.TASK)
                .where(Tables.TASK.TASK_PARAMETER_ID.eq(id))
                .orderBy(Tables.TASK.DATE_END.desc())
                .limit(limit).offset(offset)
                .fetchInto(Task.class);
    }
}
