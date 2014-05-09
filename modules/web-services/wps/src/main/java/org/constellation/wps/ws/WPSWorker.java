/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.wps.ws;

import com.vividsolutions.jts.geom.Geometry;

import org.constellation.ServiceDef;
import org.constellation.configuration.*;
import org.constellation.configuration.Process;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.RestartServiceDescriptor;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.wps.ws.rs.WPSService;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.dto.Service;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.SecurityManagerAdapter;
import org.constellation.security.SecurityManagerHolder;
import static org.constellation.wps.ws.WPSConstant.*;

import static org.constellation.api.CommonConstants.DEFAULT_CRS;
import static org.constellation.api.QueryConstants.*;

import org.constellation.ws.Worker;
import org.geotoolkit.geometry.isoonjts.GeometryUtils;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.ows.xml.v110.*;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.parameter.ExtendedParameterDescriptor;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.*;
import org.geotoolkit.referencing.CRS;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wps.converters.WPSConvertersUtils;
import org.geotoolkit.wps.io.WPSIO;
import org.geotoolkit.wps.io.WPSMimeType;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.*;
import org.geotoolkit.wps.xml.v100.ExecuteResponse.ProcessOutputs;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.apache.sis.xml.MarshallerPool;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.opengis.parameter.ParameterValue;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;

/**
 * WPS worker.Compute response of getCapabilities, DescribeProcess and Execute requests.
 *
 * @author Quentin Boileau
 */
public class WPSWorker extends AbstractWorker {

    /**
     * Supported CRS.
     */
    private static final SupportedCRSsType WPS_SUPPORTED_CRS;

    static {
        WPS_SUPPORTED_CRS = new SupportedCRSsType();

        //Default CRS.
        final SupportedCRSsType.Default defaultCRS = new SupportedCRSsType.Default();
        defaultCRS.setCRS(DEFAULT_CRS.get(0));
        WPS_SUPPORTED_CRS.setDefault(defaultCRS);

        //Supported CRS.
        final CRSsType supportedCRS = new CRSsType();
        supportedCRS.getCRS().addAll(DEFAULT_CRS);
        WPS_SUPPORTED_CRS.setSupported(supportedCRS);
    }

    /**
     * Timeout in seconds use to kill long process execution in synchronous mode.
     */
    private static final int TIMEOUT = 30;

    private static final String SCHEMA_FOLDER_NAME = "/schemas";

    private static final String WMS_SUPPORTED = "WMS_SUPPORTED";

    /**
     * Try to create temporary directory.
     */
    private final boolean supportStorage;

    /**
     * Path where output file will be saved.
     */
    private final String webdavFolderPath;
    private final String webdavName;
    private final boolean isTmpWebDav;

    /**
     * WebDav URL.
     */
    private String webdavURL;

    private final String schemaFolder;
    private String schemaURL;

    /**
     * WMS link attributes
     */
    private boolean wmsSupported = false;
    private String wmsInstanceName;
    private String wmsInstanceURL;
    private String wmsProviderId;
    private String fileCoverageStorePath;

    /**
     * WPS context configuration.
     */
    private ProcessContext context;

    /**
     * List of process descriptors available.
     */
    private final List<ProcessDescriptor> processDescriptorList = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param id
     */
    public WPSWorker(final String id) {
        super(id, ServiceDef.Specification.WPS);
        try {
            final Object obj = ConfigurationEngine.getConfiguration("WPS", id);
            if (obj instanceof ProcessContext) {
                context = (ProcessContext) obj;
                applySupportedVersion();
                isStarted = true;
            } else {
                startError = "The process context File does not contain a ProcessContext object";
                isStarted = false;
                LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, id);
            }
        } catch (JAXBException ex) {
            startError = "JAXBExeception while unmarshalling the process context File";
            isStarted = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, ex);
        }  catch (FileNotFoundException ex) {
            startError = "The configuration file processContext.xml has not been found";
            isStarted = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, id);
        } catch (CstlServiceException ex) {
            startError = "Error applying supported versions : " + ex.getMessage();
            isStarted = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, id);
        }

        webdavName = "wps-"+ id;
        if (context != null && context.getWebdavDirectory() != null) {
            webdavFolderPath = context.getWebdavDirectory();
            isTmpWebDav = false;
        } else {
            isTmpWebDav = true;
            final File tmpFolder = new File(System.getProperty("java.io.tmpdir"), webdavName);
            if (!tmpFolder.isDirectory()) {
                final boolean created = tmpFolder.mkdirs();
                if (created) {
                    tmpFolder.deleteOnExit();
                } else {
                    LOGGER.log(Level.WARNING, "Ressource folder for WPS services cannot be created. " +
                            "It makes WPS unable to use reference parameters. You should restart server or create manually following directory : "
                            + tmpFolder.getAbsolutePath());
                }
            }
            webdavFolderPath = tmpFolder.getAbsolutePath();
        }
        
        //Configure the directory to store parameters schema into.
        File schemaLoc = new File(webdavFolderPath + SCHEMA_FOLDER_NAME);
        schemaLoc.mkdir();
        
        if(schemaLoc.exists()) {
            schemaFolder = schemaLoc.getAbsolutePath();
        } else {
            schemaFolder = webdavFolderPath;
        }
        
        this.webdavURL = null; //initialize on WPS execute request.
        
        //create new WebDav instance
        final boolean webdav = createWebDav();
        if (!webdav) {
            this.supportStorage = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) does not support stockage!\nCause: Error during WPS WebDav service creation.", id);
        } else {
            this.supportStorage = true;
        }

        //WMS link
        if (context.getWmsInstanceName() != null) {
            this.wmsInstanceName = context.getWmsInstanceName();

            if (context.getFileCoverageProviderId() != null) {
                this.wmsProviderId = context.getFileCoverageProviderId();

                DataProvider provider = null;
                for (DataProvider p : DataProviders.getInstance().getProviders()) {
                    if (p.getId().equals(this.wmsProviderId)) {
                        provider = p;
                        break;
                    }
                }

                if(WSEngine.getInstance("WMS", wmsInstanceName) == null || provider == null) {
                    startError = "Linked WMS instance is not found or FileCoverageStore not defined.";
                } else {
                    final ParameterValue pathParam = (ParameterValue)Parameters.search(provider.getSource(), "path",3).get(0);
                    this.fileCoverageStorePath = ((URL) pathParam.getValue()).getPath();
                    final File dir = new File(this.fileCoverageStorePath);
                    dir.mkdirs();
                    this.wmsSupported = true;
                }

            } else {
                startError = "Linked provider identifier name is not defined.";
            }
        } else {
            startError = "Linked WMS instance name is not defined.";
        }

        if (!wmsSupported) {
            LOGGER.log(Level.WARNING, "\nThe WPS worker ({0}) don\'t support WMS outputs : \n " + startError, id);
        }

        fillProcessList();

        if (isStarted) {
            LOGGER.log(Level.INFO, "WPS worker {0} running", id);
        }
    }

    /**
     * Create process list from context file.
     */
    private void fillProcessList() {
        if (context == null || context.getProcesses() == null) {
            return;
        }
            // Load all processes from all factory
            if (Boolean.TRUE == context.getProcesses().getLoadAll()) {
                LOGGER.info("Loading all process");
                final Iterator<ProcessingRegistry> factoryIte = ProcessFinder.getProcessFactories();
                while (factoryIte.hasNext()) {
                    final ProcessingRegistry factory = factoryIte.next();
                    for (final ProcessDescriptor descriptor : factory.getDescriptors()) {
                        if (WPSUtils.isSupportedProcess(descriptor)) {
                            processDescriptorList.add(descriptor);
                        } else {
                            LOGGER.log(Level.WARNING, "Process {0}:{1} not supported.",
                                    new Object[] {descriptor.getIdentifier().getAuthority().getTitle().toString(),descriptor.getIdentifier().getCode()});
                        }
                    }
                }
            } else {
                for (final ProcessFactory processFactory : context.getProcessFactories()) {
                    final ProcessingRegistry factory = ProcessFinder.getProcessFactory(processFactory.getAutorityCode());
                    if (factory != null) {
                        if (Boolean.TRUE == processFactory.getLoadAll()) {
                            LOGGER.log(Level.INFO, "loading all process for factory:{0}", processFactory.getAutorityCode());
                            for (final ProcessDescriptor descriptor : factory.getDescriptors()) {
                                if (WPSUtils.isSupportedProcess(descriptor)) {
                                    processDescriptorList.add(descriptor);
                                } else {
                                    LOGGER.log(Level.WARNING, "Process {0}:{1} not supported.",
                                            new Object[] {descriptor.getIdentifier().getAuthority().getTitle().toString(),descriptor.getIdentifier().getCode()});
                                }
                            }
                        } else {
                            for (final Process process : processFactory.getInclude().getProcess()) {
                                try {
                                    final ProcessDescriptor desc = factory.getDescriptor(process.getId());
                                    if (desc != null) {
                                        if (WPSUtils.isSupportedProcess(desc)) {
                                            processDescriptorList.add(desc);
                                        } else {
                                            LOGGER.log(Level.WARNING, "Process {0}:{1} not supported.",
                                                    new Object[] {desc.getIdentifier().getAuthority().getTitle().toString(),desc.getIdentifier().getCode()});
                                        }
                                    }
                                } catch (NoSuchIdentifierException ex) {
                                    LOGGER.log(Level.WARNING, "Unable to find a process named:" + process.getId() + " in factory " + processFactory.getAutorityCode(), ex);
                                }
                            }
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "No process factory found for authorityCode:{0}", processFactory.getAutorityCode());
                    }
                }
            }        
        for(ProcessDescriptor desc : processDescriptorList) {
            try {
                checkForSchemasToStore(desc);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "Process " + desc.getDisplayName() + " can't be added. Needed schemas can't be build.", ex);
                processDescriptorList.remove(desc);
                continue;
            }
        }
        LOGGER.log(Level.INFO, "{0} processes loaded.", processDescriptorList.size());
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WPSMarshallerPool.getInstance();
    }

    @Override
    public void destroy() {
        super.destroy();
        //Delete recursively temporary directory.
        if (isTmpWebDav) {
            FileUtilities.deleteDirectory(new File(webdavFolderPath));
            ConfigurationEngine.deleteConfiguration("webdav", webdavName);
        }
    }

    /**
     * Update the current WMS URL based on the current service URL.
     */
    private void updateWMSURL() {
        String webappURL = getServiceUrl();
        if (webappURL != null) {
            if (webappURL.contains("/wps/"+getId())) {
                this.wmsInstanceURL = webappURL.replace("/wps/"+getId(), "/wms/"+this.wmsInstanceName);
            } else {
                LOGGER.log(Level.WARNING, "Wrong service URL.");
            }
        } else {
            LOGGER.log(Level.WARNING, "Service URL undefined.");
        }
    }

    /**
     * Update the current WebDav URL based on the current service URL. 
     * TODO find a better way to build webdavURL
     */
    private void updateWebDavURL() {
        String webappURL = getServiceUrl();
        if (webappURL != null) {
            final int index = webappURL.indexOf("/wps/" + getId());
            if (index != -1) {
                webappURL = webappURL.substring(0, index);
                if (webappURL.contains("/WS")) {
                    webappURL = webappURL.substring(0, webappURL.indexOf("/WS"));
                }
                this.webdavURL = webappURL + "/webdav/" + webdavName;
            } else {
                LOGGER.log(Level.WARNING, "Wrong service URL.");
            }
        } else {
            LOGGER.log(Level.WARNING, "Service URL undefined.");
        }
    }

    /**
     * Create a new configuration of webdav servlet if not exist and make
     * directory where temporary files will be stored.
     *
     * @return false if something went wrong.
     */
    private boolean createWebDav() {
        final File tmpDir = new File(webdavFolderPath);
        if (!tmpDir.isDirectory()) {
            tmpDir.mkdirs();
        }

        //configure webdav if not exist
        try {
            ConfigurationEngine.getConfiguration("webdav", webdavName);
        } catch (FileNotFoundException | JAXBException e) {
            final WebdavContext webdavCtx = new WebdavContext(webdavFolderPath);
            webdavCtx.setId(webdavName);
            try {
                if (SecurityManagerHolder.getInstance().isAuthenticated()) {
                    ConfigurationEngine.storeConfiguration("webdav", webdavName, webdavCtx, null);
                } else {
                    try {
                        ConfigurationEngine.setSecurityManager(new SecurityManagerAdapter() {
                            @Override
                            public String getCurrentUserLogin() {
                                return "admin";
                            }
                        });

                        ConfigurationEngine.storeConfiguration("webdav", webdavName, webdavCtx, null);
                    } finally {
                        ConfigurationEngine.setSecurityManager(SecurityManagerHolder.getInstance());
                    }
                }
                // /!\ CASE SENSITIVE /!\
                final Worker worker = WSEngine.buildWorker("WEBDAV", webdavName);
                WSEngine.addServiceInstance("WEBDAV", webdavName, worker);
            } catch (JAXBException | IOException ex) {
                LOGGER.log(Level.WARNING, "Error during WebDav configuration", ex);
                return false;
            }
        }
        return true;
    }
    
    //////////////////////////////////////////////////////////////////////
    //                      GetCapabilities
    //////////////////////////////////////////////////////////////////////
    /**
     * GetCapabilities request
     *
     * @param request request
     * @return WPSCapabilitiesType
     * @throws CstlServiceException
     */
    public WPSCapabilitiesType getCapabilities(final GetCapabilities request) throws CstlServiceException {
        isWorking();

        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter " + SERVICE_PARAMETER + " must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }

        //check LANGUAGE=en-EN
        if (request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)) {
            throw new CstlServiceException("The specified " + LANGUAGE_PARAMETER + " is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        //check VERSION
        List<String> versionsAccepted = null;
        if (request.getAcceptVersions() != null) {
            versionsAccepted = request.getAcceptVersions().getVersion();
        }

        boolean versionSupported = false;
        if (versionsAccepted != null) {

            if (versionsAccepted.contains(ServiceDef.WPS_1_0_0.version.toString())) {
                versionSupported = true;
            }
        }

        //if versionAccepted parameter is not define return the last getCapabilities
        if (versionsAccepted == null || versionSupported) {
            //set the current updateSequence parameter
            final boolean returnUS = returnUpdateSequenceDocument(request.getUpdateSequence());
            if (returnUS) {
                return new WPSCapabilitiesType("1.0.0", getCurrentUpdateSequence());
            }
            
            final Object cachedCapabilities = getCapabilitiesFromCache("1.0.0", null);
            if (cachedCapabilities != null) {
                return (WPSCapabilitiesType) cachedCapabilities;
            }

            // We unmarshall the static capabilities document.
            final Service skeleton = getStaticCapabilitiesObject("WPS", null);
            final WPSCapabilitiesType staticCapabilities = (WPSCapabilitiesType) WPSConstant.createCapabilities("1.0.0", skeleton);

            final ServiceIdentification si = staticCapabilities.getServiceIdentification();
            final ServiceProvider sp       = staticCapabilities.getServiceProvider();
            final OperationsMetadata om    = (OperationsMetadata) WPSConstant.OPERATIONS_METADATA.clone();
            om.updateURL(getServiceUrl());

            final ProcessOfferings offering = new ProcessOfferings();

            for (final ProcessDescriptor procDesc : processDescriptorList) {
                offering.getProcess().add(WPSUtils.generateProcessBrief(procDesc));
            }
            final org.geotoolkit.wps.xml.v100.Languages languages = new org.geotoolkit.wps.xml.v100.Languages("en-EN", Arrays.asList("en-EN"));
           
            final WPSCapabilitiesType response = new WPSCapabilitiesType(si, sp, om, "1.0.0", getCurrentUpdateSequence(), offering, languages, null);
            putCapabilitiesInCache("1.0.0", null, response);
            return response;
        } else {
            throw new CstlServiceException("The specified " + ACCEPT_VERSIONS_PARAMETER + " numbers are not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, ACCEPT_VERSIONS_PARAMETER.toLowerCase());
        }
    }

    public static Map<String, Object> buildParametersMap(final String webdavURL, final String webdavFolderPath, final String wmsInstanceName,
                                                         final String wmsInstanceURL, final String fileCoverageStorePath, final String wmsProviderId,
                                                         final String layerName, final Boolean wmsSupported) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(WPSConvertersUtils.OUT_STORAGE_DIR, webdavFolderPath);
        parameters.put(WPSConvertersUtils.OUT_STORAGE_URL, webdavURL);
        parameters.put(WPSConvertersUtils.WMS_INSTANCE_NAME, wmsInstanceName);
        parameters.put(WPSConvertersUtils.WMS_INSTANCE_URL, wmsInstanceURL);
        parameters.put(WPSConvertersUtils.WMS_STORAGE_DIR, fileCoverageStorePath);
        parameters.put(WPSConvertersUtils.WMS_STORAGE_ID, wmsProviderId);
        parameters.put(WPSConvertersUtils.WMS_LAYER_NAME, layerName);
        parameters.put(WMS_SUPPORTED, wmsSupported);
        return parameters;
    }


    //////////////////////////////////////////////////////////////////////
    //                      DescribeProcess
    //////////////////////////////////////////////////////////////////////
    /**
     * Describe process request.
     *
     * @param request request
     * @return ProcessDescriptions
     * @throws CstlServiceException
     *
     */
    public ProcessDescriptions describeProcess(final DescribeProcess request) throws CstlServiceException {
        isWorking();

        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter " + SERVICE_PARAMETER + " must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }

        //check LANGUAGE=en-EN
        if (request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)) {
            throw new CstlServiceException("The specified " + LANGUAGE_PARAMETER + " is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        //check mandatory version is not missing.
        if (request.getVersion() == null || request.getVersion().toString().isEmpty()) {
            throw new CstlServiceException("The parameter " + VERSION_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        //check VERSION=1.0.0
        if (request.getVersion().toString().equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return describeProcess100((org.geotoolkit.wps.xml.v100.DescribeProcess) request);
        } else {
            throw new CstlServiceException("The specified " + VERSION_PARAMETER + " number is not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Describe a process in WPS v1.0.0.
     *
     * @param request request
     * @return ProcessDescriptions
     * @throws CstlServiceException
     */
    private ProcessDescriptions describeProcess100(DescribeProcess request) throws CstlServiceException {

        //needed to get the public adress of generated schemas (for feature parameters).
        updateWebDavURL();
        schemaURL = schemaFolder.replace(webdavFolderPath, webdavURL);

        //check mandatory IDENTIFIER is not missing.
        if (request.getIdentifier() == null || request.getIdentifier().isEmpty()) {
            throw new CstlServiceException("The parameter " + IDENTIFER_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        final ProcessDescriptions descriptions = new ProcessDescriptions();
        descriptions.setLang(WPS_LANG);
        descriptions.setService(WPS_SERVICE);
        descriptions.setVersion(WPS_1_0_0);

        for (final CodeType identifier : request.getIdentifier()) {

            // Find the process
            final ProcessDescriptor processDesc = WPSUtils.getProcessDescriptor(identifier.getValue());
            if (!WPSUtils.isSupportedProcess(processDesc)) {
                throw new CstlServiceException("Process " + identifier.getValue() + " not supported by the service.",
                        INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
            }

            final ProcessDescriptionType descriptionType = new ProcessDescriptionType();
            descriptionType.setIdentifier(identifier);                                                                      //Process Identifier
            descriptionType.setTitle(WPSUtils.buildProcessTitle(processDesc));                                              //Process Title
            descriptionType.setAbstract(WPSUtils.capitalizeFirstLetter(processDesc.getProcedureDescription().toString()));  //Process abstract
            descriptionType.setProcessVersion(WPS_1_0_0);                                                                   //Process version
            descriptionType.setWSDL(null);                                                                                  //TODO WSDL
            descriptionType.setStatusSupported(true);
            descriptionType.setStoreSupported(supportStorage);

            // Get process input and output descriptors
            final ParameterDescriptorGroup input = processDesc.getInputDescriptor();
            final ParameterDescriptorGroup output = processDesc.getOutputDescriptor();

            ///////////////////////////////
            //  Process Input parameters
            ///////////////////////////////
            final ProcessDescriptionType.DataInputs dataInputs = new ProcessDescriptionType.DataInputs();
            for (final GeneralParameterDescriptor param : input.descriptors()) {

                /*
                 * Whatever the parameter type is, we prepare the name, title, abstract and multiplicity parts.
                 */
                final InputDescriptionType in = new InputDescriptionType();

                // Parameter informations
                in.setIdentifier(new CodeType(WPSUtils.buildProcessIOIdentifiers(processDesc, param, WPSIO.IOType.INPUT)));
                in.setTitle(WPSUtils.capitalizeFirstLetter(param.getName().getCode()));

                if (param.getRemarks() != null) {
                    in.setAbstract(WPSUtils.capitalizeFirstLetter(param.getRemarks().toString()));
                } else {
                    in.setAbstract(WPSUtils.capitalizeFirstLetter("No description available"));
                }

                //set occurs
                in.setMaxOccurs(BigInteger.valueOf(param.getMaximumOccurs()));
                in.setMinOccurs(BigInteger.valueOf(param.getMinimumOccurs()));

                // If the Parameter Descriptor isn't a ParameterDescriptorGroup
                if (param instanceof ParameterDescriptor) {
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;

                    // Input class
                    final Class clazz = paramDesc.getValueClass();

                    // BoundingBox type
                    if (WPSIO.isSupportedBBoxInputClass(clazz)) {
                        in.setBoundingBoxData(WPS_SUPPORTED_CRS);

                        //Complex type (XML, ...)
                    } else if (WPSIO.isSupportedComplexInputClass(clazz)) {
                        Map<String, Object> userData = null;
                        if (paramDesc instanceof ExtendedParameterDescriptor) {
                            userData = ((ExtendedParameterDescriptor) paramDesc).getUserObject();
                        }
                        in.setComplexData(WPSUtils.describeComplex(clazz, WPSIO.IOType.INPUT, WPSIO.FormChoice.COMPLEX, userData));

                        //Reference type (XML, ...)
                    } else if (WPSIO.isSupportedLiteralInputClass(clazz)) {
                        final LiteralInputType literal = new LiteralInputType();

                        if (paramDesc.getDefaultValue() != null) {
                            literal.setDefaultValue(paramDesc.getDefaultValue().toString()); //default value if enable
                        }

                        if (paramDesc.getUnit() != null) {
                            literal.setUOMs(WPSUtils.generateUOMs(paramDesc));
                        }
                        //AllowedValues setted
                        if (paramDesc.getValidValues() != null && !paramDesc.getValidValues().isEmpty()) {
                            literal.setAllowedValues(new AllowedValues(paramDesc.getValidValues()));
                        } else {
                            literal.setAnyValue(new AnyValue());
                        }
                        literal.setValuesReference(null);
                        literal.setDataType(WPSConvertersUtils.createDataType(clazz));

                        in.setLiteralData(literal);

                    } else if (WPSIO.isSupportedReferenceInputClass(clazz)) {
                        Map<String, Object> userData = null;
                        if (paramDesc instanceof ExtendedParameterDescriptor) {
                            userData = ((ExtendedParameterDescriptor) paramDesc).getUserObject();
                        }
                        in.setComplexData(WPSUtils.describeComplex(clazz, WPSIO.IOType.INPUT, WPSIO.FormChoice.REFERENCE, userData));

                        //Simple object (Integer, double, ...) and Object which need a conversion from String like affineTransform or WKT Geometry
                    } else {
                        throw new CstlServiceException("Process input not supported.", NO_APPLICABLE_CODE);
                    }

                } else if (param instanceof ParameterDescriptorGroup) {
                    /*
                     * If we get a parameterDescriptorGroup, we must expose the 
                     * parameters contained in it as one single input. To do so,
                     * we'll expose a feature type input.
                     */
                    FeatureType ft = WPSConvertersUtils.descriptorGroupToFeatureType((ParameterDescriptorGroup) param);

                    // Build the schema xsd, and store it into temporary folder.
                    String placeToStore = schemaFolder + "/" + ft.getName().getLocalPart() + ".xsd";
                    String publicAddress = schemaURL + "/" + ft.getName().getLocalPart() + ".xsd";
                    File xsdStore = new File(placeToStore);
                    try {
                        WPSUtils.storeFeatureSchema(ft, xsdStore);
                        final Class clazz = ft.getClass();
                        HashMap<String, Object> userData = new HashMap<>(1);
                        userData.put(WPSIO.SCHEMA_KEY, publicAddress);
                        in.setComplexData(WPSUtils.describeComplex(clazz, WPSIO.IOType.INPUT, WPSIO.FormChoice.COMPLEX, userData));
                    } catch (JAXBException ex) {
                        throw new CstlServiceException("The schema for parameter " + param.getName().getCode() + "can't be build.", NO_APPLICABLE_CODE);
                    }

                } else {
                    throw new CstlServiceException("Process parameter invalid", NO_APPLICABLE_CODE);
                }

                dataInputs.getInput().add(in);
            }
            if (!dataInputs.getInput().isEmpty()) {
                descriptionType.setDataInputs(dataInputs);
            }

            ///////////////////////////////
            //  Process Output parameters
            ///////////////////////////////
            final ProcessDescriptionType.ProcessOutputs dataOutput = new ProcessDescriptionType.ProcessOutputs();
            for (GeneralParameterDescriptor param : output.descriptors()) {
                final OutputDescriptionType out = new OutputDescriptionType();

                //parameter information
                out.setIdentifier(new CodeType(WPSUtils.buildProcessIOIdentifiers(processDesc, param, WPSIO.IOType.OUTPUT)));
                out.setTitle(WPSUtils.capitalizeFirstLetter(param.getName().getCode()));
                if (param.getRemarks() != null) {
                    out.setAbstract(WPSUtils.capitalizeFirstLetter(param.getRemarks().toString()));
                } else {
                    out.setAbstract(WPSUtils.capitalizeFirstLetter("No description available"));
                }

                //simple parameter
                if (param instanceof ParameterDescriptor) {
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;

                    //input class
                    final Class clazz = paramDesc.getValueClass();

                    //BoundingBox type
                    if (WPSIO.isSupportedBBoxOutputClass(clazz)) {
                        out.setBoundingBoxOutput(WPS_SUPPORTED_CRS);

                        //Simple object (Integer, double) and Object which need a conversion from String like affineTransform or Geometry
                        //Complex type (XML, raster, ...)
                    } else if (WPSIO.isSupportedComplexInputClass(clazz)) {
                        Map<String, Object> userData = null;
                        if (paramDesc instanceof ExtendedParameterDescriptor) {
                            userData = ((ExtendedParameterDescriptor) paramDesc).getUserObject();
                        }
                        out.setComplexOutput(WPSUtils.describeComplex(clazz, WPSIO.IOType.OUTPUT, WPSIO.FormChoice.COMPLEX, userData));

                        //Reference type (XML, ...)
                    } else if (WPSIO.isSupportedLiteralOutputClass(clazz)) {

                        final LiteralOutputType literal = new LiteralOutputType();
                        literal.setDataType(WPSConvertersUtils.createDataType(clazz));
                        if (paramDesc.getUnit() != null) {
                            literal.setUOMs(WPSUtils.generateUOMs(paramDesc));
                        }

                        out.setLiteralOutput(literal);

                    } else if (WPSIO.isSupportedReferenceInputClass(clazz)) {
                        Map<String, Object> userData = null;
                        if (paramDesc instanceof ExtendedParameterDescriptor) {
                            userData = ((ExtendedParameterDescriptor) paramDesc).getUserObject();
                        }
                        out.setComplexOutput(WPSUtils.describeComplex(clazz, WPSIO.IOType.OUTPUT, WPSIO.FormChoice.REFERENCE, userData));

                    } else {
                        throw new CstlServiceException("Process output not supported.", NO_APPLICABLE_CODE);
                    }

                } else if (param instanceof ParameterDescriptorGroup) {
                    /*
                     * If we get a parameterDescriptorGroup, we must expose the 
                     * parameters contained in it as one single input. To do so,
                     * we'll expose a feature type input.
                     */
                    FeatureType ft = WPSConvertersUtils.descriptorGroupToFeatureType((ParameterDescriptorGroup) param);

                    // Input class
                    final Class clazz = ft.getClass();
                    String placeToStore = schemaFolder + "/" + ft.getName().getLocalPart() + ".xsd";
                    String publicAddress = schemaURL + "/" + ft.getName().getLocalPart() + ".xsd";
                    File xsdStore = new File(placeToStore);
                    try {
                        WPSUtils.storeFeatureSchema(ft, xsdStore);
                        HashMap<String, Object> userData = new HashMap<>(1);
                        userData.put(WPSIO.SCHEMA_KEY, publicAddress);
                        out.setComplexOutput(WPSUtils.describeComplex(clazz, WPSIO.IOType.OUTPUT, WPSIO.FormChoice.COMPLEX, userData));
                    } catch (JAXBException ex) {
                        throw new CstlServiceException("The schema for parameter " + param.getName().getCode() + "can't be build.", NO_APPLICABLE_CODE);
                    }

                } else {
                    throw new CstlServiceException("Process parameter invalid", NO_APPLICABLE_CODE);
                }

                dataOutput.getOutput().add(out);
            }
            descriptionType.setProcessOutputs(dataOutput);
            descriptions.getProcessDescription().add(descriptionType);
        }

        return descriptions;
    }

    //////////////////////////////////////////////////////////////////////
    //                      Execute
    //////////////////////////////////////////////////////////////////////
    /**
     * Redirect execute requests from the WPS version requested.
     *
     * @param request request
     * @return execute response (Raw data or Document response) depends of the ResponseFormType in execute request
     * @throws CstlServiceException
     */
    public Object execute(Execute request) throws CstlServiceException {
        isWorking();
        updateWebDavURL();
        updateWMSURL();

        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter " + SERVICE_PARAMETER + " must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }

        //check LANGUAGE=en-EN
        if (request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)) {
            throw new CstlServiceException("The specified " + LANGUAGE_PARAMETER + " is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        //check mandatory version is not missing.
        if (request.getVersion() == null || request.getVersion().toString().isEmpty()) {
            throw new CstlServiceException("The parameter " + VERSION_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        //check VERSION=1.0.0
        if (request.getVersion().toString().equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return execute100((org.geotoolkit.wps.xml.v100.Execute) request);
        } else {
            throw new CstlServiceException("The specified " + VERSION_PARAMETER + " number is not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Execute a process in wps v1.0.0.
     *
     * @param request request
     * @return execute response (Raw data or Document response) depends of the ResponseFormType in execute request
     * @throws CstlServiceException
     */
    private Object execute100(Execute request) throws CstlServiceException {

        //check mandatory IDENTIFIER is not missing.
        if (request.getIdentifier() == null || request.getIdentifier().getValue() == null || request.getIdentifier().getValue().isEmpty()) {
            throw new CstlServiceException("The parameter " + IDENTIFER_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        //Find the process
        final ProcessDescriptor processDesc = WPSUtils.getProcessDescriptor(request.getIdentifier().getValue());

        if (!WPSUtils.isSupportedProcess(processDesc)) {
            throw new CstlServiceException("Process " + request.getIdentifier().getValue() + " not supported by the service.",
                    INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        //check requested INPUT/OUTPUT. Throw a CstlException otherwise.
        WPSUtils.checkValidInputOuputRequest(processDesc, request);

        /*
         * Get the requested output form
         */
        final ResponseFormType responseForm = request.getResponseForm();
        OutputDefinitionType rawData = null;
        final ResponseDocumentType respDoc;

        if (responseForm == null) {
            //create default response form if not define
            final List<DocumentOutputDefinitionType> outputs = new ArrayList<>();

            for (GeneralParameterDescriptor gpd : processDesc.getOutputDescriptor().descriptors()) {
                if (gpd instanceof ParameterDescriptor) {
                    final DocumentOutputDefinitionType docOutDef = new DocumentOutputDefinitionType();
                    docOutDef.setIdentifier(new CodeType(WPSUtils.buildProcessIOIdentifiers(processDesc, gpd, WPSIO.IOType.OUTPUT)));
                    docOutDef.setAsReference(false);
                    outputs.add(docOutDef);
                }
                //TODO handle sub levels of ParameterDescriptors
            }

            respDoc = new ResponseDocumentType();
            respDoc.setLineage(false);
            respDoc.setStatus(false);
            respDoc.setStoreExecuteResponse(false);
            respDoc.getOutput().addAll(outputs);

        } else {
            rawData = responseForm.getRawDataOutput();
            respDoc = responseForm.getResponseDocument();
        }


        boolean isOutputRaw = rawData != null; // the default output is a ResponseDocument
        boolean isOutputRespDoc = respDoc != null;

        if (!isOutputRaw && !isOutputRespDoc) {
            throw new CstlServiceException("The response form should be defined.", MISSING_PARAMETER_VALUE, "responseForm");
        }

        //status="true" && storeExecuteResponse="false" -> exception (see WPS-1.0.0 spec page 43).
        if(isOutputRespDoc && respDoc.isStatus() && !respDoc.isStoreExecuteResponse()){
             throw new CstlServiceException("Set the storeExecuteResponse to true if you want to see status in response documents.", INVALID_PARAMETER_VALUE, "storeExecuteResponse");
        }

        LOGGER.log(Level.INFO, "Process Execute : {0}", request.getIdentifier().getValue());

        /*
         * ResponseDocument attributes
         */
        boolean useLineage = isOutputRespDoc && respDoc.isLineage();
        boolean useStatus = isOutputRespDoc && respDoc.isStatus();
        boolean useStorage = isOutputRespDoc && respDoc.isStoreExecuteResponse();
        final List<DocumentOutputDefinitionType> wantedOutputs = isOutputRespDoc ? respDoc.getOutput() : null;

        //LOGGER.log(Level.INFO, "Request : [Lineage=" + isLineage + ", Storage=" + useStatus + ", Status=" + useStorage + "]");


        //Input temporary files used by the process. In order to delete them at the end of the process.
        List<File> files = null;

        //Create Process and Inputs
        final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();

        ///////////////////////
        //   Process INPUT
        //////////////////////
        List<InputType> requestInputData = new ArrayList<>();
        if (request.getDataInputs() != null && request.getDataInputs().getInput() != null) {
            requestInputData = request.getDataInputs().getInput();
        }
        final List<GeneralParameterDescriptor> processInputDesc = processDesc.getInputDescriptor().descriptors();
        //Fill input process with there default values
        for (final GeneralParameterDescriptor inputGeneDesc : processInputDesc) {

            if (inputGeneDesc instanceof ParameterDescriptor) {
                final ParameterDescriptor inputDesc = (ParameterDescriptor) inputGeneDesc;

                if (inputDesc.getDefaultValue() != null) {
                    in.parameter(inputDesc.getName().getCode()).setValue(inputDesc.getDefaultValue());
                }
            } 
        }

        //Fill process input with data from execute request.
        fillProcessInputFromRequest(in, requestInputData, processInputDesc, files);


        ///////////////////////
        //   RUN Process
        //////////////////////


        //Give input parameter to the process
        final org.geotoolkit.process.Process process = processDesc.createProcess(in);

        //Submit the process execution to the ExecutorService
        final List<GeneralParameterDescriptor> processOutputDesc = processDesc.getOutputDescriptor().descriptors();
        ParameterValueGroup result = null;

        if (isOutputRaw) {

            ////////
            // RAW Sync no timeout
            ////////
            final Future<ParameterValueGroup> future = WPSService.getExecutor().submit(process);
            try {
                result = future.get();

            } catch (InterruptedException ex) {
                throw new CstlServiceException("Process interrupted.", ex, NO_APPLICABLE_CODE);
            } catch (ExecutionException ex) {
                throw new CstlServiceException("Process execution failed.", ex, NO_APPLICABLE_CODE);
            }
            return createRawOutput(rawData, processOutputDesc, result);

        } else {

            final ExecuteResponse response = new ExecuteResponse();
            response.setService(WPS_SERVICE);
            response.setVersion(WPS_1_0_0);
            response.setLang(WPS_LANG);
            response.setServiceInstance(getServiceUrl() + "SERVICE=WPS&REQUEST=GetCapabilities");

            //Give a brief process description into the execute response
            response.setProcess(WPSUtils.generateProcessBrief(processDesc));

            //Lineage option.
            if (useLineage) {
                //Inputs
                response.setDataInputs(request.getDataInputs());
                final OutputDefinitionsType outputsDef = new OutputDefinitionsType();
                outputsDef.getOutput().addAll(respDoc.getOutput());
                //Outputs
                response.setOutputDefinitions(outputsDef);
            }

            final Map<String, Object> parameters =  buildParametersMap(
                    webdavURL,
                    webdavFolderPath,
                    wmsInstanceName,
                    wmsInstanceURL,
                    fileCoverageStorePath,
                    wmsProviderId,
                    "WPS_"+getId()+"_"+WPSUtils.buildLayerName(processDesc),
                    wmsSupported);

            StatusType status = new StatusType();
            ////////
            // DOC Async
            ////////
            if (useStorage) {
                if (!supportStorage) {
                    throw new CstlServiceException("Storage not supported.", STORAGE_NOT_SUPPORTED, "storeExecuteResponse");
                }

                final String respDocFileName = UUID.randomUUID().toString();

                //copy of the current response for async purpose.
                final ExecuteResponse responseAsync = new ExecuteResponse(response);

                process.addListener(new WPSProcessListener(request, responseAsync, respDocFileName, ServiceDef.WPS_1_0_0, parameters));

                status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
                status.setProcessAccepted("Process "+request.getIdentifier().getValue()+" accepted.");
                response.setStatus(status);

                //run process in asynchronous
                WPSService.getExecutor().submit(process);

                //store response document
                WPSUtils.storeResponse(response, webdavFolderPath, respDocFileName);
                response.setStatusLocation(webdavURL + "/" + respDocFileName); //Output data URL

            } else {

                ////////
                // DOC Sync
                ////////
                final Future<ParameterValueGroup> future = WPSService.getExecutor().submit(process);

                final ProcessFailedType processFT = new ProcessFailedType();
                ExceptionReport report = null;


                // timeout
                try {
                    //run process
                    result = future.get(TIMEOUT, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.WARNING, "Process "+WPSUtils.buildProcessIdentifier(processDesc)+" interrupted.", ex);
                    report = new ExceptionReport(ex.getLocalizedMessage(), null, null, null);
                } catch (ExecutionException ex) {
                    //process exception
                    LOGGER.log(Level.WARNING, "Process "+WPSUtils.buildProcessIdentifier(processDesc)+" has failed.", ex);
                    report = new ExceptionReport("Process error : " + ex.getLocalizedMessage(), null, null, null);
                } catch (TimeoutException ex) {
                    ((AbstractProcess) process).cancelProcess();
                    future.cancel(true);

                    report = new ExceptionReport("Process execution timeout. This process is too long and had been canceled,"
                            + " re-run request with status set to true.", null, null, null);

                }

                if (report != null) {
                    processFT.setExceptionReport(report);
                    status.setProcessFailed(processFT);
                    status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());

                } else {

                    //no error - fill response outputs.
                    final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
                    fillOutputsFromProcessResult(outputs, wantedOutputs, processOutputDesc, result, parameters);
                    response.setProcessOutputs(outputs);
                    status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
                    status.setProcessSucceeded("Process completed.");
                }

                // add status
                response.setStatus(status);
            }

            //Delete input temporary files
            //WPSUtils.cleanTempFiles(files);
            return response;
        }
    }

    /**
     * For each inputs in Execute request, this method will find corresponding {@link ParameterDescriptor ParameterDescriptor} input in the
     * process and fill the {@link ParameterValueGroup ParameterValueGroup} with the data.
     *
     * @param in
     * @param requestInputData
     * @param processInputDesc
     * @param files
     * @throws CstlServiceException
     */
    private void fillProcessInputFromRequest(final ParameterValueGroup in, final List<InputType> requestInputData,
            final List<GeneralParameterDescriptor> processInputDesc, List<File> files) throws CstlServiceException {

        ArgumentChecks.ensureNonNull("in", in);
        ArgumentChecks.ensureNonNull("requestInputData", requestInputData);

        for (final InputType inputRequest : requestInputData) {

            if (inputRequest.getIdentifier() == null || inputRequest.getIdentifier().getValue() == null || inputRequest.getIdentifier().getValue().isEmpty()) {
                throw new CstlServiceException("Empty input Identifier.", INVALID_PARAMETER_VALUE);
            }

            final String inputIdentifier = inputRequest.getIdentifier().getValue();
            final String inputIdentifierCode = WPSUtils.extractProcessIOCode(inputIdentifier);

            //Check if it's a valid input identifier and hold it if found.
            GeneralParameterDescriptor inputDescriptor = null;
            for (final GeneralParameterDescriptor processInput : processInputDesc) {
                if (processInput.getName().getCode().equals(inputIdentifierCode)) {
                    inputDescriptor = processInput;
                    break;
                }
            }
            if (inputDescriptor == null) {
                throw new CstlServiceException("Invalid or unknown input identifier " + inputIdentifier + ".", INVALID_PARAMETER_VALUE, inputIdentifier);
            }

            boolean isReference = false;
            boolean isBBox = false;
            boolean isComplex = false;
            boolean isLiteral = false;

            if (inputRequest.getReference() != null) {
                isReference = true;
            } else {
                if (inputRequest.getData() != null) {

                    final DataType dataType = inputRequest.getData();
                    if (dataType.getBoundingBoxData() != null) {
                        isBBox = true;
                    } else if (dataType.getComplexData() != null) {
                        isComplex = true;
                    } else if (dataType.getLiteralData() != null) {
                        isLiteral = true;
                    } else {
                        throw new CstlServiceException("Input Data element not found.");
                    }
                } else {
                    throw new CstlServiceException("Input doesn't have data or reference.");
                }
            }

            /*
             * Get expected input Class from the process input
             */
            Class expectedClass;
            if(inputDescriptor instanceof ParameterDescriptor) {
                expectedClass = ((ParameterDescriptor)inputDescriptor).getValueClass();
            } else {
                expectedClass = Feature.class;
            }
            
            Object dataValue = null;
            //LOGGER.log(Level.INFO, "Input : " + inputIdentifier + " : expected Class " + expectedClass.getCanonicalName());

            /**
             * Handle referenced input data.
             */
            if (isReference) {

                //Check if the expected class is supported for reference using
                if (!WPSIO.isSupportedReferenceInputClass(expectedClass)) {
                    throw new CstlServiceException("The input" + inputIdentifier + " can't handle reference.", INVALID_PARAMETER_VALUE, inputIdentifier);
                }
                final InputReferenceType requestedRef = inputRequest.getReference();
                if (requestedRef.getHref() == null) {
                    throw new CstlServiceException("Invalid reference input : href can't be null.", INVALID_PARAMETER_VALUE, inputIdentifier);
                }
                try {
                    dataValue = WPSConvertersUtils.convertFromReference(requestedRef, expectedClass);
                } catch (NonconvertibleObjectException ex) {
                    LOGGER.log(Level.WARNING, "Error during conversion of reference input {0}.",inputIdentifier);
                    throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                }

                if (dataValue instanceof File) {
                    if (files == null) {
                        files = new ArrayList<>();
                    }
                    files.add((File) dataValue);
                }
            }

            /**
             * Handle Bbox input data.
             */
            if (isBBox) {
                final BoundingBoxType bBox = inputRequest.getData().getBoundingBoxData();
                final List<Double> lower = bBox.getLowerCorner();
                final List<Double> upper = bBox.getUpperCorner();
                final String crs = bBox.getCrs();
                final int dimension = bBox.getDimensions();

                //Check if it's a 2D boundingbox
                if (dimension != 2 || lower.size() != 2 || upper.size() != 2) {
                    throw new CstlServiceException("Invalid data input : Only 2 dimension boundingbox supported.", OPERATION_NOT_SUPPORTED, inputIdentifier);
                }

                CoordinateReferenceSystem crsDecode;
                try {
                    crsDecode = CRS.decode(crs);
                } catch (FactoryException ex) {
                    throw new CstlServiceException("Invalid data input : CRS not supported.",
                            ex, OPERATION_NOT_SUPPORTED, inputIdentifier);
                }

                dataValue = GeometryUtils.createCRSEnvelope(crsDecode, lower.get(0), lower.get(1), upper.get(0), upper.get(1));
            }

            /**
             * Handle Complex input data.
             */
            if (isComplex) {
                //Check if the expected class is supported for complex using
                if (!WPSIO.isSupportedComplexInputClass(expectedClass)) {
                    throw new CstlServiceException("Complex value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                }

                final ComplexDataType complex = inputRequest.getData().getComplexData();

                if (complex.getContent() == null || complex.getContent().size() <= 0) {
                    throw new CstlServiceException("Missing data input value.", INVALID_PARAMETER_VALUE, inputIdentifier);

                } else {

                    try {
                        dataValue = WPSConvertersUtils.convertFromComplex(complex, expectedClass);
                    } catch (NonconvertibleObjectException ex) {
                        LOGGER.log(Level.WARNING, "Error during conversion of complex input {0}.",inputIdentifier);
                        throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                    }
                }
            }

            /**
             * Handle Literal input data.
             */
            if (isLiteral) {
                //Check if the expected class is supported for literal using
                if (!WPSIO.isSupportedLiteralInputClass(expectedClass)) {
                    throw new CstlServiceException("Literal value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                }

                if(!(inputDescriptor instanceof ParameterDescriptor)) {
                    throw new CstlServiceException("Invalid parameter type.", INVALID_PARAMETER_VALUE, inputIdentifier);
                }
                
                final LiteralDataType literal = inputRequest.getData().getLiteralData();
                final String data = literal.getValue();

                final Unit paramUnit = ((ParameterDescriptor)inputDescriptor).getUnit();
                if (paramUnit != null) {
                    final Unit requestedUnit = Unit.valueOf(literal.getUom());
                    final UnitConverter converter = requestedUnit.getConverterTo(paramUnit);
                    dataValue = converter.convert(Double.valueOf(data));

                } else {

                    try {
                        dataValue = WPSConvertersUtils.convertFromString(data, expectedClass);
                    } catch (NonconvertibleObjectException ex) {
                        LOGGER.log(Level.WARNING, "Error during conversion of literal input {0}.",inputIdentifier);
                        throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                    }
                }
            }

            try {
                if(inputDescriptor instanceof ParameterDescriptor) {
                    in.parameter(inputIdentifierCode).setValue(dataValue);
                } else if(inputDescriptor instanceof ParameterDescriptorGroup && dataValue instanceof ComplexAttribute) {
                    WPSConvertersUtils.featureToParameterGroup(
                            (ComplexAttribute)dataValue, 
                            in.addGroup(inputIdentifierCode));
                } else {
                    throw new Exception();
                }
            } catch (Exception ex) {
                throw new CstlServiceException("Invalid data input value.", ex, INVALID_PARAMETER_VALUE, inputIdentifier);
            }
        }
    }

    /**
     * Fill outputs of the ProcessOutputs object using the process result, the
     * list of requested outputs and the list of process output descriptors.
     *
     * @param outputs The WPS outputs to fill.
     * @param wantedOutputs The definition of the outputs we must treat.
     * @param processOutputDesc The descriptors of the geotk process outputs.
     * @param result The values which have been filled by the process.
     * @param parameters URL of the WPS service.
     * @throws CstlServiceException If one of the outputs is invalid.
     */
    public static void fillOutputsFromProcessResult(final ProcessOutputs outputs, final List<DocumentOutputDefinitionType> wantedOutputs,
            final List<GeneralParameterDescriptor> processOutputDesc, final ParameterValueGroup result, final Map<String, Object> parameters)
            throws CstlServiceException {
        if (result == null) {
            throw new CstlServiceException("Empty process result.", NO_APPLICABLE_CODE);
        }

        for (final DocumentOutputDefinitionType outputsRequest : wantedOutputs) {

            if (outputsRequest.getIdentifier() == null || outputsRequest.getIdentifier().getValue() == null || outputsRequest.getIdentifier().getValue().isEmpty()) {
                throw new CstlServiceException("Empty output Identifier.", INVALID_PARAMETER_VALUE);
            }

            final String outputIdentifier = outputsRequest.getIdentifier().getValue();
            final String outputIdentifierCode = WPSUtils.extractProcessIOCode(outputIdentifier);

            //Check if it's a valid input identifier and hold it if found.
            GeneralParameterDescriptor outputDescriptor = null;
            for (final GeneralParameterDescriptor processInput : processOutputDesc) {
                if (processInput.getName().getCode().equals(outputIdentifierCode)) {
                    outputDescriptor = (ParameterDescriptor) processInput;
                    break;
                }
            }
            if (outputDescriptor == null) {
                throw new CstlServiceException("Invalid or unknown output identifier " + outputIdentifier + ".", INVALID_PARAMETER_VALUE, outputIdentifier);
            }

            if (outputDescriptor instanceof ParameterDescriptor) {
                //output value object.
                final Object outputValue = result.parameter(outputIdentifierCode).getValue();
                outputs.getOutput().add(createDocumentResponseOutput((ParameterDescriptor) outputDescriptor, outputsRequest, outputValue, parameters));
            } else if (outputDescriptor instanceof ParameterDescriptorGroup) {
                /**
                 * TODO: Treat ParameterValueGroup for outputs.
                 */
                throw new CstlServiceException("Invalid or unknown output identifier " + outputIdentifier + ".", INVALID_PARAMETER_VALUE, outputIdentifier); 
            } else {
                throw new CstlServiceException("Invalid or unknown output identifier " + outputIdentifier + ".", INVALID_PARAMETER_VALUE, outputIdentifier);                
            }

        }//end foreach wanted outputs
    }
    

    /**
     * Create {@link OutputDataType output} object for one requested output.
     *
     * @param outputDescriptor
     * @param requestedOutput
     * @param outputValue
     * @param parameters
     * @return
     * @throws CstlServiceException
     */
    public static OutputDataType createDocumentResponseOutput(final ParameterDescriptor outputDescriptor, final DocumentOutputDefinitionType requestedOutput,
            final Object outputValue, final Map<String, Object> parameters) throws CstlServiceException {

        final OutputDataType outData = new OutputDataType();

        final String outputIdentifier = requestedOutput.getIdentifier().getValue();
        final String outputIdentifierCode = WPSUtils.extractProcessIOCode(outputIdentifier);

        //set Output information
        outData.setIdentifier(new CodeType(outputIdentifier));

        //support custom title/abstract.
        final LanguageStringType titleOut = requestedOutput.getTitle() != null ?
                requestedOutput.getTitle() : WPSUtils.capitalizeFirstLetter(outputIdentifierCode);
        final LanguageStringType abstractOut = requestedOutput.getAbstract() != null ?
                requestedOutput.getAbstract() : WPSUtils.capitalizeFirstLetter(outputDescriptor.getRemarks().toString());
        outData.setTitle(titleOut);
        outData.setAbstract(abstractOut);

        final Class outClass = outputDescriptor.getValueClass(); // output class

        if (requestedOutput.isAsReference()) {
            final OutputReferenceType ref = createReferenceOutput(outputIdentifier, outClass, requestedOutput, outputValue, parameters);

            outData.setReference(ref);
        } else {

            final DataType data = new DataType();

            if (WPSIO.isSupportedBBoxOutputClass(outClass)) {
                org.opengis.geometry.Envelope envelop = (org.opengis.geometry.Envelope) outputValue;
                data.setBoundingBoxData(new BoundingBoxType(envelop));

            } else if (WPSIO.isSupportedComplexOutputClass(outClass)) {

                try {
                    ComplexDataType complex = null;
                    if (outputValue instanceof GridCoverage && requestedOutput.getMimeType().equals(WPSMimeType.OGC_WMS.val())) {
                        if (parameters.get(WMS_SUPPORTED).equals(Boolean.TRUE)) {
                            //add output identifier to layerName
                            parameters.put(WPSConvertersUtils.WMS_LAYER_NAME, parameters.get(WPSConvertersUtils.WMS_LAYER_NAME)+"_"+outputIdentifierCode);
                            complex = WPSConvertersUtils.convertToWMSComplex(
                                    outputValue,
                                    requestedOutput.getMimeType(),
                                    requestedOutput.getEncoding(),
                                    requestedOutput.getSchema(),
                                    parameters);
                            restartWMS(parameters);
                        } else {
                            LOGGER.log(Level.WARNING, "Can't publish {0} value in a WMS.", outputIdentifier);
                        }

                    } else {
                        complex = WPSConvertersUtils.convertToComplex(
                            outputValue,
                            requestedOutput.getMimeType(),
                            requestedOutput.getEncoding(),
                            requestedOutput.getSchema(),
                            parameters);
                    }

                    data.setComplexData(complex);

                } catch (NonconvertibleObjectException ex) {
                    LOGGER.log(Level.WARNING, "Error during conversion of complex output {0}.", outputIdentifier);
                    throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                }

            } else if (WPSIO.isSupportedLiteralOutputClass(outClass)) {

                final LiteralDataType literal = new LiteralDataType();
                literal.setDataType(WPSConvertersUtils.getDataTypeString(outClass));
                literal.setValue(WPSConvertersUtils.convertToString(outputValue));
                if (outputDescriptor.getUnit() != null) {
                    literal.setUom(outputDescriptor.getUnit().toString());
                }
                data.setLiteralData(literal);

            } else {
                throw new CstlServiceException("Process output parameter invalid", MISSING_PARAMETER_VALUE, outputIdentifier);
            }

            outData.setData(data);
        }
        return outData;
    }

    private static void restartWMS(Map<String, Object> parameters) {

        final String wmsInstance = (String) parameters.get(WPSConvertersUtils.WMS_INSTANCE_NAME);
        final String providerId = (String) parameters.get(WPSConvertersUtils.WMS_STORAGE_ID);

        //restart provider
        final Collection<DataProvider> layerProviders = DataProviders.getInstance().getProviders();
        for (DataProvider p : layerProviders) {
            if (p.getId().equals(providerId)) {
                p.reload();
                break;
            }
        }

        //restart WMS worker
        try {
            final ProcessDescriptor restartServiceDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);
            final ParameterValueGroup restartServiceInputs = restartServiceDesc.getInputDescriptor().createValue();
            restartServiceInputs.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue("WMS");
            restartServiceInputs.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(wmsInstance);
            restartServiceDesc.createProcess(restartServiceInputs).call();
        } catch (ProcessException | NoSuchIdentifierException e) {
            LOGGER.log(Level.WARNING, "Error during WMS " + wmsInstance + " restart.", e);
        }
    }

    /**
     * Create reference output.
     *
     * @param clazz
     * @param requestedOutput
     * @param outputValue
     * @param parameters
     * @return
     * @throws CstlServiceException
     */
    private static OutputReferenceType createReferenceOutput(final String outputIdentifier, final Class clazz, final DocumentOutputDefinitionType requestedOutput,
            final Object outputValue, final Map<String, Object> parameters) throws CstlServiceException {

        try {
           return (OutputReferenceType) WPSConvertersUtils.convertToReference(
                    outputValue,
                    requestedOutput.getMimeType(),
                    requestedOutput.getEncoding(),
                    requestedOutput.getSchema(),
                    parameters,
                    WPSIO.IOType.OUTPUT);

        } catch (NonconvertibleObjectException ex) {
            LOGGER.log(Level.WARNING, "Error during conversion of reference output {0}.", outputIdentifier);
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }


    /**
     * Handle Raw output.
     *
     * @param rawData
     * @param processOutputDesc
     * @param result
     * @return
     * @throws CstlServiceException
     */
    private Object createRawOutput(final OutputDefinitionType rawData, final List<GeneralParameterDescriptor> processOutputDesc,
            final ParameterValueGroup result) throws CstlServiceException {

        final String outputIdentifier = rawData.getIdentifier().getValue();
        final String outputIdentifierCode = WPSUtils.extractProcessIOCode(outputIdentifier);

        //Check if it's a valid input identifier and hold it if found.
        ParameterDescriptor outputDescriptor = null;
        for (final GeneralParameterDescriptor processInput : processOutputDesc) {
            if (processInput.getName().getCode().equals(outputIdentifierCode) && processInput instanceof ParameterDescriptor) {
                outputDescriptor = (ParameterDescriptor) processInput;
                break;
            }
        }
        if (outputDescriptor == null) {
            throw new CstlServiceException("Invalid or unknown output identifier " + outputIdentifier + ".", INVALID_PARAMETER_VALUE, outputIdentifier);
        }

        //output value object.
        final Object outputValue = result.parameter(outputIdentifierCode).getValue();

        if (outputValue instanceof Geometry) {
            try {

                final Geometry jtsGeom = (Geometry) outputValue;
                return JTStoGeometry.toGML("3.1.1", jtsGeom); // TODO determine gml version

            } catch (FactoryException ex) {
                throw new CstlServiceException(ex);
            }
        }

        if (outputValue instanceof Envelope) {
            return new BoundingBoxType((Envelope) outputValue);
        }
        return outputValue;
    }
    
    
    private void checkForSchemasToStore(ProcessDescriptor source) throws JAXBException {
        /*
         * Check each input and output. If we get a parameterDescriptorGroup,
         * we must store a schema which describe its structure.
         */
        for (GeneralParameterDescriptor desc : source.getInputDescriptor().descriptors()) {
            if (desc instanceof ParameterDescriptorGroup) {
                FeatureType ft = WPSConvertersUtils.descriptorGroupToFeatureType((ParameterDescriptorGroup) desc);
                String placeToStore = schemaFolder + "/" + ft.getName().getLocalPart() + ".xsd";
                File xsdStore = new File(placeToStore);
                WPSUtils.storeFeatureSchema(ft, xsdStore);
            }
        }
        for (GeneralParameterDescriptor desc : source.getOutputDescriptor().descriptors()) {
            if (desc instanceof ParameterDescriptorGroup) {
                FeatureType ft = WPSConvertersUtils.descriptorGroupToFeatureType((ParameterDescriptorGroup) desc);
                String placeToStore = schemaFolder + "/" + ft.getName().getLocalPart() + ".xsd";
                File xsdStore = new File(placeToStore);
                WPSUtils.storeFeatureSchema(ft, xsdStore);
            }
        }
    }
    
    @Override
    protected String getProperty(final String key) {
        if (context != null && context.getCustomParameters() != null) {
            return context.getCustomParameters().get(key);
        }
        return null;
    }
}

