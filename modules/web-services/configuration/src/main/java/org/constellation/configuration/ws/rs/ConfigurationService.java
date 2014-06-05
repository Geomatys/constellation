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
package org.constellation.configuration.ws.rs;

// J2SE dependencies
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.util.logging.Level;
import java.lang.ref.WeakReference;

// Jersey dependencies
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MultivaluedMap;
import javax.inject.Named;
import javax.inject.Singleton;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.bind.JAXBElement;


// Constellation dependencies
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.configuration.filter.ConfigurerFilter;
import org.constellation.configuration.ExceptionReport;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.rs.WebService;

import static org.constellation.api.QueryConstants.*;
import static org.constellation.ws.ExceptionCode.*;

// Geotoolkit dependencies
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.util.StringUtilities;

// SIS dependencies
import org.apache.sis.xml.MarshallerPool;

/**
 * Web service for administration and configuration operations.
 * <p>
 * This web service enables basic remote management of a Constellation server.
 * </p>
 * 
 * @author Guilhem Legal (Geomatys)
 * @since 0.1
 */
@Named
@Path("configuration")
@Singleton
public final class ConfigurationService extends WebService {

    private static WeakReference<ConfigurationService> INSTANCE = null;

    /**
     * The implementation specific configurers.
     */
    private final List<AbstractConfigurer> configurers = new ArrayList<>();

    /**
     * The factory registry allowing to load the correct implementation specific
     * configurer.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractConfigurerFactory.class);

    /**
     * Construct the ConfigurationService and configure its context.
     */
    public ConfigurationService() {
        super();
        try {
            setXMLContext(GenericDatabaseMarshallerPool.getInstance());
            final Iterator<AbstractConfigurerFactory> ite = factory.getServiceProviders(
                    AbstractConfigurerFactory.class, new ConfigurerFilter(), null, null);
            while (ite.hasNext()) {
                AbstractConfigurerFactory currentfactory = ite.next();
                try {
                    AbstractConfigurer ac = currentfactory.getConfigurer();
                    LOGGER.log(Level.INFO, "Found a configurer:{0}", ac.getClass().getName());
                    configurers.add(ac);
                } catch (ConfigurationException ex) {
                    LOGGER.log(Level.WARNING, "Specific operation will not be available.\nCause:{0}", ex.getMessage());
                }

            }

        } catch (FactoryNotFoundException ex) {
            LOGGER.warning("Factory not found for Configurer, specific operation will not be available.");

        }
        LOGGER.info("Configuration service runing");

        if (INSTANCE == null || INSTANCE.get() == null) {
            INSTANCE = new WeakReference<>(this);
        }

    }

    /**
     * Handle the various types of requests made to the service.
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) {
        try {
            final String request = getParameter(REQUEST_PARAMETER, true);

            if (REQUEST_LIST_SERVICE.equalsIgnoreCase(request)) {
                final ServiceReport response = new ServiceReport(WSEngine.getRegisteredServices());
                return Response.ok(response).build();
            }


            /* specific operations */

            else {
                for (AbstractConfigurer configurer : configurers) {
                    if (objectRequest != null) {
                        if (objectRequest instanceof JAXBElement) {
                            objectRequest = ((JAXBElement) objectRequest).getValue();
                        }
                        LOGGER.log(Level.FINER, "request type:{0}", request.getClass().getName());
                    }
                    final Object response = configurer.treatRequest(request, getUriContext().getQueryParameters(),
                            objectRequest);
                    if (response != null) {
                        return Response.ok(response).build();
                    }
                }
            }

            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                    OPERATION_NOT_SUPPORTED, REQUEST_PARAMETER);

        } catch (CstlServiceException ex) {
            final String code = StringUtilities.transformCodeName(ex.getExceptionCode().name());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code);
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)
                    && !ex.getExceptionCode().equals(ExceptionCode.MISSING_PARAMETER_VALUE)
                    && !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)
                    && !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            } else {
                LOGGER.info(ex.getMessage());
            }
            return Response.ok(report).build();

        }
    }

    /**
     * Build a service ExceptionReport
     * 
     * @param message
     * @param codeName
     * @return
     */
    @Override
    protected Response launchException(final String message, final String codeName, final String locator) {
        final ExceptionCode code = ExceptionCode.valueOf(codeName);
        final ExceptionReport report = new ExceptionReport(message, code.name());
        return Response.ok(report).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreDestroy
    public void destroy() {
        super.destroy();
        LOGGER.info("Shutting down the REST Configuration service facade. Disposing " + "all datastore instances.");
        for (AbstractConfigurer configurer : configurers) {
            configurer.destroy();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MarshallerPool getConfigurationPool() {
        return GenericDatabaseMarshallerPool.getInstance();
    }

    @Override
    protected Object unmarshallRequest(final Unmarshaller unmarshaller, final InputStream is) throws JAXBException,
            CstlServiceException {

        final String request = (String) getParameter(REQUEST_PARAMETER, true);
        final MultivaluedMap<String, String> parameters = getParameters();
        for (AbstractConfigurer configurer : configurers) {
            if (configurer.needCustomUnmarshall(request, parameters)) {
                return configurer.unmarshall(request, parameters, is);
            }
        }
        return super.unmarshallRequest(unmarshaller, is);
    }

    @Override
    protected boolean isRequestValidationActivated(final String workerID) {
        return false;
    }

    @Override
    protected List<Schema> getRequestValidationSchema(String workerID) {
        return new ArrayList<>();
    }

}
