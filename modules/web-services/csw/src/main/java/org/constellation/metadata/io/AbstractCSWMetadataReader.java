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

/// geotoolkit dependencies
import java.util.List;
import javax.xml.namespace.QName;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCSWMetadataReader extends AbstractMetadataReader implements CSWMetadataReader {

    public AbstractCSWMetadataReader(boolean isCacheEnabled, boolean isThreadEnabled) {
        super(isCacheEnabled, isThreadEnabled);
    }

    /**
     * Return a list of values for each specific fields specified as a coma separated String.
     */
    @Override
    public abstract List<DomainValues> getFieldDomainofValues(String propertyNames) throws MetadataIoException;

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
    @Override
    public abstract Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws MetadataIoException;


    @Override
    public Object getMetadata(String identifier, int mode, List<QName> elementName) throws MetadataIoException {
        return getMetadata(identifier, mode, ElementSetType.FULL, elementName);
    }
}
