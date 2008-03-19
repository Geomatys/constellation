/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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

package net.seagis.sos.webservice;

// JDK dependencies
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import net.seagis.catalog.NoSuchTableException;
import org.xml.sax.SAXException;

// SeaGis dependencies
import net.seagis.sos.Capabilities;
import net.seagis.sos.Contents;
import net.seagis.sos.Contents.ObservationOfferingList;
import net.seagis.sos.DescribeSensor;
import net.seagis.sos.EventTime;
import net.seagis.sos.GetCapabilities;
import net.seagis.sos.GetObservation;
import net.seagis.sos.GetResult;
import net.seagis.sos.GetResultResponse;
import net.seagis.sos.InsertObservation;
import net.seagis.sos.InsertObservationResponse;
import net.seagis.sos.RegisterSensor;
import net.seagis.sos.RegisterSensorResponse;
import net.seagis.sos.RequestBaseType;
import net.seagis.gml.TimeInstantType;
import net.seagis.gml.TimePeriodType;
import net.seagis.ogc.LiteralType;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.NoSuchRecordException;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.ows.v110.OWSWebServiceException;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.wms.WebService;
import net.seagis.gml.AbstractTimeGeometricPrimitiveType;
import net.seagis.gml.EnvelopeEntry;
import net.seagis.gml.FeaturePropertyType;
import net.seagis.gml.ReferenceEntry;
import net.seagis.gml.ReferenceTable;
import net.seagis.gml.TimePositionType;
import net.seagis.swe.CompositePhenomenonEntry;
import net.seagis.swe.CompositePhenomenonTable;
import net.seagis.observation.MeasurementEntry;
import net.seagis.observation.MeasurementTable;
import net.seagis.observation.ObservationCollectionEntry;
import net.seagis.observation.ObservationEntry;
import net.seagis.observation.ObservationTable;
import net.seagis.swe.PhenomenonEntry;
import net.seagis.swe.PhenomenonTable;
import net.seagis.observation.ProcessEntry;
import net.seagis.observation.ProcessTable;
import net.seagis.observation.SamplingFeatureEntry;
import net.seagis.observation.SamplingFeatureTable;
import net.seagis.observation.SamplingPointEntry;
import net.seagis.observation.SamplingPointTable;
import net.seagis.ows.v110.AcceptFormatsType;
import net.seagis.ows.v110.AcceptVersionsType;
import net.seagis.ows.v110.Operation;
import net.seagis.ows.v110.OperationsMetadata;
import net.seagis.ows.v110.RangeType;
import net.seagis.ows.v110.SectionsType;
import net.seagis.ows.v110.ServiceIdentification;
import net.seagis.ows.v110.ServiceProvider;
import net.seagis.sos.FilterCapabilities;
import net.seagis.sos.ObservationOfferingEntry;
import net.seagis.sos.ObservationOfferingTable;
import net.seagis.sos.ObservationTemplate;
import net.seagis.sos.OfferingPhenomenonEntry;
import net.seagis.sos.OfferingProcedureEntry;
import net.seagis.sos.OfferingSamplingFeatureEntry;
import net.seagis.sos.ResponseModeType;
import net.seagis.swe.AbstractEncodingPropertyType;
import net.seagis.swe.AnyResultEntry;
import net.seagis.swe.AnyResultTable;
import net.seagis.swe.DataArrayEntry;
import net.seagis.swe.DataArrayPropertyType;
import net.seagis.swe.DataComponentPropertyType;
import net.seagis.swe.PhenomenonPropertyType;
import static net.seagis.ows.OWSExceptionCode.*;

// MDWeb dependencies
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.schemas.ValuePath;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.users.User;
import org.mdweb.sql.v20.Reader20;
import org.mdweb.sql.v20.Writer20;
import org.mdweb.xml.MalFormedDocumentException;
import org.mdweb.xml.Reader;
import org.mdweb.xml.Writer;

// GeoAPI dependencies
import org.opengis.observation.Observation;

// postgres driver
import org.postgresql.ds.PGSimpleDataSource;

/**
 *
 * @author Guilhem Legal.
 */
public class SOSworker {

    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("net.seagis.sos.webservice");
    
    /**
     * A simple Connection to the SensorML database.
     */
    private Connection sensorMLConnection;
    
    /**
     * A Reader to the SensorML database.
     */
    private Reader20 sensorMLReader;
    
    /**
     * A Writer to the SensorML database.
     */
    private Writer20 sensorMLWriter;
    
    /**
     * the data catalog for SensorML database.
     */
    private Catalog SMLCatalog;
    
    /**
     * A Database object for the O&M dataBase.
     */
    private Database OMDatabase;
    
    /**
     * A database table for insert and get observation
     */
    private ObservationTable obsTable;
    
    /**
     * A database table for insert and get observation offerring.
     */
    private ObservationOfferingTable offTable;
    
    /**
     * A database table for insert and get reference object.
     */
    private ReferenceTable refTable;
   
    /**
     * A list of temporary ObservationTemplate
     */
    private Map<String, ObservationEntry> templates = new HashMap<String, ObservationEntry>();
    
    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */ 
    private Properties map;
    
    /**
     * The base for sensor id.
     */ 
    private String sensorIdBase;
    
     /**
     * The base for observation id.
     */ 
    private String observationIdBase;
    
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
    private final String phenomenonIdBase = "urn:ogc:def:phenomenon:BRGM:";
    
    /**
     * The valid time for a getObservation template (in ms).
     */
    private long templateValidTime;
    
    /**
     * The temporary folder for describe sensor operation.
     */
    private String temporaryFolder;
    
    /**
     * The maximum of observation return in a getObservation request.
     */
    private int maxObservationByRequest;
    
    /**
     * The version of the service
     */
    private ServiceVersion version;
    
    /**
     * A capabilities object containing the static part of the document.
     */
    private Capabilities staticCapabilities;
    
    /**
     * The service url.
     */
    private String serviceURL;
    
    /**
     * Initialize the database connection.
     */
    public SOSworker() throws SQLException, IOException, NoSuchTableException {
       
        //we load the properties files
        Properties prop = new Properties();
        map    = new Properties();
        File f = null;
        String env = "/home/tomcat/.sicade" ; //System.getenv("CATALINA_HOME");
        logger.info("CATALINA_HOME=" + env);
        try {
            // we get the configuration file
            f = new File(env + "/sos_configuration/config.properties");
            FileInputStream in = new FileInputStream(f);
            prop.load(in);
            in.close();
            
            // the file who record the map between phisycal ID and DB ID.
            f = new File(env + "/sos_configuration/mapping.properties");
            in = new FileInputStream(f);
            map.load(in);
            in.close();
            
        } catch (FileNotFoundException e) {
            if (f != null) {
                logger.severe(f.getPath());
            }
            logger.severe("The sevice can not load the properties files" + '\n' + 
                          "cause: " + e.getMessage());
            return;
        }
      
        //we create a connection to the sensorML database
        PGSimpleDataSource dataSourceSML = new PGSimpleDataSource();
        dataSourceSML.setServerName(prop.getProperty("SMLDBServerName"));
        dataSourceSML.setPortNumber(Integer.parseInt(prop.getProperty("SMLDBServerPort")));
        dataSourceSML.setDatabaseName(prop.getProperty("SMLDBName"));
        dataSourceSML.setUser(prop.getProperty("SMLDBUser"));
        dataSourceSML.setPassword(prop.getProperty("SMLDBUserPassword"));
        sensorMLConnection = dataSourceSML.getConnection();
        sensorMLReader     = new Reader20(Standard.SENSORML, sensorMLConnection);
        SMLCatalog         = sensorMLReader.getCatalog("SMLC");
        sensorMLWriter     = new Writer20(sensorMLConnection);
        if (sensorMLConnection == null) {
            logger.severe("THE WEB SERVICE CAN'T CONNECT TO THE SENSORML DB!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        
        //we create a connection to the O&M database
        PGSimpleDataSource dataSourceOM = new PGSimpleDataSource();
        dataSourceOM.setServerName(prop.getProperty("OMDBServerName"));
        dataSourceOM.setPortNumber(Integer.parseInt(prop.getProperty("OMDBServerPort")));
        dataSourceOM.setDatabaseName(prop.getProperty("OMDBName"));
        dataSourceOM.setUser(prop.getProperty("OMDBUser"));
        dataSourceOM.setPassword(prop.getProperty("OMDBUserPassword"));
        OMDatabase   = new Database(dataSourceOM);
        
        //we build the database table frequently used.
        obsTable  = OMDatabase.getTable(ObservationTable.class);
        offTable  = OMDatabase.getTable(ObservationOfferingTable.class);
        refTable  = OMDatabase.getTable(ReferenceTable.class);
        
        //we initailize the properties attribute 
        sensorIdBase              = prop.getProperty("sensorIdBase");
        observationIdBase         = prop.getProperty("observationIdBase");
        observationTemplateIdBase = prop.getProperty("observationTemplateIdBase");
        temporaryFolder           = prop.getProperty("temporaryFolder");
        maxObservationByRequest   = Integer.parseInt(prop.getProperty("maxObservationByRequest"));
        String validTime          = prop.getProperty("templateValidTime");
        int h                     = Integer.parseInt(validTime.substring(0, validTime.indexOf(':')));
        int m                     = Integer.parseInt(validTime.substring(validTime.indexOf(':') + 1));
        templateValidTime         = (h *  3600000) + (m * 60000);
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
                throw new OWSWebServiceException("service must be \"SOS\"!",
                                                 INVALID_PARAMETER_VALUE,
                                                 "service", version);
            }
        } else {
            throw new OWSWebServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service",
                                             version);
        }
        AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("1.0.0")){
                 throw new OWSWebServiceException("version available : 1.0.0",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion",
                                             version);
            }
        }
        AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 && !formats.getOutputFormat().contains("text/xml")) {
            throw new OWSWebServiceException("accepted format : text/xml",
                                             INVALID_PARAMETER_VALUE, "acceptFormats",
                                             version);
        }
        
        //we prepare the response document
        Capabilities c = null; 
        try {
            
            ServiceIdentification si = null;
            ServiceProvider       sp = null;
            OperationsMetadata    om = null;
            FilterCapabilities    fc = null;
            Contents            cont = null;
            
            SectionsType sections = requestCapabilities.getSections();
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
               //we update the URL
               WebService.updateOWSURL(om.getOperation(), serviceURL, "SOS");
               
               //we update the parameter in operation metadata.
               Operation go = om.getOperation("GetObservation");
               
               // the list of offering names
               Set<String> offNames = offTable.getIdentifiers();
               go.updateParameter("offering", offNames);
               
               // the event time range
               RangeType range = new RangeType(getMinimalEventTime(), "now");
               go.updateParameter("eventTime", range);
               
               //the process list
               ProcessTable procTable = OMDatabase.getTable(ProcessTable.class);
               Set<String> procNames  = procTable.getIdentifiers();
               go.updateParameter("procedure", procNames);
               
               //the phenomenon list
               PhenomenonTable phenoTable = OMDatabase.getTable(PhenomenonTable.class);
               Set<String> phenoNames  = phenoTable.getIdentifiers();
               go.updateParameter("observedProperty", phenoNames);
               
               //the feature of interest list
               SamplingFeatureTable featureTable = OMDatabase.getTable(SamplingFeatureTable.class);
               Set<String> featureNames  = featureTable.getIdentifiers();
               go.updateParameter("featureOfInterest", featureNames);
               
               Operation ds = om.getOperation("DescribeSensor");
               ds.updateParameter("procedure", procNames);
               
            }
            
            //we enter the information filter capablities.
            if (sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains("All")) {
            
                fc = staticCapabilities.getFilterCapabilities();
            }
            
            
             if (sections.getSection().contains("Contents") || sections.getSection().contains("All")) {
                // we add the list of observation ofeerings 
                List<ObservationOfferingEntry> loo = new ArrayList<ObservationOfferingEntry>();
                Set<ObservationOfferingEntry> set = offTable.getEntries();
                loo.addAll(set);
                ObservationOfferingList ool = new ObservationOfferingList(loo);
                
                cont = new Contents(ool);
            }
            c = new Capabilities(si, sp, om, "1.0.0", null, fc, cont);
            
        } catch (SQLException ex) {
           ex.printStackTrace();
           throw new OWSWebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE, null, version);
           
        } catch (CatalogException ex) {
            throw new OWSWebServiceException("the service has throw a Catalog Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        }
        return normalizeDocument(c);
        
    }
    
    /**
     * Web service operation whitch return an sml description of the specified sensor.
     * 
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     */
    public String describeSensor(DescribeSensor requestDescSensor) throws WebServiceException  {
        logger.info("DescribeSensor request processing"  + '\n');
        
            // we get the form
            verifyBaseRequest(requestDescSensor);
            
            //we verify that the output format is good.     
            if (requestDescSensor.getOutputFormat()!= null) {
                    if (!requestDescSensor.getOutputFormat().equalsIgnoreCase("text/xml;subtype=\"SensorML/1.0.0\"")) {
                        throw new OWSWebServiceException("only text/xml;subtype=\"SensorML/1.0.0\" is accepted for outputFormat",
                                                         INVALID_PARAMETER_VALUE, "outputFormat", version);
                    }
            } else {
                throw new OWSWebServiceException("output format text/xml;subtype=\"SensorML/1.0.0\" must be specify",
                                                 MISSING_PARAMETER_VALUE, "outputFormat", version);
            }
            //we transform the form into an XML string
            if (requestDescSensor.getProcedure() == null) {
                throw new OWSWebServiceException("You must specify the sensor ID!",
                                             MISSING_PARAMETER_VALUE, "procedure", version);
            }
            String sensorId = requestDescSensor.getProcedure();
            logger.info("sensorId received: " + sensorId);
            String result = "";

            try {
                String dbId = map.getProperty(sensorId);
                if (dbId == null) {
                    dbId = sensorId;
                }
                // we find the form id describing the sensor.
                int id = sensorMLReader.getIdFromTitleForm(dbId);
                logger.info("describesensor id: " + dbId);
                logger.info("describesensor mdweb id: " + id);
                // we get the form
                Form f = sensorMLReader.getForm(SMLCatalog, id);

                if (f == null) {
                    throw new OWSWebServiceException("this sensor is not registered in the database!",
                                                  INVALID_PARAMETER_VALUE, "procedure", version);
                }
                //we transform the form into an XML string
                Writer XMLWriter = new Writer(sensorMLReader);
                result = XMLWriter.writeForm(f);
            } catch (SQLException ex) {
                ex.printStackTrace();
                throw new OWSWebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE, null, version);
            }

            /* the following code avoid the replacement of the mark by their ASCII code
             * it has been removed for a namespace issue 
             
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = factory.newDocumentBuilder();
            
            File tempFile = new File(temporaryFolder + "/temp.xml");
            FileOutputStream outstr = new FileOutputStream(tempFile);
            OutputStreamWriter outstrR = new OutputStreamWriter(outstr,"UTF-8");
            BufferedWriter output = new BufferedWriter(outstrR);
            output.write(result);
            output.flush();
            output.close();
            File tempFile = new File(temporaryFolder + "/notemp.xml");
            Document d = constructor.parse(tempFile);
            
            Element e = d.getDocumentElement();
            
            
            DescribeSensorResponse response = new DescribeSensorResponse();
            response.setContent(e);*/
            return result;
       
    }
    
    /*
    public static class DescribeSensorResponse {
    
        @XmlAnyElement
        private Element content;

        @XmlTransient
        public Element getContent() {
            return content;
        }

        public void setContent(Element value) {
            this.content = value;
        }
    } */
    
    
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
         if (requestObservation.getResponseFormat()!= null) {
            if (!requestObservation.getResponseFormat().equalsIgnoreCase("text/xml; subtype=\"om/1.0.0\"")) {
                throw new OWSWebServiceException("only text/xml; subtype=\"om/1.0.0\" is accepted for responseFormat", 
                                                 INVALID_PARAMETER_VALUE, "responseFormat", version);
            }
         } else {
            throw new OWSWebServiceException("Response format text/xml;subtype=\"om/1.0.0\" must be specify",
                                             MISSING_PARAMETER_VALUE, "responseFormat", version);
         }
        
        //we get the mode of result
        ObservationCollectionEntry response = new ObservationCollectionEntry();
        try {
            boolean template  = false;
            ResponseModeType mode;
            if (requestObservation.getResponseMode() == null) {
                logger.info("responseMode was null");
                mode = ResponseModeType.INLINE; 
            } else {
                try {
                    mode = ResponseModeType.fromValue(requestObservation.getResponseMode());
                } catch (IllegalArgumentException e) {
                    throw new OWSWebServiceException(" the response Mode: " + requestObservation.getResponseMode() + " is not supported by the service (inline or template available)!",
                                                     INVALID_PARAMETER_VALUE, "responseMode", version);
                }
            }
            StringBuilder SQLrequest = new StringBuilder("SELECT name FROM observations WHERE name LIKE '%");
            
            if (mode == ResponseModeType.INLINE) {
                SQLrequest.append(observationIdBase).append("%' AND ");
            } else if (mode == ResponseModeType.RESULT_TEMPLATE) {
                SQLrequest.append(observationTemplateIdBase).append("%' AND ");
                template = true;
            } else {
                throw new OWSWebServiceException(" this response Mode is not supported by the service (inline or template available)!",
                                                 OPERATION_NOT_SUPPORTED, "responseMode", version);
            }
            
            ObservationOfferingEntry off;
            //we verify that there is an offering 
            if (requestObservation.getOffering() == null) {
                throw new OWSWebServiceException(" offering must be specify!",
                                                 MISSING_PARAMETER_VALUE, "offering", version);
            } else {
                try {
                    off = offTable.getEntry(requestObservation.getOffering());
                } catch (NoSuchRecordException ex) {
                    throw new OWSWebServiceException("this offering is not registered in the service",
                                                  INVALID_PARAMETER_VALUE, "offering", version);
                } catch (CatalogException ex) {
                  throw new OWSWebServiceException("Catalog exception while getting the offering",
                                                  NO_APPLICABLE_CODE, "offering", version);
                }
                
            }
            
            //we verify that the srsName (if there is one) is advertised in the offering
            if (requestObservation.getSrsName() != null) {
                if (!off.getSrsName().contains(requestObservation.getSrsName())) {
                    throw new OWSWebServiceException("This srs name is not advertised in the offering",
                                                    INVALID_PARAMETER_VALUE, "srsName", version);
                }
            }
            
            //we verify that the resultModel (if there is one) is advertised in the offering
            if (requestObservation.getResultModel() != null) {
                if (!off.getResultModel().contains(requestObservation.getResultModel())) {
                    throw new OWSWebServiceException("This result model is not advertised in the offering",
                                                    INVALID_PARAMETER_VALUE, "resultModel", version);
                }
            }
            
            //we get the list of process
            List<String> procedures = requestObservation.getProcedure();
            SQLrequest.append(" ( ");
            if (procedures.size() != 0 ) {
                        
                for (String s: procedures) {
                    String dbId = map.getProperty(s);
                    if ( dbId == null) {
                        dbId = s;
                    } 
                    logger.info("process ID: " + dbId);
                    ReferenceEntry proc = null;
                    try {
                        proc = getReferenceFromHRef(dbId); 
                    } catch (NoSuchRecordException ex) {
                        throw new OWSWebServiceException(" this process is not registred in the table",
                                                      INVALID_PARAMETER_VALUE, "procedure", version);
                    } catch (CatalogException ex) {
                        throw new OWSWebServiceException("Catalog exception while getting the procedure",
                                                         NO_APPLICABLE_CODE, "offering", version);
                    }
                     
                    if (!off.getProcedure().contains(proc)) {
                       throw new OWSWebServiceException(" this process is not registred in the offering",
                                                        INVALID_PARAMETER_VALUE, "procedure", version);
                    } else {
                        SQLrequest.append(" procedure='").append(dbId).append("' OR ");
                    }
                }
            } else {
            //if is not specified we use the process of the offering   
                for (ReferenceEntry proc: off.getProcedure()) {
                
                    SQLrequest.append(" procedure='").append(proc.getHref()).append("' OR ");
                } 
            }
        
            SQLrequest.delete(SQLrequest.length() - 3, SQLrequest.length());
            SQLrequest.append(") "); 
        
            //we get the list of phenomenon 
            //TODO verifier que les pheno appartiennent a l'offering
            List<String> observedProperties = requestObservation.getObservedProperty();
            if (observedProperties.size() != 0 ) {
                PhenomenonTable phenomenons                   = OMDatabase.getTable(PhenomenonTable.class);
                CompositePhenomenonTable compositePhenomenons = OMDatabase.getTable(CompositePhenomenonTable.class);
                SQLrequest.append(" AND( ");
            
                for (String s: observedProperties) {
                    if (s.indexOf(phenomenonIdBase) != -1){
                        s = s.replace(phenomenonIdBase, "");
                    }
                    CompositePhenomenonEntry cphen = null;
                    try {
                        compositePhenomenons.getEntry(s);
                    } catch (NoSuchRecordException ex) {
                        //we let continue to look if it is a phenomenon (simple)
                    } catch (CatalogException ex){
                        throw new OWSWebServiceException("Catalog exception while getting the phenomenon",
                                                        NO_APPLICABLE_CODE, "observedProperty", version);
                    }
                     
                        
                    if (cphen == null ) {
                        PhenomenonEntry phen = null;
                        try {
                            phen = (PhenomenonEntry) phenomenons.getEntry(s);
                        } catch (NoSuchRecordException ex) {
                            throw new OWSWebServiceException(" this phenomenon " + s + " is not registred in the database!",
                                                          INVALID_PARAMETER_VALUE, "observedProperty", version);
                        } catch (CatalogException ex){
                            throw new OWSWebServiceException("Catalog exception while getting the phenomenon",
                                                             NO_APPLICABLE_CODE, "observedProperty", version);
                        }
                        if (phen != null) {
                            SQLrequest.append(" observed_property='").append(s).append("' OR ");
                            
                        }
                    } else {
                        SQLrequest.append(" observed_property_composite='").append(s).append("' OR ");
                    }
                }
            
                SQLrequest.delete(SQLrequest.length() - 3, SQLrequest.length());
                SQLrequest.append(") "); 
            }
        
            
            //we treat the time restriction
            List<EventTime> times = requestObservation.getEventTime();
            AbstractTimeGeometricPrimitiveType templateTime = treatEventTimeRequest(times, SQLrequest, template);
                    
            //we treat the restriction on the feature of interest
            if (requestObservation.getFeatureOfInterest() != null) {
                GetObservation.FeatureOfInterest foi = requestObservation.getFeatureOfInterest();
                SamplingPointTable foiTable = OMDatabase.getTable(SamplingPointTable.class);
                
                // if the request is a list of station
                if (!foi.getObjectID().isEmpty()) {
                    SQLrequest.append(" AND (");
                    for (final String s : foi.getObjectID()) {
                        //verify that the station is registred in the DB.
                        try {
                            foiTable.getEntry(s);
                        } catch (NoSuchRecordException ex){
                            throw new OWSWebServiceException("the feature of interest is not registered",
                                                             INVALID_PARAMETER_VALUE, "featureOfInterest", version);
                        } catch (CatalogException ex){
                            throw new OWSWebServiceException("Catalog exception while getting the feature of interest",
                                                             NO_APPLICABLE_CODE, "featureOfInterest", version);
                        }
                        SQLrequest.append("feature_of_interest_point='").append(s).append("' OR");
                    }
                    SQLrequest.delete(SQLrequest.length() - 2, SQLrequest.length());
                    SQLrequest.append(") ");
            
                // if the request is a spatial operator    
                } else {
                    // for a BBOX Spatial ops
                    if (foi.getBBOX() != null) {
                        
                        if (foi.getBBOX().getEnvelope() != null && 
                            foi.getBBOX().getEnvelope().getLowerCorner().getValue().size() == 2 &&
                            foi.getBBOX().getEnvelope().getUpperCorner().getValue().size() == 2 ) {
                            SQLrequest.append(" AND (");
                            boolean add = false;
                            EnvelopeEntry e = foi.getBBOX().getEnvelope();
                            for (ReferenceEntry refStation:off.getFeatureOfInterest()) {
                                SamplingPointEntry station = null;
                                try {
                                    station = foiTable.getEntry(refStation.getHref());
                                } catch (NoSuchTableException ex){
                                    throw new OWSWebServiceException("the feature of interest is not registered",
                                                                     INVALID_PARAMETER_VALUE, "", version);
                                } catch (CatalogException ex){
                                     throw new OWSWebServiceException("Catalog exception while getting the feature of interest",
                                                                      NO_APPLICABLE_CODE, "observedProperty", version);
                        }
                                if (station instanceof SamplingPointEntry) {
                                    SamplingPointEntry sp = (SamplingPointEntry) station;
                                    if(sp.getPosition().getPos().getValue().get(0)>e.getUpperCorner().getValue().get(0) &&
                                       sp.getPosition().getPos().getValue().get(0)<e.getLowerCorner().getValue().get(0) &&
                                       sp.getPosition().getPos().getValue().get(1)>e.getUpperCorner().getValue().get(1) &&
                                       sp.getPosition().getPos().getValue().get(1)<e.getLowerCorner().getValue().get(1)) {
                                    
                                        add = true;
                                        SQLrequest.append("feature_of_interest_point='").append(sp.getId()).append("' OR");
                                    } else {
                                        logger.info(" the feature of interest " + sp.getId() + " is not in the BBOX");
                                    }
                                }
                            }
                            if (add) {
                                SQLrequest.delete(SQLrequest.length() - 3, SQLrequest.length());
                                SQLrequest.append(") ");
                            } else {
                                SQLrequest.delete(SQLrequest.length() - 5, SQLrequest.length());
                            }
                        
                        } else {
                            throw new OWSWebServiceException("the envelope is not build correctly",
                                                         INVALID_PARAMETER_VALUE, "", version);
                        }
                    } else {
                        throw new OWSWebServiceException("This operation is not take in charge by the Web Service",
                                                     OPERATION_NOT_SUPPORTED, "", version);
                    }
                }
            
            }
        
            //TODO we treat the restriction on the result
            if (requestObservation.getResult() != null) {
                GetObservation.Result result = requestObservation.getResult();
                
                //we treat the different operation
                if (result.getPropertyIsLessThan() != null) {
                    
                    if (result.getPropertyIsLessThan().getExpressionOrLiteralOrPropertyName().size() != 2) {
                        throw new OWSWebServiceException(" to use the operation Less Than you must specify the propertyName and the litteral",
                                                      MISSING_PARAMETER_VALUE, "lessThan", version);
                    } 
                    String propertyName  = null;
                    LiteralType literal  = null;
                    
                    for (Object j:result.getPropertyIsLessThan().getExpressionOrLiteralOrPropertyName()){
                        
                        if (j instanceof String) {
                            propertyName = (String)j;
                        } else if (j instanceof LiteralType) {
                            literal = (LiteralType)j;
                        } else {
                            throw new OWSWebServiceException("This type of parameter is not accepted by the SOS service: " + j.getClass().getSimpleName() + "!",
                                                          INVALID_PARAMETER_VALUE, "lessThan", version);
                        }
                    }
                    if (propertyName == null || propertyName.equals("") || literal == null) {
                            throw new OWSWebServiceException("This property name and literal must be specify",
                                                          INVALID_PARAMETER_VALUE, "result", version);
                            
                    }
                
                } else if (result.getPropertyIsGreaterThan() != null) {
                    logger.info("PROP IS GREATER");
                    if (result.getPropertyIsGreaterThan().getExpressionOrLiteralOrPropertyName().size() != 2) {
                        throw new OWSWebServiceException(" to use the operation Greater Than you must specify the propertyName and the litteral",
                                                     MISSING_PARAMETER_VALUE, "greaterThan", version);
                    } 
                    String propertyName  = null;
                    LiteralType literal  = null;
                    
                    for (Object j:result.getPropertyIsGreaterThan().getExpressionOrLiteralOrPropertyName()){
                        
                        if (j instanceof String) {
                            propertyName = (String)j;
                        } else if (j instanceof LiteralType) {
                            literal = (LiteralType)j;
                        } else {
                            throw new OWSWebServiceException("This type of parameter is not accepted by the SOS service: " + j.getClass().getSimpleName() + "!",
                                                          INVALID_PARAMETER_VALUE, "greaterThan", version);
                        }
                    }
                    if (propertyName == null || propertyName.equals("") || literal == null) {
                            throw new OWSWebServiceException("This property name and literal must be specify",
                                                          INVALID_PARAMETER_VALUE, "result", version);
                            
                    }
                
                } else if (result.getPropertyIsEqualTo() != null) {
                    
                    logger.info("PROP IS EQUAL");
                    if (result.getPropertyIsEqualTo().getExpressionOrLiteralOrPropertyName().size() != 2) {
                         throw new OWSWebServiceException(" to use the operation Equal you must specify the propertyName and the litteral",
                                                       MISSING_PARAMETER_VALUE, "propertyIsEqualTo", version);
                    } 
                    String propertyName  = null;
                    LiteralType literal  = null;
                    
                    for (Object j:result.getPropertyIsEqualTo().getExpressionOrLiteralOrPropertyName()){
                        
                        if (j instanceof String) {
                            propertyName = (String)j;
                            logger.info("j=|" + propertyName + "|" + propertyName.length());
                        } else if (j instanceof LiteralType) {
                            literal = (LiteralType)j;
                        } else {
                            throw new OWSWebServiceException("This type of parameter is not accepted by the SOS service: " + j.getClass().getSimpleName() + "!",
                                                          INVALID_PARAMETER_VALUE, "propertyIsEqualTo", version);
                        }
                    }
                    if (propertyName == null || propertyName.equals("") || literal == null) {
                            throw new OWSWebServiceException("This property name and literal must be specify",
                                                          INVALID_PARAMETER_VALUE, "result", version);
                            
                    }
                
                } else if (result.getPropertyIsLike() != null) {
                    throw new OWSWebServiceException("This operation is not take in charge by the Web Service",
                                                  OPERATION_NOT_SUPPORTED, "propertyIsLike", version);

                } else if (result.getPropertyIsBetween() != null) {
                    
                    logger.info("PROP IS BETWEEN");
                    if (result.getPropertyIsBetween().getPropertyName() == null) {
                        throw new OWSWebServiceException("To use the operation Between you must specify the propertyName and the litteral",
                                                      MISSING_PARAMETER_VALUE, "propertyIsBetween", version);
                    } 
                    String propertyName  = result.getPropertyIsBetween().getPropertyName();
                    
                    LiteralType LowerLiteral  = result.getPropertyIsBetween().getLowerBoundary().getLiteral();
                    LiteralType UpperLiteral  = result.getPropertyIsBetween().getUpperBoundary().getLiteral();
                    
                    if (propertyName == null || propertyName.equals("") || LowerLiteral == null || UpperLiteral == null) {
                            throw new OWSWebServiceException("This property name, lower and upper literal must be specify",
                                                          INVALID_PARAMETER_VALUE, "result", version);
                            
                    }
                
                } else {
                    throw new OWSWebServiceException("This operation is not take in charge by the Web Service",
                                                  OPERATION_NOT_SUPPORTED, null, version);
                }
            }
            logger.info("request:" + SQLrequest.toString());
        
            //TODO remplacer par une filteredList ds postgrid
            Statement stmt    = OMDatabase.getConnection().createStatement();
            ResultSet results = stmt.executeQuery(SQLrequest.toString());
            try {
                while (results.next()) {
                    ObservationEntry o = (ObservationEntry) obsTable.getEntry(results.getString(1));
                    if (template) {
                    
                        String temporaryTemplateId = o.getName() + '-' + getTemplateSuffix(o.getName());
                        ObservationEntry temporaryTemplate = o.getTemporaryTemplate(temporaryTemplateId, templateTime);
                        templates.put(temporaryTemplateId, temporaryTemplate);
                    
                        // we launch a timer which will destroy the template in one hours
                        Timer t = new Timer();
                        //we get the date and time for now
                        Calendar now = new GregorianCalendar();
                        long next = now.getTimeInMillis() + templateValidTime;
                        Date d = new Date(next);
                        logger.info("this template will be destroyed at:" + d.toString());
                        t.schedule(new DestroyTemplateTask(temporaryTemplateId), d);
                        response.add(temporaryTemplate);
                    } else {
                        response.add(o);
                    
                        //we stop the request if its too big
                        if (response.getMember().size() > maxObservationByRequest) {
                            throw new OWSWebServiceException("Your request is to voluminous please add filter and try again",
                                                          NO_APPLICABLE_CODE, null, version);
                        }
                    }
                }
            } catch (CatalogException ex) {
                  throw new OWSWebServiceException("Catalog exception while getting the observations: " + ex.getMessage(),
                                                  NO_APPLICABLE_CODE, "getObservation", version);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OWSWebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        } catch (NoSuchTableException e) {
            e.printStackTrace();
            throw new OWSWebServiceException("the service has throw a NoSuchTableException:" + e.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        }
        return normalizeDocument(response);
    }
    
    /**
     * Web service operation
     */
    public GetResultResponse getResult(GetResult requestResult) throws WebServiceException {
        logger.info("getResult request processing"  + '\n');
        //we verify the base request attribute
        verifyBaseRequest(requestResult);
        
        ObservationEntry template = null;
        if (requestResult.getObservationTemplateId() != null) {
            String id = requestResult.getObservationTemplateId();
            template = templates.get(id);
            if (template == null) {
                throw new OWSWebServiceException("this template does not exist or is no longer usable",
                                              INVALID_PARAMETER_VALUE, "ObservationTemplateId", version);
            }
        } else {
            throw new OWSWebServiceException("ObservationTemplateID must be specified",
                                          MISSING_PARAMETER_VALUE, "ObservationTemplateId", version);
        }
        
        //we begin to create the sql request
        StringBuilder SQLrequest = new StringBuilder("SELECT result FROM observations WHERE ");
        
        //we add to the request the property of the template
        SQLrequest.append("procedure='").append(((ProcessEntry)template.getProcedure()).getHref()).append("'");
        
        if (template.getSamplingTime() instanceof TimeInstantType) {
            TimeInstantType ti = (TimeInstantType) template.getSamplingTime();
            
            SQLrequest.append("AND sampling_time_begin>'").append(ti.getTimePosition().getValue()).append("'");
        
        } else if (template.getSamplingTime() instanceof TimePeriodType) {
            TimePeriodType tp = (TimePeriodType) template.getSamplingTime();
            
            SQLrequest.append("AND sampling_time_begin>'").append(tp.getBeginPosition().getValue()).append("'");
            if (tp.getEndPosition()!= null && !tp.getEndPosition().getValue().equals("")) {
                SQLrequest.append("AND ( sampling_time_end<'").append(tp.getEndPosition().getValue()).append("'");
                SQLrequest.append("OR sampling_time_end IS NULL)");
            }
        }
        
        //we treat the time constraint
        if (requestResult.getEventTime() != null) {
            List<EventTime> times = requestResult.getEventTime();
            treatEventTimeRequest(times, SQLrequest, false);
        }
        
        //we prepare the response document
        GetResultResponse response = null;
        try {
            logger.info(SQLrequest.toString());
            Statement stmt    = OMDatabase.getConnection().createStatement();
            ResultSet results = stmt.executeQuery(SQLrequest.toString());
            AnyResultTable resTable = OMDatabase.getTable(AnyResultTable.class);
            String datablock = "";
            while (results.next()) {
                AnyResultEntry a = resTable.getEntry(results.getString(1));
                if (a != null) {
                    if (a.getArray() != null) {
                        datablock += a.getArray().getValues() + '\n';
                    } else {
                        throw new IllegalArgumentException("Array is null");
                    }
                }
            }

            GetResultResponse.Result r = new GetResultResponse.Result(datablock, serviceURL + '/' + requestResult.getObservationTemplateId());
            response = new GetResultResponse(r);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OWSWebServiceException("The service has throw an SQL exception in GetResult Operation",
                                          NO_APPLICABLE_CODE, null, version);
        } catch (CatalogException e) {
            e.printStackTrace();
            throw new OWSWebServiceException("the service has throw a Catalog Exception:" + e.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        }

        return response;
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
        
        Savepoint savePoint = null;
        boolean success = false;
        String id = "";
        try {
            //we begin a transaction
            sensorMLConnection.setAutoCommit(false);
            savePoint = sensorMLConnection.setSavepoint("registerSensorTransaction");
            
            //we get the SensorML file who describe the Sensor to insert.
            RegisterSensor.SensorDescription d = requestRegSensor.getSensorDescription();
            String process  = (String)d.getAny();     
            
            //we get the observation template provided with the sensor description.
            ObservationTemplate temp = requestRegSensor.getObservationTemplate();
            ObservationEntry obs = temp.getObservation();
            if(obs == null) {
                throw new OWSWebServiceException("observation template must be specify",
                                              MISSING_PARAMETER_VALUE,
                                              "observationTemplate",
                                              version);
            } else if (!obs.isComplete()) {
                throw new OWSWebServiceException("observation template must specify at least the following fields: procedure ,observedProperty ,featureOfInterest",
                                              INVALID_PARAMETER_VALUE,
                                              "observationTemplate",
                                              version); 
            }
            
            //we decode the content
            String decodedprocess = java.net.URLDecoder.decode(process, "ISO-8859-1");
            logger.severe("process null = " + (decodedprocess== null));
            
            //we create a new Tempory File SensorML
            File tempFile = new File(temporaryFolder + "/temp.xml");
            FileOutputStream outstr = new FileOutputStream(tempFile);
            OutputStreamWriter outstrR = new OutputStreamWriter(outstr,"UTF-8");
            BufferedWriter output = new BufferedWriter(outstrR);
            output.write(decodedprocess);
            output.flush();
            output.close();
            
            //we reate a new Identifier from the SensorML database
            int num = getSensorId();
            id = sensorIdBase + num;
            
            //we parse the temporay xmlFile
            Reader XMLReader = new Reader(sensorMLReader,temporaryFolder + "/temp.xml",sensorMLWriter);
            
            //and we write it in the sensorML Database
            Catalog cat  = sensorMLReader.getCatalog("SMLC");
            User u       = sensorMLReader.getUser("admin");
            Form f       = XMLReader.readForm(cat, u,"source",id, Standard.SENSORML);
            sensorMLWriter.writeForm(f, false);
            
            
            // we record the mapping between physical id and database id
            String phyId = recordMapping(f, id);
            // and we record the position of the piezometer
            recordSensorLocation(f, phyId);
                                    
            //we write the observation template in the O&M database
            
            //we assign the new capteur id to the observation template
            ProcessEntry p = new ProcessEntry(id);
            obs.setProcedure(p);
            obs.setName(observationTemplateIdBase + num);
            logger.info(obs.toString());
            if (obsTable != null) {
                obsTable.getIdentifier(obs);
                   
                // we add the sensor to the offering specified in the sensorML Document
                addSensorToOffering(f, obs);
                     
            } else {
                throw new OWSWebServiceException("error with the database, the service can't retrieve the observation Table",
                                                 NO_APPLICABLE_CODE, null, version);
           }
           success = true; 
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OWSWebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                             NO_APPLICABLE_CODE, null, version);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new OWSWebServiceException("The service cannot find the temporary file " + temporaryFolder + "/temp.xml",
                                          NO_APPLICABLE_CODE, null, version);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new OWSWebServiceException("the service has throw an IOException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            throw new OWSWebServiceException("The service has throw a ParserException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        } catch (SAXException ex) {
            ex.printStackTrace();
            throw new OWSWebServiceException("The service has throw a SAXException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        } catch (CatalogException ex) {
            ex.printStackTrace();
            throw new OWSWebServiceException("The service has throw a CatalogException:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        } catch (MalFormedDocumentException ex) {
            ex.printStackTrace();
            logger.severe("MalFormedDocumentException:" + ex.getMessage());
            throw new OWSWebServiceException("The SensorML Document is Malformed",
                                          INVALID_PARAMETER_VALUE,
                                          "sensorDescription", version);
        } finally {
            try {
                if (!success && savePoint != null) {
                   sensorMLConnection.rollback(savePoint);
                } else {
                    if (savePoint != null)
                        sensorMLConnection.releaseSavepoint(savePoint); 
                }
                sensorMLConnection.commit();
                sensorMLConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new OWSWebServiceException("the service has throw a SQL Exception:" + e.getMessage(),
                                              NO_APPLICABLE_CODE,
                                              null,
                                              version);
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
        try {
            //we get the id of the sensor and we create a sensor object
            String sensorId   = requestInsObs.getAssignedSensorId();
            int num = -1;
            if (sensorId.startsWith(sensorIdBase)) {
                num = Integer.parseInt(sensorId.substring(sensorIdBase.length()));
            } else {
                throw new OWSWebServiceException("The sensor identifier is not valid",
                                             INVALID_PARAMETER_VALUE, "assignedSensorId", version);
            }
            ProcessEntry proc = new ProcessEntry(sensorId);
            
            //we get the observation and we assign to it the sensor
            ObservationEntry obs = requestInsObs.getObservation();
            obs.setProcedure(proc);
            obs.setName(getObservationId());
            
            //we record the observation in the O&M database
           if (obs instanceof MeasurementEntry) {
               MeasurementEntry meas = (MeasurementEntry)obs;
                MeasurementTable measTable = OMDatabase.getTable(MeasurementTable.class);
                id = measTable.getIdentifier(meas);
            } else if (obs instanceof ObservationEntry) {
                
                //in first we verify that the observation is conform to the template
                ObservationEntry template = (ObservationEntry)obsTable.getEntry( observationTemplateIdBase + num);
                //if the observation to insert match the template we can insert it in the OM db
                if (obs.matchTemplate(template)) {
                    if (obs.getSamplingTime() != null && obs.getResult() != null) {
                        id = obsTable.getIdentifier(obs);
                        logger.info("new observation inserted:"+ "id = " + id + '\n' + obs.getSamplingTime().toString() + '\n' + obs.getResult().toString());
                    } else {
                        throw new OWSWebServiceException("The observation sampling time and the result must be specify",
                                                      MISSING_PARAMETER_VALUE, "samplingTime", version);
                    }
                } else {
                    throw new OWSWebServiceException(" The observation doesn't match with the template of the sensor",
                                                  INVALID_PARAMETER_VALUE, "samplingTime", version);
                }
            } 
            
        } catch( SQLException e) {
            e.printStackTrace();
            throw new OWSWebServiceException("The service has throw a SQLException:" + e.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        } catch( CatalogException e) {
            e.printStackTrace();
            throw new OWSWebServiceException("The service has throw a CatalogException:" + e.getMessage(),
                                          NO_APPLICABLE_CODE, null, version);
        }
         
        return new InsertObservationResponse(id);
    }
    
   
    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    private int getSensorId() throws SQLException {
        PreparedStatement stmt = sensorMLConnection.prepareStatement("SELECT Count(*) FROM \"Forms\" WHERE title LIKE '%" + sensorIdBase + "%' ");
        ResultSet res = stmt.executeQuery();
        int id = -1;
        while (res.next()) {
            id = res.getInt(1);
        }
        
        return (id + 1);
        
    }
    
    /**
     * Create a new identifier for an observation by searching in the O&M database.
     */
    private String getObservationId() throws SQLException {
        PreparedStatement stmt = OMDatabase.getConnection().prepareStatement("SELECT Count(*) FROM \"observations\" WHERE name LIKE '%" + observationIdBase + "%' ");
        ResultSet res = stmt.executeQuery();
        int id = -1;
        while (res.next()) {
            id = res.getInt(1);
        }
        return observationIdBase + (id + 1);
        
    }
    
    /**
     * TODO factoriser
     * 
     * @param times A list of time constraint.
     * @param SQLrequest A stringBuilder building the SQL request.
     * 
     * @return true if there is no errors in the time constraint else return false.
     */
    private AbstractTimeGeometricPrimitiveType treatEventTimeRequest(List<EventTime> times, StringBuilder SQLrequest, boolean template) throws WebServiceException {
        
        //In mode template this method return a temporal Object.
        AbstractTimeGeometricPrimitiveType templateTime = null;
        
        if (times.size() != 0) {
            if (!template)
                SQLrequest.append("AND (");
            
            for (EventTime time: times) {
                
                // The operation Time Equals
                if (time.getTEquals() != null && time.getTEquals().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    Object j;
                    if(time.getTEquals().getRest().size() == 2) {
                       String propertyName = (String)time.getTEquals().getRest().get(0);
                       j = time.getTEquals().getRest().get(1);
                    } else {
                       j = time.getTEquals().getRest().get(0);
                    }
                    
                    
                    //if the temporal object is a timePeriod
                    if (j instanceof TimePeriodType) {
                        TimePeriodType tp = (TimePeriodType)j;
                        String begin = getTimeValue(tp.getBeginPosition());
                        String end   = getTimeValue(tp.getEndPosition()); 
                        if (!template) {
                            SQLrequest.append(" sampling_time_begin='").append(begin).append("' AND ");
                            SQLrequest.append(" sampling_time_end='").append(end).append("') ");
                        } else {
                            templateTime = tp;
                        }
                    
                    // if the temporal object is a timeInstant    
                    } else if (j instanceof TimeInstantType) {
                        TimeInstantType ti = (TimeInstantType) j;
                        String position = getTimeValue(ti.getTimePosition());
                        if (!template) {
                            SQLrequest.append(" sampling_time_begin='").append(position).append("' AND sampling_time_end=NULL )");
                        } else {
                            templateTime = ti;
                        }
                    } else {
                        throw new OWSWebServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, "eventTime", version);
                    }
                
                // The operation Time before    
                } else if (time.getTBefore() != null && time.getTBefore().getRest().size() != 0) {

                    // we get the property name (not used for now)
                    Object j;
                    if(time.getTBefore().getRest().size() == 2) {
                       String propertyName = (String)time.getTBefore().getRest().get(0);
                       j = time.getTBefore().getRest().get(1);
                    } else {
                       j = time.getTBefore().getRest().get(0);
                    }
                    
                    // for the operation before the temporal object must be an timeInstant
                    if (j instanceof TimeInstantType) {
                        TimeInstantType ti = (TimeInstantType)j;
                        String position = getTimeValue(ti.getTimePosition());
                        if (!template) { 
                            SQLrequest.append("sampling_time_begin<'").append(position).append("' )");
                        } else {
                            templateTime = ti;
                        }
                        
                    } else {
                        throw new OWSWebServiceException("TM_Before operation require timeInstant!",
                                                      INVALID_PARAMETER_VALUE, "eventTime", version);
                    }
                    
                // The operation Time after    
                } else if (time.getTAfter() != null && time.getTAfter().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    Object j;
                    if(time.getTAfter().getRest().size() == 2) {
                       String propertyName = (String)time.getTAfter().getRest().get(0);
                       j = time.getTAfter().getRest().get(1);
                    } else {
                       j = time.getTAfter().getRest().get(0);
                    }
                    
                    // for the operation after the temporal object must be an timeInstant
                    if (j instanceof TimeInstantType) {
                        TimeInstantType ti = (TimeInstantType)j;
                        String position = getTimeValue(ti.getTimePosition());
                        if (!template) {
                            SQLrequest.append("sampling_time_begin>'").append(position).append("' )");
                        } else {
                            templateTime = ti;
                        }
                    } else {
                       throw new OWSWebServiceException("TM_After operation require timeInstant!",
                                                     INVALID_PARAMETER_VALUE, "eventTime", version);
                    }
                    
                // The time during operation    
                } else if (time.getTDuring() != null && time.getTDuring().getRest().size() != 0) {
                    
                    // we get the property name (not used for now)
                    Object j;
                    if(time.getTDuring().getRest().size() == 2) {
                       String propertyName = (String)time.getTDuring().getRest().get(0);
                       j = time.getTDuring().getRest().get(1);
                    } else {
                       j = time.getTDuring().getRest().get(0);
                    }
                    
                    if (j instanceof TimePeriodType) {
                        TimePeriodType tp = (TimePeriodType)j;
                        String begin = getTimeValue(tp.getBeginPosition());
                        String end   = getTimeValue(tp.getEndPosition()); 
                        
                        if (!template) {
                            SQLrequest.append(" sampling_time_begin>'").append(begin).append("' AND ");
                            SQLrequest.append(" (sampling_time_end<'").append(end).append("' OR  sampling_time_end IS NULL)) ");
                        } else {
                            templateTime = tp;
                        }
                    } else {
                        throw new OWSWebServiceException("TM_During operation require TimePeriod!",
                                                      INVALID_PARAMETER_VALUE, "eventTime", version);
                    }
                } else if (time.getTBegins() != null || time.getTBegunBy() != null || time.getTContains() != null ||time.getTEndedBy() != null || time.getTEnds() != null || time.getTMeets() != null
                           || time.getTOveralps() != null || time.getTOverlappedBy() != null) {
                    throw new OWSWebServiceException("This operation is not take in charge by the Web Service, supported one are: TM_Equals, TM_After, TM_Before, TM_During",
                                                  OPERATION_NOT_SUPPORTED, null, version);
                } else {
                    throw new OWSWebServiceException("Unknow time filter operation, supported one are: TM_Equals, TM_After, TM_Before, TM_During",
                                                  OPERATION_NOT_SUPPORTED, null, version);
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
            value = value.replace('T', ' ');
            
            //we delete the data after the second
            if (value.indexOf('.') != -1) {
                value = value.substring(0, value.indexOf('.'));
            }
             try {
                 //here t is not used but it allow to verify the syntax of the timestamp
                 Timestamp t = Timestamp.valueOf(value);
                 return t.toString();
                 
             } catch(IllegalArgumentException e) {
                throw new OWSWebServiceException("bad format of timestamp: accepted format yyyy-mm-jjThh:mm:ss.msmsms",
                                                 INVALID_PARAMETER_VALUE, "eventTime", version);
             }
          } else {
            throw new OWSWebServiceException("bad format of time, TimePostion mustn't be null",
                                              MISSING_PARAMETER_VALUE, "eventTime", version);
          }
    }
    
    /**
     *  Verify that the bases request attributes are correct.
     */ 
    private void verifyBaseRequest(RequestBaseType request) throws WebServiceException {
        if (request != null) {
            if (request.getService() != null) {
                if (!request.getService().equals("SOS"))  {
                    throw new OWSWebServiceException("service must be \"SOS\"!",
                                                  INVALID_PARAMETER_VALUE, "service", version);
                }
            } else {
                throw new OWSWebServiceException("service must be specified!",
                                              MISSING_PARAMETER_VALUE, "service", version);
            }
            if (request.getVersion()!= null) {
                if (!request.getVersion().equals("1.0.0")) {
                    throw new OWSWebServiceException("version must be \"1.0.0\"!",
                                                  VERSION_NEGOTIATION_FAILED, null, version);
                }
            } else {
                throw new OWSWebServiceException("version must be specified!",
                                              MISSING_PARAMETER_VALUE, "version", version);
            }
         } else { 
            throw new OWSWebServiceException("The request is null!",
                                          NO_APPLICABLE_CODE, null, version);
         }  
        
    }
    
    /**
     * Record the mapping between physical ID and database ID.
     * 
     * @param form The "form" containing the sensorML data.
     * @param dbId The identifier of the sensor in the O&M database.
     */
    private String recordMapping(Form form, String dbId) throws SQLException, FileNotFoundException, IOException {
       
        //we search which identifier is the supervisor code
        int i = 1;
        boolean found = false;
        Value v = null;
        do {
            ValuePath vp = sensorMLReader.getValuePath("SensorML:SensorML:member.1:identification.1:identifier." + i + ":name.1");
            logger.info("SensorML:SensorML:member.1:identification.1:identifier." + i + ":name.1");
            if (vp != null) {
               v = sensorMLReader.getValue(form, "1", vp, -1);
            } else {
                logger.severe("The valuePath is null i=" + i);
                v = null;
            } 
            if (v != null && v instanceof TextValue) {
                logger.info(((TextValue)v).getValue());
                if (((TextValue)v).getValue().equals("supervisorCode")){
                    found = true;
                } else {
                    i++;
                }
            } else {
                logger.info("v pas textValue");
                i++;
            }
        } while (v != null && !found);
        
        if (!found) {
            logger.severe("There is no supervisor code in that SensorML file");
            return "";
        } else {
            v = sensorMLReader.getValue(form, "2", 
                sensorMLReader.getValuePath("SensorML:SensorML:member.1:identification.1:identifier." + i + ":value.1")
                , -1);
        }
        // if we find the value we put it in the mapping properties file and store it.
        if (v instanceof TextValue) {
            logger.severe("PhysicalId:" + ((TextValue)v).getValue());
            map.setProperty(((TextValue)v).getValue(), dbId);
            String env = System.getenv("CATALINA_HOME");
            FileOutputStream out = new FileOutputStream(env + "/bin/mapping.properties");
            map.store(out, "");
       }
       return ((TextValue)v).getValue();
    }
    
    /**
     * Find a newe suffix to obtain a unic temporary template id. 
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
     * Record the position of the sensor in the table system location
     * 
     *  @param form The "form" containing the sensorML data.
     */
    private void recordSensorLocation(Form form, String sensorId) throws SQLException {
        String column      = "";
        String coordinates = "";
        Value v = null;
        
        //we get the srs name
        ValuePath vp = sensorMLReader.getValuePath("SensorML:SensorML:member.1:location.1:pos.1:srsName.1");
        if (vp != null) {
            v = sensorMLReader.getValue(form, "1", vp, -1);
        } else {
            logger.severe("The valuePath is null");
            v = null;
        } 
        if (v != null && v instanceof TextValue) {
            logger.info(((TextValue)v).getValue());
            column = ((TextValue)v).getValue();
            column = column.substring(column.lastIndexOf(':') + 1);
        } else {
            logger.severe("there is no srsName for the piezo location");
            return;
        }
        
        // we get the coordinates
        v = null;
        vp = sensorMLReader.getValuePath("SensorML:SensorML:member.1:location.1:pos.1");
        if (vp != null) {
            v = sensorMLReader.getValue(form, "2", vp, -1);
        } else {
            logger.severe("The valuePath is null");
            v = null;
        } 
        if (v != null && v instanceof TextValue) {
            logger.info(((TextValue)v).getValue());
            coordinates = ((TextValue)v).getValue();
        } else {
            logger.severe("there is no coordinates for the piezo location");
            return;
        }
        String x = coordinates.substring(0, coordinates.indexOf(' '));
        String y = coordinates.substring(coordinates.indexOf(' ') + 1 );
        String request = "";
        if (column.equals("27582"))
            request = "INSERT INTO projected_localisations VALUES ('" + sensorId + "', GeometryFromText( 'POINT(" + x + ' ' + y + ")', " + column + "))";
        else
            request = "INSERT INTO geographic_localisations VALUES ('" + sensorId + "', GeometryFromText( 'POINT(" + x + ' ' + y + ")', " + column + "))";
        logger.info(request);
        Statement stmt    = OMDatabase.getConnection().createStatement();
        stmt.executeUpdate(request);
    }
    
    /**
     * Add the new Sensor to an offering specified in the network attribute of sensorML file.
     * if the offering doesn't yet exist in the database, it will be create.
     * 
     * @param form The "form" contain the sensorML data.
     * @param template The observation template for this sensor.
     */
    private void addSensorToOffering(Form form, Observation template) throws SQLException, CatalogException {
     
        //we search which are the classifier describing the networks
        int i = 1;
        Value v;
        int[] networksIndex = new int[20];
        int size = 0;
        do {
            
            ValuePath vp = sensorMLReader.getValuePath("SensorML:SensorML:member.1:classification.1:classifier." + i + ":name.1");
            if (vp != null)
                v = sensorMLReader.getValue(form, "1", vp, -1);
            else 
                v = null;
            if (v != null && v instanceof TextValue) {
                if (((TextValue)v).getValue().equals("network")){
                    logger.info(((TextValue)v).getValue());
                    networksIndex[size] = i;
                    size++;
                    i++;
                } else {
                    i++;
                }
            } else {
                i++;
            }
        } while (v != null);
        
        if (size == 0) {
            logger.severe("There is no network in that SensorML file");
        } 
            
            // for each network we create (or update) an offering
            for (int j = 0; j < size + 1; j++) {
                if (j != size) {
                    v = sensorMLReader.getValue(form, "2", 
                        sensorMLReader.getValuePath("SensorML:SensorML:member.1:classification.1:classifier." + networksIndex[j] + ":value.1")
                        , -1);
                
                    if (v != null && v instanceof TextValue) {
                        String offeringName = "offering-" + ((TextValue)v).getValue();
                        logger.info("networks:" + offeringName);
                    
                        //we get the offering from the O&M database
                        ObservationOfferingEntry offering = offTable.getEntry(offeringName);
                    
                        //if the offering is already in the database
                        if (offering != null) {
                             
                            //we add the new sensor to the offering
                            ReferenceEntry ref = getReferenceFromHRef(((ProcessEntry)template.getProcedure()).getHref());
                            if (!offering.getProcedure().contains(ref)) {
                                if (ref == null) {
                                   ref = new ReferenceEntry(null, ((ProcessEntry)template.getProcedure()).getHref()); 
                                }
                                OfferingProcedureEntry offProc= new OfferingProcedureEntry(offering.getId(), ref);
                                offTable.getProcedures().getIdentifier(offProc);
                            }
                            
                            //we add the phenomenon to the offering
                            if (!offering.getObservedProperty().contains(template.getObservedProperty())){
                                OfferingPhenomenonEntry offPheno= new OfferingPhenomenonEntry(offering.getId(), (PhenomenonEntry)template.getObservedProperty());
                                offTable.getPhenomenons().getIdentifier(offPheno);
                            }
                            
                            // we add the feature of interest (station) to the offering
                            ref = getReferenceFromHRef(((SamplingFeatureEntry)template.getFeatureOfInterest()).getId());
                            if (!offering.getFeatureOfInterest().contains(ref)) {
                                if (ref == null) {
                                   ref = new ReferenceEntry(null, ((SamplingFeatureEntry)template.getFeatureOfInterest()).getId()); 
                                }
                                OfferingSamplingFeatureEntry offSF= new OfferingSamplingFeatureEntry(offering.getId(), ref);
                                offTable.getStations().getIdentifier(offSF);
                            }
                        // we build a new offering
                        // TODO bounded by??? station?    
                        } else {
                            // for the eventime of the offering we take the time of now.
                            Calendar now = new GregorianCalendar();
                            Timestamp t = new Timestamp(now.getTimeInMillis());
                            TimePeriodType time = new TimePeriodType(new TimePositionType(t.toString()));
                            
                            //we create a new List of process and add the template process to it
                            List<ReferenceEntry> process = new ArrayList<ReferenceEntry>();
                            ReferenceEntry ref = new ReferenceEntry(null, ((ProcessEntry)template.getProcedure()).getHref());
                            process.add(ref);
                        
                            //we create a new List of phenomenon and add the template phenomenon to it
                            List<PhenomenonEntry> phenos = new ArrayList<PhenomenonEntry>();
                            phenos.add((PhenomenonEntry)template.getObservedProperty());
                        
                            //we create a new List of process and add the template process to it
                            List<ReferenceEntry> stations = new ArrayList<ReferenceEntry>();
                            ref = new ReferenceEntry(null, ((SamplingFeatureEntry)template.getFeatureOfInterest()).getId());
                            stations.add(ref);
                        
                            //we create a list of accepted responseMode (fixed)
                            List<ResponseModeType> responses = new ArrayList<ResponseModeType>();
                            responses.add(ResponseModeType.INLINE);
                            responses.add(ResponseModeType.RESULT_TEMPLATE);
                            
                            List<QName> resultModel = new ArrayList<QName>();
                            resultModel.add(new QName("http://www.opengis.net/om/1.0",
                                                      "Observation",
                                                      "om"));
                            List<String> outputFormat = new ArrayList<String>();
                            outputFormat.add("text/xml");
                            
                            List<String> srsName = new ArrayList<String>();
                            srsName.add("EPSG:4326");
                            
                            // we create a the new Offering
                            offering = new ObservationOfferingEntry(offeringName, 
                                                                    offeringIdBase + offeringName,
                                                                    "",
                                                                    null, 
                                                                    null, //TODO boundedby 
                                                                    srsName,
                                                                    time,
                                                                    process,
                                                                    phenos,
                                                                    stations,
                                                                    outputFormat,
                                                                    resultModel,
                                                                    responses);
                            offTable.getIdentifier(offering);
                        }
                    }
                    
                // we add the sensor to the global offering containing all the sensor    
                } else {
                    
                    //we get the offering from the O&M database
                    ObservationOfferingEntry offering = offTable.getEntry("offering-allSensor");
                    
                   if (offering != null) {
                        //we add the new sensor to the offering
                       ReferenceEntry ref = getReferenceFromHRef(((ProcessEntry)template.getProcedure()).getHref());
                       if (!offering.getProcedure().contains(ref)) {
                            if (ref == null){
                                ref = new ReferenceEntry(null, ((ProcessEntry)template.getProcedure()).getHref());
                            }
                            OfferingProcedureEntry offProc= new OfferingProcedureEntry(offering.getId(), ref);
                            offTable.getProcedures().getIdentifier(offProc);
                        }
                        //we add the phenomenon to the offering
                        if (!offering.getObservedProperty().contains(template.getObservedProperty())){
                            OfferingPhenomenonEntry offPheno= new OfferingPhenomenonEntry(offering.getId(), (PhenomenonEntry)template.getObservedProperty());
                            offTable.getPhenomenons().getIdentifier(offPheno);
                        }
                        // we add the feature of interest (station) to the offering
                       ref = getReferenceFromHRef(((SamplingFeatureEntry)template.getFeatureOfInterest()).getId());
                       if (!offering.getFeatureOfInterest().contains(ref)) {
                           if (ref == null) {
                                ref = new ReferenceEntry(null, ((SamplingFeatureEntry)template.getFeatureOfInterest()).getId());
                           }
                           OfferingSamplingFeatureEntry offSF= new OfferingSamplingFeatureEntry(offering.getId(), ref);
                           offTable.getStations().getIdentifier(offSF);
                        }
                    } else {
                         // for the eventime of the offering we take the time of now.
                            Calendar now = new GregorianCalendar();
                            Timestamp t = new Timestamp(now.getTimeInMillis());
                            TimePeriodType time = new TimePeriodType(new TimePositionType(t.toString()));
                            
                            //we create a new List of process and add the template process to it
                            List<ReferenceEntry> process = new ArrayList<ReferenceEntry>();
                            ReferenceEntry ref = new ReferenceEntry(null, ((ProcessEntry)template.getProcedure()).getHref());
                            process.add(ref);
                        
                            //we create a new List of phenomenon and add the template phenomenon to it
                            List<PhenomenonEntry> phenos = new ArrayList<PhenomenonEntry>();
                            phenos.add((PhenomenonEntry)template.getObservedProperty());
                        
                            //we create a new List of process and add the template process to it
                            List<ReferenceEntry> stations = new ArrayList<ReferenceEntry>();
                            ref = new ReferenceEntry(null, ((SamplingFeatureEntry)template.getFeatureOfInterest()).getId());
                            stations.add(ref);
                        
                            //we create a list of accepted responseMode (fixed)
                            List<ResponseModeType> responses = new ArrayList<ResponseModeType>();
                            responses.add(ResponseModeType.RESULT_TEMPLATE);
                        
                            List<QName> resultModel = new ArrayList<QName>();
                            resultModel.add(new QName("http://www.opengis.net/om/1.0",
                                                      "Observation",
                                                      "om"));
                            List<String> outputFormat = new ArrayList<String>();
                            outputFormat.add("text/xml");
                            
                            List<String> srsName = new ArrayList<String>();
                            srsName.add("EPSG:4326");
                            
                            // we create a the new Offering
                            offering = new ObservationOfferingEntry(offeringIdBase + "allSensor", 
                                                                    offeringIdBase + "allSensor",
                                                                    "",
                                                                    null, 
                                                                    null, //TODO boundedby
                                                                    srsName,
                                                                    time,
                                                                    process,
                                                                    phenos,
                                                                    stations,
                                                                    outputFormat,
                                                                    resultModel,
                                                                    responses);
                            offTable.getIdentifier(offering);
                    }
                }
            }
    }
    
    /**
     * Return the referenceEntry with the specified href attribute.
     */
    private ReferenceEntry getReferenceFromHRef(String href) throws SQLException, CatalogException {
        Set<ReferenceEntry> refs = refTable.getEntries();
        Iterator<ReferenceEntry> it = refs.iterator();
        while (it.hasNext()) {
            ReferenceEntry ref = it.next();
            if (ref != null && ref.getHref() != null && ref.getHref().equals(href))
                return ref;
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
     * Return the minimal value for the offering event Time
     */
    private String getMinimalEventTime() throws SQLException {
        PreparedStatement stmt = OMDatabase.getConnection().prepareStatement("select MIN(event_time_begin) from observation_offerings");
        ResultSet res = stmt.executeQuery();
        Timestamp t = null;
        while (res.next()) {
            t = res.getTimestamp(1);
        }
        String ret = null;
        if (t != null) {
            ret = t.toString();
        } 
        return ret;
    }
    
    /**
     * Set the current service version
     */
    public void setVersion(ServiceVersion version){
        this.version = version;
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
