/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.generic;

import org.apache.sis.util.logging.Logging;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.QueryList;
import org.constellation.metadata.io.MetadataIoException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// constellation dependecies

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class GenericReader  {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * A precompiled Statement requesting all The identifiers
     */
    private String mainStatement;
    
    /**
     * A Map of the SQL request for the variable the contains.
     */
    private final Map<String, List<String>> varStatements = new HashMap<String, List<String>>();
    
    /**
     * A Map of the named SQL request .
     */
    private final Map<String, String> queryStatements = new HashMap<String, String>();

    /**
     * A flag indicating if the JDBC driver support several specific operation
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
    private HashMap<String, Object> staticParameters = new HashMap<String, Object>();

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
    private DataSource datasource;

    /**
     * A list of unbounded variable.
     * Used to avoid to search for an unexistant statement each time.
     */
    private final List<String> unboundedVariable = new ArrayList<String>();
    
    /**
     * A list of unbounded query.
     * Used to avoid to search for an unexistant statement each time.
     */
    private final List<String> unboundedQueries = new ArrayList<String>();

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
                this.datasource = bdd.getPooledDataSource();
                //try to connect
                final Connection c = datasource.getConnection();
                c.close();
                
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
    protected GenericReader(final Map<List<String>, Values> debugValues, final HashMap<String, Object> staticParameters) throws MetadataIoException {
        advancedJdbcDriver = true;
        debugMode          = true;
        configuration      = null;
        this.debugValues   = debugValues;
        if (staticParameters != null) {
            this.staticParameters = staticParameters;
        } else {
            this.staticParameters = new HashMap<String, Object>();
        }
    }

    /**
     * Initialize the prepared statement build from the configuration file.
     *
     * @throws java.sql.SQLException
     */
    private void initStatement() throws SQLException {
        varStatements.clear();
        queryStatements.clear();
        final Queries queries = configuration.getQueries();
        if (queries != null) {

            // initialize the static parameters obtained by a static query (no parameters & 1 ouput param)
            intStaticParameters(queries);
            
            //initialize the main statement
            if (queries.getMain() != null) {
                final Query mainQuery = queries.getMain();
                mainStatement         = mainQuery.buildSQLQuery(staticParameters);
            }

            // initialize the statements
            final List<Query> allQueries = queries.getAllQueries();
            for (Query query : allQueries) {
                final List<String> varNames        = query.getVarNames();
                final String textQuery             = query.buildSQLQuery(staticParameters);
                varStatements.put(textQuery, varNames);
                queryStatements.put(query.getName(),textQuery);
            }
        } else {
            LOGGER.warning("The configuration file is probably malformed, there is no queries part.");
        }
    }

    /**
     * Fill the static parameters map, with the direct parameters in the configuration
     * and the results of the static queries.
     * 
     * @param queries
     * @throws SQLException
     */
    private void intStaticParameters(final Queries queries) throws SQLException {
        staticParameters = new HashMap<String, Object>();
        if (queries.getParameters() != null) {
            for (Entry<String, String> entry : queries.getParameters().entrySet()) {
                staticParameters.put(entry.getKey(), entry.getValue());
            }
        }
        final QueryList statique = queries.getStatique();
        if (statique != null) {
            final Connection connection = datasource.getConnection();
            final Statement stmt   = connection.createStatement();
            for (Query query : statique.getQuery()) {
                processStatiqueQuery(query, stmt);
                if (query.getStatique() != null) {
                    for (Query subQuery : query.getStatique().getQuery()) {
                        processStatiqueQuery(subQuery, stmt);
                    }
                }
            }
            stmt.close();
            connection.close();
        }
    }
    
    private void processStatiqueQuery(final Query query, final Statement stmt) throws SQLException {
        final List<String> varNames = query.getVarNames();
        final String textQuery = query.buildSQLQuery(staticParameters);
        try {
            final ResultSet res = stmt.executeQuery(textQuery);
            final Map<String, StringBuilder> parameterValue = new HashMap<String, StringBuilder>();
            for (String varName : varNames) {
                parameterValue.put(varName, new StringBuilder());
            }
            while (res.next()) {
                for (String varName : varNames) {
                    final StringBuilder builder = parameterValue.get(varName);
                    builder.append("'").append(res.getString(varName)).append("',");
                }
            }
            res.close();
            //we remove the last ','
            for (String varName : varNames) {
                final StringBuilder builder = parameterValue.get(varName);
                final String pValue;
                if (builder.length() > 0) {
                    pValue = builder.substring(0, builder.length() - 1);
                } else {
                    pValue = "";
                }
                staticParameters.put(varName, pValue);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "SQL exception while executing static query :{0}", textQuery);
            throw ex;
        }
    }

    protected List<String> getMainQueryResult() throws MetadataIoException {
        final List<String> result = new ArrayList<String>();
        try {
            final Connection connection  = datasource.getConnection();
            final PreparedStatement stmt = connection.prepareStatement(mainStatement);
            final ResultSet res          = stmt.executeQuery();
            while (res.next()) {
                result.add(res.getString(1));
            }
            res.close();
            stmt.close();
            connection.close();
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while executing main query: " + ex.getMessage());
        }
        return result;
    }

    /**
     * Load all the data for the specified variable from the database.
     * @param identifier
     */
    protected Values loadData(final String variable) throws MetadataIoException {
        return loadData(Arrays.asList(variable), new ArrayList<String>());
    }
    
    /**
     * Load all the data for the specified query from the database.
     * @param identifier
     */
    protected Values loadQuery(final String query) throws MetadataIoException {
        return loadQuery(Arrays.asList(query), new ArrayList<String>());
    }

    /**
     * Load all the data for the specified variables from the database.
     * @param identifier
     */
    protected Values loadData(final String variable, final String parameter) throws MetadataIoException {
        return loadData(Arrays.asList(variable), Arrays.asList(parameter));
    }
    
    /**
     * Load all the data for the specified query from the database.
     * @param identifier
     */
    protected Values loadQuery(final String query, final String parameter) throws MetadataIoException {
        return loadQuery(Arrays.asList(query), Arrays.asList(parameter));
    }

    /**
     * Load all the data for the specified variables from the database.
     * @param identifier
     */
    protected Values loadData(final List<String> variables, final String parameter) throws MetadataIoException {
        return loadData(variables, Arrays.asList(parameter));
    }

    /**
     * Load all the data for the specified queries from the database.
     * @param identifier
     */
    protected Values loadQuery(final List<String> queries, final String parameter) throws MetadataIoException {
        return loadQuery(queries, Arrays.asList(parameter));
    }
    
    /**
     * Load all the data for the specified variable from the database.
     * @param identifier
     */
    protected Values loadData(final String variable, final List<String> parameter) throws MetadataIoException {
        return loadData(Arrays.asList(variable), parameter);
    }
    
     /**
     * Load all the data for the specified query from the database.
     * @param identifier
     */
    protected Values loadQuery(final String query, final List<String> parameter) throws MetadataIoException {
        return loadQuery(Arrays.asList(query), parameter);
    }

    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(final List<String> variables) throws MetadataIoException {
        return loadData(variables, new ArrayList<String>());
    }
    
    /**
     * Load all the data for the specified queries from the database.
     * @param identifier
     */
    protected Values loadQuery(final List<String> queries) throws MetadataIoException {
        return loadData(queries, new ArrayList<String>());
    }

    /**
     * Load all the data for the specified queries from the database.
     *
     * @param identifier
     */
    protected Values loadQuery(final List<String> queries, final List<String> parameters) throws MetadataIoException {
        final Set<String> subStmts = new HashSet<String>();
        final Values staticValues = new Values();
        for (String query : queries) {
            if (unboundedQueries.contains(query)) {continue;}
            
            final String stmt = queryStatements.get(query);
            if (stmt != null) {
                if (!subStmts.contains(stmt)) {
                    subStmts.add(stmt);
                }
            } else {
                unboundedQueries.add(query);
                LOGGER.log(Level.WARNING, "no statement found for query name: {0}", query);
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
     * Load all the data for the specified Identifier from the database.
     *
     * @param identifier
     */
    protected Values loadData(final List<String> variables, final List<String> parameters) throws MetadataIoException {

        final Set<String> subStmts = new HashSet<String>();
        final Values staticValues = new Values();
        for (String var : variables) {
            if (unboundedVariable.contains(var)) {continue;}
            
            final String stmt = getStatementFromVar(var);
            if (stmt != null) {
                if (!subStmts.contains(stmt)) {
                    subStmts.add(stmt);
                }
            } else {
                
                final Object staticValue = staticParameters.get(var);
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
    private Values loading(final List<String> parameters, final Set<String> subStmts) throws MetadataIoException {
        final Values values = new Values();
        
        //we extract the single values
        for (String sql : subStmts) {
            String phase = "connecting";
            final List<String> varList = varStatements.get(sql);
            try {
                final Connection connection  = datasource.getConnection();
                phase = "preparing";
                final PreparedStatement stmt = connection.prepareStatement(sql);
                phase = "filling params";
                fillStatement(stmt, parameters);
                phase = "executing";
                fillValues(stmt, sql, varList, values);
                stmt.close();
                connection.close();
            } catch (SQLException ex) {
                /*
                 * If we get the error code 17008 (oracle),
                 * or SQL state 08006 (psotgresql)
                 * this mean that we have lost the connection
                 * So we try to reconnect.
                 */
                if (ex.getErrorCode() == 17008 || "08006".equals(ex.getSQLState())) {
                    LOGGER.log(Level.WARNING, "detected a connection lost:{0}", ex.getMessage());
                    reloadConnection();
                }
                logError(phase, varList, ex, sql);
            } catch (IllegalArgumentException ex) {
                logError(phase, varList, ex, sql);
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
    private void fillStatement(final PreparedStatement stmt, List<String> parameters) throws SQLException {
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
            } else if (type == java.sql.Types.TIMESTAMP) {
                try {
                    final Timestamp ts = Timestamp.valueOf(parameter);
                    stmt.setTimestamp(i, ts);
                } catch(IllegalArgumentException ex) {
                    LOGGER.log(Level.SEVERE, "unable to parse the timestamp parameter:{0}", parameter);
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
    private void fillValues(final PreparedStatement stmt, final String sql, final List<String> varNames, final Values values) throws SQLException {

        LOGGER.log(Level.FINER, "ExecuteQuery:{0}", sql);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            for (String varName : varNames) {
                final int columnIndex = result.findColumn(varName);
                final int type        = result.getMetaData().getColumnType(columnIndex);
                if (type == java.sql.Types.INTEGER || type == java.sql.Types.SMALLINT) {
                    values.addToValue(varName, result.getInt(varName));
                } else if (type == java.sql.Types.DOUBLE) {
                    values.addToValue(varName, result.getDouble(varName));
                } else if (type == java.sql.Types.TIMESTAMP) {
                    values.addToValue(varName, result.getTimestamp(varName));
                } else {
                    values.addToValue(varName, result.getString(varName));
                }
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
    private String getStatementFromVar(final String varName) {
        for (Entry<String, List<String>> stmtEntry : varStatements.entrySet()) {
            if (stmtEntry.getValue().contains(varName)) {
                return stmtEntry.getKey();
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
    private void logError(final String phase, final List<String> varList, final Exception ex, final String sql) {
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
        final StringBuilder sb = new StringBuilder(ex.getClass().getSimpleName());
        sb.append(" occurs while executing query: \nQuery: ").append(sql);
        sb.append("\nPhase: ").append(phase);
        sb.append("\nCause: ").append(ex.getMessage());
        sb.append("\nCode: ").append(code);
        sb.append("\nSQLState : ").append(state);
        sb.append("\nFor variable: ").append(value).append('\n');
        LOGGER.severe(sb.toString());
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
               // TODO still needed ?
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
        varStatements.clear();
        queryStatements.clear();
        // do nothing anymore
    }
}
