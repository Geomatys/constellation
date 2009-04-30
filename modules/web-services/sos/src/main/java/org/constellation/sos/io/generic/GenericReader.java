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

package org.constellation.sos.io.generic;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

// constellation dependecies
import java.util.logging.Logger;
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Column;
import org.constellation.generic.database.MultiFixed;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.Single;
import org.constellation.ws.CstlServiceException;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class GenericReader  {

    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos");

    /**
     * A list of precompiled SQL request returning single value.
     */
    private Map<PreparedStatement, List<String>> singleStatements;

    /**
     * A list of precompiled SQL request returning multiple value.
     */
    private Map<PreparedStatement, List<String>> multipleStatements;

    /**
     * A map binding the statements and the string query used for debug purpose.
     * (because of the implementation of the SQL driver which don't implements the toString method (Oracle))
     */
    private Map<PreparedStatement, String> StringQueryMap;

    /**
     * A precompiled Statement requesting all The identifiers
     */
    private PreparedStatement mainStatement;

    /**
     * A flag indicating if the multi thread mecanism is enabled or not.
     */
    private final boolean isThreadEnabled;

    /**
     * A flag indicating if the jdbc driver support several specific operation
     * (the oracle driver does not support a lot of method for example).
     *
     */
    private boolean advancedJdbcDriver;
    
    /**
     * A flag indicating that the debug mode is ON.
     */
    private boolean debugMode = false;

    /**
     * A list of predefined Values used in debug mode
     */
    private Map<List<String>, Values> debugValues;

    /**
     *
     */
    private HashMap<String, String> staticParameters;

    /**
     * A flag indicating that the service is trying to reconnect the database.
     */
    private boolean isReconnecting = false;

    /**
     * The database informations.
     */
    private Automatic configuration;

     /**
     * A connection to the database.
     */
    private Connection connection;
    
    /**
     * Shared Thread Pool for parralele execution
     */
    private ExecutorService pool = Executors.newFixedThreadPool(6);

    public GenericReader(Automatic configuration) throws CstlServiceException {
        this.configuration = configuration;
        advancedJdbcDriver = true;
        try {
            BDD bdd = configuration.getBdd();
            if (bdd != null) {
                this.connection = bdd.getConnection();
                initStatement();
            } else {
                throw new CstlServiceException("The database par of the generic configuration file is null", NO_APPLICABLE_CODE);
            }
        } catch (SQLException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        } catch (IllegalArgumentException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
        isThreadEnabled = false;
    }

    protected GenericReader(Map<List<String>, Values> debugValues, HashMap<String, String> staticParameters) throws CstlServiceException {
        advancedJdbcDriver = true;
        debugMode          = true;
        isThreadEnabled    = false;
        this.debugValues   = debugValues;
        singleStatements   = new HashMap<PreparedStatement, List<String>>();
        multipleStatements = new HashMap<PreparedStatement, List<String>>();
        StringQueryMap     = new HashMap<PreparedStatement, String>();
        if (staticParameters != null) {
            this.staticParameters = staticParameters;
        } else {
            this.staticParameters = new HashMap<String, String>();
        }
    }

    /**
     * Initialize the prepared statement build from the configuration file.
     *
     * @throws java.sql.SQLException
     */
    private final void initStatement() throws SQLException {
        // no main query in sos
        singleStatements   = new HashMap<PreparedStatement, List<String>>();
        multipleStatements = new HashMap<PreparedStatement, List<String>>();
        StringQueryMap     = new HashMap<PreparedStatement, String>();
        Queries queries = configuration.getQueries();
        if (queries != null) {

            staticParameters = queries.getParameters();
            if (staticParameters == null) {
                staticParameters = new HashMap<String, String>();
            }
            
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
                    String textQuery = query.buildSQLQuery(staticParameters);
                    logger.finer("new Single query: " + textQuery);
                    PreparedStatement stmt =  connection.prepareStatement(textQuery);
                    singleStatements.put(stmt, varNames);
                    StringQueryMap.put(stmt, textQuery);
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
                    String textQuery = query.buildSQLQuery(staticParameters);
                    logger.finer("new Multiple query: " + textQuery);
                    PreparedStatement stmt =  connection.prepareStatement(textQuery);
                    multipleStatements.put(stmt, varNames);
                    StringQueryMap.put(stmt, textQuery);
                }
            } else {
                logger.severe("The configuration file is probably malformed, there is no single query.");
            }
        } else {
            logger.severe("The configuration file is probably malformed, there is no queries part.");
        }
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(String variable) throws CstlServiceException {
        return loadData(Arrays.asList(variable), new ArrayList<String>());
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(String variable, String parameter) throws CstlServiceException {
        return loadData(Arrays.asList(variable), Arrays.asList(parameter));
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(List<String> variables, String parameter) throws CstlServiceException {
        return loadData(variables, Arrays.asList(parameter));
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(String variable, List<String> parameter) throws CstlServiceException {
        return loadData(Arrays.asList(variable), parameter);
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(List<String> variables) throws CstlServiceException {
        return loadData(variables, new ArrayList<String>());
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(List<String> variables, List<String> parameters) throws CstlServiceException {

        Set<PreparedStatement> subSingleStmts = new HashSet<PreparedStatement>();
        Set<PreparedStatement> subMultiStmts = new HashSet<PreparedStatement>();
        Values values = null;
        for (String var : variables) {
            PreparedStatement stmt = getStatementFromSingleVar(var);
            if (stmt != null) {
                if (!subSingleStmts.contains(stmt)) {
                    subSingleStmts.add(stmt);
                }
            } else {
                stmt = getStatementFromMultipleVar(var);
                if (stmt != null) {
                    if (!subMultiStmts.contains(stmt)) {
                        subMultiStmts.add(stmt);
                    }
                } else {
                    if (staticParameters.get(var) != null) {
                        values = new Values();
                        values.singleValue.put(var, staticParameters.get(var));
                    }
                    if (!debugMode)
                        logger.severe("no statement found for variable: " + var);
                }
            }
        }
        if (values != null) {
            return values;
        }
        
        if (debugMode) {
            values = debugLoading(variables, parameters);
        } else if (isThreadEnabled) {
            values = paraleleLoading(parameters, subSingleStmts, subMultiStmts);
        } else {
            values = sequentialLoading(parameters, subSingleStmts, subMultiStmts);
        }
        return values;
    }


    /**
     * Load the data in debug mode without queying the database .
     *
     */
    private Values debugLoading(List<String> variables, List<String> parameters) {
        if (debugValues != null) {
            return debugValues.get(parameters);
        }
        return null;
    }

    /**
     * Execute the list of single and multiple statement sequentially.
     *
     * @param identifier
     * @param subSingleStmts
     * @param subMultiStmts
     */
    private Values sequentialLoading(List<String> parameters, Set<PreparedStatement> subSingleStmts, Set<PreparedStatement> subMultiStmts) throws CstlServiceException {
        Values values = new Values();
        
        //we extract the single values
        for (PreparedStatement stmt : subSingleStmts) {
            try {
                fillStatement(stmt, parameters);
                fillSingleValues(stmt, values);
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 17008) {
                    reloadConnection();
                }
                logError(singleStatements.get(stmt), ex, stmt);
            } catch (IllegalArgumentException ex) {
                logError(singleStatements.get(stmt), ex, stmt);
            }
        }

        //we extract the multiple values
        for (PreparedStatement stmt : subMultiStmts) {
            try {
                fillStatement(stmt, parameters);
                fillMultipleValues(stmt, values);
                
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 17008) {
                    reloadConnection();
                }
                logError(multipleStatements.get(stmt), ex, stmt);
            } catch (IllegalArgumentException ex) {
                logError(multipleStatements.get(stmt), ex, stmt);
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
    private Values paraleleLoading(final List<String> parameters, Set<PreparedStatement> subSingleStmts, Set<PreparedStatement> subMultiStmts) throws CstlServiceException {
        final Values values = new Values();

        //we extract the single values
        CompletionService cs = new BoundedCompletionService(this.pool, 5);
        for (final PreparedStatement stmt : subSingleStmts) {
            cs.submit(new Callable() {

                public Object call() throws CstlServiceException {
                    try {
                        fillStatement(stmt, parameters);
                        fillSingleValues(stmt, values);

                    } catch (SQLException ex) {
                        if (ex.getErrorCode() == 17008) {
                            reloadConnection();
                        }
                        logError(singleStatements.get(stmt), ex, stmt);
                    } catch (IllegalArgumentException ex) {
                        logError(singleStatements.get(stmt), ex, stmt);
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
                if (ex.getCause() != null && ex.getCause() instanceof CstlServiceException) {
                    throw (CstlServiceException) ex.getCause();
                } else {
                    logger.severe("ExecutionException in parralele load data:" + '\n' + ex.getMessage());
                }
            } 
        }
        //we extract the multiple values
        cs = new BoundedCompletionService(this.pool, 5);
        for (final PreparedStatement stmt : subMultiStmts) {
            cs.submit(new Callable() {

                public Object call() throws CstlServiceException {
                    try {
                        fillStatement(stmt, parameters);
                        fillMultipleValues(stmt, values);
                        
                    } catch (SQLException ex) {
                        if (ex.getErrorCode() == 17008) {
                            reloadConnection();
                        }
                        logError(multipleStatements.get(stmt), ex, stmt);
                    } catch (IllegalArgumentException ex) {
                        logError(multipleStatements.get(stmt), ex, stmt);
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
                if (ex.getCause() != null && ex.getCause() instanceof CstlServiceException) {
                    throw (CstlServiceException) ex.getCause();
                } else {
                    logger.severe("ExecutionException in parralele load data:" + '\n' + ex.getMessage());
                }
            }
        }
        return values;
    }

    /**
     * Fill the dynamic parameters of a prepared statement with the specified parameters.
     * 
     * @param stmt
     * @param parameters
     */
    private void fillStatement(PreparedStatement stmt, List<String> parameters) throws SQLException {
        if (parameters == null)
            parameters = new ArrayList<String>();
        ParameterMetaData meta = stmt.getParameterMetaData();
        int nbParam = meta.getParameterCount();
        if (nbParam != parameters.size())
            throw new IllegalArgumentException("There is not the good number of parameters specified for this statement: stmt:" + nbParam + " parameters:" + parameters.size());
        
        int i = 1;
        while (i < nbParam + 1) {
            String parameter = parameters.get(i - 1);

            // in some jdbc driver (oracle for example) the following instruction is not supported.
            int type = -1;
            if (advancedJdbcDriver)  {
                try {
                    type = meta.getParameterType(i);
                } catch (Exception ex) {
                    logger.warning("unsupported jdbc operation in fillstatement");
                    advancedJdbcDriver = false;
                }
            }
            if (type == java.sql.Types.INTEGER) {
                try {
                    int id = Integer.parseInt(parameter);
                    stmt.setInt(i, id);
                } catch(NumberFormatException ex) {
                    logger.severe("unable to parse the int parameter:" + parameter);
                }
            } else  {
                stmt.setString(i, parameter);
            }
            i++;
        }
    }

    private void fillSingleValues(PreparedStatement stmt, Values values) throws SQLException {
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            for (String varName : singleStatements.get(stmt)) {
                values.addSingleValue(varName, result.getString(varName));
            }
        }
        result.close();
    }

    private void fillMultipleValues(PreparedStatement stmt, Values values) throws SQLException {
        ResultSet result = stmt.executeQuery();
        logger.finer("QUERY:" + stmt.toString());
        for (String varName : multipleStatements.get(stmt)) {
            values.createNewMultipleValue(varName);
        }
        while (result.next()) {
            for (String varName : multipleStatements.get(stmt)) {
                values.addToMultipleValue(varName, result.getString(varName));
            }
        }
        result.close();
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
     * Log the list of variables involved in a query which launch a SQL exception.
     * (debugging purpose).
     *
     * @param varList a list of variable.
     * @param ex
     */
    private void logError(List<String> varList, Exception ex, PreparedStatement stmt) {
        String varlist = "";
        if (varList != null) {
            for (String s : varList) {
                varlist += s + ',';
            }
            if (varlist.length() > 1);
                varlist = varlist.substring(0, varlist.length() - 1);
        } else {
            varlist = "no variables";
        }
        logger.severe( ex.getClass().getSimpleName() +
                      " occurs while executing query: "          + '\n' +
                      "query: " + StringQueryMap.get(stmt)       + '\n' +
                      "cause: " + ex.getMessage()                + '\n' +
                      "for variable: " + varlist                 + '\n');
    }

    /**
     * Try to reconnect to the database if the connection have been lost.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    public void reloadConnection() throws CstlServiceException {
        if (!isReconnecting) {
            try {
               logger.info("refreshing the connection");
               BDD db          = configuration.getBdd();
               this.connection = db.getConnection();
               initStatement();
               isReconnecting = false;

            } catch(SQLException ex) {
                logger.severe("SQLException while restarting the connection:" + ex);
                isReconnecting = false;
            }
        }
        throw new CstlServiceException("The database connection has been lost, the service is trying to reconnect", NO_APPLICABLE_CODE);
    }
    
    public void destroy() {
        logger.info("destroying generic reader");
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

            pool.shutdown();
        } catch (SQLException ex) {
            logger.severe("SQLException while destroying Generic metadata reader");
        }
    }
}
