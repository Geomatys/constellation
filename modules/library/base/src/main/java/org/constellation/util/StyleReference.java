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
package org.constellation.util;

import java.util.Date;

/**
 * Define a reference to a Style in a StyleProvider.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class StyleReference extends DataReference {

    public StyleReference(String reference) {
        super(checkReference(reference));
    }

    public StyleReference(String providerId, String layerId) {
        super(DataReference.PROVIDER_STYLE_TYPE, providerId, null, null, null, layerId, null);
    }

    /**
     * Static method to ensure valid reference when use {@link #StyleReference(String)} constructor and
     * {@link #setReference(String)} setter.
     * @param reference
     * @return
     */
    private static String checkReference(String reference) {
        DataReference tmpRef = new DataReference(reference);
        if (tmpRef.getDataType().equals(DataReference.PROVIDER_STYLE_TYPE) && tmpRef.getDataVersion() == null) {
            return reference;
        } else {
            throw new IllegalArgumentException("Invalid style reference. Should match pattern ${providerStyleType|styleProviderId|styleId}");
        }
    }

    @Override
    public void setReference(String reference) {
        super.setReference(checkReference(reference));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[StyleReference]");
        sb.append("reference:\n").append(reference).append('\n');
        sb.append("type:\n").append(type).append('\n');
        sb.append("providerId:\n").append(providerId).append('\n');
        sb.append("layerId:\n").append(layerId).append('\n');
        return sb.toString();
    }
}
