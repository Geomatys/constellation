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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;

// JAXB dependencies
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// Apache Lucene dependencies
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

//Constellation dependencies
import org.apache.sis.util.logging.MonolineFormatter;
import org.constellation.ServiceDef;
import org.constellation.configuration.DataSourceType;
import org.constellation.filter.FilterParser;
import org.constellation.filter.FilterParserException;
import org.constellation.filter.SQLQuery;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.security.MetadataSecurityFilter;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.security.SecurityManager;

import static org.constellation.api.QueryConstants.*;
import static org.constellation.metadata.io.AbstractMetadataReader.*;
import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.metadata.CSWConstants.*;

//geotoolkit dependencies
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.UnauthorizedException;
import org.geotoolkit.csw.xml.*;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.inspire.xml.InspireCapabilitiesType;
import org.geotoolkit.inspire.xml.MultiLingualCapabilities;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.ebrim.xml.v300.IdentifiableType;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.SearchingException;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.lucene.index.LuceneIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.ogc.xml.SortBy;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.util.StringUtilities;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.Namespaces;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.xsd.xml.v2001.XSDMarshallerPool;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.csw.xml.TypeNames.*;

// GeoAPI dependencies
import org.opengis.filter.sort.SortOrder;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.util.CodeList;


/**
 * The CSW (Catalog Service Web) engine.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CSWworker extends AbstractWorker {

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
    private LuceneIndexSearcher indexSearcher;

    /**
     * A filter parser which create lucene query from OGC filter
     */
    private FilterParser luceneFilterParser;

    /**
     * A filter parser which create SQL query from OGC filter (used for ebrim query)
     */
    private FilterParser sqlFilterParser;

    /**
     * A catalogue Harvester communicating with other CSW
     */
    private CatalogueHarvester catalogueHarvester;

    /**
     * A task scheduler for asynchronous harvest.
     */
    private HarvestTaskSchreduler harvestTaskSchreduler;

    /**
     * A list of the supported Type name
     */
    private List<QName> supportedTypeNames;

    /**
     * A list of the supported SchemaLanguage for describeRecord Operation
     */
    private List<String> supportedSchemaLanguage;

    /**
     * A map of QName - xsd schema object
     */
    private final Map<QName, Object> schemas = new HashMap<>();

    /**
     * A list of supported MIME type.
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = new ArrayList<>();
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

    private MetadataSecurityFilter securityFilter;
    
    private Automatic configuration;
            
    /**
     * Build a new CSW worker with the specified configuration directory
     *
     * @param serviceID The service identifier (used in multiple CSW context). default value is "".
     *
     */
    public CSWworker(final String serviceID, final File configDir) {
        this(serviceID, configDir, null);
    }

    /**
     * Build a new CSW worker with the specified configuration directory
     *
     * @param serviceID The service identifier (used in multiple CSW context). default value is "".
     *
     */
    public CSWworker(final String serviceID, final File configDir, Automatic candidate) {
        super(serviceID, configDir, ServiceDef.Specification.CSW);
        setSupportedVersion(ServiceDef.CSW_2_0_2);
        isStarted = true;
        try {
            //we look if the configuration have been specified
            if (candidate == null) {
                final MarshallerPool pool             = GenericDatabaseMarshallerPool.getInstance();
                final Unmarshaller configUnmarshaller = pool.acquireUnmarshaller();
                final File configFile                 = new File(configDir, "config.xml");
                if (!configFile.exists()) {
                    startError = "The configuration file has not been found";
                    LOGGER.log(Level.WARNING, "\nThe CSW worker( {0}) is not working!\nCause: " + startError, serviceID);
                    isStarted = false;
                    return;
                } else {
                    candidate = (Automatic) configUnmarshaller.unmarshal(configFile);
                }
                pool.recycle(configUnmarshaller);
            }
            configuration = candidate;

            // we initialize the filterParsers
            init(configuration, "", configDir);
            String suffix = "";
            if (profile == TRANSACTIONAL) {
                suffix = "-T";
            }
            // look for log level
            setLogLevel(configuration.getLogLevel());

            // look for shiro accessibility
            final String sa = configuration.getParameter("shiroAccessible");
            if (sa != null && !sa.isEmpty()) {
                shiroAccessible = Boolean.parseBoolean(sa);
            }
            LOGGER.info("CSW" + suffix + " worker (" + configuration.getFormat() + ") \"" + serviceID + "\" running\n");

        } catch (FactoryNotFoundException ex) {
            startError = " Unable to find a CSW Factory";
            LOGGER.log(Level.WARNING, "\nThe CSW worker is not working!\nCause:{0}", startError);
            isStarted = false;
        } catch (MetadataIoException e) {
            startError = e.getMessage();
            LOGGER.log(Level.WARNING, "\nThe CSW worker is not working!\nCause:{0}\n", startError);
            isStarted = false;
        } catch (IndexingException e) {
            startError = e.getMessage();
            LOGGER.log(Level.WARNING, "\nThe CSW worker is not working!\nCause:{0}\n", startError);
            isStarted = false;
        } catch (IllegalArgumentException e) {
            startError = e.getLocalizedMessage();
            LOGGER.log(Level.WARNING, "\nThe CSW worker is not working!\nCause: IllegalArgumentException: {0}\n", startError);
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            isStarted = false;
        } catch (CstlServiceException e) {
            startError = e.getLocalizedMessage();
            LOGGER.log(Level.WARNING, "\nThe CSW worker is not working!\nCause: CstlServiceException: {0}\n", startError);
            LOGGER.log(Level.FINER, e.getLocalizedMessage(), e);
            isStarted = false;
        }  catch (JAXBException e) {
            startError =  "JAXBException:" + e.getLocalizedMessage();
            LOGGER.log(Level.WARNING, "\nThe CSW worker is not working!\nCause: {0}\n", startError);
            LOGGER.log(Level.FINER, e.getLocalizedMessage(), e);
            isStarted = false;
        }
    }

    /**
     * Initialize the readers and indexSearcher to the dataSource for the discovery profile.
     * If The transactional part is enabled, it also initialize Writer and catalog harvester.
     *
     * @param configuration A configuration object containing the dataSource informations
     * @param serviceID The identifier of the instance.
     * @param configDir The directory containing the configuration files.
     *
     * @throws MetadataIoException If an error occurs while querying the dataSource.
     * @throws IndexingException If an error occurs while initializing the indexation.
     */
    private void init(final Automatic configuration,final String serviceID, final File configDir) throws MetadataIoException, IndexingException, JAXBException, CstlServiceException {

        // we assign the configuration directory
        configuration.setConfigurationDirectory(configDir);
        final DataSourceType datasourceType = configuration.getType();

        // we load the factory from the available classes
        final AbstractCSWFactory cswfactory = getCSWFactory(datasourceType);
        LOGGER.log(Level.FINER, "CSW factory loaded:{0}", cswfactory.getClass().getName());

        //we initialize all the data retriever (reader/writer) and index worker
        mdReader                      = cswfactory.getMetadataReader(configuration);
        profile                       = configuration.getProfile();
        final AbstractIndexer indexer = cswfactory.getIndexer(configuration, mdReader, serviceID, mdReader.getAdditionalQueryablePathMap());
        indexSearcher                 = cswfactory.getIndexSearcher(configDir, serviceID, indexer.getRtree());
        luceneFilterParser            = cswfactory.getLuceneFilterParser();
        sqlFilterParser               = cswfactory.getSQLFilterParser();
        securityFilter                = cswfactory.getSecurityFilter();
        if (profile == TRANSACTIONAL) {
            mdWriter                  = cswfactory.getMetadataWriter(configuration, indexer);
            catalogueHarvester        = cswfactory.getCatalogueHarvester(configuration, mdWriter);
            harvestTaskSchreduler     = new HarvestTaskSchreduler(configDir, catalogueHarvester);
        } else {
            indexer.destroy();
        }
        initializeSupportedTypeNames();
        initializeSupportedSchemaLanguage();
        initializeAcceptedResourceType();
        initializeRecordSchema();
        initializeAnchorsMap();
        loadCascadedService(configDir);
    }

    /**
     * Select the good CSW factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private AbstractCSWFactory getCSWFactory(DataSourceType type) {
        final Iterator<AbstractCSWFactory> ite = ServiceRegistry.lookupProviders(AbstractCSWFactory.class);
        while (ite.hasNext()) {
            AbstractCSWFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No OM factory has been found for type:" + type);
    }

    /**
     * Initialize the supported type names in function of the reader capacity.
     */
    private void initializeSupportedTypeNames() {
        supportedTypeNames = new ArrayList<QName>();
        final List<Integer> supportedDataTypes = mdReader.getSupportedDataTypes();
        if (supportedDataTypes.contains(ISO_19115)) {
            supportedTypeNames.addAll(ISO_TYPE_NAMES);
        }
        if (supportedDataTypes.contains(DUBLINCORE)) {
            supportedTypeNames.addAll(DC_TYPE_NAMES);
        }
        if (supportedDataTypes.contains(EBRIM)) {
            supportedTypeNames.addAll(EBRIM30_TYPE_NAMES);
            supportedTypeNames.addAll(EBRIM25_TYPE_NAMES);
        }
        if (supportedDataTypes.contains(ISO_19110)) {
            supportedTypeNames.addAll(FC_TYPE_NAMES);
        }
    }

    /**
     * Initialize the supported outputSchema in function of the reader capacity.
     */
    private void initializeAcceptedResourceType() {
        acceptedResourceType = new ArrayList<>();
        final List<Integer> supportedDataTypes = mdReader.getSupportedDataTypes();
        if (supportedDataTypes.contains(ISO_19115)) {
            acceptedResourceType.add(Namespaces.GMD);
            acceptedResourceType.add(Namespaces.GFC);
        }
        if (supportedDataTypes.contains(DUBLINCORE)) {
            acceptedResourceType.add(Namespaces.CSW);
        }
        if (supportedDataTypes.contains(EBRIM)) {
            acceptedResourceType.add(EBRIM_30);
            acceptedResourceType.add(EBRIM_25);
        }
    }

    /**
     * Initialize the supported outputSchema in function of the reader capacity.
     */
    private void initializeSupportedSchemaLanguage() {
        supportedSchemaLanguage = new ArrayList<>();
        supportedSchemaLanguage.add("http://www.w3.org/XML/Schema");
        supportedSchemaLanguage.add("XMLSCHEMA");
        supportedSchemaLanguage.add("http://www.w3.org/TR/xmlschema-1/");
    }

    /**
     * Load from the resource the XSD schemas used for the response of describeRecord.
     *
     * @throws CstlServiceException if there is a JAXBException while using the unmarshaller.
     */
    private void initializeRecordSchema() throws CstlServiceException {
        try {
            final Unmarshaller unmarshaller = XSDMarshallerPool.getInstance().acquireUnmarshaller();

            schemas.put(RECORD_QNAME,              unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/record.xsd")));
            schemas.put(METADATA_QNAME,            unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/metadata.xsd")));
            schemas.put(EXTRINSIC_OBJECT_QNAME,    unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/ebrim-3.0.xsd")));
            schemas.put(EXTRINSIC_OBJECT_25_QNAME, unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/ebrim-2.5.xsd")));
             XSDMarshallerPool.getInstance().recycle(unmarshaller);

        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB Exception when trying to parse xsd file", ex, NO_APPLICABLE_CODE);
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
        cascadedCSWservers = new ArrayList<>();
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


    public void setCascadedService(final List<String> urls) throws CstlServiceException {
        cascadedCSWservers = urls;
        final StringBuilder s = new StringBuilder("Cascaded Services:\n");
        for (String servURL: urls) {
            s.append(servURL).append('\n');
        }
        LOGGER.info(s.toString());

        final File f = new File(configurationDirectory, "CSWCascading.properties");
        try {
            final OutputStream out = new FileOutputStream(f);
            final Properties cascad = new Properties();
            for (int i = 0; i < urls.size(); i++) {
                cascad.put("csw" + i, urls.get(i));
            }
            cascad.store(out, "updated by admin service");
        } catch (IOException ex) {
            throw new CstlServiceException("IO excrption while storing cacadedService", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Web service operation describing the service and its capabilities.
     *
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     */
    public AbstractCapabilities getCapabilities(final GetCapabilities requestCapabilities) throws CstlServiceException {
        isWorking();
        LOGGER.log(logLevel, "getCapabilities request processing\n");
        final long startTime = System.currentTimeMillis();

        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals(CSW)) {
                throw new CstlServiceException("service must be \"CSW\"!", INVALID_PARAMETER_VALUE, SERVICE_PARAMETER);
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, SERVICE_PARAMETER);
        }
        final AcceptVersions versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains(CSW_202_VERSION)){
                 throw new CstlServiceException("version available : 2.0.2",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }
        
        Sections sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType(ALL);
        }
        //according to CITE test a GetCapabilities must always return Filter_Capabilities
        if (!sections.containsSection(FILTER_CAPABILITIES) || sections.containsSection(ALL)) {
            sections.add(FILTER_CAPABILITIES);
        }
        
        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(requestCapabilities.getUpdateSequence());
        if (returnUS) {
            return CswXmlFactory.createCapabilities("2.0.2", getCurrentUpdateSequence());
        }
        
        final AbstractCapabilitiesCore cachedCapabilities = getCapabilitiesFromCache("2.0.2", null);
        if (cachedCapabilities != null) {
            return (AbstractCapabilities) cachedCapabilities.applySections(sections);
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

        // we load the skeleton capabilities
        final AbstractCapabilities skeletonCapabilities = (AbstractCapabilities) getStaticCapabilitiesObject("2.0.2", "CSW");

        //we prepare the response document
        final AbstractServiceIdentification si = skeletonCapabilities.getServiceIdentification();
        final AbstractServiceProvider       sp = skeletonCapabilities.getServiceProvider();
        final FilterCapabilities            fc = CSW_FILTER_CAPABILITIES;
        final AbstractOperationsMetadata   om  = CSWConstants.OPERATIONS_METADATA.clone();
        
        // we remove the operation not supported in this profile (transactional/discovery)
        if (profile == DISCOVERY) {
            om.removeOperation("Harvest");
            om.removeOperation("Transaction");
        }

        // we update the URL
        om.updateURL(getServiceUrl());

        // we add the cascaded services (if there is some)
        final AbstractDomain cascadedCSW  = om.getConstraint("FederatedCatalogues");
        if (cascadedCSW == null) {
            if (cascadedCSWservers != null && !cascadedCSWservers.isEmpty()) {
                final AbstractDomain fedCata = CswXmlFactory.createDomain("2.0.2","FederatedCatalogues", cascadedCSWservers);
                om.addConstraint(fedCata);
            }
        } else {
            if (cascadedCSWservers != null && !cascadedCSWservers.isEmpty()) {
                cascadedCSW.setValue(cascadedCSWservers);
            } else {
                om.removeConstraint("FederatedCatalogues");
            }
        }

        // we update the operation parameters
        final AbstractOperation gr = om.getOperation("GetRecords");
        if (gr != null) {
            final AbstractDomain os = gr.getParameter(OUTPUT_SCHEMA);
            if (os != null) {
                os.setValue(acceptedResourceType);
            }
            final AbstractDomain tn = gr.getParameter(TYPENAMES);
            if (tn != null) {
                final List<String> values = new ArrayList<>();
                for (QName qn : supportedTypeNames) {
                    values.add(Namespaces.getPreferredPrefix(qn.getNamespaceURI(), "") + ':' + qn.getLocalPart());
                }
                tn.setValue(values);
            }

            //we update the ISO queryable elements :
            final AbstractDomain isoQueryable = gr.getConstraint("SupportedISOQueryables");
            if (isoQueryable != null) {
                final List<String> values = new ArrayList<>();
                for (String name : ISO_QUERYABLE.keySet() ) {
                    values.add("apiso:" + name);
                }
                isoQueryable.setValue(values);
            }
            //we update the DC queryable elements :
            final AbstractDomain dcQueryable = gr.getConstraint("SupportedDublinCoreQueryables");
            if (dcQueryable != null) {
                final List<String> values = new ArrayList<>();
                for (String name : DUBLIN_CORE_QUERYABLE.keySet() ) {
                    values.add("dc:" + name);
                }
                dcQueryable.setValue(values);
            }

            //we update the reader's additional queryable elements :
            final AbstractDomain additionalQueryable = gr.getConstraint("AdditionalQueryables");
            if (additionalQueryable != null) {
                final List<String> values = new ArrayList<String>();
                for (QName name : mdReader.getAdditionalQueryableQName()) {
                    // allow to redefine the mapping in reader implementation
                    if (!ISO_QUERYABLE.containsKey(name.getLocalPart()) &&
                        !DUBLIN_CORE_QUERYABLE.containsKey(name.getLocalPart())) {
                        values.add(name.getPrefix() + ':' + name.getLocalPart());
                    }
                }
                if (values.size() > 0) {
                    additionalQueryable.setValue(values);
                }
            }
        }

        final AbstractOperation grbi = om.getOperation("GetRecordById");
        if (grbi != null) {
            final AbstractDomain os = grbi.getParameter(OUTPUT_SCHEMA);
            if (os != null) {
                os.setValue(acceptedResourceType);
            }
        }

        final AbstractOperation dr = om.getOperation("DescribeRecord");
        if (dr != null) {
            final AbstractDomain tn = dr.getParameter("TypeName");
            if (tn != null) {
                final List<String> values = new ArrayList<String>();
                for (QName qn : supportedTypeNames) {
                    values.add(Namespaces.getPreferredPrefix(qn.getNamespaceURI(), "") + ':' + qn.getLocalPart());
                }
                tn.setValue(values);
            }
            final AbstractDomain sl = dr.getParameter("SchemaLanguage");
            if (sl != null) {
                sl.setValue(supportedSchemaLanguage);
            }
        }
        
        final AbstractOperation hr = om.getOperation("Harvest");
        if (hr != null) {
            final AbstractDomain tn = hr.getParameter("ResourceType");
            if (tn != null) {
                tn.setValue(acceptedResourceType);
            }
        }
        
        final AbstractOperation tr = om.getOperation("Transaction");
        if (tr != null) {
            final AbstractDomain tn = tr.getParameter("ResourceType");
            if (tn != null) {
                tn.setValue(acceptedResourceType);
            }
        }

        //we add the INSPIRE extend capabilties
        final InspireCapabilitiesType inspireCapa = new InspireCapabilitiesType(Arrays.asList("FRA", "ENG"));
        final MultiLingualCapabilities m          = new MultiLingualCapabilities();
        m.setMultiLingualCapabilities(inspireCapa);
        om.setExtendedCapabilities(m);

        final AbstractCapabilities c = CswXmlFactory.createCapabilities("2.0.2", si, sp, om, null, fc);

        putCapabilitiesInCache("2.0.2", null, c);
        LOGGER.log(logLevel, "GetCapabilities request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return (AbstractCapabilities) c.applySections(sections);
    }

    /**
     * Web service operation which permits to search the catalog to find records.
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

        final String version   = request.getVersion().toString();
        final String id        = request.getRequestId();
        final String userLogin = getUserLogin();

        // we initialize the output format of the response
        initializeOutputFormat(request);

        //we get the output schema and verify that we handle it
        String outputSchema = Namespaces.CSW;
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
        Query query;
        List<QName> typeNames;
        final Map<String, QName> variables = new HashMap<>();
        final Map<String, String> prefixs  = new HashMap<>();
        if (request.getAbstractQuery() != null) {
            query = (Query)request.getAbstractQuery();
            typeNames =  query.getTypeNames();
            if (typeNames == null || typeNames.isEmpty()) {
                throw new CstlServiceException("The query must specify at least typeName.",
                                              INVALID_PARAMETER_VALUE, TYPENAMES);
            } else {
                for (QName type : typeNames) {
                    if (type != null) {
                        prefixs.put(type.getPrefix(), type.getNamespaceURI());
                        //for ebrim mode the user can put variable after the Qname
                        if (type.getLocalPart().indexOf('_') != -1 && !(type.getLocalPart().startsWith("MD") || type.getLocalPart().startsWith("FC"))) {
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
                        throw new CstlServiceException("The typeName " + type.getLocalPart() + " is not supported by the service:" +'\n' +
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
                LOGGER.log(Level.FINER, report.toString());
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

        SearchResults searchResults = null;

        //we get the maxRecords wanted and start position
        final Integer maxRecord = request.getMaxRecords();
        final Integer startPos  = request.getStartPosition();
        if (startPos <= 0) {
            throw new CstlServiceException("The start position must be > 0.",
                                          NO_APPLICABLE_CODE, "startPosition");
        }

        final String[] results;
        if (outputSchema.equals(EBRIM_30) || outputSchema.equals(EBRIM_25)) {

            // build the sql query from the specified filter
            final SQLQuery sqlQuery;
            try {
                sqlQuery = (SQLQuery) sqlFilterParser.getQuery(query.getConstraint(), variables, prefixs, getConvertibleTypeNames(typeNames));
            } catch (FilterParserException ex) {
                throw new CstlServiceException(ex.getMessage(), ex, ex.getExceptionCode(), ex.getLocator());
            }

           // TODO sort not yet implemented
           LOGGER.log(logLevel, "ebrim SQL query obtained:{0}", sqlQuery);
           try {
            // we try to execute the query
            results = securityFilter.filterResults(userLogin, mdReader.executeEbrimSQLQuery(sqlQuery.getQuery()));
           } catch (MetadataIoException ex) {
               CodeList execptionCode = ex.getExceptionCode();
               if (execptionCode == null) {
                   execptionCode = NO_APPLICABLE_CODE;
               }
               throw new CstlServiceException(ex, execptionCode);
           }

        } else {

            // build the lucene query from the specified filter
            final SpatialQuery luceneQuery;
            try {
                luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(query.getConstraint(), variables, prefixs, getConvertibleTypeNames(typeNames));
            } catch (FilterParserException ex) {
                throw new CstlServiceException(ex.getMessage(), ex, ex.getExceptionCode(), ex.getLocator());
            }

            //we look for a sorting request (for now only one sort is used)
            final SortBy sortBy = query.getSortBy();
            if (sortBy != null && sortBy.getSortProperty().size() > 0) {
                final org.opengis.filter.sort.SortBy first = sortBy.getSortProperty().get(0);
                if (first.getPropertyName() == null || first.getPropertyName().getPropertyName() == null || first.getPropertyName().getPropertyName().isEmpty()) {
                    throw new CstlServiceException("A SortBy filter must specify a propertyName.",
                                                  NO_APPLICABLE_CODE);
                }

                final String propertyName = StringUtilities.removePrefix(first.getPropertyName().getPropertyName()) + "_sort";
                final boolean desc        = !first.getSortOrder().equals(SortOrder.ASCENDING);
                final SortField sf;
                final Character fieldType =  indexSearcher.getNumericFields().get(propertyName);
                if (fieldType != null) {
                    switch (fieldType) {
                        case 'd': sf = new SortField(propertyName, SortField.Type.DOUBLE, desc);break;
                        case 'i': sf = new SortField(propertyName, SortField.Type.INT, desc);break;
                        case 'f': sf = new SortField(propertyName, SortField.Type.FLOAT, desc);break;
                        case 'l': sf = new SortField(propertyName, SortField.Type.LONG, desc);break;
                        default : sf = new SortField(propertyName, SortField.Type.STRING, desc);break;
                    }
                } else {
                    sf = new SortField(propertyName, SortField.Type.STRING, desc);
                }

                final Sort sortFilter     = new Sort(sf);
                luceneQuery.setSort(sortFilter);
            }

            // we try to execute the query
            results = securityFilter.filterResults(userLogin, executeLuceneQuery(luceneQuery));
        }
        final int nbResults = results.length;

        //we look for distributed queries
        DistributedResults distributedResults = new DistributedResults();
        if (catalogueHarvester != null) {
            final DistributedSearch dSearch = request.getDistributedSearch();
            if (dSearch != null && dSearch.getHopCount() > 0) {
                int distributedStartPosition;
                int distributedMaxRecord;
                if (startPos > nbResults) {
                    distributedStartPosition = startPos - nbResults;
                    distributedMaxRecord     = maxRecord;
                } else {
                    distributedStartPosition = 1;
                    distributedMaxRecord     = maxRecord - nbResults;
                }
                //decrement the hopCount
                dSearch.setHopCount(dSearch.getHopCount() - 1);
                distributedResults = catalogueHarvester.transferGetRecordsRequest(request, cascadedCSWservers, distributedStartPosition, distributedMaxRecord);
            }
        }

        int nextRecord         = startPos + maxRecord;
        final int totalMatched = nbResults + distributedResults.nbMatched;

        if (nextRecord > totalMatched) {
            nextRecord = 0;
        }

        final int maxDistributed = distributedResults.additionalResults.size();
        int max = (startPos - 1) + maxRecord;

        if (max > nbResults) {
            max = nbResults;
        }
        LOGGER.log(Level.FINER, "local max = " + max + " distributed max = " + maxDistributed);

        int mode;
        if (outputSchema.equals(Namespaces.GMD) || outputSchema.equals(Namespaces.GFC)) {
            mode = ISO_19115;
        } else if (outputSchema.equals(EBRIM_30) || outputSchema.equals(EBRIM_25)) {
            mode = EBRIM;
        } else if (outputSchema.equals(Namespaces.CSW)) {
            mode = DUBLINCORE;
        } else {
            throw new IllegalArgumentException("undefined outputSchema");
        }

        // we return only the number of result matching
        if (resultType.equals(ResultType.HITS)) {
            searchResults = CswXmlFactory.createSearchResults("2.0.2", id, set, nbResults, nextRecord);

        // we return a list of Record
        } else if (resultType.equals(ResultType.RESULTS)) {

            final List<AbstractRecord> abstractRecords = new ArrayList<AbstractRecord>();
            final List<Object> records                 = new ArrayList<Object>();
            try {
                for (int i = startPos -1; i < max; i++) {
                    final Object obj = mdReader.getMetadata(results[i], mode, set, elementName);
                    if (obj == null && (max + 1) < nbResults) {
                        max++;

                    } else if (obj != null) {
                        if (mode == DUBLINCORE) {
                            abstractRecords.add((AbstractRecord)obj);
                        } else {
                            records.add(obj);
                        }
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
                    abstractRecords.add((AbstractRecord) additionalResult);
                } else {
                    records.add(additionalResult);
                }
            }

            if (mode == DUBLINCORE) {
                searchResults = CswXmlFactory.createSearchResults("2.0.2",
                                                      id,
                                                      set,
                                                      totalMatched,
                                                      abstractRecords,
                                                      null,
                                                      abstractRecords.size(),
                                                      nextRecord);
            } else {
                searchResults = CswXmlFactory.createSearchResults("2.0.2",
                                                      id,
                                                      set,
                                                      totalMatched,
                                                      null,
                                                      records,
                                                      records.size(),
                                                      nextRecord);
            }

            //we return an Acknowledgement if the request is valid.
        } else if (resultType.equals(ResultType.VALIDATE)) {
            return CswXmlFactory.createAcknowledgement(version, id, request, System.currentTimeMillis());
        }

        GetRecordsResponse response = CswXmlFactory.createGetRecordsResponse(request.getVersion().toString(), id, System.currentTimeMillis(), searchResults);
        LOGGER.log(logLevel, "GetRecords request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return response;
    }

    /**
     * Execute a Lucene spatial query and return the result as a List of form identifier (form_ID:CatalogCode)
     *
     * @param query
     * @return
     * @throws CstlServiceException
     */
    private String[] executeLuceneQuery(final SpatialQuery query) throws CstlServiceException {
        LOGGER.log(Level.FINE, "Lucene query obtained:{0}", query);
        try {
            final Set<String> results = indexSearcher.doSearch(query);
            return results.toArray(new String[results.size()]);

        } catch (SearchingException ex) {
            throw new CstlServiceException("The service has throw an exception while making identifier lucene request", ex,
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
     * Add the convertible typeName to the list.
     * Example : MD_Metadata can be converted to a csw:Record
     * @param typeNames
     * @return 
     */
    private List<QName> getConvertibleTypeNames(final List<QName> typeNames) {
        final List<QName> result = new ArrayList<QName>();
        for (QName typeName : typeNames) {
            if (typeName.equals(RECORD_QNAME) && !result.contains(METADATA_QNAME)) {
                result.add(METADATA_QNAME);
            }
            result.add(typeName);
        }
        return result;
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

        final String version   = request.getVersion().toString();
        final String userLogin = getUserLogin();
        
        // we initialize the output format of the response
        initializeOutputFormat(request);

        // we get the level of the record to return (Brief, summary, full)
        ElementSetType set = ElementSetType.SUMMARY;
        if (request.getElementSetName() != null && request.getElementSetName().getValue() != null) {
            set = request.getElementSetName().getValue();
        }

        //we get the output schema and verify that we handle it
        String outputSchema = Namespaces.CSW;
        if (request.getOutputSchema() != null) {
            outputSchema = request.getOutputSchema();
            if (!acceptedResourceType.contains(outputSchema)) {
                throw new CstlServiceException("The server does not support this output schema: " + outputSchema,
                                                  INVALID_PARAMETER_VALUE, OUTPUT_SCHEMA);
            }
        }

        if (request.getId().isEmpty()){
            throw new CstlServiceException("You must specify at least one identifier", MISSING_PARAMETER_VALUE, "id");
        }

        //we begin to build the result
        GetRecordByIdResponse response;
        final List<String> unexistingID    = new ArrayList<String>();
        final List<AbstractRecord> records = new ArrayList<AbstractRecord>();
        final List<Object> otherRecords    = new ArrayList<Object>();

        final Class expectedType;
        final int mode;
        if (outputSchema.equals(Namespaces.CSW)) {
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

            final String saved = id;
            id = executeIdentifierQuery(id);
            if (id == null || !securityFilter.allowed(userLogin, id)) {
                unexistingID.add(saved);
                LOGGER.log(Level.WARNING, "unexisting id:{0}", saved);
                continue;
            }

            //we get the metadata object
            try {
                final Object o = mdReader.getMetadata(id, mode, set, null);
                if (o != null) {
                    if (expectedType != null && !expectedType.isInstance(o)) {
                        LOGGER.severe("The record " + id + " is not a " + expectedType.getSimpleName() + "object.");
                        continue;
                    }
                    if (mode == DUBLINCORE) {
                        records.add((AbstractRecordType)o);
                    } else {
                        otherRecords.add(o);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "The record {0} has not be read is null.", id);
                }
            } catch (MetadataIoException ex) {
                CodeList exceptionCode = ex.getExceptionCode();
                if (exceptionCode == null) {
                    exceptionCode = NO_APPLICABLE_CODE;
                }
                throw new CstlServiceException(ex, exceptionCode);
            }
        }

        if (records.isEmpty() && otherRecords.isEmpty()) {
            throwUnexistingIdentifierException(unexistingID);
        }

        response = CswXmlFactory.createGetRecordByIdResponse(version, records, otherRecords);
        LOGGER.log(logLevel, "GetRecordById request processed in {0} ms", (System.currentTimeMillis() - startTime));
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
     * Return one or more xsd schemas corresponding to the metadata records.
     *
     * @param request
     * @return
     */
    public DescribeRecordResponse describeRecord(final DescribeRecord request) throws CstlServiceException{
        LOGGER.log(logLevel, "DescribeRecords request processing\n");
        final long startTime = System.currentTimeMillis();

        verifyBaseRequest(request);

        // we initialize the output format of the response
        initializeOutputFormat(request);

        final String version = request.getVersion().toString();
        
        // we initialize the type names
        List<QName> typeNames = (List<QName>)request.getTypeName();
        if (typeNames == null || typeNames.isEmpty()) {
            typeNames = supportedTypeNames;
        }

        // we initialize the schema language
        String schemaLanguage = request.getSchemaLanguage();
        if (schemaLanguage == null) {
            schemaLanguage = "http://www.w3.org/XML/Schema";

        } else if (!supportedSchemaLanguage.contains(schemaLanguage)){

            String supportedList = "";
            for (String s : supportedSchemaLanguage) {
                supportedList = s + '\n';
            }
            throw new CstlServiceException("The server does not support this schema language: " + schemaLanguage +
                                           "\nsupported ones are:\n" + supportedList,
                                          INVALID_PARAMETER_VALUE, "schemaLanguage");
        }
        final List<SchemaComponent> components   = new ArrayList<SchemaComponent>();

        if (typeNames.contains(RECORD_QNAME)) {
            final Object object = schemas.get(RECORD_QNAME);
            final SchemaComponent component = CswXmlFactory.createSchemaComponent(version, Namespaces.CSW, schemaLanguage, object);
            components.add(component);
        }

        if (typeNames.contains(METADATA_QNAME)) {
            final Object object = schemas.get(METADATA_QNAME);
            final SchemaComponent component = CswXmlFactory.createSchemaComponent(version, Namespaces.GMD, schemaLanguage, object);
            components.add(component);
        }

        if (containsOneOfEbrim30(typeNames)) {
            final Object object = schemas.get(EXTRINSIC_OBJECT_QNAME);
            final SchemaComponent component = CswXmlFactory.createSchemaComponent(version, EBRIM_30, schemaLanguage, object);
            components.add(component);
        }

        if (containsOneOfEbrim25(typeNames)) {
            final Object object = schemas.get(EXTRINSIC_OBJECT_25_QNAME);
            final SchemaComponent component = CswXmlFactory.createSchemaComponent(version, EBRIM_25, schemaLanguage, object);
            components.add(component);
        }

        LOGGER.log(logLevel, "DescribeRecords request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return CswXmlFactory.createDescribeRecordResponse(version, components);
    }

    /**
     * Return a list / range of values for the specified property.
     * The property can be a parameter of the GetCapabilities document or
     * a property of the metadata.
     *
     * @param request
     * @return
     */
    public GetDomainResponse getDomain(final GetDomain request) throws CstlServiceException{
        LOGGER.log(logLevel, "GetDomain request processing\n");
        final long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        final String currentVersion = request.getVersion().toString();
        
        // we prepare the response
       final  List<DomainValues> responseList;

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
            responseList = new ArrayList<DomainValues>();
            final StringTokenizer tokens = new StringTokenizer(parameterName, ",");
            while (tokens.hasMoreTokens()) {
                final String token      = tokens.nextToken().trim();
                final int pointLocation = token.indexOf('.');
                if (pointLocation != -1) {

                    final String operationName = token.substring(0, pointLocation);
                    final String parameter     = token.substring(pointLocation + 1);
                    final AbstractOperation o  = OPERATIONS_METADATA.getOperation(operationName);
                    if (o != null) {
                        final AbstractDomain param = o.getParameterIgnoreCase(parameter);
                        QName type;
                        if ("GetCapabilities".equals(operationName)) {
                            type = CAPABILITIES_QNAME;
                        } else {
                            type = RECORD_QNAME;
                        }
                        if (param != null) {
                            final DomainValues value = CswXmlFactory.getDomainValues(currentVersion, token, null, param.getValue(), type);
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
        LOGGER.log(logLevel, "GetDomain request processed in {0} ms", (System.currentTimeMillis() - startTime));

        return CswXmlFactory.getDomainResponse(currentVersion, responseList);
    }

    /**
     * A web service method allowing to Insert / update / delete record from the CSW.
     *
     * @param request
     * @return
     */
    public TransactionResponse transaction(final Transaction request) throws CstlServiceException {
        LOGGER.log(logLevel, "Transaction request processing\n");

        if (profile == DISCOVERY) {
            throw new CstlServiceException("This method is not supported by this mode of CSW",
                                          OPERATION_NOT_SUPPORTED, "Request");
        }
        if (shiroAccessible && isTransactionSecurized() && !SecurityManager.isAuthenticated()) {
            throw new UnauthorizedException("You must be authentified to perform a transaction request.");
        }
        final long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        
        final String version = request.getVersion().toString();
        
        // we prepare the report
        int totalInserted       = 0;
        int totalUpdated        = 0;
        int totalDeleted        = 0;
        final String requestID  = request.getRequestId();

        final List<Object> transactions = request.getInsertOrUpdateOrDelete();
        for (Object transaction: transactions) {
            if (transaction instanceof Insert) {
                final Insert insertRequest = (Insert)transaction;

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

            } else if (transaction instanceof Delete) {
                if (mdWriter.deleteSupported()) {
                    final Delete deleteRequest = (Delete)transaction;
                    if (deleteRequest.getConstraint() == null) {
                        throw new CstlServiceException("A constraint must be specified.",
                                                      MISSING_PARAMETER_VALUE, "constraint");
                    }
                    final List<QName> typeNames = new ArrayList<QName>();
                    final String dataType = deleteRequest.getTypeName();
                    if (dataType != null && !dataType.isEmpty()) {
                        try {
                            typeNames.add(TypeNames.valueOf(dataType));
                        } catch (IllegalArgumentException ex) {
                            throw new CstlServiceException("Unexpected value for typeName:" + dataType, INVALID_PARAMETER_VALUE, "typeName");
                        }
                    }
                    // build the lucene query from the specified filter
                    final SpatialQuery luceneQuery;
                    try {
                        luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(deleteRequest.getConstraint(), null, null, getConvertibleTypeNames(typeNames));
                    } catch (FilterParserException ex) {
                        throw new CstlServiceException(ex.getMessage(), ex, ex.getExceptionCode(), ex.getLocator());
                    }
                    // we try to execute the query
                    final String[] results = executeLuceneQuery(luceneQuery);

                    try {
                        for (String metadataID : results) {
                            final boolean deleted = mdWriter.deleteMetadata(metadataID);
                            mdReader.removeFromCache(metadataID);
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


            } else if (transaction instanceof Update) {
                if (mdWriter.updateSupported()) {
                    final Update updateRequest = (Update) transaction;
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
                    
                    final List<QName> typeNames = new ArrayList<QName>();
                    // build the lucene query from the specified filter
                    final SpatialQuery luceneQuery;
                    try {
                        luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(updateRequest.getConstraint(), null, null, getConvertibleTypeNames(typeNames));
                    } catch (FilterParserException ex) {
                        throw new CstlServiceException(ex.getMessage(), ex, ex.getExceptionCode(), ex.getLocator());
                    }

                    // we try to execute the query
                    try {
                        final String[] results = executeLuceneQuery(luceneQuery);
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
        final TransactionSummary summary = CswXmlFactory.createTransactionSummary(version, totalInserted, totalUpdated, totalDeleted, requestID);
        
        final TransactionResponse response = CswXmlFactory.createTransactionResponse(version, summary, null);
        LOGGER.log(logLevel, "Transaction request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return response;
    }

    /**
     * TODO
     *
     * @param request
     * @return
     */
    public HarvestResponse harvest(final Harvest request) throws CstlServiceException {
        LOGGER.log(logLevel, "Harvest request processing\n");
        if (profile == DISCOVERY) {
            throw new CstlServiceException("This method is not supported by this mode of CSW",
                                          OPERATION_NOT_SUPPORTED, "Request");
        }
        if (isTransactionSecurized() && !SecurityManager.isAuthenticated()) {
            throw new UnauthorizedException("You must be authentified to perform a harvest request.");
        }
        verifyBaseRequest(request);
        final String version = request.getVersion().toString();
        
        HarvestResponse response;
        // we prepare the report
        final int totalInserted;
        final int totalUpdated;
        final int totalDeleted;

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
                    final TransactionSummary summary = CswXmlFactory.createTransactionSummary(version, totalInserted, totalUpdated, totalDeleted, null);
                    final TransactionResponse transactionResponse = CswXmlFactory.createTransactionResponse(version, summary, null);
                    response = CswXmlFactory.createHarvestResponse(version, transactionResponse);

                //mode asynchronous
                } else {

                    final Acknowledgement acknowledgement = CswXmlFactory.createAcknowledgement(version, null, request, System.currentTimeMillis());
                    response = CswXmlFactory.createHarvestResponse(version, acknowledgement);
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
     *
     * @deprecated Thread unsafe todo replace.
     */
    @Deprecated
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
                                                  INVALID_PARAMETER_VALUE, SERVICE_PARAMETER);
                }
            } else {
                throw new CstlServiceException("service must be specified!",
                                              MISSING_PARAMETER_VALUE, SERVICE_PARAMETER);
            }
            if (request.getVersion()!= null) {
                /*
                 * Ugly patch to begin to support CSW 2.0.0 request
                 *
                 * TODO remove this
                 */
                if (request.getVersion().toString().equals(CSW_202_VERSION)) {
                    request.setVersion(ServiceDef.CSW_2_0_2.version.toString());
                } else if (request.getVersion().toString().equals("2.0.0") && (request instanceof GetDomain)) {
                    request.setVersion(ServiceDef.CSW_2_0_0.version.toString());

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
     * {@inheritDoc }
     */
    @Override
    public void destroy() {
        super.destroy();
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

    /**
     * {@inheritDoc }
     */
    @Override
    public final void setLogLevel(Level logLevel) {
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

    /**
     * {@inheritDoc }
     */
    @Override
    protected MarshallerPool getMarshallerPool() {
        return CSWMarshallerPool.getInstance();
    }
    
    public void clearCache() throws CstlServiceException {
        try {
            indexSearcher.refresh();
            mdReader.clearCache();
        } catch (IndexingException ex) {
            throw new CstlServiceException("Error while refreshing cache", ex, NO_APPLICABLE_CODE);
        }
    }

   @Override
    protected String getProperty(final String key) {
        if (configuration != null) {
            return configuration.getParameter(key);
        }
        return null;
    }
}
