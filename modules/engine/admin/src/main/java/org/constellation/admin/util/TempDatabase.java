/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.admin.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

public class TempDatabase {

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        // DriverManager.registerDriver(driver);

        String path = args[0];
        
        if(!path.startsWith("/tmp/"))
            throw new IOException("tmp files must be located in /tmp folder");
        
        File dbDir = new File(path);
        if (dbDir.exists()) {
            FileUtils.deleteDirectory(dbDir);
        }

        final String url = "jdbc:derby:" + path + ";create=true";
        DefaultDataSource ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/constellation/sql/v1/create-admin-db.sql"));
        sr.close(true);
        
        con.close();
        ds.shutdown();
        
    }

}
