/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.model;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import org.geotools.resources.Utilities;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.GridCoverage;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Entry;


/**
 * Implémentation d'une entrée représentant une {@linkplain Descriptor descripteur du paysage océanique}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
final class DescriptorEntry extends Entry implements Descriptor {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -4266087480414759744L;

    /**
     * L'identifiant du descripteur identité.
     */
    private static final int IDENTITY_ID = 10000;

    /**
     * Identifiant de l'observable.
     */
    protected final int identifier;

    /**
     * Référence vers le {@linkplain Phenomenon phénomène} observé.
     */
    private final Layer phenomenon;

    /**
     * Référence vers la {@linkplain Procedure procédure} associée à cet observable.
     */
    private final Operation procedure;

    /**
     * Référence vers la {@linkplain Distribution distribution} associée à cet observable.
     */
    private final Distribution distribution;

    /**
     * La position relative.
     */
    private final RegionOfInterest offset;

    /**
     * Le numéro de bande dans laquelle évaluer les valeurs de pixels des images.
     */
    private final short band;

    /**
     * Une vue des données de ce descripteur comme objet {@link GridCoverage}.
     * Ne sera établie à partir de la couche la première fois où elle sera nécessaire.
     */
    private transient Reference<GridCoverage> coverage;

    /**
     * Construit un nouveau descripteur.
     *
     * @param identifier   L'identifiant du descripteur.
     * @param symbol       Le symbole du descripteur.
     * @param layer        La couche de données ({@linkplain org.constellation.observation.Phenomenon phénomène}).
     * @param operation    L'opération associée ({@linkplain org.constellation.observation.Procedure  procédure}).
     * @param band         Le numéro de bande dans laquelle évaluer les valeurs de pixels, à partir de 0.
     * @param offset       La position relative.
     * @param distribution La distribution des données.
     * @param remarks      Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    protected DescriptorEntry(final int            identifier,
                              final String         symbol,
                              final Layer          layer,
                              final Operation      operation,
                              final short          band,
                              final RegionOfInterest offset,
                              final Distribution   distribution,
                              final String         remarks)
    {
        super(symbol, remarks);
        this.identifier   = identifier;
        this.phenomenon   = layer;
        this.procedure    = operation;
        this.distribution = distribution;
        this.band         = band;
        this.offset       = offset;
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
    public Layer getLayer() {
        return phenomenon;
    }

    /**
     * {@inheritDoc}
     */
    public Operation getOperation() {
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
     * {@inheritDoc}
     */
    public RegionOfInterest getRegionOfInterest() {
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
    public synchronized GridCoverage getCoverage() throws CatalogException {
        GridCoverage c;
        if (coverage != null) {
            c = coverage.get();
            if (c != null) {
                return c;
            }
            LOGGER.fine("Reconstruit à nouveau la converture de \"" + getName() + "\".");
        }
        c = FunctionalCoverage.getCoverage(getName());
        if (c == null) {
            throw new UnsupportedOperationException("Not yet implemented.");
            // c = new DataCoverage(this);
        }
        coverage = new SoftReference<GridCoverage>(c);
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
            return                 (this.identifier == that.identifier)   &&
                   Utilities.equals(this.phenomenon,   that.phenomenon)   &&
                   Utilities.equals(this.procedure,    that.procedure)    &&
                   Utilities.equals(this.distribution, that.distribution) &&
                   Utilities.equals(this.offset,       that.offset)       &&
                   this.band == that.band;
        }
        return false;
    }
}
