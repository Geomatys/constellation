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

package org.constellation.map.configuration;

import org.apache.sis.util.Static;
import org.constellation.admin.dao.DataRecord;
import org.constellation.configuration.DataBrief;

import java.sql.SQLException;
import java.util.Locale;

/**
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class DataProviderConfig extends Static {

    /**
     * Builds a {@link DataBrief} instance from a {@link DataRecord} instance.
     *
     * @param record the record to be converted
     * @param locale the locale for internationalized text
     * @return a {@link DataBrief} instance
     * @throws SQLException if a database access error occurs
     */
    public static DataBrief getBriefFromRecord(final DataRecord record, final Locale locale) throws SQLException {
        final DataBrief brief = new DataBrief();
        brief.setName(record.getName());
        brief.setNamespace(record.getNamespace());
        brief.setProvider(record.getProvider().getIdentifier());
        brief.setTitle(record.getTitle(locale));
        brief.setDate(record.getDate());
        brief.setType(record.getType().name());
        brief.setOwner(record.getOwnerLogin());
        return brief;
    }
}
