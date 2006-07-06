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
package net.sicade.util;

import java.util.Date;
import javax.media.jai.util.Range;


/**
 * Une plage de dates. Dans la version actuelle, cette impl�mentation ne clone pas les dates
 * donn�es en argument ou retourn�es. Cette classe devrait donc �tre consid�r�e comme mutable.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DateRange extends Range {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -6400011350250757942L;

    /**
     * Construit une nouvelle plage pour les dates sp�cifi�es.
     * Les dates de d�part et de fins sont consid�r�es inclusives.
     */
    public DateRange(final Date startTime, final Date endTime) {
        super(Date.class, startTime, endTime);
    }

    /**
     * Construit une nouvelle plage pour les dates sp�cifi�es.
     */
    public DateRange(final Date startTime, boolean isMinIncluded,
                     final Date   endTime, boolean isMaxIncluded)
    {
        super(Date.class, startTime, isMinIncluded, endTime, isMaxIncluded);
    }

    /**
     * Retourne la date de d�part.
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
