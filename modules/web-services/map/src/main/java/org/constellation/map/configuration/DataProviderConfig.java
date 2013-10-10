/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
        brief.setProvider(record.getProvider().getIdentifier());
        brief.setTitle(record.getTitle(locale));
        brief.setDate(record.getDate());
        brief.setType(record.getType().name());
        brief.setOwner(record.getOwnerLogin());
        return brief;
    }
}
