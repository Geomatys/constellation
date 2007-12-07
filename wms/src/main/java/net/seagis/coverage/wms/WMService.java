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

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
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
import net.seagis.catalog.NoSuchTableException;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTable;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.opengis.metadata.extent.GeographicBoundingBox;
import javax.ws.rs.core.Response;
import net.opengis.wms.WMSCapabilities;
/**
 * WMS 1.3.0 web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @author legal
 */

@UriTemplate("wms")
public class WMService {
    
    @HttpContext
    private UriInfo context;
    
    private Database database;
    
    private LayerTable layers;
    
    private Logger logger = Logger.getLogger("fr.geomatys.wms");
    
    private Marshaller marshaller;
    
    
    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WMService() throws JAXBException {
        JAXBContext jbcontext = JAXBContext.newInstance("net.opengis.ogc");
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
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
                String request = (String) getParameter("REQUEST", 'M', "String");
                if (request.equals("GetMap")) {
                    
                    return Response.Builder.representation(getMap(), "image/png").build();
                    
                } else if (request.equals("GetFeatureInfo")) {
                    
                    return getFeatureInfo();
                    
                } else if (request.equals("GetCapabities")) {
                    
                    return Response.Builder.representation(getCapabilities(), "text/xml").build();
                    
                } else {
                    throw new WMServiceException("The operation " + request + " is not supported by the service",
                                                  WMSExceptionCode.OPERATION_NOT_SUPPORTED);
                }
            } catch (CatalogException ex) {
                throw new WMServiceException("The service has throw an CatalogException:" + ex.getMessage() ,
                                              WMSExceptionCode.NO_APPLICABLE_CODE);
            } catch (SQLException ex) {
                throw new WMServiceException("The service has throw an SQLException:" + ex.getMessage() ,
                                              WMSExceptionCode.NO_APPLICABLE_CODE);
            }
        } catch (WMServiceException ex) {
            StringWriter sw = new StringWriter();    
            marshaller.marshal(ex.getException(), sw);
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
    private Object getParameter(String parameterName, char obligation, String type) throws WMServiceException {
        
        MultivaluedMap parameters = context.getQueryParameters();
        LinkedList<String> list = (LinkedList) parameters.get(parameterName);
        if (list == null) {
            if (obligation == 'O') {
                
                if (type.equals("int") || type.equals("Double")) {
                    return -1;
                } else if (type.equals("Boolean")) {
                    return false;
                } else return null;
                
            } else {
                throw new WMServiceException("The parameter " + parameterName + " must be specify",
                                              WMSExceptionCode.MISSING_PARAMETER_VALUE);
            }
        }
        if (type.equals("String")) {
            return list.get(0);
            
        } else if (type.equals("int")) {
            try {
                return Integer.parseInt(list.get(0));

            } catch (NumberFormatException ex) {
                throw new WMServiceException("The parameter " + parameterName + "must be an integer",
                                             WMSExceptionCode.INVALID_PARAMETER_VALUE);
            }
        } else if (type.equals("Double")) {
            try {
                return Double.parseDouble(list.get(0));
                
            } catch (NumberFormatException ex) {
                 throw new WMServiceException("The parameter " + parameterName + "must be a double",
                                              WMSExceptionCode.INVALID_PARAMETER_VALUE);
            }
        } else if (type.equals("Date")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.parse(list.get(0));

            } catch (ParseException ex) {
                throw new WMServiceException("The parameter " + parameterName +" is mal-formated (yyyy-MM-dd)",
                                              WMSExceptionCode.INVALID_PARAMETER_VALUE);
            }
        } else if (type.equals("Boolean")) {
            return Boolean.parseBoolean(list.get(0));
        }
        return null;
        
    }
    
    /**
     * Return an new instance of the postgrid database.
     * 
     * @return
     * @throws java.io.IOException
     */
    private Database getDatabase() throws IOException {
        if (database == null) {
            database = new Database();
        }
        return database;
    }
    
    private Layer getLayer(String name, GeographicBoundingBox bbox) throws IOException, NoSuchTableException, CatalogException, SQLException {
        if (layers == null) {
            layers = getDatabase().getTable(LayerTable.class);
            //layers = new LayerTable(layers);
        }
        // layers.setGeographicBoundingBox(bbox);
        // layers.setTimeRange(...);
        return layers.getEntry(name);
    }

    
    /**
     * Return a map for the specified parameters in the query.
     * 
     * @return
     * @throws net.seagis.catalog.CatalogException
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     * @throws fr.geomatys.wms.WMServiceException
     */
    private File getMap() throws CatalogException, IOException, SQLException, WMServiceException {
        logger.info("getMap request received");
        
        //we begin by extract the mandatory attribute
        if (!getParameter("VERSION", 'M', "String").equals("1.3.0")) {
            throw new WMServiceException("The parameter VERSION=1.3.0 must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE);
        }
        
        String styles       = (String)getParameter("STYLES", 'M', "String");
        String layerName    = (String)getParameter("LAYERS", 'M', "String");
        String crs          = (String)getParameter("CRS", 'M', "String");
        String format       = (String)getParameter("FORMAT", 'M', "String");
        
        String boundingBox  = (String)getParameter("BBOX", 'M', "String");
        GeographicBoundingBox bbox = null;
        if (boundingBox != null) {
            double p1   = Double.parseDouble(boundingBox.substring(0, boundingBox.indexOf(',')));
            boundingBox = boundingBox.substring(boundingBox.indexOf(',') + 1);
            double p2   = Double.parseDouble(boundingBox.substring(0, boundingBox.indexOf(',')));
            boundingBox = boundingBox.substring(boundingBox.indexOf(',') + 1);
            double p3   = Double.parseDouble(boundingBox.substring(0, boundingBox.indexOf(',')));
            boundingBox = boundingBox.substring(boundingBox.indexOf(',') + 1);
            double p4   = Double.parseDouble(boundingBox);
            bbox = new GeographicBoundingBoxImpl(p1, p2, p3, p4);
        } else {
            throw new WMServiceException("The parameter BBOX must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE);
        }
        
        int width  =  (Integer)getParameter("WIDTH", 'M', "int"); 
        int height  = (Integer)getParameter("HEIGHT", 'M', "int"); 
                
        // and then we extract the optional attribute
        boolean transparent = (Boolean)getParameter("TRANSPARENT", 'O', "Boolean");
        
        Date time      = (Date)getParameter("TIME", 'O', "Date");
        String bgColor = (String)getParameter("BGCOLOR", 'O', "String");
        if (bgColor == null) 
            bgColor = "0xFFFFFF";
        
        Double elevation = 200.0;// getParameter("ELEVATION", 'O', "Double");
        if (elevation != null) {
        
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
            } else  throw new WMServiceException("There is no layer call " + layerName,
                                                 WMSExceptionCode.LAYER_NOT_DEFINED);
        } return null;//"<pb>no elevation<pb>";
    }
    
    private Response getFeatureInfo() throws WMServiceException {
        logger.info("getFeatureInfo request received");
        
        //we begin by extract the mandatory attribute
        if (!getParameter("VERSION", 'M', "String").equals("1.3.0")) {
            throw new WMServiceException("The parameter VERSION=1.3.0 must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE);
        }
        
        String query_layers = (String)getParameter("QUERY_LAYERS", 'M', "String");
        String info_format  = (String)getParameter("INFO_FORMAT", 'M', "String");
        
        int i = (Integer) getParameter("I", 'M', "int");
        int j = (Integer) getParameter("J", 'M', "int");
       
        //and then the optional attribute
        int feature_count = (Integer) getParameter("FEATURE_COUNT", 'O', "int");
        
        String exception = (String)getParameter("EXCEPTIONS", 'O', "String");
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
    
    private String getCapabilities() throws WMServiceException, JAXBException {
        logger.info("getCapabilities request received");
        
        // the service shall return WMSCapabilities marshalled
        WMSCapabilities response = new WMSCapabilities();
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", 'M', "String").equals("WMS")) {
            throw new WMServiceException("The parameters SERVICE=WMS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE);
        }
        
        //and the the optional attribute
        String version = (String)getParameter("VERSION", 'O', "String");
         if (version != null && !version.equals("1.3.0")) {
            throw new WMServiceException("The parameter VERSION must be 1.3.0",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE);
        }
        
        String format = (String) getParameter("FORMAT", 'O', "String");
        
        
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
        
    }
   
}
