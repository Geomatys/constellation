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
import java.util.logging.Logger;

// JAX-WS dependencies
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

// constellation dependencies
import org.constellation.ws.CstlServiceException;
import org.constellation.metadata.CSWworker;

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
     * a service worker
     */
    private CSWworker worker;

    /**
     * Initialize the database connection.
     */
    public CSWService() {
       worker = new CSWworker("", null);
       //TODO find real url
       worker.setServiceUrl("http://localhost:8080/CSWServer/CSWService");
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
            return worker.getCapabilities(requestCapabilities);
            
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), requestCapabilities.getVersion().toString());
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
}


