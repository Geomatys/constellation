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

package org.constellation.utils;

import org.apache.sis.metadata.iso.DefaultMetadata;

/**
 * Enumeration of the different types of Metadata used in the application.
 *
 * @author Guilhem Legal (Geomatys)
 */
public enum CstlMetadataTemplate {
    PROVIDER("prov"), // provider
    DATA("data"), // data/layer
    SERVICE("serv");

    private final String prefix;

    private CstlMetadataTemplate(final String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
    
    /**
     * Returns the {@linkplain CstlMetadataTemplate enum value} with the given identifier,
     * or {@code null} if none found.
     *
     * @param metadataId
     * @return A template matching the prefix.
     */
    public static CstlMetadataTemplate valueForPrefix(final String metadataId) {
        if (metadataId.startsWith(PROVIDER.getPrefix())) {
            return PROVIDER;
        }
        if (metadataId.startsWith(SERVICE.getPrefix())) {
            return SERVICE;
        }
        if (metadataId.startsWith(DATA.getPrefix())) {
            return DATA;
        }
        return null;
    }

    /**
     * Determines the {@linkplain DcnsMetadataTemplate metadata template} for a given
     * {@linkplain DefaultMetadata metadata}.
     *
     * @param metadata The metadata for which to retrieve its template.
     * @return A {@linkplain DcnsMetadataTemplate template, or {@code null} if none found
     *         for this metadata.
     */
    public static CstlMetadataTemplate findTemplateForMetadata(final DefaultMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        final String ident = metadata.getFileIdentifier();
        if (ident == null || ident.isEmpty()) {
            return null;
        }
        return valueForPrefix(ident);
    }
}
