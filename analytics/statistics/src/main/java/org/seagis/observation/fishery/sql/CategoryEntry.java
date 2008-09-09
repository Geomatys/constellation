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
package net.seagis.observation.fishery.sql;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.seagis.coverage.model.Distribution;
import net.seagis.gml.v311.AbstractTimeGeometricPrimitiveType;
import net.seagis.observation.ElementEntry;
import net.seagis.metadata.MetaDataEntry;
import net.seagis.observation.fishery.Stage;
import net.seagis.observation.fishery.Species;
import net.seagis.observation.fishery.Category;
import net.seagis.observation.fishery.FisheryType;
import net.seagis.observation.ObservationEntry;
import net.seagis.sampling.SamplingFeatureEntry;

/**
 * Implémentation d'une entrée représentant une {@linkplain Category catégorie}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CategoryEntry extends ObservationEntry implements Category {
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
     * @param symbol      Le symbole de la catégorie.
     * @param species     L'espèce observée.
     * @param stage       Le stade de développement.
     * @param procedure   La procédure associée.
     * @param remarks     Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    public CategoryEntry(final String               name,
                         final String               definition,
                         final SamplingFeatureEntry featureOfInterest,
                         final SpeciesEntry         species,
                         final StageEntry           stage,
                         final FisheryTypeEntry     procedure,
                         final ElementEntry         quality,
                         final Object               result,
                         final AbstractTimeGeometricPrimitiveType  samplingTime,
                         final MetaDataEntry        observationMetadata,
                         final AbstractTimeGeometricPrimitiveType  procedureTime,
                         final Object               procedureParameter)
    {
        super(name, definition, featureOfInterest, species, procedure, Distribution.NORMAL, quality, result, samplingTime,
                observationMetadata, procedureTime, procedureParameter);
        this.stage = stage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Species getObservedProperty() {
        return (Species) super.getObservedProperty();
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
