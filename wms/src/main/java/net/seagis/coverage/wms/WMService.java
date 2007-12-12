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
import java.sql.SQLException;
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
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import javax.ws.rs.core.Response;
import net.opengis.wms.WMSCapabilities;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import org.geotools.util.Version;
/**
 * WMS 1.3.0 web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @author legal
 */

@UriTemplate("wms")
public class WMService {
    
    @HttpContext
    private UriInfo context;
    
    private Logger logger = Logger.getLogger("fr.geomatys.wms");
    
    private Marshaller marshaller;
    
    private Version version = new Version("1.3");
    
    private WebServiceWorker webServiceWorker;
    
    
    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WMService() throws JAXBException, IOException, WebServiceException {
        JAXBContext jbcontext = JAXBContext.newInstance("net.opengis.ogc");
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        webServiceWorker = new WebServiceWorker(new Database());
        webServiceWorker.setService("WMS", "1.3");
    }
   
    
    /**
     * Treat the incomming request and call the right function.
     * 
     * @return an image or xml response.
     */
    @HttpMethod("GET")
    public Response treatGETrequest() throws IOException, JAXBException  {

        try {
            try {
                String request = (String) getParameter("REQUEST", 'M');
                if (request.equals("GetMap")) {
                    
                    return Response.Builder.representation(getMap(), "image/png").build();
                    
                } else if (request.equals("GetFeatureInfo")) {
                    
                    return getFeatureInfo();
                    
                } else if (request.equals("GetCapabities")) {
                    
                    return Response.Builder.representation(getCapabilities(), "text/xml").build();
                    
                } else {
                    throw new WebServiceException("The operation " + request + " is not supported by the service",
                                                  WMSExceptionCode.OPERATION_NOT_SUPPORTED, version);
                }
            } catch (CatalogException ex) {
                throw new WebServiceException("The service has throw an CatalogException:" + ex.getMessage() ,
                                              WMSExceptionCode.NO_APPLICABLE_CODE, version);
            } catch (SQLException ex) {
                throw new WebServiceException("The service has throw an SQLException:" + ex.getMessage() ,
                                              WMSExceptionCode.NO_APPLICABLE_CODE, version);
            }
        } catch (WebServiceException ex) {
            StringWriter sw = new StringWriter();    
            marshaller.marshal(ex.getServiceExceptionReport(), sw);
            return Response.Builder.representation(sw.toString(), "text/xml").build();
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
     * @param type the type to parse can be : int, double, date, boolean or String
     * 
     * @return the parameter or null if not specified
     * @throw 
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
     * @throws net.seagis.catalog.CatalogException
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     * @throws fr.geomatys.wms.WebServiceException
     */
    private File getMap() throws CatalogException, IOException, SQLException, WebServiceException {
        logger.info("getMap request received");
        
        //we begin by extract the mandatory attribute
        if (!getParameter("VERSION", 'M').equals("1.3.0")) {
            throw new WebServiceException("The parameter VERSION=1.3.0 must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
        }
        
        String styles       = getParameter("STYLES", 'M');
        String format       = getParameter("FORMAT", 'M');
        
        webServiceWorker.setLayer(getParameter("LAYERS", 'M'));
        webServiceWorker.setCoordinateReferenceSystem(getParameter("CRS", 'M'));
        webServiceWorker.setBoundingBox(getParameter("BBOX", 'M'));
        webServiceWorker.setElevation(getParameter("ELEVATION", 'O'));
        
        String width  =  getParameter("WIDTH", 'M'); 
        String height = getParameter("HEIGHT", 'M'); 
                
        // and then we extract the optional attribute
        String transparent = getParameter("TRANSPARENT", 'O');
        
        String time        = getParameter("TIME", 'O');
        String bgColor = getParameter("BGCOLOR", 'O');
        if (bgColor == null) 
            bgColor = "0xFFFFFF";
        
        
       /* if (elevation != null) {
        
            Date date = new Date(86, 0, 1);
            Layer layer = getLayer(layerName, bbox);
            if (layer != null) {
                logger.info(layer.toString());
                logger.info(layer.getCoverageReferences().toString());
                CoverageReference ref = layer.getCoverageReference(date, elevation);
                if (ref != null) {
                    System.out.println("REF:" + ref.toString());
                    File f = new File("C:\\temp.tmp");
                    GridCoverage2D coverage = ref.getCoverage(null);
                    RenderedImage image = coverage.geophysics(false).getRenderedImage();
                    Iterator<ImageWriter> it = ImageIO.getImageWritersByMIMEType(format);
                
                    while (it.hasNext()) {
                        ImageWriter writer = it.next();
                        FileOutputStream buffer = new FileOutputStream(f);
                        ImageOutputStream stream = new MemoryCacheImageOutputStream(buffer);
                        writer.setOutput(stream);
                        writer.write(image);
                        stream.close();
                        return f;
                    }
                    throw new IOException("Format inconnu: " + format);
                } else logger.severe("ref null");
            } else  throw new WebServiceException("There is no layer call " + layerName,
                                                 WMSExceptionCode.LAYER_NOT_DEFINED, version);
        } */
        return null;
    }
    
    private Response getFeatureInfo() throws WebServiceException {
        logger.info("getFeatureInfo request received");
        
        //we begin by extract the mandatory attribute
        if (!getParameter("VERSION", 'M').equals("1.3.0")) {
            throw new WebServiceException("The parameter VERSION=1.3.0 must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
        }
        
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
        WMSCapabilities response = new WMSCapabilities();
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", 'M').equals("WMS")) {
            throw new WebServiceException("The parameters SERVICE=WMS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, version);
        }
        
        //and the the optional attribute
        String requestVersion = getParameter("VERSION", 'O');
        if (requestVersion != null && !requestVersion.equals("1.3.0")) {
            throw new WebServiceException("The parameter VERSION must be 1.3.0",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, this.version);
        }
        
        String format = getParameter("FORMAT", 'O');
        
        
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
        
    }
   
}
