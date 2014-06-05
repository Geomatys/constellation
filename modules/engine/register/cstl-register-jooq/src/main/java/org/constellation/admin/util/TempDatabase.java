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
        
        if(!path.startsWith("/tmp/") && !path.contains("target/"))
            throw new IOException("tmp files must be located in /tmp or target folder: " +path);
        
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
