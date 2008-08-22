/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
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

package net.seagis.metadata;

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
import org.w3c.dom.Document;

//seaGIS dependencies
import net.seagis.cat.csw.v202.AbstractRecordType;
import net.seagis.cat.csw.v202.AcknowledgementType;
import net.seagis.cat.csw.v202.BriefRecordType;
import net.seagis.cat.csw.v202.Capabilities;
import net.seagis.cat.csw.v202.DeleteType;
import net.seagis.cat.csw.v202.DescribeRecordResponseType;
import net.seagis.cat.csw.v202.DescribeRecordType;
import net.seagis.cat.csw.v202.DomainValuesType;
import net.seagis.cat.csw.v202.ElementSetNameType;
import net.seagis.cat.csw.v202.ElementSetType;
import net.seagis.cat.csw.v202.GetCapabilities;
import net.seagis.cat.csw.v202.GetDomainResponseType;
import net.seagis.cat.csw.v202.GetDomainType;
import net.seagis.cat.csw.v202.GetRecordByIdResponseType;
import net.seagis.cat.csw.v202.GetRecordByIdType;
import net.seagis.cat.csw.v202.GetRecordsResponseType;
import net.seagis.cat.csw.v202.GetRecordsType;
import net.seagis.cat.csw.v202.HarvestResponseType;
import net.seagis.cat.csw.v202.HarvestType;
import net.seagis.cat.csw.v202.InsertType;
import net.seagis.cat.csw.v202.ListOfValuesType;
import net.seagis.cat.csw.v202.ObjectFactory;
import net.seagis.cat.csw.v202.QueryType;
import net.seagis.cat.csw.v202.RecordType;
import net.seagis.cat.csw.v202.RequestBaseType;
import net.seagis.cat.csw.v202.ResultType;
import net.seagis.cat.csw.v202.SearchResultsType;
import net.seagis.cat.csw.v202.SummaryRecordType;
import net.seagis.cat.csw.v202.TransactionResponseType;
import net.seagis.cat.csw.v202.TransactionSummaryType;
import net.seagis.cat.csw.v202.TransactionType;
import net.seagis.cat.csw.v202.UpdateType;
import net.seagis.cat.csw.v202.SchemaComponentType;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.dublincore.AbstractSimpleLiteral;
import net.seagis.filter.FilterParser;
import net.seagis.lucene.Filter.SpatialQuery;
import net.seagis.ogc.FilterCapabilities;
import net.seagis.ogc.SortByType;
import net.seagis.ogc.SortPropertyType;
import net.seagis.ows.v100.AcceptFormatsType;
import net.seagis.ows.v100.AcceptVersionsType;
import net.seagis.ows.v100.DomainType;
import net.seagis.ows.v100.OWSWebServiceException;
import net.seagis.ows.v100.Operation;
import net.seagis.ows.v100.OperationsMetadata;
import net.seagis.ows.v100.SectionsType;
import net.seagis.ows.v100.ServiceIdentification;
import net.seagis.ows.v100.ServiceProvider;
import net.seagis.ws.rs.WebService;
import static net.seagis.ows.OWSExceptionCode.*;
import static net.seagis.metadata.MetadataReader.*;

// Apache Lucene dependencies
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Sort;

//geotols dependencies
import org.geotools.metadata.iso.MetaDataImpl;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

//mdweb model dependencies
import net.seagis.cat.csw.v202.EchoedRequestType;
import org.mdweb.model.schemas.Standard; 
import org.mdweb.model.storage.Form; 
import org.mdweb.sql.v20.Reader20; 
import org.mdweb.sql.v20.Writer20;

// GeoAPI dependencies
import org.opengis.filter.sort.SortOrder;
import org.opengis.metadata.identification.Identification;

// PostgreSQL dependencies
import org.postgresql.ds.PGSimpleDataSource;
import org.xml.sax.SAXException;


/**
 *
 * @author Guilhem Legal
 */
public class CSWworker {

    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("net.seagis.metadata");
    
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
    private final Connection MDConnection;
    
    /**
     * A Reader to the Metadata database.
     */
    private final Reader20 databaseReader;
    
    /**
     * A Writer to the Metadata database.
     */
    private final Writer20 databaseWriter;
    
    /**
     * An object creator from the MDWeb database.
     */
    private final MetadataReader MDReader;
    
    /**
     * An Form creator from the MDWeb database.
     */
    private final MetadataWriter MDWriter;
    
    /**
     * A JAXB factory to csw object version 2.0.2
     */
    protected final ObjectFactory cswFactory202;
    
    /**
     * A JAXB factory to csw object version 2.0.0 
     */
    protected final net.seagis.cat.csw.v200.ObjectFactory cswFactory200;
    
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
    private final IndexLucene index;
    
    /**
     * A filter parser whitch create lucene query from OGC filter
     */
    private final FilterParser filterParser;
    
    /**
     * A flag indicating if the worker is correctly started.
     */
    private boolean isStarted;
    
    /**
     * A catalogue Harvester comunicating with other CSW 
     */
    private final CatalogueHarvester catalogueHarvester;
    
    /**
     * The queryable element from ISO 19115 and their path id.
     */
    protected static Map<String, List<String>> ISO_QUERYABLE;
    static {
        ISO_QUERYABLE      = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of ISO 19115
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        ISO_QUERYABLE.put("Subject", paths);
        
        //MANDATORY
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        ISO_QUERYABLE.put("Title", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract");
        ISO_QUERYABLE.put("Abstract", paths);
        
        //MANDATORY TODO tout les valeur
        paths = new ArrayList<String>();
        paths.add("*");
        ISO_QUERYABLE.put("AnyText", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        ISO_QUERYABLE.put("Format", paths);
        
        //MANDATORY
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        ISO_QUERYABLE.put("Identifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dateStamp");
        ISO_QUERYABLE.put("Modified", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:hierarchyLevel");
        ISO_QUERYABLE.put("Type", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        ISO_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        ISO_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        ISO_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        ISO_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        /*
         * CRS 
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:codeSpace");
        ISO_QUERYABLE.put("Authority",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        ISO_QUERYABLE.put("ID",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        ISO_QUERYABLE.put("Version",     paths);
        
        /*
         * Additional queryable Element
         */ 
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:alternateTitle");
        ISO_QUERYABLE.put("AlternateTitle",   paths);
        
        //TODO verify codelist  CI_DateTypeCode=revision
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date:date");
        ISO_QUERYABLE.put("RevisionDate",  paths);
        
        //TODO verify codelist  CI_DateTypeCode=creation
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date:date");
        ISO_QUERYABLE.put("CreationDate",  paths);
        
        //TODO verify codelist  CI_DateTypeCode=publication
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date:date");
        ISO_QUERYABLE.put("PublicationDate",  paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:contact:organisationName");
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:organisationName");
        ISO_QUERYABLE.put("OrganisationName", paths);
        
        //TODO If an instance of the class MD_SecurityConstraint exists for a resource, the “HasSecurityConstraints” is “true”, otherwise “false”
        paths = new ArrayList<String>();
        ISO_QUERYABLE.put("HasSecurityConstraints", paths);
        
        //TODO MD_FeatureCatalogueDescription
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:language");
        ISO_QUERYABLE.put("Language", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:identifier:code");
        ISO_QUERYABLE.put("ResourceIdentifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:parentIdentifier");
        ISO_QUERYABLE.put("ParentIdentifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:Type");
        ISO_QUERYABLE.put("KeywordType", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        ISO_QUERYABLE.put("TopicCategory", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:language");
        ISO_QUERYABLE.put("ResourceLanguage", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:code");
        ISO_QUERYABLE.put("GeographicDescriptionCode", paths);
        
        /*
         * spatial resolution
         */
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:equivalentScale:denominator");
        ISO_QUERYABLE.put("Denominator", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:distance");
        ISO_QUERYABLE.put("DistanceValue", paths);
        
        //TODO not existing path in MDWeb or geotools (Distance is treated as a primitive type)
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:distance:uom");
        ISO_QUERYABLE.put("DistanceUOM", paths);
        
        /*
         * Temporal Extent
         */ 
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition");
        ISO_QUERYABLE.put("TempExtent_begin", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:endPosition");
        ISO_QUERYABLE.put("TempExtent_end", paths);
        
       
        
        // the following element are described in Service part of ISO 19139 not yet used in MDWeb 
        paths = new ArrayList<String>();
        ISO_QUERYABLE.put("ServiceType", paths);
        ISO_QUERYABLE.put("ServiceTypeVersion", paths);
        ISO_QUERYABLE.put("Operation", paths);
        ISO_QUERYABLE.put("CouplingType", paths);
        ISO_QUERYABLE.put("OperatesOn", paths);
        ISO_QUERYABLE.put("OperatesOnIdentifier", paths);
        ISO_QUERYABLE.put("OperatesOnWithOpName", paths);
    }
    
    /**
     * The queryable element from DublinCore and their path id.
     */
    protected static Map<String, List<String>> DUBLIN_CORE_QUERYABLE;
    static {
        DUBLIN_CORE_QUERYABLE = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        paths.add("Catalog Web Service:Record:title:content");
        DUBLIN_CORE_QUERYABLE.put("title", paths);
        
        //TODO verify codelist=originator
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:creator:content");
        DUBLIN_CORE_QUERYABLE.put("creator", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        paths.add("Catalog Web Service:Record:subject:content");
        DUBLIN_CORE_QUERYABLE.put("description", paths);
        DUBLIN_CORE_QUERYABLE.put("subject", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract");
        paths.add("Catalog Web Service:Record:abstract:content");
        DUBLIN_CORE_QUERYABLE.put("abstract", paths);
        
        //TODO verify codelist=publisher
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:publisher:content");
        DUBLIN_CORE_QUERYABLE.put("publisher", paths);
        
        //TODO verify codelist=contributor
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:contributor:content");
        DUBLIN_CORE_QUERYABLE.put("contributor", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dateStamp");
        paths.add("Catalog Web Service:Record:date:content");
        DUBLIN_CORE_QUERYABLE.put("date", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:hierarchyLevel");
        paths.add("Catalog Web Service:Record:type:content");
        DUBLIN_CORE_QUERYABLE.put("type", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        paths.add("Catalog Web Service:Record:format:content");
        DUBLIN_CORE_QUERYABLE.put("format", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("Catalog Web Service:Record:identifier:content");
        paths.add("ISO 19110:FC_FeatureCatalogue:id");
        DUBLIN_CORE_QUERYABLE.put("identifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("Catalog Web Service:Record:source");
        DUBLIN_CORE_QUERYABLE.put("source", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:language");
        paths.add("Catalog Web Service:Record:language:content");
        DUBLIN_CORE_QUERYABLE.put("language", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:aggregationInfo");
        paths.add("Catalog Web Service:Record:relation:content");
        DUBLIN_CORE_QUERYABLE.put("relation", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:accessConstraints");
        paths.add("Catalog Web Service:Record:rights:content");
        DUBLIN_CORE_QUERYABLE.put("rigths", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:LowerCorner");
        DUBLIN_CORE_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:UpperCorner");
        DUBLIN_CORE_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        paths.add("Catalog Web Service:Record:BoundingBox:UpperCorner");
        DUBLIN_CORE_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        paths.add("Catalog Web Service:Record:BoundingBox:LowerCorner");
        DUBLIN_CORE_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("Catalog Web Service:Record:BoundingBox:crs");
        DUBLIN_CORE_QUERYABLE.put("CRS",     paths);
    }
    
    /**
     * a QName for csw:Record type
     */
    private final static QName _Record_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Record");
    
    /**
     * a QName for gmd:MD_Metadata type
     */
    private final static QName _Metadata_QNAME = new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata");
    
    /**
     * a QName for csw:Capabilities type
     */
    private final static QName _Capabilities_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Capabilities");
    
    /**
     * A list of the supported Type name 
     */
    private final static List<QName> SUPPORTED_TYPE_NAME;
    static {
        SUPPORTED_TYPE_NAME = new ArrayList<QName>();
        SUPPORTED_TYPE_NAME.add(_Record_QNAME);
        SUPPORTED_TYPE_NAME.add(_Metadata_QNAME);
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
    }
    
    
    /**
     * Build a new CSW worker
     * 
     * @param marshaller A JAXB marshaller to send xml to MDWeb
     * 
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public CSWworker(Unmarshaller unmarshaller, Marshaller marshaller) throws IOException, SQLException {
        
        this.unmarshaller = unmarshaller;
        this.marshaller   = marshaller; 
        cswFactory202     = new ObjectFactory();
        cswFactory200     = new net.seagis.cat.csw.v200.ObjectFactory();
        Properties prop   = new Properties();
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
        
        // we initialize the filterParser
        FilterParser fp = null;
        try {
            fp = new FilterParser(version);
        } catch (JAXBException ex) {
            isStarted = false;
            
            logger.severe("The CSW service is not working!"       + '\n' + 
                          "Unable to create Filter JAXB Context." + '\n' + 
                          "Cause: " + ex.getMessage());
        }
        filterParser = fp;
        
        //we create a connection to the metadata database
        if (isStarted) {
            PGSimpleDataSource dataSourceMD = new PGSimpleDataSource();
            dataSourceMD.setServerName(prop.getProperty("MDDBServerName"));
            dataSourceMD.setPortNumber(Integer.parseInt(prop.getProperty("MDDBServerPort")));
            dataSourceMD.setDatabaseName(prop.getProperty("MDDBName"));
            dataSourceMD.setUser(prop.getProperty("MDDBUser"));
            dataSourceMD.setPassword(prop.getProperty("MDDBUserPassword"));
            MDConnection    = dataSourceMD.getConnection();
            
            if (MDConnection == null) {
                logger.severe("The CSW service is not working!" + '\n' + 
                              "cause: The web service can't connect to the metadata database!");
                databaseReader      = null;
                databaseWriter      = null;
                index               = null;
                MDReader            = null;
                MDWriter            = null;
                isStarted           = false;
                catalogueHarvester  = null;
            } else {
                 databaseReader     = new Reader20(Standard.ISO_19115,  MDConnection);
                 databaseWriter     = new Writer20(MDConnection);
                 index              = new IndexLucene(databaseReader, env);
                 MDReader           = new MetadataReader(databaseReader, dataSourceMD.getConnection());
                 MDWriter           = new MetadataWriter(databaseReader, databaseWriter);
                 catalogueHarvester = new CatalogueHarvester(this);
                 
                 logger.info("CSW service running");
            }
            
        } else {
            databaseReader     = null;
            databaseWriter     = null;
            index              = null;
            MDReader           = null;
            MDWriter           = null;
            MDConnection       = null;
            catalogueHarvester = null;
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
            //we update the URL
            if (om != null)
                WebService.updateOWSURL(om.getOperation(), serviceURL, "CSW");
               
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
            if (!outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2") && 
                !outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
                throw new OWSWebServiceException("The server does not support this output schema: " + outputSchema,
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
        if (request.getAbstractQuery() != null) {
            query = (QueryType)request.getAbstractQuery();
            typeNames =  query.getTypeNames();
            if (typeNames == null || typeNames.size() == 0) {
                throw new OWSWebServiceException("The query must specify at least typeName.",
                                                 INVALID_PARAMETER_VALUE, "TypeNames", version);
            } else {
                for (QName type:typeNames) {
                    if (!SUPPORTED_TYPE_NAME.contains(type)) {
                        throw new OWSWebServiceException("The typeName " + type.getLocalPart() + " is not supported by the service:" +'\n' +
                                                         "supported one are:" + '\n' + supportedTypeNames(),
                                                         INVALID_PARAMETER_VALUE, "TypeNames", version);
                    }
                }
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
        
        // build the lucene query from the specified filter
        SpatialQuery luceneQuery = filterParser.getLuceneQuery(query.getConstraint());
        
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
        List<String> results;
        try {
            results = index.doSearch(luceneQuery);
        
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
        
        int nextRecord = startPos + maxRecord;
        if (nextRecord > results.size())
            nextRecord = 0;
        int max = (startPos - 1) + maxRecord;
        if (max > results.size())
            max = results.size();
        
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
                        }
                        else
                            records.add((AbstractRecordType)obj);
                    }
                    searchResults = new SearchResultsType(ID, 
                                                          set, 
                                                          results.size(),
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
            } else if (outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
            
                // we return only the number of result matching
                if (resultType.equals(ResultType.HITS)) {
                    searchResults = new SearchResultsType(ID, query.getElementSetName().getValue(), results.size(), nextRecord);
                
                } else if (resultType.equals(ResultType.RESULTS)) {
                
                    List<Object> records = new ArrayList<Object>();
                    
                    for (int i = startPos -1; i < maxRecord; i++) {
                        Object obj = MDReader.getMetadata(results.get(i), ISO_19115, set, elementName);
                        if (obj instanceof MetaDataImpl)
                            records.add((MetaDataImpl)obj);
                    }
                    searchResults = new SearchResultsType(ID, 
                                                          set, 
                                                          results.size(),
                                                          maxRecord,
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
        
                // this case must never append
            } else {
                throw new OWSWebServiceException("The service does not accept this outputShema:" + outputSchema,
                                              NO_APPLICABLE_CODE, null, version);
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
        
        //we build dublin core object
        if (outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2")) {
            List<String> unexistingID = new ArrayList<String>();
            List<JAXBElement<? extends AbstractRecordType>> records = new ArrayList<JAXBElement<? extends AbstractRecordType>>(); 
            for (String id:request.getId()) {
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
                /* here we catch the exception reporting the non-existenz of identifier.
                 * if at least one id match we return the record, else we throw the exception
                 */ 
                } catch (OWSWebServiceException e) {
                   if (e.getLocator().equals("id")) {
                       unexistingID.add(id);
                       logger.severe("unexisting id:" + id);
                   } else {
                       throw e;
                   }
                }
            }
            if (records.size() == 0) {
                String identifiers = "";
                for (String s: unexistingID) {
                    identifiers = identifiers + s + ',';
                }
                if (identifiers.lastIndexOf(',') != -1)
                    identifiers.substring(0, identifiers.length() - 1);
                
                throw new OWSWebServiceException("The identifiers " + identifiers + " does not exist",
                                                     INVALID_PARAMETER_VALUE, "id", version);
            }
        
            response = new GetRecordByIdResponseType(records, null, null);
        //we build ISO 19139 object    
        } else if (outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
           List<MetaDataImpl> records = new ArrayList<MetaDataImpl>();
           for (String id:request.getId()) {
                try {
                    Object o = MDReader.getMetadata(id, ISO_19115, set, null);
                    if (o instanceof MetaDataImpl) {
                        records.add((MetaDataImpl)o);
                    }
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
                }
           }
        
           response = new GetRecordByIdResponseType(null, records, null);      
        
        } else if (outputSchema.equals("http://www.isotc211.org/2005/gfc")) {
           List<Object> records = new ArrayList<Object>();
           for (String id:request.getId()) {
                try {
                    Object o = MDReader.getMetadata(id, ISO_19115, set, null);
                    if (o != null) {
                        records.add(o);
                    }
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
                }
           }
        
           response = new GetRecordByIdResponseType(null, null, records);      
        
        // this case must never append
        } else {
            response = null;
        }
        
        logger.info("GetRecordById request processed in " + (System.currentTimeMillis() - startTime) + " ms");        
        return response;
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

                InputStream in = getResourceAsStream("net/seagis/metadata/record.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("http://www.opengis.net/cat/csw/2.0.2", schemaLanguage, d.getDocumentElement());
                components.add(component);
            }
            
            if (typeNames.contains(_Metadata_QNAME)) {

                InputStream in = getResourceAsStream("net/seagis/metadata/metadata.xsd");
                Document d = constructor.parse(in);
                SchemaComponentType component = new SchemaComponentType("http://www.isotc211.org/2005/gmd", schemaLanguage, d.getDocumentElement());
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
        
        String title = findName(obj);
        // we create a MDWeb form form the object
        Form f = null;
        try {
            long start_trans = System.currentTimeMillis();
            f = MDWriter.getFormFromObject(obj, title);
            transTime = System.currentTimeMillis() - start_trans;
            
        } catch (IllegalArgumentException e) {
             throw new OWSWebServiceException("This kind of resource cannot be parsed by the service: " + obj.getClass().getSimpleName(),
                                               NO_APPLICABLE_CODE, null, version);
        }
        
        // and we store it in the database
        if (f != null) {
            try {
                long startWrite = System.currentTimeMillis();
                databaseWriter.writeForm(f, false);
                writeTime = System.currentTimeMillis() - startWrite;
            } catch (IllegalArgumentException e) {
                //TODO restore catching at this point
                throw e;
                //return false;
            }
            
            long time = System.currentTimeMillis() - start; 
            logger.info("inserted new Form: " + f.getTitle() + " in " + time + " ms (transformation: " + transTime + " DB write: " +  writeTime + ")");
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
