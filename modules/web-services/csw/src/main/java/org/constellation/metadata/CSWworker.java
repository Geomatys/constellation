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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

// Apache Lucene dependencies
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

//Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.filter.FilterParser;
import org.constellation.filter.LuceneFilterParser;
import org.constellation.filter.SQLFilterParser;
import org.constellation.filter.SQLQuery;
import org.constellation.generic.database.Automatic;
import org.constellation.jaxb.AnchoredMarshallerPool;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;

import static org.constellation.metadata.io.AbstractMetadataReader.*;
import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.CSWConstants.*;

//geotoolkit dependencies
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.inspire.xml.InspireCapabilitiesType;
import org.geotoolkit.inspire.xml.MultiLingualCapabilities;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.csw.xml.AbstractCswRequest;
import org.geotoolkit.csw.xml.CswXmlFactory;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.ElementSetName;
import org.geotoolkit.csw.xml.GetDomain;
import org.geotoolkit.csw.xml.GetRecordById;
import org.geotoolkit.csw.xml.GetRecordByIdResponse;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.GetCapabilities;
import org.geotoolkit.csw.xml.Harvest;
import org.geotoolkit.csw.xml.Transaction;
import org.geotoolkit.csw.xml.DescribeRecord;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.GetDomainResponse;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.AcknowledgementType;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.DeleteType;
import org.geotoolkit.csw.xml.v202.DescribeRecordResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.HarvestResponseType;
import org.geotoolkit.csw.xml.v202.InsertType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.csw.xml.v202.SearchResultsType;
import org.geotoolkit.csw.xml.v202.TransactionResponseType;
import org.geotoolkit.csw.xml.v202.TransactionSummaryType;
import org.geotoolkit.csw.xml.v202.UpdateType;
import org.geotoolkit.csw.xml.v202.SchemaComponentType;
import org.geotoolkit.csw.xml.v202.EchoedRequestType;
import org.geotoolkit.ebrim.xml.v300.IdentifiableType;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.SearchingException;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.ogc.xml.v110.FilterCapabilities;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.v100.DomainType;
import org.geotoolkit.ows.xml.v100.Operation;
import org.geotoolkit.ows.xml.v100.OperationsMetadata;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.ows.xml.v100.ServiceIdentification;
import org.geotoolkit.ows.xml.v100.ServiceProvider;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.logging.MonolineFormatter;
import org.geotoolkit.xml.Namespaces;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.csw.xml.TypeNames.*;

// GeoAPI dependencies
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.xsd.xml.v2001.XSDMarshallerPool;
import org.opengis.filter.sort.SortOrder;
import org.opengis.util.CodeList;


/**
 * The CSW (Catalog Service Web) engine.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class CSWworker {

    /**
     * use for debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.metadata");
    
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
    private CSWMetadataReader mdReader;
    
    /**
     * An Database Writer.
     */
    private CSWMetadataWriter mdWriter;
    
    /**
     * The current MIME type of return
     */
    private String outputFormat;
    
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
     * A task schreduler for asynchronous harvest.
     */
    private HarvestTaskSchreduler harvestTaskSchreduler;

    /**
     * A list of the supported Type name 
     */
    private List<QName> supportedTypeNames;
    
    /**
     * A map of QName - xsd svhema object
     */
    private final Map<QName, Object> schemas = new HashMap<QName, Object>();

    /**
     * A list of supported MIME type. 
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = new ArrayList<String>();
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.TEXT_XML);
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.APPLICATION_XML);
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.TEXT_HTML);
        ACCEPTED_OUTPUT_FORMATS.add(MimeType.TEXT_PLAIN);
    }
    
    /**
     * A list of supported resource type. 
     */
    private List<String> acceptedResourceType;
    
    /**
     * A list of known CSW server used in distributed search.
     */
    private List<String> cascadedCSWservers;
    
    public static final  int DISCOVERY    = 0;
    public static final int TRANSACTIONAL = 1;
    
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
    private ServiceDef actingVersion = ServiceDef.CSW_2_0_2;

    /**
     * The log level off al the informations log.
     */
    private Level logLevel = Level.INFO;
    
    /**
     * Build a new CSW worker with the specified configuration directory
     *
     * @param serviceID The service identifier (used in multiple CSW context). default value is "".
     * 
     */
    public CSWworker(final String serviceID, File configDir) {

        final String notWorkingMsg = "The CSW service is not working!";
        if (configDir == null) {
            configDir    = getConfigDirectory();
            if (configDir == null) {
                LOGGER.log(Level.WARNING, "{0}\nCause: The configuration directory has not been found", notWorkingMsg);
                isStarted = false;
                return;
            }
        }
        LOGGER.log(Level.FINER, "Path to config directory: {0}", configDir);
        isStarted = true;
        try {
            // we initialize the filterParsers
            final JAXBContext jb                  = JAXBContext.newInstance("org.constellation.generic.database");
            final Unmarshaller configUnmarshaller = jb.createUnmarshaller();
            final File configFile                 = new File(configDir, serviceID + "config.xml");
            if (!configFile.exists()) {
                 LOGGER.log(Level.WARNING, "{0}\nCause: The configuration file has not been found", notWorkingMsg);
                 isStarted = false;
            } else {
                final Automatic configuration = (Automatic) configUnmarshaller.unmarshal(configFile);
                init(configuration, serviceID, configDir);
                initializeSupportedTypeNames();
                initializeAcceptedResourceType();
                initializeRecordSchema();
                initializeAnchorsMap();
                loadCascadedService(configDir);
                String suffix = "";
                if (profile == TRANSACTIONAL) {
                    suffix = "-T";
                }
                LOGGER.info("CSW" + suffix + " service (" + configuration.getFormat() + ") running");
            }
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning(notWorkingMsg + "\nCause: Unable to find a CSW Factory");
            isStarted = false;
        } catch (JAXBException ex) {
            LOGGER.warning(notWorkingMsg + "\nCause: JAXBException while getting configuration:" + ex.getLocalizedMessage());
            isStarted = false;
        } catch (MetadataIoException e) {
            LOGGER.warning(notWorkingMsg + "\nCause:" + e.getMessage());
            isStarted = false;
        } catch (IndexingException e) {
            LOGGER.warning(notWorkingMsg + "\nCause:" + e.getMessage());
            isStarted = false;
        } catch (IllegalArgumentException e) {
            LOGGER.warning(notWorkingMsg + "\nCause: IllegalArgumentException: " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            isStarted = false;
        } catch (CstlServiceException e) {
            LOGGER.warning(notWorkingMsg + "\nCause: CstlServiceException: " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            isStarted = false;
        }
    }

    /**
     * Build a new CSW worker with the specified configuration directory
     *
     * @param serviceID The service identifier (used in multiple CSW context). default value is "".
     *
     */
    public CSWworker(final String serviceID, File configDir, Automatic configuration) {
        final String notWorkingMsg = "The CSW service is not working!";
        isStarted = true;
        try {
            // we initialize the filterParsers
            init(configuration, serviceID, configDir);
            initializeSupportedTypeNames();
            initializeAcceptedResourceType();
            initializeRecordSchema();
            initializeAnchorsMap();
            loadCascadedService(configDir);
            String suffix = "";
            if (profile == TRANSACTIONAL) {
                suffix = "-T";
            }
            LOGGER.info("CSW" + suffix + " service (" + configuration.getFormat() + ") running");
            
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning(notWorkingMsg + "\nCause: Unable to find a CSW Factory");
            isStarted = false;
        } catch (MetadataIoException e) {
            LOGGER.warning(notWorkingMsg + "\nCause:" + e.getMessage());
            isStarted = false;
        } catch (IndexingException e) {
            LOGGER.warning(notWorkingMsg + "\nCause:" + e.getMessage());
            isStarted = false;
        } catch (IllegalArgumentException e) {
            LOGGER.warning(notWorkingMsg + "\nCause: IllegalArgumentException: " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            isStarted = false;
        } catch (CstlServiceException e) {
            LOGGER.warning(notWorkingMsg + "\nCause: CstlServiceException: " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            isStarted = false;
        }  catch (JAXBException e) {
            LOGGER.warning(notWorkingMsg + "\nCause: JAXBException: " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            isStarted = false;
        }
    }

    /**
     * Initialize the readers and indexSearcher to the datasource for the discovery profile.
     * If The transactionnal part is enabled, it also initialize Writer and catalog harvester.
     *
     * @param configuration A configuration object containing the datasource informations
     * @param serviceID The identifier of the instance.
     * @param configDir The direcory containing the configuration files.
     *
     * @throws MetadataIoException If an error occurs while querying the datasource.
     * @throws IndexingException If an error occurs while initialiazing the indexation.
     */
    private void init(final Automatic configuration, String serviceID, File configDir) throws MetadataIoException, IndexingException {

        // we assign the configuration directory
        configuration.setConfigurationDirectory(configDir);

        // we load the factory from the available classes
        final AbstractCSWFactory cswfactory = factory.getServiceProvider(AbstractCSWFactory.class, null, null,null);
        LOGGER.log(Level.FINER, "CSW factory loaded:{0}", cswfactory.getClass().getName());

        final int datasourceType = configuration.getType();
        //we initialize all the data retriever (reader/writer) and index worker
        mdReader                      = cswfactory.getMetadataReader(configuration);
        profile                       = configuration.getProfile();
        final AbstractIndexer indexer = cswfactory.getIndexer(configuration, mdReader, serviceID);
        indexSearcher                 = cswfactory.getIndexSearcher(datasourceType, configDir, serviceID);
        if (profile == TRANSACTIONAL) {
            mdWriter                  = cswfactory.getMetadataWriter(configuration, indexer);
            catalogueHarvester        = cswfactory.getCatalogueHarvester(configuration, mdWriter);
            harvestTaskSchreduler     = new HarvestTaskSchreduler(configDir, catalogueHarvester);
        } else {
            indexer.destroy();
        }
    }
    
    /**
     * Initialize the supported type names in function of the reader capacity.
     */
    private void initializeSupportedTypeNames() {
        supportedTypeNames = new ArrayList<QName>();
        final List<Integer> supportedDataTypes = mdReader.getSupportedDataTypes();
        if (supportedDataTypes.contains(ISO_19115))
            supportedTypeNames.addAll(ISO_TYPE_NAMES);
        if (supportedDataTypes.contains(DUBLINCORE))
            supportedTypeNames.addAll(DC_TYPE_NAMES);
        if (supportedDataTypes.contains(EBRIM)) {
            supportedTypeNames.addAll(EBRIM30_TYPE_NAMES);
            supportedTypeNames.addAll(EBRIM25_TYPE_NAMES);
        }
                    
    }

    /**
     * Initialize the supported outputSchema in function of the reader capacity.
     */
    private void initializeAcceptedResourceType() {
        acceptedResourceType = new ArrayList<String>();
        final List<Integer> supportedDataTypes = mdReader.getSupportedDataTypes();
        if (supportedDataTypes.contains(ISO_19115)) {
            acceptedResourceType.add(Namespaces.GMD);
            acceptedResourceType.add(Namespaces.GFC);
        }
        if (supportedDataTypes.contains(DUBLINCORE)) {
            acceptedResourceType.add(Namespaces.CSW_202);
        }
        if (supportedDataTypes.contains(EBRIM)) {
            acceptedResourceType.add(EBRIM_30);
            acceptedResourceType.add(EBRIM_25);
        }
    }

    /**
     * Load from the resource the XSD schemas used for the reponse of describeRecord.
     * 
     * @throws CstlServiceException if there is a JAXBException while using the unmarshaller.
     */
    private void initializeRecordSchema() throws CstlServiceException {
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = XSDMarshallerPool.getInstance().acquireUnmarshaller();

            schemas.put(RECORD_QNAME,            unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/record.xsd")));
            schemas.put(METADATA_QNAME,          unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/metadata.xsd")));
            schemas.put(EXTRINSIC_OBJECT_QNAME,    unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/ebrim-3.0.xsd")));
            schemas.put(EXTRINSIC_OBJECT_25_QNAME, unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/ebrim-2.5.xsd")));

        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB Exception when trying to parse xsd file", ex, NO_APPLICABLE_CODE);
        } finally {
            if (unmarshaller != null) {
                XSDMarshallerPool.getInstance().release(unmarshaller);
            }
        }
    }

    /**
     * Initialize the Anchors in function of the reader capacity.
     */
    private void initializeAnchorsMap() throws JAXBException {
        if (EBRIMMarshallerPool.getInstance() instanceof AnchoredMarshallerPool) {
            final AnchoredMarshallerPool pool = (AnchoredMarshallerPool) EBRIMMarshallerPool.getInstance();
            final Map<String, URI> concepts = mdReader.getConceptMap();
            int nbWord = 0;
            for (Entry<String, URI> entry: concepts.entrySet()) {
                pool.addAnchor(entry.getKey(),entry.getValue());
                nbWord ++;
            }
            if (nbWord > 0) {
                LOGGER.log(Level.INFO, "{0} words put in pool.", nbWord);
            }
        } else {
            LOGGER.severe("NOT an anchoredMarshaller Pool");
        }
    }
    
    /**
     * Load the federated CSW server from a properties file.
     */
    private void loadCascadedService(final File configDirectory) {
        cascadedCSWservers = new ArrayList<String>();
        try {
            // we get the cascading configuration file
            final File f = new File(configDirectory, "CSWCascading.properties");
            final InputStream in = new FileInputStream(f);
            final Properties cascad = new Properties();
            cascad.load(in);
            in.close();
            final StringBuilder s = new StringBuilder("Cascaded Services:\n");
            for (Object server: cascad.keySet()) {
                final String serverName = (String) server;
                final String servURL = (String)cascad.getProperty(serverName);
                s.append(servURL).append('\n');
                cascadedCSWservers.add(servURL);
            }
            LOGGER.info(s.toString());
            
        } catch (FileNotFoundException e) {
            LOGGER.info("no cascaded CSW server found (optionnal)");
        } catch (IOException e) {
            LOGGER.info("no cascaded CSW server found (optionnal) (IO Exception)");
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
        LOGGER.log(logLevel, "getCapabilities request processing\n");
        final long startTime = System.currentTimeMillis();
        
        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals(CSW)) {
                throw new CstlServiceException("service must be \"CSW\"!", INVALID_PARAMETER_VALUE, SERVICE);
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, SERVICE);
        }
        final AcceptVersions versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains(CSW_202_VERSION)){
                 throw new CstlServiceException("version available : 2.0.2",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }
        /*
         final AcceptFormats formats = requestCapabilities.getAcceptFormats();
        

         if (formats != null && formats.getOutputFormat().size() > 0 && !formats.getOutputFormat().contains(MimeType.TEXT_XML)) {
            
             * Acording to the CITE test this case does not return an exception
             throw new OWSWebServiceException("accepted format : text/xml",
                                             INVALID_PARAMETER_VALUE, "acceptFormats",
                                             version);
             
        }
        */

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
            sections = new SectionsType(ALL);
        }
        
        //according to CITE test a GetCapabilities must always return Filter_Capabilities
        if (!sections.containsSection(FILTER_CAPABILITIES) || sections.containsSection(ALL))
            sections.add(FILTER_CAPABILITIES);
        
        //we enter the information for service identification.
        if (sections.containsSection("ServiceIdentification") || sections.containsSection(ALL)) {
                
            si = skeletonCapabilities.getServiceIdentification();
        }
            
        //we enter the information for service provider.
        if (sections.containsSection("ServiceProvider") || sections.containsSection(ALL)) {
           
            sp = skeletonCapabilities.getServiceProvider();
        }
            
        //we enter the operation Metadata
        if (sections.containsSection("OperationsMetadata") || sections.containsSection(ALL)) {
                
            om = skeletonCapabilities.getOperationsMetadata();
            
            if (om != null) {

                // we remove the operation not supported in this profile (transactional/discovery)
                if (profile == DISCOVERY) {
                    om.removeOperation("Harvest");
                    om.removeOperation("Transaction");
                }
            
                // we update the URL
                om.updateURL(serviceURL, CSW);

                // we add the cascaded services (if there is some)
                final DomainType cascadedCSW  = om.getConstraint("FederatedCatalogues");
                if (cascadedCSW == null) {
                    if (cascadedCSWservers != null && !cascadedCSWservers.isEmpty()) {
                        final DomainType fedCata = new DomainType("FederatedCatalogues", cascadedCSWservers);
                        om.getConstraint().add(fedCata);
                    }
                } else {
                    if (cascadedCSWservers != null && !cascadedCSWservers.isEmpty())
                        cascadedCSW.setValue(cascadedCSWservers);
                    else
                        om.removeConstraint(cascadedCSW);
                }
                
                // we update the operation parameters
                final Operation gr = om.getOperation("GetRecords");
                if (gr != null) {
                    final DomainType os = gr.getParameter(OUTPUT_SCHEMA);
                    if (os != null) {
                        os.setValue(acceptedResourceType);
                    }
                    final DomainType tn = gr.getParameter(TYPENAMES);
                    if (tn != null) {
                        final List<String> values = new ArrayList<String>();
                        for (QName qn : supportedTypeNames) {
                            values.add(Namespaces.getPreferredPrefix(qn.getNamespaceURI(), "") + ':' + qn.getLocalPart());
                        }
                        tn.setValue(values);
                    }
                    
                    //we update the ISO queryable elements :
                    final DomainType isoQueryable = gr.getConstraint("SupportedISOQueryables");
                    if (isoQueryable != null) {
                        final List<String> values = new ArrayList<String>();
                        for (String name : ISO_QUERYABLE.keySet() ) {
                            values.add("apiso:" + name);
                        }
                        isoQueryable.setValue(values);
                    }
                    //we update the DC queryable elements :
                    final DomainType dcQueryable = gr.getConstraint("SupportedDublinCoreQueryables");
                    if (dcQueryable != null) {
                        final List<String> values = new ArrayList<String>();
                        for (String name : DUBLIN_CORE_QUERYABLE.keySet() ) {
                            values.add("dc:" + name);
                        }
                        dcQueryable.setValue(values);
                    }

                    //we update the reader's additional queryable elements :
                    final DomainType additionalQueryable = gr.getConstraint("AdditionalQueryables");
                    if (additionalQueryable != null) {
                        final List<String> values = new ArrayList<String>();
                        for (QName name : mdReader.getAdditionalQueryableQName()) {
                            values.add(name.getPrefix() + ':' + name.getLocalPart());
                        }
                        if (values.size() > 0) {
                            additionalQueryable.setValue(values);
                        }
                    }
                }
                
                final Operation grbi = om.getOperation("GetRecordById");
                if (grbi != null) {
                    final DomainType os = grbi.getParameter(OUTPUT_SCHEMA);
                    if (os != null) {
                        os.setValue(acceptedResourceType);
                    }
                }
                
                final Operation dr = om.getOperation("DescribeRecord");
                if (dr != null) {
                    final DomainType tn = dr.getParameter("TypeName");
                    if (tn != null) {
                        final List<String> values = new ArrayList<String>();
                        for (QName qn : supportedTypeNames) {
                            values.add(Namespaces.getPreferredPrefix(qn.getNamespaceURI(), "") + ':' + qn.getLocalPart());
                        }
                        tn.setValue(values);
                    }
                }

                //we add the INSPIRE extend capabilties
                final InspireCapabilitiesType inspireCapa = new InspireCapabilitiesType(Arrays.asList("FRA", "ENG"));
                final MultiLingualCapabilities m          = new MultiLingualCapabilities();
                m.setMultiLingualCapabilities(inspireCapa);
                om.setExtendedCapabilities(m);
            }
        }
            
        //we enter the information filter capablities.
        if (sections.containsSection(FILTER_CAPABILITIES) || sections.containsSection(ALL)) {
            
            fc = skeletonCapabilities.getFilterCapabilities();
        }
            
            
        c = new Capabilities(si, sp, om, CSW_202_VERSION, null, fc);

        LOGGER.log(logLevel, "GetCapabilities request processed in " + (System.currentTimeMillis() - startTime) + MS);
        return c;
    }
    
    /**
     * Web service operation which permits to search the catalogue to find records.
     * 
     * @param request
     *
     * @return A GetRecordsResponseType containing the result of the request or
     *         an AcknowledgementType if the resultType is set to VALIDATE.
     */
    public Object getRecords(final GetRecordsRequest request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetRecords request processing\n");
        final long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        
        //we prepare the response
        GetRecordsResponseType response;
        
        final String id = request.getRequestId();
        
        // we initialize the output format of the response
        initializeOutputFormat(request);
        
        //we get the output schema and verify that we handle it
        String outputSchema = Namespaces.CSW_202;
        if (request.getOutputSchema() != null) {
            outputSchema = request.getOutputSchema();
            if (!acceptedResourceType.contains(outputSchema)) {
                final StringBuilder supportedOutput = new StringBuilder();
                for (String s: acceptedResourceType) {
                    supportedOutput.append(s).append('\n');
                } 
                throw new CstlServiceException("The server does not support this output schema: " + outputSchema + '\n' +
                                              " supported ones are: " + '\n' + supportedOutput,
                                              INVALID_PARAMETER_VALUE, OUTPUT_SCHEMA);
            }
        }
        
        //We get the resultType
        ResultType resultType = ResultType.HITS;
        if (request.getResultType() != null) {
            resultType = request.getResultType();
        }
        
        //We initialize (and verify) the principal attribute of the query
        QueryType query;
        List<QName> typeNames;
        final Map<String, QName> variables = new HashMap<String, QName>();
        final Map<String, String> prefixs  = new HashMap<String, String>();
        if (request.getAbstractQuery() != null) {
            query = (QueryType)request.getAbstractQuery();
            typeNames =  query.getTypeNames();
            if (typeNames == null || typeNames.isEmpty()) {
                throw new CstlServiceException("The query must specify at least typeName.",
                                              INVALID_PARAMETER_VALUE, TYPENAMES);
            } else {
                for (QName type : typeNames) {
                    if (type != null) {
                        prefixs.put(type.getPrefix(), type.getNamespaceURI());
                        //for ebrim mode the user can put variable after the Qname
                        if (type.getLocalPart().indexOf('_') != -1 && !type.getLocalPart().startsWith("MD")) {
                            final StringTokenizer tokenizer = new StringTokenizer(type.getLocalPart(), "_;");
                            type = new QName(type.getNamespaceURI(), tokenizer.nextToken());
                            while (tokenizer.hasMoreTokens()) {
                                variables.put(tokenizer.nextToken(), type);
                            }
                        }
                    } else {
                        throw new CstlServiceException("The service was unable to read a typeName:" +'\n' +
                                                       "supported one are:" + '\n' + supportedTypeNames(),
                                                       INVALID_PARAMETER_VALUE, TYPENAMES);
                    }
                    //we verify that the typeName is supported        
                    if (!supportedTypeNames.contains(type)) {
                        String typeName = "null";
                        if (type != null)
                            typeName = type.getLocalPart();
                        throw new CstlServiceException("The typeName " + typeName + " is not supported by the service:" +'\n' +
                                                      "supported one are:" + '\n' + supportedTypeNames(),
                                                      INVALID_PARAMETER_VALUE, TYPENAMES);
                    }
                }
                /*
                 * debugging part
                 */
                final StringBuilder report = new StringBuilder("variables:").append('\n');
                for (Entry<String, QName> entry : variables.entrySet()) {
                    report.append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
                }
                report.append("prefixs:").append('\n');
                for (Entry<String, String> entry : prefixs.entrySet()) {
                    report.append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
                }
                LOGGER.log(logLevel, report.toString());
            }
            
        } else {
            throw new CstlServiceException("The request must contains a query.",
                                          INVALID_PARAMETER_VALUE, "Query");
        }
        
        // we get the element set type (BRIEF, SUMMARY OR FULL) or the custom elementName
        final ElementSetName setName  = query.getElementSetName();
        ElementSetType set            = ElementSetType.SUMMARY;
        final List<QName> elementName = query.getElementName();
        if (setName != null) {
            set = setName.getValue();
        } else if (elementName != null && !elementName.isEmpty()){
            set = null;
        }

        SearchResultsType searchResults = null;
        
        //we get the maxRecords wanted and start position
        final Integer maxRecord = request.getMaxRecords();
        final Integer startPos  = request.getStartPosition();
        if (startPos <= 0) {
            throw new CstlServiceException("The start position must be > 0.",
                                          NO_APPLICABLE_CODE, "startPosition");
        }

        List<String> results;
        if (outputSchema.equals(EBRIM_30) || outputSchema.equals(EBRIM_25)) {
           
            // build the sql query from the specified filter
           final SQLQuery sqlQuery = (SQLQuery) sqlFilterParser.getQuery(query.getConstraint(), variables, prefixs);
           
           // TODO sort not yet implemented
           LOGGER.log(logLevel, "ebrim SQL query obtained:{0}", sqlQuery);
           try {
            // we try to execute the query
            results = mdReader.executeEbrimSQLQuery(sqlQuery.getQuery());
           } catch (MetadataIoException ex) {
               CodeList execptionCode = ex.getExceptionCode();
               if (execptionCode == null) {
                   execptionCode = NO_APPLICABLE_CODE;
               }
               throw new CstlServiceException(ex, execptionCode);
           }
            
        } else {
            
            // build the lucene query from the specified filter
            final SpatialQuery luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(query.getConstraint(), variables, prefixs);
        
            //we look for a sorting request (for now only one sort is used)
            final SortByType sortBy = query.getSortBy();
            if (sortBy != null && sortBy.getSortProperty().size() > 0) {
                final SortPropertyType first = sortBy.getSortProperty().get(0);
                if (first.getPropertyName() == null || first.getPropertyName().getPropertyName() == null || first.getPropertyName().getPropertyName().isEmpty()) {
                    throw new CstlServiceException("A SortBy filter must specify a propertyName.",
                                                  NO_APPLICABLE_CODE);
                }

                final String propertyName = StringUtilities.removePrefix(first.getPropertyName().getPropertyName()) + "_sort";
                final boolean desc        = !first.getSortOrder().equals(SortOrder.ASCENDING);
                final SortField sf        = new SortField(propertyName, SortField.STRING, desc);
                final Sort sortFilter     = new Sort(sf);
                luceneQuery.setSort(sortFilter);
            }
        
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
        
        int nextRecord         = startPos + maxRecord;
        final int totalMatched = results.size() + distributedResults.nbMatched;
        
        if (nextRecord > totalMatched)
            nextRecord = 0;
        
        final int maxDistributed = distributedResults.additionalResults.size();
        int max = (startPos - 1) + maxRecord;
        
        if (max > results.size()) {
            max = results.size();
        }
        LOGGER.log(logLevel, "local max = " + max + " distributed max = " + maxDistributed);

        int mode;
        if (outputSchema.equals(Namespaces.GMD) || outputSchema.equals(Namespaces.GFC)) {
            mode = ISO_19115;
        } else if (outputSchema.equals(EBRIM_30) || outputSchema.equals(EBRIM_25)) {
            mode = EBRIM;
        } else if (outputSchema.equals(Namespaces.CSW_202)) {
            mode = DUBLINCORE;
        } else {
            throw new IllegalArgumentException("undefined outputSchema");
        }

        // we return only the number of result matching
        if (resultType.equals(ResultType.HITS)) {
            searchResults = new SearchResultsType(id, (ElementSetType)set, results.size(), nextRecord);

        // we return a list of Record
        } else if (resultType.equals(ResultType.RESULTS)) {

            final List<AbstractRecordType> abstractRecords = new ArrayList<AbstractRecordType>();
            final List<Object> records                     = new ArrayList<Object>();
            try {
                for (int i = startPos -1; i < max; i++) {
                    final Object obj = mdReader.getMetadata(results.get(i), mode, set, elementName);
                    if (obj == null && (max + 1) < results.size()) {
                        max++;

                    } else if (obj != null) {
                        if (mode == DUBLINCORE)
                            abstractRecords.add((AbstractRecordType)obj);
                        else
                            records.add(obj);
                    }
                }
            } catch (MetadataIoException ex) {
               CodeList execptionCode = ex.getExceptionCode();
               if (execptionCode == null) {
                   execptionCode = NO_APPLICABLE_CODE;
               }
               throw new CstlServiceException(ex, execptionCode);
           }
            //we add additional distributed result
            for (int i = 0; i < maxDistributed; i++) {

                final Object additionalResult = distributedResults.additionalResults.get(i);
                if (mode == DUBLINCORE) {
                    abstractRecords.add((AbstractRecordType) additionalResult);
                } else {
                    records.add(additionalResult);
                }
            }

            if (mode == DUBLINCORE) {
                searchResults = new SearchResultsType(id,
                                                      (ElementSetType)set,
                                                      totalMatched,
                                                      abstractRecords,
                                                      abstractRecords.size(),
                                                      nextRecord);
            } else {
                searchResults = new SearchResultsType(id,
                                                      (ElementSetType) set,
                                                      totalMatched,
                                                      records.size(),
                                                      records,
                                                      nextRecord);
            }

            //we return an Acknowledgement if the request is valid.
        } else if (resultType.equals(ResultType.VALIDATE)) {
            try {
                return new AcknowledgementType(id, new EchoedRequestType(request), System.currentTimeMillis());

            } catch(DatatypeConfigurationException ex) {
                throw new CstlServiceException("DataTypeConfiguration exception while creating acknowledgment response",
                                               NO_APPLICABLE_CODE);
            }
        }
        
        response = new GetRecordsResponseType(id, System.currentTimeMillis(), request.getVersion().toString(), searchResults);
        LOGGER.log(logLevel, "GetRecords request processed in " + (System.currentTimeMillis() - startTime) + MS);
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
        LOGGER.log(logLevel, "Lucene query obtained:{0}", query);
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
     *
     * @return A GetRecordByIdResponse containing a list of records.
     */
    public GetRecordByIdResponse getRecordById(final GetRecordById request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetRecordById request processing\n");
        final long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        
        // we initialize the output format of the response
        initializeOutputFormat(request);
        
        
        // we get the level of the record to return (Brief, summary, full)
        ElementSetType set = ElementSetType.SUMMARY;
        if (request.getElementSetName() != null && request.getElementSetName().getValue() != null) {
            set = request.getElementSetName().getValue();
        }
        
        //we get the output schema and verify that we handle it
        String outputSchema = Namespaces.CSW_202;
        if (request.getOutputSchema() != null) {
            outputSchema = request.getOutputSchema();
            if (!acceptedResourceType.contains(outputSchema)) {
                throw new CstlServiceException("The server does not support this output schema: " + outputSchema,
                                                  INVALID_PARAMETER_VALUE, OUTPUT_SCHEMA);
            }
        }
        
        if (request.getId().isEmpty())
            throw new CstlServiceException("You must specify at least one identifier",
                                          MISSING_PARAMETER_VALUE, "id");
        
        //we begin to build the result
        GetRecordByIdResponseType response;
        final List<String> unexistingID        = new ArrayList<String>();
        final List<AbstractRecordType> records = new ArrayList<AbstractRecordType>();
        final List<Object> otherRecords        = new ArrayList<Object>();

        final Class expectedType;
        final int mode;
        if (outputSchema.equals(Namespaces.CSW_202)) {
            expectedType = AbstractRecordType.class;
            mode         = DUBLINCORE;
        } else if (outputSchema.equals(Namespaces.GMD))  {
            expectedType = DefaultMetadata.class;
            mode         = ISO_19115;
        } else if (outputSchema.equals(Namespaces.GFC)) {
            expectedType = null;
            mode         = ISO_19115;
        } else if (outputSchema.equals(EBRIM_30)) {
             expectedType = IdentifiableType.class;
             mode         = EBRIM;
        } else if (outputSchema.equals(EBRIM_25)) {
            expectedType = org.geotoolkit.ebrim.xml.v250.RegistryObjectType.class;
            mode         = EBRIM;
        } else {
            throw new CstlServiceException("Unexpected outputSchema");
        }

        for (String id : request.getId()) {

            //we get the form ID and catalog code
            final String saved = id;
            id = executeIdentifierQuery(id);
            if (id == null) {
                unexistingID.add(saved);
                LOGGER.log(Level.WARNING, "unexisting id:{0}", saved);
                continue;
            }

            //we get the metadata object
            try {
                final Object o = mdReader.getMetadata(id, mode, set, null);
                if (o != null) {
                    if (expectedType != null && !expectedType.isInstance(o)) {
                        LOGGER.severe("The form " + id + " is not a " + expectedType.getSimpleName() + "object.");
                        continue;
                    }
                    if (mode == DUBLINCORE) {
                        records.add((AbstractRecordType)o);
                    } else {
                        otherRecords.add(o);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "The form {0} has not be read is null.", id);
                }
            } catch (MetadataIoException ex) {
                CodeList execptionCode = ex.getExceptionCode();
                if (execptionCode == null) {
                    execptionCode = NO_APPLICABLE_CODE;
                }
                throw new CstlServiceException(ex, execptionCode);
            }
        }

        if (records.isEmpty() && otherRecords.isEmpty()) {
            throwUnexistingIdentifierException(unexistingID);
        }

        response = new GetRecordByIdResponseType(records, otherRecords);
        LOGGER.log(logLevel, "GetRecordById request processed in " + (System.currentTimeMillis() - startTime) + MS);
        return response;
    }
    
    /**
     * Launch a service exception with th specified list of unexisting ID.
     *  
     * @param unexistingID
     * @throws CstlServiceException
     */
    private void throwUnexistingIdentifierException(final List<String> unexistingID) throws CstlServiceException {
        final StringBuilder identifiers = new StringBuilder();
        for (String s : unexistingID) {
            identifiers.append(s).append(',');
        }
        String value = identifiers.toString();
        if (value.lastIndexOf(',') != -1) {
            value = value.substring(0, identifiers.length() - 1);
        }
        if (value.isEmpty()) {
            throw new CstlServiceException("The record does not correspound to the specified outputSchema.",
                                             INVALID_PARAMETER_VALUE, OUTPUT_SCHEMA);
        } else {

            throw new CstlServiceException("The identifiers " + value + " does not exist",
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
        LOGGER.log(logLevel, "DescribeRecords request processing\n");
        final long startTime = System.currentTimeMillis();
        
        verifyBaseRequest(request);

        // we initialize the output format of the response
        initializeOutputFormat(request);

        // we initialize the type names
        List<QName> typeNames = (List<QName>)request.getTypeName();
        if (typeNames == null || typeNames.isEmpty()) {
            typeNames = supportedTypeNames;
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
        final List<SchemaComponentType> components   = new ArrayList<SchemaComponentType>();

        if (typeNames.contains(RECORD_QNAME)) {
            final Object object = schemas.get(RECORD_QNAME);
            final SchemaComponentType component = new SchemaComponentType(Namespaces.CSW_202, schemaLanguage, object);
            components.add(component);
        }

        if (typeNames.contains(METADATA_QNAME)) {
            final Object object = schemas.get(METADATA_QNAME);
            final SchemaComponentType component = new SchemaComponentType(Namespaces.GMD, schemaLanguage, object);
            components.add(component);
        }

        if (containsOneOfEbrim30(typeNames)) {
            final Object object = schemas.get(EXTRINSIC_OBJECT_QNAME);
            final SchemaComponentType component = new SchemaComponentType(EBRIM_30, schemaLanguage, object);
            components.add(component);
        }

        if (containsOneOfEbrim25(typeNames)) {
            final Object object = schemas.get(EXTRINSIC_OBJECT_25_QNAME);
            final SchemaComponentType component = new SchemaComponentType(EBRIM_25, schemaLanguage, object);
            components.add(component);
        }

        LOGGER.log(logLevel, "DescribeRecords request processed in " + (System.currentTimeMillis() - startTime) + MS);
        return new DescribeRecordResponseType(components);
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetDomainResponse getDomain(final GetDomain request) throws CstlServiceException{
        LOGGER.log(logLevel, "GetDomain request processing\n");
        final long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        // we prepare the response
        List<DomainValues> responseList = new ArrayList<DomainValues>();
        
        final String parameterName = request.getParameterName();
        final String propertyName  = request.getPropertyName();
        
        // if the two parameter have been filled we launch an exception
        if (parameterName != null && propertyName != null) {
            throw new CstlServiceException("One of propertyName or parameterName must be null",
                                             INVALID_PARAMETER_VALUE, PARAMETERNAME);
        }
        
        /*
         * "parameterName" return metadata about the service itself.
         */ 
        if (parameterName != null) {
            final StringTokenizer tokens = new StringTokenizer(parameterName, ",");
            while (tokens.hasMoreTokens()) {
                final String token      = tokens.nextToken().trim();
                final int pointLocation = token.indexOf('.');
                if (pointLocation != -1) {

                    if (skeletonCapabilities == null)
                        throw new CstlServiceException("The service was unable to find the capabilities skeleton", NO_APPLICABLE_CODE);

                    final String operationName = token.substring(0, pointLocation);
                    final String parameter     = token.substring(pointLocation + 1);
                    final Operation o          = skeletonCapabilities.getOperationsMetadata().getOperation(operationName);
                    if (o != null) {
                        final DomainType param = o.getParameterIgnoreCase(parameter);
                        QName type;
                        if (operationName.equals("GetCapabilities")) {
                            type = CAPABILITIES_QNAME;
                        } else {
                            type = RECORD_QNAME;
                        }
                        if (param != null) {
                            final DomainValues value = CswXmlFactory.getDomainValues(actingVersion.version.toString(), token, null, param.getValue(), type);
                            responseList.add(value);
                        } else {
                            throw new CstlServiceException("The parameter " + parameter + " in the operation " + operationName + " does not exist",
                                                          INVALID_PARAMETER_VALUE, PARAMETERNAME);
                        }
                    } else {
                        throw new CstlServiceException("The operation " + operationName + " does not exist",
                                                      INVALID_PARAMETER_VALUE, PARAMETERNAME);
                    }
                } else {
                    throw new CstlServiceException("ParameterName must be formed like this Operation.parameterName",
                                                     INVALID_PARAMETER_VALUE, PARAMETERNAME);
                }
            }
        
        /*
         * "PropertyName" return a list of metadata for a specific field.
         */  
        } else if (propertyName != null) {
            try {
                responseList = mdReader.getFieldDomainofValues(propertyName);
            } catch (MetadataIoException ex) {
                CodeList execptionCode = ex.getExceptionCode();
                if (execptionCode == null) {
                    execptionCode = NO_APPLICABLE_CODE;
                }
                throw new CstlServiceException(ex, execptionCode);
            }
            
        // if no parameter have been filled we launch an exception    
        } else {
            throw new CstlServiceException("One of propertyName or parameterName must be filled",
                                          MISSING_PARAMETER_VALUE, "parameterName, propertyName");
        }
        LOGGER.log(logLevel, "GetDomain request processed in " + (System.currentTimeMillis() - startTime) + MS);

        return CswXmlFactory.getDomainResponse(actingVersion.version.toString(), responseList);
    }
    
    /**
     * A web service method alowing to Insert / update / delete record from the CSW.
     * 
     * @param request
     * @return
     */
    public TransactionResponseType transaction(final Transaction request) throws CstlServiceException {
        LOGGER.log(logLevel, "Transaction request processing\n");
        
        if (profile == DISCOVERY) {
            throw new CstlServiceException("This method is not supported by this mode of CSW",
                                          OPERATION_NOT_SUPPORTED, "Request");
        }
        
        final long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        // we prepare the report
        int totalInserted       = 0;
        int totalUpdated        = 0;
        int totalDeleted        = 0;
        final String requestID  = request.getRequestId();
        
        final List<Object> transactions = request.getInsertOrUpdateOrDelete();
        for (Object transaction: transactions) {
            if (transaction instanceof InsertType) {
                final InsertType insertRequest = (InsertType)transaction;
                
                for (Object record : insertRequest.getAny()) {

                    try {
                        mdWriter.storeMetadata(record);
                        totalInserted++;

                    } catch (IllegalArgumentException e) {
                        LOGGER.severe("already that title.");
                        totalUpdated++;
                    } catch (MetadataIoException ex) {
                        CodeList execptionCode = ex.getExceptionCode();
                        if (execptionCode == null) {
                            execptionCode = NO_APPLICABLE_CODE;
                        }
                        throw new CstlServiceException(ex, execptionCode);
                    }
                }

            } else if (transaction instanceof DeleteType) {
                if (mdWriter.deleteSupported()) {
                    final DeleteType deleteRequest = (DeleteType)transaction;
                    //String dataType = deleteRequest.getTypeName();
                    if (deleteRequest.getConstraint() == null) {
                        throw new CstlServiceException("A constraint must be specified.",
                                                      MISSING_PARAMETER_VALUE, "constraint");
                    }

                    // build the lucene query from the specified filter
                    final SpatialQuery luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(deleteRequest.getConstraint(), null, null);

                    // we try to execute the query
                    final List<String> results = executeLuceneQuery(luceneQuery);

                    try {
                        for (String metadataID : results) {
                            final boolean deleted = mdWriter.deleteMetadata(metadataID);
                            if (!deleted) {
                                throw new CstlServiceException("The service does not succeed to delete the metadata:" + metadataID,
                                                  NO_APPLICABLE_CODE);
                            } else {
                                totalDeleted++;
                            }
                        }
                    } catch (MetadataIoException ex) {
                        CodeList execptionCode = ex.getExceptionCode();
                        if (execptionCode == null) {
                            execptionCode = NO_APPLICABLE_CODE;
                        }
                        throw new CstlServiceException(ex, execptionCode);
                    }
                } else {
                    throw new CstlServiceException("This kind of transaction (delete) is not supported by this Writer implementation.",
                                                  NO_APPLICABLE_CODE, TRANSACTION_TYPE);
                }
                
                
            } else if (transaction instanceof UpdateType) {
                if (mdWriter.updateSupported()) {
                    final UpdateType updateRequest = (UpdateType) transaction;
                    if (updateRequest.getConstraint() == null) {
                        throw new CstlServiceException("A constraint must be specified.",
                                MISSING_PARAMETER_VALUE, "constraint");
                    }
                    if (updateRequest.getAny() == null && updateRequest.getRecordProperty().isEmpty()) {
                        throw new CstlServiceException("The any part or a list od RecordProperty must be specified.",
                                MISSING_PARAMETER_VALUE, "MD_Metadata");
                    } else if (updateRequest.getAny() != null && !updateRequest.getRecordProperty().isEmpty()) {
                        throw new CstlServiceException("You must choose between the any part or a list of RecordProperty, you can't specify both.",
                                MISSING_PARAMETER_VALUE, "MD_Metadata");
                    }

                    // build the lucene query from the specified filter
                    final SpatialQuery luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(updateRequest.getConstraint(), null, null);

                    // we try to execute the query
                    try {
                        final List<String> results = executeLuceneQuery(luceneQuery);
                        for (String metadataID : results) {
                            boolean updated;
                            if (updateRequest.getAny() != null) {
                                updated = mdWriter.replaceMetadata(metadataID, updateRequest.getAny());
                            } else {
                                updated = mdWriter.updateMetadata(metadataID, updateRequest.getRecordProperty());
                            }
                            if (!updated) {
                                throw new CstlServiceException("The service does not succeed to update the metadata:" + metadataID,
                                        NO_APPLICABLE_CODE);
                            } else {
                                mdReader.removeFromCache(metadataID);
                                totalUpdated++;
                            }
                        }
                    } catch (MetadataIoException ex) {
                        CodeList execptionCode = ex.getExceptionCode();
                        if (execptionCode == null) {
                            execptionCode = NO_APPLICABLE_CODE;
                        }
                        throw new CstlServiceException(ex, execptionCode);
                    }
                } else {
                    throw new CstlServiceException("This kind of transaction (update) is not supported by this Writer implementation.",
                            NO_APPLICABLE_CODE, TRANSACTION_TYPE);
                }
            } else {
                String className = " null object";
                if (transaction != null) {
                    className = transaction.getClass().getName();
                }
                throw new CstlServiceException("This kind of transaction is not supported by the service: " + className,
                                              INVALID_PARAMETER_VALUE, TRANSACTION_TYPE);
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
        final TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                          totalUpdated,
                                                                          totalDeleted,
                                                                          requestID);
        final TransactionResponseType response = new TransactionResponseType(summary, null, request.getVersion().toString());
        LOGGER.log(logLevel, "Transaction request processed in " + (System.currentTimeMillis() - startTime) + MS);
        return response;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public HarvestResponseType harvest(final Harvest request) throws CstlServiceException {
        LOGGER.log(logLevel, "Harvest request processing\n");
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
        final String resourceType = request.getResourceType();
        if (resourceType == null) {
            throw new CstlServiceException("The resource type to harvest must be specified",
                                          MISSING_PARAMETER_VALUE, "resourceType");
        } else {
            if (!acceptedResourceType.contains(resourceType)) {
                throw new CstlServiceException("This resource type is not allowed. ",
                                             MISSING_PARAMETER_VALUE, "resourceType");
            }
        }
        final String sourceURL = request.getSource();
        if (sourceURL != null) {
            try {

                // TODO find a better to determine if the source is single or catalogue
                int mode = 1;
                if (sourceURL.endsWith("xml")) {
                    mode = 0;
                }

                //mode synchronous
                if (request.getResponseHandler().isEmpty()) {

                    // if the resource is a simple record
                    if (mode == 0) {
                        final int[] results = catalogueHarvester.harvestSingle(sourceURL, resourceType);
                        totalInserted = results[0];
                        totalUpdated  = results[1];
                        totalDeleted  = 0;

                    // if the resource is another CSW service we get all the data of this catalogue.
                    } else {
                        final int[] results = catalogueHarvester.harvestCatalogue(sourceURL);
                        totalInserted = results[0];
                        totalUpdated  = results[1];
                        totalDeleted  = results[2];
                    }
                    final TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                                      totalUpdated,
                                                                                      totalDeleted,
                                                                                      null);
                    final TransactionResponseType transactionResponse = new TransactionResponseType(summary, null, request.getVersion().toString());
                    response = new HarvestResponseType(transactionResponse);

                //mode asynchronous
                } else {

                    final AcknowledgementType acknowledgement = new AcknowledgementType(null, new EchoedRequestType(request), System.currentTimeMillis());
                    response = new HarvestResponseType(acknowledgement);
                    long period = 0;
                    if (request.getHarvestInterval() != null) {
                        period = request.getHarvestInterval().getTimeInMillis(new Date(System.currentTimeMillis()));
                    }

                    harvestTaskSchreduler.newAsynchronousHarvestTask(period, sourceURL, resourceType, mode, request.getResponseHandler());
                    
                }
                
            } catch (SQLException ex) {
                throw new CstlServiceException("The service has throw an SQLException: " + ex.getMessage(),
                                              NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("The resource can not be parsed: " + ex.getMessage(),
                                              INVALID_PARAMETER_VALUE, SOURCE);
            } catch (MalformedURLException ex) {
                throw new CstlServiceException("The source URL is malformed",
                                              INVALID_PARAMETER_VALUE, SOURCE);
            } catch (IOException ex) {
                throw new CstlServiceException("The service can't open the connection to the source",
                                              INVALID_PARAMETER_VALUE, SOURCE);
            } catch (DatatypeConfigurationException ex) {
                throw new CstlServiceException("The service has made an error of timestamp",
                                              INVALID_PARAMETER_VALUE);
            }
            
        } else {
            throw new CstlServiceException("you must specify a source",
                                              MISSING_PARAMETER_VALUE, SOURCE);
        }
        
        LOGGER.log(logLevel, "Harvest operation finished");
        return response;
    }
    
    
    
    /**
     * Return the current output format (default: application/xml)
     */
    public String getOutputFormat() {
        if (outputFormat == null) {
            return MimeType.APPLICATION_XML;
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
    private void verifyBaseRequest(final AbstractCswRequest request) throws CstlServiceException {
        isWorking();
        if (request != null) {
            if (request.getService() != null) {
                if (!request.getService().equals(CSW))  {
                    throw new CstlServiceException("service must be \"CSW\"!",
                                                  INVALID_PARAMETER_VALUE, SERVICE);
                }
            } else {
                throw new CstlServiceException("service must be specified!",
                                              MISSING_PARAMETER_VALUE, SERVICE);
            }
            if (request.getVersion()!= null) {
                /*
                 * Ugly patch to begin to support CSW 2.0.0 request
                 *
                 * TODO remove this
                 */
                if (request.getVersion().toString().equals(CSW_202_VERSION)) {
                    this.actingVersion = ServiceDef.CSW_2_0_2;
                } else if (request.getVersion().toString().equals("2.0.0") && (request instanceof GetDomain)) {
                    this.actingVersion = ServiceDef.CSW_2_0_0;

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
        final StringBuilder result = new StringBuilder();
        for (QName qn: supportedTypeNames) {
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
        final String format = request.getOutputFormat();
        if (format != null && isSupportedFormat(format)) {
            outputFormat = format;
        } else if (format != null && !isSupportedFormat(format)) {
            final StringBuilder supportedFormat = new StringBuilder();
            for (String s: ACCEPTED_OUTPUT_FORMATS) {
                supportedFormat.append(s).append('\n');
            } 
            throw new CstlServiceException("The server does not support this output format: " + format + '\n' +
                                             " supported ones are: " + '\n' + supportedFormat.toString(),
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
    private void initLogger(String id, String filePath) {
        try {
            if (id != null && !id.isEmpty()) {
                id = id + '-';
            }
            final FileHandler handler = new FileHandler(filePath + '/'+ id + "cstl-csw.log");
            handler.setFormatter(new MonolineFormatter(handler));
            LOGGER.addHandler(handler);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO exception while trying to separate CSW Logs:{0}", ex.getMessage());
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Security exception while trying to separate CSW Logs{0}", ex.getMessage());
        }
    }

     /**
     * In some implementations there is no sicade directory.
     * So if we don't find The .constellation/csw_configuration directory
     * IFREMER hack
     * we search the deployed war directory /WEB-INF/classes/csw_configuration
     */
    public static File getConfigDirectory() {

        /* Ifremer's server does not contain any .constellation directory, so the
         * configuration files are put under the WEB-INF/classes/configuration/ directory of the WAR file.
         */
        File configDir = FileUtilities.getDirectoryFromResource("configuration");

        final String configUrl = "csw_configuration";

        /*
         * if the configuration files are put under the WEB-INF/classes/csw_configuration directory of the WAR file.
         */
        if (configDir == null || !configDir.exists()) {
            configDir = FileUtilities.getDirectoryFromResource(configUrl);
        }

       // else we search the .constellation directory
        if (configDir == null || !configDir.exists()) {
            configDir = new File(ConfigDirectory.getConfigDirectory(), configUrl);
        }

        if (configDir != null) {
            LOGGER.log(Level.INFO, "taking configuration from constellation directory: {0}", configDir.getPath());
        }
        return configDir;
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
        if (mdReader != null) {
            mdReader.destroy();
        }
        if (mdWriter != null) {
            mdWriter.destroy();
        }
        if (indexSearcher != null) {
            indexSearcher.destroy();
        }
        if (harvestTaskSchreduler != null) {
            harvestTaskSchreduler.destroy();
        }
        if (catalogueHarvester != null) {
            catalogueHarvester.destroy();
        }
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
        if (indexSearcher != null) {
            indexSearcher.setLogLevel(logLevel);
        }
        if (mdWriter != null) {
            mdWriter.setLogLevel(logLevel);
        }

        if (mdReader != null) {
            mdReader.setLogLevel(logLevel);
        }
    }

}
