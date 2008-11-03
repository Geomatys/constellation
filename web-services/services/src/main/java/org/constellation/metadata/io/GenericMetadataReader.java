/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.Column;
import org.constellation.generic.database.MultiFixed;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.Single;
import org.constellation.generic.edmo.Organisation;
import org.constellation.generic.edmo.Organisations;
import org.constellation.generic.edmo.ws.EdmoWebservice;
import org.constellation.generic.edmo.ws.EdmoWebserviceSoap;
import org.geotools.metadata.iso.ExtendedElementInformationImpl;
import org.geotools.metadata.iso.IdentifierImpl;
import static org.constellation.generic.database.Automatic.*;

// Geotools dependencies
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.MetadataExtensionInformationImpl;
import org.geotools.metadata.iso.PortrayalCatalogueReferenceImpl;
import org.geotools.metadata.iso.citation.AddressImpl;
import org.geotools.metadata.iso.citation.CitationDateImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.ContactImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.citation.TelephoneImpl;
import org.geotools.metadata.iso.constraint.LegalConstraintsImpl;
import org.geotools.metadata.iso.distribution.DigitalTransferOptionsImpl;
import org.geotools.metadata.iso.distribution.DistributionImpl;
import org.geotools.metadata.iso.distribution.DistributorImpl;
import org.geotools.metadata.iso.distribution.FormatImpl;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.metadata.iso.extent.GeographicDescriptionImpl;
import org.geotools.metadata.iso.extent.GeographicExtentImpl;
import org.geotools.metadata.iso.extent.TemporalExtentImpl;
import org.geotools.metadata.iso.extent.VerticalExtentImpl;
import org.geotools.metadata.iso.identification.AggregateInformationImpl;
import org.geotools.metadata.iso.identification.BrowseGraphicImpl;
import org.geotools.metadata.iso.identification.DataIdentificationImpl;
import org.geotools.metadata.iso.identification.KeywordsImpl;
import org.geotools.metadata.iso.spatial.GeometricObjectsImpl;
import org.geotools.metadata.iso.spatial.VectorSpatialRepresentationImpl;
import org.geotools.util.SimpleInternationalString;

//geoAPI dependencies
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.AssociationType;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.InitiativeType;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.util.InternationalString;
import org.opengis.metadata.Datatype;

/**
 * 
 * TODO regarder les cardinalité est mettre des null la ou 0-...
 *
 * @author Guilhem Legal
 */
public class GenericMetadataReader extends MetadataReader {
    
    /**
     * A configuration object used in Generic database mode.
     */
    private Automatic genericConfiguration;
    
    /**
     * A date Formater.
     */
    private static  List<DateFormat> dateFormats;
    static {
        dateFormats = new ArrayList<DateFormat>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
    }
    
    /**
     * A connection to the database.
     */
    private Connection connection;
    
    /**
     * An unmarshaller used for getting EDMO data.
     */
    private Unmarshaller unmarshaller;
    
    /**
     * A list of precompiled SQL request returning single value.
     */
    private Map<PreparedStatement, List<String>> singleStatements;
    
    /**
     * A list of precompiled SQL request returning multiple value.
     */
    private Map<PreparedStatement, List<String>> multipleStatements;
    
    /**
     * A Map of varName - value refreshed at every request.
     */
    private Map<String, String> singleValue;
    
    /**
     * * A Map of varName - list of value refreshed at every request.
     */
    private Map<String, List<String>> multipleValue;
    
    /**
     * 
     */
    private Map<String, ResponsiblePartyImpl> contacts;
    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param genericConfiguration
     */
    public GenericMetadataReader(Automatic genericConfiguration, Connection connection) throws SQLException, JAXBException {
        super();
        this.genericConfiguration = genericConfiguration;
        this.connection     = connection;
        initStatement();
        singleValue         = new HashMap<String, String>();
        multipleValue       = new HashMap<String, List<String>>();
        contacts            = new HashMap<String, ResponsiblePartyImpl>();
        JAXBContext context = JAXBContext.newInstance("org.constellation.generic.edmo");
        unmarshaller        = context.createUnmarshaller();
        List<String> contactID = new ArrayList<String>();
    }
    
    /**
     * Initialize the prepared statement build from the configuration file.
     * 
     * @throws java.sql.SQLException
     */
    private void initStatement() throws SQLException {
        singleStatements   = new HashMap<PreparedStatement, List<String>>();
        multipleStatements = new HashMap<PreparedStatement, List<String>>();
        Queries queries = genericConfiguration.getQueries();
        if (queries != null) {
            Single single = queries.getSingle();
            if (single != null) {
                for (Query query : single.getQuery()) {
                    List<String> varNames = new ArrayList<String>();
                    if (query.getSelect() != null) {
                        for (Column col : query.getSelect().getCol()) {
                            varNames.add(col.getVar());
                        }
                    }
                    PreparedStatement stmt =  connection.prepareStatement(query.buildSQLQuery());
                    singleStatements.put(stmt, varNames);
                }
            }
            MultiFixed multi = queries.getMultiFixed();
            if (multi != null) {
                for (Query query : multi.getQuery()) {
                    List<String> varNames = new ArrayList<String>();
                    if (query.getSelect() != null) {
                        for (Column col : query.getSelect().getCol()) {
                            varNames.add(col.getVar());
                        }
                    }
                    PreparedStatement stmt =  connection.prepareStatement(query.buildSQLQuery());
                    multipleStatements.put(stmt, varNames);
                }
            }
        }
    }
    
    /**
     * Retrieve a contact from the cache or from th EDMO WS if its hasn't yet been requested.
     *  
     * @param contactIdentifier
     * @return
     */
    private ResponsiblePartyImpl getContact(String contactIdentifier) {
        ResponsiblePartyImpl result = contacts.get(contactIdentifier);
        if (result == null) {
            result = loadContactFromEDMOWS(contactIdentifier);
            if (result != null)
                contacts.put(contactIdentifier, result);
        } 
        return result;
    }
            
    /**
     * Try to get a contact from EDMO WS and add it to the map of contact.
     * 
     * @param contactIdentifiers
     */
    private ResponsiblePartyImpl loadContactFromEDMOWS(String contactID) {
        EdmoWebservice service = new EdmoWebservice();
        EdmoWebserviceSoap port = service.getEdmoWebserviceSoap();
        
        // we call the web service EDMO
        String result = port.wsEdmoGetDetail(contactID);
        StringReader sr = new StringReader(result);
        Object obj;
        try {
            obj = unmarshaller.unmarshal(sr);
            if (obj instanceof Organisations) {
                Organisations orgs = (Organisations) obj;
                switch (orgs.getOrganisation().size()) {
                    case 0:
                        logger.severe("There is nor organisation for the specified code: " + contactID);
                        break;
                    case 1:
                        logger.info("contact created for contact ID: " + contactID);
                        return createContact(orgs.getOrganisation().get(0));
                    default:
                        logger.severe("There is more than one contact for the specified code: " + contactID);
                        break;
                }
            }
        } catch (JAXBException ex) {
            logger.severe("JAXBException while getting contact from EDMO WS");
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    private void loadData(String identifier) throws SQLException {
        singleValue.clear();
        multipleValue.clear();
        for (PreparedStatement stmt : singleStatements.keySet()) {
            ParameterMetaData meta = stmt.getParameterMetaData();
            int nbParam = meta.getParameterCount();
            int i = 1;
            while (i < nbParam + 1) {
                stmt.setString(i, identifier);
                i++;
            }
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                for (String varName : singleStatements.get(stmt)) {
                    singleValue.put(varName, result.getString(varName));
                }
            } else {
                logger.info("no result");
            }
            result.close();
        }
        
        for (PreparedStatement stmt : multipleStatements.keySet()) {
            ParameterMetaData meta = stmt.getParameterMetaData();
            int nbParam = meta.getParameterCount();
            int i = 1;
            while (i < nbParam + 1) {
                stmt.setString(i, identifier);
                i++;
            }
            ResultSet result = stmt.executeQuery();
            for (String varName : multipleStatements.get(stmt)) {
                multipleValue.put(varName, new ArrayList<String>());
            }
            while (result.next()) {
                for (String varName : multipleStatements.get(stmt)) {
                    multipleValue.get(varName).add(result.getString(varName));
               }
            }
            
        }
    }
    
    /**
     * Return a new Metadata object read from the database for the specified identifier.
     *  
     * @param identifier An unique identifier
     * @param mode An output schema mode: ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotools metadata)
     * 
     * @throws java.sql.SQLException
     * @throws org.constellation.ows.v100.OWSWebServiceException
     */
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws SQLException, WebServiceException {
        Object result = null;
        
        if (mode == ISO_19115) {
            
            result = getMetadataObject(identifier, type, elementName);
            
        } else if (mode == DUBLINCORE) {
            
        } else {
            throw new IllegalArgumentException("Unknow or unAuthorized standard mode: " + mode);
        }
        return result;
    }
    
    private MetaDataImpl getMetadataObject(String identifier, ElementSetType type, List<QName> elementName) throws SQLException {
        MetaDataImpl result = null;
        
        //TODO we verify that the identifier exists
        
        loadData(identifier);
        switch (genericConfiguration.getType()) {
            
            case CDI: 
                result = getCDIMetadata(identifier);
                break;
                
            case CSR: 
                result = getCSRMetadata(identifier);
                break;
                
            case EDMED: 
                result = getEDMEDMetadata(identifier);
                break;
            
        }
        
        return result;
    }

    /**
     * Extract a metadata from a CDI database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    private MetaDataImpl getCDIMetadata(String identifier) throws SQLException {
        
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
        ResponsiblePartyImpl author = getContact(getVariable("var01"));
        author.setRole(Role.AUTHOR);
        result.setContacts(Arrays.asList(author));
        System.out.println("author: " + author);
        
        /*
         * creation date TODO
         */ 
        
        /*
         * Spatial representation info
         */  
        VectorSpatialRepresentationImpl spatialRep = new VectorSpatialRepresentationImpl();
        GeometricObjectsImpl geoObj = new GeometricObjectsImpl(GeometricObjectType.valueOf(getVariable("var02")));
        spatialRep.setGeometricObjects(Arrays.asList(geoObj));
        
        result.setSpatialRepresentationInfo(Arrays.asList(spatialRep));
        
        /*
         * Reference system info TODO (issues referencing unmarshallable)
         */ 
        
        /*
         * extension information
         */
        MetadataExtensionInformationImpl extensionInfo = new MetadataExtensionInformationImpl();
        List<ExtendedElementInformationImpl> elements = new ArrayList<ExtendedElementInformationImpl>();
        
        //EDMO
        ExtendedElementInformationImpl edmo =  createExtensionInfo("SDN:EDMO::");
        elements.add(edmo);
        
        //L021
        ExtendedElementInformationImpl L021 = createExtensionInfo("SDN:L021:1:");
        elements.add(L021);
        
        //L031
        ExtendedElementInformationImpl L031 = createExtensionInfo("SDN:L031:2:");
        elements.add(L031);
        
        //L071
        ExtendedElementInformationImpl L071 = createExtensionInfo("SDN:L071:1:");
        elements.add(L071);
        
        //L081
        ExtendedElementInformationImpl L081 = createExtensionInfo("SDN:L081:1:");
        elements.add(L081);
        
        //L231
        ExtendedElementInformationImpl L231 = createExtensionInfo("SDN:L231:3:");
        elements.add(L231);
        
        //L241
        ExtendedElementInformationImpl L241 = createExtensionInfo("SDN:L241:1:");
        elements.add(L241);
        extensionInfo.setExtendedElementInformation(elements);
        
        result.setMetadataExtensionInfo(Arrays.asList(extensionInfo));
        
        /*
         * Data indentification
         */ 
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();
        
        CitationImpl citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString(getVariable("var04")));
        citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(getVariable("var05"))));
        
        CitationDateImpl revisionDate = createRevisionDate(getVariable("var06"));
        citation.setDates(Arrays.asList(revisionDate));
        
        List<ResponsiblePartyImpl> originators = new ArrayList<ResponsiblePartyImpl>();
        for (String contactID : getVariables("var07")) {
            ResponsiblePartyImpl originator  = getContact(contactID);
            originator.setRole(Role.ORIGINATOR);
            originators.add(originator);
        }
        citation.setCitedResponsibleParties(originators);
        
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(new SimpleInternationalString(getVariable("var08")));
        
        ResponsiblePartyImpl custodian   = getContact(getVariable("var09"));
        custodian.setRole(Role.CUSTODIAN);
        
        dataIdentification.setPointOfContacts(Arrays.asList(custodian));

        /*
         * keywords
         */  
        List<KeywordsImpl> keywords = new ArrayList<KeywordsImpl>();
        
        //parameter
        List<String> parameters = getVariables("var10");
        KeywordsImpl keyword = new KeywordsImpl();
        List<InternationalString> kws = new ArrayList<InternationalString>();
        for (String parameter: parameters) {
            kws.add(new SimpleInternationalString(parameter));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf("parameter"));

        citation = createKeywordCitation("BODC Parameter Discovery Vocabulary", "P021", "2007-09-25T02:00:02", "19");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);

        //instrument
        String key = getVariable("var11");
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(key)));
        keyword.setType(KeywordType.valueOf("instrument"));

        citation = createKeywordCitation("SeaDataNet device categories", "L05", "2007-09-06T15:03:00", "1");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        //platform
        key = getVariable("var12");
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(key)));
        keyword.setType(KeywordType.valueOf("platform_class"));

        citation = createKeywordCitation("SeaDataNet Platform Classes", "L061", "2007-01-13T06:42:58", "4");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        //projects
        List<String> projects = getVariables("var13");
        keyword = new KeywordsImpl();
        kws = new ArrayList<InternationalString>();
        for (String project : projects) {
            kws.add(new SimpleInternationalString(project));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf("platform_class"));

        citation = createKeywordCitation("European Directory of Marine Environmental Research Projects", "EDMERP", null, null);
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        dataIdentification.setDescriptiveKeywords(keywords);

        /*
         * resource constraint
         */  
        List<String> resConsts = getVariables("var14");
        LegalConstraintsImpl constraint = new LegalConstraintsImpl();
        for (String resConst : resConsts) {
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
        citation.setTitle(new SimpleInternationalString(getVariable("var15")));
        citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(getVariable("var16"))));
        revisionDate = createRevisionDate(getVariable("var17"));
        citation.setDates(Arrays.asList(revisionDate));
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);
        
        //station
        aggregateInfo = new AggregateInformationImpl();
        citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString(getVariable("var18")));
        citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(getVariable("var19"))));
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
        //dataIdentification.set ????
        
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
        tempExtent.setStartTime(start);
        Date stop  = parseDate(getVariable("var29"));
        tempExtent.setEndTime(stop);
        if (start == null || stop == null)
            logger.severe("parse exception while parsing temporal extent date");extent.setTemporalElements(Arrays.asList(tempExtent));
        
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
            logger.severe("Number format exception while parsing boundingBox");
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
        
        ResponsiblePartyImpl distributorContact   = getContact(getVariable("var36"));
        distributorContact.setRole(Role.DISTRIBUTOR);
        distributor.setDistributorContact(distributorContact);
                
        distributionInfo.setDistributors(Arrays.asList(distributor));
        
        //format
        List<Format> formats  = new ArrayList<Format>();
        List<String> names    = getVariables("var37");
        List<String> versions = getVariables("var38");
        int i = 0;
        while (i < names.size() && i < versions.size()) {
            FormatImpl format = new FormatImpl();
            format.setName(new SimpleInternationalString(names.get(i)));
            format.setVersion(new SimpleInternationalString(versions.get(i)));
            formats.add(format);
            i++;
        }
        distributionInfo.setDistributionFormats(formats);
        
        //transfert options
        DigitalTransferOptionsImpl digiTrans = new DigitalTransferOptionsImpl();
        try {
            digiTrans.setTransferSize(Double.parseDouble(getVariable("var39")));
        } catch (NumberFormatException ex) {
            logger.severe("Number format exception while parsing transfer size");
        }
        OnLineResourceImpl onlines = new OnLineResourceImpl();
        try {
            onlines.setLinkage(new URI(getVariable("var40")));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in contact online resource");
        }
        onlines.setDescription(new SimpleInternationalString(getVariable("var41")));
        onlines.setFunction(OnLineFunction.DOWNLOAD);
        digiTrans.setOnLines(Arrays.asList(onlines));
        
        distributionInfo.setTransferOptions(Arrays.asList(digiTrans));
        
        result.setDistributionInfo(distributionInfo);
        
        return result;
    }
    
    /**
     * Extract a metadata from a CSR database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    private MetaDataImpl getCSRMetadata(String identifier) throws SQLException {
        
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
        ResponsiblePartyImpl contact = getContact(getVariable("var01"));
        contact.setRole(Role.AUTHOR);
        result.setContacts(Arrays.asList(contact));
        
        /*
         * creation date TODO
         */ 
        
        /*
         * extension information
         */
        MetadataExtensionInformationImpl extensionInfo = new MetadataExtensionInformationImpl();
        List<ExtendedElementInformationImpl> elements = new ArrayList<ExtendedElementInformationImpl>();
        
        
        //EDMO
        ExtendedElementInformationImpl edmo =  createExtensionInfo("SDN:EDMO::");
        elements.add(edmo);
        
        //L081
        ExtendedElementInformationImpl L081 = createExtensionInfo("SDN:L081:1:");
        elements.add(L081);
        
        //L231
        ExtendedElementInformationImpl L231 = createExtensionInfo("SDN:L231:3:");
        elements.add(L231);
        
        //C77
        ExtendedElementInformationImpl C77 = createExtensionInfo("SDN:C77:0:");
        elements.add(C77);
        
        extensionInfo.setExtendedElementInformation(elements);
        result.setMetadataExtensionInfo(Arrays.asList(extensionInfo));
        
        
        List<DataIdentificationImpl> dataIdentifications = new ArrayList<DataIdentificationImpl>();
        /*
         * Data indentification 1
         */ 
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();
        
        CitationImpl citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString(getVariable("var02")));
        citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(getVariable("var03"))));
        
        CitationDateImpl revisionDate = createRevisionDate(getVariable("var04"));
        citation.setDates(Arrays.asList(revisionDate));
        List<ResponsibleParty> chiefs = new ArrayList<ResponsibleParty>();
        
        //first chief
        contact   = getContact(getVariable("var05"));
        contact.setRole(Role.ORIGINATOR);
        chiefs.add(contact);
        
        //second and other chief
        List<String> secondChiefs = getVariables("var06");
        for (String secondChief : secondChiefs) {
            contact   = getContact(secondChief);
            contact.setRole(Role.POINT_OF_CONTACT);
            chiefs.add(contact);
        }
        
        //labo
        List<String> laboratories = getVariables("var07");
        for (String laboratory : laboratories) {
            contact   = getContact(laboratory);
            contact.setRole(Role.ORIGINATOR);
            chiefs.add(contact);
        }
        
        citation.setCitedResponsibleParties(chiefs);
        dataIdentification.setCitation(citation);
        
        dataIdentification.setPurpose(new SimpleInternationalString(getVariable("var08")));
        
        BrowseGraphicImpl graphOverview = new BrowseGraphicImpl();
        try {
            graphOverview.setFileName(new URI(getVariable("var09")));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in graph overview");
        }
        
        graphOverview.setFileDescription(new SimpleInternationalString(getVariable("var10")));
        graphOverview.setFileType(getVariable("var11"));
        
        dataIdentification.setGraphicOverviews(Arrays.asList(graphOverview));
        
        /*
         * keywords
         */  
        List<KeywordsImpl> keywords = new ArrayList<KeywordsImpl>();
        
        //port of departure
        KeywordsImpl keyword = new KeywordsImpl();
        
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(getVariable("var12"))));
        keyword.setType(KeywordType.valueOf("departure_place"));

        citation = createKeywordCitation("Ports Gazetteer", "C381", "2007-09-20T02:00:02", "2");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);

        //port of arrival
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(getVariable("var13"))));
        keyword.setType(KeywordType.valueOf("arrival_place"));

        citation = createKeywordCitation("Ports Gazetteer", "C381", "2007-09-20T02:00:02", "2");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        //country of departure
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(getVariable("var14"))));
        keyword.setType(KeywordType.valueOf("departure_contry"));

        citation = createKeywordCitation("International Standards Organisation countries", "C320", "2007-08-22T16:37:58", "1");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // country of arrival
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(getVariable("var15"))));
        keyword.setType(KeywordType.valueOf("arrival_country"));

        citation =  createKeywordCitation("International Standards Organisation countries", "C320", "2007-08-22T16:37:58", "1");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // ship
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(getVariable("var16"))));
        keyword.setType(KeywordType.valueOf("platform"));

        citation = createKeywordCitation("SeaDataNet Cruise Summary Report ship metadata", "C174", "2007-05-14T15:45:00", "0");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // platform class
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(getVariable("var17"))));
        keyword.setType(KeywordType.valueOf("platform_class"));

        citation = createKeywordCitation("SeaDataNet Platform Classes", "L061", "2007-01-13T06:42:58", "4");
        keyword.setThesaurusName(citation);
        
        // projects
        List<String> projects = getVariables("var18");
        keyword = new KeywordsImpl();
        List<InternationalString> kws = new ArrayList<InternationalString>();
        for (String project : projects) {
            kws.add(new SimpleInternationalString(project));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf("platform_class"));

        citation = createKeywordCitation("European Directory of Marine Environmental Research Projects", "EDMERP", null, null);
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // general oceans area
        List<String> oceansAreas = getVariables("var19");
        keyword = new KeywordsImpl();
        kws = new ArrayList<InternationalString>();
        for (String oceansArea : oceansAreas) {
            kws.add(new SimpleInternationalString(oceansArea));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.PLACE);

        citation = createKeywordCitation("SeaDataNet Sea Areas", "C16", "2007-03-01T12:00:00", "0");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // geographic coverage
        List<String> geoCoverages = getVariables("var20");
        keyword = new KeywordsImpl();
        kws = new ArrayList<InternationalString>();
        for (String geoCoverage : geoCoverages) {
            kws.add(new SimpleInternationalString(geoCoverage));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf("marsden_square"));

        citation = createKeywordCitation("Ten-degree Marsden Squares", "C371", "2007-08-03T02:00:02", "1");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
         //parameter
        List<String> parameters = getVariables("var20");
        keyword = new KeywordsImpl();
        kws = new ArrayList<InternationalString>();
        for (String parameter : parameters){
            kws.add(new SimpleInternationalString(parameter));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf("parameter"));

        citation = createKeywordCitation("BODC Parameter Discovery Vocabulary", "P021", "2007-09-25T02:00:02", "19");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // instrument
        String key = getVariable("var21");
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(key)));
        keyword.setType(KeywordType.valueOf("instrument"));

        citation = createKeywordCitation("SeaDataNet device categories", "L05", "2007-03-01T08:16:13", "0");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);

        dataIdentification.setDescriptiveKeywords(keywords);
        
        /*
         * resource constraint
         */  
        List<String> resConsts = getVariables("var23");
        LegalConstraintsImpl constraint = new LegalConstraintsImpl();
        for (String resConst: resConsts) {
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
        tempExtent.setStartTime(start);
        Date stop  = parseDate(getVariable("var25"));
        tempExtent.setEndTime(stop);
        if (start == null || stop == null)
            logger.severe("parse exception while parsing temporal extent date");
        
        extent.setTemporalElements(Arrays.asList(tempExtent));
        
        List<GeographicExtentImpl> geoElements = new ArrayList<GeographicExtentImpl>();
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
        citation.setTitle(new SimpleInternationalString(getVariable("var31")));
        
        revisionDate = createRevisionDate(getVariable("var32"));
        citation.setDates(Arrays.asList(revisionDate));

        //principal investigator
        contact   = getContact(getVariable("var34"));
        contact.setRole(Role.PRINCIPAL_INVESTIGATOR);
        contact.setIndividualName(getVariable("var33"));
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(new SimpleInternationalString(getVariable("var35")));
        
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
        citation.setTitle(new SimpleInternationalString(getVariable("var37")));
        
        revisionDate = createRevisionDate(getVariable("var38"));
        citation.setDates(Arrays.asList(revisionDate));
        
        //principal investigator
        contact   = getContact(getVariable("var40"));
        contact.setRole(Role.PRINCIPAL_INVESTIGATOR);
        contact.setIndividualName(getVariable("var39"));
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(new SimpleInternationalString(getVariable("var41")));
        
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(getVariable("var42"))));
        keyword.setType(KeywordType.valueOf("counting_unit"));

        citation = createKeywordCitation("ROSCOP sample quantification units", "L181", "2007-06-29T13:23:00", "0");
        keyword.setThesaurusName(citation);

        aggregateInfo = new AggregateInformationImpl();
        aggregateInfo.setInitiativeType(InitiativeType.OPERATION);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        
        dataIdentification.setAggregationInfo(Arrays.asList(aggregateInfo));
        
        dataIdentification.setLanguage(Arrays.asList(Locale.ENGLISH));
        dataIdentification.setTopicCategories(Arrays.asList(TopicCategory.OCEANS));
        
        dataIdentification.setSupplementalInformation(new SimpleInternationalString(getVariable("var13")));
        result.setIdentificationInfo(dataIdentifications);
        
        
        return result;
    }
    
     /**
     * Extract a metadata from a EDMED database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    private MetaDataImpl getEDMEDMetadata(String identifier) throws SQLException {
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
        ResponsiblePartyImpl contact   = getContact(getVariable("var01"));
        contact.setRole(Role.AUTHOR);
        result.setContacts(Arrays.asList(contact));
        
        /*
         * creation date TODO
         */ 
        
        /*
         * extension information TODO
         */
        
        /**
         * Data identification
         */
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();

        CitationImpl citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString(getVariable("var02")));
        citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(getVariable("var03"))));
        CitationDateImpl revisionDate = createRevisionDate(getVariable("var04"));
        citation.setDates(Arrays.asList(revisionDate));
        contact = getContact(getVariable("var05"));
        contact.setRole(Role.ORIGINATOR);
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(new SimpleInternationalString(getVariable("var06"))); 
        dataIdentification.setPurpose(new SimpleInternationalString("var07"));
        
        List<ResponsiblePartyImpl> pointOfContacts = new ArrayList<ResponsiblePartyImpl>();
        
        contact = getContact(getVariable("var08"));
        contact.setRole(Role.CUSTODIAN);
        pointOfContacts.add(contact);
        contact = getContact(getVariable("var10"));
        contact.setRole(Role.POINT_OF_CONTACT);
        contact.setIndividualName(getVariable("var09"));
        pointOfContacts.add(contact);
        
        dataIdentification.setPointOfContacts(pointOfContacts);

        /**
         * keywords 
         */
        
        List<KeywordsImpl> keywords = new ArrayList<KeywordsImpl>();
        
        // SEA AREAS
        List<String> seaAreas = getVariables("var11");
        KeywordsImpl keyword = new KeywordsImpl();
        List<InternationalString> kws = new ArrayList<InternationalString>();
        for (String seaArea : seaAreas) {
            kws.add(new SimpleInternationalString(seaArea));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.PLACE);

        citation = createKeywordCitation("SeaDataNet Sea Areas", "C16", "2007-03-01T12:00:00", "0");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        //parameter
        List<String> parameters = getVariables("var12");
        keyword = new KeywordsImpl();
        kws = new ArrayList<InternationalString>();
        for (String parameter : parameters) {
            kws.add(new SimpleInternationalString(parameter));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf("parameter"));

        citation = createKeywordCitation("BODC Parameter Discovery Vocabulary", "P021", "2007-09-25T02:00:02", "19");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // instrument
        String key = getVariable("var13");
        keyword = new KeywordsImpl();
        keyword.setKeywords(Arrays.asList(new SimpleInternationalString(key)));
        keyword.setType(KeywordType.valueOf("instrument"));

        citation = createKeywordCitation("SeaDataNet device categories", "L05", "2007-09-06T15:03:00", "1");
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        
        // projects
        List<String> projects = getVariables("var14");
        keyword = new KeywordsImpl();
        kws = new ArrayList<InternationalString>();
        for (String project : projects) {
            kws.add(new SimpleInternationalString(project));
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf("projects"));

        citation = createKeywordCitation("European Directory for Marine Environmental Research Projects", "EDMERP", null, null);
        keyword.setThesaurusName(citation);

        keywords.add(keyword);
        dataIdentification.setDescriptiveKeywords(keywords);
        
        /*
         * resource constraint
         */  
        List<String> resConsts = getVariables("var15");
        LegalConstraintsImpl constraint = new LegalConstraintsImpl();
        for (String resConst : resConsts) {
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
        extent.setDescription(new SimpleInternationalString(getVariable("var16")));
        
        //temporal extent
        TemporalExtentImpl tempExtent = new TemporalExtentImpl();
        Date start = parseDate(getVariable("var17"));
        tempExtent.setStartTime(start);
        Date stop  = parseDate(getVariable("var18"));
        tempExtent.setEndTime(stop);
        if (start == null || stop == null)
            logger.severe("parse exception while parsing temporal extent date");
        extent.setTemporalElements(Arrays.asList(tempExtent));
        extents.add(extent);
        
        extent = new ExtentImpl();
        List<GeographicExtentImpl> geoElements = new ArrayList<GeographicExtentImpl>();
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
        citation.setTitle(new SimpleInternationalString(getVariable("var25")));
        CitationDateImpl publicationDate = createPublicationDate(getVariable("var26"));
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
    /**
     * return a list of value for the specified variable name.
     * 
     * @param variables
     * @return
     */
    private List<String> getVariables(String variable) {
        return multipleValue.get(variable);
    }
    
    /**
     * return the value for the specified variable name.
     * 
     * @param variable
     * @return
     */
    private String getVariable(String variable) {
        return singleValue.get(variable);
    }
    
    /**
     * Build a new Responsible party with the specified Organisation object retrieved from EDMO WS.
     * 
     * TODO get from EDMO WS.
     * 
     * @param org
     * @return
     * @throws java.sql.SQLException
     */
    private ResponsiblePartyImpl createContact(Organisation org) {
        
        ResponsiblePartyImpl contact = new ResponsiblePartyImpl();
        contact.setOrganisationName(new SimpleInternationalString(org.getNative_name()));
        
        ContactImpl contactInfo = new ContactImpl();
        TelephoneImpl phone     = new TelephoneImpl();
        AddressImpl address     = new AddressImpl();
        OnLineResourceImpl or   = new OnLineResourceImpl();
                
        phone.setFacsimiles(Arrays.asList(org.getFax()));
        phone.setVoices(Arrays.asList(org.getPhone()));
        contactInfo.setPhone(phone);
        
        address.setDeliveryPoints(Arrays.asList(org.getAddress()));
        address.setCity(new SimpleInternationalString(org.getCity()));
        // TODO address.setAdministrativeArea(new SimpleInternationalString()); 
        address.setPostalCode(org.getZipcode());
        address.setCountry(new SimpleInternationalString(org.getC_country()));
        address.setElectronicMailAddresses(Arrays.asList(org.getEmail()));
        contactInfo.setAddress(address);
        
        try {
            or.setLinkage(new URI(org.getWebsite()));
        } catch (URISyntaxException ex) {
            logger.severe("URI Syntax exception in contact online resource");
        }
        contactInfo.setOnLineResource(or);
        contact.setContactInfo(contactInfo);
        return contact;
    }
    
    
    /**
     * Parse the specified date and return a CitationDate with the dateType code REVISION.
     * 
     * @param date
     * @return
     */
    private CitationDateImpl createRevisionDate(String date) {
        CitationDateImpl revisionDate = new CitationDateImpl();
        revisionDate.setDateType(DateType.REVISION);
        Date d = parseDate(date);
        if (d != null)
            revisionDate.setDate(d);
        else logger.severe("revision date null: " + date);
        return revisionDate;
    }
    
    /**
     * Parse the specified date and return a CitationDate with the dateType code PUBLICATION.
     * 
     * @param date
     * @return
     */
    private CitationDateImpl createPublicationDate(String date) {
        CitationDateImpl revisionDate = new CitationDateImpl();
        revisionDate.setDateType(DateType.PUBLICATION);
        Date d = parseDate(date);
        if (d != null)
            revisionDate.setDate(d);
        else logger.severe("publication date null: " + date);
        return revisionDate;
    }

    /**
     * 
     */
    private Date parseDate(String date) {
        int i = 0;
        while (i < dateFormats.size()) {
            DateFormat dateFormat = dateFormats.get(i);
            try {
                Date d = dateFormat.parse(date);
                return d;
            } catch (ParseException ex) {
                i++;
            }
        }
        logger.severe("unable to parse the date: " + date);
        return null;
    }
    
    /**
     * 
     * @param westVar
     * @param eastVar
     * @param southVar
     * @param northVar
     * @return
     */
    private List<GeographicExtentImpl> createGeographicExtent(String westVar, String eastVar, String southVar, String northVar) {
        List<GeographicExtentImpl> result = new ArrayList<GeographicExtentImpl>();
        
        List<String> w = getVariables(westVar);
        List<String> e = getVariables(eastVar);
        List<String> s = getVariables(southVar);
        List<String> n = getVariables(northVar);
        
        if (!(w.size() == e.size() &&  e.size() == s.size() && s.size() == n.size())) {
            logger.severe("There is not he same number of geographique extent coordinates");
            return result;
        }
        int size = w.size();
        for (int i = 0; i < size; i++) {
            double west = 0; double east = 0; double south = 0; double north = 0;
            try {
                if (w.get(i) != null) {
                    west = Double.parseDouble(w.get(i));
                }
                if (e.get(i) != null) {
                    east = Double.parseDouble(e.get(i));
                }
                if (s.get(i) != null) {
                    south = Double.parseDouble(s.get(i));
                }
                if (n.get(i) != null) {
                    north = Double.parseDouble(n.get(i));
                }
            } catch (NumberFormatException ex) {
                logger.severe("Number format exception while parsing boundingBox: " + '\n' +
                        "current box: " + w.get(i) + ',' + e.get(i) + ',' + s.get(i) + ',' + n.get(i));
            }
            GeographicExtentImpl geo = new GeographicBoundingBoxImpl(west, east, south, north);
            result.add(geo);
        }
        return result;
    }
    
    private CitationImpl createKeywordCitation(String title, String altTitle, String revDate, String editionNumber) {
        CitationImpl citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString(title));
        citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(altTitle)));
        CitationDateImpl revisionDate;
        if (revDate != null) {
            revisionDate = createRevisionDate(revDate);
        } else {
            revisionDate = new CitationDateImpl(null, DateType.REVISION); 
        }
        citation.setDates(Arrays.asList(revisionDate));
        if (editionNumber != null)
            citation.setEdition(new SimpleInternationalString(editionNumber));
        citation.setIdentifiers(Arrays.asList(new IdentifierImpl("http://www.seadatanet.org/urnurl/")));
        return citation;
    }
    
    private ExtendedElementInformationImpl createExtensionInfo(String name) {
        ExtendedElementInformationImpl element = new ExtendedElementInformationImpl();
        element.setName(name);
        element.setDefinition(new SimpleInternationalString("http://www.seadatanet.org/urnurl/"));
	element.setDataType(Datatype.CODE_LIST);		
        element.setParentEntity(Arrays.asList("SeaDataNet"));
        //TODO see for the source
        
        return element;
    }
            
}
