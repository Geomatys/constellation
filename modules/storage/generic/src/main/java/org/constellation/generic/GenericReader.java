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

package org.constellation.generic;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// constellation dependecies
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.QueryList;
import org.constellation.metadata.io.MetadataIoException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class GenericReader  {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

    /**
     * A precompiled Statement requesting all The identifiers
     */
    private PreparedStatement mainStatement;
    
    /**
     * A list of precompiled SQL request returning single and multiple values.
     */
    private final Map<LockedPreparedStatement, List<String>> statements = new HashMap<LockedPreparedStatement, List<String>>();

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
     * A map of static variable to replace in the statements.
     */
    private HashMap<String, String> staticParameters = new HashMap<String, String>();

    /**
     * A flag indicating that the service is trying to reconnect the database.
     */
    private boolean isReconnecting = false;

    /**
     * The database informations.
     */
    private final Automatic configuration;

     /**
     * A connection to the database.
     */
    private Connection connection;

    /**
     * A list of unbounded variable.
     * Used to avoid to search for an unexistant statement each time.
     */
    private final List<String> unboundedVariable = new ArrayList<String>();

    /**
     * Build a new generic Reader.
     *
     * @param configuration A configuration object containing database informations,
     *                      and all the SQL queries.
     * @throws CstlServiceException
     */
    public GenericReader(final Automatic configuration) throws MetadataIoException {
        this.configuration = configuration;
        advancedJdbcDriver = true;
        try {
            final BDD bdd = configuration.getBdd();
            if (bdd != null) {
                this.connection = bdd.getConnection();
                initStatement();
            } else {
                throw new MetadataIoException("The database par of the generic configuration file is null");
            }
        } catch (SQLException ex) {
            throw new MetadataIoException(ex);
        } catch (IllegalArgumentException ex) {
            throw new MetadataIoException(ex);
        }
    }

    /**
     * A JUnit test constructor.
     * 
     * @param debugValues
     * @param staticParameters
     * @throws CstlServiceException
     */
    protected GenericReader(final Map<List<String>, Values> debugValues, final HashMap<String, String> staticParameters) throws MetadataIoException {
        advancedJdbcDriver = true;
        debugMode          = true;
        configuration      = null;
        this.debugValues   = debugValues;
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
    private void initStatement() throws SQLException {
        statements.clear();
        final Queries queries = configuration.getQueries();
        if (queries != null) {

            // initialize the static parameters obtained by a static query (no parameters & 1 ouput param)
            intStaticParameters(queries);
            
            //initialize the main statement
            if (queries.getMain() != null) {
                final Query mainQuery = queries.getMain().getQuery();
                mainStatement         = connection.prepareStatement(mainQuery.buildSQLQuery(staticParameters));
            }

            // initialize the statements
            final List<Query> allQueries = queries.getAllQueries();
            for (Query query : allQueries) {
                final List<String> varNames        = query.getVarNames();
                final String textQuery             = query.buildSQLQuery(staticParameters);
                final LockedPreparedStatement stmt =  new LockedPreparedStatement(connection.prepareStatement(textQuery), textQuery);
                statements.put(stmt, varNames);
            }
        } else {
            LOGGER.warning("The configuration file is probably malformed, there is no queries part.");
        }
    }

    /**
     * Fill the static parameters map, with the direct parameters in the configuration
     * and the results of the statique queries.
     * 
     * @param queries
     * @throws SQLException
     */
    private void intStaticParameters(final Queries queries) throws SQLException {
        staticParameters         = queries.getParameters();
        if (staticParameters == null) {
            staticParameters = new HashMap<String, String>();
        }
        final QueryList statique = queries.getStatique();
        if (statique != null) {
            final Statement stmt   = connection.createStatement();
            for (Query query : statique.getQuery()) {
                final String varName   = query.getFirstVarName();
                final String textQuery = query.buildSQLQuery(staticParameters);
                final ResultSet res    = stmt.executeQuery(textQuery);
                final StringBuilder parameterValue = new StringBuilder();
                while (res.next()) {
                    parameterValue.append("'").append(res.getString(1)).append("',");
                }
                res.close();
                //we remove the last ','
                String pValue = parameterValue.toString();
                if (parameterValue.length() > 0) {
                    pValue = parameterValue.substring(0, parameterValue.length() - 1);
                }
                staticParameters.put(varName, pValue);
            }
            stmt.close();
        }
    }

    protected List<String> getMainQueryResult() throws MetadataIoException {
        final List<String> result = new ArrayList<String>();
        try {
            final ResultSet res = mainStatement.executeQuery();
            while (res.next()) {
                result.add(res.getString(1));
            }
            res.close();
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while executing main query: " + ex.getMessage());
        }
        return result;
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(final String variable) throws MetadataIoException {
        return loadData(Arrays.asList(variable), new ArrayList<String>());
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(final String variable, final String parameter) throws MetadataIoException {
        return loadData(Arrays.asList(variable), Arrays.asList(parameter));
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(final List<String> variables, final String parameter) throws MetadataIoException {
        return loadData(variables, Arrays.asList(parameter));
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(final String variable, final List<String> parameter) throws MetadataIoException {
        return loadData(Arrays.asList(variable), parameter);
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(final List<String> variables) throws MetadataIoException {
        return loadData(variables, new ArrayList<String>());
    }

    /**
     * Load all the data for the specified Identifier from the database.
     *
     * @param identifier
     */
    protected Values loadData(final List<String> variables, final List<String> parameters) throws MetadataIoException {

        final Set<LockedPreparedStatement> subStmts = new HashSet<LockedPreparedStatement>();
        final Values staticValues = new Values();
        for (String var : variables) {
            if (unboundedVariable.contains(var)) continue;
            
            final LockedPreparedStatement stmt = getStatementFromVar(var);
            if (stmt != null) {
                if (!subStmts.contains(stmt)) {
                    subStmts.add(stmt);
                }
            } else {
                
                final String staticValue = staticParameters.get(var);
                if (staticValue != null) {
                    staticValues.addToValue(var, staticValue);
                } else {
                    unboundedVariable.add(var);
                    LOGGER.log(Level.WARNING, "no statement found for variable: {0}", var);
                }
                
            }
        }
        // if there is only static parameters
        if (subStmts.isEmpty()) {
            return staticValues;
        }
        final Values values;
        if (debugMode) {
            values = debugLoading(parameters);
        } else {
            values = loading(parameters, subStmts);
        }
        //we add the static value to the result
        values.mergedValues(staticValues);
        return values;
    }


    /**
     * Load the data in debug mode without querying the database .
     *
     */
    private Values debugLoading(final List<String> parameters) {
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
    private Values loading(final List<String> parameters, final Set<LockedPreparedStatement> subStmts) throws MetadataIoException {
        final Values values = new Values();
        
        //we extract the single values
        for (LockedPreparedStatement stmt : subStmts) {
            try {
                fillStatement(stmt, parameters);
                fillValues(stmt, statements.get(stmt), values);
            } catch (SQLException ex) {
                /*
                 * If we get the error code 17008 (oracle),
                 * or SQL state 08006 (psotgresql)
                 * this mean that we have lost the connection
                 * So we try to reconnect.
                 */
                if (ex.getErrorCode() == 17008 || "08006".equals(ex.getSQLState())) {
                    LOGGER.warning("detected a conenction lost:" + ex.getMessage());
                    reloadConnection();
                }
                logError(statements.get(stmt), ex, stmt.getSql());
            } catch (IllegalArgumentException ex) {
                logError(statements.get(stmt), ex, stmt.getSql());
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
    private void fillStatement(final LockedPreparedStatement stmt, List<String> parameters) throws SQLException {
        if (parameters == null) {
            parameters = new ArrayList<String>();
        }
        final ParameterMetaData meta = stmt.getParameterMetaData();
        final int nbParam            = meta.getParameterCount();

        if (nbParam != parameters.size() && parameters.size() != 1) {
            throw new IllegalArgumentException("There is not the good number of parameters specified for this statement: stmt:" + nbParam + " parameters:" + parameters.size());
        } else if (nbParam != parameters.size() && parameters.size() == 1) {
            /*
             * PATCH: if we have only one parameters submit and more parameters expected we fill all the ? with the same parameter.
             */
            final String uniqueParam = parameters.get(0);
            parameters = new ArrayList<String>();
            for (int j = 0; j < nbParam; j++) {
                parameters.add(uniqueParam);
            }
        }
        
        int i = 1;
        while (i < nbParam + 1) {
            final String parameter = parameters.get(i - 1);

            // in some jdbc driver (oracle for example) the following instruction is not supported.
            int type = -1;
            if (advancedJdbcDriver)  {
                try {
                    type = meta.getParameterType(i);
                } catch (Exception ex) {
                    LOGGER.warning("unsupported jdbc operation in fillstatement (normal for oracle driver)");
                    advancedJdbcDriver = false;
                }
            }
            if (type == java.sql.Types.INTEGER || type == java.sql.Types.SMALLINT) {
                try {
                    final int id = Integer.parseInt(parameter);
                    stmt.setInt(i, id);
                } catch(NumberFormatException ex) {
                    LOGGER.log(Level.SEVERE, "unable to parse the int parameter:{0}", parameter);
                }
            } else  {
                stmt.setString(i, parameter);
            }
            i++;
        }
    }

    /**
     *
     * @param stmt
     * @param values
     * @throws SQLException
     */
    private void fillValues(final LockedPreparedStatement stmt, final List<String> varNames, final Values values) throws SQLException {

        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            for (String varName : varNames) {
                values.addToValue(varName, result.getString(varName));
            }
        }
        result.close();
    }

    /**
     * Return the corresponding statement for the specified variable name.
     *
     * @param varName
     * @return
     */
    private LockedPreparedStatement getStatementFromVar(final String varName) {
        for (LockedPreparedStatement stmt : statements.keySet()) {
            final List<String> vars = statements.get(stmt);
            if (vars.contains(varName)) {
                return stmt;
            }
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
    private void logError(final List<String> varList, final Exception ex, final String sql) {
        final StringBuilder varlist = new StringBuilder();
        final String value;
        final int code;
        final String state;
        // we build the String list of variables
        if (varList != null) {
            for (String s : varList) {
                varlist.append(s).append(',');
            }
            if (varlist.length() > 1) {
                value = varlist.substring(0, varlist.length() - 1);
            } else {
                value = "no variables";
            }
        } else {
            value = "no variables";
        }
        // we get the exception code if there is one
        if (ex instanceof SQLException) {
            code   = ((SQLException)ex).getErrorCode() ;
            state = ((SQLException)ex).getSQLState() ;
        } else {
            code = -1;
            state = "undefined";
        }
        LOGGER.severe( ex.getClass().getSimpleName()             + " " +
                      "occurs while executing query: "           + '\n' +
                      "Query: " + sql                            + '\n' +
                      "Cause: " + ex.getMessage()                + '\n' +
                      "Code: "  + code                           + '\n' +
                      "SQLState : "  + state                     + '\n' +
                      "For variable: " + value                   + '\n');
    }

    /**
     * Try to reconnect to the database if the connection have been lost.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    public void reloadConnection() throws MetadataIoException {
        if (!isReconnecting) {
            try {
               isReconnecting = true;
               LOGGER.info("refreshing the connection");
               final BDD db    = configuration.getBdd();
               this.connection = db.getFreshConnection();
               initStatement();
               isReconnecting  = false;

            } catch(SQLException ex) {
                LOGGER.log(Level.WARNING, "SQLException while restarting the connection.", ex);
                isReconnecting = false;
            }
        }
        throw new MetadataIoException("The database connection has been lost, the service is trying to reconnect");
    }

    /**
     * Destroy all the resource.
     */
    public void destroy() {
        LOGGER.info("destroying generic reader");
        try {
            if (mainStatement != null) {
                mainStatement.close();
                mainStatement = null;
            }

            for (LockedPreparedStatement stmt : statements.keySet()) {
                stmt.close();
            }
            statements.clear();
            connection.close();

        } catch (SQLException ex) {
            LOGGER.severe("SQLException while destroying Generic metadata reader");
        }
    }
}
