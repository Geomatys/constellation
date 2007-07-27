/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.catalog;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import org.junit.*;


/**
 * Teste le fonctionnement de {@link ResultSet#getTimestamp(int,Calendar)}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TimeStampTest extends DatabaseTest {
    /**
     * Tests la méthode {@link {@link ResultSet#getTimestamp(int,Calendar)}.
     */
    @Test
    public void testGet() throws SQLException {
        final TimeZone UTC = TimeZone.getTimeZone("UTC");
        final Calendar cal = new GregorianCalendar(UTC, Locale.CANADA);
        final Statement  s = database.getConnection().createStatement();
        final ResultSet  r = s.executeQuery("SELECT \"startTime\", \"startTime\" FROM \"GridCoverages\" " +
                                            "WHERE series='WTH' AND filename='198601'");
        Date t1, t2;
        try {
            assertTrue(r.next());
            t1 = r.getTimestamp(1);
            t2 = r.getTimestamp(2, cal);
            assertFalse(r.next());
        } finally {
            r.close();
            s.close();
        }
        // 'offset' sera expliqué plus bas...
        final int offset = TimeZone.getDefault().getOffset(t1.getTime());
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
        /*
         * Vérifie la valeur formatée par Timestamp. L'heure t1 est construite de façon à ce qu'elle
         * apparaissent correctement lorsque affichée selon le fuseau horaire local, tandis que l'on
         * veut que t2 apparaisse correctement lorsque affichée selon le fuseau horaire UTC. On peut
         * vérifier que c'est bien le cas avec les pilote plus récents (les plus anciens on un bug).
         */
        assertEquals("1986-01-01 00:00:00.0", String.valueOf(t1));
        assertEquals("1986-01-01 00:00:00",        df.format(t1));
        if (offset == 0) {
            assertEquals(t1, t2);
        } else {
            assertFalse("Calendrier ignoré.", String.valueOf(t2).equals("1999-01-01 00:00:00.0"));
        }
        df.setTimeZone(UTC);
        assertEquals("1986-01-01 00:00:00", df.format(t2));
        /*
         * Tentative d'explication de ce qui se passe: offset est le laps de temps (en millisecondes)
         * qu'il faut ajouter au temps UTC afin d'obtenir le temps local. A l'est de Greenwich, cette
         * valeur est positive (par exemple GMT+1 en France). Cela signifie que l'heure t1, qui était
         * affichée comme 00:00 GMT+1, correspond à la valeur -01:00 UTC (ou 23:00 UTC de la veille)
         * en mémoire, puisque toutes les dates sont représentées en heure UTC en Java. Si l'on veut
         * que t2 soit affichée comme 00:00 UTC, on devrait avoir t2 = t1 + 01:00, donc t2 > t1.
         */
        assertEquals("Bug corrigé?", t1.getTime(), t2.getTime() - offset);
        if (false) {
            // Affichage des heures en UTC.
            System.out.println(df.format(t1));
            System.out.println(df.format(t2));
        }
    }
}
