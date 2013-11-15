/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2011, Geomatys
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

package org.constellation.metadata.configuration;

// J2SE dependencies
import java.io.File;
import java.util.logging.Level;
import javax.ws.rs.core.MultivaluedMap;

// constellation dependencies
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.RestartServiceDescriptor;
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 * The base for The CSW configurer.
 *
 * @author Guilhem Legal
 */
public abstract class AbstractCSWConfigurer extends AbstractConfigurer {

    /**
     * Build a new CSW configurer.
     *
     * @throws ConfigurationException
     */
    public AbstractCSWConfigurer() throws ConfigurationException {
    }


    @Override
    public Object treatRequest(final String request, final MultivaluedMap<String,String> parameters, final Object objectRequest) throws CstlServiceException {

        try {
            if ("RefreshIndex".equalsIgnoreCase(request)) {
                final boolean asynchrone = getBooleanParameter("ASYNCHRONE", false, parameters);
                final String id          = getParameter("ID", true, parameters);
                final boolean forced     = getBooleanParameter("FORCED", false, parameters);

                final AcknowlegementType ack = CSWConfigurationManager.getInstance().refreshIndex(id, asynchrone, forced);
                if (!asynchrone && ack.getStatus().equals("Success")) {
                    restartCSW(id);
                }
                return ack;
            }

            if ("AddToIndex".equalsIgnoreCase(request)) {

                final String id = getParameter("ID", true, parameters);
                final String identifierList = getParameter("IDENTIFIERS", true, parameters);
                return CSWConfigurationManager.getInstance().addToIndex(id, identifierList);
            }

            if ("RemoveFromIndex".equalsIgnoreCase(request)) {

                final String id = getParameter("ID", true, parameters);
                final String identifierList = getParameter("IDENTIFIERS", true, parameters);
                return CSWConfigurationManager.getInstance().removeFromIndex(id, identifierList);
            }

            if ("stopIndex".equalsIgnoreCase(request)) {

                final String id = getParameter("ID", false, parameters);
                return CSWConfigurationManager.getInstance().stopIndexation(id);
            }

            if ("importRecords".equalsIgnoreCase(request)) {

                final String id       = getParameter("ID", true, parameters);
                final String fileName = getParameter("fileName", true, parameters);
                return CSWConfigurationManager.getInstance().importRecords(id, (File)objectRequest, fileName);
            }

            if ("deleteRecords".equalsIgnoreCase(request)) {

                final String id       = getParameter("ID", true, parameters);
                final String metadata = getParameter("metadata", true, parameters);
                return CSWConfigurationManager.getInstance().deleteMetadata(id, metadata);
            }

            if ("deleteAllRecords".equalsIgnoreCase(request)) {

                final String id       = getParameter("ID", true, parameters);
                return CSWConfigurationManager.getInstance().deleteAllMetadata(id);
            }

            if ("metadataExist".equalsIgnoreCase(request)) {

                final String id       = getParameter("ID", true, parameters);
                final String metadata = getParameter("metadata", true, parameters);
                return CSWConfigurationManager.getInstance().metadataExist(id, metadata);
            }

            if ("GetCSWDatasourceType".equalsIgnoreCase(request)) {
                return CSWConfigurationManager.getInstance().getAvailableCSWDataSourceType();
            }
        } catch (ConfigurationException ex) {
            throw new CstlServiceException(ex);
        }
        return null;
    }

    @Override
    public boolean isLock() {
        return CSWConfigurationManager.getInstance().isIndexing();
    }

    @Override
    public void closeForced() {
        AbstractIndexer.stopIndexation();
    }

    /**
     * Reload a CSW Instance.
     *
     * @param identifier the identifier of the CSW instance to restart.
     * @return {@code true} if the CSW instance successfully restart.
     */
    protected boolean restartCSW(final String identifier) {
        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);
            final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue("CSW");
            inputs.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
            inputs.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(true);
            desc.createProcess(inputs).call();
            return true;
        } catch (ProcessException ex) {
            LOGGER.log(Level.WARNING, "Process to restart a service instance has reported an error.", ex);
        } catch (NoSuchIdentifierException ex) {
            LOGGER.log(Level.WARNING, "Unable to find the RestartService process");
        }
        return false;
    }
}
