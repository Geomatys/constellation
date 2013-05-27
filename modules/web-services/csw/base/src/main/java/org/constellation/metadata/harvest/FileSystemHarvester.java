/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.constellation.metadata.DistributedResults;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;
import javax.xml.bind.JAXBElement;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.metadata.iso.DefaultMetadata;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileSystemHarvester extends CatalogueHarvester {

    
    /**
     * Build a new catalogue harvester able to harvest a fileSystem.
     *
     * @param metadataWriter The Writer allowing to store the metadata in the datasource.
     *
     */
    public FileSystemHarvester(MetadataWriter metadataWriter) throws MetadataIoException {
        super(metadataWriter);
        
    }

    @Override
    public int[] harvestCatalogue(String sourceURL) throws MalformedURLException, IOException, CstlServiceException, SQLException {
        if (metadataWriter == null) {
            throw new CstlServiceException("The Service can not write into the database",
                                          OPERATION_NOT_SUPPORTED, "Harvest");
        }
        final File dataDirectory = new File(sourceURL);
        if (!dataDirectory.exists() || !dataDirectory.isDirectory()) {
            throw new CstlServiceException("The supplied source is not a valid directory",
                                          NO_APPLICABLE_CODE, "sourceURL");
        }
        //we initialize the getRecords request
        final int nbRecordInserted = harvestDirectory(dataDirectory);
        // TODO
        final int nbRecordUpdated  = 0;

        final int[] result = new int [3];
        result[0]    = nbRecordInserted;
        result[1]    = nbRecordUpdated;
        result[2]    = 0;

        return result;
    }
    

    /**
     * Harvest recursively a directy and its children.
     *
     * @param dataDirectory
     * @return
     * @throws CstlServiceException
     */
    private int harvestDirectory(File dataDirectory) throws CstlServiceException {
        int nbRecordInserted      = 0;
        try {
            final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
            for (File dataFile : dataDirectory.listFiles()) {
                LOGGER.log(Level.INFO, "current file:{0}", dataFile.getPath());

                if (!dataFile.isDirectory()) {
                    Object harvested = unmarshaller.unmarshal(dataFile);

                    if (harvested instanceof JAXBElement) {
                        harvested = ((JAXBElement) harvested).getValue();
                    }

                    // if the file is storable
                    if (harvested instanceof DefaultMetadata || harvested instanceof AbstractRecordType) {

                        //Temporary ugly patch TODO handle update in CSW
                        try {
                            if (metadataWriter.storeMetadata(harvested)) {
                                nbRecordInserted++;
                            } else {
                                LOGGER.log(Level.INFO, "The file:{0} has not been recorded", dataFile.getPath());
                            }
                        } catch (IllegalArgumentException ex) {
                            LOGGER.log(Level.WARNING, "Illegal argument while storing the file:" + dataFile.getPath(), ex);
                        } catch (MetadataIoException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }

                        // the file does not contain a storable metadata
                    } else {
                        String type = "null";
                        if (harvested != null) {
                            type = harvested.getClass().getSimpleName();
                        }
                        throw new CstlServiceException("The file does not contain an expected metadata type: " + type, NO_APPLICABLE_CODE);
                    }
                } else {
                    nbRecordInserted = nbRecordInserted + harvestDirectory(dataFile);
                }
            }
            marshallerPool.release(unmarshaller);
            return nbRecordInserted;
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex);
        }
    }

    @Override
    protected InputStream getSingleMetadata(String sourceURL) throws CstlServiceException{
        try {
            final File source = new File(sourceURL);
            return new FileInputStream(source);
        } catch (FileNotFoundException ex) {
            throw new CstlServiceException(ex);
        }
    }
    
    @Override
    public DistributedResults transferGetRecordsRequest(GetRecordsRequest request, List<String> distributedServers, int startPosition, int maxRecords) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
