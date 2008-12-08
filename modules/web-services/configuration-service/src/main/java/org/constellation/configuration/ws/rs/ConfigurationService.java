/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// Jersey dependencies
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;

// JAXB dependencies
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.UpdatePropertiesFileType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.metadata.Utils;
import org.constellation.ows.OWSExceptionCode;
import org.constellation.ows.v110.ExceptionReport;
import org.constellation.ws.Service;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.WebService;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.geotools.factory.FactoryNotFoundException;
import static org.constellation.ows.OWSExceptionCode.*;

// geotools dependencies
import org.geotools.metadata.note.Anchors;
import org.geotools.factory.FactoryRegistry;

/**
 * A Web service dedicate to perform administration and configuration operations
 * 
 * @author Guilhem Legal
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
    
    public static final ServiceVersion version = new ServiceVersion(Service.OTHER, "1.0.0");
    
            
    public ConfigurationService() {
        super("Configuration");
        try {
            setXMLContext("org.constellation.ows.v110:org.constellation.configuration:org.constellation.skos", "");
            AbstractConfigurerFactory configurerfactory = factory.getServiceProvider(AbstractConfigurerFactory.class, null, null, null);
            cswConfigurer      = configurerfactory.getCSWConfigurer(cn);
            CSWFunctionEnabled = true;
        } catch (JAXBException ex) {
            LOGGER.severe("JAXBexception while setting the JAXB context for configuration service");
            ex.printStackTrace();
            CSWFunctionEnabled = false;
        } catch (ConfigurationException ex) {
            LOGGER.warning("Specific CSW operation will not be available." + '\n' + ex);
            CSWFunctionEnabled = false;
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning("Factory not foun for CSWConfigurer, specific CSW operation will not be available.");
            CSWFunctionEnabled = false;
        }
        LOGGER.info("Configuration service runing");
    }
    
    
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        try {
            String request  = "";
            StringWriter sw = new StringWriter();
            
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }
            
            if (request.equalsIgnoreCase("restart")) {
                
                marshaller.marshal(restartService(), sw);
                return Response.ok(sw.toString(), "text/xml").build();
                
            } else if (request.equalsIgnoreCase("UpdatePropertiesFile") || objectRequest instanceof UpdatePropertiesFileType) {
                
                UpdatePropertiesFileType updateProp = (UpdatePropertiesFileType) objectRequest;
                
                marshaller.marshal(updatePropertiesFile(updateProp), sw);
                return Response.ok(sw.toString(), "text/xml").build();
                
            } else if (request.equalsIgnoreCase("download")) {    
                File f = downloadFile();
                
                return Response.ok(f, MediaType.MULTIPART_FORM_DATA_TYPE).build(); 
            
            /**
             *  CSW specific operation
             */
            } else if (request.equalsIgnoreCase("refreshIndex")) {
            
                if (CSWFunctionEnabled) {
                    boolean asynchrone = Boolean.parseBoolean((String) getParameter("ASYNCHRONE", false));
                    String service     = getParameter("SERVICE", false);
                
                    marshaller.marshal(cswConfigurer.refreshIndex(asynchrone, service), sw);
                    return Response.ok(sw.toString(), "text/xml").build();
                } else {
                     throw new WebServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, version, "Request");
                }
            
            } else if (request.equalsIgnoreCase("refreshCascadedServers") || objectRequest instanceof CSWCascadingType) {
                
                CSWCascadingType refreshCS = (CSWCascadingType) objectRequest;
                
                marshaller.marshal(cswConfigurer.refreshCascadedServers(refreshCS), sw);
                return Response.ok(sw.toString(), "text/xml").build();
            
            } else if (request.equalsIgnoreCase("updateVocabularies")) {    
                                
                return Response.ok(cswConfigurer.updateVocabularies(),"text/xml").build(); 
            
            } else if (request.equalsIgnoreCase("updateContacts")) {    
                                
                return Response.ok(cswConfigurer.updateContacts(),"text/xml").build(); 
            
            } else {
                throw new WebServiceException("The operation " + request + " is not supported by the service",
                                                 OPERATION_NOT_SUPPORTED, version, "Request");
            }
        
        } catch (WebServiceException ex) {
            final String code = transformCodeName(ex.getExceptionCode().name());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(), ex.getVersion());
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                    !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                    !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                ex.printStackTrace();
            } else {
                LOGGER.info(ex.getMessage());
            }
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(cleanSpecialCharacter(sw.toString()), "text/xml").build();
        }
        
    }

    /**
     * build an service Exception and marshall it into a StringWriter
     *
     * @param message
     * @param codeName
     * @return
     */
    protected Object launchException(final String message, final String codeName, final String locator) {
        final OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
        final ExceptionReport report = new ExceptionReport(message, code.name(), locator, new ServiceVersion(Service.OTHER, "1.0"));
        return report;
    }

    /**
     * Restart all the web-services.
     * 
     * @return an Acknowlegement if the restart succeed.
     */
    private AcknowlegementType restartService() {
        LOGGER.info("\n restart requested \n");
        Anchors.clear();
        cn.reload();
        return new AcknowlegementType("success", "services succefully restarted");
    }
    
    /**
     * Update a properties file on the server file system.
     * 
     * @param request
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private AcknowlegementType updatePropertiesFile(UpdatePropertiesFileType request) throws WebServiceException {
        LOGGER.info("update properties file requested");
        
        String service  = request.getService();
        String fileName = request.getFileName();
        Map<String, String> newProperties = request.getProperties();
        
        if ( service == null) {
            throw new WebServiceException("You must specify the service parameter.",
                                              MISSING_PARAMETER_VALUE, version, "service");
        } else if (!serviceDirectory.keySet().contains(service)) {
            String msg = "Invalid value for the service parameter: " + service + '\n' +
                         "accepted values are:";
            for (String s: serviceDirectory.keySet()) {
                msg = msg + s + ',';
            }
            throw new WebServiceException(msg, MISSING_PARAMETER_VALUE, version, "service");
            
        }
        
        if (fileName == null) {
             throw new WebServiceException("You must specify the fileName parameter.", MISSING_PARAMETER_VALUE, version, "fileName");
        }
        
        if (newProperties == null || newProperties.size() == 0) {
             throw new WebServiceException("You must specify a non empty properties parameter.", MISSING_PARAMETER_VALUE, 
                     version, "properties");
        }
        
        File configDir   = serviceDirectory.get(service);
        File propertiesFile = new File(configDir, fileName);
        
        Properties prop     = new Properties();
        if (propertiesFile.exists()) {
            for (String key : newProperties.keySet()) {
                prop.put(key, newProperties.get(key));
            }
        } else {
            throw new WebServiceException("The file does not exist: " + propertiesFile.getPath(),
                                          NO_APPLICABLE_CODE, version);
        }
        try {
            Utils.storeProperties(prop, propertiesFile);
        } catch (IOException ex) {
            throw new WebServiceException("IOException xhile trying to store the properties files.",
                                          NO_APPLICABLE_CODE, version);
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
        } catch (WebServiceException ex) {
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
    private File downloadFile() throws WebServiceException {
        throw new WebServiceException("Not implemented", NO_APPLICABLE_CODE, version);
    }

    /**
     * Free the resource and close the connection at undeploying time.
     */
    @PreDestroy
    public void destroy() {
        LOGGER.info("destroying Configuration Service");
        if (cswConfigurer != null)
            cswConfigurer.destroy();
    }
}
