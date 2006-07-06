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
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain Descriptor descripteur du paysage oc�anique}.
 *
 * @version $Id$
 * @author Martin Desruisseaux 
 * @author Antoine Hnawia
 */
public class DescriptorEntry extends ObservableEntry implements Descriptor {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -4266087480414759744L;

    /**
     * L'identifiant du descripteur identit�.
     */
    private static final int IDENTITY_ID = 10000;

    /**
     * La position relative.
     */
    private final LocationOffset offset;

    /**
     * Le num�ro de bande dans laquelle �valuer les valeurs de pixels des images.
     */
    private final short band;

    /**
     * Une vue des donn�es de ce descripteur comme objet {@link DynamicCoverage}.
     * Ne sera �tablie � partir de la s�rie la premi�re fois o� elle sera n�cessaire.
     */
    private transient Reference<DynamicCoverage> coverage;

    /**
     * Construit un nouveau descripteur.
     *
     * @param identifier   L'identifiant du descripteur.
     * @param symbol       Le symbole du descripteur.
     * @param series       La s�ries de donn�es ({@linkplain net.sicade.observation.Phenomenon ph�nom�ne}).
     * @param operation    L'op�ration associ�e ({@linkplain net.sicade.observation.Procedure  proc�dure}).
     * @param band         Le num�ro de bande dans laquelle �valuer les valeurs de pixels, � partir de 0.
     * @param offset       La position relative.
     * @param distribution La distribution des donn�es.
     * @param remarks      Remarques s'appliquant � cette entr�e, ou {@code null}.
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
     * @todo La valeur de l'identifiant est cod�e en dur.
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
            LOGGER.fine("Reconstruit � nouveau la converture de \"" + getName() + "\".");
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
