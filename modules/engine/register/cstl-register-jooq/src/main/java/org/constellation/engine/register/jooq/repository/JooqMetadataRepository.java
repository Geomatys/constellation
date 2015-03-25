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

import static org.constellation.engine.register.jooq.Tables.METADATA;
import static org.constellation.engine.register.jooq.Tables.METADATA_X_CSW;

import java.util.List;
import java.util.Map;

import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataXCsw;
import org.constellation.engine.register.jooq.tables.records.MetadataRecord;
import org.constellation.engine.register.jooq.tables.records.MetadataXCswRecord;
import org.constellation.engine.register.repository.MetadataRepository;
import org.jooq.AggregateFunction;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
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
                .set(METADATA.MD_COMPLETION, metadata.getMdCompletion())
                .set(METADATA.OWNER, metadata.getOwner())
                .set(METADATA.PARENT_IDENTIFIER, metadata.getParentIdentifier())
                .set(METADATA.DATESTAMP, metadata.getDatestamp())
                .set(METADATA.DATE_CREATION, metadata.getDateCreation())
                .set(METADATA.ELEMENTARY, metadata.getElementary())
                .set(METADATA.IS_PUBLISHED, metadata.getIsPublished())
                .set(METADATA.IS_VALIDATED, metadata.getIsValidated())
                .set(METADATA.PROFILE, metadata.getProfile())
                .set(METADATA.TITLE, metadata.getTitle())
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
        metadataRecord.setMdCompletion(metadata.getMdCompletion());
        metadataRecord.setDateCreation(metadata.getDateCreation());
        metadataRecord.setDatestamp(metadata.getDatestamp());
        metadataRecord.setElementary(metadata.getElementary());
        metadataRecord.setIsPublished(metadata.getIsPublished());
        metadataRecord.setIsValidated(metadata.getIsPublished());
        metadataRecord.setOwner(metadata.getOwner());
        metadataRecord.setParentIdentifier(metadata.getParentIdentifier());
        metadataRecord.setProfile(metadata.getProfile());
        metadataRecord.setTitle(metadata.getTitle());
        metadataRecord.store();
        return metadataRecord.getId();
    }

    @Override
    public Metadata findByMetadataId(String metadataId) {
        return dsl.select().from(METADATA).where(METADATA.METADATA_ID.eq(metadataId)).fetchOneInto(Metadata.class);
    }

    @Override
    public Metadata findById(int id) {
        return dsl.select().from(METADATA).where(METADATA.ID.eq(id)).fetchOneInto(Metadata.class);
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

    @Override
    public Map<String,Integer> getProfilesCount() {
        AggregateFunction<Integer> count = DSL.count(METADATA.PROFILE);
        return dsl.select(METADATA.PROFILE, count ).from(METADATA).groupBy(METADATA.PROFILE).orderBy(count.desc()).fetchMap(METADATA.PROFILE, count);
    }

    @Override
    public List<Metadata> filterAndGet(final Map<String,Object> filterMap) {
        if(filterMap == null || filterMap.isEmpty()) {
            return findAll();
        }else {
            SelectConditionStep<Record> condition = null;
            for(final Map.Entry<String,Object> entry : filterMap.entrySet()) {
                if("owner".equals(entry.getKey())) {
                    if(condition == null) {
                        condition = dsl.select().from(METADATA).where(METADATA.OWNER.equal((Integer)entry.getValue()));
                    }else {
                        condition = condition.and(METADATA.OWNER.equal((Integer)entry.getValue()));
                    }
                }else if("profile".equals(entry.getKey())) {
                    if(condition == null) {
                        condition = dsl.select().from(METADATA).where(METADATA.PROFILE.equal((String)entry.getValue()));
                    }else {
                        condition = condition.and(METADATA.PROFILE.equal((String)entry.getValue()));
                    }
                }else if("validated".equals(entry.getKey())) {
                    if(condition == null) {
                        condition = dsl.select().from(METADATA).where(METADATA.IS_VALIDATED.equal((Boolean)entry.getValue()));
                    }else {
                        condition = condition.and(METADATA.IS_VALIDATED.equal((Boolean)entry.getValue()));
                    }
                }else if("published".equals(entry.getKey())) {
                    if(condition == null) {
                        condition = dsl.select().from(METADATA).where(METADATA.IS_PUBLISHED.equal((Boolean)entry.getValue()));
                    }else {
                        condition = condition.and(METADATA.IS_PUBLISHED.equal((Boolean)entry.getValue()));
                    }
                }else if("level".equals(entry.getKey())) {
                    if(condition == null) {
                        condition = dsl.select().from(METADATA).where(METADATA.ELEMENTARY.equal((Boolean)entry.getValue()));
                    }else {
                        condition = condition.and(METADATA.ELEMENTARY.equal((Boolean)entry.getValue()));
                    }
                }else if("term".equals(entry.getKey())) {
                    if(condition == null) {
                        condition = dsl.select().from(METADATA).where(METADATA.METADATA_ISO.contains((String)entry.getValue()));
                    }else {
                        condition = condition.and(METADATA.METADATA_ISO.contains((String)entry.getValue()));
                    }
                }
            }
            if(condition == null) {
                return findAll();
            }
            return condition.fetchInto(Metadata.class);
        }
    }
    
}
