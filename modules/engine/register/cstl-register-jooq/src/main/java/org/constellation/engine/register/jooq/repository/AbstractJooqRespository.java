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
