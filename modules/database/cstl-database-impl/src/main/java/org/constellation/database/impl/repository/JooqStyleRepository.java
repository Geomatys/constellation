package org.constellation.database.impl.repository;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.constellation.database.api.i18n.StyleWithI18N;
import org.constellation.database.api.jooq.Tables;
import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Layer;
import org.constellation.database.api.jooq.tables.pojos.Style;
import org.constellation.database.api.jooq.tables.pojos.StyleI18n;
import org.constellation.database.api.jooq.tables.records.StyleRecord;
import org.constellation.database.api.jooq.tables.records.StyledDataRecord;
import org.constellation.database.api.jooq.tables.records.StyledLayerRecord;
import org.constellation.database.api.pojo.StyleReference;
import org.constellation.database.api.repository.StyleRepository;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.UpdateConditionStep;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.constellation.database.api.jooq.Tables.PROVIDER;
import static org.constellation.database.api.jooq.Tables.STYLE;
import static org.constellation.database.api.jooq.Tables.STYLED_DATA;
import static org.constellation.database.api.jooq.Tables.STYLED_LAYER;

@Component
public class JooqStyleRepository extends AbstractJooqRespository<StyleRecord, Style> implements StyleRepository {

    public static final Field[] REFERENCE_FIELDS = new Field[]{
            STYLE.ID.as("id"),
            STYLE.NAME.as("name"),
            STYLE.PROVIDER.as("provider_id"),
            PROVIDER.IDENTIFIER.as("provider_identifier")};


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
    public List<Style> findByName(String name) {
        return dsl.select().from(STYLE).where(STYLE.NAME.eq(name)).fetchInto(Style.class);
    }

    @Override
    public Style findByNameAndProvider(int providerId, String name) {
        return dsl.select().from(STYLE).where(STYLE.NAME.eq(name)).and(STYLE.PROVIDER.eq(providerId)).fetchOneInto(Style.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void linkStyleToData(int styleId, int dataid) {
        StyledDataRecord link = dsl.select().from(STYLED_DATA)
                .where(STYLED_DATA.DATA.eq(dataid).and(STYLED_DATA.STYLE.eq(styleId)))
                .fetchOneInto(StyledDataRecord.class);
        if (link == null) {
            dsl.insertInto(STYLED_DATA).set(STYLED_DATA.DATA, dataid).set(STYLED_DATA.STYLE, styleId).execute();
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void unlinkStyleToData(int styleId, int dataid) {
        dsl.delete(STYLED_DATA).where(STYLED_DATA.DATA.eq(dataid).and(STYLED_DATA.STYLE.eq(styleId))).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void linkStyleToLayer(int styleId, int layerId) {
        StyledLayerRecord styledLayerRecord = dsl.select().from(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerId)).and(STYLED_LAYER.STYLE.eq(styleId))
                .fetchOneInto(StyledLayerRecord.class);
        if (styledLayerRecord == null) {

            InsertSetMoreStep<StyledLayerRecord> insert = dsl.insertInto(STYLED_LAYER).set(STYLED_LAYER.LAYER, layerId).set(STYLED_LAYER.STYLE, styleId);
            insert.execute();
            setDefaultStyleToLayer(styleId, layerId);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void setDefaultStyleToLayer(int styleId, int layerId) {
        StyledLayerRecord styledLayerRecord = dsl.select().from(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerId)).and(STYLED_LAYER.IS_DEFAULT.eq(true)).fetchOneInto(StyledLayerRecord.class);
        if (styledLayerRecord != null) {
            dsl.update(STYLED_LAYER).set(STYLED_LAYER.IS_DEFAULT, false).where(STYLED_LAYER.LAYER.eq(layerId)).execute();
        }
        UpdateConditionStep<StyledLayerRecord> update = dsl.update(STYLED_LAYER).set(STYLED_LAYER.IS_DEFAULT, true).where(STYLED_LAYER.LAYER.eq(layerId)).and(STYLED_LAYER.STYLE.eq(styleId));
        update.execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void unlinkStyleToLayer(int styleId, int layerid) {
        dsl.delete(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerid).and(STYLED_LAYER.STYLE.eq(styleId))).execute();

    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteStyle(int providerId, String name) {
        dsl.delete(STYLE).where(STYLE.PROVIDER.eq(providerId).and(STYLE.NAME.eq(name))).execute();

    }

    @Override
    public List<Data> getLinkedData(int styleId) {
        return dsl.select().from(STYLED_DATA).where(STYLED_DATA.STYLE.eq(styleId)).fetchInto(Data.class);
    }

    @Override
    public List<Integer> getStyleIdsForData(int id) {
        return dsl.select(STYLE.ID).from(STYLE).join(STYLED_DATA).on(STYLED_DATA.STYLE.eq(STYLE.ID)).where(STYLED_DATA.DATA.eq(id)).fetch(STYLE.ID);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
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
    @Transactional(propagation = Propagation.MANDATORY)
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

    @Override
    public StyleWithI18N getStyleWithI18Ns(Style style) {
        Result<Record> fetch = dsl.select().from(Tables.STYLE_I18N).where(Tables.STYLE_I18N.STYLE_ID.eq(style.getId())).fetch();
        ImmutableMap<String, StyleI18n> styleI18ns = Maps.uniqueIndex(fetch.into(StyleI18n.class), new Function<StyleI18n, String>() {
            @Override
            public String apply(StyleI18n input) {
                return input.getLang();
            }
        });
        return new StyleWithI18N(style, styleI18ns);
    }

    @Override
    public boolean existsById(int styleId) {
        return dsl.selectCount().from(STYLE)
                .where(STYLE.ID.eq(styleId))
                .fetchOne(0, Integer.class) > 0;
    }

    @Override
    public List<StyleReference> fetchByDataId(int dataId) {
        return dsl.select(REFERENCE_FIELDS).from(STYLE)
                .join(STYLED_DATA).on(STYLED_DATA.STYLE.eq(STYLE.ID)) // style -> styled_data
                .join(PROVIDER).on(PROVIDER.ID.eq(STYLE.PROVIDER)) // style -> provider
                .where(STYLED_DATA.DATA.eq(dataId))
                .fetchInto(StyleReference.class);
    }
}
