/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.catalog;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.ListIterator;
import static org.constellation.catalog.QueryType.*;

import org.junit.*;


/**
 * Tests {@code Query}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class QueryTest extends DatabaseTest {
    /**
     * Tests a case similar to the SQL query used in the format table.
     * This is a relatively simple case.
     */
    @Test
    public void testSimple() throws SQLException {
        final QueryType[] uses = new QueryType[] {SELECT, LIST};
        final Query  query = new Query(database, "Formats");
        final Column name  = new Column(query,   "Formats", "name");
        final Column mime  = new Column(query,   "Formats", "mime",     "mime",     null, uses);
        final Column type  = new Column(query,   "Formats", "encoding", "encoding", null, uses);
        final String SQL   = query.select(SELECT);

        assertEquals(1, name.indexOf(SELECT));
        assertEquals(2, mime.indexOf(SELECT));
        assertEquals(3, type.indexOf(SELECT));
        assertEquals(Arrays.asList(name, mime, type), query.getColumns(SELECT));
        assertEquals("SELECT \"name\", \"mime\", \"encoding\" FROM \"Formats\"", SQL);
        assertEquals("SELECT \"name\" FROM \"Formats\"", query.select(EXISTS));
        tryStatement(SQL);

        final ListIterator<Column> iterator = query.getColumns(SELECT).listIterator(2);
        assertTrue (iterator.hasNext());
        assertSame (type, iterator.next());
        assertFalse(iterator.hasNext());
        assertTrue (iterator.hasPrevious());
        assertSame (type, iterator.previous());
        assertSame (mime, iterator.previous());
        assertSame (name, iterator.previous());
        assertFalse(iterator.hasPrevious());
        assertEquals(-1, iterator.previousIndex());

        // Tests a non-existant column
        final Column dummy = new Column(query, "Formats", "dummy", "dummy", 10,  (QueryType[]) null);
        final String SQL2 = query.select(SELECT);
        assertEquals("SELECT \"name\", \"mime\", \"encoding\", 10 AS \"dummy\" FROM \"Formats\"", SQL2);
        assertEquals(4, dummy.indexOf(SELECT));
        tryStatement(SQL);
    }

    /**
     * Tests a case similar to the SQL query used in the grid coverage table.
     * This is a more complex case involving joints.
     */
    @Test
    public void testJoins() throws SQLException {
        final Query  query     = new Query(database, "GridCoverages");
        final Column layer     = new Column(query, "Series",         "layer");
        final Column pathname  = new Column(query, "Series",         "pathname");
        final Column filename  = new Column(query, "GridCoverages",  "filename");
        final Column startTime = new Column(query, "GridCoverages",  "startTime", "tmin", null, (QueryType[]) null);
        final Column endTime   = new Column(query, "GridCoverages",  "endTime",   "tmax", null, (QueryType[]) null);
        final Column width     = new Column(query, "GridGeometries", "width");
        final Column height    = new Column(query, "GridGeometries", "height");
        final Column format    = new Column(query, "Series",         "format");
        final String SQL       = query.select(SELECT);

        assertEquals(1, layer    .indexOf(SELECT));
        assertEquals(2, pathname .indexOf(SELECT));
        assertEquals(3, filename .indexOf(SELECT));
        assertEquals(4, startTime.indexOf(SELECT));
        assertEquals(5, endTime  .indexOf(SELECT));
        assertEquals(6, width    .indexOf(SELECT));
        assertEquals(7, height   .indexOf(SELECT));
        assertEquals(8, format   .indexOf(SELECT));
        assertEquals(Arrays.asList(layer, pathname, filename, startTime, endTime, width, height, format),
                query.getColumns(SELECT));
        assertEquals("SELECT \"layer\", \"pathname\", \"filename\", \"startTime\" AS \"tmin\", " +
                "\"endTime\" AS \"tmax\", \"width\", \"height\", \"format\" FROM \"GridCoverages\" " +
                "JOIN \"Series\" ON \"GridCoverages\".\"series\"=\"Series\".\"identifier\" " +
                "JOIN \"GridGeometries\" ON \"GridCoverages\".\"extent\"=\"GridGeometries\".\"identifier\"", SQL);
        tryStatement(SQL);
    }

    /**
     * Tests a case involving parameters.
     */
    @Test
    public void testParameters() throws SQLException {
        final QueryType[] uses = new QueryType[] {SELECT, SELECT_BY_NUMBER, LIST};
        final Query     query      = new Query(database,  "Categories");
        final Column    name       = new Column   (query, "Categories", "name");
        final Column    identifier = new Column   (query, "Categories", "lower", "identifier", null, uses);
        final Column    colors     = new Column   (query, "Categories", "colors", "colors",    null, uses);
        final Parameter byName     = new Parameter(query, name,       SELECT, EXISTS);
        final Parameter byId       = new Parameter(query, identifier, SELECT_BY_NUMBER);
        byId.setComparator("IS NULL OR >=");

        assertEquals(1, name      .indexOf(SELECT));
        assertEquals(1, name      .indexOf(SELECT_BY_NUMBER));
        assertEquals(2, identifier.indexOf(SELECT));
        assertEquals(2, identifier.indexOf(SELECT_BY_NUMBER));
        assertEquals(3, colors    .indexOf(SELECT));
        assertEquals(3, colors    .indexOf(SELECT_BY_NUMBER));
        assertEquals(1, byName    .indexOf(SELECT));
        assertEquals(0, byName    .indexOf(SELECT_BY_NUMBER));
        assertEquals(0, byId      .indexOf(SELECT));
        assertEquals(1, byId      .indexOf(SELECT_BY_NUMBER));
        assertEquals(Arrays.asList(name, identifier, colors), query.getColumns(SELECT));

        String actual = query.select(LIST);
        String expectedAll = "SELECT \"name\", \"lower\" AS \"identifier\", \"colors\" FROM \"Categories\"";
        assertEquals(expectedAll, actual);
        tryStatement(actual);

        actual = query.select(LIST);
        assertEquals(expectedAll, actual);

        actual = query.select(SELECT);
        String expected = expectedAll + " WHERE (\"name\" = ?)";
        assertEquals(expected, actual);

        actual = query.select(EXISTS);
        expected = "SELECT \"name\" FROM \"Categories\" WHERE (\"name\" = ?)";
        assertEquals(expected, actual);

        actual = query.select(SELECT_BY_NUMBER);
        expected = expectedAll + " WHERE (\"identifier\" IS NULL OR \"identifier\" >= ?)";
        assertEquals(expected, actual);
    }
}
