/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
 * Tests le fonctionnement de {@link ResultSet#getTimestamp(int,Calendar)}.
 *
 * @version $Id: TimeStampTest.java 20 2007-05-22 11:04:09Z cedricbr $
 * @author Martin Desruisseaux
 */
public class BoundingBoxTest extends AbstractTest {
    /**
     * Construit la suite de tests.
     */
    public BoundingBoxTest(final String name) {
        super(name);
    }

    /**
     * Tests la m�thode {@link {@link ResultSet#getTimestamp(int,Calendar)}.
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
