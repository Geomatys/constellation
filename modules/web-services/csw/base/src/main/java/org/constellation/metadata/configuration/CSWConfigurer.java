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

package org.constellation.metadata.configuration;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.BriefNode;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.StringList;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataType;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.lucene.index.IndexDirectoryFilter;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.imageio.spi.ServiceRegistry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.ws.ICSWConfigurer;
import org.geotoolkit.index.tree.manager.NamedEnvelope;
import org.opengis.metadata.Metadata;

/**
 * {@link org.constellation.configuration.ServiceConfigurer} implementation for CSW service.
 *
 * TODO: implement specific configuration methods
 *
 * @author Fabien Bernard (Geomatys).
 * @author Cédric Briançon (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class CSWConfigurer extends OGCConfigurer implements ICSWConfigurer {

    protected final DocumentBuilderFactory dbf;
    
    /**
     * A flag indicating if an indexation is going on.
     */
    private boolean indexing;

    /**
     * The list of service currently indexing.
     */
    private final List<String> SERVICE_INDEXING = new ArrayList<>();
    
    /**
     * Create a new {@link CSWConfigurer} instance.
     */
    public CSWConfigurer() {
        indexing = false;
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }

    @Override
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

        final List<File> cswInstanceDirectories = new ArrayList<>();
        if ("all".equals(id)) {
            cswInstanceDirectories.addAll(ConfigDirectory.getInstanceDirectories("CSW"));
        } else {
            final File instanceDir = ConfigDirectory.getInstanceDirectory("CSW", id);
            if (instanceDir != null) {
                cswInstanceDirectories.add(instanceDir);
            }
        }

        if (!asynchrone) {
            synchroneIndexRefresh(cswInstanceDirectories, true);
        } else {
            asynchroneIndexRefresh(cswInstanceDirectories);
        }

        final String msg = "CSW index succefully recreated";
        return new AcknowlegementType("Success", msg);
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
     * Add some CSW record to the index.
     *
     * @param id identifier of the CSW service.
     * @param identifierList list of metadata identifier to add into the index.
     *
     * @return
     * @throws ConfigurationException
     */
    @Override
    public AcknowlegementType addToIndex(final String id, final String identifierList) throws ConfigurationException {
        LOGGER.info("Add to index requested");
        final List<String> identifiers = StringUtilities.toStringList(identifierList);
        AbstractIndexer indexer  = null;
        try {
            final CSWMetadataReader reader  = getReader(id);
            final List<Object> objectToIndex = new ArrayList<>();
            if (reader != null) {
                try {
                    for (String identifier : identifiers) {
                        final Object obj = reader.getMetadata(identifier, MetadataType.NATIVE);
                        if (obj == null) {
                            throw new ConfigurationException("Unable to find the metadata: " + identifier);
                        }
                        objectToIndex.add(obj);
                    }
                } catch (MetadataIoException ex) {
                    throw new ConfigurationException(ex);
                }
            } else {
                throw new ConfigurationException("Unable to create a reader for the id:" + id);
            }

            indexer = getIndexer(id, reader);
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
     * @param id identifier of the CSW service.
     * @param identifierList list of metadata identifier to add into the index.
     *
     * @return
     * @throws ConfigurationException
     */
    @Override
    public AcknowlegementType removeFromIndex(final String id, final String identifierList) throws ConfigurationException {
        LOGGER.info("Remove from index requested");
        final List<String> identifiers = StringUtilities.toStringList(identifierList);
        AbstractIndexer indexer  = null;
        try {
            final CSWMetadataReader reader  = getReader(id);
            indexer = getIndexer(id, reader);
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
     * @param id identifier of the CSW service.
     * @return an Acknowledgment.
     */
    @Override
    public AcknowlegementType stopIndexation(final String id) {
        LOGGER.info("\n stop indexation requested \n");
        if (isIndexing(id)) {
            return new AcknowlegementType("Success", "There is no indexation to stop");
        } else {
            AbstractIndexer.stopIndexation(Arrays.asList(id));
            return new AcknowlegementType("Success", "The indexation have been stopped");
        }
    }

    @Override
    public AcknowlegementType importRecords(final String id, final File f, final String fileName) throws ConfigurationException {
        LOGGER.info("Importing record");
        final AbstractIndexer indexer = getIndexer(id, null);
        try {
            final MetadataWriter writer = getWriter(id, indexer);
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

                final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                for (File importedFile: files) {
                    if (importedFile != null) {
                        Document document = docBuilder.parse(importedFile);
                        writer.storeMetadata(document.getDocumentElement());
                    } else {
                        throw new ConfigurationException("An imported file is null");
                    }
                }
                final String msg = "The specified record have been imported in the CSW";
                return new AcknowlegementType("Success", msg);
            } catch (SAXException | ParserConfigurationException | IOException ex) {
                LOGGER.log(Level.WARNING, "Exception while unmarshalling imported file", ex);
            } catch (MetadataIoException ex) {
                throw new ConfigurationException(ex);
            }
            return new AcknowlegementType("Error", "An error occurs during the process");
        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }
    }
    
    @Override
    public AcknowlegementType importRecord(final String id, final Node n) throws ConfigurationException {
        LOGGER.info("Importing record");
        final AbstractIndexer indexer = getIndexer(id, null);
        try {
            final MetadataWriter writer = getWriter(id, indexer);
            writer.storeMetadata(n);
            final String msg = "The specified record have been imported in the CSW";
            return new AcknowlegementType("Success", msg);
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }
    }

   
    @Override
    public AcknowlegementType removeRecords(final String id, final String identifierList) throws ConfigurationException {
        final AbstractIndexer indexer = getIndexer(id, null);
        try {
            final MetadataWriter writer = getWriter(id, indexer);
            try {
                final boolean deleted = writer.deleteMetadata(identifierList);
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
        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }
    }
    
    @Override
    public AcknowlegementType removeAllRecords(final String id) throws ConfigurationException {
        final CSWMetadataReader reader = getReader(id);
        final AbstractIndexer indexer  = getIndexer(id, reader);
        try {
            final MetadataWriter writer    = getWriter(id, indexer);
            try {
                final List<String> metaIDS = reader.getAllIdentifiers();
                for (String metaID : metaIDS) {
                    writer.deleteMetadata(metaID);
                }
                final String msg = "All records have been deleted from the CSW";
                return new AcknowlegementType("Success", msg);

            } catch (MetadataIoException ex) {
                throw new ConfigurationException(ex);
            }
        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }
    }

    @Override
    public AcknowlegementType metadataExist(final String id, final String metadataName) throws ConfigurationException {
        final CSWMetadataReader reader = getReader(id);
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

    @Override
    public List<BriefNode> getMetadataList(final String id, final int count, final int startIndex) throws ConfigurationException {
        final CSWMetadataReader reader = getReader(id);
        final AbstractCSWFactory factory = getCSWFactory(id);
        try {
            final List<BriefNode> results = new ArrayList<>();
            final List<String> ids = reader.getAllIdentifiers();
            if (startIndex >= ids.size()) {
                return results;
            }
            final Map<String , List<String>> fieldMap = factory.getBriefFieldMap();

            for (int i = startIndex; i<ids.size() && i<startIndex + count; i++) {
                results.add(new BriefNode(reader.getMetadata(ids.get(i), MetadataType.NATIVE), fieldMap));
            }
            return results;
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    @Override
    public List<Node> getFullMetadataList(final String id, final int count, final int startIndex, MetadataType type) throws ConfigurationException {
        final CSWMetadataReader reader = getReader(id);
        try {
            final List<Node> results = new ArrayList<>();
            final List<String> ids = reader.getAllIdentifiers();
            if (startIndex >= ids.size()) {
                return results;
            }

            for (int i = startIndex; i<ids.size() && i<startIndex + count; i++) {
                results.add(reader.getMetadata(ids.get(i), type));
            }
            return results;
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        }
    }


    @Override
    public Node getMetadata(final String id, final String metadataName) throws ConfigurationException {
        final CSWMetadataReader reader = getReader(id);
        try {
            return reader.getMetadata(metadataName, MetadataType.NATIVE);
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    @Override
    public int getMetadataCount(final String id) throws ConfigurationException {
        final CSWMetadataReader reader = getReader(id);
        try {
            return reader.getEntryCount();
        } catch (MetadataIoException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
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
     * @param id identifier of the CSW service.
     * @return
     * @throws ConfigurationException
     */
    protected Automatic getServiceConfiguration(final String id) throws ConfigurationException {
        final File instanceDirectory = ConfigDirectory.getInstanceDirectory("CSW", id);
        try {
            // we get the CSW configuration file
            final Automatic config = (Automatic) serviceBusiness.getConfiguration("CSW", id);
            if (config !=  null) {
                config.setConfigurationDirectory(instanceDirectory);
            }
            return config;

        } catch (ConfigurationException ex) {
            throw new ConfigurationException("Configuration exception while getting the CSW configuration for:" + id, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
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
    private void synchroneIndexRefresh(final List<File> cswInstanceDirectories, final boolean deleteFileIndexDb) throws ConfigurationException {
        boolean deleted = false;
        for (File cswInstanceDirectory : cswInstanceDirectories) {
            //we delete each index directory
            for (File indexDir : cswInstanceDirectory.listFiles(new IndexDirectoryFilter(null))) {
                deleted = true;
                for (File f : indexDir.listFiles()) {
                    final boolean sucess;
                    if (f.isDirectory()) {
                        sucess = FileUtilities.deleteDirectory(f);
                    } else {
                        sucess = f.delete();
                    }
                    if (!sucess) {
                        throw new ConfigurationException("The service can't delete the index file:" + f.getPath());
                    }
                }
                if (!indexDir.delete()) {
                    throw new ConfigurationException("The service can't delete the index folder.");
                }
            }
            //hack for FS CSW
            final File f = new File(cswInstanceDirectory, "csw-db");
            if (f.isDirectory() && deleteFileIndexDb) {
                FileUtilities.deleteDirectory(f);
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
                indexer = getIndexer(id, null);
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
    
    public Map<Integer, NamedEnvelope> getMapperContent(String serviceID) throws ConfigurationException {
        final AbstractIndexer indexer = getIndexer(serviceID, null);
        if (indexer != null) {
            try {
                return indexer.getMapperContent();
            } catch (IOException ex) {
                throw new ConfigurationException(ex);
            } finally {
                indexer.destroy();
            }
        }
        return new HashMap<>();
    }

    public String getTreeRepresentation(String serviceID) throws ConfigurationException {
        final AbstractIndexer indexer = getIndexer(serviceID, null);
        if (indexer != null) {
            try {
                return indexer.getTreeRepresentation();
            } finally {
                indexer.destroy();
            }
        }
        return null;
    }

    /**
     * Build a new Indexer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     * @param currentReader the metadata reader of the specified sevrice.
     *
     * @return A lucene Indexer.
     * @throws ConfigurationException
     */
    protected AbstractIndexer getIndexer(final String serviceID, CSWMetadataReader currentReader) throws ConfigurationException {

        // we get the CSW configuration file
        final Automatic config = getServiceConfiguration(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                if (currentReader == null) {
                    currentReader = cswfactory.getMetadataReader(config, serviceID);
                }
                final AbstractIndexer indexer = cswfactory.getIndexer(config, currentReader, "", currentReader.getAdditionalQueryablePathMap());
                if (indexer.needCreation()) {
                    indexer.createIndex();
                }
                return indexer;
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
     * @throws ConfigurationException
     */
    protected CSWMetadataReader getReader(final String serviceID) throws ConfigurationException {

        // we get the CSW configuration file
        final Automatic config = getServiceConfiguration(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                return cswfactory.getMetadataReader(config, serviceID);

            } catch (MetadataIoException ex) {
                throw new ConfigurationException("MetadataIoException while initializing the reader:" + ex.getMessage(), ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }

    /**
     * Build a new Metadata writer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     * @param indexer
     *
     * @return A metadata writer.
     * @throws ConfigurationException
     */
    protected MetadataWriter getWriter(final String serviceID, final AbstractIndexer indexer) throws ConfigurationException {

        // we get the CSW configuration file
        final Automatic config = getServiceConfiguration(serviceID);
        if (config != null) {
            final AbstractCSWFactory cswfactory = getCSWFactory(config.getType());
            try {
                return cswfactory.getMetadataWriter(config, indexer, serviceID);

            } catch (MetadataIoException ex) {
                throw new ConfigurationException("JAXBException while initializing the writer!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }

    private AbstractCSWFactory getCSWFactory(final String serviceID) throws ConfigurationException {
        final Automatic config = getServiceConfiguration(serviceID);
        if (config != null) {
            return getCSWFactory(config.getType());
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
     * {@inheritDoc}
     */
    @Override
    public Instance getInstance(final String serviceType, final String identifier) throws ConfigurationException {
        final Instance instance = super.getInstance(serviceType, identifier);
        try {
            instance.setLayersNumber(getMetadataCount(identifier));
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Error while getting metadata count on CSW instance:" + identifier, ex);
        }
        return instance;
    }
    
    /**
     * Convert {@code Metadata} to {@code Node} object.
     *
     * @param serviceID
     * @param metadataNode given node object to convert.
     * @return {@code Metadata}
     * @throws ConfigurationException
     */
    public Metadata getMetadataFromNode(final String serviceID, final Node metadataNode) throws ConfigurationException {
        try {
            final AbstractCSWFactory factory = getCSWFactory(serviceID);
            final MarshallerPool pool = factory.getMarshallerPool();
            final Unmarshaller um = pool.acquireUnmarshaller();
            final Object obj = um.unmarshal(metadataNode);
            pool.recycle(um);
            if (obj instanceof Metadata) {
                return (Metadata) obj;
            } else {
                throw new TargetNotFoundException("Record is not a metadata object");
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException("JAXB Exception while reading record", ex);
        }
    }
    
    public String getTemplateName(final String serviceID, final String metadataID, final String type) throws ConfigurationException {
        final AbstractCSWFactory factory = getCSWFactory(serviceID);
        return factory.getTemplateName(metadataID, type);
    }

    @Override
    public String getImplementation(final String serviceID) throws ConfigurationException {
        final Automatic config = getServiceConfiguration(serviceID);
        if (config != null) {
            return config.getFormat();
        }
        return null;
    }
}
