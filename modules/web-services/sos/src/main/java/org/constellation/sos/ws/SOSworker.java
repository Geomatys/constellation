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
import java.util.logging.Level;
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
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.AbstractMetadataWriter;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.sos.factory.AbstractSOSFactory;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationFilterReader;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationResult;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.OGCWebService;
import static org.constellation.sos.ws.SOSConstants.*;
import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.Normalizer.*;

// GeoAPI dependencies
import org.opengis.observation.Observation;
import org.opengis.observation.CompositePhenomenon;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Measure;
import org.opengis.observation.sampling.SamplingFeature;

// Geotoolkit dependencies
import org.geotoolkit.gml.xml.v311.AbstractTimeGeometricPrimitiveType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.TimeIndeterminateValueType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.RangeType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.ows.xml.v110.ServiceIdentification;
import org.geotoolkit.ows.xml.v110.ServiceProvider;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.Contents;
import org.geotoolkit.sos.xml.v100.Contents.ObservationOfferingList;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.geotoolkit.sos.xml.v100.EventTime;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.GetResult;
import org.geotoolkit.sos.xml.v100.GetResultResponse;
import org.geotoolkit.sos.xml.v100.InsertObservation;
import org.geotoolkit.sos.xml.v100.InsertObservationResponse;
import org.geotoolkit.sos.xml.v100.RegisterSensor;
import org.geotoolkit.sos.xml.v100.RegisterSensorResponse;
import org.geotoolkit.sos.xml.v100.RequestBaseType;
import org.geotoolkit.sos.xml.v100.FilterCapabilities;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ObservationTemplate;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonEntry;
import org.geotoolkit.sos.xml.v100.OfferingProcedureEntry;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.gml.xml.v311.AbstractFeatureEntry;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.FeatureCollectionType;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.observation.xml.v100.MeasurementEntry;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.observation.xml.v100.ProcessEntry;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.BinaryTemporalOpType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.SpatialOpsType;
import org.geotoolkit.sampling.xml.v100.ObjectFactory;
import org.geotoolkit.sampling.xml.v100.SamplingCurveType;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sampling.xml.v100.SamplingPointEntry;
import org.geotoolkit.sampling.xml.v100.SamplingSolidType;
import org.geotoolkit.sampling.xml.v100.SamplingSurfaceType;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SmlFactory;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.swe.xml.AbstractEncoding;
import org.geotoolkit.swe.xml.AnyResult;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.logging.MonolineFormatter;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.sos.xml.v100.ResponseModeType.*;


/**
 *
 * @author Guilhem Legal (Geomatys).
 */
public class SOSworker {

    public static final int DISCOVERY     = 0;
    public static final int TRANSACTIONAL = 1;
    
    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");
    
    /**
     * A list of temporary ObservationTemplate
     */
    private Map<String, ObservationEntry> templates = new HashMap<String, ObservationEntry>();
    
    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */ 
    private final Properties map = new Properties();
    
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
    private static final String OFFERING_ID_BASE = "offering-";
    
    /**
     * The base for phenomenon id.
     */ 
    private String phenomenonIdBase;
    
    /**
     * The valid time for a getObservation template (in ms).
     */
    private long templateValidTime;
    
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
     * A list of schreduled Task (used in close method).
     */
    private List<Timer> schreduledTask = new ArrayList<Timer>();
    
    /**
     * A list of supported MIME type 
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList(MimeType.TEXT_XML,
                                                MimeType.APP_XML,
                                                MimeType.TEXT_PLAIN);
    }

    /**
     * A list of supported SensorML version
     */
    private static final List<String> ACCEPTED_SENSORML_FORMATS;
    static {
        ACCEPTED_SENSORML_FORMATS = Arrays.asList(SENSORML_100_FORMAT,
                                                  SENSORML_101_FORMAT);
    }
    
    /**
     * The profile of the SOS service (transational/discovery). 
     */
    private int profile;
    
    /**
     * A date formater used to parse datablock.
     */
    private static final List<DateFormat> DATE_FORMATS = new ArrayList<DateFormat>();
    static {
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        DATE_FORMATS.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * The Observation database reader
     */
    private ObservationReader omReader;
    
    /**
     * The Observation database writer
     */
    private ObservationWriter omWriter;

    /**
     * The observation filter
     */
    private ObservationFilter omFilter;
    
    /**
     * The sensorML database reader
     */
    private SensorReader smlReader;
    
    /**
     * The sensorML database writer
     */
    private SensorWriter smlWriter;

    /**
     * A registry factory allowing to load carious SOS factory in function of the build implementation.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractSOSFactory.class);

    /**
     * The factory to instanciate the Readers / Writers / Filters
     */
    private AbstractSOSFactory sosFactory;

    /**
     * A flag indicating if the worker is correctly started.
     */
    private boolean isStarted;
    
    /**
     * The supported Response Mode for GetObservation request (depends on reader capabilities)
     */
    private List<ResponseModeType> acceptedResponseMode;

    /**
     * The supported Response Format for GetObservation request (depends on reader capabilities)
     */
    private List<String> acceptedResponseFormat;

    /**
     * A debug flag.
     * If true the server will verify the gap between a the samplingTime of an observation and the time of insertion.
     */
    private boolean verifySynchronization;

    /**
     * The log level off al the informations log.
     */
    private Level logLevel = Level.INFO;
    
    /**
     * Initialize the database connection.
     */
    public SOSworker(File configurationDirectory) {
        
        if (configurationDirectory == null) {
            configurationDirectory = getConfigDirectory();
        }

        final String notWorkingMsg     = "The SOS service is not running!";
        isStarted                      = true;
        SOSConfiguration configuration = null;

        // Database configuration
        try {

            final Unmarshaller configUM = JAXBContext.newInstance(SOSConfiguration.class).createUnmarshaller();
            final File configFile = new File(configurationDirectory, "config.xml");
            if (configFile.exists()) {
                final Object object = configUM.unmarshal(configFile);
                if (object instanceof SOSConfiguration) {
                    configuration = (SOSConfiguration) object;
                } else {
                    LOGGER.warning(notWorkingMsg                                 + '\n' +
                            "cause: The generic configuration file is malformed" + '\n');
                    isStarted = false;
                    return;
                }
            } else {
                LOGGER.warning(notWorkingMsg                           + '\n' +
                        "cause: The configuration file can't be found" + '\n');
                isStarted = false;
                return;
            }

            if (configuration.getLogFolder() != null) {
                initLogger("", configuration.getLogFolder());
                LOGGER.info("Redirecting the log to: " + configuration.getLogFolder());
            }
            this.profile = configuration.getProfile();
            this.verifySynchronization = configuration.isVerifySynchronization();

            // the file who record the map between phisycal ID and DB ID.
            loadMapping(configurationDirectory);

            //we get the O&M filter Type
            final ObservationFilterType omFilterType = configuration.getObservationFilterType();

            //we get the O&M reader Type
            final ObservationReaderType omReaderType = configuration.getObservationReaderType();

            //we get the O&M writer Type
            final ObservationWriterType omWriterType = configuration.getObservationWriterType();

            //we get the Sensor reader type
            final DataSourceType smlType = configuration.getSMLType();

            final Automatic smlConfiguration = configuration.getSMLConfiguration();
            if (smlConfiguration == null) {
                LOGGER.warning(notWorkingMsg + '\n' +
                        "cause: The configuration file does not contains a SML configuration");
                isStarted = false;
                return;
            }
            smlConfiguration.setConfigurationDirectory(configurationDirectory);

            final Automatic omConfiguration = configuration.getOMConfiguration();
            if (omConfiguration == null) {
                LOGGER.warning(notWorkingMsg + '\n' +
                        "cause: The configuration file does not contains a O&M configuration");
                isStarted = false;
                return;
            }
            omConfiguration.setConfigurationDirectory(configurationDirectory);

            // we load the factory from the available classes
            sosFactory = factory.getServiceProvider(AbstractSOSFactory.class, null, null, null);
        
            //we initialize the properties attribute
            final String observationIdBase  = configuration.getObservationIdBase() != null ?
            configuration.getObservationIdBase() : "urn:ogc:object:observation:unknow:";

            sensorIdBase              = configuration.getSensorIdBase() != null ?
            configuration.getSensorIdBase() : "urn:ogc:object:sensor:unknow:";

            phenomenonIdBase          = configuration.getPhenomenonIdBase() != null ?
            configuration.getPhenomenonIdBase() : "urn:ogc:def:phenomenon:OGC:1.0.30:";

            observationTemplateIdBase = configuration.getObservationTemplateIdBase() != null ?
            configuration.getObservationTemplateIdBase() : "urn:ogc:object:observationTemplate:unknow:";

            int h, m;
            try {
                String validTime = configuration.getTemplateValidTime();
                if (validTime == null || validTime.equals("") || validTime.indexOf(':') == -1) {
                    validTime = "1:00";
                    LOGGER.info("using default template valid time: one hour.\n");
                }
                h = Integer.parseInt(validTime.substring(0, validTime.indexOf(':')));
                m = Integer.parseInt(validTime.substring(validTime.indexOf(':') + 1));
            } catch (NumberFormatException ex) {
                LOGGER.info("using default template valid time: one hour.\n");
                h = 1;
                m = 0;
            }
            templateValidTime = (h * 3600000) + (m * 60000);

            // we initialize the reader/writer/filter
            smlReader = sosFactory.getSensorReader(smlType, smlConfiguration, sensorIdBase, map);
            smlWriter = sosFactory.getSensorWriter(smlType, smlConfiguration, sensorIdBase, map);
            omReader  = sosFactory.getObservationReader(omReaderType, omConfiguration, observationIdBase);
            omWriter  = sosFactory.getObservationWriter(omWriterType, observationTemplateIdBase, omConfiguration);
            omFilter  = sosFactory.getObservationFilter(omFilterType, observationIdBase, observationTemplateIdBase, map, omConfiguration);

            //we initialize the variables depending on the Reader capabilities
            this.acceptedResponseMode   = omReader.getResponseModes();
            this.acceptedResponseFormat = omReader.getResponseFormats();
            
            // we log some implementation informations
            logInfos();

        } catch (JAXBException ex) {
            LOGGER.log(Level.FINER, ex.getMessage(), ex);
            String msg;
            if (ex.getMessage() != null) {
                msg = ex.getMessage();
            } else {
                if (ex.getLinkedException() != null) {
                    msg = ex.getLinkedException().getMessage();
                } else {
                    msg = "no message";
                }
            }
            LOGGER.warning(notWorkingMsg + '\n' + "cause: JAXBException:" + msg);
            isStarted = false;
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning(notWorkingMsg + '\n' + "cause: Unable to find a SOS Factory");
            isStarted = false;
        } catch (CstlServiceException ex) {
            LOGGER.warning(notWorkingMsg + '\n' + "cause:" + ex.getMessage());
            isStarted = false;
        }
    }

    /**
     * Log some informations about the implementations classes for reader / writer / filter object.
     */
    public final void logInfos() {
        final String loaded =  " loaded.\n";
        final StringBuilder infos = new StringBuilder();

        if (this.profile == DISCOVERY) {
            infos.append("Discovery profile loaded.").append('\n');
        } else {
            infos.append("Transactional profile loaded.").append('\n');
        }
        if (smlReader != null) {
            infos.append('\n').append(smlReader.getInfos() + loaded).append('\n');
        } else {
            infos.append("No SensorML reader loaded.\n");
        }
        if ( profile == TRANSACTIONAL) {
            if (smlWriter != null) {
                infos.append(smlWriter.getInfos() + loaded).append('\n');
            } else {
                infos.append("No SensorML writer loaded.\n");
            }
        }
        if (omReader != null) {
            infos.append(omReader.getInfos() + loaded).append('\n');
        } else {
            infos.append("No O&M reader loaded.\n");
        }
        if (omFilter != null) {
            infos.append(omFilter.getInfos() + loaded).append('\n');
        } else {
            infos.append("No O&M filter loaded.\n");
        }
        if ( profile == TRANSACTIONAL) {
            if (omWriter != null) {
                infos.append(omWriter.getInfos() + loaded).append('\n');
            } else {
                infos.append("No O&M writer loaded.\n");
            }
        }
        infos.append("SOS service running").append('\n');
        LOGGER.info(infos.toString());
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
            final File f = new File(configDir, "mapping.properties");
            if (f.exists()) {
                final FileInputStream in = new FileInputStream(f);
                map.load(in);
                in.close();
            } else {
                LOGGER.info("No mapping file found creating one.");
                final boolean created = f.createNewFile();
                if (!created) {
                    LOGGER.warning("unable to create a new empty mapping file.");
                }
            }
        } catch (FileNotFoundException e) {
            // this tecnically can't happen
            LOGGER.warning("File Not Found Exception while loading the mapping file");
        }  catch (IOException e) {
            LOGGER.severe("IO Exception while loading the mapping file:" + e.getMessage());
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
        LOGGER.log(logLevel, "getCapabilities request processing" + '\n');
        final long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals(SOS)) {
                throw new CstlServiceException("service must be \"SOS\"!",
                                                 INVALID_PARAMETER_VALUE, SERVICE);
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, SERVICE);
        }
        final AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains(VERSION)){
                 throw new CstlServiceException("version available : 1.0.0",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }
        final AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
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
            this.outputFormat = MimeType.APP_XML;
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
        if (sections.getSection().contains("ServiceIdentification") || sections.getSection().contains(ALL)) {

            si = skeletonCapabilities.getServiceIdentification();
        }

        //we enter the information for service provider.
        if (sections.getSection().contains("ServiceProvider") || sections.getSection().contains(ALL)) {

            sp = skeletonCapabilities.getServiceProvider();
        }

        //we enter the operation Metadata
        if (sections.getSection().contains("OperationsMetadata") || sections.getSection().contains(ALL)) {

           om = skeletonCapabilities.getOperationsMetadata();

           //we remove the operation not supported in this profile (transactional/discovery)
           if (profile == DISCOVERY) {
               om.removeOperation("InsertObservation");
               om.removeOperation("RegisterSensor");
           }


           //we update the URL
           OGCWebService.updateOWSURL(om.getOperation(), serviceURL, SOS);


           //we update the parameter in operation metadata.
           final Operation go = om.getOperation("GetObservation");

           // the list of offering names
           go.updateParameter(OFFERING, omReader.getOfferingNames());

           // the event time range
           final List<String> eventTime = omReader.getEventTime();
           if (eventTime != null && eventTime.size() == 1) {
               final RangeType range = new RangeType(eventTime.get(0), "now");
               go.updateParameter(EVENT_TIME, range);
           } else if (eventTime != null && eventTime.size() == 2) {
               final RangeType range = new RangeType(eventTime.get(0), eventTime.get(1));
               go.updateParameter(EVENT_TIME, range);
           }

           //the process list
           final Collection<String> procNames = omReader.getProcedureNames();
           go.updateParameter(PROCEDURE, procNames);

           //the phenomenon list
           go.updateParameter("observedProperty", omReader.getPhenomenonNames());

           //the feature of interest list
           go.updateParameter("featureOfInterest", omReader.getFeatureOfInterestNames());

           // the different responseMode available
           final List<String> arm = new ArrayList<String>();
           for (ResponseModeType rm: acceptedResponseMode) {
               arm.add(rm.value());
           }
           go.updateParameter(RESPONSE_MODE, arm);

           // the different responseFormat available
           go.updateParameter("responseFormat", acceptedResponseFormat);

           /**
            * Because sometimes there is some sensor that are queryable in DescribeSensor but not in GetObservation
            */
           final Operation ds = om.getOperation("DescribeSensor");
           if (smlReader != null) {
               ds.updateParameter(PROCEDURE, smlReader.getSensorNames());
           } else {
               ds.updateParameter(PROCEDURE, procNames);
           }

        }

        //we enter the information filter capablities.
        if (sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains(ALL)) {

            fc = skeletonCapabilities.getFilterCapabilities();
        }

        if (sections.getSection().contains("Contents") || sections.getSection().contains(ALL)) {
            // we add the list of observation ofeerings 
            final ObservationOfferingList ool = new ObservationOfferingList(omReader.getObservationOfferings());
            cont = new Contents(ool);
        }
        c = new Capabilities(si, sp, om, VERSION, null, fc, cont);

        // we normalize the document
        c = normalizeDocument(c);

        LOGGER.log(logLevel, "getCapabilities processed in " + (System.currentTimeMillis() - start) + "ms.\n");
        return c;
    }
    
    /**
     * Web service operation whitch return an sml description of the specified sensor.
     * 
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     */
    public AbstractSensorML describeSensor(DescribeSensor requestDescSensor) throws CstlServiceException  {
        LOGGER.log(logLevel, "DescribeSensor request processing"  + '\n');
        final long start = System.currentTimeMillis();

        // we get the form
        verifyBaseRequest(requestDescSensor);

        //we verify that the output format is good.
        final String out = requestDescSensor.getOutputFormat();
        if (out != null) {
            if (!StringUtilities.containsIgnoreCase(ACCEPTED_SENSORML_FORMATS, requestDescSensor.getOutputFormat())) {
                String msg = "Accepted values for outputFormat:";
                for (String s : ACCEPTED_SENSORML_FORMATS) {
                    msg = msg + '\n' + s;
                }
                throw new CstlServiceException(msg, INVALID_PARAMETER_VALUE, "outputFormat");
            }
        } else {
            String msg = "output format must be specify, accepted value are:";
            for (String s : ACCEPTED_SENSORML_FORMATS) {
                msg = msg + '\n' + s;
            }
            throw new CstlServiceException(msg, MISSING_PARAMETER_VALUE, "outputFormat");
        }

        // we verify that we have a sensor ID.
        final String sensorId = requestDescSensor.getProcedure();
        if (sensorId == null) {
            throw new CstlServiceException("You must specify the sensor ID!",
                                         MISSING_PARAMETER_VALUE, PROCEDURE);
        }
       

        AbstractSensorML result = smlReader.getSensor(sensorId);
        if (result instanceof SensorML && out.equalsIgnoreCase(SENSORML_101_FORMAT)) {
            result = SmlFactory.convertTo101((SensorML)result);
        }
        
        LOGGER.log(logLevel, "describeSensor processed in " + (System.currentTimeMillis() - start) + "ms.\n");
        return result;
    }
    
    /**
     * Web service operation whitch respond a collection of observation satisfying 
     * the restriction specified in the query.
     * 
     * @param requestObservation a document specifying the parameter of the request.
     */
    public Object getObservation(GetObservation requestObservation) throws CstlServiceException {
        LOGGER.log(logLevel, "getObservation request processing"  + '\n');
        final long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        verifyBaseRequest(requestObservation);

        // we clone the filter for this request
        final ObservationFilter localOmFilter = sosFactory.cloneObservationFilter(omFilter);


        //we verify that the output format is good.     
        if (requestObservation.getResponseFormat() != null) {
            if (!acceptedResponseFormat.contains(requestObservation.getResponseFormat())) {
                final StringBuilder arf = new StringBuilder();
                for (String s : acceptedResponseFormat) {
                    arf.append(s).append('\n');
                }
                throw new CstlServiceException(requestObservation.getResponseFormat() + " is not accepted for responseFormat.\n" +
                                               "Accepted values are:\n" + arf.toString(),
                                               INVALID_PARAMETER_VALUE, "responseFormat");
            }
        } else {
            final StringBuilder arf = new StringBuilder();
            for (String s : acceptedResponseFormat) {
                arf.append(s).append('\n');
            }
            throw new CstlServiceException("Response format must be specify\n" +
                    "Accepted values are:\n" + arf.toString(),
                    MISSING_PARAMETER_VALUE, "responseFormat");
        }

        QName resultModel = requestObservation.getResultModel();
        if (resultModel == null) {
            resultModel = OBSERVATION_QNAME;
        }

        //we get the mode of result
        boolean template   = false;
        boolean outOfBand  = false;
        ResponseModeType mode;
        if (requestObservation.getResponseMode() == null) {
            mode = INLINE;
        } else {
            try {
                mode = ResponseModeType.fromValue(requestObservation.getResponseMode());
            } catch (IllegalArgumentException e) {
                String arm = "";
                for (ResponseModeType s : acceptedResponseMode) {
                    arm = arm + s.value() + '\n';
                }
                throw new CstlServiceException("The response Mode: " + requestObservation.getResponseMode() + " is not supported by the service." +
                                               "Supported Values are:\n" + arm,
                                                 INVALID_PARAMETER_VALUE, RESPONSE_MODE);
            }
        }
        try {
            localOmFilter.initFilterObservation(mode, resultModel);
        } catch (IllegalArgumentException ex) {
            throw new CstlServiceException(ex);
        }

        if (mode == OUT_OF_BAND) {
            outOfBand = true;
        } else if (mode == RESULT_TEMPLATE) {
            template = true;
        } else if (!acceptedResponseMode.contains(mode)) {
            String arm = "";
            for (ResponseModeType s : acceptedResponseMode) {
                arm = arm + s.value() + '\n';
            }
            throw new CstlServiceException("This response Mode is not supported by the service" + 
                                           "Supported Values are:\n" + arm,
                                             OPERATION_NOT_SUPPORTED, RESPONSE_MODE);
        }

        ObservationOfferingEntry off;
        //we verify that there is an offering
        if (requestObservation.getOffering() == null) {
            throw new CstlServiceException("Offering must be specify!",
                                             MISSING_PARAMETER_VALUE, OFFERING);
        } else {
            off = omReader.getObservationOffering(requestObservation.getOffering());
            if (off == null) {
                throw new CstlServiceException("This offering is not registered in the service",
                                              INVALID_PARAMETER_VALUE, OFFERING);
            }
        }

        //we verify that the srsName (if there is one) is advertised in the offering
        if (requestObservation.getSrsName() != null) {
            if (!off.getSrsName().contains(requestObservation.getSrsName())) {
                String availableSrs = "";
                for (String s : off.getSrsName()) {
                    availableSrs = availableSrs + s + '\n';
                }
                throw new CstlServiceException("This srs name is not advertised in the offering.\n" +
                                               "Available srs name are:\n" + availableSrs,
                                                INVALID_PARAMETER_VALUE, "srsName");
            }
        }

        //we verify that the resultModel (if there is one) is advertised in the offering
        if (requestObservation.getResultModel() != null) {
            if (!off.getResultModel().contains(requestObservation.getResultModel())) {
                String availableRM = "";
                for (QName s : off.getResultModel()) {
                    availableRM = availableRM + s + '\n';
                }
                throw new CstlServiceException("This result model is not advertised in the offering:" + requestObservation.getResultModel() + '\n' +
                                               "Available result model for this offering are:",
                                                INVALID_PARAMETER_VALUE, "resultModel");
            }
        }

        //we get the list of process
        final List<String> procedures = requestObservation.getProcedure();
        for (String s : procedures) {
            if (s != null) {
                String dbId = map.getProperty(s);
                if (dbId == null) {
                    dbId = s;
                }
                LOGGER.log(logLevel, "process ID: " + dbId);
                final ReferenceEntry proc = omReader.getReference(dbId);
                if (proc == null) {
                    throw new CstlServiceException(" this process is not registred in the table",
                            INVALID_PARAMETER_VALUE, PROCEDURE);
                }
                if (!off.getProcedure().contains(proc)) {
                    throw new CstlServiceException(" this process is not registred in the offering",
                            INVALID_PARAMETER_VALUE, PROCEDURE);
                }
            } else {
                //if there is only one proccess null we return error (we'll see)
                if (procedures.size() == 1) {
                    throw new CstlServiceException("the process is null",
                            INVALID_PARAMETER_VALUE, PROCEDURE);
                }
            }
        }
        localOmFilter.setProcedure(procedures, off);

        //we get the list of phenomenon
        //TODO verifier que les pheno appartiennent a l'offering
        final List<String> observedProperties = requestObservation.getObservedProperty();
        if (observedProperties != null && observedProperties.size() != 0) {
            final List<String> singlePhenomenons    = new ArrayList<String>();
            final List<String> compositePhenomenons = new ArrayList<String>();
            for (String phenomenonName : observedProperties) {
                
                if (!phenomenonName.equals(phenomenonIdBase + "ALL")) {
                    
                    // we remove the phenomenon id base
                    if (phenomenonName.indexOf(phenomenonIdBase) != -1) {
                        phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
                    }
                    final Phenomenon phen = omReader.getPhenomenon(phenomenonName);
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
            }
            if (singlePhenomenons.size() > 0 || compositePhenomenons.size() > 0) {
                localOmFilter.setObservedProperties(singlePhenomenons, compositePhenomenons);
            }
        }


        //we treat the time restriction
        final List<EventTime> times = requestObservation.getEventTime();
        final AbstractTimeGeometricPrimitiveType templateTime = treatEventTimeRequest(times, template, localOmFilter);

        //we treat the restriction on the feature of interest
        if (requestObservation.getFeatureOfInterest() != null) {
            final GetObservation.FeatureOfInterest foiRequest = requestObservation.getFeatureOfInterest();

            // if the request is a list of station
            if (!foiRequest.getObjectID().isEmpty()) {

                //verify that the station is registred in the DB.
                final Collection<String> fois = omReader.getFeatureOfInterestNames();
                for (final String samplingFeatureName : foiRequest.getObjectID()) {
                    if (!fois.contains(samplingFeatureName))
                        throw new CstlServiceException("the feature of interest "+ samplingFeatureName + " is not registered",
                                                         INVALID_PARAMETER_VALUE, "featureOfInterest");
                }
                localOmFilter.setFeatureOfInterest(foiRequest.getObjectID());

            // if the request is a spatial operator
            } else {
                // for a BBOX Spatial ops
                if (foiRequest.getBBOX() != null) {
                    final EnvelopeEntry e = foiRequest.getBBOX().getEnvelope();

                    if (isWellFormedEnvelope(e)) {
                        boolean add = false;
                        final List<String> matchingFeatureOfInterest = new ArrayList<String>();
                        if (localOmFilter.isBoundedObservation()) {
                            localOmFilter.setBoundingBox(e);
                        } else {
                            for (ReferenceEntry refStation : off.getFeatureOfInterest()) {
                                final SamplingFeature station = (SamplingFeature) omReader.getFeatureOfInterest(refStation.getHref());
                                if (station == null)
                                    throw new CstlServiceException("the feature of interest is not registered",
                                            INVALID_PARAMETER_VALUE);
                                if (station instanceof SamplingPointEntry) {
                                    final SamplingPointEntry sp = (SamplingPointEntry) station;
                                    if (sp.getPosition() != null && sp.getPosition().getPos() != null && sp.getPosition().getPos().getValue().size() >= 2) {

                                        final double stationX  = sp.getPosition().getPos().getValue().get(0);
                                        final double stationY  = sp.getPosition().getPos().getValue().get(1);
                                        final double minx      = e.getLowerCorner().getValue().get(0);
                                        final double maxx      = e.getUpperCorner().getValue().get(0);
                                        final double miny      = e.getLowerCorner().getValue().get(1);
                                        final double maxy      = e.getUpperCorner().getValue().get(1);
                                        
                                        // we look if the station if contained in the BBOX
                                        if (stationX < maxx && stationX > minx &&
                                            stationY < maxy && stationY > miny) {

                                            matchingFeatureOfInterest.add(sp.getId());
                                            add = true;
                                        } else {
                                            LOGGER.finer(" the feature of interest " + sp.getId() + " is not in the BBOX");
                                        }
                                    } else {
                                        LOGGER.warning(" the feature of interest " + sp.getId() + " does not have proper position");
                                    }
                                } else if (station instanceof SamplingCurveType) {
                                    final SamplingCurveType sc = (SamplingCurveType) station;
                                    if (sc.getBoundedBy() != null && sc.getBoundedBy().getEnvelope() != null &&
                                        sc.getBoundedBy().getEnvelope().getLowerCorner() != null && sc.getBoundedBy().getEnvelope().getUpperCorner() != null &&
                                        sc.getBoundedBy().getEnvelope().getLowerCorner().getValue().size() > 1 && sc.getBoundedBy().getEnvelope().getUpperCorner().getValue().size() > 1) {

                                        final double stationMinX  = sc.getBoundedBy().getEnvelope().getLowerCorner().getValue().get(0);
                                        final double stationMaxX  = sc.getBoundedBy().getEnvelope().getUpperCorner().getValue().get(0);
                                        final double stationMinY  = sc.getBoundedBy().getEnvelope().getLowerCorner().getValue().get(1);
                                        final double stationMaxY  = sc.getBoundedBy().getEnvelope().getUpperCorner().getValue().get(1);
                                        final double minx         = e.getLowerCorner().getValue().get(0);
                                        final double maxx         = e.getUpperCorner().getValue().get(0);
                                        final double miny         = e.getLowerCorner().getValue().get(1);
                                        final double maxy         = e.getUpperCorner().getValue().get(1);

                                        // we look if the station if contained in the BBOX
                                        if (stationMaxX < maxx && stationMinX > minx &&
                                            stationMaxY < maxy && stationMinY > miny) {

                                            matchingFeatureOfInterest.add(sc.getId());
                                            add = true;
                                        } else {
                                            LOGGER.finer(" the feature of interest " + sc.getId() + " is not in the BBOX");
                                        }
                                    } else {
                                        LOGGER.warning(" the feature of interest (samplingCurve)" + sc.getId() + " does not have proper bounds");
                                    }
                                } else {
                                    LOGGER.warning("unknow implementation:" + station.getClass().getName());
                                }
                            }
                            if (add) {
                                localOmFilter.setFeatureOfInterest(matchingFeatureOfInterest);
                            // if there is no matching FOI we must return an empty result
                            } else {
                                return new ObservationCollectionEntry("urn:ogc:def:nil:OGC:inapplicable");
                            }
                        }
                        
                    } else {
                        throw new CstlServiceException("the envelope is not build correctly", INVALID_PARAMETER_VALUE);
                    }
                } else {
                    throw new CstlServiceException(NOT_SUPPORTED, OPERATION_NOT_SUPPORTED);
                }
            }

        }

        //TODO we treat the restriction on the result
        if (requestObservation.getResult() != null) {
            final GetObservation.Result result = requestObservation.getResult();

            //we treat the different operation
            if (result.getPropertyIsLessThan() != null) {

                final String propertyName  = result.getPropertyIsLessThan().getPropertyName();
                final LiteralType literal  = result.getPropertyIsLessThan().getLiteral() ;
                if (literal == null || propertyName == null || propertyName.equals("")) {
                    throw new CstlServiceException(" to use the operation Less Than you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "lessThan");
                }


            } else if (result.getPropertyIsGreaterThan() != null) {

                final String propertyName  = result.getPropertyIsGreaterThan().getPropertyName();
                final LiteralType literal  = result.getPropertyIsGreaterThan().getLiteral();
                if (propertyName == null || propertyName.equals("") || literal == null) {
                    throw new CstlServiceException(" to use the operation Greater Than you must specify the propertyName and the litteral",
                                                 MISSING_PARAMETER_VALUE, "greaterThan");
                }

            } else if (result.getPropertyIsEqualTo() != null) {

                final String propertyName  = result.getPropertyIsEqualTo().getPropertyName();
                final LiteralType literal  = result.getPropertyIsEqualTo().getLiteral();
                if (propertyName == null || propertyName.equals("") || literal == null) {
                     throw new CstlServiceException(" to use the operation Equal you must specify the propertyName and the litteral",
                                                   INVALID_PARAMETER_VALUE, "propertyIsEqualTo"); // cite test
                }


            } else if (result.getPropertyIsLike() != null) {
                throw new CstlServiceException(NOT_SUPPORTED, OPERATION_NOT_SUPPORTED, "propertyIsLike");

            } else if (result.getPropertyIsBetween() != null) {

                if (result.getPropertyIsBetween().getPropertyName() == null) {
                    throw new CstlServiceException("To use the operation Between you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "propertyIsBetween");
                }
                
                final String propertyName       = result.getPropertyIsBetween().getPropertyName();
                final LiteralType lowerLiteral  = result.getPropertyIsBetween().getLowerBoundary().getLiteral();
                final LiteralType upperLiteral  = result.getPropertyIsBetween().getUpperBoundary().getLiteral();

                if (propertyName == null || propertyName.equals("") || lowerLiteral == null || upperLiteral == null) {
                        throw new CstlServiceException("This property name, lower and upper literal must be specify",
                                                      INVALID_PARAMETER_VALUE, "result");
                }

            } else {
                throw new CstlServiceException(NOT_SUPPORTED,OPERATION_NOT_SUPPORTED);
            }
        }

        Object response;
        if (!outOfBand) {

            ObservationCollectionEntry ocResponse = new ObservationCollectionEntry();
            
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
            if (!(localOmFilter instanceof ObservationFilterReader)) {
                final List<String> observationIDs = localOmFilter.filterObservation();
                for (String observationID : observationIDs) {
                    matchingResult.add(omReader.getObservation(observationID, resultModel));
                }

            // case (2)
            } else {
                final ObservationFilterReader omFR = (ObservationFilterReader) localOmFilter;
                if (template) {
                    matchingResult = omFR.getObservationTemplates();
                } else {
                    matchingResult = omFR.getObservations();
                }
            }

            for (Observation o : matchingResult) {
                if (template) {

                    final String temporaryTemplateId = o.getName() + '-' + getTemplateSuffix(o.getName());
                    final ObservationEntry temporaryTemplate = ((ObservationEntry) o).getTemporaryTemplate(temporaryTemplateId, templateTime);
                    templates.put(temporaryTemplateId, temporaryTemplate);

                    // we launch a timer which will destroy the template in one hours
                    final Timer t = new Timer();
                    //we get the date and time for now
                    final Date d = new Date(System.currentTimeMillis() + templateValidTime);
                    LOGGER.log(logLevel, "this template will be destroyed at:" + d.toString());
                    t.schedule(new DestroyTemplateTask(temporaryTemplateId), d);
                    schreduledTask.add(t);

                    ocResponse.add(temporaryTemplate);
                } else {
                    ocResponse.add((ObservationEntry) o);
                }
            }
            ocResponse = regroupObservation(ocResponse);
            ocResponse.setId("collection-1");
            // this is a little hack for cite test dummy srsName comparaison
            String srsName = "urn:ogc:def:crs:EPSG::4326";
            if ("EPSG:4326".equals(requestObservation.getSrsName())) {
                srsName ="EPSG:4326";
            }
            ocResponse.setBoundedBy(getCollectionBound(ocResponse, srsName));
            ocResponse = normalizeDocument(ocResponse);
            response = ocResponse;
        } else {
            String sReponse = "";
            if (localOmFilter instanceof ObservationFilterReader) {
                sReponse = ((ObservationFilterReader)localOmFilter).getOutOfBandResults();
            } else {
                throw new CstlServiceException("Out of band response mode has been implemented only for ObservationFilterReader for now", NO_APPLICABLE_CODE, RESPONSE_MODE);
            }
            response = sReponse;
        }
        LOGGER.log(logLevel, "getObservation processed in " + (System.currentTimeMillis() - start) + "ms.\n");
        return response;
    }

    
    /**
     * Web service operation
     */
    public GetResultResponse getResult(GetResult requestResult) throws CstlServiceException {
        LOGGER.log(logLevel, "getResult request processing"  + '\n');
        final long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        verifyBaseRequest(requestResult);

        // we clone the filter for this request
        final ObservationFilter localOmFilter = sosFactory.cloneObservationFilter(omFilter);
        
        ObservationEntry template = null;
        if (requestResult.getObservationTemplateId() != null) {
            final String id = requestResult.getObservationTemplateId();
            template = templates.get(id);
            if (template == null) {
                throw new CstlServiceException("this template does not exist or is no longer usable",
                                              INVALID_PARAMETER_VALUE, "ObservationTemplateId");
            }
        } else {
            throw new CstlServiceException("ObservationTemplateID must be specified",
                                          MISSING_PARAMETER_VALUE, "ObservationTemplateId");
        }
        
        final QName resultModel;
        if (template instanceof MeasurementEntry) {
            resultModel = MEASUREMENT_QNAME;
        } else {
            resultModel = OBSERVATION_QNAME;
        }
        
        //we begin to create the sql request
        localOmFilter.initFilterGetResult(template, resultModel);
        
        //we treat the time constraint
        final List<EventTime> times = requestResult.getEventTime();

        /**
         * The template time :
         */ 
        
        // case TEquals with time instant
        if (template.getSamplingTime() instanceof TimeInstantType) {
           final TimeInstantType ti           = (TimeInstantType) template.getSamplingTime();
           final BinaryTemporalOpType equals  = new BinaryTemporalOpType(ti);
           final EventTime e                  = new EventTime(equals);
           times.add(e);
        
        } else if (template.getSamplingTime() instanceof TimePeriodType) {
            final TimePeriodType tp = (TimePeriodType) template.getSamplingTime();
            
            //case TBefore
            if (tp.getBeginPosition().equals(new TimePositionType(TimeIndeterminateValueType.BEFORE))) {
                final BinaryTemporalOpType before  = new BinaryTemporalOpType(new TimeInstantType(tp.getEndPosition()));
                final EventTime e                  = new EventTime(null, before, null);
                times.add(e);
            
            //case TAfter    
            } else if (tp.getEndPosition().equals(new TimePositionType(TimeIndeterminateValueType.NOW))) {
                final BinaryTemporalOpType after  = new BinaryTemporalOpType(new TimeInstantType(tp.getBeginPosition()));
                final EventTime e                  = new EventTime(after, null, null);
                times.add(e);
            
            //case TDuring/TEquals  (here the sense of T_Equals with timePeriod is lost but not very usefull) 
            } else {
                final BinaryTemporalOpType during  = new BinaryTemporalOpType(tp);
                final EventTime e                  = new EventTime(null, null, during);
                times.add(e);
            }
        }
        
        //we treat the time constraint
        treatEventTimeRequest(times, false, localOmFilter);
        
        //we prepare the response document
        
        String values;
        if (localOmFilter instanceof ObservationFilterReader) {
            values = ((ObservationFilterReader)localOmFilter).getResults();
            
        } else {
            final List<ObservationResult> results = localOmFilter.filterResult();
            final StringBuilder datablock         = new StringBuilder();
            
            for (ObservationResult result: results) {
                final Timestamp tBegin = result.beginTime;
                final Timestamp tEnd   = result.endTime;
                final Object r         = omReader.getResult(result.resultID, resultModel);
                if (r instanceof AnyResult) {
                    final DataArray array = ((AnyResult)r).getArray();
                    if (array != null) {
                        final String resultValues = getResultValues(tBegin, tEnd, array, times);
                        datablock.append(resultValues).append('\n');
                    } else {
                        throw new IllegalArgumentException("Array is null");
                    }
                } else if (r instanceof Measure) {
                    final Measure meas = (Measure) r;
                    datablock.append(tBegin).append(',').append(meas.getValue()).append("@@");
                }

            }
            values = datablock.toString();
        }
        final GetResultResponse.Result r = new GetResultResponse.Result(values, serviceURL + '/' + requestResult.getObservationTemplateId());
        final GetResultResponse response = new GetResultResponse(r);
        LOGGER.log(logLevel, "GetResult processed in " + (System.currentTimeMillis() - start) + "ms");
        return response;
    }
    
    private String getResultValues(Timestamp tBegin, Timestamp tEnd, DataArray array, List<EventTime> eventTimes) throws CstlServiceException {
        String values = null;
        
        //for multiple observations we parse the brut values (if we got a time constraint)
        if (tBegin != null && tEnd != null) {

            values = array.getValues();
            
            for (EventTime bound: eventTimes) {
                LOGGER.finer(" Values: " + values);
                if (bound.getTEquals() != null) {
                    if (bound.getTEquals().getRest().get(0) instanceof TimeInstantType) {
                        final TimeInstantType ti    = (TimeInstantType) bound.getTEquals().getRest().get(0);
                        final Timestamp boundEquals = Timestamp.valueOf(getTimeValue(ti.getTimePosition()));
                        
                        LOGGER.finer("TE case 1");
                        //case 1 the periods contains a matching values
                        values = parseDataBlock(values, array.getEncoding(), null, null, boundEquals);
                        
                    }
                    
                } else if (bound.getTAfter()  != null) {
                    final TimeInstantType ti   = (TimeInstantType) bound.getTAfter().getRest().get(0);
                    final Timestamp boundBegin = Timestamp.valueOf(getTimeValue(ti.getTimePosition()));
                    
                    // case 1 the period overlaps the bound 
                    if (tBegin.before(boundBegin) && tEnd.after(boundBegin)) {
                        LOGGER.finer("TA case 1");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, null, null);
                    
                    }
                        
                } else if (bound.getTBefore() != null) {
                    final TimeInstantType ti = (TimeInstantType) bound.getTBefore().getRest().get(0);
                    final Timestamp boundEnd = Timestamp.valueOf(getTimeValue(ti.getTimePosition()));
                    
                    // case 1 the period overlaps the bound 
                    if (tBegin.before(boundEnd) && tEnd.after(boundEnd)) {
                        LOGGER.finer("TB case 1");
                        values = parseDataBlock(values, array.getEncoding(), null, boundEnd, null);
                    
                    }
                    
                } else if (bound.getTDuring() != null) {
                    
                    final TimePeriodType tp = (TimePeriodType) bound.getTDuring().getRest().get(0);
                    final Timestamp boundBegin = Timestamp.valueOf(getTimeValue(tp.getBeginPosition()));
                    final Timestamp boundEnd   = Timestamp.valueOf(getTimeValue(tp.getEndPosition()));
                    
                    // case 1 the period overlaps the first bound 
                    if (tBegin.before(boundBegin) && tEnd.before(boundEnd) && tEnd.after(boundBegin)) {
                        LOGGER.finer("TD case 1");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, boundEnd, null);

                    // case 2 the period overlaps the second bound    
                    } else if (tBegin.after(boundBegin) && tEnd.after(boundEnd) && tBegin.before(boundEnd)) {
                        LOGGER.finer("TD case 2");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, boundEnd, null);

                    // case 3 the period totaly overlaps the bounds
                    } else if (tBegin.before(boundBegin) && tEnd.after(boundEnd)) {
                        LOGGER.finer("TD case 3");
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
                final TextBlock encoding        = (TextBlock) abstractEncoding;
                final StringTokenizer tokenizer = new StringTokenizer(brutValues, encoding.getBlockSeparator());
                int i = 1;
                while (tokenizer.hasMoreTokens()) {
                    final String block = tokenizer.nextToken();
                    LOGGER.finer(i + " eme block =" + block);
                    i++;
                    String samplingTimeValue = block.substring(0, block.indexOf(encoding.getTokenSeparator()));
                    samplingTimeValue = samplingTimeValue.replace('T', ' ');
                    Date d = null;
                    for (DateFormat dateformat : DATE_FORMATS) {
                        try {
                            synchronized (dateformat) {
                                d = dateformat.parse(samplingTimeValue);
                                break;
                            }
                        } catch (ParseException ex) {
                            LOGGER.finer("unable to parse the value: " + samplingTimeValue);
                        }
                    }
                    if (d == null) {
                        LOGGER.severe("unable to parse the value: " + samplingTimeValue);
                        continue;
                    }
                    final Timestamp t = new Timestamp(d.getTime());
                    
                    // time during case
                    if (boundBegin != null && boundEnd != null) {
                        if (t.after(boundBegin) && t.before(boundEnd)) {
                            values += block + encoding.getBlockSeparator();
                            LOGGER.finer("TD matching");
                        }
                        
                    //time after case    
                    } else if (boundBegin != null && boundEnd == null) {
                        if (t.after(boundBegin)) {
                            values += block + encoding.getBlockSeparator();
                            LOGGER.finer("TA matching");
                        }
                    
                    //time before case    
                    } else if (boundBegin == null && boundEnd != null) {
                        if (t.before(boundEnd)) {
                            values += block + encoding.getBlockSeparator();
                            LOGGER.finer("TB matching");
                        }
                        
                    //time equals case    
                    } else if (boundEquals != null) {
                        if (t.equals(boundEquals)) {
                            values += block + encoding.getBlockSeparator();
                            LOGGER.finer("TE matching");
                        }
                    }
                }
            } else {
                LOGGER.severe("unable to parse datablock unknown encoding");
                values = brutValues;
            }
        return values;
    }

    public AbstractFeatureEntry getFeatureOfInterest(GetFeatureOfInterest request) throws CstlServiceException {
        verifyBaseRequest(request);
        LOGGER.log(logLevel, "GetFeatureOfInterest request processing"  + '\n');
        final long start = System.currentTimeMillis();

        // if there is no filter we throw an exception
        if (request.getEventTime().size() ==  0 && request.getFeatureOfInterestId().size() == 0 && request.getLocation() == null) {
            throw new CstlServiceException("You must choose a filter parameter: eventTime, featureId or location", MISSING_PARAMETER_VALUE);
        }

        // for now we don't support time filter on FOI
        if (request.getEventTime().size() > 0) {
            throw new CstlServiceException("The time filter on feature Of Interest is not yet supported", OPERATION_NOT_SUPPORTED);
        }

        // we return a single result
        if (request.getFeatureOfInterestId().size() == 1) {
            final SamplingFeature singleResult = omReader.getFeatureOfInterest(request.getFeatureOfInterestId().get(0));
            if (singleResult == null) {
                throw new CstlServiceException("There is no such Feature Of Interest", INVALID_PARAMETER_VALUE);
            } else {
                return (SamplingFeatureEntry)singleResult;
            }

        // we return a featureCollection
        } else if (request.getFeatureOfInterestId().size() > 1) {
            final List<FeaturePropertyType> features = new ArrayList<FeaturePropertyType>();
            for (String featureID : request.getFeatureOfInterestId()) {
                final SamplingFeature feature = omReader.getFeatureOfInterest(featureID);
                if (feature == null) {
                    throw new CstlServiceException("There is no such Feature Of Interest", INVALID_PARAMETER_VALUE);
                } else {
                    features.add(buildFeatureProperty(feature));
                }
            }
            return new FeatureCollectionType("feature-collection-1", null, null, features);
        }

        if (request.getLocation() != null && request.getLocation().getSpatialOps() != null) {
            final SpatialOpsType spatialFilter = request.getLocation().getSpatialOps().getValue();
            if (spatialFilter instanceof BBOXType) {
                final List<SamplingFeature> result = spatialFiltering((BBOXType) spatialFilter);
                
                // we return a single result
                if (result.size() == 1) {
                    return (AbstractFeatureEntry) result.get(0);

                // we return a feature collection
                } else if (result.size() > 1) {
                    final List<FeaturePropertyType> features = new ArrayList<FeaturePropertyType>();
                    for (SamplingFeature feature : result) {
                        features.add(buildFeatureProperty(feature));
                    }
                    return new FeatureCollectionType("feature-collection-1", null, null, features);

                // if there is no response we send an error
                } else {
                    throw new CstlServiceException("There is no such Feature Of Interest", INVALID_PARAMETER_VALUE);
                }
            } else {
                throw new CstlServiceException("Only the filter BBOX is upported for now", OPERATION_NOT_SUPPORTED);
            }
        }
        // TODO never readh
        LOGGER.log(logLevel, "GetFeatureOfInterest processed in " + (System.currentTimeMillis() - start) + "ms");
        return null;
    }

    /**
     * Build the correct featurePropertyType from a sampling feature
     * 
     * @param feature
     * @return
     */
    private FeaturePropertyType buildFeatureProperty(SamplingFeature feature) {
        final ObjectFactory samplingFactory = new ObjectFactory();
        if (feature instanceof SamplingPointEntry) {
            return new FeaturePropertyType(samplingFactory.createSamplingPoint((SamplingPointEntry)feature));
        } else if (feature instanceof SamplingCurveType) {
            return new FeaturePropertyType(samplingFactory.createSamplingCurve((SamplingCurveType)feature));
        } else if (feature instanceof SamplingSolidType) {
            return new FeaturePropertyType(samplingFactory.createSamplingSolid((SamplingSolidType)feature));
        } else if (feature instanceof SamplingSurfaceType) {
            return new FeaturePropertyType(samplingFactory.createSamplingSurface((SamplingSurfaceType)feature));
        } else {
            LOGGER.warning("unexpected feature type:" + feature);
            return null;
        }
    }

    public List<SamplingFeature> spatialFiltering(BBOXType bbox) throws CstlServiceException {

        final EnvelopeEntry e = bbox.getEnvelope();
        if (isWellFormedEnvelope(e)) {

            final List<SamplingFeature> matchingFeatureOfInterest = new ArrayList<SamplingFeature>();
            final List<ObservationOfferingEntry> offerings        = omReader.getObservationOfferings();
            for (ObservationOfferingEntry off : offerings) {
                for (ReferenceEntry refStation : off.getFeatureOfInterest()) {
                    final SamplingFeature station = (SamplingFeature) omReader.getFeatureOfInterest(refStation.getHref());
                    if (station == null) {
                        LOGGER.warning("the feature of interest is not registered:" + refStation.getHref());
                        continue;
                    }
                    if (station instanceof SamplingPointEntry) {
                        final SamplingPointEntry sp = (SamplingPointEntry) station;
                        if (sp.getPosition() != null && sp.getPosition().getPos() != null && sp.getPosition().getPos().getValue().size() >= 2) {

                            final double stationX  = sp.getPosition().getPos().getValue().get(0);
                            final double stationY  = sp.getPosition().getPos().getValue().get(1);
                            final double minx      = e.getLowerCorner().getValue().get(0);
                            final double maxx      = e.getUpperCorner().getValue().get(0);
                            final double miny      = e.getLowerCorner().getValue().get(1);
                            final double maxy      = e.getUpperCorner().getValue().get(1);

                            // we look if the station if contained in the BBOX
                            if (stationX < maxx && stationX > minx && stationY < maxy && stationY > miny) {

                                matchingFeatureOfInterest.add(sp);
                            } else {
                                LOGGER.finer(" the feature of interest " + sp.getId() + " is not in the BBOX");
                            }
                        } else {
                            LOGGER.warning(" the feature of interest " + sp.getId() + " does not have proper position");
                        }
                    } else {
                        LOGGER.warning("unknow implementation:" + station.getClass().getName());
                    }
                }
            }
            return matchingFeatureOfInterest;
        } else {
            throw new CstlServiceException("the envelope is not build correctly", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Return True if the envellope got the sufficient 
     * 
     * @param env
     * @return
     */
    private boolean isWellFormedEnvelope(EnvelopeEntry env) {
        return env != null && env.getLowerCorner() != null && env.getUpperCorner() != null &&
               env.getLowerCorner().getValue().size() == 2 && env.getUpperCorner().getValue().size() == 2;
    }

    /**
     * Web service operation whitch register a Sensor in the SensorML database, 
     * and initialize its observation by adding an observation template in the O&M database.
     *
     * @param requestRegSensor A request containing a SensorML File describing a Sensor,
     *                         and an observation template for this sensor.
     */
    public RegisterSensorResponse registerSensor(RegisterSensor requestRegSensor) throws CstlServiceException {
        if (profile == DISCOVERY) {
            throw new CstlServiceException("The operation registerSensor is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");
        }
        LOGGER.log(logLevel, "registerSensor request processing"  + '\n');
        final long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        verifyBaseRequest(requestRegSensor);
        
        boolean success = false;
        String id = "";
        try {
            //we begin a transaction
            smlWriter.startTransaction();
            
            //we get the SensorML file who describe the Sensor to insert.
            final RegisterSensor.SensorDescription d = requestRegSensor.getSensorDescription();
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
            final ObservationTemplate temp = requestRegSensor.getObservationTemplate();
            ObservationEntry obs           = null;
            if (temp != null) {
                obs = temp.getObservation();
            }
            if(obs == null) {
                throw new CstlServiceException("observation template must be specify",
                                              MISSING_PARAMETER_VALUE,
                                              OBSERVATION_TEMPLATE);
            } else if (!obs.isComplete()) {
                throw new CstlServiceException("observation template must specify at least the following fields: procedure ,observedProperty ,featureOfInterest, Result",
                                              INVALID_PARAMETER_VALUE,
                                              OBSERVATION_TEMPLATE);
            }
            
            //we create a new Identifier from the SensorML database
            String num = "";
            if (obs.getProcedure() instanceof ProcessEntry) {
                final ProcessEntry pentry = (ProcessEntry) obs.getProcedure();
                if (pentry.getHref() != null && pentry.getHref().startsWith(sensorIdBase)) {
                    id  = pentry.getHref();
                    num = id.substring(sensorIdBase.length());
                    LOGGER.log(logLevel, "using specified sensor ID:" + id + " num =" + num);
                }
            } 

            if (id.equals("")) {
                num = smlWriter.getNewSensorId() + "";
                id  = sensorIdBase + num;
            }
            
            //and we write it in the sensorML Database
            smlWriter.writeSensor(id, process);

            final String phyId = getPhysicalID(process);

            // we record the mapping between physical id and database id
            recordMapping(id, phyId);
            
            // and we record the position of the piezometer
            final DirectPositionType position = getSensorPosition(process);
            omWriter.recordProcedureLocation(phyId, position);
                                    
            //we assign the new capteur id to the observation template
            final ProcessEntry p = new ProcessEntry(id);
            obs.setProcedure(p);
            obs.setName(observationTemplateIdBase + num);
            LOGGER.finer(obs.toString());
            //we write the observation template in the O&M database
            omWriter.writeObservation(obs);
                   
            addSensorToOffering(process, obs);
            
           success = true; 

        } finally {
            if (!success) {
               smlWriter.abortTransaction();
               LOGGER.severe("Transaction failed");
            } else {
                smlWriter.endTransaction();
            }
        }
        
        LOGGER.log(logLevel, "registerSensor processed in " + (System.currentTimeMillis() - start) + "ms");
        return new RegisterSensorResponse(id);
    }
    
    /**
     * Web service operation whitch insert a new Observation for the specified sensor
     * in the O&M database.
     * 
     * @param requestInsObs an InsertObservation request containing an O&M object and a Sensor id.
     */
    public InsertObservationResponse insertObservation(InsertObservation requestInsObs) throws CstlServiceException {
        if (profile == DISCOVERY) {
            throw new CstlServiceException("The operation insertObservation is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");
        }
        LOGGER.log(logLevel, "InsertObservation request processing"  + '\n');
        final long start = System.currentTimeMillis();

        //we verify the base request attribute
        verifyBaseRequest(requestInsObs);
        
        String id = "";
        //we get the id of the sensor and we create a sensor object
        final String sensorId = requestInsObs.getAssignedSensorId();
        String num = null;
        if (sensorId.startsWith(sensorIdBase)) {
            num = sensorId.substring(sensorIdBase.length());
        } else {
            throw new CstlServiceException("The sensor identifier is not valid it must start with " + sensorIdBase,
                                         INVALID_PARAMETER_VALUE, "assignedSensorId");
        }
        final ProcessEntry proc = new ProcessEntry(sensorId);

        //we get the observation and we assign to it the sensor
        final ObservationEntry obs = requestInsObs.getObservation();
        if (obs != null) {
            obs.setProcedure(proc);
            obs.setName(omReader.getNewObservationId());
            LOGGER.finer("samplingTime received: " + obs.getSamplingTime());
            LOGGER.finer("template received:" + '\n' + obs.toString());
        } else {
            throw new CstlServiceException("The observation template must be specified",
                                             MISSING_PARAMETER_VALUE, OBSERVATION_TEMPLATE);
        }

        // Debug part
        if (verifySynchronization) {
            if (obs.getSamplingTime() instanceof TimeInstantType) {
               final TimeInstantType timeInstant = (TimeInstantType) obs.getSamplingTime();
                try {
                    final Date d = DATE_FORMATS.get(0).parse(timeInstant.getTimePosition().getValue());
                    final long t = System.currentTimeMillis() - d.getTime();
                    LOGGER.info("gap between time of reception and time of sampling: " + t + " ms (" + Utils.getPeriodDescription(t) + ')');
                } catch (ParseException ex) {
                    LOGGER.warning("unable to parse the samplingTime");
                }
            }
        }

        //we record the observation in the O&M database
       if (obs instanceof MeasurementEntry) {
           id = omWriter.writeMeasurement((MeasurementEntry)obs);
           LOGGER.log(logLevel, "new Measurement inserted: id = " + id + " for the sensor " + ((ProcessEntry)obs.getProcedure()).getName());
        } else {

            //in first we verify that the observation is conform to the template
            final ObservationEntry template = (ObservationEntry) omReader.getObservation(observationTemplateIdBase + num, OBSERVATION_QNAME);
            //if the observation to insert match the template we can insert it in the OM db
            if (obs.matchTemplate(template)) {
                if (obs.getSamplingTime() != null && obs.getResult() != null) {
                    id = omWriter.writeObservation(obs);
                    LOGGER.log(logLevel, "new observation inserted: id = " + id + " for the sensor " + ((ProcessEntry)obs.getProcedure()).getName());
                } else {
                    throw new CstlServiceException("The observation sampling time and the result must be specify",
                                                  MISSING_PARAMETER_VALUE, "samplingTime");
                }
            } else {
                throw new CstlServiceException(" The observation doesn't match with the template of the sensor",
                                              INVALID_PARAMETER_VALUE, "samplingTime");
            }
        }

        LOGGER.log(logLevel, "insertObservation processed in " + (System.currentTimeMillis() - start) + "ms");
        omFilter.refresh();
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
    private AbstractTimeGeometricPrimitiveType treatEventTimeRequest(List<EventTime> times, boolean template, ObservationFilter localOmFilter) throws CstlServiceException {
        
        //In template mode  his method return a temporal Object.
        AbstractTimeGeometricPrimitiveType templateTime = null;
        
        if (times.size() != 0) {
            
            for (EventTime time: times) {

                // The operation Time Equals
                if (time.getTEquals() != null && time.getTEquals().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    //String propertyName = time.getTEquals().getPropertyName();
                    final Object timeFilter   = time.getTEquals().getRest().get(0);
                    
                    if (!template) {
                        localOmFilter.setTimeEquals(timeFilter);
                        
                    } else if (timeFilter instanceof TimePeriodType || timeFilter instanceof TimeInstantType) {
                        templateTime = (AbstractTimeGeometricPrimitiveType) timeFilter;
                        
                    } else {
                        throw new CstlServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, EVENT_TIME);
                    }
                
                // The operation Time before    
                } else if (time.getTBefore() != null && time.getTBefore().getRest().size() != 0) {

                    // we get the property name (not used for now)
                    // String propertyName = time.getTBefore().getPropertyName();
                    final Object timeFilter   = time.getTBefore().getRest().get(0);

                    if (!template) {
                        localOmFilter.setTimeBefore(timeFilter);
                    } else if (timeFilter instanceof TimeInstantType) {
                        final TimeInstantType ti = (TimeInstantType)timeFilter;
                        templateTime = new TimePeriodType(TimeIndeterminateValueType.BEFORE, ti.getTimePosition());
                    } else {
                        throw new CstlServiceException("TM_Before operation require timeInstant!",
                                                      INVALID_PARAMETER_VALUE, EVENT_TIME);
                    }
                    
                // The operation Time after    
                } else if (time.getTAfter() != null && time.getTAfter().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    //String propertyName = time.getTAfter().getPropertyName();
                    final Object timeFilter   = time.getTAfter().getRest().get(0);

                    if (!template) {
                        localOmFilter.setTimeAfter(timeFilter);
                    } else if (timeFilter instanceof TimeInstantType) {
                        final TimeInstantType ti = (TimeInstantType)timeFilter;
                        templateTime = new TimePeriodType(ti.getTimePosition());
                        
                    } else {
                       throw new CstlServiceException("TM_After operation require timeInstant!",
                                                     INVALID_PARAMETER_VALUE, EVENT_TIME);
                    }
                    
                // The time during operation    
                } else if (time.getTDuring() != null && time.getTDuring().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    //String propertyName = time.getTDuring().getPropertyName();
                    final Object timeFilter   = time.getTDuring().getRest().get(0);

                    if (!template) {
                        localOmFilter.setTimeDuring(timeFilter);
                    }
                    if (timeFilter instanceof TimePeriodType) {
                        templateTime = (TimePeriodType)timeFilter;
                        
                    } else {
                        throw new CstlServiceException("TM_During operation require TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, EVENT_TIME);
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
                if (!request.getService().equals(SOS))  {
                    throw new CstlServiceException("service must be \"SOS\"!", INVALID_PARAMETER_VALUE, SERVICE);
                }
            } else {
                throw new CstlServiceException("service must be specified!", MISSING_PARAMETER_VALUE, SERVICE);
            }
            if (request.getVersion()!= null) {
                if (!request.getVersion().equals(VERSION)) {
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
     * @param sensor A sensorML object describing the sensor.
     * @param template The observation template for this sensor.
     *
     * @throws CstlServiceException If an error occurs during the the storage of offering in the datasource.
     */
    private void addSensorToOffering(AbstractSensorML sensor, Observation template) throws CstlServiceException {
     
        //we search which are the networks binded to this sensor
        final List<String> networkNames = getNetworkNames(sensor);

        final int size = networkNames.size();
        if (size == 0) {
            LOGGER.severe("There is no network in that SensorML file");
        }

        // for each network we create (or update) an offering
        for (String networkName : networkNames) {
            final String offeringName               = "offering-" + networkName;
            final ObservationOfferingEntry offering = omReader.getObservationOffering(offeringName);
            
            if (offering != null) {
                updateOffering(offering, template);
            } else {
                createOffering(offeringName, template);
            }
        }

        /*
         * then  we add the sensor to the global offering containing all the sensor
         */
        final ObservationOfferingEntry offering = omReader.getObservationOffering("offering-allSensor");
        if (offering != null) {
            updateOffering(offering, template);
        } else {
            createOffering("allSensor", template);
        }
    }

    /**
     * Update an offering by adding to it the phenomenons, procedures and features of interest.
     *
     * @param offering The offering to update
     * @param template An Observation template.
     * 
     * @throws CstlServiceException If the service does not succeed to update the offering in the datasource.
     */
    private void updateOffering(ObservationOfferingEntry offering, Observation template) throws CstlServiceException {

        //we add the new sensor to the offering
        OfferingProcedureEntry offProc = null;
        ReferenceEntry ref = omReader.getReference(((ProcessEntry) template.getProcedure()).getHref());
        if (!offering.getProcedure().contains(ref)) {
            if (ref == null) {
                ref = new ReferenceEntry(null, ((ProcessEntry) template.getProcedure()).getHref());
            }
            offProc = new OfferingProcedureEntry(offering.getId(), ref);
        }

        //we add the phenomenon to the offering
        OfferingPhenomenonEntry offPheno = null;
        if (template.getObservedProperty() != null && !offering.getObservedProperty().contains(template.getObservedProperty())) {
            offPheno = new OfferingPhenomenonEntry(offering.getId(), (PhenomenonEntry) template.getObservedProperty());
        }

        // we add the feature of interest (station) to the offering
        OfferingSamplingFeatureEntry offSF = null;
        if (template.getFeatureOfInterest() != null) {
            ref = omReader.getReference(((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
            if (!offering.getFeatureOfInterest().contains(ref)) {
                if (ref == null) {
                    ref = new ReferenceEntry(null, ((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
                }
                offSF = new OfferingSamplingFeatureEntry(offering.getId(), ref);
            }
        }
        omWriter.updateOffering(offProc, offPheno, offSF);
    }

    
    /**
     * Create a new Offering with the specified observation template
     * 
     * @param offeringName the name of the new offering (not including offering base name).
     * @param template An observation template used as a base for the offering.
     *
     * @throws CstlServiceException If the service does not succeed to store the offering in the datasource.
     */
    private void createOffering(String offeringName, Observation template) throws CstlServiceException {
       LOGGER.log(logLevel, "offering " + offeringName + " not present, first build");

        // TODO bounded by??? station?

        // for the eventime of the offering we take the time of now.
        final Timestamp t = new Timestamp(System.currentTimeMillis());
        final TimePeriodType time = new TimePeriodType(new TimePositionType(t.toString()));

        //we add the template process
        final ReferenceEntry process = new ReferenceEntry(null, ((ProcessEntry) template.getProcedure()).getHref());

        //we add the template phenomenon
        final PhenomenonEntry phenomenon = (PhenomenonEntry) template.getObservedProperty();

        //we add the template feature of interest
        final ReferenceEntry station;
        if (template.getFeatureOfInterest() != null) {
            station = new ReferenceEntry(null, ((SamplingFeatureEntry) template.getFeatureOfInterest()).getId());
        } else {
            station = null;
        }

        //we create a list of accepted responseMode (fixed)
        final List<ResponseModeType> responses = Arrays.asList(RESULT_TEMPLATE, INLINE);
        final List<QName> resultModel = Arrays.asList(OBSERVATION_QNAME, MEASUREMENT_QNAME);
        final List<String> offeringOutputFormat = Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
        final List<String> srsName = Arrays.asList("EPSG:4326");

        String description = "";
        if (offeringName.equals("allSensor")) {
            description = "Base offering containing all the sensors.";
        }
        // we create a the new Offering
        omWriter.writeOffering(new ObservationOfferingEntry(
                                            OFFERING_ID_BASE + offeringName,
                                            OFFERING_ID_BASE + offeringName,
                                            description,
                                            srsName,
                                            time,
                                            process,
                                            phenomenon,
                                            station,
                                            offeringOutputFormat,
                                            resultModel,
                                            responses));
    }
    
    /**
     * Return the current output format MIME type (default: application/xml).
     * 
     * @return The current output format MIME type (default: application/xml).
     */
    public String getOutputFormat() {
        if (outputFormat == null) {
            return MimeType.APP_XML;
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
            if (dbId != null && physicalID != null) {
                map.setProperty(physicalID, dbId);
                final File configDirectory = getConfigDirectory();
                if (configDirectory != null && configDirectory.exists() && configDirectory.isDirectory()) {
                    final File mappingFile     = new File(getConfigDirectory(), "mapping.properties");
                    final FileOutputStream out = new FileOutputStream(mappingFile);
                    map.store(out, "");
                    out.close();
                }
            }

        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("The service cannot build the temporary file",
                    NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
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
     * In some implementations there is no .constellation directory.
     *
     * we search first in the classes resource into /WEB-INF/classes/configuration
     * else if we don't find it we look for .constellation/sos_configuration directory
     *
     */
    private File getConfigDirectory() {

        /* Ifremer's server does not contain any .constellation directory, so the
         * configuration files are put under the WEB-INF/classes/configuration/ directory of the WAR file.
         */
        File configDir = FileUtilities.getDirectoryFromResource("configuration");

        // if not find we search also in WEB-INF//classes/sos_configuration
        if (configDir == null || !configDir.exists()) {
            configDir = FileUtilities.getDirectoryFromResource("sos_configuration");
        }

        // else we search the .constellation directory
        if (configDir == null || !configDir.exists()) {
            configDir = new File(ConfigDirectory.getConfigDirectory(), "sos_configuration");
        }

        if (configDir != null) {
            LOGGER.info("taking configuration from constellation directory: " + configDir.getPath());
        }
        return configDir;
    }

    /**
     * Redirect the logs into the specified folder.
     * if the parameter ID is null or empty it create a file named "cstl-sos.log"
     * else the file is named "ID-cstl-sos.log"
     *
     * @param ID The ID of the service in a case of multiple sos server.
     * @param filePath The path to the log folder.
     */
    private void initLogger(String id, String filePath) {
        try {
            if (id != null && !id.equals("")) {
                id = id + '-';
            }
            final FileHandler handler  = new FileHandler(filePath + '/'+ id + "cstl-sos.log");
            handler.setFormatter(new MonolineFormatter(handler));
            LOGGER.addHandler(handler);
        } catch (IOException ex) {
            LOGGER.severe("IO exception while trying to separate CSW Logs:" + ex.getMessage());
        } catch (SecurityException ex) {
            LOGGER.severe("Security exception while trying to separate CSW Logs" + ex.getMessage());
        }
    }

    /**
     * Destroy and free the resource used by the worker.
     */
    public void destroy() {
        if (smlReader != null)
            smlReader.destroy();
        if (smlWriter != null)
            smlWriter.destroy();
        if (omReader != null)
            omReader.destroy();
        if (omWriter != null)
            omWriter.destroy();
        for (Timer t : schreduledTask) {
            t.cancel();
        }
        isStarted = false;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
        if (omFilter != null) {
            omFilter.setLoglevel(logLevel);
        }
        if (smlReader instanceof AbstractMetadataReader) {
            ((AbstractMetadataReader)smlReader).setLogLevel(logLevel);
        }
        if (smlWriter instanceof AbstractMetadataWriter) {
            ((AbstractMetadataWriter)smlWriter).setLogLevel(logLevel);
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
        @Override
        public void run() {
            templates.remove(templateId);
            LOGGER.log(logLevel, "template:" + templateId + " destroyed");
        }
    }
}
