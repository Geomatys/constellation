/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.metadata.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.StringList;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.lucene.index.IndexDirectoryFilter;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CSWConfigurationManager {

    private static final Logger LOGGER = Logging.getLogger(CSWConfigurationManager.class);
    
    /**
     * A flag indicating if an indexation is going on.
     */
    private boolean indexing;

    /**
     * The list of service currently indexing.
     */
    private final List<String> SERVICE_INDEXING = new ArrayList<>();

    protected Map<String, Automatic> serviceConfiguration = new HashMap<>();

    private static CSWConfigurationManager INSTANCE;

    public static CSWConfigurationManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CSWConfigurationManager();
        }
        return INSTANCE;
    }

    public CSWConfigurationManager() {
        indexing = false;
        try {
            refreshServiceConfiguration();
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Error while starting CSW configuration Manager", ex);
        }
    }

    public AcknowlegementType refreshIndex(final String id, final boolean asynchrone, final boolean forced) throws ConfigurationException {

        if (isIndexing(id) && !forced) {
            final AcknowlegementType refused = new AcknowlegementType("Failure",
                    "An indexation is already started for this service:" + id);
            return refused;
        } else if (indexing && forced) {
            AbstractIndexer.stopIndexation(Arrays.asList(id));
        }

        startIndexation(id);
        AcknowlegementType ack;
        try {
            ack = refreshIndex(asynchrone, id);
        } finally {
            endIndexation(id);
        }
        return ack;
    }

    /**
     * Add the specified service to the indexing service list.
     * @param id
     */
    private void startIndexation(final String id) {
        indexing  = true;
        if (id != null) {
            SERVICE_INDEXING.add(id);
        }
    }

    /**
     * remove the selected service from the indexing service list.
     * @param id
     */
    private void endIndexation(final String id) {
        indexing = false;
        if (id != null) {
            SERVICE_INDEXING.remove(id);
        }
    }

    /**
     * Return true if the select service (identified by his ID) is currently indexing (CSW).
     * @param id
     * @return
     */
    private boolean isIndexing(final String id) {
        return indexing && SERVICE_INDEXING.contains(id);
    }

    public boolean isIndexing() {
        return indexing;
    }

    /**
     * Destroy the CSW index directory in order that it will be recreated.
     *
     * @param asynchrone a flag for indexation mode.
     * @param id The service identifier.
     *
     * @return
     * @throws CstlServiceException
     */
    private AcknowlegementType refreshIndex(final boolean asynchrone, final String id) throws ConfigurationException {
        String suffix = "";
        if (asynchrone) {
            suffix = " (asynchrone)";
        }
        if (id != null && !id.isEmpty()) {
            suffix = suffix + " id:" + id;
        }
        LOGGER.log(Level.INFO, "refresh index requested{0}", suffix);
        refreshServiceConfiguration();
        
        final List<File> cswInstanceDirectories = new ArrayList<>();
        if ("all".equals(id)) {
            cswInstanceDirectories.addAll(getAllCswInstanceDirectory());
        } else {
            final File instanceDir = getCswInstanceDirectory(id);
            if (instanceDir != null) {
                cswInstanceDirectories.add(instanceDir);
            }
        }
        
        if (!asynchrone) {
            synchroneIndexRefresh(cswInstanceDirectories);
        } else {
            asynchroneIndexRefresh(cswInstanceDirectories);
        }

        final String msg = "CSW index succefully recreated";
        return new AcknowlegementType("Success", msg);
    }

    /**
     * Add some CSW record to the index.
     *
     * @param asynchrone
     * @return
     * @throws CstlServiceException
     */
    public AcknowlegementType addToIndex(final String id, final String identifierList) throws ConfigurationException {
        LOGGER.info("Add to index requested");
        final List<String> identifiers = StringUtilities.toStringList(identifierList);
        AbstractIndexer indexer  = null;
        try {
            final CSWMetadataReader reader  = initReader(id);
            final List<Object> objectToIndex = new ArrayList<>();
            if (reader != null) {
                try {
                    for (String identifier : identifiers) {
                        objectToIndex.add(reader.getMetadata(identifier, AbstractMetadataReader.NATIVE));
                    }
                } catch (MetadataIoException ex) {
                    throw new ConfigurationException(ex);
                }
            } else {
                throw new ConfigurationException("Unable to create a reader for the id:" + id);
            }

            indexer = initIndexer(id, reader);
            if (indexer != null) {
                for (Object obj : objectToIndex) {
                    indexer.indexDocument(obj);
                }
            } else {
                throw new ConfigurationException("Unable to create an indexer for the id:" + id);
            }

        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }

        final String msg = "The specified record have been added to the CSW index";
        return new AcknowlegementType("Success", msg);
    }

    /**
     * Remove some CSW record to the index.
     *
     * @param asynchrone
     * @return
     * @throws CstlServiceException
     */
    public AcknowlegementType removeFromIndex(final String id, final String identifierList) throws ConfigurationException {
        LOGGER.info("Remove from index requested");
        final List<String> identifiers = StringUtilities.toStringList(identifierList);
        AbstractIndexer indexer  = null;
        try {
            final CSWMetadataReader reader  = initReader(id);
            indexer = initIndexer(id, reader);
            if (indexer != null) {
                for (String metadataID : identifiers) {
                    indexer.removeDocument(metadataID);
                }
            } else {
                throw new ConfigurationException("Unable to create an indexer for the id:" + id);
            }

        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }

        final String msg = "The specified record have been remove from the CSW index";
        return new AcknowlegementType("Success", msg);
    }

    /**
     * Stop all the indexation going on.
     *
     * @return an Acknowledgment.
     */
    public AcknowlegementType stopIndexation(final String id) {
        LOGGER.info("\n stop indexation requested \n");
        if (isIndexing(id)) {
            return new AcknowlegementType("Success", "There is no indexation to stop");
        } else {
            AbstractIndexer.stopIndexation(Arrays.asList(id));
            return new AcknowlegementType("Success", "The indexation have been stopped");
        }
    }

    public AcknowlegementType importRecords(final String id, final File f, final String fileName) throws ConfigurationException {
        LOGGER.info("Importing record");
        final CSWMetadataWriter writer = initWriter(id);
        final List<File> files;
        if (fileName.endsWith("zip")) {
            try  {
                final FileInputStream fis = new FileInputStream(f);
                files = FileUtilities.unZipFileList(fis);
                fis.close();
            } catch (IOException ex) {
                throw new ConfigurationException(ex);
            }
        } else if (fileName.endsWith("xml")) {
            files = Arrays.asList(f);
        } else {
            throw new ConfigurationException("Unexpected file extension, accepting zip or xml");
        }
        try {
            final Unmarshaller u = EBRIMMarshallerPool.getInstance().acquireUnmarshaller();
            for (File importedFile: files) {
                if (importedFile != null) {
                    Object unmarshalled = u.unmarshal(importedFile);
                    EBRIMMarshallerPool.getInstance().recycle(u);
                    if (unmarshalled instanceof JAXBElement) {
                        unmarshalled = ((JAXBElement)unmarshalled).getValue();
                    }
                    writer.storeMetadata(unmarshalled);
                } else {
                    throw new ConfigurationException("An imported file is null");
                }
            }
            final String msg = "The specified record have been imported in the CSW";
            return new AcknowlegementType("Success", msg);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Exception while unmarshalling imported file", ex);
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        }
        return new AcknowlegementType("Error", "An error occurs during the process");
    }

    public AcknowlegementType metadataExist(final String id, final String metadataName) throws ConfigurationException {
        final CSWMetadataReader reader = initReader(id);
        try {
            final boolean exist = reader.existMetadata(metadataName);
            if (exist) {
                final String msg = "The specified record exist in the CSW";
                return new AcknowlegementType("Exist", msg);
            } else {
                final String msg = "The specified record does not exist in the CSW";
                return new AcknowlegementType("Not Exist", msg);
            }
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        }
    }

    public AcknowlegementType deleteMetadata(final String id, final String metadataName) throws ConfigurationException {
        final CSWMetadataWriter writer = initWriter(id);
        try {
            final boolean deleted = writer.deleteMetadata(metadataName);
            if (deleted) {
                final String msg = "The specified record has been deleted from the CSW";
                return new AcknowlegementType("Success", msg);
            } else {
                final String msg = "The specified record has not been deleted from the CSW";
                return new AcknowlegementType("Failure", msg);
            }
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        }
    }

    public StringList getAvailableCSWDataSourceType() {
        final List<DataSourceType> sources = new ArrayList<>();
        final Iterator<AbstractCSWFactory> ite = ServiceRegistry.lookupProviders(AbstractCSWFactory.class);
        while (ite.hasNext()) {
            AbstractCSWFactory currentFactory = ite.next();
            sources.addAll(currentFactory.availableType());
        }
        final StringList result = new StringList();
        for (DataSourceType source : sources) {
            result.getList().add(source.getName());
        }
        return result;
    }

    /**
     * Refresh the map of configuration object.
     *
     * @throws ConfigurationException
     */
    protected void refreshServiceConfiguration() throws ConfigurationException {
        serviceConfiguration    = new HashMap<>();
        final File cswConfigDir = getConfigurationDirectory();
        if (cswConfigDir != null && cswConfigDir.exists() && cswConfigDir.isDirectory()) {
            try {
                final Unmarshaller configUnmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();

                for (File instanceDirectory : cswConfigDir.listFiles()) {
                    if (instanceDirectory.isDirectory()) {
                        //we get the csw ID
                        final String id = instanceDirectory.getName();
                        final File configFile = new File(instanceDirectory, "config.xml");
                        if (configFile.exists()) {
                            // we get the CSW configuration file
                            final Automatic config = (Automatic) configUnmarshaller.unmarshal(configFile);
                            config.setConfigurationDirectory(instanceDirectory);
                            serviceConfiguration.put(id, config);
                        }
                    }
                }
                GenericDatabaseMarshallerPool.getInstance().recycle(configUnmarshaller);

            } catch (JAXBException ex) {
                throw new ConfigurationException("JAXBexception while setting the JAXB context for configuration service", ex.getMessage());
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
            }
        } else {
            LOGGER.warning("No CSW configuration directory.");
        }
    }

    /**
     * Delete The index folder and call the restart() method.
     *
     * TODO maybe we can directly recreate the index here (fusion of synchrone/asynchrone)
     *
     * @param configurationDirectory The CSW configuration directory.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    private void synchroneIndexRefresh(final List<File> cswInstanceDirectories) throws ConfigurationException {
        boolean deleted = false;
        for (File cswInstanceDirectory : cswInstanceDirectories) {
            //we delete each index directory
            for (File indexDir : cswInstanceDirectory.listFiles(new IndexDirectoryFilter(null))) {
                deleted = true;
                for (File f : indexDir.listFiles()) {
                    final boolean sucess = f.delete();
                    if (!sucess) {
                        throw new ConfigurationException("The service can't delete the index file:" + f.getPath());
                    }
                }
                if (!indexDir.delete()) {
                    throw new ConfigurationException("The service can't delete the index folder.");
                }
            }
        }

        //if we have deleted something we restart the services
        if (deleted) {
            //restart(); TODO
        } else {
            LOGGER.log(Level.INFO, "there is no index to delete");
        }
    }

    /**
     * Build a new Index in a new folder.
     * This index will be used at the next restart of the server.
     *
     * @param id The service identifier.
     * @param configurationDirectory  The CSW configuration directory.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    private void asynchroneIndexRefresh(final List<File> cswInstanceDirectories) throws ConfigurationException {
        for (File cswInstanceDirectory : cswInstanceDirectories) {
            String id = cswInstanceDirectory.getName();
            final File nexIndexDir        = new File(cswInstanceDirectory, "index-" + System.currentTimeMillis());
            AbstractIndexer indexer = null;
            try {
                indexer = initIndexer(id, null);
                if (indexer != null) {
                    final boolean success = nexIndexDir.mkdir();
                    if (!success) {
                        throw new ConfigurationException("Unable to create a directory nextIndex for  the id:" + id);
                    }
                    indexer.setFileDirectory(nexIndexDir);
                    indexer.createIndex();

                } else {
                    throw new ConfigurationException("Unable to create an indexer for the id:" + id);
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, "unable to create an indexer for id:{0}", id);
            } catch (IndexingException ex) {
                throw new ConfigurationException("An exception occurs while creating the index!\ncause:" + ex.getMessage());
            } finally {
                if (indexer != null) {
                    indexer.destroy();
                }
            }
        }
    }

    /**
     * Build a new Indexer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     * @param cswConfigDir the CSW configuration directory.
     *
     * @return A lucene Indexer
     * @throws org.constellation.ws.CstlServiceException
     */
    protected AbstractIndexer initIndexer(final String serviceID, CSWMetadataReader currentReader) throws ConfigurationException {

        // we get the CSW configuration file
        final Automatic config = serviceConfiguration.get(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                if (currentReader == null) {
                    currentReader = cswfactory.getMetadataReader(config);
                }
                return cswfactory.getIndexer(config, currentReader, "", currentReader.getAdditionalQueryablePathMap());

            } catch (Exception ex) {
                throw new ConfigurationException("An exception occurs while initializing the indexer!\ncause:" + ex.getMessage());
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }

    /**
     * Build a new Metadata reader for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     *
     * @return A metadata reader.
     * @throws org.constellation.ws.CstlServiceException
     */
    protected CSWMetadataReader initReader(final String serviceID) throws ConfigurationException {

        // we get the CSW configuration file
        final Automatic config = serviceConfiguration.get(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                return cswfactory.getMetadataReader(config);

            } catch (MetadataIoException ex) {
                throw new ConfigurationException("JAXBException while initializing the reader!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }

    /**
     * Build a new Metadata writer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     *
     * @return A metadata reader.
     * @throws org.constellation.ws.CstlServiceException
     */
    protected CSWMetadataWriter initWriter(final String serviceID) throws ConfigurationException {

        // we get the CSW configuration file
        final Automatic config = serviceConfiguration.get(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                return cswfactory.getMetadataWriter(config, null);

            } catch (MetadataIoException ex) {
                throw new ConfigurationException("JAXBException while initializing the writer!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }

    /**
     * Select the good CSW factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private AbstractCSWFactory getCSWFactory(DataSourceType type) {
        final Iterator<AbstractCSWFactory> ite = ServiceRegistry.lookupProviders(AbstractCSWFactory.class);
        while (ite.hasNext()) {
            AbstractCSWFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No CSW factory has been found for type:" + type);
    }

    /**
     * Return all the founded CSW service identifiers.
     *
     * @return all the founded CSW service identifiers.
     */
    public List<String> getAllServiceIDs() {
        final List<String> result = new ArrayList<>();
        for (String id : serviceConfiguration.keySet()) {
            result.add(id);
        }
        return result;
    }

    /**
     * Return the CSW configuration directory.
     * example for regular constellation configuration it return the file USER_DIRECTORY/.constellation/CSW
     *
     * @return Return the CSW configuration directory.
     * @throws ConfigurationException
     */
    protected File getConfigurationDirectory() throws ConfigurationException {
        final File configDir = ConfigDirectory.getConfigDirectory();
        final File cswConfigDir;
        if (configDir == null || !configDir.isDirectory()) {
            throw new ConfigurationException("No configuration directory have been found");
        } else {
            cswConfigDir = new File(configDir, "CSW");
            if (!cswConfigDir.isDirectory()) {
                throw new ConfigurationException("No CSW configuration directory have been found");
            }
            return cswConfigDir;
        }
    }

    /*
     * Return the configuration directory for the specified instance identifier.
     */
    protected File getCswInstanceDirectory(String instanceId) throws ConfigurationException {
        final File configDir = getConfigurationDirectory();
        if (configDir != null && configDir.exists()) {
            File instanceDir = new File(configDir, instanceId);
            if (instanceDir.exists() && instanceDir.isDirectory()) {
                return instanceDir;
            }
        }
        return null;
    }

    /**
     * Return the configuration directory for all the instances.
     * @return
     */
    protected List<File> getAllCswInstanceDirectory() throws ConfigurationException {
        final File configDir = getConfigurationDirectory();
        final List<File> results = new ArrayList<>();
        if (configDir != null && configDir.exists()) {
            for (File instanceDir : configDir.listFiles()) {
                if (instanceDir.isDirectory()) {
                    results.add(instanceDir);
                }
            }
        }
        return results;
    }
}
