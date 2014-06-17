package org.constellation.engine.register.jooq.repository;

import java.util.List;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Style;
import static org.constellation.engine.register.jooq.Tables.STYLE;
import static org.constellation.engine.register.jooq.Tables.STYLED_DATA;
import static org.constellation.engine.register.jooq.Tables.STYLED_LAYER;
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
    public List<Style> findByType(String type) {
        return dsl.select().from(STYLE).where(STYLE.TYPE.eq(type)).fetchInto(Style.class);
    }

    @Override
    public List<Style> findByTypeAndProvider(final int providerId, String type) {
        return dsl.select().from(STYLE).where(STYLE.TYPE.eq(type)).and(STYLE.PROVIDER.eq(providerId)).fetchInto(Style.class);
    }

    @Override
    public List<Style> findByProvider(final int providerId) {
        return dsl.select().from(STYLE).where(STYLE.PROVIDER.eq(providerId)).fetchInto(Style.class);
    }

    @Override
    public List<Style> findByLayer(Layer layer) {
        return dsl.select(STYLE.fields()).from(STYLE).join(STYLED_LAYER).onKey()
                .where(STYLED_LAYER.LAYER.eq(layer.getId())).fetchInto(Style.class);
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
    public Style findByNameAndProvider(int providerId, String name) {
        return dsl.select().from(STYLE).where(STYLE.NAME.eq(name)).and(STYLE.PROVIDER.eq(providerId)).fetchOneInto(Style.class);
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
        if (styledLayerRecord != null) {
            dsl.update(STYLED_LAYER).set(STYLED_LAYER.DEFAULT, false).where(STYLED_LAYER.LAYER.eq(layerId)).execute();
        }
        dsl.update(STYLED_LAYER).set(STYLED_LAYER.DEFAULT, true).where(STYLED_LAYER.LAYER.eq(layerId)).and(STYLED_LAYER.STYLE.eq(styleId)).execute();
    }

    @Override
    public void unlinkStyleToLayer(int styleId, int layerid) {
        dsl.delete(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerid).and(STYLED_LAYER.STYLE.eq(styleId))).execute();

    }

    @Override
    public void deleteStyle(int providerId, String name) {
        dsl.delete(STYLE).where(STYLE.PROVIDER.eq(providerId).and(STYLE.NAME.eq(name))).execute();

    }

    @Override
    public List<org.constellation.engine.register.Data> getLinkedData(int styleId) {
        return dsl.select().from(STYLED_DATA).where(STYLED_DATA.STYLE.eq(styleId)).fetchInto(Data.class);
    }

    @Override
    public List<Integer> getStyleIdsForData(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int create(Style style) {
        StyleRecord styleRecord = dsl.newRecord(STYLE);
        styleRecord.setBody(style.getBody());
        styleRecord.setDate(style.getDate());
        styleRecord.setName(style.getName());
        styleRecord.setOwner(style.getOwner());
        styleRecord.setProvider(style.getProvider());
        styleRecord.setType(style.getType());
        styleRecord.store();
        return styleRecord.getId();
    }

    @Override
    public Style save(Style s) {
        dsl.update(STYLE)
                .set(STYLE.DATE, s.getDate())
                .set(STYLE.BODY, s.getBody())
                .set(STYLE.NAME, s.getName())
                .set(STYLE.PROVIDER, s.getProvider())
                .set(STYLE.OWNER, s.getOwner())
                .set(STYLE.TYPE, s.getType())
                .where(STYLE.ID.eq(s.getId())).execute();
        return s;
    }
}
