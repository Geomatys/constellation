/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.metadata.ws.soap;

// J2SE dependencies
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
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingType;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.metadata.configuration.CSWConfigurer;
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
@XmlSeeAlso({org.apache.sis.metadata.iso.DefaultMetadata.class,
             org.apache.sis.internal.jaxb.geometry.ObjectFactory.class,
             org.apache.sis.internal.profile.fra.Constraints.class,
             org.apache.sis.internal.profile.fra.DataIdentification.class,
             org.apache.sis.internal.profile.fra.DirectReferenceSystem.class,
             org.apache.sis.internal.profile.fra.IndirectReferenceSystem.class,
             org.apache.sis.internal.profile.fra.LegalConstraints.class,
             org.apache.sis.internal.profile.fra.SecurityConstraints.class,
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
    protected CSWworker createWorker(String id) {
        return new CSWworker(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return CSWworker.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends ServiceConfigurer> getConfigurerClass() {
        return CSWConfigurer.class;
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
            return (Capabilities) worker.getCapabilities(requestCapabilities);

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
            return (DescribeRecordResponseType) worker.describeRecord(requestDescribeRecord);
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
            return (HarvestResponseType) worker.harvest(requestHarvest);
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
            return (TransactionResponseType) worker.transaction(requestTransaction);
        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestTransaction.getVersion());
        }
    }

    @Override
    protected Object treatIncomingRequest(Object objectRequest, CSWworker worker) throws CstlServiceException {
        throw new UnsupportedOperationException("TODO.");
    }

    @Override
    protected SOAPMessage processExceptionResponse(String message, String code, String locator) {
        throw new UnsupportedOperationException("TODO");
    }
}


