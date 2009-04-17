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

import java.util.List;
import org.opengis.coverage.Coverage;
import org.geotoolkit.util.XArrays;
import org.geotoolkit.util.Utilities;
import org.constellation.catalog.Entry;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Layer;


/**
 * Classe de base d'une entrée représentant une {@linkplain Model modèle}, pas obligatoirement
 * linéaire. Il peut s'agir par exemple d'un réseau de neurones artificiels.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
abstract class ModelEntry extends Entry implements Model {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5614887885038525651L;

    /**
     * La couche dans laquelle seront stockées les valeurs de la variable dépendante <var>y</var>.
     */
    private final Layer target;

    /**
     * Les distributions de chaque descripteurs. Ne sera construite que la première
     * fois où elle sera nécessaire.
     */
    private transient Distribution[] distributions;

    /**
     * Index des valeurs pour lesquelles une normalisation autre que la transformation identitée
     * est nécessaire.
     */
    private transient int[] index;

    /**
     * Construit un modèle.
     *
     * @param target La couche dans laquelle seront stockées les valeurs de la variable dépendante.
     */
    public ModelEntry(final Layer target) {
        super(target.getName());
        this.target = target;
    }

    /**
     * {inheritDoc}
     */
    public Layer getTarget() {
        return target;
    }

    /**
     * {inheritDoc}
     */
    public void normalize(final double[] values) {
        Distribution[] distributions = this.distributions;
        int[]          index         = this.index;
        if (index == null) {
            // Pas besoin de synchroniser. Ce n'est pas grave si le tableau est construit 2 fois.
            int count = 0;
            final List<Descriptor> descriptors = getDescriptors();
            distributions = new Distribution[descriptors.size()];
            index = new int[distributions.length];
            for (int i=0; i<distributions.length; i++) {
                final Distribution candidate = descriptors.get(i).getDistribution();
                if (!candidate.isIdentity()) {
                    distributions[count] = candidate;
                    index[count++] = i;
                }
            }
            this.distributions = distributions = XArrays.resize(distributions, count);
            this.index         = index         = XArrays.resize(index,         count);
        }
        for (int i=0; i<index.length; i++) {
            final int n = index[i];
            values[n] = distributions[i].normalize(values[n]);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Coverage asCoverage() throws CatalogException {
        return new ModelCoverage(this);
    }

    /**
     * Vérifie si cet objet est égal à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final ModelEntry that = (ModelEntry) object;
            return Utilities.equals(this.target, that.target);
        }
        return false;
    }
}
