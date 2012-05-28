/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.wfs.ws.rs;

// J2SE dependencies
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLEventReader;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;

// jersey dependencies
import com.sun.jersey.spi.resource.Singleton;
import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

// constellation dependencies
import org.constellation.ws.rs.GridWebService;
import org.constellation.ws.WebServiceUtilities;
import org.constellation.ServiceDef;
import org.constellation.wfs.ws.DefaultWFSWorker;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.ws.CstlServiceException;

import static org.constellation.api.QueryConstants.*;
import static org.constellation.wfs.ws.WFSConstants.*;

// Geotoolkit dependencies
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.client.util.RequestsUtilities;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.GmlObjectIdType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.v100.AcceptFormatsType;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.wfs.xml.v110.AllSomeType;
import org.geotoolkit.wfs.xml.v110.DeleteElementType;
import org.geotoolkit.wfs.xml.v110.DescribeFeatureTypeType;
import org.geotoolkit.wfs.xml.v110.GetCapabilitiesType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.GetGmlObjectType;
import org.geotoolkit.wfs.xml.v110.LockFeatureType;
import org.geotoolkit.wfs.xml.v110.LockType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.TransactionType;
import org.geotoolkit.wfs.xml.v110.BaseRequestType;
import org.geotoolkit.xml.MarshallerPool;

import org.opengis.filter.sort.SortOrder;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.wfs.xml.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("wfs/{serviceId}")
@Singleton
public class WFSService extends GridWebService<WFSWorker> {

    private static Map<String, String> schemaLocations;
    static {
        System.setProperty("javax.xml.stream.XmlInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
        System.setProperty("javax.xml.stream.XmlEventFactory", "com.ctc.wstx.stax.WstxEventFactory");
        System.setProperty("javax.xml.stream.XmlOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
    }

    /**
     * Build a new Restful WFS service.
     */
    public WFSService() {
        super(ServiceDef.WFS_1_1_0);

        try {
            final MarshallerPool pool = new MarshallerPool("org.geotoolkit.wfs.xml.v110"   +
            		  ":org.geotoolkit.ogc.xml.v110"  +
            		  ":org.geotoolkit.gml.xml.v311"  +
                          ":org.geotoolkit.xsd.xml.v2001" +
                          //":org.geotoolkit.sampling.xml.v100" +
                         ":org.geotoolkit.internal.jaxb.geometry");
            setXMLContext(pool);
            LOGGER.log(Level.INFO, "WFS REST service running ({0} instances)\n", getWorkerMapSize());

        } catch (JAXBException ex){
            LOGGER.warning("The WFS REST service is not running.\ncause  : Error creating XML context.\n error  : " + ex.getMessage()  + 
                           "\n details: " + ex.toString());
        } 

        //activateRequestValidation("http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");
    }

    @Override
    protected WFSWorker createWorker(final File instanceDirectory) {
        return new DefaultWFSWorker(instanceDirectory.getName(), instanceDirectory);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest, final WFSWorker worker) {

        ServiceDef version    = null;

        worker.setServiceUrl(getServiceURL());

        try {
            logParameters();

            // if the request is not an xml request we fill the request parameter.
            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter(REQUEST_PARAMETER, true));
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
            }
            version = getVersionFromNumber(request.getVersion());

            if (request instanceof GetCapabilities) {
                final GetCapabilities model = (GetCapabilities) request;
                String outputFormat = model.getFirstAcceptFormat();
                if (outputFormat == null) {
                    outputFormat = "application/xml";
                }
                return Response.ok(worker.getCapabilities(model), outputFormat).build();

            } else if (request instanceof DescribeFeatureType) {
                final DescribeFeatureType model = (DescribeFeatureType) request;
                String requestOutputFormat = model.getOutputFormat();
                final MediaType outputFormat;
                if (requestOutputFormat == null || requestOutputFormat.equals("text/xml; subtype=gml/3.1.1")) {
                    outputFormat = GML_3_1_1;                                                                       
                } else if (requestOutputFormat.equals("text/xml; subtype=gml/3.2.1") || requestOutputFormat.equals("text/xml; subtype=gml/3.2")) {
                    outputFormat = GML_3_2_1;
                } else {
                    outputFormat = MediaType.valueOf(requestOutputFormat);
                }
                LOGGER.log(Level.INFO, "outputFormat asked:{0}", requestOutputFormat);
                
                return Response.ok(worker.describeFeatureType(model), outputFormat).build();

            } else if (request instanceof GetFeature) {
                final GetFeature model = (GetFeature) request;
                String requestOutputFormat = model.getOutputFormat();
                final MediaType outputFormat;
                if (requestOutputFormat == null || requestOutputFormat.equals("text/xml; subtype=gml/3.1.1")) {
                    outputFormat = GML_3_1_1;                                                                       
                } else if (requestOutputFormat.equals("text/xml; subtype=gml/3.2.1") || requestOutputFormat.equals("text/xml; subtype=gml/3.2")) {
                    outputFormat = GML_3_2_1;
                } else {
                    outputFormat = MediaType.valueOf(requestOutputFormat);
                }
                final Object response = worker.getFeature(model);
                schemaLocations = worker.getSchemaLocations();
                return Response.ok(response, outputFormat).build();
                
            } else if (request instanceof GetGmlObject) {
                final GetGmlObject model = (GetGmlObject) request;
                final WFSResponseWrapper response = new WFSResponseWrapper(worker.getGMLObject(model));
                return Response.ok(response, MediaType.TEXT_XML).build();

            } else if (request instanceof LockFeatureType) {
                final LockFeatureType model = (LockFeatureType) request;
                return Response.ok(worker.lockFeature(model), MediaType.TEXT_XML).build();

            } else if (request instanceof TransactionType) {
                final TransactionType model = (TransactionType) request;
                return Response.ok(worker.transaction(model), MediaType.TEXT_XML).build();
            }

            throw new CstlServiceException("The operation " + request.getClass().getName() + " is not supported by the service",
                                          INVALID_PARAMETER_VALUE, "request");

        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, version);
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
        final String exceptionCode   = getOWSExceptionCodeRepresentation(ex.getExceptionCode());
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), exceptionCode, ex.getLocator(), version);
        return Response.ok(report, "text/xml").build();
    }

    /**
     * Override the parent method in order to extract namespace mapping
     * during the unmarshall.
     * 
     * @param unmarshaller
     * @param is
     * @return
     * @throws JAXBException 
     */
    @Override
    protected Object unmarshallRequest(Unmarshaller unmarshaller, InputStream is) throws JAXBException {
        try {
            final JAXBEventHandler handler          = new JAXBEventHandler(); 
            unmarshaller.setEventHandler(handler);
            final Map<String, String> prefixMapping = new LinkedHashMap<String, String>();
            final XMLEventReader rootEventReader    = XMLInputFactory.newInstance().createXMLEventReader(is);
            final XMLEventReader eventReader        = (XMLEventReader) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{XMLEventReader.class}, new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Object returnVal = null;
                    try {
                        returnVal = method.invoke(rootEventReader, args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getTargetException();
                    }
                    if (method.getName().equals("nextEvent")) {
                        XMLEvent evt = (XMLEvent) returnVal;
                        if (evt.isStartElement()) {
                            StartElement startElem = evt.asStartElement();
                            Iterator<Namespace> t = startElem.getNamespaces();
                            while (t.hasNext()) {
                                Namespace n = t.next();
                                prefixMapping.put(n.getPrefix(), n.getNamespaceURI());
                            }
                        }
                    }
                    return returnVal;
                }
            });
            Object request =  unmarshaller.unmarshal(eventReader);
            if (request instanceof JAXBElement) {
                request = ((JAXBElement)request).getValue();
            }
            if (request instanceof BaseRequestType) {
                ((BaseRequestType)request).setPrefixMapping(prefixMapping);
            }
            return request;
        } catch (XMLStreamException ex) {
            throw new JAXBException(ex);
        }
    }
    
    private RequestBase adaptQuery(final String request) throws CstlServiceException {
        if (STR_GETCAPABILITIES.equalsIgnoreCase(request)) {
            return createNewGetCapabilitiesRequest();
        } else if (STR_DESCRIBEFEATURETYPE.equalsIgnoreCase(request)) {
            return createNewDescribeFeatureTypeRequest();
        } else if (STR_GETFEATURE.equalsIgnoreCase(request)) {
            return createNewGetFeatureRequest();
        } else if (STR_GETGMLOBJECT.equalsIgnoreCase(request)) {
            return createNewGetGmlObjectRequest();
        } else if (STR_LOCKFEATURE.equalsIgnoreCase(request)) {
            return createNewLockFeatureRequest();
        } else if (STR_TRANSACTION.equalsIgnoreCase(request)) {
            return createNewTransactionRequest();
        }
        throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
    }

    private DescribeFeatureTypeType createNewDescribeFeatureTypeRequest() throws CstlServiceException {
        String outputFormat   = getParameter("outputFormat", false);
        final String handle   = getParameter(HANDLE, false);
        final String service  = getParameter(SERVICE_PARAMETER, true);
        final String version  = getParameter(VERSION_PARAMETER, true);

        if (outputFormat == null) {
            outputFormat = "text/xml; subtype=gml/3.1.1";
        }
        final String namespace = getParameter(NAMESPACE, false);
        final Map<String, String> mapping = WebServiceUtilities.extractNamespace(namespace);

        final String typeName = getParameter("typeName", false);
        final List<QName> typeNames = extractTypeName(typeName, mapping);

        return new DescribeFeatureTypeType(service, version, handle, typeNames, outputFormat);
    }

    private GetCapabilitiesType createNewGetCapabilitiesRequest() throws CstlServiceException {
        String version = getParameter(ACCEPT_VERSIONS_PARAMETER, false);
        AcceptVersionsType versions;
        if (version != null) {
            if (version.indexOf(',') != -1) {
                version = version.substring(0, version.indexOf(','));
            }
            versions = new AcceptVersionsType(version);
        } else {
             versions = new AcceptVersionsType("1.1.0");
        }
        
        final String updateSequence = getParameter(UPDATESEQUENCE_PARAMETER, false);

        final AcceptFormatsType formats = new AcceptFormatsType(getParameter(ACCEPT_FORMATS_PARAMETER, false));

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        final SectionsType sections;
        final String section = getParameter(SECTIONS_PARAMETER, false);
        if (section != null && !section.equalsIgnoreCase("All")) {
            final List<String> requestedSections = new ArrayList<String>();
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
            sections = new SectionsType(requestedSections);
        } else {
            sections = null;
            
        }
        
        return new GetCapabilitiesType(versions,
                                       sections,
                                       formats,
                                       updateSequence,
                                       getParameter(SERVICE_PARAMETER, true));

    }

    private GetFeatureType createNewGetFeatureRequest() throws CstlServiceException {
        Integer maxFeature = null;
        final String max = getParameter("maxfeatures", false);
        if (max != null) {
            try {
                maxFeature = Integer.parseInt(max);
            } catch (NumberFormatException ex) {
                throw new CstlServiceException("Unable to parse the integer maxfeatures parameter" + max,
                                                  INVALID_PARAMETER_VALUE, "MaxFeatures");
            }

        }
        final String service = getParameter(SERVICE_PARAMETER, true);
        final String version = getParameter(VERSION_PARAMETER, true);
        final String handle  = getParameter(HANDLE,  false);
        String outputFormat  = getParameter("outputFormat", false);

        if (outputFormat == null) {
            outputFormat = "text/xml; subtype=gml/3.1.1";
        }
        final String namespace = getParameter(NAMESPACE, false);
        final Map<String, String> mapping = WebServiceUtilities.extractNamespace(namespace);

        final String result = getParameter("resultType", false);
        ResultTypeType resultType = null;
        if (result != null) {
            resultType = ResultTypeType.fromValue(result.toLowerCase());
        }
        
        final String featureVersion = getParameter("featureVersion", false);

        String featureId = getParameter("featureid", false);
        boolean mandatory = true;
        if (featureId != null) {
            //cite test fix
            if (featureId.endsWith(",")) {
                featureId = featureId.substring(0, featureId.length() - 1);
            }
            mandatory = false;
        }

        final String typeName = getParameter("typeName", mandatory);
        final List<QName> typeNames = extractTypeName(typeName, mapping);

        if (featureId != null) {
            final QueryType query = new QueryType(null, typeNames, featureVersion);
            return new GetFeatureType(service, version, handle, maxFeature, featureId, Arrays.asList(query), resultType, outputFormat);

        }

        final Object xmlFilter  = getComplexParameter(FILTER, false);

        FilterType filter;
        final Map<String, String> prefixMapping;
        if (xmlFilter instanceof FilterType) {
            filter = (FilterType) xmlFilter;
            prefixMapping = filter.getPrefixMapping();
        } else {
            filter = null;
            prefixMapping = new HashMap<String, String>();
        }

        final String bbox = getParameter("bbox", false);
        if (bbox != null) {
            final double[] coodinates = new double[4];

            final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
            int index = 0;
            while (tokens.hasMoreTokens() && index < 4) {
                final double value = RequestsUtilities.toDouble(tokens.nextToken());
                coodinates[index] = value;
                index++;
            }
            String crs = null;
            if (tokens.hasMoreTokens()) {
                crs = tokens.nextToken();
            }
            
            if (coodinates != null) {
                final BBOXType bboxFilter = new BBOXType("", coodinates[0], coodinates[1], coodinates[2], coodinates[3], crs);
                if (filter == null) {
                    filter = new FilterType(bboxFilter);
                } else {
                    LOGGER.info("unexpected case --> filter + bbox TODO");
                }
            }
        }
        
        final QueryType query = new QueryType(filter, typeNames, featureVersion);

        final String srsName = getParameter("srsName", false);
        query.setSrsName(srsName);

        // TODO handle multiple properties and handle prefixed properties
        String sortByParam = getParameter("sortBy", false);
        if (sortByParam != null) {
            if (sortByParam.indexOf(':') != -1) {
                sortByParam = sortByParam.substring(sortByParam.indexOf(':') + 1);
            }
            //we get the order
            final SortOrder order;
            if (sortByParam.indexOf(' ') != -1) {
                final char cOrder = sortByParam.charAt(sortByParam.length() -1);
                sortByParam = sortByParam.substring(0, sortByParam.indexOf(' '));
                if (cOrder == 'D') {
                    order = SortOrder.DESCENDING;
                } else {
                    order = SortOrder.ASCENDING;
                }
            } else {
                order = SortOrder.ASCENDING;
            }
            final List<SortPropertyType> sortProperties = new ArrayList<SortPropertyType>();
            sortProperties.add(new SortPropertyType(sortByParam, order));
            final SortByType sortBy = new SortByType(sortProperties);
            query.setSortBy(sortBy);
        }

        final String propertyNameParam = getParameter("propertyName", false);
        if (propertyNameParam != null) {
            final List<String> propertyNames = new ArrayList<String>();
            final StringTokenizer tokens = new StringTokenizer(propertyNameParam, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                propertyNames.add(token);
            }
            query.getPropertyNameOrXlinkPropertyNameOrFunction().addAll(propertyNames);
        }
        
        final GetFeatureType gf = new GetFeatureType(service, version, handle, maxFeature, Arrays.asList(query), resultType, outputFormat);
        gf.setPrefixMapping(prefixMapping);
        return gf;
    }

    private GetGmlObjectType createNewGetGmlObjectRequest() throws CstlServiceException {
        final String service      = getParameter(SERVICE_PARAMETER, true);
        final String version      = getParameter(VERSION_PARAMETER, true);
        final String handle       = getParameter(HANDLE,  false);
        final String outputFormat = getParameter("outputFormat", false);
        final String id           = getParameter("gmlobjectid", true);

        final GmlObjectIdType gmlObjectId = new GmlObjectIdType(id);
        return new GetGmlObjectType(service, version, handle, gmlObjectId, outputFormat);
    }

    private LockFeatureType createNewLockFeatureRequest() throws CstlServiceException {
        final String service  = getParameter(SERVICE_PARAMETER, true);
        final String version  = getParameter(VERSION_PARAMETER, true);
        final String handle   = getParameter(HANDLE,  false);

        final String lockAct  = getParameter("lockAction",  false);
        AllSomeType lockAction = null;
        if (lockAct != null) {
            lockAction = AllSomeType.fromValue(lockAct);
        }
        final String exp   = getParameter("expiry",  false);
        Integer expiry     = null;
        if (exp != null) {
            try {
                expiry = Integer.parseInt(exp);
            } catch (NumberFormatException ex) {
                throw new CstlServiceException("The service was to parse the expiry value :" + exp,
                                                  INVALID_PARAMETER_VALUE, "expiry");
            }
        }

        final String namespace            = getParameter(NAMESPACE, false);
        final Map<String, String> mapping = WebServiceUtilities.extractNamespace(namespace);

        final String typeName       = getParameter("typeName", true);
        final List<QName> typeNames = extractTypeName(typeName, mapping);

        final Object xmlFilter  = getComplexParameter(FILTER, false);
        final FilterType filter;
        final Map<String, String> prefixMapping;
        if (xmlFilter instanceof FilterType) {
            filter = (FilterType) xmlFilter;
            prefixMapping = filter.getPrefixMapping();
        } else {
            filter = null;
            prefixMapping = new HashMap<String, String>();
        }
        
        // TODO
        final QName typeNamee = typeNames.get(0);
        final LockType lock = new LockType(filter, handle, typeNamee);

        final LockFeatureType lf = new LockFeatureType(service, version, handle, Arrays.asList(lock), expiry, lockAction);
        lf.setPrefixMapping(prefixMapping);
        return lf;
    }

    private TransactionType createNewTransactionRequest() throws CstlServiceException {
        final String service      = getParameter(SERVICE_PARAMETER, true);
        final String version      = getParameter(VERSION_PARAMETER, true);
        final String handle       = getParameter(HANDLE,  false);
        final String relAct       = getParameter("releaseAction",  false);
        AllSomeType releaseAction = null;
        if (relAct != null) {
            releaseAction = AllSomeType.fromValue(relAct);
        }

        final String namespace            = getParameter(NAMESPACE, false);
        final Map<String, String> mapping = WebServiceUtilities.extractNamespace(namespace);

        final String typeName       = getParameter("typeName", true);
        final List<QName> typeNames = extractTypeName(typeName, mapping);

        final Object xmlFilter  = getComplexParameter(FILTER, false);
        final FilterType filter;
        final Map<String, String> prefixMapping;
        if (xmlFilter instanceof FilterType) {
            filter = (FilterType) xmlFilter;
            prefixMapping = filter.getPrefixMapping();
        } else {
            filter = null;
            prefixMapping = new HashMap<String, String>();
        }

        // TODO
        final QName typeNamee = typeNames.get(0);
        final DeleteElementType delete = new DeleteElementType(filter, handle, typeNamee);
        final TransactionType t = new TransactionType(service, version, handle, releaseAction, delete);
        t.setPrefixMapping(prefixMapping);
        return t;
     }

    /**
     * Extract proper QName from a String list of typeName.
     * @param typeName A String with the pattern: ns1:type1,ns1:type2,ns2:type3
     * @param mapping A Map of  @{<prefix, namespace>}
     *
     * @return A list of QName.
     * @throws CstlServiceException if the pattern of the typeName parameter if wrong,
     *                              or if a prefix is not bounded to a namespace in the mapping map.
     */
    private List<QName> extractTypeName(final String typeName, final Map<String, String> mapping) throws CstlServiceException {
        final List<QName> typeNames = new ArrayList<QName>();
        if (typeName != null) {
            final StringTokenizer tokens = new StringTokenizer(typeName, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf(':') != -1) {
                    final String prefix    = token.substring(0, token.indexOf(':'));
                    final String localPart = token.substring(token.indexOf(':') + 1);
                    final String namesp    = mapping.get(prefix);
                    if (namesp != null) {
                        typeNames.add(new QName(namesp, localPart, prefix));
                    } else {
                        typeNames.add(new QName(prefix, localPart));
                        /*throw new CstlServiceException("The typeName parameter is malformed : the prefix [" + prefix + "] is not bounded with a namespace",
                                                  INVALID_PARAMETER_VALUE, "typeName");*/
                    }
                } else {
                    typeNames.add(new QName(token));
                    /*throw new CstlServiceException("The typeName parameter is malformed : [" + token + "] the good pattern is ns1:feature",
                                                  INVALID_PARAMETER_VALUE, "typeName");*/
                }
            }
        }
        return typeNames;
    }

    public static Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }
    
    /**
     * {@inheritDoc}
     * overriden for extract namespace mapping.
     */
    @Override
    protected Object getComplexParameter(final String parameterName, final boolean mandatory)
                                                                  throws CstlServiceException
    {
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = getMarshallerPool().acquireUnmarshaller();
            final MultivaluedMap<String,String> parameters = getUriContext().getQueryParameters();
            List<String> list = parameters.get(parameterName);
            if (list == null) {
                list = parameters.get(parameterName.toLowerCase());
                if (list == null) {
                    if (!mandatory) {
                        return null;
                    } else {
                        throw new CstlServiceException("The parameter " + parameterName + " must be specified",
                                       MISSING_PARAMETER_VALUE);
                    }
                }
            }
            final StringReader sr                   = new StringReader(list.get(0));
            final Map<String, String> prefixMapping = new LinkedHashMap<String, String>();
            final XMLEventReader rootEventReader    = XMLInputFactory.newInstance().createXMLEventReader(sr);
            final XMLEventReader eventReader        = (XMLEventReader) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{XMLEventReader.class}, new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Object returnVal = method.invoke(rootEventReader, args);
                    if (method.getName().equals("nextEvent")) {
                        XMLEvent evt = (XMLEvent) returnVal;
                        if (evt.isStartElement()) {
                            StartElement startElem = evt.asStartElement();
                            Iterator<Namespace> t = startElem.getNamespaces();
                            while (t.hasNext()) {
                                Namespace n = t.next();
                                prefixMapping.put(n.getPrefix(), n.getNamespaceURI());
                            }
                        }
                    }
                    return returnVal;
                }
            });
            Object result = unmarshaller.unmarshal(eventReader);
            if (result instanceof JAXBElement) {
                result = ((JAXBElement)result).getValue();
            }
            if (result instanceof FilterType) {
                ((FilterType)result).setPrefixMapping(prefixMapping);
            }
            return result;
        } catch (JAXBException ex) {
             throw new CstlServiceException("The xml object for parameter " + parameterName + " is not well formed:" + '\n' +
                            ex, INVALID_PARAMETER_VALUE);
        } catch (XMLStreamException ex) {
             throw new CstlServiceException("The xml object for parameter " + parameterName + " is not well formed:" + '\n' +
                            ex, INVALID_PARAMETER_VALUE);
        } finally {
            if (unmarshaller != null) {
                getMarshallerPool().release(unmarshaller);
            }
        }
    }
}
