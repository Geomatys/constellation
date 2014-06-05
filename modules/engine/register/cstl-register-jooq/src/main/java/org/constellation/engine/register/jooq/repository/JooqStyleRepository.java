package org.constellation.engine.register.jooq.repository;

import java.util.List;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.StyleRecord;
import org.constellation.engine.register.repository.StyleRepository;
import org.springframework.stereotype.Component;


@Component
public class JooqStyleRepository extends AbstractJooqRespository<StyleRecord, Style> implements StyleRepository {

    public JooqStyleRepository() {
        super(Style.class, Tables.STYLE);
    }

    @Override
    public List<Style> findByData(Data data) {
        // TODO Auto-generated method stub
        return null;
    }

}
