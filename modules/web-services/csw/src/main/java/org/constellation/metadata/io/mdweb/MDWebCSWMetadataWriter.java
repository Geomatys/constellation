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

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.util.ReflectionUtilities;

import org.geotoolkit.csw.xml.Record;
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.dublincore.xml.AbstractSimpleLiteral;
import org.geotoolkit.ebrim.xml.EbrimInternationalString;
import org.geotoolkit.ebrim.xml.RegistryObject;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.util.Utilities;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.mdweb.io.MD_IOException;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;

import org.opengis.metadata.identification.Identification;


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

    public MDWebCSWMetadataWriter() throws MetadataIoException {
        super();
        indexer = null;
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
         final boolean success = super.deleteMetadata(identifier);
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
        LOGGER.log(logLevel, "metadataID: {0}", metadataID);
        int id;
        String recordSetCode = "";
        Form f = null;
        //we parse the identifier (Form_ID:RecordSet_Code)
        try  {
            if (metadataID.indexOf(':') != -1) {
                recordSetCode = metadataID.substring(metadataID.indexOf(':') + 1, metadataID.length());
                metadataID    = metadataID.substring(0, metadataID.indexOf(':'));
                id            = Integer.parseInt(metadataID);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
             throw new MetadataIoException("Unable to parse: " + metadataID, NO_APPLICABLE_CODE, "id");
        }
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
     * This method try to find a title for this object.
     * if the object is a ISO19115:Metadata or CSW:Record we know were to search the title,
     * else we try to find a getName(), getTitle(), or getId() method.
     *
     * @param obj the object for which we want a title.
     *
     * @return the founded title or UNKNOW_TITLE
     */
    @Override
    public String findTitle(Object obj) {

        //here we try to get the title
        AbstractSimpleLiteral titleSL = null;
        String title = UNKNOW_TITLE;
        if (obj instanceof Record) {
            titleSL = ((Record) obj).getTitle();
            if (titleSL == null) {
                titleSL = ((Record) obj).getIdentifier();
            }

            if (titleSL == null) {
                title = UNKNOW_TITLE;
            } else {
                if (titleSL.getContent().size() > 0)
                    title = titleSL.getContent().get(0);
            }

        } else if (obj instanceof DefaultMetadata) {
            final Collection<Identification> idents = ((DefaultMetadata) obj).getIdentificationInfo();
            if (!idents.isEmpty()) {
                final Identification ident = idents.iterator().next();
                if (ident != null && ident.getCitation() != null && ident.getCitation().getTitle() != null) {
                    title = ident.getCitation().getTitle().toString();
                }
            }
        } else if (obj instanceof RegistryObject) {
            final EbrimInternationalString ident = ((RegistryObject) obj).getName();
            if (ident != null && ident.getLocalizedString().size() > 0) {
                title = ident.getLocalizedString().get(0).getValue();
            } else {
                title = ((RegistryObject) obj).getId();
            }

        } else {
            Method nameGetter = null;
            String methodName = "";
            int i = 0;
            while (i < 3) {
                try {
                    switch (i) {
                        case 0: methodName = "getTitle";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;

                        case 1: methodName = "getName";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;

                        case 2: methodName = "getId";
                                nameGetter = obj.getClass().getMethod(methodName);
                                break;
                        default: break;
                    }


                } catch (NoSuchMethodException ex) {
                    LOGGER.finer("There is no " + methodName + " method in " + obj.getClass().getSimpleName());
                } catch (SecurityException ex) {
                    LOGGER.severe(" security exception while getting the title of the object.");
                }
                if (nameGetter != null) {
                    i = 3;
                } else {
                    i++;
                }
            }

            if (nameGetter != null) {
                final Object objT = ReflectionUtilities.invokeMethod(obj, nameGetter);
                if (objT instanceof String) {
                    title = (String) obj;

                } else if (objT instanceof AbstractSimpleLiteral) {
                    titleSL = (AbstractSimpleLiteral) objT;
                    if (titleSL.getContent().size() > 0) {
                        title = titleSL.getContent().get(0);
                    } else {
                        title = UNKNOW_TITLE;
                    }
                } else {
                    title = UNKNOW_TITLE;
                }

                if (title == null) {
                    title = UNKNOW_TITLE;
                }
            }

            if (title.equals(UNKNOW_TITLE)) {
                LOGGER.warning("unknow type: " + obj.getClass().getName() + " unable to find a title, using default then.");
            }
        }
        return title;
    }

    @Override
    public void setLogLevel(Level logLevel) {
        super.setLogLevel(logLevel);
        if (this.indexer != null) {
            this.indexer.setLogLevel(logLevel);
        }
    }


}
