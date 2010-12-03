/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.metadata;

import org.geotoolkit.sml.xml.v100.ComponentType;
import org.geotoolkit.swe.xml.v100.DataRecordType;
import org.geotoolkit.sml.xml.v100.IoComponentPropertyType;
import org.geotoolkit.sml.xml.v100.SystemType;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.opengis.metadata.content.ContentInformation;
import org.geotoolkit.feature.catalog.PropertyTypeImpl;
import org.geotoolkit.feature.catalog.FeatureTypeImpl;
import org.opengis.metadata.citation.Citation;
import org.geotoolkit.util.DefaultInternationalString;
import java.util.Iterator;
import org.geotoolkit.feature.catalog.FeatureAttributeImpl;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.content.CoverageDescription;
import org.opengis.metadata.content.FeatureCatalogueDescription;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.spatial.Georectified;
import org.opengis.metadata.spatial.SpatialRepresentation;

import static org.junit.Assert.*;

/**
 *
 * @author guilhem
 */
public class CSWTestUtils {

    public static void metadataEquals(DefaultMetadata expResult, DefaultMetadata result) {

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
            }
        }
        assertEquals(expResult.getContentInfo(), result.getContentInfo());
        assertEquals(expResult.getDataQualityInfo().size(), result.getDataQualityInfo().size());

        Iterator<DataQuality> expDqIt = expResult.getDataQualityInfo().iterator();
        Iterator<DataQuality> resDqIt = result.getDataQualityInfo().iterator();
        while (expDqIt.hasNext()) {
            DataQuality expDq = expDqIt.next();
            DataQuality resDq = resDqIt.next();
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
                DefaultDataIdentification idExpResult = (DefaultDataIdentification) expResult.getIdentificationInfo().iterator().next();
                DefaultDataIdentification idResult    = (DefaultDataIdentification) result.getIdentificationInfo().iterator().next();
                assertEquals(idExpResult.getCharacterSets(), idResult.getCharacterSets());
                assertEquals(idExpResult.getAbstract(), idResult.getAbstract());
                assertEquals(idExpResult.getCitation(), idResult.getCitation());
                assertEquals(idExpResult.getAggregationInfo(), idResult.getAggregationInfo());
                assertEquals(idExpResult.getCredits(), idResult.getCredits());
                if (idResult.getDescriptiveKeywords().iterator().hasNext()) {
                    assertEquals(idExpResult.getDescriptiveKeywords().iterator().next().getKeywords(), idResult.getDescriptiveKeywords().iterator().next().getKeywords());
                    if (idResult.getDescriptiveKeywords().iterator().next().getThesaurusName() != null) {
                        if (idResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().hasNext()) {
                            assertEquals(idExpResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next().getClass(), idResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next().getClass());
                            assertEquals(idExpResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next().getCode(), idResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next().getCode());
                            assertEquals(idExpResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next(), idResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers().iterator().next());
                        }
                        assertEquals(idExpResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers(), idResult.getDescriptiveKeywords().iterator().next().getThesaurusName().getIdentifiers());
                        citationEquals(idExpResult.getDescriptiveKeywords().iterator().next().getThesaurusName(), idResult.getDescriptiveKeywords().iterator().next().getThesaurusName());
                    }
                    assertEquals(idExpResult.getDescriptiveKeywords().iterator().next().getThesaurusName(), idResult.getDescriptiveKeywords().iterator().next().getThesaurusName());
                    assertEquals(idExpResult.getDescriptiveKeywords().iterator().next().getType(), idResult.getDescriptiveKeywords().iterator().next().getType());
                    assertEquals(idExpResult.getDescriptiveKeywords().iterator().next(), idResult.getDescriptiveKeywords().iterator().next());
                }
                assertEquals(idExpResult.getDescriptiveKeywords(), idResult.getDescriptiveKeywords());
                assertEquals(idExpResult.getEnvironmentDescription(), idResult.getEnvironmentDescription());
                assertEquals(idExpResult.getExtents().size(), idResult.getExtents().size());

                Iterator<Extent> expIt = idExpResult.getExtents().iterator();
                Iterator<Extent> resIt = idResult.getExtents().iterator();

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
                                assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getClass(), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getClass());
                                assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getName(), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0).getName());
                                assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getAxis(0), resVEx.getVerticalCRS().getCoordinateSystem().getAxis(0));
                                assertEquals(expVEx.getVerticalCRS().getCoordinateSystem().getName(), resVEx.getVerticalCRS().getCoordinateSystem().getName());
                                assertEquals(expVEx.getVerticalCRS().getCoordinateSystem(), resVEx.getVerticalCRS().getCoordinateSystem());
                                assertEquals(expVEx.getVerticalCRS().getDatum(), resVEx.getVerticalCRS().getDatum());
                                assertEquals(expVEx.getVerticalCRS(), resVEx.getVerticalCRS());
                            }
                        }
                        assertEquals(expVEx, resVEx);
                    }
                    assertEquals(expEx.getTemporalElements(),   resEx.getTemporalElements());
                }

                assertEquals(idExpResult.getExtents(), idResult.getExtents());
                assertEquals(idExpResult.getGraphicOverviews(), idResult.getGraphicOverviews());
                assertEquals(idExpResult.getInterface(), idResult.getInterface());
                assertEquals(idExpResult.getLanguages(), idResult.getLanguages());
                assertEquals(idExpResult.getPointOfContacts(), idResult.getPointOfContacts());
                assertEquals(idExpResult.getPurpose(), idResult.getPurpose());
                assertEquals(idExpResult.getResourceConstraints().size(), idResult.getResourceConstraints().size());
                if (idExpResult.getResourceConstraints().size() > 0) {
                    Constraints expConst = idExpResult.getResourceConstraints().iterator().next();
                    Constraints resConst = idResult.getResourceConstraints().iterator().next();
                    assertEquals(expConst.getUseLimitations(), resConst.getUseLimitations());
                    assertEquals(expConst, resConst);
                }
                assertEquals(idExpResult.getResourceConstraints(), idResult.getResourceConstraints());
                assertEquals(idExpResult.getResourceFormats(), idResult.getResourceFormats());
                assertEquals(idExpResult.getResourceMaintenances(), idResult.getResourceMaintenances());
                assertEquals(idExpResult.getResourceSpecificUsages(), idResult.getResourceSpecificUsages());
                assertEquals(idExpResult.getSpatialRepresentationTypes(), idResult.getSpatialRepresentationTypes());
                assertEquals(idExpResult.getStandard(), idResult.getStandard());
                assertEquals(idExpResult.getStatus(), idResult.getStatus());
                assertEquals(idExpResult.getSupplementalInformation(), idResult.getSupplementalInformation());
                assertEquals(idExpResult.getTopicCategories(), idResult.getTopicCategories());
                assertEquals(idExpResult, idResult);
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
                if (expResult.getReferenceSystemInfo().iterator().next().getName() != null) {
                    assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName().getAuthority(), result.getReferenceSystemInfo().iterator().next().getName().getAuthority());
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
        assertEquals(expResult.getSpatialRepresentationInfo(), result.getSpatialRepresentationInfo());
        assertEquals(expResult, result);
    }

    public static void citationEquals(Citation expectedCitation, Citation resultCitation) {
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

    public static void catalogueEquals(FeatureCatalogueImpl expResult, FeatureCatalogueImpl result) {

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


    public static void SystemSMLEquals(SensorML expResult, SensorML result) {
        
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

    public static void ComponentEquals(SensorML expResult, SensorML result) {
        
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

        assertEquals(expProcess.getBoundedBy().getEnvelope().getLowerCorner(), resProcess.getBoundedBy().getEnvelope().getLowerCorner());
        assertEquals(expProcess.getBoundedBy().getEnvelope(), resProcess.getBoundedBy().getEnvelope());
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
        assertEquals(expProcess.getValidTime(), resProcess.getValidTime());

        assertEquals(expResult.getMember().iterator().next().getArcrole(), result.getMember().iterator().next().getArcrole());
        assertEquals(expResult.getMember().iterator().next(), result.getMember().iterator().next());
        assertEquals(expResult.getMember(), result.getMember());

        assertEquals(expResult, result);
    }
}
