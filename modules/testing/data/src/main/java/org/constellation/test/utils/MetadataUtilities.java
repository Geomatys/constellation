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

package org.constellation.test.utils;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.util.Utilities;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.xml.IdentifierSpace;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.ebrim.xml.v250.ExtrinsicObjectType;
import org.geotoolkit.ebrim.xml.v250.SlotType;
import org.geotoolkit.ebrim.xml.v300.AdhocQueryType;
import org.geotoolkit.ebrim.xml.v300.IdentifiableType;
import org.geotoolkit.ebrim.xml.v300.RegistryObjectType;
import org.geotoolkit.ebrim.xml.v300.RegistryPackageType;
import org.geotoolkit.feature.catalog.FeatureAttributeImpl;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import org.geotoolkit.feature.catalog.FeatureTypeImpl;
import org.geotoolkit.feature.catalog.PropertyTypeImpl;
import org.geotoolkit.service.ServiceIdentificationImpl;
import org.geotoolkit.sml.xml.v100.ComponentType;
import org.geotoolkit.sml.xml.v100.IoComponentPropertyType;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.sml.xml.v100.SystemType;
import org.geotoolkit.swe.xml.v100.DataRecordType;
import org.opengis.metadata.acquisition.AcquisitionInformation;
import org.opengis.metadata.acquisition.Operation;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.content.ContentInformation;
import org.opengis.metadata.content.CoverageDescription;
import org.opengis.metadata.content.FeatureCatalogueDescription;
import org.opengis.metadata.content.RangeDimension;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.lineage.Algorithm;
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.metadata.lineage.Source;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.spatial.Georectified;
import org.opengis.metadata.spatial.SpatialRepresentation;
import org.opengis.referencing.ReferenceIdentifier;

import javax.xml.bind.JAXBElement;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class MetadataUtilities {

    private MetadataUtilities() {}

    public static void assertEqualsMode(final Object expected, final Object result, final ComparisonMode mode) {
        final boolean eq = Utilities.deepEquals(expected, result, mode);
        final String msg;
        if (!eq) {
            msg = "expected:<" + expected + "> but was <" + result + ">";
        } else {
            msg = "object are equals";
        }
        assertTrue(msg, eq);
    }

    public static void metadataEquals(final DefaultMetadata expResult, final DefaultMetadata result, ComparisonMode mode) {

        assertEqualsMode(expResult.getFileIdentifier(), result.getFileIdentifier(), mode);
        if (expResult.getIdentificationInfo() != null && result.getIdentificationInfo() != null) {
            assertEquals(expResult.getIdentificationInfo().size(), result.getIdentificationInfo().size());
            for (int i = 0; i < expResult.getIdentificationInfo().size(); i++) {
                Identification expId = expResult.getIdentificationInfo().iterator().next();
                Identification resId = result.getIdentificationInfo().iterator().next();
                assertEqualsMode(expId.getAbstract(), resId.getAbstract(), mode);
                assertEqualsMode(expId.getAggregationInfo(), resId.getAggregationInfo(), mode);
                assertEqualsMode(expId.getCitation(), resId.getCitation(), mode);
                assertEqualsMode(expId.getCredits(), resId.getCredits(), mode);
                assertEqualsMode(expId.getDescriptiveKeywords(), resId.getDescriptiveKeywords(), mode);
                assertEqualsMode(expId.getGraphicOverviews(), resId.getGraphicOverviews(), mode);
                assertEqualsMode(expId.getPointOfContacts(), resId.getPointOfContacts(), mode);
                assertEqualsMode(expId.getPurpose(), resId.getPurpose(), mode);
                assertEqualsMode(expId.getResourceConstraints(), resId.getResourceConstraints(), mode);
                assertEqualsMode(expId.getResourceFormats(), resId.getResourceFormats(), mode);
                assertEqualsMode(expId.getResourceMaintenances(), resId.getResourceMaintenances(), mode);
                assertEqualsMode(expId.getResourceSpecificUsages(), resId.getResourceSpecificUsages(), mode);
                assertEqualsMode(expId.getStatus(), resId.getStatus(), mode);


                if (expId instanceof DefaultDataIdentification) {
                    DefaultDataIdentification expDid = (DefaultDataIdentification) expId;
                    DefaultDataIdentification resDid = (DefaultDataIdentification) resId;
                    assertEqualsMode(expDid.getCharacterSets(), resDid.getCharacterSets(), mode);
                    assertEqualsMode(expDid.getEnvironmentDescription(), resDid.getEnvironmentDescription(), mode);
                    assertEquals(expDid.getExtents().size(), resDid.getExtents().size());

                    Iterator<Extent> expExtents = expDid.getExtents().iterator();
                    Iterator<Extent> resExtents = resDid.getExtents().iterator();
                    while (expExtents.hasNext()) {
                        Extent expExtent = expExtents.next();
                        Extent resExtent = resExtents.next();
                        assertEqualsMode(expExtent.getGeographicElements(), resExtent.getGeographicElements(), mode);
                        assertEqualsMode(expExtent.getTemporalElements(), resExtent.getTemporalElements(), mode);

                        Iterator<? extends VerticalExtent> expVExtents = expExtent.getVerticalElements().iterator();
                        Iterator<? extends VerticalExtent> resVExtents = resExtent.getVerticalElements().iterator();
                        while (expVExtents.hasNext()) {
                            VerticalExtent expVExtent = expVExtents.next();
                            VerticalExtent resVExtent = resVExtents.next();
                            assertEqualsMode(expVExtent.getMaximumValue(), resVExtent.getMaximumValue(), mode);
                            assertEqualsMode(expVExtent.getMinimumValue(), resVExtent.getMinimumValue(), mode);
                            if (expVExtent.getVerticalCRS() != null && resVExtent.getVerticalCRS() != null) {
                                assertEqualsMode(expVExtent.getVerticalCRS().getCoordinateSystem(), resVExtent.getVerticalCRS().getCoordinateSystem(), mode);
                                assertEqualsMode(expVExtent.getVerticalCRS().getDatum(), resVExtent.getVerticalCRS().getDatum(), mode);
                            }
                            assertEqualsMode(expVExtent.getVerticalCRS(), resVExtent.getVerticalCRS(), mode);

                            assertEqualsMode(expVExtent, resVExtent, mode);
                        }

                        assertEqualsMode(expExtent.getVerticalElements(), resExtent.getVerticalElements(), mode);
                        assertEqualsMode(expExtent, resExtent, mode);
                    }


                    assertEqualsMode(expDid.getExtents(), resDid.getExtents(), mode);
                    assertEqualsMode(expDid.getLanguages(), resDid.getLanguages(), mode);
                    assertEqualsMode(expDid.getSpatialRepresentationTypes(), resDid.getSpatialRepresentationTypes(), mode);
                    assertEqualsMode(expDid.getSpatialResolutions(), resDid.getSpatialResolutions(), mode);
                    assertEqualsMode(expDid.getSupplementalInformation(), resDid.getSupplementalInformation(), mode);
                    assertEqualsMode(expDid.getTopicCategories(), resDid.getTopicCategories(), mode);

                } else if (expId instanceof ServiceIdentificationImpl) {
                    ServiceIdentificationImpl expService = (ServiceIdentificationImpl) expId;
                    ServiceIdentificationImpl resService = (ServiceIdentificationImpl) result.getIdentificationInfo().iterator().next();
                    assertEqualsMode(expService.getOperatesOn(), resService.getOperatesOn(), mode);
                    assertEqualsMode(expService, resService, mode);
                }
            }
            assertEqualsMode(expResult.getIdentificationInfo(), result.getIdentificationInfo(), mode);
        }
        assertEqualsMode(expResult.getContentInfo(), result.getContentInfo(), mode);
        assertEqualsMode(expResult.getDistributionInfo(), result.getDistributionInfo(), mode);
        assertEqualsMode(expResult, result, mode);
    }

    public static void metadataEquals(final DefaultMetadata expResult, final DefaultMetadata result) {

        assertEquals(expResult.getAcquisitionInformation().size(), result.getAcquisitionInformation().size());
        Iterator<AcquisitionInformation> expAcquIt = expResult.getAcquisitionInformation().iterator();
        Iterator<AcquisitionInformation> resAcquIt = result.getAcquisitionInformation().iterator();
        while (expAcquIt.hasNext()) {
            AcquisitionInformation expAcqu = expAcquIt.next();
            AcquisitionInformation resAcqu = resAcquIt.next();
            assertEquals(expAcqu.getAcquisitionPlans(), resAcqu.getAcquisitionPlans());
            assertEquals(expAcqu.getAcquisitionRequirements(), resAcqu.getAcquisitionRequirements());
            assertEquals(expAcqu.getEnvironmentalConditions(), resAcqu.getEnvironmentalConditions());
            assertEquals(expAcqu.getInstruments(), resAcqu.getInstruments());
            assertEquals(expAcqu.getObjectives(), resAcqu.getObjectives());
            assertEquals(expAcqu.getOperations().size(), resAcqu.getOperations().size());
            Iterator<? extends Operation> expOperationsIt = expAcqu.getOperations().iterator();
            Iterator<? extends Operation> resOperationsIt = resAcqu.getOperations().iterator();
            while (expOperationsIt.hasNext()) {
                Operation expOperation = expOperationsIt.next();
                Operation resOperation = resOperationsIt.next();

                assertEquals(expOperation.getChildOperations(), resOperation.getChildOperations());
                assertEquals(expOperation.getCitation(), resOperation.getCitation());
                assertEquals(expOperation.getDescription(), resOperation.getDescription());
                assertEquals(expOperation.getIdentifier(), resOperation.getIdentifier());
                assertEquals(expOperation.getObjectives(), resOperation.getObjectives());
                assertEquals(expOperation.getParentOperation(), resOperation.getParentOperation());
                assertEquals(expOperation.getPlan(), resOperation.getPlan());
                assertEquals(expOperation.getPlatforms(), resOperation.getPlatforms());
                assertEquals(expOperation.getSignificantEvents(), resOperation.getSignificantEvents());
                assertEquals(expOperation.getType(), resOperation.getType());
                assertEquals(expOperation.getStatus(), resOperation.getStatus());
                assertEquals(expOperation, resOperation);
            }
            assertEquals(expAcqu.getOperations(), resAcqu.getOperations());
            assertEquals(expAcqu.getPlatforms(), resAcqu.getPlatforms());
            assertEquals(expAcqu, resAcqu);
        }
        assertEquals(expResult.getAcquisitionInformation(), result.getAcquisitionInformation());
        assertEquals(expResult.getApplicationSchemaInfo(), result.getApplicationSchemaInfo());
        assertEquals(expResult.getCharacterSet(), result.getCharacterSet());
        assertEquals(expResult.getContacts().size(), result.getContacts().size());
        if (expResult.getContacts().size() > 0) {
            ResponsibleParty expResp = expResult.getContacts().iterator().next();
            ResponsibleParty resResp = result.getContacts().iterator().next();

            assertEquals(expResp.getIndividualName(), resResp.getIndividualName());
            assertEquals(expResp.getOrganisationName(), resResp.getOrganisationName());
            assertEquals(expResp.getPositionName(), resResp.getPositionName());
            assertEquals(expResp.getRole(), resResp.getRole());
            if (expResp.getContactInfo() != null) {
                assertEquals(expResp.getContactInfo().getHoursOfService(), resResp.getContactInfo().getHoursOfService());
                assertEquals(expResp.getContactInfo().getContactInstructions(), resResp.getContactInfo().getContactInstructions());
                assertEquals(expResp.getContactInfo().getPhone(), resResp.getContactInfo().getPhone());
                assertEquals(expResp.getContactInfo().getOnlineResource(), resResp.getContactInfo().getOnlineResource());
                if (expResp.getContactInfo().getAddress() != null) {
                    assertEquals(expResp.getContactInfo().getAddress().getAdministrativeArea(), resResp.getContactInfo().getAddress().getAdministrativeArea());
                    assertEquals(expResp.getContactInfo().getAddress().getCity(), resResp.getContactInfo().getAddress().getCity());
                    assertEquals(expResp.getContactInfo().getAddress().getCountry(), resResp.getContactInfo().getAddress().getCountry());
                    assertEquals(expResp.getContactInfo().getAddress().getDeliveryPoints(), resResp.getContactInfo().getAddress().getDeliveryPoints());
                    assertEquals(expResp.getContactInfo().getAddress().getElectronicMailAddresses(), resResp.getContactInfo().getAddress().getElectronicMailAddresses());
                    assertEquals(expResp.getContactInfo().getAddress().getPostalCode(), resResp.getContactInfo().getAddress().getPostalCode());
                }
                assertEquals(expResp.getContactInfo().getAddress(), resResp.getContactInfo().getAddress());
            }
            assertEquals(expResp.getContactInfo(), resResp.getContactInfo());
        }
        assertEquals(expResult.getContacts(), result.getContacts());
        assertEquals(expResult.getContentInfo().size(), result.getContentInfo().size());

        Iterator<ContentInformation> expContentIt =  expResult.getContentInfo().iterator();
        Iterator<ContentInformation> resContentIt =  result.getContentInfo().iterator();
        while (expContentIt.hasNext()) {
            ContentInformation expContent = expContentIt.next();
            ContentInformation resContent = resContentIt.next();
            if (expContent instanceof FeatureCatalogueDescription) {
                assertTrue(resContent instanceof FeatureCatalogueDescription);
                FeatureCatalogueDescription expFeatureCatalogue = (FeatureCatalogueDescription) expContent;
                FeatureCatalogueDescription resFeatureCatalogue = (FeatureCatalogueDescription) resContent;
                assertEquals(expFeatureCatalogue.getFeatureCatalogueCitations(), resFeatureCatalogue.getFeatureCatalogueCitations());
                assertEquals(expFeatureCatalogue.getLanguages(), resFeatureCatalogue.getLanguages());
                assertEquals(expFeatureCatalogue.getFeatureTypes(), resFeatureCatalogue.getFeatureTypes());
            }
            if (expContent instanceof CoverageDescription) {
                assertTrue(resContent instanceof CoverageDescription);
                CoverageDescription expCovDesc = (CoverageDescription) expContent;
                CoverageDescription resCovDesc = (CoverageDescription) resContent;
                assertEquals(expContent.getClass().getName(), resContent.getClass().getName());
                assertEquals(expCovDesc.getAttributeDescription(), resCovDesc.getAttributeDescription());
                assertEquals(expCovDesc.getContentType(), resCovDesc.getContentType());
                assertEquals(expCovDesc.getRangeElementDescriptions(), resCovDesc.getRangeElementDescriptions());
                assertEquals(expCovDesc.getDimensions().size(), resCovDesc.getDimensions().size());
                Iterator<? extends RangeDimension> expDimIt = expCovDesc.getDimensions().iterator();
                Iterator<? extends RangeDimension> resDimIt = resCovDesc.getDimensions().iterator();
                while (expDimIt.hasNext()) {
                    RangeDimension expDim = expDimIt.next();
                    RangeDimension resDim = resDimIt.next();
                    assertEquals(expDim.getDescriptor(), resDim.getDescriptor());
                    assertEquals(expDim.getSequenceIdentifier(), resDim.getSequenceIdentifier());
                }
                assertEquals(expCovDesc.getDimensions(), resCovDesc.getDimensions());
            }
        }
        assertEquals(expResult.getContentInfo(), result.getContentInfo());
        assertEquals(expResult.getDataQualityInfo().size(), result.getDataQualityInfo().size());

        Iterator<DataQuality> expDqIt = expResult.getDataQualityInfo().iterator();
        Iterator<DataQuality> resDqIt = result.getDataQualityInfo().iterator();
        while (expDqIt.hasNext()) {
            DataQuality expDq = expDqIt.next();
            DataQuality resDq = resDqIt.next();
            if (expDq.getLineage() != null) {
                assertEquals(expDq.getLineage().getProcessSteps(), resDq.getLineage().getProcessSteps());
                assertEquals(expDq.getLineage().getStatement(), resDq.getLineage().getStatement());

                assertEquals(expDq.getLineage().getSources().size(), resDq.getLineage().getSources().size());
                Iterator<? extends Source> expSrcIt = expDq.getLineage().getSources().iterator();
                Iterator<? extends Source> resSrcIt = resDq.getLineage().getSources().iterator();
                while (expSrcIt.hasNext()) {
                    Source expSrc = expSrcIt.next();
                    Source resSrc = resSrcIt.next();
                    if (expSrc != null && resSrc == null) {
                        assertTrue(false);
                    } else if (expSrc == null && resSrc != null) {
                        assertTrue(false);
                    }
                    assertEquals(expSrc.getDescription(), resSrc.getDescription());
                    assertEquals(expSrc.getProcessedLevel(), resSrc.getProcessedLevel());
                    assertEquals(expSrc.getResolution(), resSrc.getResolution());
                    assertEquals(expSrc.getScaleDenominator(), resSrc.getScaleDenominator());
                    assertEquals(expSrc.getSourceCitation(), resSrc.getSourceCitation());
                    assertEquals(expSrc.getSourceExtents(), resSrc.getSourceExtents());
                    assertEquals(expSrc.getSourceReferenceSystem(), resSrc.getSourceReferenceSystem());
                    assertEquals(expSrc.getSourceSteps().size(), resSrc.getSourceSteps().size());
                    Iterator<? extends ProcessStep> expStepIt = expSrc.getSourceSteps().iterator();
                    Iterator<? extends ProcessStep> resStepIt = resSrc.getSourceSteps().iterator();
                    while (expStepIt.hasNext()) {
                        ProcessStep expStep = expStepIt.next();
                        ProcessStep resStep = resStepIt.next();
                        assertEquals(expStep.getDate(), resStep.getDate());
                        assertEquals(expStep.getDescription(), resStep.getDescription());
                        assertEquals(expStep.getOutputs(), resStep.getOutputs());
                        if (expStep.getProcessingInformation() != null) {
                            assertEquals(expStep.getProcessingInformation().getAlgorithms().size(), resStep.getProcessingInformation().getAlgorithms().size());
                            Iterator<? extends Algorithm> expAlgoIt = expStep.getProcessingInformation().getAlgorithms().iterator();
                            Iterator<? extends Algorithm> resAlgoIt = resStep.getProcessingInformation().getAlgorithms().iterator();
                            while (expAlgoIt.hasNext()) {
                                Algorithm expAlgo = expAlgoIt.next();
                                Algorithm resAlgo = resAlgoIt.next();
                                assertEquals(expAlgo.getDescription(), resAlgo.getDescription());
                                assertEquals(expAlgo.getCitation(), resAlgo.getCitation());
                            }
                            assertEquals(expStep.getProcessingInformation().getDocumentations(), resStep.getProcessingInformation().getDocumentations());
                            assertEquals(expStep.getProcessingInformation().getIdentifier(), resStep.getProcessingInformation().getIdentifier());
                            assertEquals(expStep.getProcessingInformation().getProcedureDescription(), resStep.getProcessingInformation().getProcedureDescription());
                            assertEquals(expStep.getProcessingInformation().getRunTimeParameters(), resStep.getProcessingInformation().getRunTimeParameters());
                            assertEquals(expStep.getProcessingInformation().getSoftwareReferences(), resStep.getProcessingInformation().getSoftwareReferences());
                        }
                        assertEquals(expStep.getProcessingInformation(), resStep.getProcessingInformation());
                        assertEquals(expStep.getProcessors(), resStep.getProcessors());
                        assertEquals(expStep.getRationale(), resStep.getRationale());
                        assertEquals(expStep.getReports(), resStep.getReports());
                        assertEquals(expStep.getSources(), resStep.getSources());
                        assertEquals(expStep, resStep);
                    }
                    assertEquals(expSrc.getSourceSteps(), resSrc.getSourceSteps());
                    assertEquals(expSrc, resSrc);
                }
                assertEquals(expDq.getLineage().getSources(), resDq.getLineage().getSources());
            }
            assertEquals(expDq.getLineage(), resDq.getLineage());
            assertEquals(expDq.getReports().size(), resDq.getReports().size());
            Iterator<? extends Element> expDqRep = expDq.getReports().iterator();
            Iterator<? extends Element> resDqRep = resDq.getReports().iterator();
            while (expDqRep.hasNext()) {
                Element expElement = expDqRep.next();
                Element resElement = resDqRep.next();
                assertEquals(expElement.getClass().getName(), resElement.getClass().getName());
                assertEquals(expElement.getDates(), resElement.getDates());
                assertEquals(expElement.getEvaluationMethodDescription(), resElement.getEvaluationMethodDescription());
                assertEquals(expElement.getEvaluationMethodType(), resElement.getEvaluationMethodType());
                assertEquals(expElement.getEvaluationProcedure(), resElement.getEvaluationProcedure());
                assertEquals(expElement.getMeasureDescription(), resElement.getMeasureDescription());
                assertEquals(expElement.getMeasureIdentification(), resElement.getMeasureIdentification());
                assertEquals(expElement.getNamesOfMeasure(), resElement.getNamesOfMeasure());
                assertEquals(expElement.getResults(), resElement.getResults());
                assertEquals(expElement, resElement);
            }
            assertEquals(expDq.getReports(), resDq.getReports());
            assertEquals(expDq.getScope(), resDq.getScope());
            assertEquals(expDq, resDq);
        }
        assertEquals(expResult.getDataQualityInfo(), result.getDataQualityInfo());
        assertEquals(expResult.getDataSetUri(), result.getDataSetUri());
        assertEquals(expResult.getDateStamp(), result.getDateStamp());
        assertEquals(expResult.getDistributionInfo(), result.getDistributionInfo());
        assertEquals(expResult.getFileIdentifier(), result.getFileIdentifier());
        assertEquals(expResult.getHierarchyLevelNames(), result.getHierarchyLevelNames());
        assertEquals(expResult.getHierarchyLevels(), result.getHierarchyLevels());
        if (expResult.getIdentificationInfo() != null && result.getIdentificationInfo() != null) {
            assertEquals(expResult.getIdentificationInfo().size(), result.getIdentificationInfo().size());
            for (int i = 0; i < expResult.getIdentificationInfo().size(); i++) {
                Identification expId = expResult.getIdentificationInfo().iterator().next();
                Identification resId = result.getIdentificationInfo().iterator().next();
                assertEquals(expId.getAbstract(), resId.getAbstract());
                assertEquals(expId.getAggregationInfo(), resId.getAggregationInfo());
                if (expId.getCitation() != null && resId.getCitation() != null) {
                    assertEquals(expId.getCitation().getDates().size(), resId.getCitation().getDates().size());
                    Iterator<? extends CitationDate> expCitDateIt = expId.getCitation().getDates().iterator();
                    Iterator<? extends CitationDate> resCitDateIt = resId.getCitation().getDates().iterator();
                    while (expCitDateIt.hasNext()) {
                        CitationDate expCitDate = expCitDateIt.next();
                        CitationDate resCitDate = resCitDateIt.next();
                        assertEquals(expCitDate.getDate(), resCitDate.getDate());
                        assertEquals(expCitDate.getDateType(), resCitDate.getDateType());
                        assertEquals(expCitDate, resCitDate);
                    }
                    assertEquals(expId.getCitation().getDates(), resId.getCitation().getDates());
                }
                assertEquals(expId.getCitation(), resId.getCitation());
                assertEquals(expId.getCredits(), resId.getCredits());
                if (resId.getDescriptiveKeywords().iterator().hasNext()) {
                        assertEquals(expId.getDescriptiveKeywords().iterator().next().getKeywords(), resId.getDescriptiveKeywords().iterator().next().getKeywords());
                        if (resId.getDescriptiveKeywords().iterator().next().getThesaurusName() != null) {
                            if (resId.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().hasNext()) {
                                assertEquals(expId.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next().getCode(), resId.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next().getCode());
                                assertEquals(expId.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next(), resId.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next());
                            }
                            assertEquals(expId.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers(), resId.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers());
                            citationEquals(expId.getDescriptiveKeywords().iterator().next().getThesaurusName(), resId.getDescriptiveKeywords().iterator().next().getThesaurusName());
                        }
                        assertEquals(expId.getDescriptiveKeywords().iterator().next().getThesaurusName(), resId.getDescriptiveKeywords().iterator().next().getThesaurusName());
                        assertEquals(expId.getDescriptiveKeywords().iterator().next().getType(), resId.getDescriptiveKeywords().iterator().next().getType());
                        assertEquals(expId.getDescriptiveKeywords().iterator().next(), resId.getDescriptiveKeywords().iterator().next());
                    }
                    assertEquals(expId.getDescriptiveKeywords(), resId.getDescriptiveKeywords());
                    assertEquals(expId.getGraphicOverviews(), resId.getGraphicOverviews());
                    assertEquals(expId.getPointOfContacts(), resId.getPointOfContacts());
                    assertEquals(expId.getPurpose(), resId.getPurpose());
                    assertEquals(expId.getResourceConstraints().size(), resId.getResourceConstraints().size());
                    if (expId.getResourceConstraints().size() > 0) {
                        Constraints expConst = expId.getResourceConstraints().iterator().next();
                        Constraints resConst = resId.getResourceConstraints().iterator().next();
                        assertEquals(expConst.getUseLimitations(), resConst.getUseLimitations());
                        assertEquals(expConst, resConst);
                    }
                    assertEquals(expId.getResourceConstraints(), resId.getResourceConstraints());
                    assertEquals(expId.getResourceFormats(), resId.getResourceFormats());
                    assertEquals(expId.getResourceMaintenances(), resId.getResourceMaintenances());
                    assertEquals(expId.getResourceSpecificUsages(), resId.getResourceSpecificUsages());
                    assertEquals(expId.getStatus(), resId.getStatus());

                if (expId instanceof DefaultDataIdentification) {
                    DefaultDataIdentification idExpResult = (DefaultDataIdentification) expId;
                    DefaultDataIdentification idResult    = (DefaultDataIdentification) result.getIdentificationInfo().iterator().next();
                    assertEquals(idExpResult.getCharacterSets(), idResult.getCharacterSets());
                    assertEquals(idExpResult.getEnvironmentDescription(), idResult.getEnvironmentDescription());
                    extentsEquals(idExpResult.getExtents(), idResult.getExtents());
                    assertEquals(idExpResult.getInterface(), idResult.getInterface());
                    assertEquals(idExpResult.getLanguages(), idResult.getLanguages());
                    assertEquals(idExpResult.getSpatialRepresentationTypes(), idResult.getSpatialRepresentationTypes());
                    assertEquals(idExpResult.getStandard(), idResult.getStandard());
                    assertEquals(idExpResult.getSupplementalInformation(), idResult.getSupplementalInformation());
                    assertEquals(idExpResult.getTopicCategories(), idResult.getTopicCategories());
                    
                    assertEquals(idExpResult, idResult);
                } else if (expId instanceof ServiceIdentificationImpl) {
                    ServiceIdentificationImpl expService = (ServiceIdentificationImpl) expId;
                    ServiceIdentificationImpl resService = (ServiceIdentificationImpl) result.getIdentificationInfo().iterator().next();
                    assertEquals(expService.getAccessProperties(), resService.getAccessProperties());
                    assertEquals(expService.getContainsOperations(), resService.getContainsOperations());
                    assertEquals(expService.getCoupledResource(), resService.getCoupledResource());
                    assertEquals(expService.getCouplingType(), resService.getCouplingType());
                    extentsEquals(expService.getExtent(), resService.getExtent());
                    assertEquals(expService.getOperatesOn().size(), resService.getOperatesOn().size());
                    Iterator<DataIdentification> expItOo = expService.getOperatesOn().iterator();
                    Iterator<DataIdentification> resItOo = resService.getOperatesOn().iterator();
                    while (expItOo.hasNext()) {
                        DefaultDataIdentification expOo = (DefaultDataIdentification) expItOo.next();
                        DefaultDataIdentification resOo = (DefaultDataIdentification) resItOo.next();
                        assertEquals(expOo.getIdentifierMap().getSpecialized(IdentifierSpace.XLINK), resOo.getIdentifierMap().getSpecialized(IdentifierSpace.XLINK));
                        assertEquals(expOo, resOo);
                    }
                    assertEquals(expService.getOperatesOn(), resService.getOperatesOn());
                    assertEquals(expService.getRestrictions(), resService.getRestrictions());
                    assertEquals(expService.getServiceType(), resService.getServiceType());
                    assertEquals(expService.getServiceTypeVersion(), resService.getServiceTypeVersion());
                    assertEquals(expService, resService);
                }
            }
            assertEquals(expResult.getIdentificationInfo(), result.getIdentificationInfo());
        }
        assertEquals(expResult.getLanguage(), result.getLanguage());
        assertEquals(expResult.getLocales(), result.getLocales());
        assertEquals(expResult.getMetadataConstraints(), result.getMetadataConstraints());
        assertEquals(expResult.getMetadataExtensionInfo(), result.getMetadataExtensionInfo());
        assertEquals(expResult.getMetadataMaintenance(), result.getMetadataMaintenance());
        assertEquals(expResult.getMetadataStandardName(), result.getMetadataStandardName());
        assertEquals(expResult.getMetadataStandardVersion(), result.getMetadataStandardVersion());
        assertEquals(expResult.getParentIdentifier(), result.getParentIdentifier());
        assertEquals(expResult.getPortrayalCatalogueInfo(), result.getPortrayalCatalogueInfo());
        assertEquals(expResult.getReferenceSystemInfo().size(), result.getReferenceSystemInfo().size());
        if (expResult.getReferenceSystemInfo().iterator().hasNext()) {
            if (expResult.getReferenceSystemInfo().iterator().next() != null) {
                if (expResult.getReferenceSystemInfo().iterator().next().getName() != null && result.getReferenceSystemInfo().iterator().next().getName() != null) {
                    final Citation resAuthority = result.getReferenceSystemInfo().iterator().next().getName().getAuthority();
                    final Citation expAuthority = expResult.getReferenceSystemInfo().iterator().next().getName().getAuthority();
                    if (resAuthority != null && expAuthority != null) {
                        citationEquals(expAuthority, resAuthority);
                    }
                    assertEquals(expAuthority, resAuthority);
                    assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName().getCodeSpace(), result.getReferenceSystemInfo().iterator().next().getName().getCodeSpace());
                }
                assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName(), result.getReferenceSystemInfo().iterator().next().getName());
            }
            assertEquals(expResult.getReferenceSystemInfo().iterator().next(), result.getReferenceSystemInfo().iterator().next());
        }
        assertEquals(expResult.getReferenceSystemInfo(), result.getReferenceSystemInfo());
        if (expResult.getSpatialRepresentationInfo() != null && result.getSpatialRepresentationInfo() != null) {
            assertEquals(expResult.getSpatialRepresentationInfo().size(), result.getSpatialRepresentationInfo().size());
            Iterator<SpatialRepresentation> expIt = expResult.getSpatialRepresentationInfo().iterator();
            Iterator<SpatialRepresentation> resIt = result.getSpatialRepresentationInfo().iterator();
            while (expIt.hasNext()) {
                SpatialRepresentation expSpa  = expIt.next();
                SpatialRepresentation  resSpa = resIt.next();
                if (expSpa instanceof Georectified) {
                    Georectified expRect = (Georectified) expSpa;
                    Georectified resRect = (Georectified) resSpa;
                    assertEquals(expRect.getAxisDimensionProperties(), resRect.getAxisDimensionProperties());
                    assertEquals(expRect.getCellGeometry(), resRect.getCellGeometry());
                    assertEquals(expRect.getCenterPoint(), resRect.getCenterPoint());
                    assertEquals(expRect.getCheckPointDescription(), resRect.getCheckPointDescription());
                    assertEquals(expRect.getCheckPoints(), resRect.getCheckPoints());
                    assertEquals(expRect.getCornerPoints(), resRect.getCornerPoints());
                    assertEquals(expRect.getPointInPixel(), resRect.getPointInPixel());
                    assertEquals(expRect.getTransformationDimensionDescription(), resRect.getTransformationDimensionDescription());
                    assertEquals(expRect.getTransformationDimensionMapping(), resRect.getTransformationDimensionMapping());
                    assertEquals(expRect.getNumberOfDimensions(), resRect.getNumberOfDimensions());
                    assertEquals(expRect, resRect);
                }
                assertEquals(expSpa, resSpa);
            }
        }
        assertEquals(expResult.getSpatialRepresentationInfo(), result.getSpatialRepresentationInfo());
        assertEquals(expResult, result);
    }

    public static void extentsEquals(final Collection<Extent> expectedExtents, final Collection<Extent> resultExtents) {
        assertEquals(expectedExtents.size(), resultExtents.size());

        Iterator<Extent> expIt = expectedExtents.iterator();
        Iterator<Extent> resIt = resultExtents.iterator();

        while (expIt.hasNext() && resIt.hasNext()) {
            Extent expEx = expIt.next();
            Extent resEx = resIt.next();
            assertEquals(expEx.getGeographicElements().size(), resEx.getGeographicElements().size());
            Iterator<? extends GeographicExtent> expGeExIt = expEx.getGeographicElements().iterator();
            Iterator<? extends GeographicExtent> resGeExIt = resEx.getGeographicElements().iterator();
            while (expGeExIt.hasNext() && resGeExIt.hasNext()) {
                GeographicExtent expGeEx = expGeExIt.next();
                GeographicExtent resGeEx = resGeExIt.next();

                //assertEquals(expGeEx.getInclusion(), resGeEx.getInclusion());
                assertEquals(expGeEx, resGeEx);
            }
            assertEquals(expEx.getGeographicElements(), resEx.getGeographicElements());
            assertEquals(expEx.getVerticalElements().size(),   resEx.getVerticalElements().size());
            Iterator<? extends VerticalExtent> expVIt = expEx.getVerticalElements().iterator();
            Iterator<? extends VerticalExtent> resVIt = resEx.getVerticalElements().iterator();
            while (expVIt.hasNext() && resVIt.hasNext()) {
                VerticalExtent expVEx = expVIt.next();
                VerticalExtent resVEx = resVIt.next();
                if (expVEx != null && resVEx != null) {
                    if (expVEx.getVerticalCRS() != null && resVEx.getVerticalCRS() != null) {
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getDimension(), resVEx.getVerticalCRS().getCoordinateSystem().getDimension());
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getUnit(), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getUnit());
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getDirection(), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getDirection());
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getAbbreviation(), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getAbbreviation());
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getRangeMeaning(), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getRangeMeaning());
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getIdentifiers(), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getIdentifiers());
                        final ReferenceIdentifier expName = expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getName();
                        final ReferenceIdentifier resName = resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getName();
                        assertEquals(expName, resName);
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0));
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getName(), resVEx.getVerticalCRS().getCoordinateSystem().getName());
                        assertEquals(expVEx.getVerticalCRS().getCoordinateSystem(), resVEx.getVerticalCRS().getCoordinateSystem());
                        assertEquals(expVEx.getVerticalCRS().getDatum().getName(), resVEx.getVerticalCRS().getDatum().getName());
                        assertEquals(expVEx.getVerticalCRS().getDatum().getVerticalDatumType(), resVEx.getVerticalCRS().getDatum().getVerticalDatumType());
                        assertEquals(expVEx.getVerticalCRS().getDatum(), resVEx.getVerticalCRS().getDatum());
                        assertEquals(expVEx.getVerticalCRS(), resVEx.getVerticalCRS());
                    }
                }
                assertEquals(expVEx, resVEx);
            }
            assertEquals(expEx.getTemporalElements(),   resEx.getTemporalElements());
        }

        assertEquals(expectedExtents, resultExtents);
    }

    public static void citationEquals(final Citation expectedCitation, final Citation resultCitation) {
        if (expectedCitation != null && resultCitation != null) {
            assertEquals(expectedCitation.getAlternateTitles(), resultCitation.getAlternateTitles());
            assertEquals(expectedCitation.getCitedResponsibleParties(), resultCitation.getCitedResponsibleParties());
            assertEquals(expectedCitation.getCollectiveTitle(), resultCitation.getCollectiveTitle());
            assertEquals(expectedCitation.getDates(), resultCitation.getDates());
            assertEquals(expectedCitation.getEdition(), resultCitation.getEdition());
            assertEquals(expectedCitation.getEditionDate(), resultCitation.getEditionDate());
            assertEquals(expectedCitation.getISBN(), resultCitation.getISBN());
            assertEquals(expectedCitation.getISSN(), resultCitation.getISSN());
            assertEquals(expectedCitation.getIdentifiers(), resultCitation.getIdentifiers());
            assertEquals(expectedCitation.getOtherCitationDetails(), resultCitation.getOtherCitationDetails());
            assertEquals(expectedCitation.getPresentationForms(), resultCitation.getPresentationForms());
            assertEquals(expectedCitation.getSeries(), resultCitation.getSeries());
            if (expectedCitation.getTitle() instanceof DefaultInternationalString) {
                assertTrue("result citation title:" + resultCitation.getTitle() +"\n expected:" + expectedCitation.getTitle(), resultCitation.getTitle() instanceof DefaultInternationalString);
                DefaultInternationalString expTitle = (DefaultInternationalString) expectedCitation.getTitle();
                DefaultInternationalString resTitle = (DefaultInternationalString) resultCitation.getTitle();
                assertEquals(expTitle.getLocales(), resTitle.getLocales());
                assertEquals(expTitle, resTitle);
            }
            assertEquals(expectedCitation.getTitle(), resultCitation.getTitle());
        }
    }

    public static void catalogueEquals(final FeatureCatalogueImpl expResult, final FeatureCatalogueImpl result) {

        assertEquals(expResult.getDefinitionSource(), result.getDefinitionSource());

        assertEquals(expResult.getFeatureType().size(), result.getFeatureType().size());
        for (int i = 0; i < expResult.getFeatureType().size(); i++) {
            FeatureTypeImpl expFtype = (FeatureTypeImpl) expResult.getFeatureType().get(i);
            FeatureTypeImpl resFtype = (FeatureTypeImpl) result.getFeatureType().get(i);
            assertEquals(expFtype.getAliases(), resFtype.getAliases());
            assertEquals(expFtype.getCarrierOfCharacteristics().size(), resFtype.getCarrierOfCharacteristics().size());
            for (int j =0; j< expFtype.getCarrierOfCharacteristics().size(); j++) {
                PropertyTypeImpl expCarrier = (PropertyTypeImpl) expFtype.getCarrierOfCharacteristics().get(j);
                PropertyTypeImpl resCarrier = (PropertyTypeImpl) resFtype.getCarrierOfCharacteristics().get(j);
                assertEquals(expCarrier.getCardinality(), resCarrier.getCardinality());
                assertEquals(expCarrier.getConstrainedBy(), resCarrier.getConstrainedBy());
                assertEquals(expCarrier.getDefinition(), resCarrier.getDefinition());
                assertEquals(expCarrier.getDefinitionReference(), resCarrier.getDefinitionReference());
                assertEquals(expCarrier.getFeatureType(), resCarrier.getFeatureType());
                assertEquals(expCarrier.getId(), resCarrier.getId());
                assertEquals(expCarrier.getMemberName(), resCarrier.getMemberName());
                if (expCarrier instanceof FeatureAttributeImpl) {
                    assertTrue(resCarrier instanceof FeatureAttributeImpl);
                    FeatureAttributeImpl expAtt = (FeatureAttributeImpl) expCarrier;
                    FeatureAttributeImpl resAtt = (FeatureAttributeImpl) resCarrier;
                    assertEquals(expAtt.getCode(), resAtt.getCode());
                    assertEquals(expAtt.getListedValue(), resAtt.getListedValue());
                    assertEquals(expAtt.getValueType(), resAtt.getValueType());
                }
                assertEquals(expCarrier, resCarrier);
            }
            assertEquals(expFtype.getCarrierOfCharacteristics(), resFtype.getCarrierOfCharacteristics());
            assertEquals(expFtype.getCode(), resFtype.getCode());
            assertEquals(expFtype.getConstrainedBy(), resFtype.getConstrainedBy());
            assertEquals(expFtype.getDefinition(), resFtype.getDefinition());
            assertEquals(expFtype.getDefinitionReference(), resFtype.getDefinitionReference());
            assertEquals(expFtype.getFeatureCatalogue(), resFtype.getFeatureCatalogue());
            assertEquals(expFtype.getId(), resFtype.getId());
            assertEquals(expFtype.getInheritsFrom(), resFtype.getInheritsFrom());
            assertEquals(expFtype.getInheritsTo(), resFtype.getInheritsTo());
            assertEquals(expFtype.getIsAbstract(), resFtype.getIsAbstract());
            assertEquals(expFtype.getReference(), resFtype.getReference());
            assertEquals(expFtype.getTypeName(), resFtype.getTypeName());
            assertEquals(expFtype, resFtype);
        }
        assertEquals(expResult.getFeatureType(), result.getFeatureType());
        assertEquals(expResult.getFieldOfApplication(), result.getFieldOfApplication());
        assertEquals(expResult.getFunctionalLanguage(), result.getFunctionalLanguage());
        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getProducer(), result.getProducer());
        assertEquals(expResult.getReference(), result.getReference());
        assertEquals(expResult.getScope(), result.getScope());
        assertEquals(expResult.getVersionDate(), result.getVersionDate());
        assertEquals(expResult.getVersionNumber(), result.getVersionNumber());
    }


    public static void sensorMLEquals(final SensorML expResult, final SensorML result) {
        assertEquals(expResult.getCapabilities(), result.getCapabilities());
        assertEquals(expResult.getCharacteristics(), result.getCharacteristics());
        assertEquals(expResult.getClassification(), result.getClassification());
        assertEquals(expResult.getContact(), result.getContact());
        assertEquals(expResult.getDocumentation(), result.getDocumentation());
        assertEquals(expResult.getHistory(), result.getHistory());
        assertEquals(expResult.getIdentification(), result.getIdentification());
        assertEquals(expResult.getKeywords(), result.getKeywords());
        assertEquals(expResult.getLegalConstraint(), result.getLegalConstraint());
        assertEquals(expResult.getSecurityConstraint(), result.getSecurityConstraint());
        assertEquals(expResult.getValidTime(), result.getValidTime());
        assertEquals(expResult.getVersion(), result.getVersion());

        assertEquals(expResult.getMember().size(), result.getMember().size());
    }

    public static void systemSMLEquals(final SensorML expResult, final SensorML result) {

        sensorMLEquals(expResult, result);
        assertEquals(expResult.getMember().size(), 1);
        SystemType expSysProcess = (SystemType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof SystemType);
        SystemType resSysProcess = (SystemType) result.getMember().iterator().next().getProcess().getValue();

        assertEquals(expSysProcess.getOutputs().getOutputList().getOutput().size(), resSysProcess.getOutputs().getOutputList().getOutput().size());
        Iterator<IoComponentPropertyType> expIt = expSysProcess.getOutputs().getOutputList().getOutput().iterator();
        Iterator<IoComponentPropertyType> resIt = resSysProcess.getOutputs().getOutputList().getOutput().iterator();
        for (int i = 0; i < expSysProcess.getOutputs().getOutputList().getOutput().size(); i++) {
            IoComponentPropertyType resio = resIt.next();
            IoComponentPropertyType expio = expIt.next();
            DataRecordType resIoRec       = (DataRecordType) resio.getAbstractDataRecord().getValue();
            DataRecordType expIoRec       = (DataRecordType) expio.getAbstractDataRecord().getValue();
            assertEquals(expIoRec.getId(), resIoRec.getId());
            assertEquals(expIoRec.getField().size(), resIoRec.getField().size());
            assertEquals(expIoRec.getField().get(0), resIoRec.getField().get(0));
            assertEquals(expIoRec.getField().get(1), resIoRec.getField().get(1));
            assertEquals(expIoRec.getField().get(2), resIoRec.getField().get(2));
            assertEquals(expIoRec.getField(), resIoRec.getField());
            assertEquals(expIoRec, resIoRec);
            assertEquals(expio, resio);
        }

        assertEquals(expSysProcess.getOutputs().getOutputList().getOutput(), resSysProcess.getOutputs().getOutputList().getOutput());
        assertEquals(expSysProcess.getOutputs().getOutputList(), resSysProcess.getOutputs().getOutputList());
        assertEquals(expSysProcess.getOutputs(), resSysProcess.getOutputs());

        assertEquals(expSysProcess.getBoundedBy(), resSysProcess.getBoundedBy());

        if (expSysProcess.getCapabilities().size() > 0 && resSysProcess.getCapabilities().size() > 0) {
            assertTrue(resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue() instanceof DataRecordType);
            DataRecordType expRecord = (DataRecordType) expSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            DataRecordType resRecord = (DataRecordType) resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            assertEquals(expRecord.getField(), resRecord.getField());
            assertEquals(expSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue(), resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue());
            assertEquals(expSysProcess.getCapabilities().get(0), resSysProcess.getCapabilities().get(0));
        }
        assertEquals(expSysProcess.getCapabilities(), resSysProcess.getCapabilities());


        assertEquals(expSysProcess.getCharacteristics().iterator().next(), resSysProcess.getCharacteristics().iterator().next());
        assertEquals(expSysProcess.getCharacteristics(), resSysProcess.getCharacteristics());

        assertEquals(expSysProcess.getClassification().size(), resSysProcess.getClassification().size());
        assertEquals(resSysProcess.getClassification().size(), 1);
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier().size(), resSysProcess.getClassification().get(0).getClassifierList().getClassifier().size());
        for (int i = 0; i < expSysProcess.getClassification().get(0).getClassifierList().getClassifier().size(); i++) {
            assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier().get(i), resSysProcess.getClassification().get(0).getClassifierList().getClassifier().get(i));
        }

        assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier(), resSysProcess.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList(), resSysProcess.getClassification().get(0).getClassifierList());
        assertEquals(expSysProcess.getClassification().get(0), resSysProcess.getClassification().get(0));
        assertEquals(expSysProcess.getClassification(), resSysProcess.getClassification());
        assertEquals(expSysProcess.getConnections(), resSysProcess.getConnections());

        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress(), resSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress());
        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo(), resSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo());
        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty(), resSysProcess.getContact().iterator().next().getResponsibleParty());
        assertEquals(expSysProcess.getContact().iterator().next(), resSysProcess.getContact().iterator().next());
        assertEquals(expSysProcess.getContact(), resSysProcess.getContact());
        assertEquals(expSysProcess.getDescription(), resSysProcess.getDescription());
        assertEquals(expSysProcess.getDescriptionReference(), resSysProcess.getDescriptionReference());
        assertEquals(expSysProcess.getDocumentation().size(), resSysProcess.getDocumentation().size());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument().getOnlineResource(), resSysProcess.getDocumentation().get(0).getDocument().getOnlineResource());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument().getDescription(), resSysProcess.getDocumentation().get(0).getDocument().getDescription());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument(), resSysProcess.getDocumentation().get(0).getDocument());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocumentList(), resSysProcess.getDocumentation().get(0).getDocumentList());
        assertEquals(expSysProcess.getDocumentation().get(0), resSysProcess.getDocumentation().get(0));
        assertEquals(expSysProcess.getDocumentation(), resSysProcess.getDocumentation());
        assertEquals(expSysProcess.getHistory().size(), resSysProcess.getHistory().size());
        for (int i = 0; i < expSysProcess.getHistory().size(); i++) {
            assertEquals(expSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getContact(), resSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getContact());
            assertEquals(expSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getDocumentation(), resSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getDocumentation());
            assertEquals(expSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getIdentification(), resSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getIdentification());
            assertEquals(expSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getKeywords(), resSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getKeywords());
            assertEquals(expSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getProperty(), resSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getProperty());
            assertEquals(expSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getClassification(), resSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent().getClassification());
            assertEquals(expSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent(), resSysProcess.getHistory().get(i).getEventList().getMember().get(0).getEvent());
            assertEquals(expSysProcess.getHistory().get(i).getEventList(), resSysProcess.getHistory().get(i).getEventList());
        }
        assertEquals(expSysProcess.getHistory(), resSysProcess.getHistory());
        assertEquals(expSysProcess.getId(), resSysProcess.getId());
        assertEquals(expSysProcess.getIdentification(), resSysProcess.getIdentification());
        assertEquals(expSysProcess.getInputs(), resSysProcess.getInputs());
        assertEquals(expSysProcess.getInterfaces(), resSysProcess.getInterfaces());
        assertEquals(expSysProcess.getKeywords(), resSysProcess.getKeywords());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument().getDescription(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument().getDescription());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights(), resSysProcess.getLegalConstraint().get(0).getRights());
        assertEquals(expSysProcess.getLegalConstraint().get(0), resSysProcess.getLegalConstraint().get(0));
        assertEquals(expSysProcess.getLegalConstraint(), resSysProcess.getLegalConstraint());
        assertEquals(expSysProcess.getLocation(), resSysProcess.getLocation());
        assertEquals(expSysProcess.getName(), resSysProcess.getName());
        assertEquals(expSysProcess.getComponents(), resSysProcess.getComponents());

        assertEquals(expSysProcess.getParameters(), resSysProcess.getParameters());
        assertEquals(expSysProcess.getPosition(), resSysProcess.getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getDefinition(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getDefinition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getCoordinate(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getCoordinate());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getVector(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getVector());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getName(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getName());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0), resSysProcess.getPositions().getPositionList().getPosition().get(0));
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition(), resSysProcess.getPositions().getPositionList().getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList(), resSysProcess.getPositions().getPositionList());
        assertEquals(expSysProcess.getPositions(), resSysProcess.getPositions());

        assertEquals(expSysProcess.getSMLLocation().getPoint().getPos(), resSysProcess.getSMLLocation().getPoint().getPos());
        assertEquals(expSysProcess.getSMLLocation().getPoint().getAxisLabels(), resSysProcess.getSMLLocation().getPoint().getAxisLabels());
        assertEquals(expSysProcess.getSMLLocation().getPoint().getUomLabels(), resSysProcess.getSMLLocation().getPoint().getUomLabels());
        assertEquals(expSysProcess.getSMLLocation().getPoint(), resSysProcess.getSMLLocation().getPoint());
        assertEquals(expSysProcess.getSMLLocation(), resSysProcess.getSMLLocation());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getSrsName(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getSrsName());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesCS(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesCS());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesEngineeringDatum(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesEngineeringDatum());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS());
        assertEquals(expSysProcess.getSpatialReferenceFrame(), resSysProcess.getSpatialReferenceFrame());
        assertEquals(expSysProcess.getSrsName(), resSysProcess.getSrsName());
        assertEquals(expSysProcess.getTemporalReferenceFrame(), resSysProcess.getTemporalReferenceFrame());
        assertEquals(expSysProcess.getTimePosition(), resSysProcess.getTimePosition());
        assertEquals(expSysProcess.getValidTime(), resSysProcess.getValidTime());


        assertEquals(expResult.getMember().iterator().next().getArcrole(), result.getMember().iterator().next().getArcrole());
        assertEquals(expResult.getMember().iterator().next(), result.getMember().iterator().next());
        assertEquals(expResult.getMember(), result.getMember());


        assertEquals(expResult, result);
    }

    public static void componentEquals(final SensorML expResult, final SensorML result) {

        sensorMLEquals(expResult, result);

        assertEquals(expResult.getMember().size(), 1);
        ComponentType expProcess = (ComponentType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof ComponentType);
        ComponentType resProcess = (ComponentType) result.getMember().iterator().next().getProcess().getValue();

        assertEquals(expProcess.getOutputs().getOutputList().getOutput().size(), resProcess.getOutputs().getOutputList().getOutput().size());
        Iterator<IoComponentPropertyType> expIt = expProcess.getOutputs().getOutputList().getOutput().iterator();
        Iterator<IoComponentPropertyType> resIt = resProcess.getOutputs().getOutputList().getOutput().iterator();
        for (int i = 0; i < expProcess.getOutputs().getOutputList().getOutput().size(); i++) {
            IoComponentPropertyType resio = resIt.next();
            IoComponentPropertyType expio = expIt.next();
            assertEquals(expio, resio);
        }

        assertEquals(expProcess.getOutputs().getOutputList().getOutput(), resProcess.getOutputs().getOutputList().getOutput());
        assertEquals(expProcess.getOutputs().getOutputList(), resProcess.getOutputs().getOutputList());
        assertEquals(expProcess.getOutputs(), resProcess.getOutputs());

        if (expProcess.getBoundedBy() != null) {
            assertEquals(expProcess.getBoundedBy().getEnvelope().getLowerCorner(), resProcess.getBoundedBy().getEnvelope().getLowerCorner());
            assertEquals(expProcess.getBoundedBy().getEnvelope(), resProcess.getBoundedBy().getEnvelope());
        }
        assertEquals(expProcess.getBoundedBy(), resProcess.getBoundedBy());

        if (expProcess.getCapabilities().size() > 0 && resProcess.getCapabilities().size() > 0) {
            assertTrue(resProcess.getCapabilities().get(0).getAbstractDataRecord().getValue() instanceof DataRecordType);
            DataRecordType expRecord = (DataRecordType) expProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            DataRecordType resRecord = (DataRecordType) resProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            assertEquals(expRecord.getField(), resRecord.getField());
            assertEquals(expProcess.getCapabilities().get(0).getAbstractDataRecord().getValue(), resProcess.getCapabilities().get(0).getAbstractDataRecord().getValue());
            assertEquals(expProcess.getCapabilities().get(0), resProcess.getCapabilities().get(0));
        }
        assertEquals(expProcess.getCapabilities(), resProcess.getCapabilities());

        assertEquals(expProcess.getCharacteristics(), resProcess.getCharacteristics());

        assertEquals(expProcess.getClassification().size(), resProcess.getClassification().size());
        assertEquals(resProcess.getClassification().size(), 1);
        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier().size(), resProcess.getClassification().get(0).getClassifierList().getClassifier().size());

        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier(), resProcess.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expProcess.getClassification().get(0).getClassifierList(), resProcess.getClassification().get(0).getClassifierList());
        assertEquals(expProcess.getClassification().get(0), resProcess.getClassification().get(0));
        assertEquals(expProcess.getClassification(), resProcess.getClassification());

        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty(), resProcess.getContact().iterator().next().getResponsibleParty());
        assertEquals(expProcess.getContact().iterator().next(), resProcess.getContact().iterator().next());
        assertEquals(expProcess.getContact(), resProcess.getContact());
        assertEquals(expProcess.getDescription(), resProcess.getDescription());
        assertEquals(expProcess.getDescriptionReference(), resProcess.getDescriptionReference());
        assertEquals(expProcess.getDocumentation(), resProcess.getDocumentation());
        assertEquals(expProcess.getHistory(), resProcess.getHistory());
        assertEquals(expProcess.getId(), resProcess.getId());
        assertEquals(expProcess.getIdentification(), resProcess.getIdentification());
        assertEquals(expProcess.getInputs(), resProcess.getInputs());
        assertEquals(expProcess.getInterfaces(), resProcess.getInterfaces());
        assertEquals(expProcess.getKeywords(), resProcess.getKeywords());
        assertEquals(expProcess.getLegalConstraint(), resProcess.getLegalConstraint());
        assertEquals(expProcess.getLocation(), resProcess.getLocation());
        assertEquals(expProcess.getName(), resProcess.getName());

        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(0), resProcess.getParameters().getParameterList().getParameter().get(0));
        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(1), resProcess.getParameters().getParameterList().getParameter().get(1));
        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(2).getQuantityRange(), resProcess.getParameters().getParameterList().getParameter().get(2).getQuantityRange());
        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(2), resProcess.getParameters().getParameterList().getParameter().get(2));
        assertEquals(expProcess.getParameters().getParameterList().getParameter(), resProcess.getParameters().getParameterList().getParameter());
        assertEquals(expProcess.getParameters().getParameterList(), resProcess.getParameters().getParameterList());
        assertEquals(expProcess.getParameters(), resProcess.getParameters());
        assertEquals(expProcess.getSMLLocation(), resProcess.getSMLLocation());
        assertEquals(expProcess.getSpatialReferenceFrame(), resProcess.getSpatialReferenceFrame());
        assertEquals(expProcess.getSrsName(), resProcess.getSrsName());
        assertEquals(expProcess.getTemporalReferenceFrame(), resProcess.getTemporalReferenceFrame());
        assertEquals(expProcess.getTimePosition(), resProcess.getTimePosition());
        if (expProcess.getValidTime() != null && resProcess.getValidTime() != null) {
            if (expProcess.getValidTime().getTimePeriod() != null && resProcess.getValidTime().getTimePeriod() != null) {
                if (expProcess.getValidTime().getTimePeriod().getBeginPosition()!= null && resProcess.getValidTime().getTimePeriod().getBeginPosition() != null) {
                    assertEquals(expProcess.getValidTime().getTimePeriod().getBeginPosition().getValue(), resProcess.getValidTime().getTimePeriod().getBeginPosition().getValue());
                }
                assertEquals(expProcess.getValidTime().getTimePeriod().getBeginPosition(), resProcess.getValidTime().getTimePeriod().getBeginPosition());
                assertEquals(expProcess.getValidTime().getTimePeriod().getEndPosition(), resProcess.getValidTime().getTimePeriod().getEndPosition());
            }
            assertEquals(expProcess.getValidTime().getTimePeriod(), resProcess.getValidTime().getTimePeriod());
        }
        assertEquals(expProcess.getValidTime(), resProcess.getValidTime());

        assertEquals(expResult.getMember().iterator().next().getArcrole(), result.getMember().iterator().next().getArcrole());
        assertEquals(expResult.getMember().iterator().next(), result.getMember().iterator().next());
        assertEquals(expResult.getMember(), result.getMember());

        assertEquals(expResult, result);
    }

    public static void ebrimEquals(final ExtrinsicObjectType expResult, final ExtrinsicObjectType result) {
        assertEquals(expResult.getExpiration(), result.getExpiration());
        assertEquals(expResult.getDescription(), result.getDescription());
        assertEquals(expResult.getClassification(), result.getClassification());
        assertEquals(expResult.getExternalIdentifier(), result.getExternalIdentifier());
        assertEquals(expResult.getHome(), result.getHome());
        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getIsOpaque(), result.getIsOpaque());
        assertEquals(expResult.getMajorVersion(), result.getMajorVersion());
        assertEquals(expResult.getMimeType(), result.getMimeType());
        assertEquals(expResult.getMinorVersion(), result.getMinorVersion());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getObjectType(), result.getObjectType());
        assertEquals(expResult.getSlot().size(), result.getSlot().size());
        Iterator<SlotType> expSlotIt = expResult.getSlot().iterator();
        Iterator<SlotType> resSlotIt = result.getSlot().iterator();
        while (expSlotIt.hasNext()) {
            SlotType resSlot = resSlotIt.next();
            SlotType expSlot = expSlotIt.next();
            assertEquals(expSlot.getName(), resSlot.getName());
            assertEquals(expSlot.getSlotType(), resSlot.getSlotType());
            if (expSlot.getValueList() != null && resSlot.getValueList() != null) {
                assertEquals(expSlot.getValueList().getValue(), resSlot.getValueList().getValue());
            }
            assertEquals(expSlot.getValueList(), resSlot.getValueList());
            assertEquals(expSlot, resSlot);
        }
        assertEquals(expResult.getSlot(), result.getSlot());

        assertEquals(expResult.getStability(), result.getStability());
        assertEquals(expResult.getStatus(), result.getStatus());
        assertEquals(expResult.getUserVersion(), result.getUserVersion());
        assertEquals(expResult, result);
    }

    public static void ebrimEquals(final RegistryPackageType expResult, final RegistryPackageType result) {
        assertEquals(expResult.getClassification(), result.getClassification());
        assertEquals(expResult.getDescription(), result.getDescription());
        assertEquals(expResult.getExternalIdentifier(), result.getExternalIdentifier());
        assertEquals(expResult.getHome(), result.getHome());
        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getLid(), result.getLid());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getObjectType(), result.getObjectType());
        if (expResult.getRegistryObjectList() != null && result.getRegistryObjectList() != null) {
            assertEquals(expResult.getRegistryObjectList().getIdentifiable().size(), result.getRegistryObjectList().getIdentifiable().size());
            Iterator<JAXBElement<? extends IdentifiableType>> expIdentList = expResult.getRegistryObjectList().getIdentifiable().iterator();
            Iterator<JAXBElement<? extends IdentifiableType>> resIdentList = result.getRegistryObjectList().getIdentifiable().iterator();
            while (expIdentList.hasNext()) {
                IdentifiableType expIdent = (IdentifiableType) ((JAXBElement)expIdentList.next()).getValue();
                IdentifiableType resIdent = (IdentifiableType) ((JAXBElement)resIdentList.next()).getValue();
                assertEquals(expIdent.getHome(), resIdent.getHome());
                assertEquals(expIdent.getId(), resIdent.getId());
                if (expIdent.getSlot() != null && resIdent.getSlot() != null) {
                    assertEquals(expIdent.getSlot().size(), resIdent.getSlot().size());
                    Iterator<org.geotoolkit.ebrim.xml.v300.SlotType> expSlotIt = expIdent.getSlot().iterator();
                    Iterator<org.geotoolkit.ebrim.xml.v300.SlotType> resSlotIt = resIdent.getSlot().iterator();
                    while (expSlotIt.hasNext()) {
                        org.geotoolkit.ebrim.xml.v300.SlotType expSlot = expSlotIt.next();
                        org.geotoolkit.ebrim.xml.v300.SlotType resSlot = resSlotIt.next();
                        assertEquals(expSlot.getName(), resSlot.getName());
                        assertEquals(expSlot.getSlotType(), resSlot.getSlotType());
                        if (expSlot.getValueList() != null && resSlot.getValueList() != null) {
                            assertEquals(expSlot.getValueList().getValue(), resSlot.getValueList().getValue());
                        }
                        assertEquals(expSlot, resSlot);
                    }
                }
                assertEquals(expIdent.getSlot(), resIdent.getSlot());

                if (expIdent instanceof RegistryObjectType) {
                    assertTrue(resIdent instanceof RegistryObjectType);
                    RegistryObjectType expReg = (RegistryObjectType) expIdent;
                    RegistryObjectType resReg = (RegistryObjectType) resIdent;
                    assertEquals(expReg.getClassification(), resReg.getClassification());
                    assertEquals(expReg.getDescription(), resReg.getDescription());
                    assertEquals(expReg.getExternalIdentifier(), resReg.getExternalIdentifier());
                    assertEquals(expReg.getLid(), resReg.getLid());
                    assertEquals(expReg.getName(), resReg.getName());
                    assertEquals(expReg.getObjectType(), resReg.getObjectType());
                    assertEquals(expReg.getStatus(), resReg.getStatus());
                    assertEquals(expReg.getVersionInfo(), resReg.getVersionInfo());

                    if (expIdent instanceof AdhocQueryType) {
                        assertTrue(resIdent instanceof AdhocQueryType);
                        AdhocQueryType expAq = (AdhocQueryType) expIdent;
                        AdhocQueryType resAq = (AdhocQueryType) resIdent;
                        if (expAq.getQueryExpression() != null && resAq.getQueryExpression() != null) {
                            if (expAq.getQueryExpression().getContent() != null && resAq.getQueryExpression().getContent() != null) {
                                assertEquals(expAq.getQueryExpression().getContent().size(), resAq.getQueryExpression().getContent().size());
                                for (int i = 0; i < expAq.getQueryExpression().getContent().size(); i++) {
                                    Object expCont = expAq.getQueryExpression().getContent().get(i);
                                    Object resCont = resAq.getQueryExpression().getContent().get(i);
                                    if (expCont instanceof JAXBElement) {
                                        expCont = ((JAXBElement)expCont).getValue();
                                    }
                                    if (resCont instanceof JAXBElement) {
                                        resCont = ((JAXBElement)resCont).getValue();
                                    }
                                    if (expCont instanceof GetRecordsType) {
                                        assertTrue("unexpected type:" + resCont.getClass().getName(), resCont instanceof GetRecordsType);
                                        GetRecordsType expGR = (GetRecordsType) expCont;
                                        GetRecordsType resGR = (GetRecordsType) resCont;
                                        if (expGR.getAbstractQuery() instanceof QueryType) {
                                            assertTrue(resGR.getAbstractQuery() instanceof QueryType);
                                            QueryType expQuery = (QueryType) expGR.getAbstractQuery();
                                            QueryType resQuery = (QueryType) resGR.getAbstractQuery();
                                            assertEquals(expQuery.getElementName(), resQuery.getElementName());
                                            if (expQuery.getConstraint() != null && resQuery.getConstraint() != null) {
                                                assertEquals(expQuery.getConstraint().getCqlText(), resQuery.getConstraint().getCqlText());
                                                if (expQuery.getConstraint().getFilter() != null && resQuery.getConstraint().getFilter() != null) {
                                                    if (expQuery.getConstraint().getFilter().getComparisonOps() != null){
                                                        assertEquals(expQuery.getConstraint().getFilter().getComparisonOps().getValue(), resQuery.getConstraint().getFilter().getComparisonOps().getValue());
                                                    }
                                                    if (expQuery.getConstraint().getFilter().getLogicOps() != null) {
                                                        assertEquals(expQuery.getConstraint().getFilter().getLogicOps().getValue(), resQuery.getConstraint().getFilter().getLogicOps().getValue());
                                                    }
                                                    if (expQuery.getConstraint().getFilter().getSpatialOps() != null) {
                                                        assertEquals(expQuery.getConstraint().getFilter().getSpatialOps().getValue(), resQuery.getConstraint().getFilter().getSpatialOps().getValue());
                                                    }
                                                }
                                                assertEquals(expQuery.getConstraint().getFilter(), resQuery.getConstraint().getFilter());
                                                assertEquals(expQuery.getConstraint().getVersion(), resQuery.getConstraint().getVersion());
                                            }
                                            assertEquals(expQuery.getConstraint(), resQuery.getConstraint());
                                            assertEquals(expQuery.getElementSetName(), resQuery.getElementSetName());
                                            assertEquals(expQuery.getSortBy(), resQuery.getSortBy());
                                            assertEquals(expQuery.getTypeNames(), resQuery.getTypeNames());

                                        }
                                        assertEquals(expGR.getAbstractQuery(), resGR.getAbstractQuery());
                                        assertEquals(expGR.getAny(), resGR.getAny());
                                        assertEquals(expGR.getDistributedSearch(), resGR.getDistributedSearch());
                                        assertEquals(expGR.getMaxRecords(), resGR.getMaxRecords());
                                        assertEquals(expGR.getOutputFormat(), resGR.getOutputFormat());
                                        assertEquals(expGR.getOutputSchema(), resGR.getOutputSchema());
                                        assertEquals(expGR.getRequestId(), resGR.getRequestId());
                                        assertEquals(expGR.getResponseHandler(), resGR.getResponseHandler());
                                        assertEquals(expGR.getResultType(), resGR.getResultType());
                                        assertEquals(expGR.getStartPosition(), resGR.getStartPosition());
                                        assertEquals(expGR, resGR);
                                    }
                                    assertEquals(expCont, resCont);
                                }
                            }
                            assertEquals(expAq.getQueryExpression(), resAq.getQueryExpression());
                            assertEquals(expAq.getQueryExpression().getQueryLanguage(), resAq.getQueryExpression().getQueryLanguage());
                        }
                    }
                }
                assertEquals(expIdent, resIdent);
            }
        }
        assertEquals(expResult.getRegistryObjectList(), result.getRegistryObjectList());
        assertEquals(expResult.getSlot().size(), result.getSlot().size());
        Iterator<org.geotoolkit.ebrim.xml.v300.SlotType> expSlotIt = expResult.getSlot().iterator();
        Iterator<org.geotoolkit.ebrim.xml.v300.SlotType> resSlotIt = result.getSlot().iterator();
        while (expSlotIt.hasNext()) {
            org.geotoolkit.ebrim.xml.v300.SlotType resSlot = resSlotIt.next();
            org.geotoolkit.ebrim.xml.v300.SlotType expSlot = expSlotIt.next();
            assertEquals(expSlot.getName(), resSlot.getName());
            assertEquals(expSlot.getSlotType(), resSlot.getSlotType());
            if (expSlot.getValueList() != null && resSlot.getValueList() != null) {
                assertEquals(expSlot.getValueList().getValue(), resSlot.getValueList().getValue());
            }
            assertEquals(expSlot.getValueList(), resSlot.getValueList());
            assertEquals(expSlot, resSlot);
        }
        assertEquals(expResult.getSlot(), result.getSlot());

        assertEquals(expResult.getStatus(), result.getStatus());
        assertEquals(expResult.getVersionInfo(), result.getVersionInfo());
        assertEquals(expResult, result);
    }
}
