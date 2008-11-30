/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

import java.sql.SQLException;
import java.util.List;
import javax.xml.namespace.QName;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.coverage.web.WebServiceException;
import static org.constellation.metadata.io.MetadataReader.*;

/**
 *
 * @author Guilhem Legal 
 */
public class FileMetadataReader extends MetadataReader {

    
    public FileMetadataReader() {
        super(true);
    }
    /**
     * Return a metadata object from the specified identifier.
     * 
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * 
     * @return A marshallable metadata object.
     * @throws java.sql.SQLException
     */
    @Override
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws SQLException, WebServiceException {
        return getObjectFromFile(identifier);
    }

    private Object getObjectFromFile(String identifier) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
