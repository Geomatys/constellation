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
import org.constellation.swe.v101.AbstractDataComponentEntry;
import org.constellation.swe.v101.AnyResultEntry;
import org.constellation.swe.v101.AnyScalarPropertyType;
import org.constellation.swe.v101.BooleanType;
import org.constellation.swe.v101.CompositePhenomenonEntry;
import org.constellation.swe.v101.DataArrayEntry;
import org.constellation.swe.v101.DataArrayPropertyType;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.swe.v101.QuantityType;
import org.constellation.swe.v101.SimpleDataRecordEntry;
import org.constellation.swe.v101.TextBlockEntry;
import org.constellation.swe.v101.TimeType;
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
    private Values loadData(List<String> variables, String value) {

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
    private List<String> getVariables(String variable, Values val) {
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
    private String getVariable(String variable, Values values) {
        return values.singleValue.get(variable);
    }
    
    @Override
    public List<String> getOfferingNames() throws WebServiceException {
        Values values = loadData(Arrays.asList("var01"), null);
        return getVariables("var01", values);
    }

    @Override
    public List<String> getProcedureNames() throws WebServiceException {
        Values values = loadData(Arrays.asList("var02"), null);
        return getVariables("var02", values);
    }

    @Override
    public List<String> getPhenomenonNames() throws WebServiceException {
        Values values = loadData(Arrays.asList("var03"), null);
        return getVariables("var03", values);
    }

    @Override
    public List<String> getFeatureOfInterestNames() throws WebServiceException {
        Values values = loadData(Arrays.asList("var04"), null);
        return getVariables("var04", values);
    }

    @Override
    public String getNewObservationId() throws WebServiceException {
        Values values = loadData(Arrays.asList("var05"), null);
        int id = Integer.parseInt(getVariable("var05", values));

        values = loadData(Arrays.asList("var44"), observationIdBase + id);
        String _continue = null;
        do {
            id++;
            _continue = getVariable("var44", values);

        } while (_continue != null);
        return observationIdBase + id;
    }

    @Override
    public String getMinimalEventTime() throws WebServiceException {
         Values values = loadData(Arrays.asList("var06"), null);
        return getVariable("var06", values);
    }

    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws WebServiceException {
        Values values = loadData(Arrays.asList("var07", "var08", "var09", "var10", "var11", "var12", "var18"), offeringName);
        List<String> srsName = getVariables("var07", values);
        
        // event time
        TimePeriodType time;
        String offeringBegin = getVariable("var08", values);
        if (offeringBegin != null)
            offeringBegin        = offeringBegin.replace(' ', 'T');
        String offeringEnd   = getVariable("var09", values);
        if (offeringEnd != null) {
            offeringEnd          = offeringEnd.replace(' ', 'T');
            time  = new TimePeriodType(offeringBegin, offeringEnd);
        } else {
            time  = new TimePeriodType(offeringBegin);
        }


        // procedure
        List<ReferenceEntry> procedures = new ArrayList<ReferenceEntry>();
        for (String procedureName : getVariables("var10", values)) {
            procedures.add(new ReferenceEntry(null, procedureName));
        }

        // phenomenon
        List<PhenomenonEntry> observedProperties = new ArrayList<PhenomenonEntry>();
        for (String phenomenonId : getVariables("var12", values)) {
            if (phenomenonId!= null && !phenomenonId.equals("")) {
                Values compositeValues = loadData(Arrays.asList("var17"), phenomenonId);
                List<PhenomenonEntry> components = new ArrayList<PhenomenonEntry>();
                for (String componentID : getVariables("var17", compositeValues)) {
                    components.add(getPhenomenon(componentID));
                }
                compositeValues = loadData(Arrays.asList("var15", "var16"), phenomenonId);
                CompositePhenomenonEntry phenomenon = new CompositePhenomenonEntry(phenomenonId,
                                                                                   getVariable("var15", compositeValues),
                                                                                   getVariable("var16", compositeValues),
                                                                                   null,
                                                                                   components);
                observedProperties.add(phenomenon);
            }
        }
        for (String phenomenonId : getVariables("var11", values)) {
            if (phenomenonId != null && !phenomenonId.equals("")) {
                PhenomenonEntry phenomenon = getPhenomenon(phenomenonId);
                observedProperties.add(phenomenon);
            }
        }

        // feature of interest
        List<ReferenceEntry> fois = new ArrayList<ReferenceEntry>();
        for (String foiID : getVariables("var18", values)) {
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
        Values values = loadData(Arrays.asList("var01"), null);
        List<ObservationOfferingEntry> offerings = new ArrayList<ObservationOfferingEntry>();
        List<String> offeringNames = getVariables("var01", values);
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
        Values values = loadData(Arrays.asList("var13", "var14"), phenomenonName);
        PhenomenonEntry phenomenon = new PhenomenonEntry(phenomenonName, getVariable("var13", values), getVariable("var14", values));
        return phenomenon;
    }

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureId) throws WebServiceException {
        Values values = loadData(Arrays.asList("var19", "var20", "var21", "var22", "var23", "var24"), samplingFeatureId);
        String name            = getVariable("var19", values);
        String description     = getVariable("var20", values);
        String sampledFeature  = getVariable("var21", values);
        
        String pointID         = getVariable("var22", values);
        String SRSname         = getVariable("var23", values);

        String dimension       = getVariable("var24", values);
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
        Values values = loadData(Arrays.asList("var25"), samplingFeatureId);
        List<Double> result = new ArrayList<Double>();
        List<String> coordinates = getVariables("var25", values);
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
        Values values = loadData(Arrays.asList("var26", "var27", "var28", "var29", "var30", "var31"), identifier);
        SamplingFeatureEntry featureOfInterest = getFeatureOfInterest(getVariable("var26", values));
        PhenomenonEntry observedProperty = getPhenomenon(getVariable("var27", values));
        ProcessEntry procedure = new ProcessEntry(getVariable("var28", values));

        TimePeriodType samplingTime = new TimePeriodType(getVariable("var29", values), getVariable("var30", values));
        AnyResultEntry anyResult = getResult(getVariable("var31", values));
        DataArrayEntry dataArray = anyResult.getArray();
        DataArrayPropertyType result = new DataArrayPropertyType(dataArray);
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
        Values values = loadData(Arrays.asList("var32", "var33", "var34", "var35", "var36", "var37", "var38", "var39",
                "var40", "var41", "var42", "var43"), identifier);
        int count = Integer.parseInt(getVariable("var32", values));

        // encoding
        String encodingID       = getVariable("var34", values);
        String tokenSeparator   = getVariable("var35", values);
        String decimalSeparator = getVariable("var36", values);
        String blockSeparator   = getVariable("var37", values);
        TextBlockEntry encoding = new TextBlockEntry(encodingID, tokenSeparator, blockSeparator, decimalSeparator);
        
        //data block description
        String blockId          = getVariable("var38", values);
        String dataRecordId     = getVariable("var39", values);
        Set<AnyScalarPropertyType> fields = new HashSet<AnyScalarPropertyType>();
        List<String> fieldNames = getVariables("var40", values);
        List<String> fieldDef   = getVariables("var41", values);
        List<String> type       = getVariables("var42", values);
        List<String> uomCodes   = getVariables("var43", values);
        for(int i = 0; i < fieldNames.size(); i++) {
            AbstractDataComponentEntry component = null;
            String typeName   = type.get(i);
            String definition = fieldDef.get(i);
            String uomCode    = uomCodes.get(i);
            if (typeName != null) {
                if (typeName.equals("Quantity")) {
                    component = new QuantityType(definition, uomCode, null);
                } else if (typeName.equals("Time")) {
                    component = new TimeType(definition, uomCode, null);
                } else if (typeName.equals("Boolean")) {
                    component = new BooleanType(definition, null);
                } else {
                    logger.severe("unexpected field type");
                }
            }
            AnyScalarPropertyType field = new AnyScalarPropertyType(dataRecordId, blockId, component);
            fields.add(field);
        }

        SimpleDataRecordEntry elementType = new SimpleDataRecordEntry(blockId, dataRecordId, null, false, fields);

        String dataValues = getVariable("var33", values);
        DataArrayEntry result = new DataArrayEntry(blockId, count, elementType, encoding, dataValues);
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
        logger.info("destroying generic reader");
    }

    private class Values {

        /**
         * A Map of varName - value refreshed at every request.
         */
        private Map<String, String> singleValue;
        /**
         * * A Map of varName - list of value refreshed at every request.
         */
        private Map<String, List<String>> multipleValue;

        public Values() {
            singleValue     = new HashMap<String, String>();
            multipleValue   = new HashMap<String, List<String>>();
        }

        public void addSingleValue(String varName, String value) {
            singleValue.put(varName, value);
        }

        public void createNewMultipleValue(String varName) {
            multipleValue.put(varName, new ArrayList<String>());
        }

        public void addToMultipleValue(String varName, String value) {
            if (multipleValue.get(varName) != null)
                multipleValue.get(varName).add(value);
        }
    }

}
