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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

//seaGIS dependencies
import javax.xml.bind.JAXBElement;
import net.seagis.cat.csw.AbstractRecordType;
import net.seagis.cat.csw.Capabilities;
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
import net.seagis.cat.csw.RequestBaseType;
import net.seagis.cat.csw.TransactionResponseType;
import net.seagis.cat.csw.TransactionType;
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


//geotols dependencies
import org.geotools.metadata.iso.MetaDataImpl;

//mdweb model dependencies
import org.mdweb.model.schemas.Standard;
import org.mdweb.sql.v20.Reader20;

import org.postgresql.ds.PGSimpleDataSource;


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
    private Reader20 DatabaseReader;
    
    /**
     * An object creator from the MDWeb database.
     */
    private MetadataReader MDReader;
    
    public CSWworker() throws IOException, SQLException {
        
        Properties prop = new Properties();
        File f = null;
        String env = "/home/tomcat/.sicade" ; //System.getenv("CATALINA_HOME");
        logger.info("CATALINA_HOME=" + env);
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
            return;
        }
        //we create a connection to the metadata database
        PGSimpleDataSource dataSourceMD = new PGSimpleDataSource();
        dataSourceMD.setServerName(prop.getProperty("MDDBServerName"));
        dataSourceMD.setPortNumber(Integer.parseInt(prop.getProperty("MDDBServerPort")));
        dataSourceMD.setDatabaseName(prop.getProperty("MDDBName"));
        dataSourceMD.setUser(prop.getProperty("MDDBUser"));
        dataSourceMD.setPassword(prop.getProperty("MDDBUserPassword"));
        DatabaseReader  = new Reader20(Standard.ISO_19115,  dataSourceMD.getConnection());
        if (dataSourceMD.getConnection() == null) {
            logger.severe("THE WEB SERVICE CAN'T CONNECT TO THE METADATA DB!");
        }
        MDReader = new MetadataReader(DatabaseReader);
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
        
        //we build ISO 19139 object
        if (outputSchema.equals("http://www.opengis.net/cat/csw/2.0.2")) {
            List<JAXBElement<? extends AbstractRecordType>> records = new ArrayList<JAXBElement<? extends AbstractRecordType>>(); 
            for (String id:request.getId()) {
                try {
                    Object o = MDReader.getMetadata(id);
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This identifier" + id + "does not exist",
                                                      INVALID_PARAMETER_VALUE, "id", version);
                }
            }
        
            response = new GetRecordByIdResponseType(records, null);
        //we build dublin core object    
        } else if (outputSchema.equals("http://www.isotc211.org/2005/gmd")) {
           List<MetaDataImpl> records = new ArrayList<MetaDataImpl>();
           for (String id:request.getId()) {
                try {
                    Object o = MDReader.getMetadata(id);
                } catch (SQLException e) {
                    throw new OWSWebServiceException("This identifier" + id + "does not exist",
                                                      INVALID_PARAMETER_VALUE, "id", version);
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
    public DescribeRecordResponseType describeRecord(DescribeRecordType request){
        
        return new DescribeRecordResponseType();
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public GetDomainResponseType getDomain(GetDomainType request){
        
        return new GetDomainResponseType();
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public TransactionResponseType transaction(TransactionType request){
        
        return new TransactionResponseType();
    }
    
    /**
     * TODO
     * 
     * @param request
     * @return
     */
    public HarvestResponseType harvest(HarvestType request){
        
        return new HarvestResponseType();
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
