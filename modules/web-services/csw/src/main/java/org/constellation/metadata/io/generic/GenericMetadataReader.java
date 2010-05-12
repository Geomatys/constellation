/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

package org.constellation.metadata.io.generic;

// J2SE dependencies
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

// constellation dependencies
import org.constellation.generic.GenericReader;
import org.constellation.generic.Values;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import static org.constellation.metadata.io.AbstractMetadataReader.*;

// Geotoolkit dependencies
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.AbstractRecordType;

/**
 * A database Reader using a generic configuration to request an unknown database.
 * 
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class GenericMetadataReader extends GenericReader implements CSWMetadataReader {
    
    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param configuration
     */
    public GenericMetadataReader(Automatic configuration) throws MetadataIoException {
        super(configuration);
    }
    
    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     */
    protected Values loadData(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        LOGGER.finer("loading data for " + identifier);

        final List<String> variables;
        
        if (mode == ISO_19115) {
            variables = getVariablesForISO();
        } else {
            if (mode == DUBLINCORE) {
                variables = getVariablesForDublinCore(type, elementName);
            } else {
                throw new IllegalArgumentException("unknow mode");
            }
        }
        return loadData(variables, identifier);
    }

    @Override
    public Object getMetadata(String identifier, int mode, List<QName> elementName) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, elementName);
    }

    /**
     * Return a new Metadata object read from the database for the specified identifier.
     *  
     * @param identifier An unique identifier
     * @param mode An output schema mode: ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (dublin core Record / geotoolkit metadata)
     * 
     * @throws MetadataIoException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        
        //TODO we verify that the identifier exists
        final Values values = loadData(identifier, mode, type, elementName);

        final Object result;
        if (mode == ISO_19115) {
            result = getISO(identifier, values);
            
        } else if (mode == DUBLINCORE) {
            result = getDublinCore(identifier, type, elementName, values);
            
        } else {
            throw new IllegalArgumentException("Unknow or unAuthorized standard mode: " + mode);
        }
        return result;
    }
    
    /**
     * return a metadata in dublin core representation.
     * 
     * @param identifier
     * @param type
     * @param elementName
     * @return
     */
    protected abstract AbstractRecordType getDublinCore(String identifier, ElementSetType type, List<QName> elementName, Values values);
    
    /**
     * return a metadata in ISO representation.
     * 
     * @param identifier
     * @return
     */
    protected abstract DefaultMetadata getISO(String identifier, Values values);
    
    /**
     * Return a list of variables name used for the dublicore representation.
     * @return
     */
    protected abstract List<String> getVariablesForDublinCore(ElementSetType type, List<QName> elementName);

    /**
     * Return a list of variables name used for the ISO representation.
     * @return
     */
    protected abstract List<String> getVariablesForISO();

       
    /**
     * Return all the entries from the database.
     * 
     * @return
     * @throws MetadataIoException
     */
    @Override
    public List<DefaultMetadata> getAllEntries() throws MetadataIoException {
        final List<DefaultMetadata> result = new ArrayList<DefaultMetadata>();
        final List<String> identifiers  = getAllIdentifiers();
        for (String id : identifiers) {
            result.add((DefaultMetadata) getMetadata(id, ISO_19115, ElementSetType.FULL, null));
        }
        return result;
    }
    
    /**
     * Return all the identifiers in this database.
     * 
     * @return
     */
    @Override
    public List<String> getAllIdentifiers() throws MetadataIoException {
        return getMainQueryResult();
    }
    
    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException {
         throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public List<String> executeEbrimSQLQuery(String sqlQuery) throws MetadataIoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
