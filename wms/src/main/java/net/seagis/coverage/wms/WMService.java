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

import com.sun.ws.rest.spi.resource.Singleton;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import javax.ws.rs.core.Response;
import javax.xml.bind.Unmarshaller;
import net.seagis.sld.DescribeLayerResponseType;
import net.seagis.sld.LayerDescriptionType;
import net.seagis.sld.StyledLayerDescriptor;
import net.seagis.wms.Layer;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.wms.AbstractWMSCapabilities;
import net.seagis.wms.BoundingBox;
import net.seagis.wms.Dimension;
import net.seagis.wms.EXGeographicBoundingBox;
import net.seagis.wms.LegendURL;
import net.seagis.wms.OnlineResource;
import net.seagis.wms.Style;
import org.geotools.util.NumberRange;
import org.geotools.util.Version;
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 * WMS 1.3.0 web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @version
 * @author Guilhem Legal
 */
@UriTemplate("wms")
@Singleton
public class WMService {
    
    @HttpContext
    private UriInfo context;
    
    private final Logger logger = Logger.getLogger("net.seagis.wms");
    
    /**
     * A JAXB marshaller used to transform the java object in XML String.
     */
    private final Marshaller marshaller;
    
    /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    private final Unmarshaller unmarshaller;
    
    /**
     * The version of the WMS web service. fixed a 1.3.0 for now.
     */
    private final List<Version> versions = new ArrayList<Version>();
    
    /**
     * The current version used (since the last request)
     */
    private Version currentVersion;
    
    /**
     * The version of the SLD profile for the WMS web service. fixed a 1.1.0 for now.
     */
    private final Version sldVersion = new Version("1.1.0");
    
    /**
     * The object whitch made all the operation on the postgrid database
     */
    private final WebServiceWorker webServiceWorker;
    
    /**
     * 
     */
    private final String serviceURL;
            
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
    private static final String CAPABILITIES_FILENAME_1_3_0 = "WMSCapabilities1.3.0.xml";
    
    /**
     * The file where to store configuration parameters.
     */
    private static final String CAPABILITIES_FILENAME_1_1_1 = "WMSCapabilities1.1.1.xml";
    
    
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
        
        versions.add(new Version("1.3.0"));
        versions.add(new Version("1.1.1"));
        currentVersion = versions.get(0);
        
        webServiceWorker = new WebServiceWorker(new Database());
        webServiceWorker.setService("WMS", versions.get(0).toString());
        serviceURL = "http://sensor.geomatys.fr/wms-1.0-SNAPSHOT/wms?";
        
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
                                                  WMSExceptionCode.OPERATION_NOT_SUPPORTED, versions.get(0));
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
     * @param sld case 0: no sld.
     *            case 1: VERSION parameter for WMS version and SLD_VERSION for sld version.
     *            case 2: VERSION parameter for sld version.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private void verifyBaseParameter(int sld) throws WebServiceException {  
        if (sld == 2) {
            if (!getParameter("VERSION", true).equals(sldVersion.toString())) {
                throw new WebServiceException("The parameter VERSION=" + sldVersion + "must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, sldVersion);
            } else {
                return;
            }
        }
        if (!(getParameter("VERSION", true).equals(versions.get(0).toString()) || getParameter("VERSION", true).equals(versions.get(1).toString()) )) {
            String message = "The parameter ";
            for (Version vers:versions){
                message += "VERSION=" + vers + "OR ";
            }
            message = message.substring(0, message.length()-3);
            message += "must be specify";
            throw new WebServiceException(message,
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, versions.get(0));
        
        } else if (getParameter("VERSION", true).equals(versions.get(0).toString())){
            currentVersion = versions.get(0);
        } else if (getParameter("VERSION", true).equals(versions.get(1).toString())){
            currentVersion = versions.get(1);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throw new WebServiceException("The parameter SLD_VERSION=" + sldVersion + "must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, versions.get(0));
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
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, currentVersion);
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
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE, currentVersion);
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
        
        verifyBaseParameter(0);
        
        //we set the attribute od the webservice worker with the parameters.
        webServiceWorker.setFormat(getParameter("FORMAT", true));
        webServiceWorker.setLayer(getParameter("LAYERS", true));
        webServiceWorker.setDimensionRange(getParameter("DIM_RANGE", false));
        
        String crs;
        if (currentVersion.equals(versions.get(0))){
            crs = getParameter("CRS", true);
        } else {
            crs = getParameter("SRS", true);
        }
        webServiceWorker.setCoordinateReferenceSystem(crs);
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
        
        verifyBaseParameter(0);
        
        webServiceWorker.setLayer(getParameter("QUERY_LAYERS", true));
        
        String crs;
        if (currentVersion.equals(versions.get(0))){
            crs = getParameter("CRS", true);
        } else {
            crs = getParameter("SRS", true);
        }
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(getParameter("BBOX", true));
        webServiceWorker.setElevation(getParameter("ELEVATION", false));
        webServiceWorker.setTime(getParameter("TIME", false));
        webServiceWorker.setDimension(getParameter("WIDTH", true), getParameter("HEIGHT", true));
        

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
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", true).equalsIgnoreCase("WMS")) {
            throw new WebServiceException("The parameters SERVICE=WMS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, currentVersion);
        }
        
        //and the the optional attribute
        String inputVersion = getParameter("VERSION", false);
        if(inputVersion != null) {
            if (!(inputVersion.equals(versions.get(0).toString()) || inputVersion.equals(versions.get(1).toString())) 
                    || inputVersion.equals(versions.get(0).toString())) {
                currentVersion = versions.get(0);
        
            } else if (inputVersion.equals(versions.get(1).toString())){
                currentVersion = versions.get(1);
            }
        } else {
            currentVersion = versions.get(0);
        } 
        
        // the service shall return WMSCapabilities marshalled
        AbstractWMSCapabilities response = (AbstractWMSCapabilities)unmarshaller.unmarshal(getCapabilitiesFile(false, currentVersion));
        
        String format = getParameter("FORMAT", false);
        
        //we build the layers object of the document
        
        //we get the list of layers
         List<Layer> layers = new ArrayList<Layer>();
        for (net.seagis.coverage.catalog.Layer inputLayer: webServiceWorker.getLayers()) {
            try {
                
                List<String> crs = new ArrayList<String>();
                
                Integer code = 4326;//CRS.lookupEpsgCode(inputLayer.getCoordinateReferenceSystem(), false);
                if(code != null)
                    crs.add(code.toString());
                
                GeographicBoundingBox inputGeoBox = inputLayer.getGeographicBoundingBox();
                Rectangle inputBox                = inputLayer.getBounds();
                BoundingBox outputBBox = null;
                if(inputBox != null) {
                    outputBBox = new BoundingBox(code.toString(), 
                                                 inputBox.x + 0.0,
                                                 inputBox.y + 0.0,
                                                 inputBox.x + inputBox.width + 0.0,
                                                 inputBox.y + inputBox.height + 0.0,
                                                 0.0, 0.0,
                                                 currentVersion);
                }
                //we add the list od available date and elevation
                List<Dimension> dimensions = new ArrayList<Dimension>();
                
                
                //the available date
                String defaut = null;
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Dimension dim;
                String value = "";
                SortedSet<Date> dates = inputLayer.getAvailableTimes();
                if (dates.size() > 0) {
                    defaut = df.format(dates.last());
                
                
                    dim = new Dimension("time", "ISO8601", defaut, null);
                    for (Date d:dates){
                        value += df.format(d) + ','; 
                    }
                    dim.setValue(value);
                    dimensions.add(dim);
                }
                
                //the available elevation
                defaut = null;
                SortedSet<Number> elevations = inputLayer.getAvailableElevations();
                if (elevations.size() > 0) {
                    defaut = elevations.first().toString();
                
                    dim = new Dimension("elevation", "EPSG:5030", defaut, null);
                    value = "";
                    for (Number n:elevations){
                        value += n.toString() + ','; 
                    }
                    dim.setValue(value);
                    dimensions.add(dim);
                }
                
                //the dimension range
                defaut = null;
                NumberRange[] ranges = inputLayer.getSampleValueRanges();
                if (ranges!= null && ranges.length>0 && ranges[0]!= null) {
                    defaut = ranges[0].getMinimum() + "," + ranges[0].getMaximum();
                
                    dim = new Dimension("dim_range", "degrees", defaut, ranges[0].getMinimum() + "," + ranges[0].getMaximum());
                    dimensions.add(dim);
                }
                
                // we build a Style Object
                OnlineResource or = new OnlineResource(this.serviceURL + "REQUEST=GetLegendGraphic&VERSION=1.3.0&FORMAT=image/png&LAYER=" + inputLayer.getName());
                LegendURL legendURL1 = new LegendURL("image/png", or);

                or = new OnlineResource(this.serviceURL + "REQUEST=GetLegendGraphic&VERSION=1.3.0&FORMAT=image/gif&LAYER=" + inputLayer.getName());
                LegendURL legendURL2 = new LegendURL("image/gif", or);
                Style style = new Style("Style1", "default Style", null, null, null,legendURL1,legendURL2);
                
                //we build and add a layer 
                Layer outputLayer = new Layer(inputLayer.getName(), 
                                              inputLayer.getRemarks(),
                                              inputLayer.getThematic(), 
                                              crs, 
                                              new EXGeographicBoundingBox(inputGeoBox.getWestBoundLongitude(), 
                                                                          inputGeoBox.getEastBoundLongitude(), 
                                                                          inputGeoBox.getSouthBoundLatitude(), 
                                                                          inputGeoBox.getNorthBoundLatitude()), 
                                              outputBBox,  
                                              true,
                                              dimensions,
                                              style,
                                              currentVersion);
                layers.add(outputLayer);
                
            } catch (CatalogException exception) {
                throw new WebServiceException(exception, WMSExceptionCode.NO_APPLICABLE_CODE, currentVersion);
            }
        }
       
        
        //we build the list of accepted crs
        List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");crs.add("EPSG:3395");crs.add("EPSG:27574");
        
        //we build a general boundingbox
        EXGeographicBoundingBox exGeographicBoundingBox = null;
        //we build the general layer and add it to the document
        Layer layer = new Layer("Seagis Web Map Layer", 
                                "description of the service(need to be fill)", 
                                crs, 
                                exGeographicBoundingBox, 
                                layers,
                                currentVersion);
        
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
        
        verifyBaseParameter(2);
        
        String layers = getParameter("LAYERS", true);
        
        LayerDescriptionType layersDescription = new LayerDescriptionType(null);         
        DescribeLayerResponseType response = new DescribeLayerResponseType(sldVersion.toString(), layersDescription);
       
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
    }
    
    
    private File getLegendGraphic() throws WebServiceException, JAXBException {
        
        verifyBaseParameter(2);
        webServiceWorker.setLayer(getParameter("LAYER", true));
        webServiceWorker.setFormat(getParameter("FORMAT", false));
        webServiceWorker.setDimension(getParameter("WIDTH", false), getParameter("HEIGHT", false));

        
        String style = getParameter("STYLE", false);
       
        String featureType   = getParameter("FEATURETYPE", false);
        String remoteSld     = getParameter("SLD", false);
        String remoteOwsType = getParameter("REMOTE_OWS_TYPE", false);
        String remoteOwsUrl  = getParameter("REMOTE_OWS_URL", false);
        String coverage      = getParameter("COVERAGE", false);
        String rule          = getParameter("RULE", false);
        String scale         = getParameter("SCALE", false);
        
        StyledLayerDescriptor sld = (StyledLayerDescriptor) getComplexParameter("SLD_BODY", false);
        
        return  webServiceWorker.getLegendFile();
        
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
    File getCapabilitiesFile(final boolean create, Version version) {
        String fileName = null;
        if (version.toString().equals("1.1.1")){
            fileName = CAPABILITIES_FILENAME_1_1_1;
        } else {
            fileName = CAPABILITIES_FILENAME_1_3_0;
        }
            
        if (fileName == null) {
            return null;
        }
        /*
         * Donne priorité au fichier de configuration dans le répertoire courant, s'il existe.
         */
        File path = new File(fileName);
        if (path.isFile()) {
            return path;
        }
        if (path.isDirectory()) {
            path = new File(path, fileName);
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
        return new File(path, fileName);
    }
   
}
