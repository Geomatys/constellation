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

import java.sql.Connection;
import java.sql.DriverManager;
import org.geotoolkit.internal.jdbc.JDBC;

/**
 *
 * @author Guilhem Legal
 */
public class MDWMetadataReaderProfile {


    public MDWMetadataReaderProfile(Connection MDConnection) throws Exception {
        MDWebMetadataReader reader = new MDWebMetadataReader(MDConnection);
        reader.getAllEntries();
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 3) {
            JDBC.loadDriver("org.postgresql.Driver");
            Connection MDConnection = DriverManager.getConnection(args[0], args[1], args[2]);
            MDWMetadataReaderProfile profile = new MDWMetadataReaderProfile(MDConnection);
        } else {
            System.out.println("missing argument for connection");
        }
    }

}
