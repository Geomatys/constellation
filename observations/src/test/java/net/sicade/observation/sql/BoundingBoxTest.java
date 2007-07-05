/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.sql;

// J2SE dependencies
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Sicade dependencies
import net.sicade.observation.coverage.AbstractTest;


/**
 * Test le bon fonctionnement de la colonne "spatialSchema" dans la table GridGeometries.
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
     * Tests l'extraction d'informations depuis une colonne ayant pour type de données une {@code box3d}.
     *
     * @throws SQLException
     */
    public void testGet() throws SQLException {
        if (database.getConnection() instanceof org.postgresql.PGConnection) {
            org.postgresql.PGConnection connec = (org.postgresql.PGConnection)database.getConnection();
            connec.addDataType("box3d", org.postgis.PGbox3d.class);
        }        
        final Statement  s = database.getConnection().createStatement();
        final ResultSet  r = s.executeQuery("SELECT \"spatialExtent\" FROM \"GridGeometries\" " +
                                            "WHERE id='W003'");
        assertTrue(r.next());
        System.out.println(r.getString("spatialExtent"));
        System.out.println(((org.postgis.PGbox3d)r.getObject("spatialExtent")).getValue());
    }
}
