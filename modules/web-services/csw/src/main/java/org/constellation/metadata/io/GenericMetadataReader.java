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
import java.util.ArrayList;
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
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// constellation dependencies
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.generic.Values;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Column;
import org.constellation.generic.database.MultiFixed;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.Single;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Geotoolkit dependencies
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;

//geoAPI dependencies
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;

/**
 * A database Reader using a generic configuration to request an unknown database.
 * 
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class GenericMetadataReader extends AbstractCSWMetadataReader {
    
    /**
     * An unmarshaller used for getting EDMO data.
     */
    protected final MarshallerPool marshallerPool;
    
    /**
     * A list of precompiled SQL request returning multiple value.
     */
    private Map<PreparedStatement, List<String>> statements;
    
    /**
     * A precompiled Statement requesting all The identifiers
     */
    private PreparedStatement mainStatement;
    
    /**
     * A map of the already retrieved contact from EDMO WS.
     */
    private Map<String, ResponsibleParty> contacts;
    
    /**
     * A flag mode indicating we are searching the database for contacts.
     */
    private static final int CONTACT = 10;

    /**
     * Shared Thread Pool for parralele execution
     */
    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    /**
     * A connection to the database.
     */
    private Connection connection;

    /**
     * The configuration Object
     */
    private final Automatic configuration;

    /**
     * A flag indicating that the service is trying to reconnect the database.
     */
    private boolean isReconnecting = false;

    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param configuration
     */
    public GenericMetadataReader(Automatic configuration) throws MetadataIoException {
        super(false, true);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new MetadataIoException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        if (configuration.getConfigurationDirectory() == null || !configuration.getConfigurationDirectory().exists()) {
            throw new MetadataIoException("The configuration file does not contains a configuration directory", NO_APPLICABLE_CODE);
        }
        this.configuration = configuration;
        try {
            this.connection  = db.getConnection();
            initStatement();
            marshallerPool   = new MarshallerPool(getJAXBContext());
            contacts         = loadContacts(new File(configuration.getConfigurationDirectory(), "contacts"));
        } catch (SQLException ex) {
            throw new MetadataIoException("SQLException while initializing the Generic reader:" + '\n' +
                    "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            throw new MetadataIoException("JAXBException while initializing the Generic reader:" + '\n' +
                    "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * Build a new Generic metadata reader and initialize the statement (with a flag for filling the Anchors).
     * @param configuration
     */
    public GenericMetadataReader(Automatic configuration, boolean fillAnchor) throws MetadataIoException {
        super(false, true);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new MetadataIoException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        if (configuration.getConfigurationDirectory() == null || !configuration.getConfigurationDirectory().exists()) {
            throw new MetadataIoException("The configuration file does not contains a configuration directory", NO_APPLICABLE_CODE);
        }
        this.configuration = configuration;
        try {
            this.connection  = db.getConnection();
            initStatement();
            contacts         = new HashMap<String, ResponsibleParty>();
            marshallerPool   = new MarshallerPool(getJAXBContext());
            contacts         = loadContacts(new File(configuration.getConfigurationDirectory(), "contacts"));
        } catch (SQLException ex) {
            throw new MetadataIoException("SQLException while initializing the Generic reader:" + '\n' +
                    "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            throw new MetadataIoException("JAXBException while initializing the Generic reader:" + '\n' +
                    "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    /**
     * Build a new Generic metadata reader for test purpose.
     *
     * it replace the SQL statement by specified datas.
     *
     * @param genericConfiguration
     */
    protected GenericMetadataReader(Automatic configuration, Map<String, ResponsibleParty> contacts) throws MetadataIoException {
        super(false, true);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        this.configuration = configuration;
        try {
            this.connection    = null;
            statements         = new HashMap<PreparedStatement, List<String>>();
            this.contacts      = contacts;
            marshallerPool     = new MarshallerPool(getJAXBContext());

        } catch (JAXBException ex) {
            throw new MetadataIoException("JAXBException while initializing the Generic reader:" + '\n' +
                    "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * Initialize the prepared statement build from the configuration file.
     * 
     * @throws java.sql.SQLException
     */
    private void initStatement() throws SQLException {
        // we initialize the main query
        if (configuration.getQueries() != null           &&
            configuration.getQueries().getMain() != null &&
            configuration.getQueries().getMain().getQuery() != null) {
            final Query mainQuery = configuration.getQueries().getMain().getQuery();
            mainStatement         = connection.prepareStatement(mainQuery.buildSQLQuery());
        } else {
            LOGGER.severe("The configuration file is malformed, unable to reach the main query");
        }
        
        //singleStatements      = new HashMap<PreparedStatement, List<String>>();
        statements            = new HashMap<PreparedStatement, List<String>>();
        final Queries queries = configuration.getQueries();
        if (queries != null) {
            
            // initialize the single statements
            final Single single = queries.getSingle();
            if (single != null) {
                for (Query query : single.getQuery()) {
                    final List<String> varNames = new ArrayList<String>();
                    if (query.getSelect() != null) {
                        for (Column col : query.getSelect().getCol()) {
                            varNames.add(col.getVar());
                        }
                    }
                    final String textQuery = query.buildSQLQuery();
                    LOGGER.finer("new Single query: " + textQuery);
                    final PreparedStatement stmt =  connection.prepareStatement(textQuery);
                    statements.put(stmt, varNames);
                }
            } else {
                LOGGER.severe("The configuration file is probably malformed, there is no single query.");
            }
            
            // initialize the multiple statements
            final MultiFixed multi = queries.getMultiFixed();
            if (multi != null) {
                for (Query query : multi.getQuery()) {
                    final List<String> varNames = new ArrayList<String>();
                    if (query.getSelect() != null) {
                        for (Column col : query.getSelect().getCol()) {
                            varNames.add(col.getVar());
                        }
                    }
                    final PreparedStatement stmt =  connection.prepareStatement(query.buildSQLQuery());
                    statements.put(stmt, varNames);
                }
            } else {
                LOGGER.severe("The configuration file is probably malformed, there is no single query.");
            }
        } else {
            LOGGER.severe("The configuration file is probably malformed, there is no queries part.");
        }
    }

    /**
     * Load a Map of contact from the specified directory
     */
    private Map<String, ResponsibleParty> loadContacts(File contactDirectory) {
        final Map<String, ResponsibleParty> results = new HashMap<String, ResponsibleParty>();
        if (contactDirectory.isDirectory()) {
            if (contactDirectory.listFiles().length == 0) {
                LOGGER.severe("the contacts folder is empty :" + contactDirectory.getPath());
            }
            for (File f : contactDirectory.listFiles()) {
                if (f.getName().startsWith("EDMO.") && f.getName().endsWith(".xml")) {
                    Unmarshaller unmarshaller = null;
                    try {
                        unmarshaller = marshallerPool.acquireUnmarshaller();
                        final Object obj = unmarshaller.unmarshal(f);
                        if (obj instanceof ResponsibleParty) {
                            final ResponsibleParty contact = (ResponsibleParty) obj;
                            String code = f.getName();
                            code = code.substring(code.indexOf("EDMO.") + 5, code.indexOf(".xml"));
                            results.put(code, contact);
                        }
                    } catch (JAXBException ex) {
                        LOGGER.severe("Unable to unmarshall the contact file : " + f.getPath());
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    } finally {
                        if (unmarshaller != null) {
                            marshallerPool.release(unmarshaller);
                        }
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
        if (contacts != null) {
            DefaultResponsibleParty result = (DefaultResponsibleParty)contacts.get(contactIdentifier);
            if (result != null) {
                result = new DefaultResponsibleParty(result);
                result.setRole(role);
            }
            return result;
        }
        return null;
    }
    
    /**
     * Retrieve a contact from the cache or from th EDMO WS if its hasn't yet been requested.
     *  
     * @param contactIdentifier
     * @return
     */
    protected ResponsibleParty getContact(String contactIdentifier, Role role, String individualName) {
        DefaultResponsibleParty result = (DefaultResponsibleParty)contacts.get(contactIdentifier);
        if (result != null) {
            result = new DefaultResponsibleParty(result);
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
        final ParameterMetaData meta = stmt.getParameterMetaData();
        final int nbParam = meta.getParameterCount();
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
    private PreparedStatement getStatementFromVar(String varName) {
        for (PreparedStatement stmt : statements.keySet()) {
            final List<String> vars = statements.get(stmt);
            if (vars.contains(varName))
                return stmt;
        }
        return null;
    }
    
    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    private Values loadData(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        LOGGER.finer("loading data for " + identifier);

        // we get a sub list of all the statement
        Set<PreparedStatement> subStmts;

        //for ISO mode we load all variables
        if (mode == ISO_19115) {
            subStmts = statements.keySet();

        } else {
            List<String> variables;
            if (mode == DUBLINCORE) {
                variables = getVariablesForDublinCore(type, elementName);
            } else if (mode == CONTACT) {
                variables = getVariablesForContact();
            } else {
                throw new IllegalArgumentException("unknow mode");
            }
            subStmts = new HashSet<PreparedStatement>();
            for (String var: variables) {
                PreparedStatement stmt = getStatementFromVar(var);
                if (stmt != null) {
                    if (!subStmts.contains(stmt))
                        subStmts.add(stmt);
                }  else {
                   LOGGER.severe("no statement found for variable: " + var);
                }
            }
        }

        final Values values;
        if (isThreadEnabled()) {
            values = paraleleLoading(identifier, subStmts);
        } else {
            values = sequentialLoading(identifier, subStmts);
        }
        return values;
    }

    /**
     * Try to reconnect to the database if the connection have been lost.
     * 
     * @throws org.constellation.ws.MetadataIoException
     */
    public void reloadConnection() throws MetadataIoException {
        if (!isReconnecting) {
            try {
               LOGGER.info("refreshing the connection");
               final BDD db    = configuration.getBdd();
               this.connection = db.getConnection();
               initStatement();
               isReconnecting = false;

            } catch(SQLException ex) {
                LOGGER.severe("SQLException while restarting the connection:" + ex);
                isReconnecting = false;
            }
        }
        throw new MetadataIoException("The database connection has been lost, the service is trying to reconnect", NO_APPLICABLE_CODE);
    }

    /**
     * Execute the list of single and multiple statement sequentially.
     *
     * @param identifier
     * @param subSingleStmts
     * @param subMultiStmts
     */
    private Values sequentialLoading(String identifier, Set<PreparedStatement> subStmts) throws MetadataIoException {
        final Values values = new Values();

        //we extract the values
        for (PreparedStatement stmt : subStmts) {
            try {
                fillStatement(stmt, identifier);
                fillValues(stmt, statements.get(stmt), values);
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 17008) {
                    reloadConnection();
                }
                logSqlError(statements.get(stmt), ex);
            }
        }
        return values;
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * Execute the list of single and multiple statement by pack of ten thread.
     *
     * @param identifier
     */
    private Values paraleleLoading(final String identifier, Set<PreparedStatement> subStmts) throws MetadataIoException {
        final Values values = new Values();
        
        //we extract the single values
        CompletionService cs = new BoundedCompletionService(this.pool, 5);
        for (final PreparedStatement stmt : subStmts) {
            cs.submit(new Callable() {

                @Override
                public Object call() throws MetadataIoException {
                    try {
                        fillStatement(stmt, identifier);
                        fillValues(stmt, statements.get(stmt), values);

                    } catch (SQLException ex) {
                        if (ex.getErrorCode() == 17008) {
                            reloadConnection();
                        }
                        logSqlError(statements.get(stmt), ex);

                    }
                    return null;
                }
            });
        }

        for (int i = 0; i < subStmts.size(); i++) {
            try {
                cs.take().get();
            } catch (InterruptedException ex) {
               LOGGER.severe("InterruptedException in parralele load data:" + '\n' + ex.getMessage());
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof MetadataIoException) {
                    throw (MetadataIoException) ex.getCause();
                } else {
                    LOGGER.severe("ExecutionException in parralele load data:" + '\n' + ex.getMessage());
                }
            }
        }
        return values;
    }

    private void fillValues(PreparedStatement stmt, List<String> varNames, Values values) throws SQLException {
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            for (String varName : varNames) {
                values.addToValue(varName, result.getString(varName));
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
        final StringBuilder varlist = new StringBuilder();
        if (varList != null) {
            for (String s : varList) {
                varlist.append(s).append(',');
            }
        } else {
            varlist.append("no variables");
        }
        LOGGER.severe("SQLException while executing query: " + ex.getMessage() + '\n' +
                      "for variable: " + varlist.toString());
    }
    
    /**
     * Return a new Metadata object read from the database for the specified identifier.
     *  
     * @param identifier An unique identifier
     * @param mode An output schema mode: ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotoolkit metadata)
     * 
     * @throws java.sql.SQLException
     * @throws MetadataIoException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        
        //TODO we verify that the identifier exists
        final Values values = loadData(identifier, mode, type, elementName);

        final Object result;
        if (mode == ISO_19115) {
            result = getISO(identifier, values);
            
        } else if (mode == DUBLINCORE) {
            result = getDublinCore(identifier, type, elementName, values);
            
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
    protected abstract AbstractRecordType getDublinCore(String identifier, ElementSetType type, List<QName> elementName, Values values);
    
    /**
     * return a metadata in ISO representation.
     * 
     * @param identifier
     * @return
     */
    protected abstract DefaultMetadata getISO(String identifier, Values values);
    
    /**
     * Return a list of variables name used for the dublicore representation.
     * @return
     */
    protected abstract List<String> getVariablesForDublinCore(ElementSetType type, List<QName> elementName);
       
    /**
     * Return a list of contact id used in this database.
     */
    public abstract List<String> getVariablesForContact();

    /**
     * Return a list of package ':' separated use to create JAXBContext for the unmarshaller.
     */
    protected abstract Class[] getJAXBContext();
    
    /**
     * Return all the entries from the database.
     * 
     * @return
     * @throws java.sql.SQLException
     */
    @Override
    public List<DefaultMetadata> getAllEntries() throws MetadataIoException {
        final List<DefaultMetadata> result = new ArrayList<DefaultMetadata>();
        final List<String> identifiers  = getAllIdentifiers();
        for (String id : identifiers) {
            result.add((DefaultMetadata) getMetadata(id, ISO_19115, ElementSetType.FULL, null));
        }
        return result;
    }
    
    /**
     * Return all the identifiers in this database.
     * 
     * @return
     */
    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        final List<String> result = new ArrayList<String>();
        try {
            final ResultSet res = mainStatement.executeQuery();
            while (res.next()) {
                result.add(res.getString(1));
            }
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while getting all the identifiers: " + ex.getMessage(), NO_APPLICABLE_CODE);
        }
        return result;
    }
    
    /**
     * Return all the contact identifiers used in this database
     * 
     * @return
     * @throws org.constellation.ws.MetadataIoException
     */
    public List<String> getAllContactID(Values values) throws MetadataIoException {
        final List<String> results = new ArrayList<String>();
        final List<String> identifiers = getAllIdentifiers();
        for (String id : identifiers) {
            loadData(id, CONTACT, null, null);
            for(String var: getVariablesForContact()) {
                final String contactID = values.getVariable(var);
                if (contactID == null) {
                    final List<String> contactIDs = values.getVariables(var);
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
    
    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException {
         throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public List<String> executeEbrimSQLQuery(String sqlQuery) throws MetadataIoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * close all the statements and clear the maps.
     */
    @Override
    public void destroy() {
        try {
            for (PreparedStatement stmt : statements.keySet()) {
                stmt.close();
            }
            statements.clear();

            if (mainStatement != null) {
                mainStatement.close();
                mainStatement = null;
            }
            connection.close();
            pool.shutdown();
        } catch (SQLException ex) {
            LOGGER.severe("SQLException while destroying Generic metadata reader");
        }
        contacts.clear();
        LOGGER.info("destroying generic metadata reader");
    }
            
}
