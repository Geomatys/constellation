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
package net.sicade.observation.fishery.sql;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.coverage.model.Distribution;
import net.sicade.observation.fishery.Stage;
import net.sicade.observation.fishery.Species;
import net.sicade.observation.fishery.Category;
import net.sicade.observation.fishery.FisheryType;
import net.sicade.observation.sql.ObservableEntry;


/**
 * Implémentation d'une entrée représentant une {@linkplain Category catégorie}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CategoryEntry extends ObservableEntry implements Category {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3164568338698958014L;

    /**
     * Le stade de développement.
     */
    private final Stage stage;

    /**
     * Construit une nouvelle catégorie.
     *
     * @param identifier  L'identifiant de la catégorie.
     * @param symbol      Le symbole de la catégorie.
     * @param species     L'espèce observée.
     * @param stage       Le stade de développement.
     * @param procedure   La procédure associée.
     * @param remarks     Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    protected CategoryEntry(final int          identifier,
                            final String       symbol,
                            final Species      species,
                            final Stage        stage,
                            final FisheryType  procedure,
                            final String       remarks)
    {
        super(identifier, symbol, species, procedure, Distribution.NORMAL, remarks);
        this.stage = stage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Species getPhenomenon() {
        return (Species) super.getPhenomenon();
    }
    
    /**
     * {@inheritDoc}
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FisheryType getProcedure() {
        return (FisheryType) super.getProcedure();
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
            final CategoryEntry that = (CategoryEntry) object;
            return Utilities.equals(this.stage, that.stage);
        }
        return false;
    }
}
