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
package org.constellation.metadata.ws.soap;

// J2SE dependencies 
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

// JAX-WS dependencies
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


// constellation dependencies
import org.constellation.cat.csw.v202.Capabilities;
import org.constellation.cat.csw.v202.DescribeRecordResponseType;
import org.constellation.cat.csw.v202.DescribeRecordType;
import org.constellation.cat.csw.v202.DistributedSearchType;
import org.constellation.cat.csw.v202.ElementSetNameType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.GetCapabilitiesType;
import org.constellation.cat.csw.v202.GetDomainResponseType;
import org.constellation.cat.csw.v202.GetDomainType;
import org.constellation.cat.csw.v202.GetRecordByIdResponseType;
import org.constellation.cat.csw.v202.GetRecordByIdType;
import org.constellation.cat.csw.v202.GetRecordsResponseType;
import org.constellation.cat.csw.v202.GetRecordsType;
import org.constellation.cat.csw.v202.HarvestResponseType;
import org.constellation.cat.csw.v202.HarvestType;
import org.constellation.cat.csw.v202.QueryConstraintType;
import org.constellation.cat.csw.v202.QueryType;
import org.constellation.cat.csw.v202.ResultType;
import org.constellation.cat.csw.v202.TransactionResponseType;
import org.constellation.cat.csw.v202.TransactionType;
import org.constellation.ws.CstlServiceException;
import org.constellation.metadata.CSWworker;
import org.constellation.ows.v100.ExceptionReport;

//geotools dependencies
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebService(name = "CSWService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public class CSWService {
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("org.costellation.metadata");
    
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
     * The maximum number of elements in a queue of marshallers and unmarshallers.
     */
    private static final int MAX_QUEUE_SIZE = 4;

    /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    private MarshallerPool marshallerPool;
    
    /**
     * Initialize the database connection.
     */
    public CSWService() {

       try {
           marshallerPool =
                   new MarshallerPool(DefaultMetaData.class, Capabilities.class, DescribeRecordType.class
                            ,DistributedSearchType.class, ElementSetNameType.class, ElementSetType.class
                            ,GetCapabilitiesType.class, GetDomainType.class, GetRecordByIdType.class
                            ,GetRecordsType.class, HarvestType.class, QueryConstraintType.class
                            ,QueryType.class, ResultType.class, TransactionType.class
                            ,GetRecordsResponseType.class, GetRecordByIdResponseType.class
                            ,DescribeRecordResponseType.class, GetDomainResponseType.class
                            ,TransactionResponseType.class, HarvestResponseType.class
                            ,ExceptionReport.class, org.constellation.ows.v110.ExceptionReport.class
                            ,org.constellation.dublincore.v2.terms.ObjectFactory.class);

           worker = new CSWworker("", marshallerPool);
           //TODO find real url
           worker.setServiceURL("http://localhost:8080/SOServer/SOService");
       } catch (JAXBException ex){
           logger.severe("The CSW service is not running."       + '\n' +
                         " cause  : Error creating XML context." + '\n' +
                         " error  : " + ex.getMessage()          + '\n' +
                         " details: " + ex.toString());
        }
    }
    
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     * @throws SOAPServiceException
     */
    @WebMethod(action="getCapabilities")
    public Capabilities getCapabilities(@WebParam(name = "GetCapabilities") GetCapabilitiesType requestCapabilities) throws SOAPServiceException  {
        try {
            logger.info("received SOAP getCapabilities request");
            worker.setSkeletonCapabilities((Capabilities)getCapabilitiesObject());
             
            return worker.getCapabilities(requestCapabilities);
            
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), requestCapabilities.getVersion().toString());
        } catch (JAXBException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getErrorCode(), requestCapabilities.getVersion().toString());
        }
    }
    
    /**
     * Web service operation 
     */
    @WebMethod(action="getDomain")
    public GetDomainResponseType getDomain(@WebParam(name = "GetDomain") GetDomainType requestGetDomain) throws SOAPServiceException  {
        try {
            logger.info("received SOAP GetDomain request");
            return (GetDomainResponseType) worker.getDomain(requestGetDomain);
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestGetDomain.getVersion());
        }
    }
    
    
    /**
     * Web service operation 
     */
    @WebMethod(action="getRecordById")
    public GetRecordByIdResponseType getRecordById(@WebParam(name = "GetRecordById") GetRecordByIdType requestRecordById) throws SOAPServiceException {
        try {
            logger.info("received SOAP getRecordById request");
            return (GetRecordByIdResponseType) worker.getRecordById(requestRecordById);
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestRecordById.getVersion());
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
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestRecords.getVersion());
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
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestDescribeRecord.getVersion());
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
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestHarvest.getVersion());
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
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestTransaction.getVersion());
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
               Unmarshaller unmarshaller = null;
               try {
                   unmarshaller = marshallerPool.acquireUnmarshaller();
                   response = unmarshaller.unmarshal(f);
                   capabilities.put(fileName, response);
               } finally {
                   if (unmarshaller != null) {
                       marshallerPool.release(unmarshaller);
                   }
               }
           }
           
           return response;
        }
    }
}


