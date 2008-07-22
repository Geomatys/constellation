/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
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

package net.seagis.metadata;

// J2SE dependencies
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
import java.util.List;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

// SeaGIS dependencies
import javax.xml.namespace.QName;
import net.seagis.cat.csw.GetRecordsRequest;
import net.seagis.cat.csw.v200.GetCapabilitiesType;
import net.seagis.cat.csw.v202.AbstractQueryType;
import net.seagis.cat.csw.v202.AbstractRecordType;
import net.seagis.cat.csw.v202.Capabilities;
import net.seagis.cat.csw.v202.ElementSetNameType;
import net.seagis.cat.csw.v202.ElementSetType;
import net.seagis.cat.csw.v202.GetCapabilities;
import net.seagis.cat.csw.v202.GetRecordsResponseType;
import net.seagis.cat.csw.v202.GetRecordsType;
import net.seagis.cat.csw.v202.QueryConstraintType;
import net.seagis.cat.csw.v202.QueryType;
import net.seagis.cat.csw.v202.ResultType;
import net.seagis.cat.csw.v202.SearchResultsType;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.ogc.FilterType;
import net.seagis.ogc.PropertyIsLikeType;
import net.seagis.ogc.PropertyNameType;
import net.seagis.ows.v100.AcceptFormatsType;
import net.seagis.ows.v100.AcceptVersionsType;
import net.seagis.ows.v100.CapabilitiesBaseType;
import net.seagis.ows.v100.DCP;
import net.seagis.ows.v100.DomainType;
import net.seagis.ows.v100.ExceptionReport;
import net.seagis.ows.v100.ExceptionType;
import net.seagis.ows.v100.OWSWebServiceException;
import net.seagis.ows.v100.Operation;
import net.seagis.ows.v100.OperationsMetadata;
import net.seagis.ows.v100.RequestMethodType;
import net.seagis.ows.v100.SectionsType;
import net.seagis.ws.rs.NamespacePrefixMapperImpl;
import static net.seagis.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class CatalogueHarvester {

    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("net.seagis.metadata");
    
    /**
     * A getRecords request used to request another csw (2.0.2).
     */
    private GetRecordsType fullGetRecordsRequestv202;
    
    /**
     * A getRecords request used to request another csw(2.0.0).
     */
    private net.seagis.cat.csw.v200.GetRecordsType fullGetRecordsRequestv200;
    
    /**
     * A special getRecords request used to request another unstandardized csw (2.0.0).
     */
    private net.seagis.cat.csw.v200.GetRecordsType fullGetRecordsRequestv200_Special1;
    
    /**
     * A getCapabilities request used request another csw(2.0.2)
     */
    private GetCapabilities getCapabilitiesRequestv202;
    
    /**
     * A getCapabilities request used request another csw(2.0.0)
     */
    private GetCapabilitiesType getCapabilitiesRequestv200;
    
    /**
     * A global variable used during the harvest of a distant CSW.
     * it record the supported outputSchema in the GetRecords request of the distant web service.
     */
    private List<String> currentDistantOuputSchema;
    
    /**
     * a QName for csw:Record type
     */
    private final static QName _Record_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Record");
    
    /**
     * The CSW worker which owe this ctalogue harvester.
     */
    private final CSWworker worker;
    
    /**
     * A prefix mapper
     */
    private NamespacePrefixMapperImpl prefixMapper;
    
    /**
     * A flag indicating that we are harvesting a CSW special case 1
     */
    private boolean specialCase1 = false;
    
    /**
     * Build a new catalogue harvester.
     * 
     * @param worker
     */
    public CatalogueHarvester(CSWworker worker) {
        
        this.worker = worker;
        prefixMapper = new NamespacePrefixMapperImpl("");
        initializeRequest();
    }
    
    /**
     * Initialize The object request to harvest distant CSW
     */
    public void initializeRequest() {
        
        
        
        /*
         * we build the first filter : < dublinCore:Title IS LIKE '*' >
         */ 
        List<QName> typeNames          = new ArrayList<QName>();
        PropertyNameType pname         = new PropertyNameType("dc:Title");
        PropertyIsLikeType pil         = new PropertyIsLikeType(pname, "*", "*", "?", "\\");
        FilterType filter1              = new FilterType(pil);
        
        /*
         * Second filter a special case for some unstandardized CSW : < title IS NOT LIKE 'something' >
         */
        typeNames          = new ArrayList<QName>();
        pname              = new PropertyNameType("title");
        pil                = new PropertyIsLikeType(pname, "something", null, null, null);
        FilterType filter2 = new FilterType(pil);
        
        
        //we build the base request to harvest another CSW service (2.0.2)
        QueryConstraintType constraint = new QueryConstraintType(filter1, "1.1.0");
        typeNames.add(_Record_QNAME);
        QueryType query = new QueryType(typeNames, new ElementSetNameType(ElementSetType.FULL), null, constraint); 
        JAXBElement<? extends AbstractQueryType> jbQuery =  worker.cswFactory202.createQuery(query);
        fullGetRecordsRequestv202 = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 20, jbQuery, null);
                 
        
        //we build the base request to harvest another CSW service (2.0.0)
        net.seagis.cat.csw.v200.QueryConstraintType constraint2 = new net.seagis.cat.csw.v200.QueryConstraintType(filter1, "1.1.0");
        List<String> typeNames2 = new ArrayList<String>();
        typeNames2.add("csw:dataset");
        net.seagis.cat.csw.v200.QueryType query2 = new net.seagis.cat.csw.v200.QueryType(typeNames2, 
                                                                                         new net.seagis.cat.csw.v200.ElementSetNameType(net.seagis.cat.csw.v200.ElementSetType.FULL), 
                                                                                         constraint2); 
        JAXBElement<? extends net.seagis.cat.csw.v200.AbstractQueryType> jbQuery2 =  worker.cswFactory200.createQuery(query2);
        fullGetRecordsRequestv200 = new net.seagis.cat.csw.v200.GetRecordsType("CSW", "2.0.0", net.seagis.cat.csw.v200.ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 20, jbQuery2, null);
        
        
        //we build the special request to harvest unstandardized CSW service (2.0.0)
        constraint2        = new net.seagis.cat.csw.v200.QueryConstraintType(filter2, "1.0.20");
        typeNames2         = new ArrayList<String>();
        typeNames2.add("Dataset");
        query2             = new net.seagis.cat.csw.v200.QueryType(typeNames2, 
                                                                   new net.seagis.cat.csw.v200.ElementSetNameType(net.seagis.cat.csw.v200.ElementSetType.FULL), 
                                                                   constraint2); 
        jbQuery2 =  worker.cswFactory200.createQuery(query2);
        fullGetRecordsRequestv200_Special1 = new net.seagis.cat.csw.v200.GetRecordsType("CSW", "2.0.0", net.seagis.cat.csw.v200.ResultType.RESULTS, null, "application/xml", null, 1, 20, jbQuery2, null);
        
        
        //we build the base request to get the capabilities of anoter CSW service (2.0.2)
        AcceptVersionsType versions = new AcceptVersionsType("2.0.2", "2.0.0");
        SectionsType sections       = new SectionsType("All");
        AcceptFormatsType formats   = new AcceptFormatsType("text/xml", "application/xml");
        getCapabilitiesRequestv202  = new GetCapabilities(versions, sections, formats, null, "CSW");
        
        //we build the base request to get the capabilities of anoter CSW service (2.0.0)
        getCapabilitiesRequestv200  = new GetCapabilitiesType(versions, sections, formats, null, "CSW");
    }
    
    
    /**
     * Harvest another CSW service by getting all this records ans storing it into the database
     * 
     * @param sourceURL The URL of the distant CSW service
     * 
     * @return the number of inserted Record.
     */
    protected int[] harvestCatalogue(String sourceURL) throws MalformedURLException, IOException, WebServiceException, SQLException {
        
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
            
        } else if (distantCapabilities instanceof net.seagis.cat.csw.v200.CapabilitiesType) {
            getRecordRequest = fullGetRecordsRequestv200;
            
        } else {
            throw new OWSWebServiceException("This service if it is one is not requestable by constellation",
                                              OPERATION_NOT_SUPPORTED, "ResponseHandler", worker.getVersion());
        }
        
        getRecordRequest = analyseCapabilitiesDocument((CapabilitiesBaseType)distantCapabilities, getRecordRequest);
        
        //we initialize the getRecords request
        getRecordRequest.setStartPosition(1);
        int nbRecordInserted = 0;
        int nbRecordUpdated  = 0;
        boolean succeed      = false;
        
        //we prepare to store the distant serviceException and send it later if this is necessary
        List<WebServiceException> distantException = new ArrayList<WebServiceException>();
        
        //we request all the records for each outputSchema supported
        for (String outputSchema: currentDistantOuputSchema) {
            logger.info("harvesting with outputSchema: " + outputSchema);
            
            if (!specialCase1)
                getRecordRequest.setOutputSchema(outputSchema);
            
            boolean moreResults = true;
            //we make multiple request by pack of 20 record 
            while (moreResults) {
        
                Object harvested = sendRequest(sourceURL, getRecordRequest);
        
                // if the service respond with non xml or unstandardized response
                if (harvested == null) {
                    throw new OWSWebServiceException("The distant service does not respond correctly.",
                                                     NO_APPLICABLE_CODE, null, worker.getVersion());
            
                // if the service respond correctly    
                } else if (harvested instanceof GetRecordsResponseType) {
                    succeed = true;
                    logger.info("Response of distant service:" + '\n' + harvested.toString());
                    GetRecordsResponseType serviceResponse = (GetRecordsResponseType) harvested;
                    SearchResultsType results = serviceResponse.getSearchResults();
            
                    for (JAXBElement<? extends AbstractRecordType> JBrecord: results.getAbstractRecord()) {
                        AbstractRecordType record = JBrecord.getValue();
                        
                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (worker.storeMetadata(record))
                                nbRecordInserted++;
                        } catch (IllegalArgumentException e) {
                            nbRecordUpdated++;
                        }
                    }
                
                    //if there is more results we need to make another request
                    moreResults = results.getNumberOfRecordsReturned() != 0;
                    if (moreResults) {
                        fullGetRecordsRequestv202.setStartPosition(results.getNextRecord());
                    }
                    
                // if the distant service has launch a standardized exception    
                } else if (harvested instanceof ExceptionReport) {
                    ExceptionReport ex = (ExceptionReport) harvested;
                    String msg = "";
                    if (ex.getException() != null && ex.getException().size() > 0) {
                        for (ExceptionType e:ex.getException()) {
                            for (String s: e.getExceptionText())
                                msg = msg + s + '\n';
                        }
                    }
                    logger.severe("The distant service has throw a webService exception: ");
                    distantException.add(new OWSWebServiceException("The distant service has throw a webService exception: " + ex.getException().get(0),
                                                 NO_APPLICABLE_CODE, null, worker.getVersion()));
                    moreResults = false;
                
                // if we obtain an object that we don't expect    
                } else {
                    throw new OWSWebServiceException("The distant service does not respond correctly: unexpected response type: " + harvested.getClass().getSimpleName(),
                                                 NO_APPLICABLE_CODE, null, worker.getVersion());
                }
            }
        }
        
        
        if (!succeed && distantException.size() > 0) {
            // TODO see how return multiple exception in one
            throw distantException.get(0);
        }
        
        int result[] = new int[3];
        result[0]    = nbRecordInserted;
        result[1]    = nbRecordUpdated;
        result[2]    = 0;
        
        return result;
    }
    
    /**
     *  Analyse a capabilities Document and update the specified GetRecords request at the same time.
     */
    public GetRecordsRequest analyseCapabilitiesDocument(CapabilitiesBaseType capa, GetRecordsRequest request) {
        String distantVersion = "2.0.2";
        StringBuilder report = new StringBuilder();

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
            request      = fullGetRecordsRequestv200_Special1;
            special      = "Special case 1";
        }
        
        report.append("CSW ").append(distantVersion).append(" service identified: " + serviceName).append(" ").append(special).append('\n');
        
        //we get the Operations metadata if they are present
        OperationsMetadata om = capa.getOperationsMetadata();

        //we look for the GetRecords operation.
        Operation getRecordOp = om.getOperation("GetRecords");
        if (getRecordOp != null) {
            report .append("GetRecords operation supported:").append('\n');
            
            // if there is only one DCP (most case)
            if (getRecordOp.getDCP().size() == 1) {
                DCP dcp = getRecordOp.getDCP().get(0);
                List<JAXBElement<RequestMethodType>> protocols = dcp.getHTTP().getRealGetOrPost();
                report.append("available protocols:").append('\n');
                for (JAXBElement<RequestMethodType> jb : protocols) {
                    report .append(jb.getName().getLocalPart()).append('\n');
                }
                
            // if there is multiple DCP
            } else if (getRecordOp.getDCP().size() > 1) {
                report.append("multiple DCP").append('\n');
                int i = 0;
                for (DCP dcp: getRecordOp.getDCP()) {
                    report.append("DCP ").append(i).append(':').append('\n');
                    List<JAXBElement<RequestMethodType>> protocols = dcp.getHTTP().getRealGetOrPost();
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
            DomainType outputDomain = getRecordOp.getParameter("outputSchema");
            if (outputDomain == null) {
                outputDomain = getRecordOp.getParameter("OutputSchema");
                if (outputDomain == null) {
                    outputDomain = getRecordOp.getParameter("OUTPUTSCHEMA");
                }
            }
            
            if (outputDomain != null) {
                currentDistantOuputSchema = outputDomain.getValue();
                String defaultValue = outputDomain.getDefaultValue(); 
                if (defaultValue != null && !defaultValue.equals(""))
                    currentDistantOuputSchema.add(defaultValue);
                
                
                //ugly patch to be compatible with some CSW service who specify the wrong ouputSchema
                currentDistantOuputSchema.add("csw:Record");
                
                report.append("OutputSchema supported:").append('\n');
                for (String osc: currentDistantOuputSchema) {
                    report.append('\t').append("- ").append(osc).append('\n');
                }
             
            } else {
                report.append("No outputSchema specified using default:"    + '\n' +
                              '\t' + "http://www.opengis.net/cat/csw/2.0.2" + '\n' + 
                              '\t' + "csw:Record"                           + '\n');
                
                //we add the default outputSchema used
                currentDistantOuputSchema = new ArrayList<String>();
                currentDistantOuputSchema.add("http://www.opengis.net/cat/csw/2.0.2");
                currentDistantOuputSchema.add("csw:Record");
            }
            
            // we look for the different Type names
            DomainType typeNameDomain = getRecordOp.getParameter("typeName");
            if (typeNameDomain == null) {
                typeNameDomain = getRecordOp.getParameter("TypeName");
                if (typeNameDomain == null) {
                    typeNameDomain = getRecordOp.getParameter("TYPENAME");
                    if (typeNameDomain == null) {
                        typeNameDomain = getRecordOp.getParameter("typeNames");
                        if (typeNameDomain == null) {
                            typeNameDomain = getRecordOp.getParameter("TypeNames");
                            if (typeNameDomain == null) {
                                typeNameDomain = getRecordOp.getParameter("TYPENAMES");
                            }
                        }
                    }
                }
            }
            
            List<QName>  typeNamesQname = new ArrayList<QName>();
            if (typeNameDomain != null) {
                List<String> typeNames      = typeNameDomain.getValue();
                
                String defaultValue = typeNameDomain.getDefaultValue(); 
                if (defaultValue != null && !defaultValue.equals(""))
                    typeNames.add(defaultValue);
                
                report.append("TypeNames supported:").append('\n');
                for (String osc: typeNames) {
                    report.append('\t').append("- ").append(osc).append('\n');
                    String prefix, localPart;
                    
                    if (osc.indexOf(':') != -1) {
                        prefix    = osc.substring(0, osc.indexOf(':'));
                        localPart = osc.substring(osc.indexOf(':') + 1, osc.length());
                        String namespaceURI = getNamespaceURIFromprefix(prefix, distantVersion);
                        typeNamesQname.add(new QName(namespaceURI, localPart, prefix));
                    } else {
                        logger.severe("NO ':' in Typenames => unexpected!!!");
                    }
                }
            } else {
                report.append("No outputSchema specified using default:"    + '\n' +
                              '\t' + "csw:Record"                           + '\n');
                
                //we add the default typeNames used
                typeNamesQname.add(_Record_QNAME);
            }
            //we update the request
            request.setTypeNames(typeNamesQname);
            
        } else {
            report.append("No GetRecords operation find").append('\n');
        }

        logger.info(report.toString());
        return request;
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
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private Object sendRequest(String sourceURL, Object request) throws MalformedURLException, IOException, WebServiceException {
        
        URL source         = new URL(sourceURL);
        URLConnection conec = source.openConnection();
        

        // for a POST request
        if (request != null) {
        
            conec.setDoOutput(true);
            conec.setRequestProperty("Content-Type","text/xml");
            OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
            StringWriter sw = new StringWriter();
            try {
                worker.marshaller.marshal(request, sw);
            } catch (JAXBException ex) {
                throw new OWSWebServiceException("Unable to marshall the request: " + ex.getMessage(),
                                                 NO_APPLICABLE_CODE, null, worker.getVersion());
            }
            String XMLRequest = sw.toString();
            
            // in the special case 1 we need to remove ogc prefix inside  the >Filter
            if (specialCase1) {
                XMLRequest = XMLRequest.replace("<ogc:", "<");
                XMLRequest = XMLRequest.replace("<Filter", "<ogc:Filter");
                XMLRequest = XMLRequest.replace("</Filter", "</ogc:Filter");
                XMLRequest = XMLRequest.replace("xmlns:gco=\"http://www.isotc211.org/2005/gco\"", "");
                XMLRequest = XMLRequest.replace("xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"", "");
                XMLRequest = XMLRequest.replace("xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" , ""); 
                XMLRequest = XMLRequest.replace("xmlns:dc2=\"http://www.purl.org/dc/elements/1.1/\"" , "");
                        logger.info("special obtained request: " + '\n' + XMLRequest);
            }
            
            wr.write(XMLRequest);
            wr.flush();
        }
        
        // we get the response document
        InputStream in = conec.getInputStream();
        StringWriter out = new StringWriter();
        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
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
        
        Object harvested = null;
        try {
            harvested = worker.unmarshaller.unmarshal(new StringReader(decodedString));
            if (harvested != null && harvested instanceof JAXBElement) {
                harvested = ((JAXBElement) harvested).getValue();
            }
        } catch (JAXBException ex) {
            logger.severe("The distant service does not respond correctly: unable to unmarshall response document." + '\n' +
                    "cause: " + ex.getMessage());
        }
        return harvested;
    }
    
    /**
     * 
     * @param prefix
     * @param distantVersion
     * @return
     */
    private String getNamespaceURIFromprefix(String prefix, String distantVersion) {
        if (distantVersion.equals("2.0.2")) {
            if (prefix.equals("csw"))
                return "http://www.opengis.net/cat/csw/2.0.2";
            
            else if (prefix.equals("ebrim"))
                return "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";
            
            else 
                throw new IllegalArgumentException("prefix unsupported: " + prefix);
        } else {
            if (prefix.equals("csw"))
                return "http://www.opengis.net/cat/csw";
            
            else if (prefix.equals("ebrim"))
                return "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5";
            
            else 
                throw new IllegalArgumentException("prefix unsupported: " + prefix);
        }
    }
}
