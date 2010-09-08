/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.metadata.harvest;

// J2SE dependencies
import org.constellation.metadata.DistributedResults;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.csw.xml.v202.SearchResultsType;
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
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.NotType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLikeType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.xml.Namespaces;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.metadata.CSWConstants.*;

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
    private static final GetCapabilitiesType getCapabilitiesRequestv202;
    
    /**
     * A getCapabilities request used request another csw(2.0.0)
     */
    private static final org.geotoolkit.csw.xml.v200.GetCapabilitiesType getCapabilitiesRequestv200;

    static {
        //we build the base request to get the capabilities of anoter CSW service (2.0.2)
        final AcceptVersionsType versions = new AcceptVersionsType(CSW_202_VERSION, "2.0.0");
        final SectionsType sections       = new SectionsType("All");
        final AcceptFormatsType formats   = new AcceptFormatsType(MimeType.TEXT_XML, MimeType.APPLICATION_XML);
        getCapabilitiesRequestv202            = new GetCapabilitiesType(versions, sections, formats, null, CSW);

        //we build the base request to get the capabilities of anoter CSW service (2.0.0)
        getCapabilitiesRequestv200            = new org.geotoolkit.csw.xml.v200.GetCapabilitiesType(versions, sections, formats, null, CSW);
    }
    
    /**
     * A global variable used during the harvest of a distant CSW.
     * it record the supported outputSchema in the GetRecords request of the distant web service.
     */
    private String bestDistantOuputSchema;
    
    /**
     * a QName for csw:Record type
     */
    private static final QName RECORD_QNAME = new QName(Namespaces.CSW_202, "Record");
    
    /**
     * a QName for gmd:Dataset type
     */
    private static final QName DATASET_QNAME = new QName(Namespaces.GMD, "Dataset");
    
    /**
     * A flag indicating that we are harvesting a CSW special case 1
     */
    private boolean specialCase1 = false;
    
    /**
     * A flag indicating that we are harvesting a CSW special case 2
     */
    private boolean specialCase2 = false;
    
    /**
     * Build a new catalogue harvester with the write part.
     */
    public DefaultCatalogueHarvester(MetadataWriter metadataWriter) {
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
        final List<QName> typeNames = new ArrayList<QName>();
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
        fullGetRecordsRequestv202 = new GetRecordsType(CSW, CSW_202_VERSION, ResultType.RESULTS, null, MimeType.APPLICATION_XML, Namespaces.CSW_202, 1, 20, query, null);
                 
        
        //we build the base request to harvest another CSW service (2.0.0)
        org.geotoolkit.csw.xml.v200.QueryConstraintType constraint2 = new org.geotoolkit.csw.xml.v200.QueryConstraintType(filter1, "1.1.0");
        List<QName> typeNames2 = new ArrayList<QName>();
        typeNames2.add(DATASET_QNAME);
        org.geotoolkit.csw.xml.v200.QueryType query2 = new org.geotoolkit.csw.xml.v200.QueryType(typeNames2,
                                                                                         new org.geotoolkit.csw.xml.v200.ElementSetNameType(ElementSetType.FULL),
                                                                                         constraint2); 
        fullGetRecordsRequestv200 = new org.geotoolkit.csw.xml.v200.GetRecordsType(CSW, "2.0.0", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw", 1, 20, query2, null);
        
        
        //we build the special request to harvest unstandardized CSW service (2.0.0)
        constraint2        = new org.geotoolkit.csw.xml.v200.QueryConstraintType(filter2, "1.0.20");
        typeNames2         = new ArrayList<QName>();
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
    public int[] harvestCatalogue(String sourceURL) throws MalformedURLException, IOException, CstlServiceException, SQLException {
        
        if (metadataWriter == null)
            throw new CstlServiceException("The Service can not write into the database",
                                          OPERATION_NOT_SUPPORTED, "Harvest");
        
        //first we make a getCapabilities(GET) request to see what service version we have
        Object distantCapabilities = sendRequest(sourceURL + "?request=GetCapabilities&service=CSW", null);
        
        //if the GET request does not work we try the POST request
        if (distantCapabilities == null) {
            distantCapabilities = sendRequest(sourceURL, getCapabilitiesRequestv202);
            if (distantCapabilities == null) {
                distantCapabilities = sendRequest(sourceURL, getCapabilitiesRequestv200);
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
        final List<CstlServiceException> distantException = new ArrayList<CstlServiceException>();
        
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
            
                // if the service respond correctly    
                } else if (harvested instanceof GetRecordsResponseType) {
                    succeed = true;
                    LOGGER.log(Level.INFO, "Response of distant service:\n{0}", harvested.toString());
                    final GetRecordsResponseType serviceResponse = (GetRecordsResponseType) harvested;
                    final SearchResultsType results              = serviceResponse.getSearchResults();
            
                    //we looking for CSW record
                    for (JAXBElement<? extends AbstractRecordType> jbRecord: results.getAbstractRecord()) {
                        final AbstractRecordType record = jbRecord.getValue();
                        
                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (metadataWriter.storeMetadata(record))
                                nbRecordInserted++;
                        } catch (IllegalArgumentException e) {
                            nbRecordUpdated++;
                        }  catch (MetadataIoException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }
                    }
                    
                    //we looking for any other Record type
                    for (Object otherRecord: results.getAny()) {
                        if (otherRecord instanceof JAXBElement)
                            otherRecord = ((JAXBElement)otherRecord).getValue();
                        
                        LOGGER.log(Level.INFO, "other Record Type: {0}", otherRecord.getClass().getSimpleName());
                        
                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (metadataWriter.storeMetadata(otherRecord))
                                nbRecordInserted++;
                        } catch (IllegalArgumentException e) {
                            nbRecordUpdated++;
                        } catch (MetadataIoException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }
                    }
                
                    //if there is more results we need to make another request
                    moreResults = results.getNumberOfRecordsReturned() != 0;
                    if (moreResults) {
                        startPosition = startPosition + results.getAbstractRecord().size() + results.getAny().size();
                        LOGGER.log(Level.INFO, "startPosition={0}", startPosition);
                        getRecordRequest.setStartPosition(startPosition);
                    } 
                    
                // a correct response v2.0.0
                } else if (harvested instanceof org.geotoolkit.csw.xml.v200.GetRecordsResponseType) {
                    succeed = true;
                    LOGGER.log(Level.INFO, "Response of distant service:\n{0}", harvested.toString());
                    final org.geotoolkit.csw.xml.v200.GetRecordsResponseType serviceResponse = (org.geotoolkit.csw.xml.v200.GetRecordsResponseType) harvested;
                    final org.geotoolkit.csw.xml.v200.SearchResultsType results              = serviceResponse.getSearchResults();
            
                    //we looking for CSW record
                    for (JAXBElement<? extends org.geotoolkit.csw.xml.v200.AbstractRecordType> jbRecord: results.getAbstractRecord()) {
                        final org.geotoolkit.csw.xml.v200.AbstractRecordType record = jbRecord.getValue();
                        
                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (metadataWriter.storeMetadata(record))
                                nbRecordInserted++;
                        } catch (IllegalArgumentException e) {
                            nbRecordUpdated++;
                        }  catch (MetadataIoException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }
                    }
                
                    //we looking for any other Record type
                    for (Object otherRecord: results.getAny()) {
                        if (otherRecord instanceof JAXBElement)
                            otherRecord = ((JAXBElement)otherRecord).getValue();
                        
                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (metadataWriter.storeMetadata(otherRecord))
                                nbRecordInserted++;
                        } catch (IllegalArgumentException e) {
                            nbRecordUpdated++;
                        }  catch (MetadataIoException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }
                    }
                    
                    //if there is more results we need to make another request
                    moreResults = (results.getAbstractRecord().size() + results.getAny().size()) != 0;
                    if (moreResults) {
                        startPosition = startPosition +  results.getAbstractRecord().size() + results.getAny().size();
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
        specialCase2 = false;
        
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
        if (serviceName.equals("IAAA CSW")) {
            specialCase1 = true;
            request      = fullGetRecordsRequestv200Special1;
            special      = "Special case 1";
            
        // Special case 2    
        } else if (serviceName.contains("INSPIRE EU Geoportal Catalogue")) {
            specialCase2 = true;
            special      = "Special case 2";
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
                final List<String> availableOutputSchema = StringUtilities.cleanStrings(outputDomain.getValue());
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
                bestDistantOuputSchema = Namespaces.CSW_202;
            }
            
            // we look for the different Type names
            DomainType typeNameDomain = getRecordOp.getParameterIgnoreCase("typename");
            if (typeNameDomain == null) {
                typeNameDomain = getRecordOp.getParameterIgnoreCase("typenames");
            }
            
            final List<QName>  typeNamesQname = new ArrayList<QName>();
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
            return Namespaces.CSW_202;
        
        } else if (availableOutputSchema.size() == 1) {
            return availableOutputSchema.get(0);
        
        // Priority to the ISO schema
        } else if (availableOutputSchema.contains(Namespaces.GMD)) {
            return Namespaces.GMD;
        
        } else if (availableOutputSchema.contains("csw:profile")) {
            return "csw:profile";
            
        // else to Dublincore schema    
        } else if (availableOutputSchema.contains(Namespaces.CSW_202)) {
            return Namespaces.CSW_202;
        
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
            return Namespaces.CSW_202;
        }
    }
    
    /**
     * Send a request to another CSW service.
     * 
     * @param sourceURL the url of the distant web-service
     * @param request The XML object to send in POST mode (if null the request is GET)
     * 
     * @return The object correspounding to the XML response of the distant web-service
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
                Marshaller marshaller = null;
                try {
                    marshaller = marshallerPool.acquireMarshaller();
                    marshaller.marshal(request, sw);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Unable to marshall the request: " + ex.getMessage(),
                                                 NO_APPLICABLE_CODE);
                } finally {
                    if (marshaller != null) {
                        marshallerPool.release(marshaller);
                    }
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
                LOGGER.log(Level.INFO, "sended:{0}", xmlRequest);
                wr.write(xmlRequest);
                wr.flush();
            }
        
            // we get the response document
            final InputStream in   = conec.getInputStream();
            final StringWriter out = new StringWriter();
            final byte[] buffer    = new byte[1024];
            int size;

            while ((size = in.read(buffer, 0, 1024)) > 0) {
                out.write(new String(buffer, 0, size));
            }

            //we convert the brut String value into UTF-8 encoding
            String brutString = out.toString();

            //we need to replace % character by "percent because they are reserved char for url encoding
            brutString = brutString.replaceAll("%", "percent");
            String decodedString = java.net.URLDecoder.decode(brutString, "UTF-8");

            // Special case 2 we reformat the response of the distant service
            if (specialCase2)
                decodedString = restoreGoodNamespace(decodedString); 
            
            /*
            * Some implemention replace the standardized namespace "http://www.opengis.net/cat/csw" by "http://www.opengis.net/csw"
            * if we detect this we replace this namespace before unmarshalling the object.
            * 
            * TODO replace even when the prefix is not "csw" or blank
            */ 
            if (decodedString.contains("xmlns:csw=\"http://www.opengis.net/csw\"")) {
                decodedString = decodedString.replace("xmlns:csw=\"http://www.opengis.net/csw\"", "xmlns:csw=\"http://www.opengis.net/cat/csw\"");
            }

            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                harvested = unmarshaller.unmarshal(new StringReader(decodedString));
                if (harvested instanceof JAXBElement) {
                    harvested = ((JAXBElement) harvested).getValue();
                }
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            }  catch (IllegalAccessError ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        } catch (IOException ex) {
            LOGGER.severe("The Distant service have made an error");
            return null;
        }
        return harvested;
    }
    
    
    /**
     * Replace the special namespace by those of ISO/TC
     *
     * Deprecated : we don't have to support this speciel case.
     *
     * @param s An xml piece before unmarshaling.
     * @return
     */
    @Deprecated
    private String restoreGoodNamespace(String s) {
       s = s.replace("MD_Metadata ", "MD_Metadata xmlns:gco=\"http://www.isotc211.org/2005/gco\" ");
       s = s.replace("http://schemas.opengis.net/iso19115full", Namespaces.GMD);
       s = s.replace("http://metadata.dgiwg.org/smXML", Namespaces.GMD);
       s = StringUtilities.replacePrefix(s, "CharacterString", "gco");
       return s;
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
            if (prefix.equals("csw"))
                return Namespaces.CSW_202;
            
            else if (prefix.equals("ebrim") || prefix.equals("rim"))
                return "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";
            
            else if (prefix.equals("rim25"))
                return "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5";
            
            else if (prefix.equals("gmd"))
                return Namespaces.GMD;
            
            else if (prefix.equals("wrs"))
                return "http://www.opengis.net/cat/wrs/1.0";
            
             else if (prefix.equals("wrs09"))
                return "http://www.opengis.net/cat/wrs";
            
            else 
                throw new IllegalArgumentException("2.0.2 prefix unsupported: " + prefix + ".");
        } else {
            if (prefix.equals("csw"))
                return "http://www.opengis.net/cat/csw";
            
            else if (prefix.equals("ebrim") || prefix.equals("rim") || prefix.equals("rim25"))
                return "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5";
            
            else if (prefix.equals("gmd"))
                return Namespaces.GMD;
            
            else if (prefix.equals("wrs") || prefix.equals("wrs09"))
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
        final List<Object> additionalResults = new ArrayList<Object>();
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
                    for (JAXBElement<? extends AbstractRecordType> jbRecord : results.getAbstractRecord()) {
                        final AbstractRecordType record = jbRecord.getValue();
                        additionalResults.add(record);
                    }

                    //we looking for any other Record type
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


    /**
     * Harvest a single record and storing it into the database
     *
     * @param sourceURL The URL of the resource.
     * @param resourceType The record schema of the document to harvest.
     * 
     * @return An array containing: the number of inserted records, the number of updated records and the number of deleted records.
     */
    @Override
    public int[] harvestSingle(String sourceURL, String resourceType) throws MalformedURLException, IOException, CstlServiceException, JAXBException {
        final int[] result = new int[3];
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = marshallerPool.acquireUnmarshaller();

            if (resourceType.equals(Namespaces.GMD) ||
                resourceType.equals(Namespaces.CSW_202) ||
                resourceType.equals("http://www.isotc211.org/2005/gfc")) {

                final URL source          = new URL(sourceURL);
                final URLConnection conec = source.openConnection();
                final InputStream in      = conec.getInputStream();
                final Object harvested    = unmarshaller.unmarshal(in);

                if (harvested == null) {
                    throw new CstlServiceException("The resource can not be parsed.",
                            INVALID_PARAMETER_VALUE, "Source");
                }

                LOGGER.log(Level.INFO, "Object Type of the harvested Resource: {0}", harvested.getClass().getName());

                // ugly patch TODO handle update in mdweb
                try {
                    if (metadataWriter.storeMetadata(harvested)) {
                        result[0] = 1;
                    }
                } catch (IllegalArgumentException e) {
                    result[1] = 1;
                }  catch (MetadataIoException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
            } else {
                throw new CstlServiceException("unexpected resourceType: " + resourceType, NO_APPLICABLE_CODE);
            }
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
        return result;
    }

    
    @Override
    public void destroy() {
        if (metadataWriter != null)
            metadataWriter.destroy();
    }
}
