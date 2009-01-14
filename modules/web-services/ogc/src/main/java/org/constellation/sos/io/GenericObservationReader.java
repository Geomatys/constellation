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
import javax.xml.namespace.QName;
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.coverage.model.Distribution;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Column;
import org.constellation.generic.database.MultiFixed;
import org.constellation.generic.database.Queries;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.Single;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.PointType;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.observation.ObservationEntry;
import org.constellation.observation.ProcessEntry;
import org.constellation.sampling.SamplingFeatureEntry;
import org.constellation.sampling.SamplingPointEntry;
import org.constellation.sos.ObservationOfferingEntry;
import org.constellation.sos.ResponseModeType;
import org.constellation.swe.v101.AnyResultEntry;
import org.constellation.swe.v101.CompositePhenomenonEntry;
import org.constellation.swe.v101.DataArrayEntry;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.swe.v101.SimpleDataRecordEntry;
import org.constellation.swe.v101.TextBlockEntry;
import org.constellation.ws.WebServiceException;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.sos.ws.SOSworker.*;

/**
 *
 * @author Guilhem Legal
 */
public class GenericObservationReader extends ObservationReader {

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
     * A flag indicating if the multi thread mecanism is enabled or not.
     */
    private final boolean isThreadEnabled;
    
    /**
     * Shared Thread Pool for parralele execution
     */
    private ExecutorService pool = Executors.newFixedThreadPool(6);

    public GenericObservationReader(String observationIdBase, Automatic configuration) throws WebServiceException {
        super(observationIdBase);
        try {
            BDD bdd = configuration.getBdd();
            if (bdd != null) {
                Connection connection = bdd.getConnection();
                initStatement(connection, configuration);
            } else {
                throw new WebServiceException("The database par of the generic configuration file is null", NO_APPLICABLE_CODE);
            }
        } catch (SQLException ex) {
            throw new WebServiceException(ex, NO_APPLICABLE_CODE);
        }
        singleValue     = new HashMap<String, String>();
        multipleValue   = new HashMap<String, List<String>>();
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
    private void loadData(List<String> variables, String value) {
        singleValue.clear();
        multipleValue.clear();

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

        if (isThreadEnabled) {
            paraleleLoading(value, singleStatements.keySet(), multipleStatements.keySet());
        } else {
            sequentialLoading(value, singleStatements.keySet(), multipleStatements.keySet());
        }
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
                logSqlError(singleStatements.get(stmt), ex, stmt);
            }
        }

        //we extract the multiple values
        for (PreparedStatement stmt : subMultiStmts) {
            try {
                fillStatement(stmt, identifier);
                fillMultipleValues(stmt);
                
            } catch (SQLException ex) {
               logSqlError(multipleStatements.get(stmt), ex, stmt);
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
                        fillMultipleValues(stmt);
                        
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
    protected List<String> getVariables(String variable) {
        List<String> values = multipleValue.get(variable);
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
    protected String getVariable(String variable) {
        return singleValue.get(variable);
    }
    
    @Override
    public List<String> getOfferingNames() throws WebServiceException {
        loadData(Arrays.asList("var01"), null);
        return getVariables("var01");
    }

    @Override
    public List<String> getProcedureNames() throws WebServiceException {
        loadData(Arrays.asList("var02"), null);
        return getVariables("var02");
    }

    @Override
    public List<String> getPhenomenonNames() throws WebServiceException {
        loadData(Arrays.asList("var03"), null);
        return getVariables("var03");
    }

    @Override
    public List<String> getFeatureOfInterestNames() throws WebServiceException {
        loadData(Arrays.asList("var04"), null);
        return getVariables("var04");
    }

    @Override
    public String getNewObservationId() throws WebServiceException {
        int id = Integer.parseInt(getVariable("var05"));
        String _continue = null;
        do {
            id++;
            _continue = getVariable("var34");

        } while (_continue != null);
        return observationIdBase + id;
    }

    @Override
    public String getMinimalEventTime() throws WebServiceException {
        return getVariable("var06");
    }

    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws WebServiceException {
        loadData(Arrays.asList("var07", "var08", "var09", "var10", "var11", "var12", "var15", "var16", "var17", "var18"), offeringName);
        List<String> srsName = getVariables("var07");
        
        // event time
        String offeringBegin = getVariable("var08");
        String offeringEnd   = getVariable("var09");
        TimePeriodType time  = new TimePeriodType(offeringBegin, offeringEnd);

        // procedure
        List<ReferenceEntry> procedures = new ArrayList<ReferenceEntry>();
        for (String procedureName : getVariables("var10")) {
            procedures.add(new ReferenceEntry(null, procedureName));
        }

        // phenomenon
        List<PhenomenonEntry> observedProperties = new ArrayList<PhenomenonEntry>();
        for (String phenomenonId : getVariables("var11")) {
            PhenomenonEntry phenomenon = getPhenomenon(phenomenonId);
            observedProperties.add(phenomenon);
        }
        for (String phenomenonId : getVariables("var12")) {
            List<PhenomenonEntry> components = new ArrayList<PhenomenonEntry>();
            for (String componentID : getVariables("var17")) {
                components.add(getPhenomenon(componentID));
            }
            CompositePhenomenonEntry phenomenon = new CompositePhenomenonEntry(phenomenonId,
                                                                               getVariable("var15"),
                                                                               getVariable("var16"),
                                                                               null,
                                                                               components);
            observedProperties.add(phenomenon);
        }

        // feature of interest
        List<ReferenceEntry> fois = new ArrayList<ReferenceEntry>();
        for (String foiID : getVariables("var18")) {
            fois.add(new ReferenceEntry(null, foiID));
        }

        //static part
        List<String> responseFormat = Arrays.asList("application/xml");
        List<QName> resultModel     = Arrays.asList(observation_QNAME);
        List<ResponseModeType> responseMode = Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
        ObservationOfferingEntry offering = new ObservationOfferingEntry(offeringName,
                                                                         offeringName,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         srsName,
                                                                         time, 
                                                                         procedures,
                                                                         observedProperties,
                                                                         fois,
                                                                         responseFormat, 
                                                                         resultModel, 
                                                                         responseMode);
        return offering;
    }

    @Override
    public List<ObservationOfferingEntry> getObservationOfferings() throws WebServiceException {
        loadData(Arrays.asList("var01"), null);
        List<ObservationOfferingEntry> offerings = new ArrayList<ObservationOfferingEntry>();
        List<String> offeringNames = getVariables("var01");
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName));
        }
        return offerings;
    }

    /**
     *  TODO return composite phenomenon
     */
    @Override
    public PhenomenonEntry getPhenomenon(String phenomenonName) throws WebServiceException {
        loadData(Arrays.asList("var13", "var14"), phenomenonName);
        PhenomenonEntry phenomenon = new PhenomenonEntry(phenomenonName, getVariable("var13"), getVariable("var14"));
        return phenomenon;
    }

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureId) throws WebServiceException {
        loadData(Arrays.asList("var19", "var20", "var21", "var22", "var23", "var24"), samplingFeatureId);
        String name            = getVariable("var19");
        String description     = getVariable("var20");
        String sampledFeature  = getVariable("var21");
        
        String pointID         = getVariable("var22");
        String SRSname         = getVariable("var23");

        String dimension       = getVariable("var24");
        int srsDimension       = 0;
        try {
            srsDimension       = Integer.parseInt(dimension);
        } catch (NumberFormatException ex) {
            logger.severe("unable to parse the srs dimension: " + dimension);
        }
        List<Double> coordinates = getCoordinates(samplingFeatureId);
        DirectPositionType pos = new DirectPositionType(SRSname, srsDimension, coordinates);
        PointType location     = new PointType(pointID, pos);

        SamplingPointEntry foi = new SamplingPointEntry(samplingFeatureId, name, description, sampledFeature, location);
        return foi;
    }

    private List<Double> getCoordinates(String samplingFeatureId) throws WebServiceException {
        loadData(Arrays.asList("var25"), samplingFeatureId);
        List<Double> result = new ArrayList<Double>();
        List<String> coordinates = getVariables("var25");
        for (String coordinate : coordinates) {
            try {
                result.add(Double.parseDouble(coordinate));
            } catch (NumberFormatException ex) {
                throw new WebServiceException(ex, NO_APPLICABLE_CODE);
            }
        }
        return result;
    }

    @Override
    public ObservationEntry getObservation(String identifier) throws WebServiceException {
        loadData(Arrays.asList("var26", "var27", "var28", "var29", "var30", "var31"), identifier);
        SamplingFeatureEntry featureOfInterest = getFeatureOfInterest(getVariable("var26"));
        PhenomenonEntry observedProperty = getPhenomenon(getVariable("var27"));
        ProcessEntry procedure = new ProcessEntry(getVariable("var28"));

        TimePeriodType samplingTime = new TimePeriodType(getVariable("var29"), getVariable("var30"));
        AnyResultEntry anyResult = getResult(getVariable("var31"));
        DataArrayEntry result = anyResult.getArray();
        ObservationEntry observation = new ObservationEntry(identifier,
                                                            null,
                                                            featureOfInterest,
                                                            observedProperty,
                                                            procedure,
                                                            Distribution.NORMAL,
                                                            result,
                                                            samplingTime);
        return observation;
    }

    @Override
    public AnyResultEntry getResult(String identifier) throws WebServiceException {
        loadData(Arrays.asList("var32", "var33"), identifier);
        int count = Integer.parseInt(getVariable("var32"));
        TextBlockEntry encoding = new TextBlockEntry("encoding-1", ",", "@@", ".");
        //TODO
        SimpleDataRecordEntry elementType = new SimpleDataRecordEntry();
        String values = getVariable("var33");
        DataArrayEntry result = new DataArrayEntry(identifier, count, elementType, encoding, values);
        return new AnyResultEntry(identifier, result);
    }

    @Override
    public ReferenceEntry getReference(String href) throws WebServiceException {
        //TODO
        return new ReferenceEntry(null, href);
    }


    @Override
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

            pool.shutdown();
        } catch (SQLException ex) {
            logger.severe("SQLException while destroying Generic metadata reader");
        }
        singleValue.clear();
        multipleValue.clear();
        logger.info("destroying generic reader");
    }

}
