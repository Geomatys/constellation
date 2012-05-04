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
import java.util.logging.Level;

// JAX-WS dependencies
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

// constellation dependencies
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.BindingType;
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.CstlServiceException;
import org.constellation.metadata.CSWworker;

//geotoolkit dependencies
import org.constellation.ws.soap.OGCWebService;
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
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
@XmlSeeAlso({org.geotoolkit.metadata.iso.DefaultMetadata.class,
             org.geotoolkit.internal.jaxb.geometry.ObjectFactory.class,
             org.geotoolkit.metadata.fra.FRA_Constraints.class,
             org.geotoolkit.metadata.fra.FRA_DataIdentification.class,
             org.geotoolkit.metadata.fra.FRA_DirectReferenceSystem.class,
             org.geotoolkit.metadata.fra.FRA_IndirectReferenceSystem.class,
             org.geotoolkit.metadata.fra.FRA_LegalConstraints.class,
             org.geotoolkit.metadata.fra.FRA_SecurityConstraints.class,
             org.geotoolkit.metadata.fra.FRA_DirectReferenceSystem.class,
             org.geotoolkit.service.ServiceIdentificationImpl.class,
             org.geotoolkit.feature.catalog.AssociationRoleImpl.class,
             org.geotoolkit.feature.catalog.BindingImpl.class,
             org.geotoolkit.feature.catalog.BoundFeatureAttributeImpl.class,
             org.geotoolkit.feature.catalog.ConstraintImpl.class,
             org.geotoolkit.feature.catalog.DefinitionReferenceImpl.class,
             org.geotoolkit.feature.catalog.DefinitionSourceImpl.class,
             org.geotoolkit.feature.catalog.FeatureAssociationImpl.class,
             org.geotoolkit.feature.catalog.FeatureAttributeImpl.class,
             org.geotoolkit.feature.catalog.FeatureCatalogueImpl.class,
             org.geotoolkit.feature.catalog.FeatureOperationImpl.class,
             org.geotoolkit.feature.catalog.FeatureTypeImpl.class,
             org.geotoolkit.feature.catalog.InheritanceRelationImpl.class,
             org.geotoolkit.feature.catalog.ListedValueImpl.class,
             org.geotoolkit.feature.catalog.PropertyTypeImpl.class,
             org.geotoolkit.util.Multiplicity.class})
public class CSWService extends OGCWebService<CSWworker>{
    
    /**
     * Initialize the workers.
     */
    public CSWService() {
      super(Specification.CSW);
      LOGGER.log(Level.INFO, "CSW SOAP service running ({0} instances)\n", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CSWworker createWorker(File instanceDirectory) {
        return new CSWworker(instanceDirectory.getName(), instanceDirectory);
    }

    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     * @throws SOAPServiceException
     */
    @WebMethod(action="getCapabilities")
    @WebResult(name="Capabilities", targetNamespace="http://www.opengis.net/cat/csw/2.0.2")
    public Capabilities getCapabilities(@WebParam(name = "GetCapabilities") GetCapabilitiesType requestCapabilities) throws SOAPServiceException  {
        try {
            LOGGER.info("received SOAP getCapabilities request");
            final CSWworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return worker.getCapabilities(requestCapabilities);
            
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(), requestCapabilities.getVersion().toString());
        }
    }
    
    /**
     * Web service operation 
     */
    @WebMethod(action="getDomain")
    @WebResult(name="GetDomainResponse", targetNamespace="http://www.opengis.net/cat/csw/2.0.2")
    public GetDomainResponseType getDomain(@WebParam(name = "GetDomain") GetDomainType requestGetDomain) throws SOAPServiceException  {
        try {
            LOGGER.info("received SOAP GetDomain request");
            final CSWworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
    @WebResult(name="GetRecordByIdResponse", targetNamespace="http://www.opengis.net/cat/csw/2.0.2")
    public GetRecordByIdResponseType getRecordById(@WebParam(name = "GetRecordById") GetRecordByIdType requestRecordById) throws SOAPServiceException {
        try {
            LOGGER.info("received SOAP getRecordById request");
            final CSWworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
            final CSWworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
    @WebResult(name="DescribeRecordResponse", targetNamespace="http://www.opengis.net/cat/csw/2.0.2")
    public DescribeRecordResponseType describeRecord(@WebParam(name = "DescribeRecord") DescribeRecordType requestDescribeRecord) throws SOAPServiceException {
        try {
            LOGGER.info("received SOAP describeRecord request");
            final CSWworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
    @WebResult(name="HarvestResponse", targetNamespace="http://www.opengis.net/cat/csw/2.0.2")
    public HarvestResponseType harvest(@WebParam(name = "Harvest") HarvestType requestHarvest) throws SOAPServiceException {
        try {
            LOGGER.info("received SOAP harvest request");
            final CSWworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
    @WebResult(name="TransactionResponse", targetNamespace="http://www.opengis.net/cat/csw/2.0.2")
    public TransactionResponseType transaction(@WebParam(name = "Transaction") TransactionType requestTransaction) throws SOAPServiceException {
        try {
            LOGGER.info("received SOAP transaction request");
            final CSWworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return worker.transaction(requestTransaction);
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestTransaction.getVersion());
        }
    }
}


