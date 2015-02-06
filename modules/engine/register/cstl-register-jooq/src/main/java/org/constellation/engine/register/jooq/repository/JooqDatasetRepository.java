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

import java.util.List;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Metadata;
import org.constellation.engine.register.MetadataXCsw;
import static org.constellation.engine.register.jooq.Tables.DATASET;
import static org.constellation.engine.register.jooq.Tables.METADATA;
import static org.constellation.engine.register.jooq.Tables.METADATA_X_CSW;
import org.constellation.engine.register.jooq.tables.records.DatasetRecord;
import org.constellation.engine.register.jooq.tables.records.MetadataXCswRecord;
import org.constellation.engine.register.repository.DatasetRepository;
import org.jooq.UpdateConditionStep;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Guilhem Legal
 */
@Component
public class JooqDatasetRepository extends AbstractJooqRespository<DatasetRecord, Dataset> implements
        DatasetRepository {
 
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
                .set(DATASET.MD_COMPLETION,dataset.getMdCompletion())
                .where(DATASET.ID.eq(dataset.getId()));

        return set.execute();

    }
    
    @Override
    public Dataset findByMetadataId(String metadataId) {
        return dsl.select().from(DATASET).join(METADATA).onKey(METADATA.DATASET_ID).where(METADATA.METADATA_ID.eq(metadataId)).fetchOneInto(Dataset.class);
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
    public Dataset findByIdentifierAndDomainId(String datasetIdentifier, Integer domainId) {
        // @FIXME binding domainId
        return dsl.select().from(DATASET).where(DATASET.IDENTIFIER.eq(datasetIdentifier)).fetchOneInto(Dataset.class);

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
    public MetadataXCsw addDatasetToCSW(final int serviceID, final int datasetID, final boolean allData) {
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
    
}
