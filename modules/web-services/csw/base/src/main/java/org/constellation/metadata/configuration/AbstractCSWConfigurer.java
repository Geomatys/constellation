/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2011, Geomatys
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

// J2SE dependencies
import javax.imageio.spi.ServiceRegistry;
import java.util.Iterator;
import org.constellation.configuration.DataSourceType;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.StringList;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;

import static org.constellation.ws.ExceptionCode.*;
import org.constellation.ws.WSEngine;

// Geotoolkit dependencies
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.lucene.index.IndexDirectoryFilter;
import org.geotoolkit.util.StringUtilities;

/**
 * The base for The CSW configurer.
 *
 * @author Guilhem Legal
 */
public abstract class AbstractCSWConfigurer extends AbstractConfigurer {

    /**
     * A Map of service configuration.
     */
    protected Map<String, Automatic> serviceConfiguration = new HashMap<String, Automatic>();

    /**
     * A flag indicating if an indexation is going on.
     */
    public boolean indexing;

    /**
     * The list of service currently indexing.
     */
    private final List<String> SERVICE_INDEXING = new ArrayList<String>();

    /**
     * Build a new CSW configurer.
     *
     * @param cn a injected container notifier allowing to reload all the jersey web-services.
     * @throws org.constellation.configuration.exception.ConfigurationException
     */
    public AbstractCSWConfigurer(final ContainerNotifierImpl cn) throws ConfigurationException {
        this.containerNotifier = cn;
        indexing = false;
        refreshServiceConfiguration();
    }


    @Override
    public Object treatRequest(final String request, final MultivaluedMap<String,String> parameters, final Object objectRequest) throws CstlServiceException {

        if ("RefreshIndex".equalsIgnoreCase(request)) {
            final boolean asynchrone = getBooleanParameter("ASYNCHRONE", false, parameters);
            final String id          = getParameter("ID", true, parameters);
            final boolean forced     = getBooleanParameter("FORCED", false, parameters);

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

        if ("AddToIndex".equalsIgnoreCase(request)) {

            final String id = getParameter("ID", true, parameters);
            final String identifierList = getParameter("IDENTIFIERS", true, parameters);
            return addToIndex(id, identifierList);
        }
        
        if ("RemoveFromIndex".equalsIgnoreCase(request)) {

            final String id = getParameter("ID", true, parameters);
            final String identifierList = getParameter("IDENTIFIERS", true, parameters);
            return removeFromIndex(id, identifierList);
        }

        if ("stopIndex".equalsIgnoreCase(request)) {

            final String id = getParameter("ID", false, parameters);
            return stopIndexation(id);
        }

        if ("importRecords".equalsIgnoreCase(request)) {

            final String id       = getParameter("ID", true, parameters);
            final String fileName = getParameter("fileName", true, parameters);
            return importRecords(id, (File)objectRequest, fileName);
        }

        if ("deleteRecords".equalsIgnoreCase(request)) {

            final String id       = getParameter("ID", true, parameters);
            final String metadata = getParameter("metadata", true, parameters);
            return deleteMetadata(id, metadata);
        }

        if ("metadataExist".equalsIgnoreCase(request)) {

            final String id       = getParameter("ID", true, parameters);
            final String metadata = getParameter("metadata", true, parameters);
            return metadataExist(id, metadata);
        }

        if ("GetCSWDatasourceType".equalsIgnoreCase(request)) {
            return getAvailableCSWDataSourceType();
        }

        return null;
    }

    @Override
    public boolean isLock() {
        return indexing;
    }

    @Override
    public void closeForced() {
        AbstractIndexer.stopIndexation();
    }

    /**
     * Return true if the select service (identified by his ID) is currently indexing (CSW).
     * @param id
     * @return
     */
    public boolean isIndexing(final String id) {
        return indexing && SERVICE_INDEXING.contains(id);
    }


    /**
     * Add the specified service to the indexing service list.
     * @param id
     */
    public void startIndexation(final String id) {
        indexing  = true;
        if (id != null) {
            SERVICE_INDEXING.add(id);
        }
    }

    /**
     * remove the selected service from the indexing service list.
     * @param id
     */
    public void endIndexation(final String id) {
        indexing = false;
        if (id != null) {
            SERVICE_INDEXING.remove(id);
        }
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
     * Build a new Indexer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     * @param cswConfigDir the CSW configuration directory.
     *
     * @return A lucene Indexer
     * @throws org.constellation.ws.CstlServiceException
     */
    protected AbstractIndexer initIndexer(final String serviceID, CSWMetadataReader currentReader) throws CstlServiceException {

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
                throw new CstlServiceException("An exception occurs while initializing the indexer!" + '\n' +
                        "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
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
    protected CSWMetadataReader initReader(final String serviceID) throws CstlServiceException {

        // we get the CSW configuration file
        final Automatic config = serviceConfiguration.get(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                return cswfactory.getMetadataReader(config);

            } catch (MetadataIoException ex) {
                throw new CstlServiceException("JAXBException while initializing the reader!", ex, NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
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
    protected CSWMetadataWriter initWriter(final String serviceID) throws CstlServiceException {

        // we get the CSW configuration file
        final Automatic config = serviceConfiguration.get(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                return cswfactory.getMetadataWriter(config, null);

            } catch (MetadataIoException ex) {
                throw new CstlServiceException("JAXBException while initializing the writer!", ex, NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Return all the founded CSW service identifiers.
     *
     * @return all the founded CSW service identifiers.
     */
    public List<String> getAllServiceIDs() {
        final List<String> result = new ArrayList<String>();
        for (String id : serviceConfiguration.keySet()) {
            result.add(id);
        }
        return result;
    }

    /**
     * Refresh the map of configuration object.
     *
     * @throws ConfigurationException
     */
    protected void refreshServiceConfiguration() throws ConfigurationException {
        serviceConfiguration    = new HashMap<String, Automatic>();
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
                GenericDatabaseMarshallerPool.getInstance().release(configUnmarshaller);

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
     * Destroy the CSW index directory in order that it will be recreated.
     *
     * @param asynchrone a flag for indexation mode.
     * @param id The service identifier.
     *
     * @return
     * @throws CstlServiceException
     */
    public AcknowlegementType refreshIndex(final boolean asynchrone, final String id) throws CstlServiceException {
        String suffix = "";
        if (asynchrone) {
            suffix = " (asynchrone)";
        }
        if (id != null && !id.isEmpty()) {
            suffix = suffix + " id:" + id;
        }
        LOGGER.log(Level.INFO, "refresh index requested{0}", suffix);
        try {
            refreshServiceConfiguration();
        } catch (ConfigurationException ex) {
            throw new CstlServiceException(ex);
        }
        final List<File> cswInstanceDirectories = new ArrayList<File>();
        try  {
            if ("all".equals(id)) {
                cswInstanceDirectories.addAll(getAllCswInstanceDirectory());
            } else {
                final File instanceDir = getCswInstanceDirectory(id);
                if (instanceDir != null) {
                    cswInstanceDirectories.add(instanceDir);
                }
            }
        } catch (ConfigurationException ex) {
            throw new CstlServiceException(ex);
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
     * Delete The index folder and call the restart() method.
     *
     * TODO maybe we can directly recreate the index here (fusion of synchrone/asynchrone)
     *
     * @param configurationDirectory The CSW configuration directory.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    private void synchroneIndexRefresh(final List<File> cswInstanceDirectories) throws CstlServiceException {
        boolean deleted = false;
        for (File cswInstanceDirectory : cswInstanceDirectories) {
            //we delete each index directory
            for (File indexDir : cswInstanceDirectory.listFiles(new IndexDirectoryFilter(null))) {
                deleted = true;
                for (File f : indexDir.listFiles()) {
                    final boolean sucess = f.delete();
                    if (!sucess) {
                        throw new CstlServiceException("The service can't delete the index file:" + f.getPath(), NO_APPLICABLE_CODE);
                    }
                }
                if (!indexDir.delete()) {
                    throw new CstlServiceException("The service can't delete the index folder.", NO_APPLICABLE_CODE);
                }
            }
        }

        //if we have deleted something we restart the services
        if (deleted) {
            restart();
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
    private void asynchroneIndexRefresh(final List<File> cswInstanceDirectories) throws CstlServiceException {
        for (File cswInstanceDirectory : cswInstanceDirectories) {
            String id = cswInstanceDirectory.getName();
            final File nexIndexDir        = new File(cswInstanceDirectory, "index-" + System.currentTimeMillis());
            AbstractIndexer indexer = null;
            try {
                indexer = initIndexer(id, null);
                if (indexer != null) {
                    final boolean success = nexIndexDir.mkdir();
                    if (!success) {
                        throw new CstlServiceException("Unable to create a directory nextIndex for  the id:" + id, NO_APPLICABLE_CODE);
                    }
                    indexer.setFileDirectory(nexIndexDir);
                    indexer.createIndex();

                } else {
                    throw new CstlServiceException("Unable to create an indexer for the id:" + id, NO_APPLICABLE_CODE);
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, "unable to create an indexer for id:{0}", id);
            } catch (IndexingException ex) {
                throw new CstlServiceException("An eception occurs while creating the index!" + '\n' +
                        "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
            } finally {
                if (indexer != null) {
                    indexer.destroy();
                }
            }
        }
    }

    /**
     * Add some CSW record to the index.
     *
     * @param asynchrone
     * @return
     * @throws CstlServiceException
     */
    public AcknowlegementType addToIndex(final String id, final String identifierList) throws CstlServiceException {
        LOGGER.info("Add to index requested");
        final List<String> identifiers = StringUtilities.toStringList(identifierList);
        AbstractIndexer indexer  = null;
        try {
            final CSWMetadataReader reader  = initReader(id);
            final List<Object> objectToIndex = new ArrayList<Object>();
            if (reader != null) {
                try {
                    for (String identifier : identifiers) {
                        objectToIndex.add(reader.getMetadata(identifier, AbstractMetadataReader.NATIVE));
                    }
                } catch (MetadataIoException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
            } else {
                throw new CstlServiceException("Unable to create a reader for the id:" + id, NO_APPLICABLE_CODE);
            }

            indexer = initIndexer(id, reader);
            if (indexer != null) {
                for (Object obj : objectToIndex) {
                    indexer.indexDocument(obj);
                }
            } else {
                throw new CstlServiceException("Unable to create an indexer for the id:" + id, NO_APPLICABLE_CODE);
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
    public AcknowlegementType removeFromIndex(final String id, final String identifierList) throws CstlServiceException {
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
                throw new CstlServiceException("Unable to create an indexer for the id:" + id, NO_APPLICABLE_CODE);
            }

        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }

        final String msg = "The specified record have been remove from the CSW index";
        return new AcknowlegementType("Success", msg);
    }

    private AcknowlegementType importRecords(final String id, final File f, final String fileName) throws CstlServiceException {
        LOGGER.info("Importing record");
        final CSWMetadataWriter writer = initWriter(id);
        final List<File> files;
        if (fileName.endsWith("zip")) {
            try  {
                final FileInputStream fis = new FileInputStream(f);
                files = FileUtilities.unZipFileList(fis);
                fis.close();
            } catch (IOException ex) {
                throw new CstlServiceException(ex);
            }
        } else if (fileName.endsWith("xml")) {
            files = Arrays.asList(f);
        } else {
            throw new CstlServiceException("Unexpected file extension, accepting zip or xml");
        }
        Unmarshaller u = null;
        try {
            u = EBRIMMarshallerPool.getInstance().acquireUnmarshaller();
            for (File importedFile: files) {
                if (importedFile != null) {
                    Object unmarshalled = u.unmarshal(importedFile);
                    if (unmarshalled instanceof JAXBElement) {
                        unmarshalled = ((JAXBElement)unmarshalled).getValue();
                    }
                    writer.storeMetadata(unmarshalled);
                } else {
                    throw new CstlServiceException("An imported file is null");
                }
            }
            final String msg = "The specified record have been imported in the CSW";
            return new AcknowlegementType("Success", msg);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Exception while unmarshalling imported file", ex);
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        } finally {
            if (u != null) {
                EBRIMMarshallerPool.getInstance().release(u);
            }
        }
        return new AcknowlegementType("Error", "An error occurs during the process");
    }

    private AcknowlegementType metadataExist(final String id, final String metadataName) throws CstlServiceException {
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
            throw new CstlServiceException(ex);
        }
    }

    private AcknowlegementType deleteMetadata(final String id, final String metadataName) throws CstlServiceException {
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
            throw new CstlServiceException(ex);
        }
    }

    /**
     * Reload all the web-services.
     */
    protected boolean restart() {
        if (containerNotifier != null) {
            BDD.clearConnectionPool();
            WSEngine.prepareRestart();
            containerNotifier.reload();
            return true;
        } else {
            return false;
        }
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
        final List<File> results = new ArrayList<File>();
        if (configDir != null && configDir.exists()) {
            for (File instanceDir : configDir.listFiles()) {
                if (instanceDir.isDirectory()) {
                    results.add(instanceDir);
                }
            }
        }
        return results;
    }

    private StringList getAvailableCSWDataSourceType() {
        final List<DataSourceType> sources = new ArrayList<DataSourceType>();
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
}
