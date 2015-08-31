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

import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Search for Database configuration and register beans like "jooq-setting', "dataSource", "epsgDataSource", "dialect".
 *
 * @author Quentin Boileau (Geomatys)
 */
@Configuration
@DependsOn(value = "database-conf")
public class DatabaseRegister {

    /**
     * Optional injection. Default implementation used is {@link PostgreSQLConfiguration}
     */
    @Autowired(required = false)
    private IDatabaseConfiguration databaseConfiguration = new PostgreSQLConfiguration();

    @Bean(name = "jooq-setting")
    public Settings getJOOQSettings() {
        return databaseConfiguration.getJOOQSettings();
    }

    @Bean(name = "dialect")
    public SQLDialect getDialect() {
        return databaseConfiguration.getDialect();
    }

    @Bean(name = "dataSource")
    public DataSource createDatasource() {
        return databaseConfiguration.createCstlDatasource();
    }

    @Bean(name = "epsgDataSource")
    public DataSource createEPSGDatasource() {
        return databaseConfiguration.createEPSGDatasource();
    }
}
