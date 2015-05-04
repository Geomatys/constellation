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

import org.constellation.engine.register.domain.Page;
import org.constellation.engine.register.domain.PageImpl;
import org.constellation.engine.register.domain.Pageable;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataXCsw;
import org.constellation.engine.register.jooq.tables.records.DatasetRecord;
import org.constellation.engine.register.jooq.tables.records.MetadataXCswRecord;
import org.constellation.engine.register.jooq.util.JooqUtils;
import org.constellation.engine.register.pojo.DatasetItem;
import org.constellation.engine.register.repository.DatasetRepository;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.constellation.engine.register.jooq.Tables.CSTL_USER;
import static org.constellation.engine.register.jooq.Tables.DATA;
import static org.constellation.engine.register.jooq.Tables.DATASET;
import static org.constellation.engine.register.jooq.Tables.LAYER;
import static org.constellation.engine.register.jooq.Tables.METADATA;
import static org.constellation.engine.register.jooq.Tables.METADATA_X_CSW;
import static org.constellation.engine.register.jooq.Tables.SENSORED_DATA;

/**
 *
 * @author Guilhem Legal
 */
@Component
public class JooqDatasetRepository extends AbstractJooqRespository<DatasetRecord, Dataset> implements
        DatasetRepository {

    private static final Field[] ITEM_FIELDS = new Field[]{
            DATASET.ID.as("id"),
            DATASET.IDENTIFIER.as("identifier"),
            DATASET.DATE.as("creation_date"),
            DATASET.OWNER.as("owner_id"),
            CSTL_USER.LOGIN.as("owner_login"),
            countDataInDataset(DATASET.ID).asField("data_count")};

 
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
    public Page<DatasetItem> fetchPage(Pageable pageable,
                                       boolean excludeEmpty,
                                       String textFilter,
                                       Boolean hasVectorData,
                                       Boolean hasCoverageData,
                                       Boolean hasLayerData,
                                       Boolean hasSensorData) {
        // Query filters.
        Condition condition = DSL.trueCondition();
        if (isNotBlank(textFilter)) {
            condition = condition.and(DATASET.IDENTIFIER.likeIgnoreCase(textFilter));
        }
        if (excludeEmpty) {
            condition = condition.and(countDataInDataset(DATASET.ID).asField().greaterThan(0));
        }
        if (hasVectorData != null) {
            Field<Integer> countVectorData = countDataOfTypeInDataset(DATASET.ID, "VECTOR").asField();
            condition = condition.and(hasVectorData ? countVectorData.greaterThan(0) : countVectorData.eq(0));
        }
        if (hasCoverageData != null) {
            Field<Integer> countCoverageData = countDataOfTypeInDataset(DATASET.ID, "COVERAGE").asField();
            condition = condition.and(hasCoverageData ? countCoverageData.greaterThan(0) : countCoverageData.eq(0));
        }
        if (hasLayerData != null) {
            Field<Integer> countLayerData = countLayerDataInDataset(DATASET.ID).asField();
            condition = condition.and(hasLayerData ? countLayerData.greaterThan(0) : countLayerData.eq(0));
        }
        if (hasSensorData != null) {
            Field<Integer> countSensorData = countSensorDataInDataset(DATASET.ID).asField();
            condition = condition.and(hasSensorData ? countSensorData.greaterThan(0) : countSensorData.eq(0));
        }

        // Content query.
        List<DatasetItem> content = dsl.select(ITEM_FIELDS).from(DATASET)
                .leftOuterJoin(CSTL_USER).on(DATASET.OWNER.eq(CSTL_USER.ID)) // style -> cstl_user
                .where(condition)
                .orderBy(JooqUtils.sortFields(pageable, ITEM_FIELDS))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(DatasetItem.class);

        // Total query.
        Long total = dsl.select(DSL.countDistinct(DATASET.ID)).from(DATASET)
                .leftOuterJoin(CSTL_USER).on(DATASET.OWNER.eq(CSTL_USER.ID)) // style -> cstl_user
                .where(condition)
                .fetchOne(0, Long.class);

        return new PageImpl<>(pageable, content, total);
    }

    // -------------------------------------------------------------------------
    //  Private utility methods
    // -------------------------------------------------------------------------

    private static SelectConditionStep<Record1<Integer>> countDataInDataset(Field<Integer> datasetId) {
        return DSL.selectCount().from(DATA)
                .where(DATA.DATASET_ID.eq(datasetId));
    }

    private static SelectConditionStep<Record1<Integer>> countDataOfTypeInDataset(Field<Integer> datasetId, String type) {
        return DSL.selectCount().from(DATA)
                .where(DATA.DATASET_ID.eq(datasetId)).and(DATA.TYPE.eq(type));
    }

    private static SelectConditionStep<Record1<Integer>> countLayerDataInDataset(Field<Integer> datasetId) {
        return DSL.selectCount().from(LAYER)
                .join(DATA).on(LAYER.DATA.eq(DATA.ID)) // layer -> data
                .where(DATA.DATASET_ID.eq(datasetId));
    }

    private static SelectConditionStep<Record1<Integer>> countSensorDataInDataset(Field<Integer> datasetId) {
        return DSL.selectCount().from(SENSORED_DATA)
                .join(DATA).on(SENSORED_DATA.DATA.eq(DATA.ID)) // sensored_data -> data
                .where(DATA.DATASET_ID.eq(datasetId));
    }
}
