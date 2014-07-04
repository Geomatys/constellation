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
package org.constellation.metadata.ws.rs;

// java se dependencies

import org.apache.sis.xml.Namespaces;
import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.jaxb.CstlXMLSerializer;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.configuration.CSWConfigurer;
import org.constellation.metadata.utils.CSWUtils;
import org.constellation.metadata.utils.SerializerResponse;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.UnauthorizedException;
import org.constellation.ws.WebServiceUtilities;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.OGCWebService;
import org.geotoolkit.csw.xml.CSWResponse;
import org.geotoolkit.csw.xml.CswXmlFactory;
import org.geotoolkit.csw.xml.DescribeRecord;
import org.geotoolkit.csw.xml.DistributedSearch;
import org.geotoolkit.csw.xml.ElementSetName;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.GetCapabilities;
import org.geotoolkit.csw.xml.GetDomain;
import org.geotoolkit.csw.xml.GetRecordById;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.Harvest;
import org.geotoolkit.csw.xml.Query;
import org.geotoolkit.csw.xml.QueryConstraint;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.Transaction;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortOrderType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.ows.xml.v100.SectionsType;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import static org.constellation.api.QueryConstants.ACCEPT_FORMATS_PARAMETER;
import static org.constellation.api.QueryConstants.ACCEPT_VERSIONS_PARAMETER;
import static org.constellation.api.QueryConstants.SECTIONS_PARAMETER;
import static org.constellation.api.QueryConstants.SERVICE_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import static org.constellation.metadata.CSWConstants.MALFORMED;
import static org.constellation.metadata.CSWConstants.NAMESPACE;
import static org.constellation.metadata.CSWConstants.NOT_EXIST;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;

// Geotoolkit dependencies

/**
 * RestFul CSW service.
 *
 * @author Guilhem Legal
 * @author Benjamin Garcia (Geomatys)
 *
 * @version 0.9
 */
@Path("csw/{serviceId}")
@Singleton
public class CSWService extends OGCWebService<CSWworker> {

    /**
     * Build a new Restful CSW service.
     */
    public CSWService() {
        super(Specification.CSW);
        setXMLContext(EBRIMMarshallerPool.getInstance());
        LOGGER.log(Level.INFO, "CSW REST service running ({0} instances)\n", getWorkerMapSize());
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
     * This method has to be overridden by child classes.
     *
     * @return
     */
    protected CstlXMLSerializer getXMLSerializer() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response treatIncomingRequest(final Object objectRequest, final CSWworker worker) {
        ServiceDef serviceDef = null;

        try {

            // if the request is not an xml request we fill the request parameter.
            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter("REQUEST", true), worker);
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " +  objectRequest.getClass().getName() + " is not supported by the service",
                    INVALID_PARAMETER_VALUE, "request");
            }

            serviceDef = worker.getVersionFromNumber(request.getVersion());

            if (request instanceof GetCapabilities) {
                final GetCapabilities gc = (GetCapabilities) request;
                final String outputFormat  = MimeType.APPLICATION_XML; // TODO
                return Response.ok(worker.getCapabilities(gc), outputFormat).build();
            }

            if (request instanceof GetRecordsRequest) {

                final GetRecordsRequest gr = (GetRecordsRequest)request;
                final String outputFormat  = CSWUtils.getOutputFormat(gr);
                // we pass the serializer to the messageBodyWriter
                final SerializerResponse response = new SerializerResponse((CSWResponse) worker.getRecords(gr), getXMLSerializer());
                return Response.ok(response, outputFormat).build();
            }

            if (request instanceof GetRecordById) {

                final GetRecordById grbi = (GetRecordById)request;
                final String outputFormat  = CSWUtils.getOutputFormat(grbi);
                // we pass the serializer to the messageBodyWriter
                final SerializerResponse response = new SerializerResponse((CSWResponse) worker.getRecordById(grbi), getXMLSerializer());
                return Response.ok(response, outputFormat).build();
            }

            if (request instanceof DescribeRecord) {

                final DescribeRecord dr = (DescribeRecord)request;
                final String outputFormat  = CSWUtils.getOutputFormat(dr);
                return Response.ok(worker.describeRecord(dr), outputFormat).build();
            }

            if (request instanceof GetDomain) {
                final GetDomain gd = (GetDomain)request;
                final String outputFormat  = CSWUtils.getOutputFormat(gd);
                return Response.ok(worker.getDomain(gd), outputFormat).build();
            }

            if (request instanceof Transaction) {
                final Transaction tr = (Transaction)request;
                final String outputFormat  = CSWUtils.getOutputFormat(tr);
                return Response.ok(worker.transaction(tr), outputFormat).build();
            }

            if (request instanceof Harvest) {
                final Harvest hv = (Harvest)request;
                final String outputFormat  = CSWUtils.getOutputFormat(hv);
                return Response.ok(worker.harvest(hv), outputFormat).build();
            }

            throw new CstlServiceException("The operation " +  request.getClass().getName() + " is not supported by the service",
                    INVALID_PARAMETER_VALUE, "request");

        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef, worker);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef, final Worker w) {
        // asking for authentication
        if (ex instanceof UnauthorizedException) {
            return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", " Basic").build();
        }
        logException(ex);
        if (serviceDef == null) {
            serviceDef = w.getBestVersion(null);
        }
        final String version         = serviceDef.exceptionVersion.toString();
        final String code            = getOWSExceptionCodeRepresentation(ex.getExceptionCode());
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(), version);
        return Response.ok(report, MimeType.TEXT_XML).build();
    }


    /**
     * Build request object from KVP parameters.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(final String request, final Worker w) throws CstlServiceException {

        if ("GetCapabilities".equalsIgnoreCase(request)) {
            return createNewGetCapabilitiesRequest(w);
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
     * Build a new GetCapabilities request object with the url parameters
     */
    private GetCapabilities createNewGetCapabilitiesRequest(final Worker w) throws CstlServiceException {

        final String service = getParameter(SERVICE_PARAMETER, true);
        
        String acceptVersion = getParameter(ACCEPT_VERSIONS_PARAMETER, false);
        final AcceptVersions versions;
        final String version;
        if (acceptVersion != null) {
            if (acceptVersion.indexOf(',') != -1) {
                acceptVersion = acceptVersion.substring(0, acceptVersion.indexOf(','));
            }
            version = acceptVersion;
            w.checkVersionSupported(version, true);
            versions = CswXmlFactory.buildAcceptVersion(version, Arrays.asList(acceptVersion));
        } else {
            version = "2.0.2";
            versions = CswXmlFactory.buildAcceptVersion(version, Arrays.asList("2.0.2"));
        }

        final AcceptFormats formats = CswXmlFactory.buildAcceptFormat(version, Arrays.asList(getParameter(ACCEPT_FORMATS_PARAMETER, false)));
        final String updateSequence = getParameter(UPDATESEQUENCE_PARAMETER, false);

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        final String section = getParameter(SECTIONS_PARAMETER, false);
        List<String> requestedSections = new ArrayList<>();
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
        final Sections sections = CswXmlFactory.buildSections(version, requestedSections);
        return CswXmlFactory.createGetCapabilities(version, versions, sections, formats, updateSequence, service);
    }


    /**
     * Build a new GetRecords request object with the url parameters
     */
    private GetRecordsRequest createNewGetRecordsRequest() throws CstlServiceException {

        final String version    = getParameter(VERSION_PARAMETER, true);
        final String service    = getParameter(SERVICE_PARAMETER, true);

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
            outputSchema = Namespaces.CSW;
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
        final Map<String, String> namespaces = WebServiceUtilities.extractNamespace(namespace);

        //if there is not namespace specified, using the default namespace
        if (namespaces.isEmpty()) {
            namespaces.put("csw", Namespaces.CSW);
            namespaces.put("gmd", Namespaces.GMD);
        }

        final String names          = getParameter("TYPENAMES", true);
        final List<QName> typeNames = new ArrayList<>();
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
        final ElementSetName setName = CswXmlFactory.createElementSetName(version, elementSet);

        //we get the list of sort by object
        final String sort                  = getParameter("SORTBY", false);
        final List<SortPropertyType> sorts = new ArrayList<>();
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
        final String constLanguage = getParameter("CONSTRAINTLANGUAGE", false);
        QueryConstraint constraint = null;
        if (constLanguage != null) {
            final String languageVersion  = getParameter("CONSTRAINT_LANGUAGE_VERSION", true);

            if (constLanguage.equalsIgnoreCase("CQL_TEXT")) {

                String constraintObject = getParameter("CONSTRAINT", false);
                if (constraintObject == null) {
                    constraintObject = "AnyText LIKE '%%'";
                }
                constraint = CswXmlFactory.createQueryConstraint(version, constraintObject, languageVersion);

            } else if (constLanguage.equalsIgnoreCase("FILTER")) {
                final Object constraintObject = getComplexParameter("CONSTRAINT", false);
                if (constraintObject == null) {
                    // do nothing
                } else if (constraintObject instanceof FilterType){
                    constraint = CswXmlFactory.createQueryConstraint(version, (FilterType)constraintObject, languageVersion);
                } else {
                    throw new CstlServiceException("The filter type is not supported:" + constraintObject.getClass().getName(),
                                                 INVALID_PARAMETER_VALUE, "Constraint");
                }

            } else {
                throw new CstlServiceException("The constraint language " + constLanguage + " is not supported",
                                                 INVALID_PARAMETER_VALUE, "ConstraintLanguage");
            }
        }

        final Query query = CswXmlFactory.createQuery(version, typeNames, setName, sortBy, constraint);
        
        /*
         * here we build a optionnal ditributed search object
         */
        final String distrib = getParameter("DISTRIBUTEDSEARCH", false);
        DistributedSearch distribSearch = null;
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
            distribSearch = CswXmlFactory.createDistributedSearch(version, hopCount);
        }

        // TODO not implemented yet
        // String handler = getParameter("RESPONSEHANDLER", false);

        return CswXmlFactory.createGetRecord(version, service, resultType, requestID, outputFormat, outputSchema, startPosition, maxRecords, query, distribSearch);
    }

    /**
     * Build a new GetRecordById request object with the url parameters
     */
    private GetRecordById createNewGetRecordByIdRequest() throws CstlServiceException {

        final String version    = getParameter(VERSION_PARAMETER, true);
        final String service    = getParameter(SERVICE_PARAMETER, true);

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
        final ElementSetName setName = CswXmlFactory.createElementSetName(version, elementSet);

        String outputFormat = getParameter("OUTPUTFORMAT", false);
        if (outputFormat == null) {
            outputFormat = MimeType.APPLICATION_XML;
        }

        String outputSchema = getParameter("OUTPUTSCHEMA", false);
        if (outputSchema == null) {
            outputSchema = Namespaces.CSW;
        }

        final String ids             = getParameter("ID", true);
        final List<String> id        = new ArrayList<>();
        final StringTokenizer tokens = new StringTokenizer(ids, ",;");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            id.add(token);
        }

        return CswXmlFactory.createGetRecordById(version, service, setName, outputFormat, outputSchema, id);
    }

    /**
     * Build a new DescribeRecord request object with the url parameters
     */
    private DescribeRecord createNewDescribeRecordRequest() throws CstlServiceException {

        final String version    = getParameter(VERSION_PARAMETER, true);
        final String service    = getParameter(SERVICE_PARAMETER, true);

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
        final Map<String, String> namespaces = WebServiceUtilities.extractNamespace(namespace);

        //if there is not namespace specified, using the default namespace
        // TODO add gmd...
        if (namespaces.isEmpty()) {
            namespaces.put("csw", Namespaces.CSW);
            namespaces.put("gmd", Namespaces.GMD);
        }

        final List<QName> typeNames  = new ArrayList<>();
        final String names           = getParameter("TYPENAME", false);
        if (names != null) {
            final StringTokenizer tokens = new StringTokenizer(names, ",;");
                while (tokens.hasMoreTokens()) {
                    final String token = tokens.nextToken().trim();

                    if (token.indexOf(':') != -1) {
                        final String prefix    = token.substring(0, token.indexOf(':'));
                        final String localPart = token.substring(token.indexOf(':') + 1);
                        typeNames.add(new QName(namespaces.get(prefix), localPart));
                    } else {
                         throw new CstlServiceException("The QName " + token + MALFORMED,
                                                       INVALID_PARAMETER_VALUE, NAMESPACE);
                    }
            }
        }

        return CswXmlFactory.createDescribeRecord(version, service, typeNames, outputFormat, schemaLanguage);
    }

    /**
     * Build a new GetDomain request object with the url parameters
     */
    private GetDomain createNewGetDomainRequest() throws CstlServiceException {

        final String version    = getParameter(VERSION_PARAMETER, true);
        final String service    = getParameter(SERVICE_PARAMETER, true);

        //not supported by the ISO profile
        final String parameterName = getParameter("PARAMETERNAME", false);

        final String propertyName = getParameter("PROPERTYNAME", false);
        if (propertyName != null && parameterName != null) {
            throw new CstlServiceException("One of propertyName or parameterName must be null",
                                          INVALID_PARAMETER_VALUE, "parameterName");
        }
        return CswXmlFactory.createGetDomain(version, service, propertyName, parameterName);
    }

    /**
     * Build a new GetDomain request object with the url parameters
     */
    private Harvest createNewHarvestRequest() throws CstlServiceException {

        final String version      = getParameter(VERSION_PARAMETER, true);
        final String service      = getParameter(SERVICE_PARAMETER, true);
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
        return CswXmlFactory.createHarvest(version, service, source, resourceType, resourceFormat, handler, harvestInterval);
    }
}
