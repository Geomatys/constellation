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
import java.util.List;
import java.util.Locale;

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
import org.geotools.metadata.iso.constraint.LegalConstraintsImpl;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicDescriptionImpl;
import org.geotools.metadata.iso.extent.TemporalExtentImpl;
import org.geotools.metadata.iso.identification.AggregateInformationImpl;
import org.geotools.metadata.iso.identification.BrowseGraphicImpl;
import org.geotools.metadata.iso.identification.DataIdentificationImpl;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPeriod;
import org.geotools.temporal.object.DefaultPosition;

// GeoAPI dependencies
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.AssociationType;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.InitiativeType;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal
 */
public class CSRReader extends SDNMetadataReader {
    
    /**
     * Build a new reader for the CSR database profile.
     * 
     * @param configuration An Automatic configuration object containing all the SQL request.
     * @param connection A connection to the database.
     * 
     * @throws java.sql.SQLException 
     * @throws javax.xml.bind.JAXBException
     */
    public CSRReader(Automatic configuration, Connection connection) throws SQLException, JAXBException {
        super(configuration, connection);
    }
    
    /**
     * Build a new reader for the CSR database profile.
     * 
     * @param configuration An Automatic configuration object containing all the SQL request.
     * @param connection A connection to the database.
     * @param fillAnchor A flag indicating if we have to fill the anchors with vocabulary urns.
     * 
     * @throws java.sql.SQLException
     * @throws javax.xml.bind.JAXBException
     */
    public CSRReader(Automatic configuration, Connection connection, boolean fillAnchor) throws SQLException, JAXBException {
        super(configuration, connection, fillAnchor);
    }
    
    /**
     * Extract a metadata from a CSR database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    protected AbstractRecordType getDublinCore(String identifier, ElementSetType type, List<QName> elementName) {
        
        SimpleLiteral ident    = new SimpleLiteral(identifier);
        SimpleLiteral title    = new SimpleLiteral(getVariable("var02"));
        SimpleLiteral dataType = new SimpleLiteral("series");
        List<BoundingBoxType> bboxes = createBoundingBoxes("var27", "var28", "var29", "var30");
        if (type.equals(ElementSetType.BRIEF))
            return new BriefRecordType(ident, title, dataType, bboxes);
        
        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        //topic category
        subject.add(new SimpleLiteral("oceans"));
        
        //port of departure
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var12")), "C381"))
            subject.add(new SimpleLiteral(k));

        //port of arrival
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var13")), "C381"))
            subject.add(new SimpleLiteral(k));
        
        //country of departure
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var14")), "C320"))
            subject.add(new SimpleLiteral(k));
        
        // country of arrival
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var15")), "C320"))
            subject.add(new SimpleLiteral(k));
        
        // ship
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var16")), "C174"))
            subject.add(new SimpleLiteral(k));
       
        // platform class
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var17")), "L061"))
            subject.add(new SimpleLiteral(k));
               
        // projects
        for (String k : getKeywordsValue(getVariables("var18"), "EDMERP"))
            subject.add(new SimpleLiteral(k));
        
        // general oceans area
        for (String k : getKeywordsValue(getVariables("var19"), "C16"))
            subject.add(new SimpleLiteral(k));
        
        // geographic coverage
        for (String k : getKeywordsValue(getVariables("var20"), "C371"))
            subject.add(new SimpleLiteral(k));
        
         //parameter
        for (String k : getKeywordsValue(getVariables("var21"), "P021"))
            subject.add(new SimpleLiteral(k));
        
        // instrument
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var22")), "L05"))
            subject.add(new SimpleLiteral(k));
        
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var42")), "L181"))
            subject.add(new SimpleLiteral(k));
        
        // No formats in CSR
        List<SimpleLiteral> formats = new ArrayList<SimpleLiteral>();
        
        SimpleLiteral modified = new SimpleLiteral(dateFormats.get(0).format(new Date()));
        
        List<SimpleLiteral> _abstract = new ArrayList<SimpleLiteral>();
        _abstract.add(new SimpleLiteral(getVariable("var35")));
        _abstract.add(new SimpleLiteral(getVariable("var41")));
        
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
        
        //no distribution info in CSR
        SimpleLiteral distributor = null;
       
        SimpleLiteral language = new SimpleLiteral("en");
        
        return new RecordType(ident, title, dataType, subject, formats, modified, modified, _abstract, bboxes, creators, distributor, language, null, null);
        
    }
    
    @Override
    protected List<String> getVariablesForDublinCore(ElementSetType type) {
        if (type == ElementSetType.BRIEF) {
            return Arrays.asList("var02", "var27", "var28", "var29", "var30");
        } else if (type == ElementSetType.SUMMARY) {
            return Arrays.asList("var02", "var27", "var28", "var29", "var30", "var12", "var13",  "var14", 
                    "var15", "var16", "var17", "var18", "var19", "var20", "var21", "var22", "var42", "var35", "var41");
        } else if (type == ElementSetType.FULL) {
            return Arrays.asList("var02", "var27", "var28", "var29", "var30", "var12", "var13",  "var14", 
                    "var15", "var16", "var17", "var18", "var19", "var20", "var21", "var22", "var42", "var35", "var41", "var07");
        } else throw new IllegalArgumentException("unknow ElementSet: " + type);
    }
    
    /**
     * Return the list of contact ID used in this database.
     * 
     * @return
     */
    @Override
    public List<String> getVariablesForContact() {
        return Arrays.asList("var01", "var05", "var06", "var07", "var34", "var40");
    }
    
    /**
     * Extract a metadata from a CSR database.
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
        result.setHierarchyLevels(Arrays.asList(ScopeCode.SERIES));
        result.setHierarchyLevelNames(Arrays.asList("Cruise Summary record"));
                
        /*
         * contact parts
         */
        ResponsibleParty contact = getContact(getVariable("var01"), Role.AUTHOR);
        result.setContacts(Arrays.asList(contact));
        
        /*
         * creation date
         */ 
        result.setDateStamp(new Date());
        
        /*
         * extension information
         */
        MetadataExtensionInformationImpl extensionInfo = new MetadataExtensionInformationImpl();
        List<ExtendedElementInformation> elements = new ArrayList<ExtendedElementInformation>();
        
        
        //EDMO
        ExtendedElementInformation edmo =  createExtensionInfo("SDN:EDMO::");
        elements.add(edmo);
        
        //L081
        ExtendedElementInformation L081 = createExtensionInfo("SDN:L081:1:");
        elements.add(L081);
        
        //L231
        ExtendedElementInformation L231 = createExtensionInfo("SDN:L231:3:");
        elements.add(L231);
        
        //C77
        ExtendedElementInformation C77 = createExtensionInfo("SDN:C77:0:");
        elements.add(C77);
        
        extensionInfo.setExtendedElementInformation(elements);
        result.setMetadataExtensionInfo(Arrays.asList(extensionInfo));
        
        
        List<DataIdentificationImpl> dataIdentifications = new ArrayList<DataIdentificationImpl>();
        
        /*
         * Data indentification 1
         */ 
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();
        
        CitationImpl citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var02"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var03")));
        
        CitationDate revisionDate = createRevisionDate(getVariable("var04"));
        citation.setDates(Arrays.asList(revisionDate));
        List<ResponsibleParty> chiefs = new ArrayList<ResponsibleParty>();
        
        //first chief
        contact   = getContact(getVariable("var05"), Role.POINT_OF_CONTACT);
        chiefs.add(contact);
        
        //second and other chief
        List<String> secondChiefs = getVariables("var06");
        for (String secondChief : secondChiefs) {
            contact   = getContact(secondChief, Role.POINT_OF_CONTACT);
            chiefs.add(contact);
        }
        
        //labo
        List<String> laboratories = getVariables("var07");
        for (String laboratory : laboratories) {
            contact   = getContact(laboratory, Role.ORIGINATOR);
            chiefs.add(contact);
        }
        
        citation.setCitedResponsibleParties(chiefs);
        dataIdentification.setCitation(citation);
        
        dataIdentification.setPurpose(getInternationalStringVariable("var08"));
        
        BrowseGraphicImpl graphOverview = new BrowseGraphicImpl();
        try {
            graphOverview.setFileName(new URI(getVariable("var09")));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in graph overview");
        }
        
        graphOverview.setFileDescription(getInternationalStringVariable("var10"));
        graphOverview.setFileType(getVariable("var11"));
        
        dataIdentification.setGraphicOverviews(Arrays.asList(graphOverview));
        
        /*
         * keywords
         */  
        List<Keywords> keywords = new ArrayList<Keywords>();
        
        //port of departure
        Keywords keyword = createKeyword(Arrays.asList(getVariable("var12")), "departure_place", "C381");
        keywords.add(keyword);

        //port of arrival
        keyword = createKeyword(Arrays.asList(getVariable("var13")), "arrival_place", "C381");
        keywords.add(keyword);
        
        //country of departure
        keyword = createKeyword(Arrays.asList(getVariable("var14")), "departure_contry", "C320");
        keywords.add(keyword);
        
        // country of arrival
        keyword =  createKeyword(Arrays.asList(getVariable("var15")), "arrival_country", "C320");
        keywords.add(keyword);
        
        // ship
        keyword = createKeyword(Arrays.asList(getVariable("var16")), "platform", "C174");
        keywords.add(keyword);
        
        // platform class
        keyword = createKeyword(Arrays.asList(getVariable("var17")), "platform_class", "L061");
        keywords.add(keyword);
        
        // projects
        keyword = createKeyword(getVariables("var18"), "platform_class", "EDMERP");
        keywords.add(keyword);
        
        // general oceans area
        keyword = createKeyword(getVariables("var19"), "place", "C16");
        keywords.add(keyword);
        
        // geographic coverage
        keyword = createKeyword(getVariables("var20"), "marsden_square", "C371");
        keywords.add(keyword);
        
         //parameter
        keyword = createKeyword(getVariables("var21"), "parameter", "P021");
        keywords.add(keyword);
        
        // instrument
        keyword = createKeyword(Arrays.asList(getVariable("var22")), "instrument", "L05");
        keywords.add(keyword);

        dataIdentification.setDescriptiveKeywords(keywords);
        
        /*
         * resource constraint
         */  
        List<String> resConsts = getVariables("var23");
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
        
        //static part        
        dataIdentification.setLanguage(Arrays.asList(Locale.ENGLISH));
        dataIdentification.setTopicCategories(Arrays.asList(TopicCategory.OCEANS));
        
        /*
         * Extent 
         */
        ExtentImpl extent = new ExtentImpl();
        
        //temporal extent
        TemporalExtentImpl tempExtent = new TemporalExtentImpl();
        Date start = parseDate(getVariable("var24"));
        Date stop  = parseDate(getVariable("var25"));

        if (start != null && stop != null) {
            DefaultInstant begin = new DefaultInstant(new DefaultPosition(start));
            DefaultInstant end   = new DefaultInstant(new DefaultPosition(stop));
            DefaultPeriod period = new DefaultPeriod(begin, end);
            tempExtent.setExtent(period);
            extent.setTemporalElements(Arrays.asList(tempExtent));
        } else {
            logger.severe("parse exception while parsing temporal extent date");
        }
        
        
        
        List<GeographicExtent> geoElements = new ArrayList<GeographicExtent>();
        //geographic areas
        List<String> geoAreas = getVariables("var26");
        for (String geoArea: geoAreas) {
            IdentifierImpl id  = new IdentifierImpl(geoArea);
            GeographicDescriptionImpl geoDesc = new GeographicDescriptionImpl();
            geoElements.add(geoDesc);
        }
        
        // geographic extent 
        geoElements.addAll(createGeographicExtent("var27", "var28", "var29", "var30"));
        extent.setGeographicElements(geoElements);
        
        dataIdentification.setExtent(Arrays.asList(extent));
        dataIdentifications.add(dataIdentification);
        
        /**
         * dataIdentification MOORING  todo multiple
         */
        dataIdentification = new DataIdentificationImpl();
        
        citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var31"));
        
        revisionDate = createRevisionDate(getVariable("var32"));
        citation.setDates(Arrays.asList(revisionDate));

        //principal investigator
        contact   = getContact(getVariable("var34"), Role.PRINCIPAL_INVESTIGATOR, getVariable("var33"));
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(getInternationalStringVariable("var35"));
        
        /*
         * Aggregate info
         */
        AggregateInformationImpl aggregateInfo = new AggregateInformationImpl();
        aggregateInfo.setInitiativeType(InitiativeType.PLATFORM);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        
        dataIdentification.setAggregationInfo(Arrays.asList(aggregateInfo));
        
        dataIdentification.setLanguage(Arrays.asList(Locale.ENGLISH));
        dataIdentification.setTopicCategories(Arrays.asList(TopicCategory.OCEANS));
        
        /**
         * TODO extent with GM_point var36
         */
        
        dataIdentifications.add(dataIdentification);
        
        /**
         * data Identification : samples TODO multiple
         */
        dataIdentification = new DataIdentificationImpl();
        
        citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var37"));
        
        revisionDate = createRevisionDate(getVariable("var38"));
        citation.setDates(Arrays.asList(revisionDate));
        
        //principal investigator
        contact   = getContact(getVariable("var40"), Role.PRINCIPAL_INVESTIGATOR, getVariable("var39"));
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(getInternationalStringVariable("var41"));
        
        keyword = createKeyword(Arrays.asList(getVariable("var42")), "counting_unit", "L181");
        dataIdentification.setDescriptiveKeywords(Arrays.asList(keyword));
        
        aggregateInfo = new AggregateInformationImpl();
        aggregateInfo.setInitiativeType(InitiativeType.OPERATION);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        
        dataIdentification.setAggregationInfo(Arrays.asList(aggregateInfo));
        
        dataIdentification.setLanguage(Arrays.asList(Locale.ENGLISH));
        dataIdentification.setTopicCategories(Arrays.asList(TopicCategory.OCEANS));
        
        dataIdentification.setSupplementalInformation(getInternationalStringVariable("var13"));
        result.setIdentificationInfo(dataIdentifications);
        
        
        return result;
    }

}
