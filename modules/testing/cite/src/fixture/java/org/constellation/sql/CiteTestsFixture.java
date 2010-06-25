/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.sql;

import com.greenpepper.interpreter.flow.scenario.Check;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;


/**
 * Tests the results of a CITE tests session.
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.6
 */
public final class CiteTestsFixture extends ResultsDatabase {
    private static final String SELECT_LAST_DATE =
            "SELECT max(date) FROM \"Suites\" WHERE service=? AND version=?";

    public CiteTestsFixture() throws SQLException {
        super();
    }

    /**
     * Compare the last results between the current session and the previous one.
     *
     * @param service The service name.
     * @param version The service version.
     * @return {@code True} if there is no test that fails for this session and succeed
     *         for the previous one. {@code False} if there is one or more new problems.
     * @throws SQLException
     */
    @Check("Vérifier que la dernière session pour le service (\\w+) en version (\\d.\\d.\\d) n'a pas régressé")
    public boolean compareLastResults(final String service, final String version)
                                  throws SQLException, ParseException
    {
        final PreparedStatement ps = connection.prepareStatement(SELECT_LAST_DATE);
        ps.setString(1, service);
        ps.setString(2, version);
        final ResultSet rs = ps.executeQuery();
        final String date;
        if (rs.next()) {
            date = rs.getString(1);
        } else {
            throw new SQLException("The requested session does not contain values");
        }
        return compareResults(DATE_FORMAT.parse(date), service, version);
    }
}
