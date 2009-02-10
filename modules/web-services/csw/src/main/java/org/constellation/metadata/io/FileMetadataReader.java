/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.metadata.io;

import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.cat.csw.DomainValues;
import org.constellation.cat.csw.ElementSet;
import org.constellation.cat.csw.v202.AbstractRecordType;
import org.constellation.cat.csw.v202.BriefRecordType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.cat.csw.v202.SummaryRecordType;
import org.constellation.dublincore.v2.elements.SimpleLiteral;
import org.constellation.generic.database.Automatic;
import org.constellation.ows.v100.BoundingBoxType;
import org.constellation.ws.CstlServiceException;
import org.geotools.metadata.iso.MetaDataImpl;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.InternationalString;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal 
 */
public class FileMetadataReader extends MetadataReader {

    private File dataDirectory;
    
    /**
     * A unMarshaller to get object from metadata files.
     */
    private final Unmarshaller unmarshaller;

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public FileMetadataReader(Automatic configuration, Unmarshaller unmarshaller) throws CstlServiceException {
        super(true, false);
        this.unmarshaller  = unmarshaller;
        dataDirectory = configuration.getdataDirectory();
        if (dataDirectory == null || !dataDirectory.exists()) {
            throw new CstlServiceException("cause: The unable to find the data directory", NO_APPLICABLE_CODE);
        } 
    }
    
    /**
     * Return a metadata object from the specified identifier.
     * 
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * 
     * @return A marshallable metadata object.
     * @throws java.sql.SQLException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSet type, List<QName> elementName) throws CstlServiceException {
        Object obj = getObjectFromFile(identifier);
        if (obj instanceof MetaDataImpl && mode == DUBLINCORE) {
            obj = translateISOtoDC((MetaDataImpl)obj, type);
        }
        return obj;
    }

    private Object getObjectFromFile(String identifier) throws CstlServiceException {
        File metadataFile = new File (dataDirectory,  identifier + ".xml");
        if (metadataFile.exists()) {
            try {
                Object metadata = unmarshaller.unmarshal(metadataFile);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                addInCache(identifier, metadata);
                return metadata;
            } catch (JAXBException ex) {
                throw new CstlServiceException("The metadataFile : " + identifier + ".xml can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            }
        } else {
            throw new CstlServiceException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    private AbstractRecordType translateISOtoDC(MetaDataImpl metadata, ElementSet type) {
        if (metadata != null) {

            /*
             * BRIEF part
             */
            SimpleLiteral identifier = new SimpleLiteral(metadata.getFileIdentifier());

            SimpleLiteral title = null;
            //TODO see for multiple identification
            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification.getCitation() != null && identification.getCitation().getTitle() != null) {
                    title = new SimpleLiteral(identification.getCitation().getTitle().toString());
                }
            }

            SimpleLiteral dataType = null;
            //TODO see for multiple hierarchyLevel
            for (ScopeCode code: metadata.getHierarchyLevels()) {
                dataType = new SimpleLiteral(code.identifier());
            }

            List<BoundingBoxType> bboxes = new ArrayList<BoundingBoxType>();

            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification instanceof DataIdentification) {
                    DataIdentification dataIdentification = (DataIdentification) identification;
                    for (Extent extent : dataIdentification.getExtent()) {
                        for (GeographicExtent geoExtent :extent.getGeographicElements()) {
                            if (geoExtent instanceof GeographicBoundingBox) {
                                GeographicBoundingBox bbox = (GeographicBoundingBox) geoExtent;
                                // TODO find CRS
                                bboxes.add(new BoundingBoxType("EPSG:4326",
                                                                bbox.getWestBoundLongitude(),
                                                                bbox.getSouthBoundLatitude(),
                                                                bbox.getEastBoundLongitude(),
                                                                bbox.getNorthBoundLatitude()));
                            }
                        }
                    }
                }
            }

            if (type == ElementSetType.BRIEF)
                return new BriefRecordType(identifier, title, dataType, bboxes);

            /*
             *  SUMMARY part
             */
            List<SimpleLiteral> _abstract = new ArrayList<SimpleLiteral>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification.getAbstract() != null) {
                    _abstract.add(new SimpleLiteral(identification.getAbstract().toString()));
                }
            }

            List<SimpleLiteral> subjects = new ArrayList<SimpleLiteral>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                if (identification instanceof DataIdentification) {
                    DataIdentification dataIdentification = (DataIdentification) identification;
                    for (TopicCategory tc : dataIdentification.getTopicCategories()) {
                        subjects.add(new SimpleLiteral(tc.identifier()));
                    }
                }
                for (Keywords kw :identification.getDescriptiveKeywords()) {
                    for (InternationalString str : kw.getKeywords()) {
                        subjects.add(new SimpleLiteral(str.toString()));
                    }
                }
            }
            List<SimpleLiteral> formats = new ArrayList<SimpleLiteral>();
            Distribution distribution   = metadata.getDistributionInfo();
            if (distribution != null) {
                for (Format f: distribution.getDistributionFormats()) {
                    formats.add(new SimpleLiteral(f.getName().toString()));
                }
            }

            SimpleLiteral modified = new SimpleLiteral(formatter.format(metadata.getDateStamp()));

            if (type == ElementSetType.SUMMARY)
                return new SummaryRecordType(identifier, title, dataType, bboxes, subjects, formats, modified, _abstract);

            SimpleLiteral date    = modified;
            List<SimpleLiteral> creator = new ArrayList<SimpleLiteral>();
            for (Identification identification: metadata.getIdentificationInfo()) {
                for (ResponsibleParty rp :identification.getPointOfContacts()) {
                    if (Role.ORIGINATOR.equals(rp.getRole())) {
                        creator.add(new SimpleLiteral(rp.getOrganisationName().toString()));
                    }

                }
            }
            if (creator.size() == 0) creator = null;

            // TODO multiple
            SimpleLiteral distributor = null;
            for (Identification identification: metadata.getIdentificationInfo()) {
                for (ResponsibleParty rp :identification.getPointOfContacts()) {
                    if (Role.PUBLISHER.equals(rp.getRole())) {
                        distributor = new SimpleLiteral(rp.getOrganisationName().toString());
                    }

                }
            }

            SimpleLiteral language = new SimpleLiteral(metadata.getLanguage().getLanguage());

            // TODO
            SimpleLiteral spatial = null;
            SimpleLiteral references = null;
            return new RecordType(identifier, title, dataType, subjects, formats, modified, date, _abstract, bboxes, creator, distributor, language, spatial, references);
        }
        return null;
    }

    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws CstlServiceException {
        throw new CstlServiceException("GetDomain operation are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public List<String> executeEbrimSQLQuery(String SQLQuery) throws CstlServiceException {
        throw new CstlServiceException("Ebrim query are not supported int the FILESYSTEM mode.", OPERATION_NOT_SUPPORTED);
    }

    @Override
    public List<? extends Object> getAllEntries() throws CstlServiceException {
        List<Object> results = new ArrayList<Object>();
        for (File f : dataDirectory.listFiles()) {
            if (f.getName().endsWith(".xml")) {
                String identifier = f.getName().substring(0, f.getName().indexOf(".xml"));
                try {
                    Object metadata = unmarshaller.unmarshal(f);
                    if (metadata instanceof JAXBElement) {
                        metadata = ((JAXBElement) metadata).getValue();
                    }
                    addInCache(identifier, metadata);
                    results.add(metadata);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("The metadataFile : " + f.getPath() + " can not be unmarshalled" + "\n" +
                            "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
                }
            } else {
                throw new CstlServiceException("The metadataFile : " + f.getPath() + " is not present", INVALID_PARAMETER_VALUE);
            }
        }
        return results;
    }

    @Override
    public List<Integer> getSupportedDataTypes() {
        return Arrays.asList(ISO_19115, DUBLINCORE);
    }

    /**
     * Return the list of Additional queryable element (0 in MDWeb).
     */
    public List<QName> getAdditionalQueryableQName() {
        return new ArrayList<QName>();
    }

    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return new HashMap<String, List<String>>();
    }
}

