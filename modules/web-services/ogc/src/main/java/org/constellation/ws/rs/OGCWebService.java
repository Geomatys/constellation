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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

import net.iharder.Base64;

// Jersey dependencies
import javax.annotation.PreDestroy;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.validation.Schema;

// Shiro dependencies
//import org.apache.shiro.authc.IncorrectCredentialsException;
//import org.apache.shiro.authc.UnknownAccountException;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ExceptionReport;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.StartServiceDescriptor;
import org.constellation.security.IncorrectCredentialsException;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.security.UnknownAccountException;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.ConfigurationEngine;
import static org.constellation.api.QueryConstants.*;

// Geotoolkit dependencies
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Apache SIS dependencies
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.util.iso.Types;

// GeoAPI dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.CodeList;
import org.opengis.util.NoSuchIdentifierException;


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
    protected final OGCConfigurer configurer;

    
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

        this.serviceName = specification.name();
        this.configurer  = (OGCConfigurer) ReflectionUtilities.newInstance(getConfigurerClass());

        LOGGER.log(Level.INFO, "Starting the REST {0} service facade.\n", serviceName);
        WSEngine.registerService(serviceName, "REST", getWorkerClass(), getConfigurerClass());

        /*
         * build the map of Workers, by scanning the sub-directories of its
         * service directory.
         */
        if (!WSEngine.isSetService(serviceName)) {
            startAllInstance();
        } else {
            LOGGER.log(Level.INFO, "Workers already set for {0}", serviceName);
        }
    }

    private void startAllInstance() {
        final List<String> serviceIDs = ConfigurationEngine.getServiceConfigurationIds(serviceName);
        for (String serviceID : serviceIDs) {
            try {
                ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);
                ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                inputs.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
                inputs.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(serviceID);

                org.geotoolkit.process.Process proc = desc.createProcess(inputs);
                proc.call();
            } catch (NoSuchIdentifierException ex) {
                LOGGER.log(Level.WARNING, "StartService process is unreachable.");
            } catch (ProcessException ex) {
                LOGGER.log(Level.WARNING, "Error while starting all instances", ex);
            }
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

        // administration a the instance
        } else if ("admin".equals(serviceID)){
            return treatAdminRequest(objectRequest);

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
     * treat the request sent to the admin instance.
     */
    private Response treatAdminRequest(final Object objectRequest) {
        try {
            final String request = getParameter("request", true);

            /*
             * Restart service instance (kill all the workers and rebuild each one).
             */
            if ("restart".equalsIgnoreCase(request)) {
                LOGGER.info("refreshing the workers");
                final String identifier = getParameter("id", false);
                final String closeFirst = getParameter("closeFirst", false);
                configurer.restartInstance(identifier, closeFirst == null || Boolean.parseBoolean(closeFirst));
                return Response.ok(new AcknowlegementType("Success", "Service instance successfully restarted.")).build();

            /*
             * Start service instance.
             */
            } else if ("start".equalsIgnoreCase(request)) {
                LOGGER.info("starting an instance");
                final String identifier = getParameter("id", true);
                configurer.startInstance(identifier);
                return Response.ok(new AcknowlegementType("Success", "Service instance successfully started.")).build();

            /*
             * Stop service instance.
             */
            } else if ("stop".equalsIgnoreCase(request)) {
                final String identifier = getParameter("id", true);
                configurer.stopInstance(identifier);
                return Response.ok(new AcknowlegementType("Success", "Service instance successfully stopped.")).build();

            /*
             * Delete service instance.
             */
            } else if ("delete".equalsIgnoreCase(request)) {
                LOGGER.info("deleting an instance");
                final String identifier = getParameter("id", true);
                configurer.deleteInstance(identifier);
                return Response.ok(new AcknowlegementType("Success", "Service instance successfully deleted.")).build();

            /*
             * Create service instance.
             */
            } else if ("newInstance".equalsIgnoreCase(request)) {
                LOGGER.info("creating an instance");
                final String identifier = getParameter("id", true);
                if (!ConfigurationEngine.serviceConfigurationExist(serviceName, identifier)) {
                    configurer.createInstance(identifier, null, objectRequest);
                    return Response.ok(new AcknowlegementType("Success", "Service instance successfully created.")).build();
                } else {
                    return Response.ok(new AcknowlegementType("Error", "Unable to create an instance.")).build();
                }

            /*
             * Rename service instance.
             */
            } else if ("renameInstance".equalsIgnoreCase(request)) {
                LOGGER.info("renaming an instance");
                final String identifier = getParameter("id", true);
                final String newName    = getParameter("newName", true);
                configurer.renameInstance(identifier, newName);
                return Response.ok(new AcknowlegementType("Success", "Service instance successfully renamed.")).build();

            /*
             * Set service instance configuration.
             */
            } else if ("configure".equalsIgnoreCase(request)) {
                LOGGER.info("configure an instance");
                final String identifier = getParameter("id", true);
                configurer.setInstanceConfiguration(identifier, objectRequest);
                return Response.ok(new AcknowlegementType("Success", "Service instance configuration successfully updated.")).build();

            /*
             * Get service instance configuration.
             */
            } else if ("getConfiguration".equalsIgnoreCase(request)) {
                LOGGER.info("sending instance configuration");
                final String identifier = getParameter("id", true);
                final Object response = configurer.getInstanceConfiguration(identifier);
                return Response.ok(response).build();

            /*
             * Get list of current service instances.
             */
            } else if (REQUEST_LIST_INSTANCE.equalsIgnoreCase(request)) {
                final InstanceReport report = new InstanceReport(configurer.getInstances());
                return Response.ok(report).build();

            /*
             * Treat other specific administration operations.
             */
            } else {
                return treatSpecificAdminRequest(request);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Sending admin exception: {0}", ex.getMessage());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getMessage());
            return Response.ok(report).build();
        }
    }

    /**
     * need to be overriden by subClasses to add specific admin operation
     * @param request the request name of the specific admin operation
     * @throws CstlServiceException
     */
    protected Response treatSpecificAdminRequest(final String request) throws CstlServiceException {
        throw new CstlServiceException("The operation " + request + " is not supported by the administration service",
                        INVALID_PARAMETER_VALUE, "request");
    }

    /**
     * Receive a file and write it into the static file path.
     *
     * @param in The input stream.
     * @return an Acknowledgment indicating if the operation succeed or not.
     *
     * @todo Not implemented. This is just a placeholder where we can customize the
     *       download action for some users. Will probably be removed in a future version.
     */
    @PUT
    public Response uploadFile(final InputStream in) {
        final String serviceID = getSafeParameter("serviceId");
        try {
            // allow this method only for admin
            if ("admin".equals(serviceID)) {
                final File tmp          = File.createTempFile("cstl-", null);
                final File uploadedFile = FileUtilities.buildFileFromStream(in, tmp);
                in.close();
                return treatAdminRequest(uploadedFile);
            } else {
                LOGGER.log(Level.WARNING, "Received a PUT request on a not admin instance identifier:{0}", serviceID);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO exception while uploading file", ex);
            launchException("error while uploading the file", NO_APPLICABLE_CODE.name(), null);
            // should never happen
            return null;
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
