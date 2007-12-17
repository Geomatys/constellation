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
import java.io.StringReader;
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
import net.seagis.sld.DescribeLayerResponseType;
import net.seagis.sld.LayerDescriptionType;
import net.seagis.sld.StyledLayerDescriptor;
import net.seagis.wms.Layer;
import net.seagis.wms.WMSCapabilities;
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
    
    /**
     * A JAXB marshaller used to transform the java object in XML String.
     */
    private Marshaller marshaller;
    
    /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    private Unmarshaller unmarshaller;
    
    /**
     * The version of the WMS web service. fixed a 1.3.0 for now.
     */
    private Version version = new Version("1.3.0");
    
    /**
     * The version of the SLD profile for the WMS web service. fixed a 1.1.0 for now.
     */
    private Version sldVersion = new Version("1.1.0");
    
    
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
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.ogc:net.seagis.wms:net.seagis.sld");
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
        unmarshaller = jbcontext.createUnmarshaller();
        
        webServiceWorker = new WebServiceWorker(new Database());
        webServiceWorker.setService("WMS", version.toString());
    }
   
    
    /**
     * Treat the incomming GET request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @HttpMethod("GET")
    public Response treatGETrequest() throws JAXBException  {

        try {
            
                String request = (String) getParameter("REQUEST", true);
                if (request.equals("GetMap")) {
                    
                    return Response.Builder.representation(getMap(), webServiceWorker.getMimeType()).build();
                    
                } else if (request.equals("GetFeatureInfo")) {
                    
                    return getFeatureInfo();
                    
                } else if (request.equals("GetCapabilities")) {
                    
                    return Response.Builder.representation(getCapabilities(), "text/xml").build();
                    
                } else if (request.equals("DescribeLayer")) {
                    
                    return Response.Builder.representation(describeLayer(), "text/xml").build();
                    
                } else if (request.equals("GetLegendGraphic")) {
                    
                    return Response.Builder.representation(getLegendGraphic(), webServiceWorker.getMimeType()).build();
                    
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
     * Verify the base parameter or each request.
     * 
     * @param sld if true the operation versify as well the version of the sld.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private void verifyBaseParameter(boolean sld) throws WebServiceException {  
        if (!getParameter("VERSION", true).equals(version.toString())) {
            throw new WebServiceException("The parameter VERSION=" + version.toString() + "must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
        }
        if (sld) {
            if (!getParameter("SLD_VERSION", true).equals(version.toString())) {
                throw new WebServiceException("The parameter VERSION=" + version.toString() + "must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
            }
        }
    } 
   
    /**
     * Extract The parameter named parameterName from the query.
     * If the parameter is mandatory and if it is null it throw an exception.
     * else it return null.
     * 
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional. 
     * 
     * @return the parameter or null if not specified
     * @throw WebServiceException
     */
    private String getParameter(String parameterName, boolean mandatory) throws WebServiceException {
        
        MultivaluedMap parameters = context.getQueryParameters();
        //we try with the parameter in Upper case.
        LinkedList<String> list = (LinkedList) parameters.get(parameterName);
        if (list == null) {
            //else with the parameter in lower case.
            list = (LinkedList) parameters.get(parameterName.toLowerCase());
            if (list == null) {
                //and finally with the first character in uppercase
                String s = parameterName.toLowerCase();
                s = s.substring(1);
                s = parameterName.charAt(0) + s;
                list = (LinkedList) parameters.get(s);
                if (list == null) {
                    if (!mandatory) {
                        return null;
                    } else {
                        throw new WebServiceException("The parameter " + parameterName + " must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
                    }
                }
            } 
        } 
        
        return list.get(0);
    }
    
    /**
     * Extract The complex parameter encoded in XML from the query.
     * If the parameter is mandatory and if it is null it throw an exception.
     * else it return null.
     * 
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional. 
     * 
     * @return the parameter or null if not specified
     * @throw WebServiceException
     */
    private Object getComplexParameter(String parameterName, boolean mandatory) throws WebServiceException, JAXBException {
        
        MultivaluedMap parameters = context.getQueryParameters();
        LinkedList<String> list = (LinkedList) parameters.get(parameterName);
        if (list == null) {
            list = (LinkedList) parameters.get(parameterName.toLowerCase());
            if (list == null) {
                if (!mandatory) {
                    return null;
                } else {
                    throw new WebServiceException("The parameter " + parameterName + " must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
                }
            } 
        }
        StringReader sr = new StringReader(list.get(0));
                
        return unmarshaller.unmarshal(sr);
    }
   
    /**
     * Return a map for the specified parameters in the query.
     * 
     * @return
     * @throws fr.geomatys.wms.WebServiceException
     */
    private File getMap() throws  WebServiceException {
        logger.info("getMap request received");
        
        verifyBaseParameter(false);
        
        //we set the attribute od the webservice worker with the parameters.
        webServiceWorker.setFormat(getParameter("FORMAT", true));
        webServiceWorker.setLayer(getParameter("LAYERS", true));
        webServiceWorker.setCoordinateReferenceSystem(getParameter("CRS", true));
        webServiceWorker.setBoundingBox(getParameter("BBOX", true));
        webServiceWorker.setElevation(getParameter("ELEVATION", false));
        webServiceWorker.setTime(getParameter("TIME", false));
        webServiceWorker.setDimension(getParameter("WIDTH", true), getParameter("HEIGHT", true));

        //this parameters are not yet used
        String styles      = getParameter("STYLES", true);
        String transparent = getParameter("TRANSPARENT", false);
        
        String bgColor = getParameter("BGCOLOR", false);
        if (bgColor == null) 
            bgColor = "0xFFFFFF";
        
        //extended parameter of the specification SLD
        String sld           = getParameter("SLD", false);
        String remoteOwsType = getParameter("REMOTE_OWS_TYPE", false);
        String remoteOwsUrl  = getParameter("REMOTE_OWS_URL", false);
        
        return webServiceWorker.getImageFile();
    }
    
    /**
     * 
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private Response getFeatureInfo() throws WebServiceException {
        logger.info("getFeatureInfo request received");
        
        verifyBaseParameter(false);
        
        String query_layers = getParameter("QUERY_LAYERS", true);
        String info_format  = getParameter("INFO_FORMAT", true);
        
        String i = getParameter("I", true);
        String j = getParameter("J", true);
       
        //and then the optional attribute
        String feature_count = getParameter("FEATURE_COUNT", false);
        
        String exception = getParameter("EXCEPTIONS", false);
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
    
    /**
     * Describe the capabilities and the layers available of this service.
     * 
     * @return a WMSCapabilities XML document describing the capabilities of the service.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private String getCapabilities() throws WebServiceException, JAXBException {
        logger.info("getCapabilities request received");
        
        // the service shall return WMSCapabilities marshalled
        WMSCapabilities response = (WMSCapabilities)unmarshaller.unmarshal(getCapabilitiesFile(false));
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", true).equals("WMS")) {
            throw new WebServiceException("The parameters SERVICE=WMS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
        }
        
        //and the the optional attribute
        String requestVersion = getParameter("VERSION", false);
        if (requestVersion != null && !requestVersion.equals(version.toString())) {
            throw new WebServiceException("The parameter VERSION must be " + version.toString(),
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, this.version);
        }
        
        String format = getParameter("FORMAT", false);
        
        Layer layer = null;
        
        response.getCapability().setLayer(layer);
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
        
    }
    
    
    
    /**
     * 
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private String describeLayer() throws WebServiceException, JAXBException {
        
        verifyBaseParameter(true);
        
        String layers = getParameter("LAYERS", true);
        
        LayerDescriptionType layersDescription = new LayerDescriptionType(null);         
        DescribeLayerResponseType response = new DescribeLayerResponseType(sldVersion.toString(), layersDescription);
       
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
    }
    
    
    private File getLegendGraphic() throws WebServiceException, JAXBException {
        
        verifyBaseParameter(true);
        
        String layer = getParameter("LAYER", true);
        String style = getParameter("STYLE", false);
       
        String remoteSld     = getParameter("SLD", false);
        String remoteOwsType = getParameter("REMOTE_OWS_TYPE", false);
        String remoteOwsUrl  = getParameter("REMOTE_OWS_URL", false);
        String coverage      = getParameter("COVERAGE", false);
        String rule          = getParameter("RULE", false);
        String scale         = getParameter("SCALE", false);
        
        StyledLayerDescriptor sld = (StyledLayerDescriptor) getComplexParameter("SLD_BODY", false);
        
        return new File("null");
        
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
