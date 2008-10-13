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
import java.util.List;
import javax.xml.namespace.QName;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.ows.v100.OWSWebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public interface MetadataReader {
    
    public final static int DUBLINCORE = 0;
    public final static int ISO_19115  = 1;
    public final static int EBRIM      = 2;
    
    /**
     * Return a metadata object from the specified identifier.
     * if is not already in cache it read it from the MDWeb database.
     * 
     * @param identifier The form identifier with the pattern : Form_ID:
     * 
     * @return An metadata object.
     * @throws java.sql.SQLException
     */
    public Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws SQLException, OWSWebServiceException;
    
    
    public void setVersion(ServiceVersion version);

}
