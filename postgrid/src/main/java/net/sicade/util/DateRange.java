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
package net.sicade.util;

import java.util.Date;
import javax.media.jai.util.Range;


/**
 * Une plage de dates. Dans la version actuelle, cette implémentation ne clone pas les dates
 * données en argument ou retournées. Cette classe devrait donc être considérée comme mutable.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DateRange extends Range {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -6400011350250757942L;

    /**
     * Construit une nouvelle plage pour les dates spécifiées.
     * Les dates de départ et de fins sont considérées inclusives.
     */
    public DateRange(final Date startTime, final Date endTime) {
        super(Date.class, startTime, endTime);
    }

    /**
     * Construit une nouvelle plage pour les dates spécifiées.
     */
    public DateRange(final Date startTime, boolean isMinIncluded,
                     final Date   endTime, boolean isMaxIncluded)
    {
        super(Date.class, startTime, isMinIncluded, endTime, isMaxIncluded);
    }

    /**
     * Retourne la date de départ.
     */
    @Override
    public Date getMinValue() {
        return (Date) super.getMinValue();
    }

    /**
     * Retourne la date de fin.
     */
    @Override
    public Date getMaxValue() {
        return (Date) super.getMaxValue();
    }
}
