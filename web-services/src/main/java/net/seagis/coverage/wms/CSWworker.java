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

package net.seagis.coverage.wms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import java.util.logging.Level;
import java.util.logging.Logger;

//seaGIS dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import net.seagis.cat.csw.AbstractRecordType;
import net.seagis.cat.csw.BriefRecordType;
import net.seagis.cat.csw.Capabilities;
import net.seagis.cat.csw.DeleteType;
import net.seagis.cat.csw.DescribeRecordResponseType;
import net.seagis.cat.csw.DescribeRecordType;
import net.seagis.cat.csw.DomainValuesType;
import net.seagis.cat.csw.ElementSetNameType;
import net.seagis.cat.csw.ElementSetType;
import net.seagis.cat.csw.GetCapabilities;
import net.seagis.cat.csw.GetDomainResponseType;
import net.seagis.cat.csw.GetDomainType;
import net.seagis.cat.csw.GetRecordByIdResponseType;
import net.seagis.cat.csw.GetRecordByIdType;
import net.seagis.cat.csw.GetRecordsResponseType;
import net.seagis.cat.csw.GetRecordsType;
import net.seagis.cat.csw.HarvestResponseType;
import net.seagis.cat.csw.HarvestType;
import net.seagis.cat.csw.InsertType;
import net.seagis.cat.csw.ListOfValuesType;
import net.seagis.cat.csw.ObjectFactory;
import net.seagis.cat.csw.QueryType;
import net.seagis.cat.csw.RecordType;
import net.seagis.cat.csw.RequestBaseType;
import net.seagis.cat.csw.ResultType;
import net.seagis.cat.csw.SearchResultsType;
import net.seagis.cat.csw.SummaryRecordType;
import net.seagis.cat.csw.TransactionResponseType;
import net.seagis.cat.csw.TransactionSummaryType;
import net.seagis.cat.csw.TransactionType;
import net.seagis.cat.csw.UpdateType;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.dublincore.elements.SimpleLiteral;
import net.seagis.filter.FilterParser;
import net.seagis.ogc.FilterCapabilities;
import net.seagis.ows.v100.AcceptFormatsType;
import net.seagis.ows.v100.AcceptVersionsType;
import net.seagis.ows.v100.DomainType;
import net.seagis.ows.v100.OWSWebServiceException;
import net.seagis.ows.v100.Operation;
import net.seagis.ows.v100.OperationsMetadata;
import net.seagis.ows.v100.SectionsType;
import net.seagis.ows.v100.ServiceIdentification;
import net.seagis.ows.v100.ServiceProvider;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import static net.seagis.ows.OWSExceptionCode.*;
import static net.seagis.coverage.wms.MetadataReader.*;


//geotols dependencies
import org.geotools.metadata.iso.MetaDataImpl;

//mdweb model dependencies
import org.mdweb.model.schemas.Standard; 
import org.mdweb.model.storage.Form; 
import org.mdweb.sql.v20.Reader20; 

import org.mdweb.sql.v20.Writer20;
import org.opengis.metadata.identification.Identification;
import org.postgresql.ds.PGSimpleDataSource;


/**
 *
 * @author Guilhem Legal
 */
public class CSWworker {

    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("net.seagis.covrage.wms");
    
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
     * A JAXB factory to csw object 
     */
    private final ObjectFactory cswFactory;
    
    /**
     * The current MIME type of return
     */
    private String outputFormat;
    
    /**
     * A marshaller to send xml to mdweb.
     */
    private final Marshaller marshaller;
    
    /**
     * A lucene index to make quick search on the metadatas.
     */
    private final IndexLucene index;
    
    /**
     * A filter parser whitch create lucene query from OGC filter
     */
    private FilterParser filterParser;
    
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
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLongitude");
        ISO_QUERYABLE.put("NorthBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLongitude");
        ISO_QUERYABLE.put("SouthBoundLongitude",     paths);
        
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
    private static Map<String, List<String>> DUBLIN_CORE_QUERYABLE;
    static {
        DUBLIN_CORE_QUERYABLE = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        paths.add("Catalog Web Service:Record:title");
        DUBLIN_CORE_QUERYABLE.put("title", paths);
        
        //TODO verify codelist=originator
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:creator");
        DUBLIN_CORE_QUERYABLE.put("creator", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        paths.add("Catalog Web Service:Record:subject");
        DUBLIN_CORE_QUERYABLE.put("description", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract");
        paths.add("Catalog Web Service:Record:abstract");
        DUBLIN_CORE_QUERYABLE.put("abstract", paths);
        
        //TODO verify codelist=publisher
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:publisher");
        DUBLIN_CORE_QUERYABLE.put("publisher", paths);
        
        //TODO verify codelist=contributor
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("Catalog Web Service:Record:contributor");
        DUBLIN_CORE_QUERYABLE.put("contributor", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dateStamp");
        paths.add("Catalog Web Service:Record:date");
        DUBLIN_CORE_QUERYABLE.put("date", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:hierarchyLevel");
        paths.add("Catalog Web Service:Record:type");
        DUBLIN_CORE_QUERYABLE.put("type", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        paths.add("Catalog Web Service:Record:format");
        DUBLIN_CORE_QUERYABLE.put("format", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("Catalog Web Service:Record:identifier");
        DUBLIN_CORE_QUERYABLE.put("identifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("Catalog Web Service:Record:source");
        DUBLIN_CORE_QUERYABLE.put("source", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:language");
        paths.add("Catalog Web Service:Record:language");
        DUBLIN_CORE_QUERYABLE.put("language", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:aggregationInfo");
        paths.add("Catalog Web Service:Record:relation");
        DUBLIN_CORE_QUERYABLE.put("relation", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:accessConstraints");
        paths.add("Catalog Web Service:Record:rights");
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
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:UpperCorner");
        DUBLIN_CORE_QUERYABLE.put("NorthBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:LowerCorner");
        DUBLIN_CORE_QUERYABLE.put("SouthBoundLongitude",     paths);
    }
    
    /**
     * A list of supported MIME type 
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
     * a QName for csw:Record type
     */
    private final static QName _Record_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Record");
    
    /**
     * a QName for gmd:MD_Metadata type
     */
    private final static QName _Metadata_QNAME = new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata");
    
    /**
     * a QName for gmd:MD_Metadata type
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
     * Build a new CSW worker
     * 
     * @param marshaller A JAXB marshaller to send xml to MDWeb
     * 
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public CSWworker(Marshaller marshaller) throws IOException, SQLException {
        
        this.marshaller = marshaller;
        cswFactory      = new ObjectFactory();
        Properties prop = new Properties();
        File f          = null;
        String env      = "/home/tomcat/.sicade" ; //System.getenv("CATALINA_HOME");
        logger.info("Path to config file=" + env);
        boolean start = true;
        try {
            // we get the configuration file
            f = new File(env + "/csw_configuration/config.properties");
            FileInputStream in = new FileInputStream(f);
            prop.load(in);
            in.close();
            
        } catch (FileNotFoundException e) {
            if (f != null) {
                logger.severe(f.getPath());
            }
            logger.severe("The CSW service is not working!"                       + '\n' + 
                          "cause: The srevice can not load the properties files!" + '\n' + 
                          "cause: " + e.getMessage());
            start = false;
        }
        
        //we create a connection to the metadata database
        if (start) {
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
                databaseReader   = null;
                databaseWriter   = null;
                index            = null;
                MDReader         = null;
                MDWriter         = null;
            } else {
                 databaseReader  = new Reader20(Standard.ISO_19115,  MDConnection);
                 databaseWriter  = new Writer20(MDConnection);
                 index           = new IndexLucene(databaseReader);
                 MDReader        = new MetadataReader(databaseReader, dataSourceMD.getConnection());
                 MDWriter        = new MetadataWriter(databaseReader, databaseWriter);
                 logger.info("CSW service running");
            }
            
        } else {
            databaseReader  = null;
            databaseWriter  = null;
            index           = null;
            MDReader        = null;
            MDWriter        = null;
            MDConnection    = null;
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
            throw new OWSWebServiceException("accepted format : text/xml",
                                             INVALID_PARAMETER_VALUE, "acceptFormats",
                                             version);
        }
        
        //we prepare the response document
        Capabilities c = null; 
        
        ServiceIdentification si = null;
        ServiceProvider       sp = null;
        OperationsMetadata    om = null;
        FilterCapabilities    fc = null;
            
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
            if (om != null)
                WebService.updateOWSURL(om.getOperation(), serviceURL, "CSW");
               
        }
            
        //we enter the information filter capablities.
        if (sections.getSection().contains("Filter_Capabilities") || sections.getSection().contains("All")) {
            
            fc = staticCapabilities.getFilterCapabilities();
        }
            
            
        c = new Capabilities(si, sp, om, "2.0.2", null, fc);
            
        return c;
        
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetRecordsResponseType getRecords(GetRecordsType request) throws WebServiceException {
        verifyBaseRequest(request);
        
        // we initialize the filterParser
        if (filterParser == null) {
            try {
                filterParser = new FilterParser(version);
            } catch (JAXBException ex) {
                 throw new OWSWebServiceException("The server can't build the Filter JAXB Context: " + ex.getMessage(),
                                                   NO_APPLICABLE_CODE, null, version);
            }
        }
        
        //we prepare the response
        GetRecordsResponseType response;
        
        String ID = request.getRequestId();
        
        // we initialize the output format of the response
        String format = request.getOutputFormat();
        if (format != null && isSupportedFormat(format)) {
            outputFormat = format;
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
                        throw new OWSWebServiceException("The typeName " + type.getLocalPart() + " is not supported by the service" ,
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
        ElementSetType set         = ElementSetType.BRIEF;
        if (setName == null) {
            set = setName.getValue();
        }
        SearchResultsType searchResults = null;
        
        //we get the maxRecords wanted and start position
        Integer maxRecord = request.getMaxRecords();
        Integer startPos  = request.getStartPosition();
        
        // build the lucene query from the specified filter
        String luceneQuery = filterParser.getLuceneQuery(query.getConstraint());
        logger.info("Lucene query=" + luceneQuery);
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
        
        try {
            if (outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2")) {
            
                // we return only the number of result matching
                if (resultType.equals(ResultType.HITS)) {
                    searchResults = new SearchResultsType(ID, query.getElementSetName().getValue(), results.size());
                
                // we return a list of Record
                } else if (resultType.equals(ResultType.RESULTS)) {
                
                    List<AbstractRecordType> records = new ArrayList<AbstractRecordType>();
                    for (String id: results) {
                        records.add((AbstractRecordType)MDReader.getMetadata(id, DUBLINCORE, set));
                    }
                    searchResults = new SearchResultsType(ID, 
                                                          query.getElementSetName().getValue(), 
                                                          results.size(),
                                                          records,
                                                          maxRecord);
                        
                // TODO
                } else if (resultType.equals(ResultType.VALIDATE)) {
                    throw new OWSWebServiceException("The service does not yet handle the VALIDATE resultType.",
                                                   NO_APPLICABLE_CODE, "resultType", version);
                }
            } else if (outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
            
                // we return only the number of result matching
                if (resultType.equals(ResultType.HITS)) {
                    searchResults = new SearchResultsType(ID, query.getElementSetName().getValue(), results.size());
                } else if (resultType.equals(ResultType.RESULTS)) {
                
                //TODO
                } else if (resultType.equals(ResultType.VALIDATE)) {
                    throw new OWSWebServiceException("The service does not yet handle the VALIDATE resultType.",
                                                 NO_APPLICABLE_CODE, "resultType", version);
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
        return response;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetRecordByIdResponseType getRecordById(GetRecordByIdType request) throws WebServiceException {
        verifyBaseRequest(request);
        
        // we initialize the output format of the response
        String format = request.getOutputFormat();
        if (format != null && isSupportedFormat(format)) {
            outputFormat = format;
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
            if (!outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2") && 
                !outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
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
            List<JAXBElement<? extends AbstractRecordType>> records = new ArrayList<JAXBElement<? extends AbstractRecordType>>(); 
            for (String id:request.getId()) {
                try {
                    Object o = MDReader.getMetadata(id, DUBLINCORE, set);
                    if (o instanceof BriefRecordType) {
                        records.add(cswFactory.createBriefRecord((BriefRecordType)o));
                    } else if (o instanceof SummaryRecordType) {
                        records.add(cswFactory.createSummaryRecord((SummaryRecordType)o));
                    } else if (o instanceof RecordType) {
                        records.add(cswFactory.createRecord((RecordType)o));
                    }
                    
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
                }
            }
        
            response = new GetRecordByIdResponseType(records, null);
        //we build ISO 19139 object    
        } else if (outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
           List<MetaDataImpl> records = new ArrayList<MetaDataImpl>();
           for (String id:request.getId()) {
                try {
                    Object o = MDReader.getMetadata(id, ISO_19115, set);
                    if (o instanceof MetaDataImpl) {
                        records.add((MetaDataImpl)o);
                    }
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This service has throw an SQLException: " + e.getMessage(),
                                                      NO_APPLICABLE_CODE, "id", version);
                }
           }
        
           response = new GetRecordByIdResponseType(null, records);      
        
        // this case must never append
        } else {
            response = null;
        }
        
                
        return response;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public DescribeRecordResponseType describeRecord(DescribeRecordType request) throws WebServiceException{
        verifyBaseRequest(request);
        
        return new DescribeRecordResponseType();
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetDomainResponseType getDomain(GetDomainType request) throws WebServiceException{
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
        return new GetDomainResponseType(responseList);
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public TransactionResponseType transaction(TransactionType request) throws WebServiceException {
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
                    
                        OutputStreamWriter outstrR = null;
                        try {
                            //we build a temporary file to write the xml
                            File tempFile = File.createTempFile("CSWRecord", "xml");
                            FileOutputStream outstr = new FileOutputStream(tempFile);
                            outstrR = new OutputStreamWriter(outstr, "UTF-8");
                            BufferedWriter output = new BufferedWriter(outstrR);
                            marshaller.marshal(record, output);
                            output.flush();
                            output.close();
                            
                            
                            if (record instanceof JAXBElement) {
                                record = ((JAXBElement)record).getValue();
                            }
                            //here we try to get the title
                            SimpleLiteral titleSL = null;
                            String title = "unknow title";
                            if (record instanceof RecordType) {
                                titleSL = ((RecordType) record).getTitle();
                                if (titleSL == null) {
                                    titleSL = ((RecordType) record).getIdentifier();
                                }
                                
                                if (titleSL == null) {
                                    title = "unknow title";
                                } else {
                                    if (titleSL.getContent() != null && titleSL.getContent().size() > 0)
                                        title = titleSL.getContent().get(0);
                                }
                                
                            
                            } else if (record instanceof MetaDataImpl) {
                                Collection<Identification> idents = ((MetaDataImpl) record).getIdentificationInfo();
                                if (idents.size() != 0) {
                                    Identification ident = idents.iterator().next();
                                    if (ident.getCitation() != null && ident.getCitation().getTitle() != null) {
                                        title = ident.getCitation().getTitle().toString();
                                    } 
                                }
                            } else {
                                logger.severe("unknow type: " + record.getClass().getName() + " unable to find a title");
                            } 
                            Form f = MDWriter.getFormFromObject(record, title);
                            databaseWriter.writeForm(f, false);
                            totalInserted++;
                            
                        } catch (IOException ex) {
                           throw new OWSWebServiceException("This service has throw an IOException: " + ex.getMessage(),
                                                            NO_APPLICABLE_CODE, null, version);
                        } catch (JAXBException ex) {
                            throw new OWSWebServiceException("The request is malFormed(JAXB): " + ex.getMessage(),
                                                             NO_APPLICABLE_CODE, null, version);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            throw new OWSWebServiceException("The service has throw an SQLException: " + ex.getMessage(),
                                                             NO_APPLICABLE_CODE, null, version);
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
        return response;
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public HarvestResponseType harvest(HarvestType request) throws WebServiceException {
        verifyBaseRequest(request);
        
        return new HarvestResponseType();
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
     */
    private boolean isSupportedFormat(String format) {
        return ACCEPTED_OUTPUT_FORMATS.contains(format);
    }
    
    /**
     * Set the current service version
     */
    public void setVersion(ServiceVersion version){
        this.version = version;
        if (MDReader != null) {
            this.MDReader.setVersion(version);
        }
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
     * Verify that the bases request attributes are correct.
     * 
     * @param request an object request with the base attribute (all except GetCapabilities request); 
     */ 
    private void verifyBaseRequest(RequestBaseType request) throws WebServiceException {
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
    
}
