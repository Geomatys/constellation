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

// Constellation dependencies
import org.constellation.observation.ProcessEntry;
import org.constellation.observation.fishery.FisheryType;


/**
 * Implémentation d'une entrée représentant un {@linkplain FisheryType type de pêche}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FisheryTypeEntry extends ProcessEntry implements FisheryType {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 6718082896471037388L;

    /**
     * Crée une nouvelle entrée du nom spécifié.
     */
    public FisheryTypeEntry(final String name, final String remarks) {
        super(name, remarks);
    }
}
