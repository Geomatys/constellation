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

    public GenericObservationReader(String observationIdBase, Connection connection, Automatic configuration) throws WebServiceException {
        super(observationIdBase);
        try {
            initStatement(connection, configuration);
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
    private void loadData(String identifier) {
        logger.finer("loading data for " + identifier);
        singleValue.clear();
        multipleValue.clear();

        if (isThreadEnabled)
            paraleleLoading(identifier, singleStatements.keySet(), multipleStatements.keySet());
        else
            sequentialLoading(identifier, singleStatements.keySet(), multipleStatements.keySet());
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
                ResultSet result = stmt.executeQuery();
                if (result.next()) {
                    for (String varName : singleStatements.get(stmt)) {
                        singleValue.put(varName, result.getString(varName));
                    }
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

        //we extract the multiple values
        for (PreparedStatement stmt : subMultiStmts) {
            try {
                fillStatement(stmt, identifier);

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
                        ResultSet result = stmt.executeQuery();
                        if (result.next()) {
                            for (String varName : singleStatements.get(stmt)) {
                                singleValue.put(varName, result.getString(varName));
                            }
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
    
    @Override
    public List<String> getOfferingNames() throws WebServiceException {
        return getVariables("var01");
    }

    @Override
    public List<String> getProcedureNames() throws WebServiceException {
        return getVariables("var02");
    }

    @Override
    public List<String> getPhenomenonNames() throws WebServiceException {
        return getVariables("var03");
    }

    @Override
    public List<String> getFeatureOfInterestNames() throws WebServiceException {
        return getVariables("var04");
    }

    @Override
    public String getNewObservationId() throws WebServiceException {
        return getVariable("var05");
    }

    @Override
    public String getMinimalEventTime() throws WebServiceException {
        return getVariable("var06");
    }

    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws WebServiceException {
        List<String> srsName = getVariables("var07");
        
        // event time
        String offeringBegin = getVariable("var08");
        String offeringEnd   = getVariable("var09");
        TimePeriodType time  = new TimePeriodType(offeringBegin, offeringEnd);

        // procedure
        List<ReferenceEntry> procedures = new ArrayList<ReferenceEntry>();
        for (String procedureName : getVariables("var09")) {
            procedures.add(new ReferenceEntry(null, procedureName));
        }

        // phenomenon
        List<PhenomenonEntry> observedProperties = new ArrayList<PhenomenonEntry>();
        for (String phenomenonId : getVariables("var10")) {
            PhenomenonEntry phenomenon = getPhenomenon(phenomenonId);
            observedProperties.add(phenomenon);
        }
        for (String phenomenonId : getVariables("var11")) {
            List<PhenomenonEntry> components = new ArrayList<PhenomenonEntry>();
            for (String componentID : getVariables("var16")) {
                components.add(getPhenomenon(componentID));
            }
            CompositePhenomenonEntry phenomenon = new CompositePhenomenonEntry(phenomenonId,
                                                                               getVariable("var14"),
                                                                               getVariable("var15"),
                                                                               null,
                                                                               components);
            observedProperties.add(phenomenon);
        }

        // feature of interest
        List<ReferenceEntry> fois = new ArrayList<ReferenceEntry>();
        for (String foiID : getVariables("var17")) {
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
       PhenomenonEntry phenomenon = new PhenomenonEntry(phenomenonName, getVariable("var12"), getVariable("var13"));
       return phenomenon;
    }

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureId) throws WebServiceException {

        String name            = getVariable("var18");
        String description     = getVariable("var19");
        String sampledFeature  = getVariable("var20");
        
        String pointID         = getVariable("var21");
        String SRSname         = getVariable("var22");
        int srsDimension       = Integer.parseInt(getVariable("var23"));
        List<Double> coordinates = getCoordinates(samplingFeatureId);
        DirectPositionType pos = new DirectPositionType(SRSname, srsDimension, coordinates);
        PointType location     = new PointType(pointID, pos);

        SamplingPointEntry foi = new SamplingPointEntry(samplingFeatureId, name, description, sampledFeature, location);
        return foi;
    }

    private List<Double> getCoordinates(String samplingFeatureId) throws WebServiceException {
        List<Double> result = new ArrayList<Double>();
        List<String> coordinates = getVariables("var24");
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
        SamplingFeatureEntry featureOfInterest = getFeatureOfInterest(getVariable("var25"));
        PhenomenonEntry observedProperty = getPhenomenon(getVariable("var26"));
        ProcessEntry procedure = new ProcessEntry(getVariable("var27"));

        TimePeriodType samplingTime = new TimePeriodType(getVariable("var28"), getVariable("var29"));
        AnyResultEntry anyResult = getResult(getVariable("var30"));
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
        int count = Integer.parseInt(getVariable("var31"));
        TextBlockEntry encoding = new TextBlockEntry("encoding-1", ",", "@@", ".");
        //TODO
        SimpleDataRecordEntry elementType = null;
        String values = getVariable("var32");
        DataArrayEntry result = new DataArrayEntry(identifier, count, elementType, encoding, values);
        return new AnyResultEntry(identifier, result);
    }

    @Override
    public List<ReferenceEntry> getReferences() throws WebServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
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
