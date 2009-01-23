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
import java.io.File;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// constellation dependencies
import org.constellation.cat.csw.v202.AbstractRecordType;
import org.constellation.cat.csw.v202.DomainValuesType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.ws.CstlServiceException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.Column;
import org.constellation.generic.database.MultiFixed;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.Single;
import org.constellation.ows.v100.BoundingBoxType;
import static org.constellation.ows.OWSExceptionCode.*;

// Geotools dependencies
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.citation.CitationDateImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.util.SimpleInternationalString;

//geoAPI dependencies
import org.opengis.metadata.citation.DateType;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.extent.GeographicExtent;

/**
 * A database Reader using a generic configuration to request an unknown database.
 * 
 * TODO regarder les cardinalite est mettre des null la ou 0-...
 *
 * @author Guilhem Legal
 */
public abstract class GenericMetadataReader extends MetadataReader {
    
    /**
     * A date Formater.
     */
    protected static  List<DateFormat> dateFormats;
    static {
        dateFormats = new ArrayList<DateFormat>();
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
        dateFormats.add(new SimpleDateFormat("yyyy"));
    }
    
    /**
     * An unmarshaller used for getting EDMO data.
     */
    protected final Unmarshaller unmarshaller;
    
    /**
     * A list of precompiled SQL request returning single value.
     */
    private Map<PreparedStatement, List<String>> singleStatements;
    
    /**
     * A list of precompiled SQL request returning multiple value.
     */
    private Map<PreparedStatement, List<String>> multipleStatements;
    
    /**
     * A precompiled Statement requesting all The identifiers
     */
    private PreparedStatement mainStatement;
    
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
     * A flag mode indicating we are searching the database for contacts.
     */
    private static int CONTACT = 10;

    /**
     * Shared Thread Pool for parralele execution
     */
    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    /**
     * A connection to the database.
     */
    private final Connection connection;
    
    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param genericConfiguration
     */
    public GenericMetadataReader(Automatic configuration, Connection connection, File configDir) throws SQLException, JAXBException {
        super(false, true);
        this.connection        = connection;
        initStatement(configuration);
        singleValue            = new HashMap<String, String>();
        multipleValue          = new HashMap<String, List<String>>();
        JAXBContext context    = JAXBContext.newInstance(getJAXBContext());
        unmarshaller           = context.createUnmarshaller();
        contacts               = loadContacts(new File(configDir, "contacts"));
    }
    
    /**
     * Build a new Generic metadata reader and initialize the statement (with a flag for filling the Anchors).
     * @param genericConfiguration
     */
    public GenericMetadataReader(Automatic configuration, Connection connection, File configDir, boolean fillAnchor) throws SQLException, JAXBException {
        super(false, true);
        this.connection        = connection;
        initStatement(configuration);
        singleValue            = new HashMap<String, String>();
        multipleValue          = new HashMap<String, List<String>>();
        contacts               = new HashMap<String, ResponsibleParty>();
        JAXBContext context    = JAXBContext.newInstance(getJAXBContext());
        unmarshaller           = context.createUnmarshaller();
        contacts               = loadContacts(new File(configDir, "contacts"));
    }
    
    /**
     * Initialize the prepared statement build from the configuration file.
     * 
     * @throws java.sql.SQLException
     */
    private final void initStatement(Automatic configuration) throws SQLException {
        // we initialize the main query
        if (configuration.getQueries() != null           &&
            configuration.getQueries().getMain() != null &&
            configuration.getQueries().getMain().getQuery() != null) {
            Query mainQuery    = configuration.getQueries().getMain().getQuery();
            mainStatement      = connection.prepareStatement(mainQuery.buildSQLQuery());
        } else {
            logger.severe("The configuration file is malformed, unable to reach the main query");
        }
        
        singleStatements   = new HashMap<PreparedStatement, List<String>>();
        multipleStatements = new HashMap<PreparedStatement, List<String>>();
        Queries queries = configuration.getQueries();
        if (queries != null) {
            
            // initialize the single statements
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
            } else {
                logger.severe("The configuration file is probably malformed, there is no single query.");
            }
            
            // initialize the multiple statements
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
            } else {
                logger.severe("The configuration file is probably malformed, there is no single query.");
            }
        } else {
            logger.severe("The configuration file is probably malformed, there is no queries part.");
        }
    }
    
    /**
     * Load a Map of contact from the specified directory
     */
    private Map<String, ResponsibleParty> loadContacts(File contactDirectory) {
        Map<String, ResponsibleParty> results = new HashMap<String, ResponsibleParty>();
        if (contactDirectory.isDirectory()) {
            if (contactDirectory.listFiles().length == 0) {
                logger.severe("the contacts folder is empty :" + contactDirectory.getPath());
            }
            for (File f : contactDirectory.listFiles()) {
                if (f.getName().startsWith("EDMO.") && f.getName().endsWith(".xml")) {
                    try {
                        Object obj = unmarshaller.unmarshal(f);
                        if (obj instanceof ResponsibleParty) {
                            ResponsibleParty contact = (ResponsibleParty) obj;
                            String code = f.getName();
                            code = code.substring(code.indexOf("EDMO.") + 5, code.indexOf(".xml"));
                            results.put(code, contact);
                        }
                    } catch (JAXBException ex) {
                        logger.severe("Unable to unmarshall the contact file : " + f.getPath());
                        ex.printStackTrace();
                    }
                }
            }
        }
        return results;
    }
    
    /**
     * Retrieve a contact from the cache or from th EDMO WS if its hasn't yet been requested.
     *  
     * @param contactIdentifier
     * @return
     */
    protected ResponsibleParty getContact(String contactIdentifier, Role role) {
        ResponsiblePartyImpl result = (ResponsiblePartyImpl)contacts.get(contactIdentifier);
        if (result != null) {
            result = new ResponsiblePartyImpl(result);
            result.setRole(role);
        }
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
        if (result != null) {
            result = new ResponsiblePartyImpl(result);
            result.setRole(role);
            if (individualName != null)
                result.setIndividualName(individualName);
        }
        return result;
    }
            
    /**
     * Fill the dynamic parameters of a prepared statement with the specified identifier.
     * @param stmt
     * @param identifier
     */
    private void fillStatement(PreparedStatement stmt, String identifier) throws SQLException {
        ParameterMetaData meta = stmt.getParameterMetaData();
        int nbParam = meta.getParameterCount();
        int i = 1;
        while (i < nbParam + 1) {
            stmt.setString(i, identifier);
            i++;
        }
    }
    
    /**
     * Return the correspounding statement for the specified variable name.
     * 
     * @param varName
     * @return
     */
    private PreparedStatement getStatementFromSingleVar(String varName) {
        for (PreparedStatement stmt : singleStatements.keySet()) {
            List<String> vars = singleStatements.get(stmt);
            if (vars.contains(varName))
                return stmt;
        }
        return null;
    }
    
     /**
     * Return the correspounding statement for the specified variable name.
     * 
     * @param varName
     * @return
     */
    private PreparedStatement getStatementFromMultipleVar(String varName) {
        for (PreparedStatement stmt : multipleStatements.keySet()) {
            List<String> vars = multipleStatements.get(stmt);
            if (vars.contains(varName))
                return stmt;
        }
        return null;
    }
    
    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    private void loadData(String identifier, int mode, ElementSetType type) {
        logger.finer("loading data for " + identifier);
        singleValue.clear();
        multipleValue.clear();
        
        // we get a sub list of all the statement
        Set<PreparedStatement> subSingleStmts;
        Set<PreparedStatement> subMultiStmts;

        //for ISO mode we load all variables
        if (mode == ISO_19115) {
            subSingleStmts = singleStatements.keySet();
            subMultiStmts  = multipleStatements.keySet();
        } else {
            List<String> variables;
            if (mode == DUBLINCORE) {
                variables = getVariablesForDublinCore(type);
            } else if (mode == CONTACT) {
                variables = getVariablesForContact();
            } else {
                throw new IllegalArgumentException("unknow mode");
            }
            subSingleStmts = new HashSet<PreparedStatement>();
            subMultiStmts  = new HashSet<PreparedStatement>();
            for (String var: variables) {
                PreparedStatement stmt = getStatementFromSingleVar(var);
                if (stmt != null) {
                    if (!subSingleStmts.contains(stmt))
                        subSingleStmts.add(stmt);
                } else {
                    stmt = getStatementFromMultipleVar(var);
                    if (stmt != null) {
                        if (!subMultiStmts.contains(stmt))
                            subMultiStmts.add(stmt);
                    } else {
                        logger.severe("no statement found for variable: " + var);
                    }
                }
            }
        }
        if (isThreadEnabled())
            paraleleLoading(identifier, subSingleStmts, subMultiStmts);
        else
            sequentialLoading(identifier, subSingleStmts, subMultiStmts);
    }

    /**
     * Execute the list of single and multiple statement sequentially.
     *
     * @param identifier
     * @param subSingleStmts
     * @param subMultiStmts
     */
    private void sequentialLoading(String identifier, Set<PreparedStatement> subSingleStmts, Set<PreparedStatement> subMultiStmts) {
        //we extract the single values
        for (PreparedStatement stmt : subSingleStmts) {
            try {
                fillStatement(stmt, identifier);
                fillSingleValues(stmt);
            } catch (SQLException ex) {
                logSqlError(singleStatements.get(stmt), ex);
            }
        }

        //we extract the multiple values
        for (PreparedStatement stmt : subMultiStmts) {
            try {
                fillStatement(stmt, identifier);
                fillMultipleValues(stmt);
            } catch (SQLException ex) {
                logSqlError(multipleStatements.get(stmt), ex);
            }

        }
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * Execute the list of single and multiple statement by pack of ten thread.
     *
     * @param identifier
     */
    private void paraleleLoading(final String identifier, Set<PreparedStatement> subSingleStmts, Set<PreparedStatement> subMultiStmts) {
        //we extract the single values
        CompletionService cs = new BoundedCompletionService(this.pool, 5);
        for (final PreparedStatement stmt : subSingleStmts) {
            cs.submit(new Callable() {

                public Object call() {
                    try {
                        fillStatement(stmt, identifier);
                        fillSingleValues(stmt);

                    } catch (SQLException ex) {
                        logSqlError(singleStatements.get(stmt), ex);

                    }
                    return null;
                }
            });
        }

        for (int i = 0; i < subSingleStmts.size(); i++) {
            try {
                cs.take().get();
            } catch (InterruptedException ex) {
               logger.severe("InterruptedException in parralele load data:" + '\n' + ex.getMessage());
            } catch (ExecutionException ex) {
               logger.severe("ExecutionException in parralele load data:" + '\n' + ex.getMessage());
            }
        }
        //we extract the multiple values
        cs = new BoundedCompletionService(this.pool, 5);
        for (final PreparedStatement stmt : subMultiStmts) {
            cs.submit(new Callable() {

                public Object call() {
                    try {
                        fillStatement(stmt, identifier);
                        fillMultipleValues(stmt);
                        
                    } catch (SQLException ex) {
                        logSqlError(multipleStatements.get(stmt), ex);
                    }
                    return null;
                }
            });
        }

        for (int i = 0; i < subMultiStmts.size(); i++) {
            try {
                cs.take().get();
            } catch (InterruptedException ex) {
               logger.severe("InterruptedException in parralele load data:" + '\n' + ex.getMessage());
            } catch (ExecutionException ex) {
               logger.severe("ExecutionException in parralele load data:" + '\n' + ex.getMessage());
            }
        }
    }

    private void fillSingleValues(PreparedStatement stmt) throws SQLException {
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            for (String varName : singleStatements.get(stmt)) {
                singleValue.put(varName, result.getString(varName));
            }
        }
        result.close();
    }

    private void fillMultipleValues(PreparedStatement stmt) throws SQLException {
        ResultSet result = stmt.executeQuery();
        for (String varName : multipleStatements.get(stmt)) {
            multipleValue.put(varName, new ArrayList<String>());
        }
        while (result.next()) {
            for (String varName : multipleStatements.get(stmt)) {
                multipleValue.get(varName).add(result.getString(varName));
            }
        }
        result.close();
    }

    /**
     * Log the list of variables involved in a query which launch a SQL exception.
     * (debugging purpose).
     *
     * @param varList a list of variable.
     * @param ex
     */
    public void logSqlError(List<String> varList, SQLException ex) {
        String varlist = "";
        if (varList != null) {
            for (String s : varList) {
                varlist += s + ',';
            }
        } else {
            varlist = "no variables";
        }
        logger.severe("SQLException while executing query: " + ex.getMessage() + '\n' +
                      "for variable: " + varlist);
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
     * @throws CstlServiceException
     */
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) {
        Object result = null;
        
        //TODO we verify that the identifier exists
        loadData(identifier, mode, type);
        
        if (mode == ISO_19115) {
            result = getISO(identifier);
            
        } else if (mode == DUBLINCORE) {
            result = getDublinCore(identifier, type, elementName);
            
        } else {
            throw new IllegalArgumentException("Unknow or unAuthorized standard mode: " + mode);
        }
        return result;
    }
    
    /**
     * return a metadata in dublin core representation.
     * 
     * @param identifier
     * @param type
     * @param elementName
     * @return
     */
    protected abstract AbstractRecordType getDublinCore(String identifier, ElementSetType type, List<QName> elementName);
    
    /**
     * return a metadata in ISO representation.
     * 
     * @param identifier
     * @return
     */
    protected abstract MetaDataImpl getISO(String identifier);
    
    /**
     * Return a list of variables name used for the dublicore representation.
     * @return
     */
    protected abstract List<String> getVariablesForDublinCore(ElementSetType type);
       
    /**
     * Return a list of contact id used in this database.
     */
    public abstract List<String> getVariablesForContact();

    /**
     * Return a list of package ':' separated use to create JAXBContext for the unmarshaller.
     */
    protected abstract Class[] getJAXBContext();
    
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
        else logger.finer("revision date null: " + date);
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
        else logger.finer("publication date null: " + date);
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
            String westValue  = null; String eastValue  = null;
            String southValue = null; String northValue = null;
            try {
                westValue = w.get(i);
                if (westValue != null) {
                    if (westValue.indexOf(',') != -1) {
                        westValue = westValue.substring(0, westValue.indexOf(','));
                    }
                    west = Double.parseDouble(westValue);
                }
                eastValue = e.get(i);
                if (eastValue != null) {
                    if (eastValue.indexOf(',') != -1) {
                        eastValue = eastValue.substring(0, eastValue.indexOf(','));
                    }
                    east = Double.parseDouble(eastValue);
                }
                southValue = s.get(i);
                if (southValue != null) {
                    if (southValue.indexOf(',') != -1) {
                        southValue = southValue.substring(0, southValue.indexOf(','));
                    }
                    south = Double.parseDouble(southValue);
                }
                northValue = n.get(i);
                if (northValue != null) {
                    north = Double.parseDouble(northValue);
                }

                // for point BBOX we replace the westValue equals to 0 by the eastValue (respectively for  north/south)
                if (east == 0) {
                    east = west;
                }
                if (north == 0) {
                    north = south;
                }
            } catch (NumberFormatException ex) {
                logger.severe("Number format exception while parsing boundingBox: " + '\n' +
                        "current box: " + westValue + ',' + eastValue + ',' + southValue + ',' + northValue);
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
     * Return all the entries from the database.
     * 
     * @return
     * @throws java.sql.SQLException
     */
    public List<MetaDataImpl> getAllEntries() throws CstlServiceException {
        List<MetaDataImpl> result = new ArrayList<MetaDataImpl>();
        List<String> identifiers  = getAllIdentifiers();
        for (String id : identifiers) {
            result.add((MetaDataImpl) getMetadata(id, ISO_19115, ElementSetType.FULL, null));
        }
        return result;
    }
    
    /**
     * Return all the identifiers in this database.
     * 
     * @return
     */
    private List<String> getAllIdentifiers() throws CstlServiceException {
        List<String> result = new ArrayList<String>();
        try {
            ResultSet res = mainStatement.executeQuery();
            while (res.next()) {
                result.add(res.getString(1));
            }
        } catch (SQLException ex) {
            throw new CstlServiceException("SQL Exception while getting all the identifiers: " + ex.getMessage(), NO_APPLICABLE_CODE);
        }
        return result;
    }
    
    /**
     * Return all the contact identifiers used in this database
     * 
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public List<String> getAllContactID() throws CstlServiceException {
        List<String> results = new ArrayList<String>();
        List<String> identifiers = getAllIdentifiers();
        for (String id : identifiers) {
            loadData(id, CONTACT, null);
            for(String var: getVariablesForContact()) {
                String contactID = getVariable(var);
                if (contactID == null) {
                    List<String> contactIDs = getVariables(var);
                    if (contactIDs != null) {
                        for (String cID : contactIDs) {
                            if (!results.contains(cID))
                                results.add(cID);
                        }
                    }
                } else {
                    if (!results.contains(contactID))
                        results.add(contactID);
                }
            }
        }
        return results;
    }
    
    public List<DomainValuesType> getFieldDomainofValues(String propertyNames) throws CstlServiceException {
         throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public List<String> executeEbrimSQLQuery(String SQLQuery) throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * close all the statements and clear the maps.
     */
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

            if (mainStatement != null) {
                mainStatement.close();
                mainStatement = null;
            }
            connection.close();
            pool.shutdown();
        } catch (SQLException ex) {
            logger.severe("SQLException while destroying Generic metadata reader");
        }
        singleValue.clear();
        multipleValue.clear();
        contacts.clear();
        logger.info("destroying generic metadata reader");
    }
            
}
