/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.tile.ws.rs;

import com.sun.jersey.spi.resource.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.imageio.IIOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.constellation.ServiceDef;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.GridWebService;
import org.constellation.tile.ws.WMTSWorker;
import org.constellation.tile.ws.DefaultWMTSWorker;
import static org.constellation.ws.ExceptionCode.*;
import static org.constellation.query.Query.KEY_REQUEST;
import static org.constellation.query.Query.KEY_SERVICE;
import static org.constellation.query.Query.KEY_VERSION;
import org.geotoolkit.ows.xml.RequestBase;

import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.util.ImageIOUtilities;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;

/**
 * The REST facade to an OGC Web Map Tile Service, implementing the 1.0.0 version.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
@Path("{serviceId}/wmts")
@Singleton
public class WMTSService extends GridWebService<WMTSWorker> {
    

    private static final String NOT_WORKING = "The WMTS service is not running";
    /**
     * Builds a new WMTS service REST (both REST Kvp and RESTFUL). This service only
     * provides the version 1.0.0 of OGC WMTS standard, for the moment.
     */
    public WMTSService() {
        super(ServiceDef.WMTS_1_0_0);
        setXMLContext(WMTSMarshallerPool.getInstance());
        setFullRequestLog(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WMTSWorker createWorker(File instanceDirectory) {
        return new DefaultWMTSWorker(instanceDirectory.getName());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest, WMTSWorker worker) throws JAXBException {
        ServiceDef serviceDef = null;
        try {
            if (worker == null) {
                throw new CstlServiceException(NOT_WORKING,
                                              NO_APPLICABLE_CODE);
            }
            logParameters();
            worker.setServiceUrl(getServiceURL());

            // if the request is not an xml request we fill the request parameter.
            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter(KEY_REQUEST, true));
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
            }
            serviceDef = getVersionFromNumber(request.getVersion());

            if (request instanceof GetCapabilities) {
                final GetCapabilities gc = (GetCapabilities) request;
                return Response.ok(worker.getCapabilities(gc), MimeType.TEXT_XML).build();
            }
            if (request instanceof GetTile) {
                final GetTile gt = (GetTile) request;
                return Response.ok(worker.getTile(gt), gt.getFormat()).build();
            }
            if (request instanceof GetFeatureInfo) {
                final GetFeatureInfo gf = (GetFeatureInfo) request;
                return Response.ok(worker.getFeatureInfo(gf), MimeType.TEXT_XML).build();
            }

            throw new CstlServiceException("The operation " + request.getClass().getName() +
                    " is not supported by the service", OPERATION_NOT_SUPPORTED, "request");
        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef);
        }
    }

    /**
     * Build request object fom KVP parameters.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(String request) throws CstlServiceException {

        if ("GetCapabilities".equalsIgnoreCase(request)) {
            return createNewGetCapabilitiesRequest();
        } else if ("GetTile".equalsIgnoreCase(request)) {
            return createNewGetTileRequest();
        } else if ("GetFeatureInfo".equalsIgnoreCase(request)) {
            return createNewGetFeatureInfoRequest();
        }
        throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
    }

    /**
     * Builds a new {@link GetCapabilities} request from a REST Kvp request.
     *
     * @return The {@link GetCapabilities} request.
     * @throws CstlServiceException if a required parameter is not present in the request.
     */
    private GetCapabilities createNewGetCapabilitiesRequest() throws CstlServiceException {

        String version = getParameter("acceptVersions", false);
        AcceptVersionsType versions;
        if (version != null) {
            if (version.indexOf(',') != -1) {
                version = version.substring(0, version.indexOf(','));
            }
            versions = new AcceptVersionsType(version);
        } else {
             versions = new AcceptVersionsType("1.0.0");
        }

        final AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        final String section = getParameter("Sections", false);
        List<String> requestedSections = new ArrayList<String>();
        if (section != null && !section.equalsIgnoreCase("All")) {
            final StringTokenizer tokens = new StringTokenizer(section, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (SectionsType.getExistingSections("1.1.1").contains(token)){
                    requestedSections.add(token);
                } else {
                    throw new CstlServiceException("The section " + token + " does not exist",
                                                  INVALID_PARAMETER_VALUE, "Sections");
                }
            }
        } else {
            //if there is no requested Sections we add all the sections
            requestedSections = SectionsType.getExistingSections("1.1.1");
        }
        final SectionsType sections     = new SectionsType(requestedSections);
        return new GetCapabilities(versions,
                                   sections,
                                   formats,
                                   null,
                                   getParameter(KEY_SERVICE, true));

    }

    /**
     * Builds a new {@link GetCapabilities} request from a RESTFUL request.
     *
     * @return The {@link GetCapabilities} request.
     * @throws CstlServiceException if a required parameter is not present in the request.
     */
    private GetCapabilities createNewGetCapabilitiesRequestRestful(final String version)
                                                             throws CstlServiceException
    {
        AcceptVersionsType versions;
        if (version != null) {
            versions = new AcceptVersionsType(version);
        } else {
            versions = new AcceptVersionsType("1.0.0");
        }
        return new GetCapabilities(versions, null, null, null, "WMTS");
    }

    /**
     * Builds a new {@link GetFeatureInfo} request from a REST Kvp request.
     *
     * @return The {@link GetFeatureInfo} request.
     * @throws CstlServiceException if a required parameter is not present in the request.
     */
    private GetFeatureInfo createNewGetFeatureInfoRequest() throws CstlServiceException {
        final GetFeatureInfo gfi = new GetFeatureInfo();
        gfi.setGetTile(createNewGetTileRequest());
        gfi.setI(Integer.valueOf(getParameter("I", true)));
        gfi.setJ(Integer.valueOf(getParameter("J", true)));
        gfi.setInfoFormat(getParameter("infoformat", true));
        gfi.setService(getParameter(KEY_SERVICE, true));
        gfi.setVersion(getParameter(KEY_VERSION, true));
        return gfi;
    }

    /**
     * Builds a new {@link GetTile} request from a REST Kvp request.
     *
     * @return The {@link GetTile} request.
     * @throws CstlServiceException if a required parameter is not present in the request.
     */
    private GetTile createNewGetTileRequest() throws CstlServiceException {
        final GetTile getTile = new GetTile();
        // Mandatory parameters
        getTile.setFormat(getParameter("format", true));
        getTile.setLayer(getParameter("layer", true));
        getTile.setService(getParameter(KEY_SERVICE, true));
        getTile.setVersion(getParameter(KEY_VERSION, true));
        getTile.setTileCol(Integer.valueOf(getParameter("TileCol", true)));
        getTile.setTileRow(Integer.valueOf(getParameter("TileRow", true)));
        getTile.setTileMatrix(getParameter("TileMatrix", true));
        getTile.setTileMatrixSet(getParameter("TileMatrixSet", true));
        // Optionnal parameters
        getTile.setStyle(getParameter("style", false));
        return getTile;
    }

    /**
     * Builds a new {@link GetTile} request from a RESTFUL request.
     *
     * @return The {@link GetTile} request.
     * @throws CstlServiceException if a required parameter is not present in the request.
     */
    private GetTile createNewGetTileRequestRestful(String layer, String tileMatrixSet,
                                                   String tileMatrix, String tileRow,
                                                   String tileCol, String format, String style)
                                                   throws CstlServiceException
    {
        final GetTile getTile = new GetTile();
        // Mandatory parameters
        if (format == null) {
            throw new CstlServiceException("The parameter FORMAT must be specified",
                        MISSING_PARAMETER_VALUE);
        }
        getTile.setFormat(format);
        getTile.setLayer(layer);
        if (layer == null) {
            throw new CstlServiceException("The parameter LAYER must be specified",
                        MISSING_PARAMETER_VALUE);
        }
        getTile.setService("WMTS");
        getTile.setVersion("1.0.0");
        if (tileCol == null) {
            throw new CstlServiceException("The parameter TILECOL must be specified",
                        MISSING_PARAMETER_VALUE);
        }
        getTile.setTileCol(Integer.valueOf(tileCol));
        if (tileRow == null) {
            throw new CstlServiceException("The parameter TILEROW must be specified",
                        MISSING_PARAMETER_VALUE);
        }
        getTile.setTileRow(Integer.valueOf(tileRow));
        if (tileMatrix == null) {
            throw new CstlServiceException("The parameter TILEMATRIX must be specified",
                        MISSING_PARAMETER_VALUE);
        }
        getTile.setTileMatrix(tileMatrix);
        if (tileMatrixSet == null) {
            throw new CstlServiceException("The parameter TILEMATRIXSET must be specified",
                        MISSING_PARAMETER_VALUE);
        }
        getTile.setTileMatrixSet(tileMatrixSet);
        // Optionnal parameters
        getTile.setStyle(style);
        return getTile;
    }

    /**
     * Handle {@code GetCapabilities request} in RESTFUL mode.
     *
     * @param version The version of the GetCapabilities request.
     * @param resourcename The name of the resource file.
     *
     * @return The XML formatted response, for an OWS GetCapabilities of the WMTS standard.
     */
    @GET
    @Path("{version}/{caps}")
    public Response processGetCapabilitiesRestful(@PathParam("version") String version,
                                                  @PathParam("caps") String resourcename)
                                                                     
    {
        ServiceDef serviceDef = null;
        try {
            final String serviceId = getParameter("serviceId", false);
            WMTSWorker worker = workersMap.get(serviceId);
            if (worker == null) {
                throw new CstlServiceException(NOT_WORKING,
                                              NO_APPLICABLE_CODE);
            }
            final GetCapabilities gc = createNewGetCapabilitiesRequestRestful(version);
            serviceDef = getVersionFromNumber(gc.getVersion());
            return Response.ok(worker.getCapabilities(gc), MimeType.TEXT_XML).build();

        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef);

        }
    }

    /**
     * Handle {@code GetTile request} in RESTFUL mode.
     *
     * @param layer The layer to request.
     * @param tileMatrixSet The matrix set of the tile.
     * @param tileMatrix The matrix tile.
     * @param tileRow The row of the tile in the matrix.
     * @param tileCol The column of the tile in the matrix.
     * @param format The format extension, like png.
     *
     * @return The response containing the tile.
     */
    @GET
    @Path("{layer}/{tileMatrixSet}/{tileMatrix}/{tileRow}/{tileCol}.{format}")
    public Response processGetTileRestful(@PathParam("layer") String layer,
                                          @PathParam("tileMatrixSet") String tileMatrixSet,
                                          @PathParam("tileMatrix") String tileMatrix,
                                          @PathParam("tileRow") String tileRow,
                                          @PathParam("tileCol") String tileCol,
                                          @PathParam("format") String format)
    {
        try {
            final String serviceId = getParameter("serviceId", false);
            WMTSWorker worker = workersMap.get(serviceId);
            if (worker == null) {
                throw new CstlServiceException(NOT_WORKING,
                                              NO_APPLICABLE_CODE);
            }
            final GetTile gt = createNewGetTileRequestRestful(layer, tileMatrixSet, tileMatrix,
                    tileRow, tileCol, format, null);
            final String mimeType;
            try {
                mimeType = ImageIOUtilities.formatNameToMimeType(format);
            } catch (IIOException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
            return Response.ok(worker.getTile(gt), mimeType).build();
        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, null);
        }
    }

    /**
     * Handle all exceptions returned by a web service operation in two ways:
     * <ul>
     *   <li>if the exception code indicates a mistake done by the user, just display a single
     *       line message in logs.</li>
     *   <li>otherwise logs the full stack trace in logs, because it is something interesting for
     *       a developper</li>
     * </ul>
     * In both ways, the exception is then marshalled and returned to the client.
     *
     * @param ex The exception that has been generated during the webservice operation requested.
     * @return An XML representing the exception.
     *
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) {
        logException(ex);
        
        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        final String codeName = getOWSExceptionCodeRepresentation(ex.getExceptionCode());

        final ExceptionReport report = new ExceptionReport(ex.getMessage(), codeName,
                ex.getLocator(), serviceDef.exceptionVersion.toString());
        return Response.ok(report, MimeType.TEXT_XML).build();
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        LOGGER.info("Shutting down the REST WMTS service facade.");
    }

}
