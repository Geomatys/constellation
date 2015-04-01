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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import org.apache.sis.util.Locales;
import org.apache.sis.util.iso.Types;
import org.constellation.dto.MetadataLists;
import org.constellation.engine.register.MetadataComplete;
import org.constellation.engine.register.jooq.tables.pojos.MetadataBbox;
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
    private ServiceRepository serviceRepository;
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
     * Returns the xml as string representation of metadata for given metadata identifier.
     *
     * @param metadataId given metadata identifier
     * @param includeService flag that indicates if service repository will be requested.
     * @return String representation of metadata in xml.
     */
    @Override
    public String searchMetadata(final String metadataId, final boolean includeService)  {
        final Metadata metadata = metadataRepository.findByMetadataId(metadataId);
        if (metadata != null) {
            if (!includeService && metadata.getServiceId() != null) {
                return null;
            }
            return metadata.getMetadataIso();
        }
        return null;
    }
    
    @Override
    @Transactional
    public boolean updateMetadata(final String metadataId, final String xml) throws ConfigurationException  {
        Metadata metadata          = metadataRepository.findByMetadataId(metadataId);
        final boolean update       = metadata != null;
        final DefaultMetadata meta = (DefaultMetadata) unmarshallMetadata(xml);
        
        final Long dateStamp  = MetadataUtilities.extractDatestamp(meta);
        final String title    = MetadataUtilities.extractTitle(meta);
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
        metadata.setMetadataId(metadataId);
        metadata.setMetadataIso(xml);
        metadata.setParentIdentifier(parentID);
        metadata.setIsPublished(false);
        metadata.setIsValidated(false);
        
        // if the metadata is not yet present look for empty metadata object
        final Dataset dataset = datasetRepository.findByIdentifierWithEmptyMetadata(metadataId);
        // unsafe but no better way for now
        final Data data = dataRepository.findByIdentifierWithEmptyMetadata(metadataId);
        if (!update && dataset != null) {
            List<Data> datas = dataRepository.findByDatasetId(dataset.getId());
            if (!datas.isEmpty()) {
                final String type = datas.get(0).getType();
                templateName = getDatasetTemplate(dataset.getIdentifier(), type);
            }
            metadata.setDatasetId(dataset.getId());
        } else if (!update && data != null) {
            templateName = getDataTemplate(data.getName(), data.getNamespace(), data.getType());
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
            metadataRepository.create(new MetadataComplete(metadata, bboxes));
        }
        return true;
    }

    /**
     * Returns {@code true} if the xml metadata exists for given metadata identifier.
     *
     * @param metadataID given metadata identifier.
     * @param includeService flag that indicates if service repository will be requested.
     * @return boolean to indicates if metadata is present or not.
     */
    @Override
    public boolean existInternalMetadata(final String metadataID, final boolean includeService) {
        return searchMetadata(metadataID, includeService) != null;
    }

    /**
     * Returns a list of all metadata identifiers.
     *
     * @param includeService flag that indicates if service repository will be requested.
     * @return List of string identifiers.
     */
    @Override
    public List<String> getInternalMetadataIds(final boolean includeService) {
        final List<String> results = new ArrayList<>();
        final List<Metadata> metadatas = metadataRepository.findAll();
        for (final Metadata record : metadatas) {
            if (record.getServiceId() != null) {
                if (includeService) {
                    results.add(record.getMetadataId());
                }
            } else {
                results.add(record.getMetadataId());
            }
        }
        return results;
    }

    /**
     * Returns all metadata stored in database.
     *
     * @param includeService given flag to include service's metadata
     * @return List of all metadata as string xml stored in database.
     */
    @Override
    public List<String> getAllMetadata(final boolean includeService) {
        final List<String> results = new ArrayList<>();
        final List<Metadata> metadatas = metadataRepository.findAll();
        for (final Metadata record : metadatas) {
            if (record.getServiceId() != null) {
                if (includeService) {
                    results.add(record.getMetadataIso());
                }
            } else {
                results.add(record.getMetadataIso());
            }
        }
        return results;
    }
    
    @Override
    public List<String> getLinkedMetadataIDs(final String cswIdentifier) {
        final List<String> results = new ArrayList<>();
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            List<Metadata> metas = metadataRepository.findByCswId(service.getId());
            for (Metadata meta : metas) {
                results.add(meta.getMetadataId());
            }
        }
        return results;
    }
    
    @Override
    @Transactional
    public void linkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            metadataRepository.addMetadataToCSW(metadataId, service.getId());
        }
    }
    
    @Override
    @Transactional
    public void unlinkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            metadataRepository.removeDataFromCSW(metadataId, service.getId());
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
    
    protected String getDataTemplate(final String dataName, final String dataNamespace, final String dataType) throws ConfigurationException {
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
    
    protected String getDatasetTemplate(final String datasetId, final String dataType) throws ConfigurationException {
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
}
