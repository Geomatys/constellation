package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.STYLE;
import static org.constellation.engine.register.jooq.Tables.STYLED_DATA;
import static org.constellation.engine.register.jooq.Tables.STYLED_LAYER;

import java.util.List;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.jooq.tables.records.StyleRecord;
import org.constellation.engine.register.jooq.tables.records.StyledDataRecord;
import org.constellation.engine.register.jooq.tables.records.StyledLayerRecord;
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
        return dsl.select(STYLE.fields()).from(STYLE).join(STYLED_DATA).onKey()
                .where(STYLED_DATA.DATA.eq(data.getId())).fetchInto(Style.class);
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

	@Override
	public void linkStyleToLayer(int styleId, int layerId) {
		StyledLayerRecord styledLayerRecord = dsl.select().from(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerId)).and(STYLED_LAYER.STYLE.eq(styleId))
		        .fetchOneInto(StyledLayerRecord.class);
		if (styledLayerRecord == null) {
			
			InsertSetMoreStep<StyledLayerRecord> insert = dsl.insertInto(STYLED_LAYER).set(STYLED_LAYER.LAYER, layerId).set(STYLED_LAYER.STYLE, styleId);
			insert.execute();
			setDefaultStyleToLayer(styleId, layerId);
		}
	}
	
	public void setDefaultStyleToLayer(int styleId, int layerId) {
		StyledLayerRecord styledLayerRecord = dsl.select().from(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerId)).and(STYLED_LAYER.DEFAULT.eq(true)).fetchOneInto(StyledLayerRecord.class);
		if (styledLayerRecord!=null) {
			dsl.update(STYLED_LAYER).set(STYLED_LAYER.DEFAULT, false).where(STYLED_LAYER.LAYER.eq(layerId)).execute();
		}
		dsl.update(STYLED_LAYER).set(STYLED_LAYER.DEFAULT, true).where(STYLED_LAYER.LAYER.eq(layerId)).and(STYLED_LAYER.STYLE.eq(styleId)).execute();
	}

	@Override
	public void unlinkStyleToLayer(int styleId, int layerid) {
		dsl.delete(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerid).and(STYLED_LAYER.STYLE.eq(styleId))).execute();
		
	}

}
