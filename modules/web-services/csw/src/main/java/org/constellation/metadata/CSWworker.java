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
package org.constellation.metadata;

// J2SE dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

// W3C DOM dependecies
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

// Apache Lucene dependencies
import org.apache.lucene.search.Sort;

//Constellation dependencies
import org.constellation.configuration.HarvestTask;
import org.constellation.configuration.HarvestTasks;
import org.geotoolkit.csw.xml.AbstractCswRequest;
import org.geotoolkit.csw.xml.AbstractResultType;
import org.geotoolkit.csw.xml.CswXmlFactory;
import org.geotoolkit.csw.xml.ElementSet;
import org.geotoolkit.csw.xml.ElementSetName;
import org.geotoolkit.csw.xml.GetDomain;
import org.geotoolkit.csw.xml.GetRecordById;
import org.geotoolkit.csw.xml.RequestBase;
import org.geotoolkit.csw.xml.GetRecordByIdResponse;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.GetCapabilities;
import org.geotoolkit.csw.xml.Harvest;
import org.geotoolkit.csw.xml.Transaction;
import org.geotoolkit.csw.xml.DescribeRecord;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.GetDomainResponse;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.AcknowledgementType;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.DeleteType;
import org.geotoolkit.csw.xml.v202.DescribeRecordResponseType;
import org.geotoolkit.csw.xml.v202.ElementSetType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.HarvestResponseType;
import org.geotoolkit.csw.xml.v202.InsertType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.csw.xml.v202.ResultType;
import org.geotoolkit.csw.xml.v202.SearchResultsType;
import org.geotoolkit.csw.xml.v202.TransactionResponseType;
import org.geotoolkit.csw.xml.v202.TransactionSummaryType;
import org.geotoolkit.csw.xml.v202.UpdateType;
import org.geotoolkit.csw.xml.v202.SchemaComponentType;
import org.geotoolkit.csw.xml.v202.EchoedRequestType;
import org.geotoolkit.ebrim.xml.v300.IdentifiableType;
import org.constellation.filter.FilterParser;
import org.constellation.filter.LuceneFilterParser;
import org.constellation.filter.SQLFilterParser;
import org.constellation.filter.SQLQuery;
import org.constellation.generic.database.Automatic;
import org.constellation.jaxb.AnchoredMarshallerPool;
import org.constellation.lucene.filter.SpatialQuery;
import org.constellation.lucene.SearchingException;
import org.constellation.lucene.IndexingException;
import org.constellation.lucene.index.AbstractIndexSearcher;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.utils.MailSendingUtilities;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.v100.DomainType;
import org.geotoolkit.ows.xml.v100.Operation;
import org.geotoolkit.ows.xml.v100.OperationsMetadata;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.ows.xml.v100.ServiceIdentification;
import org.geotoolkit.ows.xml.v100.ServiceProvider;
import org.constellation.util.Util;
import org.constellation.ws.rs.OGCWebService;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.WebService;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.metadata.io.MetadataReader.*;
import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.TypeNames.*;

//geotools dependencies
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.inspire.xml.InspireCapabilitiesType;
import org.geotoolkit.inspire.xml.LanguagesType;
import org.geotoolkit.inspire.xml.MultiLingualCapabilities;
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.ogc.xml.v110modified.FilterCapabilities;
import org.geotoolkit.ogc.xml.v110modified.SortByType;
import org.geotoolkit.ogc.xml.v110modified.SortPropertyType;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.util.logging.MonolineFormatter;

// GeoAPI dependencies
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xml.Namespaces;
import org.opengis.filter.sort.SortOrder;


/**
 * The CSW (Catalog Service Web) engine.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class CSWworker {

    /**
     * use for debugging purpose
     */
    private Logger logger = Logger.getLogger("org.constellation.metadata");
    
    /**
     * A capabilities object containing the static part of the document.
     */
    private Capabilities skeletonCapabilities;
    
    /**
     * The service url.
     */
    private String serviceURL;
    
    /**
     * A Database reader.
     */
    private MetadataReader MDReader;
    
    /**
     * An Database Writer.
     */
    private MetadataWriter MDWriter;
    
    /**
     * The current MIME type of return
     */
    private String outputFormat;
    
    /**
     * A unMarshaller to get object from harvested resource.
     */
    private final MarshallerPool marshallerPool;
    
    /**
     * A lucene index searcher to make quick search on the metadatas.
     */
    private AbstractIndexSearcher indexSearcher;
    
    /**
     * A filter parser whitch create lucene query from OGC filter
     */
    private final FilterParser luceneFilterParser = new LuceneFilterParser();
    
    /**
     * A filter parser whitch create SQL query from OGC filter (used for ebrim query)
     */
    private final FilterParser sqlFilterParser = new SQLFilterParser();
    
    /**
     * A flag indicating if the worker is correctly started.
     */
    private boolean isStarted;
    
    /**
     * A catalogue Harvester comunicating with other CSW 
     */
    private CatalogueHarvester catalogueHarvester;
    
    /**
     * A list of the supported Type name 
     */
    private List<QName> SUPPORTED_TYPE_NAME;
    
    /**
     * A list of supported MIME type. 
     */
    private final static List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = new ArrayList<String>();
        ACCEPTED_OUTPUT_FORMATS.add("text/xml");
        ACCEPTED_OUTPUT_FORMATS.add("application/xml");
        ACCEPTED_OUTPUT_FORMATS.add("text/html");
        ACCEPTED_OUTPUT_FORMATS.add("text/plain");
    }
    
    /**
     * A list of supported resource type. 
     */
    private List<String> ACCEPTED_RESOURCE_TYPE;
    
    /**
     * A list of known CSW server used in distributed search.
     */
    private List<String> cascadedCSWservers;
    
    public final static int DISCOVERY     = 0;
    public final static int TRANSACTIONAL = 1;
    
    /**
     * A flag indicating if the service have to support Transactional operations.
     */
    private int profile;

    /**
     * A factory registry allowing to load various CSW Factory in function of the implementation build.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractCSWFactory.class);


    /**
     * The current version of the service.
     */
    private ServiceVersion actingVersion = new ServiceVersion(ServiceType.CSW, "2.0.2");

    /**
     * A list of schreduled Task (used in close method).
     */
    private List<Timer> schreduledTask = new ArrayList<Timer>();

    /**
     * Default constructor for CSW worker.
     *
     * @param serviceID The service identifier (used in multiple CSW context). default value is "".
     * @param unmarshaller
     * @param marshaller
     */
    public CSWworker(final String serviceID, final MarshallerPool marshallerPool) {
        this(serviceID, marshallerPool, null);
    }
    
    /**
     * Build a new CSW worker with the specified configuration directory
     *
     * @param serviceID The service identifier (used in multiple CSW context). default value is "".
     * @param marshaller A JAXB marshaller to send xml to another CSW service.
     * @param unmarshaller  An Unmarshaller to get object from harvested resource.
     * 
     */
    protected CSWworker(final String serviceID, final MarshallerPool marshallerPool, File configDir) {

        this.marshallerPool = marshallerPool;
        if (configDir == null) {
            configDir    = getConfigDirectory();
            if (configDir == null) {
                logger.severe("The CSW service is not working!" + '\n' +
                              "cause: The configuration directory has not been found");
                isStarted = false;
                return;
            }
        }
        logger.finer("Path to config directory: " + configDir);
        isStarted = true;
        try {
            // we initialize the filterParsers
            JAXBContext jb = JAXBContext.newInstance("org.constellation.generic.database");
            Unmarshaller configUnmarshaller = jb.createUnmarshaller();
            File configFile = new File(configDir, serviceID + "config.xml");
            if (!configFile.exists()) {
                 logger.severe("The CSW service is not working!" + '\n' +
                        "cause: The configuration file has not been found");
                 isStarted = false;
            } else {
                Automatic configuration = (Automatic) configUnmarshaller.unmarshal(configFile);

                // we assign the configuration directory
                configuration.setConfigurationDirectory(configDir);

                // we load the factory from the available classes
                AbstractCSWFactory CSWfactory = factory.getServiceProvider(AbstractCSWFactory.class, null, null,null);
                logger.finer("CSW factory loaded:" + CSWfactory.getClass().getName());

                int datasourceType = configuration.getType();
                //we initialize all the data retriever (reader/writer) and index worker
                MDReader      = CSWfactory.getMetadataReader(configuration);
                profile       = CSWfactory.getProfile(datasourceType);
                AbstractIndexer indexer = CSWfactory.getIndexer(configuration, MDReader, serviceID);
                indexSearcher = CSWfactory.getIndexSearcher(datasourceType, configDir, serviceID);
                MDWriter      = CSWfactory.getMetadataWriter(configuration, indexer);
                catalogueHarvester = new CatalogueHarvester(marshallerPool, MDWriter);
                
                initializeSupportedTypeNames();
                initializeAcceptedResourceType();
                initializeAnchorsMap();
                loadCascadedService(configDir);
                initializeHarvestTask(configDir);
                logger.info("CSW service (" + configuration.getFormat() + ") running");
            }
        } catch (FactoryNotFoundException ex) {
            logger.severe("The CSW service is not working!" + '\n' +
                    "cause: Unable to find a CSW Factory");
            isStarted = false;
        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.severe("The CSW service is not working!" + '\n' +
                    "cause: JAXBException while getting configuration");
            isStarted = false;
        } catch (CstlServiceException e) {
            logger.severe("The CSW service is not working!" + '\n' +
                    "cause:" + e.getMessage());
            isStarted = false;
        } catch (IndexingException e) {
            logger.severe("The CSW service is not working!" + '\n' +
                    "cause:" + e.getMessage());
            isStarted = false;
        } catch (IllegalArgumentException e) {
            logger.severe(e.getMessage());
            logger.severe("The CSW service is not working!" + '\n' +
                    "cause: IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            isStarted = false;
        }
    }
    
    /**
     * In some implementations there is no sicade directory.
     * So if we don't find The .sicade/csw_configuration directory
     * IFREMER hack
     * we search the CATALINA_HOME/webapps/sdn-csw_WS/WEB-INF/csw_configuration
     */
    private File getConfigDirectory() {
        File configDir = new File(WebService.getConfigDirectory(), "csw_configuration/");
        if (configDir.exists()) {
            logger.info("taking configuration from constellation directory: " + configDir.getPath());
            return configDir;
        } else {

            /* Ifremer's server does not contain any .sicade directory, so the
             * configuration files are put under the WEB-INF/classes/configuration directory of the WAR file.
             */
            return Util.getDirectoryFromResource("configuration");
        }
    }

    /**
     * Initialize the supported type names in function of the reader capacity.
     */
    private void initializeSupportedTypeNames() {
        SUPPORTED_TYPE_NAME = new ArrayList<QName>();
        List<Integer> supportedDataTypes = MDReader.getSupportedDataTypes();
        if (supportedDataTypes.contains(ISO_19115))
            SUPPORTED_TYPE_NAME.addAll(ISO_TYPE_NAMES);
        if (supportedDataTypes.contains(DUBLINCORE))
            SUPPORTED_TYPE_NAME.addAll(DC_TYPE_NAMES);
        if (supportedDataTypes.contains(EBRIM)) {
            SUPPORTED_TYPE_NAME.addAll(EBRIM30_TYPE_NAMES);
            SUPPORTED_TYPE_NAME.addAll(EBRIM25_TYPE_NAMES);
        }
                    
    }

    /**
     * Initialize the supported outputSchema in function of the reader capacity.
     */
    private void initializeAcceptedResourceType() {
        ACCEPTED_RESOURCE_TYPE = new ArrayList<String>();
        List<Integer> supportedDataTypes = MDReader.getSupportedDataTypes();
        if (supportedDataTypes.contains(ISO_19115)) {
            ACCEPTED_RESOURCE_TYPE.add("http://www.isotc211.org/2005/gmd");
            ACCEPTED_RESOURCE_TYPE.add("http://www.isotc211.org/2005/gfc");
        }
        if (supportedDataTypes.contains(DUBLINCORE)) {
            ACCEPTED_RESOURCE_TYPE.add("http://www.opengis.net/cat/csw/2.0.2");
        }
        if (supportedDataTypes.contains(EBRIM)) {
            ACCEPTED_RESOURCE_TYPE.add("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
            ACCEPTED_RESOURCE_TYPE.add("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5");
        }
    }

    /**
     * Initialize the Anchors in function of the reader capacity.
     */
    private void initializeAnchorsMap() {
        if (marshallerPool instanceof AnchoredMarshallerPool) {
            AnchoredMarshallerPool pool = (AnchoredMarshallerPool) marshallerPool;
            Map<String, URI> concepts = MDReader.getConceptMap();
            int nbWord = 0;
            for (String key: concepts.keySet()) {
                pool.addAnchor(key,concepts.get(key));
                nbWord ++;
            }
            logger.info(nbWord + " words put in pool.");
        }
    }
    
    /**
     * Load the federated CSW server from a properties file.
     */
    private void loadCascadedService(final File configDirectory) {
        cascadedCSWservers = new ArrayList<String>();
        try {
            // we get the cascading configuration file
            File f = new File(configDirectory, "CSWCascading.properties");
            FileInputStream in = new FileInputStream(f);
            Properties cascad = new Properties();
            cascad.load(in);
            in.close();
            String s = "Cascaded Services:" + '\n';
            for (Object server: cascad.keySet()) {
                String serverName = (String) server;
                String servURL = (String)cascad.getProperty(serverName);
                s = s + servURL + '\n';
                cascadedCSWservers.add(servURL);
            }
            logger.info(s);
            
        } catch (FileNotFoundException e) {
            logger.info("no cascaded CSW server found (optionnal)");
        } catch (IOException e) {
            logger.info("no cascaded CSW server found (optionnal) (IO Exception)");
        }
    }

    private void initializeHarvestTask(final File configDirectory) {
        Unmarshaller unmarshaller = null;
        try {
            // we get the saved harvest task file
            File f = new File(configDirectory, "HarvestTask.xml");
            if (f.exists()) {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(f);
                Timer t = new Timer();
                if (obj instanceof HarvestTasks) {
                    HarvestTasks tasks = (HarvestTasks) obj;
                    for (HarvestTask task : tasks.getTask()) {
                        AsynchronousHarvestTask at = new AsynchronousHarvestTask(catalogueHarvester,
                                                                                 task.getSourceURL(),
                                                                                 task.getResourceType(),
                                                                                 task.getMode(),
                                                                                 task.getEmails());
                        t.scheduleAtFixedRate(at, 2000, task.getPeriod());
                    }
                } else {
                    logger.severe("Bad data type for file HarvestTask.xml");
                }
            } else {
                logger.info("no Harvest task found (optionnal)");
            }

        } catch (JAXBException e) {
            logger.info("JAXB Exception while unmarshalling the file HarvestTask.xml");
            
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
    }

    private void saveSchreduledHarvestTask(String sourceURL, String resourceType, int mode, List<String> emails, long period) {
        HarvestTask newTask       = new HarvestTask(sourceURL, resourceType, mode, emails, period);
        File configDir            = getConfigDirectory();
        File f                    = new File(configDir, "HarvestTask.xml");
        Marshaller marshaller     = null;
        Unmarshaller unmarshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            if (f.exists()) {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object obj   = unmarshaller.unmarshal(f);
                if (obj instanceof HarvestTasks) {
                    HarvestTasks tasks = (HarvestTasks) obj;
                    tasks.addTask(newTask);
                    marshaller.marshal(tasks, f);
                } else {
                    logger.severe("Bad data type for file HarvestTask.xml");
                }
            } else {
                
                f.createNewFile();
                HarvestTasks tasks = new HarvestTasks(Arrays.asList(newTask));
                marshaller.marshal(tasks, f);
            }

        } catch (IOException ex) {
            logger.severe("unable to create a file for schreduled harvest task");
        } catch (JAXBException ex) {
            logger.severe("A JAXB exception occurs when trying to marshall the shreduled harvest task");
            ex.printStackTrace();
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }
            
    
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     */
    public Capabilities getCapabilities(final GetCapabilities requestCapabilities) throws CstlServiceException {
        isWorking();
        logger.info("getCapabilities request processing" + '\n');
        long startTime = System.currentTimeMillis();
        
        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals("CSW")) {
                throw new CstlServiceException("service must be \"CSW\"!", INVALID_PARAMETER_VALUE, "service");
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service");
        }
        AcceptVersions versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("2.0.2")){
                 throw new CstlServiceException("version available : 2.0.2",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }
        AcceptFormats formats = requestCapabilities.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 && !formats.getOutputFormat().contains("text/xml")) {
            /*
             * Acording to the CITE test this case does not return an exception
             throw new OWSWebServiceException("accepted format : text/xml",
                                             INVALID_PARAMETER_VALUE, "acceptFormats",
                                             version);
             */ 
        }

        // if the static capabilities are null we send an exception
        if (skeletonCapabilities == null)
            throw new CstlServiceException("The service was unable to find the capabilities skeleton", NO_APPLICABLE_CODE);

        //we prepare the response document
        Capabilities c = null; 
        
        ServiceIdentification si = null;
        ServiceProvider       sp = null;
        OperationsMetadata    om = null;
        FilterCapabilities    fc = null;
            
        Sections sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType("All");
        }
        
        //according to CITE test a GetCapabilities must always return Filter_Capabilities
        if (!sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains("All"))
            sections.add("Filter_Capabilities");
        
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
            
            if (om != null) {

                // we remove the operation not supported in this profile (transactional/discovery)
                if (profile == DISCOVERY) {
                    om.removeOperation("Harvest");
                    om.removeOperation("Transaction");
                }
            
                // we update the URL
                OGCWebService.updateOWSURL(om.getOperation(), serviceURL, "CSW");

                // we add the cascaded services (if there is some)
                DomainType cascadedCSW  = om.getConstraint("FederatedCatalogues");
                if (cascadedCSW == null) {
                    if (cascadedCSWservers != null && cascadedCSWservers.size() != 0) {
                        DomainType Fcata = new DomainType("FederatedCatalogues", cascadedCSWservers);
                        om.getConstraint().add(Fcata);
                    }
                } else {
                    if (cascadedCSWservers != null && cascadedCSWservers.size() != 0)
                        cascadedCSW.setValue(cascadedCSWservers);
                    else
                        om.removeConstraint(cascadedCSW);
                }
                
                // we update the operation parameters
                Operation gr = om.getOperation("GetRecords");
                if (gr != null) {
                    DomainType os = gr.getParameter("outputSchema");
                    if (os != null) {
                        os.setValue(ACCEPTED_RESOURCE_TYPE);
                    }
                    DomainType tn = gr.getParameter("TypeNames");
                    if (tn != null) {
                        List<String> values = new ArrayList<String>();
                        for (QName qn : SUPPORTED_TYPE_NAME) {
                            values.add(Namespaces.getPreferredPrefix(qn.getNamespaceURI(), "") + ':' + qn.getLocalPart());
                        }
                        tn.setValue(values);
                    }
                    
                    //we update the ISO queryable elements :
                    DomainType isoQueryable = gr.getConstraint("SupportedISOQueryables");
                    if (isoQueryable != null) {
                        List<String> values = new ArrayList<String>();
                        for (String name : ISO_QUERYABLE.keySet() ) {
                            values.add("apiso:" + name);
                        }
                        isoQueryable.setValue(values);
                    }
                    //we update the DC queryable elements :
                    DomainType dcQueryable = gr.getConstraint("SupportedDublinCoreQueryables");
                    if (dcQueryable != null) {
                        List<String> values = new ArrayList<String>();
                        for (String name : DUBLIN_CORE_QUERYABLE.keySet() ) {
                            values.add("dc:" + name);
                        }
                        dcQueryable.setValue(values);
                    }

                    //we update the reader's additional queryable elements :
                    DomainType additionalQueryable = gr.getConstraint("AdditionalQueryables");
                    if (additionalQueryable != null) {
                        List<String> values = new ArrayList<String>();
                        for (QName name : MDReader.getAdditionalQueryableQName()) {
                            values.add(name.getPrefix() + ':' + name.getLocalPart());
                        }
                        additionalQueryable.setValue(values);
                    }
                }
                
                Operation grbi = om.getOperation("GetRecordById");
                if (grbi != null) {
                    DomainType os = grbi.getParameter("outputSchema");
                    if (os != null) {
                        os.setValue(ACCEPTED_RESOURCE_TYPE);
                    }
                }
                
                Operation dr = om.getOperation("DescribeRecord");
                if (dr != null) {
                    DomainType tn = dr.getParameter("TypeName");
                    if (tn != null) {
                        List<String> values = new ArrayList<String>();
                        for (QName qn : SUPPORTED_TYPE_NAME) {
                            values.add(Namespaces.getPreferredPrefix(qn.getNamespaceURI(), "") + ':' + qn.getLocalPart());
                        }
                        tn.setValue(values);
                    }
                }

                //we add the INSPIRE extend capabilties
                LanguagesType languages = new LanguagesType("FR");
                InspireCapabilitiesType inspireCapa = new InspireCapabilitiesType(languages, null);
                MultiLingualCapabilities m = new MultiLingualCapabilities();
                m.setMultiLingualCapabilities(inspireCapa);
                om.setExtendedCapabilities(m);
            }
        }
            
        //we enter the information filter capablities.
        if (sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains("All")) {
            
            fc = skeletonCapabilities.getFilterCapabilities();
        }
            
            
        c = new Capabilities(si, sp, om, "2.0.2", null, fc);

        logger.info("GetCapabilities request processed in " + (System.currentTimeMillis() - startTime) + " ms"); 
        return c;
    }
    
    /**
     * Web service operation which permits to search the catalogue.
     * 
     * @param request
     * @return
     */
    public Object getRecords(final GetRecordsRequest request) throws CstlServiceException {
        logger.info("GetRecords request processing" + '\n');
        long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        
        //we prepare the response
        GetRecordsResponseType response;
        
        String ID = request.getRequestId();
        
        // we initialize the output format of the response
        initializeOutputFormat(request);
        
        //we get the output schema and verify that we handle it
        String outputSchema = "http://www.opengis.net/cat/csw/2.0.2";
        if (request.getOutputSchema() != null) {
            outputSchema = request.getOutputSchema();
            if (!ACCEPTED_RESOURCE_TYPE.contains(outputSchema)) {
                String supportedOutput = "";
                for (String s: ACCEPTED_RESOURCE_TYPE) {
                    supportedOutput = supportedOutput  + s + '\n';
                } 
                throw new CstlServiceException("The server does not support this output schema: " + outputSchema + '\n' +
                                              " supported ones are: " + '\n' + supportedOutput,
                                              INVALID_PARAMETER_VALUE, "outputSchema");
            }
        }
        
        //We get the resultType
        AbstractResultType resultType = ResultType.HITS;
        if (request.getResultType() != null) {
            resultType = request.getResultType();
        }
        
        //We initialize (and verify) the principal attribute of the query
        QueryType query;
        List<QName> typeNames;
        Map<String, QName> variables = new HashMap<String, QName>();
        Map<String, String> prefixs  = new HashMap<String, String>();
        if (request.getAbstractQuery() != null) {
            query = (QueryType)request.getAbstractQuery();
            typeNames =  query.getTypeNames();
            if (typeNames == null || typeNames.size() == 0) {
                throw new CstlServiceException("The query must specify at least typeName.",
                                              INVALID_PARAMETER_VALUE, "TypeNames");
            } else {
                for (QName type : typeNames) {
                    if (type != null) {
                        prefixs.put(type.getPrefix(), type.getNamespaceURI());
                        //for ebrim mode the user can put variable after the Qname
                        if (type.getLocalPart().indexOf('_') != -1 && !type.getLocalPart().startsWith("MD")) {
                            StringTokenizer tokenizer = new StringTokenizer(type.getLocalPart(), "_;");
                            type = new QName(type.getNamespaceURI(), tokenizer.nextToken());
                            while (tokenizer.hasMoreTokens()) {
                                variables.put(tokenizer.nextToken(), type);
                            }
                        }
                    } else {
                        throw new CstlServiceException("The service was unable to read a typeName:" +'\n' +
                                                       "supported one are:" + '\n' + supportedTypeNames(),
                                                       INVALID_PARAMETER_VALUE, "TypeNames");
                    }
                    //we verify that the typeName is supported        
                    if (!SUPPORTED_TYPE_NAME.contains(type)) {
                        System.out.println("TYPE:" + type + " LIST:" + SUPPORTED_TYPE_NAME);
                        String typeName = "null";
                        if (type != null)
                            typeName = type.getLocalPart();
                        throw new CstlServiceException("The typeName " + typeName + " is not supported by the service:" +'\n' +
                                                      "supported one are:" + '\n' + supportedTypeNames(),
                                                      INVALID_PARAMETER_VALUE, "TypeNames");
                    }
                }
                // debugging part
                String var = "variables:" + '\n';
                for (String s : variables.keySet()) {
                    var = var + s + " = " + variables.get(s) + '\n';
                }
                logger.info(var);
                String prefix = "prefixs:" + '\n';
                for (String s : prefixs.keySet()) {
                    prefix = prefix + s + " = " + prefixs.get(s) + '\n';
                }
                logger.info(prefix);
            }
            
        } else {
            throw new CstlServiceException("The request must contains a query.",
                                          INVALID_PARAMETER_VALUE, "Query");
        }
        
        // we get the element set type (BRIEF, SUMMARY OR FULL) or the custom elementName
        ElementSetName setName  = query.getElementSetName();
        ElementSet set          = ElementSetType.SUMMARY;
        List<QName> elementName = query.getElementName();
        if (setName != null) {
            set = setName.getValue();
        } else if (elementName != null && elementName.size() != 0){
            set = null;
        }

        SearchResultsType searchResults = null;
        
        //we get the maxRecords wanted and start position
        Integer maxRecord = request.getMaxRecords();
        Integer startPos  = request.getStartPosition();
        if (startPos <= 0) {
            throw new CstlServiceException("The start position must be > 0.",
                                          NO_APPLICABLE_CODE, "startPosition");
        }

        List<String> results;
        if (outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0") || outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5")) {
           
            // build the sql query from the specified filter
           SQLQuery sqlQuery = (SQLQuery) sqlFilterParser.getQuery(query.getConstraint(), variables, prefixs); 
           
           // TODO sort not yet implemented
           logger.info("ebrim SQL query obtained:" + sqlQuery);
           
           // we try to execute the query
           results = MDReader.executeEbrimSQLQuery(sqlQuery.getQuery());
            
        } else {
            
            // build the lucene query from the specified filter
            SpatialQuery luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(query.getConstraint(), variables, prefixs);
        
            //we look for a sorting request (for now only one sort is used)
            SortByType sortBy = query.getSortBy();
            if (sortBy != null && sortBy.getSortProperty().size() > 0) {
                SortPropertyType first = sortBy.getSortProperty().get(0);
                if (first.getPropertyName() == null || first.getPropertyName().getPropertyName() == null || first.getPropertyName().getPropertyName().equals(""))
                    throw new CstlServiceException("A SortBy filter must specify a propertyName.",
                                                  NO_APPLICABLE_CODE);
                String propertyName = Util.removePrefix(first.getPropertyName().getPropertyName()) + "_sort";
            
                Sort sortFilter;
                if (first.getSortOrder().equals(SortOrder.ASCENDING)) {
                    sortFilter = new Sort(propertyName, false);
                    logger.info("sort ASC");
                } else {
                    sortFilter = new Sort(propertyName, true);
                    logger.info("sort DSC");
                }
                luceneQuery.setSort(sortFilter);
            }
        
            logger.info("Lucene query obtained:" + luceneQuery);
            // we try to execute the query
            results = executeLuceneQuery(luceneQuery);
        }
        
        //we look for distributed queries
        DistributedResults distributedResults = new DistributedResults();
        if (catalogueHarvester != null) {
            if (request.getDistributedSearch() != null) {
                int distributedStartPosition;
                int distributedMaxRecord;
                if (startPos > results.size()) {
                    distributedStartPosition = startPos - results.size();
                    distributedMaxRecord     = maxRecord;
                } else {
                    distributedStartPosition = 1;
                    distributedMaxRecord     = maxRecord - results.size();
                }
                distributedResults = catalogueHarvester.transferGetRecordsRequest(request, cascadedCSWservers, distributedStartPosition, distributedMaxRecord);
            }
        }
        
        int nextRecord   = startPos + maxRecord;
        int totalMatched = results.size() + distributedResults.nbMatched;
        
        if (nextRecord > totalMatched)
            nextRecord = 0;
        
        int maxDistributed = distributedResults.additionalResults.size();
        int max = (startPos - 1) + maxRecord;
        
        if (max > results.size()) {
            max = results.size();
        }
        logger.info("local max = " + max + " distributed max = " + maxDistributed);

        int mode;
        if (outputSchema.equals("http://www.isotc211.org/2005/gmd") || outputSchema.equals("http://www.isotc211.org/2005/gfc")) {
            mode = ISO_19115;
        } else if (outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0") || outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5")) {
            mode = EBRIM;
        } else if (outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2")) {
            mode = DUBLINCORE;
        } else {
            throw new IllegalArgumentException("undefined outputSchema");
        }

        // we return only the number of result matching
        if (resultType.equals(ResultType.HITS)) {
            searchResults = new SearchResultsType(ID, (ElementSetType)set, results.size(), nextRecord);

        // we return a list of Record
        } else if (resultType.equals(ResultType.RESULTS)) {

            List<AbstractRecordType> Arecords = new ArrayList<AbstractRecordType>();
            List<Object> records              = new ArrayList<Object>();

            for (int i = startPos -1; i < max; i++) {
                Object obj = MDReader.getMetadata(results.get(i), mode, set, elementName);
                if (obj == null && (max + 1) < results.size()) {
                    max++;

                } else {
                    if (mode == DUBLINCORE)
                        Arecords.add((AbstractRecordType)obj);
                    else
                        records.add(obj);
                }
            }

            //we add additional distributed result
            for (int i = 0; i < maxDistributed; i++) {

                Object additionalResult = distributedResults.additionalResults.get(i);
                if (mode == DUBLINCORE) {
                    Arecords.add((AbstractRecordType) additionalResult);
                } else {
                    records.add(additionalResult);
                }
            }

            if (mode == DUBLINCORE) {
                searchResults = new SearchResultsType(ID,
                                                      (ElementSetType)set,
                                                      totalMatched,
                                                      Arecords,
                                                      Arecords.size(),
                                                      nextRecord);
            } else {
                searchResults = new SearchResultsType(ID,
                                                      (ElementSetType) set,
                                                      totalMatched,
                                                      records.size(),
                                                      records,
                                                      nextRecord);
            }

            //we return an Acknowledgement if the request is valid.
        } else if (resultType.equals(ResultType.VALIDATE)) {
            try {
                EchoedRequestType echoRequest = new EchoedRequestType(request);
                return new AcknowledgementType(ID, echoRequest, System.currentTimeMillis());

            } catch(DatatypeConfigurationException ex) {
                throw new CstlServiceException("DataTypeConfiguration exception while creating acknowledgment response",
                                               NO_APPLICABLE_CODE);
            }
        }
        
        response = new GetRecordsResponseType(ID, System.currentTimeMillis(), request.getVersion(), searchResults);
        logger.info("GetRecords request processed in " + (System.currentTimeMillis() - startTime) + " ms");
        return response;
    }
    
    /**
     * Execute a Lucene spatial query and return the result as a List of form identifier (form_ID:CatalogCode)
     * 
     * @param query
     * @return
     * @throws CstlServiceException
     */
    private List<String> executeLuceneQuery(final SpatialQuery query) throws CstlServiceException {
        try {
            return indexSearcher.doSearch(query);
        
        } catch (SearchingException ex) {
            throw new CstlServiceException("The service has throw an exception while making identifier lucene request",
                                             NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * Execute a Lucene spatial query and return the result as a database identifier.
     * 
     * @param query
     * @return
     * @throws CstlServiceException
     */
    private String executeIdentifierQuery(final String id) throws CstlServiceException {
        try {
            return indexSearcher.identifierQuery(id);
        
        } catch (SearchingException ex) {
            throw new CstlServiceException("The service has throw an exception while making identifier lucene request",
                                          NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * web service operation return one or more records specified by there identifier.
     * 
     * @param request
     * @return
     */
    public GetRecordByIdResponse getRecordById(final GetRecordById request) throws CstlServiceException {
        logger.info("GetRecordById request processing" + '\n');
        long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        
        // we initialize the output format of the response
        initializeOutputFormat(request);
        
        
        // we get the level of the record to return (Brief, summary, full)
        ElementSet set = ElementSetType.SUMMARY;
        if (request.getElementSetName() != null && request.getElementSetName().getValue() != null) {
            set = request.getElementSetName().getValue();
        }
        
        //we get the output schema and verify that we handle it
        String outputSchema = "http://www.opengis.net/cat/csw/2.0.2";
        if (request.getOutputSchema() != null) {
            outputSchema = request.getOutputSchema();
            if (!ACCEPTED_RESOURCE_TYPE.contains(outputSchema)) {
                throw new CstlServiceException("The server does not support this output schema: " + outputSchema,
                                                  INVALID_PARAMETER_VALUE, "outputSchema");
            }
        }
        
        if (request.getId().size() == 0)
            throw new CstlServiceException("You must specify at least one identifier",
                                          MISSING_PARAMETER_VALUE, "id");
        
        //we begin to build the result
        GetRecordByIdResponseType response;
        List<String> unexistingID = new ArrayList<String>();
        
        //we build dublin core object
        if (outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2")) {
            List<AbstractRecordType> records = new ArrayList<AbstractRecordType>(); 
            for (String id:request.getId()) {
                
                //we verify if  the identifier of the metadata exist
                String saved = id;
                id = executeIdentifierQuery(id);
                if (id == null){
                    unexistingID.add(saved);
                    logger.severe("unexisting metadata id: " + saved);
                    continue;
                }
                //we get the metadata object
                Object o = MDReader.getMetadata(id, DUBLINCORE, set, null);
                if (o instanceof AbstractRecordType)
                    records.add((AbstractRecordType)o);
            }
            if (records.size() == 0) {
                throwUnexistingIdentifierException(unexistingID);
            }
            response = new GetRecordByIdResponseType(records, null);
            
        //we build ISO 19139 object    
        } else if (outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
           List<DefaultMetaData> records = new ArrayList<DefaultMetaData>();
           for (String id:request.getId()) {
               
               //we get the form ID and catalog code
               String saved = id;
                id = executeIdentifierQuery(id);
                if (id == null) {
                    unexistingID.add(saved);
                    logger.severe("unexisting metadata id:" + saved);
                    continue;
                }
                
                //we get the metadata object
                Object o = MDReader.getMetadata(id, ISO_19115, set, null);
                if (o instanceof DefaultMetaData) {
                    records.add((DefaultMetaData)o);
                } else {
                    logger.severe("the form " + id + " is not a ISO object");
                }
           }
           if (records.size() == 0) {
                throwUnexistingIdentifierException(unexistingID);
            }
        
           response = new GetRecordByIdResponseType(null, records);      
        
        //we build a Feature catalogue object
        } else if (outputSchema.equals("http://www.isotc211.org/2005/gfc")) {
           List<Object> records = new ArrayList<Object>();
           for (String id:request.getId()) {
               
               //we get the form ID and catalog code
               String saved = id;
                id = executeIdentifierQuery(id);
                if (id == null) {
                    unexistingID.add(saved);
                    logger.severe("unexisting id:" + saved);
                    continue;
                }
                
                //we get the metadata object 
                Object o = MDReader.getMetadata(id, ISO_19115, set, null);
                if (o != null) {
                    records.add(o);
                } else {
                    logger.severe("GFC object is null");
                }
           }
           if (records.size() == 0) {
                throwUnexistingIdentifierException(unexistingID);
            }
        
           response = new GetRecordByIdResponseType(null, records);      
        
        //we build a Ebrim 3.0 object
        } else if (outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0")) {
           List<Object> records = new ArrayList<Object>();
           for (String id:request.getId()) {
               
               //we get the form ID and catalog code
               String saved = id;
                id = executeIdentifierQuery(id);
                if (id == null) {
                    unexistingID.add(saved);
                    logger.severe("unexisting metadata id: " + saved);
                    continue;
                }
                
                //we get the metadata object 
                Object o = MDReader.getMetadata(id, EBRIM, set, null);
                if (o instanceof IdentifiableType) {
                    records.add(o);
                } else {
                    logger.severe("The form " + id + " is not a EBRIM v3.0 object");
                }
           }
           if (records.size() == 0) {
                throwUnexistingIdentifierException(unexistingID);
            }
        
           response = new GetRecordByIdResponseType(null, records);      
      
         //we build a Ebrim 2.5 object
        } else if (outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5")) {
           List<Object> records = new ArrayList<Object>();
           for (String id:request.getId()) {
               
               //we get the form ID and catalog code
               String saved = id;
                id = executeIdentifierQuery(id);
                if (id == null) {
                    unexistingID.add(saved);
                    logger.severe("unexisting id:" + saved);
                    continue;
                }
                
                //we get the metadata object 
                Object o = MDReader.getMetadata(id, EBRIM, set, null);
                if (o instanceof org.geotoolkit.ebrim.xml.v250.RegistryObjectType) {
                    records.add(o);
                } else {
                    if (o == null)
                        logger.severe("The form " + id + " has not be read is null.");
                    else
                        logger.severe("The form " + id + " is not a EBRIM v2.5 object.");
                }
           }
           if (records.size() == 0) {
                throwUnexistingIdentifierException(unexistingID);
            }
        
           response = new GetRecordByIdResponseType(null, records);  
           
        // this case must never append
        } else {
            response = null;
        }
        
        logger.info("GetRecordById request processed in " + (System.currentTimeMillis() - startTime) + " ms");        
        return response;
    }
    
    /**
     * Launch a service exception with th specified list of unexisting ID.
     *  
     * @param unexistingID
     * @throws CstlServiceException
     */
    private void throwUnexistingIdentifierException(final List<String> unexistingID) throws CstlServiceException {
        String identifiers = "";
        for (String s : unexistingID) {
            identifiers = identifiers + s + ',';
        }
        if (identifiers.lastIndexOf(',') != -1) {
            identifiers.substring(0, identifiers.length() - 1);
        }
        if (identifiers.equals("")) {
            throw new CstlServiceException("The record does not correspound to the specified outputSchema.",
                                             INVALID_PARAMETER_VALUE, "outputSchema");
        } else {

            throw new CstlServiceException("The identifiers " + identifiers + " does not exist",
                                             INVALID_PARAMETER_VALUE, "id");
        }
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public DescribeRecordResponseType describeRecord(final DescribeRecord request) throws CstlServiceException{
        logger.info("DescribeRecords request processing" + '\n');
        long startTime = System.currentTimeMillis();
        DescribeRecordResponseType response;
        try {
            
            verifyBaseRequest(request);
            
            // we initialize the output format of the response
            initializeOutputFormat(request);
        
            // we initialize the type names
            List<QName> typeNames = (List<QName>)request.getTypeName();
            if (typeNames == null || typeNames.size() == 0) {
                typeNames = SUPPORTED_TYPE_NAME;
            }
            
            // we initialize the schema language
            String schemaLanguage = request.getSchemaLanguage(); 
            if (schemaLanguage == null) {
                schemaLanguage = "http://www.w3.org/XML/Schema";
            
            } else if (!schemaLanguage.equals("http://www.w3.org/XML/Schema") && !schemaLanguage.equalsIgnoreCase("XMLSCHEMA")){
               
                throw new CstlServiceException("The server does not support this schema language: " + schemaLanguage + '\n' +
                                              " supported ones are: XMLSCHEMA or http://www.w3.org/XML/Schema",
                                              INVALID_PARAMETER_VALUE, "schemaLanguage"); 
            }
            
            List<SchemaComponentType> components = new ArrayList<SchemaComponentType>();
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = documentFactory.newDocumentBuilder();

            if (typeNames.contains(_Record_QNAME)) {

                InputStream in = Util.getResourceAsStream("org/constellation/metadata/record.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("http://www.opengis.net/cat/csw/2.0.2", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
            
            if (typeNames.contains(_Metadata_QNAME)) {

                InputStream in = Util.getResourceAsStream("org/constellation/metadata/metadata.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("http://www.isotc211.org/2005/gmd", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
            
            if (containsOneOfEbrim30(typeNames)) {
                InputStream in = Util.getResourceAsStream("org/constellation/metadata/ebrim-3.0.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
            
            if (containsOneOfEbrim25(typeNames)) {
                InputStream in = Util.getResourceAsStream("org/constellation/metadata/ebrim-2.5.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
                
                
            response  = new DescribeRecordResponseType(components);
            
        } catch (ParserConfigurationException ex) {
            throw new CstlServiceException("Parser Configuration Exception while creating the DocumentBuilder",
                                          NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("IO Exception when trying to access xsd file",
                                          NO_APPLICABLE_CODE);
        } catch (SAXException ex) {
            throw new CstlServiceException("SAX Exception when trying to parse xsd file",
                                          NO_APPLICABLE_CODE);
        }
        logger.info("DescribeRecords request processed in " + (System.currentTimeMillis() - startTime) + " ms");   
        return response;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetDomainResponse getDomain(final GetDomain request) throws CstlServiceException{
        logger.info("GetDomain request processing" + '\n');
        long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        // we prepare the response
        List<DomainValues> responseList = new ArrayList<DomainValues>();
        
        String parameterName = request.getParameterName();
        String propertyName  = request.getPropertyName();
        
        // if the two parameter have been filled we launch an exception
        if (parameterName != null && propertyName != null) {
            throw new CstlServiceException("One of propertyName or parameterName must be null",
                                             INVALID_PARAMETER_VALUE, "parameterName");
        }
        
        /*
         * "parameterName" return metadata about the service itself.
         */ 
        if (parameterName != null) {
            final StringTokenizer tokens = new StringTokenizer(parameterName, ",");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                int pointLocation = token.indexOf('.');
                if (pointLocation != -1) {

                    if (skeletonCapabilities == null)
                        throw new CstlServiceException("The service was unable to find the capabilities skeleton", NO_APPLICABLE_CODE);

                    String operationName = token.substring(0, pointLocation);
                    String parameter     = token.substring(pointLocation + 1);
                    Operation o  = skeletonCapabilities.getOperationsMetadata().getOperation(operationName);
                    if (o != null) {
                        DomainType param        = o.getParameter(parameter);
                        QName type;
                        if (operationName.equals("GetCapabilities")) {
                            type = _Capabilities_QNAME;
                        } else {
                            type = _Record_QNAME;
                        }
                        if (param != null) {
                            DomainValues value = CswXmlFactory.getDomainValues(actingVersion.toString(), token, null, param.getValue(), type);
                            responseList.add(value);
                        } else {
                            throw new CstlServiceException("The parameter " + parameter + " in the operation " + operationName + " does not exist",
                                                          INVALID_PARAMETER_VALUE, "parameterName");
                        }
                    } else {
                        throw new CstlServiceException("The operation " + operationName + " does not exist",
                                                      INVALID_PARAMETER_VALUE, "parameterName");
                    }
                } else {
                    throw new CstlServiceException("ParameterName must be formed like this Operation.parameterName",
                                                     INVALID_PARAMETER_VALUE, "parameterName");
                }
            }
        
        /*
         * "PropertyName" return a list of metadata for a specific field.
         */  
        } else if (propertyName != null) {
            responseList = MDReader.getFieldDomainofValues(propertyName);
            
        // if no parameter have been filled we launch an exception    
        } else {
            throw new CstlServiceException("One of propertyName or parameterName must be filled",
                                          MISSING_PARAMETER_VALUE, "parameterName, propertyName");
        }
        logger.info("GetDomain request processed in " + (System.currentTimeMillis() - startTime) + " ms");

        return CswXmlFactory.getDomainResponse(actingVersion.toString(), responseList);
    }
    
    /**
     * A web service method alowing to Insert / update / delete record from the CSW.
     * 
     * @param request
     * @return
     */
    public TransactionResponseType transaction(final Transaction request) throws CstlServiceException {
        logger.info("Transaction request processing" + '\n');
        
        if (profile == DISCOVERY) {
            throw new CstlServiceException("This method is not supported by this mode of CSW",
                                          OPERATION_NOT_SUPPORTED, "Request");
        }
        
        long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        // we prepare the report
        int totalInserted = 0;
        int totalUpdated  = 0;
        int totalDeleted  = 0;
        String requestID  = request.getRequestId();
        
        List<Object> transactions = request.getInsertOrUpdateOrDelete();
        for (Object transaction: transactions) {
            if (transaction instanceof InsertType) {
                InsertType insertRequest = (InsertType)transaction;
                
                for (Object record : insertRequest.getAny()) {

                    try {
                        MDWriter.storeMetadata(record);
                        totalInserted++;

                    } catch (IllegalArgumentException e) {
                        logger.severe("already that title.");
                        totalUpdated++;
                    }
                }

            } else if (transaction instanceof DeleteType) {
                if (MDWriter.deleteSupported()) {
                    DeleteType deleteRequest = (DeleteType)transaction;
                    String dataType = deleteRequest.getTypeName();
                    if (deleteRequest.getConstraint() == null) {
                        throw new CstlServiceException("A constraint must be specified.",
                                                      MISSING_PARAMETER_VALUE, "constraint");
                    }

                    // build the lucene query from the specified filter
                    SpatialQuery luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(deleteRequest.getConstraint(), null, null);
                    logger.info("Lucene query obtained:" + luceneQuery);

                    // we try to execute the query
                    List<String> results = executeLuceneQuery(luceneQuery);

                    for (String metadataID : results) {
                       boolean deleted = MDWriter.deleteMetadata(metadataID);
                       if (!deleted) {
                           throw new CstlServiceException("The service does not succeed to delete the metadata:" + metadataID,
                                                  NO_APPLICABLE_CODE);
                       } else {
                           totalDeleted++;
                       }
                    }
                } else {
                    throw new CstlServiceException("This kind of transaction (delete) is not supported by this Writer implementation.",
                                                  NO_APPLICABLE_CODE, "TransactionType");
                }
                
                
            } else if (transaction instanceof UpdateType) {
                if (MDWriter.updateSupported()) {
                    UpdateType updateRequest = (UpdateType) transaction;
                    if (updateRequest.getConstraint() == null) {
                        throw new CstlServiceException("A constraint must be specified.",
                                MISSING_PARAMETER_VALUE, "constraint");
                    }
                    if (updateRequest.getAny() == null && updateRequest.getRecordProperty().size() == 0) {
                        throw new CstlServiceException("The any part or a list od RecordProperty must be specified.",
                                MISSING_PARAMETER_VALUE, "MD_Metadata");
                    } else if (updateRequest.getAny() != null && updateRequest.getRecordProperty().size() != 0) {
                        throw new CstlServiceException("You must choose between the any part or a list of RecordProperty, you can't specify both.",
                                MISSING_PARAMETER_VALUE, "MD_Metadata");
                    }

                    // build the lucene query from the specified filter
                    SpatialQuery luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(updateRequest.getConstraint(), null, null);
                    logger.info("Lucene query obtained:" + luceneQuery);

                    // we try to execute the query
                    List<String> results = executeLuceneQuery(luceneQuery);
                    for (String metadataID : results) {
                        boolean updated;
                        if (updateRequest.getAny() != null) {
                            updated = MDWriter.replaceMetadata(metadataID, updateRequest.getAny());
                        } else {
                            updated = MDWriter.updateMetadata(metadataID, updateRequest.getRecordProperty());
                        }
                        if (!updated) {
                            throw new CstlServiceException("The service does not succeed to update the metadata:" + metadataID,
                                    NO_APPLICABLE_CODE);
                        } else {
                            MDReader.removeFromCache(metadataID);
                            totalUpdated++;
                        }
                    }
                } else {
                    throw new CstlServiceException("This kind of transaction (update) is not supported by this Writer implementation.",
                            NO_APPLICABLE_CODE, "TransactionType");
                }
            } else {
                String className = " null object";
                if (transaction != null) {
                    className = transaction.getClass().getName();
                }
                throw new CstlServiceException("This kind of transaction is not supported by the service: " + className,
                                              INVALID_PARAMETER_VALUE, "TransactionType");
            }
            
        }
        if (totalDeleted > 0 || totalInserted > 0 || totalUpdated > 0) {
            try {
                indexSearcher.refresh();
            } catch (IndexingException ex) {
                throw new CstlServiceException("The service does not succeed to refresh the index after deleting documents:" + ex.getMessage(),
                        NO_APPLICABLE_CODE);
            }
        }
        TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                    totalUpdated,
                                                                    totalDeleted,
                                                                    requestID); 
        TransactionResponseType response = new TransactionResponseType(summary, null, request.getVersion());
        logger.info("Transaction request processed in " + (System.currentTimeMillis() - startTime) + " ms");   
        return response;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public HarvestResponseType harvest(final Harvest request) throws CstlServiceException {
        logger.info("Harvest request processing" + '\n');
        if (profile == DISCOVERY) {
            throw new CstlServiceException("This method is not supported by this mode of CSW",
                                          OPERATION_NOT_SUPPORTED, "Request");
        }
        verifyBaseRequest(request);
        HarvestResponseType response;
        // we prepare the report
        int totalInserted = 0;
        int totalUpdated  = 0;
        int totalDeleted  = 0;
        
        //we verify the resource Type
        String resourceType = request.getResourceType();
        if (resourceType == null) {
            throw new CstlServiceException("The resource type to harvest must be specified",
                                          MISSING_PARAMETER_VALUE, "resourceType");
        } else {
            if (!ACCEPTED_RESOURCE_TYPE.contains(resourceType)) {
                throw new CstlServiceException("This resource type is not allowed. ",
                                             MISSING_PARAMETER_VALUE, "resourceType");
            }
        }
        String sourceURL = request.getSource();
        if (sourceURL != null) {
            try {

                // TODO find a better to determine if the source is single or catalogue
                int mode = 1;
                if (sourceURL.endsWith("xml")) {
                    mode = 0;
                }

                //mode synchronous
                if (request.getResponseHandler().size() == 0) {

                    // if the resource is a simple record
                    if (mode == 0) {
                        int[] results = catalogueHarvester.harvestSingle(sourceURL, resourceType);
                        totalInserted = results[0];
                        totalUpdated  = results[1];
                        totalDeleted  = 0;

                    // if the resource is another CSW service we get all the data of this catalogue.
                    } else {
                        int[] results = catalogueHarvester.harvestCatalogue(sourceURL);
                        totalInserted = results[0];
                        totalUpdated  = results[1];
                        totalDeleted  = results[2];
                    }
                    TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                                totalUpdated,
                                                                                totalDeleted,
                                                                                null);
                    TransactionResponseType transactionResponse = new TransactionResponseType(summary, null, request.getVersion());
                    response = new HarvestResponseType(transactionResponse);

                //mode asynchronous
                } else {

                    AcknowledgementType acknowledgement = new AcknowledgementType(null, new EchoedRequestType(request), System.currentTimeMillis());
                    response = new HarvestResponseType(acknowledgement);
                    long period = 0;
                    if (request.getHarvestInterval() != null) {
                        period = request.getHarvestInterval().getTimeInMillis(new Date(System.currentTimeMillis()));
                    }

                    Timer t = new Timer();
                    TimerTask harvestTask = new AsynchronousHarvestTask(catalogueHarvester, sourceURL, resourceType, mode, request.getResponseHandler());
                    //we launch only once the harvest
                    if (period == 0) {
                        t.schedule(harvestTask, 1000);

                    //we launch the harvest periodically
                    } else {
                        t.scheduleAtFixedRate(harvestTask, 1000, period);
                        schreduledTask.add(t);
                        saveSchreduledHarvestTask(sourceURL, resourceType, mode, request.getResponseHandler(), period);
                    }
                    
                }
                
            } catch (SQLException ex) {
                throw new CstlServiceException("The service has throw an SQLException: " + ex.getMessage(),
                                              NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("The resource can not be parsed: " + ex.getMessage(),
                                              INVALID_PARAMETER_VALUE, "Source");
            } catch (MalformedURLException ex) {
                throw new CstlServiceException("The source URL is malformed",
                                              INVALID_PARAMETER_VALUE, "Source");
            } catch (IOException ex) {
                throw new CstlServiceException("The service can't open the connection to the source",
                                              INVALID_PARAMETER_VALUE, "Source");
            } catch (DatatypeConfigurationException ex) {
                throw new CstlServiceException("The service has made an error of timestamp",
                                              INVALID_PARAMETER_VALUE);
            }
            
        } else {
            throw new CstlServiceException("you must specify a source",
                                              MISSING_PARAMETER_VALUE, "Source");
        }
        
        logger.info("Harvest operation finished");
        return response;
    }
    
    
    
    /**
     * Return the current output format (default: application/xml)
     */
    public String getOutputFormat() {
        if (outputFormat == null) {
            return "application/xml";
        }
        return outputFormat;
    }
    
    /**
     * Return true if the MIME type is supported.
     * 
     * @param format a MIME type represented by a String.
     */
    private boolean isSupportedFormat(final String format) {
        return ACCEPTED_OUTPUT_FORMATS.contains(format);
    }
    
    /**
     * Set the capabilities document.
     * 
     * @param skeletonCapabilities An OWS 1.0.0 capabilities object.
     */
    public void setSkeletonCapabilities(final Capabilities skeletonCapabilities) {
        this.skeletonCapabilities = skeletonCapabilities;
    }
    
    /**
     * Set the current service URL
     */
    public void setServiceURL(final String serviceURL){
        this.serviceURL = serviceURL;
    }
    
    /**
     * Verify that the bases request attributes are correct.
     * 
     * @param request an object request with the base attribute (all except GetCapabilities request); 
     */ 
    private void verifyBaseRequest(final RequestBase request) throws CstlServiceException {
        isWorking();
        if (request != null) {
            if (request.getService() != null) {
                if (!request.getService().equals("CSW"))  {
                    throw new CstlServiceException("service must be \"CSW\"!",
                                                  INVALID_PARAMETER_VALUE, "service");
                }
            } else {
                throw new CstlServiceException("service must be specified!",
                                              MISSING_PARAMETER_VALUE, "service");
            }
            if (request.getVersion()!= null) {
                /*
                 * Ugly patch to begin to support CSW 2.0.0 request
                 *
                 * TODO remove this
                 */
                if (request.getVersion().equals("2.0.2")) {
                    this.actingVersion = new ServiceVersion(ServiceType.CSW, "2.0.2");
                } else if (request.getVersion().equals("2.0.0") && (request instanceof GetDomain)) {
                    this.actingVersion = new ServiceVersion(ServiceType.CSW, "2.0.0");

                } else {
                    throw new CstlServiceException("version must be \"2.0.2\"!", VERSION_NEGOTIATION_FAILED, "version");
                }
            } else {
                throw new CstlServiceException("version must be specified!", MISSING_PARAMETER_VALUE, "version");
            }
         } else { 
            throw new CstlServiceException("The request is null!", NO_APPLICABLE_CODE);
         }  
        
    }
    
    /**
     * Return a string list of the supported TypeName
     */
    private String supportedTypeNames() {
        StringBuilder result = new StringBuilder();
        for (QName qn: SUPPORTED_TYPE_NAME) {
            result.append(qn.getPrefix()).append(qn.getLocalPart()).append('\n');
        }
        return result.toString();
    }
    
    /**
     * Initialize the outputFormat (MIME type) of the response.
     * if the format is not supported it throws a WebService Exception.
     * 
     * @param request
     * @throws org.constellation.ws.CstlServiceException
     */
    private void initializeOutputFormat(final AbstractCswRequest request) throws CstlServiceException {
        
        // we initialize the output format of the response
        String format = request.getOutputFormat();
        if (format != null && isSupportedFormat(format)) {
            outputFormat = format;
        } else if (format != null && !isSupportedFormat(format)) {
            String supportedFormat = "";
            for (String s: ACCEPTED_OUTPUT_FORMATS) {
                supportedFormat = supportedFormat  + s + '\n';
            } 
            throw new CstlServiceException("The server does not support this output format: " + format + '\n' +
                                             " supported ones are: " + '\n' + supportedFormat,
                                             INVALID_PARAMETER_VALUE, "outputFormat");
        }
    }

    /**
     * Redirect the logs into the specified folder.
     * if the parameter ID is null or empty it create a file named "cstl-csw.log"
     * else the file is named "ID-cstl-csw.log"
     *
     * @param ID The ID of the service in a case of multiple sos server.
     * @param filePath The path to the log folder.
     */
    private void initLogger(String ID, String filePath) {
        try {
            if (ID != null && !ID.equals("")) {
                ID = ID + '-';
            }
            FileHandler handler  = new FileHandler(filePath + '/'+ ID + "cstl-csw.log");
            handler.setFormatter(new MonolineFormatter(handler));
            logger.addHandler(handler);
        } catch (IOException ex) {
            logger.severe("IO exception while trying to separate CSW Logs:" + ex.getMessage());
        } catch (SecurityException ex) {
            logger.severe("Security exception while trying to separate CSW Logs" + ex.getMessage());
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
     * Destroy all the resource and close the connection when the web application is undeployed.
     */
    public void destroy() {
        if (MDReader != null) {
            MDReader.destroy();
        }
        if (MDWriter != null) {
            MDWriter.destroy();
        }
        if (indexSearcher != null) {
            indexSearcher.destroy();
        }
        for (Timer t : schreduledTask) {
            t.cancel();
        }
        if (catalogueHarvester != null) {
            catalogueHarvester.destroy();
        }
    }

     /**
     * A task launching an harvest periodically.
     */
    class AsynchronousHarvestTask extends TimerTask {

        /**
         * The catalogue harvester.
         */
        private CatalogueHarvester harvester;

        /**
         * The harvest mode SINGLE(0) or CATALOGUE(1)
         */
        private int mode;

        /**
         * The URL of the data source.
         */
        private String sourceURL;

        /**
         * The type of the resource (for single mode).
         */
        private String resourceType;

        /**
         * A list of email addresses.
         */
        private String[] emails;

        /**
         * Build a new Timer which will Harvest the source periodically.
         *
         */
        public AsynchronousHarvestTask(CatalogueHarvester harvester, String sourceURL, String resourceType, int mode, List<String> emails) {
            this.harvester    = harvester;
            this.sourceURL    = sourceURL;
            this.mode         = mode;
            this.resourceType = resourceType;
            if (emails != null) {
                this.emails = emails.toArray(new String[emails.size()]);
            } else {
                this.emails = new String[0];
            }
        }

        /**
         * This method is launch when the timer expire.
         */
        @Override
        public void run() {
            logger.info("launching harvest on:" + sourceURL);
            try {
                int[] results;
                if (mode == 0) {
                    results = catalogueHarvester.harvestSingle(sourceURL, resourceType);
                } else {
                    results = catalogueHarvester.harvestCatalogue(sourceURL);
                }

                //TODO does we have to send a HarvestResponseType or a custom report to the mails addresses?
                TransactionSummaryType summary = new TransactionSummaryType(results[0],
                                                                            results[1],
                                                                            results[2], null);
                 TransactionResponseType transactionResponse = new TransactionResponseType(summary, null, "2.0.2");
                 HarvestResponseType response = new HarvestResponseType(transactionResponse);

                 StringBuilder report = new StringBuilder("Harvest report:\n");
                 report.append("From: ").append(sourceURL).append('\n');
                 report.append("Inserted: ").append(results[0]).append('\n');
                 report.append("Updated: ").append(results[1]).append('\n');
                 report.append("Deleted: ").append(results[2]).append('\n');
                 report.append("at ").append(new Date(System.currentTimeMillis()));

                 MailSendingUtilities.mail(emails, "Harvest report", report.toString());
            } catch (SQLException ex) {
                logger.severe("The service has throw an SQLException: " + ex.getMessage());
            } catch (JAXBException ex) {
                logger.severe("The resource can not be parsed: " + ex.getMessage());
            } catch (MalformedURLException ex) {
                logger.severe("The source URL is malformed");
            } catch (IOException ex) {
                logger.severe("The service can't open the connection to the source");
            } catch (CstlServiceException ex) {
                logger.severe("Constellation exception:" + ex.getMessage());
            } catch (MessagingException ex) {
                logger.severe("MessagingException exception:" + ex.getMessage());
            } catch (NamingException ex) {
                logger.severe("Naming exception:" + ex.getMessage());
            }
        }
    }
}
