/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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
package org.constellation.database.configuration;

import org.geotoolkit.factory.Hints;
import org.geotoolkit.referencing.factory.epsg.EpsgInstaller;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

/**
 * Install EPSG database on given datasource (mostly same datasource of Constellation database)
 *
 * @author Quentin Boileau (Geomatys)
 */
public class EPSGDatabaseIniter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EPSGDatabaseIniter.class);

    /**
     * Some EPSG database table used to check if database already installed.
     */
    private static final String[] SAMPLES = {
            "Coordinate Reference System",
            "coordinatereferencesystem",
            "epsg_coordinatereferencesystem"
    };

    private DataSource dataSource;

    @PostConstruct
    public void init() {
        //set datasource used by geotoolkit EPSG database
        Hints.putSystemDefault(Hints.EPSG_DATA_SOURCE, dataSource);

        try {
            if (exists(dataSource)) {
                LOGGER.info("EPSG database already installed.");
            } else {
                try (Connection connection = dataSource.getConnection()) {
                    final EpsgInstaller epsgInstaller = new EpsgInstaller();
                    epsgInstaller.setSchema(EpsgInstaller.DEFAULT_SCHEMA);
                    epsgInstaller.setDatabase(connection);
                    epsgInstaller.call();
                } catch (FactoryException | SQLException e) {
                    LOGGER.error("Unable to initialize EPSG database : " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Unable to connect to database " + e.getMessage(), e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Check if EPSG database is already installed.
     *
     * @param dataSource
     * @returnq
     * @throws IOException
     */
    private synchronized boolean exists(DataSource dataSource) throws IOException {
        try (Connection conn = dataSource.getConnection()) {
            final DatabaseMetaData md = conn.getMetaData();
            LOGGER.info("Check EPSG database installation on " + md.getURL());

            final ResultSet result = md.getTables(null, EpsgInstaller.DEFAULT_SCHEMA, null, new String[] {"TABLE"});
            while (result.next()) {
                final String table = result.getString("TABLE_NAME");
                for (final String candidate : SAMPLES) {
                    if (candidate.equalsIgnoreCase(table)) {
                        return true;
                    }
                }
            }
            return false;

        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
