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
package net.sicade.observation.sql;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgis.PGbox3d;
import org.postgresql.PGConnection;

import net.sicade.observation.coverage.AbstractTest;


/**
 * Teste le bon fonctionnement de la colonne "spatialSchema" dans la table GridGeometries.
 * 
 * @version $Id$
 * @author Cédric Briançon
 */
public class BoundingBoxTest extends AbstractTest {
    /**
     * Construit la suite de tests.
     */
    public BoundingBoxTest(final String name) {
        super(name);
    }

    /**
     * Teste l'extraction d'informations depuis une colonne ayant pour type de données une {@code box3d}.
     */
    public void testGet() throws SQLException {
        final Connection c = database.getConnection();
        if (c instanceof PGConnection) {
            final PGConnection pgc = (PGConnection) c;
            pgc.addDataType("box3d", PGbox3d.class);
        }        
        final Statement  s = database.getConnection().createStatement();
        final ResultSet  r = s.executeQuery("SELECT \"spatialExtent\" FROM \"GridGeometries\" " +
                                            "WHERE id='W003'");
        assertTrue(r.next());
        System.out.println(r.getString("spatialExtent"));
        System.out.println(((PGbox3d)r.getObject("spatialExtent")).getValue());
    }
}
