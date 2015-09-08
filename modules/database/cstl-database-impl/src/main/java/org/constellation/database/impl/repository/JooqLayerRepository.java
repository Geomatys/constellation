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

import static org.constellation.database.api.jooq.Tables.DATA;
import static org.constellation.database.api.jooq.Tables.DATASET;
import static org.constellation.database.api.jooq.Tables.LAYER;
import static org.constellation.database.api.jooq.Tables.PROVIDER;
import static org.constellation.database.api.jooq.Tables.STYLED_LAYER;

import java.util.List;

import org.constellation.configuration.LayerSummary;
import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Layer;
import org.constellation.database.api.jooq.tables.pojos.Service;
import org.constellation.database.api.jooq.tables.records.LayerRecord;
import org.constellation.database.api.repository.LayerRepository;
import org.jooq.DeleteConditionStep;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.UpdateConditionStep;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqLayerRepository extends AbstractJooqRespository<LayerRecord, Layer> implements LayerRepository {

    /**
     * Fields use to select a lighten Layer reference objects
     */
    public static final Field[] REF_FIELDS = new Field[]{
            LAYER.ID,
            LAYER.NAME};

    public JooqLayerRepository() {

        super(Layer.class, LAYER);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int deleteServiceLayer(Service service) {
        return dsl.delete(LAYER).where(LAYER.SERVICE.eq(service.getId())).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Layer save(Layer layer) {
        LayerRecord newRecord = dsl.newRecord(LAYER);
        newRecord.setOwner(layer.getOwner());
        newRecord.setAlias(layer.getAlias());
        newRecord.setConfig(layer.getConfig());
        newRecord.setData(layer.getData());
        newRecord.setName(layer.getName());
        newRecord.setNamespace(layer.getNamespace());
        newRecord.setService(layer.getService());
        newRecord.setDate(layer.getDate());

        if (newRecord.store() > 0)
            return newRecord.into(Layer.class);

        return null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(Layer layer) {
        LayerRecord layerRecord = new LayerRecord();
        layerRecord.from(layer);
        UpdateConditionStep<LayerRecord> set = dsl.update(LAYER).set(LAYER.NAME, layer.getName())
                .set(LAYER.NAMESPACE, layer.getNamespace()).set(LAYER.ALIAS, layer.getAlias()).set(LAYER.DATA, layer.getData())
                .set(LAYER.CONFIG, layer.getConfig()).set(LAYER.TITLE, layer.getTitle()).where(LAYER.ID.eq(layer.getId()));
        set.execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateLayerTitle(LayerSummary layer) {
        dsl.update(LAYER).set(LAYER.TITLE, layer.getTitle()).where(LAYER.ID.eq(layer.getId())).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(int layerId) {
        final DeleteConditionStep<LayerRecord> delete = dsl.delete(LAYER).where(LAYER.ID.eq(layerId));
        delete.execute();
    }

    @Override
    public Layer findById(Integer layerId) {
        return dsl.select().from(LAYER).where(LAYER.ID.eq(layerId)).fetchOneInto(Layer.class);
    }

    @Override
    public List<Layer> findByServiceId(int serviceId) {
        return dsl.select().from(LAYER).where(LAYER.SERVICE.eq(serviceId)).fetchInto(Layer.class);
    }

    @Override
    public List<Layer> findByDataId(int dataId) {
        return dsl.select().from(LAYER).where(LAYER.DATA.eq(dataId)).fetchInto(Layer.class);
    }

    @Override
    public Layer findByServiceIdAndLayerName(int serviceId, String layerName) {
        return dsl.select().from(LAYER).where(LAYER.SERVICE.eq(serviceId)).and(LAYER.NAME.eq(layerName)).fetchOneInto(Layer.class);
    }

    @Override
    public Layer findByServiceIdAndLayerName(int serviceId, String layerName, String namespace) {
        if (namespace != null) {
            return dsl.select().from(LAYER).where(LAYER.SERVICE.eq(serviceId)).and(LAYER.NAME.eq(layerName))
                    .and(LAYER.NAMESPACE.eq(namespace)).fetchOneInto(Layer.class);
        } else {
            return dsl.select().from(LAYER).where(LAYER.SERVICE.eq(serviceId)).and(LAYER.NAME.eq(layerName)).and(LAYER.NAMESPACE.isNull())
                    .fetchOneInto(Layer.class);
        }
    }

    @Override
    public Data findDatasFromLayerAlias(String layerAlias, String dataProviderIdentifier) {
        return dsl.select().from(DATA).join(PROVIDER).on(DATA.PROVIDER.eq(PROVIDER.ID)).join(LAYER).on(LAYER.DATA.eq(DATA.ID))
                .where(PROVIDER.IDENTIFIER.eq(dataProviderIdentifier)).and(LAYER.ALIAS.eq(layerAlias)).fetchOneInto(Data.class);
    }

    @Override
    public List<Layer> getLayersByLinkedStyle(final int styleId) {
        return dsl.select(LAYER.fields()).from(LAYER).join(STYLED_LAYER).onKey(STYLED_LAYER.LAYER).where(STYLED_LAYER.STYLE.eq(styleId))
                .fetchInto(Layer.class);
    }

    @Override
    public List<Layer> getLayersRefsByLinkedStyle(final int styleId) {
        return dsl.select(REF_FIELDS).from(LAYER).join(STYLED_LAYER).onKey(STYLED_LAYER.LAYER).where(STYLED_LAYER.STYLE.eq(styleId))
                .fetchInto(Layer.class);
    }

    @Override
    public int getDataSet(String layer) {
         Record1<Integer> fetchOne = dsl.selectDistinct(DATASET.ID).from(DATASET).join(DATA).on(DATASET.ID.eq(DATA.DATASET_ID)).join(LAYER)
                .on(DATA.ID.eq(LAYER.DATA)).where(LAYER.NAME.eq(layer)).fetchOne();
         if(fetchOne==null)
             return -1;
         return fetchOne.getValue(DATASET.ID);
    }
}
