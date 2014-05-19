/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.engine.register.jooq.repository;

import java.util.Collections;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import org.jooq.TableLike;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractJooqRespository<T extends Record, U> {

    @Autowired
    DSLContext dsl;

    private Class<U> dtoClass;
    
    private TableLike<T> table;

    public AbstractJooqRespository(Class<U> dtoClass, TableLike<T> table) {
        this.dtoClass = dtoClass;
        this.table = table;
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

   
}
