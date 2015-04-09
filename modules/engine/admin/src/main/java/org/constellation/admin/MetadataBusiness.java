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

package org.constellation.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.XML;
import org.constellation.admin.util.MetadataUtilities;
import org.constellation.business.IMetadataBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.Service;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.MetadataRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.json.metadata.v2.Template;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.apache.sis.util.Locales;
import org.apache.sis.util.iso.Types;
import org.constellation.ServiceDef;
import org.constellation.dto.MetadataLists;
import org.constellation.engine.register.MetadataComplete;
import org.constellation.engine.register.jooq.tables.pojos.MetadataBbox;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.utils.MetadataFeeder;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ICSWConfigurer;
import org.constellation.ws.Refreshable;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.WSEngine;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.content.CoverageContentType;
import org.opengis.metadata.content.ImagingCondition;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.CellGeometry;
import org.opengis.metadata.spatial.DimensionNameType;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.metadata.spatial.PixelOrientation;

/**
 * Business facade for metadata.
 *
 * @author guilhem
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@Component("cstlMetadataBusiness")
@Primary
public class MetadataBusiness implements IMetadataBusiness {

    protected static final Logger LOGGER = Logging.getLogger(MetadataBusiness.class);
    
    /**
     * Injected data repository.
     */
    @Inject
    private DataRepository dataRepository;
    /**
     * Injected dataset repository.
     */
    @Inject
    private DatasetRepository datasetRepository;
    /**
     * Injected service repository.
     */
    @Inject
    protected ServiceRepository serviceRepository;
    /**
     * Injected metadata repository.
     */
    @Inject
    protected MetadataRepository metadataRepository;
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private org.constellation.security.SecurityManager securityManager;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String searchMetadata(final String metadataId, final boolean includeService, final boolean onlyPublished)  {
        final Metadata metadata = metadataRepository.findByMetadataId(metadataId);
        if (metadata != null) {
            if (!includeService && metadata.getServiceId() != null) {
                return null;
            }
            if (onlyPublished && !metadata.getIsPublished()) {
                return null;
            }
            return metadata.getMetadataIso();
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata searchFullMetadata(final String metadataId, final boolean includeService, final boolean onlyPublished)  {
        final Metadata metadata = metadataRepository.findByMetadataId(metadataId);
        if (metadata != null) {
            if (!includeService && metadata.getServiceId() != null) {
                return null;
            }
            if (onlyPublished && !metadata.getIsPublished()) {
                return null;
            }
            return metadata;
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean updateMetadata(final String metadataId, final DefaultMetadata metadata) throws ConfigurationException  {
        return updateMetadata(metadataId, metadata, null, null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean updateMetadata(final String metadataId, final String xml) throws ConfigurationException  {
        return updateMetadata(metadataId, xml, null, null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean updateMetadata(final String metadataId, final DefaultMetadata metadata, final Integer dataID, final Integer datasetID) throws ConfigurationException  {
        final String xml = marshallMetadata(metadata);
        return updateMetadata(metadataId, xml, dataID, datasetID);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean updateMetadata(final String metadataId, final String xml, final Integer dataID, final Integer datasetID) throws ConfigurationException  {
        Metadata metadata          = metadataRepository.findByMetadataId(metadataId);
        final boolean update       = metadata != null;
        final DefaultMetadata meta = (DefaultMetadata) unmarshallMetadata(xml);
        
        final Long dateStamp  = MetadataUtilities.extractDatestamp(meta);
        final String title    = MetadataUtilities.extractTitle(meta);
        final String resume   = MetadataUtilities.extractResume(meta);
        Integer parentID      = null;
        final String parent   = MetadataUtilities.extractParent(meta);
        Metadata parentRecord = metadataRepository.findByMetadataId(parent);
        if (parentRecord != null) {
            parentID = parentRecord.getId();
        }
        final List<MetadataBbox> bboxes = MetadataUtilities.extractBbox(meta);
        final Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
        Integer userID = null;
        if (user.isPresent()) {
            userID = user.get().getId();
        }
        Integer completion  = null;
        String level        = "NONE";
        String templateName = null;
        
        if (metadata != null) {
            templateName = metadata.getProfile();
        } else {
            metadata = new Metadata();
        }

        metadata.setOwner(userID);
        metadata.setDatestamp(dateStamp);
        metadata.setDateCreation(System.currentTimeMillis());
        metadata.setTitle(title);
        metadata.setResume(resume);
        metadata.setMetadataId(metadataId);
        metadata.setMetadataIso(xml);
        metadata.setParentIdentifier(parentID);
        metadata.setIsPublished(false);
        metadata.setIsValidated(false);
        
        // if the metadata is not yet present look for empty metadata object
        final Dataset dataset;
        if (datasetID != null) {
            dataset = datasetRepository.findById(datasetID);
        } else {
            dataset = datasetRepository.findByIdentifierWithEmptyMetadata(metadataId);
        }
        final Data data;
        if (dataID != null) {
            data = dataRepository.findById(dataID);
        } else {
            // unsafe but no better way for now
            data = dataRepository.findByIdentifierWithEmptyMetadata(metadataId);
        }
        if (!update && dataset != null) {
            List<Data> datas = dataRepository.findByDatasetId(dataset.getId());
            if (!datas.isEmpty()) {
                final String type = datas.get(0).getType();
                templateName = getDatasetTemplate(dataset.getIdentifier(), type);
            }
            metadata.setDatasetId(dataset.getId());
        } else if (!update && data != null) {
            templateName = getDataTemplate(new QName(data.getNamespace(), data.getName()) , data.getType());
            metadata.setDataId(data.getId());
        } else {
            templateName = getTemplateFromMetadata(meta);
        }
        
        if (templateName != null) {
            try {
                final Template template = Template.getInstance(templateName);
                completion = template.calculateMDCompletion(unmarshallMetadata(xml));
                level = template.getCompletion(meta);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error while calculating metadata completion", ex);
            }
        }
        
        metadata.setProfile(templateName);
        metadata.setMdCompletion(completion);
        metadata.setLevel(level);
        
        if (update) {
            metadataRepository.update(new MetadataComplete(metadata, bboxes));
        } else {
            int id = metadataRepository.create(new MetadataComplete(metadata, bboxes));
            metadata.setId(id);
        }
        updateInternalCSWIndex(Arrays.asList(metadata), true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existInternalMetadata(final String metadataID, final boolean includeService, final boolean onlyPublished) {
        return searchMetadata(metadataID, includeService, onlyPublished) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getInternalMetadataIds(final boolean includeService, final boolean onlyPublished) {
        final List<String> results = new ArrayList<>();
        final List<Metadata> metadatas = metadataRepository.findAll();
        for (final Metadata record : metadatas) {
            if (record.getServiceId() != null) {
                if (includeService) {
                    results.add(record.getMetadataId());
                }
            } else if (!record.getIsPublished()) {
                if (!onlyPublished) {
                    results.add(record.getMetadataId());
                }
            } else {
                results.add(record.getMetadataId());
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllMetadata(final boolean includeService, final boolean onlyPublished) {
        final List<String> results = new ArrayList<>();
        final List<Metadata> metadatas = metadataRepository.findAll();
        for (final Metadata record : metadatas) {
            if (record.getServiceId() != null) {
                if (includeService) {
                    results.add(record.getMetadataIso());
                }
            } else if (!record.getIsPublished()) {
                if (!onlyPublished) {
                    results.add(record.getMetadataId());
                }
            } else {
                results.add(record.getMetadataIso());
            }
        }
        return results;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getLinkedMetadataIDs(final String cswIdentifier, final boolean includeService, final boolean onlyPublished) {
        final List<String> results = new ArrayList<>();
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            List<Metadata> metas = metadataRepository.findByCswId(service.getId());
            for (Metadata record : metas) {
                if (record.getServiceId() != null) {
                    if (includeService) {
                        results.add(record.getMetadataIso());
                    }
                } else if (!record.getIsPublished()) {
                    if (!onlyPublished) {
                        results.add(record.getMetadataId());
                    }
                } else {
                    results.add(record.getMetadataIso());
                }
            }
        }
        return results;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLinkedMetadataToCSW(final int metadataID, final int cswID) {
        return metadataRepository.isLinkedMetadata(metadataID, cswID);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void linkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            metadataRepository.addMetadataToCSW(metadataId, service.getId());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void unlinkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            metadataRepository.removeDataFromCSW(metadataId, service.getId());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultMetadata getMetadata(final int id) throws ConfigurationException {
        final Metadata metadata = metadataRepository.findById(id);
        if (metadata != null) {
            return (DefaultMetadata) unmarshallMetadata(metadata.getMetadataIso());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultMetadata getIsoMetadataForData(final int dataId) throws ConfigurationException {
        final Metadata metadata = metadataRepository.findByDataId(dataId);
        if (metadata != null) {
            return (DefaultMetadata) unmarshallMetadata(metadata.getMetadataIso());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultMetadata getIsoMetadataForDataset(final int datasetId) throws ConfigurationException {
        final Metadata metadata = metadataRepository.findByDatasetId(datasetId);
        if (metadata != null) {
            return (DefaultMetadata) unmarshallMetadata(metadata.getMetadataIso());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata getMetadataById(final int id) {
        return metadataRepository.findById(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePublication(final int id, final boolean newStatus) throws ConfigurationException {
        final Metadata metadata = metadataRepository.findById(id);
        if (metadata != null) {
            metadataRepository.changePublication(id, newStatus);
            metadata.setIsPublished(newStatus);
            updateInternalCSWIndex(Arrays.asList(metadata), true);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePublication(final List<Integer> ids, final boolean newStatus) throws ConfigurationException {
        final List<Metadata> toUpdate = new ArrayList<>();
        for (Integer id : ids) {
            final Metadata metadata = metadataRepository.findById(id);
            if (metadata != null) {
                metadataRepository.changePublication(id, newStatus);
                metadata.setIsPublished(newStatus);
                toUpdate.add(metadata);
            }
        }
        updateInternalCSWIndex(toUpdate, true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateValidation(int id, boolean newStatus) {
        metadataRepository.changeValidation(id, newStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOwner(int id, int newOwner) {
        metadataRepository.changeOwner(id, newOwner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMetadata(int id) throws ConfigurationException {
        final Metadata metadata = metadataRepository.findById(id);
        if (metadata != null) {
            updateInternalCSWIndex(Arrays.asList(metadata), false);
            metadataRepository.delete(id);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMetadata(List<Integer> ids) throws ConfigurationException {
        // First we update the csw index
        final List<Metadata> toDelete = new ArrayList<>();
        for (Integer id : ids) {
            final Metadata metadata = metadataRepository.findById(id);
            if (metadata != null) {
                toDelete.add(metadata);
            }
        }
        updateInternalCSWIndex(toDelete, false);
        // then we remove the metadata from the database
        for (Integer id : ids) {
            metadataRepository.delete(id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCompletionForData(int dataId) {
        final Metadata metadata = metadataRepository.findByDataId(dataId);
        if (metadata != null) {
            return metadata.getMdCompletion();
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCompletionForDataset(int datasetId) {
        final Metadata metadata = metadataRepository.findByDatasetId(datasetId);
        if (metadata != null) {
            return metadata.getMdCompletion();
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInternalCSWIndex(final List<Metadata> metadatas, final boolean update) throws ConfigurationException {
        try {
            final List<Service> services = serviceRepository.findByType("csw");
            for (Service service : services) {
            
                final Unmarshaller um = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                // read config to determine CSW type
                final Automatic conf = (Automatic) um.unmarshal(new StringReader(service.getConfig()));
                
                if (conf.getFormat().equals("internal")) {
                    boolean partial       = conf.getBooleanParameter("partial", false);
                    boolean onlyPublished = conf.getBooleanParameter("onlyPublished", false);
                    final List<String> identifierToRemove = new ArrayList<>();
                    final List<String> identifierToUpdate = new ArrayList<>();
                    ICSWConfigurer configurer = null;
                    for (Metadata metadata : metadatas) {
                        if (!partial || (partial && isLinkedMetadataToCSW(metadata.getId(), service.getId()))) {
                            if (configurer == null) {
                                configurer = (ICSWConfigurer) ServiceConfigurer.newInstance(ServiceDef.Specification.CSW);
                            }
                            identifierToRemove.add(metadata.getMetadataId());
                            if (update) {
                                if ((onlyPublished && metadata.getIsPublished()) || !onlyPublished) {
                                    identifierToUpdate.add(metadata.getMetadataId());
                                }
                            }
                        }
                    }
                    if (configurer != null) {
                        configurer.removeFromIndex(service.getIdentifier(), identifierToRemove);
                        configurer.addToIndex(service.getIdentifier(), identifierToUpdate);
                        final Refreshable worker = (Refreshable) WSEngine.getInstance("CSW", service.getIdentifier());
                        worker.refresh();
                    }
                }
            }
        } catch (JAXBException | CstlServiceException ex) {
            throw new ConfigurationException("Error while updating internal CSW index", ex);
        }
    }
    
    @Override
    public MetadataLists getMetadataCodeLists() {
        final MetadataLists mdList = new MetadataLists();

        //for role codes
        final List<String> roleCodes = new LinkedList<>();
        for (final org.opengis.metadata.citation.Role role : org.opengis.metadata.citation.Role.values()) {
            final String standardName = Types.getStandardName(role.getClass());
            final String code = role.identifier()!=null?role.identifier():role.name();
            final String codeListName = standardName+"."+code;
            roleCodes.add(codeListName);
        }
        Collections.sort(roleCodes);
        mdList.setRoleCodes(roleCodes);

        //for keyword type codes
        final List<String> keywordTypesCodes = new LinkedList<>();
        for (final KeywordType ktype : KeywordType.values()) {
            final String standardName = Types.getStandardName(ktype.getClass());
            final String code = ktype.identifier()!=null?ktype.identifier():ktype.name();
            final String codeListName = standardName+"."+code;
            keywordTypesCodes.add(codeListName);
        }
        Collections.sort(keywordTypesCodes);
        mdList.setKeywordTypeCodes(keywordTypesCodes);

        //for locale codes
        final List<String> localeCodes = new LinkedList<>();
        for (final Locale locale : Locales.ALL.getAvailableLanguages()) {
            localeCodes.add("LanguageCode."+locale.getISO3Language());
        }
        // add missing locale (FRE)
        localeCodes.add("LanguageCode.fre");
        Collections.sort(localeCodes);
        mdList.setLocaleCodes(localeCodes);

        //for topic category codes
        final List<String> topicCategoryCodes = new LinkedList<>();
        for (final TopicCategory tc : TopicCategory.values()) {
            final String standardName = Types.getStandardName(tc.getClass());
            final String code = tc.identifier()!=null? tc.identifier(): tc.name();
            final String codeListName = standardName+"."+code;
            topicCategoryCodes.add(codeListName);
        }
        Collections.sort(topicCategoryCodes);
        mdList.setTopicCategoryCodes(topicCategoryCodes);

        //for date type codes
        final List<String> dateTypeCodes = new LinkedList<>();
        for (final DateType dateType : DateType.values()) {
            final String standardName = Types.getStandardName(dateType.getClass());
            final String code = dateType.identifier()!=null? dateType.identifier(): dateType.name();
            final String codeListName = standardName+"."+code;
            dateTypeCodes.add(codeListName);
        }
        Collections.sort(dateTypeCodes);
        mdList.setDateTypeCodes(dateTypeCodes);

        //for maintenanceFrequency codes
        final List<String> maintenanceFrequencyCodes = new LinkedList<>();
        for (final MaintenanceFrequency cl : MaintenanceFrequency.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            maintenanceFrequencyCodes.add(codeListName);
        }
        Collections.sort(maintenanceFrequencyCodes);
        mdList.setMaintenanceFrequencyCodes(maintenanceFrequencyCodes);

        //for GeometricObjectType codes
        final List<String> geometricObjectTypeCodes = new LinkedList<>();
        for (final GeometricObjectType got : GeometricObjectType.values()) {
            final String standardName = Types.getStandardName(got.getClass());
            final String code = got.identifier()!=null? got.identifier(): got.name();
            final String codeListName = standardName+"."+code;
            geometricObjectTypeCodes.add(codeListName);
        }
        Collections.sort(geometricObjectTypeCodes);
        mdList.setGeometricObjectTypeCodes(geometricObjectTypeCodes);

        //for Classification codes
        final List<String> classificationCodes = new LinkedList<>();
        for (final Classification cl : Classification.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            classificationCodes.add(codeListName);
        }
        Collections.sort(classificationCodes);
        mdList.setClassificationCodes(classificationCodes);

        // for characterSet codes
        final List<String> characterSetCodes = new LinkedList<>();
        final Set<String> keys = Charset.availableCharsets().keySet();
        final List<String> keep = Arrays.asList("UTF-8","UTF-16","UTF-32",
                "ISO-8859-1","ISO-8859-13","ISO-8859-15",
                "ISO-8859-2","ISO-8859-3","ISO-8859-4",
                "ISO-8859-5","ISO-8859-6","ISO-8859-7",
                "ISO-8859-8","ISO-8859-9","Shift_JIS",
                "EUC-JP","EUC-KR","US-ASCII","Big5","GB2312");
        keep.retainAll(keys);
        for (final String c : keep) {
            characterSetCodes.add(c);
        }
        Collections.sort(characterSetCodes);
        mdList.setCharacterSetCodes(characterSetCodes);

        //for Restriction codes
        final List<String> restrictionCodes = new LinkedList<>();
        for (final Restriction cl : Restriction.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            restrictionCodes.add(codeListName);
        }
        Collections.sort(restrictionCodes);
        mdList.setRestrictionCodes(restrictionCodes);

        final List<String> dimensionNameTypeCodes = new LinkedList<>();
        for (final DimensionNameType cl : DimensionNameType.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            dimensionNameTypeCodes.add(codeListName);
        }
        Collections.sort(dimensionNameTypeCodes);
        mdList.setDimensionNameTypeCodes(dimensionNameTypeCodes);

        final List<String> coverageContentTypeCodes = new LinkedList<>();
        for (final CoverageContentType cl : CoverageContentType.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            coverageContentTypeCodes.add(codeListName);
        }
        Collections.sort(coverageContentTypeCodes);
        mdList.setCoverageContentTypeCodes(coverageContentTypeCodes);

        final List<String> imagingConditionCodes = new LinkedList<>();
        for (final ImagingCondition cl : ImagingCondition.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            imagingConditionCodes.add(codeListName);
        }
        Collections.sort(imagingConditionCodes);
        mdList.setImagingConditionCodes(imagingConditionCodes);

        final List<String> cellGeometryCodes = new LinkedList<>();
        for (final CellGeometry cl : CellGeometry.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            cellGeometryCodes.add(codeListName);
        }
        Collections.sort(cellGeometryCodes);
        mdList.setCellGeometryCodes(cellGeometryCodes);

        //for pixel orientation codes
        final List<String> pixelOrientationCodes = new LinkedList<>();
        for (final PixelOrientation cl : PixelOrientation.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            pixelOrientationCodes.add(codeListName);
        }
        Collections.sort(pixelOrientationCodes);
        mdList.setPixelOrientationCodes(pixelOrientationCodes);

        //for Scope codes
        final List<String> scopeCodes = new LinkedList<>();
        for (final ScopeCode cl : ScopeCode.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            scopeCodes.add(codeListName);
        }
        Collections.sort(scopeCodes);
        mdList.setScopeCodes(scopeCodes);

        return mdList;
    }
    
    protected Object unmarshallMetadata(final String metadata) throws ConfigurationException {
        try {
            return XML.unmarshal(metadata);
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to unmarshall metadata", ex);
        }
    }
    
    protected String marshallMetadata(final DefaultMetadata metadata) throws ConfigurationException {
        try {
            return XML.marshal(metadata);
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to unmarshall metadata", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataTemplate(final QName dataName, final String dataType) throws ConfigurationException {
        final String templateName;
        if ("vector".equalsIgnoreCase(dataType)) {
            //vector template
            templateName = "profile_default_vector";
        } else if ("raster".equalsIgnoreCase(dataType)) {
            //raster template
            templateName = "profile_default_raster";
        } else {
            //default template is import
            templateName = "profile_import";
        }
        return templateName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatasetTemplate(final String datasetId, final String dataType) throws ConfigurationException {
        //get template name
        final String templateName;
        if ("vector".equalsIgnoreCase(dataType)) {
            //vector template
            templateName = "profile_default_vector";
        } else if ("raster".equalsIgnoreCase(dataType)) {
            //raster template
            templateName = "profile_default_raster";
        } else {
            //default template is import
            templateName = "profile_import";
        }
        return templateName;
    }

    
    protected String getTemplateFromMetadata(DefaultMetadata meta) {
        return null; // must be overriden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata duplicateMetadata(final int id, final String newTitle) throws ConfigurationException {
        final Metadata meta = metadataRepository.findById(id);
        if (meta != null) {
            List<MetadataBbox> bboxes = metadataRepository.getBboxes(id);
            final DefaultMetadata metaObj = (DefaultMetadata) unmarshallMetadata(meta.getMetadataIso());
            MetadataFeeder feeder = new MetadataFeeder(metaObj);
            String title;
            if (newTitle != null) {
                title = newTitle;
                
            } else {
                String oldTitle = feeder.getTitle();
                title = oldTitle + "(1)";
            }
            feeder.setTitle(title);
            final String newMetadataID = UUID.randomUUID().toString();
            metaObj.setFileIdentifier(newMetadataID);
            
            meta.setMetadataIso(marshallMetadata(metaObj));
            meta.setMetadataId(newMetadataID);
            meta.setTitle(title);
            meta.setIsPublished(false);
            meta.setIsValidated(false);
            
            final MetadataComplete duplicated = new MetadataComplete(meta, bboxes);
            final int newID = metadataRepository.create(duplicated);
            return metadataRepository.findById(newID);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countTotal() {
        return metadataRepository.countTotalMetadata();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countPublished(final boolean status) {
        return metadataRepository.countPublished(status);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int countValidated(final boolean status) {
        return metadataRepository.countValidated(status);
    }
    
}
