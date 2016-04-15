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

import org.constellation.database.api.domain.Page;
import org.constellation.database.api.domain.PageImpl;
import org.constellation.database.api.domain.Pageable;
import org.constellation.database.api.jooq.tables.Data;
import org.constellation.database.api.jooq.tables.pojos.Dataset;
import org.constellation.database.api.jooq.tables.pojos.Metadata;
import org.constellation.database.api.jooq.tables.pojos.MetadataXCsw;
import org.constellation.database.api.jooq.tables.records.DatasetRecord;
import org.constellation.database.api.jooq.tables.records.MetadataXCswRecord;
import org.constellation.database.api.pojo.DatasetItem;
import org.constellation.database.api.repository.DatasetRepository;
import org.constellation.database.impl.jooq.util.JooqUtils;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.constellation.database.api.jooq.Tables.CSTL_USER;
import static org.constellation.database.api.jooq.Tables.DATA;
import static org.constellation.database.api.jooq.Tables.DATASET;
import static org.constellation.database.api.jooq.Tables.LAYER;
import static org.constellation.database.api.jooq.Tables.METADATA;
import static org.constellation.database.api.jooq.Tables.METADATA_X_CSW;
import static org.constellation.database.api.jooq.Tables.SENSORED_DATA;

/**
 *
 * @author Guilhem Legal
 */
@Component
public class JooqDatasetRepository extends AbstractJooqRespository<DatasetRecord, Dataset> implements
        DatasetRepository {

    private static final CommonTableExpression<Record2<Integer, Integer>> FETCH_PAGE_WITH =
            DSL.name("w").fields("id", "count").as(DSL.select(DATA.DATASET_ID, DSL.count(DATA.ID)).
                            from(DATA).
                            where(isIncludedAndNotHiddenData(DATA)).
                            groupBy(DATA.DATASET_ID));

    private static final Field[] ITEM_FIELDS = new Field[]{
            DATASET.ID.as("id"),
            DATASET.IDENTIFIER.as("name"),
            DATASET.DATE.as("creation_date"),
            DATASET.OWNER.as("owner_id"),
            CSTL_USER.LOGIN.as("owner_login"),
            FETCH_PAGE_WITH.field("count").as("data_count")
    };


    public JooqDatasetRepository() {
        super(Dataset.class, DATASET);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Dataset insert(Dataset dataset) {
        DatasetRecord newRecord = dsl.newRecord(DATASET);
        newRecord.setIdentifier(dataset.getIdentifier());
        newRecord.setOwner(dataset.getOwner());
        newRecord.setDate(dataset.getDate());
        newRecord.setFeatureCatalog(dataset.getFeatureCatalog());
        newRecord.store();
        return newRecord.into(Dataset.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int update(Dataset dataset) {
        DatasetRecord datasetRecord = new DatasetRecord();
        datasetRecord.from(dataset);
        UpdateConditionStep<DatasetRecord> set = dsl.update(DATASET)
                .set(DATASET.IDENTIFIER, dataset.getIdentifier())
                .set(DATASET.OWNER, dataset.getOwner())
                .set(DATASET.DATE, dataset.getDate())
                .set(DATASET.FEATURE_CATALOG, dataset.getFeatureCatalog())
                .where(DATASET.ID.eq(dataset.getId()));

        return set.execute();

    }

    @Override
    public Dataset findByMetadataId(String metadataId) {
        return dsl.select(DATASET.fields()).from(DATASET).join(METADATA).onKey(METADATA.DATASET_ID).where(METADATA.METADATA_ID.eq(metadataId)).fetchOneInto(Dataset.class);
    }

    @Override
    public Dataset findByIdentifier(String identifier) {
        return dsl.select().from(DATASET).where(DATASET.IDENTIFIER.eq(identifier)).fetchOneInto(Dataset.class);
    }

    @Override
    public Dataset findByIdentifierWithEmptyMetadata(String identifier) {
        List<Dataset> datas = dsl.select().from(DATASET).where(DATASET.IDENTIFIER.eq(identifier)).fetchInto(Dataset.class);
        for (Dataset dataset : datas) {
            Metadata m = dsl.select().from(METADATA).where(METADATA.DATASET_ID.eq(dataset.getId())).fetchOneInto(Metadata.class);
            if (m == null) {
                return dataset;
            }
        }
        return null;
    }

    @Override
    public Dataset findById(int id) {
        return dsl.select().from(DATASET).where(DATASET.ID.eq(id)).fetchOneInto(Dataset.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void remove(int id) {
        dsl.delete(DATASET).where(DATASET.ID.eq(id)).execute();
    }

    @Override
    public List<Dataset> getCswLinkedDataset(final int cswId) {
        return dsl.select(DATASET.fields()).from(DATASET, METADATA, METADATA_X_CSW)
                .where(METADATA.ID.eq(METADATA_X_CSW.METADATA_ID))
                .and(DATASET.ID.eq(METADATA.DATASET_ID))
                .and(METADATA_X_CSW.CSW_ID.eq(cswId)).and(METADATA.DATASET_ID.isNotNull()).fetchInto(Dataset.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public MetadataXCsw addDatasetToCSW(final int serviceID, final int datasetID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.DATASET_ID.eq(datasetID)).fetchOneInto(Metadata.class);
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
    public void removeDatasetFromCSW(int serviceID, int datasetID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.DATASET_ID.eq(datasetID)).fetchOneInto(Metadata.class);
        if (metadata != null) {
            dsl.delete(METADATA_X_CSW).where(METADATA_X_CSW.CSW_ID.eq(serviceID)).and(METADATA_X_CSW.METADATA_ID.eq(metadata.getId())).execute();
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeDatasetFromAllCSW(int datasetID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.DATASET_ID.eq(datasetID)).fetchOneInto(Metadata.class);
        if (metadata != null) {
            dsl.delete(METADATA_X_CSW).where(METADATA_X_CSW.METADATA_ID.eq(metadata.getId())).execute();
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeAllDatasetFromCSW(int serviceID) {
        dsl.delete(METADATA_X_CSW).where(METADATA_X_CSW.CSW_ID.eq(serviceID)).execute();
    }

    @Override
    public boolean existsById(int datasetId) {
        return dsl.selectCount().from(DATASET)
                .where(DATASET.ID.eq(datasetId))
                .fetchOne(0, Integer.class) > 0;
    }

    @Override
    public Page<DatasetItem> fetchPage(Pageable pageable,
                                       boolean excludeEmpty,
                                       String termFilter,
                                       Boolean hasVectorData,
                                       Boolean hasCoverageData,
                                       Boolean hasLayerData,
                                       Boolean hasSensorData) {

        // Query filters.
        Condition condition = DSL.trueCondition();
        if (isNotBlank(termFilter)) {
            String likeExpr = '%' + termFilter + '%';
            condition = condition.and(DATASET.IDENTIFIER.likeIgnoreCase(likeExpr).or(CSTL_USER.LOGIN.likeIgnoreCase(likeExpr)).or(DATA.NAME.likeIgnoreCase(likeExpr)));
        }
        if (excludeEmpty) {
            condition = condition.and(DSL.fieldByName(Integer.class, "w.count").greaterThan(0));
        }
        if (hasVectorData != null) {
            Field<Integer> countVectorData = countDataOfType(DATASET.ID, "VECTOR");
            condition = condition.and(hasVectorData ? countVectorData.greaterThan(0) : countVectorData.eq(0));
        }
        if (hasCoverageData != null) {
            Field<Integer> countCoverageData = countDataOfType(DATASET.ID, "COVERAGE");
            condition = condition.and(hasCoverageData ? countCoverageData.greaterThan(0) : countCoverageData.eq(0));
        }
        if (hasLayerData != null) {
            Field<Integer> countLayerData = countLayerData(DATASET.ID);
            condition = condition.and(hasLayerData ? countLayerData.greaterThan(0) : countLayerData.eq(0));
        }
        if (hasSensorData != null) {
            Field<Integer> countSensorData = countSensorData(DATASET.ID);
            condition = condition.and(hasSensorData ? countSensorData.greaterThan(0) : countSensorData.eq(0));
        }

        // Content query.
        List<DatasetItem> content = dsl.with(FETCH_PAGE_WITH).selectDistinct(ITEM_FIELDS).from(DATASET)
                .join(FETCH_PAGE_WITH).on(DSL.fieldByName(Integer.class, "w.id").eq(DATASET.ID)) // dataset -> with
                .leftOuterJoin(CSTL_USER).on(CSTL_USER.ID.eq(DATASET.OWNER)) // dataset -> cstl_user
                .leftOuterJoin(DATA).on(DATA.DATASET_ID.eq(DATASET.ID)) // dataset -> data
                .where(condition)
                .orderBy(JooqUtils.sortFields(pageable, ITEM_FIELDS))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(DatasetItem.class);

        // Total query.
        Long total = dsl.with(FETCH_PAGE_WITH).selectDistinct(DSL.countDistinct(DATASET.ID)).from(DATASET)
                .join(FETCH_PAGE_WITH).on(DSL.fieldByName(Integer.class, "w.id").eq(DATASET.ID)) // dataset -> with
                .leftOuterJoin(CSTL_USER).on(DATASET.OWNER.eq(CSTL_USER.ID)) // dataset -> cstl_user
                .leftOuterJoin(DATA).on(DATA.DATASET_ID.eq(DATASET.ID)) // dataset -> data
                .where(condition)
                .fetchOne(0, Long.class);

        return new PageImpl<>(pageable, content, total);
    }

    // -------------------------------------------------------------------------
    //  Private utility methods
    // -------------------------------------------------------------------------

    private static Field<Integer> countData(Field<Integer> datasetId) {
        return DSL.selectCount().from(DATA)
                .where(DATA.DATASET_ID.eq(datasetId))
                .and(isIncludedAndNotHiddenData(DATA))
                .asField();
    }

    private static Field<Integer> countDataOfType(Field<Integer> datasetId, String type) {
        return DSL.selectCount().from(DATA)
                .where(DATA.DATASET_ID.eq(datasetId))
                .and(isIncludedAndNotHiddenData(DATA))
                .and(DATA.TYPE.eq(type))
                .asField();
    }

    private static Field<Integer> countLayerData(Field<Integer> datasetId) {
        return DSL.selectCount().from(LAYER)
                .join(DATA).on(LAYER.DATA.eq(DATA.ID)) // layer -> data
                .where(DATA.DATASET_ID.eq(datasetId))
                .and(isIncludedAndNotHiddenData(DATA))
                .asField();
    }

    private static Field<Integer> countSensorData(Field<Integer> datasetId) {
        return DSL.selectCount().from(SENSORED_DATA)
                .join(DATA).on(SENSORED_DATA.DATA.eq(DATA.ID)) // sensored_data -> data
                .where(DATA.DATASET_ID.eq(datasetId))
                .and(isIncludedAndNotHiddenData(DATA))
                .asField();
    }

    private static Condition isIncludedAndNotHiddenData(Data dataTable) {
        return dataTable.INCLUDED.eq(true).and(dataTable.HIDDEN.eq(false));
    }
}
