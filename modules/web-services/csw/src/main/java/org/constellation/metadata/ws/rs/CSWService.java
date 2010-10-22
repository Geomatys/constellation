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
package org.constellation.metadata.ws.rs;

// java se dependencies
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

// jersey dependencies
import com.sun.jersey.spi.resource.Singleton;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.apache.xml.serialize.XMLSerializer;

//JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.ws.CstlServiceException;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.utils.SerializerResponse;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.OGCWebService;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.CSWResponse;
import org.geotoolkit.csw.xml.DescribeRecord;
import org.geotoolkit.csw.xml.GetCapabilities;
import org.geotoolkit.csw.xml.GetDomain;
import org.geotoolkit.csw.xml.GetRecordById;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.Transaction;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.DescribeRecordType;
import org.geotoolkit.csw.xml.v202.DistributedSearchType;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetDomainType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.HarvestType;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortOrderType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.v100.AcceptFormatsType;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.xml.Namespaces;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.metadata.CSWConstants.*;

/**
 * RestFul CSW service.
 * 
 * @author Guilhem Legal
 */
@Path("csw")
@Singleton
public class CSWService extends OGCWebService {
    
    protected Map<String, CSWworker> workers;

    private final String serviceID;

    private final XMLSerializer serializer;
    
    /**
     * Build a new Restfull CSW service.
     */
    public CSWService() {
        this("");
    }

    /**
     * Build a new Restfull CSW service.
     * used by subClasses.
     */
    protected CSWService(final String serviceID) {
        this(null, serviceID);
    }

    /**
     * Build a new Restfull CSW service.
     * used by subClasses.
     */
    protected CSWService(final String serviceID, final Map<String, CSWworker> workers) {
        super(ServiceDef.CSW_2_0_2);
        setXMLContext(EBRIMMarshallerPool.getInstance());
        this.serviceID  = serviceID;
        this.workers    = workers;
        this.serializer = getXMLSerializer();
    }

    /**
     * Build a new Restfull CSW service.
     * used by subClasses.
     */
    protected CSWService(final File configDirectory, String serviceID) {
        super(ServiceDef.CSW_2_0_2);
        setXMLContext(EBRIMMarshallerPool.getInstance());
        this.serviceID         = serviceID;
        final CSWworker worker = new CSWworker(serviceID, configDirectory);
        this.workers           = new HashMap<String, CSWworker>();
        this.serializer        = getXMLSerializer();
        workers.put(serviceID, worker);
    }

    /**
     * This method has to be overriden by child classes.
     * 
     * @return
     */
    protected XMLSerializer getXMLSerializer() {
        return null;
    }

     /**
     * Treat the incomming request and call the right function.
     * 
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this param is {@code null}
     * 
     * @return an xml response.
     * @throw JAXBException
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        final CSWworker worker = workers.get(serviceID);
        return treatIncomingRequest(objectRequest, worker);
    }

    /**
     * Treat the incomming request and call the right function.
     *
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this param is {@code null}
     * @param The worker to call.
      * 
     * @return an xml response.
     * @throw JAXBException
     */
    protected Response treatIncomingRequest(final Object objectRequest, CSWworker worker) throws JAXBException {
        ServiceDef serviceDef = null;
        
        try {
            if (worker != null) {

                worker.setServiceURL(getServiceURL());
                logParameters();

                // if the request is not an xml request we fill the request parameter.
                final RequestBase request;
                if (objectRequest == null) {
                    request = adaptQuery(getParameter("REQUEST", true));
                } else if (objectRequest instanceof RequestBase) {
                    request = (RequestBase) objectRequest;
                } else {
                    throw new CstlServiceException("The operation " +  objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
                }

                serviceDef = getVersionFromNumber(request.getVersion());

                if (request instanceof GetCapabilities) {

                    final GetCapabilities gc = (GetCapabilities)request;
                    return Response.ok(worker.getCapabilities(gc), worker.getOutputFormat()).build();
                }

                if (request instanceof GetRecordsRequest) {

                    final GetRecordsRequest gr = (GetRecordsRequest)request;

                    // we pass the serializer to the messageBodyWriter
                    final SerializerResponse response = new SerializerResponse((CSWResponse) worker.getRecords(gr), serializer);
                    return Response.ok(response, worker.getOutputFormat()).build();

                }

                if (request instanceof GetRecordById) {

                    final GetRecordById grbi = (GetRecordById)request;

                    // we pass the serializer to the messageBodyWriter
                    final SerializerResponse response = new SerializerResponse((CSWResponse) worker.getRecordById(grbi), serializer);
                    return Response.ok(response, worker.getOutputFormat()).build();

                }

                if (request instanceof DescribeRecord) {

                    final DescribeRecord dr = (DescribeRecord)request;
                    return Response.ok(worker.describeRecord(dr), worker.getOutputFormat()).build();
                }

                if (request instanceof GetDomain) {

                    final GetDomain gd = (GetDomain)request;
                    return Response.ok(worker.getDomain(gd), worker.getOutputFormat()).build();
                }

                if (request instanceof Transaction) {

                    final Transaction t = (Transaction)request;
                    return Response.ok(worker.transaction(t), worker.getOutputFormat()).build();
                }

                if (request instanceof HarvestType) {

                    final HarvestType h = (HarvestType)request;
                    return Response.ok(worker.harvest(h), worker.getOutputFormat()).build();
                }

                throw new CstlServiceException("The operation " +  request.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");

            } else {
                throw new CstlServiceException("The CSW service is not running", NO_APPLICABLE_CODE);
            }

        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef);

        } 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) {
        logException(ex);
        
        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        final String version         = serviceDef.exceptionVersion.toString();
        final String code            = getOWSExceptionCodeRepresentation(ex.getExceptionCode());
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(), version);
        return Response.ok(report, MimeType.TEXT_XML).build();
    }


    /**
     * Build request object fom KVP parameters.
     * 
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(String request) throws CstlServiceException {
        
        if ("GetCapabilities".equalsIgnoreCase(request)) {
            return createNewGetCapabilitiesRequest();
        } else if ("GetRecords".equalsIgnoreCase(request)) {
            return createNewGetRecordsRequest();
        } else if ("GetRecordById".equalsIgnoreCase(request)) {
            return createNewGetRecordByIdRequest();
        } else if ("DescribeRecord".equalsIgnoreCase(request)) {
            return createNewDescribeRecordRequest();
        } else if ("GetDomain".equalsIgnoreCase(request)) {
            return createNewGetDomainRequest();
        } else if ("Transaction".equalsIgnoreCase(request)) {
            throw new CstlServiceException("The Operation transaction is not available in KVP", OPERATION_NOT_SUPPORTED, "transaction");
        } else if ("Harvest".equalsIgnoreCase(request)) {
            return createNewHarvestRequest();
        }
        throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
    }

    /**
     * Destroy all the resource and close the connection when the web application is undeployed.
     */
    @PreDestroy
    @Override
    public void destroy() {
        String id = "";
        if (serviceID != null && !serviceID.isEmpty())
            id = '(' + serviceID + ')';

        LOGGER.log(Level.INFO, "Shutting down the REST CSW service facade {0}.", id);
        for (final CSWworker worker : workers.values()) {
            worker.destroy();
        }
        workers.clear();
    }
    
    /**
     * Build a new GetCapabilities request object with the url parameters 
     */
    private GetCapabilities createNewGetCapabilitiesRequest() throws CstlServiceException {
        
        String version = getParameter("acceptVersions", false);
        AcceptVersionsType versions;
        if (version != null) {
            if (version.indexOf(',') != -1) {
                version = version.substring(0, version.indexOf(','));
            } 
            versions = new AcceptVersionsType(version);
        } else {
             versions = new AcceptVersionsType("2.0.2");
        }
                    
        final AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));
                        
        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid. 
        final String section = getParameter("Sections", false);
        List<String> requestedSections = new ArrayList<String>();
        if (section != null && !section.equalsIgnoreCase("All")) {
            final StringTokenizer tokens = new StringTokenizer(section, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (SectionsType.getExistingSections().contains(token)){
                    requestedSections.add(token);
                } else {
                    throw new CstlServiceException("The section " + token + NOT_EXIST,
                                                  INVALID_PARAMETER_VALUE, "Sections");
                }   
            }
        } else {
            //if there is no requested Sections we add all the sections
            requestedSections = SectionsType.getExistingSections();
        }
        final SectionsType sections     = new SectionsType(requestedSections);
        return new GetCapabilitiesType(versions,
                                       sections,
                                       formats,
                                       null,
                                       getParameter(SERVICE, true));
        
    }
    
    
    /**
     * Build a new GetRecords request object with the url parameters 
     */
    private GetRecordsType createNewGetRecordsRequest() throws CstlServiceException {
        
        final String version    = getParameter(VERSION, true);
        final String service    = getParameter(SERVICE, true);
        
        //we get the value of result type, if not set we put default value "HITS"
        final String resultTypeName = getParameter("RESULTTYPE", false);
        ResultType resultType = ResultType.HITS;
        if (resultTypeName != null) {
            try {
                resultType = ResultType.fromValue(resultTypeName);
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The resultType " + resultTypeName + NOT_EXIST,
                                             INVALID_PARAMETER_VALUE, "ResultType");
            }
        }
        
        final String requestID    = getParameter("REQUESTID", false);
        
        String outputFormat = getParameter("OUTPUTFORMAT", false);
        if (outputFormat == null) {
            outputFormat = MimeType.APPLICATION_XML;
        }
        
        String outputSchema = getParameter("OUTPUTSCHEMA", false);
        if (outputSchema == null) {
            outputSchema = Namespaces.CSW_202;
        }
        
        //we get the value of start position, if not set we put default value "1"
        final String startPos = getParameter("STARTPOSITION", false);
        Integer startPosition = Integer.valueOf("1");
        if (startPos != null) {
            try {
                startPosition = Integer.valueOf(startPos);
            } catch (NumberFormatException e){
               throw new CstlServiceException("The positif integer " + startPos + MALFORMED,
                                             INVALID_PARAMETER_VALUE, "startPosition");
            }
        } 
        
        //we get the value of max record, if not set we put default value "10"
        final String maxRec = getParameter("MAXRECORDS", false);
        Integer maxRecords= Integer.valueOf("10");
        if (maxRec != null) {
            try {
                maxRecords = Integer.valueOf(maxRec);
            } catch (NumberFormatException e){
               throw new CstlServiceException("The positif integer " + maxRec + MALFORMED,
                                             INVALID_PARAMETER_VALUE, "maxRecords");
            }
        } 
        
        /*
         * here we build the "Query" object 
         */
        
        // we get the namespaces.
        final String namespace               = getParameter("NAMESPACE", false);
        final Map<String, String> namespaces = extractNamespace(namespace);
        
        //if there is not namespace specified, using the default namespace
        if (namespaces.isEmpty()) {
            namespaces.put("csw", Namespaces.CSW_202);
            namespaces.put("gmd", Namespaces.GMD);
        }
        
        final String names          = getParameter("TYPENAMES", true);
        final List<QName> typeNames = new ArrayList<QName>();
        StringTokenizer tokens = new StringTokenizer(names, ",;");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();

            if (token.indexOf(':') != -1) {
                final String prefix = token.substring(0, token.indexOf(':'));
                final String localPart = token.substring(token.indexOf(':') + 1);
                typeNames.add(new QName(namespaces.get(prefix), localPart, prefix));
            } else {
                throw new CstlServiceException("The QName " + token + MALFORMED,
                        INVALID_PARAMETER_VALUE, NAMESPACE);
            }
        }
        
        final String eSetName     = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The ElementSet Name " + eSetName + NOT_EXIST,
                                                INVALID_PARAMETER_VALUE, "ElementSetName");
            }
        }
        
        //we get the list of sort by object
        final String sort                  = getParameter("SORTBY", false);
        final List<SortPropertyType> sorts = new ArrayList<SortPropertyType>();
        SortByType sortBy = null;
        if (sort != null) {
            tokens = new StringTokenizer(sort, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                
                if (token.indexOf(':') != -1) {
                    final String propName    = token.substring(0, token.indexOf(':'));
                    final String order       = token.substring(token.indexOf(':') + 1);
                    SortOrderType orderType;
                    try {
                        orderType = SortOrderType.fromValue(order);
                    } catch (IllegalArgumentException e){
                        throw new CstlServiceException("The SortOrder Name " + order + NOT_EXIST,
                                                      INVALID_PARAMETER_VALUE, "SortBy");
                    }
                    sorts.add(new SortPropertyType(propName, orderType));
                } else {
                     throw new CstlServiceException("The expression " + token + MALFORMED,
                                                      INVALID_PARAMETER_VALUE, "SortBy");
                }
            }
            sortBy = new SortByType(sorts);
        }
        
        /*
         * here we build the constraint object
         */ 
        final String constLanguage     = getParameter("CONSTRAINTLANGUAGE", false);
        QueryConstraintType constraint = null;
        if (constLanguage != null) {
            final String languageVersion  = getParameter("CONSTRAINT_LANGUAGE_VERSION", true);
            
            if (constLanguage.equalsIgnoreCase("CQL_TEXT")) {

                String constraintObject = getParameter("CONSTRAINT", false);
                if (constraintObject == null) {
                    constraintObject = "AnyText LIKE '%%'";
                }
                constraint = new QueryConstraintType(constraintObject, languageVersion);
                
            } else if (constLanguage.equalsIgnoreCase("FILTER")) {
                final Object constraintObject = getComplexParameter("CONSTRAINT", false);
                if (constraintObject == null) {
                    //final PropertyIsLikeType filter = new PropertyIsLikeType(new PropertyNameType("AnyText"), "%%", "%", "?", "\\");
                    //constraintObject = new FilterType(filter);
                } else if (constraintObject instanceof FilterType){
                    constraint = new QueryConstraintType((FilterType)constraintObject, languageVersion);
                } else {
                    throw new CstlServiceException("The filter type is not supported:" + constraintObject.getClass().getName(),
                                                 INVALID_PARAMETER_VALUE, "Constraint");
                }
                
            } else {
                throw new CstlServiceException("The constraint language " + constLanguage + " is not supported",
                                                 INVALID_PARAMETER_VALUE, "ConstraintLanguage");
            }
        }
        
        final QueryType query = new QueryType(typeNames,
                                        new ElementSetNameType(elementSet),
                                        sortBy,
                                        constraint);
        
        /*
         * here we build a optionnal ditributed search object
         */  
        final String distrib = getParameter("DISTRIBUTEDSEARCH", false);
        DistributedSearchType distribSearch = null;
        if (distrib != null && distrib.equalsIgnoreCase("true")) {
            final String count = getParameter("HOPCOUNT", false);
            Integer hopCount   = 2;
            if (count != null) {
                try {
                    hopCount = Integer.parseInt(count);
                } catch (NumberFormatException e){
                    throw new CstlServiceException("The positif integer " + count + MALFORMED,
                                                  INVALID_PARAMETER_VALUE, "HopCount");
                }
            }
            distribSearch = new DistributedSearchType(hopCount);
        }
        
        // TODO not implemented yet
        // String handler = getParameter("RESPONSEHANDLER", false);
        
        return new GetRecordsType(service, 
                                  version, 
                                  resultType,
                                  requestID,
                                  outputFormat,
                                  outputSchema,
                                  startPosition,
                                  maxRecords,
                                  query,
                                  distribSearch);
            
    }

    /**
     * Build a new GetRecordById request object with the url parameters 
     */
    private GetRecordByIdType createNewGetRecordByIdRequest() throws CstlServiceException {
    
        final String version    = getParameter(VERSION, true);
        final String service    = getParameter(SERVICE, true);
        
        String eSetName         = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                eSetName = eSetName.toLowerCase();
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The ElementSet Name " + eSetName + NOT_EXIST,
                                             INVALID_PARAMETER_VALUE, "ElementSetName");
            }
        }
        
        String outputFormat = getParameter("OUTPUTFORMAT", false);
        if (outputFormat == null) {
            outputFormat = MimeType.APPLICATION_XML;
        }
        
        String outputSchema = getParameter("OUTPUTSCHEMA", false);
        if (outputSchema == null) {
            outputSchema = Namespaces.CSW_202;
        }
        
        final String ids             = getParameter("ID", true);
        final List<String> id        = new ArrayList<String>();
        final StringTokenizer tokens = new StringTokenizer(ids, ",;");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            id.add(token);
        }
        
        return new GetRecordByIdType(service, 
                                     version,
                                     new ElementSetNameType(elementSet),
                                     outputFormat,
                                     outputSchema,
                                     id);
                                     
                                     
    }
    
    /**
     * Build a new DescribeRecord request object with the url parameters 
     */
    private DescribeRecordType createNewDescribeRecordRequest() throws CstlServiceException {
    
        final String version    = getParameter(VERSION, true);
        final String service    = getParameter(SERVICE, true);
        
        String outputFormat = getParameter("OUTPUTFORMAT", false);
        if (outputFormat == null) {
            outputFormat = MimeType.APPLICATION_XML;
        }
        
        String schemaLanguage = getParameter("SCHEMALANGUAGE", false);
        if (schemaLanguage == null) {
            schemaLanguage = "XMLSCHEMA";
        }
        
         // we get the namespaces.
        final String namespace               = getParameter("NAMESPACE", false);
        final Map<String, String> namespaces = extractNamespace(namespace);
        
        //if there is not namespace specified, using the default namespace
        // TODO add gmd...
        if (namespaces.isEmpty()) {
            namespaces.put("csw", Namespaces.CSW_202);
            namespaces.put("gmd", Namespaces.GMD);
        }
        
        final String names           = getParameter("TYPENAMES", true);
        final List<QName> typeNames  = new ArrayList<QName>();
        final StringTokenizer tokens = new StringTokenizer(names, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                
                if (token.indexOf(':') != -1) {
                    final String prefix    = token.substring(0, token.indexOf(':'));
                    final String localPart = token.substring(token.indexOf(':') + 1);
                    typeNames.add(new QName(namespaces.get(prefix), localPart, null));
                } else {
                     throw new CstlServiceException("The QName " + token + MALFORMED,
                                                   INVALID_PARAMETER_VALUE, NAMESPACE);
                }
        }
        
        
        return new DescribeRecordType(service, 
                                     version,
                                     typeNames,
                                     outputFormat,
                                     schemaLanguage);
                                     
                                     
    }
    
    /**
     * Build a new GetDomain request object with the url parameters 
     */
    private GetDomainType createNewGetDomainRequest() throws CstlServiceException {
    
        final String version    = getParameter(VERSION, true);
        final String service    = getParameter(SERVICE, true);
        
        //not supported by the ISO profile
        final String parameterName = getParameter("PARAMETERNAME", false);
        
        final String propertyName = getParameter("PROPERTYNAME", false);
        if (propertyName != null && parameterName != null) {
            throw new CstlServiceException("One of propertyName or parameterName must be null",
                                          INVALID_PARAMETER_VALUE, "parameterName");
        }
       
        return new GetDomainType(service, version, propertyName, parameterName);
    }
    
    /**
     * Build a new GetDomain request object with the url parameters 
     */
    private HarvestType createNewHarvestRequest() throws CstlServiceException {
    
        final String version      = getParameter(VERSION, true);
        final String service      = getParameter(SERVICE, true);
        final String source       = getParameter("SOURCE", true);
        final String resourceType = getParameter("RESOURCETYPE", true);
        String resourceFormat     = getParameter("RESOURCEFORMAT", false);
        if (resourceFormat == null) {
            resourceFormat = MimeType.APPLICATION_XML;
        }
        final String handler      = getParameter("RESPONSEHANDLER", false);
        final String interval     = getParameter("HARVESTINTERVAL", false);
        Duration harvestInterval  = null;
        if (interval != null) {
            try {
                final DatatypeFactory factory = DatatypeFactory.newInstance();
                harvestInterval               = factory.newDuration(interval) ;
            } catch (DatatypeConfigurationException ex) {
                throw new CstlServiceException("The Duration " + interval + MALFORMED,
                                              INVALID_PARAMETER_VALUE, "HarvestInsterval");
            }
        }
        
        return new HarvestType(service, 
                               version, 
                               source, 
                               resourceType, 
                               resourceFormat, 
                               handler, 
                               harvestInterval);
    }
}
