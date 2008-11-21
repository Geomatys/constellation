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
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.constellation.generic.nerc.CodeTableType;
import org.constellation.generic.vocabulary.Vocabulary;
import org.constellation.skos.RDF;
import org.constellation.ws.rs.WebService;
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
import org.geotools.metadata.iso.identification.ResolutionImpl;
import org.geotools.metadata.iso.spatial.GeometricObjectsImpl;
import org.geotools.metadata.iso.spatial.VectorSpatialRepresentationImpl;
import org.geotools.metadata.note.Anchors;
import org.geotools.temporal.object.DefaultPeriod;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPosition;
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
 * TODO regarder les cardinalite est mettre des null la ou 0-...
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
     * A map of the already retrieved contact from EDMO WS.
     */
    private Map<String, ResponsiblePartyImpl> contacts;
    
    /**
     * A map making the correspondance between parameter code and the real keyword value.
     * this map is fill from a list of configuration file P021.xml, L05.xml, ..
     */
    private Map<String, Vocabulary> vocabularies;
    
    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param genericConfiguration
     */
    public GenericMetadataReader(Automatic genericConfiguration, Connection connection) throws SQLException, JAXBException {
        super();
        this.genericConfiguration = genericConfiguration;
        this.connection     = connection;
        initStatement();
        singleValue            = new HashMap<String, String>();
        multipleValue          = new HashMap<String, List<String>>();
        contacts               = new HashMap<String, ResponsiblePartyImpl>();
        JAXBContext context    = JAXBContext.newInstance("org.constellation.generic.edmo:org.constellation.generic.vocabulary:" +
                                                         "org.constellation.generic.nerc:org.constellation.skos");
        unmarshaller           = context.createUnmarshaller();
        File cswConfigDir      = new File(WebService.getSicadeDirectory(), "csw_configuration");
        vocabularies           = loadVocabulary(new File(cswConfigDir, "vocabulary"), true);
        List<String> contactID = new ArrayList<String>();
    }
    
    /**
     * Build a new Generic metadata reader and initialize the statement (with a flag for filling the Anchors).
     * @param genericConfiguration
     */
    public GenericMetadataReader(Automatic genericConfiguration, Connection connection, boolean fillAnchor) throws SQLException, JAXBException {
        super();
        this.genericConfiguration = genericConfiguration;
        this.connection     = connection;
        initStatement();
        singleValue            = new HashMap<String, String>();
        multipleValue          = new HashMap<String, List<String>>();
        contacts               = new HashMap<String, ResponsiblePartyImpl>();
        JAXBContext context    = JAXBContext.newInstance("org.constellation.generic.edmo:org.constellation.generic.vocabulary:" +
                                                         "org.constellation.generic.nerc:org.constellation.skos");
        unmarshaller           = context.createUnmarshaller();
        File cswConfigDir      = new File(WebService.getSicadeDirectory(), "csw_configuration");
        vocabularies           = loadVocabulary(new File(cswConfigDir, "vocabulary"), fillAnchor);
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
                    String textQuery = query.buildSQLQuery();
                    logger.finer("new Single query: " + textQuery);
                    PreparedStatement stmt =  connection.prepareStatement(textQuery);
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
     * Load a Map of vocabulary from the specified directory
     */
    private Map<String, Vocabulary> loadVocabulary(File vocabDirectory, boolean fillAnchor) {
        Map<String, Vocabulary> result = new HashMap<String, Vocabulary>();
        if (vocabDirectory.isDirectory()) {
            if (vocabDirectory.listFiles().length == 0) {
                logger.severe("the vocabulary folder is empty :" + vocabDirectory.getPath());
            }
            for (File f : vocabDirectory.listFiles()) {
                if (f.getName().startsWith("SDN.") && f.getName().endsWith(".xml")) {
                    try {
                        
                        Object obj      = unmarshaller.unmarshal(f);
                        Vocabulary voca = null;
                        if (obj instanceof CodeTableType) {
                            CodeTableType ct = (CodeTableType) obj;
                            voca = new Vocabulary(ct.getListVersion(), ct.getListLongName(), ct.getListLastMod());

                            File skosFile = new File(f.getPath().replace("xml", "rdf"));
                            if (skosFile.exists()) {
                                RDF rdf = (RDF) unmarshaller.unmarshal(skosFile);
                                if (fillAnchor)
                                    rdf.fillAnchors();
                                voca.setMap(rdf.getShortMap());
                            } else {
                                logger.severe("no skos file found for vocabulary file : " + f.getName());
                            }

                        } else if (obj instanceof Vocabulary) {
                            voca = (Vocabulary) obj;
                            voca.fillMap();
                            String vocaName = f.getName();
                            vocaName = vocaName.substring(vocaName.indexOf("SDN.") + 4);
                            vocaName = vocaName.substring(0, vocaName.indexOf('.'));
                        } else {
                            logger.severe("Unexpected vocabulary file type for file: " + f.getName());
                        }
                        
                        if (voca != null) {
                            String vocaName = f.getName();
                            vocaName = vocaName.substring(vocaName.indexOf("SDN.") + 4);
                            vocaName = vocaName.substring(0, vocaName.indexOf('.'));
                            result.put(vocaName, voca);
                             //info part (debug) 
                            String report = "added vocabulary: " + vocaName + " with ";
                            report += voca.getMap().size() + " entries";
                            logger.finer(report);
                        }
                    } catch (JAXBException ex) {
                        logger.severe("Unable to unmarshall the vocabulary configuration file : " + f.getPath());
                        ex.printStackTrace();
                    }
                } else if (!f.getName().endsWith(".rdf")){
                    logger.severe("Vocabulary file : " + f.getPath() + " does not follow the pattern 'SDN.<vocabName>...'");
                }
            }
        } else {
            logger.severe("There is nor vocabulary directory: " + vocabDirectory.getPath());
        }
        return result;
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
        } else {
            result = new ResponsiblePartyImpl(result);
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
    private void loadData(String identifier) {
        singleValue.clear();
        multipleValue.clear();
        for (PreparedStatement stmt : singleStatements.keySet()) {
            try {
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
            } catch (SQLException ex) {
                String varlist = "";
                for (String s : singleStatements.get(stmt)) {
                    varlist += s + ',';
                }
                logger.severe("SQLException while executing single query: " + ex.getMessage() + '\n' +
                              "for variable: " + varlist);
                
            }
        }
        
        for (PreparedStatement stmt : multipleStatements.keySet()) {
            try {
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
            } catch (SQLException ex) {
                String varlist = "";
                List<String> varList = singleStatements.get(stmt);
                if (varList != null) {
                    for (String s : varList) {
                        varlist += s + ',';
                    }
                } else {
                  varlist = "no variables"; 
                }
                logger.severe("SQLException while executing multiple query: " + ex.getMessage() + '\n' +
                              "for variable: " + varlist);
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
     * @throws WebServiceException
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
    
    private MetaDataImpl getMetadataObject(String identifier, ElementSetType type, List<QName> elementName) {
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
    private MetaDataImpl getCDIMetadata(String identifier) {
        
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
        if (author != null)
            author.setRole(Role.AUTHOR);
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
        citation.setTitle(getInternationalStringVariable("var04"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var05")));
        
        CitationDateImpl revisionDate = createRevisionDate(getVariable("var06"));
        citation.setDates(Arrays.asList(revisionDate));
        
        List<ResponsiblePartyImpl> originators = new ArrayList<ResponsiblePartyImpl>();
        for (String contactID : getVariables("var07")) {
            ResponsiblePartyImpl originator  = getContact(contactID);
            if (originator != null) {
                originator.setRole(Role.ORIGINATOR);
                originators.add(originator);
            }
        }
        citation.setCitedResponsibleParties(originators);
        
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(getInternationalStringVariable("var08"));
        
        ResponsiblePartyImpl custodian   = getContact(getVariable("var09"));
        if (custodian != null)
            custodian.setRole(Role.CUSTODIAN);
        
        dataIdentification.setPointOfContacts(Arrays.asList(custodian));

        /*
         * keywords
         */  
        List<KeywordsImpl> keywords = new ArrayList<KeywordsImpl>();
        
        //parameter
        KeywordsImpl keyword = createKeyword(getVariables("var10"), "parameter", "P021");
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
        
        ResponsiblePartyImpl distributorContact   = getContact(getVariable("var36"));
        if (distributorContact != null)
            distributorContact.setRole(Role.DISTRIBUTOR);
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
    
    /**
     * Extract a metadata from a CSR database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    private MetaDataImpl getCSRMetadata(String identifier) {
        
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
         * creation date
         */ 
        result.setDateStamp(new Date());
        
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
        citation.setTitle(getInternationalStringVariable("var02"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var03")));
        
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
        List<KeywordsImpl> keywords = new ArrayList<KeywordsImpl>();
        
        //port of departure
        KeywordsImpl keyword = createKeyword(Arrays.asList(getVariable("var12")), "departure_place", "C381");
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
        citation.setTitle(getInternationalStringVariable("var31"));
        
        revisionDate = createRevisionDate(getVariable("var32"));
        citation.setDates(Arrays.asList(revisionDate));

        //principal investigator
        contact   = getContact(getVariable("var34"));
        contact.setRole(Role.PRINCIPAL_INVESTIGATOR);
        contact.setIndividualName(getVariable("var33"));
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
        contact   = getContact(getVariable("var40"));
        contact.setRole(Role.PRINCIPAL_INVESTIGATOR);
        contact.setIndividualName(getVariable("var39"));
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
    
     /**
     * Extract a metadata from a EDMED database.
     * 
     * @param identifier
     * 
     * @return
     * @throws java.sql.SQLException
     */
    private MetaDataImpl getEDMEDMetadata(String identifier)  {
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
         * creation date
         */ 
        result.setDateStamp(new Date());
        
        /*
         * extension information TODO
         */
        
        /**
         * Data identification
         */
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();

        CitationImpl citation = new CitationImpl();
        citation.setTitle(getInternationalStringVariable("var02"));
        citation.setAlternateTitles(Arrays.asList(getInternationalStringVariable("var03")));
        CitationDateImpl revisionDate = createRevisionDate(getVariable("var04"));
        citation.setDates(Arrays.asList(revisionDate));
        contact = getContact(getVariable("var05"));
        contact.setRole(Role.ORIGINATOR);
        citation.setCitedResponsibleParties(Arrays.asList(contact));
        dataIdentification.setCitation(citation);
        
        dataIdentification.setAbstract(getInternationalStringVariable("var06")); 
        dataIdentification.setPurpose(getInternationalStringVariable("var07"));
        
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
        KeywordsImpl keyword = createKeyword(getVariables("var11"), "place", "C16");
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
        citation.setTitle(getInternationalStringVariable("var25"));
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
     * Avoid the IllegalArgumentException when the variable value is null.
     * 
     */
    private InternationalString getInternationalStringVariable(String variable) {
        String value = getVariable(variable);
        if (value != null)
            return new SimpleInternationalString(value);
        else return null;
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
        contact.setOrganisationName(new SimpleInternationalString(org.getName()));
        try {
            URI uri = new URI("SDN:EDMO::" + org.getN_code());
            Anchors.create(org.getName(), uri); 
        } catch (URISyntaxException ex) {
            logger.severe("URI syntax exeption while adding contact code.");
        }
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
        if (org.getEmail() != null) {
            address.setElectronicMailAddresses(Arrays.asList(org.getEmail()));
        }
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
        if (date == null)
            return null;
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
        if (w == null || e == null || s == null || n == null) {
            logger.severe("One or more extent coordinates are null");
            return result;
        }
        if (!(w.size() == e.size() &&  e.size() == s.size() && s.size() == n.size())) {
            logger.severe("There is not the same number of geographic extent coordinates");
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
    
    /**
     * TODO recuperer les elements du thesaurus citation a partir d'uin objet vocabulary.
     * 
     * @param values
     * @param keywordType
     * @param title
     * @param altTitle
     * @param revDate
     * @param editionNumber
     * @param vocabulary
     * @return
     */
    private KeywordsImpl createKeyword(List<String> values, String keywordType, String altTitle) {

        //we try to get the vaocabulary Map.
        Vocabulary voca = vocabularies.get(altTitle);
        Map<String, String> vocaMap = null;
        if (voca == null) {
            logger.info("No vocabulary found for code: " + altTitle);
        } else {
            vocaMap = voca.getMap();
        }
        
        KeywordsImpl keyword = new KeywordsImpl();
        List<InternationalString> kws = new ArrayList<InternationalString>();
        for (String value: values) {
            if (vocaMap != null) {
                String mappedValue = vocaMap.get(value);
                if (mappedValue != null)
                    value = mappedValue;
            }
            if (value != null) {
                kws.add(new SimpleInternationalString(value));
            } else {
                logger.severe("keywords value null");
            }
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf(keywordType));
        
        //we create the citation describing the vocabulary used
        if (voca != null) {
            CitationImpl citation = new CitationImpl();
            citation.setTitle(new SimpleInternationalString(voca.getTitle()));
            citation.setAlternateTitles(Arrays.asList(new SimpleInternationalString(altTitle)));
            CitationDateImpl revisionDate;
            if (voca.getDate() != null && !voca.getDate().equals("")) {
                revisionDate = createRevisionDate(voca.getDate());
            } else {
                revisionDate = new CitationDateImpl(null, DateType.REVISION); 
            }
            citation.setDates(Arrays.asList(revisionDate));
            if (voca.getVersion() != null && !voca.getVersion().equals(""))
                citation.setEdition(new SimpleInternationalString(voca.getVersion()));
            citation.setIdentifiers(Arrays.asList(new IdentifierImpl("http://www.seadatanet.org/urnurl/")));
            keyword.setThesaurusName(citation);
        }
        
        return keyword;
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
    
    /**
     * Return all the entries from the database.
     * 
     * @return
     * @throws java.sql.SQLException
     */
    public List<MetaDataImpl> getAllEntries() throws SQLException {
        List<MetaDataImpl> result = new ArrayList<MetaDataImpl>();
        Statement stmt = connection.createStatement();
        if (genericConfiguration.getQueries() != null           &&
            genericConfiguration.getQueries().getMain() != null &&
            genericConfiguration.getQueries().getMain().getQuery() != null) {
            Query mainQuery = genericConfiguration.getQueries().getMain().getQuery();
            ResultSet res = stmt.executeQuery(mainQuery.buildSQLQuery());
            while (res.next()) {
                result.add(getMetadataObject(res.getString(1), ElementSetType.FULL, null));
            }
            
        } else {
            logger.severe("The configuration file is malformed, unable to reach the main query");
        }
        return result;
    }
            
}
