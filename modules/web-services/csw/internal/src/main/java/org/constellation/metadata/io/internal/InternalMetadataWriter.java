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
package org.constellation.metadata.io.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IMetadataBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.database.api.jooq.tables.pojos.Metadata;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.AbstractMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.utils.Utils;
import org.geotoolkit.lucene.index.AbstractIndexer;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class InternalMetadataWriter extends AbstractMetadataWriter {

    private final boolean displayServiceMetadata = false;
    
    @Inject
    protected IMetadataBusiness metadataBusiness;
    
    /**
     * An indexer lucene to add object into the index.
     */
    protected final AbstractIndexer indexer;
    
    protected boolean partial = false;
    
    private boolean onlyPublished = false;
    
    protected final String id;
    
    protected final DocumentBuilderFactory dbf;

    protected final XMLInputFactory xif = XMLInputFactory.newFactory();
    
    public InternalMetadataWriter(final Automatic configuration, final AbstractIndexer indexer, final String serviceID) throws MetadataIoException {
        SpringHelper.injectDependencies(this);
        this.indexer = indexer;
        if (configuration.getCustomparameters().containsKey("partial")) {
            this.partial = Boolean.parseBoolean(configuration.getParameter("partial"));
        }
        if (configuration.getCustomparameters().containsKey("onlyPublished")) {
            this.onlyPublished = Boolean.parseBoolean(configuration.getParameter("onlyPublished"));
        }
        this.id = serviceID;
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }
    
    @Override
    public boolean storeMetadata(Node original) throws MetadataIoException {
        final String identifier = Utils.findIdentifier(original);
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(new DOMSource(original),sr);
            
            if (indexer != null) {
                indexer.removeDocument(identifier);
                indexer.indexDocument(original);
            }
            metadataBusiness.updateMetadata(identifier, sw.toString());
            metadataBusiness.linkMetadataIDToCSW(identifier, id);
            return true;
        } catch (TransformerException | ConfigurationException ex) {
            throw new MetadataIoException("Unable to write the file.", ex, NO_APPLICABLE_CODE);
        }
    }

    @Override
    public boolean deleteMetadata(String metadataID) throws MetadataIoException {
        if (partial) {
            metadataBusiness.unlinkMetadataIDToCSW(metadataID, id);
            return true;
        } else {
            throw new MetadataIoException("The delete is not supported in internal metadata.");
        }
    }

    @Override
    public boolean isAlreadyUsedIdentifier(String metadataID) throws MetadataIoException {
        return metadataBusiness.existInternalMetadata(metadataID, true, false);
    }

    @Override
    public boolean replaceMetadata(String metadataID, Node any) throws MetadataIoException {
        return storeMetadata(any);
    }

    @Override
    public boolean deleteSupported() {
        return false;
    }

    @Override
    public boolean updateSupported() {
        return true;
    }

    @Override
    public void destroy() {
        if (indexer != null) {
            indexer.destroy();
        }
    }

    @Override
    public boolean updateMetadata(String metadataID, Map<String, Object> properties) throws MetadataIoException {
        throw new MetadataIoException("The update by properties is not supported in internal metadata.");
    }

    @Override
    public boolean canImportInternalData() {
        return partial;
    }

    @Override
    public void linkInternalMetadata(final String metadataID) throws MetadataIoException {
        final Metadata metadata = metadataBusiness.searchFullMetadata(metadataID, true, false);
        if (metadata != null) {
            if (displayServiceMetadata ||
               (!displayServiceMetadata && metadata.getServiceId() == null) ||
               !onlyPublished ||
               (onlyPublished && metadata.getIsPublished())) {
                try {
                    final InputSource source = new InputSource(new StringReader(metadata.getMetadataIso()));
                    final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    final Document document = docBuilder.parse(source);
                    final Node n =  document.getDocumentElement();

                    if (indexer != null) {
                        indexer.removeDocument(metadataID);
                        indexer.indexDocument(n);
                    }
                } catch (SAXException | IOException | ParserConfigurationException ex) {
                    throw new MetadataIoException(ex);
                }
            }
            try {
                metadataBusiness.linkMetadataIDToCSW(metadataID, id);
            } catch (ConfigurationException ex) {
                throw new MetadataIoException(ex);
            }
        }
    }
}
