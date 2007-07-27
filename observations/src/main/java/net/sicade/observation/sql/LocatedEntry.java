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
package net.sicade.observation.sql;

import java.util.Date;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;

import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.measure.AngleFormat;

import net.sicade.util.DateRange;
import net.sicade.catalog.Entry;
import net.sicade.observation.LocatedElement;
import net.sicade.coverage.catalog.ServerException;
import net.sicade.coverage.catalog.CatalogException;


/**
 * Une entrée représentant un élément à une certaine position spatio-temporelle.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LocatedEntry extends Entry implements LocatedElement {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 9063512864495392613L;

    /**
     * Objet à utiliser par défaut pour les écritures des dates.
     */
    private static DateFormat dateFormat;

    /**
     * Objet à utiliser par défaut pour les écritures des coordonnées.
     */
    private static AngleFormat angleFormat;

    /**
     * Forme représentant la trajectoire de l'élément. Ne sera construit que la première
     * fois où elle sera nécessaire.
     */
    private Shape path;

    /**
     * Position moyenne de l'élement. Ne sera calculée que la première fois où elle sera nécessaire.
     */
    private double x, y;

    /**
     * Plage de temps. Ne sera construit que la première fois où elle sera nécessaire.
     */
    private DateRange timeRange;

    /**
     * Connexion vers la table des positions.
     * Sera mis à {@code null} lorsqu'elle ne sera plus nécessaire.
     */
    private transient LocationTable locations;

    /**
     * Construit une entrée pour le nom spécifié.
     *
     * @param table      La table à utiliser pour localiser cet élément.
     * @param name       Un nom unique identifiant cet élément.
     * @param coordinate Une coordonnée représentative en degrés de longitude et de latitude,
     *                   ou {@code null} si inconue.
     * @param timeRange  Plage de temps de cet élément, ou {@code null} si inconue.
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
     * Vérifie que les champs {@link #path} et {@link #timeRange} sont initialisés. Si ce n'est
     * pas le cas, complète les informations et "oublie" la connexion à la base de données des
     * positions (sans la fermer, car d'autres peuvent l'utiliser).
     *
     * @throws SQLException si l'interrogation de la base de données a échoué.
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
        // Note: les dates correspondant à MIN_VALUE ou MAX_VALUE
        //       seront traitées comme représentant l'infinie.
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
     * Complete spatio-temporal informations before serialization.
     *
     * @param  out The output stream where to serialize this object.
     * @throws IOException if the serialization failed.
     */
    protected synchronized void writeObject(final ObjectOutputStream out) throws IOException {
        try {
            complete();
        } catch (SQLException exception) {
            final InvalidObjectException e = new InvalidObjectException(exception.toString());
            e.initCause(exception);
            throw e;
        }
        out.defaultWriteObject();
    }
}
