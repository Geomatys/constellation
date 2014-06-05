package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.STYLE;
import static org.constellation.engine.register.jooq.Tables.STYLED_DATA;

import java.util.List;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.jooq.tables.records.StyleRecord;
import org.constellation.engine.register.jooq.tables.records.StyledDataRecord;
import org.constellation.engine.register.repository.StyleRepository;
import org.jooq.InsertSetMoreStep;
import org.springframework.stereotype.Component;


@Component
public class JooqStyleRepository extends AbstractJooqRespository<StyleRecord, Style> implements StyleRepository {

    public JooqStyleRepository() {
        super(Style.class, STYLE);
    }

    @Override
    public List<Style> findByData(Data data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Style findById(int id) {
        return dsl.select().from(STYLE).where(STYLE.ID.eq(id)).fetchOneInto(Style.class);
    }

    @Override
    public Style findByName(String name) {
        return dsl.select().from(STYLE).where(STYLE.NAME.eq(name)).fetchOneInto(Style.class);
    }

    @Override
    public void linkStyleToData(int styleId, int dataid) {
        InsertSetMoreStep<StyledDataRecord> insert = dsl.insertInto(STYLED_DATA).set(STYLED_DATA.DATA, dataid).set(STYLED_DATA.STYLE, styleId);
        insert.execute();
        
    }

    @Override
    public void unlinkStyleToData(int styleId, int dataid) {
        dsl.delete(STYLED_DATA).where(STYLED_DATA.DATA.eq(dataid).and(STYLED_DATA.STYLE.eq(styleId))).execute();
    }

}
