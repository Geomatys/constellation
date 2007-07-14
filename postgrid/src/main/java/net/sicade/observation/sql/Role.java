/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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


/**
 * The role for a {@linkplain Column column} or {@linkplain Parameter parameter}
 * in a SQL {@linkplain Query query}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum Role {
    /**
     * The column or parameter is a textual identifier (primary key) for a record.
     */
    NAME,

    /**
     * The column or parameter is a numerical identifier (primary key) for a record.
     */
    IDENTIFIER,

    /**
     * The column or parameter is a spatial envelope in (<var>x</var>,<var>y</var>,<var>z</var>)
     * dimensions. For spatial enabled database, this is a single column of {@code BOX3D} type.
     * For other databases, this is the first column of a (<var>xmin</var>, <var>xmax</var>,
     * <var>ymin</var>, <var>ymax</var>, <var>zmin</var>, <var>zmax</var>) tupple.
     */
    SPATIAL_ENVELOPE,

    /**
     * The column or parameter is a time range. This is the first column of a
     * (<var>tmin</var>, <var>tmax</var>) tupple.
     */
    TIME_RANGE
}
