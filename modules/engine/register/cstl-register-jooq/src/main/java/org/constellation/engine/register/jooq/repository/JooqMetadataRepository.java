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

import java.util.List;
import org.constellation.engine.register.Metadata;
import org.constellation.engine.register.MetadataXCsw;
import static org.constellation.engine.register.jooq.Tables.METADATA;
import static org.constellation.engine.register.jooq.Tables.METADATA_X_CSW;
import org.constellation.engine.register.jooq.tables.records.MetadataRecord;
import org.constellation.engine.register.jooq.tables.records.MetadataXCswRecord;
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

    @Override
    public MetadataXCsw addMetadataToCSW(final String metadataID, final int serviceID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.METADATA_ID.eq(metadataID)).fetchOneInto(Metadata.class);
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
    public void removeDataFromCSW(final String metadataID, final int serviceID) {
        final Metadata metadata = dsl.select().from(METADATA).where(METADATA.METADATA_ID.eq(metadataID)).fetchOneInto(Metadata.class);
        if (metadata != null) {
            dsl.delete(METADATA_X_CSW).where(METADATA_X_CSW.CSW_ID.eq(serviceID)).and(METADATA_X_CSW.METADATA_ID.eq(metadata.getId())).execute();
        }
    }

    @Override
    public List<Metadata> findByCswId(Integer id) {
        return dsl.select(METADATA.fields()).from(METADATA, METADATA_X_CSW)
                  .where(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                  .and(METADATA_X_CSW.CSW_ID.eq(id)).fetchInto(Metadata.class);
    }
    
}
