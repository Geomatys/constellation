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

package org.constellation.sos.io;

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

// constellation dependecies
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Column;
import org.constellation.generic.database.MultiFixed;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.Single;
import org.constellation.ws.CstlServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public abstract class GenericObservationReader extends ObservationReader {

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
     * A flag indicating if the multi thread mecanism is enabled or not.
     */
    private final boolean isThreadEnabled;
    
    /**
     * Shared Thread Pool for parralele execution
     */
    private ExecutorService pool = Executors.newFixedThreadPool(6);

    public GenericObservationReader(String observationIdBase, Automatic configuration) throws CstlServiceException {
        super(observationIdBase);
        try {
            BDD bdd = configuration.getBdd();
            if (bdd != null) {
                Connection connection = bdd.getConnection();
                initStatement(connection, configuration);
            } else {
                throw new CstlServiceException("The database par of the generic configuration file is null", NO_APPLICABLE_CODE);
            }
        } catch (SQLException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
        isThreadEnabled = false;
    }

    /**
     * Initialize the prepared statement build from the configuration file.
     *
     * @throws java.sql.SQLException
     */
    private final void initStatement(Connection connection, Automatic configuration) throws SQLException {
        // we initialize the main query
        if (configuration.getQueries() != null           &&
            configuration.getQueries().getMain() != null &&
            configuration.getQueries().getMain().getQuery() != null) {
            Query mainQuery = configuration.getQueries().getMain().getQuery();
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
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(List<String> variables, String value) {

        Set<PreparedStatement> subSingleStmts = new HashSet<PreparedStatement>();
        Set<PreparedStatement> subMultiStmts = new HashSet<PreparedStatement>();
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
                    logger.severe("no statement found for variable: " + var);
                }
            }
        }

        Values values;
        if (isThreadEnabled) {
            values = paraleleLoading(value, subSingleStmts, subMultiStmts);
        } else {
            values = sequentialLoading(value, subSingleStmts, subMultiStmts);
        }
        return values;
    }

    /**
     * Execute the list of single and multiple statement sequentially.
     *
     * @param identifier
     * @param subSingleStmts
     * @param subMultiStmts
     */
    private Values sequentialLoading(String identifier, Set<PreparedStatement> subSingleStmts, Set<PreparedStatement> subMultiStmts) {
        Values values = new Values();
        
        //we extract the single values
        for (PreparedStatement stmt : subSingleStmts) {
            try {
                fillStatement(stmt, identifier);
                fillSingleValues(stmt, values);
            } catch (SQLException ex) {
                logSqlError(singleStatements.get(stmt), ex, stmt);
            }
        }

        //we extract the multiple values
        for (PreparedStatement stmt : subMultiStmts) {
            try {
                fillStatement(stmt, identifier);
                fillMultipleValues(stmt, values);
                
            } catch (SQLException ex) {
               logSqlError(multipleStatements.get(stmt), ex, stmt);
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
    private Values paraleleLoading(final String identifier, Set<PreparedStatement> subSingleStmts, Set<PreparedStatement> subMultiStmts) {
        final Values values = new Values();

        //we extract the single values
        CompletionService cs = new BoundedCompletionService(this.pool, 5);
        for (final PreparedStatement stmt : subSingleStmts) {
            cs.submit(new Callable() {

                public Object call() {
                    try {
                        fillStatement(stmt, identifier);
                        fillSingleValues(stmt, values);

                    } catch (SQLException ex) {
                        logSqlError(singleStatements.get(stmt), ex, stmt);
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
                        fillMultipleValues(stmt, values);
                        
                    } catch (SQLException ex) {
                        logSqlError(multipleStatements.get(stmt), ex, stmt);
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
        return values;
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
            int type = meta.getParameterType(i);
            if (type == java.sql.Types.INTEGER) {
                try {
                    int id = Integer.parseInt(identifier);
                    stmt.setInt(i, id);
                } catch(NumberFormatException ex) {
                    logger.severe("unable to parse the int parameter:" + identifier);
                }
            } else  {
                stmt.setString(i, identifier);
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
    public void logSqlError(List<String> varList, SQLException ex, PreparedStatement stmt) {
        String varlist = "";
        if (varList != null) {
            for (String s : varList) {
                varlist += s + ',';
            }
        } else {
            varlist = "no variables";
        }
        logger.severe("SQLException while executing query: " + ex.getMessage() + '\n' +
                      "query: " + stmt.toString()                              + '\n' +
                      "for variable: " + varlist);
    }

    /**
     * return a list of value for the specified variable name.
     *
     * @param variables
     * @return
     */
    protected List<String> getVariables(String variable, Values val) {
        List<String> values = val.multipleValue.get(variable);
        if (values == null)
            values = new ArrayList<String>();
        return values;
    }

    /**
     * return the value for the specified variable name.
     *
     * @param variable
     * @return
     */
    protected String getVariable(String variable, Values values) {
        return values.singleValue.get(variable);
    }

    @Override
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
