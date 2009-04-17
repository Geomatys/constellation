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
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.util.Util;
import org.constellation.ows.OWSExceptionCode;
import org.constellation.ows.v110.ExceptionReport;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.WebService;
import org.constellation.ws.rs.ContainerNotifierImpl;
import static org.constellation.ows.OWSExceptionCode.*;

// Geotools dependencies
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.factory.FactoryNotFoundException;

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
public class ConfigurationService extends WebService  {

    /**
     * A container notifier allowing to dynamically reload all the active service.
     */
    @Context
    private ContainerNotifierImpl cn;
    
    private AbstractCSWConfigurer cswConfigurer;
    
    private static FactoryRegistry factory = new FactoryRegistry(AbstractConfigurerFactory.class);
    
    private boolean CSWFunctionEnabled;
    
    public final static Map<String, File> serviceDirectory = new HashMap<String, File>();
    static {
        serviceDirectory.put("CSW",      new File(getSicadeDirectory(), "csw_configuration"));
        serviceDirectory.put("SOS",      new File(getSicadeDirectory(), "sos_configuration"));
        serviceDirectory.put("MDSEARCH", new File(getSicadeDirectory(), "mdweb/search"));
    }
    
    /**
     * Construct the ConfigurationService and configure its context.
     */
    public ConfigurationService() {
        super();
        try {
            setXMLContext("org.constellation.ows.v110:org.constellation.configuration:org.constellation.skos", "");
            AbstractConfigurerFactory configurerfactory = factory.getServiceProvider(AbstractConfigurerFactory.class, null, null, null);
            cswConfigurer      = configurerfactory.getCSWConfigurer(cn);
            CSWFunctionEnabled = true;
        } catch (JAXBException ex) {
            workingContext = false;
            LOGGER.severe("JAXBException while setting the JAXB context for configuration service:" + ex.getMessage());
            ex.printStackTrace();
            CSWFunctionEnabled = false;
        } catch (ConfigurationException ex) {
            LOGGER.warning("Specific CSW operation will not be available." + '\n' + ex);
            CSWFunctionEnabled = false;
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning("Factory not found for CSWConfigurer, specific CSW operation will not be available.");
            CSWFunctionEnabled = false;
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
            marshaller = marshallers.take();
            String request  = "";
            StringWriter sw = new StringWriter();

            if (cswConfigurer != null) {
                cswConfigurer.setContainerNotifier(cn);
            }
            
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }

            if ("Restart".equalsIgnoreCase(request)) {
                marshaller.marshal(restartService(), sw);
                return Response.ok(sw.toString(), "text/xml").build();
            }
            
            if ("UpdatePropertiesFile".equalsIgnoreCase(request) || objectRequest instanceof UpdatePropertiesFileType) {
                UpdatePropertiesFileType updateProp = (UpdatePropertiesFileType) objectRequest;
                marshaller.marshal(updatePropertiesFile(updateProp), sw);
                return Response.ok(sw.toString(), "text/xml").build();
            }
            
            if ("Download".equalsIgnoreCase(request)) {    
                File f = downloadFile();
                return Response.ok(f, MediaType.MULTIPART_FORM_DATA_TYPE).build(); 
            }
            
            
            /* CSW specific operations */
            
            if ("RefreshIndex".equalsIgnoreCase(request)) {
                if (CSWFunctionEnabled) {
                    boolean asynchrone = Boolean.parseBoolean((String) getParameter("ASYNCHRONE", false));
                    String service     = getParameter("SERVICE", false);
                    String id          = getParameter("ID", false);
                
                    marshaller.marshal(cswConfigurer.refreshIndex(asynchrone, service, id), sw);
                    return Response.ok(sw.toString(), "text/xml").build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, "Request");
                }
            }

            if ("AddToIndex".equalsIgnoreCase(request)) {
                if (CSWFunctionEnabled) {
                    String service           = getParameter("SERVICE", false);
                    String id                = getParameter("ID", false);
                    List<String> identifiers = new ArrayList<String>();
                    String identifierList    = getParameter("IDENTIFIERS", true);
                    StringTokenizer tokens   = new StringTokenizer(identifierList, ",;");
                    while (tokens.hasMoreTokens()) {
                        final String token = tokens.nextToken().trim();
                        identifiers.add(token);
                    }

                    marshaller.marshal(cswConfigurer.addToIndex(service, id, identifiers), sw);
                    return Response.ok(sw.toString(), "text/xml").build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, "Request");
                }
            }
            
            if ("RefreshCascadedServers".equalsIgnoreCase(request) || objectRequest instanceof CSWCascadingType) {
                CSWCascadingType refreshCS = (CSWCascadingType) objectRequest;
                marshaller.marshal(cswConfigurer.refreshCascadedServers(refreshCS), sw);
                return Response.ok(sw.toString(), "text/xml").build();
            }
            
            if ("UpdateVocabularies".equalsIgnoreCase(request)) {    
                if (CSWFunctionEnabled) {
                    return Response.ok(cswConfigurer.updateVocabularies(),"text/xml").build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, "Request");
                }
            }
            
            if ("UpdateContacts".equalsIgnoreCase(request)) {    
                if (CSWFunctionEnabled) {
                    return Response.ok(cswConfigurer.updateContacts(),"text/xml").build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, "Request");
                }
            }
            
            
            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                                 OPERATION_NOT_SUPPORTED, "Request");
            
        
        } catch (CstlServiceException ex) {
            final String code = Util.transformCodeName(ex.getExceptionCode().name());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(),
                                                               ServiceDef.CONFIG.exceptionVersion.toString());
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                    !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                    !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                ex.printStackTrace();
            } else {
                LOGGER.info(ex.getMessage());
            }
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(Util.cleanSpecialCharacter(sw.toString()), "text/xml").build();
            
        } catch (InterruptedException ex) {
            return Response.ok("Interrupted Exception while getting the marshaller in treatIncommingRequest", "text/plain").build();

        } finally {
            if (marshaller != null) {
                marshallers.add(marshaller);
            }
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
    protected Response launchException(final String message, final String codeName, final String locator) throws JAXBException {
        Marshaller marshaller = null;
        try {
            marshaller = marshallers.take();

            final OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
            final ExceptionReport report = new ExceptionReport(message, code.name(), locator,
                                                           ServiceDef.CONFIG.exceptionVersion.toString());
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(sw.toString(), "text/xml").build();
            
        } catch (InterruptedException ex) {
            return Response.ok("Interrupted Exception while getting the marshaller in launchException", "text/plain").build();

        } finally {
            if (marshaller != null) {
                marshallers.add(marshaller);
            }
        }
    }

    /**
     * Restart all the web-services.
     * 
     * @return an Acknowlegement if the restart succeed.
     */
    private AcknowlegementType restartService() {
        LOGGER.info("\n restart requested \n");
        cn.reload();
        return new AcknowlegementType("success", "services succefully restarted");
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
        
        String service  = request.getService();
        String fileName = request.getFileName();
        Map<String, String> newProperties = request.getProperties();
        
        if ( service == null) {
            throw new CstlServiceException("You must specify the service parameter.",
                                              MISSING_PARAMETER_VALUE, "service");
        } else if (!serviceDirectory.keySet().contains(service)) {
            String msg = "Invalid value for the service parameter: " + service + '\n' +
                         "accepted values are:";
            for (String s: serviceDirectory.keySet()) {
                msg = msg + s + ',';
            }
            throw new CstlServiceException(msg, MISSING_PARAMETER_VALUE, "service");
            
        }
        
        if (fileName == null) {
             throw new CstlServiceException("You must specify the fileName parameter.", MISSING_PARAMETER_VALUE, "fileName");
        }
        
        if (newProperties == null || newProperties.size() == 0) {
             throw new CstlServiceException("You must specify a non empty properties parameter.", MISSING_PARAMETER_VALUE, 
                     "properties");
        }
        
        File configDir   = serviceDirectory.get(service);
        File propertiesFile = new File(configDir, fileName);
        
        Properties prop     = new Properties();
        if (propertiesFile.exists()) {
            for (String key : newProperties.keySet()) {
                prop.put(key, newProperties.get(key));
            }
        } else {
            throw new CstlServiceException("The file does not exist: " + propertiesFile.getPath(),
                                          NO_APPLICABLE_CODE);
        }
        try {
            Util.storeProperties(prop, propertiesFile);
        } catch (IOException ex) {
            throw new CstlServiceException("IOException xhile trying to store the properties files.",
                                          NO_APPLICABLE_CODE);
        }
        
        return new AcknowlegementType("success", "properties file sucessfully updated");
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
            String layer = getParameter("layer", false);
            System.out.println("LAYER= " + layer);
            // TODO: implement upload action here.
            in.close();
        } catch (CstlServiceException ex) {
            //must never happen in normal case
            LOGGER.severe("Webservice exception while get the layer parameter");
            return new AcknowlegementType("failed", "Webservice exception while get the layer parameter");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while uploading file");
            ex.printStackTrace();
            return new AcknowlegementType("failed", "IO exception while performing upload");
        }
        return new AcknowlegementType("success", "the file has been successfully uploaded");
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
        LOGGER.info("Shutting down the REST Configuration service facade.");
        if (cswConfigurer != null)
            cswConfigurer.destroy();
    }
}
