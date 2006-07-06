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
package net.sicade.observation.fishery.sql;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.fishery.Stage;
import net.sicade.observation.fishery.Species;
import net.sicade.observation.fishery.Category;
import net.sicade.observation.fishery.FisheryType;
import net.sicade.observation.sql.ObservableEntry;
import net.sicade.observation.sql.DistributionEntry;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain Category cat�gorie}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CategoryEntry extends ObservableEntry implements Category {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 3164568338698958014L;

    /**
     * Le stade de d�veloppement.
     */
    private final Stage stage;

    /**
     * Construit une nouvelle cat�gorie.
     *
     * @param identifier  L'identifiant de la cat�gorie.
     * @param symbol      Le symbole de la cat�gorie.
     * @param species     L'esp�ce observ�e.
     * @param stage       Le stade de d�veloppement.
     * @param procedure   La proc�dure associ�e.
     * @param remarks     Remarques s'appliquant � cette entr�e, ou {@code null}.
     */
    protected CategoryEntry(final int          identifier,
                            final String       symbol,
                            final Species      species,
                            final Stage        stage,
                            final FisheryType  procedure,
                            final String       remarks)
    {
        super(identifier, symbol, species, procedure, DistributionEntry.NORMAL, remarks);
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
     * Compare cette entr� avec l'object sp�cifi�.
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
