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

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import org.jooq.TableLike;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractJooqRespository<T extends Record, U> {

    @Autowired
    DSLContext dsl;

    private Class<U> dtoClass;
    
    private TableLike<T> table;

    public AbstractJooqRespository(Class<U> dtoClass, TableLike<T> table) {
        this.dtoClass = dtoClass;
        this.table = table;
    }

    
     interface Predicate<U> {
         boolean match(U t);
     }
    
   <V> List<V> filter(List<V> list, Predicate<V> p) {
        List<V> ret = new ArrayList<V>();
        for (V t : list) {
            if(p.match(t))
                ret.add(t);
        }
        return ret;        
    }
    
    List<? extends U> mapResult(SelectQuery<T> selectQuery) {
        if (selectQuery.execute() > 0)
            return selectQuery.getResult().map(getDTOMapper());
        return Collections.emptyList();
    }

    RecordMapper<? super T, U> getDTOMapper() {
        return new RecordMapper<Record, U>() {

            @Override
            public U map(Record record) {
                return record.into(dtoClass);
            }
        };
    }

    public List<U> findAll() {
        SelectQuery<T> selectQuery = dsl.selectQuery(table);
        selectQuery.execute();
        return selectQuery.getResult().map(getDTOMapper());
    }

    protected List<U> findBy(Condition condition) {
        return dsl.select().from(table).where(condition).fetchInto(dtoClass);
    }
   
}
