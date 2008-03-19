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

import com.sun.ws.rest.spi.resource.Singleton;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import net.seagis.cat.csw.Capabilities;
import net.seagis.cat.csw.DescribeRecordType;
import net.seagis.cat.csw.DistributedSearchType;
import net.seagis.cat.csw.ElementSetNameType;
import net.seagis.cat.csw.ElementSetType;
import net.seagis.cat.csw.GetCapabilities;
import net.seagis.cat.csw.GetDomainType;
import net.seagis.cat.csw.GetRecordByIdType;
import net.seagis.cat.csw.GetRecordsType;
import net.seagis.cat.csw.HarvestType;
import net.seagis.cat.csw.ObjectFactory;
import net.seagis.cat.csw.QueryConstraintType;
import net.seagis.cat.csw.QueryType;
import net.seagis.cat.csw.ResultType;
import net.seagis.cat.csw.TransactionType;
import net.seagis.coverage.web.Service;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.ogc.FilterType;
import net.seagis.ogc.SortByType;
import net.seagis.ogc.SortOrderType;
import net.seagis.ogc.SortPropertyType;
import net.seagis.ows.v100.AcceptFormatsType;
import net.seagis.ows.v100.AcceptVersionsType;
import net.seagis.ows.v100.OWSWebServiceException;
import net.seagis.ows.v100.SectionsType;
import net.seagis.coverage.web.Service;
import static net.seagis.ows.OWSExceptionCode.*;

/**
 *
 * @author legal
 */
@Path("csw")
@Singleton
public class CSWService extends WebService {
    
    private CSWworker worker;
    
    private ObjectFactory cswFactory = new ObjectFactory();
    
    /**
     * Build a new Restfull CSW service.
     */
    public CSWService() throws JAXBException, IOException, SQLException {
        super("CSW", new ServiceVersion(Service.OWS, "2.0.2"));
        worker = new CSWworker();
        worker.setVersion(getCurrentVersion());
        setXMLContext("net.seagis.cat.csw:net.seagis.gml:net.seagis.gml","");
    }

    @Override
    public Response treatIncommingRequest(Object objectRequest) throws JAXBException {
        try {
            
            worker.setServiceURL(getServiceURL());
            writeParameters();
            String request = "";
            if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);
            
            if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                
                GetCapabilities gc = (GetCapabilities)objectRequest;
                
                if (gc == null) {
                     /*
                      * if the parameters have been send by GET or POST kvp,
                      * we build a request object with this parameter.
                      */
                    gc = createNewGetCapabilitiesRequest();
                }
                
                worker.setStaticCapabilities((Capabilities)getCapabilitiesObject());
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getCapabilities(gc), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetRecords") || (objectRequest instanceof GetRecordsType)) {
                
                GetRecordsType gr = (GetRecordsType)objectRequest;
                
                if (gr == null) {
                    /*
                     * if the parameters have been send by GET or POST kvp,
                     * we build a request object with this parameter.
                     */
                    gr = createNewGetRecordsRequest();
                }
                
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getRecords(gr), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                
            } if (request.equalsIgnoreCase("GetRecordById") || (objectRequest instanceof GetRecordByIdType)) {
                
                GetRecordByIdType grbi = (GetRecordByIdType)objectRequest;
                
                if (grbi == null) {
                    /*
                     * if the parameters have been send by GET or POST kvp,
                     * we build a request object with this parameter.
                     */
                    grbi = createNewGetRecordByIdRequest();
                }
                
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getRecordById(grbi), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                
            } if (request.equalsIgnoreCase("DescribeRecord") || (objectRequest instanceof DescribeRecordType)) {
                
                DescribeRecordType dr = (DescribeRecordType)objectRequest;
                
                if (dr == null) {
                    /*
                     * if the parameters have been send by GET or POST kvp,
                     * we build a request object with this parameter.
                     */
                    dr = createNewDescribeRecordRequest();
                }
                
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.describeRecord(dr), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                
            } if (request.equalsIgnoreCase("GetDomain") || (objectRequest instanceof GetDomainType)) {
                
                GetDomainType gd = (GetDomainType)objectRequest;
                
                if (gd == null) {
                    /*
                     * if the parameters have been send by GET or POST kvp,
                     * we build a request object with this parameter.
                     */
                    gd = createNewGetDomainRequest();
                }
                
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getDomain(gd), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                
            } if (request.equalsIgnoreCase("Transaction") || (objectRequest instanceof TransactionType)) {
                
                TransactionType t = (TransactionType)objectRequest;
                
                if (t == null) {
                     throw new OWSWebServiceException("The Operation transaction is not available in KVP",
                                                      OPERATION_NOT_SUPPORTED, "transaction", getCurrentVersion());
                }
                
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.transaction(t), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                
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
        
                return Response.ok(sw.toString(), "text/xml").build();
                
            } else {
                throw new OWSWebServiceException("The operation " + request + " is not supported by the service",
                                                 INVALID_PARAMETER_VALUE, "request", getCurrentVersion());
            }
        
        } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (ex instanceof OWSWebServiceException) {
                OWSWebServiceException owsex = (OWSWebServiceException)ex;
                if (!owsex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)   &&
                    !owsex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)&& 
                    !owsex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)&& 
                    !owsex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                    owsex.printStackTrace();
                } else {
                    logger.info("SENDING EXCEPTION: " + owsex.getExceptionCode().name() + " " + owsex.getMessage() + '\n');
                }
                StringWriter sw = new StringWriter();    
                marshaller.marshal(owsex.getExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()), "text/xml").build();
            } else {
                throw new IllegalArgumentException("this service can't return WMS Exception");
            }
        }
    }
    
    
    /**
     * Build a new GetCapabilities request object with the url parameters 
     */
    private GetCapabilities createNewGetCapabilitiesRequest() throws WebServiceException {
        
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
                    throw new OWSWebServiceException("The section " + token + " does not exist",
                                                     INVALID_PARAMETER_VALUE, "Sections", getCurrentVersion());
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
    private GetRecordsType createNewGetRecordsRequest() throws WebServiceException {
        
        String version    = getParameter("VERSION", true);
        String service    = getParameter("SERVICE", true);
        
        //we get the value of result type, if not set we put default value "HITS"
        String resultTypeName = getParameter("RESULTTYPE", false);
        ResultType resultType = ResultType.HITS;
        if (resultTypeName != null) {
            try {
                resultType = ResultType.fromValue(resultTypeName);
            } catch (IllegalArgumentException e){
               throw new OWSWebServiceException("The resultType " + resultTypeName + " does not exist",
                                                INVALID_PARAMETER_VALUE, "ResultType", getCurrentVersion());        
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
        BigInteger startPosition = new BigInteger("1");
        if (startPos != null) {
            try {
                startPosition = new BigInteger(startPos);
            } catch (NumberFormatException e){
               throw new OWSWebServiceException("The positif integer " + startPos + " is malformed",
                                                INVALID_PARAMETER_VALUE, "startPosition", getCurrentVersion());        
            }
        } 
        
        //we get the value of max record, if not set we put default value "10"
        String maxRec = getParameter("MAXRECORDS", false);
        BigInteger maxRecords= new BigInteger("10");
        if (maxRec != null) {
            try {
                maxRecords = new BigInteger(maxRec);
            } catch (NumberFormatException e){
               throw new OWSWebServiceException("The positif integer " + maxRec + " is malformed",
                                                INVALID_PARAMETER_VALUE, "maxRecords", getCurrentVersion());        
            }
        } 
        
        /*
         * here we build the "Query" object 
         */
        
        // we get the namespaces.
        String namespace               = getParameter("NAMESPACE", false);
        Map<String, String> namespaces = new HashMap<String, String>();
        StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf('=') != -1) {
                    String prefix = token.substring(0, token.indexOf('='));
                    String url    = token.substring(token.indexOf('=') + 1);
                    namespaces.put(prefix, url);
                } else {
                     throw new OWSWebServiceException("The namespace " + token + " is malformed",
                                                      INVALID_PARAMETER_VALUE, "namespace", getCurrentVersion());
                }
                
        }
        
        //if there is not namespace specified, using the default namespace
        // TODO add gmd...
        if (namespaces.size() == 0) {
            namespaces.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
        }
        
        String names   = getParameter("TYPENAMES", true);
        List<QName> typeNames = new ArrayList<QName>();
        tokens = new StringTokenizer(names, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                
                if (token.indexOf(':') != -1) {
                    String prefix    = token.substring(0, token.indexOf(':'));
                    String localPart = token.substring(token.indexOf(':') + 1);
                    typeNames.add(new QName(namespaces.get(prefix), localPart, prefix));
                } else {
                     throw new OWSWebServiceException("The QName " + token + " is malformed",
                                                      INVALID_PARAMETER_VALUE, "namespace", getCurrentVersion());
                }
        }
        
        String eSetName           = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new OWSWebServiceException("The ElementSet Name " + eSetName + " does not exist",
                                                INVALID_PARAMETER_VALUE, "ElementSetName", getCurrentVersion());        
            }
        }
        
        //we get the list of sort by object
        String sort = getParameter("SORTBY", false);
        List<SortPropertyType> sorts = new ArrayList<SortPropertyType>();
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
                        throw new OWSWebServiceException("The SortOrder Name " + order + " does not exist",
                                                         INVALID_PARAMETER_VALUE, "SortBy", getCurrentVersion());        
                    }
                    sorts.add(new SortPropertyType(propName, orderType));
                } else {
                     throw new OWSWebServiceException("The expression " + token + " is malformed",
                                                      INVALID_PARAMETER_VALUE, "SortBy", getCurrentVersion());
                }
        }
        SortByType sortBy = new SortByType(sorts);
        
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
                throw new OWSWebServiceException("The constraint language " + constLanguage + " is not supported",
                                                 INVALID_PARAMETER_VALUE, "ConstraintLanguage", getCurrentVersion());
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
            BigInteger hopCount = new BigInteger("2");
            if (count != null) {
                try {
                    hopCount = new BigInteger(count);
                } catch (NumberFormatException e){
                    throw new OWSWebServiceException("The positif integer " + count + " is malformed",
                                                INVALID_PARAMETER_VALUE, "HopCount", getCurrentVersion());        
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
                                  cswFactory.createQuery(query),
                                  distribSearch);
            
    }
    
    /**
     * Build a new GetRecordById request object with the url parameters 
     */
    private GetRecordByIdType createNewGetRecordByIdRequest() throws WebServiceException {
    
        String version    = getParameter("VERSION", true);
        String service    = getParameter("SERVICE", true);
        
        String eSetName           = getParameter("ELEMENTSETNAME", false);
        ElementSetType elementSet = ElementSetType.SUMMARY;
        if (eSetName != null) {
            try {
                elementSet = ElementSetType.fromValue(eSetName);
            
            } catch (IllegalArgumentException e){
               throw new OWSWebServiceException("The ElementSet Name " + eSetName + " does not exist",
                                                INVALID_PARAMETER_VALUE, "ElementSetName", getCurrentVersion());        
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
    private DescribeRecordType createNewDescribeRecordRequest() throws WebServiceException {
    
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
        StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf('=') != -1) {
                    String prefix = token.substring(0, token.indexOf('='));
                    String url    = token.substring(token.indexOf('=') + 1);
                    namespaces.put(prefix, url);
                } else {
                     throw new OWSWebServiceException("The namespace " + token + " is malformed",
                                                      INVALID_PARAMETER_VALUE, "namespace", getCurrentVersion());
                }
                
        }
        
        //if there is not namespace specified, using the default namespace
        // TODO add gmd...
        if (namespaces.size() == 0) {
            namespaces.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
        }
        
        String names   = getParameter("TYPENAMES", true);
        List<QName> typeNames = new ArrayList<QName>();
        tokens = new StringTokenizer(names, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                
                if (token.indexOf(':') != -1) {
                    String prefix    = token.substring(0, token.indexOf(':'));
                    String localPart = token.substring(token.indexOf(':') + 1);
                    typeNames.add(new QName(namespaces.get(prefix), localPart, prefix));
                } else {
                     throw new OWSWebServiceException("The QName " + token + " is malformed",
                                                      INVALID_PARAMETER_VALUE, "namespace", getCurrentVersion());
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
    private GetDomainType createNewGetDomainRequest() throws WebServiceException {
    
        String version    = getParameter("VERSION", true);
        String service    = getParameter("SERVICE", true);
        
        //not supported by the ISO profile
        String parameterName = getParameter("PARAMETERNAME", false);
        
        String propertyName = getParameter("PROPERTYNAME", false);
       
        return new GetDomainType(service, version, propertyName);
    }
    
    /**
     * Build a new GetDomain request object with the url parameters 
     */
    private HarvestType createNewHarvestRequest() throws WebServiceException {
    
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
                throw new OWSWebServiceException("The Duration " + interval + " is malformed",
                                              INVALID_PARAMETER_VALUE, "HarvestInsterval", getCurrentVersion());
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
