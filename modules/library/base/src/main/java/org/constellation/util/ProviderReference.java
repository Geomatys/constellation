/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.util;

/**
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

    public ProviderReference(final String reference) {
        this.reference = reference;
        computeReferenceParts();
    }



    public ProviderReference(final String providerType, final String providerId) {
        this.providerType = providerType;
        this.providerId = providerId;
    }

    private String buildRefrenceString() {
        final StringBuffer buffer = new StringBuffer("${");
        buffer.append(providerType).append(SEPARATOR);
        buffer.append(providerId);
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
            if (!datatype.isEmpty() && (datatype.equals(PROVIDER_LAYER_TYPE) || datatype.equals(PROVIDER_STYLE_TYPE) ) && groupCount == 2 ) {
                this.providerType = datatype;   //providerType
            } else {
                throw new IllegalArgumentException("Reference data should be type of providerLayerType or providerStyleType.");
            }

            this.providerId = dataSplit[1];     //providerID
        } else {
            throw new IllegalArgumentException("Reference should match pattern ${providerLayerType|providerId} or ${providerStyleType|providerId}.");
        }
    }

    public String getReference() {
        return buildRefrenceString();
    }

    public void setReference(String reference) {
        this.reference = reference;
        computeReferenceParts();
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
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
        int hash = 3;
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

}
