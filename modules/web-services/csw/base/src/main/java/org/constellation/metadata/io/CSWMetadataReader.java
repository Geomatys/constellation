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

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

// geotoolkit dependencies
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface CSWMetadataReader extends MetadataReader {


    /**
     * @param propertyNames A comma speparated list of property to retrieve.
     * 
     * @return a list of values for each specific fields specified as a coma separated String.
     * @throws MetadataIoException
     */
    List<DomainValues> getFieldDomainofValues(final String propertyNames) throws MetadataIoException;

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
    Node getMetadata(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException;

    @Deprecated
    Node getOriginalMetadata(final String identifier, final MetadataType mode, final ElementSetType type, final List<QName> elementName) throws MetadataIoException;

    /**
     * @return the list of QName for additional queryable element.
     */
    List<QName> getAdditionalQueryableQName();

    /**
     * @return the list of path for the additional queryable element.
     */
    Map<String, List<String>> getAdditionalQueryablePathMap();

    /**
     * Execute a SQL query and return the result as a List of identifier;
     *
     * @param sqlQuery
     * @return
     * @throws MetadataIoException
     */
    String[] executeEbrimSQLQuery(final String sqlQuery) throws MetadataIoException;

}
