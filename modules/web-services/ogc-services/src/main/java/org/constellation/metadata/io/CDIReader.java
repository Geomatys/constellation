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

// J2SE dependencies
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Contellation dependencies
import org.constellation.cat.csw.v202.AbstractRecordType;
import org.constellation.cat.csw.v202.BriefRecordType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.cat.csw.v202.SummaryRecordType;
import org.constellation.dublincore.v2.elements.SimpleLiteral;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.vocabulary.Vocabulary;
import org.constellation.ows.v100.BoundingBoxType;

// geotools dependencies
import org.geotools.metadata.iso.IdentifierImpl;
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.MetadataExtensionInformationImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.constraint.LegalConstraintsImpl;
import org.geotools.metadata.iso.distribution.DigitalTransferOptionsImpl;
import org.geotools.metadata.iso.distribution.DistributionImpl;
import org.geotools.metadata.iso.distribution.DistributorImpl;
import org.geotools.metadata.iso.distribution.FormatImpl;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.TemporalExtentImpl;
import org.geotools.metadata.iso.extent.VerticalExtentImpl;
import org.geotools.metadata.iso.identification.AggregateInformationImpl;
import org.geotools.metadata.iso.identification.DataIdentificationImpl;
import org.geotools.metadata.iso.identification.ResolutionImpl;
import org.geotools.metadata.iso.spatial.GeometricObjectsImpl;
import org.geotools.metadata.iso.spatial.VectorSpatialRepresentationImpl;
import org.geotools.referencing.AbstractReferenceSystem;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPeriod;
import org.geotools.temporal.object.DefaultPosition;
import org.geotools.util.SimpleInternationalString;

// GeoAPI dependencies
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.AssociationType;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.InitiativeType;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal
 */
public class CDIReader extends GenericMetadataReader {
    
    /**
     * 
     * @param genericConfiguration
     * @param connection
     * @throws java.sql.SQLException
     * @throws javax.xml.bind.JAXBException
     */
    public CDIReader(Automatic genericConfiguration, Connection connection) throws SQLException, JAXBException {
        super(genericConfiguration, connection);
    }
    
    /**
     * 
     * @param genericConfiguration
     * @param connection
     * @param fillAnchor
     * @throws java.sql.SQLException
     * @throws javax.xml.bind.JAXBException
     */
    public CDIReader(Automatic genericConfiguration, Connection connection, boolean fillAnchor) throws SQLException, JAXBException {
        super(genericConfiguration, connection, fillAnchor);
    }
    
    /**
     * Extract a metadata from a CDI database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    protected AbstractRecordType getDublinCore(String identifier, ElementSetType type, List<QName> elementName) {
        SimpleLiteral ident    = new SimpleLiteral(identifier);
        SimpleLiteral title    = new SimpleLiteral(getVariable("var04"));
        SimpleLiteral dataType = new SimpleLiteral("dataset");
        List<BoundingBoxType> bboxes = createBoundingBoxes("var24", "var25", "var26", "var27");
        if (type.equals(ElementSetType.BRIEF))
            return new BriefRecordType(ident, title, dataType, bboxes);
        
        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        //topic category
        subject.add(new SimpleLiteral("oceans"));
        
         //parameter
        for (String k : getKeywordsValue(getVariables("var10"), "P021"))
            subject.add(new SimpleLiteral(k));

        //instrument
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var11")), "L05"))
            subject.add(new SimpleLiteral(k));
        
        //platform
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var12")), "L061"))
            subject.add(new SimpleLiteral(k));
        
        //projects
        for (String k : getKeywordsValue(getVariables("var13"), "EDMERP"))
            subject.add(new SimpleLiteral(k));
        
        List<SimpleLiteral> formats = new ArrayList<SimpleLiteral>();
        for (String s : getVariables("var37")) {
            formats.add(new SimpleLiteral(s));
        }
        SimpleLiteral modified = new SimpleLiteral(dateFormats.get(0).format(new Date()));
        
        List<SimpleLiteral> _abstract = Arrays.asList(new SimpleLiteral(getVariable("var08")));
        
        if (type.equals(ElementSetType.SUMMARY))
            return new SummaryRecordType(ident, title, dataType, bboxes, subject, formats, modified, _abstract);
        
        List<SimpleLiteral> creators = new ArrayList<SimpleLiteral>();
        for (String contactID : getVariables("var07")) {
            ResponsibleParty originator  = getContact(contactID, Role.ORIGINATOR);
            if (originator != null) {
                InternationalString s = originator.getOrganisationName();
                if (s != null)
                    creators.add(new SimpleLiteral(s.toString()));
            }
        }
        
        SimpleLiteral distributor = null;
        ResponsibleParty distrib  = getContact(getVariable("var36"), Role.DISTRIBUTOR);
        if (distrib != null) {
            InternationalString s = distrib.getOrganisationName();
            if (s != null) {
                distributor = new SimpleLiteral(s.toString());
            }
        }
       
        SimpleLiteral language = new SimpleLiteral("en");
        
        
        return new RecordType(ident, title, dataType, subject, formats, modified, modified, _abstract, bboxes, creators, distributor, language, null, null);
        
    }
    
    /**
     * Extract a metadata from a CDI database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    protected MetaDataImpl getISO(String identifier) {
        
        MetaDataImpl result     = new MetaDataImpl();
        
        /*
         * static part
         */
        result.setFileIdentifier(identifier);
        result.setLanguage(Locale.ENGLISH);
        result.setCharacterSet(CharacterSet.UTF_8);
        result.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        result.setHierarchyLevelNames(Arrays.asList("Common Data Index record"));
        /*
         * contact parts
         */
        ResponsibleParty author = getContact(getVariable("var01"), Role.AUTHOR);
        result.setContacts(Arrays.asList(author));
        
        /*
         * creation date 
         */ 
        result.setDateStamp(new Date());
        
        /*
         * Spatial representation info
         */  
        VectorSpatialRepresentationImpl spatialRep = new VectorSpatialRepresentationImpl();
        GeometricObjectsImpl geoObj = new GeometricObjectsImpl(GeometricObjectType.valueOf(getVariable("var02")));
        spatialRep.setGeometricObjects(Arrays.asList(geoObj));
        
        result.setSpatialRepresentationInfo(Arrays.asList(spatialRep));
        
        /*
         * Reference system info 
         */ 
        String code = getVariable("var03");
        if (code != null) {
            Vocabulary Rvoca = vocabularies.get("L101");
            CitationImpl RScitation = new CitationImpl();
            Map<String, Object> properties = new HashMap<String, Object>();
            if (Rvoca != null) {
                RScitation.setTitle(new SimpleInternationalString(Rvoca.getTitle()));
                RScitation.setAlternateTitles(Arrays.asList(new SimpleInternationalString("L101")));
                RScitation.setIdentifiers(Arrays.asList(new IdentifierImpl("http://www.seadatanet.org/urnurl/")));
                RScitation.setEdition(new SimpleInternationalString(Rvoca.getVersion()));
                String mappedCode = Rvoca.getMap().get(code);
                if (mappedCode != null)
                    code = mappedCode;
            } else {
                logger.severe("voca L101 not found");
            }
            NamedIdentifier Nidentifier = new NamedIdentifier(RScitation, code);
            properties.put("identifiers", Nidentifier);
            properties.put("name", Nidentifier);

            AbstractReferenceSystem rs = new AbstractReferenceSystem(properties);
            result.setReferenceSystemInfo(Arrays.asList(rs));
        } 
        
        /*
         * extension information
         */
        MetadataExtensionInformationImpl extensionInfo = new MetadataExtensionInformationImpl();
        List<ExtendedElementInformation> elements = new ArrayList<ExtendedElementInformation>();
        
        //EDMO
        ExtendedElementInformation edmo =  createExtensionInfo("SDN:EDMO::");
        elements.add(edmo);
        
        //L021
        ExtendedElementInformation L021 = createExtensionInfo("SDN:L021:1:");
        elements.add(L021);
        
        //L031
        ExtendedElementInformation L031 = createExtensionInfo("SDN:L031:2:");
        elements.add(L031);
        
        //L071
        ExtendedElementInformation L071 = createExtensionInfo("SDN:L071:1:");
        elements.add(L071);
        
        //L081
        ExtendedElementInformation L081 = createExtensionInfo("SDN:L081:1:");
        elements.add(L081);
        
        //L231
        ExtendedElementInformation L231 = createExtensionInfo("SDN:L231:3:");
        elements.add(L231);
        
        //L241
        ExtendedElementInformation L241 = createExtensionInfo("SDN:L241:1:");
        elements.add(L241);
        
        extensionInfo.setExtendedElementInformation(elements);
        
        result.setMetadataExtensionInfo(Arrays.asList(extensionInfo));
        
        /*
         * Data indentification
         */ 
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();
        
        CitationImpl citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var04"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var05")));
        
        CitationDate revisionDate = createRevisionDate(getVariable("var06"));
        citation.setDates(Arrays.asList(revisionDate));
        
        List<ResponsibleParty> originators = new ArrayList<ResponsibleParty>();
        for (String contactID : getVariables("var07")) {
            ResponsibleParty originator  = getContact(contactID, Role.ORIGINATOR);
            if (originator != null) {
                originators.add(originator);
            }
        }
        citation.setCitedResponsibleParties(originators);
        
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(getInternationalStringVariable("var08"));
        
        ResponsibleParty custodian   = getContact(getVariable("var09"), Role.CUSTODIAN);
        dataIdentification.setPointOfContacts(Arrays.asList(custodian));

        /*
         * keywords
         */  
        List<Keywords> keywords = new ArrayList<Keywords>();
        
        //parameter
        Keywords keyword = createKeyword(getVariables("var10"), "parameter", "P021");
        keywords.add(keyword);

        keyword = createKeyword(Arrays.asList(getVariable("var11")), "instrument", "L05");
        keywords.add(keyword);
        
        //platform
        keyword = createKeyword(Arrays.asList(getVariable("var12")), "platform_class", "L061");
        keywords.add(keyword);
        
        //projects
        keyword = createKeyword(getVariables("var13"), "project", "EDMERP");
        keywords.add(keyword);
        
        dataIdentification.setDescriptiveKeywords(keywords);

        /*
         * resource constraint
         */  
        List<String> resConsts = getVariables("var14");
        LegalConstraintsImpl constraint = new LegalConstraintsImpl();
        Vocabulary voca = vocabularies.get("L081");
        for (String resConst : resConsts) {
            if (voca != null) {
                String mappedValue = voca.getMap().get(resConst);
                if (mappedValue != null)
                    resConst = mappedValue;
            }
                
            constraint.setAccessConstraints(Arrays.asList(Restriction.valueOf(resConst)));
        }
        dataIdentification.setResourceConstraints(Arrays.asList(constraint));
        
        /*
         * Aggregate info
         */
        List<AggregateInformationImpl> aggregateInfos = new ArrayList<AggregateInformationImpl>();
        
        //cruise
        AggregateInformationImpl aggregateInfo = new AggregateInformationImpl();
        citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var15"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var16")));
        revisionDate = createRevisionDate(getVariable("var17"));
        citation.setDates(Arrays.asList(revisionDate));
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);
        
        //station
        aggregateInfo = new AggregateInformationImpl();
        citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var18"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var19")));
        revisionDate = createRevisionDate(getVariable("var20"));
        citation.setDates(Arrays.asList(revisionDate));
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);
        
        dataIdentification.setAggregationInfo(aggregateInfos);
        
        /*
         * data scale TODO
         */
        String scale = getVariable("var21");
        if (scale != null) {
            try {
                ResolutionImpl resolution = new ResolutionImpl();
                resolution.setDistance(Double.parseDouble(scale));
                dataIdentification.setSpatialResolutions(Arrays.asList(resolution));
            }  catch (NumberFormatException ex) {
                logger.severe("parse exception while parsing scale");
            }
        }
        
        //static part        
        dataIdentification.setLanguage(Arrays.asList(Locale.ENGLISH));
        dataIdentification.setTopicCategories(Arrays.asList(TopicCategory.OCEANS));
        
        /*
         * Extent 
         */
        ExtentImpl extent = new ExtentImpl();
        
        // geographic extent
        extent.setGeographicElements(createGeographicExtent("var24", "var25", "var26", "var27"));
        
        //temporal extent
        TemporalExtentImpl tempExtent = new TemporalExtentImpl();
        Date start = parseDate(getVariable("var28"));
        Date stop  = parseDate(getVariable("var29"));
        
        if (start != null && stop != null) {
            DefaultInstant begin = new DefaultInstant(new DefaultPosition(start));
            DefaultInstant end   = new DefaultInstant(new DefaultPosition(stop));
            DefaultPeriod period = new DefaultPeriod(begin, end);
            tempExtent.setExtent(period);
            extent.setTemporalElements(Arrays.asList(tempExtent));
        } else {
            logger.severe("parse exception while parsing temporal extent date");
        }
        
        //vertical extent
        VerticalExtentImpl vertExtent = new VerticalExtentImpl();
        try{
            String miv = getVariable("var30");
            if (miv != null)
                vertExtent.setMinimumValue(Double.parseDouble(miv));
            String mav = getVariable("var31");
            if (mav != null)
                vertExtent.setMaximumValue(Double.parseDouble(mav));
        } catch (NumberFormatException ex) {
            logger.severe("Number format exception while parsing vertical extent min-max");
        }
        // TODO DefaultVerticalCRS verticalCRS = new DefaultVerticalCRS(key, arg1, arg2)
        extent.setVerticalElements(Arrays.asList(vertExtent));
        
        dataIdentification.setExtent(Arrays.asList(extent));
        
        result.setIdentificationInfo(Arrays.asList(dataIdentification));
        
        /*
         * Distribution info
         */ 
        DistributionImpl distributionInfo = new DistributionImpl();
        
        //distributor
        DistributorImpl distributor       = new DistributorImpl();
        
        ResponsibleParty distributorContact   = getContact(getVariable("var36"), Role.DISTRIBUTOR);
        distributor.setDistributorContact(distributorContact);
                
        distributionInfo.setDistributors(Arrays.asList(distributor));
        
        //format
        List<Format> formats  = new ArrayList<Format>();
        List<String> names    = getVariables("var37");
        List<String> versions = getVariables("var38");
        if (names == null || versions == null) {
            logger.severe("Distribution formats elements are null.");
        } else {
            int i = 0;
            voca = vocabularies.get("L241");
            while (i < names.size() && i < versions.size()) {
                FormatImpl format = new FormatImpl();
                String name = names.get(i);
                if (voca != null) {
                    String mappedValue = voca.getMap().get(name);
                    if (mappedValue != null)
                        name = mappedValue;
                }
                format.setName(new SimpleInternationalString(name));
                format.setVersion(new SimpleInternationalString(versions.get(i)));
                formats.add(format);
                i++;
            }
        }
        distributionInfo.setDistributionFormats(formats);
        
        //transfert options
        DigitalTransferOptionsImpl digiTrans = new DigitalTransferOptionsImpl();
        try {
            String size = getVariable("var39");
            if (size != null)
                digiTrans.setTransferSize(Double.parseDouble(size));
            else
                logger.severe("Transfer size is null");
        } catch (NumberFormatException ex) {
            logger.severe("Number format exception while parsing transfer size");
        }
        OnLineResourceImpl onlines = new OnLineResourceImpl();
        try {
            onlines.setLinkage(new URI(getVariable("var40")));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in contact online resource");
        }
        onlines.setDescription(getInternationalStringVariable("var41"));
        onlines.setFunction(OnLineFunction.DOWNLOAD);
        digiTrans.setOnLines(Arrays.asList(onlines));
        
        distributionInfo.setTransferOptions(Arrays.asList(digiTrans));
        
        result.setDistributionInfo(distributionInfo);
        
        return result;
    }

}
