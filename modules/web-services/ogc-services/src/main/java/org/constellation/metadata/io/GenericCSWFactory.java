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

import java.sql.Connection;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;
import org.constellation.generic.database.Automatic;
import static org.constellation.generic.database.Automatic.*;

/**
 *
 * @author Guilhem Legal
 */
public class GenericCSWFactory {
    
    public static GenericMetadataReader getMetadataReader(Automatic configuration, Connection MDConnection) throws SQLException, JAXBException {
        switch (configuration.getType()) {
            case CDI:
                return new CDIReader(configuration, MDConnection);
            case CSR:
                return new CSRReader(configuration, MDConnection);
            case EDMED:
                return new EDMEDReader(configuration, MDConnection);
            default:
                throw new IllegalArgumentException("Unknow database type");
        }
    }
}
