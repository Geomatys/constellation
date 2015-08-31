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
package org.constellation.database.impl.repository;

import static org.constellation.database.api.jooq.Tables.METADATA;
import static org.constellation.database.api.jooq.Tables.METADATA_BBOX;
import static org.constellation.database.api.jooq.Tables.METADATA_X_CSW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.constellation.database.api.MetadataComplete;
import static org.constellation.database.api.jooq.Tables.SERVICE;

import org.constellation.database.api.jooq.tables.pojos.Metadata;
import org.constellation.database.api.jooq.tables.pojos.MetadataBbox;
import org.constellation.database.api.jooq.tables.pojos.MetadataXCsw;
import org.constellation.database.api.jooq.tables.records.MetadataBboxRecord;
import org.constellation.database.api.jooq.tables.records.MetadataRecord;
import org.constellation.database.api.jooq.tables.records.MetadataXCswRecord;
import org.constellation.database.api.repository.MetadataRepository;
import org.jooq.AggregateFunction;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectLimitStep;
import org.jooq.SortField;
import org.jooq.UpdateSetFirstStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(propagation = Propagation.MANDATORY)
    public Metadata update(MetadataComplete metadata) {
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
        
        if (metadata.getValidationRequired() != null) update.set(METADATA.VALIDATION_REQUIRED, metadata.getValidationRequired());
        update.set(METADATA.COMMENT, metadata.getComment());
        update.set(METADATA.VALIDATED_STATE, metadata.getValidatedState());
        update.set(METADATA.PROFILE, metadata.getProfile());
        update.set(METADATA.TITLE, metadata.getTitle());
        update.set(METADATA.RESUME, metadata.getResume()).where(METADATA.ID.eq(metadata.getId())).execute();                
        
        updateBboxes(metadata.getId(), metadata.getBboxes());
        
        return metadata;
    }
    
    private void updateBboxes(int metadataID, List<MetadataBbox> bboxes) {
        dsl.delete(METADATA_BBOX).where(METADATA_BBOX.METADATA_ID.eq(metadataID)).execute();
        for (MetadataBbox bbox : bboxes) {
            MetadataBboxRecord record = dsl.newRecord(METADATA_BBOX);
            record.setMetadataId(metadataID);
            record.setEast(bbox.getEast());
            record.setWest(bbox.getWest());
            record.setNorth(bbox.getNorth());
            record.setSouth(bbox.getSouth());
            record.store();
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int create(MetadataComplete metadata) {
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
        metadataRecord.setResume(metadata.getResume());
        metadataRecord.setComment(metadata.getComment());
        metadataRecord.setValidatedState(metadata.getValidatedState());
        if (metadata.getValidationRequired() != null) metadataRecord.setValidationRequired(metadata.getValidationRequired());

        if (metadata.getIsPublished() != null) metadataRecord.setIsPublished(metadata.getIsPublished());
        else metadataRecord.setIsPublished(false); //default

        if (metadata.getIsValidated() != null) metadataRecord.setIsValidated(metadata.getIsValidated());
        else metadataRecord.setIsValidated(false); //default
        
        
        metadataRecord.store();
        
        updateBboxes(metadataRecord.getId(), metadata.getBboxes());
        
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
    public List<String> findMetadataIDByCswId(final Integer id, final boolean includeService, final boolean onlyPublished) {
        SelectConditionStep<Record1<String>> query = 
               dsl.select(METADATA.METADATA_ID).from(METADATA, METADATA_X_CSW)
                  .where(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                  .and(METADATA_X_CSW.CSW_ID.eq(id));
        
        if (!includeService) {
            query = query.and(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            query = query.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
        }
        return query.fetchInto(String.class);
    }
    
    @Override
    public int countMetadataByCswId(final Integer id, final boolean includeService, final boolean onlyPublished) {
        SelectConditionStep<Record1<String>> query = 
               dsl.select(METADATA.METADATA_ID).from(METADATA, METADATA_X_CSW)
                  .where(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                  .and(METADATA_X_CSW.CSW_ID.eq(id));
        
        if (!includeService) {
            query = query.and(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            query = query.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
        }
        return query.fetchCount();
    }
    
    @Override
    public List<String> findMetadataID(final boolean includeService, final boolean onlyPublished) {
        SelectJoinStep<Record1<String>> query =  dsl.select(METADATA.METADATA_ID).from(METADATA);
        if (includeService && !onlyPublished) {
            return query.fetchInto(String.class);
        }
        SelectConditionStep<Record1<String>> filterQuery = null;
        if (!includeService) {
            filterQuery = query.where(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            if (filterQuery == null) {
                filterQuery = query.where(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            } else {
                filterQuery = filterQuery.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            }
        }
        return filterQuery.fetchInto(String.class);
    }
    
    @Override
    public int countMetadata(final boolean includeService, final boolean onlyPublished) {
        SelectJoinStep<Record1<String>> query =  dsl.select(METADATA.METADATA_ID).from(METADATA);
        if (includeService && !onlyPublished) {
            return query.fetchCount();
        }
        SelectConditionStep<Record1<String>> filterQuery = null;
        if (!includeService) {
            filterQuery = query.where(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            if (filterQuery == null) {
                filterQuery = query.where(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            } else {
                filterQuery = filterQuery.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            }
        }
        return filterQuery.fetchCount();
    }
    
    @Override
    public List<Metadata> findAll(final boolean includeService, final boolean onlyPublished) {
        SelectJoinStep<Record> query =  dsl.select(METADATA.fields()).from(METADATA);
        if (includeService && !onlyPublished) {
            return query.fetchInto(Metadata.class);
        }
        SelectConditionStep<Record> filterQuery = null;
        if (!includeService) {
            filterQuery = query.where(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            if (filterQuery == null) {
                filterQuery = query.where(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            } else {
                filterQuery = filterQuery.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            }
        }
        return filterQuery.fetchInto(Metadata.class);
    }
    
    @Override
    public List<String> findAllIsoMetadata(final boolean includeService, final boolean onlyPublished) {
        SelectJoinStep<Record1<String>> query =  dsl.select(METADATA.METADATA_ISO).from(METADATA);
        if (includeService && !onlyPublished) {
            return query.fetchInto(String.class);
        }
        SelectConditionStep<Record1<String>> filterQuery = null;
        if (!includeService) {
            filterQuery = query.where(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            if (filterQuery == null) {
                filterQuery = query.where(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            } else {
                filterQuery = filterQuery.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
            }
        }
        return filterQuery.fetchInto(String.class);
    }
    
    @Override
    public boolean isLinkedMetadata(Integer metadataID, Integer cswID) {
        return dsl.select(METADATA.fields()).from(METADATA, METADATA_X_CSW)
                  .where(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                  .and(METADATA_X_CSW.CSW_ID.eq(cswID))
                  .and(METADATA_X_CSW.METADATA_ID.eq(metadataID)).fetchOneInto(Metadata.class) != null;
    }
    
    @Override
    public boolean isLinkedMetadata(String metadataID, String cswID) {
        return dsl.select(METADATA.fields()).from(METADATA, METADATA_X_CSW, SERVICE)
                  .where(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                  .and(METADATA_X_CSW.CSW_ID.eq(SERVICE.ID))
                  .and(SERVICE.IDENTIFIER.eq(cswID))
                  .and(SERVICE.TYPE.eq("csw"))
                  .and(METADATA.METADATA_ID.eq(metadataID)).fetchOneInto(Metadata.class) != null;
    }
    
    @Override
    public boolean isLinkedMetadata(String metadataID, String cswID, final boolean includeService, final boolean onlyPublished) {
        SelectConditionStep query = dsl.select(METADATA.ID).from(METADATA, METADATA_X_CSW, SERVICE)
                  .where(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                  .and(METADATA_X_CSW.CSW_ID.eq(SERVICE.ID))
                  .and(SERVICE.IDENTIFIER.eq(cswID))
                  .and(SERVICE.TYPE.eq("csw"))
                  .and(METADATA.METADATA_ID.eq(metadataID));
        
        if (!includeService) {
            query = query.and(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            query = query.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
        }
        
        return query.fetchOne() != null;
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
        Collection<Field<?>> fields = new ArrayList<>();
        Collections.addAll(fields,METADATA.ID,METADATA.TITLE);
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
                }else if("validation_required".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.VALIDATION_REQUIRED.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.VALIDATION_REQUIRED.equal((String) entry.getValue()));
                    }
                }else if("id".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.METADATA_ID.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.METADATA_ID.equal((String) entry.getValue()));
                    }
                }else if("parent".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.PARENT_IDENTIFIER.equal((Integer)entry.getValue()).or(METADATA.ID.equal((Integer)entry.getValue())));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.PARENT_IDENTIFIER.equal((Integer)entry.getValue()).or(METADATA.ID.equal((Integer)entry.getValue())));
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
                        query = dsl.select(fields).from(METADATA).where(METADATA.TITLE.likeIgnoreCase("%"+entry.getValue()+"%").or(METADATA.RESUME.likeIgnoreCase("%"+entry.getValue()+"%")));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.TITLE.likeIgnoreCase("%"+entry.getValue()+"%").or(METADATA.RESUME.likeIgnoreCase("%"+entry.getValue()+"%")));
                    }
                }else if ("period".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.DATESTAMP.greaterOrEqual((Long)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.DATESTAMP.greaterOrEqual((Long) entry.getValue()));
                    }
                }
            }
        }
        if(query == null) {
            return dsl.select(fields).from(METADATA).fetchMap(METADATA.ID, METADATA.TITLE);
        }else {
            return query.fetchMap(METADATA.ID,METADATA.TITLE);
        }
    }

    /**
     * Returns a singleton map that contains the total count of records as key,
     * and the list of records as value.
     * the list is resulted by filters, it use pagination and sorting.

     * @param filterMap given filters
     * @param sortEntry given sort
     * @param pageNumber pagination page
     * @param rowsPerPage count of rows per page
     * @return Map
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
                METADATA.IS_VALIDATED,METADATA.IS_PUBLISHED,METADATA.RESUME,
                METADATA.VALIDATION_REQUIRED,METADATA.COMMENT);
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
                }else if("validation_required".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.VALIDATION_REQUIRED.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.VALIDATION_REQUIRED.equal((String) entry.getValue()));
                    }
                }else if("id".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.METADATA_ID.equal((String)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.METADATA_ID.equal((String) entry.getValue()));
                    }
                }else if("parent".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.PARENT_IDENTIFIER.equal((Integer)entry.getValue()).or(METADATA.ID.equal((Integer)entry.getValue())));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.PARENT_IDENTIFIER.equal((Integer)entry.getValue()).or(METADATA.ID.equal((Integer)entry.getValue())));
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
                        query = dsl.select(fields).from(METADATA).where(METADATA.TITLE.likeIgnoreCase("%"+entry.getValue()+"%").or(METADATA.RESUME.likeIgnoreCase("%"+entry.getValue()+"%")));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.TITLE.likeIgnoreCase("%"+entry.getValue()+"%").or(METADATA.RESUME.likeIgnoreCase("%"+entry.getValue()+"%")));
                    }
                }else if ("period".equals(entry.getKey())) {
                    if(query == null) {
                        query = dsl.select(fields).from(METADATA).where(METADATA.DATESTAMP.greaterOrEqual((Long)entry.getValue()));
                    }else {
                        query = ((SelectConditionStep)query).and(METADATA.DATESTAMP.greaterOrEqual((Long) entry.getValue()));
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
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int delete(final int id) {
        dsl.delete(METADATA_BBOX).where(METADATA_BBOX.METADATA_ID.eq(id)).execute();
        return dsl.delete(METADATA).where(METADATA.ID.eq(id)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteAll() {
        dsl.delete(METADATA).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void changeOwner(final int id, final int owner) {
        UpdateSetFirstStep<MetadataRecord> update = dsl.update(METADATA);
        update.set(METADATA.OWNER, owner).where(METADATA.ID.eq(id)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void changeValidation(int id, boolean validated) {
        UpdateSetFirstStep<MetadataRecord> update = dsl.update(METADATA);
        update.set(METADATA.IS_VALIDATED, validated).where(METADATA.ID.eq(id)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void changePublication(int id, boolean published) {
        UpdateSetFirstStep<MetadataRecord> update = dsl.update(METADATA);
        update.set(METADATA.IS_PUBLISHED, published).where(METADATA.ID.eq(id)).execute();
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void changeProfile(int id, String newProfile) {
        UpdateSetFirstStep<MetadataRecord> update = dsl.update(METADATA);
        update.set(METADATA.PROFILE, newProfile).where(METADATA.ID.eq(id)).execute();
    }
 
    @Override
    public List<MetadataBbox> getBboxes(int id) {
        return dsl.select().from(METADATA_BBOX).where(METADATA_BBOX.METADATA_ID.eq(id)).fetchInto(MetadataBbox.class);
    }

    @Override
    public Map<String,Integer> getProfilesCount(final Map<String,Object> filterMap) {
        final Integer owner   = (Integer) filterMap.get("owner");
        final Long period     = (Long)    filterMap.get("period");
        
        
        AggregateFunction<Integer> count = DSL.count(METADATA.PROFILE);
        
        SelectJoinStep select = dsl.select(METADATA.PROFILE, count ).from(METADATA);
        SelectConditionStep cond = null;
        if (owner != null) {
            cond = select.where(METADATA.OWNER.eq(owner));
            select = null;
        }
        if (period != null) {
            if (select == null) {
                cond = cond.and(METADATA.DATESTAMP.greaterOrEqual(period));
            } else {
                cond = select.where(METADATA.DATESTAMP.greaterOrEqual(period));
                select = null;
            }
        }
        if (select != null) {
            return select.groupBy(METADATA.PROFILE).orderBy(count.desc()).fetchMap(METADATA.PROFILE, count);
        } else {
            return cond.groupBy(METADATA.PROFILE).orderBy(count.desc()).fetchMap(METADATA.PROFILE, count);
        }
    }
    
    @Override
    public int countInCompletionRange(final Map<String,Object> filterMap, final int minCompletion, final int maxCompletion) {
        final Integer owner   = (Integer) filterMap.get("owner");
        final Long period     = (Long)    filterMap.get("period");
        
        SelectConditionStep cond = dsl.select().from(METADATA).where(METADATA.MD_COMPLETION.between(minCompletion, maxCompletion));
        if (owner != null) {
            cond = cond.and(METADATA.OWNER.eq(owner));
        }
        if (period != null) {
            cond = cond.and(METADATA.DATESTAMP.greaterOrEqual(period));
        }
        return dsl.fetchCount(cond);
    }
    
    @Override
    public int countTotalMetadata(final Map<String,Object> filterMap) {
        final Integer owner   = (Integer) filterMap.get("owner");
        final Long period     = (Long)    filterMap.get("period");
        
        SelectJoinStep select = dsl.select().from(METADATA);
        SelectConditionStep cond = null;
        if (owner != null) {
            cond = select.where(METADATA.OWNER.eq(owner));
            select = null;
        }
        if (period != null) {
            if (select == null) {
                cond = cond.and(METADATA.DATESTAMP.greaterOrEqual(period));
            } else {
                cond = select.where(METADATA.DATESTAMP.greaterOrEqual(period));
                select = null;
            }
        }
        if (select != null) {
            return dsl.fetchCount(select);
        } else {
            return dsl.fetchCount(cond);
        }
    }

    @Override
    public int countValidated(boolean status, final Map<String,Object> filterMap) {
        final Integer owner   = (Integer) filterMap.get("owner");
        final Long period     = (Long)    filterMap.get("period");
        final String validationReq = (String) filterMap.get("validation_required");
        SelectConditionStep cond = dsl.select().from(METADATA).where(METADATA.IS_VALIDATED.equal(status));
        if (owner != null) {
            cond = cond.and(METADATA.OWNER.eq(owner));
        }
        if (period != null) {
            cond = cond.and(METADATA.DATESTAMP.greaterOrEqual(period));
        }
        if (validationReq != null) {
            cond = cond.and(METADATA.VALIDATION_REQUIRED.eq(validationReq));
        }
        return dsl.fetchCount(cond);
    }

    @Override
    public int countPublished(boolean status, final Map<String,Object> filterMap) {
        final Integer owner   = (Integer) filterMap.get("owner");
        final Long period     = (Long)    filterMap.get("period");
        final Boolean validated = (Boolean) filterMap.get("validated");
        
        SelectConditionStep cond = dsl.select().from(METADATA).where(METADATA.IS_PUBLISHED.equal(status));
        if (owner != null) {
            cond = cond.and(METADATA.OWNER.eq(owner));
        }
        if (period != null) {
            cond = cond.and(METADATA.DATESTAMP.greaterOrEqual(period));
        }
        if(validated != null) {
            cond = cond.and(METADATA.IS_VALIDATED.equal(validated));
        }
        return dsl.fetchCount(cond);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void setValidationRequired(int id, String state, String validationState) {
        UpdateSetFirstStep<MetadataRecord> update = dsl.update(METADATA);
        update.set(METADATA.VALIDATION_REQUIRED, state)
              .set(METADATA.VALIDATED_STATE, validationState).where(METADATA.ID.eq(id)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void denyValidation(int id, String comment) {
        UpdateSetFirstStep<MetadataRecord> update = dsl.update(METADATA);
        update.set(METADATA.VALIDATION_REQUIRED, "REJECTED")
              .set(METADATA.COMMENT, comment).where(METADATA.ID.eq(id)).execute();
    }

    @Override
    public boolean existInternalMetadata(String metadataID, boolean includeService, boolean onlyPublished) {
        SelectConditionStep query = dsl.select(METADATA.ID).from(METADATA)
                                       .where(METADATA.METADATA_ID.eq(metadataID));
        
        if (!includeService) {
            query = query.and(METADATA.SERVICE_ID.isNull());
        }
        if (onlyPublished) {
            query = query.and(METADATA.IS_PUBLISHED.eq(Boolean.TRUE));
        }
        
        return query.fetchOne() != null;
    }
    
}
