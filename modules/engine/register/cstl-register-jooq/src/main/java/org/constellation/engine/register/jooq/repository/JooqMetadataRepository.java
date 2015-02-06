/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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

import org.constellation.engine.register.Metadata;
import static org.constellation.engine.register.jooq.tables.Metadata.METADATA;
import org.constellation.engine.register.jooq.tables.records.MetadataRecord;
import org.constellation.engine.register.repository.MetadataRepository;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component
public class JooqMetadataRepository extends AbstractJooqRespository<MetadataRecord, Metadata> implements MetadataRepository {

    public JooqMetadataRepository() {
        super(Metadata.class, METADATA);
    }
    
    @Override
    public Metadata update(Metadata metadata) {
        dsl.update(METADATA)
                .set(METADATA.DATASET_ID, metadata.getDatasetId())
                .set(METADATA.DATA_ID, metadata.getDataId())
                .set(METADATA.METADATA_ID, metadata.getMetadataId())
                .set(METADATA.METADATA_ISO, metadata.getMetadataIso())
                .set(METADATA.SERVICE_ID, metadata.getServiceId())
                .where(METADATA.ID.eq(metadata.getId())).execute();
        return metadata;
    }

    @Override
    public int create(Metadata metadata) {
        MetadataRecord metadataRecord = dsl.newRecord(METADATA);
        metadataRecord.setDataId(metadata.getDataId());
        metadataRecord.setDatasetId(metadata.getDatasetId());
        metadataRecord.setMetadataId(metadata.getMetadataId());
        metadataRecord.setMetadataIso(metadata.getMetadataIso());
        metadataRecord.setServiceId(metadata.getServiceId());
        metadataRecord.store();
        return metadataRecord.getId();
    }

    @Override
    public Metadata findByMetadataId(String metadataId) {
        return dsl.select().from(METADATA).where(METADATA.METADATA_ID.eq(metadataId)).fetchOneInto(Metadata.class);
    }
    
    @Override
    public Metadata findByDataId(int dataId) {
        return dsl.select().from(METADATA).where(METADATA.DATA_ID.eq(dataId)).fetchOneInto(Metadata.class);
    }
    
    @Override
    public Metadata findByDatasetId(int datasetId) {
        return dsl.select().from(METADATA).where(METADATA.DATASET_ID.eq(datasetId)).fetchOneInto(Metadata.class);
    }
    
}
