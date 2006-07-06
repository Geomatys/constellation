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

import org.geotools.resources.Utilities;
import net.sicade.observation.Observable;
import net.sicade.observation.Phenomenon;
import net.sicade.observation.Procedure;
import net.sicade.observation.Distribution;


/**
 * Impl�mentation d'une entr�e repr�sentant un {@linkplain Observable observable}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class ObservableEntry extends Entry implements Observable {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 7998601835565062137L;

    /**
     * Identifiant de l'observable.
     */
    protected final int identifier;

    /**
     * R�f�rence vers le {@linkplain Phenomenon ph�nom�ne} observ�.
     */
    private final Phenomenon phenomenon;

    /**
     * R�f�rence vers la {@linkplain Procedure proc�dure} associ�e � cet observable.
     */
    private final Procedure procedure;

    /**
     * R�f�rence vers la {@linkplain Distribution distribution} associ�e � cet observable.
     */
    private final Distribution distribution;

    /**
     * Construit un nouvel observable.
     *
     * @param identifier  L'identifiant de l'observable.
     * @param symbol      Le symbole de l'observable.
     * @param phenomenon  Le ph�nom�ne observ�.
     * @param procedure   La proc�dure associ�e.
     * @param remarks     Remarques s'appliquant � cette entr�e, ou {@code null}.
     */
    protected ObservableEntry(final int          identifier,
                              final String       symbol,
                              final Phenomenon   phenomenon,
                              final Procedure    procedure,
                              final Distribution distribution,
                              final String       remarks)
    {
        super(symbol, remarks);
        this.identifier   = identifier;
        this.phenomenon   = phenomenon;
        this.procedure    = procedure;
        this.distribution = distribution;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNumericIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    public Phenomenon getPhenomenon() {
        return phenomenon;
    }

    /**
     * {@inheritDoc}
     */
    public Procedure getProcedure() {
        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    public Distribution getDistribution() {
        return distribution;
    }

    /**
     * Retourne le code num�rique identifiant cette entr�e.
     */
    @Override
    public int hashCode() {
        return identifier;
    }

    /**
     * Compare cette entr� avec l'object sp�cifi�.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final ObservableEntry that = (ObservableEntry) object;
            return                 (this.identifier == that.identifier) &&
                   Utilities.equals(this.phenomenon,   that.phenomenon) &&
                   Utilities.equals(this.procedure,    that.procedure)  &&
                   Utilities.equals(this.distribution, that.distribution);
        }
        return false;
    }
}
