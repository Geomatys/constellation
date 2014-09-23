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
import static org.constellation.engine.register.jooq.Tables.PROVIDER;
import static org.constellation.engine.register.jooq.Tables.DATASET;
import org.constellation.engine.register.jooq.tables.records.DatasetRecord;
import org.constellation.engine.register.repository.DatasetRepository;
import org.jooq.UpdateConditionStep;
import org.springframework.stereotype.Component;

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
    public Dataset insert(Dataset dataset) {
        DatasetRecord newRecord = dsl.newRecord(DATASET);
        newRecord.setIdentifier(dataset.getIdentifier());
        newRecord.setMetadataIso(dataset.getMetadataIso());
        newRecord.setMetadataId(dataset.getMetadataId());
        newRecord.setOwner(dataset.getOwner());
        newRecord.store();
        return newRecord.into(Dataset.class);
    }
    
    @Override
    public int update(Dataset dataset) {
        DatasetRecord datasetRecord = new DatasetRecord();
        datasetRecord.from(dataset);
        UpdateConditionStep<DatasetRecord> set = dsl.update(DATASET)
                .set(DATASET.IDENTIFIER, dataset.getIdentifier())
                .set(DATASET.METADATA_ISO, dataset.getMetadataIso())
                .set(DATASET.OWNER, dataset.getOwner())
                .set(DATASET.METADATA_ID, dataset.getMetadataId())
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
    public Dataset findById(int id) {
        return dsl.select().from(DATASET).where(DATASET.ID.eq(id)).fetchOneInto(Dataset.class);
    }
    
    @Override
    public Dataset findByIdentifierAndDomainId(String datasetIdentifier, Integer domainId) {
        // @FIXME binding domainId
        return dsl.select().from(DATASET).where(DATASET.IDENTIFIER.eq(datasetIdentifier)).fetchOneInto(Dataset.class);

    }

    @Override
    public void remove(int id) {
        dsl.delete(DATASET).where(DATASET.ID.eq(id)).execute();
    }
}
