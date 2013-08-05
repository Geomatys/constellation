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

package org.constellation.metadata.io.mdweb;

import java.util.HashMap;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.geotoolkit.csw.xml.RecordProperty;

// Geotoolkit dependencies
import org.geotoolkit.lucene.index.AbstractIndexer;
import java.util.Objects;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// MDWeb meta model dependencies
import org.mdweb.io.MD_IOException;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;

/**
 * A CSW Metadata Writer specific for MDweb data source.
 * It allows to write, update and delete metadatas, it also keep the lucene Index of the CSW up to date.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebCSWMetadataWriter extends MDWebMetadataWriter implements CSWMetadataWriter {

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
     */
    @Override
    public boolean updateMetadata(final String metadataID, final List<? extends RecordProperty> properties) throws MetadataIoException {
        LOGGER.log(logLevel, "metadataID: {0}", metadataID);

        FullRecord f = null;
        try {
            f  = mdWriter.getRecord(metadataID);
        } catch (MD_IOException ex) {
            throw new MetadataIoException("The service has throw an SQLException while updating the metadata: " + ex.getMessage(),
                        NO_APPLICABLE_CODE);
        }

        for (RecordProperty property : properties) {
            try {
                final String xpath = property.getName();
                final Object value = property.getValue();
                final MixedPath mp = getMDWPathFromXPath(xpath);
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
                        final Map<Object, Value> alreadyWrite = new HashMap<Object, Value>();
                        for (Value parentValue : parentValues) {
                            final List<Value> toInsert = addValueFromObject(f, value, mp.path, parentValue, alreadyWrite);
                            for (Value ins : toInsert) {
                                mdWriter.writeValue(ins);
                            }
                        }
                    } else {
                        throw new MetadataIoException("The service is not yet capable to build a value chain", NO_APPLICABLE_CODE);
                    }

                // if the value(s) already exist
                } else {
                    final Map<Object, Value> alreadyWrite = new HashMap<Object, Value>();
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
                                throw new MetadataIoException("The type of the replacement value (" + requestType.getName() +
                                                               ") does not match with the value type :" + valueType.getName(),
                                        INVALID_PARAMETER_VALUE);
                            } else {
                                LOGGER.finer("value updated");
                                mdWriter.deleteValue(v);
                                final List<Value> toInsert = addValueFromObject(f, value, mp.path, v.getParent(), alreadyWrite);
                                for (Value ins : toInsert) {
                                    mdWriter.writeValue(ins);
                                }
                            }
                        }
                    }
                }
            } catch (MD_IOException ex) {
                throw new MetadataIoException(ex);
            } catch (IllegalArgumentException ex) {
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


}
