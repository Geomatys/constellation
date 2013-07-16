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
