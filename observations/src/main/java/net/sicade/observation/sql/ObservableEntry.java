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

import org.geotools.resources.Utilities;
import net.sicade.observation.Observable;
import net.sicade.observation.Phenomenon;
import net.sicade.observation.Procedure;
import net.sicade.observation.Distribution;


/**
 * Implémentation d'une entrée représentant un {@linkplain Observable observable}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class ObservableEntry extends Entry implements Observable {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 7998601835565062137L;

    /**
     * Identifiant de l'observable.
     */
    protected final int identifier;

    /**
     * Référence vers le {@linkplain Phenomenon phénomène} observé.
     */
    private final Phenomenon phenomenon;

    /**
     * Référence vers la {@linkplain Procedure procédure} associée à cet observable.
     */
    private final Procedure procedure;

    /**
     * Référence vers la {@linkplain Distribution distribution} associée à cet observable.
     */
    private final Distribution distribution;

    /**
     * Construit un nouvel observable.
     *
     * @param identifier  L'identifiant de l'observable.
     * @param symbol      Le symbole de l'observable.
     * @param phenomenon  Le phénomène observé.
     * @param procedure   La procédure associée.
     * @param remarks     Remarques s'appliquant à cette entrée, ou {@code null}.
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
     * Retourne le code numérique identifiant cette entrée.
     */
    @Override
    public int hashCode() {
        return identifier;
    }

    /**
     * Compare cette entré avec l'object spécifié.
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
