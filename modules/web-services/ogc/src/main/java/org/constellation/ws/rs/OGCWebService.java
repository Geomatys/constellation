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
package org.constellation.ws.rs;

// J2SE dependencies

import net.iharder.Base64;
import org.apache.sis.util.iso.Types;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.ConfigurationException;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.security.IncorrectCredentialsException;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.security.UnknownAccountException;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.StringUtilities;
import org.opengis.util.CodeList;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_CRS;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_POINT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_REQUEST;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_UPDATE_SEQUENCE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_DEFINED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.STYLE_NOT_DEFINED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;

// Apache SIS dependencies


/**
 * Abstract parent REST facade for all OGC web services in Constellation.
 * <p>
 * This class
 * </p>
 * <p>
 * The Open Geospatial Consortium (OGC) has defined a number of web services for
 * geospatial data such as:
 * <ul>
 *   <li><b>CSW</b> -- Catalog Service for the Web</li>
 *   <li><b>WMS</b> -- Web Map Service</li>
 *   <li><b>WCS</b> -- Web Coverage Service</li>
 *   <li><b>SOS</b> -- Sensor Observation Service</li>
 * </ul>
 * Many of these Web Services have been defined to work with REST based HTTP
 * message exchange; this class provides base functionality for those services.
 * </p>
 *
 * @version $Id$
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Garcia Benjamin (Geomatys)
 *
 * @version 0.9
 * @since 0.3
 */
public abstract class OGCWebService<W extends Worker> extends WebService {

    private final String serviceName;
    
    @Inject
    private ServiceBusiness serviceBusiness;
    
    /**
     * Initialize the basic attributes of a web serviceType.
     *
     * @param specification The OGC specification.
     */
    public OGCWebService(final Specification specification) {
        super();
        if (specification == null){
            throw new IllegalArgumentException("It is compulsory for a web service to have a specification.");
        }
        SpringHelper.injectDependencies(this);
        this.serviceName = specification.name();

        LOGGER.log(Level.INFO, "Starting the REST {0} service facade.\n", serviceName);
        WSEngine.registerService(serviceName, "REST", getWorkerClass(), getConfigurerClass());

        /*
         * build the map of Workers, by scanning the sub-directories of its
         * service directory.
         */
        if (!WSEngine.isSetService(serviceName)) {
            try {
                serviceBusiness.start(serviceName.toLowerCase());
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.WARNING, "Error while starting services for :" + serviceName, ex);
            }
        } else {
            LOGGER.log(Level.INFO, "Workers already set for {0}", serviceName);
        }
    }

    /**
     * @return the worker class of the service.
     */
    protected abstract Class getWorkerClass();

    /**
     * Returns the {@link ServiceConfigurer} class implementation.
     *
     * @return the implementation {@link Class}
     */
    protected abstract Class<? extends ServiceConfigurer> getConfigurerClass();

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isRequestValidationActivated(final String serviceID) {
        if (serviceID != null && WSEngine.serviceInstanceExist(serviceName, serviceID)) {
            final W worker = (W) WSEngine.getInstance(serviceName, serviceID);
            return worker.isRequestValidationActivated();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Schema> getRequestValidationSchema(final String serviceID) {
        if (serviceID != null && WSEngine.serviceInstanceExist(serviceName, serviceID)) {
            final W worker = (W) WSEngine.getInstance(serviceName, serviceID);
            return worker.getRequestValidationSchema();
        }
        return new ArrayList<>();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(final Object request) {
        try {
              processAuthentication();
        } catch (UnknownAccountException ex) {
            LOGGER.log(Level.FINER, "Unknow acount", ex);
            SecurityManagerHolder.getInstance().logout();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (IncorrectCredentialsException ex) {
            LOGGER.log(Level.FINER, "incorrect password", ex);
            SecurityManagerHolder.getInstance().logout();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        final Object objectRequest;
        if (request instanceof JAXBElement) {
            objectRequest = ((JAXBElement) request).getValue();
            LOGGER.log(Level.FINER, "request type:{0}", request.getClass().getName());
        } else {
            objectRequest = request;
        }

        final String serviceID = getSafeParameter("serviceId");

        // request is send to the specified worker
        if (serviceID != null && WSEngine.serviceInstanceExist(serviceName, serviceID)) {
            final W worker = (W) WSEngine.getInstance(serviceName, serviceID);
            if (worker.isSecured()) {
                final String ip = getHttpServletRequest().getRemoteAddr();
                final String referer = httpHeaders.getHeaderString("referer");
                if (!worker.isAuthorized(ip, referer)) {
                    LOGGER.log(Level.INFO, "Received a request from unauthorized ip:{0} or referer:{1}",
                            new String[]{ip, referer});
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            }
            if (worker.isPostRequestLog()) {
                logPostParameters(request);
            }
            if (worker.isPrintRequestParameter()) {
                logParameters();
            }
            worker.setServiceUrl(getServiceURL());

            return treatIncomingRequest(objectRequest, worker);

        // unbounded URL
        } else {
            LOGGER.log(Level.WARNING, "Received request on undefined instance identifier:{0}", serviceID);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private void processAuthentication() throws UnknownAccountException, IncorrectCredentialsException{
        if (httpHeaders != null) {
            final String authorization = httpHeaders.getHeaderString("authorization");
            if (authorization != null) {
                if (authorization.startsWith("Basic ")) {
                    final String toDecode = authorization.substring(6);
                    try {
                        final String logPass = new String(Base64.decode(toDecode));
                        final int separatorIndex = logPass.indexOf(":");
                        if (separatorIndex != -1) {
                            final String login = logPass.substring(0, separatorIndex);
                            final String passw = logPass.substring(separatorIndex + 1);
                            SecurityManagerHolder.getInstance().login(login, passw);
                        } else {
                            LOGGER.warning("separator missing in authorization header");
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "IO exception while cdecoding basic authentication", ex);
                    }
                } else {
                    LOGGER.info("only basic authorization are handled for now");
                }
            }
        }
    }

    /**
     * Service specific task which has to be executed when a restart is asked.
     *
     * @param identifier the instance identifier or {@code null} for all the instance.
     */
    protected void specificRestart(final String identifier) {
        // do nothing in this implementation
    }


    /**
     * Treat the incoming request and call the right function.
     *
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this parameter is {@code null}
     *
     * @param worker the selected worker on which apply the request.
     *
     * @return an xml response.
     */
    protected abstract Response treatIncomingRequest(final Object objectRequest,final  W worker);

    /**
     * Handle all exceptions returned by a web service operation in two ways:
     * <ul>
     *   <li>if the exception code indicates a mistake done by the user, just display a single
     *       line message in logs.</li>
     *   <li>otherwise logs the full stack trace in logs, because it is something interesting for
     *       a developer</li>
     * </ul>
     * In both ways, the exception is then marshalled and returned to the client.
     *
     * @param ex         The exception that has been generated during the web-service operation requested.
     * @param serviceDef The service definition, from which the version number of exception report will
     *                   be extracted.
     * @return An XML representing the exception.
     */
    protected abstract Response processExceptionResponse(final CstlServiceException ex, final ServiceDef serviceDef, final Worker w);

    /**
     * The shared method to build a service ExceptionReport.
     *
     * @param message
     * @param codeName
     * @return
     */
    @Override
    protected Response launchException(final String message, String codeName, final String locator) {
        final String serviceID = getSafeParameter("serviceId");
        final W worker = (W) WSEngine.getInstance(serviceName, serviceID);
        ServiceDef mainVersion = null;
        if (worker != null) {
            mainVersion = worker.getBestVersion(null);
            if (mainVersion.owsCompliant) {
                codeName = StringUtilities.transformCodeName(codeName);
            }
        }
        final OWSExceptionCode code   = Types.forCodeName(OWSExceptionCode.class, codeName, true);
        final CstlServiceException ex = new CstlServiceException(message, code, locator);
        return processExceptionResponse(ex, mainVersion, worker);
    }

    /**
     * Return the correct representation of an OWS exceptionCode
     *
     * @param exceptionCode
     * @return
     */
    protected String getOWSExceptionCodeRepresentation(final CodeList exceptionCode) {
        final String codeRepresentation;
        if (exceptionCode instanceof org.constellation.ws.ExceptionCode) {
            codeRepresentation = StringUtilities.transformCodeName(exceptionCode.name());
        } else {
            codeRepresentation = exceptionCode.name();
        }
        return codeRepresentation;
    }

    /**
     * We don't print the stack trace:
     * - if the user have forget a mandatory parameter.
     * - if the version number is wrong.
     * - if the user have send a wrong request parameter
     */
    protected void logException(final CstlServiceException ex) {
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)    && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.MISSING_PARAMETER_VALUE) &&
            !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.VERSION_NEGOTIATION_FAILED) &&
            !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)    && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_PARAMETER_VALUE) &&
            !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)    && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.OPERATION_NOT_SUPPORTED) &&
            !ex.getExceptionCode().equals(STYLE_NOT_DEFINED)          && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.STYLE_NOT_DEFINED) &&
            !ex.getExceptionCode().equals(INVALID_POINT)              && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_POINT) &&
            !ex.getExceptionCode().equals(INVALID_FORMAT)             && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_FORMAT) &&
            !ex.getExceptionCode().equals(INVALID_CRS)                && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_CRS) &&
            !ex.getExceptionCode().equals(LAYER_NOT_DEFINED)          && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.LAYER_NOT_DEFINED) &&
            !ex.getExceptionCode().equals(INVALID_REQUEST)            && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_REQUEST) &&
            !ex.getExceptionCode().equals(INVALID_UPDATE_SEQUENCE)    && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_UPDATE_SEQUENCE) &&
            !ex.getExceptionCode().equals(INVALID_VALUE) &&
            !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_SRS)) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
        }
    }

    /**
     * Return the number of instance if the web-service
     */
    protected int getWorkerMapSize() {
        return WSEngine.getInstanceSize(serviceName);
    }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    @Override
    public void destroy() {
        super.destroy();
        LOGGER.log(Level.INFO, "Shutting down the REST {0} service facade.", serviceName);
        WSEngine.destroyInstances(serviceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MarshallerPool getConfigurationPool() {
        return GenericDatabaseMarshallerPool.getInstance();
    }

}
