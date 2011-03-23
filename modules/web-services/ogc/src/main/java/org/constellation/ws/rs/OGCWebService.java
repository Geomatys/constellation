/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.ws.rs;

// J2SE dependencies
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.Response;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.constellation.ws.Worker;
import org.geotoolkit.internal.CodeLists;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.Version;
import org.geotoolkit.util.collection.UnmodifiableArrayList;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.util.CodeList;


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

    /**
     * The supported supportedVersions supported by this web serviceType.
     * avoid modification after instantiation.
     */
    private final UnmodifiableArrayList<ServiceDef> supportedVersions;

    /**
     * A map of service worker.
     * TODO this attribute must be set to private when will fix the WFS service
     */
    protected final Map<String, W> workersMap;

    /**
     * Initialize the basic attributes of a web serviceType.
     *
     * @param supportedVersions A list of the supported version of this serviceType.
     *                          The first version specified <strong>MUST</strong> be the highest
     *                          one, the best one.
     */
    public OGCWebService(final ServiceDef... supportedVersions) {
        super();

        if (supportedVersions == null || supportedVersions.length == 0 ||
                (supportedVersions.length == 1 && supportedVersions[0] == null)){
            throw new IllegalArgumentException("It is compulsory for a web service to have " +
                    "at least one version specified.");
        }
        LOGGER.log(Level.INFO, "Starting the REST {0} service facade.\n", supportedVersions[0].specification.name());
        
        //guarantee it will not be modified
        this.supportedVersions = UnmodifiableArrayList.wrap(supportedVersions.clone());

        /*
         * build the map of Workers, by scanning the sub-directories of its service directory.
         */
        workersMap = new HashMap<String, W>();
        buildWorkerMap();
    }

    /**
     * Initialize the basic attributes of a web serviceType.
     * the worker Map here is fill by the subClasse, this is not the best behavior.
     * This constructor is here to keep compatibility with old version.
     *
     * @param supportedVersions A list of the supported version of this serviceType.
     *                          The first version specified <strong>MUST</strong> be the highest
     *                          one, the best one.
     * @param workers A map of worker id / worker.
     */
    public OGCWebService(Map<String, W> workers, final ServiceDef... supportedVersions) {
        super();

        if (supportedVersions == null || supportedVersions.length == 0 ||
                (supportedVersions.length == 1 && supportedVersions[0] == null)){
            throw new IllegalArgumentException("It is compulsory for a web service to have " +
                    "at least one version specified.");
        }

        //guarantee it will not be modified
        this.supportedVersions = UnmodifiableArrayList.wrap(supportedVersions.clone());
        this.workersMap        = workers;
    }

    protected File getServiceDirectory() {
        final File configDirectory   = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {
            final File serviceDirectory = new File(configDirectory, supportedVersions.get(0).specification.name());
            if (serviceDirectory.isDirectory()) {
                return serviceDirectory;
            } else {
                LOGGER.log(Level.SEVERE, "The service configuration directory: {0} does not exist or is not a directory.", serviceDirectory.getPath());
            }
        } else {
            if (configDirectory == null) {
                LOGGER.severe("The service was unable to find a config directory.");
            } else {
                LOGGER.log(Level.SEVERE, "The configuration directory: {0} does not exist or is not a directory.", configDirectory.getPath());
            }
        }
        return null;
    }

    /**
     * Scan the configuration directory to instantiate Web service workers.
     */
    private void buildWorkerMap() {
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            for (File instanceDirectory : serviceDirectory.listFiles()) {
                /*
                 * For each sub-directory we build a new Worker.
                 */
                if (instanceDirectory.isDirectory() && !instanceDirectory.getName().startsWith(".")) {
                    final W newWorker = createWorker(instanceDirectory);
                    workersMap.put(instanceDirectory.getName(), newWorker);
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "no {0} directory.", supportedVersions.get(0).specification.name());
        }
    }

    private W buildWorker(String identifier) {
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null) {
            final File instanceDirectory = new File(serviceDirectory, identifier);
            if (instanceDirectory.isDirectory()) {
                final W newWorker = createWorker(instanceDirectory);
                workersMap.put(instanceDirectory.getName(), newWorker);
                return newWorker;
            } else {
                LOGGER.log(Level.SEVERE, "The instance directory: {0} does not exist or is not a directory.", instanceDirectory.getPath());
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
    protected abstract W createWorker(File instanceDirectory);


    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) {
        try {
            final String serviceID = getParameter("serviceId", false);
            // request is send to the specified worker
            if (serviceID != null && workersMap.containsKey(serviceID)) {
                final W worker = workersMap.get(serviceID);
                if (worker.isSecured()) {
                    final String ip = getHttpServletRequest().getRemoteAddr();
                    final String referer = getHttpContext().getRequest().getHeaderValue("referer");
                    if (!worker.isAuthorized(ip, referer)) {
                        LOGGER.log(Level.INFO, "Received a request from unauthorized ip:{0} or referer:{1}", 
                                new String[]{ip, referer});
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                }
                return treatIncomingRequest(objectRequest, worker);

            // administration a the instance
            } else if ("admin".equals(serviceID)){
                return treatAdminRequest(objectRequest);

            // unbounded URL
            } else {
                LOGGER.log(Level.WARNING, "Received request on undefined instance identifier:{0}", serviceID);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, supportedVersions.get(0));
        }
    }

    /**
     * treat the request sent to the admin instance.
     */
    public Response treatAdminRequest(Object objectRequest) throws CstlServiceException {
        final String request = getParameter("request", true);

        /*
         * restart operation
         * kill all the workers and rebuild each one.
         */
        if ("restart".equalsIgnoreCase(request)) {
            LOGGER.info("refreshing the workers");
            final String identifier = getParameter("id", false);
            specificRestart(identifier);
            if (identifier == null) {
                for (final Worker worker : workersMap.values()) {
                    worker.destroy();
                }
                workersMap.clear();
                buildWorkerMap();
            } else {
                if (workersMap.containsKey(identifier)) {
                    Worker worker = workersMap.get(identifier);
                    workersMap.remove(identifier);
                    worker.destroy();
                    buildWorker(identifier);
                } else {
                    throw new CstlServiceException("There is no instance " + identifier, INVALID_PARAMETER_VALUE, "id");
                }
            }
            return Response.ok(new AcknowlegementType("success", "instances succefully restarted"), "text/xml").build();

        } else if ("start".equalsIgnoreCase(request)) {
            LOGGER.info("starting a new worker");
            final String identifier = getParameter("id", true);
            specificRestart(identifier);
            final AcknowlegementType response;
            if (workersMap.containsKey(identifier)) {
                throw new CstlServiceException("The instance " + identifier + " is already started try restart method instead.", INVALID_PARAMETER_VALUE);
            } else {
                final Worker worker = buildWorker(identifier);
                if (worker == null) {
                    throw new CstlServiceException("The instance " + identifier + " can be started, maybe there is no configuration directory with this name.", INVALID_PARAMETER_VALUE);
                } else {
                    if (worker.isStarted()) {
                        response = new AcknowlegementType("Success", "new instance succefully started");
                    } else {
                        response = new AcknowlegementType("Error", "unable to start the instance");
                    }
                }
            }
            return Response.ok(response, "text/xml").build();
        
        } else if ("delete".equalsIgnoreCase(request)) {
            LOGGER.info("deleting an instance");
            final String identifier = getParameter("id", true);
            final W worker = workersMap.get(identifier);
            final AcknowlegementType response;
            if (worker == null) {
                throw new CstlServiceException("The instance " + identifier + " is does not exist.", INVALID_PARAMETER_VALUE);
            } else {
                worker.destroy();
                workersMap.remove(identifier);
                final File serviceDirectory = getServiceDirectory();
                if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                    File instanceDirectory     = new File (serviceDirectory, identifier);
                    File instanceDirectoryBack = new File (serviceDirectory, "." + identifier);
                    if (instanceDirectory.isDirectory()) {
                        if (instanceDirectory.renameTo(instanceDirectoryBack)) {
                            response = new AcknowlegementType("Success", "instance succesfully deleted");
                        } else {
                            response = new AcknowlegementType("Error", "instance was deactivated new but can't be deleted");
                        }
                    } else {
                        throw new CstlServiceException("The instance " + identifier + " does not have a proper directory.", NO_APPLICABLE_CODE);
                    }
                } else {
                    throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
                }
            }
            return Response.ok(response, "text/xml").build();

        } else if ("newInstance".equalsIgnoreCase(request)) {
            LOGGER.info("creating an instance");
            final String identifier = getParameter("id", true);
            final AcknowlegementType response;
            final File serviceDirectory = getServiceDirectory();
            if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                File instanceDirectory     = new File (serviceDirectory, identifier);
                if (instanceDirectory.mkdir()) {
                    response = new AcknowlegementType("Success", "instance succefully created");
                } else {
                    response = new AcknowlegementType("Error", "unbale to create an instance");
                }
            } else {
                throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
            }
            return Response.ok(response, "text/xml").build();


        } else if ("configure".equalsIgnoreCase(request)) {
            LOGGER.info("configure an instance");
            final String identifier = getParameter("id", true);
            final File serviceDirectory = getServiceDirectory();
            final AcknowlegementType response;
            if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                File instanceDirectory     = new File (serviceDirectory, identifier);
                configureInstance(instanceDirectory, objectRequest);
                response = new AcknowlegementType("Success", "Instance correctly configured");
            } else {
                throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
            }
            return Response.ok(response, "text/xml").build();


        } else if ("listInstance".equalsIgnoreCase(request)) {
            LOGGER.info("listing instances");
            final List<Instance> instances = new ArrayList<Instance>();
            // 1- First we list the instance in the map
            for (Entry<String, W> entry : workersMap.entrySet()) {
                final String status;
                if (entry.getValue().isStarted()) {
                    status = "working";
                } else {
                    status = "error";
                }
                instances.add(new Instance(entry.getKey(), status));
            }
            // 2- Then we list the instance not yet started
            final File serviceDirectory = getServiceDirectory();
            if (serviceDirectory != null && serviceDirectory.isDirectory()) {
                for (File instanceDirectory : serviceDirectory.listFiles()) {
                    final String name = instanceDirectory.getName();
                    if (instanceDirectory.isDirectory() && !name.startsWith(".") && !workersMap.containsKey(name)) {
                        instances.add(new Instance(name, "not started"));
                    }
                }
            }
            final InstanceReport report = new InstanceReport(instances);
            return Response.ok(report, "text/xml").build();
        } else {
            throw new CstlServiceException("The operation " + request + " is not supported by the administration service",
                    INVALID_PARAMETER_VALUE, "request");
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
    protected abstract void configureInstance(final File instanceDirectory, final Object configuration) throws CstlServiceException;

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
     * Verify if the version is supported by this serviceType.
     * <p>
     * If the version is not accepted we send an exception.
     * </p>
     */
    protected void isVersionSupported(final String versionNumber) throws CstlServiceException {
        if (getVersionFromNumber(versionNumber) == null) {
            final StringBuilder messageb = new StringBuilder("The parameter ");
            for (ServiceDef vers : supportedVersions) {
                messageb.append("VERSION=").append(vers.version.toString()).append(" OR ");
            }
            messageb.delete(messageb.length()-4, messageb.length()-1);
            messageb.append(" must be specified");
            throw new CstlServiceException(messageb.toString(), VERSION_NEGOTIATION_FAILED);
        }
    }

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
     * @param marshaller The marshaller to use for the exception report.
     * @param serviceDef The service definition, from which the version number of exception report will
     *                   be extracted.
     * @return An XML representing the exception.
     */
    protected abstract Response processExceptionResponse(final CstlServiceException ex, final ServiceDef serviceDef);

    /**
     * The shared method to build a service ExceptionReport.
     *
     * @param message
     * @param codeName
     * @return
     */
    @Override
    protected Response launchException(final String message, String codeName, final String locator) {
        final ServiceDef mainVersion = supportedVersions.get(0);
        if (mainVersion.owsCompliant) {
            codeName = StringUtilities.transformCodeName(codeName);
        }
        final OWSExceptionCode code   = CodeLists.valueOf(OWSExceptionCode.class, codeName);
        final CstlServiceException ex = new CstlServiceException(message, code, locator);
        return processExceptionResponse(ex, mainVersion);
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
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     */
    protected ServiceDef getVersionFromNumber(final String number) {
        for (ServiceDef v : supportedVersions) {
            if (v.version.toString().equals(number)){
                return v;
            }
        }
        return null;
    }

    /**
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     */
    protected ServiceDef getVersionFromNumber(final Version number) {
        if (number != null) {
            for (ServiceDef v : supportedVersions) {
                if (v.version.toString().equals(number.toString())){
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * If the requested version number is not available we choose the best version to return.
     *
     * @param number A version number, which will be compared to the ones specified.
     *               Can be {@code null}, in this case the best version specified is just returned.
     * @return The best version (the highest one) specified for this web service.
     */
    protected ServiceDef getBestVersion(final String number) {
        for (ServiceDef v : supportedVersions) {
            if (v.version.toString().equals(number)){
                return v;
            }
        }
        final ServiceDef firstSpecifiedVersion = supportedVersions.get(0);
        if (number == null || number.isEmpty()) {
            return firstSpecifiedVersion;
        }
        final ServiceDef.Version wrongVersion = new ServiceDef.Version(number);
        if (wrongVersion.compareTo(firstSpecifiedVersion.version) > 0) {
            return firstSpecifiedVersion;
        } else {
            if (wrongVersion.compareTo(supportedVersions.get(supportedVersions.size() - 1).version) < 0) {
                return supportedVersions.get(supportedVersions.size() - 1);
            }
        }
        return firstSpecifiedVersion;
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
            !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_SRS) && !ex.getExceptionCode().equals(org.constellation.ws.ExceptionCode.INVALID_REQUEST)) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
        }
    }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    @Override
    public void destroy() {
        super.destroy();
        LOGGER.log(Level.INFO, "Shutting down the REST {0} service facade.", supportedVersions.get(0).specification.name());
        for (final Worker worker : workersMap.values()) {
            worker.destroy();
        }
        workersMap.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MarshallerPool getConfigurationPool() {
        return GenericDatabaseMarshallerPool.getInstance();
    }

}
