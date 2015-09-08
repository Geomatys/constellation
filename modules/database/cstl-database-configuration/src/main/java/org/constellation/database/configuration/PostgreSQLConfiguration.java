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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.constellation.configuration.AppProperty;
import org.constellation.configuration.Application;
import org.constellation.configuration.ConfigurationRuntimeException;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Default DatabaseConfiguration using PostgreSQL.
 */
@Configuration
public class PostgreSQLConfiguration implements IDatabaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLConfiguration.class);

    @Override
    public Settings getJOOQSettings() {
        return new Settings().withRenderNameStyle(RenderNameStyle.AS_IS);
    }

    @Override
    public SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @Override
    public DataSource createCstlDatasource() {

        // Force loading driver because some containers like tomcat 7.0.21+ disable drivers at startup.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }

        final String databaseURL = Application.getProperty(AppProperty.CSTL_DATABASE_URL);
        if (databaseURL == null) {
            throw new ConfigurationRuntimeException("Property \""+AppProperty.CSTL_DATABASE_URL.name()+"\" not defined.");
        }

        final HikariConfig config = DatabaseConfigurationUtils.createHikariConfig(databaseURL, "constellation", null);
        return new HikariDataSource(config);
    }

    @Override
    public DataSource createEPSGDatasource() {
        // Force loading driver because some containers like tomcat 7.0.21+ disable drivers at startup.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }

        final String databaseURL = Application.getProperty(AppProperty.EPSG_DATABASE_URL);
        if (databaseURL == null) {
            throw new ConfigurationRuntimeException("Property \""+AppProperty.EPSG_DATABASE_URL.name()+"\" not defined.");
        }

        final HikariConfig config = DatabaseConfigurationUtils.createHikariConfig(databaseURL, "epsg", 5);
        return new HikariDataSource(config);
    }

}
