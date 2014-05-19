/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.observation.fishery.sql;

// Geotools dependencies
import org.geotoolkit.util.Utilities;

// Constellation dependencies
import org.constellation.coverage.model.Distribution;
import org.constellation.gml.v311.AbstractTimeGeometricPrimitiveType;
import org.constellation.observation.ElementEntry;
import org.constellation.metadata.MetaDataEntry;
import org.constellation.observation.fishery.Stage;
import org.constellation.observation.fishery.Species;
import org.constellation.observation.fishery.Category;
import org.constellation.observation.fishery.FisheryType;
import org.constellation.observation.ObservationEntry;
import org.constellation.sampling.SamplingFeatureEntry;

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
