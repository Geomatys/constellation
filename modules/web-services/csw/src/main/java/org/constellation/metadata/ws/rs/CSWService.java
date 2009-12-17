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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

// jersey dependencies
import com.sun.jersey.spi.resource.Singleton;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

//JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.ws.CstlServiceException;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.Parameters;
import org.constellation.util.Util;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.OGCWebService;
import org.constellation.metadata.CSWClassesContext;

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
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortOrderType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.v100.AcceptFormatsType;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.xml.Catching;
import org.geotoolkit.xml.Namespaces;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

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
        this("");
    }

    /**
     * Build a new Restfull CSW service.
     * used by subClasses.
     */
    protected CSWService(final String serviceID) {
        super(ServiceDef.CSW_2_0_2);
        this.serviceID = serviceID;
        try {
            setXMLContext("", CSWClassesContext.getAllClasses());
            worker = new CSWworker(serviceID, getMarshallerPool());

        } catch (JAXBException ex){
            LOGGER.severe("The CSW service is not running."       + '\n' +
                          " cause  : Error creating XML context." + '\n' +
                          " error  : " + ex.getMessage()          + '\n' +
                          " details: " + ex.toString());
        }
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
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        Catching.Marshaller marshaller = null;
        ServiceDef serviceDef          = null;
        MarshallWarnings warnings       = new MarshallWarnings();
        try {
            marshaller = getMarshallerPool().acquireMarshaller();
            marshaller.setObjectConverters(warnings);

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
                    serviceDef = getVersionFromNumber(gc.getVersion().toString());
                    worker.setSkeletonCapabilities((Capabilities)getStaticCapabilitiesObject());
                
                    final StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getCapabilities(gc), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                    
                }

                if (request.equalsIgnoreCase("GetRecords") || (objectRequest instanceof GetRecordsRequest)) {
                
                    GetRecordsRequest gr = (GetRecordsRequest)objectRequest;
                
                    if (gr == null) {
                        /*
                        * if the parameters have been send by GET or POST kvp,
                        * we build a request object with this parameter.
                        */
                        gr = createNewGetRecordsRequest();
                    }
                    serviceDef = getVersionFromNumber(gr.getVersion());
                    final StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getRecords(gr), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                }

                if (request.equalsIgnoreCase("GetRecordById") || (objectRequest instanceof GetRecordById)) {
                
                    GetRecordById grbi = (GetRecordById)objectRequest;
                
                    if (grbi == null) {
                        /*
                        * if the parameters have been send by GET or POST kvp,
                        * we build a request object with this parameter.
                        */
                        grbi = createNewGetRecordByIdRequest();
                    }
                    serviceDef = getVersionFromNumber(grbi.getVersion());
                    final StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getRecordById(grbi), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                }

                if (request.equalsIgnoreCase("DescribeRecord") || (objectRequest instanceof DescribeRecord)) {
                
                    DescribeRecord dr = (DescribeRecord)objectRequest;
                
                    if (dr == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        dr = createNewDescribeRecordRequest();
                    }
                    serviceDef = getVersionFromNumber(dr.getVersion());
                    final StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.describeRecord(dr), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                }

                if (request.equalsIgnoreCase("GetDomain") || (objectRequest instanceof GetDomain)) {
                
                    GetDomain gd = (GetDomain)objectRequest;
                
                    if (gd == null) {
                        /*
                        * if the parameters have been send by GET or POST kvp,
                        * we build a request object with this parameter.
                        */
                        gd = createNewGetDomainRequest();
                    }
                    serviceDef = getVersionFromNumber(gd.getVersion());
                    worker.setSkeletonCapabilities((Capabilities)getStaticCapabilitiesObject());
                    
                    final StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getDomain(gd), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                }

                if (request.equalsIgnoreCase("Transaction") || (objectRequest instanceof Transaction)) {
                
                    final Transaction t = (Transaction)objectRequest;
                
                    if (t == null) {
                         throw new CstlServiceException("The Operation transaction is not available in KVP",
                                                       OPERATION_NOT_SUPPORTED, "transaction");
                    }
                    serviceDef = getVersionFromNumber(t.getVersion());
                    final StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.transaction(t), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                }

                if (request.equalsIgnoreCase("Harvest") || (objectRequest instanceof HarvestType)) {
                
                    HarvestType h = (HarvestType)objectRequest;
                
                    if (h == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        h = createNewHarvestRequest();
                    }
                    serviceDef = getVersionFromNumber(h.getVersion());
                    final StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.harvest(h), sw);
        
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                
                }

                if (request.equals("") && objectRequest != null) {
                    request = objectRequest.getClass().getName();
                } else if (request.equals("") && objectRequest == null) {
                    request = "undefined request";
                }

                throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");

            } else {
                throw new CstlServiceException("The CSW service is not running", NO_APPLICABLE_CODE);
            }
        
        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef);

        } finally {
            if (marshaller != null) {
                getMarshallerPool().release(marshaller);
            }
            if (!warnings.isEmpty()) {
               for (String message : warnings.getMessages()) {
                   LOGGER.warning(message);
               }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) throws JAXBException
    {
        /* We don't print the stack trace:
         * - if the user have forget a mandatory parameter.
         * - if the version number is wrong.
         * - if the user have send a wrong request parameter
         */
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
        }
        if (isJaxBContextValid()) {
            if (serviceDef == null) {
                serviceDef = getBestVersion(null);
            }
            final String version = serviceDef.exceptionVersion.toString();
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getExceptionCode().name(), ex.getLocator(), version);
            final StringWriter sw = new StringWriter();
            Marshaller marshaller = null;
            try {
                marshaller = getMarshallerPool().acquireMarshaller();
                marshaller.marshal(report, sw);
            } finally {
                if (marshaller != null) {
                    getMarshallerPool().release(marshaller);
                }
            }
            return Response.ok(Util.cleanSpecialCharacter(sw.toString()), MimeType.TEXT_XML).build();
        } else {
            return Response.ok("The CSW server is not running cause: unable to create JAXB context!", MimeType.TEXT_PLAIN).build();
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
                    throw new CstlServiceException("The section " + token + " does not exist",
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
                                       getParameter("SERVICE", true));
        
    }
    
    
    /**
     * Build a new GetRecords request object with the url parameters 
     */
    private GetRecordsType createNewGetRecordsRequest() throws CstlServiceException {
        
        final String version    = getParameter("VERSION", true);
        final String service    = getParameter("SERVICE", true);
        
        //we get the value of result type, if not set we put default value "HITS"
        final String resultTypeName = getParameter("RESULTTYPE", false);
        ResultType resultType = ResultType.HITS;
        if (resultTypeName != null) {
            try {
                resultType = ResultType.fromValue(resultTypeName);
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The resultType " + resultTypeName + " does not exist",
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
               throw new CstlServiceException("The positif integer " + startPos + " is malformed",
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
               throw new CstlServiceException("The positif integer " + maxRec + " is malformed",
                                             INVALID_PARAMETER_VALUE, "maxRecords");
            }
        } 
        
        /*
         * here we build the "Query" object 
         */
        
        // we get the namespaces.
        final String namespace               = getParameter("NAMESPACE", false);
        final Map<String, String> namespaces = new HashMap<String, String>();
        StringTokenizer tokens;
                
        if (namespace != null) {
            tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim();
                if (token.startsWith("xmlns(") && token.endsWith(")")) {
                    token = token.substring(6, token.length() -1);
                    if (token.indexOf('=') != -1) {
                        final String prefix = token.substring(0, token.indexOf('='));
                        final String url    = token.substring(token.indexOf('=') + 1);
                        namespaces.put(prefix, url);
                    } else {
                         throw new CstlServiceException("The namespace " + token + " is malformed",
                                                       INVALID_PARAMETER_VALUE, Parameters.NAMESPACE);
                    }
                } else {
                    throw new CstlServiceException("The namespace attribute is malformed: good pattern is \"xmlns(ns1=http://namespace1),xmlns(ns2=http://namespace2)\"",
                                                       INVALID_PARAMETER_VALUE, Parameters.NAMESPACE);
                }
            }
        }
        
        //if there is not namespace specified, using the default namespace
        if (namespaces.size() == 0) {
            namespaces.put("csw", Namespaces.CSW_202);
            namespaces.put("gmd", Namespaces.GMD);
        }
        
        final String names          = getParameter("TYPENAMES", true);
        final List<QName> typeNames = new ArrayList<QName>();
        tokens = new StringTokenizer(names, ",;");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();

            if (token.indexOf(':') != -1) {
                final String prefix = token.substring(0, token.indexOf(':'));
                final String localPart = token.substring(token.indexOf(':') + 1);
                typeNames.add(new QName(namespaces.get(prefix), localPart, prefix));
            } else {
                throw new CstlServiceException("The QName " + token + " is malformed",
                        INVALID_PARAMETER_VALUE, Parameters.NAMESPACE);
            }
        }
        
        final String eSetName     = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The ElementSet Name " + eSetName + " does not exist",
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
                        throw new CstlServiceException("The SortOrder Name " + order + " does not exist",
                                                      INVALID_PARAMETER_VALUE, "SortBy");
                    }
                    sorts.add(new SortPropertyType(propName, orderType));
                } else {
                     throw new CstlServiceException("The expression " + token + " is malformed",
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
            final String languageVersion  = getParameter("CONSTRAINT_LANGUAGE_VERSION", false);
            
            if (constLanguage.equalsIgnoreCase("CQL_TEXT")) {

                String constraintObject = getParameter("CONSTRAINT", false);
                if (constraintObject == null) {
                    constraintObject = "AnyText LIKE '%%'";
                }
                constraint = new QueryConstraintType(constraintObject, languageVersion);
                
            } else if (constLanguage.equalsIgnoreCase("FILTER")) {
                Object constraintObject = getComplexParameter("CONSTRAINT", false);
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
                    throw new CstlServiceException("The positif integer " + count + " is malformed",
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
    
        final String version    = getParameter("VERSION", true);
        final String service    = getParameter("SERVICE", true);
        
        String eSetName         = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                eSetName = eSetName.toLowerCase();
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new CstlServiceException("The ElementSet Name " + eSetName + " does not exist",
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
    
        final String version    = getParameter("VERSION", true);
        final String service    = getParameter("SERVICE", true);
        
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
        final Map<String, String> namespaces = new HashMap<String, String>();
        if (namespace != null) {
            final StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim();
                if (token.startsWith("xmlns(") && token.endsWith(")")) {
                    token = token.substring(6, token.length() -1);
                    if (token.indexOf('=') != -1) {
                        final String prefix = token.substring(0, token.indexOf('='));
                        final String url    = token.substring(token.indexOf('=') + 1);
                        namespaces.put(prefix, url);
                    } else {
                         throw new CstlServiceException("The namespace " + token + " is malformed",
                                                       INVALID_PARAMETER_VALUE, Parameters.NAMESPACE);
                    }
                } else {
                    throw new CstlServiceException("The namespace attribute is malformed: good pattern is \"xmlns(ns1=http://namespace1),xmlns(ns2=http://namespace2)\"",
                                                       INVALID_PARAMETER_VALUE, Parameters.NAMESPACE);
                }
            }
        }
        //if there is not namespace specified, using the default namespace
        // TODO add gmd...
        if (namespaces.size() == 0) {
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
                     throw new CstlServiceException("The QName " + token + " is malformed",
                                                   INVALID_PARAMETER_VALUE, Parameters.NAMESPACE);
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
    
        final String version    = getParameter("VERSION", true);
        final String service    = getParameter("SERVICE", true);
        
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
    
        final String version      = getParameter("VERSION", true);
        final String service      = getParameter("SERVICE", true);
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
                throw new CstlServiceException("The Duration " + interval + " is malformed",
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
    
    /**
     * Destroy all the resource and close the connection when the web application is undeployed.
     */
    @PreDestroy
    @Override
    public void destroy() {
        String id = "";
        if (serviceID != null && !serviceID.equals(""))
            id = '(' + serviceID + ')';

        LOGGER.info("Shutting down the REST CSW service facade " + id + '.');
        if (worker != null)
            worker.destroy();
    }

}
