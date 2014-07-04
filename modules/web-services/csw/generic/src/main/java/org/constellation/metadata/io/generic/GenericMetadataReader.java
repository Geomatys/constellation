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

package org.constellation.metadata.io.generic;

// J2SE dependencies

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.generic.GenericReader;
import org.constellation.generic.Values;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.ElementSetType;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataType;
import org.geotoolkit.csw.xml.DomainValues;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// constellation dependencies
// Geotoolkit dependencies

/**
 * A database Reader using a generic configuration to request an unknown database.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public abstract class GenericMetadataReader extends GenericReader implements CSWMetadataReader {

    protected Level logLevel = Level.INFO;
    
    /**
     * Build a new Generic metadata reader and initialize the statement.
     * @param configuration
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public GenericMetadataReader(Automatic configuration) throws MetadataIoException {
        super(configuration);
    }
    
    /**
     * Load all the data for the specified Identifier from the database.
     * @param identifier
     * @param mode
     * @param type
     * @param elementName
     * @return
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    protected Values loadData(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException {
        LOGGER.log(Level.FINER, "loading data for {0}", identifier);

        final List<String> variables;
        
        if (mode == MetadataType.ISO_19115) {
            variables = getVariablesForISO();
        } else {
            if (mode == MetadataType.DUBLINCORE) {
                variables = getVariablesForDublinCore(type, elementName);
            } else {
                throw new IllegalArgumentException("unknow mode");
            }
        }
        return loadData(variables, identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getMetadata(String identifier, MetadataType mode) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, new ArrayList<QName>());
    }

    /**
     * Return a new Metadata object read from the database for the specified identifier.
     *  
     * @param identifier An unique identifier
     * @param mode An output schema mode: ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return A metadata Object (Dublin core Record / GeotoolKit metadata)
     * 
     * @throws MetadataIoException
     */
    @Override
    public Node getMetadata(String identifier, MetadataType mode, ElementSetType type, List<QName> elementName) throws MetadataIoException {
        
        //TODO we verify that the identifier exists
        final Values values = loadData(identifier, mode, type, elementName);

        final Node result;
        if (mode == MetadataType.ISO_19115 || mode == MetadataType.NATIVE) {
            result = getISO(identifier, values);
            
        } else if (mode == MetadataType.DUBLINCORE) {
            result = getDublinCore(identifier, type, elementName, values);
            
        } else {
            throw new IllegalArgumentException("Unknow or unAuthorized standard mode: " + mode);
        }
        return result;
    }

    /**
     * Return a metadata in dublin core representation.
     *
     * @param identifier An unique identifier for the metadata.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName  A list of QName describing the requested fields. (implies type == null)
     * @param values A set of variables and their associated values.
     *
     * @return A Dublin-core representation of the metadata.
     */
    protected abstract Node getDublinCore(String identifier, ElementSetType type, List<QName> elementName, Values values);
    
    /**
     * return a metadata in ISO representation.
     *
     * @param identifier An unique identifier for the metadata.
     * @param values A set of variables and their associated values.
     *
     * @return An ISO 19139 representation of the metadata.
     */
    protected abstract Node getISO(String identifier, Values values);
    
    /**
     * Return a list of variables names used for the dublicore representation.
     * 
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * @return a list of variables names.
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
        final List<DefaultMetadata> result = new ArrayList<>();
        final List<String> identifiers     = getAllIdentifiers();
        for (String id : identifiers) {
            result.add((DefaultMetadata) getMetadata(id, MetadataType.ISO_19115, ElementSetType.FULL, null));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException {
         throw new MetadataIoException("Not supported int this implementation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] executeEbrimSQLQuery(String sqlQuery) throws MetadataIoException {
        throw new MetadataIoException("Not supported int this implementation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThreadEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QName> getAdditionalQueryableQName() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String>> getAdditionalQueryablePathMap() {
        return new HashMap<>();
    }

    @Override
    public List<MetadataType> getSupportedDataTypes() {
        return Arrays.asList(MetadataType.ISO_19115, MetadataType.DUBLINCORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromCache(String identifier) {
        throw new UnsupportedOperationException("Cache is not enabled on this implementation");
    }

    /**
     * @return the logLevel
     */
    public Level getLogLevel() {
        return logLevel;
    }

    /**
     * @param logLevel the logLevel to set
     */
    @Override
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

}
