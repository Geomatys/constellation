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

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.util.Utilities;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// MDWeb meta model dependencies
import org.mdweb.io.MD_IOException;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.Form;
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
        mdWriter.setProperty("skipSameTitle", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexDocument(final Form f) {
        if (indexer != null) {
            indexer.indexDocument(f);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMetadata(String identifier) throws MetadataIoException {
         final boolean success = super.deleteMetadata(identifier);
         if (success) {
             if (identifier.indexOf(':') != -1) {
                identifier = identifier.substring(0, identifier.indexOf(':'));
             }
             indexer.removeDocument(identifier);
         }
         return success;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMetadata(String metadataID, final List<RecordPropertyType> properties) throws MetadataIoException {
        LOGGER.log(logLevel, "metadataID: {0}", metadataID);
        int id;
        final String recordSetCode;
        //we parse the identifier (Form_ID:RecordSet_Code)
        try  {
            if (metadataID != null && metadataID.indexOf(':') != -1) {
                recordSetCode = metadataID.substring(metadataID.indexOf(':') + 1, metadataID.length());
                metadataID    = metadataID.substring(0, metadataID.indexOf(':'));
                id            = Integer.parseInt(metadataID);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
             throw new MetadataIoException("Unable to parse: " + metadataID, NO_APPLICABLE_CODE, "id");
        }

        Form f = null;
        try {
            final RecordSet recordSet = mdWriter.getRecordSet(recordSetCode);
            f                     = mdWriter.getForm(recordSet, id);

        } catch (MD_IOException ex) {
            throw new MetadataIoException("The service has throw an SQLException while updating the metadata: " + ex.getMessage(),
                        NO_APPLICABLE_CODE);
        }

        for (RecordPropertyType property : properties) {
            try {
                final String xpath = property.getName();
                final Object value = property.getValue();
                final MixedPath mp = getMDWPathFromXPath(xpath);
                LOGGER.log(Level.FINER, "IDValue: {0}", mp.idValue);
                final List<Value> matchingValues = f.getValueFromNumberedPath(mp.path, mp.idValue);

                if (matchingValues.isEmpty()) {
                    throw new MetadataIoException("There is no value matching for the xpath:" + property.getName(), INVALID_PARAMETER_VALUE);
                }
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
                        if (!Utilities.equals(requestType, valueType)) {
                            throw new MetadataIoException("The type of the replacement value (" + requestType.getName() +
                                                           ") does not match with the value type :" + valueType.getName(),
                                    INVALID_PARAMETER_VALUE);
                        } else {
                            LOGGER.finer("value updated");
                            mdWriter.deleteValue(v);
                            final List<Value> toInsert = addValueFromObject(f, value, mp.path, v.getParent());
                            for (Value ins : toInsert) {
                                mdWriter.writeValue(ins);
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
