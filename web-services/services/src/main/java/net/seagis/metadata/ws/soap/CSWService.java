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

package net.seagis.metadata.ws.soap;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.seagis.cat.csw.v202.Capabilities;
import net.seagis.cat.csw.v202.DescribeRecordResponseType;
import net.seagis.cat.csw.v202.DescribeRecordType;
import net.seagis.cat.csw.v202.DistributedSearchType;
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
import net.seagis.cat.csw.v202.QueryConstraintType;
import net.seagis.cat.csw.v202.QueryType;
import net.seagis.cat.csw.v202.ResultType;
import net.seagis.cat.csw.v202.TransactionResponseType;
import net.seagis.cat.csw.v202.TransactionType;
import net.seagis.coverage.web.Service;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.metadata.CSWworker;
import net.seagis.ows.v100.ExceptionReport;
import net.seagis.ows.v100.OWSWebServiceException;
import org.geotools.metadata.iso.MetaDataImpl;

/**
 *
 * @author Guilhem Legal
 */
@WebService(name = "CSWService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public class CSWService {
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("fr.geomatys.sos");
    
    /**
     * A map containing the Capabilities Object already load from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();
    
    /**
     * a service worker
     */
    private CSWworker worker;
    
    /**
     * The user directory where to store the configuration file on Unix platforms.
     */
    private static final String UNIX_DIRECTORY = ".sicade";

    /**
     * The user directory where to store the configuration file on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Sicade";
    
    /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    private Unmarshaller unmarshaller;
    
       
    /**
     * Initialize the database connection.
     */
    public CSWService() throws JAXBException, IOException, IOException, SQLException  {
       
       JAXBContext jbcontext = JAXBContext.newInstance(MetaDataImpl.class, Capabilities.class, DescribeRecordType.class
                        ,DistributedSearchType.class, ElementSetNameType.class, ElementSetType.class
                        ,GetCapabilities.class, GetDomainType.class, GetRecordByIdType.class
                        ,GetRecordsType.class, HarvestType.class, QueryConstraintType.class
                        ,QueryType.class, ResultType.class, TransactionType.class
                        ,GetRecordsResponseType.class, GetRecordByIdResponseType.class
                        ,DescribeRecordResponseType.class, GetDomainResponseType.class
                        ,TransactionResponseType.class, HarvestResponseType.class
                        ,ExceptionReport.class, net.seagis.ows.v110.ExceptionReport.class
                        ,net.seagis.dublincore.v2.terms.ObjectFactory.class);
       
       unmarshaller = jbcontext.createUnmarshaller();
       worker = new CSWworker(unmarshaller, jbcontext.createMarshaller());
       //TODO find real url
       worker.setServiceURL("http://localhost:8080/SOServer/SOService");
       worker.setVersion(new ServiceVersion(Service.OWS, "1.0.0"));
    }
    
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     */
    @WebMethod(action="getCapabilities")
    public Capabilities getCapabilities(@WebParam(name = "GetCapabilities") GetCapabilities requestCapabilities) throws SOAPServiceException  {
        try {
            logger.info("received SOAP getCapabilities request");
            worker.setStaticCapabilities((Capabilities)getCapabilitiesObject());
             
            return worker.getCapabilities(requestCapabilities);
            
        } catch (WebServiceException ex) {
            OWSWebServiceException oex = (OWSWebServiceException)ex; 
            throw new SOAPServiceException(oex.getMessage(), oex.getExceptionCode().name(), oex.getVersion());
        } catch (JAXBException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getErrorCode(), null);
        }
    }
    
    /**
     * Web service operation 
     */
    @WebMethod(action="getDomain")
    public GetDomainResponseType getDomain(@WebParam(name = "GetDomain") GetDomainType requestGetDomain) throws SOAPServiceException  {
        try {
            logger.info("received SOAP GetDomain request");
            return worker.getDomain(requestGetDomain);
        } catch (WebServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), ex.getVersion());
        }
    }
    
    
    /**
     * Web service operation 
     */
    @WebMethod(action="getRecordById")
    public GetRecordByIdResponseType getRecordById(@WebParam(name = "GetRecordById") GetRecordByIdType requestRecordById) throws SOAPServiceException {
        try {
            logger.info("received SOAP getRecordById request");
            return worker.getRecordById(requestRecordById);
        } catch (WebServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), ex.getVersion());
        }
    }
    
    /**
     * Web service operation
     */
    @WebMethod(action="getRecords")
    public Object getRecords(@WebParam(name = "GetRecords") GetRecordsType requestRecords) throws SOAPServiceException {
        try {
            logger.info("received SOAP getRecords request");
            return worker.getRecords(requestRecords);
        } catch (WebServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), ex.getVersion());
        }
    }
    
    /**
     * Web service operation 
     */
    @WebMethod(action="describeRecord")
    public DescribeRecordResponseType describeRecord(@WebParam(name = "DescribeRecord") DescribeRecordType requestDescribeRecord) throws SOAPServiceException {
        try {
            logger.info("received SOAP describeRecord request");
            return worker.describeRecord(requestDescribeRecord);
        } catch (WebServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), ex.getVersion());
        }
    }
    
    /**
     * Web service operation 
     */
    @WebMethod(action="harvest")
    public HarvestResponseType harvest(@WebParam(name = "Harvest") HarvestType requestHarvest) throws SOAPServiceException {
        try {
            logger.info("received SOAP harvest request");
            return worker.harvest(requestHarvest);
        } catch (WebServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), ex.getVersion());
        }
    }
    
    /**
     * Web service operation 
     */
    @WebMethod(action="transaction")
    public TransactionResponseType transaction(@WebParam(name = "Transaction") TransactionType requestTransaction) throws SOAPServiceException {
        try {
            logger.info("received SOAP transaction request");
            return worker.transaction(requestTransaction);
        } catch (WebServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), ex.getVersion());
        }
    }
    
    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param  version the version of the service.
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getCapabilitiesObject() throws JAXBException {
       String fileName = "CSWCapabilities2.0.2.xml";
       
       if (fileName == null) {
           return null;
       } else {
           Object response = capabilities.get(fileName);
           if (response == null) {
           
               String home;
                    
               String env = "/home/tomcat/.sicade"; //System.getenv("CATALINA_HOME");
                // we get the configuration file
               File path = new File(env + "/csw_configuration/");     
               //we delete the /WS
               if (!path.isDirectory()) {
                    home = System.getProperty("user.home");
                    if (System.getProperty("os.name", "").startsWith("Windows")) {
                        path = new File(home, WINDOWS_DIRECTORY);
                    } else {
                        path = new File(home, UNIX_DIRECTORY);
                    }
                } 
            
               File f = new File(path, fileName);
               logger.info(f.toString());
               response = unmarshaller.unmarshal(f);
               capabilities.put(fileName, response);
           }
           
           return response;
        }
    }
}


