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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.sql;

// J2SE Standard et JAI
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.Calendar;
import static java.lang.Double.isNaN;

// GeoTools
import org.geotools.math.Line;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.resources.geometry.ShapeUtilities;

// Sicade
import net.sicade.util.DateRange;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;


/**
 * Obtient le chemin d'une {@linkplain net.sicade.observation.Station station} ou d'une
 * {@linkplain net.sicade.observation.Platform plateforme}. Cette classe relie les points
 * que contient la base de données pour une même station ou plateforme.
 * <p>
 * Pour calculer la forme de la trajectoire, on récupère l'ensemble des positions et on relie les
 * points entre eux (deux à deux) à l'aide d'un objet {@link GeneralPath}. Mais on ne les relie pas
 * par une ligne droite car cela reviendrait à considérer que la vitesse est constante. On tient
 * compte de la vitesse (en considérant que l'accélération est constante) et la courbe que l'on
 * obtient est une courbe quadratique entre les deux points considérés en s'appuyant sur un point
 * de contrôle. Ce dernier est le point d'intersection des tangentes à la trajectoire, aux deux
 * points considérés.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class LocationTable extends Table implements Shareable {
    /**
     * Obtient le chemin d'une {@linkplain net.sicade.observation.Station station}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Station extends LocationTable {
        /**
         * Requête SQL pour obtenir les positions d'une
         * {@linkplain net.sicade.observation.Station station}.
         */
        private static final ConfigurationKey SELECT = new ConfigurationKey("Stations:PATH",
                "SELECT date, x, y, z, u, v, w\n"   +
                "  FROM \"Locations\"\n"            +
                " WHERE station=? ORDER BY date");

        /**
         * Construit une nouvelle connexion vers la table des positions des stations.
         * 
         * @param database La base de données à laquelle on se connecte.
         */
        public Station(final Database database) {
            super(database, SELECT);
        }
    }

    /**
     * Obtient le chemin d'une {@linkplain net.sicade.observation.Platform platforme}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Platform extends LocationTable {
        /**
         * Requête SQL pour obtenir les positions d'une
         * {@linkplain net.sicade.observation.Platform plateforme}.
         */
        private static final ConfigurationKey SELECT = new ConfigurationKey("Platforms:PATH",
                "SELECT MIN(date) date, AVG(x) AS x, AVG(y) AS y, AVG(z) AS z,"  +
                                      " AVG(u) AS u, AVG(v) AS v, AVG(w) AS w\n" +
                "  FROM \"Locations\"\n"                                         +
                "  JOIN \"Stations\" ON station=identifier\n"                    +
                " WHERE platform=?\n"                                            +
                " GROUP BY station\n"                                            +
                " ORDER BY date");

        /**
         * Construit une nouvelle connexion vers la table des positions des platforme.
         * 
         * @param database La base de données à laquelle on se connecte.
         */
        public Platform(final Database database) {
            super(database, SELECT);
        }
    }

    /** Numéro d'argument. */ private static final int  ARGUMENT_ID = 1;
    /** Numéro de colonne. */ private static final int  DATE = 1;
    /** Numéro de colonne. */ private static final int  X    = 2;
    /** Numéro de colonne. */ private static final int  Y    = 3;
    /** Numéro de colonne. */ private static final int  Z    = 4;
    /** Numéro de colonne. */ private static final int  U    = 5;
    /** Numéro de colonne. */ private static final int  V    = 6;
    /** Numéro de colonne. */ private static final int  W    = 7;

    /**
     * La clé désignant la requête à utiliser.
     */
    private final ConfigurationKey select;

    /**
     * The geodetic calculator for distance computation.
     */
    private final GeodeticCalculator calculator = new GeodeticCalculator();

    /**
     * Position moyenne du dernier element retourné par {@link #getPath}.
     */
    private double averageX=Double.NaN, averageY=Double.NaN;

    /**
     * Date du début et de fin du dernier element retourné par {@link #getPath}.
     */
    private long startTime=Long.MAX_VALUE, endTime=Long.MIN_VALUE;

    /**
     * Construit une nouvelle connexion vers la table des positions des stations ou plateformes.
     * 
     * @param database La base de données à laquelle on se connecte.
     * @param select   La clé désignant la requête SQL à exécuter.
     */
    protected LocationTable(final Database database, final ConfigurationKey select) {
        super(database);
        this.select = select;
    }

    /**
     * Retourne la valeur pour la colonne spécifiée, ou {@code NaN} si la colonne correspondante
     * n'a pas de valeur.
     */
    private static double getDouble(final ResultSet result, final int column) throws SQLException {
        final double valeur = result.getDouble(column);
        return result.wasNull() ? Double.NaN : valeur;
    }

    /**
     * Retourne le chemin correspondant à la {@linkplain net.sicade.observation.Station station} ou
     * à la {@linkplain net.sicade.observation.Platform plateforme} spécifiée. La nature de l'objet
     * identifié (station ou plateforme) dépend de l'argument {@code select} qui a été spécifié au
     * {@linkplain #LocationTable(Database, ConfigurationKey) constructeur} de cette table.
     * 
     * @param  identifier L'identifiant de la station ou de la plateforme concernée.
     * @return Le chemin de la station ou plateforme, ou {@code null} s'il n'y en a pas.
     * @throws SQLException si l'interrogation de la base de données a échoué.
     */
    public synchronized Shape getPath(final String identifier) throws SQLException {
        averageX  = 0;
        averageY  = 0;
        endTime   = Long.MIN_VALUE;
        startTime = Long.MAX_VALUE;
        int    nbPos     = 0;
        long   previousT = Long.MIN_VALUE;
        double previousX = Double.NaN;
        double previousY = Double.NaN;
        double previousU = Double.NaN;
        double previousV = Double.NaN;
        final  Line tangeant1 = new Line();
        final  Line tangeant2 = new Line();
        final  GeneralPath path = new GeneralPath();
        final  PreparedStatement statement = getStatement(select);
        statement.setString(ARGUMENT_ID, identifier);
        final ResultSet result  = statement.executeQuery();
        final Calendar calendar = getCalendar();
        while (result.next()) {
            final long   t = result.getTimestamp(DATE, calendar).getTime();
            final double x = getDouble(result, X);
            final double y = getDouble(result, Y);
            final double u = getDouble(result, U);
            final double v = getDouble(result, V);
            if (t < startTime) startTime = t;
            if (t >   endTime)   endTime = t;
            averageX += x;
            averageY += y;
            nbPos++;
            if (isNaN(previousX) || isNaN(previousY)) {
                path.moveTo((float)x, (float)y);
            } else if (isNaN(previousU) || isNaN(previousV) || isNaN(u) || isNaN(v)) {
                path.lineTo((float)x, (float)y);
            } else {
                final double dt = (t - previousT) / 1000.0; // En secondes
                /*
                 * Set the tangeant line for previous point.
                 */
                double azimuth = 90 - Math.toDegrees(Math.atan2(previousV, previousU));
                double distance = Math.hypot(previousU, previousV) * dt;
                calculator.setStartingGeographicPoint(previousX, previousY);
                calculator.setDirection(azimuth, distance);
                tangeant1.setLine(calculator.getStartingGeographicPoint(),
                                  calculator.getDestinationGeographicPoint());
                /*
                 * Set the tangeant line for current point.
                 */
                azimuth = 90 - Math.toDegrees(Math.atan2(v, u));
                distance = Math.hypot(u, v) * dt;
                calculator.setStartingGeographicPoint(x, y);
                calculator.setDirection(azimuth, distance);
                tangeant1.setLine(calculator.getStartingGeographicPoint(),
                                  calculator.getDestinationGeographicPoint());
                /*
                 * Gets the intersection point and creates the quadratic line accordingly.
                 */
                final Point2D intersect = tangeant1.intersectionPoint(tangeant2);
                path.quadTo((float) intersect.getX(), (float) intersect.getY(), (float) x, (float) y);
            }
        }
        result.close();
        averageX /= nbPos;
        averageY /= nbPos;
        return (nbPos >= 2) ? ShapeUtilities.toPrimitive(path) : null;
    }

    /**
     * Retourne la position moyenne du dernier élément retourné par {@link #getPath},
     * ou {@code null} s'il n'y en a pas.
     */
    public synchronized Point2D getLastPosition() {
        if (isNaN(averageX) || isNaN(averageY)) {
            return null;
        }
        return new Point2D.Double(averageX, averageY);
    }

    /**
     * Retourne la plage de temps du dernier élément retourné par {@link #getPath},
     * ou {@code null} s'il n'y en a pas.
     */
    public synchronized DateRange getLastTimeRange() {
        if (startTime > endTime) {
            return null;
        }
        return new DateRange(new Date(startTime), new Date(endTime));
    }
}
