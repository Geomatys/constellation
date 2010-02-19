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

package org.constellation.metadata.io;

import java.sql.Timestamp;
import java.util.List;
import org.constellation.generic.database.Automatic;
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.util.Utilities;
import org.mdweb.io.MD_IOException;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
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
    public MDWebCSWMetadataWriter(Automatic configuration, AbstractIndexer index) throws MetadataIoException {
        super(configuration);
        this.indexer = index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexDocument(Form f) {
        if (indexer != null) {
            indexer.indexDocument(f);
        }
    }

    @Override
    public boolean deleteMetadata(String identifier) throws MetadataIoException {
         boolean success = super.deleteMetadata(identifier);
         if (identifier.indexOf(':') != -1) {
            identifier = identifier.substring(0, identifier.indexOf(':'));
         }
         indexer.removeDocument(identifier);
         return success;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws MetadataIoException {
        LOGGER.info("metadataID: " + metadataID);
        int id;
        String catalogCode = "";
        Form f = null;
        //we parse the identifier (Form_ID:Catalog_Code)
        try  {
            if (metadataID.indexOf(':') != -1) {
                catalogCode    = metadataID.substring(metadataID.indexOf(':') + 1, metadataID.length());
                metadataID = metadataID.substring(0, metadataID.indexOf(':'));
                id         = Integer.parseInt(metadataID);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
             throw new MetadataIoException("Unable to parse: " + metadataID, NO_APPLICABLE_CODE, "id");
        }
        try {
            final Catalog catalog = mdWriter.getCatalog(catalogCode);
            f                     = mdWriter.getForm(catalog, id);

        } catch (MD_IOException ex) {
            throw new MetadataIoException("The service has throw an SQLException while updating the metadata: " + ex.getMessage(),
                        NO_APPLICABLE_CODE);
        }

        for (RecordPropertyType property : properties) {
            try {
                final String xpath = property.getName();
                final Object value = property.getValue();
                final MixedPath mp = getMDWPathFromXPath(xpath);
                LOGGER.finer("IDValue: " + mp.idValue);
                final List<Value> matchingValues = f.getValueFromNumberedPath(mp.path, mp.idValue);

                if (matchingValues.size() == 0) {
                    throw new MetadataIoException("There is no value matching for the xpath:" + property.getName(), INVALID_PARAMETER_VALUE);
                }
                for (Value v : matchingValues) {
                    LOGGER.finer("value:" + v);
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
}
