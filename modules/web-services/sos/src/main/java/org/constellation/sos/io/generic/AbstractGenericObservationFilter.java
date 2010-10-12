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

import java.util.Map;
import org.constellation.sos.factory.AbstractSOSFactory;
import java.util.logging.Level;
import java.io.File;
import javax.xml.bind.JAXBException;
import java.sql.SQLException;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import javax.xml.bind.Unmarshaller;
import javax.sql.DataSource;
import java.util.logging.Logger;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.FilterQuery;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.ws.CstlServiceException;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public abstract class AbstractGenericObservationFilter implements ObservationFilter {

    /**
     * The base whole configuration query extract from the file Affinage.xml
     */
    protected final FilterQuery configurationQuery;

    /**
     *  The current query built by the sos worker in the scope of a getObservation/getResult request.
     */
    protected FilterQuery currentQuery;

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

    
    protected Level logLevel = Level.INFO;

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos.io.generic");

    public AbstractGenericObservationFilter(Automatic configuration, Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase         = (String) properties.get(AbstractSOSFactory.OBSERVATION_ID_BASE);
        this.observationTemplateIdBase = (String) properties.get(AbstractSOSFactory.OBSERVATION_TEMPLATE_ID_BASE);
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final File affinage = new File(configuration.getConfigurationDirectory(), "affinage.xml");
            if (affinage.exists()) {
                final Object object = unmarshaller.unmarshal(affinage);
                if (object instanceof FilterQuery)
                    this.configurationQuery = (FilterQuery) object;
                else
                    throw new CstlServiceException("Invalid content in affinage.xml", NO_APPLICABLE_CODE);
            } else {
                throw new CstlServiceException("Unable to find affinage.xml", NO_APPLICABLE_CODE);
            }
            GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
            this.dataSource = db.getDataSource();
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXBException in Generic Observation Filter constructor", NO_APPLICABLE_CODE);
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the observation filter:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }

    public AbstractGenericObservationFilter(AbstractGenericObservationFilter that) {
        this.observationIdBase         = that.observationIdBase;
        this.observationTemplateIdBase = that.observationTemplateIdBase;
        this.configurationQuery        = that.configurationQuery;
        this.dataSource                = that.dataSource;
        this.logLevel                  = that.logLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoglevel(Level logLevel) {
         this.logLevel = logLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        // do nothing
    }
}
