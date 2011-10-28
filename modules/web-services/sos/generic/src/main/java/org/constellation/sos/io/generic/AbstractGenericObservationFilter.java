/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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


import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.sql.SQLException;
import javax.sql.DataSource;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Query;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.factory.OMFactory;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.util.logging.Logging;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public abstract class AbstractGenericObservationFilter implements ObservationFilter {

    /**
     * The base whole configuration query extract from the file Affinage.xml
     */
    protected final Query configurationQuery;

    /**
     * A map of static variable to replace in the statements.
     */
    protected HashMap<String, String> staticParameters = new HashMap<String, String>();
    
    /**
     *  The current query built by the sos worker in the scope of a getObservation/getResult request.
     */
    protected Query currentQuery;

     /**
     * The base for observation id.
     */
    protected String observationIdBase;

    /**
     * The base for observation id.
     */
    protected String observationTemplateIdBase;
    
    /**
     * The O&M data source
     */
    protected DataSource dataSource;

    /**
     * A flag indicating that the service is trying to reconnect the database.
     */
    private boolean isReconnecting = false;

     /**
     * The database informations.
     */
    private Automatic configuration;
    
    
    protected Level logLevel = Level.INFO;

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos.io.generic");

    public AbstractGenericObservationFilter(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase         = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.observationTemplateIdBase = (String) properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        this.configuration = configuration;
        
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            this.dataSource = db.getDataSource();
            final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final File affinage = new File(configuration.getConfigurationDirectory(), "affinage.xml");
            if (affinage.exists()) {
                final Object object = unmarshaller.unmarshal(affinage);
                if (object instanceof Query) {
                    this.configurationQuery = (Query) object;
                    if (configurationQuery.getStatique() != null) {
                        for (Query query : configurationQuery.getStatique().getQuery()) {
                            processStatiqueQuery(query);
                        }
                    } 
                } else {
                    throw new CstlServiceException("Invalid content in affinage.xml", NO_APPLICABLE_CODE);
                }
            } else {
                throw new CstlServiceException("Unable to find affinage.xml", NO_APPLICABLE_CODE);
            }
            GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXBException in Generic Observation Filter constructor", NO_APPLICABLE_CODE);
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the observation filter:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    public AbstractGenericObservationFilter(final AbstractGenericObservationFilter that) {
        this.observationIdBase         = that.observationIdBase;
        this.observationTemplateIdBase = that.observationTemplateIdBase;
        this.configurationQuery        = that.configurationQuery;
        this.dataSource                = that.dataSource;
        this.logLevel                  = that.logLevel;
        this.staticParameters          = that.staticParameters;
    }

    private void processStatiqueQuery(final Query query) throws SQLException {
        final Connection connection = acquireConnection();
        final Statement stmt        = connection.createStatement();
        final List<String> varNames = query.getVarNames();
        final String textQuery      = query.buildSQLQuery(staticParameters);
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoglevel(final Level logLevel) {
         this.logLevel = logLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> supportedQueryableResultProperties() {
        return new ArrayList<String>();
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public void setResultEquals(final String propertyName, final String value) throws CstlServiceException{
        throw new CstlServiceException("setResultEquals is not supported by this ObservationFilter implementation.");
    }

    /**
     * Try to reconnect to the database if the connection have been lost.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    protected void reloadConnection() throws CstlServiceException {
        if (!isReconnecting) {
            try {
               LOGGER.info("refreshing the connection");
               BDD db          = configuration.getBdd();
               this.dataSource = db.getDataSource();
               isReconnecting  = false;

            } catch(SQLException ex) {
                LOGGER.log(Level.SEVERE, "SQLException while restarting the connection:{0}", ex);
                isReconnecting = false;
            }
        }
        throw new CstlServiceException("The database connection has been lost, the service is trying to reconnect", NO_APPLICABLE_CODE);
    }
    
    protected Connection acquireConnection() throws SQLException {
        final Connection c = dataSource.getConnection();
        c.setReadOnly(true);
        c.setAutoCommit(false);
        return c;
    }
}
