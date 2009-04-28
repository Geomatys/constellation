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
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.imageio.IIOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.constellation.ServiceDef;
import org.constellation.tile.ws.AbstractWMTSWorker;
import org.constellation.tile.ws.WMTSWorker;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.OGCWebService;

import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;
import static org.constellation.ws.ExceptionCode.*;
import static org.constellation.query.Query.KEY_REQUEST;
import static org.constellation.query.Query.KEY_SERVICE;
import static org.constellation.query.Query.KEY_VERSION;
import static org.constellation.query.Query.TEXT_PLAIN;
import static org.constellation.query.Query.TEXT_XML;


/**
 * The REST facade to an OGC Web Map Tile Service, implementing the 1.0.0 version.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
@Path("wmts")
@Singleton
public class WMTSService extends OGCWebService {
    /**
     * A worker to use in order to do WMTS operations.
     */
    protected AbstractWMTSWorker worker;

    /**
     * Builds a new WMTS service REST (both REST Kvp and RESTFUL). This service only
     * provides the version 1.0.0 of OGC WMTS standard, for the moment.
     */
    public WMTSService() {
        super(ServiceDef.WMTS_1_0_0);
        try {
            setXMLContext("org.geotoolkit.wmts.xml.v100:" +
                          "org.geotoolkit.ows.xml.v110:"  +
                          "org.geotoolkit.gml.xml.v311.modified",
                          "http://www.opengis.net/wmts");
            worker = new WMTSWorker();

        } catch (JAXBException ex){
            workingContext = false;
            LOGGER.severe("The WMTS service is not running."       + '\n' +
                          " cause  : Error creating XML context." + '\n' +
                          " error  : " + ex.getMessage()          + '\n' +
                          " details: " + ex.toString());
        }
    }

    /**
     * Treat the incoming request and call the right function.
     *
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this param is {@code null}
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        Marshaller marshaller = null;
        ServiceDef serviceDef = null;
        try {
            if (worker == null) {
                throw new CstlServiceException("The WMTS service is not running",
                                              NO_APPLICABLE_CODE);
            }
            marshaller = marshallerPool.acquireMarshaller();
            logParameters();
            String request = "";

            if (objectRequest instanceof JAXBElement) {
                objectRequest = ((JAXBElement) objectRequest).getValue();
            }

            // if the request is not an xml request we fill the request parameter.
            if (objectRequest == null) {
                request = getParameter(KEY_REQUEST, true);
            }

            if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                GetCapabilities gc = (GetCapabilities) objectRequest;

                if (gc == null) {
                    /*
                     * if the parameters have been send by GET or POST kvp,
                     * we build a request object with this parameter.
                     */
                    gc = createNewGetCapabilitiesRequest();
                }
                serviceDef = getVersionFromNumber(gc.getVersion().toString());
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getCapabilities(gc), sw);

                return Response.ok(sw.toString(), TEXT_XML).build();
            }
            if (request.equalsIgnoreCase("GetTile") || (objectRequest instanceof GetTile)) {
                GetTile gt = (GetTile) objectRequest;

                if (gt == null) {
                    /*
                     * if the parameters have been send by GET or POST kvp,
                     * we build a request object with this parameter.
                     */
                    gt = createNewGetTileRequest();
                }
                serviceDef = getVersionFromNumber(gt.getVersion());
                return Response.ok(worker.getTile(gt), gt.getFormat()).build();
            }
            if (request.equalsIgnoreCase("GetFeatureInfo") || (objectRequest instanceof GetFeatureInfo)) {
                GetFeatureInfo gf = (GetFeatureInfo) objectRequest;

                if (gf == null) {
                    /*
                     * if the parameters have been send by GET or POST kvp,
                     * we build a request object with this parameter.
                     */
                    gf = createNewGetFeatureInfoRequest();
                }
                serviceDef = getVersionFromNumber(gf.getVersion());
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getFeatureInfo(gf), sw);

                return Response.ok(sw.toString(), TEXT_XML).build();
            }
            throw new CstlServiceException("The operation " + request +
                    " is not supported by the service", OPERATION_NOT_SUPPORTED, "request");
        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, marshaller, serviceDef);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
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

        AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        String section = getParameter("Sections", false);
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
        SectionsType sections     = new SectionsType(requestedSections);
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
        gfi.setI(new BigInteger(getParameter("I", true)));
        gfi.setJ(new BigInteger(getParameter("J", true)));
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
        getTile.setTileCol(new BigInteger(getParameter("TileCol", true)));
        getTile.setTileRow(new BigInteger(getParameter("TileRow", true)));
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
        getTile.setTileCol(new BigInteger(tileCol));
        if (tileRow == null) {
            throw new CstlServiceException("The parameter TILEROW must be specified",
                        MISSING_PARAMETER_VALUE);
        }
        getTile.setTileRow(new BigInteger(tileRow));
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
     * @throws JAXBException
     */
    @GET
    @Path("{version}/{caps}")
    public Response processGetCapabilitiesRestful(@PathParam("version") String version,
                                                  @PathParam("caps") String resourcename)
                                                                     throws JAXBException
    {
        Marshaller marshaller = null;
        ServiceDef serviceDef = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            if (worker == null) {
                throw new CstlServiceException("The WMTS service is not running",
                                              NO_APPLICABLE_CODE);
            }
            final GetCapabilities gc = createNewGetCapabilitiesRequestRestful(version);
            serviceDef = getVersionFromNumber(gc.getVersion().toString());
            final StringWriter sw = new StringWriter();
            marshaller.marshal(worker.getCapabilities(gc), sw);
            return Response.ok(sw.toString(), TEXT_XML).build();

        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, marshaller, serviceDef);

        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
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
     * @throws JAXBException
     */
    @GET
    @Path("{layer}/{tileMatrixSet}/{tileMatrix}/{tileRow}/{tileCol}.{format}")
    public Response processGetTileRestful(@PathParam("layer") String layer,
                                          @PathParam("tileMatrixSet") String tileMatrixSet,
                                          @PathParam("tileMatrix") String tileMatrix,
                                          @PathParam("tileRow") String tileRow,
                                          @PathParam("tileCol") String tileCol,
                                          @PathParam("format") String format)
                                          throws JAXBException
    {
        try {
            if (worker == null) {
                throw new CstlServiceException("The WMTS service is not running",
                                              NO_APPLICABLE_CODE);
            }
            final GetTile gt = createNewGetTileRequestRestful(layer, tileMatrixSet, tileMatrix,
                    tileRow, tileCol, format, null);
            final String mimeType;
            try {
                mimeType = Util.formatNameToMimeType(format);
            } catch (IIOException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
            return Response.ok(worker.getTile(gt), mimeType).build();
        } catch (CstlServiceException ex) {
            Marshaller marshaller = null;
            try {
                marshaller = marshallerPool.acquireMarshaller();
                return processExceptionResponse(ex, marshaller, null);
            } finally {
                if (marshaller != null) {
                    marshallerPool.release(marshaller);
                }
            }
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
     * @throws JAXBException if an error occurs during the marshalling of the exception.
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, final Marshaller marshaller,
                                                ServiceDef serviceDef) throws JAXBException
    {
        /* We don't print the stack trace:
         * - if the user have forget a mandatory parameter.
         * - if the version number is wrong.
         * - if the user have send a wrong request parameter
         */
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
            ex.printStackTrace();
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
        }

        if (workingContext) {
            if (serviceDef == null) {
                serviceDef = getBestVersion(null);
            }
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getExceptionCode().name(),
                    ex.getLocator(), serviceDef.exceptionVersion.toString());
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(Util.cleanSpecialCharacter(sw.toString()), TEXT_XML).build();
        } else {
            return Response.ok("The WMTS server is not running cause: unable to create JAXB context!", TEXT_PLAIN).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        LOGGER.info("Shutting down the REST WMTS service facade.");
    }

}
