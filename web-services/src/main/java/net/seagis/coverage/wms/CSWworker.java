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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

//seaGIS dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import net.seagis.cat.csw.AbstractRecordType;
import net.seagis.cat.csw.BriefRecordType;
import net.seagis.cat.csw.Capabilities;
import net.seagis.cat.csw.DeleteType;
import net.seagis.cat.csw.DescribeRecordResponseType;
import net.seagis.cat.csw.DescribeRecordType;
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
import net.seagis.cat.csw.ObjectFactory;
import net.seagis.cat.csw.RecordType;
import net.seagis.cat.csw.RequestBaseType;
import net.seagis.cat.csw.SummaryRecordType;
import net.seagis.cat.csw.TransactionResponseType;
import net.seagis.cat.csw.TransactionSummaryType;
import net.seagis.cat.csw.TransactionType;
import net.seagis.cat.csw.UpdateType;
import net.seagis.coverage.web.Service;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.ogc.FilterCapabilities;
import net.seagis.ows.v100.AcceptFormatsType;
import net.seagis.ows.v100.AcceptVersionsType;
import net.seagis.ows.v100.OWSWebServiceException;
import net.seagis.ows.v100.OperationsMetadata;
import net.seagis.ows.v100.SectionsType;
import net.seagis.ows.v100.ServiceIdentification;
import net.seagis.ows.v100.ServiceProvider;
import static net.seagis.ows.OWSExceptionCode.*;
import static net.seagis.coverage.wms.MetadataReader.*;


//geotols dependencies
import org.geotools.metadata.iso.MetaDataImpl;

//mdweb model dependencies
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.users.User;
import org.mdweb.sql.v20.Reader20;

import org.mdweb.sql.v20.Writer20;
import org.mdweb.xml.MalFormedDocumentException;
import org.mdweb.xml.Reader;
import org.opengis.metadata.identification.Identification;
import org.postgresql.ds.PGSimpleDataSource;
import org.xml.sax.SAXException;


/**
 *
 * @author legal
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
     * The data Catalog on whitch we write the data.
     */
    private final Catalog CSWCatalog;
    
    /**
     * The MDWeb user who owe the inserted form.
     */
    private final User user;
    
    
    /**
     * A list of supported MIME type 
     */
    private final static List<String> acceptedOutputFormats;
    static {
        acceptedOutputFormats = new ArrayList<String>();
        acceptedOutputFormats.add("text/xml");
        acceptedOutputFormats.add("application/xml");
        acceptedOutputFormats.add("text/html");
        acceptedOutputFormats.add("text/plain");
    }
    
    public CSWworker(Marshaller marshaller) throws IOException, SQLException {
        
        this.marshaller = marshaller;
        cswFactory      = new ObjectFactory();
        Properties prop = new Properties();
        File f          = null;
        String env      = "/home/tomcat/.sicade" ; //System.getenv("CATALINA_HOME");
        logger.info("CATALINA_HOME=" + env);
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
            logger.severe("The sevice can not load the properties files" + '\n' + 
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
            databaseReader  = new Reader20(Standard.ISO_19115,  dataSourceMD.getConnection());
            databaseWriter  = new Writer20(dataSourceMD.getConnection());
        
            if (dataSourceMD.getConnection() == null) {
                logger.severe("THE WEB SERVICE CAN'T CONNECT TO THE METADATA DB!");
            }
            MDReader   = new MetadataReader(databaseReader, dataSourceMD.getConnection());
            CSWCatalog = databaseReader.getCatalog("CSWCat");
            user       = databaseReader.getUser("admin");
        } else {
            databaseReader  = null;
            databaseWriter  = null;
            MDReader        = null;
            CSWCatalog      = null;
            user            = null;
            logger.severe("The CSW service is not working");
        }
        //TODO inititalize version
        version    = new ServiceVersion(Service.OWS, "2.0.2");
        
        
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
    public GetRecordsResponseType getRecords(GetRecordsType request){
        
        return new GetRecordsResponseType();
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetRecordByIdResponseType getRecordById(GetRecordByIdType request) throws WebServiceException {
        verifyBaseRequest(request);
        
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
        
        
        
        return new GetDomainResponseType();
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
                            String title = "unknow title";
                            if (record instanceof RecordType) {
                                title = ((RecordType) record).getTitle();
                                if (title == null) {
                                    title = ((RecordType) record).getIdentifier();
                                }
                                if (title == null) {
                                    title = "unknow title";
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
                            Reader XMLReader = new Reader(databaseReader, tempFile, databaseWriter);
                            Form f           = XMLReader.readForm(CSWCatalog, 
                                                                  user, 
                                                                  "seagis CSW", 
                                                                  title,
                                                                  databaseReader.getStandard("Catalog Web Service"));
                            
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
                            
                        } catch (ParserConfigurationException ex) {
                            throw new OWSWebServiceException("The request is malFormed(ParserConfiguration): " + ex.getMessage() ,
                                                             NO_APPLICABLE_CODE, null, version);
                        } catch (SAXException ex) {
                            throw new OWSWebServiceException("The request is malFormed(SAX): " + ex.getMessage(),
                                                             NO_APPLICABLE_CODE, null, version);
                        } catch (MalFormedDocumentException ex) {
                            throw new OWSWebServiceException("The request is malFormed: " + ex.getMessage(),
                                                             NO_APPLICABLE_CODE, null, version);
                        }
                        
                }
            } else if (transaction instanceof DeleteType) {
                DeleteType deleteRequest = (DeleteType)transaction;
                
                
            } else if (transaction instanceof UpdateType) {
                UpdateType updateRequest = (UpdateType)transaction;
            
                
            } else {
                throw new OWSWebServiceException("This kind of transaction is not supported by the service.",
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
        return acceptedOutputFormats.contains(format);
    }
    
    /**
     * Set the current service version
     */
    public void setVersion(ServiceVersion version){
        this.version = version;
        this.MDReader.setVersion(version);
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
