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
package org.constellation.ws.rs;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.validation.Schema;
import net.iharder.Base64;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ExceptionReport;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.ServiceStatus;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.DeleteServiceDescriptor;
import org.constellation.process.service.RestartServiceDescriptor;
import org.constellation.process.service.StartServiceDescriptor;
import org.constellation.process.service.StopServiceDescriptor;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.security.SecurityManager;

// Geotoolkit dependencies
import org.constellation.ws.Worker;
import org.apache.sis.util.iso.Types;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.xml.MarshallerPool;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

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
 * @since 0.3
 */
public abstract class OGCWebService<W extends Worker> extends WebService {

    final String serviceName;

    /**
     * Initialize the basic attributes of a web serviceType.
     *
     */
    public OGCWebService(final ServiceDef.Specification specification) {
        super();
        if (specification == null){
            throw new IllegalArgumentException("It is compulsory for a web service to have a specification.");
        }
        serviceName = specification.name();
        LOGGER.log(Level.INFO, "Starting the REST {0} service facade.\n", serviceName);
        WSEngine.registerService(serviceName, "REST", getWorkerClass());

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

    /**
     * Return the dedicated Web-service configuration directory.
     *
     * @return
     */
    protected File getServiceDirectory() {
        final File configDirectory   = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {
            final File serviceDirectory = new File(configDirectory, serviceName);
            if (serviceDirectory.isDirectory()) {
                return serviceDirectory;
            } else {
                LOGGER.log(Level.INFO, "The service configuration directory: {0} does not exist or is not a directory, creating new one.", serviceDirectory.getPath());
                if (!serviceDirectory.mkdir()) {
                    LOGGER.log(Level.WARNING, "The service was unable to create the directory.{0}", serviceDirectory.getPath());
                } else {
                    return serviceDirectory;
                }
            }
        } else {
            if (configDirectory == null) {
                LOGGER.warning("The service was unable to find a config directory.");
            } else {
                LOGGER.log(Level.WARNING, "The configuration directory: {0} does not exist or is not a directory.", configDirectory.getPath());
            }
        }
        return null;
    }

    private void startAllInstance() {
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            for (File instanceDirectory : serviceDirectory.listFiles()) {
                if (instanceDirectory.isDirectory()) {
                    final String instance = instanceDirectory.getName();
                    try {
                        ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);
                        ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
                        inputs.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(instance);
                        inputs.parameter(StartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(serviceDirectory);

                        org.geotoolkit.process.Process proc = desc.createProcess(inputs);
                        proc.call();
                    } catch (NoSuchIdentifierException ex) {
                        LOGGER.log(Level.WARNING, "StartService process is unreachable.");
                    } catch (ProcessException ex) {
                        LOGGER.log(Level.WARNING, "Error while starting all instances", ex);
                    }
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "no service directory for :{0}", serviceName);
        }
    }

    private Worker buildWorker(final String identifier) {
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null) {
            final File instanceDirectory = new File(serviceDirectory, identifier);
            if (instanceDirectory.isDirectory()) {
                final Worker newWorker = createWorker(instanceDirectory);
                if (newWorker != null) {
                    WSEngine.addServiceInstance(serviceName, instanceDirectory.getName(), newWorker);
                }
                return newWorker;
            } else {
                LOGGER.log(Level.WARNING, "The instance directory: {0} does not exist or is not a directory.", instanceDirectory.getPath());
            }
        }
        return null;
    }

    /**
     * Build a new instance of Web service worker with the specified configuration directory
     *
     * @param instanceDirectory The configuration directory of the instance.
     * @return
     */
    private Worker createWorker(final File instanceDirectory) {
        return (Worker) ReflectionUtilities.newInstance(getWorkerClass(), instanceDirectory.getName(), instanceDirectory);
    }

    /**
     * @return the worker class of the service.
     */
    protected abstract Class getWorkerClass();

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
        return new ArrayList<Schema>();
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
            SecurityManager.logout();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (IncorrectCredentialsException ex) {
            LOGGER.log(Level.FINER, "incorrect password", ex);
            SecurityManager.logout();
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
                final String referer = getHttpContext().getRequest().getHeaderValue("referer");
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
        if (getHttpServletRequest() != null) {
            final String authorization = getHttpServletRequest().getHeader("authorization");
            if (authorization != null) {
                if (authorization.startsWith("Basic ")) {
                    final String toDecode = authorization.substring(6);
                    try {
                        final String logPass = new String(Base64.decode(toDecode));
                        final int separatorIndex = logPass.indexOf(":");
                        if (separatorIndex != -1) {
                            final String login = logPass.substring(0, separatorIndex);
                            final String passw = logPass.substring(separatorIndex + 1);
                            SecurityManager.login(login, passw);
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
             * restart operation
             * kill all the workers and rebuild each one.
             */
            if ("restart".equalsIgnoreCase(request)) {
                LOGGER.info("refreshing the workers");
                final String identifier      = getParameter("id", false);
                final String closeFirstValue = getParameter("closeFirst", false);
                final boolean closeFirst;
                if (closeFirstValue != null) {
                    closeFirst = Boolean.parseBoolean(closeFirstValue);
                } else {
                    closeFirst = true;
                }
                AcknowlegementType response;
                try {
                    ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);
                    ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
                    inputs.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
                    inputs.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(closeFirst);
                    inputs.parameter(RestartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory());

                    org.geotoolkit.process.Process proc = desc.createProcess(inputs);
                    proc.call();
                    response = new AcknowlegementType("Success", "instances succefully restarted");
                } catch (NoSuchIdentifierException ex) {
                    response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
                } catch (ProcessException ex) {
                    response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
                }
                return Response.ok(response, "text/xml").build();

            } else if ("start".equalsIgnoreCase(request)) {
                LOGGER.info("starting an instance");
                final String identifier = getParameter("id", true);
                final Class clazz       = getWorkerClass();
                AcknowlegementType response;
                try {
                    ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);
                    ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
                    inputs.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
                    inputs.parameter(StartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory());

                    org.geotoolkit.process.Process proc = desc.createProcess(inputs);
                    proc.call();
                    response = new AcknowlegementType("Success", "new instance succefully started");
                } catch (NoSuchIdentifierException ex) {
                    response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
                } catch (ProcessException ex) {
                    response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
                }
                return Response.ok(response, "text/xml").build();

            } else if ("stop".equalsIgnoreCase(request)) {
                LOGGER.info("stopping an instance");
                final String identifier = getParameter("id", true);
                AcknowlegementType response;
                try {
                    ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StopServiceDescriptor.NAME);
                    ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(StopServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
                    inputs.parameter(StopServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);

                    org.geotoolkit.process.Process proc = desc.createProcess(inputs);
                    proc.call();
                    response = new AcknowlegementType("Success", "instance succesfully stopped");
                } catch (NoSuchIdentifierException ex) {
                    response = new AcknowlegementType("Error", "unable to stop the instance : " + ex.getMessage());
                } catch (ProcessException ex) {
                    response = new AcknowlegementType("Error", "unable to stop the instance : " + ex.getMessage());
                }

                return Response.ok(response, "text/xml").build();

            } else if ("delete".equalsIgnoreCase(request)) {
                LOGGER.info("deleting an instance");
                final String identifier = getParameter("id", true);
                AcknowlegementType response;

                try {
                    ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteServiceDescriptor.NAME);
                    ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                    inputs.parameter(DeleteServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
                    inputs.parameter(DeleteServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
                    inputs.parameter(DeleteServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory());

                    org.geotoolkit.process.Process proc = desc.createProcess(inputs);
                    proc.call();
                    response = new AcknowlegementType("Success", "instance succesfully deleted");
                } catch (NoSuchIdentifierException ex) {
                    response = new AcknowlegementType("Error", "unable to delete the instance : " + ex.getMessage());
                } catch (ProcessException ex) {
                    response = new AcknowlegementType("Error", "unable to delete the instance : " + ex.getMessage());
                }

                return Response.ok(response, "text/xml").build();

            } else if ("newInstance".equalsIgnoreCase(request)) {
                return newInstance(objectRequest);


            } else if ("renameInstance".equalsIgnoreCase(request)) {
                LOGGER.info("renaming an instance");
                final String identifier = getParameter("id", true);
                final String newName    = getParameter("newName", true);
                // we stop the current worker
                WSEngine.shutdownInstance(serviceName, identifier);

                final AcknowlegementType response;
                final File serviceDirectory = getServiceDirectory();
                if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                    final File instanceDirectory = new File (serviceDirectory, identifier);
                    final File newDirectory      = new File (serviceDirectory, newName);

                    if (instanceDirectory.isDirectory()) {
                        if (!newDirectory.exists()) {
                            if (instanceDirectory.renameTo(newDirectory)) {
                                final Worker newWorker = buildWorker(newName);
                                if (newWorker == null) {
                                    throw new CstlServiceException("The instance " + newName + " can be started, maybe there is no configuration directory with this name.", INVALID_PARAMETER_VALUE);
                                } else {
                                    if (newWorker.isStarted()) {
                                        response = new AcknowlegementType("Success", "instance succefully renamed");
                                    } else {
                                        response = new AcknowlegementType("Error", "unable to start the renamed instance");
                                    }
                                }
                            } else {
                                response = new AcknowlegementType("Error", "Unable to rename the directory");
                            }
                        } else {
                            response = new AcknowlegementType("Error", "already existing instance:" + newName);
                        }
                    } else {
                        response = new AcknowlegementType("Error", "no existing instance:" + identifier);
                    }
                } else {
                    throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
                }
                return Response.ok(response, "text/xml").build();


            /*
             * Update the configuration file of an instance.
             */
            } else if ("configure".equalsIgnoreCase(request)) {
                LOGGER.info("configure an instance");
                final String identifier = getParameter("id", true);
                final File serviceDirectory = getServiceDirectory();
                final AcknowlegementType response;
                if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                    File instanceDirectory     = new File (serviceDirectory, identifier);
                    configureInstance(instanceDirectory, objectRequest, null);
                    response = new AcknowlegementType("Success", "Instance correctly configured");
                } else {
                    throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
                }
                return Response.ok(response, "text/xml").build();

            /*
             * Send the configuration file of an instance.
             */
            } else if ("getConfiguration".equalsIgnoreCase(request)) {
                LOGGER.info("sending instance configuration");
                final String identifier = getParameter("id", true);
                final File serviceDirectory = getServiceDirectory();
                final Object response;
                if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                    File instanceDirectory     = new File (serviceDirectory, identifier);
                    if (instanceDirectory.isDirectory()) {
                        response = getInstanceConfiguration(instanceDirectory);
                    } else {
                        throw new CstlServiceException("Unable to find an instance:" + identifier, NO_APPLICABLE_CODE);
                    }
                } else {
                    throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
                }
                return Response.ok(response, "text/xml").build();

            /*
             * Return a report about the instances in the service.
             */
            } else if ("listInstance".equalsIgnoreCase(request)) {
                LOGGER.finer("listing instances");
                final List<Instance> instances = new ArrayList<Instance>();
                // 1- First we list the instance in the map
                for (Entry<String, Boolean> entry : WSEngine.getEntriesStatus(serviceName)) {
                    final ServiceStatus status;
                    if (entry.getValue()) {
                        status = ServiceStatus.WORKING;
                    } else {
                        status = ServiceStatus.ERROR;
                    }
                    instances.add(new Instance(entry.getKey(), serviceName, status));
                }
                // 2- Then we list the instance not yet started
                final File serviceDirectory = getServiceDirectory();
                if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                    for (File instanceDirectory : serviceDirectory.listFiles()) {
                        final String name = instanceDirectory.getName();
                        if (instanceDirectory.isDirectory() && !name.startsWith(".") && !WSEngine.serviceInstanceExist(serviceName, name)) {
                            instances.add(new Instance(name, serviceName, ServiceStatus.NOT_STARTED));
                        }
                    }
                }
                final InstanceReport report = new InstanceReport(instances);
                return Response.ok(report, "text/xml").build();

            } else if ("updateCapabilities".equalsIgnoreCase(request)) {
                LOGGER.info("updating instance capabilities");
                final String identifier = getParameter("id", true);
                final String fileName   = getParameter("fileName", true);
                final File serviceDirectory = getServiceDirectory();
                final AcknowlegementType response;
                if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                    File instanceDirectory     = new File (serviceDirectory, identifier);
                    if (instanceDirectory.isDirectory()) {
                        // recup the file
                        if (objectRequest instanceof File) {
                            try {
                                final File newCapabilitiesFile = new File(instanceDirectory, fileName);
                                if (!newCapabilitiesFile.exists()) {
                                    newCapabilitiesFile.createNewFile();
                                }
                                FileUtilities.copy((File)objectRequest, newCapabilitiesFile);
                                response = new AcknowlegementType("Success", "Instance Capabilities correctly updated");
                            } catch (IOException ex) {
                                throw new CstlServiceException("An IO exception occurs when creating the new Capabilities File.", ex, NO_APPLICABLE_CODE);
                            }
                        } else {
                            throw new CstlServiceException("Unable to find the specified File.", NO_APPLICABLE_CODE);
                        }
                    } else {
                        throw new CstlServiceException("Unable to find an instance:" + identifier, NO_APPLICABLE_CODE);
                    }
                } else {
                    throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
                }
                return Response.ok(response, "text/xml").build();

            } else {
                return treatSpecificAdminRequest(request);
            }
        } catch (CstlServiceException ex) {
            LOGGER.log(Level.WARNING, "Sending admin exception:{0}", ex.getMessage());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getMessage());
            return Response.ok(report, "text/xml").build();
        }
    }

    /**
     * create new service intance
     * @return
     * @throws CstlServiceException
     * @param objectRequest
     */
    private Response newInstance(Object objectRequest) throws CstlServiceException {
        LOGGER.info("creating an instance");
        final String identifier = getParameter("id", true);
        final AcknowlegementType response;
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            final File instanceDirectory = new File (serviceDirectory, identifier);
            if (instanceDirectory.mkdir()) {


//                reset
                if(objectRequest!= null && objectRequest instanceof Service){
                    Service tocreated = (Service) objectRequest;
                    basicConfigure(instanceDirectory, tocreated);
                }else{
                    //create basic conf
                    basicConfigure(instanceDirectory, null);
                }
                response = new AcknowlegementType("Success", "instance succefully created");
            } else {
                response = new AcknowlegementType("Error", "unable to create an instance");
            }
        } else {
            throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
        }
        return Response.ok(response, "text/xml").build();
    }

    /**
     * need to be overriden by subClasses to add specific admin operation
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
     * create a new File containing the specific object sent.
     *
     * @param instanceDirectory The directory containing the instance configuration files.
     * @param configuration A service specific configuration Object.
     */
    protected abstract void configureInstance(final File instanceDirectory, final Object configuration, final Object capabilitiesConfiguration) throws CstlServiceException;

    /**
     * Return the configuration object of the instance.
     *
     * @param instanceDirectory The directory containing the instance configuration files.
     */
    protected abstract Object getInstanceConfiguration(final File instanceDirectory) throws CstlServiceException;

    /**
     * create an empty configuration for the service.
     *
     * @param instanceDirectory The directory containing the instance configuration files.
     * @param capabilitiesConfiguration
     */
    protected abstract void basicConfigure(final File instanceDirectory, Object capabilitiesConfiguration) throws CstlServiceException;


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
