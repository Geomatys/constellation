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
import org.geotools.metadata.iso.PortrayalCatalogueReferenceImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.constraint.LegalConstraintsImpl;
import org.geotools.metadata.iso.distribution.DigitalTransferOptionsImpl;
import org.geotools.metadata.iso.distribution.DistributionImpl;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicDescriptionImpl;
import org.geotools.metadata.iso.extent.TemporalExtentImpl;
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
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal
 */
public class EDMEDReader extends GenericMetadataReader {
    
    
    /**
     * 
     * @param genericConfiguration
     * @param connection
     * @throws java.sql.SQLException
     * @throws javax.xml.bind.JAXBException
     */
    public EDMEDReader(Automatic genericConfiguration, Connection connection) throws SQLException, JAXBException {
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
    public EDMEDReader(Automatic genericConfiguration, Connection connection, boolean fillAnchor) throws SQLException, JAXBException {
        super(genericConfiguration, connection, fillAnchor);
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
        List<BoundingBoxType> bboxes = createBoundingBoxes("var20", "var21", "var22", "var23");
        if (type.equals(ElementSetType.BRIEF))
            return new BriefRecordType(ident, title, dataType, bboxes);
        
        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        //topic category
        subject.add(new SimpleLiteral("oceans"));
        
        // SEA AREAS
        for (String k : getKeywordsValue(getVariables("var11"), "C16"))
            subject.add(new SimpleLiteral(k));
        
        //parameter
        for (String k : getKeywordsValue(getVariables("var12"), "P021"))
            subject.add(new SimpleLiteral(k));
        
        // instrument
        for (String k : getKeywordsValue(Arrays.asList(getVariable("var13")), "L05"))
            subject.add(new SimpleLiteral(k));
        
        // projects
        for (String k : getKeywordsValue(getVariables("var14"), "EDMERP"))
            subject.add(new SimpleLiteral(k));
        
        // No formats in EDMED
        List<SimpleLiteral> formats = new ArrayList<SimpleLiteral>();
        
        SimpleLiteral modified = new SimpleLiteral(dateFormats.get(0).format(new Date()));
        
        List<SimpleLiteral> _abstract = Arrays.asList(new SimpleLiteral(getVariable("var06")));
        
        if (type.equals(ElementSetType.SUMMARY))
            return new SummaryRecordType(ident, title, dataType, bboxes, subject, formats, modified, _abstract);
        
        List<SimpleLiteral> creators = new ArrayList<SimpleLiteral>();
        ResponsibleParty originator  = getContact(getVariable("var05"), Role.ORIGINATOR);
        if (originator != null) {
            InternationalString s = originator.getOrganisationName();
            if (s != null)
                creators.add(new SimpleLiteral(s.toString()));
        }
        
        
        SimpleLiteral distributor = null;
        ResponsibleParty distrib  = getContact(getVariable("var28"), Role.PUBLISHER);
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
     * Extract a metadata from a EDMED database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    protected MetaDataImpl getISO(String identifier)  {
        MetaDataImpl result     = new MetaDataImpl();
        
        /*
         * static part
         */
        result.setFileIdentifier(identifier);
        result.setLanguage(Locale.ENGLISH);
        result.setCharacterSet(CharacterSet.UTF_8);
        result.setHierarchyLevels(Arrays.asList(ScopeCode.SERIES));
        result.setHierarchyLevelNames(Arrays.asList("EDMED record"));
        
         /*
         * contact parts
         */
        ResponsibleParty contact   = getContact(getVariable("var01"), Role.AUTHOR);
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
        ExtendedElementInformation L231 = createExtensionInfo("SDN:L231:1:");
        elements.add(L231);
        
        /**
         * Data identification
         */
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();

        CitationImpl citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var02"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var03")));
        CitationDate revisionDate = createRevisionDate(getVariable("var04"));
        citation.setDates(Arrays.asList(revisionDate));
        contact = getContact(getVariable("var05"), Role.ORIGINATOR);
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(getInternationalStringVariable("var06")); 
        dataIdentification.setPurpose(getInternationalStringVariable("var07"));
        
        List<ResponsibleParty> pointOfContacts = new ArrayList<ResponsibleParty>();
        
        contact = getContact(getVariable("var08"), Role.CUSTODIAN);
        pointOfContacts.add(contact);
        contact = getContact(getVariable("var10"), Role.POINT_OF_CONTACT, getVariable("var09"));
        pointOfContacts.add(contact);
        
        dataIdentification.setPointOfContacts(pointOfContacts);

        /**
         * keywords 
         */
        
        List<Keywords> keywords = new ArrayList<Keywords>();
        
        // SEA AREAS
        Keywords keyword = createKeyword(getVariables("var11"), "place", "C16");
        keywords.add(keyword);
        
        //parameter
        keyword = createKeyword(getVariables("var12"), "parameter", "P021");
        keywords.add(keyword);
        
        // instrument
        keyword = createKeyword(Arrays.asList(getVariable("var13")), "instrument", "L05");
        keywords.add(keyword);
        
        // projects
        keyword = createKeyword(getVariables("var14"), "projects", "EDMERP");
        keywords.add(keyword);
        dataIdentification.setDescriptiveKeywords(keywords);
        
        /*
         * resource constraint
         */  
        List<String> resConsts = getVariables("var15");
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
        
        dataIdentification.setLanguage(Arrays.asList(Locale.ENGLISH));
        dataIdentification.setTopicCategories(Arrays.asList(TopicCategory.OCEANS));
        
         /*
         * Extents 
         */
        List<ExtentImpl> extents = new ArrayList<ExtentImpl>();
        //temporal 
        ExtentImpl extent = new ExtentImpl();
        extent.setDescription(getInternationalStringVariable("var16"));
        
        //temporal extent
        TemporalExtentImpl tempExtent = new TemporalExtentImpl();
        Date start = parseDate(getVariable("var17"));
        Date stop  = parseDate(getVariable("var18"));
        
        if (start != null && stop != null) {
            DefaultInstant begin = new DefaultInstant(new DefaultPosition(start));
            DefaultInstant end   = new DefaultInstant(new DefaultPosition(stop));
            DefaultPeriod period = new DefaultPeriod(begin, end);
            tempExtent.setExtent(period);
            extent.setTemporalElements(Arrays.asList(tempExtent));
        } else {
            logger.severe("parse exception while parsing temporal extent date");
        }
        extents.add(extent);
        
        extent = new ExtentImpl();
        List<GeographicExtent> geoElements = new ArrayList<GeographicExtent>();
        //geographic areas
        List<String> geoAreas = getVariables("var19");
        for (String geoArea : geoAreas) {
            IdentifierImpl id  = new IdentifierImpl(geoArea);
            GeographicDescriptionImpl geoDesc = new GeographicDescriptionImpl();
            geoElements.add(geoDesc);
        }
        
        // geographic extent
        geoElements.addAll(createGeographicExtent("var20", "var21", "var22", "var23"));
        extent.setGeographicElements(geoElements);
        extents.add(extent);
        
        dataIdentification.setExtent(extents);
        
        result.setIdentificationInfo(Arrays.asList(dataIdentification));
        
        /**
         * Distribution info
         */
        DistributionImpl distributionInfo = new DistributionImpl();
        
        //transfert options
        DigitalTransferOptionsImpl digiTrans = new DigitalTransferOptionsImpl();
        OnLineResourceImpl onlines = new OnLineResourceImpl();
        try {
            onlines.setLinkage(new URI(getVariable("var24")));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in contact online resource");
        }
        digiTrans.setOnLines(Arrays.asList(onlines));
        distributionInfo.setTransferOptions(Arrays.asList(digiTrans));
        result.setDistributionInfo(distributionInfo);
        
        /**
         * Portayal catalogue info TODO mulitple
         */
        PortrayalCatalogueReferenceImpl portrayal = new PortrayalCatalogueReferenceImpl();
        citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var25"));
        CitationDate publicationDate = createPublicationDate(getVariable("var26"));
        citation.setDates(Arrays.asList(publicationDate));
        ResponsiblePartyImpl author = new ResponsiblePartyImpl();
        author.setIndividualName(getVariable("var27"));
        author.setRole(Role.AUTHOR);
        ResponsiblePartyImpl editor = new ResponsiblePartyImpl();
        editor.setIndividualName(getVariable("var28"));
        editor.setRole(Role.PUBLISHER);
        
        portrayal.setPortrayalCatalogueCitations(Arrays.asList(citation));
        result.setPortrayalCatalogueInfo(Arrays.asList(portrayal));
        
        
        return result;
    }

}
