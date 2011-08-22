/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.auth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.internal.SetupService;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.logging.Logging;
import org.mdweb.io.auth.AuthenticationReader;
import org.mdweb.io.auth.sql.DataSourceAuthenticationReader;
import org.mdweb.sql.auth.AuthDatabaseCreator;

/**
 *
 * @since 0.8
 * @author Guilhem Legal (Geomatys)
 */
public class CstlAuthSetup implements SetupService {

    private static final Logger LOGGER = Logging.getLogger(CstlAuthSetup.class);
    
    @Override
    public void initialize(Properties properties, boolean reinit) {
        final File authDir          = ConfigDirectory.getAuthConfigDirectory();
        final File authDbProperties = ConfigDirectory.getAuthConfigFile();
        if (!authDbProperties.exists()) {
            LOGGER.info("Creating default Authentication Derby database");
            final String dbUrl = "jdbc:derby:" + authDir.getPath() + "/Cstl_User_Db";
            final File authDbDir = new File(authDir, "Cstl_User_Db");
            try {
                AuthDatabaseCreator.createEmbeddedUserDatabase(authDbDir);
                final DataSource ds = new DefaultDataSource(dbUrl + ";create=true");
                final AuthenticationReader reader = new DataSourceAuthenticationReader(ds);
                reader.writeUser("admin", "admin", "Default Constellation Administrator", Arrays.asList("cstl-admin"));
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "SQL exception while creating authentication derby database", ex);
            }
            
            Properties prop = new Properties();
            prop.put("cstl_authdb_type", "DERBYDIR");
            prop.put("cstl_authdb_host", "jdbc:derby:" + authDir.getPath() + "/Cstl_User_Db");
            try {
                final FileOutputStream stream = new FileOutputStream(authDbProperties);
                prop.store(stream, "auto generated at cstl startup");
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "IO exception while storing authentication properties file", ex);
            }
        } else {
            LOGGER.info("Authentication datasource present");
        }
        
        
    }

    @Override
    public void shutdown() {
        // do nothing
    }
    
}
