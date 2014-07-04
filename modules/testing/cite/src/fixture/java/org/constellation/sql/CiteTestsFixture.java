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
package org.constellation.sql;

import com.greenpepper.interpreter.flow.scenario.Check;
import com.greenpepper.interpreter.flow.scenario.Display;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


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
    public boolean compareLastResults(final String service, final String version) throws SQLException {
        final Date date = getLastDateForSession(service, version);
        if (date == null) {
            throw new SQLException("There is no session in the database for "+ service +
                    " version "+ version);
        }
        return compareResults(date, service, version);
    }

    /**
     * Display the failing tests.
     *
     * @param service The service name.
     * @param version The service version.
     * @return
     * @throws SQLException
     */
    @Display("Tests en échec pour le service (\\w+) en version (\\d.\\d.\\d)")
    public String displayErrors(final String service, final String version) throws SQLException {
        final Date date = getLastDateForSession(service, version);
        if (date == null) {
            throw new SQLException("There is no session in the database for "+ service +
                    " version "+ version);
        }
        final List<Result> failings = getTestsFailed(date);
        if (failings.isEmpty()) {
            return "";
        }
        final StringBuilder builder = new StringBuilder("{li}");
        for (Result failing : failings) {
            builder.append(failing.toString()).append('\n')
                   .append("=> Assertion: ").append(failing.getAssertion())
                   .append('\n');
        }
        builder.append("{li}");
        return builder.toString();
    }

    /**
     * Display the result for the specified test.
     *
     * @param id The test id.
     * @param service The service name.
     * @param version The service version.
     * @return {@code Disappear} if a test has disappeared between two sessions, {@code Passed}
     *         if the test is valid, {@code Failed} if the test fails.
     * @throws SQLException
     */
    @Check("Test (\\S+) pour le service (\\w+) en version (\\d.\\d.\\d)")
    public boolean getResultsForTest(final String id, final String service, final String version)
                                                                             throws SQLException
    {
        final Date date = getLastDateForSession(service, version);
        if (date == null) {
            throw new SQLException("There is no session in the database for "+ service +
                    " version "+ version);
        }
        final Result res = getTest(date, id);
        if (res == null) {
            return false;
        }
        if (res.isPassed()) {
            return true;
        }
        return false;
    }

    /**
     * Return the date of the nearest session of tests stored in the database, for the given service
     * and version.
     *
     * @param service The service name.
     * @param version The service version.
     * @return
     * @throws SQLException
     */
    private Date getLastDateForSession(final String service, final String version) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement(SELECT_LAST_DATE);
        ps.setString(1, service);
        ps.setString(2, version);
        final ResultSet rs = ps.executeQuery();
        final Date date;
        if (rs.next()) {
            date = rs.getTimestamp(1);
        } else {
            rs.close();
            ps.close();
            throw new SQLException("The requested session does not contain values");
        }
        rs.close();
        ps.close();
        return date;
    }
}
