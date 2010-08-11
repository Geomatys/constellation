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
import org.constellation.ws.CstlServiceException;
import org.constellation.metadata.CSWworker;
import org.constellation.provider.configuration.ConfigDirectory;

//geotoolkit dependencies
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.DescribeRecordResponseType;
import org.geotoolkit.csw.xml.v202.DescribeRecordType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetDomainResponseType;
import org.geotoolkit.csw.xml.v202.GetDomainType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.HarvestResponseType;
import org.geotoolkit.csw.xml.v202.HarvestType;
import org.geotoolkit.csw.xml.v202.TransactionResponseType;
import org.geotoolkit.csw.xml.v202.TransactionType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.util.FileUtilities;

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
    private static final Logger LOGGER = Logger.getLogger("org.costellation.metadata");
    
    /**
     * A map containing the Capabilities Object already load from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();
    
    /**
     * a service worker
     */
    private CSWworker worker;

    /**
     * Initialize the database connection.
     */
    public CSWService() {
       worker = new CSWworker("", null);
       //TODO find real url
       worker.setServiceURL("http://localhost:8080/CSWServer/CSWService");
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
            LOGGER.info("received SOAP getCapabilities request");
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
            LOGGER.info("received SOAP GetDomain request");
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
            LOGGER.info("received SOAP getRecordById request");
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
            LOGGER.info("received SOAP getRecords request");
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
            LOGGER.info("received SOAP describeRecord request");
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
            LOGGER.info("received SOAP harvest request");
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
            LOGGER.info("received SOAP transaction request");
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
        final String fileName = "CSWCapabilities2.0.2.xml";

        Object response = capabilities.get(fileName);
        if (response == null) {
            final String configUrl = "csw_configuration";
            final File configDir = new File(ConfigDirectory.getConfigDirectory(), configUrl);
            if (configDir.exists()) {
                LOGGER.info("taking configuration from constellation directory: " + configDir.getPath());
            } else {
                return FileUtilities.getDirectoryFromResource(configUrl);
            }
            final File f = new File(configDir, fileName);
            LOGGER.info(f.toString());
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = EBRIMMarshallerPool.getInstance().acquireUnmarshaller();
                response = unmarshaller.unmarshal(f);
                capabilities.put(fileName, response);
            } finally {
                if (unmarshaller != null) {
                    EBRIMMarshallerPool.getInstance().release(unmarshaller);
                }
            }
        }
        return response;
    }
}


