/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.coverage.wms;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import net.seagis.catalog.Database;
import javax.ws.rs.core.Response;
import javax.xml.bind.Unmarshaller;
import net.opengis.wms.WMSCapabilities;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import org.geotools.util.Version;

/**
 * WMS 1.3.0 web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @version
 * @author Guilhem Legal
 */
@UriTemplate("wms")
public class WMService {
    
    @HttpContext
    private UriInfo context;
    
    private Logger logger = Logger.getLogger("fr.geomatys.wms");
    
    private Marshaller marshaller;
    
    private Unmarshaller unmarshaller;
    
    private Version version = new Version("1.3.0");
    
    private WebServiceWorker webServiceWorker;
    
    /** 
     * these attributes will be removed. 
     */
    /**
     * The user directory where to store the configuration file on Unix platforms.
     */
    private static final String UNIX_DIRECTORY = ".sicade";

    /**
     * The user directory where to store the configuration file on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Sicade";

    /**
     * The file where to store configuration parameters.
     */
    private static final String CAPABILITIES_FILENAME = "WMSCapabilities.xml";
    
    
    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WMService() throws JAXBException, IOException, WebServiceException {
        
        //we build the JAXB marshaller and unmarshaller to bind java/xml
        JAXBContext jbcontext = JAXBContext.newInstance("net.opengis.ogc:net.opengis.wms");
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = jbcontext.createUnmarshaller();
        
        webServiceWorker = new WebServiceWorker(new Database());
        webServiceWorker.setService("WMS", "1.3.0");
    }
   
    
    /**
     * Treat the incomming request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @HttpMethod("GET")
    public Response treatGETrequest() throws JAXBException  {

        try {
            
                String request = (String) getParameter("REQUEST", 'M');
                if (request.equals("GetMap")) {
                    
                    return Response.Builder.representation(getMap(), webServiceWorker.getMimeType()).build();
                    
                } else if (request.equals("GetFeatureInfo")) {
                    
                    return getFeatureInfo();
                    
                } else if (request.equals("GetCapabilities")) {
                    
                    return Response.Builder.representation(getCapabilities(), "text/xml").build();
                    
                } else {
                    throw new WebServiceException("The operation " + request + " is not supported by the service",
                                                  WMSExceptionCode.OPERATION_NOT_SUPPORTED, version);
                }
        } catch (WebServiceException ex) {
            ex.printStackTrace();
            StringWriter sw = new StringWriter();    
            marshaller.marshal(ex.getServiceExceptionReport(), sw);
            return Response.Builder.representation(sw.toString(), "text/xml").build();
        }
    }

    /**
     * Verify the base parameter or each request
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private void verifyBaseParameter() throws WebServiceException {  
        if (!getParameter("VERSION", 'M').equals(version.toString())) {
            throw new WebServiceException("The parameter VERSION=" + version.toString() + "must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
        }
    } 
    /**
     * Extract The parameter named parameterName from the query.
     * If obligation is M (mandatory) and if the parameter is null it throw an exception.
     * else if obligation is O it return null.
     * This function made the parsing to the specified type.
     * 
     * @param parameterName The name of the parameter.
     * @param obligation can be M (Mandatory) or O (Optional) 
     * 
     * @return the parameter or null if not specified
     * @throw WebServiceException
     */
    private String getParameter(String parameterName, char obligation) throws WebServiceException {
        
        MultivaluedMap parameters = context.getQueryParameters();
        LinkedList<String> list = (LinkedList) parameters.get(parameterName);
        if (list == null) {
            if (obligation == 'O') {
                return null;
            } else {
                throw new WebServiceException("The parameter " + parameterName + " must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
            }
        } else {
            return list.get(0);
        } 
    }
   
    /**
     * Return a map for the specified parameters in the query.
     * 
     * @return
     * @throws fr.geomatys.wms.WebServiceException
     */
    private File getMap() throws  WebServiceException {
        logger.info("getMap request received");
        
        verifyBaseParameter();
        webServiceWorker.setFormat(getParameter("FORMAT", 'M'));
        webServiceWorker.setLayer(getParameter("LAYERS", 'M'));
        webServiceWorker.setCoordinateReferenceSystem(getParameter("CRS", 'M'));
        webServiceWorker.setBoundingBox(getParameter("BBOX", 'M'));
        webServiceWorker.setElevation(getParameter("ELEVATION", 'O'));
        webServiceWorker.setTime(getParameter("TIME", 'O'));
        webServiceWorker.setDimension(getParameter("WIDTH", 'M'), getParameter("HEIGHT", 'M'));

        //this parameters are not yet used
        String styles       = getParameter("STYLES", 'M');
        String transparent = getParameter("TRANSPARENT", 'O');
        
        String bgColor = getParameter("BGCOLOR", 'O');
        if (bgColor == null) 
            bgColor = "0xFFFFFF";
        
        return webServiceWorker.getImageFile();
    }
    
    private Response getFeatureInfo() throws WebServiceException {
        logger.info("getFeatureInfo request received");
        
        verifyBaseParameter();
        
        String query_layers = getParameter("QUERY_LAYERS", 'M');
        String info_format  = getParameter("INFO_FORMAT", 'M');
        
        String i = getParameter("I", 'M');
        String j = getParameter("J", 'M');
       
        //and then the optional attribute
        String feature_count = getParameter("FEATURE_COUNT", 'O');
        
        String exception = getParameter("EXCEPTIONS", 'O');
        if ( exception == null)
            exception = "XML";
        
        
        // plusieur type de retour possible
        String response = "";
        // si on retourne du html
        if (info_format.equals("text/html"))
            return Response.Builder.representation(response, "text/html").build();
        //si on retourne du xml
        else if (info_format.equals("text/xml"))
            return Response.Builder.representation(response, "text/xml").build();
        //si on retourne du gml
        else 
            return Response.Builder.representation(response, "application/vnd.ogc.gml").build();
    }
    
    private String getCapabilities() throws WebServiceException, JAXBException {
        logger.info("getCapabilities request received");
        
        // the service shall return WMSCapabilities marshalled
        WMSCapabilities response = (WMSCapabilities)unmarshaller.unmarshal(getCapabilitiesFile(false));
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", 'M').equals("WMS")) {
            throw new WebServiceException("The parameters SERVICE=WMS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
        }
        
        //and the the optional attribute
        String requestVersion = getParameter("VERSION", 'O');
        if (requestVersion != null && !requestVersion.equals(version.toString())) {
            throw new WebServiceException("The parameter VERSION must be 1.3.0",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, this.version);
        }
        
        String format = getParameter("FORMAT", 'O');
        
        response.getCapability().
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
        
    }
    
    /**
     * @todo this method is duplicate from the database class. it must be fix.
     *   
     * Returns the file where to read or write user configuration. If no such file is found,
     * then this method returns {@code null}. This method is allowed to create the destination
     * directory if and only if {@code create} is {@code true}.
     * <p>
     * Subclasses may override this method in order to search for an other file than the default one.
     *
     * @param  create {@code true} if this method is allowed to create the destination directory.
     * @return The configuration file, or {@code null} if none.
     */
    File getCapabilitiesFile(final boolean create) {
        if (CAPABILITIES_FILENAME == null) {
            return null;
        }
        /*
         * Donne priorité au fichier de configuration dans le répertoire courant, s'il existe.
         */
        File path = new File(CAPABILITIES_FILENAME);
        if (path.isFile()) {
            return path;
        }
        if (path.isDirectory()) {
            path = new File(path, CAPABILITIES_FILENAME);
        }
        /*
         * Recherche dans le répertoire de configuration de l'utilisateur,
         * en commançant par le répertoire de de GeoServer s'il est définit.
         */
        if (!path.isAbsolute()) {
            String home = System.getenv("GEOSERVER_DATA_DIR");
            if (home == null || !(path=new File(home)).isDirectory()) {
                home = System.getProperty("user.home");
                if (System.getProperty("os.name", "").startsWith("Windows")) {
                    path = new File(home, WINDOWS_DIRECTORY);
                } else {
                    path = new File(home, UNIX_DIRECTORY);
                }
            }
        }
        if (!path.exists()) {
            if (!create || !path.mkdir()) {
                return null;
            }
        }
        return new File(path, CAPABILITIES_FILENAME);
    }
   
}
