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
package org.constellation.metadata;

// J2SE dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

// W3C DOM dependecies
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

//Constellation dependencies
import org.constellation.ebrim.v300.RegistryObjectType;
import org.constellation.ebrim.v300.InternationalStringType;
import org.constellation.cat.csw.v202.AbstractRecordType;
import org.constellation.cat.csw.v202.AcknowledgementType;
import org.constellation.cat.csw.v202.BriefRecordType;
import org.constellation.cat.csw.v202.Capabilities;
import org.constellation.cat.csw.v202.DeleteType;
import org.constellation.cat.csw.v202.DescribeRecordResponseType;
import org.constellation.cat.csw.v202.DescribeRecordType;
import org.constellation.cat.csw.v202.DomainValuesType;
import org.constellation.cat.csw.v202.ElementSetNameType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.GetCapabilities;
import org.constellation.cat.csw.v202.GetDomainResponseType;
import org.constellation.cat.csw.v202.GetDomainType;
import org.constellation.cat.csw.v202.GetRecordByIdResponseType;
import org.constellation.cat.csw.v202.GetRecordByIdType;
import org.constellation.cat.csw.v202.GetRecordsResponseType;
import org.constellation.cat.csw.v202.GetRecordsType;
import org.constellation.cat.csw.v202.HarvestResponseType;
import org.constellation.cat.csw.v202.HarvestType;
import org.constellation.cat.csw.v202.InsertType;
import org.constellation.cat.csw.v202.ListOfValuesType;
import org.constellation.cat.csw.v202.ObjectFactory;
import org.constellation.cat.csw.v202.QueryType;
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.cat.csw.v202.RequestBaseType;
import org.constellation.cat.csw.v202.ResultType;
import org.constellation.cat.csw.v202.SearchResultsType;
import org.constellation.cat.csw.v202.SummaryRecordType;
import org.constellation.cat.csw.v202.TransactionResponseType;
import org.constellation.cat.csw.v202.TransactionSummaryType;
import org.constellation.cat.csw.v202.TransactionType;
import org.constellation.cat.csw.v202.UpdateType;
import org.constellation.cat.csw.v202.SchemaComponentType;
import org.constellation.cat.csw.v202.EchoedRequestType;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.dublincore.AbstractSimpleLiteral;
import org.constellation.filter.LuceneFilterParser;
import org.constellation.lucene.filter.SpatialQuery;
import org.constellation.ogc.FilterCapabilities;
import org.constellation.ogc.SortByType;
import org.constellation.ogc.SortPropertyType;
import org.constellation.ows.v100.AcceptFormatsType;
import org.constellation.ows.v100.AcceptVersionsType;
import org.constellation.ows.v100.DomainType;
import org.constellation.ows.v100.OWSWebServiceException;
import org.constellation.ows.v100.Operation;
import org.constellation.ows.v100.OperationsMetadata;
import org.constellation.ows.v100.SectionsType;
import org.constellation.ows.v100.ServiceIdentification;
import org.constellation.ows.v100.ServiceProvider;
import org.constellation.ws.rs.WebService;
import org.constellation.ebrim.v300.IdentifiableType;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.metadata.io.MetadataReader.*;
import static org.constellation.metadata.CSWQueryable.*;

// Apache Lucene dependencies
import org.apache.lucene.index.Term;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Sort;

//geotools dependencies
import org.geotools.metadata.iso.MetaDataImpl;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

//mdweb model dependencies
import org.constellation.filter.FilterParser;
import org.constellation.filter.SQLFilterParser;
import org.constellation.filter.SQLQuery;
import org.constellation.metadata.io.GenericMetadataReader;
import org.constellation.metadata.io.MDWebMetadataReader;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.rs.NamespacePrefixMapperImpl;
import org.mdweb.model.schemas.Standard; 
import org.mdweb.model.storage.Form; 
import org.mdweb.sql.v20.Reader20; 
import org.mdweb.sql.v20.Writer20;

// GeoAPI dependencies
import org.opengis.filter.sort.SortOrder;
import org.opengis.metadata.identification.Identification;

// PostgreSQL dependencies
import org.postgresql.ds.PGSimpleDataSource;


/**
 *
 * @author Guilhem Legal
 */
public class CSWworker {

    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("org.constellation.metadata");
    
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
     * A connection to the Database
     */
    private Connection MDConnection;
    
    /**
     * An object creator from the MDWeb database.
     */
    private MetadataReader MDReader;
    
    /**
     * An Form creator from the MDWeb database.
     */
    private MetadataWriter MDWriter;
    
    /**
     * A JAXB factory to csw object version 2.0.2
     */
    protected final ObjectFactory cswFactory202;
    
    /**
     * A JAXB factory to csw object version 2.0.0 
     */
    protected final org.constellation.cat.csw.v200.ObjectFactory cswFactory200;
    
    /**
     * The current MIME type of return
     */
    private String outputFormat;
    
    /**
     * A unMarshaller to get object from harvested resource.
     */
    protected final Unmarshaller unmarshaller;
    
    /**
     * A Marshaller to send request to another CSW services.
     */
    protected final Marshaller marshaller;
    
    /**
     * A lucene index to make quick search on the metadatas.
     */
    private  IndexLucene index;
    
    /**
     * A filter parser whitch create lucene query from OGC filter
     */
    private final FilterParser luceneFilterParser;
    
    /**
     * A filter parser whitch create SQL query from OGC filter (used for ebrim query)
     */
    private final FilterParser sqlFilterParser;
    
    /**
     * A flag indicating if the worker is correctly started.
     */
    private boolean isStarted;
    
    /**
     * A catalogue Harvester comunicating with other CSW 
     */
    private CatalogueHarvester catalogueHarvester;
    
    /**
     * Used to map a prefix with a namespace URI.
     */
    private NamespacePrefixMapperImpl prefixMapper;
    
    /**
     * A list of the supported Type name 
     */
    private final static List<QName> SUPPORTED_TYPE_NAME;
    static {
        SUPPORTED_TYPE_NAME = new ArrayList<QName>();
        //dublin core typeNames
        SUPPORTED_TYPE_NAME.add(_Record_QNAME);
        //iso 19115 typeNames
        SUPPORTED_TYPE_NAME.add(_Metadata_QNAME);
        //ebrim v3.0 typeNames
        SUPPORTED_TYPE_NAME.add(_AdhocQuery_QNAME);
        SUPPORTED_TYPE_NAME.add(_Association_QNAME);
        SUPPORTED_TYPE_NAME.add(_AuditableEvent_QNAME);
        SUPPORTED_TYPE_NAME.add(_ClassificationNode_QNAME);
        SUPPORTED_TYPE_NAME.add(_ClassificationScheme_QNAME);
        SUPPORTED_TYPE_NAME.add(_Classification_QNAME);
        SUPPORTED_TYPE_NAME.add(_ExternalIdentifier_QNAME);
        SUPPORTED_TYPE_NAME.add(_ExternalLink_QNAME);
        SUPPORTED_TYPE_NAME.add(_ExtrinsicObject_QNAME);
        SUPPORTED_TYPE_NAME.add(_Federation_QNAME);
        SUPPORTED_TYPE_NAME.add(_Notification_QNAME);
        SUPPORTED_TYPE_NAME.add(_ObjectRefList_QNAME);
        SUPPORTED_TYPE_NAME.add(_Person_QNAME);
        SUPPORTED_TYPE_NAME.add(_Organization_QNAME);
        SUPPORTED_TYPE_NAME.add(_RegistryObject_QNAME);
        SUPPORTED_TYPE_NAME.add(_RegistryPackage_QNAME);
        SUPPORTED_TYPE_NAME.add(_Registry_QNAME);
        SUPPORTED_TYPE_NAME.add(_ServiceBinding_QNAME);
        SUPPORTED_TYPE_NAME.add(_Service_QNAME);
        SUPPORTED_TYPE_NAME.add(_SpecificationLink_QNAME);
        SUPPORTED_TYPE_NAME.add(_Subscription_QNAME);
        SUPPORTED_TYPE_NAME.add(_User_QNAME);
        SUPPORTED_TYPE_NAME.add(_WRSExtrinsicObject_QNAME);
        //ebrim v2.5 typenames
        SUPPORTED_TYPE_NAME.add(_ExtrinsicObject25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Federation25_QNAME);
        SUPPORTED_TYPE_NAME.add(_ExternalLink25_QNAME);
        SUPPORTED_TYPE_NAME.add(_ClassificationNode25_QNAME);
        SUPPORTED_TYPE_NAME.add(_User25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Classification25_QNAME);
        SUPPORTED_TYPE_NAME.add(_RegistryPackage25_QNAME);
        SUPPORTED_TYPE_NAME.add(_RegistryObject25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Association25_QNAME);
        SUPPORTED_TYPE_NAME.add(_RegistryEntry25_QNAME);
        SUPPORTED_TYPE_NAME.add(_ClassificationScheme25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Organization25_QNAME);
        SUPPORTED_TYPE_NAME.add(_ExternalIdentifier25_QNAME);
        SUPPORTED_TYPE_NAME.add(_SpecificationLink25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Registry25_QNAME);
        SUPPORTED_TYPE_NAME.add(_ServiceBinding25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Service25_QNAME);
        SUPPORTED_TYPE_NAME.add(_AuditableEvent25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Subscription25_QNAME);
        SUPPORTED_TYPE_NAME.add(_Geometry09_QNAME);
        SUPPORTED_TYPE_NAME.add(_ApplicationModule09_QNAME);
        SUPPORTED_TYPE_NAME.add(_WRSExtrinsicObject09_QNAME);
    }
    
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
    private final static List<String> ACCEPTED_RESOURCE_TYPE;
    static {
        ACCEPTED_RESOURCE_TYPE = new ArrayList<String>();
        ACCEPTED_RESOURCE_TYPE.add("http://www.isotc211.org/2005/gmd");
        ACCEPTED_RESOURCE_TYPE.add("http://www.isotc211.org/2005/gfc");
        ACCEPTED_RESOURCE_TYPE.add("http://www.opengis.net/cat/csw/2.0.2");
        ACCEPTED_RESOURCE_TYPE.add("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
        ACCEPTED_RESOURCE_TYPE.add("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5");
    }
    
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
     * Build a new CSW worker
     * 
     * @param marshaller A JAXB marshaller to send xml to MDWeb
     * @param unmarshaller  An Unmarshaller to get object from harvested resource.
     * 
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public CSWworker(Unmarshaller unmarshaller, Marshaller marshaller) throws IOException {
        
        this.unmarshaller = unmarshaller;
        this.marshaller   = marshaller; 
        prefixMapper      = new NamespacePrefixMapperImpl("");
        cswFactory202     = new ObjectFactory();
        cswFactory200     = new org.constellation.cat.csw.v200.ObjectFactory();
        Properties prop   = new Properties();
        Properties cascad = new Properties();
        File f            = null;
        String home       = System.getProperty("user.home");
        File env          = new File(home, ".sicade/csw_configuration/");
        logger.info("Path to config file=" + env);
        isStarted = true;
        try {
            // we get the configuration file
            f = new File(env, "config.properties");
            FileInputStream in = new FileInputStream(f);
            prop.load(in);
            in.close();
            
        } catch (FileNotFoundException e) {
            if (f != null) {
                logger.severe(f.getPath());
            }
            logger.severe("The CSW service is not working!"                       + '\n' + 
                          "cause: The service can not load the properties files!" + '\n' + 
                          "cause: " + e.getMessage());
            isStarted = false;
        }
        
        cascadedCSWservers = new ArrayList<String>();
        try {
            // we get the cascading configuration file
            f = new File(env, "CSWCascading.properties");
            FileInputStream in = new FileInputStream(f);
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
        }
        
        // we initialize the filterParsers
        luceneFilterParser = new LuceneFilterParser(version);
        sqlFilterParser    = new SQLFilterParser(version);
        
        
        //we create a connection to the metadata database
        if (isStarted) {
            String databaseType = prop.getProperty("DBType");
            
            // if The database is unknow to the service we use the generic metadata reader.
            if (databaseType != null && databaseType.equals("generic")) {
                
                profile = DISCOVERY;
                
                // TODO make it generic for any Database Type
                PGSimpleDataSource dataSourceMD = new PGSimpleDataSource();
                dataSourceMD.setServerName(prop.getProperty("MDDBServerName"));
                dataSourceMD.setPortNumber(Integer.parseInt(prop.getProperty("MDDBServerPort")));
                dataSourceMD.setDatabaseName(prop.getProperty("MDDBName"));
                dataSourceMD.setUser(prop.getProperty("MDDBUser"));
                dataSourceMD.setPassword(prop.getProperty("MDDBUserPassword"));
                try {
                    MDConnection    = dataSourceMD.getConnection();
                } catch (SQLException e) {
                    MDConnection = null;
                    logger.severe(e.getMessage());
                }
                if (MDConnection == null) {
                    logger.severe("The CSW service is not working!" + '\n' + 
                                  "cause: The web service can't connect to the generic metadata database!");
                    isStarted = false;
                } else {
                 
                    try {
                        MDReader                = new GenericMetadataReader();
                        index                   = new IndexLucene((GenericMetadataReader)MDReader, env);
                        //in generic mode there is no transactionnal part.
                        MDWriter                = null;
                        catalogueHarvester      = null;
                 
                        logger.info("CSW service (Generic database) running");
                    } catch (SQLException e) {
                        logger.severe(e.getMessage());
                        logger.severe("The CSW service is not working!" + '\n' + 
                                  "cause: The web service can't connect to the generic metadata database!");
                    }
                }
            
            // else we use the defaut database mode: MDWeb.
            } else {
                logger.info("Using default database type: MDWeb");
            
                profile = TRANSACTIONAL;
                
                PGSimpleDataSource dataSourceMD = new PGSimpleDataSource();
                dataSourceMD.setServerName(prop.getProperty("MDDBServerName"));
                dataSourceMD.setPortNumber(Integer.parseInt(prop.getProperty("MDDBServerPort")));
                dataSourceMD.setDatabaseName(prop.getProperty("MDDBName"));
                dataSourceMD.setUser(prop.getProperty("MDDBUser"));
                dataSourceMD.setPassword(prop.getProperty("MDDBUserPassword"));
                try {
                    MDConnection    = dataSourceMD.getConnection();
                } catch (SQLException e) {
                    MDConnection = null;
                    logger.severe(e.getMessage());
                }
                if (MDConnection == null) {
                    logger.severe("The CSW service is not working!" + '\n' + 
                                  "cause: The web service can't connect to the MDWeb metadata database!");
                    isStarted = false;
                } else {
                 
                    try {
                        Reader20 databaseReader = new Reader20(Standard.ISO_19115,  MDConnection);
                        Writer20 databaseWriter = new Writer20(MDConnection);
                        index                   = new IndexLucene(databaseReader, env);
                        MDReader                = new MDWebMetadataReader(databaseReader);
                        MDWriter                = new MetadataWriter(databaseReader, databaseWriter);
                        catalogueHarvester      = new CatalogueHarvester(this);
                 
                        logger.info("CSW service (MDweb database) running");
                    } catch (SQLException e) {
                        logger.severe(e.getMessage());
                        logger.severe("The CSW service is not working!" + '\n' + 
                                  "cause: The web service can't connect to the MDWeb metadata database!");
                    }
                }
            }
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
        long startTime = System.currentTimeMillis();
        
        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals("CSW")) {
                throw new OWSWebServiceException("service must be \"CSW\"!",
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
            if (!versions.getVersion().contains("2.0.2")){
                 throw new OWSWebServiceException("version available : 2.0.2",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion",
                                             version);
            }
        }
        AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 && !formats.getOutputFormat().contains("text/xml")) {
            /*
             * Acording to the CITE test this case does not return an exception
             throw new OWSWebServiceException("accepted format : text/xml",
                                             INVALID_PARAMETER_VALUE, "acceptFormats",
                                             version);
             */ 
        }
        
        //we prepare the response document
        Capabilities c = null; 
        
        ServiceIdentification si = null;
        ServiceProvider       sp = null;
        OperationsMetadata    om = null;
        FilterCapabilities    fc = null;
            
        SectionsType sections = requestCapabilities.getSections();
        
        //according to CITE test a GetCapabilities must always return Filter_Capabilities
        if (!sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains("All"))
            sections.add("Filter_Capabilities");
        
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
                om.removeOperation("Harvest");
                om.removeOperation("Transaction");
            }
            
            //we update the URL
            if (om != null) {
                WebService.updateOWSURL(om.getOperation(), serviceURL, "CSW");
                
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
                
                
                //we update the operation parameters 
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
                            values.add(prefixMapper.getPreferredPrefix(qn.getNamespaceURI(), "", true) + ':' + qn.getLocalPart());
                        }
                        tn.setValue(values);
                    }
                    
                    //we update the queryable elements :
                    DomainType isoQ = gr.getConstraint("SupportedISOQueryables");
                    if (isoQ != null) {
                        List<String> values = new ArrayList<String>();
                        for (String name : ISO_QUERYABLE.keySet() ) {
                            values.add("apiso:" + name);
                        }
                        isoQ.setValue(values);
                    }
                    //we update the queryable elements :
                    DomainType dubQ = gr.getConstraint("SupportedDublinCoreQueryables");
                    if (dubQ != null) {
                        List<String> values = new ArrayList<String>();
                        for (String name : DUBLIN_CORE_QUERYABLE.keySet() ) {
                            values.add("dc:" + name);
                        }
                        dubQ.setValue(values);
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
                            values.add(prefixMapper.getPreferredPrefix(qn.getNamespaceURI(), "", true) + ':' + qn.getLocalPart());
                        }
                        tn.setValue(values);
                    }
                }
            }
        }
            
        //we enter the information filter capablities.
        if (sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains("All")) {
            
            fc = staticCapabilities.getFilterCapabilities();
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
    public Object getRecords(GetRecordsType request) throws WebServiceException {
        logger.info("GetRecords request processing" + '\n');
        long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        
        //we prepare the response
        GetRecordsResponseType response;
        
        String ID = request.getRequestId();
        
        // we initialize the output format of the response
        String format = request.getOutputFormat();
        if (format != null && isSupportedFormat(format)) {
            outputFormat = format;
        } else if (format != null && !isSupportedFormat(format)) {
            String supportedFormat = "";
            for (String s: ACCEPTED_OUTPUT_FORMATS) {
                supportedFormat = supportedFormat  + s + '\n';
            } 
            throw new OWSWebServiceException("The server does not support this output format: " + format + '\n' +
                                             " supported ones are: " + '\n' + supportedFormat,
                                             INVALID_PARAMETER_VALUE, "outputFormat", version);
        }
        
        //we get the output schema and verify that we handle it
        String outputSchema = "http://www.opengis.net/cat/csw/2.0.2";
        if (request.getOutputSchema() != null) {
            outputSchema = request.getOutputSchema();
            if (!ACCEPTED_RESOURCE_TYPE.contains(outputSchema)) {
                String supportedOutput = "";
                for (String s: ACCEPTED_RESOURCE_TYPE) {
                    supportedOutput = supportedOutput  + s + '\n';
                } 
                throw new OWSWebServiceException("The server does not support this output schema: " + outputSchema + '\n' +
                                                 " supported ones are: " + '\n' + supportedOutput,
                                                  INVALID_PARAMETER_VALUE, "outputSchema", version);
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
        Map<String, QName> variables = new HashMap<String, QName>();
        Map<String, String> prefixs  = new HashMap<String, String>();
        if (request.getAbstractQuery() != null) {
            query = (QueryType)request.getAbstractQuery();
            typeNames =  query.getTypeNames();
            if (typeNames == null || typeNames.size() == 0) {
                throw new OWSWebServiceException("The query must specify at least typeName.",
                                                 INVALID_PARAMETER_VALUE, "TypeNames", version);
            } else {
                for (QName type:typeNames) {
                    prefixs.put(type.getPrefix(), type.getNamespaceURI());
                    //for ebrim mode the user can put variable after the Qname
                    if (type.getLocalPart().indexOf('_') != -1 && !type.getLocalPart().startsWith("MD")) {
                        StringTokenizer tokenizer = new StringTokenizer(type.getLocalPart(), "_;");
                        type = new QName(type.getNamespaceURI(), tokenizer.nextToken());
                        while (tokenizer.hasMoreTokens()) {
                            variables.put(tokenizer.nextToken(), type);
                        }
                    }
                    //we verify that the typeName is supported        
                    if (!SUPPORTED_TYPE_NAME.contains(type)) {
                        String typeName = "null";
                        if (type != null)
                            typeName = type.getLocalPart();
                        throw new OWSWebServiceException("The typeName " + typeName + " is not supported by the service:" +'\n' +
                                                         "supported one are:" + '\n' + supportedTypeNames(),
                                                         INVALID_PARAMETER_VALUE, "TypeNames", version);
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
            throw new OWSWebServiceException("The request must contains a query.",
                                             INVALID_PARAMETER_VALUE, "Query", version);
        }
        
        // we get the element set type (BRIEF, SUMMARY OR FULL)
        ElementSetNameType setName = query.getElementSetName();
        ElementSetType set         = ElementSetType.SUMMARY;
        List<QName> elementName    = query.getElementName();
        if (setName != null) {
            set = setName.getValue();
        }
        
        SearchResultsType searchResults = null;
        
        //we get the maxRecords wanted and start position
        Integer maxRecord = request.getMaxRecords();
        Integer startPos  = request.getStartPosition();
        if (startPos <= 0) {
            throw new OWSWebServiceException("The start position must be > 0.",
                                             NO_APPLICABLE_CODE, "startPosition", version);
        }

        List<String> results;
        if (outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0") || outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5")) {
           
            // build the sql query from the specified filter
           SQLQuery sqlQuery = (SQLQuery) sqlFilterParser.getQuery(query.getConstraint(), variables, prefixs); 
           
           // TODO sort not yet implemented
           logger.info("ebrim SQL query obtained:" + sqlQuery);
           
           // we try to execute the query
           results = executeSQLQuery(sqlQuery);
            
        } else {
            
            // build the lucene query from the specified filter
            SpatialQuery luceneQuery = (SpatialQuery) luceneFilterParser.getQuery(query.getConstraint(), variables, prefixs);
        
            //we look for a sorting request (for now only one sort is used)
            SortByType sortBy = query.getSortBy();
            if (sortBy != null && sortBy.getSortProperty().size() > 0) {
                SortPropertyType first = sortBy.getSortProperty().get(0);
                if (first.getPropertyName() == null || first.getPropertyName().getPropertyName() == null || first.getPropertyName().getPropertyName().equals(""))
                    throw new OWSWebServiceException("A SortBy filter must specify a propertyName.",
                                                     NO_APPLICABLE_CODE, null, version);
                String propertyName = removePrefix(first.getPropertyName().getPropertyName()) + "_sort";
            
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
        CatalogueHarvester.DistributedResults distributedResults = catalogueHarvester.new DistributedResults();
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
        
        
        try {
            if (outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2")) {
            
                // we return only the number of result matching
                if (resultType.equals(ResultType.HITS)) {
                    searchResults = new SearchResultsType(ID, set, results.size(), nextRecord);
                
                // we return a list of Record
                } else if (resultType.equals(ResultType.RESULTS)) {
                
                    List<AbstractRecordType> records = new ArrayList<AbstractRecordType>();
                    
                    for (int i = startPos -1; i < max; i++) {
                        Object obj = MDReader.getMetadata(results.get(i), DUBLINCORE, set, elementName);
                        if (obj == null && (max + 1) < results.size()) {
                            max++;
                        
                        } else {
                            records.add((AbstractRecordType)obj);
                        }
                    }
                    
                    //we add additional distributed result
                    for (int i = 0; i < maxDistributed; i++) {
                        
                        Object additionalResult = distributedResults.additionalResults.get(i);
                        if (additionalResult instanceof AbstractRecordType) {
                            records.add((AbstractRecordType) additionalResult);
                        }
                    }
                    
                    searchResults = new SearchResultsType(ID, 
                                                          set, 
                                                          totalMatched,
                                                          records,
                                                          records.size(),
                                                          nextRecord);
                        
                //we return an Acknowledgement if the request is valid. 
                } else if (resultType.equals(ResultType.VALIDATE)) {
                   try {
                       EchoedRequestType echoRequest = new EchoedRequestType(request);
                       return new AcknowledgementType(ID, echoRequest, System.currentTimeMillis());
                    
                    } catch(DatatypeConfigurationException ex) {
                        throw new OWSWebServiceException("DataTypeConfiguration exception while creating acknowledgment response",
                                                         NO_APPLICABLE_CODE, null, version);
                    }
                }
                
            } else {
            
                int mode;
                if (outputSchema.equals("http://www.isotc211.org/2005/gmd") || outputSchema.equals("http://www.isotc211.org/2005/gfc")) {
                    mode = ISO_19115;
                } else if (outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0")  || outputSchema.equals("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5")) {
                    mode = EBRIM;
                } else {
                    mode = DUBLINCORE;
                }
                    
                // we return only the number of result matching
                if (resultType.equals(ResultType.HITS)) {
                    searchResults = new SearchResultsType(ID, query.getElementSetName().getValue(), results.size(), nextRecord);
                
                } else if (resultType.equals(ResultType.RESULTS)) {
                
                    List<Object> records = new ArrayList<Object>();
                    
                    for (int i = startPos -1; i < max; i++) {

                        Object obj = MDReader.getMetadata(results.get(i), mode, set, elementName);
                        if (obj == null && (max + 1) < results.size()) {
                            max++;
                        } else {
                            records.add(obj);
                        }
                    }
                    
                     //we add additional distributed result
                    for (int i = 0; i < maxDistributed; i++) {
                        
                        Object additionalResult = distributedResults.additionalResults.get(i);
                        records.add(additionalResult);
                    }
                    
                    searchResults = new SearchResultsType(ID, 
                                                          set, 
                                                          totalMatched,
                                                          records.size(),
                                                          records);
                    
                //we return an Acknowledgement if the request is valid.
                } else if (resultType.equals(ResultType.VALIDATE)) {
                    try {
                        EchoedRequestType echoRequest = new EchoedRequestType(request);
                        return new AcknowledgementType(ID, echoRequest, System.currentTimeMillis());
                    
                    } catch(DatatypeConfigurationException ex) {
                        throw new OWSWebServiceException("DataTypeConfiguration exception while creating acknowledgment response",
                                                         NO_APPLICABLE_CODE, null, version);
                    }
                }
        
            }
        } catch (SQLException ex) {
            throw new OWSWebServiceException("The service has throw an SQLException:" + ex.getMessage(),
                                              NO_APPLICABLE_CODE, null, version);
        }
        response = new GetRecordsResponseType(ID, System.currentTimeMillis(), version.toString(), searchResults);
        logger.info("GetRecords request processed in " + (System.currentTimeMillis() - startTime) + " ms");
        return response;
    }
    
    /**
     * Execute a Lucene spatial query and return the result as a List of form identifier (form_ID:CatalogCode)
     * 
     * @param query
     * @return
     * @throws org.constellation.ows.v100.OWSWebServiceException
     */
    private List<String> executeLuceneQuery(SpatialQuery query) throws OWSWebServiceException {
        try {
            return index.doSearch(query);
        
        } catch (CorruptIndexException ex) {
            throw new OWSWebServiceException("The service has throw an CorruptIndex exception. please rebuild the luncene index.",
                                             NO_APPLICABLE_CODE, null, version);
        } catch (IOException ex) {
            throw new OWSWebServiceException("The service has throw an IO exception while making lucene request.",
                                             NO_APPLICABLE_CODE, null, version);
        } catch (ParseException ex) {
            throw new OWSWebServiceException("The service has throw an Parse exception while making lucene request.",
                                             NO_APPLICABLE_CODE, null, version);
        }
    }
    
    /**
     * Execute a Lucene spatial query and return the result as a List of form identifier (form_ID:CatalogCode)
     * 
     * @param query
     * @return
     * @throws org.constellation.ows.v100.OWSWebServiceException
     */
    private List<String> executeLuceneQuery(TermQuery query) throws OWSWebServiceException {
        try {
            return index.doSearch(query);
        
        } catch (CorruptIndexException ex) {
            throw new OWSWebServiceException("The service has throw an CorruptIndex exception. please rebuild the luncene index.",
                                             NO_APPLICABLE_CODE, null, version);
        } catch (IOException ex) {
            throw new OWSWebServiceException("The service has throw an IO exception while making lucene request.",
                                             NO_APPLICABLE_CODE, null, version);
        } catch (ParseException ex) {
            throw new OWSWebServiceException("The service has throw an Parse exception while making lucene request.",
                                             NO_APPLICABLE_CODE, null, version);
        }
    }
    
    /**
     * Execute a SQL query and return the result as a List of form identifier (form_ID:CatalogCode)
     * 
     * @param query
     * @return
     * @throws org.constellation.ows.v100.OWSWebServiceException
     */
    private List<String> executeSQLQuery(SQLQuery query) throws OWSWebServiceException {
        try {
            List<String> results = new ArrayList<String>();
            Statement stmt = MDConnection.createStatement();
            ResultSet result = stmt.executeQuery(query.getQuery());
            while (result.next()) {
                results.add(result.getInt("identifier") + ":" + result.getString("catalog"));
            }
            return results;
        } catch (SQLException ex) {
           throw new OWSWebServiceException("The service has throw an SQL exception while making eberim request:" + '\n' +
                                            "Cause: " + ex.getMessage(), NO_APPLICABLE_CODE, null, version);
        }
    }
    
    /**
     * web service operation return one or more records specified by there identifier.
     * 
     * @param request
     * @return
     */
    public GetRecordByIdResponseType getRecordById(GetRecordByIdType request) throws WebServiceException {
        logger.info("GetRecordById request processing" + '\n');
        long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        
        // we initialize the output format of the response
        String format = request.getOutputFormat();
        if (format != null && isSupportedFormat(format)) {
            outputFormat = format;
        } else if (format != null && !isSupportedFormat(format)) {
            String supportedFormat = "";
            for (String s: ACCEPTED_OUTPUT_FORMATS) {
                supportedFormat = supportedFormat  + s + '\n';
            } 
            throw new OWSWebServiceException("The server does not support this output format: " + format + '\n' +
                                             " supported ones are: " + '\n' + supportedFormat,
                                             INVALID_PARAMETER_VALUE, "outputFormat", version);
        }
        
        
        // we get the level of the record to return (Brief, summary, full)
        ElementSetType set = ElementSetType.SUMMARY;
        if (request.getElementSetName() != null && request.getElementSetName().getValue() != null) {
            set = request.getElementSetName().getValue();
        }
        
        //we get the output schema and verify that we handle it
        String outputSchema = "http://www.opengis.net/cat/csw/2.0.2";
        if (request.getOutputSchema() != null) {
            outputSchema = request.getOutputSchema();
            if (!ACCEPTED_RESOURCE_TYPE.contains(outputSchema)) {
                throw new OWSWebServiceException("The server does not support this output schema: " + outputSchema,
                                                  INVALID_PARAMETER_VALUE, "outputSchema", version);
            }
        }
        
        if (request.getId().size() == 0)
            throw new OWSWebServiceException("You must specify at least one identifier",
                                              MISSING_PARAMETER_VALUE, "id", version);
        
        //we begin to build the result
        GetRecordByIdResponseType response;
        List<String> unexistingID = new ArrayList<String>();
        
        //we build dublin core object
        if (outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2")) {
            List<JAXBElement<? extends AbstractRecordType>> records = new ArrayList<JAXBElement<? extends AbstractRecordType>>(); 
            for (String id:request.getId()) {
                
                //we get the form ID and catalog code
                List<String> ids = executeLuceneQuery(new TermQuery(new Term("identifier_sort", id)));
                if (ids.size() > 0) {
                    id = ids.get(0);
                } else {
                    unexistingID.add(id);
                    logger.severe("unexisting id:" + id);
                    continue;
                }
                //we get the metadata object
                try {
                    Object o = MDReader.getMetadata(id, DUBLINCORE, set, null);
                    if (o instanceof BriefRecordType) {
                        records.add(cswFactory202.createBriefRecord((BriefRecordType)o));
                    } else if (o instanceof SummaryRecordType) {
                        records.add(cswFactory202.createSummaryRecord((SummaryRecordType)o));
                    } else if (o instanceof RecordType) {
                        records.add(cswFactory202.createRecord((RecordType)o));
                    }
                    
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
                }
            }
            if (records.size() == 0) {
                throwUnexistingIdentifierException(unexistingID);
            }
        
            response = new GetRecordByIdResponseType(records, null);
            
        //we build ISO 19139 object    
        } else if (outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
           List<MetaDataImpl> records = new ArrayList<MetaDataImpl>();
           for (String id:request.getId()) {
               
               //we get the form ID and catalog code
                List<String> ids = executeLuceneQuery(new TermQuery(new Term("identifier_sort", id)));
                if (ids.size() > 0) {
                    id = ids.get(0);
                } else {
                    unexistingID.add(id);
                    logger.severe("unexisting id:" + id);
                    continue;
                }
                
                //we get the metadata object
                try {
                    Object o = MDReader.getMetadata(id, ISO_19115, set, null);
                    if (o instanceof MetaDataImpl) {
                        records.add((MetaDataImpl)o);
                    } else {
                        logger.severe("the form " + id + " is not a ISO object");
                    }
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
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
                List<String> ids = executeLuceneQuery(new TermQuery(new Term("identifier_sort", id)));
                if (ids.size() > 0) {
                    id = ids.get(0);
                } else {
                    unexistingID.add(id);
                    logger.severe("unexisting id:" + id);
                    continue;
                }
                
                //we get the metadata object 
                try {
                    Object o = MDReader.getMetadata(id, ISO_19115, set, null);
                    if (o != null) {
                        records.add(o);
                    } else {
                        logger.severe("GFC object is null");
                    }
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
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
                List<String> ids = executeLuceneQuery(new TermQuery(new Term("identifier_sort", id)));
                if (ids.size() > 0) {
                    id = ids.get(0);
                } else {
                    unexistingID.add(id);
                    logger.severe("unexisting id:" + id);
                    continue;
                }
                
                //we get the metadata object 
                try {
                    Object o = MDReader.getMetadata(id, EBRIM, set, null);
                    if (o instanceof IdentifiableType) {
                        records.add(o);
                    } else {
                        logger.severe("The form " + id + " is not a EBRIM v3.0 object");
                    }
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
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
                List<String> ids = executeLuceneQuery(new TermQuery(new Term("identifier_sort", id)));
                if (ids.size() > 0) {
                    id = ids.get(0);
                } else {
                    unexistingID.add(id);
                    logger.severe("unexisting id:" + id);
                    continue;
                }
                
                //we get the metadata object 
                try {
                    Object o = MDReader.getMetadata(id, EBRIM, set, null);
                    if (o instanceof org.constellation.ebrim.v250.RegistryObjectType) {
                        records.add(o);
                    } else {
                        if (o == null)
                            logger.severe("The form " + id + " has not be read is null.");
                        else
                            logger.severe("The form " + id + " is not a EBRIM v2.5 object.");
                    }
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
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
     * @throws org.constellation.ows.v100.OWSWebServiceException
     */
    private void throwUnexistingIdentifierException(List<String> unexistingID) throws OWSWebServiceException {
        String identifiers = "";
        for (String s : unexistingID) {
            identifiers = identifiers + s + ',';
        }
        if (identifiers.lastIndexOf(',') != -1) {
            identifiers.substring(0, identifiers.length() - 1);
        }
        if (identifiers.equals("")) {
            throw new OWSWebServiceException("The record does not correspound to the specified outputSchema.",
                                             INVALID_PARAMETER_VALUE, "outputSchema", version);
        } else {

            throw new OWSWebServiceException("The identifiers " + identifiers + " does not exist",
                                             INVALID_PARAMETER_VALUE, "id", version);
        }
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public DescribeRecordResponseType describeRecord(DescribeRecordType request) throws WebServiceException{
        logger.info("DescribeRecords request processing" + '\n');
        long startTime = System.currentTimeMillis();
        DescribeRecordResponseType response;
        try {
            
            verifyBaseRequest(request);
            
            // we initialize the output format of the response
            String format = request.getOutputFormat();
            if (format != null && isSupportedFormat(format)) {
                outputFormat = format;
            } else if (format != null && !isSupportedFormat(format)) {
                String supportedFormat = "";
                for (String s: ACCEPTED_OUTPUT_FORMATS) {
                    supportedFormat = supportedFormat  + s + '\n';
                } 
                throw new OWSWebServiceException("The server does not support this output format: " + format + '\n' +
                                                 " supported ones are: " + '\n' + supportedFormat,
                                                  INVALID_PARAMETER_VALUE, "outputFormat", version);
            }
        
            // we initialize the type names
            List<QName> typeNames = request.getTypeName();
            if (typeNames == null || typeNames.size() == 0) {
                typeNames = SUPPORTED_TYPE_NAME;
            }
            
            // we initialize the schema language
            String schemaLanguage = request.getSchemaLanguage(); 
            if (schemaLanguage == null) {
                schemaLanguage = "http://www.w3.org/XML/Schema";
            
            } else if (!schemaLanguage.equals("http://www.w3.org/XML/Schema") && !schemaLanguage.equalsIgnoreCase("XMLSCHEMA")){
               
                throw new OWSWebServiceException("The server does not support this schema language: " + schemaLanguage + '\n' +
                                                 " supported ones are: XMLSCHEMA or http://www.w3.org/XML/Schema",
                                                  INVALID_PARAMETER_VALUE, "schemaLanguage", version); 
            }
            
            List<SchemaComponentType> components = new ArrayList<SchemaComponentType>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = factory.newDocumentBuilder();

            if (typeNames.contains(_Record_QNAME)) {

                InputStream in = getResourceAsStream("org/constellation/metadata/record.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("http://www.opengis.net/cat/csw/2.0.2", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
            
            if (typeNames.contains(_Metadata_QNAME)) {

                InputStream in = getResourceAsStream("org/constellation/metadata/metadata.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("http://www.isotc211.org/2005/gmd", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
            
            if (containsOneOfEbrim30(typeNames)) {
                InputStream in = getResourceAsStream("org/constellation/metadata/ebrim-3.0.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
            
            if (containsOneOfEbrim25(typeNames)) {
                InputStream in = getResourceAsStream("org/constellation/metadata/ebrim-2.5.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
                
                
            response  = new DescribeRecordResponseType(components);
            
        } catch (ParserConfigurationException ex) {
            throw new OWSWebServiceException("Parser Configuration Exception while creating the DocumentBuilder",
                                                          NO_APPLICABLE_CODE, null, version);
        } catch (IOException ex) {
            throw new OWSWebServiceException("IO Exception when trying to access xsd file",
                                                          NO_APPLICABLE_CODE, null, version);
        } catch (SAXException ex) {
            throw new OWSWebServiceException("SAX Exception when trying to parse xsd file",
                                                          NO_APPLICABLE_CODE, null, version);
        }
        logger.info("DescribeRecords request processed in " + (System.currentTimeMillis() - startTime) + " ms");   
        return response;
    }
    
    /**
     * Return true if the specified list of QNames contains an ebrim V3.0 QName.
     *
     * @param qnames A list of QNames.
     * @return true if the list contains at least one ebrim V3.0 QName.
     */
    private boolean containsOneOfEbrim30(List<QName> qnames) {
        
        if (qnames.contains(_AdhocQuery_QNAME)
         || qnames.contains(_Association_QNAME)
         || qnames.contains(_AuditableEvent_QNAME)
         || qnames.contains(_ClassificationNode_QNAME)
         || qnames.contains(_ClassificationScheme_QNAME)
         || qnames.contains(_Classification_QNAME)
         || qnames.contains(_ExternalIdentifier_QNAME)
         || qnames.contains(_ExternalLink_QNAME)
         || qnames.contains(_ExtrinsicObject_QNAME)
         || qnames.contains(_Federation_QNAME)
         || qnames.contains(_Notification_QNAME)
         || qnames.contains(_ObjectRefList_QNAME)
         || qnames.contains(_Person_QNAME)
         || qnames.contains(_Organization_QNAME)
         || qnames.contains(_RegistryObject_QNAME)
         || qnames.contains(_RegistryPackage_QNAME)
         || qnames.contains(_Registry_QNAME)
         || qnames.contains(_ServiceBinding_QNAME)
         || qnames.contains(_Service_QNAME)
         || qnames.contains(_SpecificationLink_QNAME)
         || qnames.contains(_Subscription_QNAME)
         || qnames.contains(_User_QNAME)
         || qnames.contains(_WRSExtrinsicObject_QNAME))
            return true;
        return false;
    }
    
    /**
     * Return true if the specified list of QNames contains an ebrim V2.5 QName.
     *
     * @param qnames A list of QNames.
     * @return true if the list contains at least one ebrim V2.5 QName.
     */
    private boolean containsOneOfEbrim25(List<QName> qnames) {
        
        if (qnames.contains(_ExtrinsicObject25_QNAME)
         || qnames.contains(_Federation25_QNAME)
         || qnames.contains(_ExternalLink25_QNAME)
         || qnames.contains(_ClassificationNode25_QNAME)
         || qnames.contains(_User25_QNAME)
         || qnames.contains(_Classification25_QNAME)
         || qnames.contains(_RegistryPackage25_QNAME)
         || qnames.contains(_RegistryObject25_QNAME)
         || qnames.contains(_Association25_QNAME)
         || qnames.contains(_RegistryEntry25_QNAME)
         || qnames.contains(_ClassificationScheme25_QNAME)
         || qnames.contains(_Organization25_QNAME)
         || qnames.contains(_ExternalIdentifier25_QNAME)
         || qnames.contains(_SpecificationLink25_QNAME)
         || qnames.contains(_Registry25_QNAME)
         || qnames.contains(_ServiceBinding25_QNAME)
         || qnames.contains(_Service25_QNAME)
         || qnames.contains(_AuditableEvent25_QNAME)
         || qnames.contains(_Subscription25_QNAME)
         || qnames.contains(_Geometry09_QNAME)
         || qnames.contains(_ApplicationModule09_QNAME)
         || qnames.contains(_WRSExtrinsicObject09_QNAME))
            return true;
        return false;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetDomainResponseType getDomain(GetDomainType request) throws WebServiceException{
        logger.info("GetDomain request processing" + '\n');
        long startTime = System.currentTimeMillis();
        verifyBaseRequest(request);
        // we prepare the response
        List<DomainValuesType> responseList = new ArrayList<DomainValuesType>();
        
        String parameterName = request.getParameterName();
        String propertyName  = request.getPropertyName();
        
        // if the two parameter have been filled we launch an exception
        if (parameterName != null && propertyName != null) {
            throw new OWSWebServiceException("One of propertyName or parameterName must be null",
                                             INVALID_PARAMETER_VALUE, "parameterName", version);
        }
        
        if (parameterName != null) {
            final StringTokenizer tokens = new StringTokenizer(parameterName, ",");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                int pointLocation = token.indexOf('.');
                if (pointLocation != -1) {
                    String operationName = token.substring(0, pointLocation);
                    String parameter     = token.substring(pointLocation + 1);
                    Operation o          = staticCapabilities.getOperationsMetadata().getOperation(operationName);
                    if (o != null) {
                        DomainType param        = o.getParameter(parameter);
                        QName type;
                        if (operationName.equals("GetCapabilities")) {
                            type = _Capabilities_QNAME;
                        } else {
                            type = _Record_QNAME;
                        }
                        if (param != null) {
                            ListOfValuesType values = new  ListOfValuesType(param.getValue());
                            DomainValuesType value  = new DomainValuesType(token, null, values, type); 
                            responseList.add(value);
                        } else {
                            throw new OWSWebServiceException("The parameter " + parameter + " in the operation " + operationName + " does not exist",
                                                             INVALID_PARAMETER_VALUE, "parameterName", version);
                        }
                    } else {
                        throw new OWSWebServiceException("The operation " + operationName + " does not exist",
                                                          INVALID_PARAMETER_VALUE, "parameterName", version);
                    }
                } else {
                    throw new OWSWebServiceException("ParameterName must be formed like this Operation.parameterName",
                                                     INVALID_PARAMETER_VALUE, "parameterName", version);
                }
            }
        
        } else if (propertyName != null) {
            final StringTokenizer tokens = new StringTokenizer(propertyName, ",");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                List<String> paths = ISO_QUERYABLE.get(token);
                if (paths != null) {
                    StringBuilder SQLRequest = new StringBuilder("SELECT distinct(value) FROM \"TextValues\" WHERE ");
                    for (String path: paths) {
                        SQLRequest.append("path='").append(path).append("' OR ");
                    }
                    if (paths.size() != 0){
                        // we remove the last "OR "
                        int length        = SQLRequest.length() ;
                        SQLRequest.delete(length - 3, length);
                        
                        //we build and execute the SQL query
                        try {
                            Statement stmt      = MDConnection.createStatement();
                            ResultSet results   = stmt.executeQuery(SQLRequest.toString());
                            List<String> values = new ArrayList<String>();
                            while (results.next()) {
                                values.add(results.getString(1));
                            }
                            ListOfValuesType ListValues = new  ListOfValuesType(values);
                            DomainValuesType value      = new DomainValuesType(null, token, ListValues, _Metadata_QNAME); 
                            responseList.add(value);
                                
                        } catch (SQLException e) {
                            throw new OWSWebServiceException("The service has launch an SQL exeption:" + e.getMessage(),
                                                             NO_APPLICABLE_CODE, null, version);
                        }
                    } else {
                        throw new OWSWebServiceException("The property " + token + " is not queryable for now",
                                                         INVALID_PARAMETER_VALUE, "propertyName", version);
                    }
                } else {
                    throw new OWSWebServiceException("The property " + token + " is not queryable",
                                                     INVALID_PARAMETER_VALUE, "propertyName", version);
                }
            }
        // if no parameter have been filled we launch an exception    
        } else {
            throw new OWSWebServiceException("One of propertyName or parameterName must be filled",
                                             MISSING_PARAMETER_VALUE, "parameterName, propertyName", version);
        }
        logger.info("GetDomain request processed in " + (System.currentTimeMillis() - startTime) + " ms");   
        return new GetDomainResponseType(responseList);
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public TransactionResponseType transaction(TransactionType request) throws WebServiceException {
        logger.info("Transaction request processing" + '\n');
        
        if (profile == DISCOVERY) {
            throw new OWSWebServiceException("This method is not supported by this mode of CSW",
                                             OPERATION_NOT_SUPPORTED, "Request", version);
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
                
                for(Object record: insertRequest.getAny()) {
                    
                        try {
                            storeMetadata(record);
                            totalInserted++;
                        
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            throw new OWSWebServiceException("The service has throw an SQLException: " + ex.getMessage(),
                                                             NO_APPLICABLE_CODE, null, version);
                        } catch (IllegalArgumentException e) {
                            logger.severe("already that title.");
                            totalUpdated++;
                        }
                }
            } else if (transaction instanceof DeleteType) {
                DeleteType deleteRequest = (DeleteType)transaction;
                throw new OWSWebServiceException("This kind of transaction (delete) is not yet supported by the service.",
                                                  NO_APPLICABLE_CODE, "TransactionType", version);
                
                
            } else if (transaction instanceof UpdateType) {
                UpdateType updateRequest = (UpdateType)transaction;
                throw new OWSWebServiceException("This kind of transaction (update) is not yet supported by the service.",
                                                  NO_APPLICABLE_CODE, "TransactionType", version);
            
                
            } else {
                String className = " null object";
                if (transaction != null) {
                    className = transaction.getClass().getName();
                }
                throw new OWSWebServiceException("This kind of transaction is not supported by the service: " + className,
                                                  INVALID_PARAMETER_VALUE, "TransactionType", version);
            }
            
        }
        TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                    totalUpdated,
                                                                    totalDeleted,
                                                                    requestID); 
        TransactionResponseType response = new TransactionResponseType(summary, null, version.toString());
        logger.info("Transaction request processed in " + (System.currentTimeMillis() - startTime) + " ms");   
        return response;
    }
    
    /**
     * Record an object in the metadata database.
     * 
     * @param obj The object to store in the database.
     * @return true if the storage succeed, false else.
     */
    protected boolean storeMetadata(Object obj) throws SQLException, WebServiceException {
        // profiling operation
        long start     = System.currentTimeMillis();
        long transTime = 0;
        long writeTime = 0;
        
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        
        //we try to find a title for the from
        String title = findName(obj);
        if (title.equals("unknow title")) {
            title = MDWriter.getAvailableTitle();
        }
        
        // we create a MDWeb form form the object
        Form f = null;
        try {
            long start_trans = System.currentTimeMillis();
            f = MDWriter.getFormFromObject(obj, title);
            transTime = System.currentTimeMillis() - start_trans;
            
        } catch (IllegalArgumentException e) {
             throw new OWSWebServiceException("This kind of resource cannot be parsed by the service: " + obj.getClass().getSimpleName() +'\n' +
                                              "cause: " + e.getMessage(),
                                               NO_APPLICABLE_CODE, null, version);
        }
        
        // and we store it in the database
        if (f != null) {
            try {
                long startWrite = System.currentTimeMillis();
                MDWriter.writeForm(f);
                writeTime = System.currentTimeMillis() - startWrite;
            } catch (IllegalArgumentException e) {
                //TODO restore catching at this point
                throw e;
                //return false;
            }
            
            long time = System.currentTimeMillis() - start; 
            logger.info("inserted new Form: " + f.getTitle() + " in " + time + " ms (transformation: " + transTime + " DB write: " +  writeTime + ")");
            index.indexDocument(f);
            return true;
        }
        return false;
    }
    
    /**
     * This method try to find a title to this object.
     * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
     * else we try to find a getName() method.
     * 
     * @param obj the object for wich we want a title
     * 
     * @return the founded title or "Unknow title"
     */
    private String findName(Object obj) {
        
        //here we try to get the title
        AbstractSimpleLiteral titleSL = null;
        String title = "unknow title";
        if (obj instanceof RecordType) {
            titleSL = ((RecordType) obj).getTitle();
            if (titleSL == null) {
                titleSL = ((RecordType) obj).getIdentifier();
            }
                               
            if (titleSL == null) {
                title = "unknow title";
            } else {
                if (titleSL.getContent().size() > 0)
                    title = titleSL.getContent().get(0);
            }
                            
        } else if (obj instanceof MetaDataImpl) {
            Collection<Identification> idents = ((MetaDataImpl) obj).getIdentificationInfo();
            if (idents.size() != 0) {
                Identification ident = idents.iterator().next();
                if (ident != null && ident.getCitation() != null && ident.getCitation().getTitle() != null) {
                    title = ident.getCitation().getTitle().toString();
                } 
            }
        } else if (obj instanceof RegistryObjectType) {
            InternationalStringType ident = ((RegistryObjectType) obj).getName();
            if (ident != null && ident.getLocalizedString().size() > 0) {
                title = ident.getLocalizedString().get(0).getValue();
            } else {
                title = ((RegistryObjectType) obj).getId();
            } 
        
        } else if (obj instanceof org.constellation.ebrim.v250.RegistryObjectType) {
            org.constellation.ebrim.v250.InternationalStringType ident = ((org.constellation.ebrim.v250.RegistryObjectType) obj).getName();
            if (ident != null && ident.getLocalizedString().size() > 0) {
                title = ident.getLocalizedString().get(0).getValue();
            } else {
                title = ((org.constellation.ebrim.v250.RegistryObjectType) obj).getId();
            } 
            
        } else {
            Method nameGetter = null;
            String methodName = "";
            int i = 0;
            while (i < 3) {
                try {
                    switch (i) {
                        case 0: methodName = "getTitle";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                                 
                        case 1: methodName = "getName";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                                
                        case 2: methodName = "getId";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                    }
                
                
                } catch (NoSuchMethodException ex) {
                    logger.finer("not " + methodName + " method in " + obj.getClass().getSimpleName());
                } catch (SecurityException ex) {
                    logger.severe(" security exception while getting the title of the object.");
                }
                if (nameGetter != null) {
                    i = 3;
                } else {
                    i++;
                }
            }
            
            if (nameGetter != null) {
                try {
                    Object objT = nameGetter.invoke(obj);
                    if (objT instanceof String) {
                        title = (String) obj;
                    
                    } else if (objT instanceof AbstractSimpleLiteral) {
                        titleSL = (AbstractSimpleLiteral) objT;
                        if (titleSL.getContent().size() > 0)
                            title = titleSL.getContent().get(0);
                        else title = "unknow title";
                    
                    } else {
                        title = "unknow title";
                    }
                    
                    if (title == null)
                        title = "unknow title";
                } catch (IllegalAccessException ex) {
                    logger.severe("illegal access for method " + methodName + " in " + obj.getClass().getSimpleName() + '\n' + 
                                  "cause: " + ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    logger.severe("illegal argument for method " + methodName + " in " + obj.getClass().getSimpleName()  +'\n' +
                                  "cause: " + ex.getMessage());
                } catch (InvocationTargetException ex) {
                    logger.severe("invocation target exception for " + methodName + " in " + obj.getClass().getSimpleName() +'\n' +
                                  "cause: " + ex.getMessage());
                }
            }
            
            if (title.equals("unknow title"))
                logger.severe("unknow type: " + obj.getClass().getName() + " unable to find a title");
        }
        return title;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public HarvestResponseType harvest(HarvestType request) throws WebServiceException {
        logger.info("Harvest request processing" + '\n');
        if (profile == DISCOVERY) {
            throw new OWSWebServiceException("This method is not supported by this mode of CSW",
                                             OPERATION_NOT_SUPPORTED, "Request", version);
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
            throw new OWSWebServiceException("The resource type to harvest must be specified",
                                             MISSING_PARAMETER_VALUE, "resourceType", version);
        } else {
            if (!ACCEPTED_RESOURCE_TYPE.contains(resourceType)) {
                throw new OWSWebServiceException("This resource type is not allowed. ",
                                             MISSING_PARAMETER_VALUE, "resourceType", version);
            }
        }
        String sourceURL = request.getSource();
        if (sourceURL != null) {
            
            try {
                
                // if the resource is a simple record
                if (sourceURL.endsWith("xml")) {
                    
                    URL source          = new URL(sourceURL);
                    URLConnection conec = source.openConnection();
                
                    // we get the source document
                    File fileToHarvest = File.createTempFile("harvested", "xml");
                    fileToHarvest.deleteOnExit();
                    InputStream in = conec.getInputStream();
                    FileOutputStream out = new FileOutputStream(fileToHarvest);
                    byte[] buffer = new byte[1024];
                    int size;

                    while ((size = in.read(buffer, 0, 1024)) > 0) {
                        out.write(buffer, 0, size);
                    }
                
                    if (resourceType.equals("http://www.isotc211.org/2005/gmd")      || 
                        resourceType.equals("http://www.opengis.net/cat/csw/2.0.2")  ||
                        resourceType.equals("http://www.isotc211.org/2005/gfc"))        {

                        Object harvested = unmarshaller.unmarshal(fileToHarvest);
                        if (harvested == null) {
                            throw new OWSWebServiceException("The resource can not be parsed.",
                                                              INVALID_PARAMETER_VALUE, "Source", version);
                        }
                    
                        logger.info("Object Type of the harvested Resource: " + harvested.getClass().getName());
                        
                        // ugly patch TODO handle update in mdweb
                        try {
                            if (storeMetadata(harvested))
                                totalInserted++;
                        } catch( IllegalArgumentException e) {
                            totalUpdated++;
                        }
                    }
                
                // if the resource is another CSW service we get all the data of this catalogue.
                } else {
                    int[] results = catalogueHarvester.harvestCatalogue(sourceURL);
                    totalInserted = results[0];
                    totalUpdated  = results[1];
                    totalDeleted  = results[2];
                }
                
            } catch (SQLException ex) {
                throw new OWSWebServiceException("The service has throw an SQLException: " + ex.getMessage(),
                                                  NO_APPLICABLE_CODE, null, version);
            } catch (JAXBException ex) {
                throw new OWSWebServiceException("The resource can not be parsed: " + ex.getMessage(),
                                                  INVALID_PARAMETER_VALUE, "Source", version);
            } catch (MalformedURLException ex) {
                throw new OWSWebServiceException("The source URL is malformed",
                                                  INVALID_PARAMETER_VALUE, "Source", version);
            } catch (IOException ex) {
                throw new OWSWebServiceException("The service can't open the connection to the source",
                                                  INVALID_PARAMETER_VALUE, "Source", version);
            } 
            
        }
        
        //mode synchronous
        if (request.getResponseHandler().size() == 0) {
           
            TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                        totalUpdated,
                                                                        totalDeleted,
                                                                        null);
            TransactionResponseType transactionResponse = new TransactionResponseType(summary, null, version.toString());
            response = new HarvestResponseType(transactionResponse);
        
        //mode asynchronous    
        } else {
            AcknowledgementType acknowledgement = null;
            response = new HarvestResponseType(acknowledgement);
            throw new OWSWebServiceException("This asynchronous mode for harvest is not yet supported by the service.",
                                                  OPERATION_NOT_SUPPORTED, "ResponseHandler", version);
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
    private boolean isSupportedFormat(String format) {
        return ACCEPTED_OUTPUT_FORMATS.contains(format);
    }
    
    /**
     * Return the current version number of the service. 
     */
    public ServiceVersion getVersion() {
        return version;
    }
    
    /**
     * Set the current service version
     * 
     * @param version The current version.
     */
    public void setVersion(ServiceVersion version){
        this.version = version;
        if (MDReader != null) {
            this.MDReader.setVersion(version);
        }
    }
    
    /**
     * Set the capabilities document.
     * 
     * @param staticCapabilities An OWS 1.0.0 capabilities object.
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
     * Verify that the bases request attributes are correct.
     * 
     * @param request an object request with the base attribute (all except GetCapabilities request); 
     */ 
    private void verifyBaseRequest(RequestBaseType request) throws WebServiceException {
        if (!isStarted) {
            throw new OWSWebServiceException("The service is not running!",
                                              NO_APPLICABLE_CODE, null, version);
        }
        if (request != null) {
            if (request.getService() != null) {
                if (!request.getService().equals("CSW"))  {
                    throw new OWSWebServiceException("service must be \"CSW\"!",
                                                  INVALID_PARAMETER_VALUE, "service", version);
                }
            } else {
                throw new OWSWebServiceException("service must be specified!",
                                              MISSING_PARAMETER_VALUE, "service", version);
            }
            if (request.getVersion()!= null) {
                if (!request.getVersion().equals("2.0.2")) {
                    throw new OWSWebServiceException("version must be \"2.0.2\"!",
                                                  VERSION_NEGOTIATION_FAILED, "version", version);
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
     * Remove the prefix on propertyName.
     */
    private String removePrefix(String s) {
        int i = s.indexOf(':');
        if ( i != -1) {
            s = s.substring(i + 1, s.length());
        }
        return s;
    }

    /**
     * Obtain the Thread Context ClassLoader.
     */
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
    
    /**
     * Return an input stream of the specified resource. 
     */
    public static InputStream getResourceAsStream(String url) {
        ClassLoader cl = getContextClassLoader();
        return cl.getResourceAsStream(url);
    }
    
}
