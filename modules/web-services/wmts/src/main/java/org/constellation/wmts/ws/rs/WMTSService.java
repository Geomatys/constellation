/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.wmts.ws.rs;

import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.wmts.ws.DefaultWMTSWorker;
import org.constellation.wmts.ws.WMTSWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.GridWebService;
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

import javax.imageio.IIOException;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import static org.constellation.api.QueryConstants.ACCEPT_FORMATS_PARAMETER;
import static org.constellation.api.QueryConstants.ACCEPT_VERSIONS_PARAMETER;
import static org.constellation.api.QueryConstants.REQUEST_PARAMETER;
import static org.constellation.api.QueryConstants.SECTIONS_PARAMETER;
import static org.constellation.api.QueryConstants.SERVICE_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import static org.constellation.ws.ExceptionCode.INVALID_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.MISSING_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.NO_APPLICABLE_CODE;
import static org.constellation.ws.ExceptionCode.OPERATION_NOT_SUPPORTED;

// Jersey dependencies

/**
 * The REST facade to an OGC Web Map Tile Service, implementing the 1.0.0 version.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
@Path("wmts/{serviceId}")
@Singleton
public class WMTSService extends GridWebService<WMTSWorker> {

    private static final String NOT_WORKING = "The WMTS service is not running";

    /**
     * Builds a new WMTS service REST (both REST Kvp and RESTFUL). This service only
     * provides the version 1.0.0 of OGC WMTS standard, for the moment.
     */
    public WMTSService() {
        super(Specification.WMTS);
        setXMLContext(WMTSMarshallerPool.getInstance());
        setFullRequestLog(true);
        LOGGER.log(Level.INFO, "WMTS REST service running ({0} instances)\n", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return DefaultWMTSWorker.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest, final WMTSWorker worker) {
        ServiceDef serviceDef = null;
        try {
            
            // if the request is not an xml request we fill the request parameter.
            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter(REQUEST_PARAMETER, true));
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
            }
            serviceDef = worker.getVersionFromNumber(request.getVersion());

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
                final Map.Entry<String, Object> result = worker.getFeatureInfo(gf);
                if (result != null) {
                    return Response.ok(result.getValue(), result.getKey()).build();
                }
                //throw an exception if result of GetFeatureInfo visitor is null
                throw new CstlServiceException("An error occurred during GetFeatureInfo response building.");
            }

            throw new CstlServiceException("The operation " + request.getClass().getName() +
                    " is not supported by the service", OPERATION_NOT_SUPPORTED, "request");
        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef, worker);
        }
    }

    /**
     * Build request object fom KVP parameters.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(final String request) throws CstlServiceException {

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

        String version = getParameter(ACCEPT_VERSIONS_PARAMETER, false);
        AcceptVersionsType versions;
        if (version != null) {
            if (version.indexOf(',') != -1) {
                version = version.substring(0, version.indexOf(','));
            }
            versions = new AcceptVersionsType(version);
        } else {
             versions = new AcceptVersionsType("1.0.0");
        }

        final AcceptFormatsType formats = new AcceptFormatsType(getParameter(ACCEPT_FORMATS_PARAMETER, false));

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        final String section = getParameter(SECTIONS_PARAMETER, false);
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
        final String updateSequence = getParameter(UPDATESEQUENCE_PARAMETER, false);
        return new GetCapabilities(versions,
                                   sections,
                                   formats,
                                   updateSequence,
                                   getParameter(SERVICE_PARAMETER, true));

    }

    /**
     * Builds a new {@link GetCapabilities} request from a RESTFUL request.
     *
     * @return The {@link GetCapabilities} request.
     * @throws CstlServiceException if a required parameter is not present in the request.
     */
    private GetCapabilities createNewGetCapabilitiesRequestRestful(final String version) throws CstlServiceException {
        final AcceptVersionsType versions;
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
        gfi.setService(getParameter(SERVICE_PARAMETER, true));
        gfi.setVersion(getParameter(VERSION_PARAMETER, true));
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
        getTile.setService(getParameter(SERVICE_PARAMETER, true));
        getTile.setVersion(getParameter(VERSION_PARAMETER, true));
        getTile.setTileCol(Integer.valueOf(getParameter("TileCol", true)));
        getTile.setTileRow(Integer.valueOf(getParameter("TileRow", true)));
        getTile.setTileMatrix(getParameter("TileMatrix", true));
        getTile.setTileMatrixSet(getParameter("TileMatrixSet", true));
        // Optional parameters
        getTile.setStyle(getParameter("style", false));
        return getTile;
    }

    /**
     * Builds a new {@link GetTile} request from a RESTFUL request.
     *
     * @return The {@link GetTile} request.
     * @throws CstlServiceException if a required parameter is not present in the request.
     */
    private GetTile createNewGetTileRequestRestful(final String layer, final String tileMatrixSet,
                                                   final String tileMatrix, final String tileRow,
                                                   final String tileCol, final String format, final String style)
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
    public Response processGetCapabilitiesRestful(@PathParam("version") final String version,
                                                  @PathParam("caps") final String resourcename) {
        try {
            final GetCapabilities gc = createNewGetCapabilitiesRequestRestful(version);
            return treatIncomingRequest(gc);
        } catch (CstlServiceException ex) {
            final Worker w = WSEngine.getInstance("WMTS", getSafeParameter("serviceId"));
            return processExceptionResponse(ex, null, w);
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
    public Response processGetTileRestful(@PathParam("layer") final String layer,
                                          @PathParam("tileMatrixSet") final String tileMatrixSet,
                                          @PathParam("tileMatrix") final String tileMatrix,
                                          @PathParam("tileRow") final String tileRow,
                                          @PathParam("tileCol") final String tileCol,
                                          @PathParam("format") final String format) {
        try {
            final String mimeType;
            try {
                mimeType = ImageIOUtilities.formatNameToMimeType(format);
            } catch (IIOException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
            final GetTile gt = createNewGetTileRequestRestful(layer, tileMatrixSet, tileMatrix, tileRow, tileCol, mimeType, null);
            return treatIncomingRequest(gt);
        } catch (CstlServiceException ex) {
            final Worker w = WSEngine.getInstance("WMTS", getSafeParameter("serviceId"));
            return processExceptionResponse(ex, null, w);
        }
    }

    /**
     * Handle all exceptions returned by a web service operation in two ways:
     * <ul>
     *   <li>if the exception code indicates a mistake done by the user, just display a single
     *       line message in logs.</li>
     *   <li>otherwise logs the full stack trace in logs, because it is something interesting for
     *       a developer</li>
     * </ul>
     * In both ways, the exception is then marshalled and returned to the client.
     *
     * @param ex The exception that has been generated during the web-service operation requested.
     * @return An XML representing the exception.
     *
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef, final Worker worker) {
        logException(ex);

        if (serviceDef == null) {
            serviceDef = worker.getBestVersion(null);
        }
        final String codeName = getOWSExceptionCodeRepresentation(ex.getExceptionCode());

        final ExceptionReport report = new ExceptionReport(ex.getMessage(), codeName,
                ex.getLocator(), serviceDef.exceptionVersion.toString());
        return Response.ok(report, MimeType.TEXT_XML).build();

    }
}
