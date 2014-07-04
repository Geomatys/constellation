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

import org.constellation.metadata.DistributedResults;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;

// JAXB dependencies
// Constellation dependencies
// Geotoolkit dependencies

/**
 * This catalogue harvester is a special tool used to harvest a CSW.
 * we must have a list of identifier stored in n file named id0, id1, ....idn
 *
 * Each file must have one identifier by line.
 *
 * @author Guilhem Legal
 */
public class ByIDHarvester extends CatalogueHarvester {

    /**
     * Default GET GetRecordById request to complete with an identifier.
     */
    private static final String GET_RECORD_BY_ID_REQUEST = "?service=CSW&request=getRecordbyid&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&outputformat=text/xml&ELEMENTSETNAME=full&id=";

    /**
     * The path of the directory containing the files fill with identifiers.
     */
    private String identifierDirectoryPath;

    /**
     * Build a new catalogue harvester using a list of identifiers stored in one or more Files.
     *
     * @param metadataWriter The Writer allowing to store the metadata in the datasource.
     * @param identifierDirectory a Path of the directory containing the identifier files.
     * 
     * @throws MetadataIoException If the parameters identifierDirectory does not point to a valid and existing directory,
     *                             or if its {@code null}.
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


    /**
     * Parse the identifier file named "id + currentPath" and return a List of string.
     * The list is constitued of each line of the file.
     *
     * @param currentFile an integer pointing to the current file to read.
     * @return A list of identifier correspoundong of each line of the identifier file.
     */
    private List<String> parseIdentifierFile(int currentFile) {
        final List<String> result = new ArrayList<>();
        try {
            final File f = new File(identifierDirectoryPath + "id" + currentFile);
            if (!f.exists()) {
                LOGGER.log(Level.WARNING, "the file " + identifierDirectoryPath + "id{0} does not exist", currentFile);
                return result;
            }
            final FileInputStream in = new FileInputStream(f);
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
        int nbRecordInserted       = 0;
        final int nbRecordUpdated  = 0;
        boolean succeed            = false;

        //we prepare to store the distant serviceException and send it later if this is necessary
        final List<CstlServiceException> distantException = new ArrayList<>();

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

                    //we looking for any record type
                    for (Object otherRecordObj: serviceResponse.getAny()) {
                        if (!(otherRecordObj instanceof Node)){
                            throw new CstlServiceException("object has been unmarshalled.");
                        } else {
                            LOGGER.log(Level.FINER, "record Type: {0}", otherRecordObj.getClass().getSimpleName());
                        }

                        final Node otherRecord = (Node)otherRecordObj;
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

            try {
                final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                harvested = unmarshaller.unmarshal(in);
                marshallerPool.recycle(unmarshaller);
                if (harvested instanceof JAXBElement) {
                    harvested = ((JAXBElement) harvested).getValue();
                }
                in.close();
            } catch (JAXBException | IllegalAccessError ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
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
