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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataXCsw;
import org.constellation.engine.register.jooq.tables.records.MetadataRecord;
import org.constellation.engine.register.jooq.tables.records.MetadataXCswRecord;
import org.constellation.engine.register.repository.MetadataRepository;
import org.jooq.AggregateFunction;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectLimitStep;
import org.jooq.SortField;
import org.jooq.UpdateSetFirstStep;
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
        UpdateSetFirstStep<MetadataRecord> update = dsl.update(METADATA);
        update.set(METADATA.METADATA_ID, metadata.getMetadataId());
        update.set(METADATA.METADATA_ISO, metadata.getMetadataIso());
        if (metadata.getDatasetId() != null) update.set(METADATA.DATASET_ID, metadata.getDatasetId());
        if (metadata.getDataId() != null) update.set(METADATA.DATA_ID, metadata.getDataId());
        if (metadata.getServiceId() != null) update.set(METADATA.SERVICE_ID, metadata.getServiceId());
        if (metadata.getMdCompletion() != null) update.set(METADATA.MD_COMPLETION, metadata.getMdCompletion());
        if (metadata.getParentIdentifier() != null) update.set(METADATA.PARENT_IDENTIFIER, metadata.getParentIdentifier());
        update.set(METADATA.OWNER, metadata.getOwner());
        update.set(METADATA.DATESTAMP, metadata.getDatestamp());
        update.set(METADATA.DATE_CREATION, metadata.getDateCreation());
        update.set(METADATA.LEVEL, metadata.getLevel());

        if (metadata.getIsPublished() != null) update.set(METADATA.IS_PUBLISHED, metadata.getIsPublished());
        else update.set(METADATA.IS_PUBLISHED, false);

        if (metadata.getIsValidated() != null) update.set(METADATA.IS_VALIDATED, metadata.getIsValidated());
        else update.set(METADATA.IS_VALIDATED, false);

        update.set(METADATA.PROFILE, metadata.getProfile());
        update.set(METADATA.TITLE, metadata.getTitle())
                .where(METADATA.ID.eq(metadata.getId())).execute();
        return metadata;
    }

    @Override
    public int create(Metadata metadata) {
        MetadataRecord metadataRecord = dsl.newRecord(METADATA);
        metadataRecord.setMetadataId(metadata.getMetadataId());
        metadataRecord.setMetadataIso(metadata.getMetadataIso());
        if (metadata.getDataId() != null) metadataRecord.setDataId(metadata.getDataId());
        if (metadata.getDatasetId() != null) metadataRecord.setDatasetId(metadata.getDatasetId());
        if (metadata.getServiceId() != null) metadataRecord.setServiceId(metadata.getServiceId());
        if (metadata.getMdCompletion() != null) metadataRecord.setMdCompletion(metadata.getMdCompletion());
        if (metadata.getParentIdentifier() != null) metadataRecord.setParentIdentifier(metadata.getParentIdentifier());
        metadataRecord.setDateCreation(metadata.getDateCreation());
        metadataRecord.setDatestamp(metadata.getDatestamp());
        metadataRecord.setLevel(metadata.getLevel());
        metadataRecord.setOwner(metadata.getOwner());
        metadataRecord.setProfile(metadata.getProfile());
        metadataRecord.setTitle(metadata.getTitle());

        if (metadata.getIsPublished() != null) metadataRecord.setIsPublished(metadata.getIsPublished());
        else metadataRecord.setIsPublished(false); //default

        if (metadata.getIsValidated() != null) metadataRecord.setIsValidated(metadata.getIsValidated());
        else metadataRecord.setIsValidated(false); //default

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

    /**
     * Returns a map that contains id of metadata as key and the title of metadata as value.
     * the filterMap passed in arguments is optional and can contains one or multiple filter on each field.
     * This method is used for selection of rows to check the state when using server pagination,
     * the pagination should not be included in this result to keep a list of all existing ids.
     * @param filterMap
     * @return
     */
    @Override
    public Map<Integer,String> filterAndGetWithoutPagination(final Map<String,Object> filterMap) {
        Select query = null;
        if(filterMap != null) {
            for(final Map.Entry<String,Object> entry : filterMap.entrySet()) {
                if("owner".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(METADATA.ID,METADATA.TITLE).from(METADATA).where(METADATA.OWNER.equal((Integer)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.OWNER.equal((Integer)entry.getValue()));
                    }
                }else if("profile".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(METADATA.ID,METADATA.TITLE).from(METADATA).where(METADATA.PROFILE.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.PROFILE.equal((String) entry.getValue()));
                    }
                }else if("validated".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(METADATA.ID,METADATA.TITLE).from(METADATA).where(METADATA.IS_VALIDATED.equal((Boolean)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.IS_VALIDATED.equal((Boolean) entry.getValue()));
                    }
                }else if("published".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(METADATA.ID,METADATA.TITLE).from(METADATA).where(METADATA.IS_PUBLISHED.equal((Boolean)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.IS_PUBLISHED.equal((Boolean) entry.getValue()));
                    }
                }else if("level".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(METADATA.ID,METADATA.TITLE).from(METADATA).where(METADATA.LEVEL.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.LEVEL.equal((String) entry.getValue()));
                    }
                }else if("term".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(METADATA.ID,METADATA.TITLE).from(METADATA).where(METADATA.METADATA_ISO.contains((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.METADATA_ISO.contains((String) entry.getValue()));
                    }
                }
            }
        }
        if(query == null) {
            return dsl.select(METADATA.ID,METADATA.TITLE).from(METADATA).fetchMap(METADATA.ID, METADATA.TITLE);
        }else {
            return query.fetchMap(METADATA.ID,METADATA.TITLE);
        }
    }

    /**
     * Returns a singleton map that contains the total count of records as key,
     * and the list of records as value.
     * the list is resulted by filters, it use pagination and sorting.
      
     * @param filterMap
     * @param sortEntry
     * @param pageNumber
     * @param rowsPerPage
     * @return
     */
    @Override
    public Map<Integer, List> filterAndGet(final Map<String,Object> filterMap,
                                       final Map.Entry<String,String> sortEntry,
                                       final int pageNumber,
                                       final int rowsPerPage) {
        final Map<Integer,List> result = new HashMap<>();
        Collection<Field<?>> fields = new ArrayList<>();
        Collections.addAll(fields,METADATA.ID,METADATA.METADATA_ID,
                METADATA.TITLE,METADATA.PROFILE,METADATA.OWNER,METADATA.DATESTAMP,
                METADATA.DATE_CREATION,METADATA.MD_COMPLETION,METADATA.LEVEL,
                METADATA.IS_VALIDATED,METADATA.IS_PUBLISHED);
        Select query = null;
        if(filterMap != null) {
            for(final Map.Entry<String,Object> entry : filterMap.entrySet()) {
                if("owner".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.OWNER.equal((Integer)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.OWNER.equal((Integer)entry.getValue()));
                    }
                }else if("profile".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.PROFILE.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.PROFILE.equal((String) entry.getValue()));
                    }
                }else if("validated".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.IS_VALIDATED.equal((Boolean)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.IS_VALIDATED.equal((Boolean) entry.getValue()));
                    }
                }else if("published".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.IS_PUBLISHED.equal((Boolean)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.IS_PUBLISHED.equal((Boolean) entry.getValue()));
                    }
                }else if("level".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.LEVEL.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.LEVEL.equal((String) entry.getValue()));
                    }
                }else if("term".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.METADATA_ISO.contains((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.METADATA_ISO.contains((String) entry.getValue()));
                    }
                }
            }
        }
        if(sortEntry != null) {
            final SortField f;
            if("title".equals(sortEntry.getKey())){
                f = "ASC".equals(sortEntry.getValue()) ? METADATA.TITLE.asc() : METADATA.TITLE.desc();
            }else {
                f = "ASC".equals(sortEntry.getValue()) ? METADATA.DATESTAMP.asc() : METADATA.DATESTAMP.desc();
            }
            if(query == null) {
                query = dsl.select(fields).from(METADATA).orderBy(f);
            }else {
                query = ((SelectConditionStep)query).orderBy(f);
            }
        }

        if(query == null) { //means there are no sorting and no filters
            final int count = dsl.selectCount().from(METADATA).fetchOne(0,int.class);
            result.put(count,dsl.select(fields).from(METADATA).limit(rowsPerPage).offset((pageNumber - 1) * rowsPerPage).fetchInto(Metadata.class));
        }else {
            final int count = dsl.fetchCount(query);
            result.put(count, ((SelectLimitStep) query).limit(rowsPerPage).offset((pageNumber - 1) * rowsPerPage).fetchInto(Metadata.class));
        }
        return result;
    }
    
}
