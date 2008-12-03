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
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.constellation.cat.csw.v202.AbstractRecordType;
import org.constellation.cat.csw.v202.DomainValuesType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.ws.WebServiceException;
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
import org.constellation.ows.v100.BoundingBoxType;
import org.constellation.skos.RDF;
import org.constellation.ws.rs.WebService;
import org.geotools.metadata.iso.ExtendedElementInformationImpl;
import org.geotools.metadata.iso.IdentifierImpl;

// Geotools dependencies
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.citation.AddressImpl;
import org.geotools.metadata.iso.citation.CitationDateImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.ContactImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.citation.TelephoneImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.metadata.iso.identification.KeywordsImpl;
import org.geotools.metadata.note.Anchors;
import org.geotools.util.SimpleInternationalString;

//geoAPI dependencies
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.util.InternationalString;
import org.opengis.metadata.Datatype;
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.Keywords;

/**
 * A database Reader using a generic configuration to request an unknown database.
 * 
 * TODO regarder les cardinalite est mettre des null la ou 0-...
 *
 * @author Guilhem Legal
 */
public abstract class GenericMetadataReader extends MetadataReader {
    
    /**
     * A configuration object used in Generic database mode.
     */
    private Automatic genericConfiguration;
    
    /**
     * A date Formater.
     */
    protected static  List<DateFormat> dateFormats;
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
    private Map<String, ResponsibleParty> contacts;
    
    /**
     * A map making the correspondance between parameter code and the real keyword value.
     * this map is fill from a list of configuration file P021.xml, L05.xml, ..
     */
    protected Map<String, Vocabulary> vocabularies;
    
    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param genericConfiguration
     */
    public GenericMetadataReader(Automatic genericConfiguration, Connection connection) throws SQLException, JAXBException {
        super(false);
        this.genericConfiguration = genericConfiguration;
        this.connection     = connection;
        initStatement();
        singleValue            = new HashMap<String, String>();
        multipleValue          = new HashMap<String, List<String>>();
        contacts               = new HashMap<String, ResponsibleParty>();
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
        super(false);
        this.genericConfiguration = genericConfiguration;
        this.connection     = connection;
        initStatement();
        singleValue            = new HashMap<String, String>();
        multipleValue          = new HashMap<String, List<String>>();
        contacts               = new HashMap<String, ResponsibleParty>();
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
    protected ResponsibleParty getContact(String contactIdentifier, Role role) {
        ResponsiblePartyImpl result = (ResponsiblePartyImpl)contacts.get(contactIdentifier);
        if (result == null) {
            result = (ResponsiblePartyImpl) loadContactFromEDMOWS(contactIdentifier);
            if (result != null)
                contacts.put(contactIdentifier, result);
        } else {
            result = new ResponsiblePartyImpl(result);
        }
        if (result != null)
            result.setRole(role);
        return result;
    }
    
    /**
     * Retrieve a contact from the cache or from th EDMO WS if its hasn't yet been requested.
     *  
     * @param contactIdentifier
     * @return
     */
    protected ResponsibleParty getContact(String contactIdentifier, Role role, String individualName) {
        ResponsiblePartyImpl result = (ResponsiblePartyImpl)contacts.get(contactIdentifier);
        if (result == null) {
            result = (ResponsiblePartyImpl) loadContactFromEDMOWS(contactIdentifier);
            if (result != null)
                contacts.put(contactIdentifier, result);
        } else {
            result = new ResponsiblePartyImpl(result);
        }
        if (result != null) {
            result.setRole(role);
            result.setIndividualName(individualName);
        }
        return result;
    }
            
    /**
     * Try to get a contact from EDMO WS and add it to the map of contact.
     * 
     * @param contactIdentifiers
     */
    private ResponsibleParty loadContactFromEDMOWS(String contactID) {
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
                        logger.severe("There is no organisation for the specified code: " + contactID);
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
        logger.finer("loading data for " + identifier);
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
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) {
        Object result = null;
        
        //TODO we verify that the identifier exists
        loadData(identifier);
        
        if (mode == ISO_19115) {
            result = getISO(identifier);
            
        } else if (mode == DUBLINCORE) {
            result = getDublinCore(identifier, type, elementName);
            
        } else {
            throw new IllegalArgumentException("Unknow or unAuthorized standard mode: " + mode);
        }
        return result;
    }
    
    protected abstract AbstractRecordType getDublinCore(String identifier, ElementSetType type, List<QName> elementName);
    
    protected abstract MetaDataImpl getISO(String identifier);
    /**
     * return a list of value for the specified variable name.
     * 
     * @param variables
     * @return
     */
    protected List<String> getVariables(String variable) {
        return multipleValue.get(variable);
    }
    
    /**
     * return the value for the specified variable name.
     * 
     * @param variable
     * @return
     */
    protected String getVariable(String variable) {
        return singleValue.get(variable);
    }
    
    /**
     * Avoid the IllegalArgumentException when the variable value is null.
     * 
     */
    protected InternationalString getInternationalStringVariable(String variable) {
        String value = getVariable(variable);
        if (value != null)
            return new SimpleInternationalString(value);
        else return null;
    }
    
    /**
     * Build a new Responsible party with the specified Organisation object retrieved from EDMO WS.
     * 
     * @param org
     * @return
     * @throws java.sql.SQLException
     */
    private ResponsibleParty createContact(Organisation org) {
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
    protected CitationDate createRevisionDate(String date) {
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
    protected CitationDate createPublicationDate(String date) {
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
    protected Date parseDate(String date) {
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
    protected List<GeographicExtent> createGeographicExtent(String westVar, String eastVar, String southVar, String northVar) {
        List<GeographicExtent> result = new ArrayList<GeographicExtent>();
        
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
            GeographicExtent geo = new GeographicBoundingBoxImpl(west, east, south, north);
            result.add(geo);
        }
        return result;
    }
    
    /**
     * 
     * @param westVar
     * @param eastVar
     * @param southVar
     * @param northVar
     * @return
     */
    protected List<BoundingBoxType> createBoundingBoxes(String westVar, String eastVar, String southVar, String northVar) {
        List<BoundingBoxType> result = new ArrayList<BoundingBoxType>();
        
        List<String> w = getVariables(westVar);
        List<String> e = getVariables(eastVar);
        List<String> s = getVariables(southVar);
        List<String> n = getVariables(northVar);
        if (w == null || e == null || s == null || n == null) {
            logger.severe("One or more BBOX coordinates are null");
            return result;
        }
        if (!(w.size() == e.size() &&  e.size() == s.size() && s.size() == n.size())) {
            logger.severe("There is not the same number of geographic BBOX coordinates");
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
            //TODO CRS
            BoundingBoxType bbox = new BoundingBoxType("EPSG:4326", west, south, east, north);
            result.add(bbox);
        }
        return result;
    }
    
    /**
     * 
     */
    protected List<String> getKeywordsValue(List<String> values, String altTitle) {
        
        //we try to get the vocabulary Map.
        Vocabulary voca = vocabularies.get(altTitle);
        Map<String, String> vocaMap = null;
        if (voca == null) {
            logger.info("No vocabulary found for code: " + altTitle);
        } else {
            vocaMap = voca.getMap();
        }
        
        List<String> result = new ArrayList<String>();
        for (String value: values) {
            if (vocaMap != null) {
                String mappedValue = vocaMap.get(value);
                if (mappedValue != null)
                    value = mappedValue;
            }
            if (value != null) {
                result.add(value);
            } else {
                logger.severe("keywords value null");
            }
        }
        return result;
    }
    
    /**
     * 
     * @param values
     * @param keywordType
     * @param altTitle
     * @return
     */
    protected Keywords createKeyword(List<String> values, String keywordType, String altTitle) {

        //we try to get the vocabulary Map.
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
            CitationDate revisionDate;
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
    
    protected ExtendedElementInformation createExtensionInfo(String name) {
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
                result.add((MetaDataImpl)getMetadata(res.getString(1), ISO_19115, ElementSetType.FULL, null));
            }
            
        } else {
            logger.severe("The configuration file is malformed, unable to reach the main query");
        }
        return result;
    }
    
    public List<DomainValuesType> getFieldDomainofValues(String propertyNames) throws WebServiceException {
         throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public List<String> executeEbrimSQLQuery(String SQLQuery) throws WebServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void destroy() {
        try {
            for (PreparedStatement stmt : singleStatements.keySet()) {
                stmt.close();
            }
            singleStatements.clear();

            for (PreparedStatement stmt : multipleStatements.keySet()) {
                stmt.close();
            }
            multipleStatements.clear();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            logger.severe("SQLException while destroying Generic metadata reader");
        }
        singleValue.clear();
        multipleValue.clear();
        vocabularies.clear();
        contacts.clear();
        logger.info("destroying generic reader");
    }
            
}
