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

// J2SE dependencies

import org.constellation.observation.fishery.Species;
import org.constellation.swe.v101.PhenomenonEntry;

import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;

// Constellation dependencies


/**
 * Implémentation d'une entrée représentant une {@linkplain Species espèce}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SpeciesEntry extends PhenomenonEntry implements Species {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 6568964374209025692L;

    /**
     * Conventions locales pré-définies.
     */
    private static final Locale[] LOCALES = {
        ENGLISH, FRENCH, LATIN, FAO
    };

    /**
     * Les noms de cette espèce selon les différentes langues.
     */
    private final String english, french, latin;

    /**
     * Crée une nouvelle entrée du nom spécifié.
     */
    public SpeciesEntry(final String name,
                        final String english,
                        final String french,
                        final String latin,
                        final String remarks)
    {
        super(name, remarks);
        this.english = english;
        this.french  = french;
        this.latin   = latin;
    }

    /**
     * {@inheritDoc}
     */
    public Locale[] getLocales() {
        return (Locale[]) LOCALES.clone();
    }

    /**
     * {@inheritDoc}
     */
    public String getName(final Locale locale) {
        if (ENGLISH.equals(locale)) return english;
        if (FRENCH .equals(locale)) return french;
        if (LATIN  .equals(locale)) return latin;
        if (FAO    .equals(locale)) return super.getName();
        return null;
    }
    
    /**
     * {@inheritDoc}
     *
     * @deprecated Pas encore implémenté...
     */
    public Icon getIcon() {
        throw new UnsupportedOperationException();
    }
}
