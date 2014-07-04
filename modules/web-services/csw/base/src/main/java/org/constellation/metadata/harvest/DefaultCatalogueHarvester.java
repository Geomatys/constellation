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
package org.constellation.metadata.harvest;

// J2SE dependencies

import org.apache.sis.xml.Namespaces;
import org.constellation.metadata.DistributedResults;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.metadata.utils.Utils;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.GetRecordsResponse;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.SearchResults;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.csw.xml.v202.SearchResultsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.NotType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLikeType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ows.xml.v100.AcceptFormatsType;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.CapabilitiesBaseType;
import org.geotoolkit.ows.xml.v100.DCP;
import org.geotoolkit.ows.xml.v100.DomainType;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.ows.xml.v100.Operation;
import org.geotoolkit.ows.xml.v100.OperationsMetadata;
import org.geotoolkit.ows.xml.v100.RequestMethodType;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.util.StringUtilities;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static org.constellation.metadata.CSWConstants.CSW;
import static org.constellation.metadata.CSWConstants.CSW_202_VERSION;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;

// JAXB dependencies
// Constellation dependencies
// Geotoolkit dependencies

/**
 *
 * @author Guilhem Legal
 */
public class DefaultCatalogueHarvester extends CatalogueHarvester {

    /**
     * A getRecords request used to request another csw (2.0.2).
     */
    private GetRecordsType fullGetRecordsRequestv202;

    /**
     * A getRecords request used to request another csw(2.0.0).
     */
    private org.geotoolkit.csw.xml.v200.GetRecordsType fullGetRecordsRequestv200;

    /**
     * A special getRecords request used to request another unstandardized csw (2.0.0).
     */
    private org.geotoolkit.csw.xml.v200.GetRecordsType fullGetRecordsRequestv200Special1;

    /**
     * A getCapabilities request used request another csw(2.0.2)
     */
    private static final GetCapabilitiesType GETCAPABILITIES_V202;

    /**
     * A getCapabilities request used request another csw(2.0.0)
     */
    private static final org.geotoolkit.csw.xml.v200.GetCapabilitiesType GETCAPABILITIES_V200;

    static {
        //we build the base request to get the capabilities of anoter CSW service (2.0.2)
        final AcceptVersionsType versions = new AcceptVersionsType(CSW_202_VERSION, "2.0.0");
        final SectionsType sections       = new SectionsType("All");
        final AcceptFormatsType formats   = new AcceptFormatsType(MimeType.TEXT_XML, MimeType.APPLICATION_XML);
        GETCAPABILITIES_V202            = new GetCapabilitiesType(versions, sections, formats, null, CSW);

        //we build the base request to get the capabilities of anoter CSW service (2.0.0)
        GETCAPABILITIES_V200            = new org.geotoolkit.csw.xml.v200.GetCapabilitiesType(versions, sections, formats, null, CSW);
    }

    /**
     * A global variable used during the harvest of a distant CSW.
     * it record the supported outputSchema in the GetRecords request of the distant web service.
     */
    private String bestDistantOuputSchema;

    /**
     * a QName for csw:Record type
     */
    private static final QName RECORD_QNAME = new QName(Namespaces.CSW, "Record");

    /**
     * a QName for gmd:Dataset type
     */
    private static final QName DATASET_QNAME = new QName(Namespaces.GMD, "Dataset");

    /**
     * A flag indicating that we are harvesting a CSW special case 1
     */
    private boolean specialCase1 = false;

    /**
     * Build a new catalogue harvester with the write part.
     * 
     * @param metadataWriter a writer to the metadata datasource.
     */
    public DefaultCatalogueHarvester(final MetadataWriter metadataWriter) {
       super(metadataWriter);
        initializeRequest();
    }

    /**
     * Initialize The object request to harvest distant CSW
     */
    private void initializeRequest() {

        /*
         * we build the first filter : < dublinCore:Title IS LIKE '*' >
         */
        final List<QName> typeNames = new ArrayList<>();
        PropertyNameType pname      = new PropertyNameType("dc:title");
        PropertyIsLikeType pil      = new PropertyIsLikeType(pname, "something?", "*", "?", "\\");
        NotType n                   = new NotType(pil);
        final FilterType filter1    = new FilterType(n);

        /*
         * Second filter a special case for some unstandardized CSW : < title IS NOT LIKE 'something' >
         */
        pname                    = new PropertyNameType("title");
        pil                      = new PropertyIsLikeType(pname, "something", null, null, null);
        n                        = new NotType(pil);
        final FilterType filter2 = new FilterType(n);


        //we build the base request to harvest another CSW service (2.0.2)
        final QueryConstraintType constraint = new QueryConstraintType(filter1, "1.1.0");
        typeNames.add(RECORD_QNAME);
        final QueryType query = new QueryType(typeNames, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        fullGetRecordsRequestv202 = new GetRecordsType(CSW, CSW_202_VERSION, ResultType.RESULTS, null, MimeType.APPLICATION_XML, Namespaces.CSW, 1, 20, query, null);


        //we build the base request to harvest another CSW service (2.0.0)
        org.geotoolkit.csw.xml.v200.QueryConstraintType constraint2 = new org.geotoolkit.csw.xml.v200.QueryConstraintType(filter1, "1.1.0");
        List<QName> typeNames2 = new ArrayList<>();
        typeNames2.add(DATASET_QNAME);
        org.geotoolkit.csw.xml.v200.QueryType query2 = new org.geotoolkit.csw.xml.v200.QueryType(typeNames2,
                                                                                         new org.geotoolkit.csw.xml.v200.ElementSetNameType(ElementSetType.FULL),
                                                                                         constraint2);
        fullGetRecordsRequestv200 = new org.geotoolkit.csw.xml.v200.GetRecordsType(CSW, "2.0.0", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw", 1, 20, query2, null);


        //we build the special request to harvest unstandardized CSW service (2.0.0)
        constraint2        = new org.geotoolkit.csw.xml.v200.QueryConstraintType(filter2, "1.0.20");
        typeNames2         = new ArrayList<>();
        typeNames2.add(DATASET_QNAME);
        query2             = new org.geotoolkit.csw.xml.v200.QueryType(typeNames2,
                                                                   new org.geotoolkit.csw.xml.v200.ElementSetNameType(ElementSetType.FULL),
                                                                   constraint2);
        fullGetRecordsRequestv200Special1 = new org.geotoolkit.csw.xml.v200.GetRecordsType(CSW, "2.0.0", ResultType.RESULTS, null, MimeType.APPLICATION_XML, null, 1, 20, query2, null);

    }


    /**
     * Harvest another CSW service by getting all this records ans storing it into the database
     *
     * @param sourceURL The URL of the distant CSW service
     *
     * @return An array containing: the number of inserted records, the number of updated records and the number of deleted records.
     */
    @Override
    public int[] harvestCatalogue(String sourceURL) throws MalformedURLException, IOException, CstlServiceException {

        if (metadataWriter == null)
            throw new CstlServiceException("The Service can not write into the database",
                                          OPERATION_NOT_SUPPORTED, "Harvest");

        //first we make a getCapabilities(GET) request to see what service version we have
        Object distantCapabilities = sendRequest(sourceURL + "?request=GetCapabilities&service=CSW", null);

        //if the GET request does not work we try the POST request
        if (distantCapabilities == null) {
            distantCapabilities = sendRequest(sourceURL, GETCAPABILITIES_V202);
            if (distantCapabilities == null) {
                distantCapabilities = sendRequest(sourceURL, GETCAPABILITIES_V200);
            }
        }

        GetRecordsRequest getRecordRequest = null;

        if (distantCapabilities instanceof Capabilities) {
            getRecordRequest = fullGetRecordsRequestv202;

        } else if (distantCapabilities instanceof org.geotoolkit.csw.xml.v200.CapabilitiesType) {
            getRecordRequest = fullGetRecordsRequestv200;

        } else {
            throw new CstlServiceException("This service if it is one is not requestable by constellation",
                                          OPERATION_NOT_SUPPORTED, "ResponseHandler");
        }

        getRecordRequest = analyseCapabilitiesDocument((CapabilitiesBaseType)distantCapabilities, getRecordRequest);

        //we initialize the getRecords request
        getRecordRequest.setStartPosition(1);
        int startPosition    = 1;
        int nbRecordInserted = 0;
        int nbRecordUpdated  = 0;
        boolean succeed      = false;
        boolean firstTry     = true;
        boolean secondTry    = false;

        //we prepare to store the distant serviceException and send it later if this is necessary
        final List<CstlServiceException> distantException = new ArrayList<>();

        //we request all the records for the best outputSchema supported

            LOGGER.log(Level.INFO, "harvesting with outputSchema: {0}", bestDistantOuputSchema);
            startPosition    = 1;

            if (!specialCase1)
                getRecordRequest.setOutputSchema(bestDistantOuputSchema);

            boolean moreResults = true;
            //we make multiple request by pack of 20 record
            while (moreResults) {

                final Object harvested = sendRequest(sourceURL, getRecordRequest);

                // if the service respond with non xml or unstandardized response
                if (harvested == null) {
                    final CstlServiceException exe = new CstlServiceException("The distant service does not respond correctly.",
                                                     NO_APPLICABLE_CODE);
                    LOGGER.severe("The distant service does not respond correctly");
                    distantException.add(exe);
                    moreResults = false;

                // if the service respond correctly  (CSW 2.0.2 and 2.0.0)
                } else if (harvested instanceof GetRecordsResponse) {
                    succeed = true;
                    LOGGER.log(Level.INFO, "Response of distant service:\n{0}", harvested.toString());
                    final GetRecordsResponse serviceResponse = (GetRecordsResponse) harvested;
                    final SearchResults results              = serviceResponse.getSearchResults();
                    final List<Object> records               = results.getAny();

                    //we looking for CSW record
                    for (Object recordObj: records) {
                        if (!(recordObj instanceof Node)) {
                            throw new CstlServiceException("object has been unmarshalled.");
                        }

                        final Node record = (Node)recordObj;
                        final String metadataID = Utils.findIdentifier(record);
                        try {
                            if (!metadataWriter.isAlreadyUsedIdentifier(metadataID)) {
                                if (metadataWriter.storeMetadata(record)) {
                                    nbRecordInserted++;
                                }
                            } else {
                                if (metadataWriter.replaceMetadata(metadataID, record)) {
                                    nbRecordUpdated++;
                                }
                            }
                        } catch (IllegalArgumentException | MetadataIoException e) {
                            throw new CstlServiceException(e, NO_APPLICABLE_CODE);
                        }
                    }

                    //if there is more results we need to make another request
                    moreResults = results.getNumberOfRecordsReturned() != 0;
                    if (moreResults) {
                        startPosition = startPosition + records.size();
                        LOGGER.log(Level.INFO, "startPosition={0}", startPosition);
                        getRecordRequest.setStartPosition(startPosition);
                    }

                // if the distant service has launch a standardized exception
                } else if (harvested instanceof ExceptionReport) {
                    final ExceptionReport ex = (ExceptionReport) harvested;
                    final CstlServiceException exe = new CstlServiceException("The distant service has throw a webService exception: " + ex.getException().get(0),
                                                                      NO_APPLICABLE_CODE);
                    LOGGER.log(Level.WARNING, "The distant service has throw a webService exception: \n{0}", exe.toString());
                    distantException.add(exe);
                    moreResults = false;

                // if we obtain an object that we don't expect
                } else {
                    throw new CstlServiceException("The distant service does not respond correctly: unexpected response type: " + harvested.getClass().getSimpleName(),
                                                 NO_APPLICABLE_CODE);
                }

                //if we don't have succeed we try without constraint part
                if (firstTry && !succeed) {
                    moreResults = true;
                    getRecordRequest.removeConstraint();
                    firstTry    = false;
                    secondTry   = true;
                    LOGGER.info("trying with no constraint request");

                //if we don't succeed agin we try with CQL constraint
                } else if (secondTry && ! succeed) {
                    secondTry   = false;
                    moreResults = true;
                    getRecordRequest.setCQLConstraint("title NOT LIKE 'something'");
                    LOGGER.info("trying with CQL constraint request");
                }
            }


        if (!succeed && distantException.size() > 0) {
            throw distantException.get(0);
        }

        final int[] result = new int [3];
        result[0]    = nbRecordInserted;
        result[1]    = nbRecordUpdated;
        result[2]    = 0;

        specialCase1 = false;
        return result;
    }

    /**
     *  Analyse a capabilities Document and update the specified GetRecords request at the same time.
     */
    private GetRecordsRequest analyseCapabilitiesDocument(CapabilitiesBaseType capa, GetRecordsRequest request) {
        String distantVersion = CSW_202_VERSION;
        final StringBuilder report = new StringBuilder();

        //we get the service version (could be 2.0.0 or 2.0.1 or 2.0.2)
        if (capa.getVersion() != null) {
            distantVersion = capa.getVersion();
        }
        request.setVersion(distantVersion);

        String serviceName = "unknow";
        String special     = "";
        //we get the name of the service
        if (capa.getServiceIdentification() != null) {
            serviceName = capa.getServiceIdentification().getTitle();
        }

        // Special case 1
        if ("IAAA CSW".equals(serviceName)) {
            specialCase1 = true;
            request      = fullGetRecordsRequestv200Special1;
            special      = "Special case 1";
        }

        report.append("CSW ").append(distantVersion).append(" service identified: ").append(serviceName).append(" ").append(special).append('\n');

        //we get the Operations metadata if they are present
        final OperationsMetadata om = capa.getOperationsMetadata();

        //we look for the GetRecords operation.
        final Operation getRecordOp = om.getOperation("GetRecords");
        if (getRecordOp != null) {
            report .append("GetRecords operation supported:").append('\n');

            // if there is only one DCP (most case)
            if (!getRecordOp.getDCP().isEmpty()) {
                int i = 0;
                for (DCP dcp: getRecordOp.getDCP()) {
                    report.append("DCP ").append(i).append(':').append('\n');
                    final List<JAXBElement<RequestMethodType>> protocols = dcp.getHTTP().getRealGetOrPost();
                    report .append("available protocols:").append('\n');
                    for (JAXBElement<RequestMethodType> jb : protocols) {
                        report.append(jb.getName().getLocalPart()).append('\n');
                    }
                    i++;
                }

            // id there is no DCP
            } else {
                report.append("no DCP found").append('\n');
            }

            //we look for the different output schema available
            final DomainType outputDomain = getRecordOp.getParameterIgnoreCase("outputSchema");
            if (outputDomain != null) {
                final List<String> availableOutputSchema = StringUtilities.cleanCharSequences(outputDomain.getValue());
                final String defaultValue                = outputDomain.getDefaultValue();

                if (defaultValue != null && !defaultValue.isEmpty() && !availableOutputSchema.contains(defaultValue))
                    availableOutputSchema.add(defaultValue);


                /* TODO place it elsewhere
                  ugly patch to be compatible with some CSW service who specify the wrong ouputSchema
                  currentDistantOuputSchema.add("csw:Record");*/


                report.append("OutputSchema supported:").append('\n');
                for (String osc: availableOutputSchema) {
                    report.append('\t').append("- ").append(osc).append('\n');
                }
                bestDistantOuputSchema = getBestOutputSchema(availableOutputSchema);

            } else {
                report.append("No outputSchema specified using default: http://www.opengis.net/cat/csw/2.0.2");

                //we add the default outputSchema used
                bestDistantOuputSchema = Namespaces.CSW;
            }

            // we look for the different Type names
            DomainType typeNameDomain = getRecordOp.getParameterIgnoreCase("typename");
            if (typeNameDomain == null) {
                typeNameDomain = getRecordOp.getParameterIgnoreCase("typenames");
            }

            final List<QName>  typeNamesQname = new ArrayList<>();
            if (typeNameDomain != null) {
                final List<String> typeNames  = typeNameDomain.getValue();

                boolean defaultTypeName = false;
                final String defaultValue = typeNameDomain.getDefaultValue();
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    typeNames.add(defaultValue);
                    final String prefix       = defaultValue.substring(0, defaultValue.indexOf(':'));
                    final String localPart    = defaultValue.substring(defaultValue.indexOf(':') + 1, defaultValue.length());
                    final String namespaceURI = getNamespaceURIFromprefix(prefix, distantVersion);
                    typeNamesQname.add(new QName(namespaceURI, localPart, prefix));
                    defaultTypeName = true;
                }

                report.append("TypeNames supported:").append('\n');
                for (String osc: typeNames) {

                    //we remove the bad character before the real value
                    while ((osc.startsWith(" ") || osc.startsWith("\n") || osc.startsWith("/t")) && osc.length() > 0) {
                        osc = osc.substring(1);
                    }

                    report.append('\t').append("- ").append(osc).append('\n');
                    if (defaultTypeName) {
                        String prefix, localPart;
                        if (osc.indexOf(':') != -1) {
                            prefix    = osc.substring(0, osc.indexOf(':'));
                            localPart = osc.substring(osc.indexOf(':') + 1, osc.length());
                            final String namespaceURI = getNamespaceURIFromprefix(prefix, distantVersion);
                            typeNamesQname.add(new QName(namespaceURI, localPart, prefix));
                        } else {
                            LOGGER.severe("NO ':' in Typenames => unexpected!!!");
                        }
                    }
                }
            } else {
                report.append("No outputSchema specified using default:\n\tcsw:Record\n");

                //we add the default typeNames used
                typeNamesQname.add(RECORD_QNAME);
            }
            //we update the request TODO
            request.setTypeNames(Arrays.asList(RECORD_QNAME));

        } else {
            report.append("No GetRecords operation find").append('\n');
        }

        LOGGER.info(report.toString());
        return request;
    }

    private String getBestOutputSchema(List<String> availableOutputSchema) {
        if (availableOutputSchema.isEmpty()) {
            //default case
            return Namespaces.CSW;

        } else if (availableOutputSchema.size() == 1) {
            return availableOutputSchema.get(0);

        // Priority to the ISO schema
        } else if (availableOutputSchema.contains(Namespaces.GMD)) {
            return Namespaces.GMD;

        } else if (availableOutputSchema.contains("csw:profile")) {
            return "csw:profile";

        // else to Dublincore schema
        } else if (availableOutputSchema.contains(Namespaces.CSW)) {
            return Namespaces.CSW;

        } else if (availableOutputSchema.contains("csw:record")) {
            return "csw:record";

        } else if (availableOutputSchema.contains("csw:Record")) {
            return "csw:Record";

        } else if (availableOutputSchema.contains("ISO19139")) {
            return "ISO19139";

        } else if (availableOutputSchema.contains("OGCCORE")) {
            return "OGCCORE";

        } else if (availableOutputSchema.contains("DublinCore")) {
            return "DublinCore";
        } else {
            LOGGER.severe("unable to found a outputSchema!!!");
            return Namespaces.CSW;
        }
    }

    /**
     * Send a request to another CSW service.
     *
     * @param sourceURL the URL of the distant web-service
     * @param request The XML object to send in POST mode (if null the request is GET)
     *
     * @return The object corresponding to the XML response of the distant web-service
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    private Object sendRequest(String sourceURL, Object request) throws MalformedURLException, CstlServiceException, IOException {

        final URL source          = new URL(sourceURL);
        final URLConnection conec = source.openConnection();
        Object harvested    = null;

        try {

            // for a POST request
            if (request != null) {

                conec.setDoOutput(true);
                conec.setRequestProperty("Content-Type", MimeType.TEXT_XML);
                final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
                final StringWriter sw = new StringWriter();
                try {
                    final Marshaller marshaller = marshallerPool.acquireMarshaller();
                    marshaller.marshal(request, sw);
                    marshallerPool.recycle(marshaller);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Unable to marshall the request: " + ex.getMessage(),
                                                 NO_APPLICABLE_CODE);
                }
                String xmlRequest = sw.toString();

                // in the special case 1 we need to remove ogc prefix inside  the >Filter
                if (specialCase1) {
                    xmlRequest = xmlRequest.replace("<ogc:", "<");
                    xmlRequest = xmlRequest.replace("</ogc:", "</");
                    xmlRequest = xmlRequest.replace("<Filter", "<ogc:Filter");
                    xmlRequest = xmlRequest.replace("</Filter", "</ogc:Filter");
                    xmlRequest = xmlRequest.replace("xmlns:gco=\"http://www.isotc211.org/2005/gco\""    , "");
                    xmlRequest = xmlRequest.replace("xmlns:gmd=\"http://www.isotc211.org/2005/gmd\""    , "");
                    xmlRequest = xmlRequest.replace("xmlns:dc=\"http://purl.org/dc/elements/1.1/\""     , "");
                    xmlRequest = xmlRequest.replace("xmlns:dc2=\"http://www.purl.org/dc/elements/1.1/\"", "");
                    xmlRequest = xmlRequest.replace("xmlns:dct2=\"http://www.purl.org/dc/terms/\""      , "");
                    LOGGER.log(Level.INFO, "special obtained request: \n{0}", xmlRequest);
                }
                LOGGER.log(Level.INFO, "sended:{0} to {1}", new Object[]{xmlRequest, sourceURL});
                wr.write(xmlRequest);
                wr.flush();
            }

            /*
             * 4.2- Check if it is XML, first line must start with '<?xml'
             */
            conec.setReadTimeout(20000);
            InputStream in = conec.getInputStream();
            if (!in.markSupported()) {
                in = new BufferedInputStream(in);
            }
            in.mark(60);
            final StringWriter firstBlock = new StringWriter();
            final byte[] firstbuffer = new byte[60];
            firstBlock.write(new String(firstbuffer, 0, in.read(firstbuffer, 0, 60)));
            String first = firstBlock.toString();
            if (!first.startsWith("<?xml version=\"1.0\"")) {
                throw new CstlServiceException("The response when communicating with " + sourceURL + "  is not a valid XML format !");
            }


            /*
             * 4.3- Find encoding and convert to string,
             * @TODO at first we need to use a string here because there are several usecases to apply some fix for special catalogs
             * but it could be better to use a filter writer
             */
            String encoding = "UTF-8";
            if (first.indexOf("encoding=\"") != -1) {
                final String temp = first.substring(first.indexOf("encoding=\"") + 10);
                encoding = temp.substring(0, temp.indexOf('"'));
            }

            LOGGER.log(Level.INFO, "response encoding : {0}", encoding);

            /*
             * 4.4- Return string or unmarshalled object depending on if the MarshallerPool mpool is null
             */
            //we must use a string because we have a fix for no standard catalogs
            //@TODO use a FilterReader/Writer to apply the fix per lines when reading the response instead of passing by a string.

            in.reset();
            InputStreamReader conv = new InputStreamReader(in, encoding);


            final StringWriter out = new StringWriter();
            char[] buffer          = new char[1024];
            int size;

            while ((size = conv.read(buffer, 0, 1024)) > 0) {
                out.write(new String(buffer, 0, size));
            }

            //we convert the brut String value into UTF-8 encoding
            String brutString = out.toString();

            //we need to replace % character by "percent because they are reserved char for url encoding
            brutString = brutString.replaceAll("%", "percent");
            String decodedString = java.net.URLDecoder.decode(brutString, "UTF-8");

            /*
            * Some implemention replace the standardized namespace "http://www.opengis.net/cat/csw" by "http://www.opengis.net/csw"
            * if we detect this we replace this namespace before unmarshalling the object.
            *
            * TODO replace even when the prefix is not "csw" or blank
            */
            if (decodedString.contains("xmlns:csw=\"http://www.opengis.net/csw\"")) {
                decodedString = decodedString.replace("xmlns:csw=\"http://www.opengis.net/csw\"", "xmlns:csw=\"http://www.opengis.net/cat/csw\"");
            }

            try {
                Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                harvested = unmarshaller.unmarshal(new StringReader(decodedString));
                marshallerPool.recycle(unmarshaller);
                if (harvested instanceof JAXBElement) {
                    harvested = ((JAXBElement) harvested).getValue();
                }
            } catch (JAXBException | IllegalAccessError ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.severe("The Distant service have made an error");
            return null;
        }
        return harvested;
    }


    /**
     * return The namespace URI for the specified prefix end version.
     * caution: the prefix are not dynamically attributed.
     *
     * @param prefix
     * @param distantVersion
     * @return
     */
    private String getNamespaceURIFromprefix(String prefix, String distantVersion) {
        if (distantVersion.equals(CSW_202_VERSION)) {
            if ("csw".equals(prefix))
                return Namespaces.CSW;

            else if ("ebrim".equals(prefix) || "rim".equals(prefix))
                return "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

            else if ("rim25".equals(prefix))
                return "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5";

            else if ("gmd".equals(prefix))
                return Namespaces.GMD;

            else if ("wrs".equals(prefix))
                return "http://www.opengis.net/cat/wrs/1.0";

             else if ("wrs09".equals(prefix))
                return "http://www.opengis.net/cat/wrs";

            else
                throw new IllegalArgumentException("2.0.2 prefix unsupported: " + prefix + ".");
        } else {
            if ("csw".equals(prefix))
                return "http://www.opengis.net/cat/csw";

            else if ("ebrim".equals(prefix) || "rim".equals(prefix) || "rim25".equals(prefix))
                return "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5";

            else if ("gmd".equals(prefix))
                return Namespaces.GMD;

            else if ("wrs".equals(prefix) || "wrs09".equals(prefix))
                return "http://www.opengis.net/cat/wrs";

            else
                throw new IllegalArgumentException("2.0.0 prefix unsupported: " + prefix + ".");
        }
    }

    /**
     * Transfer The request to all the servers specified in distributedServers.
     *
     * @return
     */
    @Override
    public DistributedResults transferGetRecordsRequest(GetRecordsRequest request, List<String> distributedServers,
            int startPosition, int maxRecords) {
        final List<Object> additionalResults = new ArrayList<>();
        int matched = 0;
        for (String serverURL : distributedServers) {
            request.setStartPosition(startPosition);
            request.setMaxRecords(maxRecords);

            try {

                final Object response = sendRequest(serverURL, request);

                if (response instanceof GetRecordsResponseType) {

                    LOGGER.log(Level.INFO, "Response of distant service:\n{0}", response.toString());
                    final GetRecordsResponseType serviceResponse = (GetRecordsResponseType) response;
                    final SearchResultsType results = serviceResponse.getSearchResults();

                    //we looking for CSW record
                    for (Object otherRecord : results.getAny()) {
                        if (otherRecord instanceof JAXBElement) {
                            otherRecord = ((JAXBElement) otherRecord).getValue();
                        }
                        additionalResults.add(otherRecord);
                    }
                    matched = matched + results.getNumberOfRecordsMatched();
                    //if we have enought results a this point we stop requesting other CSW
                    if (additionalResults.size() == maxRecords) {
                        break;
                    } else {
                        startPosition = 1;
                        maxRecords    = maxRecords - additionalResults.size();

                    }
                }

            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, "{0} is a malformed URL. unable to request that service", serverURL);
            } catch (CstlServiceException ex) {
                LOGGER.warning(ex.getMessage());
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "IO exeception while distibuting the request: {0}", ex.getMessage());
            }
        }

        return new DistributedResults(matched, additionalResults);
    }

    @Override
    protected InputStream getSingleMetadata(String sourceURL) throws CstlServiceException {
        try {
            final URL source = new URL(sourceURL);
            final URLConnection conec = source.openConnection();
            return conec.getInputStream();
        } catch (IOException ex) {
            throw new CstlServiceException(ex);
        }
    }
}
