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

import java.net.URI;
import java.util.List;

/// geotoolkit dependencies
import java.util.Map;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface CSWMetadataReader extends MetadataReader {


    /**
     * Return a list of values for each specific fields specified as a coma separated String.
     */
    public List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException;

    /**
     * Return a metadata object from the specified identifier.
     *
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115, DUBLINCORE and SENSORML supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     *
     * @return A marshallable metadata object.
     * @throws MetadataIoException
     */
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException;

    /**
     * Return the list of supported data types.
     */
    public List<Integer> getSupportedDataTypes();

    /**
     * Return the list of QName for additional queryable element.
     */
    public List<QName> getAdditionalQueryableQName();

    /**
     * Return the list of path for the additional queryable element.
     */
    public Map<String, List<String>> getAdditionalQueryablePathMap();

    /**
     * Return the list of Additional queryable element.
     */
    public abstract Map<String, URI> getConceptMap();

    /**
     * Execute a SQL query and return the result as a List of identifier;
     *
     * @param query
     * @return
     * @throws MetadataIoException
     */
    public abstract List<String> executeEbrimSQLQuery(String sqlQuery) throws MetadataIoException;

    public void setLogLevel(Level logLevel);
}
