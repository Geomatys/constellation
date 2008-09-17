/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.coverage.ws;

import com.sun.jersey.spi.resource.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.measure.unit.Unit;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

//Constellation dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.WMSWebServiceException;
import org.constellation.sld.v110.TypeNameType;
import org.constellation.sld.v110.DescribeLayerResponseType;
import org.constellation.sld.v110.LayerDescriptionType;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.coverage.web.WebServiceWorker;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.PointType;
import org.constellation.se.OnlineResourceType;
import org.constellation.util.PeriodUtilities;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.wms.AbstractDimension;
import org.constellation.wms.AbstractLayer;
import org.constellation.wms.v111.LatLonBoundingBox;
import org.constellation.wms.v130.EXGeographicBoundingBox;
import org.constellation.coverage.metadata.LayerMetadata;
import org.constellation.coverage.metadata.SeriesMetadata;
import org.constellation.coverage.metadata.LayerMetadataTable;
import org.constellation.coverage.metadata.PointOfContact;
import org.constellation.coverage.metadata.PointOfContactTable;
import org.constellation.coverage.metadata.SeriesMetadataTable;
import org.constellation.ws.rs.WebService;

//geotools dependencies
import org.geotools.util.DateRange;
import org.geotools.util.MeasurementRange;

//geoapi dependencies
import org.opengis.coverage.SampleDimension;
import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;

import static org.constellation.coverage.wms.WMSExceptionCode.*;
import static org.constellation.query.wms.WMSQuery.*;

/**
 * WMS 1.3.0 / 1.1.1
 * web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @version
 * @author Guilhem Legal
 * @author Cédric Briançon
 */
@Path("wms")
@Singleton
public class WMService extends WebService {
    /**
     * A list of layer initialized a begining;
     */
    private Set<org.constellation.coverage.catalog.Layer> layerList;

    /**
     * The object whitch made all the operation on the postgrid database
     */
    private static ThreadLocal<WebServiceWorker> webServiceWorker;

    /**
     * Instanciate the {@link WebServiceWorker} if not already defined, trying to get some
     * information already defined in the JNDI configuration of the application server.
     *
     * @throws IOException if we try to connect to the database using information stored in the
     *                     configuration file.
     * @throws NamingException if an error in getting properties from JNDI references occured.
     *                         For the moment, it is not thrown, and it fall backs on the
     *                         configuration defined in the config.xml file.
     * @throws SQLException if an error occured while configuring the connection.
     */
    private static synchronized void ensureWorkerInitialized() throws SQLException, IOException,
                                                                      NamingException {

        if (webServiceWorker == null) {
            Database database;
            try {
                final Connection connection;
                final Properties properties = new Properties();
                String permission = null, readOnly = null, rootDir = null;
                final InitialContext ctx = new InitialContext();
                /* domain.name is a property only present when using the glassfish application
                 * server.
                 */
                if (System.getProperty("domain.name") != null) {
                    final DataSource ds = (DataSource) ctx.lookup("Coverages");
                    if (ds == null) {
                        throw new NamingException("DataSource \"Coverages\" is not defined.");
                    }
                    connection = ds.getConnection();
                    final String coverageProps = "Coverages Properties";
                    permission = getPropertyValue(coverageProps, "Permission");
                    readOnly   = getPropertyValue(coverageProps, "ReadOnly");
                    rootDir    = getPropertyValue(coverageProps, "RootDirectory");
                } else {
                    // Here we are not in glassfish, probably in a Tomcat application server.
                    final Context envContext = (Context) ctx.lookup("java:/comp/env");
                    final DataSource ds = (DataSource) envContext.lookup("Coverages");
                    if (ds == null) {
                        throw new NamingException("DataSource \"Coverages\" is not defined.");
                    }
                    connection = ds.getConnection();
                    permission = getPropertyValue(null, "Permission");
                    readOnly   = getPropertyValue(null, "ReadOnly");
                    rootDir    = getPropertyValue(null, "RootDirectory");
                }
                // Put all properties found in the JNDI reference into the Properties HashMap
                if (permission != null) {
                    properties.setProperty(ConfigurationKey.PERMISSION.getKey(), permission);
                }
                if (readOnly != null) {
                    properties.setProperty(ConfigurationKey.READONLY.getKey(), readOnly);
                }
                if (rootDir != null) {
                    properties.setProperty(ConfigurationKey.ROOT_DIRECTORY.getKey(), rootDir);
                }
                try {
                    database = new Database(connection, properties);
                } catch (IOException io) {
                    /* This error should never appear, because the IOException on the Database
                     * constructor can only overcome if we use the constructor
                     * Database(DataSource, Properties, String), and here the string for the
                     * configuration file is null, so no reading method on a file will be used.
                     * Anyways if this error occurs, an AssertionError is then thrown.
                     */
                    throw new AssertionError(io);
                }
            } catch (NamingException n) {
                /* If a NamingException occurs, it is because the JNDI connection is not
                 * correctly defined, and some information are lacking.
                 * In this case we try to use the old system of configuration file.
                 */

                /* Ifremer's server does not contain any .sicade directory, so the
                 * configuration file is put under the WEB-INF directory of constellation.
                 * todo: get the webservice name (here ifremerWS) from the servlet context.
                 */
                File configFile = null;
                File dirCatalina = null;
                final String catalinaPath = System.getenv().get("CATALINA_HOME");
                if (catalinaPath != null) {
                    dirCatalina = new File(catalinaPath);
                }
                if (dirCatalina != null && dirCatalina.exists()) {
                    configFile = new File(dirCatalina, "webapps/ifremerWS/WEB-INF/config.xml");
                    if (!configFile.exists()) {
                        configFile = null;
                    }
                }
                database = (configFile != null) ? new Database(configFile) : new Database();
            }
            final WebServiceWorker initialValue = new WebServiceWorker(database, true);
            webServiceWorker = new ThreadLocal<WebServiceWorker>() {
                @Override
                protected WebServiceWorker initialValue() {
                    return new WebServiceWorker(initialValue);
                }
            };
        }
    }

    /**
     * Build a new instance of the webService and initialise the JAXB marshaller.
     */
    public WMService() throws JAXBException, WebServiceException, SQLException,
                                IOException, NamingException {
        super("WMS", new ServiceVersion(Service.WMS, "1.3.0"), new ServiceVersion(Service.WMS, "1.1.1"));
        ensureWorkerInitialized();

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext("org.constellation.coverage.web:org.constellation.wms.v111:org.constellation.wms.v130:" +
                "org.constellation.sld.v110:org.constellation.gml.v311", "http://www.opengis.net/wms");

        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        LOGGER.info("Loading layers please wait...");
        layerList = webServiceWorker.getLayers();
        LOGGER.info("WMS service running");
    }

    /**
     * Treat the incoming request and call the right function.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        try {
            String request = (String) getParameter(KEY_REQUEST, true);
            LOGGER.info("new request:" + request);
            writeParameters();

            if (REQUEST_MAP.equalsIgnoreCase(request)) {
                return Response.ok(getMap(), webServiceWorker.getMimeType()).build();
            } else if (REQUEST_FEATUREINFO.equalsIgnoreCase(request)) {
                return getFeatureInfo();
            } else if (REQUEST_CAPABILITIES.equalsIgnoreCase(request)) {
                return getCapabilities();
            } else if (REQUEST_DESCRIBELAYER.equalsIgnoreCase(request)) {
                return Response.ok(describeLayer(), "text/xml").build();
            } else if (REQUEST_LEGENDGRAPHIC.equalsIgnoreCase(request)) {
                return Response.ok(getLegendGraphic(), webServiceWorker.getMimeType()).build();
            } else if (REQUEST_ORIGFILE.equalsIgnoreCase(request)) {
                return Response.ok(getOrigFile()).build();
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
                marshaller.marshal(wmsex.getExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()),
                                   webServiceWorker.getExceptionFormat()).build();
            } else {
                throw new IllegalArgumentException("this service can't return OWS Exception");
            }
        }
    }

    /**
     * Get the original image.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private File getOrigFile() throws WebServiceException {
        LOGGER.info("getOrigFile request received");
        final WebServiceWorker worker = this.webServiceWorker.get();
        worker.setLayer(getParameter(KEY_LAYER, true));
        worker.setTime(getParameter(KEY_TIME, true));
        worker.setService("WMS", "1.3.0");
        return worker.getCoverageReference().getFile();
    }

    /**
     * Return a map for the specified parameters in the query.
     *
     * @return a file containing the image requested.
     * @throws fr.geomatys.wms.WebServiceException
     */
    private File getMap() throws WebServiceException {
        LOGGER.info("getMap request received");
        verifyBaseParameter(0);

        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();

        //we set the attribute od the webservice worker with the parameters.
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        webServiceWorker.setFormat(getParameter(KEY_FORMAT, true));
        webServiceWorker.setLayer(getParameter(KEY_LAYERS, true));
        webServiceWorker.setColormapRange(getParameter(KEY_DIM_RANGE, false));

        final String crs = (getCurrentVersion().toString().equals("1.3.0")) ?
            getParameter(KEY_CRS_v130, true) : getParameter(KEY_CRS_v110, true);
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(getParameter(KEY_BBOX, true));
        webServiceWorker.setElevation(getParameter(KEY_ELEVATION, false));
        webServiceWorker.setTime(getParameter(KEY_TIME, false));
        webServiceWorker.setDimension(getParameter(KEY_WIDTH, true), getParameter(KEY_HEIGHT, true), null);
        webServiceWorker.setBackgroundColor(getParameter(KEY_BGCOLOR, false));
        webServiceWorker.setTransparency(getParameter(KEY_TRANSPARENT, false));

        return webServiceWorker.getImageFile();
    }

    /**
     * Return the value of a point in a map.
     *
     * @return text, HTML , XML or GML code.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private Response getFeatureInfo() throws WebServiceException, JAXBException {
        LOGGER.info("getFeatureInfo request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();

        verifyBaseParameter(0);
        final String currentVersion = getCurrentVersion().toString();
        webServiceWorker.setService("WMS", currentVersion);
        final String layer = getParameter(KEY_QUERY_LAYERS, true);
        webServiceWorker.setLayer(layer);

        final String crs = (currentVersion.equals("1.3.0")) ?
            getParameter(KEY_CRS_v130, true) : getParameter(KEY_CRS_v110, true);
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(getParameter(KEY_BBOX, true));
        webServiceWorker.setElevation(getParameter(KEY_ELEVATION, false));
        webServiceWorker.setTime(getParameter(KEY_TIME, false));
        webServiceWorker.setDimension(getParameter(KEY_WIDTH, true), getParameter(KEY_HEIGHT, true), null);

        final String i, j;
        if (currentVersion.equals("1.3.0")) {
            i = getParameter(KEY_I_v130, true);
            j = getParameter(KEY_J_v130, true);
        } else {
            i = getParameter(KEY_I_v110, true);
            j = getParameter(KEY_J_v110, true);
        }

        String infoFormat  = getParameter(KEY_INFO_FORMAT, false); // TODO true);
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
        //String feature_count = getParameter(KEY_FEATURE_COUNT, false);

        webServiceWorker.setExceptionFormat(getParameter(KEY_EXCEPTIONS, false));

        final double result = webServiceWorker.evaluatePixel(i,j);

        // there is many return type possible
        String response;

        // if we return html
        if (infoFormat.equals("text/html")) {
            final StringBuilder builder = new StringBuilder();
            builder.append("<html>")
                   .append("    <head>")
                   .append("        <title>GetFeatureInfo output</title>")
                   .append("    </head>")
                   .append("    <body>")
                   .append("    <table>")
                   .append("        <tr>")
                   .append("            <th>").append(layer).append("</th>")
                   .append("        </tr>")
                   .append("        <tr>")
                   .append("            <th>").append(result).append("</th>")
                   .append("       </tr>")
                   .append("    </table>")
                   .append("    </body>")
                   .append("</html>");
            response = builder.toString();
        }
        //if we return xml or gml
        else if (infoFormat.equals("text/xml") || infoFormat.equals("application/vnd.ogc.gml")) {
            final DirectPosition inputCoordinate = webServiceWorker.getCoordinates();
            final List<Double> coord = new ArrayList<Double>();
            for (Double d:inputCoordinate.getCoordinate()) {
                coord.add(d);
            }
            coord.add(result);
            final List<String> axisLabels = new ArrayList<String>();
            axisLabels.add("X");
            axisLabels.add("Y");
            axisLabels.add("RESULT");
            final DirectPositionType pos = new DirectPositionType(crs, 3, axisLabels, coord);
            final PointType pt = new PointType(layer, pos);

            //we marshall the response and return the XML String
            final StringWriter sw = new StringWriter();
            marshaller.marshal(pt, sw);
            response = sw.toString();
        }

        // HTML Response with all metadata for the
        // TODO: This is only temporary! Get metadata via CSW request instead.
        boolean getMetadata = false;
        final String getMetadataParam = getParameter(KEY_GETMETADATA, false);
        if (getMetadataParam != null && getMetadataParam.equalsIgnoreCase("TRUE")) {
                getMetadata = true;
        }
        if (infoFormat.equals("text/html") && getMetadata == true) {
            final CoverageReference coverageRef = webServiceWorker.getCoverageReference();

            // TODO:  move all of this to CoverageReference so we just need:
            // return coverageRef.getMetadataAsHTML();
            final Database database = webServiceWorker.getDatabase();
            final LayerMetadata layerMetaEntry;
            final PointOfContact pocEntry;
            try {
                final LayerMetadataTable layerMetaTable = new LayerMetadataTable(database);
                layerMetaEntry = layerMetaTable.getEntry(layer);

                final SeriesMetadataTable seriesMetaTable = new SeriesMetadataTable(database);
                final SeriesMetadata seriesMetaEntry = seriesMetaTable.getEntry(coverageRef.getSeries().toString());

                final PointOfContactTable pocTable = new PointOfContactTable(database);
                pocEntry = (seriesMetaEntry.getPointOfContactID() != null) ? pocTable.getEntry(seriesMetaEntry.getPointOfContactID()) : null;
            } catch (CatalogException ex) {
                throw new WMSWebServiceException("Could not find any matching records.",
                                              NO_APPLICABLE_CODE, getCurrentVersion());
            } catch (SQLException ex) {
                throw new WMSWebServiceException("Could not find any matching records.",
                                              NO_APPLICABLE_CODE, getCurrentVersion());
            }
            final DateRange timeRange = coverageRef.getTimeRange();
            final Date startDate = timeRange.getMinValue();
            final Date endDate = timeRange.getMaxValue();
            final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));

            final String pocName,  pocOrg,  pocEmail;
            if (pocEntry == null) {
                pocName = pocOrg = pocEmail = "---";
            } else {
                pocName = pocEntry.getName();
                pocEmail = pocEntry.getEmail();
                pocOrg = pocEntry.getOrg();
            }

            final String linkToOrig = "../postgrid-web/WS/wms?REQUEST=GetOrigFile&LAYER=" + layer + "&TIME=" + getParameter("TIME", true);
            final GeographicBoundingBox bbox = coverageRef.getGeographicBoundingBox();
            final SampleDimension firstSampleDim = coverageRef.getSampleDimensions()[0];
            final StringBuilder builder = new StringBuilder();
            builder.append("<table id=\"metadataTable\">\n")
                   .append("<tr><td width=200><b>Data Layer Title:</b></td><td>").append(layerMetaEntry.getLongTitle()).append("</td></tr>\n")
                   .append("<tr><td><b>Start Date:</b></td><td>").append(df.format(startDate)).append("</td></tr>\n")
                   .append("<tr><td><b>End Date:</b></td><td>").append(df.format(endDate)).append("</td></tr>\n")
                   .append("<tr><td><b>Data File:</b></td><td><a href=\"").append(linkToOrig).append("\">")
                   .append(coverageRef.getFile().getName()).append("</a></td></tr>\n")
                   .append("<tr><td><b></b></td><td>&nbsp;</td></tr>\n")
                   .append("<tr><td><b>Parameter Name:</b></td><td>").append(layerMetaEntry.getParameterName()).append("</td></tr>\n")
                   .append("<tr><td><b>Parameter Type:</b></td><td>").append(layerMetaEntry.getParameterType()).append("</td></tr>\n")
                   .append("<tr><td><b>Minimum Value:</b></td><td>").append(Math.round(firstSampleDim.getMinimumValue())).append("</td></tr>\n")
                   .append("<tr><td><b>Maximum Value:</b></td><td>").append(Math.round(firstSampleDim.getMaximumValue())).append("</td></tr>\n")
                   .append("<tr><td><b>Units:</b></td><td>").append(firstSampleDim.getUnits()).append("</td></tr>\n")
                   .append("<tr><td><b>Update Frequency:</b></td><td>").append(layerMetaEntry.getUpdateFrequency()).append("</td></tr>\n")
                   .append("<tr><td><b></b></td><td>&nbsp;</td></tr>\n")
                   .append("<tr><td><b>Description:</b></td><td>").append(layerMetaEntry.getDescription()).append("</td></tr>\n")
                   .append("<tr><td><b>Purpose:</b></td><td>").append(layerMetaEntry.getPurpose()).append("</td></tr>\n")
                   .append("<tr><td><b>Bounding Box:</b></td><td>&nbsp;</td></tr>\n")
                   .append("<tr><td><i>&nbsp;&nbsp;&nbsp;West</i></td><td>").append(bbox.getWestBoundLongitude()).append("</td></tr>\n")
                   .append("<tr><td><i>&nbsp;&nbsp;&nbsp;East</i></td><td>").append(bbox.getEastBoundLongitude()).append("</td></tr>\n")
                   .append("<tr><td><i>&nbsp;&nbsp;&nbsp;North</i></td><td>").append(bbox.getNorthBoundLatitude()).append("</td></tr>\n")
                   .append("<tr><td><i>&nbsp;&nbsp;&nbsp;South</i></td><td>").append(bbox.getSouthBoundLatitude()).append("</td></tr>\n")
                   .append("<tr><td><b></b></td><td>&nbsp;</td></tr>\n")
                    //.append("<tr><td><b>Spatial Reference:</b></td><td>").append(coverageRef.getCoordinateReferenceSystem().toString()).append("</td></tr>\n")
                   .append("<tr><td><b></b></td><td>&nbsp;</td></tr>\n")
                   .append("<tr><td><b>Publisher:</b></td><td>").append(pocOrg).append("</td></tr>\n")
                   .append("<tr><td><b>Contact:</b></td><td>").append(pocName).append("</td></tr>\n")
                   .append("<tr><td><b>Email:</b></td><td>").append(pocEmail).append("</td></tr>\n")
                   .append("<tr><td><b></b></td><td>&nbsp;</td></tr>\n")
                   .append("<tr><td><b>Use Constraints:</b></td><td>").append(layerMetaEntry.getUseConstraint()).append("</td></tr>\n")
                   .append("<tr><td></b></td><td>&nbsp;</td></tr>\n")
                   .append("</table>");
            response = builder.toString();
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
     * @throws org.constellation.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private Response getCapabilities() throws WebServiceException, JAXBException {
        LOGGER.info("getCapabilities request processing" + '\n');
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();

        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", true).equalsIgnoreCase("WMS")) {
            throw new WMSWebServiceException("The parameters SERVICE=WMS must be specified",
                                         MISSING_PARAMETER_VALUE, getCurrentVersion());
        }

        //and the the optional attribute
        final String inputVersion = getParameter("VERSION", false);
        if (inputVersion != null && inputVersion.equals("1.3.0")) {
            setCurrentVersion("1.3.0");
        } else {
            setCurrentVersion("1.1.1");
        }
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        String format = getParameter(KEY_FORMAT, false);
        if (format == null || !(format.equals("text/xml") || format.equals("application/vnd.ogc.wms_xml"))) {
            format = "text/xml";
        }

        final AbstractWMSCapabilities response;
        // String updateSequence = getParameter("UPDATESEQUENCE", false);

        // the service shall return WMSCapabilities marshalled
        try {
            response = (AbstractWMSCapabilities)getCapabilitiesObject();
        } catch(IOException e)   {
            throw new WMSWebServiceException("IO exception while getting Services Metadata:" + e.getMessage(),
                      INVALID_PARAMETER_VALUE, getCurrentVersion());
        }

        //we build the list of accepted crs
        final List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");  crs.add("EPSG:3395");  crs.add("EPSG:27574");
        crs.add("EPSG:27571"); crs.add("EPSG:27572"); crs.add("EPSG:27573");
        crs.add("EPSG:27574");
        //we update the url in the static part.
        response.getService().getOnlineResource().setHref(getServiceURL() + "wms");

        //we build the layers object of the document

        //we get the list of layers
        final List<AbstractLayer> layers = new ArrayList<AbstractLayer>();
        for (org.constellation.coverage.catalog.Layer inputLayer: layerList) {
            try {
                if (!inputLayer.isQueryable(Service.WMS)) {
                    LOGGER.info("layer" + inputLayer.getName() + " not queryable by WMS");
                    continue;
                }
                /*
                 *  TODO
                 * code = CRS.lookupEpsgCode(inputLayer.getCoverageReference().getCoordinateReferenceSystem(), false);
                 */
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
                        dim = new org.constellation.wms.v111.Dimension("time", "ISO8601", defaut, null);
                    else
                        dim = new org.constellation.wms.v130.Dimension("time", "ISO8601", defaut, null);

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
                        dim = new org.constellation.wms.v111.Dimension("elevation", "EPSG:5030", defaut, null);
                    else
                        dim = new org.constellation.wms.v130.Dimension("elevation", "EPSG:5030", defaut, null);
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
                    Unit<?> u = ranges[0].getUnits();
                    if (u != null)
                        unit = u.toString();

                    if (getCurrentVersion().toString().endsWith("1.1.1"))
                        dim = new org.constellation.wms.v111.Dimension("dim_range", unit, defaut, ranges[0].getMinimum() + "," + ranges[0].getMaximum());
                    else
                        dim = new org.constellation.wms.v130.Dimension("dim_range", unit, defaut, ranges[0].getMinimum() + "," + ranges[0].getMaximum());
                    dimensions.add(dim);
                }


                //we build and add a layer
                AbstractLayer outputLayer;
                if (getCurrentVersion().toString().equals("1.1.1")) {

                    /*
                     * TODO
                     * Envelope inputBox                 = inputLayer.getCoverage().getEnvelope();
                     */
                    org.constellation.wms.v111.BoundingBox outputBBox = null;
                    if(inputGeoBox != null) {
                        outputBBox = new org.constellation.wms.v111.BoundingBox("EPSG:4326",
                                                                         inputGeoBox.getWestBoundLongitude(),
                                                                         inputGeoBox.getSouthBoundLatitude(),
                                                                         inputGeoBox.getEastBoundLongitude(),
                                                                         inputGeoBox.getNorthBoundLatitude(),
                                                                         0.0, 0.0,
                                                                         getCurrentVersion().toString());
                    }

                    // we build a Style Object
                    org.constellation.wms.v111.OnlineResource or    = new org.constellation.wms.v111.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/png&LAYER=" + inputLayer.getName());
                    org.constellation.wms.v111.LegendURL legendURL1 = new org.constellation.wms.v111.LegendURL("image/png", or);

                    or = new org.constellation.wms.v111.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/gif&LAYER=" + inputLayer.getName());
                    org.constellation.wms.v111.LegendURL legendURL2 = new org.constellation.wms.v111.LegendURL("image/gif", or);
                    org.constellation.wms.v111.Style style          = new org.constellation.wms.v111.Style("Style1", "default Style", null, null, null,legendURL1,legendURL2);

                    outputLayer = new org.constellation.wms.v111.Layer(inputLayer.getName(),
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
                    org.constellation.wms.v130.BoundingBox outputBBox = null;
                    if(inputGeoBox != null) {
                        outputBBox = new org.constellation.wms.v130.BoundingBox("EPSG:4326",
                                                                         inputGeoBox.getWestBoundLongitude(),
                                                                         inputGeoBox.getSouthBoundLatitude(),
                                                                         inputGeoBox.getEastBoundLongitude(),
                                                                         inputGeoBox.getNorthBoundLatitude(),
                                                                         0.0, 0.0,
                                                                         getCurrentVersion().toString());
                    }

                    // we build a Style Object
                    org.constellation.wms.v130.OnlineResource or    = new org.constellation.wms.v130.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/png&LAYER=" + inputLayer.getName());
                    org.constellation.wms.v130.LegendURL legendURL1 = new org.constellation.wms.v130.LegendURL("image/png", or);

                    or = new org.constellation.wms.v130.OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/gif&LAYER=" + inputLayer.getName());
                    org.constellation.wms.v130.LegendURL legendURL2 = new org.constellation.wms.v130.LegendURL("image/gif", or);
                    org.constellation.wms.v130.Style style          = new org.constellation.wms.v130.Style("Style1", "default Style", null, null, null,legendURL1,legendURL2);


                    outputLayer = new org.constellation.wms.v130.Layer(inputLayer.getName(),
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

        //we build the general layer and add it to the document
        AbstractLayer mainLayer;
        if (getCurrentVersion().toString().equals("1.1.1")) {
            mainLayer = new org.constellation.wms.v111.Layer("Constellation Web Map Layer",
                                                      "description of the service(need to be fill)",
                                                       crs,
                                                       null,
                                                       layers);
        // version 1.3.0
        } else {
            mainLayer = new org.constellation.wms.v130.Layer("Constellation Web Map Layer",
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
        } else {
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", "");
        }

        marshaller.marshal(response, sw);

        return Response.ok(sw.toString(), format).build();

    }

    /**
     *
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private String describeLayer() throws WebServiceException, JAXBException {
        LOGGER.info("describeLayer request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        verifyBaseParameter(2);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());

        final OnlineResourceType or = new OnlineResourceType(getServiceURL() + "wcs?");
        final List<LayerDescriptionType> layersDescriptions = new ArrayList<LayerDescriptionType>();
        final String layers = getParameter(KEY_LAYERS, true);
        final Set<String> registredLayers = webServiceWorker.getLayerNames();
        final StringTokenizer tokens = new StringTokenizer(layers, ",");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            if (registredLayers.contains(token)) {
                final TypeNameType t = new TypeNameType(token);
                final LayerDescriptionType outputLayer = new LayerDescriptionType(or,t);
                layersDescriptions.add(outputLayer);
            } else {
                throw new WMSWebServiceException("This layer is not registred: " + token,
                      INVALID_PARAMETER_VALUE, getCurrentVersion());
            }
        }

        final DescribeLayerResponseType response = new DescribeLayerResponseType(
                getSldVersion().toString(), layersDescriptions);

        //we marshall the response and return the XML String
        final StringWriter sw = new StringWriter();
        marshaller.marshal(response, sw);
        return sw.toString();
    }

    /**
     * Return the legend graphic for the current layer.
     * 
     * @return a file containing the legend graphic image.
     * @throws org.constellation.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private File getLegendGraphic() throws WebServiceException, JAXBException {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();

        verifyBaseParameter(2);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        webServiceWorker.setLayer(getParameter(KEY_LAYER, true));
        webServiceWorker.setFormat(getParameter(KEY_FORMAT, false));
        webServiceWorker.setElevation(getParameter(KEY_ELEVATION, false));
        webServiceWorker.setColormapRange(getParameter(KEY_DIM_RANGE, false));
        webServiceWorker.setDimension(getParameter(KEY_WIDTH, false), getParameter(KEY_HEIGHT, false), null);

        /*String style = getParameter(KEY_STYLE, false);

        String featureType   = getParameter(KEY_FEATURETYPE, false);
        String remoteSld     = getParameter(KEY_SLD, false);
        String remoteOwsType = getParameter(KEY_REMOTE_OWS_TYPE, false);
        String remoteOwsUrl  = getParameter(KEY_REMOTE_OWS_URL, false);
        String coverage      = getParameter(KEY_COVERAGE, false);
        String rule          = getParameter(KEY_RULE, false);
        String scale         = getParameter(KEY_SCALE, false);

        StyledLayerDescriptor sld = (StyledLayerDescriptor) getComplexParameter(KEY_SLD_BODY, false);*/

        return  webServiceWorker.getLegendFile();

    }
}
