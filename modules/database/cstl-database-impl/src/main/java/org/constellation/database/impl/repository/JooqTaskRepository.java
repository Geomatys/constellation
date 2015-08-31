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
package org.constellation.database.impl.repository;

import static org.constellation.database.api.jooq.Tables.TASK_PARAMETER;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.constellation.database.api.jooq.Tables;
import org.constellation.database.api.jooq.tables.pojos.Task;
import org.constellation.database.api.jooq.tables.records.TaskRecord;
import org.constellation.database.api.repository.TaskRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        TaskRecord newRecord = dsl.newRecord(Tables.TASK);
        if(task.getDateStart()==null)
        	task.setDateStart(System.currentTimeMillis());
        newRecord.from(task);
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


    @Override
    public List<Task> findDayTask(String process_authority) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        Long minValue = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Long maxValue = calendar.getTimeInMillis();
        
        System.out.println(dsl.select().from(Tables.TASK).join(TASK_PARAMETER).onKey()
                .where(TASK_PARAMETER.PROCESS_AUTHORITY.eq(process_authority)).and(Tables.TASK.DATE_END.between(minValue, maxValue))
                .orderBy(Tables.TASK.DATE_END.desc()).getSQL());
        
        return dsl.select().from(Tables.TASK).join(TASK_PARAMETER).onKey()
                .where(TASK_PARAMETER.PROCESS_AUTHORITY.eq(process_authority)).and(Tables.TASK.DATE_END.between(minValue, maxValue))
                .orderBy(Tables.TASK.DATE_END.desc())
                .fetchInto(Task.class);
    }
}
