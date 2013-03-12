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
import java.util.logging.Level;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

// jersey dependencies
import com.sun.jersey.spi.resource.Singleton;
import java.util.Arrays;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//JAXB dependencies
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.generic.database.Automatic;
import org.constellation.jaxb.CstlXMLSerializer;
import org.constellation.ws.CstlServiceException;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.utils.SerializerResponse;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.OGCWebService;
import org.constellation.ws.WebServiceUtilities;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

import static org.constellation.api.QueryConstants.*;
import org.constellation.configuration.AcknowlegementType;
import static org.constellation.metadata.CSWConstants.*;
import org.constellation.ws.UnauthorizedException;
import org.constellation.ws.WSEngine;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.CSWResponse;
import org.geotoolkit.csw.xml.CswXmlFactory;
import org.geotoolkit.csw.xml.DescribeRecord;
import org.geotoolkit.csw.xml.DistributedSearch;
import org.geotoolkit.csw.xml.ElementSetName;
import org.geotoolkit.csw.xml.GetCapabilities;
import org.geotoolkit.csw.xml.GetDomain;
import org.geotoolkit.csw.xml.GetRecordById;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.Transaction;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.Harvest;
import org.geotoolkit.csw.xml.Query;
import org.geotoolkit.csw.xml.QueryConstraint;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortOrderType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.xml.Namespaces;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.util.StringUtilities;

/**
 * RestFul CSW service.
 *
 * @author Guilhem Legal
 */
@Path("csw/{serviceId}")
@Singleton
public class CSWService extends OGCWebService<CSWworker> {

    /**
     * Build a new Restful CSW service.
     */
    public CSWService() {
        super(ServiceDef.CSW_2_0_2);
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
    protected Response treatIncomingRequest(final Object objectRequest, CSWworker worker) {
        ServiceDef serviceDef = null;

        try {
            if (worker != null) {

                worker.setServiceUrl(getServiceURL());

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

                    return Response.ok(worker.getCapabilities((GetCapabilities)request), worker.getOutputFormat()).build();
                }

                if (request instanceof GetRecordsRequest) {

                    final GetRecordsRequest gr = (GetRecordsRequest)request;

                    // we pass the serializer to the messageBodyWriter
                    final SerializerResponse response = new SerializerResponse((CSWResponse) worker.getRecords(gr), getXMLSerializer());
                    return Response.ok(response, worker.getOutputFormat()).build();

                }

                if (request instanceof GetRecordById) {

                    final GetRecordById grbi = (GetRecordById)request;

                    // we pass the serializer to the messageBodyWriter
                    final SerializerResponse response = new SerializerResponse((CSWResponse) worker.getRecordById(grbi), getXMLSerializer());
                    return Response.ok(response, worker.getOutputFormat()).build();

                }

                if (request instanceof DescribeRecord) {

                    return Response.ok(worker.describeRecord((DescribeRecord)request), worker.getOutputFormat()).build();
                }

                if (request instanceof GetDomain) {

                    return Response.ok(worker.getDomain((GetDomain)request), worker.getOutputFormat()).build();
                }

                if (request instanceof Transaction) {

                    return Response.ok(worker.transaction( (Transaction)request), worker.getOutputFormat()).build();
                }

                if (request instanceof Harvest) {

                    return Response.ok(worker.harvest((Harvest)request), worker.getOutputFormat()).build();
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
    protected Response treatSpecificAdminRequest(final String request) throws CstlServiceException {
        if ("setFederatedCatalog".equals(request)) {
            final String identifier = getParameter("id", true);
            final List<String> servers = StringUtilities.toStringList(getParameter("servers", true));
            final CSWworker worker = (CSWworker) WSEngine.getInstance("CSW", identifier);
            if (worker != null) {
                worker.setCascadedService(servers);
                return Response.ok(new AcknowlegementType("Success", "Federated catalogs updated"), "text/xml").build();
            } else {
                throw new CstlServiceException("There is no CSW  instance " + identifier + ".",
                        INVALID_PARAMETER_VALUE, "id");
            }
        } else if ("clearCache".equals(request)) {
            final String identifier = getParameter("id", true);
            final CSWworker worker = (CSWworker) WSEngine.getInstance("CSW", identifier);
            if (worker != null) {
                worker.clearCache();
                return Response.ok(new AcknowlegementType("Success", "CSW cache cleared"), "text/xml").build();
            } else {
                throw new CstlServiceException("There is no CSW  instance " + identifier + ".",
                        INVALID_PARAMETER_VALUE, "id");
            }
        } else {
            throw new CstlServiceException("The operation " + request + " is not supported by the CSW administration service",
                        INVALID_PARAMETER_VALUE, "request");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) {
        // asking for authentication
        if (ex instanceof UnauthorizedException) {
            return Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", " Basic").build();
        }
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
     * {@inheritDoc}
     */
    @Override
    protected void configureInstance(final File instanceDirectory, final Object configuration) throws CstlServiceException {
        if (configuration instanceof Automatic) {
            final File configurationFile = new File(instanceDirectory, "config.xml");
            Marshaller marshaller = null;
            try {
                marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);

            } catch(JAXBException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            } finally {
                if (marshaller != null) {
                    GenericDatabaseMarshallerPool.getInstance().release(marshaller);
                }
            }
        } else {
            throw new CstlServiceException("The configuration Object is not an Automatic object", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void basicConfigure(final File instanceDirectory) throws CstlServiceException {
        configureInstance(instanceDirectory, new Automatic("filesystem", new BDD()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getInstanceConfiguration(File instanceDirectory) throws CstlServiceException {
        final File configurationFile = new File(instanceDirectory, "config.xml");
        if (configurationFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(configurationFile);
                if (obj instanceof Automatic) {
                    return obj;
                } else {
                    throw new CstlServiceException("The config.xml file does not contain a Automatic object");
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex);
            } finally {
                if (unmarshaller != null) {
                    GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                }
            }
        } else {
            throw new CstlServiceException("Unable to find a file config.xml");
        }
    }

    /**
     * Build request object from KVP parameters.
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
     * Build a new GetCapabilities request object with the url parameters
     */
    private GetCapabilities createNewGetCapabilitiesRequest() throws CstlServiceException {

        final String service = getParameter(SERVICE_PARAMETER, true);
        
        String acceptVersion = getParameter(ACCEPT_VERSIONS_PARAMETER, false);
        final AcceptVersions versions;
        final String version;
        if (acceptVersion != null) {
            if (acceptVersion.indexOf(',') != -1) {
                acceptVersion = acceptVersion.substring(0, acceptVersion.indexOf(','));
            }
            version = acceptVersion;
            isVersionSupported(version);
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
        final Map<String, String> namespaces = WebServiceUtilities.extractNamespace(namespace);

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
        final ElementSetName setName = CswXmlFactory.createElementSetName(version, elementSet);

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
            outputSchema = Namespaces.CSW_202;
        }

        final String ids             = getParameter("ID", true);
        final List<String> id        = new ArrayList<String>();
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
            namespaces.put("csw", Namespaces.CSW_202);
            namespaces.put("gmd", Namespaces.GMD);
        }

        final List<QName> typeNames  = new ArrayList<QName>();
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
