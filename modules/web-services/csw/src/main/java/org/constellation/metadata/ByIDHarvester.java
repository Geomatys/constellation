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
package org.constellation.metadata;

// J2SE dependencies
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.ows.xml.v100.ExceptionType;
import org.geotoolkit.xml.Namespaces;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * This catalogue harvester is a special tool used to harvest a CSW.
 * we must have a list of identifier stored in n file named id0, id1, ....idn
 *
 * @author Guilhem Legal
 */
public class ByIDHarvester extends CatalogueHarvester {

    private static final String GET_RECORD_BY_ID_REQUEST = "?service=CSW&request=getRecordbyid&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&outputformat=text/xml&ELEMENTSETNAME=full&id=";

    private String identifierDirectoryPath;

    /**
     * Build a new catalogue harvester with the write part.
     */
    public ByIDHarvester(MetadataWriter metadataWriter, String identifierDirectory) throws MetadataIoException {
        super(metadataWriter);
        if (identifierDirectory != null) {
            identifierDirectoryPath = identifierDirectory;
            final File f = new File(identifierDirectoryPath);
            if (f.exists() && f.isDirectory()) {
                LOGGER.log(Level.INFO, "Getting identifier file from :{0}", f.getPath());
            } else {
                throw new MetadataIoException("The identifierDirectory does not exist or is not a directory:" + f.getPath());
            }
        } else {
            throw new MetadataIoException("The identifierDirectory is null.");
        }
    }


    private List<String> parseIdentifierFile(int currentFile) {
        final List<String> result = new ArrayList<String>();
        FileInputStream in = null;
        try {
            File f = new File(identifierDirectoryPath + "id" + currentFile);
            if (!f.exists()) {
                LOGGER.log(Level.WARNING, "the file " + identifierDirectoryPath + "id{0} does not exist", currentFile);
                return result;
            }
            in = new FileInputStream(f);
            final InputStreamReader ipsr = new InputStreamReader(in);
            final BufferedReader br = new BufferedReader(ipsr);
            //we skip the character already read
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            in.close();
            ipsr.close();
            br.close();
            
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
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

        if (metadataWriter == null) {
            throw new CstlServiceException("The Service can not write into the database",
                                          OPERATION_NOT_SUPPORTED, "Harvest");
        }

        //we initialize the getRecords request
        int nbRecordInserted = 0;
        int nbRecordUpdated  = 0;
        boolean succeed      = false;

        //we prepare to store the distant serviceException and send it later if this is necessary
        final List<CstlServiceException> distantException = new ArrayList<CstlServiceException>();

        for (int i = 0; i < 100; i++) {

            final List<String> identifiers = parseIdentifierFile(i);

            //we make multiple request by pack of 20 record
            for (String identifier : identifiers) {

                LOGGER.log(Level.INFO, "current identifier:{0}", identifier);


                final String currentSourceURL = sourceURL + GET_RECORD_BY_ID_REQUEST + identifier;
                final Object harvested = sendRequest(currentSourceURL);



                // if the service respond correctly
                if (harvested instanceof GetRecordByIdResponseType) {
                    succeed = true;
                    LOGGER.log(Level.INFO, "Response of distant service received for: {0}", identifier);
                    final GetRecordByIdResponseType serviceResponse = (GetRecordByIdResponseType) harvested;

                    //we looking for CSW record
                    for (AbstractRecordType record: serviceResponse.getAbstractRecord()) {

                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (metadataWriter.storeMetadata(record)) {
                                nbRecordInserted++;
                            } else {
                                LOGGER.log(Level.INFO, "The record:{0} has not been recorded", identifier);
                            }
                        } catch (IllegalArgumentException ex) {
                            LOGGER.log(Level.WARNING, "Illegal argument while storing the record:" + identifier, ex);
                        }  catch (MetadataIoException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }
                    }

                    //we looking for any other Record type
                    for (Object otherRecord: serviceResponse.getAny()) {
                        if (otherRecord instanceof JAXBElement)
                            otherRecord = ((JAXBElement)otherRecord).getValue();

                        LOGGER.log(Level.FINER, "other Record Type: {0}", otherRecord.getClass().getSimpleName());

                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (metadataWriter.storeMetadata(otherRecord)) {
                                nbRecordInserted++;
                            } else {
                                LOGGER.log(Level.INFO, "The record:{0} has not been recorded", identifier);
                            }
                        } catch (IllegalArgumentException e) {
                            LOGGER.log(Level.WARNING, "Illegal argument while storing the record:" + identifier, e);
                        } catch (MetadataIoException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }
                    }


                /*
                 * We have receved an error
                 */
                } else if (harvested instanceof ExceptionReport) {
                    final ExceptionReport ex = (ExceptionReport) harvested;
                    final StringBuilder msg  = new StringBuilder();
                    if (ex.getException() != null && ex.getException().size() > 0) {
                        for (ExceptionType e:ex.getException()) {
                            for (String s: e.getExceptionText()) {
                                msg.append(s).append('\n');
                            }
                        }
                    }
                    final CstlServiceException exe = new CstlServiceException("The distant service has throw a webService exception: " + ex.getException().get(0),
                                                                      NO_APPLICABLE_CODE);
                    LOGGER.log(Level.WARNING, "The distant service has throw a webService exception: \n{0}", exe.toString());
                    distantException.add(exe);

                // if we obtain an object that we don't expect
                } else if (harvested == null) {
                    final CstlServiceException exe = new CstlServiceException("The distant service does not respond correctly.",
                                                     NO_APPLICABLE_CODE);
                    LOGGER.severe("The distant service does not respond correctly");
                    distantException.add(exe);

                } else {
                    throw new CstlServiceException("The distant service does not respond correctly: unexpected response type: " + harvested.getClass().getSimpleName(),
                                                 NO_APPLICABLE_CODE);
                }
            }

            if (!succeed && distantException.size() > 0) {
                throw distantException.get(0);
            }
            LOGGER.info("file id" + i + " done. total inserted:" + nbRecordInserted + " total updated:" + nbRecordUpdated);
        }

        final int[] result = new int [3];
        result[0]    = nbRecordInserted;
        result[1]    = nbRecordUpdated;
        result[2]    = 0;

        return result;
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
    private Object sendRequest(String sourceURL) throws MalformedURLException, CstlServiceException, IOException {

        final URL source          = new URL(sourceURL);
        final URLConnection conec = source.openConnection();
        Object harvested    = null;

        try {

            // we get the response document
            final InputStream in   = conec.getInputStream();

            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                harvested = unmarshaller.unmarshal(in);
                if (harvested instanceof JAXBElement) {
                    harvested = ((JAXBElement) harvested).getValue();
                }
                in.close();
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
            LOGGER.log(Level.WARNING, "The Distant service have made an error", ex);
            return null;
        }
        return harvested;
    }


    

    /**
     * Transfer The request to all the servers specified in distributedServers.
     *
     * @return
     */
    @Override
    public DistributedResults transferGetRecordsRequest(GetRecordsRequest request, List<String> distributedServers, int startPosition, int maxRecords) {
        throw new UnsupportedOperationException("IGN Harvester only support harvesting");
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
            unmarshaller              = marshallerPool.acquireUnmarshaller();
            final URL source          = new URL(sourceURL);
            final URLConnection conec = source.openConnection();

            // we get the source document
            final File fileToHarvest = File.createTempFile("harvested", "xml");
            fileToHarvest.deleteOnExit();
            final InputStream in = conec.getInputStream();

            if (resourceType.equals(Namespaces.GMD) || resourceType.equals(Namespaces.CSW_202) ||
                resourceType.equals("http://www.isotc211.org/2005/gfc")) {

                final Object harvested = unmarshaller.unmarshal(in);
                if (harvested == null) {
                    throw new CstlServiceException("The resource can not be parsed.", INVALID_PARAMETER_VALUE, "Source");
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
