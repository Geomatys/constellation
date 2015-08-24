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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.constellation.engine.register.i18n.DataWithI18N;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.DataI18n;
import org.constellation.engine.register.jooq.tables.pojos.DataXData;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataXCsw;
import org.constellation.engine.register.jooq.tables.records.DataRecord;
import org.constellation.engine.register.jooq.tables.records.DataXDataRecord;
import org.constellation.engine.register.jooq.tables.records.MetadataXCswRecord;
import org.constellation.engine.register.pojo.DataItem;
import org.constellation.engine.register.repository.DataRepository;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.constellation.engine.register.jooq.Tables.CSTL_USER;
import static org.constellation.engine.register.jooq.Tables.DATA;
import static org.constellation.engine.register.jooq.Tables.DATA_I18N;
import static org.constellation.engine.register.jooq.Tables.DATA_X_DATA;
import static org.constellation.engine.register.jooq.Tables.LAYER;
import static org.constellation.engine.register.jooq.Tables.METADATA;
import static org.constellation.engine.register.jooq.Tables.METADATA_X_CSW;
import static org.constellation.engine.register.jooq.Tables.PROVIDER;
import static org.constellation.engine.register.jooq.Tables.SENSORED_DATA;
import static org.constellation.engine.register.jooq.Tables.STYLED_DATA;

@Component
public class JooqDataRepository extends AbstractJooqRespository<DataRecord, Data> implements DataRepository {

    public static final Field[] ITEM_FIELDS = new Field[]{
            DATA.ID.as("id"),
            DATA.NAMESPACE.as("namespace"),
            DATA.NAME.as("name"),
            DATA.TYPE.as("type"),
            DATA.SUBTYPE.as("subtype"),
            DATA.DATE.as("creation_date"),
            DATA.SENSORABLE.as("sensorable"),
            DATA.DATASET_ID.as("dataset_id"),
            DATA.PROVIDER.as("provider_id"),
            DATA.OWNER.as("owner_id"),
            PROVIDER.IDENTIFIER.as("provider_identifier"),
            CSTL_USER.LOGIN.as("owner_login"),
            countStyles(DATA.ID).as("style_count"),
            countLayers(DATA.ID).as("layer_count"),
            countServices(DATA.ID).as("service_count"),
            countSensors(DATA.ID).as("sensor_count"),
            selectConformPyramidProviderIdentifier(DATA.ID).as("pyramid_provider_identifier")};

    /**
     * Field list use to return a lighten reference to Data object
     */
    public static final Field[] REF_FIELDS = new Field[]{
            DATA.ID,
            DATA.NAMESPACE,
            DATA.NAME,
            DATA.TYPE,
            DATA.SUBTYPE,
            DATA.PROVIDER};

    public JooqDataRepository() {
        super(Data.class, DATA);
    }

    @Override
    public Data findById(int id) {
        return dsl.select().from(DATA).where(DATA.ID.eq(id)).fetchOneInto(Data.class);
    }

    @Override
    public Data fromLayer(String layerAlias, String providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Data create(Data data) {
        DataRecord newRecord = dsl.newRecord(DATA);
        newRecord.from(data);
        newRecord.store();
        return newRecord.into(Data.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int delete(int id) {
        return dsl.delete(DATA).where(DATA.ID.eq(id)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int delete(String namespaceURI, String localPart, int providerId) {
        Condition whereClause = buildWhereClause(namespaceURI, localPart, providerId);
        return dsl.delete(DATA).where(whereClause).execute();

    }

    private Condition buildWhereClause(String namespaceURI, String localPart, int providerId) {
        Condition whereClause = DATA.NAME.eq(localPart).and(DATA.PROVIDER.eq(providerId));
        if (namespaceURI != null) {
            return whereClause.and(DATA.NAMESPACE.eq(namespaceURI));
        }
        return whereClause;
    }

    private Condition buildWhereClause(String namespaceURI, String localPart, String providerId) {
        Condition whereClause = Tables.PROVIDER.IDENTIFIER.eq(providerId).and(DATA.NAME.eq(localPart));
        if (namespaceURI != null && ! namespaceURI.isEmpty()) {
            return whereClause.and(DATA.NAMESPACE.eq(namespaceURI));
        }
        return whereClause;
    }

    @Override
    public Data findDataFromProvider(String namespaceURI, String localPart, String providerId) {
        final Condition whereClause = buildWhereClause(namespaceURI, localPart, providerId);
        return dsl.select(DATA.fields()).from(DATA).join(Tables.PROVIDER).onKey().where(whereClause).fetchOneInto(Data.class);
    }

    @Override
    public Data findByMetadataId(String metadataId) {
        return dsl.select(DATA.fields()).from(DATA).join(METADATA).onKey(METADATA.DATA_ID).where(METADATA.METADATA_ID.eq(metadataId)).fetchOneInto(Data.class);
    }

    @Override
    public List<Data> findByProviderId(Integer id) {
        return dsl.select().from(DATA).where(DATA.PROVIDER.eq(id)).fetchInto(Data.class);
    }
    
    @Override
    public List<Data> findByDatasetId(Integer id) {
        return dsl.select().from(DATA)
                .where(DATA.DATASET_ID.eq(id))
                .and(DATA.INCLUDED.eq(Boolean.TRUE))
                .and(DATA.HIDDEN.isNull().or(DATA.HIDDEN.isFalse())).fetchInto(Data.class);
    }
    
    @Override
    public List<Data> findAllByDatasetId(Integer id) {
        return dsl.select().from(DATA).where(DATA.DATASET_ID.eq(id)).fetchInto(Data.class);
    }

    @Override
    public DataWithI18N getDescription(Data data) {
        Result<Record> fetch = dsl.select().from(DATA_I18N).where(DATA_I18N.DATA_ID.eq(data.getId())).fetch();
        ImmutableMap<String, DataI18n> dataI18ns = Maps.uniqueIndex(fetch.into(DataI18n.class), new Function<DataI18n, String>() {
            @Override
            public String apply(DataI18n input) {
                return input.getLang();
            }
        });
        return new DataWithI18N(data, dataI18ns);
    }

    @Override
    public Data findByNameAndNamespaceAndProviderId(String localPart, String namespaceURI, Integer providerId) {
        return dsl.select().from(DATA).where(DATA.PROVIDER.eq(providerId)).and(DATA.NAME.eq(localPart))
                .and(DATA.NAMESPACE.eq(namespaceURI)).fetchOneInto(Data.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(Data data) {

        dsl.update(DATA)
                .set(DATA.DATE, data.getDate())
                .set(DATA.METADATA, data.getMetadata())
                .set(DATA.NAME, data.getName())
                .set(DATA.NAMESPACE, data.getNamespace())
                .set(DATA.OWNER, data.getOwner())
                .set(DATA.PROVIDER, data.getProvider())
                .set(DATA.SENSORABLE, data.getSensorable())
                .set(DATA.SUBTYPE, data.getSubtype())
                .set(DATA.TYPE, data.getType())
                .set(DATA.INCLUDED, data.getIncluded())
                .set(DATA.DATASET_ID, data.getDatasetId())
                .set(DATA.FEATURE_CATALOG, data.getFeatureCatalog())
                .set(DATA.STATS_RESULT, data.getStatsResult())
                .set(DATA.STATS_STATE, data.getStatsState())
                .set(DATA.RENDERED, data.getRendered())
                .set(DATA.HIDDEN, data.getHidden())
                .where(DATA.ID.eq(data.getId()))
                .execute();

    }


    @Override
    public Data findByIdentifierWithEmptyMetadata(String localPart) {
        List<Data> datas = dsl.select().from(DATA).where(DATA.NAME.eq(localPart)).fetchInto(Data.class);
        for (Data data : datas) {
            Metadata m = dsl.select().from(METADATA).where(METADATA.DATA_ID.eq(data.getId())).fetchOneInto(Metadata.class);
            if (m == null) {
                return data;
            }
        }
        return null;
    }

    @Override
    public List<Data> getCswLinkedData(final int cswId) {
        return dsl.select(DATA.fields()).from(DATA, METADATA, METADATA_X_CSW)
                .where(METADATA.DATA_ID.eq(DATA.ID))
                .and(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                .and(METADATA_X_CSW.CSW_ID.eq(cswId)).fetchInto(Data.class);
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public MetadataXCsw addDataToCSW(final int serviceID, final int dataID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.DATA_ID.eq(dataID)).fetchOneInto(Metadata.class);
        if (metadata != null) {
            final MetadataXCsw dxc = dsl.select().from(METADATA_X_CSW).where(METADATA_X_CSW.CSW_ID.eq(serviceID)).and(METADATA_X_CSW.METADATA_ID.eq(metadata.getId())).fetchOneInto(MetadataXCsw.class);
            if (dxc == null) {
                MetadataXCswRecord newRecord = dsl.newRecord(METADATA_X_CSW);
                newRecord.setCswId(serviceID);
                newRecord.setMetadataId(metadata.getId());
                newRecord.store();
                return newRecord.into(MetadataXCsw.class);
            }
            return dxc;
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeDataFromCSW(int serviceID, int dataID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.DATA_ID.eq(dataID)).fetchOneInto(Metadata.class);
        if (metadata != null) {
            dsl.delete(METADATA_X_CSW).where(METADATA_X_CSW.CSW_ID.eq(serviceID)).and(METADATA_X_CSW.METADATA_ID.eq(metadata.getId())).execute();
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeDataFromAllCSW(int dataID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.DATA_ID.eq(dataID)).fetchOneInto(Metadata.class);
        if (metadata != null) {
            dsl.delete(METADATA_X_CSW).where(METADATA_X_CSW.METADATA_ID.eq(metadata.getId())).execute();
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeAllDataFromCSW(int serviceID) {
        dsl.delete(METADATA_X_CSW).where(METADATA_X_CSW.CSW_ID.eq(serviceID)).execute();
    }

    @Override
    public void linkDataToData(final int dataId, final int childId) {
        final DataXData dxd = dsl.select().from(DATA_X_DATA).where(DATA_X_DATA.DATA_ID.eq(dataId)).and(DATA_X_DATA.CHILD_ID.eq(childId)).fetchOneInto(DataXData.class);
        if (dxd == null) {
            DataXDataRecord newRecord = dsl.newRecord(DATA_X_DATA);
            newRecord.setDataId(dataId);
            newRecord.setChildId(childId);
            newRecord.store();
        }
    }

    @Override
    public List<Data> getDataLinkedData(final int dataId) {
        return dsl.select(DATA.fields()).from(DATA)
                .join(DATA_X_DATA).onKey(DATA_X_DATA.CHILD_ID)
                .where(DATA_X_DATA.DATA_ID.eq(dataId)).fetchInto(Data.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLinkedData(int dataId) {
        dsl.delete(DATA_X_DATA).where(DATA_X_DATA.DATA_ID.eq(dataId)).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Data> getFullDataByLinkedStyle(final int styleId) {
        return dsl.select(DATA.fields()).from(DATA)
                .join(STYLED_DATA).onKey(STYLED_DATA.DATA)
                .where(STYLED_DATA.STYLE.eq(styleId)).fetchInto(Data.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Data> getRefDataByLinkedStyle(final int styleId) {
        return dsl.select(REF_FIELDS).from(DATA)
                .join(STYLED_DATA).onKey(STYLED_DATA.DATA)
                .where(STYLED_DATA.STYLE.eq(styleId)).fetchInto(Data.class);
    }

    @Override
    public List<Data> findStatisticLess() {
        return dsl.select().from(DATA)
                .where(DATA.TYPE.eq("COVERAGE"))
                .and(DATA.RENDERED.isNull().or(DATA.RENDERED.isFalse())).fetchInto(Data.class);
    }

    @Override
    public boolean existsById(int dataId) {
        return dsl.selectCount().from(DATA)
                .where(DATA.ID.eq(dataId))
                .fetchOne(0, Integer.class) > 0;
    }

    @Override
    public List<DataItem> fetchByDatasetId(int datasetId) {
        return fetchByDatasetIds(Arrays.asList(datasetId));
    }

    @Override
    public List<DataItem> fetchByDatasetIds(Collection<Integer> datasetIds) {
        return dsl.select(ITEM_FIELDS).from(DATA)
                .leftOuterJoin(CSTL_USER).on(DATA.OWNER.eq(CSTL_USER.ID)) // data -> cstl_user
                .join(PROVIDER).on(DATA.PROVIDER.eq(PROVIDER.ID)) // data -> provider
                .where(DATA.DATASET_ID.in(datasetIds)).and(DATA.HIDDEN.eq(false)).and(DATA.INCLUDED.eq(true))
                .fetchInto(DataItem.class);
    }

    /**
     * Select count of data and return the result.
     * @param includeInvisibleData flag that indicates if the count will includes hidden data.
     * @return count of rows in table
     */
    @Override
    public Integer countAll(boolean includeInvisibleData) {
        if(includeInvisibleData) {
            return dsl.selectCount().from(DATA).fetchOne(0,int.class);
        } else {
            return dsl.selectCount().from(DATA).where(DATA.HIDDEN.eq(false).and(DATA.INCLUDED.eq(true))).fetchOne(0, int.class);
        }
    }

    // -------------------------------------------------------------------------
    //  Private utility methods
    // -------------------------------------------------------------------------

    private static Field<Integer> countStyles(Field<Integer> dataId) {
        return DSL.selectCount().from(STYLED_DATA)
                .where(STYLED_DATA.DATA.eq(dataId))
                .asField();
    }

    private static Field<Integer> countLayers(Field<Integer> dataId) {
        return DSL.selectCount().from(LAYER)
                .leftOuterJoin(DATA_X_DATA).on(DATA_X_DATA.CHILD_ID.eq(LAYER.DATA)) // layer -> data_x_data (child_id)
                .where(LAYER.DATA.eq(dataId).or(DATA_X_DATA.DATA_ID.eq(dataId)))
                .asField();
    }

    private static Field<Integer> countServices(Field<Integer> dataId) {
        // "Layer" services.
        Field<Integer> layerServices = DSL.select(DSL.countDistinct(LAYER.SERVICE)).from(LAYER)
                .leftOuterJoin(DATA_X_DATA).on(DATA_X_DATA.CHILD_ID.eq(LAYER.DATA)) // layer -> data_x_data (child_id)
                .where(LAYER.DATA.eq(dataId).or(DATA_X_DATA.DATA_ID.eq(dataId)))
                .asField();

        // "Metadata" services.
        org.constellation.engine.register.jooq.tables.Data dataAlias = DATA.as("target_data");
        Field<Integer> metadataServices = DSL.select(DSL.countDistinct(METADATA_X_CSW.CSW_ID)).from(dataAlias)
                .join(METADATA).on(METADATA.DATASET_ID.eq(dataAlias.DATASET_ID)) // data -> metadata
                .join(METADATA_X_CSW).on(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID)) // metadata -> metadata_x_csw
                .where(dataAlias.ID.eq(dataId))
                .asField();

        // Sum.
        return layerServices.add(metadataServices);
    }

    private static Field<Integer> countSensors(Field<Integer> dataId) {
        return DSL.selectCount().from(SENSORED_DATA)
                .where(SENSORED_DATA.DATA.eq(dataId))
                .asField();
    }

    private static Field<String> selectConformPyramidProviderIdentifier(Field<Integer> dataId) {
        org.constellation.engine.register.jooq.tables.Data dataAlias = DATA.as("child_data");
        org.constellation.engine.register.jooq.tables.Provider providerAlias = PROVIDER.as("child_provider");
        return DSL.select(providerAlias.IDENTIFIER).from(DATA_X_DATA)
                .join(dataAlias).on(dataAlias.ID.eq(DATA_X_DATA.CHILD_ID)) // data_x_data (child_id) -> data
                .join(providerAlias).on(providerAlias.ID.eq(dataAlias.PROVIDER)) // data -> provider
                .where(DATA_X_DATA.DATA_ID.eq(dataId)).and(dataAlias.SUBTYPE.eq("pyramid").and(dataAlias.RENDERED.eq(false)))
                .asField();
    }
}
