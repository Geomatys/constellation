/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
import org.constellation.metadata.index.IndexLucene;
import org.constellation.ws.WebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public class FileMetadataWriter extends MetadataWriter {
    
    public FileMetadataWriter(IndexLucene index) throws SQLException {
        super(index);
        
    }

    @Override
    public boolean storeMetadata(Object obj) throws SQLException, WebServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
