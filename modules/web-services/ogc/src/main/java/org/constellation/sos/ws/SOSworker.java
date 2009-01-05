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
package org.constellation.sos.ws;

// JDK dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.OGCWebService;
import org.constellation.gml.v311.AbstractTimeGeometricPrimitiveType;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.FeaturePropertyType;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.gml.v311.TimeIndeterminateValueType;
import org.constellation.gml.v311.TimePositionType;
import org.constellation.gml.v311.TimeInstantType;
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.observation.MeasurementEntry;
import org.constellation.observation.ObservationCollectionEntry;
import org.constellation.observation.ObservationEntry;
import org.constellation.observation.ProcessEntry;
import org.constellation.ogc.BinaryTemporalOpType;
import org.constellation.ogc.LiteralType;
import org.constellation.ows.v110.AcceptFormatsType;
import org.constellation.ows.v110.AcceptVersionsType;
import org.constellation.ows.v110.Operation;
import org.constellation.ows.v110.OperationsMetadata;
import org.constellation.ows.v110.RangeType;
import org.constellation.ows.v110.SectionsType;
import org.constellation.ows.v110.ServiceIdentification;
import org.constellation.ows.v110.ServiceProvider;
import org.constellation.sampling.SamplingFeatureEntry;
import org.constellation.sampling.SamplingPointEntry;
import org.constellation.sml.AbstractSensorML;
import org.constellation.sos.Capabilities;
import org.constellation.sos.Contents;
import org.constellation.sos.Contents.ObservationOfferingList;
import org.constellation.sos.DescribeSensor;
import org.constellation.sos.EventTime;
import org.constellation.sos.GetCapabilities;
import org.constellation.sos.GetObservation;
import org.constellation.sos.GetResult;
import org.constellation.sos.GetResultResponse;
import org.constellation.sos.InsertObservation;
import org.constellation.sos.InsertObservationResponse;
import org.constellation.sos.RegisterSensor;
import org.constellation.sos.RegisterSensorResponse;
import org.constellation.sos.RequestBaseType;
import org.constellation.sos.FilterCapabilities;
import org.constellation.sos.ObservationOfferingEntry;
import org.constellation.sos.ObservationTemplate;
import org.constellation.sos.OfferingPhenomenonEntry;
import org.constellation.sos.OfferingProcedureEntry;
import org.constellation.sos.OfferingSamplingFeatureEntry;
import org.constellation.sos.ResponseModeType;
import org.constellation.sos.factory.AbstractSOSFactory;
import org.constellation.sos.io.DataSourceType;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.swe.v101.AbstractEncodingEntry;
import org.constellation.swe.v101.AbstractEncodingPropertyType;
import org.constellation.swe.v101.AnyResultEntry;
import org.constellation.swe.v101.DataArrayEntry;
import org.constellation.swe.v101.DataArrayPropertyType;
import org.constellation.swe.v101.DataComponentPropertyType;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.swe.v101.CompositePhenomenonEntry;
import org.constellation.swe.v101.PhenomenonPropertyType;
import org.constellation.swe.v101.TextBlockEntry;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistry;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.sos.ResponseModeType.*;
import static org.constellation.sos.ws.SensorMLUtils.*;

// GeoAPI dependencies
import org.opengis.observation.Observation;

// postgres driver
import org.postgresql.ds.PGSimpleDataSource;

/**
 *
 * @author Guilhem Legal.
 */
public class SOSworker {

    public final static int DISCOVERY     = 0;
    public final static int TRANSACTIONAL = 1;
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("org.constellation.sos.ws");
    
    /**
     * A list of temporary ObservationTemplate
     */
    private Map<String, ObservationEntry> templates = new HashMap<String, ObservationEntry>();
    
    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */ 
    private final Properties map;
    
    /**
     * The base for sensor id.
     */ 
    private final String sensorIdBase;
    
    /**
     * The base for observation id.
     */ 
    private final String observationTemplateIdBase;
    
    /**
     * The base for offering id.
     */ 
    private final String offeringIdBase = "offering-";
    
    /**
     * The base for phenomenon id.
     */ 
    private final String phenomenonIdBase = "urn:ogc:def:phenomenon:BRGM:";
    
    /**
     * The valid time for a getObservation template (in ms).
     */
    private final long templateValidTime;
    
    /**
     * The maximum of observation return in a getObservation request.
     */
    private final int maxObservationByRequest;
    
    /**
     * A capabilities object containing the static part of the document.
     */
    private Capabilities staticCapabilities;
    
    /**
     * The service url.
     */
    private String serviceURL;
    
     /**
     * The current MIME type of return
     */
    private String outputFormat;
    
    /**
     * A list of schreduled Task (used in clos method).
     */
    private List<Timer> schreduledTask = new ArrayList<Timer>();
    
    /**
     * A list of supported MIME type 
     */
    private final static List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList("text/xml", "application/xml", "text/plain");
    }
    
    /**
     * The profile of the SOS service (transational/discovery). 
     */
    private final int profile;
    
    /**
     * A date formater used to parse datablock.
     */
    private DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    /**
     * The Observation database reader
     */
    private final ObservationReader OMReader;
    
    /**
     * The Observation database writer
     */
    private final ObservationWriter OMWriter;

    /**
     * The observation filter
     */
    private final ObservationFilter OMFilter;
    
    /**
     * The sensorML database reader
     */
    private final SensorReader SMLReader;
    
    /**
     * The sensorML database writer
     */
    private final SensorWriter SMLWriter;

    private final QName observation_QNAME = new QName("http://www.opengis.net/om/1.0", "Observation", "om");

    private static FactoryRegistry factory = new FactoryRegistry(AbstractSOSFactory.class);
    
    /**
     * Initialize the database connection.
     */
    public SOSworker(int profile) throws WebServiceException {
        this.profile = profile;
        if (profile != TRANSACTIONAL && profile != DISCOVERY) {
            throw new IllegalArgumentException("the flag profile must be equals to TRANSACTIONAL or DISCOVERY!");
        }
        
        //we load the properties files
        Properties prop = new Properties();
        map   = new Properties();
        File f = null;
        File env = getSicadeDirectory();
        logger.info("path to config file=" + env);
        boolean start = true;
        try {
            // we get the configuration file
            f = new File(env, "/sos_configuration/config.properties");
            FileInputStream in = new FileInputStream(f);
            prop.load(in);
            in.close();
            
            // the file who record the map between phisycal ID and DB ID.
            f = new File(env, "/sos_configuration/mapping.properties");
            in = new FileInputStream(f);
            map.load(in);
            in.close();
            
        } catch (FileNotFoundException e) {
            if (f != null) {
                logger.severe(f.getPath());
            }
            logger.severe("The SOS service is not working!"                       + '\n' + 
                          "cause: The service can not load the properties files!" + '\n' + 
                          "cause: " + e.getMessage());
            start = false;
        }  catch (IOException e) {
            if (f != null) {
                logger.severe(f.getPath());
            }
            logger.severe("The SOS service is not working(IOException)!"          + '\n' +
                          "cause: The service can not load the properties files!" + '\n' +
                          "cause: " + e.getMessage());
            start = false;
        }

        //we get the Sensor reader type
        DataSourceType SMLType = DataSourceType.valueOf(prop.getProperty("SMLDataSourceType"));
        if (SMLType == null)
            SMLType = DataSourceType.MDWEB;

        Connection SMLconnection        = null;
        PGSimpleDataSource dataSourceOM = null;
        Connection OMConnection         = null;
        File dataDirectory              = null;
        try {
            if (SMLType == DataSourceType.MDWEB) {
                //we create a connection to the sensorML database
                PGSimpleDataSource dataSourceSML = new PGSimpleDataSource();
                dataSourceSML.setServerName(prop.getProperty("SMLDBServerName"));
                dataSourceSML.setPortNumber(Integer.parseInt(prop.getProperty("SMLDBServerPort")));
                dataSourceSML.setDatabaseName(prop.getProperty("SMLDBName"));
                dataSourceSML.setUser(prop.getProperty("SMLDBUser"));
                dataSourceSML.setPassword(prop.getProperty("SMLDBUserPassword"));
                SMLconnection = dataSourceSML.getConnection();
            } else if (SMLType == DataSourceType.FILE_SYSTEM) {
                String path = prop.getProperty("SMLDataDirectory");
                if (path != null) {
                    dataDirectory = new File(path);
                } else {
                    dataDirectory = new File(env, "sensors");
                    if (!dataDirectory.exists())
                        dataDirectory.mkdir();
                }
            }

            //we create a connection to the O&M database
            dataSourceOM = new PGSimpleDataSource();
            dataSourceOM.setServerName(prop.getProperty("OMDBServerName"));
            dataSourceOM.setPortNumber(Integer.parseInt(prop.getProperty("OMDBServerPort")));
            dataSourceOM.setDatabaseName(prop.getProperty("OMDBName"));
            dataSourceOM.setUser(prop.getProperty("OMDBUser"));
            dataSourceOM.setPassword(prop.getProperty("OMDBUserPassword"));
            OMConnection = dataSourceOM.getConnection();

        } catch (SQLException ex) {
            logger.severe("The SOS service is not running!" + '\n' + "cause: SQLException:" + ex.getMessage());
            start = false;
        }

        // we load the factory from the available classes
        AbstractSOSFactory SOSfactory = null;
        try {
            SOSfactory = factory.getServiceProvider(AbstractSOSFactory.class, null, null, null);
        } catch (FactoryNotFoundException ex) {
            logger.severe("The SOS service is not working!" + '\n' + "cause: Unable to find a SOS Factory");
            start = false;
        }

        if (start) {

            //we initailize the properties attribute 
            String observationIdBase  = prop.getProperty("observationIdBase");
            sensorIdBase              = prop.getProperty("sensorIdBase");
            observationTemplateIdBase = prop.getProperty("observationTemplateIdBase");
            maxObservationByRequest   = Integer.parseInt(prop.getProperty("maxObservationByRequest"));
            String validTime          = prop.getProperty("templateValidTime");
            int h                     = Integer.parseInt(validTime.substring(0, validTime.indexOf(':')));
            int m                     = Integer.parseInt(validTime.substring(validTime.indexOf(':') + 1));
            templateValidTime         = (h * 3600000) + (m * 60000);

            SMLReader = SOSfactory.getSensorReader(SMLType, dataDirectory, sensorIdBase, SMLconnection, map);
            SMLWriter = SOSfactory.getSensorWriter(SMLType, dataDirectory, SMLconnection, sensorIdBase);
            OMReader  = SOSfactory.getObservationReader(dataSourceOM, observationIdBase);
            OMWriter  = SOSfactory.getObservationWriter(dataSourceOM);
            OMFilter  = SOSfactory.getObservationFilter(observationIdBase, observationTemplateIdBase, map, OMConnection);

            logger.info("SOS service running");
            
        } else {
            sensorIdBase              = null;
            observationTemplateIdBase = null;
            OMReader                  = null;
            OMWriter                  = null;
            OMFilter                  = null;
            SMLReader                 = null;
            SMLWriter                 = null;
            maxObservationByRequest   = -1;
            templateValidTime         = -1;
            
        }
    }
    
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     */
    public Capabilities getCapabilities(GetCapabilities requestCapabilities) throws WebServiceException {
        logger.info("getCapabilities request processing" + '\n');
        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals("SOS")) {
                throw new WebServiceException("service must be \"SOS\"!",
                                                 INVALID_PARAMETER_VALUE, "service");
            }
        } else {
            throw new WebServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service");
        }
        AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("1.0.0")){
                 throw new WebServiceException("version available : 1.0.0",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }
        AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 ) {
            boolean found = false;
            for (String form: formats.getOutputFormat()) {
                if (ACCEPTED_OUTPUT_FORMATS.contains(form)) {
                    outputFormat = form;
                    found = true;
                }
            }
            if (!found) {
                throw new WebServiceException("accepted format : text/xml, application/xml",
                                                 INVALID_PARAMETER_VALUE, "acceptFormats");
            }
            
        } else {
            this.outputFormat = "application/xml";
        }
        
        //we prepare the response document
        Capabilities c           = null; 
        ServiceIdentification si = null;
        ServiceProvider       sp = null;
        OperationsMetadata    om = null;
        FilterCapabilities    fc = null;
        Contents            cont = null;

        SectionsType sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType(SectionsType.getExistingSections("1.1.1"));
        }
        //we enter the information for service identification.
        if (sections.getSection().contains("ServiceIdentification") || sections.getSection().contains("All")) {

            si = staticCapabilities.getServiceIdentification();
        }

        //we enter the information for service provider.
        if (sections.getSection().contains("ServiceProvider") || sections.getSection().contains("All")) {

            sp = staticCapabilities.getServiceProvider();
        }

        //we enter the operation Metadata
        if (sections.getSection().contains("OperationsMetadata") || sections.getSection().contains("All")) {

           om = staticCapabilities.getOperationsMetadata();

           //we remove the operation not supported in this profile (transactional/discovery)
           if (profile == DISCOVERY) {
               om.removeOperation("InsertObservation");
               om.removeOperation("RegisterSensor");
           }


           //we update the URL
           OGCWebService.updateOWSURL(om.getOperation(), serviceURL, "SOS");


           //we update the parameter in operation metadata.
           Operation go = om.getOperation("GetObservation");

           // the list of offering names
           go.updateParameter("offering", OMReader.getOfferingNames());

           // the event time range
           RangeType range = new RangeType(OMReader.getMinimalEventTime(), "now");
           go.updateParameter("eventTime", range);

           //the process list
           Set<String> procNames  = OMReader.getProcedureNames();
           go.updateParameter("procedure", procNames);

           //the phenomenon list
           go.updateParameter("observedProperty", OMReader.getPhenomenonNames());

           //the feature of interest list
           go.updateParameter("featureOfInterest", OMReader.getFeatureOfInterestNames());

           Operation ds = om.getOperation("DescribeSensor");
           ds.updateParameter("procedure", procNames);

        }

        //we enter the information filter capablities.
        if (sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains("All")) {

            fc = staticCapabilities.getFilterCapabilities();
        }


        if (sections.getSection().contains("Contents") || sections.getSection().contains("All")) {
            // we add the list of observation ofeerings 
            ObservationOfferingList ool = new ObservationOfferingList(OMReader.getObservationOfferings());
            cont = new Contents(ool);
        }
        c = new Capabilities(si, sp, om, "1.0.0", null, fc, cont);

        return normalizeDocument(c);
    }
    
    /**
     * Web service operation whitch return an sml description of the specified sensor.
     * 
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     */
    public AbstractSensorML describeSensor(DescribeSensor requestDescSensor) throws WebServiceException  {
        logger.info("DescribeSensor request processing"  + '\n');
        
        // we get the form
        verifyBaseRequest(requestDescSensor);

        //we verify that the output format is good.     
        if (requestDescSensor.getOutputFormat() != null) {
            if (!requestDescSensor.getOutputFormat().equalsIgnoreCase("text/xml;subtype=\"SensorML/1.0.0\"")) {
                throw new WebServiceException("only text/xml;subtype=\"SensorML/1.0.0\" is accepted for outputFormat",
                        INVALID_PARAMETER_VALUE, "outputFormat");
            }
        } else {
            throw new WebServiceException("output format text/xml;subtype=\"SensorML/1.0.0\" must be specify",
                                             MISSING_PARAMETER_VALUE, "outputFormat");
        }
        //we transform the form into an XML string
        if (requestDescSensor.getProcedure() == null) {
            throw new WebServiceException("You must specify the sensor ID!",
                                         MISSING_PARAMETER_VALUE, "procedure");
        }
        String sensorId = requestDescSensor.getProcedure();
        logger.info("sensorId received: " + sensorId);
        return SMLReader.getSensor(sensorId);
    }
    
    /**
     * Web service operation whitch respond a collection of observation satisfying 
     * the restriction specified in the query.
     * 
     * @param requestObservation a document specifying the parameter of the request.
     */
    public ObservationCollectionEntry getObservation(GetObservation requestObservation) throws WebServiceException {
        logger.info("getObservation request processing"  + '\n');
        //we verify the base request attribute
        verifyBaseRequest(requestObservation);

        //we verify that the output format is good.     
        if (requestObservation.getResponseFormat() != null) {
            if (!requestObservation.getResponseFormat().equalsIgnoreCase("text/xml; subtype=\"om/1.0.0\"")) {
                throw new WebServiceException("only text/xml; subtype=\"om/1.0.0\" is accepted for responseFormat",
                        INVALID_PARAMETER_VALUE, "responseFormat");
            }
        } else {
            throw new WebServiceException("Response format text/xml;subtype=\"om/1.0.0\" must be specify",
                    MISSING_PARAMETER_VALUE, "responseFormat");
        }
        
        //we get the mode of result
        ObservationCollectionEntry response = new ObservationCollectionEntry();

        boolean template  = false;
        ResponseModeType mode;
        if (requestObservation.getResponseMode() == null) {
            mode = INLINE;
        } else {
            try {
                mode = ResponseModeType.fromValue(requestObservation.getResponseMode());
            } catch (IllegalArgumentException e) {
                throw new WebServiceException(" the response Mode: " + requestObservation.getResponseMode() + " is not supported by the service (inline or template available)!",
                                                 INVALID_PARAMETER_VALUE, "responseMode");
            }
        }
        OMFilter.initFilterObservation(mode);

        if (mode == RESULT_TEMPLATE) {
            template = true;
        } else if (mode != INLINE) {
            throw new WebServiceException("This response Mode is not supported by the service (inline or template available)!",
                                             OPERATION_NOT_SUPPORTED, "responseMode");
        }

        ObservationOfferingEntry off;
        //we verify that there is an offering
        if (requestObservation.getOffering() == null) {
            throw new WebServiceException("Offering must be specify!",
                                             MISSING_PARAMETER_VALUE, "offering");
        } else {
            off = OMReader.getObservationOffering(requestObservation.getOffering());
            if (off == null) {
                throw new WebServiceException("This offering is not registered in the service",
                                              INVALID_PARAMETER_VALUE, "offering");
            }
        }

        //we verify that the srsName (if there is one) is advertised in the offering
        if (requestObservation.getSrsName() != null) {
            if (!off.getSrsName().contains(requestObservation.getSrsName())) {
                throw new WebServiceException("This srs name is not advertised in the offering",
                                                INVALID_PARAMETER_VALUE, "srsName");
            }
        }

        //we verify that the resultModel (if there is one) is advertised in the offering
        if (requestObservation.getResultModel() != null) {
            if (!off.getResultModel().contains(requestObservation.getResultModel())) {
                throw new WebServiceException("This result model is not advertised in the offering",
                                                INVALID_PARAMETER_VALUE, "resultModel");
            }
        }

        //we get the list of process
        List<String> procedures = requestObservation.getProcedure();
        for (String s : procedures) {
            if (s != null) {
                String dbId = map.getProperty(s);
                if (dbId == null) {
                    dbId = s;
                }
                logger.info("process ID: " + dbId);
                ReferenceEntry proc = getReferenceFromHRef(dbId);
                if (proc == null) {
                    throw new WebServiceException(" this process is not registred in the table",
                            INVALID_PARAMETER_VALUE, "procedure");
                }
                if (!off.getProcedure().contains(proc)) {
                    throw new WebServiceException(" this process is not registred in the offering",
                            INVALID_PARAMETER_VALUE, "procedure");
                }
            } else {
                //if there is only one proccess null we return error (we'll see)
                if (procedures.size() == 1) {
                    throw new WebServiceException("the process is null",
                            INVALID_PARAMETER_VALUE, "procedure");
                }
            }
        }
        OMFilter.setProcedure(procedures, off);

        //we get the list of phenomenon
        //TODO verifier que les pheno appartiennent a l'offering
        List<String> observedProperties = requestObservation.getObservedProperty();
        if (observedProperties.size() != 0 ) {
            List<String> singlePhenomenons    = new ArrayList<String>();
            List<String> compositePhenomenons = new ArrayList<String>();
            for (String phenomenonName : observedProperties) {
                if (phenomenonName.indexOf(phenomenonIdBase) != -1) {
                    phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
                }
                PhenomenonEntry phen = OMReader.getPhenomenon(phenomenonName);
                if (phen == null) {
                    throw new WebServiceException(" this phenomenon " + phenomenonName + " is not registred in the database!",
                            INVALID_PARAMETER_VALUE, "observedProperty");
                }
                if (phen instanceof CompositePhenomenonEntry) {
                    singlePhenomenons.add(phenomenonName);

                } else if (phen instanceof PhenomenonEntry) {
                    compositePhenomenons.add(phenomenonName);
                }
            }
            OMFilter.setObservedProperties(singlePhenomenons, compositePhenomenons);
        }


        //we treat the time restriction
        List<EventTime> times = requestObservation.getEventTime();
        AbstractTimeGeometricPrimitiveType templateTime = treatEventTimeRequest(times, template);

        //we treat the restriction on the feature of interest
        if (requestObservation.getFeatureOfInterest() != null) {
            GetObservation.FeatureOfInterest foiRequest = requestObservation.getFeatureOfInterest();

            // if the request is a list of station
            if (!foiRequest.getObjectID().isEmpty()) {
                
                for (final String samplingFeatureName : foiRequest.getObjectID()) {
                    //verify that the station is registred in the DB.
                    SamplingFeatureEntry foi = OMReader.getFeatureOfInterest(samplingFeatureName);
                    if (foi == null)
                        throw new WebServiceException("the feature of interest is not registered",
                                                         INVALID_PARAMETER_VALUE, "featureOfInterest");
                }
                OMFilter.setFeatureOfInterest(foiRequest.getObjectID());

            // if the request is a spatial operator
            } else {
                // for a BBOX Spatial ops
                if (foiRequest.getBBOX() != null) {

                    if (foiRequest.getBBOX().getEnvelope() != null &&
                        foiRequest.getBBOX().getEnvelope().getLowerCorner().getValue().size() == 2 &&
                        foiRequest.getBBOX().getEnvelope().getUpperCorner().getValue().size() == 2) {
                        
                        EnvelopeEntry e = foiRequest.getBBOX().getEnvelope();
                        boolean add     = false;
                        List<String> matchingFeatureOfInterest = new ArrayList<String>();
                        for (ReferenceEntry refStation : off.getFeatureOfInterest()) {
                            SamplingPointEntry station = (SamplingPointEntry) OMReader.getFeatureOfInterest(refStation.getHref());
                            if (station == null)
                                throw new WebServiceException("the feature of interest is not registered",
                                        INVALID_PARAMETER_VALUE);
                            if (station instanceof SamplingPointEntry) {
                                SamplingPointEntry sp = (SamplingPointEntry) station;
                                if (sp.getPosition().getPos().getValue().get(0) > e.getUpperCorner().getValue().get(0) &&
                                        sp.getPosition().getPos().getValue().get(0) < e.getLowerCorner().getValue().get(0) &&
                                        sp.getPosition().getPos().getValue().get(1) > e.getUpperCorner().getValue().get(1) &&
                                        sp.getPosition().getPos().getValue().get(1) < e.getLowerCorner().getValue().get(1)) {

                                    matchingFeatureOfInterest.add(sp.getId());
                                    add = true;
                                } else {
                                    logger.info(" the feature of interest " + sp.getId() + " is not in the BBOX");
                                }
                            }
                        }
                        if (add)
                            OMFilter.setFeatureOfInterest(matchingFeatureOfInterest);
                        
                    } else {
                        throw new WebServiceException("the envelope is not build correctly",
                                                     INVALID_PARAMETER_VALUE);
                    }
                } else {
                    throw new WebServiceException("This operation is not take in charge by the Web Service",
                                                 OPERATION_NOT_SUPPORTED);
                }
            }

        }

        //TODO we treat the restriction on the result
        if (requestObservation.getResult() != null) {
            GetObservation.Result result = requestObservation.getResult();

            //we treat the different operation
            if (result.getPropertyIsLessThan() != null) {

                String propertyName  = result.getPropertyIsLessThan().getPropertyName();
                LiteralType literal  = result.getPropertyIsLessThan().getLiteral() ;
                if (literal == null || propertyName == null || propertyName.equals("")) {
                    throw new WebServiceException(" to use the operation Less Than you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "lessThan");
                }


            } else if (result.getPropertyIsGreaterThan() != null) {

                String propertyName  = result.getPropertyIsGreaterThan().getPropertyName();
                LiteralType literal  = result.getPropertyIsGreaterThan().getLiteral();
                if (propertyName == null || propertyName.equals("") || literal == null) {
                    throw new WebServiceException(" to use the operation Greater Than you must specify the propertyName and the litteral",
                                                 MISSING_PARAMETER_VALUE, "greaterThan");
                }

            } else if (result.getPropertyIsEqualTo() != null) {

                String propertyName  = result.getPropertyIsEqualTo().getPropertyName();
                LiteralType literal  = result.getPropertyIsEqualTo().getLiteral();
                if (propertyName == null || propertyName.equals("") || literal == null) {
                     throw new WebServiceException(" to use the operation Equal you must specify the propertyName and the litteral",
                                                   MISSING_PARAMETER_VALUE, "propertyIsEqualTo");
                }


            } else if (result.getPropertyIsLike() != null) {
                throw new WebServiceException("This operation is not take in charge by the Web Service",
                                              OPERATION_NOT_SUPPORTED, "propertyIsLike");

            } else if (result.getPropertyIsBetween() != null) {

                logger.info("PROP IS BETWEEN");
                if (result.getPropertyIsBetween().getPropertyName() == null) {
                    throw new WebServiceException("To use the operation Between you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "propertyIsBetween");
                }
                String propertyName  = result.getPropertyIsBetween().getPropertyName();

                LiteralType LowerLiteral  = result.getPropertyIsBetween().getLowerBoundary().getLiteral();
                LiteralType UpperLiteral  = result.getPropertyIsBetween().getUpperBoundary().getLiteral();

                if (propertyName == null || propertyName.equals("") || LowerLiteral == null || UpperLiteral == null) {
                        throw new WebServiceException("This property name, lower and upper literal must be specify",
                                                      INVALID_PARAMETER_VALUE, "result");

                }

            } else {
                throw new WebServiceException("This operation is not take in charge by the Web Service",
                                              OPERATION_NOT_SUPPORTED);
            }
        }

        List<String> observationIDs = OMFilter.filterObservation();
        for (String observationID : observationIDs) {
            ObservationEntry o = OMReader.getObservation(observationID);
            if (template) {

                String temporaryTemplateId = o.getName() + '-' + getTemplateSuffix(o.getName());
                ObservationEntry temporaryTemplate = o.getTemporaryTemplate(temporaryTemplateId, templateTime);
                templates.put(temporaryTemplateId, temporaryTemplate);

                // we launch a timer which will destroy the template in one hours
                Timer t = new Timer();
                //we get the date and time for now
                Date d = new Date(System.currentTimeMillis() + templateValidTime);
                logger.info("this template will be destroyed at:" + d.toString());
                t.schedule(new DestroyTemplateTask(temporaryTemplateId), d);
                schreduledTask.add(t);

                response.add(temporaryTemplate);
            } else {
                response.add(o);

                //we stop the request if its too big
                if (response.getMember().size() > maxObservationByRequest) {
                    throw new WebServiceException("Your request is to voluminous please add filter and try again",
                                                  NO_APPLICABLE_CODE);
                }
            }
        }
        return normalizeDocument(response);
    }
    
    /**
     * Web service operation
     */
    public GetResultResponse getResult(GetResult requestResult) throws WebServiceException {
        logger.info("getResult request processing"  + '\n');
        long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        verifyBaseRequest(requestResult);
        
        ObservationEntry template = null;
        if (requestResult.getObservationTemplateId() != null) {
            String id = requestResult.getObservationTemplateId();
            template = templates.get(id);
            if (template == null) {
                throw new WebServiceException("this template does not exist or is no longer usable",
                                              INVALID_PARAMETER_VALUE, "ObservationTemplateId");
            }
        } else {
            throw new WebServiceException("ObservationTemplateID must be specified",
                                          MISSING_PARAMETER_VALUE, "ObservationTemplateId");
        }
        
        //we begin to create the sql request
        OMFilter.initFilterGetResult(((ProcessEntry)template.getProcedure()).getHref());
        
        //we treat the time constraint
        List<EventTime> times = requestResult.getEventTime();
        
        /**
         * The template time :
         */ 
        
        // case TEquals with time instant
        if (template.getSamplingTime() instanceof TimeInstantType) {
           TimeInstantType ti = (TimeInstantType) template.getSamplingTime();
           BinaryTemporalOpType equals  = new BinaryTemporalOpType(ti);
           EventTime e                  = new EventTime(equals);
           times.add(e);
        
        } else if (template.getSamplingTime() instanceof TimePeriodType) {
            TimePeriodType tp = (TimePeriodType) template.getSamplingTime();
            
            //case TBefore
            if (tp.getBeginPosition().equals(new TimePositionType(TimeIndeterminateValueType.BEFORE))) {
                BinaryTemporalOpType before  = new BinaryTemporalOpType(new TimeInstantType(tp.getEndPosition()));
                EventTime e                  = new EventTime(null, before, null);
                times.add(e);
            
            //case TAfter    
            } else if (tp.getEndPosition().equals(new TimePositionType(TimeIndeterminateValueType.NOW))) {
                BinaryTemporalOpType after  = new BinaryTemporalOpType(new TimeInstantType(tp.getBeginPosition()));
                EventTime e                  = new EventTime(after, null, null);
                times.add(e);
            
            //case TDuring/TEquals  (here the sense of T_Equals with timePeriod is lost but not very usefull) 
            } else {
                BinaryTemporalOpType during  = new BinaryTemporalOpType(tp);
                EventTime e                  = new EventTime(null, null, during);
                times.add(e);
            }
        }
        
        //we treat the time constraint
        treatEventTimeRequest(times, false);
        
        //we prepare the response document
        List<ObservationFilter.ObservationResult> results = OMFilter.filterResult();
        StringBuilder datablock = new StringBuilder();
        for (ObservationFilter.ObservationResult result: results) {
            Timestamp tBegin = result.beginTime;
            Timestamp tEnd   = result.endTime;
            AnyResultEntry a = OMReader.getResult(result.resultID);
            if (a != null) {
                DataArrayEntry array = a.getArray();
                if (array != null) {
                    String values = getResultValues(tBegin, tEnd, array, times);
                    datablock.append(values).append('\n');
                } else {
                    throw new IllegalArgumentException("Array is null");
                }
            }
        }
        GetResultResponse.Result r = new GetResultResponse.Result(datablock.toString(), serviceURL + '/' + requestResult.getObservationTemplateId());
        GetResultResponse response = new GetResultResponse(r);
        logger.info("GetResult request executed in " + (System.currentTimeMillis() - start) + "ms");
        return response;
    }
    
    private String getResultValues(Timestamp tBegin, Timestamp tEnd, DataArrayEntry array, List<EventTime> eventTimes) throws WebServiceException {
        String values = null;
        
        //for multiple observations we parse the brut values (if we got a time constraint)
        if (tBegin != null && tEnd != null) {

            values = array.getValues();
            
            for (EventTime bound: eventTimes) {
                logger.finer(" Values: " + values);
                if (bound.getTEquals() != null) {
                    if (bound.getTEquals().getRest().get(0) instanceof TimeInstantType) {
                        TimeInstantType ti = (TimeInstantType) bound.getTEquals().getRest().get(0);
                        Timestamp boundEquals = Timestamp.valueOf(getTimeValue(ti.getTimePosition()));
                        
                        logger.finer("TE case 1");
                        //case 1 the periods contains a matching values
                        values = parseDataBlock(values, array.getEncoding(), null, null, boundEquals);
                        
                    }
                    
                } else if (bound.getTAfter()  != null) {
                    TimeInstantType ti = (TimeInstantType) bound.getTAfter().getRest().get(0);
                    Timestamp boundBegin = Timestamp.valueOf(getTimeValue(ti.getTimePosition()));
                    
                    // case 1 the period overlaps the bound 
                    if (tBegin.before(boundBegin) && tEnd.after(boundBegin)) {
                        logger.finer("TA case 1");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, null, null);
                    
                    }
                        
                } else if (bound.getTBefore() != null) {
                    TimeInstantType ti = (TimeInstantType) bound.getTBefore().getRest().get(0);
                    Timestamp boundEnd = Timestamp.valueOf(getTimeValue(ti.getTimePosition()));
                    
                    // case 1 the period overlaps the bound 
                    if (tBegin.before(boundEnd) && tEnd.after(boundEnd)) {
                        logger.finer("TB case 1");
                        values = parseDataBlock(values, array.getEncoding(), null, boundEnd, null);
                    
                    }
                    
                } else if (bound.getTDuring() != null) {
                    
                    TimePeriodType tp = (TimePeriodType) bound.getTDuring().getRest().get(0);
                    Timestamp boundBegin = Timestamp.valueOf(getTimeValue(tp.getBeginPosition()));
                    Timestamp boundEnd   = Timestamp.valueOf(getTimeValue(tp.getEndPosition()));
                    
                    // case 1 the period overlaps the first bound 
                    if (tBegin.before(boundBegin) && tEnd.before(boundEnd) && tEnd.after(boundBegin)) {
                        logger.finer("TD case 1");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, boundEnd, null);

                    // case 2 the period overlaps the second bound    
                    } else if (tBegin.after(boundBegin) && tEnd.after(boundEnd) && tBegin.before(boundEnd)) {
                        logger.finer("TD case 2");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, boundEnd, null);

                    // case 3 the period totaly overlaps the bounds
                    } else if (tBegin.before(boundBegin) && tEnd.after(boundEnd)) {
                        logger.finer("TD case 3");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, boundEnd, null);
                    } 
                    
                } 
            }
            
                    
        //if this is a simple observation, or if there is no time bound    
        } else {
            values = array.getValues();
        }
        return values;
    }
    
    /**
     * Parse a data block and return only the values matching the time filter.
     *
     * @param brutValues The data block.
     * @param abstractEncoding The encoding of the data block.
     * @param boundBegin The begin bound of the time filter.
     * @param boundEnd The end bound of the time filter.
     * @param boundEquals An equals time filter (implies boundBegin and boundEnd null).
     *
     * @return a datablock containing only the matching observations.
     */
    private String parseDataBlock(String brutValues, AbstractEncodingEntry abstractEncoding, Timestamp boundBegin, Timestamp boundEnd, Timestamp boundEquals) {
        String values = "";
        if (abstractEncoding instanceof TextBlockEntry) {
                TextBlockEntry encoding = (TextBlockEntry) abstractEncoding;
                StringTokenizer tokenizer = new StringTokenizer(brutValues, encoding.getBlockSeparator());
                int i = 1;
                while (tokenizer.hasMoreTokens()) {
                    String block = tokenizer.nextToken();
                    logger.finer(i + " eme block =" + block);
                    i++;
                    String samplingTimeValue = block.substring(0, block.indexOf(encoding.getTokenSeparator()));
                    samplingTimeValue = samplingTimeValue.replace('T', ' ');
                    Date d;
                    try {
                        d = dateformat.parse(samplingTimeValue);
                    } catch (ParseException ex) {
                        logger.severe("unable to parse the value: " + samplingTimeValue);
                        continue;
                    }
                    Timestamp t = new Timestamp(d.getTime());
                    
                    // time during case
                    if (boundBegin != null && boundEnd != null) {
                        if (t.after(boundBegin) && t.before(boundEnd)) {
                            values += block + encoding.getBlockSeparator();
                            logger.finer("TD matching");
                        }
                        
                    //time after case    
                    } else if (boundBegin != null && boundEnd == null) {
                        if (t.after(boundBegin)) {
                            values += block + encoding.getBlockSeparator();
                            logger.finer("TA matching");
                        }
                    
                    //time before case    
                    } else if (boundBegin == null && boundEnd != null) {
                        if (t.before(boundEnd)) {
                            values += block + encoding.getBlockSeparator();
                            logger.finer("TB matching");
                        }
                        
                    //time equals case    
                    } else if (boundEquals != null) {
                        if (t.equals(boundEquals)) {
                            values += block + encoding.getBlockSeparator();
                            logger.finer("TE matching");
                        }
                    }
                }
            } else {
                logger.severe("unable to parse datablock unknown encoding");
                values = brutValues;
            }
        return values;
    }
    
    /**
     * Web service operation whitch register a Sensor in the SensorML database, 
     * and initialize its observation by adding an observation template in the O&M database.
     *
     * @param requestRegSensor A request containing a SensorML File describing a Sensor,
     *                         and an observation template for this sensor.
     */
    public RegisterSensorResponse registerSensor(RegisterSensor requestRegSensor) throws WebServiceException {
        logger.info("registerSensor request processing"  + '\n');
        //we verify the base request attribute
        verifyBaseRequest(requestRegSensor);
        
        boolean success = false;
        String id = "";
        try {
            //we begin a transaction
            SMLWriter.startTransaction();
            
            //we get the SensorML file who describe the Sensor to insert.
            RegisterSensor.SensorDescription d = requestRegSensor.getSensorDescription();
            AbstractSensorML process;
            if (d != null && d.getAny() instanceof AbstractSensorML) {
                process = (AbstractSensorML) d.getAny();
            } else {
                String type = "null";
                if (d != null && d.getAny() != null)
                    type = d.getAny().getClass().getName();
                throw new WebServiceException("unexpected type for process: " + type , INVALID_PARAMETER_VALUE, "sensorDescription");
            }
            
            //we get the observation template provided with the sensor description.
            ObservationTemplate temp = requestRegSensor.getObservationTemplate();
            ObservationEntry obs     = null;
            if (temp != null)
                obs = temp.getObservation();
            if(temp == null && obs == null) {
                throw new WebServiceException("observation template must be specify",
                                              MISSING_PARAMETER_VALUE,
                                              "observationTemplate");
            } else if (!obs.isComplete()) {
                throw new WebServiceException("observation template must specify at least the following fields: procedure ,observedProperty ,featureOfInterest, Result",
                                              INVALID_PARAMETER_VALUE,
                                              "observationTemplate"); 
            }
            
            //we create a new Identifier from the SensorML database
            int num = SMLReader.getNewSensorId();
            id = sensorIdBase + num;
            
            //and we write it in the sensorML Database
            SMLWriter.writeSensor(id, process);

            String phyId = getPhysicalID(process);

            // we record the mapping between physical id and database id
            recordMapping(id, phyId);
            
            // and we record the position of the piezometer
            DirectPositionType position = getSensorPosition(process);
            OMWriter.recordProcedureLocation(phyId, position);
                                    
            //we assign the new capteur id to the observation template
            ProcessEntry p = new ProcessEntry(id);
            obs.setProcedure(p);
            obs.setName(observationTemplateIdBase + num);
            logger.finer(obs.toString());
            //we write the observation template in the O&M database
            OMWriter.writeObservation(obs);
                   
            addSensorToOffering(process, obs);
            
           success = true; 

        } finally {
            if (!success) {
               SMLWriter.abortTransaction();
               logger.severe("Transaction failed");
            } else {
                SMLWriter.endTransaction();
            }
        }
        
        return new RegisterSensorResponse(id);
    }
    
    /**
     * Web service operation whitch insert a new Observation for the specified sensor
     * in the O&M database.
     * 
     * @param requestInsObs an InsertObservation request containing an O&M object and a Sensor id.
     */
    public InsertObservationResponse insertObservation(InsertObservation requestInsObs) throws WebServiceException {
        logger.info("InsertObservation request processing"  + '\n');
        //we verify the base request attribute
       verifyBaseRequest(requestInsObs);
        
        String id = "";
        //we get the id of the sensor and we create a sensor object
        String sensorId   = requestInsObs.getAssignedSensorId();
        int num = -1;
        if (sensorId.startsWith(sensorIdBase)) {
            num = Integer.parseInt(sensorId.substring(sensorIdBase.length()));
        } else {
            throw new WebServiceException("The sensor identifier is not valid",
                                         INVALID_PARAMETER_VALUE, "assignedSensorId");
        }
        ProcessEntry proc = new ProcessEntry(sensorId);

        //we get the observation and we assign to it the sensor
        ObservationEntry obs = requestInsObs.getObservation();
        if (obs != null) {
            obs.setProcedure(proc);
            obs.setName(OMReader.getNewObservationId());
            logger.finer("samplingTime received: " + obs.getSamplingTime()); 
            logger.finer("template received:" + '\n' + obs.toString());
        } else {
            throw new WebServiceException("The observation template must be specified",
                                             MISSING_PARAMETER_VALUE, "observationTemplate");
        }

        //we record the observation in the O&M database
       if (obs instanceof MeasurementEntry) {
           OMWriter.writeMeasurement((MeasurementEntry)obs);
        } else if (obs instanceof ObservationEntry) {

            //in first we verify that the observation is conform to the template
            ObservationEntry template = OMReader.getObservation(observationTemplateIdBase + num);
            //if the observation to insert match the template we can insert it in the OM db
            if (obs.matchTemplate(template)) {
                if (obs.getSamplingTime() != null && obs.getResult() != null) {
                    id = OMWriter.writeObservation(obs);
                    logger.info("new observation inserted:"+ "id = " + id + " for the sensor " + ((ProcessEntry)obs.getProcedure()).getName());
                } else {
                    throw new WebServiceException("The observation sampling time and the result must be specify",
                                                  MISSING_PARAMETER_VALUE, "samplingTime");
                }
            } else {
                throw new WebServiceException(" The observation doesn't match with the template of the sensor",
                                              INVALID_PARAMETER_VALUE, "samplingTime");
            }
        } 
        return new InsertObservationResponse(id);
    }
    
    /**
     *
     * 
     * @param times A list of time constraint.
     * @param SQLrequest A stringBuilder building the SQL request.
     * 
     * @return true if there is no errors in the time constraint else return false.
     */
    private AbstractTimeGeometricPrimitiveType treatEventTimeRequest(List<EventTime> times, boolean template) throws WebServiceException {
        
        //In template mode  his method return a temporal Object.
        AbstractTimeGeometricPrimitiveType templateTime = null;
        
        if (times.size() != 0) {
            
            for (EventTime time: times) {

                // The operation Time Equals
                if (time.getTEquals() != null && time.getTEquals().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    String propertyName = time.getTEquals().getPropertyName();
                    Object timeFilter   = time.getTEquals().getRest().get(0);
                    
                    if (!template) {
                        OMFilter.setTimeEquals(timeFilter);
                        
                    } else if (timeFilter instanceof TimePeriodType || timeFilter instanceof TimeInstantType) {
                        templateTime = (AbstractTimeGeometricPrimitiveType) timeFilter;
                        
                    } else {
                        throw new WebServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, "eventTime");
                    }
                
                // The operation Time before    
                } else if (time.getTBefore() != null && time.getTBefore().getRest().size() != 0) {

                    // we get the property name (not used for now)
                    String propertyName = time.getTBefore().getPropertyName();
                    Object timeFilter   = time.getTBefore().getRest().get(0);

                    if (!template) {
                        OMFilter.setTimeBefore(timeFilter);
                    } else if (timeFilter instanceof TimeInstantType) {
                        TimeInstantType ti = (TimeInstantType)timeFilter;
                        templateTime = new TimePeriodType(TimeIndeterminateValueType.BEFORE, ti.getTimePosition());
                    } else {
                        throw new WebServiceException("TM_Before operation require timeInstant!",
                                                      INVALID_PARAMETER_VALUE, "eventTime");
                    }
                    
                // The operation Time after    
                } else if (time.getTAfter() != null && time.getTAfter().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    String propertyName = time.getTAfter().getPropertyName();
                    Object timeFilter   = time.getTAfter().getRest().get(0);

                    if (!template) {
                        OMFilter.setTimeAfter(timeFilter);
                    } else if (timeFilter instanceof TimeInstantType) {
                        TimeInstantType ti = (TimeInstantType)timeFilter;
                        templateTime = new TimePeriodType(ti.getTimePosition());
                        
                    } else {
                       throw new WebServiceException("TM_After operation require timeInstant!",
                                                     INVALID_PARAMETER_VALUE, "eventTime");
                    }
                    
                // The time during operation    
                } else if (time.getTDuring() != null && time.getTDuring().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    String propertyName = (String)time.getTDuring().getRest().get(0);
                    Object timeFilter   = time.getTDuring().getRest().get(0);

                    if (!template) {
                        OMFilter.setTimeDuring(timeFilter);
                    }
                    if (timeFilter instanceof TimePeriodType) {
                        templateTime = (TimePeriodType)timeFilter;
                        
                    } else {
                        throw new WebServiceException("TM_During operation require TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, "eventTime");
                    }
                } else if (time.getTBegins() != null || time.getTBegunBy() != null || time.getTContains() != null ||time.getTEndedBy() != null || time.getTEnds() != null || time.getTMeets() != null
                           || time.getTOveralps() != null || time.getTOverlappedBy() != null) {
                    throw new WebServiceException("This operation is not take in charge by the Web Service, supported one are: TM_Equals, TM_After, TM_Before, TM_During",
                                                  OPERATION_NOT_SUPPORTED);
                } else {
                    throw new WebServiceException("Unknow time filter operation, supported one are: TM_Equals, TM_After, TM_Before, TM_During",
                                                  OPERATION_NOT_SUPPORTED);
                }
            }
        } else {
            return null;
        }
        return templateTime;
    }
    
    /**
     * return a SQL formatted timestamp
     * 
     * @param time a GML time position object.
     */
    private String getTimeValue(TimePositionType time) throws WebServiceException {
        if (time != null && time.getValue() != null) {
            String value = time.getValue();
            value = value.replace("T", " ");
            
            //we delete the data after the second
            if (value.indexOf('.') != -1) {
                value = value.substring(0, value.indexOf('.'));
            }
             try {
                 //here t is not used but it allow to verify the syntax of the timestamp
                 Timestamp t = Timestamp.valueOf(value);
                 return t.toString();
                 
             } catch(IllegalArgumentException e) {
                throw new WebServiceException("Unable to parse the value: " + value + '\n' +
                                                 "Bad format of timestamp: accepted format yyyy-mm-jjThh:mm:ss.msmsms.",
                                                 INVALID_PARAMETER_VALUE, "eventTime");
             }
          } else {
            String locator;
            if (time == null)
                locator = "Timeposition";
            else
                locator = "TimePosition value";
            throw new  WebServiceException("bad format of time, " + locator + " mustn't be null",
                                              MISSING_PARAMETER_VALUE, "eventTime");
          }
    }
    
    /**
     *  Verify that the bases request attributes are correct.
     */ 
    private void verifyBaseRequest(RequestBaseType request) throws WebServiceException {
        if (request != null) {
            if (request.getService() != null) {
                if (!request.getService().equals("SOS"))  {
                    throw new WebServiceException("service must be \"SOS\"!",
                                                  INVALID_PARAMETER_VALUE, "service");
                }
            } else {
                throw new WebServiceException("service must be specified!",
                                              MISSING_PARAMETER_VALUE, "service");
            }
            if (request.getVersion()!= null) {
                if (!request.getVersion().equals("1.0.0")) {
                    throw new WebServiceException("version must be \"1.0.0\"!",
                                                  VERSION_NEGOTIATION_FAILED);
                }
            } else {
                throw new WebServiceException("version must be specified!",
                                              MISSING_PARAMETER_VALUE, "version");
            }
         } else { 
            throw new WebServiceException("The request is null!",
                                          NO_APPLICABLE_CODE);
         }  
        
    }
    
    /**
     * Find a new suffix to obtain a unic temporary template id. 
     * 
     * @param templateName the full name of the sensor template.
     * 
     * @return an integer to paste after the template name;
     */
    private int getTemplateSuffix(String templateName) {
        int i = 0;
        boolean notFound = true;
        while (notFound) {
            if (templates.containsKey(templateName + '-' + i)) {
                i++;
            } else notFound = false;
        }
        return i;
    }
    
    /**
     * Add the new Sensor to an offering specified in the network attribute of sensorML file.
     * if the offering doesn't yet exist in the database, it will be create.
     * 
     * @param form The "form" contain the sensorML data.
     * @param template The observation template for this sensor.
     */
    private void addSensorToOffering(AbstractSensorML sensor, Observation template) throws WebServiceException {
     
        //we search which are the networks binded to this sensor
        List<String> networkNames = getNetworkNames(sensor);

        int size = networkNames.size();
        if (size == 0) {
            logger.severe("There is no network in that SensorML file");
        }

        // for each network we create (or update) an offering
        for (String networkName : networkNames) {

            String offeringName = "offering-" + networkName;
            logger.info("networks:" + offeringName);
            ObservationOfferingEntry offering = null;

            //we get the offering from the O&M database
            offering = OMReader.getObservationOffering(offeringName);

            //if the offering is already in the database
            if (offering != null) {

                //we add the new sensor to the offering
                OfferingProcedureEntry offProc = null;
                ReferenceEntry ref = getReferenceFromHRef(((ProcessEntry) template.getProcedure()).getHref());
                if (!offering.getProcedure().contains(ref)) {
                    if (ref == null) {
                        ref = new ReferenceEntry(null, ((ProcessEntry) template.getProcedure()).getHref());
                    }
                    offProc = new OfferingProcedureEntry(offering.getId(), ref);
                }

                //we add the phenomenon to the offering
                OfferingPhenomenonEntry offPheno = null;
                if (!offering.getObservedProperty().contains(template.getObservedProperty())) {
                    offPheno = new OfferingPhenomenonEntry(offering.getId(), (PhenomenonEntry) template.getObservedProperty());
                }

                // we add the feature of interest (station) to the offering
                OfferingSamplingFeatureEntry offSF = null;
                ref = getReferenceFromHRef(((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
                if (!offering.getFeatureOfInterest().contains(ref)) {
                    if (ref == null) {
                        ref = new ReferenceEntry(null, ((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
                    }
                    offSF = new OfferingSamplingFeatureEntry(offering.getId(), ref);
                }
                OMWriter.updateOffering(offProc, offPheno, offSF);
            // we build a new offering
            // TODO bounded by??? station?
            } else {
                logger.info("offering " + offeringName + " not present, first build");

                // for the eventime of the offering we take the time of now.
                Timestamp t = new Timestamp(System.currentTimeMillis());
                TimePeriodType time = new TimePeriodType(new TimePositionType(t.toString()));

                //we add the template process
                ReferenceEntry process = new ReferenceEntry(null, ((ProcessEntry) template.getProcedure()).getHref());

                //we add the template phenomenon
                PhenomenonEntry phenomenon = (PhenomenonEntry) template.getObservedProperty();

                //we add the template feature of interest
                ReferenceEntry station = new ReferenceEntry(null, ((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());

                //we create a list of accepted responseMode (fixed)
                List<ResponseModeType> responses  = Arrays.asList(INLINE, RESULT_TEMPLATE);
                List<QName> resultModel           = Arrays.asList(observation_QNAME);
                List<String> offerinfOutputFormat = Arrays.asList("text/xml");
                List<String> srsName              = Arrays.asList("EPSG:4326");

                // we create a the new Offering
                offering = new ObservationOfferingEntry(offeringName,
                        offeringIdBase + offeringName,
                        "",
                        srsName,
                        time,
                        process,
                        phenomenon,
                        station,
                        offerinfOutputFormat,
                        resultModel,
                        responses);
                OMWriter.writeOffering(offering);
            }

        }
        //then  we add the sensor to the global offering containing all the sensor

        //we get the offering from the O&M database
        ObservationOfferingEntry offering = OMReader.getObservationOffering("offering-allSensor");

        if (offering != null) {

            //we add the new sensor to the offering
            OfferingProcedureEntry offProc = null;
            ReferenceEntry ref = getReferenceFromHRef(((ProcessEntry) template.getProcedure()).getHref());
            if (!offering.getProcedure().contains(ref)) {
                if (ref == null) {
                    ref = new ReferenceEntry(null, ((ProcessEntry) template.getProcedure()).getHref());
                }
                offProc = new OfferingProcedureEntry(offering.getId(), ref);
            }

            //we add the phenomenon to the offering
            OfferingPhenomenonEntry offPheno = null;
            if (!offering.getObservedProperty().contains(template.getObservedProperty())) {
                offPheno = new OfferingPhenomenonEntry(offering.getId(), (PhenomenonEntry) template.getObservedProperty());
            }

            // we add the feature of interest (station) to the offering
            OfferingSamplingFeatureEntry offSF = null;
            ref = getReferenceFromHRef(((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
            if (!offering.getFeatureOfInterest().contains(ref)) {
                if (ref == null) {
                    ref = new ReferenceEntry(null, ((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
                }
                offSF = new OfferingSamplingFeatureEntry(offering.getId(), ref);
            }
            OMWriter.updateOffering(offProc, offPheno, offSF);
        } else {
            logger.info("offering allSensor not present, first build");

            // for the eventime of the offering we take the time of now.
            Timestamp t = new Timestamp(System.currentTimeMillis());
            TimePeriodType time = new TimePeriodType(new TimePositionType(t.toString()));

            //we add the template process
            ReferenceEntry process = new ReferenceEntry(null, ((ProcessEntry)template.getProcedure()).getHref());

            //we add the template phenomenon
            PhenomenonEntry phenomenon = (PhenomenonEntry)template.getObservedProperty();

            //we add the template feature of interest
            ReferenceEntry station = new ReferenceEntry(null, ((SamplingFeatureEntry)template.getFeatureOfInterest()).getId());

            //we create a list of accepted responseMode (fixed)
            List<ResponseModeType> responses  = Arrays.asList(RESULT_TEMPLATE, INLINE);
            List<QName> resultModel           = Arrays.asList(observation_QNAME);
            List<String> offeringOutputFormat = Arrays.asList("text/xml");
            List<String> srsName              = Arrays.asList("EPSG:4326");

            // we create a the new Offering
            offering = new ObservationOfferingEntry(offeringIdBase + "allSensor",
                                                    offeringIdBase + "allSensor",
                                                    "Base offering containing all the sensors.",
                                                    srsName,
                                                    time,
                                                    process,
                                                    phenomenon,
                                                    station,
                                                    offeringOutputFormat,
                                                    resultModel,
                                                    responses);
            OMWriter.writeOffering(offering);
        }
    }

    /**
     * Return the referenceEntry with the specified href attribute.
     */
    private ReferenceEntry getReferenceFromHRef(String href) throws WebServiceException {
        Set<ReferenceEntry> refs = OMReader.getReferences();
        if (refs != null) {
            Iterator<ReferenceEntry> it = refs.iterator();
            while (it.hasNext()) {
                ReferenceEntry ref = it.next();
                if (ref != null && ref.getHref() != null && ref.getHref().equals(href)) {
                    return ref;
                }
            }
        }
        return null;
    }
    
    /**
     * Normalize the capabilities document by replacing the double by reference
     * 
     * @param capa the unnormalized document.
     * 
     * @return a normalized document
     */
    private Capabilities normalizeDocument(Capabilities capa){
        List<PhenomenonPropertyType> alreadySee = new ArrayList<PhenomenonPropertyType>();
        if (capa.getContents() != null) {
            for (ObservationOfferingEntry off: capa.getContents().getObservationOfferingList().getObservationOffering()) {
                for (PhenomenonPropertyType pheno: off.getRealObservedProperty()) {
                    if (alreadySee.contains(pheno)) {
                        pheno.setToHref();
                    } else {
                        if (pheno.getPhenomenon() instanceof CompositePhenomenonEntry) {
                            CompositePhenomenonEntry compo = (CompositePhenomenonEntry) pheno.getPhenomenon();
                            for (PhenomenonPropertyType pheno2: compo.getRealComponent()) {
                                if (alreadySee.contains(pheno2)) {
                                    pheno2.setToHref();
                                } else {
                                    alreadySee.add(pheno2);
                                }
                            }
                        }
                        alreadySee.add(pheno);
                    }
                }
            }
        }
        return capa;
    }
    
    /**
     * Normalize the Observation collection document by replacing the double by reference
     * 
     * @param capa the unnormalized document.
     * 
     * @return a normalized document
     */
    private ObservationCollectionEntry normalizeDocument(ObservationCollectionEntry collection){
        //first if the collection is empty
        if (collection.getMember().size() == 0) {
            return new ObservationCollectionEntry("urn:ogc:def:nil:OGC:inapplicable");
        }
        
        List<FeaturePropertyType>          foiAlreadySee   = new ArrayList<FeaturePropertyType> ();
        List<PhenomenonPropertyType>       phenoAlreadySee = new ArrayList<PhenomenonPropertyType>();
        List<AbstractEncodingPropertyType> encAlreadySee   = new ArrayList<AbstractEncodingPropertyType>();
        List<DataComponentPropertyType>    dataAlreadySee  = new ArrayList<DataComponentPropertyType>();
        int index = 0;
        for (ObservationEntry observation: collection.getMember()) {
            //we do this for the feature of interest
            FeaturePropertyType foi = observation.getPropertyFeatureOfInterest();
            if (foiAlreadySee.contains(foi)){
                foi.setToHref();
            } else {
                foiAlreadySee.add(foi);
            }
            //for the phenomenon
            PhenomenonPropertyType phenomenon = observation.getPropertyObservedProperty();
            if (phenoAlreadySee.contains(phenomenon)){
                phenomenon.setToHref();
            } else {
                if (phenomenon.getPhenomenon() instanceof CompositePhenomenonEntry) {
                    CompositePhenomenonEntry compo = (CompositePhenomenonEntry) phenomenon.getPhenomenon();
                    for (PhenomenonPropertyType pheno2: compo.getRealComponent()) {
                        if (phenoAlreadySee.contains(pheno2)) {
                                    pheno2.setToHref();
                        } else {
                            phenoAlreadySee.add(pheno2);
                        }
                    }
                }
                phenoAlreadySee.add(phenomenon);
            }
            //for the result : textBlock encoding and element type
            if (observation.getResult() instanceof DataArrayPropertyType) {
                DataArrayEntry array = ((DataArrayPropertyType)observation.getResult()).getDataArray();
                
                //element type
                DataComponentPropertyType elementType = array.getPropertyElementType();
                if (dataAlreadySee.contains(elementType)){
                    elementType.setToHref();
                } else {
                    dataAlreadySee.add(elementType);
                }
                
                //encoding
                AbstractEncodingPropertyType encoding = array.getPropertyEncoding();
                if (encAlreadySee.contains(encoding)){
                    encoding.setToHref();
                                        
                } else {
                    encAlreadySee.add(encoding);
                }
            } else {
                if (observation.getResult() != null)
                    logger.severe("NormalizeDocument: Class not recognized for result:" + observation.getResult().getClass().getSimpleName());
                else
                    logger.severe("NormalizeDocument: The result is null");
            }
            index++;
        }
        return collection;
    }
    
    /**
     * Return the current output format MIME type (default: application/xml).
     * 
     * @return The current output format MIME type (default: application/xml).
     */
    public String getOutputFormat() {
        if (outputFormat == null) {
            return "application/xml";
        }
        return outputFormat;
    }
    
    /**
     * Set the capabilities document.
     */
    public void setStaticCapabilities(Capabilities staticCapabilities) {
        this.staticCapabilities = staticCapabilities;
    }
    
    /**
     * Set the current service URL
     */
    public void setServiceURL(String serviceURL){
        this.serviceURL = serviceURL;
    }
    
     /**
     * Return the ".sicade" directory.
     *
     * @return The ".sicade" directory containing .
     */
    public File getSicadeDirectory() {
        File sicadeDirectory;
        String home = System.getProperty("user.home");

        if (System.getProperty("os.name", "").startsWith("Windows")) {
             sicadeDirectory = new File(home, "Application Data\\Sicade");
        } else {
             sicadeDirectory = new File(home, ".sicade");
        }
        return sicadeDirectory;
    }

    /**
     * Record the mapping between physical ID and database ID.
     *
     * @param form The "form" containing the sensorML data.
     * @param dbId The identifier of the sensor in the O&M database.
     */
    private void recordMapping(String dbId, String physicalID) throws WebServiceException {
        try {
            map.setProperty(physicalID, dbId);
            File mappingFile = new File(getSicadeDirectory(), "/sos_configuration/mapping.properties");
            FileOutputStream out = new FileOutputStream(mappingFile);
            map.store(out, "");
            out.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new WebServiceException("The service cannot build the temporary file",
                    NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw an IOException:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    /**
     * Destroy and free the resource used by the worker.
     */
    public void destroy() {
        SMLReader.destroy();
        SMLWriter.destroy();
        OMReader.destroy();
        OMWriter.destroy();
        for (Timer t : schreduledTask) {
            t.cancel();
        }
    }
    
    /**
     * A task destroying a observation template when the template validity period pass.
     */ 
    class DestroyTemplateTask extends TimerTask {

        /**
         * The identifier of the temporary template.
         */
        private String templateId;
        
        /**
         * Build a new Timer which will destroy the temporaryTemplate
         * 
         * @param templateId The identifier of the temporary template.
         */
        public DestroyTemplateTask(String templateId) {
            this.templateId  = templateId;
        }
        
        /**
         * This method is launch when the timer expire.
         */
        public void run() {
            templates.remove(templateId);
            logger.info("template:" + templateId + " destroyed");
        }
    }
}
