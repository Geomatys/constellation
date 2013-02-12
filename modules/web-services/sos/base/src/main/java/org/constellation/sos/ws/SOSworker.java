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
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.sos.factory.SMLFactory;
import org.constellation.sos.factory.OMFactory;
import org.constellation.ServiceDef;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationFilterReader;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationResult;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.UnauthorizedException;

import static org.constellation.api.QueryConstants.*;
import static org.constellation.sos.ws.SOSConstants.*;
import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.Normalizer.*;

// Geotoolkit dependencies
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.gml.xml.TimeIndeterminateValueType;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.Range;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.Capabilities;
import org.geotoolkit.sos.xml.Contents;
import org.geotoolkit.sos.xml.GetCapabilities;
import org.geotoolkit.sos.xml.GetObservation;
import org.geotoolkit.sos.xml.GetObservationById;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;
import org.geotoolkit.sos.xml.GetResult;
import org.geotoolkit.sos.xml.GetResultResponse;
import org.geotoolkit.sos.xml.InsertObservation;
import org.geotoolkit.sos.xml.InsertObservationResponse;
import org.geotoolkit.sos.xml.InsertResultTemplate;
import org.geotoolkit.sos.xml.InsertResultTemplateResponse;
import org.geotoolkit.sos.xml.FilterCapabilities;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.ResultTemplate;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.gml.xml.AbstractFeature;
import org.geotoolkit.gml.xml.AbstractTimePosition;
import org.geotoolkit.gml.xml.DirectPosition;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureCollection;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.Process;
import org.geotoolkit.ogc.xml.XMLLiteral;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SmlFactory;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.swe.xml.AbstractEncoding;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.AnyResult;
import org.geotoolkit.swes.xml.DeleteSensor;
import org.geotoolkit.swes.xml.DeleteSensorResponse;
import org.geotoolkit.swes.xml.DescribeSensor;
import org.geotoolkit.swes.xml.InsertSensor;
import org.geotoolkit.swes.xml.ObservationTemplate;
import org.geotoolkit.swes.xml.InsertSensorResponse;
import org.geotoolkit.temporal.object.ISODateParser;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.logging.MonolineFormatter;
import org.geotoolkit.temporal.object.TemporalUtilities;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sos.xml.InsertResult;
import org.geotoolkit.sos.xml.InsertResultResponse;
import static org.geotoolkit.sos.xml.ResponseModeType.*;
import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.DataArrayProperty;

// GeoAPI dependencies
import org.opengis.observation.Observation;
import org.opengis.observation.CompositePhenomenon;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Measure;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;
import org.opengis.geometry.primitive.Point;
import org.opengis.observation.Measurement;
import org.opengis.observation.ObservationCollection;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.temporal.TemporalObject;
import org.opengis.temporal.TemporalPrimitive;
import org.opengis.util.CodeList;


/**
 *
 * @author Guilhem Legal (Geomatys).
 */
public class SOSworker extends AbstractWorker {

    public static final int DISCOVERY     = 0;
    public static final int TRANSACTIONAL = 1;

    /**
     * A list of temporary ObservationTemplate
     */
    private final Map<String, Observation> templates = new HashMap<String, Observation>();
    
    /**
     * A list of temporary resultTemplate
     */
    private final Map<String, ResultTemplate> resultTemplates = new HashMap<String, ResultTemplate>();

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    @Deprecated
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
     * The current MIME type of return
     */
    private String outputFormat;

    /**
     * A list of schreduled Task (used in close method).
     */
    private final List<Timer> schreduledTask = new ArrayList<Timer>();

    /**
     * A list of supported MIME type
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList(MimeType.TEXT_XML,
                                                MimeType.APPLICATION_XML,
                                                MimeType.TEXT_PLAIN);
    }

    /**
     * A list of supported SensorML version
     */
    private static final Map<String, List<String>> ACCEPTED_SENSORML_FORMATS = new HashMap<String, List<String>>();
    static {
        ACCEPTED_SENSORML_FORMATS.put("1.0.0", Arrays.asList(SENSORML_100_FORMAT_V100,
                                                             SENSORML_101_FORMAT_V100));
        ACCEPTED_SENSORML_FORMATS.put("2.0.0", Arrays.asList(SENSORML_100_FORMAT_V200,
                                                             SENSORML_101_FORMAT_V200));
    }

    /**
     * The profile of the SOS service (transational/discovery).
     */
    private int profile;

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
     * The factory to instanciate the O&M Readers / Writers / Filters
     */
    private OMFactory omFactory;

    /**
     * The factory to instanciate the SensorML Readers / Writers
     */
    private SMLFactory smlFactory;

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
     * A flag indicating if we have to store in cache the capabilities document.
     */
    private boolean keepCapabilities;

    /**
     * if the flag keepCapabilities is set to true, this attribute will be fill with the reponse of a getCapabilities.
     */
    private Capabilities loadedCapabilities;

    private boolean alwaysFeatureCollection;

    /**
     * Initialize the database connection.
     */
    public SOSworker(final String id, final File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.SOS);
        setSupportedVersion(ServiceDef.SOS_2_0_0, ServiceDef.SOS_1_0_0);
        isStarted = true;
        final SOSConfiguration configuration;

        // Database configuration
        Unmarshaller configUM = null;
        try {
            configUM = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final File configFile = new File(configurationDirectory, "config.xml");
            if (configFile.exists()) {
                final Object object = configUM.unmarshal(configFile);
                if (object instanceof SOSConfiguration) {
                    configuration = (SOSConfiguration) object;
                } else {
                    startError = "The generic configuration file is malformed.";
                    LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\ncause: {0}", startError);
                    isStarted = false;
                    return;
                }
            } else {
                startError = "The configuration file can't be found.";
                LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\ncause: {0}", startError);
                isStarted = false;
                return;
            }

            if (configuration.getLogFolder() != null) {
                initLogger("", configuration.getLogFolder());
                LOGGER.log(Level.INFO, "Redirecting the log to: {0}", configuration.getLogFolder());
            }
            this.profile               = configuration.getProfile();
            this.verifySynchronization = configuration.isVerifySynchronization();
            this.keepCapabilities      = configuration.isKeepCapabilities();

            if (keepCapabilities) {
                loadCachedCapabilities(configurationDirectory);
            }

            // the file who record the map between phisycal ID and DB ID.
            loadMapping(configurationDirectory);

            //we get the O&M filter Type
            final DataSourceType omFilterType = configuration.getObservationFilterType();

            //we get the O&M reader Type
            final DataSourceType omReaderType = configuration.getObservationReaderType();

            //we get the O&M writer Type
            final DataSourceType omWriterType = configuration.getObservationWriterType();

            //we get the Sensor reader type
            final DataSourceType smlType = configuration.getSMLType();

            final Automatic smlConfiguration = configuration.getSMLConfiguration();
            if (smlConfiguration == null) {
                startError = "The configuration file does not contains a SML configuration.";
                LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\ncause: {0}", startError);
                isStarted = false;
                return;
            }
            smlConfiguration.setConfigurationDirectory(configurationDirectory);

            final Automatic omConfiguration = configuration.getOMConfiguration();
            if (omConfiguration == null) {
                startError = "The configuration file does not contains a O&M configuration.";
                LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\ncause: {0}", startError);
                isStarted = false;
                return;
            }
            omConfiguration.setConfigurationDirectory(configurationDirectory);



            //we initialize the properties attribute
            final String observationIdBase  = configuration.getObservationIdBase() != null ?
            configuration.getObservationIdBase() : "urn:ogc:object:observation:unknow:";

            sensorIdBase              = configuration.getSensorIdBase() != null ?
            configuration.getSensorIdBase() : "urn:ogc:object:sensor:unknow:";

            phenomenonIdBase          = configuration.getPhenomenonIdBase() != null ?
            configuration.getPhenomenonIdBase() : "urn:ogc:def:phenomenon:OGC:1.0.30:";

            observationTemplateIdBase = configuration.getObservationTemplateIdBase() != null ?
            configuration.getObservationTemplateIdBase() : "urn:ogc:object:observationTemplate:unknow:";

            alwaysFeatureCollection   = configuration.getParameters().containsKey(OMFactory.ALWAYS_FEATURE_COLLECTION) ?
            Boolean.parseBoolean(configuration.getParameters().get(OMFactory.ALWAYS_FEATURE_COLLECTION)) : false;


            // we fill a map of properties to sent to the reader/writer/filter
            final Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(OMFactory.OBSERVATION_ID_BASE, observationIdBase);
            properties.put(OMFactory.OBSERVATION_TEMPLATE_ID_BASE, observationTemplateIdBase);
            properties.put(OMFactory.SENSOR_ID_BASE, sensorIdBase);
            properties.put(OMFactory.PHENOMENON_ID_BASE, phenomenonIdBase);
            properties.put(OMFactory.IDENTIFIER_MAPPING, map);

            // we add the general parameters to the properties
            properties.putAll(configuration.getParameters());

            // we add the custom parameters to the properties
            properties.putAll(omConfiguration.getCustomparameters());
            properties.putAll(smlConfiguration.getCustomparameters());

            // look for template life limit
            int h, m;
            try {
                String validTime = configuration.getTemplateValidTime();
                if (validTime == null || validTime.isEmpty() || validTime.indexOf(':') == -1) {
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

            // we initialize the reader/writer
            if (!DataSourceType.NONE.equals(smlType)) {
                smlFactory = getSMLFactory(smlType);
                smlReader  = smlFactory.getSensorReader(smlType, smlConfiguration, properties);
                smlWriter  = smlFactory.getSensorWriter(smlType, smlConfiguration, properties);
            }

            // we initialize the O&M reader/writer/filter
            if (!DataSourceType.NONE.equals(omReaderType)) {
                omFactory = getOMFactory(omReaderType);
                omReader  = omFactory.getObservationReader(omReaderType, omConfiguration, properties);

                //we initialize the variables depending on the Reader capabilities
                this.acceptedResponseMode   = omReader.getResponseModes();
                this.acceptedResponseFormat = omReader.getResponseFormats();
            } else {
                this.acceptedResponseMode   = new ArrayList<ResponseModeType>();
                this.acceptedResponseFormat = new ArrayList<String>();
            }
            if (!DataSourceType.NONE.equals(omWriterType)) {
                omFactory = getOMFactory(omWriterType);
                omWriter  = omFactory.getObservationWriter(omWriterType, omConfiguration, properties);
            }
            if (!DataSourceType.NONE.equals(omFilterType)) {
                omFactory = getOMFactory(omFilterType);
                omFilter  = omFactory.getObservationFilter(omFilterType, omConfiguration, properties);
            }

            setLogLevel(configuration.getLogLevel());

            // look for transaction security
            final String ts = configuration.getParameters().get("transactionSecurized");
            if (ts != null && !ts.isEmpty()) {
                transactionSecurized = Boolean.parseBoolean(ts);
            }

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
            startError = msg;
            LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\n\ncause: JAXBException:{0}", msg);
            isStarted = false;
        } catch (FactoryNotFoundException ex) {
            startError =  "Unable to find a SOS Factory." + ex.getMessage();
            LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\ncause: {0}", startError);
            isStarted = false;
        } catch (MetadataIoException ex) {
            startError = "MetadataIOException while initializing the sensor reader/writer:\n" + ex.getMessage();
            LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\ncause: {0}", startError);
            isStarted = false;
        } catch (CstlServiceException ex) {
            startError = ex.getMessage();
            LOGGER.log(Level.WARNING, "\nThe SOS worker is not running!\ncause:{0}", startError);
            LOGGER.log(Level.FINER, "\nThe SOS worker is not running!", ex);
            isStarted = false;
        } finally {
            if (configUM != null) {
                GenericDatabaseMarshallerPool.getInstance().release(configUM);
            }
        }
    }

    /**
     * Select the good O&M factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private OMFactory getOMFactory(final DataSourceType type) {
        final Iterator<OMFactory> ite = ServiceRegistry.lookupProviders(OMFactory.class);
        while (ite.hasNext()) {
            OMFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No OM factory has been found for type:" + type);
    }

    /**
     * Select the good O&M factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private SMLFactory getSMLFactory(final DataSourceType type) {
        final Iterator<SMLFactory> ite = ServiceRegistry.lookupProviders(SMLFactory.class);
        while (ite.hasNext()) {
            SMLFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No SML factory has been found for type:" + type);
    }

    /**
     * Log some informations about the implementations classes for reader / writer / filter object.
     */
    private void logInfos() {
        final String loaded =  " loaded.\n";
        final StringBuilder infos = new StringBuilder();

        if (this.profile == DISCOVERY) {
            infos.append("Discovery profile loaded.\n");
        } else {
            infos.append("Transactional profile loaded.\n");
        }
        if (smlReader != null) {
            infos.append('\n').append(smlReader.getInfos()).append(loaded).append('\n');
        } else {
            infos.append("No SensorML reader loaded.\n");
        }
        if ( profile == TRANSACTIONAL) {
            if (smlWriter != null) {
                infos.append(smlWriter.getInfos()).append(loaded).append('\n');
            } else {
                infos.append("No SensorML writer loaded.\n");
            }
        }
        if (omReader != null) {
            infos.append(omReader.getInfos()).append(loaded).append('\n');
        } else {
            infos.append("No O&M reader loaded.\n");
        }
        if (omFilter != null) {
            infos.append(omFilter.getInfos()).append(loaded).append('\n');
        } else {
            infos.append("No O&M filter loaded.\n");
        }
        if ( profile == TRANSACTIONAL) {
            if (omWriter != null) {
                infos.append(omWriter.getInfos()).append(loaded).append('\n');
            } else {
                infos.append("No O&M writer loaded.\n");
            }
        }
        infos.append("SOS worker \"").append(getId()).append("\" running\n");
        LOGGER.info(infos.toString());
    }

    /**
     * Load the Capabilites document from a configuration file if its present.
     *
     * @param configurationDirectory
     * @throws JAXBException
     */
    private void loadCachedCapabilities(final File configurationDirectory) throws JAXBException {
        //we fill the cachedCapabilities if we have to
        LOGGER.info("adding capabilities document in cache");
        Unmarshaller capaUM = null;
        try {
            capaUM = SOSMarshallerPool.getInstance().acquireUnmarshaller();
            final File configFile = new File(configurationDirectory, "cached-offerings.xml");
            if (configFile.exists()) {
                Object object = capaUM.unmarshal(configFile);
                if (object instanceof JAXBElement) {
                    object = ((JAXBElement)object).getValue();
                }
                if (object instanceof Capabilities) {
                    loadedCapabilities = (Capabilities) object;
                } else {
                    LOGGER.severe("cached capabilities file does not contains Capablities object.");
                }
            }
        } finally {
            if (capaUM != null) {
                SOSMarshallerPool.getInstance().release(capaUM);
            }
        }
    }

    /**
     * Load the Mapping between database identifier/real Identifier
     *
     * @param configDirectory
     */
    private void loadMapping(final File configDirectory) {
        // the file who record the map between phisycal ID and DB ID.
        try {
            final File f = new File(configDirectory, "mapping.properties");
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
            LOGGER.log(Level.WARNING, "IO Exception while loading the mapping file:{0}", e.getMessage());
        }
    }

    /**
     * Web service operation describing the service and its capabilities.
     *
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     */
    public Capabilities getCapabilities(final GetCapabilities request) throws CstlServiceException {
        isWorking();
        LOGGER.log(logLevel, "getCapabilities request processing\n");
        final long start = System.currentTimeMillis();

        //we verify the base request attribute
        verifyBaseRequest(request, false, true);
        
        final String currentVersion = request.getVersion().toString();
        
        final AcceptFormats formats = request.getAcceptFormats();
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
            this.outputFormat = MimeType.APPLICATION_XML;
        }

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(request.getUpdateSequence());
        if (returnUS) {
            return buildCapabilities(currentVersion, getCurrentUpdateSequence());
        }
        
        Sections sections = request.getSections();
        if (sections == null) {
            sections = OWSXmlFactory.buildSections("1.1.0", Arrays.asList("All"));
        }
        
        // If the getCapabilities response is in cache, we just return it.
        final AbstractCapabilitiesCore cachedCapabilities = getCapabilitiesFromCache(currentVersion, null);
        if (cachedCapabilities != null) {
            return (Capabilities) cachedCapabilities.applySections(sections);
        }

        // we load the skeleton capabilities
        final Capabilities skeletonCapabilities = (Capabilities) getStaticCapabilitiesObject(currentVersion, "SOS");

        final Capabilities localCapabilities;
        if (keepCapabilities) {
            localCapabilities = loadedCapabilities;
        } else {
            localCapabilities = skeletonCapabilities;
        }

        //we prepare the different parts response document
        final AbstractServiceIdentification si = localCapabilities.getServiceIdentification();
        final AbstractServiceProvider       sp = localCapabilities.getServiceProvider();
        final FilterCapabilities fc;
        if (currentVersion.equals("2.0.0")) {
            fc = SOSConstants.SOS_FILTER_CAPABILITIES_V200;
        } else {
            fc = SOSConstants.SOS_FILTER_CAPABILITIES_V100;
        }
        final AbstractOperationsMetadata    om = OPERATIONS_METADATA.clone();

        //we remove the operation not supported in this profile (transactional/discovery)
        if (profile == DISCOVERY) {
            om.removeOperation("InsertObservation");
            om.removeOperation("RegisterSensor");
        }
        //we update the URL
        om.updateURL(getServiceUrl());

        if (!keepCapabilities) {

            //we update the parameter in operation metadata.
            final AbstractOperation go = om.getOperation("GetObservation");

            final Collection<String> foiNames;
            final Collection<String> procNames;
            final Collection<String> phenNames;
            final Collection<String> offNames;
            final List<String> eventTime;
            final List<String> queryableResultProperties;
            if (omReader != null) {
                foiNames  = omReader.getFeatureOfInterestNames();
                procNames = omReader.getProcedureNames();
                phenNames = omReader.getPhenomenonNames();
                offNames  = omReader.getOfferingNames(currentVersion);
                eventTime = omReader.getEventTime();
            } else {
                foiNames  = new ArrayList<String>();
                procNames = new ArrayList<String>();
                phenNames = new ArrayList<String>();
                offNames  = new ArrayList<String>();
                eventTime = new ArrayList<String>();
            }
            if (omFilter != null) {
                queryableResultProperties = omFilter.supportedQueryableResultProperties();
            } else {
                queryableResultProperties = new ArrayList<String>();
            }
            
            // the list of offering names
            go.updateParameter(OFFERING, offNames);

            // the event time range
            if (eventTime != null && eventTime.size() == 1) {
                final Range range = buildRange(currentVersion, eventTime.get(0), "now");
                go.updateParameter(EVENT_TIME, range);
            } else if (eventTime != null && eventTime.size() == 2) {
                final Range range = buildRange(currentVersion, eventTime.get(0), eventTime.get(1));
                go.updateParameter(EVENT_TIME, range);
            }

            //the process list
            go.updateParameter(PROCEDURE, procNames);

            //the phenomenon list
            go.updateParameter("observedProperty", phenNames);

            //the feature of interest list
            go.updateParameter("featureOfInterest", foiNames);
            
            // the different responseMode available
            final List<String> arm = new ArrayList<String>();
            for (ResponseModeType rm: acceptedResponseMode) {
                arm.add(rm.value());
            }
            go.updateParameter(RESPONSE_MODE, arm);

            // the different responseFormat available
            go.updateParameter("responseFormat", acceptedResponseFormat);

            // the result filtrable part
            if (!queryableResultProperties.isEmpty()) {
                go.updateParameter("result", queryableResultProperties);
            }

            /**
             * Because sometimes there is some sensor that are queryable in DescribeSensor but not in GetObservation
             */
            final AbstractOperation ds = om.getOperation("DescribeSensor");
            if (smlReader != null) {
                final List<String> sensorNames = new ArrayList<String>(smlReader.getSensorNames());
                Collections.sort(sensorNames);
                ds.updateParameter(PROCEDURE, sensorNames);
            } else {
                ds.updateParameter(PROCEDURE, procNames);
            }

            ds.updateParameter("outputFormat", ACCEPTED_SENSORML_FORMATS.get(currentVersion));

            final AbstractOperation gfoi = om.getOperation("GetFeatureOfInterest");
            if (gfoi != null) {
                //the feature of interest list
                gfoi.updateParameter("featureOfInterestId", foiNames);
            }

            final AbstractOperation gfoit = om.getOperation("GetFeatureOfInterestTime");
            if (gfoit != null) {
                //the feature of interest list
                gfoit.updateParameter("featureOfInterestId", foiNames);
            }
        }

        final Contents cont;
        if (keepCapabilities) {
            cont = loadedCapabilities.getContents();
        } else {
            // we add the list of observation offerings
            final List<ObservationOffering> offerings;
            if (omReader != null) {
                offerings = omReader.getObservationOfferings(currentVersion);
            } else {
                offerings = new ArrayList<ObservationOffering>();
            }
            cont = buildContents(currentVersion, offerings);
        }
        
        // we build and normalize the document
        final Capabilities temp = buildCapabilities(currentVersion, si, sp, om, getCurrentUpdateSequence(), fc, cont);
        final Capabilities c    = normalizeDocument(temp);
        LOGGER.log(logLevel, "getCapabilities processed in {0} ms.\n", (System.currentTimeMillis() - start));
        putCapabilitiesInCache(currentVersion, null, c);
        return (Capabilities) c.applySections(sections);
    }

    /**
     * Web service operation which return an sml description of the specified sensor.
     *
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     */
    public AbstractSensorML describeSensor(final DescribeSensor request) throws CstlServiceException  {
        LOGGER.log(logLevel, "DescribeSensor request processing\n");
        final long start = System.currentTimeMillis();

        // we get the form
        verifyBaseRequest(request, true, false);
        final String currentVersion = request.getVersion().toString();

        //we verify that the output format is good.
        final String out = request.getOutputFormat();
        if (out != null) {
            if (!StringUtilities.containsIgnoreCase(ACCEPTED_SENSORML_FORMATS.get(currentVersion), request.getOutputFormat())) {
                final StringBuilder msg = new StringBuilder("Accepted values for outputFormat:");
                for (String s : ACCEPTED_SENSORML_FORMATS.get(currentVersion)) {
                    msg.append('\n').append(s);
                }
                throw new CstlServiceException(msg.toString(), INVALID_PARAMETER_VALUE, "outputFormat");
            }
        } else {
            final StringBuilder msg = new StringBuilder("output format must be specify, accepted value are:");
            for (String s : ACCEPTED_SENSORML_FORMATS.get(currentVersion)) {
                msg.append('\n').append(s);
            }
            throw new CstlServiceException(msg.toString(), MISSING_PARAMETER_VALUE, "outputFormat");
        }

        // we verify that we have a sensor ID.
        final String sensorId = request.getProcedure();
        if (sensorId == null) {
            throw new CstlServiceException("You must specify the sensor ID!",
                                         MISSING_PARAMETER_VALUE, PROCEDURE);
        }


        AbstractSensorML result = smlReader.getSensor(sensorId);
        if (result instanceof SensorML && 
            out.equalsIgnoreCase(SENSORML_101_FORMAT_V100) || out.equalsIgnoreCase(SENSORML_101_FORMAT_V200)) {
            result = SmlFactory.convertTo101((SensorML)result);
        }

        LOGGER.log(logLevel, "describeSensor processed in {0} ms.\n", (System.currentTimeMillis() - start));
        return result;
    }
    
    public DeleteSensorResponse deleteSensor(final DeleteSensor request) throws CstlServiceException  {
        LOGGER.log(logLevel, "DescribeSensor request processing\n");
        final long start = System.currentTimeMillis();

        // we get the form
        verifyBaseRequest(request, true, false);
        final String currentVersion = request.getVersion().toString();

        // we verify that we have a sensor ID.
        final String sensorId = request.getProcedure();
        if (sensorId == null) {
            throw new CstlServiceException("You must specify the sensor ID!", MISSING_PARAMETER_VALUE, PROCEDURE);
        }
        final boolean result = smlWriter.deleteSensor(sensorId);
        if (result) {
            smlReader.removeFromCache(sensorId);
            LOGGER.log(logLevel, "describeSensor processed in {0} ms.\n", (System.currentTimeMillis() - start));
            return buildDeleteSensorResponse(currentVersion, sensorId);
        } else {
            throw new CstlServiceException("unable to delete sensor:" + sensorId);
        }
    }

    public Object getObservationById(final GetObservationById request) throws CstlServiceException {
        LOGGER.log(logLevel, "getObservation request processing\n");
        final long start = System.currentTimeMillis();

        //we verify the base request attribute
        verifyBaseRequest(request, true, false);

        final String currentVersion = request.getVersion().toString();
     
        final List<Observation> observation = new ArrayList<Observation>();
        for (String oid : request.getObservation()) {
            observation.add(omReader.getObservation(oid, OBSERVATION_QNAME, currentVersion));
        }
        final ObservationCollection response = buildGetObservationByIdResponse(currentVersion, "collection-1", null, observation);
        LOGGER.log(logLevel, "getObservationById processed in {0}ms.\n", (System.currentTimeMillis() - start));
        return response;
    }
    
    /**
     * Web service operation which respond a collection of observation satisfying
     * the restriction specified in the query.
     *
     * @param requestObservation a document specifying the parameter of the request.
     */
    public Object getObservation(final GetObservation requestObservation) throws CstlServiceException {
        LOGGER.log(logLevel, "getObservation request processing\n");
        final long start = System.currentTimeMillis();

        //we verify the base request attribute
        verifyBaseRequest(requestObservation, true, false);

        final String currentVersion = requestObservation.getVersion().toString();
        
        // we clone the filter for this request
        final ObservationFilter localOmFilter = omFactory.cloneObservationFilter(omFilter);


        //we verify that the output format is good.
        final String responseFormat = requestObservation.getResponseFormat();
        if (responseFormat != null) {
            if (!acceptedResponseFormat.contains(responseFormat)) {
                final StringBuilder arf = new StringBuilder();
                for (String s : acceptedResponseFormat) {
                    arf.append(s).append('\n');
                }
                throw new CstlServiceException(responseFormat + " is not accepted for responseFormat.\n" +
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

        // we set the response format on the filter reader
        if (localOmFilter instanceof ObservationFilterReader) {
            ((ObservationFilterReader)localOmFilter).setResponseFormat(responseFormat);
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
                final StringBuilder arm = new StringBuilder();
                for (ResponseModeType s : acceptedResponseMode) {
                    arm.append(s.value()).append('\n');
                }
                throw new CstlServiceException("The response Mode: " + requestObservation.getResponseMode() + " is not supported by the service." +
                                               "Supported Values are:\n" + arm.toString(),
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
            final StringBuilder arm = new StringBuilder();
            for (ResponseModeType s : acceptedResponseMode) {
                arm.append(s.value()).append('\n');
            }
            throw new CstlServiceException("This response Mode is not supported by the service" +
                                           "Supported Values are:\n" + arm.toString(),
                                             OPERATION_NOT_SUPPORTED, RESPONSE_MODE);
        }

        //we verify that there is an offering (mandatory in 1.0.0, optional in 2.0.0)
        final List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        final List<String> offeringNames = requestObservation.getOfferings();
        if (currentVersion.equals("1.0.0") && (offeringNames == null || offeringNames.isEmpty())) {
            throw new CstlServiceException("Offering must be specify!", MISSING_PARAMETER_VALUE, OFFERING);
        } else {
            for (String offeringName : offeringNames) {
                final ObservationOffering offering = omReader.getObservationOffering(offeringName, currentVersion);
                if (offering == null) {
                    throw new CstlServiceException("This offering is not registered in the service", INVALID_PARAMETER_VALUE, OFFERING);
                }
                offerings.add(offering);
            }
        }
        localOmFilter.setOfferings(offerings);

        //we verify that the srsName (if there is one) is advertised in the offering
        if (requestObservation.getSrsName() != null) {
            for (ObservationOffering off : offerings) {
                if (!off.getSrsName().contains(requestObservation.getSrsName())) {
                    final StringBuilder availableSrs = new StringBuilder();
                    for (String s : off.getSrsName()) {
                        availableSrs.append(s).append('\n');
                    }
                    throw new CstlServiceException("This srs name is not advertised in the offering.\n" +
                                                   "Available srs name are:\n" + availableSrs.toString(),
                                                    INVALID_PARAMETER_VALUE, "srsName");
                }
            }
        }

        //we verify that the resultModel (if there is one) is advertised in the offering
        if (requestObservation.getResultModel() != null) {
            for (ObservationOffering off : offerings) {
                if (!off.getResultModel().contains(requestObservation.getResultModel())) {
                    final StringBuilder availableRM = new StringBuilder();
                    for (QName s : off.getResultModel()) {
                        availableRM.append(s).append('\n');
                    }
                    throw new CstlServiceException("This result model is not advertised in the offering:" + requestObservation.getResultModel() + '\n' +
                                                   "Available result model for this offering are:", INVALID_PARAMETER_VALUE, "resultModel");
                }
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
                LOGGER.log(logLevel, "process ID: {0}", dbId);
                if (!omReader.existProcedure(dbId)) {
                    throw new CstlServiceException(" this process is not registred in the table", INVALID_PARAMETER_VALUE, PROCEDURE);
                }
                if (!offerings.isEmpty()) {
                    boolean found = false;
                    for (ObservationOffering off : offerings) {
                        if (!found && off.getProcedures().contains(dbId)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        throw new CstlServiceException(" this process is not registred in the offerings", INVALID_PARAMETER_VALUE, PROCEDURE);
                    }
                }
            } else {
                //if there is only one proccess null we return error (we'll see)
                if (procedures.size() == 1) {
                    throw new CstlServiceException("the process is null",
                            INVALID_PARAMETER_VALUE, PROCEDURE);
                }
            }
        }
        localOmFilter.setProcedure(procedures, offerings);

        //we get the list of phenomenon
        //TODO verifier que les pheno appartiennent a l'offering
        final List<String> observedProperties = requestObservation.getObservedProperty();
        if (observedProperties != null && !observedProperties.isEmpty()) {
            final List<String> singlePhenomenons    = new ArrayList<String>();
            final List<String> compositePhenomenons = new ArrayList<String>();
            for (String phenomenonName : observedProperties) {

                if (!phenomenonName.equals(phenomenonIdBase + "ALL")) {

                    /* we remove the phenomenon id base
                    if (phenomenonName.indexOf(phenomenonIdBase) != -1) {
                        phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
                    }*/
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
        } else {
            throw new CstlServiceException("You must specify at least One phenomenon", MISSING_PARAMETER_VALUE, "observedProperty");
        }


        //we treat the time restriction
        final List<Filter> times = requestObservation.getTemporalFilter();
        final TemporalGeometricPrimitive templateTime = treatEventTimeRequest(currentVersion, times, template, localOmFilter);

        //we treat the restriction on the feature of interest

        // if the request is a list of station
        if (!requestObservation.getFeatureIds().isEmpty()) {

            //verify that the station is registred in the DB.
            final Collection<String> fois = omReader.getFeatureOfInterestNames();
            for (final String samplingFeatureName : requestObservation.getFeatureIds()) {
                if (!fois.contains(samplingFeatureName)) {
                    throw new CstlServiceException("the feature of interest "+ samplingFeatureName + " is not registered",
                                                     INVALID_PARAMETER_VALUE, "featureOfInterest");
                }
            }
            localOmFilter.setFeatureOfInterest(requestObservation.getFeatureIds());
        }
        
        // if the request is a spatial operator
        if (requestObservation.getSpatialFilter() != null) {
            // for a BBOX Spatial ops
            if (requestObservation.getSpatialFilter() instanceof BBOX) {
                final Envelope e = getEnvelopeFromBBOX(currentVersion, (BBOX)requestObservation.getSpatialFilter());

                if (e != null && e.isCompleteEnvelope2D()) {
                    boolean add = false;
                    final List<String> matchingFeatureOfInterest = new ArrayList<String>();
                    if (localOmFilter.isBoundedObservation()) {
                        localOmFilter.setBoundingBox(e);
                    } else {
                        for (ObservationOffering off : offerings) {
                    
                            for (String refStation : off.getFeatureOfInterestIds()) {
                                // TODO for SOS 2.0 use observed area
                                final org.geotoolkit.sampling.xml.SamplingFeature station = (org.geotoolkit.sampling.xml.SamplingFeature) omReader.getFeatureOfInterest(refStation, currentVersion);
                                if (station == null) {
                                    throw new CstlServiceException("the feature of interest is not registered",
                                            INVALID_PARAMETER_VALUE);
                                }
                                if (station.getGeometry() instanceof Point) {
                                    if (samplingPointMatchEnvelope((Point)station.getGeometry(), e)) {
                                        matchingFeatureOfInterest.add(getIDFromObject(station));
                                        add = true;
                                    } else {
                                        LOGGER.log(Level.FINER, " the feature of interest {0} is not in the BBOX", getIDFromObject(station));
                                    }

                                } else if (station instanceof AbstractFeature) {
                                    final AbstractFeature sc = (AbstractFeature) station;
                                    if (sc.getBoundedBy() != null && 
                                        sc.getBoundedBy().getEnvelope() != null &&
                                        sc.getBoundedBy().getEnvelope().getLowerCorner() != null && 
                                        sc.getBoundedBy().getEnvelope().getUpperCorner() != null &&
                                        sc.getBoundedBy().getEnvelope().getLowerCorner().getCoordinate().length > 1 && 
                                        sc.getBoundedBy().getEnvelope().getUpperCorner().getCoordinate().length > 1) {

                                        final double stationMinX  = sc.getBoundedBy().getEnvelope().getLowerCorner().getOrdinate(0);
                                        final double stationMaxX  = sc.getBoundedBy().getEnvelope().getUpperCorner().getOrdinate(0);
                                        final double stationMinY  = sc.getBoundedBy().getEnvelope().getLowerCorner().getOrdinate(1);
                                        final double stationMaxY  = sc.getBoundedBy().getEnvelope().getUpperCorner().getOrdinate(1);
                                        final double minx         = e.getLowerCorner().getOrdinate(0);
                                        final double maxx         = e.getUpperCorner().getOrdinate(0);
                                        final double miny         = e.getLowerCorner().getOrdinate(1);
                                        final double maxy         = e.getUpperCorner().getOrdinate(1);

                                        // we look if the station if contained in the BBOX
                                        if (stationMaxX < maxx && stationMinX > minx &&
                                            stationMaxY < maxy && stationMinY > miny) {

                                            matchingFeatureOfInterest.add(sc.getId());
                                            add = true;
                                        } else {
                                            LOGGER.log(Level.FINER, " the feature of interest {0} is not in the BBOX", sc.getId());
                                        }
                                    } else {
                                        LOGGER.log(Level.WARNING, " the feature of interest (samplingCurve){0} does not have proper bounds", sc.getId());
                                    }
                                } else {
                                    LOGGER.log(Level.WARNING, "unknow implementation:{0}", station.getClass().getName());
                                }
                            }
                        }
                        if (add) {
                            localOmFilter.setFeatureOfInterest(matchingFeatureOfInterest);
                        // if there is no matching FOI we must return an empty result
                        } else {
                            return buildObservationCollection(currentVersion, "urn:ogc:def:nil:OGC:inapplicable");
                        }
                    }

                } else {
                    throw new CstlServiceException("the envelope is not build correctly", INVALID_PARAMETER_VALUE);
                }
            } else {
                throw new CstlServiceException(NOT_SUPPORTED, OPERATION_NOT_SUPPORTED);
            }
        }


        //TODO we treat the restriction on the result
        if (requestObservation.getComparisonFilter() != null) {

            final Filter filter = requestObservation.getComparisonFilter();
            
            //we treat the different operation
            if (filter instanceof PropertyIsLessThan) {

                final Expression propertyName  = ((PropertyIsLessThan)filter).getExpression1();
                final Expression literal       = ((PropertyIsLessThan)filter).getExpression2();
                if (literal == null || propertyName == null) {
                    throw new CstlServiceException(" to use the operation Less Than you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "lessThan");
                }


            } else if (filter instanceof PropertyIsGreaterThan) {

                final Expression propertyName  = ((PropertyIsGreaterThan)filter).getExpression1();
                final Expression literal       = ((PropertyIsGreaterThan)filter).getExpression2();
                if (propertyName == null || literal == null) {
                    throw new CstlServiceException(" to use the operation Greater Than you must specify the propertyName and the litteral",
                                                 MISSING_PARAMETER_VALUE, "greaterThan");
                }

            } else if (filter instanceof PropertyIsEqualTo) {

                final Expression propertyName  = ((PropertyIsEqualTo)filter).getExpression1();
                final Expression literal       = ((PropertyIsEqualTo)filter).getExpression2();
                if (propertyName == null || literal == null) {
                     throw new CstlServiceException(" to use the operation Equal you must specify the propertyName and the litteral",
                                                   INVALID_PARAMETER_VALUE, "propertyIsEqualTo"); // cite test
                }
                if (!localOmFilter.supportedQueryableResultProperties().isEmpty()) {
                    localOmFilter.setResultEquals(propertyName.toString(), literal.toString());
                }

            } else if (filter instanceof PropertyIsLike) {
                throw new CstlServiceException(NOT_SUPPORTED, OPERATION_NOT_SUPPORTED, "propertyIsLike");

            } else if (filter instanceof PropertyIsBetween) {
                final PropertyIsBetween pib = (PropertyIsBetween) filter;
                if (pib.getExpression() == null) {
                    throw new CstlServiceException("To use the operation Between you must specify the propertyName and the litteral",
                                                  MISSING_PARAMETER_VALUE, "propertyIsBetween");
                }

                final String propertyName       = pib.getExpression().toString();
                final XMLLiteral lowerLiteral  = (XMLLiteral) pib.getLowerBoundary();
                final XMLLiteral upperLiteral  = (XMLLiteral) pib.getUpperBoundary();

                if (propertyName == null || propertyName.isEmpty() || lowerLiteral == null || upperLiteral == null) {
                        throw new CstlServiceException("This property name, lower and upper literal must be specify",
                                                      INVALID_PARAMETER_VALUE, "result");
                }

            } else {
                throw new CstlServiceException(NOT_SUPPORTED,OPERATION_NOT_SUPPORTED);
            }
        }

        Object response;
        if (!outOfBand) {

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
            final List<Observation> matchingResult;
            final Envelope computedBounds;

            // case (1)
            if (!(localOmFilter instanceof ObservationFilterReader)) {
                matchingResult = new ArrayList<Observation>();
                final Set<String> observationIDs = localOmFilter.filterObservation();
                for (String observationID : observationIDs) {
                    matchingResult.add(omReader.getObservation(observationID, resultModel, currentVersion));
                }
                computedBounds         = null;

            // case (2)
            } else {
                final ObservationFilterReader omFR = (ObservationFilterReader) localOmFilter;
                if (template) {
                    matchingResult = omFR.getObservationTemplates();
                } else {
                    matchingResult = omFR.getObservations();
                }
                if (omFR.computeCollectionBound()) {
                    computedBounds = omFR.getCollectionBoundingShape();
                } else {
                    computedBounds = null;
                }
            }

            final List<Observation> observations = new ArrayList<Observation>();
            for (Observation o : matchingResult) {
                if (template) {

                    final String temporaryTemplateId = o.getName() + '-' + getTemplateSuffix(o.getName());
                    final AbstractObservation temporaryTemplate = ((AbstractObservation) o).getTemporaryTemplate(temporaryTemplateId, templateTime);
                    
                    // Remove the default templateTime
                    if (!localOmFilter.isDefaultTemplateTime() && templateTime == null) {
                        temporaryTemplate.emptySamplingTime();
                    }
                    templates.put(temporaryTemplateId, temporaryTemplate);

                    // we launch a timer which will destroy the template in one hours
                    final Timer t = new Timer();
                    //we get the date and time for now
                    final Date d = new Date(System.currentTimeMillis() + templateValidTime);
                    LOGGER.log(logLevel, "this template will be destroyed at:{0}", d.toString());
                    t.schedule(new DestroyTemplateTask(temporaryTemplateId), d);
                    schreduledTask.add(t);

                    observations.add(temporaryTemplate);
                } else {
                    observations.add(o);
                }
            }

            // this is a little hack for cite test dummy srsName comparaison
            String srsName = "urn:ogc:def:crs:EPSG::4326";
            if ("EPSG:4326".equals(requestObservation.getSrsName())) {
                srsName ="EPSG:4326";
            }
            final Envelope envelope;
            if (computedBounds == null) {
                envelope = getCollectionBound(currentVersion, observations, srsName);
            } else {
                LOGGER.log(Level.FINER, "Using computed bounds:{0}", computedBounds);
                envelope = computedBounds;
            }
            ObservationCollection ocResponse = buildGetObservationResponse(currentVersion, "collection-1", envelope, observations);
            ocResponse = regroupObservation(currentVersion, envelope, ocResponse);
            ocResponse = normalizeDocument(currentVersion, ocResponse);
            response   = ocResponse;
        } else {
            String sReponse = "";
            if (localOmFilter instanceof ObservationFilterReader) {
                sReponse = ((ObservationFilterReader)localOmFilter).getOutOfBandResults();
            } else {
                throw new CstlServiceException("Out of band response mode has been implemented only for ObservationFilterReader for now", NO_APPLICABLE_CODE, RESPONSE_MODE);
            }
            response = sReponse;
        }
        LOGGER.log(logLevel, "getObservation processed in {0}ms.\n", (System.currentTimeMillis() - start));
        return response;
    }

    /**
     * Return true if the samplingPoint entry is strictly inside the specified envelope.
     *
     * @param sp A sampling point (2D) station.
     * @param e An envelope (2D).
     * @return True if the sampling point is strictly inside the specified envelope.
     */
    private boolean samplingPointMatchEnvelope(final Point sp, final Envelope e) {
        if (sp.getDirectPosition() != null) {

            final double stationX = sp.getDirectPosition().getOrdinate(0);
            final double stationY = sp.getDirectPosition().getOrdinate(1);
            final double minx     = e.getLowerCorner().getOrdinate(0);
            final double maxx     = e.getUpperCorner().getOrdinate(0);
            final double miny     = e.getLowerCorner().getOrdinate(1);
            final double maxy     = e.getUpperCorner().getOrdinate(1);

            // we look if the station if contained in the BBOX
            return stationX < maxx && stationX > minx && stationY < maxy && stationY > miny;
        }
        LOGGER.log(Level.WARNING, " the feature of interest does not have proper position");
        return false;
    }
    
    private Envelope getEnvelopeFromBBOX(final String version, final BBOX bbox) {
        return buildEnvelope(version, null, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), bbox.getSRS());
    }

    /**
     * Web service operation
     */
    public GetResultResponse getResult(final GetResult request) throws CstlServiceException {
        LOGGER.log(logLevel, "getResult request processing\n");
        final long start = System.currentTimeMillis();

        //we verify the base request attribute
        verifyBaseRequest(request, true, false);
        final String currentVersion = request.getVersion().toString();
        
        // we clone the filter for this request
        final ObservationFilter localOmFilter = omFactory.cloneObservationFilter(omFilter);
        final String observationTemplateID    = request.getObservationTemplateId();
        
        final String procedure;
        final TemporalObject time;
        final QName resultModel;
        if (observationTemplateID != null) {
            final Observation template = templates.get(observationTemplateID);
            if (template == null) {
                throw new CstlServiceException("this template does not exist or is no longer usable",
                                              INVALID_PARAMETER_VALUE, "ObservationTemplateId");
            }
            procedure = ((Process) template.getProcedure()).getHref();
            time      = template.getSamplingTime();
            if (template instanceof Measurement) {
                resultModel = MEASUREMENT_QNAME;
            } else {
                resultModel = OBSERVATION_QNAME;
            }
        } else if (currentVersion.equals("1.0.0")){
            throw new CstlServiceException("ObservationTemplateID must be specified",
                                          MISSING_PARAMETER_VALUE, "ObservationTemplateId");
        } else {
            if (request.getOffering() == null) {
                throw new CstlServiceException("The offering parameter must be specified",
                                              MISSING_PARAMETER_VALUE, "offering");
            } else {
                final ObservationOffering off = omReader.getObservationOffering(request.getOffering(), currentVersion);
                if (off== null) {
                    throw new CstlServiceException("The offering parameter is invalid",
                                              INVALID_PARAMETER_VALUE, "offering");
                } 
                procedure = off.getProcedures().get(0);
            }
            time = null;
            resultModel = OBSERVATION_QNAME;
        }

        //we begin to create the sql request
        localOmFilter.initFilterGetResult(procedure, resultModel);

        //we treat the time constraint
        final List<Filter> times = request.getTemporalFilter();

        /**
         * The template time :
         */

        // case TEquals with time instant
        if (time instanceof Instant) {
           final Instant ti      = (Instant) time;
           final TEquals equals  = buildTimeEquals(currentVersion, null, ti);
           times.add(equals);

        } else if (time instanceof Period) {
            final Period tp = (Period) time;

            //case TBefore
            if (TimeIndeterminateValueType.BEFORE.equals(((AbstractTimePosition)tp.getBeginning().getPosition()).getIndeterminatePosition())) {
                final Before before  = buildTimeBefore(currentVersion, null, tp.getEnding());
                times.add(before);

            //case TAfter
            } else if (TimeIndeterminateValueType.NOW.equals(((AbstractTimePosition)tp.getEnding().getPosition()).getIndeterminatePosition())) {
                final After after  = buildTimeAfter(currentVersion, null, tp.getBeginning());
                times.add(after);

            //case TDuring/TEquals  (here the sense of T_Equals with timePeriod is lost but not very usefull)
            } else {
                final During during  = buildTimeDuring(currentVersion, null, tp);
                times.add(during);
            }
        }

        //we treat the time constraint
        treatEventTimeRequest(currentVersion, times, false, localOmFilter);

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
                final Object r         = omReader.getResult(result.resultID, resultModel, currentVersion);
                if (r instanceof DataArray) {
                    final DataArray array = (DataArray)r;
                    if (array != null) {
                        final String resultValues = getResultValues(tBegin, tEnd, array, times);
                        if (!resultValues.isEmpty()) {
                            datablock.append(resultValues).append('\n');
                        }
                    } else {
                        throw new IllegalArgumentException("Array is null");
                    }
                } else if (r instanceof Measure) {
                    final Measure meas = (Measure) r;
                    datablock.append(tBegin).append(',').append(meas.getValue()).append("@@");
                } else if (r instanceof AnyResult) {
                    final DataArray array = ((AnyResult)r).getArray();
                    if (array != null) {
                        final String resultValues = getResultValues(tBegin, tEnd, array, times);
                        if (!resultValues.isEmpty()) {
                            datablock.append(resultValues).append('\n');
                        }
                    } else {
                        throw new IllegalArgumentException("Array is null");
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected result type:" + r);
                }

            }
            values = datablock.toString();
        }
        final String url = getServiceUrl().substring(0, getServiceUrl().length() -1);
        final GetResultResponse response = buildGetResultResponse(currentVersion, values, url + '/' + observationTemplateID);
        LOGGER.log(logLevel, "GetResult processed in {0} ms", (System.currentTimeMillis() - start));
        return response;
    }

    private String getResultValues(final Timestamp tBegin, final Timestamp tEnd, final DataArray array, final List<Filter> eventTimes) throws CstlServiceException {
        String values;

        //for multiple observations we parse the brut values (if we got a time constraint)
        if (tBegin != null && tEnd != null) {

            values = array.getValues();

            for (Filter bound: eventTimes) {
                LOGGER.log(Level.FINER, " Values: {0}", values);
                if (bound instanceof TEquals) {
                    final TEquals filter = (TEquals) bound;
                    if (filter.getExpression2() instanceof Instant) {
                        final Instant ti    = (Instant) filter.getExpression2();
                        final Timestamp boundEquals = Timestamp.valueOf(getTimeValue(ti.getPosition()));

                        LOGGER.finer("TE case 1");
                        //case 1 the periods contains a matching values
                        values = parseDataBlock(values, array.getEncoding(), null, null, boundEquals);

                    }

                } else if (bound instanceof After) {
                    final After filter = (After) bound;
                    final Instant ti   = (Instant) filter.getExpression2();
                    final Timestamp boundBegin = Timestamp.valueOf(getTimeValue(ti.getPosition()));

                    // case 1 the period overlaps the bound
                    if (tBegin.before(boundBegin) && tEnd.after(boundBegin)) {
                        LOGGER.finer("TA case 1");
                        values = parseDataBlock(values, array.getEncoding(), boundBegin, null, null);

                    }

                } else if (bound instanceof Before) {
                    final Before filter = (Before) bound;
                    final Instant ti    = (Instant) filter.getExpression2();
                    final Timestamp boundEnd = Timestamp.valueOf(getTimeValue(ti.getPosition()));

                    // case 1 the period overlaps the bound
                    if (tBegin.before(boundEnd) && tEnd.after(boundEnd)) {
                        LOGGER.finer("TB case 1");
                        values = parseDataBlock(values, array.getEncoding(), null, boundEnd, null);

                    }

                } else if (bound instanceof During) {
                    final During filter = (During) bound;
                    final Period tp     = (Period)filter.getExpression2();
                    final Timestamp boundBegin = Timestamp.valueOf(getTimeValue(tp.getBeginning().getPosition()));
                    final Timestamp boundEnd   = Timestamp.valueOf(getTimeValue(tp.getEnding().getPosition()));

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
    private String parseDataBlock(final String brutValues, final AbstractEncoding abstractEncoding, final Timestamp boundBegin, final Timestamp boundEnd, final Timestamp boundEquals) {
        String values = "";
        if (abstractEncoding instanceof TextBlock) {
                final TextBlock encoding        = (TextBlock) abstractEncoding;
                final StringTokenizer tokenizer = new StringTokenizer(brutValues, encoding.getBlockSeparator());
                while (tokenizer.hasMoreTokens()) {
                    final String block = tokenizer.nextToken();
                    String samplingTimeValue = block.substring(0, block.indexOf(encoding.getTokenSeparator()));
                    Date d = null;
                    try {
                        final ISODateParser parser = new ISODateParser();
                        d = parser.parseToDate(samplingTimeValue);
                    } catch (NumberFormatException ex) {
                        LOGGER.log(Level.FINER, "unable to parse the value: {0}", samplingTimeValue);
                    }
                    if (d == null) {
                        LOGGER.log(Level.WARNING, "unable to parse the value: {0}", samplingTimeValue);
                        continue;
                    }
                    final Timestamp t = new Timestamp(d.getTime());

                    // time during case
                    if (boundBegin != null && boundEnd != null) {
                        if (t.after(boundBegin) && t.before(boundEnd)) {
                            values += block + encoding.getBlockSeparator();
                        }

                    //time after case
                    } else if (boundBegin != null && boundEnd == null) {
                        if (t.after(boundBegin)) {
                            values += block + encoding.getBlockSeparator();
                        }

                    //time before case
                    } else if (boundBegin == null && boundEnd != null) {
                        if (t.before(boundEnd)) {
                            values += block + encoding.getBlockSeparator();
                        }

                    //time equals case
                    } else if (boundEquals != null) {
                        if (t.equals(boundEquals)) {
                            values += block + encoding.getBlockSeparator();
                        }
                    }
                }
            } else {
                LOGGER.severe("unable to parse datablock unknown encoding");
                values = brutValues;
            }
        return values;
    }
    
    private Period extractTimeBounds(final String version, final String brutValues, final AbstractEncoding abstractEncoding) {
        final String[] result = new String[2];
        if (abstractEncoding instanceof TextBlock) {
            final TextBlock encoding        = (TextBlock) abstractEncoding;
            final StringTokenizer tokenizer = new StringTokenizer(brutValues, encoding.getBlockSeparator());
            boolean first = true;
            while (tokenizer.hasMoreTokens()) {
                final String block = tokenizer.nextToken();
                String samplingTimeValue = block.substring(0, block.indexOf(encoding.getTokenSeparator()));
                if (first) {
                    result[0] = samplingTimeValue;
                    first = false;
                } else if (!tokenizer.hasMoreTokens()) {
                    result[1] = samplingTimeValue;
                }
            }
        } else {
            LOGGER.warning("unable to parse datablock unknown encoding");
        }
        return buildTimePeriod(version, result[0], result[1]);
    }

    public AbstractFeature getFeatureOfInterest(final GetFeatureOfInterest request) throws CstlServiceException {
        verifyBaseRequest(request, true, false);
        LOGGER.log(logLevel, "GetFeatureOfInterest request processing\n");
        final long start = System.currentTimeMillis();
        final String currentVersion = request.getVersion().toString();
        
        // if there is no filter we throw an exception
        if (request.getTemporalFilters().isEmpty() && request.getFeatureOfInterestId().isEmpty() && request.getSpatialFilters().isEmpty()) {
            throw new CstlServiceException("You must choose a filter parameter: eventTime, featureId or location", MISSING_PARAMETER_VALUE);
        }

        // for now we don't support time filter on FOI
        if (request.getTemporalFilters().size() > 0) {
            throw new CstlServiceException("The time filter on feature Of Interest is not yet supported", OPERATION_NOT_SUPPORTED);
        }

        AbstractFeature result = null;

        // we return a single result
        if (request.getFeatureOfInterestId().size() == 1) {
            final SamplingFeature singleResult = omReader.getFeatureOfInterest(request.getFeatureOfInterestId().get(0), currentVersion);
            if (singleResult == null) {
                throw new CstlServiceException("There is no such Feature Of Interest", INVALID_PARAMETER_VALUE);
            } else {
                if (!alwaysFeatureCollection) {
                    return (AbstractFeature) singleResult;
                } else {
                    final List<FeatureProperty> features = new ArrayList<FeatureProperty>();
                    features.add(buildFeatureProperty(currentVersion, singleResult));
                    final FeatureCollection collection = buildFeatureCollection(currentVersion, "feature-collection-1", null, null, features);
                    collection.computeBounds();
                    result = collection;
                }
            }

        // we return a featureCollection
        } else if (request.getFeatureOfInterestId().size() > 1) {
            final List<FeatureProperty> features = new ArrayList<FeatureProperty>();
            for (String featureID : request.getFeatureOfInterestId()) {
                final SamplingFeature feature = omReader.getFeatureOfInterest(featureID, currentVersion);
                if (feature == null) {
                    throw new CstlServiceException("There is no such Feature Of Interest", INVALID_PARAMETER_VALUE);
                } else {
                    features.add(buildFeatureProperty(currentVersion, feature));
                }
            }
            final FeatureCollection collection = buildFeatureCollection(currentVersion, "feature-collection-1", null, null, features);
            collection.computeBounds();
            result = collection;
        }

        if (request.getSpatialFilters() != null && !request.getSpatialFilters().isEmpty()) {
            final Filter spatialFilter = request.getSpatialFilters().get(0); // TODO handle multiple filters (SOS 2.0.0)
            if (spatialFilter instanceof BBOX) {
                final List<SamplingFeature> results = spatialFiltering((BBOX) spatialFilter, currentVersion);

                // we return a single result
                if (results.size() == 1) {
                    result = (AbstractFeature) results.get(0);

                // we return a feature collection
                } else if (results.size() > 1) {
                    final List<FeatureProperty> features = new ArrayList<FeatureProperty>();
                    for (SamplingFeature feature : results) {
                        features.add(buildFeatureProperty(currentVersion, feature));
                    }
                    final FeatureCollection collection = buildFeatureCollection(currentVersion, "feature-collection-1", null, null, features);
                    collection.computeBounds();
                    result = collection;

                // if there is no response we send an error
                } else {
                    throw new CstlServiceException("There is no such Feature Of Interest", INVALID_PARAMETER_VALUE);
                }
            } else {
                throw new CstlServiceException("Only the filter BBOX is upported for now", OPERATION_NOT_SUPPORTED);
            }
        }

        LOGGER.log(logLevel, "GetFeatureOfInterest processed in {0}ms", (System.currentTimeMillis() - start));
        return result;
    }

    public TemporalPrimitive getFeatureOfInterestTime(final GetFeatureOfInterestTime request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetFeatureOfInterestTime request processing\n");
        final long start = System.currentTimeMillis();
        verifyBaseRequest(request, true, false);

        final String fid = request.getFeatureOfInterestId();

        // if there is no filter we throw an exception
        if (fid == null || fid.isEmpty()) {
            throw new CstlServiceException("You must specify a samplingFeatureId", MISSING_PARAMETER_VALUE);
        }

        final TemporalPrimitive result;
        if (omReader.getFeatureOfInterestNames().contains(fid)) {
            result = omReader.getFeatureOfInterestTime(fid);
        } else {
            throw new CstlServiceException("there is not such samplingFeature on the server", INVALID_PARAMETER_VALUE);
        }
        LOGGER.log(logLevel, "GetFeatureOfInterestTime processed in {0} ms", (System.currentTimeMillis() - start));
        return result;
    }
    
    public InsertResultTemplateResponse insertResultTemplate(final InsertResultTemplate request) throws CstlServiceException {
        LOGGER.log(logLevel, "InsertResultTemplate request processing\n");
        final long start = System.currentTimeMillis();
        verifyBaseRequest(request, true, false);
        final String currentVersion = request.getVersion().toString();
        if (request.getTemplate() == null) {
            throw new CstlServiceException("ResultTemplate must be specified", MISSING_PARAMETER_VALUE, "proposedTemplate");
        }
        // verify the validity of the template
        if (request.getTemplate().getObservationTemplate() == null) {
            throw new CstlServiceException("ResultTemplate must contains observationTemplate", MISSING_PARAMETER_VALUE, "observationTemplate");
        }
        if (request.getTemplate().getOffering() == null) {
            throw new CstlServiceException("ResultTemplate must contains offering", MISSING_PARAMETER_VALUE, "offering");
        }
        if (request.getTemplate().getResultEncoding() == null) {
            throw new CstlServiceException("ResultTemplate must contains resultEncoding", MISSING_PARAMETER_VALUE, "resultEncoding");
        }
        if (request.getTemplate().getResultStructure() == null) {
            throw new CstlServiceException("ResultTemplate must contains resultStructure", MISSING_PARAMETER_VALUE, "resultStructure");
        }
        
        final String templateID = observationTemplateIdBase + UUID.randomUUID();
        resultTemplates.put(templateID, request.getTemplate());
        
        final InsertResultTemplateResponse result = buildInsertResultTemplateResponse(currentVersion, templateID);
        LOGGER.log(logLevel, "InsertResultTemplate processed in {0} ms", (System.currentTimeMillis() - start));
        return result;
    }
    
    public InsertResultResponse insertResult(final InsertResult request) throws CstlServiceException {
        LOGGER.log(logLevel, "InsertResultTemplate request processing\n");
        final long start = System.currentTimeMillis();
        verifyBaseRequest(request, true, false);
        final String currentVersion = request.getVersion().toString();
        
        final String templateID = request.getTemplate();
        if (templateID == null) {
            throw new CstlServiceException("template ID missing.", MISSING_PARAMETER_VALUE, "template");
        }
        final ResultTemplate template = resultTemplates.get(templateID);
        if (template == null) {
            throw new CstlServiceException("template ID is invalid:" + templateID, INVALID_PARAMETER_VALUE, "template");
        }
        final AbstractObservation obs   = (AbstractObservation) template.getObservationTemplate();
        final AbstractEncoding encoding = template.getResultEncoding();
        final String values             = request.getResultValues();
        int count = 0;
        if (encoding instanceof TextBlock) {
            final String separator = ((TextBlock)encoding).getBlockSeparator();
            count = values.split(separator).length;
        }
        final DataArrayProperty array = buildDataArrayProperty(currentVersion, 
                                               null, 
                                               count, 
                                               null, 
                                               (AbstractDataRecord)template.getResultStructure(), 
                                               encoding, 
                                               values);
        obs.setName(omReader.getNewObservationId());
        obs.setResult(array);
        obs.setSamplingTimePeriod(extractTimeBounds(currentVersion, values, encoding));
        omWriter.writeObservation(obs);
        omFilter.refresh();
        final InsertResultResponse result = buildInsertResultResponse(currentVersion);
        LOGGER.log(logLevel, "InsertResult processed in {0} ms", (System.currentTimeMillis() - start));
        return result;
    }

    private List<SamplingFeature> spatialFiltering(final BBOX bbox, final String currentVersion) throws CstlServiceException {
        final Envelope e = getEnvelopeFromBBOX(currentVersion, bbox);
        if (e != null && e.isCompleteEnvelope2D()) {

            final List<SamplingFeature> matchingFeatureOfInterest = new ArrayList<SamplingFeature>();
            final List<ObservationOffering> offerings             = omReader.getObservationOfferings(currentVersion);
            for (ObservationOffering off : offerings) {
                // TODO for SOS 2.0 use observed area
                for (String refStation : off.getFeatureOfInterestIds()) {
                    final org.geotoolkit.sampling.xml.SamplingFeature station = (org.geotoolkit.sampling.xml.SamplingFeature) omReader.getFeatureOfInterest(refStation, currentVersion);
                    if (station == null) {
                        LOGGER.log(Level.WARNING, "the feature of interest is not registered:{0}", refStation);
                        continue;
                    }
                    if (station.getGeometry() instanceof Point) {
                        if (samplingPointMatchEnvelope((Point)station.getGeometry(), e)) {
                            matchingFeatureOfInterest.add(station);
                        } else {
                            LOGGER.log(Level.FINER, " the feature of interest {0} is not in the BBOX", getIDFromObject(station));
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "unknow implementation:{0}", station.getClass().getName());
                    }
                }
            }
            return matchingFeatureOfInterest;
        } else {
            throw new CstlServiceException("the envelope is not build correctly", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Web service operation which register a Sensor in the SensorML database,
     * and initialize its observation by adding an observation template in the O&M database.
     *
     * @param requestRegSensor A request containing a SensorML File describing a Sensor,
     *                         and an observation template for this sensor.
     */
    public InsertSensorResponse registerSensor(final InsertSensor request) throws CstlServiceException {
        if (profile == DISCOVERY) {
            throw new CstlServiceException("The operation registerSensor is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");
        }
        if (transactionSecurized && !org.constellation.ws.security.SecurityManager.isAuthenticated()) {
            throw new UnauthorizedException("You must be authentified to perform an registerSensor request.");
        }
        LOGGER.log(logLevel, "registerSensor request processing\n");
        final long start = System.currentTimeMillis();
        
        //we verify the base request attribute
        verifyBaseRequest(request, true, false);
        final String currentVersion = request.getVersion().toString();

        if (currentVersion.equals("2.0.0")) {
            if (request.getProcedureDescriptionFormat() == null) {
                throw new CstlServiceException("Procedure description format must be specified" , MISSING_PARAMETER_VALUE, PROCEDURE_DESCRIPTION_FORMAT);
            } else if (!ACCEPTED_SENSORML_FORMATS.get("2.0.0").contains(request.getProcedureDescriptionFormat())){
                throw new CstlServiceException("This procedure description format is not supported" , INVALID_PARAMETER_VALUE, PROCEDURE_DESCRIPTION_FORMAT);
            }
        }
        boolean success = false;
        String id = "";
        try {
            //we begin a transaction
            smlWriter.startTransaction();

            //we get the SensorML file who describe the Sensor to insert.
            final Object d = request.getSensorMetadata();
            AbstractSensorML process;
            if (d instanceof AbstractSensorML) {
                process = (AbstractSensorML) d;
            } else {
                String type = "null";
                if (d != null) {
                    type = d.getClass().getName();
                }
                throw new CstlServiceException("unexpected type for process: " + type , INVALID_PARAMETER_VALUE, "sensorDescription");
            }

            //we get the observation template provided with the sensor description.
            final ObservationTemplate temp = request.getObservationTemplate();
            if (temp == null || !temp.isTemplateSpecified()) {
                throw new CstlServiceException("observation template must be specify", MISSING_PARAMETER_VALUE, OBSERVATION_TEMPLATE);
            } else if (!temp.isComplete()) {
                throw new CstlServiceException("observation template must specify at least the following fields: procedure ,observedProperty ,featureOfInterest, Result",
                                              INVALID_PARAMETER_VALUE,
                                              OBSERVATION_TEMPLATE);
            }

            //we create a new Identifier from the SensorML database
            String num = "";
            if (temp.getProcedure() != null) {
                if (temp.getProcedure().startsWith(sensorIdBase)) {
                    id  = temp.getProcedure();
                    num = id.substring(sensorIdBase.length());
                    LOGGER.log(logLevel, "using specified sensor ID:{0} num ={1}", new Object[]{id, num});
                }
            }

            if (id.isEmpty()) {
                num = Integer.toString(smlWriter.getNewSensorId());
                id  = sensorIdBase + num;
            }

            /*
             * @TODO
             *
             * here we affect the new Sensor id to the metatadata
             * does we have to keep the one of the metadata instead of generating one?
             */
            if (process.getMember().size() == 1) {
                process.getMember().get(0).getRealProcess().setId(id);
            } else {
                LOGGER.warning("multiple SensorML member");
            }
            //and we write it in the sensorML Database
            smlWriter.writeSensor(id, process);

            final String phyId = getPhysicalID(process);

            // we record the mapping between physical id and database id
            recordMapping(id, phyId);

            // and we record the position of the piezometer
            final DirectPosition position = getSensorPosition(process);
            if (omWriter != null) {
                omWriter.recordProcedureLocation(phyId, position);

                //we assign the new capteur id to the observation template
                temp.setProcedure(id);
                temp.setName(observationTemplateIdBase + num);
                
                //we write the observation template in the O&M database
                omWriter.writeObservationTemplate(temp);
                addSensorToOffering(process, temp, currentVersion);
            } else {
                LOGGER.warning("unable to record Sensor template and location in O&M datasource: no O&M writer");
            }
            success = true;

        } finally {
            if (!success) {
               smlWriter.abortTransaction();
               LOGGER.warning("Transaction failed");
            } else {
                smlWriter.endTransaction();
            }
        }

        LOGGER.log(logLevel, "registerSensor processed in {0}ms", (System.currentTimeMillis() - start));
        return buildInsertSensorResponse(currentVersion, id, null);
    }

    /**
     * Web service operation which insert a new Observation for the specified sensor
     * in the O&M database.
     *
     * @param requestInsObs an InsertObservation request containing an O&M object and a Sensor id.
     */
    public InsertObservationResponse insertObservation(final InsertObservation request) throws CstlServiceException {
        if (profile == DISCOVERY) {
            throw new CstlServiceException("The operation insertObservation is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");
        }
        if (transactionSecurized && !org.constellation.ws.security.SecurityManager.isAuthenticated()) {
            throw new UnauthorizedException("You must be authentified to perform an insertObservation request.");
        }

        LOGGER.log(logLevel, "InsertObservation request processing\n");
        final long start = System.currentTimeMillis();

        //we verify the base request attribute
        verifyBaseRequest(request, true, false);
        final String currentVersion = request.getVersion().toString();
        
        //we get the id of the sensor and we create a sensor object
        final String sensorId = request.getAssignedSensorId();
        String num = null;
        if (currentVersion.equals("1.0.0")) {
            if (sensorId == null) {
                throw new CstlServiceException("The sensor identifier is missing.",
                                             MISSING_PARAMETER_VALUE, "assignedSensorId");
            } else if (sensorId.startsWith(sensorIdBase)) {
                num = sensorId.substring(sensorIdBase.length());
            } else {
                throw new CstlServiceException("The sensor identifier is not valid it must start with " + sensorIdBase,
                                             INVALID_PARAMETER_VALUE, "assignedSensorId");
            }
        } else {
            final List<String> offeringNames = request.getOffering();
            if (offeringNames == null || offeringNames.isEmpty()) {
                throw new CstlServiceException("The offering identifiers are missing.",
                                             MISSING_PARAMETER_VALUE, "offering");
            } else {
                final ObservationOffering off = omReader.getObservationOffering(offeringNames.get(0), currentVersion);
                if (off != null) {
                    // TODO
                } else {
                    throw new CstlServiceException("The offering identifier is invalid.",
                                             INVALID_PARAMETER_VALUE, "offering");
                }
            }
        }

        //we get the observation and we assign to it the sensor
        final List<String> ids = new ArrayList<String>();
        final List<? extends Observation> observations = request.getObservations();
        for (Observation observation : observations) {
            final AbstractObservation obs = (AbstractObservation) observation;
            if (obs != null) {
                obs.setProcedure(sensorId);
                obs.setName(omReader.getNewObservationId());
                LOGGER.log(Level.FINER, "samplingTime received: {0}", obs.getSamplingTime());
                LOGGER.log(Level.FINER, "template received:\n{0}", obs.toString());
            } else {
                throw new CstlServiceException("The observation template must be specified",
                                                 MISSING_PARAMETER_VALUE, OBSERVATION_TEMPLATE);
            }

            // Debug part
            if (verifySynchronization) {
                if (obs.getSamplingTime() instanceof Instant) {
                   final Instant timeInstant = (Instant) obs.getSamplingTime();
                    try {
                        final ISODateParser parser = new ISODateParser();
                        final Date d = parser.parseToDate(timeInstant.getPosition().getDateTime().toString());
                        final long t = System.currentTimeMillis() - d.getTime();
                        LOGGER.info("gap between time of reception and time of sampling: " + t + " ms (" + TemporalUtilities.durationToString(t) + ')');
                    } catch (IllegalArgumentException ex) {
                        LOGGER.warning("unable to parse the samplingTime");
                    }
                }
            }

            //we record the observation in the O&M database
           final String id;
            if (currentVersion.equals("2.0.0")) {
                id = omWriter.writeObservation(obs);
            } else {
                if (obs instanceof Measurement) {
                    id = omWriter.writeObservation(obs);
                } else {
                    //in first we verify that the observation is conform to the template
                    final Observation template = (Observation) omReader.getObservation(observationTemplateIdBase + num, OBSERVATION_QNAME, currentVersion);
                    //if the observation to insert match the template we can insert it in the OM db
                    if (obs.matchTemplate(template)) {
                        if (obs.getSamplingTime() != null && obs.getResult() != null) {
                            id = omWriter.writeObservation(obs);
                            LOGGER.log(logLevel, "new observation inserted: id = " + id + " for the sensor " + obs.getProcedure());
                        } else {
                            throw new CstlServiceException("The observation sampling time and the result must be specify",
                                    MISSING_PARAMETER_VALUE, "samplingTime");
                        }
                    } else {
                        throw new CstlServiceException(" The observation doesn't match with the template of the sensor",
                                INVALID_PARAMETER_VALUE, "samplingTime");
                    }
                }
            }
           
           ids.add(id);
        }

        LOGGER.log(logLevel, "insertObservation processed in {0} ms", (System.currentTimeMillis() - start));
        omFilter.refresh();
        return buildInsertObservationResponse(currentVersion, ids);
    }

    /**
     *
     *
     * @param times A list of time constraint.
     * @param SQLrequest A stringBuilder building the SQL request.
     *
     * @return true if there is no errors in the time constraint else return false.
     */
    private TemporalGeometricPrimitive treatEventTimeRequest(final String version, final List<Filter> times, final boolean template, final ObservationFilter localOmFilter) throws CstlServiceException {

        //In template mode  his method return a temporal Object.
        TemporalGeometricPrimitive templateTime = null;

        if (!times.isEmpty()) {

            for (Filter time: times) {

                // The operation Time Equals
                if (time instanceof TEquals) {
                    final TEquals filter = (TEquals) time;
                    
                    // we get the property name (not used for now)
                    //String propertyName = time.getTEquals().getPropertyName();
                    final Object timeFilter   = filter.getExpression2();

                    // look for "latest" or "getFirst" filter (52N compatibility)
                    if (timeFilter instanceof Instant){
                        final Instant ti = (Instant) timeFilter;
                        if (ti.getPosition() != null && ti.getPosition().getDateTime() != null &&
                            ti.getPosition().getDateTime().toString().equalsIgnoreCase("latest")) {
                            if (!template) {
                                localOmFilter.setTimeLatest();
                                continue;
                            } else {
                                LOGGER.warning("latest time are not handled with template mode");
                            }
                        }
                        if (ti.getPosition() != null && ti.getPosition().getDateTime() != null &&
                            ti.getPosition().getDateTime().toString().equalsIgnoreCase("getFirst")) {
                            if (!template) {
                                localOmFilter.setTimeFirst();
                                continue;
                            } else {
                                LOGGER.warning("getFirst time are not handled with template mode");
                            }
                        }
                    }

                    if (!template) {
                        localOmFilter.setTimeEquals(timeFilter);

                    } else if (timeFilter instanceof TemporalGeometricPrimitive) {
                        templateTime = (TemporalGeometricPrimitive) timeFilter;

                    } else {
                        throw new CstlServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, EVENT_TIME);
                    }
                    

                // The operation Time before
                } else if (time instanceof Before) {
                    final Before filter = (Before) time;
                    
                    // we get the property name (not used for now)
                    // String propertyName = time.getTBefore().getPropertyName();
                    final Object timeFilter   = filter.getExpression2();

                    if (!template) {
                        localOmFilter.setTimeBefore(timeFilter);
                    } else if (timeFilter instanceof Instant) {
                        final Instant ti = (Instant)timeFilter;
                        templateTime = buildTimePeriod(version, TimeIndeterminateValueType.BEFORE, ti.getPosition());
                    } else {
                        throw new CstlServiceException("TM_Before operation require timeInstant!",
                                                      INVALID_PARAMETER_VALUE, EVENT_TIME);
                    }
                    

                // The operation Time after
                } else if (time instanceof After) {
                    final After filter = (After) time;
                    
                    // we get the property name (not used for now)
                    //String propertyName = time.getTAfter().getPropertyName();
                    final Object timeFilter   = filter.getExpression2();

                    if (!template) {
                        localOmFilter.setTimeAfter(timeFilter);
                    } else if (timeFilter instanceof Instant) {
                        final Instant ti = (Instant)timeFilter;
                        templateTime = buildTimePeriod(version, ti.getPosition(), null);

                    } else {
                       throw new CstlServiceException("TM_After operation require timeInstant!",
                                                     INVALID_PARAMETER_VALUE, EVENT_TIME);
                    }
                    

                // The time during operation
                } else if (time instanceof During) {
                    final During filter = (During) time;
                    
                    // we get the property name (not used for now)
                    //String propertyName = time.getTDuring().getPropertyName();
                    final Object timeFilter   = filter.getExpression2();

                    if (!template) {
                        localOmFilter.setTimeDuring(timeFilter);
                    }
                    if (timeFilter instanceof Period) {
                        templateTime = (Period)timeFilter;

                    } else {
                        throw new CstlServiceException("TM_During operation require TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, EVENT_TIME);
                    }
                    
                } else if (time instanceof Begins|| time instanceof BegunBy || time instanceof TContains ||time instanceof EndedBy || time instanceof Ends || time instanceof Meets
                           || time instanceof TOverlaps|| time instanceof OverlappedBy) {
                    throw new CstlServiceException("This operation is not take in charge by the Web Service, supported one are: TM_Equals, TM_After, TM_Before, TM_During",
                                                  OPERATION_NOT_SUPPORTED);
                } else {
                    throw new CstlServiceException("Unknow time filter operation, supported one are: TM_Equals, TM_After, TM_Before, TM_During.\n"
                                                 + "Another possibility is that the content of your time filter is empty or unrecognized.",
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
    private void verifyBaseRequest(final RequestBase request, final boolean versionMandatory, final boolean getCapabilities) throws CstlServiceException {
        isWorking();
        if (request != null) {
            if (request.getService() != null) {
                if (!request.getService().equals(SOS))  {
                    throw new CstlServiceException("service must be \"SOS\"!", INVALID_PARAMETER_VALUE, SERVICE_PARAMETER);
                }
            } else {
                throw new CstlServiceException("service must be specified!", MISSING_PARAMETER_VALUE, SERVICE_PARAMETER);
            }
            if (request.getVersion()!= null) {
                
                if (request.getVersion().toString().equals("1.0.0")) {
                    request.setVersion("1.0.0");
                } else if (request.getVersion().toString().equals("2.0.0")) {
                    request.setVersion("2.0.0");
                } else {
                    final CodeList code;
                    final String locator;
                    if (getCapabilities) {
                        code = VERSION_NEGOTIATION_FAILED;
                        locator = "acceptVersion";
                    } else {
                        code = INVALID_PARAMETER_VALUE;
                        locator = "version";
                    }
                    throw new CstlServiceException("version must be \"1.0.0\" or \"2.0.0\"!", code, locator);
                }
            } else {
                if (versionMandatory) {
                    throw new CstlServiceException("version must be specified!", MISSING_PARAMETER_VALUE, "version");
                } else {
                    request.setVersion("1.0.0");
                }
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
    private int getTemplateSuffix(final String templateName) {
        int i = 0;
        boolean notFound = true;
        while (notFound) {
            if (templates.containsKey(templateName + '-' + i)) {
                i++;
            } else {
                notFound = false;
            }
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
    private void addSensorToOffering(final AbstractSensorML sensor, final ObservationTemplate template, final String version) throws CstlServiceException {

        //we search which are the networks binded to this sensor TODO remove
        final List<String> networkNames = getNetworkNames(sensor);

        // for each network we create (or update) an offering
        for (String networkName : networkNames) {
            final String offeringName          = "offering-" + networkName;
            final ObservationOffering offering = omReader.getObservationOffering(offeringName, version);

            if (offering != null) {
                updateOffering(offering, template);
            } else {
                createOffering(version, offeringName, template);
            }
        }

        /*
         * then  we add the sensor to the global offering containing all the sensor
         */
        final ObservationOffering offering = omReader.getObservationOffering("offering-allSensor", version);
        if (offering != null) {
            updateOffering(offering, template);
        } else {
            createOffering(version, "allSensor", template);
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
    private void updateOffering(final ObservationOffering offering, final ObservationTemplate template) throws CstlServiceException {

        //we add the new sensor to the offering
        String offProc = null;
        final String processID = template.getProcedure();
        if (!offering.getProcedures().contains(processID)) {
            offProc = processID;
        }

        //we add the phenomenon to the offering
        List<String> offPheno = new ArrayList<String>();
        if (template.getObservedProperties() != null) {
            for (String observedProperty : template.getObservedProperties()) {
                if (!offering.getObservedProperties().contains(observedProperty)) {
                    offPheno.add(observedProperty);
                }
            }
        }

        // we add the feature of interest (station) to the offering
        String offSF = null;
        if (template.getFeatureOfInterest() != null) {
            if (!offering.getFeatureOfInterestIds().contains(template.getFeatureOfInterest())) {
                offSF = template.getFeatureOfInterest();
            }
        }
        omWriter.updateOffering(offering.getId(), offProc, offPheno, offSF);
    }


    /**
     * Create a new Offering with the specified observation template
     *
     * @param offeringName the name of the new offering (not including offering base name).
     * @param template An observation template used as a base for the offering.
     *
     * @throws CstlServiceException If the service does not succeed to store the offering in the datasource.
     */
    private void createOffering(final String version, final String offeringName, final ObservationTemplate template) throws CstlServiceException {
       LOGGER.log(logLevel, "offering {0} not present, first build", offeringName);

        // TODO bounded by??? station?

        // for the eventime of the offering we take the time of now.
        final Timestamp t = new Timestamp(System.currentTimeMillis());
        final Period time = buildTimePeriod(version, t.toString(), null);

        //we add the template process
        final String process = template.getProcedure();

        //we add the template phenomenon
        final List<String> observedProperties = template.getObservedProperties();

        //we add the template feature of interest
        final String featureOfInterest = template.getFeatureOfInterest();

        //we create a list of accepted responseMode (fixed)
        final List<ResponseModeType> responses = Arrays.asList(RESULT_TEMPLATE, INLINE);
        final List<QName> resultModel = Arrays.asList(OBSERVATION_QNAME, MEASUREMENT_QNAME);
        final List<String> resultModelV200 = Arrays.asList(OBSERVATION_MODEL);
        final List<String> offeringOutputFormat = Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
        final List<String> srsName = Arrays.asList("EPSG:4326");

        String description = "";
        if ("allSensor".equals(offeringName)) {
            description = "Base offering containing all the sensors.";
        }
        // we create a the new Offering
        omWriter.writeOffering(buildOffering(version, 
                                            OFFERING_ID_BASE + offeringName,
                                            OFFERING_ID_BASE + offeringName,
                                            description,
                                            srsName,
                                            time,
                                            Arrays.asList(process),
                                            null,
                                            observedProperties,
                                            Arrays.asList(featureOfInterest),
                                            offeringOutputFormat,
                                            resultModel,
                                            resultModelV200,
                                            responses));
    }

    /**
     * Return the current output format MIME type (default: application/xml).
     *
     * @return The current output format MIME type (default: application/xml).
     *
     * @deprecated thread unsafe todo replace
     */
    @Deprecated
    public String getOutputFormat() {
        if (outputFormat == null) {
            return MimeType.APPLICATION_XML;
        }
        return outputFormat;
    }

    /**
     * Record the mapping between physical ID and database ID.
     *
     * @param form The "form" containing the sensorML data.
     * @param dbId The identifier of the sensor in the O&M database.
     */
    @Deprecated
    private void recordMapping(final String dbId, final String physicalID) throws CstlServiceException {
        try {
            if (dbId != null && physicalID != null) {
                map.setProperty(physicalID, dbId);
                final File configDirectory = configurationDirectory;
                if (configDirectory != null && configDirectory.exists() && configDirectory.isDirectory()) {
                    final File mappingFile     = new File(configDirectory, "mapping.properties");
                    final FileOutputStream out = new FileOutputStream(mappingFile);
                    map.store(out, "");
                    out.close();
                }
            }

        } catch (FileNotFoundException ex) {
            throw new CstlServiceException("The service cannot build the temporary file:"  + ex.getMessage(),
                    NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("the service has throw an IOException:" + ex.getMessage(),
                    NO_APPLICABLE_CODE);
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
    private void initLogger(String id, final String filePath) {
        try {
            if (id != null && !id.isEmpty()) {
                id = id + '-';
            }
            final FileHandler handler  = new FileHandler(filePath + '/'+ id + "cstl-sos.log");
            handler.setFormatter(new MonolineFormatter(handler));
            LOGGER.addHandler(handler);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO exception while trying to separate CSW Logs:{0}", ex.getMessage());
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Security exception while trying to separate CSW Logs{0}", ex.getMessage());
        }
    }

    /**
     * Destroy and free the resource used by the worker.
     */
    @Override
    public void destroy() {
        super.destroy();
        if (smlReader != null) {smlReader.destroy();}
        if (smlWriter != null) {smlWriter.destroy();}
        if (omReader != null)  {omReader.destroy();}
        if (omWriter != null)  {omWriter.destroy();}
        for (Timer t : schreduledTask) {
            t.cancel();
        }
        startError = "The service has been shutdown";
        isStarted = false;
    }

    @Override
    public final void setLogLevel(final Level logLevel) {
        this.logLevel = logLevel;
        if (omFilter != null) {
            omFilter.setLoglevel(logLevel);
        }
    }

   /**
     * {@inheritDoc}
     */
    @Override
    protected MarshallerPool getMarshallerPool() {
        return SOSMarshallerPool.getInstance();
    }

    /**
     * A task destroying a observation template when the template validity period pass.
     */
    class DestroyTemplateTask extends TimerTask {

        /**
         * The identifier of the temporary template.
         */
        private final String templateId;

        /**
         * Build a new Timer which will destroy the temporaryTemplate
         *
         * @param templateId The identifier of the temporary template.
         */
        public DestroyTemplateTask(final String templateId) {
            this.templateId  = templateId;
        }

        /**
         * This method is launch when the timer expire.
         */
        @Override
        public void run() {
            templates.remove(templateId);
            LOGGER.log(logLevel, "template:{0} destroyed", templateId);
        }
    }
}
