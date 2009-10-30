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

package org.constellation.wfs.ws.rs;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.constellation.ServiceDef;
import org.constellation.ws.rs.OGCWebService;

// jersey dependencies
import com.sun.jersey.spi.resource.Singleton;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.constellation.util.Util;
import org.constellation.wfs.WFSWorker;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.GmlObjectIdType;
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
import org.geotoolkit.wfs.xml.v110.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.TransactionType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("wfs")
@Singleton
public class WFSService extends OGCWebService {

     /**
     * use for debugging purpose
     */
    private Logger logger = Logger.getLogger("org.constellation.security.wfs");
    
    private WFSWorker worker;

    /**
     * Build a new Restfull WFS service.
     */
    public WFSService() {
        super(ServiceDef.WFS_1_1_0);
        try {
            setXMLContext("org.constellation.wfs.v110" +
            		  ":org.constellation.ogc:org.constellation.citygml.v100" +
            		  ":org.constellation.citygml.v100.building" +
            		  ":org.constellation.citygml.v100.transportation" +
            		  ":org.constellation.citygml.v100.cityfurniture" +
            		  ":exp.ows6.utds.v030:org.constellation.xsd.v2001", "");
            worker = new WFSWorker();

        } catch (JAXBException ex){
            LOGGER.severe("The WFS service is not running."       + '\n' +
                          " cause  : Error creating XML context." + '\n' +
                          " error  : " + ex.getMessage()          + '\n' +
                          " details: " + ex.toString());
            worker = null;
        } catch (CstlServiceException ex){
            LOGGER.severe("The WFS service is not running."       + '\n' +
                          " cause  : Error connecting the PEP." + '\n' +
                          " error  : " + ex.getMessage()          + '\n' +
                          " details: " + ex.toString());
            worker = null;
        }
        if (worker != null) {
            logger.info("WFS Service started");
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
        Marshaller marshaller = null;
        ServiceDef serviceDef = null;
        try {
            marshaller = getMarshallerPool().acquireMarshaller();

            if (worker != null) {

                logParameters();
                String request = "";

                if (objectRequest instanceof JAXBElement) {
                    objectRequest = ((JAXBElement<?>)objectRequest).getValue();
                }

                // if the request is not an xml request we fill the request parameter.
                if (objectRequest == null) {
                    request = (String) getParameter("REQUEST", true);
                }

                if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilitiesType)) {

                    GetCapabilitiesType gc = (GetCapabilitiesType)objectRequest;

                    if (gc == null) {
                         /*
                          * if the parameters have been send by GET or POST kvp,
                          * we build a request object with this parameter.
                          */
                        gc = createNewGetCapabilitiesRequest();
                    }
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getCapabilities(gc), sw);

                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();

                } else if (request.equalsIgnoreCase("DescribeFeatureType") || (objectRequest instanceof DescribeFeatureTypeType)) {

                    DescribeFeatureTypeType gr = (DescribeFeatureTypeType)objectRequest;

                    if (gr == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        gr = createNewDescribeFeatureTypeRequest();
                    }

                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.describeFeatureType(gr), sw);
                    
                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();

                } if (request.equalsIgnoreCase("GetFeature") || (objectRequest instanceof GetFeatureType)) {

                    GetFeatureType grbi = (GetFeatureType)objectRequest;

                    if (grbi == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        grbi = createNewGetFeatureRequest();
                    }
                    serviceDef = getVersionFromNumber(grbi.getVersion());
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getFeature(grbi), sw);

                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();

                } if (request.equalsIgnoreCase("getGMLObject") || (objectRequest instanceof GetGmlObjectType)) {

                    GetGmlObjectType dr = (GetGmlObjectType)objectRequest;

                    if (dr == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        dr = createNewGetGmlObjectRequest();
                    }

                    serviceDef = getVersionFromNumber(dr.getVersion());
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.getGMLObject(dr), sw);

                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();

                } if (request.equalsIgnoreCase("lockFeature") || (objectRequest instanceof LockFeatureType)) {

                    LockFeatureType gd = (LockFeatureType)objectRequest;

                    if (gd == null) {
                        /*
                         * if the parameters have been send by GET or POST kvp,
                         * we build a request object with this parameter.
                         */
                        gd = createNewLockFeatureRequest();
                    }

                    serviceDef = getVersionFromNumber(gd.getVersion());
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.lockFeature(gd), sw);

                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();

                } if (request.equalsIgnoreCase("Transaction") || (objectRequest instanceof TransactionType)) {

                    TransactionType t = (TransactionType)objectRequest;

                    if (t == null) {
                         t = createNewTransactionRequest();
                    }

                    serviceDef = getVersionFromNumber(t.getVersion());
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(worker.transaction(t), sw);

                    return Response.ok(sw.toString(), worker.getOutputFormat()).build();

                } else {
                    if (request.equals("") && objectRequest != null)
                        request = objectRequest.getClass().getName();
                    else if (request.equals("") && objectRequest == null)
                        request = "undefined request";

                    throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                                  INVALID_PARAMETER_VALUE, "request");
                }
            } else {
                throw new CstlServiceException("The WFS service is not running",
                                              NO_APPLICABLE_CODE);
            }

        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef);
        } finally {
            if (marshaller != null) {
                getMarshallerPool().release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) throws JAXBException {
        /* We don't print the stack trace:
         * - if the user have forget a mandatory parameter.
         * - if the version number is wrong.
         * - if the user have send a wrong request parameter
         */
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
            //ex.printStackTrace();
            } else {
            LOGGER.severe("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
        }
        if (isJaxBContextValid()) {
            if (serviceDef == null) {
                serviceDef = getBestVersion(null);
            }
            final String version = serviceDef.exceptionVersion.toString();
            ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getExceptionCode().name(), ex.getLocator(), version);
            StringWriter sw = new StringWriter();
            Marshaller marshaller = null;
            try {
                marshaller = getMarshallerPool().acquireMarshaller();
                marshaller.marshal(report, sw);
            } finally {
                if (marshaller != null) {
                    getMarshallerPool().release(marshaller);
                }
            }
            return Response.ok(Util.cleanSpecialCharacter(sw.toString()), "text/xml").build();
        } else {
            return Response.ok("The WFS server is not running cause: unable to create JAXB context!", "text/plain").build();
        }
    }


    @Override
    public void destroy() {
        // do something
    }

    private DescribeFeatureTypeType createNewDescribeFeatureTypeRequest() throws CstlServiceException {
        String outputFormat = getParameter("outputFormat", false);
        String handle       = getParameter("handle", false);
        String service      = getParameter("service", true);
        String version      = getParameter("version", true);

        if (outputFormat == null)
            outputFormat = "text/xml; subtype=gml/3.1.1";
        String namespace = getParameter("namespace", false);
        Map<String, String> mapping = new HashMap<String, String>();
        if (namespace != null) {
            final StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf("xmlns(") != -1 && token.indexOf(')') != -1 && token.indexOf('=') != -1) {
                    String tmp = token.substring(token.indexOf("xmlns(") + 6, token.indexOf(')'));
                    String prefix = tmp.substring(0, tmp.indexOf('='));
                    String namesp = tmp.substring(tmp.indexOf('=') + 1);
                    mapping.put(prefix, namesp);

                } else {
                    throw new CstlServiceException("The namespace parameter is malformed : [" + token + "] the good pattern is xmlns(ns1=http://my_ns1.com)",
                                                  INVALID_PARAMETER_VALUE, "namespace");
                }
            }
        }
        List<QName> typeNames = new ArrayList<QName>();
        String typeName = getParameter("typeName", false);
        if (typeName != null) {
            final StringTokenizer tokens = new StringTokenizer(typeName, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf(':') != -1) {
                    String prefix    = token.substring(0, token.indexOf(':'));
                    String localPart = token.substring(token.indexOf(':') + 1);
                    String namesp = mapping.get(prefix);
                    if (namesp != null) {
                        typeNames.add(new QName(namesp, localPart, prefix));
                    } else {
                        throw new CstlServiceException("The typeName parameter is malformed : the prefix [" + prefix + "] is not bounded with a namespace",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                    }
                } else {
                    throw new CstlServiceException("The typeName parameter is malformed : [" + token + "] the good pattern is ns1:feature",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                }
            }
        }

        return new DescribeFeatureTypeType(service, version, handle, typeNames, outputFormat);
    }

    private GetCapabilitiesType createNewGetCapabilitiesRequest() throws CstlServiceException {
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
                                                  INVALID_PARAMETER_VALUE, "Sections");
                }
            }
        } else {
            //if there is no requested Sections we add all the sections
            requestedSections = SectionsType.getExistingSections();
        }
        SectionsType sections       = new SectionsType(requestedSections);
        return new GetCapabilitiesType(versions,
                                       sections,
                                       formats,
                                       null,
                                       getParameter("SERVICE", true));

    }

    private GetFeatureType createNewGetFeatureRequest() throws CstlServiceException {
        Integer maxFeature = null;
        String max = getParameter("maxfeatures", false);
        if (max != null) {
            try {
                maxFeature = Integer.parseInt(max);
            } catch (NumberFormatException ex) {
                throw new CstlServiceException("Unable to parse the integer maxfeatures parameter" + max + "",
                                                  INVALID_PARAMETER_VALUE, "MaxFeatures");
            }

        }
        String service      = getParameter("service", true);
        String version      = getParameter("version", true);
        String handle       = getParameter("handle",  false);
        String outputFormat = getParameter("outputFormat", false);

        if (outputFormat == null)
            outputFormat = "text/xml; subtype=gml/3.1.1";
        String namespace = getParameter("namespace", false);
        Map<String, String> mapping = new HashMap<String, String>();
        if (namespace != null) {
            final StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf("xmlns(") != -1 && token.indexOf(')') != -1 && token.indexOf('=') != -1) {
                    String tmp = token.substring(token.indexOf("xmlns(") + 6, token.indexOf(')'));
                    String prefix = tmp.substring(0, tmp.indexOf('='));
                    String namesp = tmp.substring(tmp.indexOf('=') + 1);
                    mapping.put(prefix, namesp);

                } else {
                    throw new CstlServiceException("The namespace parameter is malformed : [" + token + "] the good pattern is xmlns(ns1=http://my_ns1.com)",
                                                  INVALID_PARAMETER_VALUE, "namespace");
                }
            }
        }
        List<QName> typeNames = new ArrayList<QName>();
        String typeName = getParameter("typeName", true);
        if (typeName != null) {
            final StringTokenizer tokens = new StringTokenizer(typeName, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf(':') != -1) {
                    String prefix    = token.substring(0, token.indexOf(':'));
                    String localPart = token.substring(token.indexOf(':') + 1);
                    String namesp = mapping.get(prefix);
                    if (namesp != null) {
                        typeNames.add(new QName(namesp, localPart, prefix));
                    } else {
                        throw new CstlServiceException("The typeName parameter is malformed : the prefix [" + prefix + "] is not bounded with a namespace",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                    }
                } else {
                    throw new CstlServiceException("The typeName parameter is malformed : [" + token + "] the good pattern is ns1:feature",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                }
            }
        }

        String xmlFilter = getParameter("filter", false);
        FilterType filter = null;
        if (xmlFilter != null) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = getMarshallerPool().acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(new StringReader(xmlFilter));
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (!(obj instanceof FilterType)) {
                    String type = "null";
                    if (obj != null) {
                        type = obj.getClass().getName();
                    }
                    throw new CstlServiceException("The xml filter does not have the good type:" + type,
                                                  INVALID_PARAMETER_VALUE, "filter");
                } else {
                    filter = (FilterType) obj;
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException("The service was unable to read the xml filter:" + ex.getMessage(),
                                                  INVALID_PARAMETER_VALUE, "filter");
            } finally {
                if (unmarshaller != null) {
                    getMarshallerPool().release(unmarshaller);
                }
            }
        }

        String featureVersion = getParameter("featureVersion", false);

        QueryType query = new QueryType(filter, typeNames, featureVersion);

        String result = getParameter("resultType", false);
        ResultTypeType resultType = null;
        if (result != null) {
            resultType = ResultTypeType.fromValue(result);
        }
        return new GetFeatureType(service, version, handle, maxFeature, Arrays.asList(query), resultType, outputFormat);


    }

    private GetGmlObjectType createNewGetGmlObjectRequest() throws CstlServiceException {
        String service      = getParameter("service", true);
        String version      = getParameter("version", true);
        String handle       = getParameter("handle",  false);
        String outputFormat = getParameter("outputFormat", false);
        String id           = getParameter("gmlobjectid", true);

        GmlObjectIdType gmlObjectId = new GmlObjectIdType(id);
        return new GetGmlObjectType(service, version, handle, gmlObjectId, outputFormat);
    }

    private LockFeatureType createNewLockFeatureRequest() throws CstlServiceException {
        String service      = getParameter("service", true);
        String version      = getParameter("version", true);
        String handle       = getParameter("handle",  false);

        String lockAct      = getParameter("lockAction",  false);
        AllSomeType lockAction = null;
        if (lockAct != null) {
            lockAction = AllSomeType.fromValue(lockAct);
        }
        String exp         = getParameter("expiry",  false);
        Integer expiry     = null;
        if (exp != null) {
            try {
                expiry = Integer.parseInt(exp);
            } catch (NumberFormatException ex) {
                throw new CstlServiceException("The service was to parse the expiry value :" + exp,
                                                  INVALID_PARAMETER_VALUE, "expiry");
            }
        }

        String namespace = getParameter("namespace", false);
        Map<String, String> mapping = new HashMap<String, String>();
        if (namespace != null) {
            final StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf("xmlns(") != -1 && token.indexOf(')') != -1 && token.indexOf('=') != -1) {
                    String tmp = token.substring(token.indexOf("xmlns(") + 6, token.indexOf(')'));
                    String prefix = tmp.substring(0, tmp.indexOf('='));
                    String namesp = tmp.substring(tmp.indexOf('=') + 1);
                    mapping.put(prefix, namesp);

                } else {
                    throw new CstlServiceException("The namespace parameter is malformed : [" + token + "] the good pattern is xmlns(ns1=http://my_ns1.com)",
                                                  INVALID_PARAMETER_VALUE, "namespace");
                }
            }
        }
        List<QName> typeNames = new ArrayList<QName>();
        String typeName = getParameter("typeName", true);
        if (typeName != null) {
            final StringTokenizer tokens = new StringTokenizer(typeName, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf(':') != -1) {
                    String prefix    = token.substring(0, token.indexOf(':'));
                    String localPart = token.substring(token.indexOf(':') + 1);
                    String namesp = mapping.get(prefix);
                    if (namesp != null) {
                        typeNames.add(new QName(namesp, localPart, prefix));
                    } else {
                        throw new CstlServiceException("The typeName parameter is malformed : the prefix [" + prefix + "] is not bounded with a namespace",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                    }
                } else {
                    throw new CstlServiceException("The typeName parameter is malformed : [" + token + "] the good pattern is ns1:feature",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                }
            }
        }

        String xmlFilter = getParameter("filter", false);
        FilterType filter = null;
        if (xmlFilter != null) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = getMarshallerPool().acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(new StringReader(xmlFilter));
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (!(obj instanceof FilterType)) {
                    String type = "null";
                    if (obj != null) {
                        type = obj.getClass().getName();
                    }
                    throw new CstlServiceException("The xml filter does not have the good type:" + type,
                                                  INVALID_PARAMETER_VALUE, "filter");
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException("The service was unable to read the xml filter:" + ex.getMessage(),
                                                  INVALID_PARAMETER_VALUE, "filter");
            } finally {
                if (unmarshaller != null) {
                    getMarshallerPool().release(unmarshaller);
                }
            }
        }
        // TODO
        QName typeNamee = typeNames.get(0);
        LockType lock = new LockType(filter, handle, typeNamee);

        return new LockFeatureType(service, version, handle, Arrays.asList(lock), expiry, lockAction);
    }

    private TransactionType createNewTransactionRequest() throws CstlServiceException {
        String service      = getParameter("service", true);
        String version      = getParameter("version", true);
        String handle       = getParameter("handle",  false);
        String relAct       = getParameter("releaseAction",  false);
        AllSomeType releaseAction = null;
        if (relAct != null) {
            releaseAction = AllSomeType.fromValue(relAct);
        }

         String namespace = getParameter("namespace", false);
        Map<String, String> mapping = new HashMap<String, String>();
        if (namespace != null) {
            final StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf("xmlns(") != -1 && token.indexOf(')') != -1 && token.indexOf('=') != -1) {
                    String tmp = token.substring(token.indexOf("xmlns(") + 6, token.indexOf(')'));
                    String prefix = tmp.substring(0, tmp.indexOf('='));
                    String namesp = tmp.substring(tmp.indexOf('=') + 1);
                    mapping.put(prefix, namesp);

                } else {
                    throw new CstlServiceException("The namespace parameter is malformed : [" + token + "] the good pattern is xmlns(ns1=http://my_ns1.com)",
                                                  INVALID_PARAMETER_VALUE, "namespace");
                }
            }
        }
        List<QName> typeNames = new ArrayList<QName>();
        String typeName = getParameter("typeName", true);
        if (typeName != null) {
            final StringTokenizer tokens = new StringTokenizer(typeName, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (token.indexOf(':') != -1) {
                    String prefix    = token.substring(0, token.indexOf(':'));
                    String localPart = token.substring(token.indexOf(':') + 1);
                    String namesp = mapping.get(prefix);
                    if (namesp != null) {
                        typeNames.add(new QName(namesp, localPart, prefix));
                    } else {
                        throw new CstlServiceException("The typeName parameter is malformed : the prefix [" + prefix + "] is not bounded with a namespace",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                    }
                } else {
                    throw new CstlServiceException("The typeName parameter is malformed : [" + token + "] the good pattern is ns1:feature",
                                                  INVALID_PARAMETER_VALUE, "typeName");
                }
            }
        }

        String xmlFilter = getParameter("filter", false);
        FilterType filter = null;
        if (xmlFilter != null) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = getMarshallerPool().acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(new StringReader(xmlFilter));
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (!(obj instanceof FilterType)) {
                    String type = "null";
                    if (obj != null) {
                        type = obj.getClass().getName();
                    }
                    throw new CstlServiceException("The xml filter does not have the good type:" + type,
                                                  INVALID_PARAMETER_VALUE, "filter");
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException("The service was unable to read the xml filter:" + ex.getMessage(),
                                                  INVALID_PARAMETER_VALUE, "filter");
            } finally {
                if (unmarshaller != null) {
                    getMarshallerPool().release(unmarshaller);
                }
            }
        }
        // TODO
        QName typeNamee = typeNames.get(0);
        DeleteElementType delete = new DeleteElementType(filter, handle, typeNamee);
        return new TransactionType(service, version, handle, releaseAction, delete);
    }

}
