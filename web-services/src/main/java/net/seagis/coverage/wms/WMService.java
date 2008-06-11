/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.units.Unit;
import java.util.Set;
import java.util.StringTokenizer;

// jersey dependencies
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.sun.ws.rest.spi.resource.Singleton;

// JAXB xml binding dependencies
import java.io.IOException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

//seagis dependencies
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.coverage.web.Service;
import net.seagis.coverage.web.WMSWebServiceException;
import net.seagis.sld.v110.DescribeLayerResponseType;
import net.seagis.sld.v110.LayerDescriptionType;
import net.seagis.sld.v110.StyledLayerDescriptor;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.gml.v311.DirectPositionType;
import net.seagis.gml.v311.PointType;
import net.seagis.se.OnlineResourceType;
import net.seagis.sld.v110.TypeNameType;
import net.seagis.util.PeriodUtilities;
import net.seagis.wms.AbstractWMSCapabilities;
import net.seagis.wms.AbstractDCP;
import net.seagis.wms.AbstractDimension;
import net.seagis.wms.AbstractLayer;
import net.seagis.wms.AbstractRequest;
import net.seagis.wms.AbstractOperation;
import net.seagis.wms.AbstractProtocol;
import net.seagis.wms.v111.LatLonBoundingBox;
import net.seagis.wms.v130.OperationType;
import net.seagis.wms.v130.EXGeographicBoundingBox;
import static net.seagis.coverage.wms.WMSExceptionCode.*;

import net.seagis.coverage.metadata.LayerMetadata;
import net.seagis.coverage.metadata.SeriesMetadata;
import net.seagis.coverage.metadata.LayerMetadataTable;
import net.seagis.coverage.metadata.PointOfContact;
import net.seagis.coverage.metadata.PointOfContactTable;
import net.seagis.coverage.metadata.SeriesMetadataTable;

//geotools dependencies
import org.geotools.util.MeasurementRange;
import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 * WMS 1.3.0 / 1.1.1 
 * web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @version
 * @author Guilhem Legal
 */
@Path("wms")
@Singleton
public class WMService extends WebService {
    
    /**
     * A list of layer initialized a begining;
     */
    private Set<net.seagis.coverage.catalog.Layer> layerList;
    /**
     * The object whitch made all the operation on the postgrid database
     */
    private static ThreadLocal<WebServiceWorker> webServiceWorker;
    static {
        try {
           /* only for ifremer configuration */
            File configFile = null;
            File dirCatalina = null;
            if(System.getenv().get("CATALINA_HOME") != null)
                dirCatalina = new File(System.getenv().get("CATALINA_HOME"));
            
            if (dirCatalina!= null && dirCatalina.exists()) {
                configFile = new File(dirCatalina, "webapps/ifremerWS/WEB-INF/config.xml");
                logger.info("path to config file:" + configFile);
                if (!configFile.exists()) {
                    configFile = null;
                }
            }
            final WebServiceWorker initialValue;
            if (configFile != null) {
                logger.info("path to config file:" + configFile.getAbsolutePath());
               initialValue = new WebServiceWorker(new Database(configFile), true); 
            } else {
                logger.info("path to catalina config file using sicade configuration");
                initialValue = new WebServiceWorker(new Database(), true);
            }
            
            webServiceWorker = new ThreadLocal<WebServiceWorker>() {
                @Override
                protected WebServiceWorker initialValue() {
                    return new WebServiceWorker(initialValue);
                }
            };
            
       }catch (IOException e) {
            logger.severe("IOException a l'initialisation du webServiceWorker:" + e);
       }
       
    }
    
    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WMService() throws JAXBException, WebServiceException, FileNotFoundException, IOException {
        super("WMS", new ServiceVersion(Service.WMS, "1.3.0") ,new ServiceVersion(Service.WMS, "1.1.1"));

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("net.seagis.coverage.web:net.seagis.wms.v111:net.seagis.wms.v130:net.seagis.sld.v110:net.seagis.gml.v311",
                      "http://www.opengis.net/wms");
                
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        logger.info("Loading layers please wait...");
        layerList = webServiceWorker.getLayers();
        logger.info("WMS service running");
    }
   
    
    /**
     * Treat the incomming request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @Override
    public Response treatIncommingRequest(Object objectRequest) throws JAXBException {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        try {
            String request = (String) getParameter("REQUEST", true);
            logger.info("new request:" + request);
            writeParameters();
             
            if (request.equalsIgnoreCase("GetMap")) {
                    
                return Response.ok(getMap(), webServiceWorker.getMimeType()).build();
                    
            } else if (request.equals("GetFeatureInfo")) {
                    
                return getFeatureInfo();
                    
            } else if (request.equalsIgnoreCase("GetCapabilities")) {
                    
                return getCapabilities();
                        
            } else if (request.equalsIgnoreCase("DescribeLayer")) {
                    
                return Response.ok(describeLayer(), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetLegendGraphic")) {
                    
                return Response.ok(getLegendGraphic(), webServiceWorker.getMimeType()).build();
                    
            } else {
                throw new WMSWebServiceException("The operation " + request + " is not supported by the service",
                                              OPERATION_NOT_SUPPORTED, getCurrentVersion());
            }
        } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (ex instanceof WMSWebServiceException) {
                WMSWebServiceException wmsex = (WMSWebServiceException)ex;
                if (!wmsex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                    !wmsex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)) {
                    wmsex.printStackTrace();
                }
                StringWriter sw = new StringWriter();    
                marshaller.marshal(wmsex.getServiceExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()), webServiceWorker.getExceptionFormat()).build();
            } else {
                throw new IllegalArgumentException("this service can't return OWS Exception");
            }
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
        verifyBaseParameter(0);
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we set the attribute od the webservice worker with the parameters.
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        webServiceWorker.setFormat(getParameter("FORMAT", true));
        webServiceWorker.setLayer(getParameter("LAYERS", true));
        webServiceWorker.setColormapRange(getParameter("DIM_RANGE", false));
        
        String crs;
        if (getCurrentVersion().toString().equals("1.3.0")) {
            crs = getParameter("CRS", true);
        } else {
            crs = getParameter("SRS", true);
        }
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(getParameter("BBOX", true));
        webServiceWorker.setElevation(getParameter("ELEVATION", false));
        webServiceWorker.setTime(getParameter("TIME", false));
        webServiceWorker.setDimension(getParameter("WIDTH", true), getParameter("HEIGHT", true), null);
        webServiceWorker.setBackgroundColor(getParameter("BGCOLOR", false));
        webServiceWorker.setTransparency(getParameter("TRANSPARENT", false));
        
        
        //this parameters are not yet used
        String styles        = getParameter("STYLES", true);
        
        //extended parameter of the specification SLD
        String sld           = getParameter("SLD", false);
        String remoteOwsType = getParameter("REMOTE_OWS_TYPE", false);
        String remoteOwsUrl  = getParameter("REMOTE_OWS_URL", false);
        
        return webServiceWorker.getImageFile();
    }
    
    /**
     * Return the value of a point in a map.
     *  
     * @return text, HTML , XML or GML code.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private Response getFeatureInfo() throws WebServiceException, JAXBException {
        logger.info("getFeatureInfo request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(0);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        String layer = getParameter("QUERY_LAYERS", true);
        webServiceWorker.setLayer(layer);
        
        String crs;
        if (getCurrentVersion().toString().equals("1.3.0")){
            crs = getParameter("CRS", true);
        } else {
            crs = getParameter("SRS", true);
        }
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(getParameter("BBOX", true));
        webServiceWorker.setElevation(getParameter("ELEVATION", false));
        webServiceWorker.setTime(getParameter("TIME", false));
        webServiceWorker.setDimension(getParameter("WIDTH", true), getParameter("HEIGHT", true), null);
        

        final String i, j;
        if (getCurrentVersion().toString().equals("1.3.0")) {
            i = getParameter("I", true);
            j = getParameter("J", true);
        } else {
            i = getParameter("X", true);
            j = getParameter("Y", true);
        }
        
        String infoFormat  = getParameter("INFO_FORMAT", false); // TODO true);
        if (infoFormat != null) {
            if(!(infoFormat.equals("text/plain") 
              || infoFormat.equals("text/html") 
              || infoFormat.equals("application/vnd.ogc.gml") 
              || infoFormat.equals("text/xml"))){
                
                throw new WMSWebServiceException("This MIME type " + infoFormat + " is not accepted by the service",
                                              INVALID_PARAMETER_VALUE, getCurrentVersion());
            }
        } else {
            infoFormat = "text/plain";
        }
        String feature_count = getParameter("FEATURE_COUNT", false);
        
        webServiceWorker.setExceptionFormat(getParameter("EXCEPTIONS", false));
        
        double result = webServiceWorker.evaluatePixel(i,j);
 
        
        // there is many return type possible
        String response;
        
        // if we return html
        if (infoFormat.equals("text/html")) {
            response = "<html>"                                       +
                       "    <head>"                                   +
                       "        <title>GetFeatureInfo output</title>" +
                       "    </head>"                                  +
                       "    <body>"                                   +
                       "    <table>"                                  +
                       "        <tr>"                                 +
                       "            <th>" + layer + "</th>"           +
                       "        </tr>"                                +
                       "        <tr>"                                 +
                       "            <th>" + result + "</th>"          +
                       "        </tr>"                                +
                       "    </table>"                                 +
                       "    </body>"                                  +
                       "</html>";
        }
        //if we return xml or gml
        else if (infoFormat.equals("text/xml") || infoFormat.equals("application/vnd.ogc.gml")) {
            DirectPosition inputCoordinate = webServiceWorker.getCoordinates();
            List<Double> coord = new ArrayList<Double>();
            for (Double d:inputCoordinate.getCoordinates()) {
                coord.add(d);
            }
            coord.add(result);
            List<String> axisLabels = new ArrayList<String>();
            axisLabels.add("X");
            axisLabels.add("Y");
            axisLabels.add("RESULT");
            DirectPositionType pos = new DirectPositionType(crs, 3, axisLabels, coord);
            PointType pt = new PointType(layer, pos);
            
            //we marshall the response and return the XML String
            StringWriter sw = new StringWriter();    
            marshaller.marshal(pt, sw);
            response = sw.toString();
        }
        
        // HTML Response with all metadata for the 
        // TODO: This is only temporary! Get metadata via CSW request instead.
        Boolean getMetadata = false;
        String getMetadataParam = getParameter("GetMetadata",false);
        if (getMetadataParam != null && getMetadataParam.equalsIgnoreCase("TRUE")) {
                getMetadata = true;
        }
        if (infoFormat.equals("text/html") && getMetadata == true )  {
            try {
                CoverageReference coverageRef = webServiceWorker.getCoverageReference();
                String filename = coverageRef.getFile().getAbsolutePath();
                
                // TODO:  move all of this to CoverageReference so we just need:
                // return coverageRef.getMetadataAsHTML();

                final LayerMetadataTable layerMetaTable = new LayerMetadataTable(webServiceWorker.getDatabase());
                final LayerMetadata layerMetaEntry = layerMetaTable.getEntry(layer);
                
                final SeriesMetadataTable seriesMetaTable = new SeriesMetadataTable(webServiceWorker.getDatabase());
                final SeriesMetadata seriesMetaEntry = seriesMetaTable.getEntry(coverageRef.getSeries().toString());
                                
                final PointOfContactTable pocTable = new PointOfContactTable(webServiceWorker.getDatabase());
                final PointOfContact pocEntry = pocTable.getEntry(seriesMetaEntry.getPointOfContactID());
                
                response =  "<table border=1>" +
                            "<tr><td>URI:</td><td>" + filename + "</td></tr>" +
                            "<tr><td>Time Period:</td><td>" + coverageRef.getTimeRange().toString() + "</td></tr>" +
                            "</table>" +
                            layerMetaEntry.getMetadata() + seriesMetaEntry.getMetadata() + pocEntry.getMetadata();
                
            } catch (CatalogException ex) {
                Logger.getLogger(WMService.class.getName()).log(Level.SEVERE, null, ex);
                response = "Could not find any matching records.";
            } catch (SQLException ex) {
                Logger.getLogger(WMService.class.getName()).log(Level.SEVERE, null, ex);
                response = "SQL error.";
            }
        }
        
        //if we return text
        else {
            response = "result for " + layer + " is:" + result;
        }
        return Response.ok(response, infoFormat).build();
    }
    
    /**
     * Describe the capabilities and the layers available of this service.
     * 
     * @return a WMSCapabilities XML document describing the capabilities of the service.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private Response getCapabilities() throws WebServiceException, JAXBException {
        logger.info("getCapabilities request processing" + '\n');
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", true).equalsIgnoreCase("WMS")) {
            throw new WMSWebServiceException("The parameters SERVICE=WMS must be specified",
                                         MISSING_PARAMETER_VALUE, getCurrentVersion());
        }
        
        //and the the optional attribute
        String inputVersion = getParameter("VERSION", false);
        if(inputVersion != null && inputVersion.equals("1.3.0")) {
            setCurrentVersion("1.3.0");
        } else {
            setCurrentVersion("1.1.1");
        } 
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        String format = getParameter("FORMAT", false);
        if (format == null ) {
            format = "application/vnd.ogc.wms_xml";
        } else if (!(format.equals("text/xml") || format.equals("application/vnd.ogc.wms_xml"))) {
            throw new WMSWebServiceException("Allowed format for GetCapabilities are : text/xml or application/vnd.ogc.wms_xml.",
                      INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
        
        AbstractWMSCapabilities response;
        String updateSequence = getParameter("UPDATESEQUENCE", false);
                
        // the service shall return WMSCapabilities marshalled
        try {
            response = (AbstractWMSCapabilities)getCapabilitiesObject();
        } catch(IOException e)   {
            throw new WMSWebServiceException("IO exception while getting Services Metadata:" + e.getMessage(),
                      INVALID_PARAMETER_VALUE, getCurrentVersion());
            
        } 
        //we update the url in the static part.
        response.getService().getOnlineResource().setHref(getServiceURL() + "wms");
        AbstractRequest request = response.getCapability().getRequest();
        
        updateURL(request.getGetCapabilities().getDCPType());
        updateURL(request.getGetFeatureInfo().getDCPType());
        updateURL(request.getGetMap().getDCPType());
        updateExtendedOperationURL(request);
        
        //we build the layers object of the document
        
        //we get the list of layers
        List<AbstractLayer> layers = new ArrayList<AbstractLayer>();
        for (net.seagis.coverage.catalog.Layer inputLayer: layerList) {
            try {
                if (!inputLayer.isQueryable(Service.WMS)) {
                    logger.info("layer" + inputLayer.getName() + " not queryable by WMS");
                    continue;
                } 
                
                List<String> crs = new ArrayList<String>();
                
                Integer code = 4326;
                /* 
                 *  TODO 
                 * code = CRS.lookupEpsgCode(inputLayer.getCoverageReference().getCoordinateReferenceSystem(), false);
                 */ 
                
                crs.add("EPSG:" + code.toString());
                
                GeographicBoundingBox inputGeoBox = inputLayer.getGeographicBoundingBox();
               
                //we add the list od available date and elevation
                List<AbstractDimension> dimensions = new ArrayList<AbstractDimension>();
                
                
                //the available date
                String defaut = null;
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                PeriodUtilities periodFormatter = new PeriodUtilities(df);
                AbstractDimension dim;
                String value = "";
                SortedSet<Date> dates = inputLayer.getAvailableTimes();
                if (dates != null && dates.size() > 0) {
                    defaut = df.format(dates.last());
                    
                    if (getCurrentVersion().toString().endsWith("1.1.1"))
                        dim = new net.seagis.wms.v111.Dimension("time", "ISO8601", defaut, null);
                    else
                        dim = new net.seagis.wms.v130.Dimension("time", "ISO8601", defaut, null);
                    
                    value = periodFormatter.getDatesRespresentation(dates);
                    dim.setValue(value);
                    dimensions.add(dim);
                }
                
                //the available elevation
                defaut = null;
                SortedSet<Number> elevations = inputLayer.getAvailableElevations();
                if (elevations != null && elevations.size() > 0) {
                    defaut = elevations.first().toString();
                    
                    if (getCurrentVersion().toString().endsWith("1.1.1"))
                        dim = new net.seagis.wms.v111.Dimension("elevation", "EPSG:5030", defaut, null);
                    else
                        dim = new net.seagis.wms.v130.Dimension("elevation", "EPSG:5030", defaut, null);
                    value = "";
                    for (Number n:elevations){
                        value += n.toString() + ','; 
                    }
                    dim.setValue(value);
                    dimensions.add(dim);
                }
                
                //the dimension range
                defaut      = null;
                String unit = null;
                MeasurementRange[] ranges = inputLayer.getSampleValueRanges();
                if (ranges!= null && ranges.length>0 && ranges[0]!= null) {
                    defaut = ranges[0].getMinimum() + "," + ranges[0].getMaximum();
                    Unit u = ranges[0].getUnits();
                    if (u != null)
                        unit = u.toString();
                    
                    if (getCurrentVersion().toString().endsWith("1.1.1"))
                        dim = new net.seagis.wms.v111.Dimension("dim_range", unit, defaut, ranges[0].getMinimum() + "," + ranges[0].getMaximum());
                    else
                        dim = new net.seagis.wms.v130.Dimension("dim_range", unit, defaut, ranges[0].getMinimum() + "," + ranges[0].getMaximum());
                    dimensions.add(dim);
                }
                
                
                //we build and add a layer 
                AbstractLayer outputLayer;
                if (getCurrentVersion().toString().equals("1.1.1")) {
                    
                    /*
                     * TODO
                     * Envelope inputBox                 = inputLayer.getCoverage().getEnvelope();
                     */
                    net.seagis.wms.v111.BoundingBox outputBBox = null;
                    if(inputGeoBox != null) {
                        outputBBox = new net.seagis.wms.v111.BoundingBox(code.toString(), 
                                                                         inputGeoBox.getWestBoundLongitude(),
                                                                         inputGeoBox.getSouthBoundLatitude(),
                                                                         inputGeoBox.getEastBoundLongitude(),
                                                                         inputGeoBox.getNorthBoundLatitude(),
                                                                         0.0, 0.0,
                                                                         getCurrentVersion().toString());
                    }
                    
                    // we build a Style Object
                    net.seagis.wms.v111.OnlineResource or    = new net.seagis.wms.v111.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/png&LAYER=" + inputLayer.getName());
                    net.seagis.wms.v111.LegendURL legendURL1 = new net.seagis.wms.v111.LegendURL("image/png", or);

                    or = new net.seagis.wms.v111.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/gif&LAYER=" + inputLayer.getName());
                    net.seagis.wms.v111.LegendURL legendURL2 = new net.seagis.wms.v111.LegendURL("image/gif", or);
                    net.seagis.wms.v111.Style style          = new net.seagis.wms.v111.Style("Style1", "default Style", null, null, null,legendURL1,legendURL2);
                    
                    outputLayer = new net.seagis.wms.v111.Layer(inputLayer.getName(), 
                                                                cleanSpecialCharacter(inputLayer.getRemarks()),
                                                                cleanSpecialCharacter(inputLayer.getThematic()), 
                                                                crs, 
                                                                new LatLonBoundingBox(inputGeoBox.getWestBoundLongitude(), 
                                                                                      inputGeoBox.getSouthBoundLatitude(), 
                                                                                      inputGeoBox.getEastBoundLongitude(), 
                                                                                      inputGeoBox.getNorthBoundLatitude()), 
                                                                outputBBox,  
                                                                1,
                                                                dimensions,
                                                                style);
                //version 1.3.0
                } else {
                    
                    /*
                     * TODO
                     * Envelope inputBox                 = inputLayer.getCoverage().getEnvelope();
                     */
                    net.seagis.wms.v130.BoundingBox outputBBox = null;
                    if(inputGeoBox != null) {
                        outputBBox = new net.seagis.wms.v130.BoundingBox(code.toString(), 
                                                                         inputGeoBox.getWestBoundLongitude(),
                                                                         inputGeoBox.getSouthBoundLatitude(),
                                                                         inputGeoBox.getEastBoundLongitude(),
                                                                         inputGeoBox.getNorthBoundLatitude(),
                                                                         0.0, 0.0,
                                                                         getCurrentVersion().toString());
                    }
                    
                    // we build a Style Object
                    net.seagis.wms.v130.OnlineResource or    = new net.seagis.wms.v130.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/png&LAYER=" + inputLayer.getName());
                    net.seagis.wms.v130.LegendURL legendURL1 = new net.seagis.wms.v130.LegendURL("image/png", or);

                    or = new net.seagis.wms.v130.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/gif&LAYER=" + inputLayer.getName());
                    net.seagis.wms.v130.LegendURL legendURL2 = new net.seagis.wms.v130.LegendURL("image/gif", or);
                    net.seagis.wms.v130.Style style          = new net.seagis.wms.v130.Style("Style1", "default Style", null, null, null,legendURL1,legendURL2);
                    
                    
                    outputLayer = new net.seagis.wms.v130.Layer(inputLayer.getName(), 
                                                                cleanSpecialCharacter(inputLayer.getRemarks()),
                                                                cleanSpecialCharacter(inputLayer.getThematic()), 
                                                                crs, 
                                                                new EXGeographicBoundingBox(inputGeoBox.getWestBoundLongitude(), 
                                                                                            inputGeoBox.getSouthBoundLatitude(), 
                                                                                            inputGeoBox.getEastBoundLongitude(), 
                                                                                            inputGeoBox.getNorthBoundLatitude()), 
                                                                outputBBox,  
                                                                1,
                                                                dimensions,
                                                                style);
                    
                }
                layers.add(outputLayer);
                
            } catch (CatalogException exception) {
                throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, getCurrentVersion());
            }
        }
       
        
        //we build the list of accepted crs
        List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");crs.add("EPSG:3395");crs.add("EPSG:27574");
        crs.add("EPSG:27571");crs.add("EPSG:27572");crs.add("EPSG:27573");
        crs.add("EPSG:27574");
        
        //we build a general boundingbox TODO 
        EXGeographicBoundingBox exGeographicBoundingBox = null;
        
        //we build the general layer and add it to the document
        AbstractLayer mainLayer;
        if (getCurrentVersion().toString().equals("1.1.1")) {
            mainLayer = new net.seagis.wms.v111.Layer("Seagis Web Map Layer", 
                                                      "description of the service(need to be fill)", 
                                                       crs, 
                                                       null, 
                                                       layers);
        // version 1.3.0
        } else {
            mainLayer = new net.seagis.wms.v130.Layer("Seagis Web Map Layer", 
                                                      "description of the service(need to be fill)", 
                                                       crs, 
                                                       null, 
                                                       layers);
        }
        
        response.getCapability().setLayer(mainLayer);
        
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();
        if (getCurrentVersion().toString().equals("1.1.1")) {
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders",
              "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd\">\n");
        }
        marshaller.marshal(response, sw);
         
        return Response.ok(sw.toString(), format).build();
        
    }
    
    
    
    /**
     * 
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private String describeLayer() throws WebServiceException, JAXBException {
        logger.info("describeLayer request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        verifyBaseParameter(2);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        
        OnlineResourceType or = new OnlineResourceType(getServiceURL() + "wcs?");
        List<LayerDescriptionType> layersDescriptions = new ArrayList<LayerDescriptionType>();
        String layers = getParameter("LAYERS", true);
        Set<String> registredLayers = webServiceWorker.getLayerNames();
        final StringTokenizer tokens = new StringTokenizer(layers, ",");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            if (registredLayers.contains(token)) {
                TypeNameType t = new TypeNameType(token);
                LayerDescriptionType outputLayer = new LayerDescriptionType(or,t);
                layersDescriptions.add(outputLayer);
            } else {
                throw new WMSWebServiceException("This layer is not registred: " + token,
                      INVALID_PARAMETER_VALUE, getCurrentVersion());
            }
        }
                
        DescribeLayerResponseType response = new DescribeLayerResponseType(getSldVersion().toString(), layersDescriptions);
       
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();   
        marshaller.marshal(response, sw);
        return sw.toString();
    }
    
    
    private File getLegendGraphic() throws WebServiceException, JAXBException {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(2);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        webServiceWorker.setLayer(getParameter("LAYER", true));
        webServiceWorker.setFormat(getParameter("FORMAT", false));
        webServiceWorker.setDimension(getParameter("WIDTH", false), getParameter("HEIGHT", false), null);

        
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
     * update The URL in capabilities document with the service actual URL.
     */
    private void updateURL(List<? extends AbstractDCP> dcpList) {
        for(AbstractDCP dcp: dcpList) {
            AbstractProtocol getMethod = dcp.getHTTP().getGet();
            if (getMethod != null) {
                getMethod.getOnlineResource().setHref(getServiceURL() + "wms?SERVICE=WMS&");
            }
            AbstractProtocol postMethod = dcp.getHTTP().getPost();
            if (postMethod != null) {
                postMethod.getOnlineResource().setHref(getServiceURL() + "wms?SERVICE=WMS&");
            }
        }
    }
    
    /**
     * update The URL in capabilities document for the extended operation.
     */
    private void updateExtendedOperationURL(AbstractRequest request) {
        
        if (getCurrentVersion().toString().equals("1.3.0")) {
            net.seagis.wms.v130.Request r = (net.seagis.wms.v130.Request) request;
            List<JAXBElement<OperationType>> extendedOperations = r.getExtendedOperation();
            for(JAXBElement<OperationType> extOp: extendedOperations) {
                updateURL(extOp.getValue().getDCPType());
            }
        
        // version 1.1.1  
        } else {
           net.seagis.wms.v111.Request r = (net.seagis.wms.v111.Request) request;
           AbstractOperation op = r.getDescribeLayer();
           if (op != null)
                updateURL(op.getDCPType());
           op = r.getGetLegendGraphic();
           if (op != null)
                updateURL(op.getDCPType());
           op = r.getGetStyles();
           if (op != null)
                updateURL(op.getDCPType());
           op = r.getPutStyles();
           if (op != null)
                updateURL(op.getDCPType());
        }
        
    }
}
