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
import java.util.Date;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.measure.AngleFormat;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.LocatedElement;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;


/**
 * Une entr�e repr�sentant un �l�ment � une certaine position spatio-temporelle.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LocatedEntry extends Entry implements LocatedElement {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 9063512864495392613L;

    /**
     * Objet � utiliser par d�faut pour les �critures des dates.
     */
    private static DateFormat dateFormat;

    /**
     * Objet � utiliser par d�faut pour les �critures des coordonn�es.
     */
    private static AngleFormat angleFormat;

    /**
     * Forme repr�sentant la trajectoire de l'�l�ment. Ne sera construit que la premi�re
     * fois o� elle sera n�cessaire.
     */
    private Shape path;

    /**
     * Position moyenne de l'�lement. Ne sera calcul�e que la premi�re fois o� elle sera n�cessaire.
     */
    private double x, y;

    /**
     * Plage de temps. Ne sera construit que la premi�re fois o� elle sera n�cessaire.
     */
    private DateRange timeRange;

    /**
     * Connexion vers la table des positions.
     * Sera mis � {@code null} lorsqu'elle ne sera plus n�cessaire.
     */
    private transient LocationTable locations;

    /**
     * Construit une entr�e pour le nom sp�cifi�.
     *
     * @param table      La table � utiliser pour localiser cet �l�ment.
     * @param name       Un nom unique identifiant cet �l�ment.
     * @param coordinate Une coordonn�e repr�sentative en degr�s de longitude et de latitude,
     *                   ou {@code null} si inconue.
     * @param timeRange  Plage de temps de cet �l�ment, ou {@code null} si inconue.
     */
    protected LocatedEntry(final LocationTable table,
                           final String         name,
                           final Point2D  coordinate,
                           final DateRange timeRange)
    {
        super(name);
        this.locations = table;
        this.timeRange = timeRange;
        if (coordinate != null) {
            x = coordinate.getX();
            y = coordinate.getY();
        } else {
            x = NaN;
            y = NaN;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getLocation() throws CatalogException {
        final Date     time       = getTime();
        final Point2D  coordinate = getCoordinate();
        final StringBuffer buffer = new StringBuffer();
        final FieldPosition dummy = new FieldPosition(0);
        synchronized (LocatedEntry.class) {
            if (dateFormat == null) {
                dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            }
            if (angleFormat == null) {
                angleFormat = new AngleFormat();
            }
            dateFormat .format(time,                             buffer, dummy); buffer.append(", ");
            angleFormat.format(new Latitude (coordinate.getY()), buffer, dummy); buffer.append(' ');
            angleFormat.format(new Longitude(coordinate.getX()), buffer, dummy);
        }
        return buffer.toString();
    }

    /**
     * V�rifie que les champs {@link #path} et {@link #timeRange} sont initialis�s. Si ce n'est
     * pas le cas, compl�te les informations et "oublie" la connexion � la base de donn�es des
     * positions (sans la fermer, car d'autres peuvent l'utiliser).
     *
     * @throws SQLException si l'interrogation de la base de donn�es a �chou�.
     */
    private synchronized void complete() throws SQLException {
        if (locations != null) {
            synchronized (locations) {
                path = locations.getPath(getName());
                if (isNaN(x) || isNaN(y)) {
                    final Point2D position = locations.getLastPosition();
                    if (position != null) {
                        x = position.getX();
                        y = position.getY();
                    }
                }
                if (timeRange == null) {
                    timeRange = locations.getLastTimeRange();
                }
            }
            locations = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Point2D getCoordinate() throws CatalogException {
        if (isNaN(x) || isNaN(y)) {
            try {
                complete();
            } catch (SQLException exception) {
                throw new ServerException(exception);
            }
            if (isNaN(x) || isNaN(y)) {
                return null;
            }
        }
        return new Point2D.Double(x,y);
    }

    /**
     * {@inheritDoc}
     */
    public Date getTime() throws CatalogException {
        // Note: les dates correspondant � MIN_VALUE ou MAX_VALUE
        //       seront trait�es comme repr�sentant l'infinie.
        final DateRange timeRange = getTimeRange();
        if (timeRange != null) {
            final Date min = timeRange.getMinValue();
            final Date max = timeRange.getMaxValue();
            if (min != null) {
                final long startTime = min.getTime();
                if (startTime > Long.MIN_VALUE) {
                    if (max != null) {
                        final long endTime = max.getTime();
                        if (endTime < Long.MAX_VALUE) {
                            return new Date(startTime + (endTime - startTime)/2);
                        }
                    }
                    return new Date(startTime);
                }
            }
            if (max != null) {
                final long endTime = max.getTime();
                if (endTime < Long.MAX_VALUE) {
                    return new Date(endTime);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public DateRange getTimeRange() throws CatalogException {
        if (timeRange == null) try {
            complete();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return timeRange;
    }

    /**
     * {@inheritDoc}
     */
    public Shape getPath() throws CatalogException {
        try {
            complete();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return path;
    }

    /**
     * {@inheritDoc}
     */
    public boolean intersects(final Rectangle2D rect) throws CatalogException {
        try {
            complete();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        if (path != null) {
            return path.intersects(rect);
        } else {
            return rect.contains(x,y);
        }
    }

    /**
     * Compl�te les informations manquantes avant l'enregistrement binaire de cette entr�e.
     */
    @Override
    protected void preSerialize() throws Exception {
        super.preSerialize();
        complete();
    }
}
