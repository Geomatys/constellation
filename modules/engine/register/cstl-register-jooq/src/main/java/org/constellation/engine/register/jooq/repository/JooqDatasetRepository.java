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
import org.constellation.engine.register.DatasetXCsw;
import static org.constellation.engine.register.jooq.Tables.DATASET;
import static org.constellation.engine.register.jooq.Tables.DATASET_X_CSW;
import org.constellation.engine.register.jooq.tables.records.DatasetRecord;
import org.constellation.engine.register.jooq.tables.records.DatasetXCswRecord;
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
        newRecord.setMetadataIso(dataset.getMetadataIso());
        newRecord.setMetadataId(dataset.getMetadataId());
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
                .set(DATASET.METADATA_ISO, dataset.getMetadataIso())
                .set(DATASET.OWNER, dataset.getOwner())
                .set(DATASET.METADATA_ID, dataset.getMetadataId())
                .set(DATASET.DATE, dataset.getDate())
                .set(DATASET.FEATURE_CATALOG, dataset.getFeatureCatalog())
                .where(DATASET.ID.eq(dataset.getId()));

        return set.execute();

    }
    
    @Override
    public Dataset findByMetadataId(String metadataId) {
        return dsl.select().from(DATASET).where(DATASET.METADATA_ID.eq(metadataId)).fetchOneInto(Dataset.class);
    }
    
    @Override
    public Dataset findByIdentifier(String identifier) {
        return dsl.select().from(DATASET).where(DATASET.IDENTIFIER.eq(identifier)).fetchOneInto(Dataset.class);
    }
    
    @Override
    public Dataset findByIdentifierWithEmptyMetadata(String identifier) {
        return dsl.select().from(DATASET).where(DATASET.IDENTIFIER.eq(identifier)).and(DATASET.METADATA_ID.isNull()).and(DATASET.METADATA_ISO.isNull()).fetchOneInto(Dataset.class);
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
    public List<DatasetXCsw> getCswLinkedDataset(final int cswId) {
        return dsl.select().from(DATASET_X_CSW).where(DATASET_X_CSW.CSW_ID.eq(cswId)).fetchInto(DatasetXCsw.class);
    }
    
    @Override
    public DatasetXCsw addDatasetToCSW(final int serviceID, final int datasetID, final boolean allData) {
        final DatasetXCsw dxc = dsl.select().from(DATASET_X_CSW).where(DATASET_X_CSW.CSW_ID.eq(serviceID)).and(DATASET_X_CSW.DATASET_ID.eq(datasetID)).fetchOneInto(DatasetXCsw.class);
        if (dxc == null) {
            DatasetXCswRecord newRecord = dsl.newRecord(DATASET_X_CSW);
            newRecord.setAllData(allData);
            newRecord.setCswId(serviceID);
            newRecord.setDatasetId(datasetID);
            newRecord.store();
            return newRecord.into(DatasetXCsw.class);
        }
        return dxc;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeDatasetFromCSW(int serviceID, int datasetID) {
        dsl.delete(DATASET_X_CSW).where(DATASET_X_CSW.CSW_ID.eq(serviceID)).and(DATASET_X_CSW.DATASET_ID.eq(datasetID)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeDatasetFromAllCSW(int datasetID) {
        dsl.delete(DATASET_X_CSW).where(DATASET_X_CSW.DATASET_ID.eq(datasetID)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeAllDatasetFromCSW(int serviceID) {
        dsl.delete(DATASET_X_CSW).where(DATASET_X_CSW.CSW_ID.eq(serviceID)).execute();
    }
    
}
