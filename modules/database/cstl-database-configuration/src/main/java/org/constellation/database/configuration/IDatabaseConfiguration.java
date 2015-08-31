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
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Quentin Boileau (Geomatys)
 */
@Configuration(value = "database-conf")
public interface IDatabaseConfiguration {

    /**
     * Return JOOQ settings specific to this database implementation.
     * @return jooq settings
     */
    Settings getJOOQSettings();

    /**
     * Specify dialect used by JOOQ to support this database implementation.
     * @return jooq SQLDialect
     */
    SQLDialect getDialect();

    /**
     * Create DataSource for Constellation using configured DATABASE_URL property
     * @return DataSource
     */
    DataSource createCstlDatasource();

    /**
     * Create DataSource for EPSG database using configured EPSG_DATABASE_URL property
     * @return DataSource
     */
    DataSource createEPSGDatasource();
}
