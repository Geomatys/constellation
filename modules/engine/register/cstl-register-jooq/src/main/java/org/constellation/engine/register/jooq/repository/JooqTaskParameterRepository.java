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

import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.helper.TaskParameterHelper;
import org.constellation.engine.register.jooq.tables.records.TaskParameterRecord;
import org.constellation.engine.register.repository.TaskParameterRepository;
import org.constellation.engine.register.jooq.Tables;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.constellation.engine.register.jooq.Tables.DOMAIN;

/**
 * @author Thomas Rouby (Geomatys)
 * @author Christophe Mourette (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
@Component
public class JooqTaskParameterRepository extends AbstractJooqRespository<TaskParameterRecord, TaskParameter> implements TaskParameterRepository {

    public JooqTaskParameterRepository() {
        super(TaskParameter.class, Tables.TASK_PARAMETER);
    }

    @Override
    public List<? extends TaskParameter> findAllByType(String type) {
        return findBy(Tables.TASK_PARAMETER.TYPE.eq(type));
    }

    @Override
    public List<? extends TaskParameter> findAllByNameAndProcess(String name, String authority, String code) {
        return findBy(Tables.TASK_PARAMETER.NAME.eq(name)
                .and(Tables.TASK_PARAMETER.PROCESS_AUTHORITY.eq(authority))
                .and(Tables.TASK_PARAMETER.PROCESS_CODE.eq(code)));
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
                .set(Tables.TASK_PARAMETER.TRIGGER_TYPE, task.getTriggerType())
                .set(Tables.TASK_PARAMETER.TRIGGER, task.getTrigger())
                .where(Tables.TASK_PARAMETER.ID.eq(task.getId()))
                .execute();
    }

    @Override
    public List<? extends TaskParameter> findProgrammedTasks() {
        return dsl.select().from(Tables.TASK_PARAMETER).where(Tables.TASK_PARAMETER.TRIGGER.isNotNull()).fetch().into(TaskParameter.class);
    }
}
