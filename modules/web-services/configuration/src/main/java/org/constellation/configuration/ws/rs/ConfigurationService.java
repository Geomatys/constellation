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
package org.constellation.configuration.ws.rs;

// J2SE dependencies
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.logging.Level;

// Jersey dependencies
import javax.annotation.PreDestroy;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.UpdatePropertiesFileType;
import org.constellation.configuration.UpdateXMLFileType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.AbstractWebService;
import org.constellation.ws.rs.ContainerNotifierImpl;


// Geotoolkit dependencies
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.ows.xml.v110.ExceptionReport;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * Web service for administration and configuration operations.
 * <p>
 * This web service enables basic remote management of a Constellation server. 
 * </p>
 * <p>
 * <b>WARNING:</b>Use of this service is discouraged since it is run without any 
 * security control. 
 * </p>
 * 
 * @author Guilhem Legal (Geomatys)
 * @since 0.1
 */
@Path("configuration")
@Singleton
public final class ConfigurationService extends AbstractWebService  {

    /**
     * A container notifier allowing to dynamically reload all the active service.
     */
    @Context
    private ContainerNotifierImpl cn;
    
    private AbstractCSWConfigurer cswConfigurer;
    
    private static FactoryRegistry factory = new FactoryRegistry(AbstractConfigurerFactory.class);
    
    private boolean cswFunctionEnabled;

    private static boolean indexing;

    private static final List<String> SERVICE_INDEXING = new ArrayList<String>();
    
    public static final Map<String, File> SERVCE_DIRECTORY = new HashMap<String, File>();
    static {
        SERVCE_DIRECTORY.put("CSW",      new File(ConfigDirectory.getConfigDirectory(), "csw_configuration"));
        SERVCE_DIRECTORY.put("SOS",      new File(ConfigDirectory.getConfigDirectory(), "sos_configuration"));
    }
    
    /**
     * Construct the ConfigurationService and configure its context.
     */
    public ConfigurationService() {
        super();
        indexing = false;
        try {
            final MarshallerPool pool = new MarshallerPool("org.geotoolkit.ows.xml.v110:org.constellation.configuration:org.geotoolkit.skos.xml:org.geotoolkit.internal.jaxb.geometry");
            setXMLContext(pool);
            final AbstractConfigurerFactory configurerfactory = factory.getServiceProvider(AbstractConfigurerFactory.class, null, null, null);
            cswConfigurer      = configurerfactory.getCSWConfigurer(cn);
            cswFunctionEnabled = true;
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXBException while setting the JAXB context for configuration service:" + ex.getMessage(), ex);
            cswFunctionEnabled = false;
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Specific CSW operation will not be available.\n", ex);
            cswFunctionEnabled = false;
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning("Factory not found for CSWConfigurer, specific CSW operation will not be available.");
            cswFunctionEnabled = false;
        }
        LOGGER.info("Configuration service runing");
    }
    
    /**
     * Handle the various types of requests made to the service.
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        Marshaller marshaller = null;
        try {
            marshaller = getMarshallerPool().acquireMarshaller();
            String request  = "";
            final StringWriter sw = new StringWriter();

            if (cswConfigurer != null) {
                cswConfigurer.setContainerNotifier(cn);
            }
            
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }

            if ("Restart".equalsIgnoreCase(request)) {
                final boolean force = Boolean.parseBoolean(getParameter("FORCED", false));
                marshaller.marshal(restartService(force), sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }
            
            if ("UpdatePropertiesFile".equalsIgnoreCase(request) || objectRequest instanceof UpdatePropertiesFileType) {
                final UpdatePropertiesFileType updateProp = (UpdatePropertiesFileType) objectRequest;
                marshaller.marshal(updatePropertiesFile(updateProp), sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }

            if ("UpdateXMLFile".equalsIgnoreCase(request) || objectRequest instanceof UpdateXMLFileType) {
                final UpdateXMLFileType updateProp = (UpdateXMLFileType) objectRequest;
                marshaller.marshal(updateXmlFile(updateProp), sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }
            
            if ("Download".equalsIgnoreCase(request)) {    
                final File f = downloadFile();
                return Response.ok(f, MediaType.MULTIPART_FORM_DATA_TYPE).build(); 
            }
            
            
            /* CSW specific operations */
            
            if ("RefreshIndex".equalsIgnoreCase(request)) {
                if (cswFunctionEnabled) {
                    final boolean asynchrone = Boolean.parseBoolean((String) getParameter("ASYNCHRONE", false));
                    final String id          = getParameter("ID", false);
                    final boolean forced     = Boolean.parseBoolean((String) getParameter("FORCED", false));

                    if (isIndexing(id) && !forced) {
                        final AcknowlegementType refused = new AcknowlegementType("Failure",
                                "An indexation is already started for this service:" + id);
                        marshaller.marshal(refused, sw);
                        return Response.ok(sw.toString(), "text/xml").build();
                    } else if (indexing && forced) {
                        AbstractIndexer.stopIndexation(Arrays.asList(id));
                    }
                    
                    startIndexation(id);
                    AcknowlegementType ack;
                    try {
                        ack = cswConfigurer.refreshIndex(asynchrone, id);
                    } finally {
                        endIndexation(id);
                    }
                    marshaller.marshal(ack, sw);
                    return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
                }
            }

            if ("AddToIndex".equalsIgnoreCase(request)) {
                if (cswFunctionEnabled) {
                    final String service           = getParameter("SERVICE", false);
                    final String id                = getParameter("ID", false);
                    final List<String> identifiers = new ArrayList<String>();
                    final String identifierList    = getParameter("IDENTIFIERS", true);
                    final StringTokenizer tokens   = new StringTokenizer(identifierList, ",;");
                    while (tokens.hasMoreTokens()) {
                        final String token = tokens.nextToken().trim();
                        identifiers.add(token);
                    }

                    marshaller.marshal(cswConfigurer.addToIndex(service, id, identifiers), sw);
                    return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
                }
            }

            if ("stopIndex".equalsIgnoreCase(request)) {
                if (cswFunctionEnabled) {
                    //final String service     = getParameter("SERVICE", false);
                    final String id          = getParameter("ID", false);

                    final AcknowlegementType ack= stopIndexation(id);
                    marshaller.marshal(ack, sw);
                    return Response.ok(sw.toString(), "text/xml").build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, "Request");
                }
            }
            
            if ("RefreshCascadedServers".equalsIgnoreCase(request) || objectRequest instanceof CSWCascadingType) {
                final CSWCascadingType refreshCS = (CSWCascadingType) objectRequest;
                marshaller.marshal(cswConfigurer.refreshCascadedServers(refreshCS), sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }
            
            if ("UpdateVocabularies".equalsIgnoreCase(request)) {    
                if (cswFunctionEnabled) {
                    return Response.ok(cswConfigurer.updateVocabularies(),MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
                }
            }
            
            if ("UpdateContacts".equalsIgnoreCase(request)) {    
                if (cswFunctionEnabled) {
                    return Response.ok(cswConfigurer.updateContacts(),MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
                }
            }
            
            
            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                                 OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
            
        
        } catch (CstlServiceException ex) {
            final String code = StringUtilities.transformCodeName(ex.getExceptionCode().name());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(),
                                                               ServiceDef.CONFIG.exceptionVersion.toString());
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                    !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                    !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            } else {
                LOGGER.info(ex.getMessage());
            }
            return Response.ok(report, MimeType.TEXT_XML).build();
            
        } finally {
            if (marshaller != null) {
                getMarshallerPool().release(marshaller);
            }
        }
        
    }

    private boolean isIndexing(String id) {
        return indexing & SERVICE_INDEXING.contains(id);
    }

    private void startIndexation(String id) {
        indexing  = true;
        if (id != null) {
            SERVICE_INDEXING.add(id);
        }
    }

    private void endIndexation(String id) {
        indexing = false;
        if (id != null) {
            SERVICE_INDEXING.remove(id);
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
        final OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
        final ExceptionReport report = new ExceptionReport(message, code.name(), locator,
                                                       ServiceDef.CONFIG.exceptionVersion.toString());
        return Response.ok(report, MimeType.TEXT_XML).build();
    }

    /**
     * Restart all the web-services.
     * 
     * @return an Acknowlegement if the restart succeed.
     */
    private AcknowlegementType restartService(boolean forced) {
        LOGGER.info("\n restart requested \n");
        // Reload the style and layer provider proxies if they already exist.
        final StyleProviderProxy spp = StyleProviderProxy.getInstance(false);
        if (spp != null) {
            spp.reload();
        }
        final LayerProviderProxy lpp = LayerProviderProxy.getInstance(false);
        if (lpp != null) {
            lpp.reload();
        }

        if (cn != null) {
            if (!indexing) {
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted");
            } else if (!forced) {
                return new AcknowlegementType("failed", "There is an indexation running use the parameter FORCED=true to bypass it.");
            } else {
                AbstractIndexer.stopIndexation();
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted (previous indexation was stopped)");
            }
        } else {
            return new AcknowlegementType("failed", "The services can not be restarted (ContainerNotifier is null)");
        }
        
    }

    /**
     * Stop all the indexation going on.
     *
     * @return an Acknowlegement.
     */
    private AcknowlegementType stopIndexation(String id) {
        LOGGER.info("\n stop indexation requested \n");
        if (!isIndexing(id)) {
            return new AcknowlegementType("Success", "There is no indexation to stop");
        } else {
            AbstractIndexer.stopIndexation(Arrays.asList(id));
            return new AcknowlegementType("Success", "The indexation have been stopped");
        }
    }

    private void verifyBaseAttribute(final String service, final String fileName) throws CstlServiceException {
         if ( service == null) {
            throw new CstlServiceException("You must specify the service parameter.",
                                              MISSING_PARAMETER_VALUE, Parameters.SERVICE);
        } else if (!SERVCE_DIRECTORY.keySet().contains(service)) {
            final StringBuilder msg = new StringBuilder("Invalid value for the service parameter: ").append(service).append('\n');
            msg.append("accepted values are:");
            for (String s: SERVCE_DIRECTORY.keySet()) {
                msg .append(s).append(',');
            }
            throw new CstlServiceException(msg.toString(), MISSING_PARAMETER_VALUE, Parameters.SERVICE);
        }

        if (fileName == null) {
             throw new CstlServiceException("You must specify the fileName parameter.", MISSING_PARAMETER_VALUE, "fileName");
        }
    }
    
    /**
     * Update a properties file on the server file system.
     * 
     * @param request
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    private AcknowlegementType updatePropertiesFile(UpdatePropertiesFileType request) throws CstlServiceException {
        LOGGER.info("update properties file requested");
        
        final String service  = request.getService();
        final String fileName = request.getFileName();
        final Map<String, String> newProperties = request.getProperties();
        
        verifyBaseAttribute(service, fileName);
        
        if (newProperties == null || newProperties.isEmpty()) {
             throw new CstlServiceException("You must specify a non empty properties parameter.", MISSING_PARAMETER_VALUE, 
                     "properties");
        }
        
        final File configDir   = SERVCE_DIRECTORY.get(service);
        final File propertiesFile = new File(configDir, fileName);
        
        final Properties prop     = new Properties();
        if (propertiesFile.exists()) {
            for (Entry<String, String> entry : newProperties.entrySet()) {
                prop.put(entry.getKey(), entry.getValue());
            }
        } else {
            throw new CstlServiceException("The file does not exist: " + propertiesFile.getPath(),
                                          NO_APPLICABLE_CODE);
        }
        try {
            FileUtilities.storeProperties(prop, propertiesFile);
        } catch (IOException ex) {
            throw new CstlServiceException("IOException xhile trying to store the properties files.",
                                          NO_APPLICABLE_CODE);
        }
        
        return new AcknowlegementType(Parameters.SUCCESS, "properties file sucessfully updated");
    }

    /**
     * Update a properties file on the server file system.
     *
     * @param request
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    private AcknowlegementType updateXmlFile(UpdateXMLFileType request) throws CstlServiceException {
        LOGGER.info("update properties file requested");

        final String service    = request.getService();
        final String fileName   = request.getFileName();
        final Object newContent = request.getXmlContent();

        verifyBaseAttribute(service, fileName);

        if (newContent == null) {
             throw new CstlServiceException("You must specify a non empty xml content parameter.", MISSING_PARAMETER_VALUE,
                     "xmlContent");
        }

        final File configDir  = SERVCE_DIRECTORY.get(service);
        final File configFile = new File(configDir, fileName);

        Marshaller marshaller = null;
        try {
            marshaller = getMarshallerPool().acquireMarshaller();
            marshaller.marshal(newContent, configFile);
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXBException while trying to store the properties files.",
                                          NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                getMarshallerPool().release(marshaller);
            }
        }

        return new AcknowlegementType(Parameters.SUCCESS, "xml file sucessfully updated");
    }

    /**
     * Receive a file and write it into the static file path.
     * 
     * @param in The input stream.
     * @return an acknowledgement indicating if the operation succeed or not.
     *
     * @todo Not implemented. This is just a placeholder where we can customize the
     *       download action for some users. Will probably be removed in a future version.
     */
    @PUT
    public AcknowlegementType uploadFile(InputStream in) {
        LOGGER.info("uploading");
        try  {
            final String layer = getParameter("layer", false);
            LOGGER.log(Level.INFO, "LAYER= {0}", layer);
            // TODO: implement upload action here.
            in.close();
        } catch (CstlServiceException ex) {
            //must never happen in normal case
            LOGGER.severe("Webservice exception while get the layer parameter");
            return new AcknowlegementType("failed", "Webservice exception while get the layer parameter");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while uploading file");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return new AcknowlegementType("failed", "IO exception while performing upload");
        }
        return new AcknowlegementType(Parameters.SUCCESS, "the file has been successfully uploaded");
    }
    
    /**
     * Return a static file present on the server.
     * 
     * @return a file.
     *
     * @todo Not implemented. This is just a placeholder where we can customize the
     *       download action for some users. Will probably be removed in a future version.
     */
    private File downloadFile() throws CstlServiceException {
        throw new CstlServiceException("Not implemented", NO_APPLICABLE_CODE);
    }

    /**
     * Free the resource and close the connection at undeploy time.
     */
    @Override
    @PreDestroy
    public void destroy() {
        LOGGER.info("Shutting down the REST Configuration service facade. Disposing " +
                "all datastore instances.");
        if (cswConfigurer != null) {
            cswConfigurer.destroy();
        }
        try {
            final StyleProviderProxy spp = StyleProviderProxy.getInstance(false);
            if (spp != null) {
                spp.dispose();
            }
            final LayerProviderProxy lpp = LayerProviderProxy.getInstance(false);
            if (lpp != null) {
                lpp.dispose();
            }
        } catch (ExceptionInInitializerError ex) {
            // Factory Registery cannot found MutableStyleFactory instance.
            // shutdown is a bit late for looking for this factory.
            // @TODO avoid this above block if StyleProviderProxy has never been initialized.
            LOGGER.fine(ex.toString());
        }
    }
}
