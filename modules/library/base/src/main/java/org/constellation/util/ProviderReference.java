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
 * Reference to a constellation provider an all his layers.
 *
 * If DataReference is from a provider layer, the pattern will be like :
 * <code>${providerLayerType|providerId}</code> for example <code>${providerLayerType|shapeFileProvider}</code>
 * Use {@link #PROVIDER_LAYER_TYPE}
 *
 * If DataReference is from a provider Style, the pattern will be like :
 * <code>${providerStyleType|providerId}</code> for example <code>${providerStyleType|sldProvider}</code>
 * use {@link #PROVIDER_STYLE_TYPE}
 *
 * If a dataVersion is used, the pattern will looks like :
 * <code>${providerLayerType|providerId|dataVersion}</code> for example <code>${providerLayerType|versionedShapeFileProvider|1373876676}</code>
 * where dataVersion will be a Date in timestamp format.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class ProviderReference implements CharSequence {

    private static final String SEPARATOR = "|";
    /*
     * Data types
     */
    public static String PROVIDER_LAYER_TYPE = "providerLayerType";
    public static String PROVIDER_STYLE_TYPE = "providerStyleType";

    private String reference;
    private String providerType;
    private String providerId;

    /**
     * Provider data version formated in timestamp. Use only if targeted FeatureStore/CoverageStore support versionning.
     */
    private String dataVersion;

    public ProviderReference(final String reference) {
        this.reference = reference;
        computeReferenceParts();
    }


    public ProviderReference(final String providerType, final String providerId) {
        this(providerType, providerId, null);
    }

    public ProviderReference(final String providerType, final String providerId, final Date dataVersion) {
        if (providerType != null && (providerType.equals(PROVIDER_LAYER_TYPE) || providerType.equals(PROVIDER_STYLE_TYPE))) {
            this.providerType = providerType;
            this.providerId = providerId;
            this.dataVersion = dataVersion != null ? Long.valueOf(dataVersion.getTime()).toString() : null;
        } else {
            throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId} or ${providerStyleType|providerId}.");
        }
    }

    private String buildRefrenceString() {
        final StringBuffer buffer = new StringBuffer("${");
        buffer.append(providerType).append(SEPARATOR);
        buffer.append(providerId);
        if (dataVersion != null) {
            buffer.append(SEPARATOR).append(dataVersion);
        }
        buffer.append("}");
        return buffer.toString();
    }

    private void computeReferenceParts() {
        if (reference != null && reference.startsWith("${") && reference.endsWith("}")) {
            final String datas = reference.substring(2,reference.length()-1);

            final String[] dataSplit = datas.split("\\"+SEPARATOR);
            final int groupCount = dataSplit.length;

            final String datatype = dataSplit[0];
            //get data type
            if (!datatype.isEmpty() && (datatype.equals(PROVIDER_LAYER_TYPE) || datatype.equals(PROVIDER_STYLE_TYPE) ) && (groupCount == 2 || groupCount == 3)) {
                this.providerType = datatype;   //providerType
            } else {
                throw new IllegalArgumentException("Reference data should be type of providerLayerType or providerStyleType.");
            }

            this.providerId = dataSplit[1];     //providerID
            if (groupCount == 3) {
                this.dataVersion = dataSplit[2]; //dataVersion
            }
        } else {
            throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId} or ${providerStyleType|providerId}.");
        }
    }

    public String getReference() {
        return buildRefrenceString();
    }

    public String getProviderType() {
        return providerType;
    }

    public String getProviderId() {
        return providerId;
    }

    /**
     * The dataVersion if not null.
     * @return Date or null if dataVersion not specified.
     */
    public Date getDataVersion() {

        if (dataVersion != null) {
            Long time = Long.valueOf(dataVersion);
            return new Date(time);
        }
        return null;
    }


    @Override
    public int length() {
        return reference.length();
    }

    @Override
    public char charAt(int index) {
        return reference.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return reference.subSequence(start, end);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.reference != null ? this.reference.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProviderReference other = (ProviderReference) obj;
        if ((this.reference == null) ? (other.reference != null) : !this.reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[ProviderReference]");
        sb.append("reference:\n").append(reference).append('\n');
        sb.append("providerType:\n").append(providerType).append('\n');
        sb.append("providerId:\n").append(providerId).append('\n');
        sb.append("dataVersion:\n").append(dataVersion).append('\n');
        return sb.toString();
    }

}
