/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.sos.ws;

// JDK dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.gml.v311.AbstractTimeGeometricPrimitiveType;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EnvelopeEntry;
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
import org.constellation.sos.v100.Capabilities;
import org.constellation.sos.v100.Contents;
import org.constellation.sos.v100.Contents.ObservationOfferingList;
import org.constellation.sos.v100.DescribeSensor;
import org.constellation.sos.v100.EventTime;
import org.constellation.sos.v100.GetCapabilities;
import org.constellation.sos.v100.GetObservation;
import org.constellation.sos.v100.GetResult;
import org.constellation.sos.v100.GetResultResponse;
import org.constellation.sos.v100.InsertObservation;
import org.constellation.sos.v100.InsertObservationResponse;
import org.constellation.sos.v100.RegisterSensor;
import org.constellation.sos.v100.RegisterSensorResponse;
import org.constellation.sos.v100.RequestBaseType;
import org.constellation.sos.v100.FilterCapabilities;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.ObservationTemplate;
import org.constellation.sos.v100.OfferingPhenomenonEntry;
import org.constellation.sos.v100.OfferingProcedureEntry;
import org.constellation.sos.v100.OfferingSamplingFeatureEntry;
import org.constellation.sos.v100.ResponseModeType;
import org.constellation.sos.factory.AbstractSOSFactory;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationFilterReader;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationResult;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.swe.AbstractEncoding;
import org.constellation.swe.DataArray;
import org.constellation.swe.TextBlock;
import org.constellation.swe.AnyResult;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.OGCWebService;

import org.constellation.ws.rs.WebService;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.sos.v100.ResponseModeType.*;
import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.Normalizer.*;

// GeoAPI dependencies
import org.opengis.observation.Observation;
import org.opengis.observation.CompositePhenomenon;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingPoint;

//geotools dependencies
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistry;
import org.geotools.util.logging.MonolineFormatter;

/**
 *
 * @author Guilhem Legal (Geomatys).
 */
public class SOSworker {

    public final static int DISCOVERY     = 0;
    public final static int TRANSACTIONAL = 1;
    
    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos");
    
    /**
     * A list of temporary ObservationTemplate
     */
    private Map<String, ObservationEntry> templates = new HashMap<String, ObservationEntry>();
    
    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */ 
    private final Properties map = new Properties();;
    
    /**
     * The base for sensor id.
     */ 
    private String sensorIdBase;
    
    /**
     * The base for observation id.
     */ 
    private String observationTemplateIdBase;
    
    /**
     * The base for offering id.
     */ 
    private final String offeringIdBase = "offering-";
    
    /**
     * The base for phenomenon id.
     */ 
    private final String phenomenonIdBase = "urn:ogc:def:phenomenon:OGC:1.0.30:";
    
    /**
     * The valid time for a getObservation template (in ms).
     */
    private long templateValidTime;
    
    /**
     * The maximum of observation return in a getObservation request.
     */
    private int maxObservationByRequest;
    
    /**
     * A capabilities object containing the static part of the document.
     */
    private Capabilities skeletonCapabilities;
    
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
    private int profile;
    
    /**
     * A date formater used to parse datablock.
     */
    private final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    /**
     * The Observation database reader
     */
    private ObservationReader OMReader;
    
    /**
     * The Observation database writer
     */
    private ObservationWriter OMWriter;

    /**
     * The observation filter
     */
    private ObservationFilter OMFilter;
    
    /**
     * The sensorML database reader
     */
    private SensorReader SMLReader;
    
    /**
     * The sensorML database writer
     */
    private SensorWriter SMLWriter;

    /**
     * The base Qname for complex observation.
     */
    public static final QName observation_QNAME = new QName("http://www.opengis.net/om/1.0", "Observation", "om");

    /**
     * A registry factory allowing to load carious SOS factory in function of the build implementation.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractSOSFactory.class);

    /**
     * A flag indicating if the worker is correctly started.
     */
    private boolean isStarted;
    
    /**
     * Initialize the database connection.
     */
    public SOSworker(File configurationDirectory) {
        
        if (configurationDirectory == null) {
            configurationDirectory = new File(WebService.getSicadeDirectory(), "sos_configuration");
        }

        logger.info("path to config file=" + configurationDirectory);

        
        isStarted = true;
        SOSConfiguration configuration = null;

        // Database configuration
        try {

            Unmarshaller configUM = JAXBContext.newInstance(SOSConfiguration.class).createUnmarshaller();
            File configFile = new File(configurationDirectory, "config.xml");
            if (configFile.exists()) {
                Object object = configUM.unmarshal(configFile);
                if (object instanceof SOSConfiguration) {
                    configuration = (SOSConfiguration) object;
                } else {
                    logger.severe("The SOS service is not running!"              + '\n' +
                            "cause: The generic configuration file is malformed" + '\n');
                    isStarted = false;
                    return;
                }
            } else {
                logger.severe("The SOS service is not running!" + '\n' +
                        "cause: The configuration file can't be found" + '\n');
                isStarted = false;
                return;
            }

            if (configuration.getLogFolder() != null) {
                initLogger("", configuration.getLogFolder());
                logger.info("Redirecting the log to: " + configuration.getLogFolder());
            }
            this.profile = configuration.getProfile();
            if (this.profile == DISCOVERY) {
                logger.info("Discovery profile loaded." + '\n');
            } else {
                logger.info("Transactional profile loaded." + '\n');
            }

            // the file who record the map between phisycal ID and DB ID.
            loadMapping(configurationDirectory);

            //we get the O&M filter Type
            ObservationFilterType OMFilterType = configuration.getObservationFilterType();

            //we get the O&M reader Type
            ObservationReaderType OMReaderType = configuration.getObservationReaderType();

            //we get the O&M writer Type
            ObservationWriterType OMWriterType = configuration.getObservationWriterType();

            //we get the Sensor reader type
            DataSourceType SMLType = configuration.getSMLType();

            Automatic SMLConfiguration = configuration.getSMLConfiguration();
            if (SMLConfiguration == null) {
                logger.severe("The SOS service is not running!" + '\n' +
                        "cause: The configuration file does not contains a SML configuration");
                isStarted = false;
                return;
            }
            SMLConfiguration.setConfigurationDirectory(configurationDirectory);

            Automatic OMConfiguration = configuration.getOMConfiguration();
            if (OMConfiguration == null) {
                logger.severe("The SOS service is not running!" + '\n' +
                        "cause: The configuration file does not contains a O&M configuration");
                isStarted = false;
                return;
            }
            OMConfiguration.setConfigurationDirectory(configurationDirectory);

            // we load the factory from the available classes
            AbstractSOSFactory SOSfactory = factory.getServiceProvider(AbstractSOSFactory.class, null, null, null);
        
            //we initialize the properties attribute
            String observationIdBase  = configuration.getObservationIdBase() != null ?
            configuration.getObservationIdBase() : "urn:ogc:object:observation:unknow:";

            sensorIdBase              = configuration.getSensorIdBase() != null ?
            configuration.getSensorIdBase() : "urn:ogc:object:sensor:unknow:";

            observationTemplateIdBase = configuration.getObservationTemplateIdBase() != null ?
            configuration.getObservationTemplateIdBase() : "urn:ogc:object:observationTemplate:unknow:";

            maxObservationByRequest   = configuration.getMaxObservationByRequest() != 0 ?
            configuration.getMaxObservationByRequest() : 500;

            int h, m;
            try {
                String validTime = configuration.getTemplateValidTime();
                if (validTime == null || validTime.equals("") || validTime.indexOf(':') == -1) {
                    validTime = "1:00";
                    logger.info("using default template valid time: one hour.\n");
                }
                h = Integer.parseInt(validTime.substring(0, validTime.indexOf(':')));
                m = Integer.parseInt(validTime.substring(validTime.indexOf(':') + 1));
            } catch (NumberFormatException ex) {
                logger.info("using default template valid time: one hour.\n");
                h = 1;
                m = 0;
            }
            templateValidTime = (h * 3600000) + (m * 60000);

            // we initialize the reader/writer/filter
            SMLReader = SOSfactory.getSensorReader(SMLType, SMLConfiguration, sensorIdBase, map);
            SMLWriter = SOSfactory.getSensorWriter(SMLType, SMLConfiguration, sensorIdBase);
            OMReader  = SOSfactory.getObservationReader(OMReaderType, OMConfiguration, observationIdBase);
            OMWriter  = SOSfactory.getObservationWriter(OMWriterType, OMConfiguration);
            OMFilter  = SOSfactory.getObservationFilter(OMFilterType, observationIdBase, observationTemplateIdBase, map, OMConfiguration);

            // we log some implementation informations
            logInfos();

            logger.info("SOS service running" + '\n');
            

        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.severe("The SOS service is not running!" + '\n' + "cause: JAXBException:" + ex.getMessage());
            isStarted = false;
        } catch (FactoryNotFoundException ex) {
            logger.severe("The SOS service is not working!" + '\n' + "cause: Unable to find a SOS Factory");
            isStarted = false;
        } catch (CstlServiceException ex) {
            logger.severe("The SOS service is not working!" + '\n' + "cause:" + ex.getMessage());
            isStarted = false;
        }
    }

    /**
     * Log some informations about the implementations classes for reader / writer / filter object.
     */
    public void logInfos() {
        if (SMLReader != null) {
            logger.info(SMLReader.getInfos() + " loaded.\n");
        } else {
            logger.warning("No SensorML reader loaded.\n");
        }
        if ( profile == TRANSACTIONAL) {
            if (SMLWriter != null) {
                logger.info(SMLWriter.getInfos() + " loaded.\n");
            } else {
                logger.warning("No SensorML writer loaded.\n");
            }
        }
        if (OMReader != null) {
            logger.info(OMReader.getInfos() + " loaded.\n");
        } else {
            logger.warning("No O&M reader loaded.\n");
        }
        if (OMFilter != null) {
            logger.info(OMFilter.getInfos() + " loaded.\n");
        } else {
            logger.warning("No O&M filter loaded.\n");
        }
        if ( profile == TRANSACTIONAL) {
            if (OMWriter != null) {
                logger.info(OMWriter.getInfos() + " loaded.\n");
            } else {
                logger.warning("No O&M writer loaded.\n");
            }
        }
    }

    /**
     *
     * @param configDir
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    private void loadMapping(File configDir) {
        // the file who record the map between phisycal ID and DB ID.
        try {
            File f = new File(configDir, "mapping.properties");
            if (f.exists()) {
                FileInputStream in = new FileInputStream(f);
                map.load(in);
                in.close();
            } else {
                logger.info("No mapping file found creating one.");
                f.createNewFile();
            }
        } catch (FileNotFoundException e) {
            // this tecnically can't happen
            logger.warning("File Not Found Exception while loading the mapping file");
        }  catch (IOException e) {
            logger.severe("IO Exception while loading the mapping file:" + e.getMessage());
        }
    }
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     */
    public Capabilities getCapabilities(GetCapabilities requestCapabilities) throws CstlServiceException {
        isWorking();
        logger.info("getCapabilities request processing" + '\n');
        long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals("SOS")) {
                throw new CstlServiceException("service must be \"SOS\"!",
                                                 INVALID_PARAMETER_VALUE, "service");
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service");
        }
        AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("1.0.0")){
                 throw new CstlServiceException("version available : 1.0.0",
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
                throw new CstlServiceException("accepted format : text/xml, application/xml",
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

        if (skeletonCapabilities == null) {
            throw new CstlServiceException("the service was unable to find the metadata for capabilities operation", NO_APPLICABLE_CODE);
        }

        //we enter the information for service identification.
        if (sections.getSection().contains("ServiceIdentification") || sections.getSection().contains("All")) {

            si = skeletonCapabilities.getServiceIdentification();
        }

        //we enter the information for service provider.
        if (sections.getSection().contains("ServiceProvider") || sections.getSection().contains("All")) {

            sp = skeletonCapabilities.getServiceProvider();
        }

        //we enter the operation Metadata
        if (sections.getSection().contains("OperationsMetadata") || sections.getSection().contains("All")) {

           om = skeletonCapabilities.getOperationsMetadata();

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
           List<String> eventTime = OMReader.getEventTime();
           if (eventTime != null && eventTime.size() == 1) {
               RangeType range = new RangeType(eventTime.get(0), "now");
               go.updateParameter("eventTime", range);
           } else if (eventTime != null && eventTime.size() == 2) {
               RangeType range = new RangeType(eventTime.get(0), eventTime.get(1));
               go.updateParameter("eventTime", range);
           }

           //the process list
           Collection<String> procNames  = OMReader.getProcedureNames();
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

            fc = skeletonCapabilities.getFilterCapabilities();
        }


        if (sections.getSection().contains("Contents") || sections.getSection().contains("All")) {
            // we add the list of observation ofeerings 
            ObservationOfferingList ool = new ObservationOfferingList(OMReader.getObservationOfferings());
            cont = new Contents(ool);
        }
        c = new Capabilities(si, sp, om, "1.0.0", null, fc, cont);

        // we normalize the document
        c = normalizeDocument(c);

        logger.info("getCapabilities processed in " + (System.currentTimeMillis() - start) + "ms.\n");
        return c;
    }
    
    /**
     * Web service operation whitch return an sml description of the specified sensor.
     * 
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     */
    public AbstractSensorML describeSensor(DescribeSensor requestDescSensor) throws CstlServiceException  {
        logger.info("DescribeSensor request processing"  + '\n');
        long start = System.currentTimeMillis();

        // we get the form
        verifyBaseRequest(requestDescSensor);

        //we verify that the output format is good.     
        if (requestDescSensor.getOutputFormat() != null) {
            if (!requestDescSensor.getOutputFormat().equalsIgnoreCase("text/xml;subtype=\"SensorML/1.0.0\"")) {
                throw new CstlServiceException("only text/xml;subtype=\"SensorML/1.0.0\" is accepted for outputFormat",
                        INVALID_PARAMETER_VALUE, "outputFormat");
            }
        } else {
            throw new CstlServiceException("output format text/xml;subtype=\"SensorML/1.0.0\" must be specify",
                                             MISSING_PARAMETER_VALUE, "outputFormat");
        }
        //we transform the form into an XML string
        if (requestDescSensor.getProcedure() == null) {
            throw new CstlServiceException("You must specify the sensor ID!",
                                         MISSING_PARAMETER_VALUE, "procedure");
        }
        String sensorId = requestDescSensor.getProcedure();

        logger.info("describeSensor processed in " + (System.currentTimeMillis() - start) + "ms.\n");
        return SMLReader.getSensor(sensorId);
    }
    
    /**
     * Web service operation whitch respond a collection of observation satisfying 
     * the restriction specified in the query.
     * 
     * @param requestObservation a document specifying the parameter of the request.
     */
    public ObservationCollectionEntry getObservation(GetObservation requestObservation) throws CstlServiceException {
        logger.info("getObservation request processing"  + '\n');
        long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        verifyBaseRequest(requestObservation);

        //we verify that the output format is good.     
        if (requestObservation.getResponseFormat() != null) {
            if (!requestObservation.getResponseFormat().equalsIgnoreCase("text/xml; subtype=\"om/1.0.0\"")) {
                throw new CstlServiceException("only text/xml; subtype=\"om/1.0.0\" is accepted for responseFormat",
                        INVALID_PARAMETER_VALUE, "responseFormat");
            }
        } else {
            throw new CstlServiceException("Response format text/xml; subtype=\"om/1.0.0\" must be specify",
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
                throw new CstlServiceException(" the response Mode: " + requestObservation.getResponseMode() + " is not supported by the service (inline or template available)!",
                                                 INVALID_PARAMETER_VALUE, "responseMode");
            }
        }
        OMFilter.initFilterObservation(mode);

        if (mode == RESULT_TEMPLATE) {
            template = true;
        } else if (mode != INLINE) {
            throw new CstlServiceException("This response Mode is not supported by the service (inline or template available)!",
                                             OPERATION_NOT_SUPPORTED, "responseMode");
        }

        ObservationOfferingEntry off;
        //we verify that there is an offering
        if (requestObservation.getOffering() == null) {
            throw new CstlServiceException("Offering must be specify!",
                                             MISSING_PARAMETER_VALUE, "offering");
        } else {
            off = OMReader.getObservationOffering(requestObservation.getOffering());
            if (off == null) {
                throw new CstlServiceException("This offering is not registered in the service",
                                              INVALID_PARAMETER_VALUE, "offering");
            }
        }

        //we verify that the srsName (if there is one) is advertised in the offering
        if (requestObservation.getSrsName() != null) {
            if (!off.getSrsName().contains(requestObservation.getSrsName())) {
                throw new CstlServiceException("This srs name is not advertised in the offering",
                                                INVALID_PARAMETER_VALUE, "srsName");
            }
        }

        //we verify that the resultModel (if there is one) is advertised in the offering
        if (requestObservation.getResultModel() != null) {
            if (!off.getResultModel().contains(requestObservation.getResultModel())) {
                throw new CstlServiceException("This result model is not advertised in the offering",
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
                ReferenceEntry proc = OMReader.getReference(dbId);
                if (proc == null) {
                    throw new CstlServiceException(" this process is not registred in the table",
                            INVALID_PARAMETER_VALUE, "procedure");
                }
                if (!off.getProcedure().contains(proc)) {
                    throw new CstlServiceException(" this process is not registred in the offering",
                            INVALID_PARAMETER_VALUE, "procedure");
                }
            } else {
                //if there is only one proccess null we return error (we'll see)
                if (procedures.size() == 1) {
                    throw new CstlServiceException("the process is null",
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
                Phenomenon phen = OMReader.getPhenomenon(phenomenonName);
                if (phen == null) {
                    throw new CstlServiceException(" this phenomenon " + phenomenonName + " is not registred in the database!",
                            INVALID_PARAMETER_VALUE, "observedProperty");
                }
                if (phen instanceof CompositePhenomenon) {
                    compositePhenomenons.add(phenomenonName);

                } else {
                    singlePhenomenons.add(phenomenonName);
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

                //verify that the station is registred in the DB.
                Collection<String> fois = OMReader.getFeatureOfInterestNames();
                for (final String samplingFeatureName : foiRequest.getObjectID()) {
                    if (!fois.contains(samplingFeatureName))
                        throw new CstlServiceException("the feature of interest "+ samplingFeatureName + " is not registered",
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
                            SamplingPoint station = (SamplingPointEntry) OMReader.getFeatureOfInterest(refStation.getHref());
                            if (station == null)
                                throw new CstlServiceException("the feature of interest is not registered",
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
                        throw new CstlServiceException("the envelope is not build correctly",
                                                     INVALID_PARAMETER_VALUE);
                    }
                } else {
                    throw new CstlServiceException("This operation is not take in charge by the Web Service",
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
                    throw new CstlServiceException(" to use the operation Less Than you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "lessThan");
                }


            } else if (result.getPropertyIsGreaterThan() != null) {

                String propertyName  = result.getPropertyIsGreaterThan().getPropertyName();
                LiteralType literal  = result.getPropertyIsGreaterThan().getLiteral();
                if (propertyName == null || propertyName.equals("") || literal == null) {
                    throw new CstlServiceException(" to use the operation Greater Than you must specify the propertyName and the litteral",
                                                 MISSING_PARAMETER_VALUE, "greaterThan");
                }

            } else if (result.getPropertyIsEqualTo() != null) {

                String propertyName  = result.getPropertyIsEqualTo().getPropertyName();
                LiteralType literal  = result.getPropertyIsEqualTo().getLiteral();
                if (propertyName == null || propertyName.equals("") || literal == null) {
                     throw new CstlServiceException(" to use the operation Equal you must specify the propertyName and the litteral",
                                                   MISSING_PARAMETER_VALUE, "propertyIsEqualTo");
                }


            } else if (result.getPropertyIsLike() != null) {
                throw new CstlServiceException("This operation is not take in charge by the Web Service",
                                              OPERATION_NOT_SUPPORTED, "propertyIsLike");

            } else if (result.getPropertyIsBetween() != null) {

                logger.info("PROP IS BETWEEN");
                if (result.getPropertyIsBetween().getPropertyName() == null) {
                    throw new CstlServiceException("To use the operation Between you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "propertyIsBetween");
                }
                String propertyName  = result.getPropertyIsBetween().getPropertyName();

                LiteralType LowerLiteral  = result.getPropertyIsBetween().getLowerBoundary().getLiteral();
                LiteralType UpperLiteral  = result.getPropertyIsBetween().getUpperBoundary().getLiteral();

                if (propertyName == null || propertyName.equals("") || LowerLiteral == null || UpperLiteral == null) {
                        throw new CstlServiceException("This property name, lower and upper literal must be specify",
                                                      INVALID_PARAMETER_VALUE, "result");

                }

            } else {
                throw new CstlServiceException("This operation is not take in charge by the Web Service",
                                              OPERATION_NOT_SUPPORTED);
            }
        }

        /*
         * here we can have 2 different behaviour :
         *
         * (1) - We have separate observation filter and reader :
         *        - The filter execute a request and return a list of identifiers.
         *        - The reader retrieve each observation from the list of identifiers
         *
         * (2) - We have mixed observation filter and reader :
         *        - The filterReader execute a request and return directly the observations
         *
         */
        List<Observation> matchingResult = new ArrayList<Observation>();

        // case (1)
        if (!(OMFilter instanceof ObservationFilterReader)) {
            List<String> observationIDs = OMFilter.filterObservation();
            for (String observationID : observationIDs) {
                matchingResult.add(OMReader.getObservation(observationID));
            }

        // case (2)
        } else {
            ObservationFilterReader OMFR = (ObservationFilterReader) OMFilter;
            if (template) {
                matchingResult = OMFR.getObservationTemplates();
            } else {
                matchingResult = OMFR.getObservations();
            }
        }

        for (Observation o : matchingResult) {
            if (template) {

                String temporaryTemplateId = o.getName() + '-' + getTemplateSuffix(o.getName());
                ObservationEntry temporaryTemplate = ((ObservationEntry) o).getTemporaryTemplate(temporaryTemplateId, templateTime);
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
                response.add((ObservationEntry) o);

                //we stop the request if its too big
                if (response.getMember().size() > maxObservationByRequest) {
                    throw new CstlServiceException("Your request is to voluminous please add filter and try again",
                            NO_APPLICABLE_CODE);
                }
            }
        }

        response.setBoundedBy(getCollectionBound(response));
        response = normalizeDocument(response);
        logger.info("getObservation processed in " + (System.currentTimeMillis() - start) + "ms.\n");
        return response;
    }

    
    /**
     * Web service operation
     */
    public GetResultResponse getResult(GetResult requestResult) throws CstlServiceException {
        logger.info("getResult request processing"  + '\n');
        long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        verifyBaseRequest(requestResult);
        
        ObservationEntry template = null;
        if (requestResult.getObservationTemplateId() != null) {
            String id = requestResult.getObservationTemplateId();
            template = templates.get(id);
            if (template == null) {
                throw new CstlServiceException("this template does not exist or is no longer usable",
                                              INVALID_PARAMETER_VALUE, "ObservationTemplateId");
            }
        } else {
            throw new CstlServiceException("ObservationTemplateID must be specified",
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
        
        String values;
        if (OMFilter instanceof ObservationFilterReader) {
            values = ((ObservationFilterReader)OMFilter).getResults();
            
        } else {
            List<ObservationResult> results = OMFilter.filterResult();
            StringBuilder datablock = new StringBuilder();
            for (ObservationResult result: results) {
                Timestamp tBegin = result.beginTime;
                Timestamp tEnd   = result.endTime;
                AnyResult a = OMReader.getResult(result.resultID);
                if (a != null) {
                    DataArray array = a.getArray();
                    if (array != null) {
                        String resultValues = getResultValues(tBegin, tEnd, array, times);
                        datablock.append(resultValues).append('\n');
                    } else {
                        throw new IllegalArgumentException("Array is null");
                    }
                }
            }
            values = datablock.toString();
        }
        GetResultResponse.Result r = new GetResultResponse.Result(values, serviceURL + '/' + requestResult.getObservationTemplateId());
        GetResultResponse response = new GetResultResponse(r);
        logger.info("GetResult processed in " + (System.currentTimeMillis() - start) + "ms");
        return response;
    }
    
    private String getResultValues(Timestamp tBegin, Timestamp tEnd, DataArray array, List<EventTime> eventTimes) throws CstlServiceException {
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
    private String parseDataBlock(String brutValues, AbstractEncoding abstractEncoding, Timestamp boundBegin, Timestamp boundEnd, Timestamp boundEquals) {
        String values = "";
        if (abstractEncoding instanceof TextBlock) {
                TextBlock encoding = (TextBlock) abstractEncoding;
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
                        synchronized (dateformat) {
                            d = dateformat.parse(samplingTimeValue);
                        }
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
    public RegisterSensorResponse registerSensor(RegisterSensor requestRegSensor) throws CstlServiceException {
        logger.info("registerSensor request processing"  + '\n');
        long start = System.currentTimeMillis();
        
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
                throw new CstlServiceException("unexpected type for process: " + type , INVALID_PARAMETER_VALUE, "sensorDescription");
            }
            
            //we get the observation template provided with the sensor description.
            ObservationTemplate temp = requestRegSensor.getObservationTemplate();
            ObservationEntry obs     = null;
            if (temp != null)
                obs = temp.getObservation();
            if(temp == null && obs == null) {
                throw new CstlServiceException("observation template must be specify",
                                              MISSING_PARAMETER_VALUE,
                                              "observationTemplate");
            } else if (!obs.isComplete()) {
                throw new CstlServiceException("observation template must specify at least the following fields: procedure ,observedProperty ,featureOfInterest, Result",
                                              INVALID_PARAMETER_VALUE,
                                              "observationTemplate"); 
            }
            
            //we create a new Identifier from the SensorML database
            int num = SMLWriter.getNewSensorId();
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
        
        logger.info("registerSensor processed in " + (System.currentTimeMillis() - start) + "ms");
        return new RegisterSensorResponse(id);
    }
    
    /**
     * Web service operation whitch insert a new Observation for the specified sensor
     * in the O&M database.
     * 
     * @param requestInsObs an InsertObservation request containing an O&M object and a Sensor id.
     */
    public InsertObservationResponse insertObservation(InsertObservation requestInsObs) throws CstlServiceException {
        logger.info("InsertObservation request processing"  + '\n');
        long start = System.currentTimeMillis();

        //we verify the base request attribute
        verifyBaseRequest(requestInsObs);
        
        String id = "";
        //we get the id of the sensor and we create a sensor object
        String sensorId   = requestInsObs.getAssignedSensorId();
        int num = -1;
        if (sensorId.startsWith(sensorIdBase)) {
            num = Integer.parseInt(sensorId.substring(sensorIdBase.length()));
        } else {
            throw new CstlServiceException("The sensor identifier is not valid",
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
            throw new CstlServiceException("The observation template must be specified",
                                             MISSING_PARAMETER_VALUE, "observationTemplate");
        }

        //we record the observation in the O&M database
       if (obs instanceof MeasurementEntry) {
           OMWriter.writeMeasurement((MeasurementEntry)obs);
        } else if (obs instanceof ObservationEntry) {

            //in first we verify that the observation is conform to the template
            ObservationEntry template = (ObservationEntry) OMReader.getObservation(observationTemplateIdBase + num);
            //if the observation to insert match the template we can insert it in the OM db
            if (obs.matchTemplate(template)) {
                if (obs.getSamplingTime() != null && obs.getResult() != null) {
                    id = OMWriter.writeObservation(obs);
                    logger.info("new observation inserted:"+ "id = " + id + " for the sensor " + ((ProcessEntry)obs.getProcedure()).getName());
                } else {
                    throw new CstlServiceException("The observation sampling time and the result must be specify",
                                                  MISSING_PARAMETER_VALUE, "samplingTime");
                }
            } else {
                throw new CstlServiceException(" The observation doesn't match with the template of the sensor",
                                              INVALID_PARAMETER_VALUE, "samplingTime");
            }
        }

        logger.info("insertObservation processed in " + (System.currentTimeMillis() - start) + "ms");
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
    private AbstractTimeGeometricPrimitiveType treatEventTimeRequest(List<EventTime> times, boolean template) throws CstlServiceException {
        
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
                        throw new CstlServiceException("TM_Equals operation require timeInstant or TimePeriod!",
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
                        throw new CstlServiceException("TM_Before operation require timeInstant!",
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
                       throw new CstlServiceException("TM_After operation require timeInstant!",
                                                     INVALID_PARAMETER_VALUE, "eventTime");
                    }
                    
                // The time during operation    
                } else if (time.getTDuring() != null && time.getTDuring().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    String propertyName = time.getTDuring().getPropertyName();
                    Object timeFilter   = time.getTDuring().getRest().get(0);

                    if (!template) {
                        OMFilter.setTimeDuring(timeFilter);
                    }
                    if (timeFilter instanceof TimePeriodType) {
                        templateTime = (TimePeriodType)timeFilter;
                        
                    } else {
                        throw new CstlServiceException("TM_During operation require TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, "eventTime");
                    }
                } else if (time.getTBegins() != null || time.getTBegunBy() != null || time.getTContains() != null ||time.getTEndedBy() != null || time.getTEnds() != null || time.getTMeets() != null
                           || time.getTOveralps() != null || time.getTOverlappedBy() != null) {
                    throw new CstlServiceException("This operation is not take in charge by the Web Service, supported one are: TM_Equals, TM_After, TM_Before, TM_During",
                                                  OPERATION_NOT_SUPPORTED);
                } else {
                    throw new CstlServiceException("Unknow time filter operation, supported one are: TM_Equals, TM_After, TM_Before, TM_During",
                                                  OPERATION_NOT_SUPPORTED);
                }
            }
        } else {
            return null;
        }
        return templateTime;
    }
    
    /**
     *  Verify that the bases request attributes are correct.
     */ 
    private void verifyBaseRequest(RequestBaseType request) throws CstlServiceException {
        isWorking();
        if (request != null) {
            if (request.getService() != null) {
                if (!request.getService().equals("SOS"))  {
                    throw new CstlServiceException("service must be \"SOS\"!", INVALID_PARAMETER_VALUE, "service");
                }
            } else {
                throw new CstlServiceException("service must be specified!", MISSING_PARAMETER_VALUE, "service");
            }
            if (request.getVersion()!= null) {
                if (!request.getVersion().equals("1.0.0")) {
                    throw new CstlServiceException("version must be \"1.0.0\"!", VERSION_NEGOTIATION_FAILED);
                }
            } else {
                throw new CstlServiceException("version must be specified!", MISSING_PARAMETER_VALUE, "version");
            }
         } else { 
            throw new CstlServiceException("The request is null!", NO_APPLICABLE_CODE);
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
    private void addSensorToOffering(AbstractSensorML sensor, Observation template) throws CstlServiceException {
     
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
                ReferenceEntry ref = OMReader.getReference(((ProcessEntry) template.getProcedure()).getHref());
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
                ref = OMReader.getReference(((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
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
            ReferenceEntry ref = OMReader.getReference(((ProcessEntry) template.getProcedure()).getHref());
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
            ref = OMReader.getReference(((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
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
    public void setSkeletonCapabilities(Capabilities skeletonCapabilities) {
        this.skeletonCapabilities = skeletonCapabilities;
    }
    
    /**
     * Set the current service URL
     */
    public void setServiceURL(String serviceURL){
        this.serviceURL = serviceURL;
    }
    
    /**
     * Record the mapping between physical ID and database ID.
     *
     * @param form The "form" containing the sensorML data.
     * @param dbId The identifier of the sensor in the O&M database.
     */
    private void recordMapping(String dbId, String physicalID) throws CstlServiceException {
        try {
            map.setProperty(physicalID, dbId);
            File mappingFile = new File(WebService.getSicadeDirectory(), "/sos_configuration/mapping.properties");
            FileOutputStream out = new FileOutputStream(mappingFile);
            map.store(out, "");
            out.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("The service cannot build the temporary file",
                    NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("the service has throw an IOException:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        }
    }

    /**
     * Throw and exception if the service is not working
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    private void isWorking() throws CstlServiceException {
        if (!isStarted) {
            throw new CstlServiceException("The service is not running!", NO_APPLICABLE_CODE);
        }
    }

    /**
     * Redirect the logs into the specified folder.
     * if the parameter ID is null or empty it create a file named "cstl-sos.log"
     * else the file is named "ID-cstl-sos.log"
     *
     * @param ID The ID of the service in a case of multiple sos server.
     * @param filePath The path to the log folder.
     */
    private void initLogger(String ID, String filePath) {
        try {
            if (ID != null && !ID.equals("")) {
                ID = ID + '-';
            }
            FileHandler handler  = new FileHandler(filePath + '/'+ ID + "cstl-sos.log");
            handler.setFormatter(new MonolineFormatter());
            logger.addHandler(handler);
        } catch (IOException ex) {
            logger.severe("IO exception while trying to separate CSW Logs:" + ex.getMessage());
        } catch (SecurityException ex) {
            logger.severe("Security exception while trying to separate CSW Logs" + ex.getMessage());
        }
    }

    /**
     * Destroy and free the resource used by the worker.
     */
    public void destroy() {
        if (SMLReader != null)
            SMLReader.destroy();
        if (SMLWriter != null)
            SMLWriter.destroy();
        if (OMReader != null)
            OMReader.destroy();
        if (OMWriter != null)
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
