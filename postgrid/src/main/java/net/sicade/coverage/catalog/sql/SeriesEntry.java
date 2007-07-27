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
package net.sicade.coverage.catalog.sql;

import org.geotools.resources.Utilities;
import net.sicade.sql.Entry;
import net.sicade.coverage.catalog.Format;
import net.sicade.coverage.catalog.Series;


/**
 * Représentation d'une série. Il n'y a pas d'interface correspondant directement à cette
 * entrée, car la division des couches en séries est habituellement cachée aux yeux de
 * l'utilisateur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesEntry extends Entry implements Series {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -7991804359597967276L;

    /**
     * Le format pour les images de cette série.
     */
    private final Format format;

    /**
     * Construit une nouvelle série du nom spécifié.
     *
     * @param name    Le nom de la procédure.
     * @param format  Le format pour les images de cette série.
     * @param remarks Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    protected SeriesEntry(final String name, final Format format, final String remarks) {
        super(name, remarks);
        this.format = format;
    }

    /**
     * {@inheritDoc}
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Compare cette série avec l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SeriesEntry that = (SeriesEntry) object;
            return Utilities.equals(this.format, that.format);
        }
        return false;
    }
}
