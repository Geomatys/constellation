/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.sicade.observation.coverage.sql;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import net.sicade.observation.sql.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests the SQL queries used by the PostGrid database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SchemaTest extends DatabaseTest {
    /**
     * Creates a new instance.
     */
    public SchemaTest() {
    }

    /**
     * Tests the SQL statements used by the {@link ThematicTable}.
     */
    @Test
    public void testThematic() throws SQLException {
        final ThematicTable table = new ThematicTable(database);
        trySelectAll(table);
    }
}
