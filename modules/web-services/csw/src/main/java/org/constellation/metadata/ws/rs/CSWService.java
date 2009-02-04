/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

// jersey dependencies
import com.sun.jersey.spi.resource.Singleton;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

//JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.cat.csw.DescribeRecord;
import org.constellation.cat.csw.GetDomain;
import org.constellation.cat.csw.GetRecordById;
import org.constellation.cat.csw.GetRecordsRequest;
import org.constellation.cat.csw.Transaction;
import org.constellation.cat.csw.v202.Capabilities;
import org.constellation.cat.csw.v202.DescribeRecordType;
import org.constellation.cat.csw.v202.DistributedSearchType;
import org.constellation.cat.csw.v202.ElementSetNameType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.GetCapabilities;
import org.constellation.cat.csw.v202.GetDomainType;
import org.constellation.cat.csw.v202.GetRecordByIdType;
import org.constellation.cat.csw.v202.GetRecordsType;
import org.constellation.cat.csw.v202.HarvestType;
import org.constellation.cat.csw.v202.QueryConstraintType;
import org.constellation.cat.csw.v202.QueryType;
import org.constellation.cat.csw.v202.ResultType;
import org.constellation.cat.wrs.v100.ExtrinsicObjectType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.CstlServiceException;
import org.constellation.ogc.FilterType;
import org.constellation.ogc.SortByType;
import org.constellation.ogc.SortOrderType;
import org.constellation.ogc.SortPropertyType;
import org.constellation.ows.v100.AcceptFormatsType;
import org.constellation.ows.v100.AcceptVersionsType;
import org.constellation.ows.v100.SectionsType;
import org.constellation.ws.ServiceType;
import org.constellation.ebrim.v250.RegistryObjectType;
import org.constellation.ebrim.v300.IdentifiableType;
import org.constellation.metadata.CSWworker;
import org.constellation.util.Util;
import org.constellation.ows.v100.ExceptionReport;
import org.constellation.ws.rs.OGCWebService;
import static org.constellation.ows.OWSExceptionCode.*;

// geotools dependencies
import org.geotools.metadata.iso.MetaDataImpl;

/**
 * RestFul CSW service.
 * 
 * @author Guilhem Legal
 */
@Path("csw")
@Singleton
public class CSWService extends OGCWebService {
    
    private CSWworker worker;

    private final String serviceID;
    
    /**
     * Build a new Restfull CSW service.
     */
    public CSWService() {
        super("CSW", new ServiceVersion(ServiceType.OWS, "2.0.2"));
        serviceID = "";
        try {
            setXMLContext("", getAllClasses());
            worker = new CSWworker(serviceID, unmarshaller, marshaller);
            
        } catch (JAXBException ex){
            LOGGER.severe("The CSW service is not running."       + '\n' +
                          " cause  : Error creating XML context." + '\n' +
                          " error  : " + ex.getMessage()          + '\n' + 
                          " details: " + ex.toString());
        }
    }

    /**
     * Build a new Restfull CSW service.
     * used by subClasses.
     */
    protected CSWService(String serviceID) {
        super("CSW", new ServiceVersion(ServiceType.OWS, "2.0.2"));
        this.serviceID = serviceID;
        try {
            setXMLContext("", getAllClasses());
            worker = new CSWworker(serviceID, unmarshaller, marshaller);

        } catch (JAXBException ex){
            LOGGER.severe("The CSW service is not running."       + '\n' +
                          " cause  : Error creating XML context." + '\n' +
                          " error  : " + ex.getMessage()          + '\n' +
                          " details: " + ex.toString());
        }
    }

    /**
     * Return the list of all the marshallable classes
     * 
     * @return
     */
    private Class[] getAllClasses() {
        List<Class> classeList = new ArrayList<Class>();
        //ISO 19115 class
        classeList.add(MetaDataImpl.class);

        //ISO 19115 French profile class
        classeList.add(org.constellation.metadata.fra.ObjectFactory.class);

        //CSW 2.0.2 classes
        classeList.addAll(Arrays.asList(org.constellation.cat.csw.v202.ObjectFactory.class,
                                        ExceptionReport.class,
                                        org.constellation.ows.v110.ExceptionReport.class,  // TODO remove
                                        org.constellation.dublincore.v2.terms.ObjectFactory.class));
            
           //CSW 2.0.0 classes
           classeList.addAll(Arrays.asList(org.constellation.cat.csw.v200.ObjectFactory.class,
                                           org.constellation.dublincore.v1.terms.ObjectFactory.class));
           
           //Ebrim classes
           classeList.add(IdentifiableType.class);
           classeList.add(ExtrinsicObjectType.class);
           classeList.add(org.constellation.ebrim.v300.ObjectFactory.class);
           classeList.add(org.constellation.cat.wrs.v100.ObjectFactory.class);
           
           classeList.add(RegistryObjectType.class);
           classeList.add(org.constellation.ebrim.v250.ObjectFactory.class);
           classeList.add(org.constellation.cat.wrs.v090.ObjectFactory.class);
           
           // we add the extensions classes
           classeList.addAll(loadExtensionsClasses());
            
            
            
           return Util.toArray(classeList);
    }
    
     /**
     * Treat the incomming request and call the right function.
     * 
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this param is {@code null}
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        try {
            
            if (worker != null) {
            
                worker.setServiceURL(getServiceURL());
                logParameters();
                String request = "";
                
                if (objectRequest instanceof JAXBElement) {
                    objectRequest = ((JAXBElement)objectRequest).getValue();
                }
                
                // if the request is not an xml request we fill the request parameter.
                if (objectRequest == null) {
                    request = (String) getParameter("REQUEST", true);
                } 

                if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                
                    GetCapabilities gc = (GetCapabilities)objectRequest;
                
                    if (gc == null) {
                         /*
                          * if the parameters have been send by GET or POST kvp,
                          * we build a request object with this parameter.
                          */
                        gc = createNewGetCapabilitiesRequest();
                    }
                    worker.setStaticCapabilities((Capabilities)getStaticCapabilitiesObject());
                
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getCapabilities(gc), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                    
                } else if (request.equalsIgnoreCase("GetRecords") || (objectRequest instanceof GetRecordsRequest)) {
                
                    GetRecordsRequest gr = (GetRecordsRequest)objectRequest;
                
                    if (gr == null) {
                        /*
                        * if the parameters have been send by GET or POST kvp,
                        * we build a request object with this parameter.
                        */
                        gr = createNewGetRecordsRequest();
                    }
                
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getRecords(gr), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                } if (request.equalsIgnoreCase("GetRecordById") || (objectRequest instanceof GetRecordById)) {
                
                    GetRecordById grbi = (GetRecordById)objectRequest;
                
                    if (grbi == null) {
                        /*
                        * if the parameters have been send by GET or POST kvp,
                        * we build a request object with this parameter.
                        */
                        grbi = createNewGetRecordByIdRequest();
                    }
                
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getRecordById(grbi), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                } if (request.equalsIgnoreCase("DescribeRecord") || (objectRequest instanceof DescribeRecord)) {
                
                    DescribeRecord dr = (DescribeRecord)objectRequest;
                
                    if (dr == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        dr = createNewDescribeRecordRequest();
                    }
                
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.describeRecord(dr), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                } if (request.equalsIgnoreCase("GetDomain") || (objectRequest instanceof GetDomain)) {
                
                    GetDomain gd = (GetDomain)objectRequest;
                
                    if (gd == null) {
                        /*
                        * if the parameters have been send by GET or POST kvp,
                        * we build a request object with this parameter.
                        */
                        gd = createNewGetDomainRequest();
                    }
                    worker.setStaticCapabilities((Capabilities)getStaticCapabilitiesObject());
                    
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getDomain(gd), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                } if (request.equalsIgnoreCase("Transaction") || (objectRequest instanceof Transaction)) {
                
                    Transaction t = (Transaction)objectRequest;
                
                    if (t == null) {
                         throw new CstlServiceException("The Operation transaction is not available in KVP",
                                                       OPERATION_NOT_SUPPORTED, getActingVersion(),"transaction");
                    }
                
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.transaction(t), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                } else if (request.equalsIgnoreCase("Harvest") || (objectRequest instanceof HarvestType)) {
                
                    HarvestType h = (HarvestType)objectRequest;
                
                    if (h == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        h = createNewHarvestRequest();
                    }
                
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.harvest(h), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                } else {
                    if (request.equals("") && objectRequest != null)
                        request = objectRequest.getClass().getName();
                    else if (request.equals("") && objectRequest == null)
                        request = "undefined request";
                
                    throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                                  INVALID_PARAMETER_VALUE, getActingVersion(), "request");
                }
            } else {
                throw new CstlServiceException("The CSW service is not running",
                                              NO_APPLICABLE_CODE, getActingVersion());
            }
        
        } catch (CstlServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             * - if the user have send a wrong request parameter
             */
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)   &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)&& 
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)&& 
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                ex.printStackTrace();
            } else {
                LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
            }
            if (marshaller != null) {
                ServiceVersion version = ex.getVersion();
                if (version == null)
                    version = getActingVersion();
                ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getExceptionCode().name(), ex.getLocator(), version.toString());
                StringWriter sw = new StringWriter();    
                marshaller.marshal(report, sw);
                return Response.ok(Util.cleanSpecialCharacter(sw.toString()), "text/xml").build();
            } else {
                return Response.ok("The CSW server is not running cause: unable to create JAXB context!", "text/plain").build(); 
            }
        }
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
                    
        AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));
                        
        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid. 
        String section = getParameter("Sections", false);
        List<String> requestedSections = new ArrayList<String>();
        if (section != null && !section.equalsIgnoreCase("All")) {
            final StringTokenizer tokens = new StringTokenizer(section, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (SectionsType.getExistingSections().contains(token)){
                    requestedSections.add(token);
                } else {
                    throw new CstlServiceException("The section " + token + " does not exist",
                                                  INVALID_PARAMETER_VALUE, getActingVersion(), "Sections");
                }   
            }
        } else {
            //if there is no requested Sections we add all the sections
            requestedSections = SectionsType.getExistingSections();
        }
        SectionsType sections     = new SectionsType(requestedSections);
        return new GetCapabilities(versions,
                                   sections,
                                   formats,
                                   null,
                                   getParameter("SERVICE", true));
        
    }
    
    
    /**
     * Build a new GetRecords request object with the url parameters 
     */
    private GetRecordsType createNewGetRecordsRequest() throws CstlServiceException {
        
        String version    = getParameter("VERSION", true);
        String service    = getParameter("SERVICE", true);
        
        //we get the value of result type, if not set we put default value "HITS"
        String resultTypeName = getParameter("RESULTTYPE", false);
        ResultType resultType = ResultType.HITS;
        if (resultTypeName != null) {
            try {
                resultType = ResultType.fromValue(resultTypeName);
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The resultType " + resultTypeName + " does not exist",
                                             INVALID_PARAMETER_VALUE, getActingVersion(), "ResultType");
            }
        }
        
        String requestID    = getParameter("REQUESTID", false);
        
        String outputFormat = getParameter("OUTPUTFORMAT", false);
        if (outputFormat == null) {
            outputFormat = "application/xml";
        }
        
        String outputSchema = getParameter("OUTPUTSCHEMA", false);
        if (outputSchema == null) {
            outputSchema = "http://www.opengis.net/cat/csw/2.0.2";
        }
        
        //we get the value of start position, if not set we put default value "1"
        String startPos = getParameter("STARTPOSITION", false);
        Integer startPosition = new Integer("1");
        if (startPos != null) {
            try {
                startPosition = new Integer(startPos);
            } catch (NumberFormatException e){
               throw new CstlServiceException("The positif integer " + startPos + " is malformed",
                                             INVALID_PARAMETER_VALUE, getActingVersion(), "startPosition");
            }
        } 
        
        //we get the value of max record, if not set we put default value "10"
        String maxRec = getParameter("MAXRECORDS", false);
        Integer maxRecords= new Integer("10");
        if (maxRec != null) {
            try {
                maxRecords = new Integer(maxRec);
            } catch (NumberFormatException e){
               throw new CstlServiceException("The positif integer " + maxRec + " is malformed",
                                             INVALID_PARAMETER_VALUE, getActingVersion(), "maxRecords");
            }
        } 
        
        /*
         * here we build the "Query" object 
         */
        
        // we get the namespaces.
        String namespace               = getParameter("NAMESPACE", false);
        Map<String, String> namespaces = new HashMap<String, String>();
        StringTokenizer tokens;
                
        if (namespace != null) {
            tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf('=') != -1) {
                    String prefix = token.substring(0, token.indexOf('='));
                    String url    = token.substring(token.indexOf('=') + 1);
                    namespaces.put(prefix, url);
                } else {
                     throw new CstlServiceException("The namespace " + token + " is malformed",
                                                   INVALID_PARAMETER_VALUE, getActingVersion(), "namespace");
                }
            }
        }
        
        //if there is not namespace specified, using the default namespace
        if (namespaces.size() == 0) {
            namespaces.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
            namespaces.put("gmd", "http://www.isotc211.org/2005/gmd");
        }
        
        String names   = getParameter("TYPENAMES", true);
        List<QName> typeNames = new ArrayList<QName>();
        tokens = new StringTokenizer(names, ",;");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();

            if (token.indexOf(':') != -1) {
                String prefix = token.substring(0, token.indexOf(':'));
                String localPart = token.substring(token.indexOf(':') + 1);
                typeNames.add(new QName(namespaces.get(prefix), localPart, prefix));
            } else {
                throw new CstlServiceException("The QName " + token + " is malformed",
                        INVALID_PARAMETER_VALUE, getActingVersion(), "namespace");
            }
        }
        
        String eSetName           = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The ElementSet Name " + eSetName + " does not exist",
                                                INVALID_PARAMETER_VALUE, getActingVersion(), "ElementSetName");
            }
        }
        
        //we get the list of sort by object
        String sort = getParameter("SORTBY", false);
        List<SortPropertyType> sorts = new ArrayList<SortPropertyType>();
        SortByType sortBy = null;
        if (sort != null) {
            tokens = new StringTokenizer(sort, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                
                if (token.indexOf(':') != -1) {
                    String propName    = token.substring(0, token.indexOf(':'));
                    String order       = token.substring(token.indexOf(':') + 1);
                    SortOrderType orderType;
                    try {
                        orderType = SortOrderType.fromValue(order);
                    } catch (IllegalArgumentException e){
                        throw new CstlServiceException("The SortOrder Name " + order + " does not exist",
                                                      INVALID_PARAMETER_VALUE, getActingVersion(), "SortBy");
                    }
                    sorts.add(new SortPropertyType(propName, orderType));
                } else {
                     throw new CstlServiceException("The expression " + token + " is malformed",
                                                      INVALID_PARAMETER_VALUE, getActingVersion(), "SortBy");
                }
            }
            sortBy = new SortByType(sorts);
        }
        
        /*
         * here we build the constraint object
         */ 
        String constLanguage           = getParameter("CONSTRAINTLANGUAGE", false);
        QueryConstraintType constraint = null;
        if (constLanguage != null) {
            String languageVersion  = getParameter("CONSTRAINT_LANGUAGE_VERSION", false);
            String constraintObject = getParameter("CONSTRAINT", false);
            
            if (constLanguage.equalsIgnoreCase("CQL_TEXT")) {
                
                constraint = new QueryConstraintType(constraintObject, languageVersion);
                
            } else if (constLanguage.equalsIgnoreCase("FILTER")) {
                //TODO xml unmarshall?
                constraint = new QueryConstraintType(new FilterType(), languageVersion);
                
            } else {
                throw new CstlServiceException("The constraint language " + constLanguage + " is not supported",
                                                 INVALID_PARAMETER_VALUE, getActingVersion(), "ConstraintLanguage");
            }
        }
        
        QueryType query = new QueryType(typeNames,
                                        new ElementSetNameType(elementSet),
                                        sortBy,
                                        constraint);
        
        /*
         * here we build a optionnal ditributed search object
         */  
        String distrib = getParameter("DISTRIBUTEDSEARCH", false);
        DistributedSearchType distribSearch = null;
        if (distrib != null && distrib.equalsIgnoreCase("true")) {
            String count = getParameter("HOPCOUNT", false);
            Integer hopCount = 2;
            if (count != null) {
                try {
                    hopCount = Integer.parseInt(count);
                } catch (NumberFormatException e){
                    throw new CstlServiceException("The positif integer " + count + " is malformed",
                                                  INVALID_PARAMETER_VALUE, getActingVersion(), "HopCount");
                }
            }
            distribSearch = new DistributedSearchType(hopCount);
        }
        
        // TODO not implemented yet
        String handler = getParameter("RESPONSEHANDLER", false);
        
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
    
        String version    = getParameter("VERSION", true);
        String service    = getParameter("SERVICE", true);
        
        String eSetName           = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                eSetName = eSetName.toLowerCase();
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The ElementSet Name " + eSetName + " does not exist",
                                             INVALID_PARAMETER_VALUE, getActingVersion(), "ElementSetName");
            }
        }
        
        String outputFormat = getParameter("OUTPUTFORMAT", false);
        if (outputFormat == null) {
            outputFormat = "application/xml";
        }
        
        String outputSchema = getParameter("OUTPUTSCHEMA", false);
        if (outputSchema == null) {
            outputSchema = "http://www.opengis.net/cat/csw/2.0.2";
        }
        
        String ids      = getParameter("ID", true);
        List<String> id = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(ids, ",;");
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
    
        String version    = getParameter("VERSION", true);
        String service    = getParameter("SERVICE", true);
        
        String outputFormat = getParameter("OUTPUTFORMAT", false);
        if (outputFormat == null) {
            outputFormat = "application/xml";
        }
        
        String schemaLanguage = getParameter("SCHEMALANGUAGE", false);
        if (schemaLanguage == null) {
            schemaLanguage = "XMLSCHEMA";
        }
        
         // we get the namespaces.
        String namespace               = getParameter("NAMESPACE", false);
        Map<String, String> namespaces = new HashMap<String, String>();
        if (namespace != null) {
            StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf('=') != -1) {
                    String prefix = token.substring(0, token.indexOf('='));
                    String url    = token.substring(token.indexOf('=') + 1);
                    namespaces.put(prefix, url);
                } else {
                     throw new CstlServiceException("The namespace " + token + " is malformed",
                                                   INVALID_PARAMETER_VALUE, getActingVersion(), "namespace");
                }
            }
        }
        //if there is not namespace specified, using the default namespace
        // TODO add gmd...
        if (namespaces.size() == 0) {
            namespaces.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
            namespaces.put("gmd", "http://www.isotc211.org/2005/gmd");
        }
        
        String names   = getParameter("TYPENAMES", true);
        List<QName> typeNames = new ArrayList<QName>();
        StringTokenizer tokens = new StringTokenizer(names, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                
                if (token.indexOf(':') != -1) {
                    String prefix    = token.substring(0, token.indexOf(':'));
                    String localPart = token.substring(token.indexOf(':') + 1);
                    typeNames.add(new QName(namespaces.get(prefix), localPart, prefix));
                } else {
                     throw new CstlServiceException("The QName " + token + " is malformed",
                                                   INVALID_PARAMETER_VALUE, getActingVersion(), "namespace");
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
    
        String version    = getParameter("VERSION", true);
        String service    = getParameter("SERVICE", true);
        
        //not supported by the ISO profile
        String parameterName = getParameter("PARAMETERNAME", false);
        
        String propertyName = getParameter("PROPERTYNAME", false);
        if (propertyName != null && parameterName != null) {
            throw new CstlServiceException("One of propertyName or parameterName must be null",
                                          INVALID_PARAMETER_VALUE, getActingVersion(), "parameterName");
        }
       
        return new GetDomainType(service, version, propertyName, parameterName);
    }
    
    /**
     * Build a new GetDomain request object with the url parameters 
     */
    private HarvestType createNewHarvestRequest() throws CstlServiceException {
    
        String version      = getParameter("VERSION", true);
        String service      = getParameter("SERVICE", true);
        String source       = getParameter("SOURCE", true);
        String resourceType = getParameter("RESOURCETYPE", true);
        String resourceFormat = getParameter("RESOURCEFORMAT", false);
        if (resourceFormat == null) {
            resourceFormat = "application/xml";
        }
        String handler           = getParameter("RESPONSEHANDLER", false);
        String interval          = getParameter("HARVESTINTERVAL", false);
        Duration harvestInterval = null;
        if (interval != null) {
            try {
                DatatypeFactory factory  = DatatypeFactory.newInstance();
                harvestInterval          = factory.newDuration(interval) ;
            } catch (DatatypeConfigurationException ex) {
                throw new CstlServiceException("The Duration " + interval + " is malformed",
                                              INVALID_PARAMETER_VALUE, getActingVersion(), "HarvestInsterval");
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
    
    /**
     * Load some extensions classes (ISO 19119 and ISO 19110) if thay are present in the classPath.
     * Return a list of classes to add in the context of JAXB.
     */
    private List<Class> loadExtensionsClasses() {
        List<Class> ExtClasses = new ArrayList<Class>();
        
        // if they are present in the classPath we add the ISO 19119 classes
        Class c = null;
        try {
            c = Class.forName("org.geotools.service.ServiceIdentificationImpl");
        } catch (ClassNotFoundException e) {
            LOGGER.info("ISO 19119 classes not found (optional)") ;
        }
        if (c != null) {
            ExtClasses.add(c);
            LOGGER.info("extension ISO 19119 loaded");
        } 

        // if they are present in the classPath we add the ISO 19110 classes
        
        try {
            c = Class.forName("org.geotools.feature.catalog.AssociationRoleImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.BindingImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.BoundFeatureAttributeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.ConstraintImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.DefinitionReferenceImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.DefinitionSourceImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureAssociationImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureAttributeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureCatalogueImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureOperationImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.FeatureTypeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.InheritanceRelationImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.ListedValueImpl");
            if (c != null) {
                ExtClasses.add(c);
            }

            c = Class.forName("org.geotools.feature.catalog.PropertyTypeImpl");
            if (c != null) {
                ExtClasses.add(c);
            }
            
            c = Class.forName("org.geotools.util.Multiplicity");
            if (c != null) {
                ExtClasses.add(c);
            }
            
            LOGGER.info("extension ISO 19110 loaded");
        } catch (ClassNotFoundException e) {
            LOGGER.info("ISO 19110 classes not found (optional).");
        }
        return ExtClasses;
    }
    
    /**
     * Destroy all the resource and close the connection when the web application is undeployed.
     */
    @PreDestroy
    public void destroy() {
        String id = "";
        if (serviceID != null && !serviceID.equals(""))
            id = '(' + serviceID + ')';

        LOGGER.info("destroying CSW service " + id);
        if (worker != null)
            worker.destroy();
    }

}
