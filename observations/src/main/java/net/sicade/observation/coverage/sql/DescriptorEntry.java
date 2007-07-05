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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.rmi.RemoteException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.Distribution;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.LocationOffset;
import net.sicade.observation.coverage.DynamicCoverage;
import net.sicade.observation.coverage.FunctionalCoverage;
import net.sicade.observation.sql.ObservableEntry;
import net.sicade.observation.CatalogException;
import net.sicade.observation.ServerException;


/**
 * Implémentation d'une entrée représentant une {@linkplain Descriptor descripteur du paysage océanique}.
 *
 * @version $Id$
 * @author Martin Desruisseaux 
 * @author Antoine Hnawia
 */
public class DescriptorEntry extends ObservableEntry implements Descriptor {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -4266087480414759744L;

    /**
     * L'identifiant du descripteur identité.
     */
    private static final int IDENTITY_ID = 10000;

    /**
     * La position relative.
     */
    private final LocationOffset offset;

    /**
     * Le numéro de bande dans laquelle évaluer les valeurs de pixels des images.
     */
    private final short band;

    /**
     * Une vue des données de ce descripteur comme objet {@link DynamicCoverage}.
     * Ne sera établie à partir de la série la première fois où elle sera nécessaire.
     */
    private transient Reference<DynamicCoverage> coverage;

    /**
     * Construit un nouveau descripteur.
     *
     * @param identifier   L'identifiant du descripteur.
     * @param symbol       Le symbole du descripteur.
     * @param series       La séries de données ({@linkplain net.sicade.observation.Phenomenon phénomène}).
     * @param operation    L'opération associée ({@linkplain net.sicade.observation.Procedure  procédure}).
     * @param band         Le numéro de bande dans laquelle évaluer les valeurs de pixels, à partir de 0.
     * @param offset       La position relative.
     * @param distribution La distribution des données.
     * @param remarks      Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    protected DescriptorEntry(final int            identifier,
                              final String         symbol,
                              final Series         series,
                              final Operation      operation,
                              final short          band,
                              final LocationOffset offset,
                              final Distribution   distribution,
                              final String         remarks)
    {
        super(identifier, symbol, series, operation, distribution, remarks);
        this.band   = band;
        this.offset = offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Series getPhenomenon() { 
        return (Series) super.getPhenomenon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Operation getProcedure() { 
        return (Operation) super.getProcedure();
    }

    /**
     * {@inheritDoc}
     */
    public LocationOffset getLocationOffset() {
        return offset;
    }

    /**
     * {@inheritDoc}
     */
    public short getBand() {
        return band;
    }

    /**
     * {@inheritDoc}
     *
     * @todo La valeur de l'identifiant est codée en dur.
     */
    public boolean isIdentity() {
        return identifier == IDENTITY_ID;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized DynamicCoverage getCoverage() throws CatalogException {
        DynamicCoverage c;
        if (coverage != null) {
            c = coverage.get();
            if (c != null) {
                return c;
            }
            LOGGER.fine("Reconstruit à nouveau la converture de \"" + getName() + "\".");
        }
        c = FunctionalCoverage.getCoverage(getName());
        if (c == null) try {
            c = new DataCoverage(this);
        } catch (RemoteException exception) {
            throw new ServerException(exception);
        }
        coverage = new SoftReference<DynamicCoverage>(c);
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final DescriptorEntry that = (DescriptorEntry) object;
            return                  this.band == that.band &&
                   Utilities.equals(this.offset, that.offset);
        }
        return false;
    }
}
