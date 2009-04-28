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
package org.constellation.map.ws;

//J2SE dependencies
import com.sun.jersey.api.core.HttpContext;
import java.awt.image.BufferedImage;
import javax.servlet.ServletContext;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

//Constellation dependencies
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.constellation.ws.CstlServiceException;

//Geotools dependencies
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;


/**
 * Abstract definition of a {@code Web Map Service} worker called by a facade
 * to perform the logic for a particular WMS instance.
 *
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractWMSWorker {

    /**
     * Contains information about the HTTP exchange of the request, for instance, 
     * the HTTP headers.
     */
    protected HttpContext httpContext = null;
    /**
     * Contains authentication information related to the requesting principal.
     */
    protected SecurityContext securityContext = null;
    /**
     * Defines a set of methods that a servlet uses to communicate with its servlet container,
     * for example, to get the MIME type of a file, dispatch requests, or write to a log file.
     */
    protected ServletContext servletContext = null;
    /**
     * Contains the request URI and therefore any  KVP parameters it may contain.
     */
    protected UriInfo uriContext = null;

    
    
    
    /**
     * Initialize the {@see #uriContext} information.
     */
    public void initUriContext(final UriInfo uriInfo){
        uriContext = uriInfo;
    }

    /**
     * Initialize the {@see #httpContext} value.
     */
    public void initHTTPContext(final HttpContext httpCtxt){
        httpContext = httpCtxt;
    }

    /**
     * Initialize the {@see #servletContext} value.
     */
    public void initServletContext(final ServletContext servCtxt){
        servletContext = servCtxt;
    }

    /**
     * Initialize the {@see #servletContext} value.
     */
    public void initSecurityContext(final SecurityContext secCtxt){
        securityContext = secCtxt;
    }

    /**
     * Returns a description of the requested layer.
     *
     * @param descLayer The {@linkplain DescribeLayer describe layer} request done on this service.
     * @throws CstlServiceException
     */
    public abstract DescribeLayerResponseType describeLayer(final DescribeLayer descLayer)           throws CstlServiceException;

    /**
     * Returns an unmarshalled {@linkplain AbstractWMSCapabilities get capabilities} object.
     *
     * @param getCapabilities The {@linkplain GetCapabilities get capabilities} request done on this service.
     * @throws CstlServiceException
     */
    public abstract AbstractWMSCapabilities   getCapabilities(final GetCapabilities getCapabilities) throws CstlServiceException;

    /**
     * Returns a string, which will contain the result of a {@code GetFeatureInfo} request.
     *
     * @param getFeatureInfo The {@linkplain GetFeatureInfo get feature info} request done on this service.
     * @throws CstlServiceException
     */
    public abstract String                    getFeatureInfo(final GetFeatureInfo getFeatureInfo)    throws CstlServiceException;

    /**
     * Returns a {@link BufferedImage}, which is the result of a {@code GetLegendGraphic} request.
     *
     * @param getLegend The {@linkplain GetLegendGraphic get legend graphic} request done on this service.
     * @throws CstlServiceException
     */
    public abstract BufferedImage             getLegendGraphic(final GetLegendGraphic getLegend)     throws CstlServiceException;

    /**
     * Returns a {@link BufferedImage}, which is the result of a {@code GetMap} request.
     *
     * @param getMap The {@linkplain GetMap get map} request done on this service.
     * @throws CstlServiceException
     */
    public abstract BufferedImage             getMap(final GetMap getMap)                            throws CstlServiceException;
}
