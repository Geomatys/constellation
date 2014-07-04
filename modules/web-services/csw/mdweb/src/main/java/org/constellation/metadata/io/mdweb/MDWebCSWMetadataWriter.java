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

package org.constellation.metadata.io.mdweb;

import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.mdweb.io.MD_IOException;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Constellation dependencies
// Geotoolkit dependencies
// MDWeb meta model dependencies

/**
 * A CSW Metadata Writer specific for MDweb data source.
 * It allows to write, update and delete metadatas, it also keep the lucene Index of the CSW up to date.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebCSWMetadataWriter extends MDWebMetadataWriter {

    /**
     * An indexer lucene to add object into the index.
     */
    protected final AbstractIndexer indexer;

     /**
     * Build a new metadata writer.
     *
     * @param MDReader an MDWeb database reader.
     */
    public MDWebCSWMetadataWriter(final Automatic configuration, final AbstractIndexer index) throws MetadataIoException {
        super(configuration);
        this.indexer = index;
    }

    @Override
    public boolean storeMetadata(Node obj) throws MetadataIoException {
        return super.storeMetadata(obj); 
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void indexDocument(final FullRecord f) {
        if (indexer != null) {
            indexer.indexDocument(f);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMetadata(final String identifier) throws MetadataIoException {
         final boolean success = super.deleteMetadata(identifier);
         if (success) {
             indexer.removeDocument(identifier);
         }
         return success;
    }


    /**
     * {@inheritDoc}
     *
     * TODO move to {@link MDWebMetadataWriter}
     */
    @Override
    public boolean updateMetadata(final String metadataID, final Map<String, Object> properties) throws MetadataIoException {
        LOGGER.log(logLevel, "metadataID: {0}", metadataID);

        FullRecord f = null;
        try {
            f  = mdWriter.getRecord(metadataID);
        } catch (MD_IOException ex) {
            throw new MetadataIoException("The service has throw an SQLException while updating the metadata: " + ex.getMessage(),
                        NO_APPLICABLE_CODE);
        }

        for (Entry<String, Object> property : properties.entrySet()) {
            try {
                final Object value;
                if (property.getValue() instanceof Node) {
                    final Node n = (Node) property.getValue();
                    if (n instanceof Text) {
                        value = ((Text)property.getValue()).getTextContent();

                    // special case for LanguageCode
                    } else if (n.getLocalName().equals("LanguageCode")) {
                        final Node langAtt = n.getAttributes().getNamedItem("codeListValue");
                        if (langAtt == null) {
                            throw new MetadataIoException("missing codeListValue in languageCode node");
                        }
                        value = new Locale(langAtt.getNodeValue());
                    } else {
                        try {
                            final Unmarshaller u = CSWMarshallerPool.getInstance().acquireUnmarshaller();
                            value = u.unmarshal(n);
                            CSWMarshallerPool.getInstance().recycle(u);
                        } catch (JAXBException ex) {
                            throw new MetadataIoException(ex);
                        }
                    }
                } else {
                    value = property.getValue();
                }
                final MixedPath mp = getMDWPathFromXPath(property.getKey());
                LOGGER.log(Level.FINER, "IDValue: {0}", mp.idValue);
                final List<Value> matchingValues = f.getValueFromNumberedPath(mp.path, mp.idValue);

                // if there is no value for this path
                if (matchingValues.isEmpty()) {
                    //1. if the path is erroned
                    // 1.1 we have a cardinality in a non multiple property
                    Property prop = mp.path.getProperty();
                    if (prop != null && prop.getMaximumOccurence() == 1 && mp.ordinal > 1) {
                        throw new MetadataIoException("The property: " + prop.getName() + " is not a collection", INVALID_PARAMETER_VALUE);
                    }

                    //2. We must build the values non existing yet.
                    // to do that we must find the highest value existing in the value tree,
                    // and build the chain to the new Value.
                    //
                    // TODO for now the algorithm can work only if the parent value exist
                    // because if we have to build a chain of value, how do we find the type of each element?
                    final String parentIdValue = mp.idValue.substring(0, mp.idValue.lastIndexOf(':'));
                    final List<Value> parentValues    = f.getValueFromNumberedPath(mp.path.getParent(), parentIdValue);
                    if (parentValues != null && !parentValues.isEmpty()) {
                        final Map<Object, Value> alreadyWrite = new HashMap<>();
                        for (Value parentValue : parentValues) {
                            final List<Value> toInsert = addValueFromObject(f, value, mp.path, parentValue, alreadyWrite);
                            alreadyWrite.clear();
                            for (Value ins : toInsert) {
                                mdWriter.writeValue(ins);
                            }
                        }
                    } else {
                        throw new MetadataIoException("The service is not yet capable to build a value chain", NO_APPLICABLE_CODE);
                    }

                // if the value(s) already exist
                } else {
                    final Map<Object, Value> alreadyWrite = new HashMap<>();
                    for (Value v : matchingValues) {
                        if (v instanceof TextValue && value instanceof String) {
                            // TODO verify more Type
                            if (v.getType().equals(PrimitiveType.DATE)) {
                                try {
                                    String timeValue = (String)value;
                                    timeValue        = timeValue.replaceAll("T", " ");
                                    if (timeValue.indexOf('+') != -1) {
                                        timeValue    = timeValue.substring(0, timeValue.indexOf('+'));
                                    }
                                    LOGGER.finer(timeValue);
                                    Timestamp.valueOf(timeValue);
                                } catch(IllegalArgumentException ex) {
                                    throw new MetadataIoException("The type of the replacement value does not match with the value type : Date",
                                        INVALID_PARAMETER_VALUE);
                                }
                            }
                            LOGGER.finer("textValue updated");
                            mdWriter.updateTextValue((TextValue) v, (String) value);
                        } else {
                            final Classe requestType = getClasseFromObject(value);
                            final Classe valueType   = v.getType();
                            if (!Objects.equals(requestType, valueType)) {
                                String typeName = "null";
                                if (requestType != null) {
                                    typeName = requestType.getName();
                                }
                                throw new MetadataIoException("The type of the replacement value (" + typeName +
                                                               ") does not match with the value type :" + valueType.getName(),
                                        INVALID_PARAMETER_VALUE);
                            } else {
                                LOGGER.finer("value updated");
                                mdWriter.deleteValue(v);
                                final List<Value> toInsert = addValueFromObject(f, value, mp.path, v.getParent(), alreadyWrite);
                                alreadyWrite.clear();
                                for (Value ins : toInsert) {
                                    mdWriter.writeValue(ins);
                                }
                            }
                        }
                    }
                }
            } catch (MD_IOException | IllegalArgumentException ex) {
                throw new MetadataIoException(ex);
            }
            indexer.removeDocument(metadataID);
            indexer.indexDocument(f);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogLevel(final Level logLevel) {
        super.setLogLevel(logLevel);
        if (this.indexer != null) {
            this.indexer.setLogLevel(logLevel);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (indexer != null) {
            indexer.destroy();
        }
    }
}
