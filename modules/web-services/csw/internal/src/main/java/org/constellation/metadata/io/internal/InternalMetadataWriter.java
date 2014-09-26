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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Map;
import javax.inject.Inject;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IMetadataBusiness;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.AbstractMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.utils.Utils;
import org.geotoolkit.lucene.index.AbstractIndexer;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import org.w3c.dom.Node;

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
    
    public InternalMetadataWriter(final Automatic configuration, final AbstractIndexer indexer, final String serviceID) throws MetadataIoException {
        SpringHelper.injectDependencies(this);
        this.indexer = indexer;
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
            
            return metadataBusiness.updateMetadata(identifier, sw.toString());
        } catch (TransformerException ex) {
            throw new MetadataIoException("Unable to write the file.", ex, NO_APPLICABLE_CODE);
        }
    }

    @Override
    public boolean deleteMetadata(String metadataID) throws MetadataIoException {
        throw new MetadataIoException("The delete is not supported in internal metadata.");
    }

    @Override
    public boolean isAlreadyUsedIdentifier(String metadataID) throws MetadataIoException {
        return metadataBusiness.existInternalMetadata(metadataID, displayServiceMetadata);
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
        
    }

    @Override
    public boolean updateMetadata(String metadataID, Map<String, Object> properties) throws MetadataIoException {
        throw new MetadataIoException("The update by properties is not supported in internal metadata.");
    }
}
